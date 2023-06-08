package es.karmadev.api.core.scheduler;

import es.karmadev.api.JavaVirtualMachine;
import es.karmadev.api.MemoryUnit;
import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.core.KarmaPlugin;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.logger.SourceLogger;
import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.schedule.task.TaskScheduler;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spigot task scheduler
 */
public class SpigotTaskScheduler implements TaskScheduler {

    private final ConcurrentLinkedQueue<SpigotTask> taskQue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<SpigotTask> overloadQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean schedulerOverloaded = new AtomicBoolean();
    private final AtomicBoolean systemOverloaded = new AtomicBoolean();
    private final AtomicInteger completedTasks = new AtomicInteger(0);
    private final AtomicInteger cancelledTasks = new AtomicInteger(0);
    private final Semaphore globalSemaphore;
    private final Map<Class<?>, Semaphore> clazzSemaphores = new ConcurrentHashMap<>();

    private final KarmaPlugin schedulerSource;
    private final int QUEUE_CAPACITY;

    /**
     * Create an asynchronous scheduler
     */
    public SpigotTaskScheduler() {
        this(1000, null, 10, 1);
    }

    /**
     * Create an asynchronous scheduler
     *
     * @param capacity the scheduler max capacity
     */
    public SpigotTaskScheduler(final int capacity) {
        this(capacity, null, 10, 1);
    }

    /**
     * Create an asynchronous scheduler
     *
     * @param capacity the scheduler max capacity
     * @param source the scheduler source
     */
    public SpigotTaskScheduler(final int capacity, final KarmaPlugin source) {
        this(capacity, source, 10, 1);
    }

    /**
     * Create an asynchronous scheduler
     *
     * @param capacity the scheduler max capacity
     * @param source the scheduler source
     */
    public SpigotTaskScheduler(final int capacity, final KarmaPlugin source, final int simultaneous) {
        this(capacity, source, simultaneous, 1);
    }

    /**
     * Create an asynchronous scheduler
     *
     * @param capacity the scheduler max capacity
     * @param source the scheduler owner
     * @param simultaneous the amount of allowed simultaneous schedulers
     * @param perClass the amount fo allowed simultaneous schedulers for class
     */
    public SpigotTaskScheduler(final int capacity, final KarmaPlugin source, final int simultaneous, final int perClass) {
        QUEUE_CAPACITY = Math.max(1, capacity);
        this.schedulerSource = (source != null ? source : (KarmaPlugin) ((APISource) KarmaKore.INSTANCE()));
        globalSemaphore = new Semaphore(Math.max(2, simultaneous));

        if (source == null) throw new RuntimeException("Failed to create AsynchronousScheduler because the source is not valid");

        int threads = JavaVirtualMachine.cores();
        long memory = JavaVirtualMachine.allocatedMemory(MemoryUnit.GIGABYTES);
        if (memory == 0) memory = JavaVirtualMachine.allocatedMemory(MemoryUnit.MEGABYTES);

        double rs = (double) memory / threads;
        long period = Math.round(rs * 1000);

        SourceLogger logger = source.logger();
        logger.send(LogLevel.INFO, "Using a period of {0} for the spigot scheduler", period);

        Set<Future<?>> queued = Collections.newSetFromMap(new ConcurrentHashMap<>());

        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(Math.max(1, threads / 2));
        scheduler.scheduleAtFixedRate(() -> {
            if (!taskQue.isEmpty()) {
                if (globalSemaphore.tryAcquire() && !systemOverloaded.get()) {
                    Future<?> task = scheduler.submit(() -> {
                        SpigotTask next = taskQue.peek();
                        if (next != null) {
                            Semaphore subSemaphore = clazzSemaphores.computeIfAbsent(next.owner(), (p) -> new Semaphore(perClass));
                            if (subSemaphore.tryAcquire()) {
                                taskQue.poll();

                                if (next.cancelled()) {
                                    globalSemaphore.release();
                                    subSemaphore.release();
                                    cancelledTasks.addAndGet(1);
                                    return;
                                }

                                if (next.isSynchronous()) {
                                    schedulerSource.getServer().getScheduler().runTask(schedulerSource, next);
                                } else {
                                    next.run();
                                }
                                completedTasks.addAndGet(1);
                                globalSemaphore.release();
                                subSemaphore.release();
                            }
                        } else {
                            globalSemaphore.release();
                        }
                    });
                    queued.add(task);
                }
            }
        }, 0, period, TimeUnit.MILLISECONDS);

        scheduler.scheduleAtFixedRate(() -> {
            int tasks = taskQue.size();
            double load = Math.abs(Math.max(0, JavaVirtualMachine.systemLoad()));

            String percent = String.format("%d%%", (int) load * 100);
            if (load >= 0.75 || tasks > QUEUE_CAPACITY) {
                if (load >= 0.75) {
                    systemOverloaded.set(true);
                    queued.forEach((task) -> task.cancel(false));
                }

                if (schedulerOverloaded.compareAndSet(false, true)) {
                    logger.send(LogLevel.SEVERE,
                            "Paused asynchronous scheduler because high system load has been detected. System usage: {0} | Queue capacity: {1}/{2}",
                            percent, tasks, capacity);
                }
            } else {
                systemOverloaded.set(false);
                if (schedulerOverloaded.get()) {
                    if (tasks <= Math.max(0, capacity - 10)) {
                        schedulerOverloaded.set(false);
                        logger.send(LogLevel.SUCCESS, "Resumed asynchronous scheduler. System usage: {0} | Queue capacity: {1}/{2}", percent, tasks, capacity);
                    }
                }
            }

            if (!overloadQueue.isEmpty() && !schedulerOverloaded.get()) {
                int sub = overloadQueue.size();
                if (sub >= 90) {
                    int max = 90;
                    for (int i = 0; i < max && !overloadQueue.isEmpty(); i++) {
                        SpigotTask task = overloadQueue.poll();
                        if (task != null) taskQue.add(task);
                    }
                }
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedule a task
     *
     * @param task the task to schedule
     * @return the scheduled task
     */
    @Override
    public SpigotTask schedule(final Runnable task) {
        return null;
    }

    /**
     * Get a scheduled task
     *
     * @param id the task id
     * @return the scheduled task
     */
    @Override
    public @Nullable SpigotTask getTask(final int id) {
        return null;
    }

    /**
     * Get the scheduler source
     *
     * @return the scheduler source
     */
    @Override
    public KarmaSource getSource() {
        return null;
    }

    /**
     * Get the task scheduler size
     *
     * @return the scheduler size
     */
    @Override
    public int size() {
        return 0;
    }

    /**
     * Get the task scheduler overload queue
     * size
     *
     * @return the overloaded tasks size
     */
    @Override
    public int overloadSize() {
        return 0;
    }

    /**
     * Get if the scheduler is paused
     *
     * @return if the scheduler is paused
     */
    @Override
    public boolean paused() {
        return false;
    }

    /**
     * Get if the system is overloaded in
     * for this scheduler
     *
     * @return if the system is overloaded
     */
    @Override
    public boolean overloaded() {
        return false;
    }

    /**
     * Get the amount of completed tasks
     *
     * @return the completed tasks
     */
    @Override
    public int completed() {
        return 0;
    }

    /**
     * Get the amount of cancelled tasks
     *
     * @return the cancelled tasks
     */
    @Override
    public int cancelled() {
        return 0;
    }
}
