package es.karmadev.api.schedule.task.scheduler;

import es.karmadev.api.JavaVirtualMachine;
import es.karmadev.api.MemoryUnit;
import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.core.source.KarmaSource;
import es.karmadev.api.core.source.runtime.SourceRuntime;
import es.karmadev.api.logger.LogLevel;
import es.karmadev.api.logger.console.ConsoleLogger;
import es.karmadev.api.schedule.task.ScheduledTask;
import es.karmadev.api.schedule.task.TaskScheduler;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * KarmaAPI asynchronous scheduler
 */
public class AsynchronousScheduler implements TaskScheduler {

    private final ConcurrentLinkedQueue<Task> taskQue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Task> overloadQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean schedulerPaused = new AtomicBoolean();
    private final AtomicBoolean schedulerOverload = new AtomicBoolean();
    private final AtomicInteger completedTasks = new AtomicInteger(0);
    private final AtomicInteger cancelledTasks = new AtomicInteger(0);
    private final Semaphore globalSemaphore;
    private final Map<Class<?>, Semaphore> clazzSemaphores = new ConcurrentHashMap<>();

    private final KarmaSource schedulerSource;
    private final int QUEUE_CAPACITY;

    /**
     * Create an asynchronous scheduler
     */
    public AsynchronousScheduler() {
        this(1000, null, 10, 1);
    }

    /**
     * Create an asynchronous scheduler
     *
     * @param capacity the scheduler max capacity
     */
    public AsynchronousScheduler(final int capacity) {
        this(capacity, null, 10, 1);
    }

    /**
     * Create an asynchronous scheduler
     *
     * @param capacity the scheduler max capacity
     * @param source the scheduler source
     */
    public AsynchronousScheduler(final int capacity, final KarmaSource source) {
        this(capacity, source, 10, 1);
    }

    /**
     * Create an asynchronous scheduler
     *
     * @param capacity the scheduler max capacity
     * @param source the scheduler source
     */
    public AsynchronousScheduler(final int capacity, final KarmaSource source, final int simultaneous) {
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
    public AsynchronousScheduler(final int capacity, final KarmaSource source, final int simultaneous, final int perClass) {
        QUEUE_CAPACITY = Math.max(1, capacity);
        this.schedulerSource = (source != null ? source : KarmaKore.INSTANCE());
        globalSemaphore = new Semaphore(Math.max(2, simultaneous));

        if (source == null) throw new RuntimeException("Failed to create AsynchronousScheduler because the source is not valid");

        int threads = JavaVirtualMachine.cores();
        long memory = JavaVirtualMachine.allocatedMemory(MemoryUnit.GIGABYTES);
        if (memory == 0) memory = JavaVirtualMachine.allocatedMemory(MemoryUnit.MEGABYTES);

        double rs = (double) memory / threads;
        long period = Math.round(rs * 1000);

        ConsoleLogger logger = source.getConsole();
        logger.send(LogLevel.INFO, "Using a period of {0} for the asynchronous scheduler", period);

        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(Math.max(1, threads / 2));
        scheduler.scheduleAtFixedRate(() -> {
            if (globalSemaphore.tryAcquire() && !schedulerOverload.get()) {
                scheduler.execute(() -> {
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
                    }
                });
            }
        }, 0, period, TimeUnit.MILLISECONDS);

        scheduler.scheduleAtFixedRate(() -> {
            int tasks = taskQue.size();
            double load = Math.abs(Math.max(0, JavaVirtualMachine.systemLoad()));

            String percent = String.format("%d%%", (int) load * 100);
            if (load >= 0.75 || tasks > QUEUE_CAPACITY) {
                schedulerOverload.set(load >= 0.75);
                if (schedulerPaused.compareAndSet(false, true)) {
                    logger.send(LogLevel.SEVERE,
                            "Paused asynchronous scheduler because high system load has been detected. System usage: {0} | Queue capacity: {1}/{2}",
                            percent, tasks, capacity);
                }
            } else {
                schedulerOverload.set(false);
                if (schedulerPaused.get()) {
                    if (tasks <= Math.max(0, capacity - 10)) {
                        schedulerPaused.set(false);
                        logger.send(LogLevel.SUCCESS, "Resumed asynchronous scheduler. System usage: {0} | Queue capacity: {1}/{2}", percent, tasks, capacity);
                    }
                }
            }


            if (!overloadQueue.isEmpty() && !schedulerPaused.get()) {
                int sub = overloadQueue.size();
                if (sub >= 90) {
                    int max = 90;
                    CountDownLatch latch = new CountDownLatch(max);
                    while (latch.getCount() != 0) {
                        latch.countDown();
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
        SourceRuntime runtime = schedulerSource.getRuntime();
        Class<?> clazz = AsynchronousScheduler.class;
        try {
            clazz = runtime.getCallerClass();
        } catch (ClassNotFoundException ignored) {}

        Task scheduledTask = new Task(task, clazz);
        if (schedulerPaused.get()) {
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
    public KarmaSource getSource() {
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
        return schedulerPaused.get();
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
