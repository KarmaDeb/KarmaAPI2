package es.karmadev.api.schedule.task.scheduler;

import es.karmadev.api.JavaVirtualMachine;
import es.karmadev.api.MemoryUnit;
import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.runtime.SourceRuntime;
import es.karmadev.api.logger.SourceLogger;
import es.karmadev.api.logger.log.console.LogLevel;
import es.karmadev.api.schedule.runner.async.AsyncTaskExecutor;
import es.karmadev.api.schedule.task.ScheduledTask;
import es.karmadev.api.schedule.task.TaskScheduler;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * KarmaAPI asynchronous scheduler
 */
@SuppressWarnings("unused")
public class BalancedScheduler implements TaskScheduler {

    private final ConcurrentLinkedQueue<Task> taskQue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Task> overloadQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean schedulerOverloaded = new AtomicBoolean();
    private final AtomicBoolean systemOverloaded = new AtomicBoolean();
    private final AtomicInteger completedTasks = new AtomicInteger(0);
    private final AtomicInteger cancelledTasks = new AtomicInteger(0);
    private final Semaphore globalSemaphore;
    private final Map<Class<?>, Semaphore> clazzSemaphores = new ConcurrentHashMap<>();

    private final APISource schedulerSource;
    private final int QUEUE_CAPACITY;

    /**
     * Create an asynchronous scheduler
     */
    public BalancedScheduler() {
        this(1000, null, 10, 1);
    }

    /**
     * Create an asynchronous scheduler
     *
     * @param capacity the scheduler max capacity
     */
    public BalancedScheduler(final int capacity) {
        this(capacity, null, 10, 1);
    }

    /**
     * Create an asynchronous scheduler
     *
     * @param capacity the scheduler max capacity
     * @param source the scheduler source
     */
    public BalancedScheduler(final int capacity, final APISource source) {
        this(capacity, source, 10, 1);
    }

    /**
     * Create an asynchronous scheduler
     *
     * @param capacity the scheduler max capacity
     * @param source the scheduler source
     */
    public BalancedScheduler(final int capacity, final APISource source, final int simultaneous) {
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
    public BalancedScheduler(final int capacity, final APISource source, final int simultaneous, final int perClass) {
        QUEUE_CAPACITY = Math.max(1, capacity);
        this.schedulerSource = (source != null ? source : KarmaKore.INSTANCE());
        globalSemaphore = new Semaphore(Math.max(2, simultaneous));

        if (source == null) throw new RuntimeException("Failed to create AsynchronousScheduler because the source is not valid");

        int threads = JavaVirtualMachine.cores();
        long memory = JavaVirtualMachine.allocatedMemory(MemoryUnit.GIGABYTES);
        if (memory == 0) memory = JavaVirtualMachine.allocatedMemory(MemoryUnit.MEGABYTES);

        double rs = (double) memory / threads;
        long period = Math.round(rs * 1000);

        SourceLogger logger = source.logger();
        logger.send(LogLevel.INFO, "Using a period of {0} for the asynchronous scheduler", period);

        Set<Future<?>> queued = Collections.newSetFromMap(new ConcurrentHashMap<>());

        //ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(Math.max(1, threads / 2));
        AsyncTaskExecutor.EXECUTOR.scheduleAtFixedRate(() -> {
            if (!taskQue.isEmpty()) {
                if (globalSemaphore.tryAcquire() && !systemOverloaded.get()) {
                    Future<?> task = CompletableFuture.runAsync(() -> {
                        Task next = taskQue.peek();
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

                                next.run();
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

        AsyncTaskExecutor.EXECUTOR.scheduleAtFixedRate(() -> {
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
                        Task task = overloadQueue.poll();
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
    public ScheduledTask schedule(final Runnable task) {
        SourceRuntime runtime = schedulerSource.runtime();
        Class<?> clazz = BalancedScheduler.class;
        try {
            clazz = runtime.getCallerClass();
        } catch (ClassNotFoundException ignored) {}

        Task scheduledTask = new Task(task, clazz);
        if (schedulerOverloaded.get()) {
            overloadQueue.add(scheduledTask);
        } else {
            taskQue.add(scheduledTask);
        }

        return scheduledTask;
    }

    /**
     * Get a scheduled task
     *
     * @param id the task id
     * @return the scheduled task
     */
    @Override
    public @Nullable ScheduledTask getTask(final int id) {
        return taskQue.stream().filter((task) -> task.id() == id).findAny().orElse(
                overloadQueue.stream().filter((task) -> task.id() == id).findAny().orElse(null)
        );
    }

    /**
     * Get the scheduler source
     *
     * @return the scheduler source
     */
    @Override
    public APISource getSource() {
        return schedulerSource;
    }

    /**
     * Get the task scheduler size
     *
     * @return the scheduler size
     */
    @Override
    public int size() {
        return taskQue.size();
    }

    /**
     * Get the task scheduler overload queue
     * size
     *
     * @return the overloaded tasks size
     */
    @Override
    public int overloadSize() {
        return overloadQueue.size();
    }

    /**
     * Get if the scheduler is paused
     *
     * @return if the scheduler is paused
     */
    @Override
    public boolean paused() {
        return schedulerOverloaded.get();
    }

    /**
     * Get if the system is overloaded in
     * for this scheduler
     *
     * @return if the system is overloaded
     */
    @Override
    public boolean overloaded() {
        return systemOverloaded.get();
    }

    /**
     * Get the amount of completed tasks
     *
     * @return the completed tasks
     */
    @Override
    public int completed() {
        return completedTasks.get();
    }

    /**
     * Get the amount of cancelled tasks
     *
     * @return the cancelled tasks
     */
    @Override
    public int cancelled() {
        return cancelledTasks.get();
    }
}
