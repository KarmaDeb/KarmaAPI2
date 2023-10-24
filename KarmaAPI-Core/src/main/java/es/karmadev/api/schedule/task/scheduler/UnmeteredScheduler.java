package es.karmadev.api.schedule.task.scheduler;

import es.karmadev.api.JavaVirtualMachine;
import es.karmadev.api.MemoryUnit;
import es.karmadev.api.core.KarmaKore;
import es.karmadev.api.core.source.APISource;
import es.karmadev.api.core.source.runtime.SourceRuntime;
import es.karmadev.api.logger.SourceLogger;
import es.karmadev.api.schedule.task.ScheduledTask;
import es.karmadev.api.schedule.task.TaskScheduler;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * KarmaAPI asynchronous scheduler
 */
@SuppressWarnings("unused")
public class UnmeteredScheduler implements TaskScheduler {

    public final static ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    private final ConcurrentLinkedQueue<Task> taskQue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger completedTasks = new AtomicInteger(0);
    private final AtomicInteger cancelledTasks = new AtomicInteger(0);

    private final APISource schedulerSource;

    /**
     * Create an asynchronous scheduler
     */
    public UnmeteredScheduler() {
        this(null);
    }

    /**
     * Create an asynchronous scheduler
     *
     * @param source the scheduler owner
     */
    public UnmeteredScheduler(final APISource source) {
        this.schedulerSource = (source != null ? source : KarmaKore.INSTANCE());
        if (source == null) throw new RuntimeException("Failed to create unmetered scheduler because the source is not valid");

        int threads = JavaVirtualMachine.cores();
        long memory = JavaVirtualMachine.allocatedMemory(MemoryUnit.GIGABYTES);
        if (memory == 0) memory = JavaVirtualMachine.allocatedMemory(MemoryUnit.MEGABYTES);

        double rs = (double) memory / threads;
        long period = Math.round(rs * 1000);

        SourceLogger logger = source.logger();
        //logger.send(LogLevel.INFO, "Using a period of {0} for the asynchronous scheduler", period);

        Set<Future<?>> queued = Collections.newSetFromMap(new ConcurrentHashMap<>());

        //ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(Math.max(1, threads / 2));
        EXECUTOR.scheduleAtFixedRate(() -> {
            if (!taskQue.isEmpty()) {
                Future<?> task = CompletableFuture.runAsync(() -> {
                    Task next = taskQue.peek();
                    if (next != null) {
                        taskQue.poll();

                        if (next.cancelled()) {
                            cancelledTasks.addAndGet(1);
                            return;
                        }

                        next.run();
                        completedTasks.addAndGet(1);
                    }
                });
                queued.add(task);
            }
        }, 0, period, TimeUnit.MILLISECONDS);
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
        Class<?> clazz = UnmeteredScheduler.class;
        try {
            clazz = runtime.getCallerClass();
        } catch (ClassNotFoundException ignored) {}

        Task scheduledTask = new Task(task, clazz);
        taskQue.add(scheduledTask);
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
        return taskQue.stream().filter((task) -> task.id() == id).findAny().orElse(null);
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
        return -1;
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
