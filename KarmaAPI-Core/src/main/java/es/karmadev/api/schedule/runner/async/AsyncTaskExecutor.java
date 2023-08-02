package es.karmadev.api.schedule.runner.async;

import es.karmadev.api.schedule.runner.TaskRunner;
import es.karmadev.api.schedule.runner.TaskStatus;
import es.karmadev.api.schedule.runner.event.TaskEvent;
import es.karmadev.api.schedule.runner.event.TaskRunnerEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Asynchronous task executor
 */
@SuppressWarnings("unused")
public class AsyncTaskExecutor implements TaskRunner {

    public final static ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    private final static AtomicLong GLOBAL_TASK = new AtomicLong(0);
    private final static Map<Long, AsyncTaskExecutor> tasks = new ConcurrentHashMap<>();

    private final AtomicLong TIME_LEFT = new AtomicLong(1);
    private final AtomicLong ELAPSED = new AtomicLong(1);
    private final AtomicLong TASK_ID = new AtomicLong(GLOBAL_TASK.incrementAndGet());
    private final AtomicBoolean UPDATE_TL = new AtomicBoolean(true);
    private final AtomicBoolean REPEATING = new AtomicBoolean(false);
    //private final AtomicReference<ScheduledThreadPoolExecutor> EXECUTOR = new AtomicReference<>();
    private final AtomicReference<TaskStatus> STATUS = new AtomicReference<>(TaskStatus.STOPPED);
    private final AtomicReference<TaskStatus> LAST_STATUS = new AtomicReference<>(TaskStatus.STOPPED);
    protected final List<TaskRunnerEvent<?>> events = new CopyOnWriteArrayList<>();

    private final long interval;
    private long limit;
    private final TimeUnit workingUnit;

    private TaskEvent CONTEXT_EVENT = TaskEvent.START;

    /**
     * Create a new asynchronous task scheduler
     *
     * @param limit the time limit
     * @param workingUnit the working unit
     */
    public AsyncTaskExecutor(final long limit, final TimeUnit workingUnit) {
        this(1, limit, workingUnit);
    }

    /**
     * Create a new asynchronous scheduler
     *
     * @param interval the scheduler interval
     * @param limit the scheduler time limit
     * @param workingUnit the scheduler working unit
     */
    public AsyncTaskExecutor(final long interval, final long limit, final TimeUnit workingUnit) {
        this.interval = interval;
        this.limit = limit;
        this.workingUnit = workingUnit;
        ELAPSED.set(interval);
        TIME_LEFT.set(limit - interval);

        tasks.put(TASK_ID.get(), this);
    }

    /**
     * Get the task ID
     *
     * @return the task ID
     */
    @Override
    public long id() {
        return TASK_ID.get();
    }

    /**
     * Force the task runner time left
     *
     * @param newTimeLeft the new runner time left
     */
    @Override
    public void forceTimeLeft(final long newTimeLeft) {
        ELAPSED.set(Math.max(limit - Math.min(limit, newTimeLeft), 1));
        TIME_LEFT.set(Math.min(newTimeLeft, limit) - interval);

        UPDATE_TL.set(false);
        System.out.println("New time left: " + TIME_LEFT.get());
    }

    /**
     * Force the task runner max time
     *
     * @param newMaxTime the new runner max time
     */
    @Override
    public void forceMaxTime(final long newMaxTime) {
        long oldLimit = this.limit;
        this.limit = newMaxTime;

        if (limit > oldLimit) {
            ELAPSED.set(ELAPSED.get() + (limit - oldLimit)); //Add the difference to the elapsed time
        } else {
            ELAPSED.set(1);
            TIME_LEFT.set(newMaxTime - 1); //Is like if we restarted the timer with a lower value
            UPDATE_TL.set(false);
        }
    }

    /**
     * Set if the task repeats infinitely
     *
     * @param status the task repeat status
     */
    @Override
    public void setRepeating(final boolean status) {
        REPEATING.compareAndSet(!status, status);
    }

    /**
     * Start the task
     */
    @Override
    @SuppressWarnings("unchecked")
    public void start() {
        if (STATUS.get().equals(TaskStatus.STOPPED)) {
            STATUS.set(TaskStatus.RUNNING);

            AtomicReference<ScheduledFuture<?>> future = new AtomicReference<>();
            future.set(EXECUTOR.scheduleAtFixedRate(() -> {
                if (STATUS.get().equals(TaskStatus.STOPPED)) return;

                if (STATUS.get().equals(TaskStatus.RUNNING) || STATUS.get().equals(TaskStatus.RESUMING)) {
                    if (STATUS.get().equals(TaskStatus.RESUMING)) {
                        CONTEXT_EVENT = TaskEvent.RESUME;
                        events.stream().filter((e) -> e.trigger() == null || e.trigger().equals(TaskEvent.RESUME)).forEachOrdered((event) -> {
                            Object runner = event.get();
                            if (runner instanceof Consumer) {
                                ((Consumer<Long>) runner).accept(ELAPSED.getAndAdd(interval));
                            }
                            if (runner instanceof Runnable) {
                                ((Runnable) runner).run();
                            }
                        });

                        STATUS.set(TaskStatus.RUNNING);
                        LAST_STATUS.set(TaskStatus.RESUMING);
                    }

                    long timeLeft = TIME_LEFT.get();

                    CONTEXT_EVENT = TaskEvent.TICK;
                    events.stream().filter((e) -> e.trigger() == null || e.trigger().equals(TaskEvent.TICK)).forEachOrdered((event) -> {
                        Object runner = event.get();
                        if (runner instanceof Consumer) {
                            ((Consumer<Long>) runner).accept(ELAPSED.getAndAdd(interval));
                        }
                        if (runner instanceof Runnable) {
                            ((Runnable) runner).run();
                        }
                    });
                    if (timeLeft == 0) {
                        if (!REPEATING.get()) {
                            future.get().cancel(true);
                            STATUS.set(TaskStatus.STOPPED);

                            CONTEXT_EVENT = TaskEvent.END;
                            events.stream().filter((e) -> e.trigger() == null || e.trigger().equals(TaskEvent.END)).forEachOrdered((event) -> {
                                Object runner = event.get();
                                if (runner instanceof Consumer) {
                                    ((Consumer<Long>) runner).accept(ELAPSED.get());
                                }
                                if (runner instanceof Runnable) {
                                    ((Runnable) runner).run();
                                }
                            });
                        } else {
                            TIME_LEFT.set(this.limit - this.interval);

                            CONTEXT_EVENT = TaskEvent.RESTART;
                            events.stream().filter((e) -> e.trigger() == null || e.trigger().equals(TaskEvent.RESTART)).forEachOrdered((event) -> {
                                Object runner = event.get();
                                if (runner instanceof Consumer) {
                                    ((Consumer<Long>) runner).accept(ELAPSED.get());
                                }
                                if (runner instanceof Runnable) {
                                    ((Runnable) runner).run();
                                }
                            });
                        }

                        ELAPSED.set(this.interval);
                    } else {
                        if (UPDATE_TL.get()) {
                            TIME_LEFT.set(timeLeft - this.interval);
                        }

                        UPDATE_TL.set(true);
                    }
                } else {
                    if (LAST_STATUS.get().equals(TaskStatus.RUNNING) && STATUS.get().equals(TaskStatus.PAUSED)) {
                        LAST_STATUS.set(TaskStatus.PAUSED);

                        CONTEXT_EVENT = TaskEvent.PAUSE;
                        events.stream().filter((e) -> e.trigger() == null || e.trigger().equals(TaskEvent.PAUSE)).forEachOrdered((event) -> {
                            Object runner = event.get();
                            if (runner instanceof Consumer) {
                                ((Consumer<Long>) runner).accept(ELAPSED.get());
                            }
                            if (runner instanceof Runnable) {
                                ((Runnable) runner).run();
                            }
                        });
                    }
                }
            }, 0, interval, workingUnit));

            CONTEXT_EVENT = TaskEvent.START;
            events.stream().filter((e) -> e.trigger() == null || e.trigger().equals(TaskEvent.START)).forEachOrdered((event) -> {
                Object runner = event.get();
                if (runner instanceof Consumer) {
                    ((Consumer<Long>) runner).accept(ELAPSED.get());
                }
                if (runner instanceof Runnable) {
                    ((Runnable) runner).run();
                }
            });
        }
    }

    /**
     * Stops the task completely.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void stop() {
        if (!STATUS.get().equals(TaskStatus.STOPPED)) {
            //EXECUTOR.get().shutdown();
            STATUS.set(TaskStatus.STOPPED);
            LAST_STATUS.set(TaskStatus.STOPPED);

            CONTEXT_EVENT = TaskEvent.STOP;
            events.stream().filter((e) -> e.trigger() == null || e.trigger().equals(TaskEvent.STOP)).forEachOrdered((event) -> {
                Object runner = event.get();
                if (runner instanceof Consumer) {
                    ((Consumer<Long>) runner).accept(ELAPSED.get());
                }
                if (runner instanceof Runnable) {
                    ((Runnable) runner).run();
                }
            });
        }
    }

    /**
     * Pauses the task and tries to
     * store its current state
     */
    @Override
    public void pause() {
        if (STATUS.get().equals(TaskStatus.RUNNING)) {
            STATUS.set(TaskStatus.PAUSED);
            LAST_STATUS.set(TaskStatus.RUNNING);
        }
    }

    /**
     * Resume the task
     */
    @Override
    public void resume() {
        if (STATUS.get().equals(TaskStatus.PAUSED)) {
            STATUS.set(TaskStatus.RESUMING);
            LAST_STATUS.set(TaskStatus.PAUSED);
        }
    }

    /**
     * Get if this task is a repeating
     * task
     *
     * @return if the task repeats constantly
     */
    @Override
    public boolean repeating() {
        return false;
    }

    /**
     * Get the task status
     *
     * @return the task status
     */
    @Override
    public TaskStatus status() {
        return STATUS.get();
    }

    /**
     * Get the task interval
     *
     * @param unit the unit to get the
     *             interval as
     * @return the task interval
     */
    @Override
    public long interval(final TimeUnit unit) {
        return unit.convert(this.interval, this.workingUnit);
    }

    /**
     * Get the time left for this
     * task to end
     *
     * @param unit the unit to get the
     *             time as
     * @return the task time left
     */
    @Override
    public long timeLeft(final TimeUnit unit) {
        return unit.convert(TIME_LEFT.get(), workingUnit);
    }

    /**
     * get the task working unit
     *
     * @return the task working unit
     */
    @Override
    public TimeUnit workingUnit() {
        return workingUnit;
    }

    /**
     * Add an event listener for this task
     *
     * @param event the event
     * @return the task runner
     */
    @Override
    public TaskRunnerEvent<Consumer<Long>> onAny(final BiConsumer<TaskEvent, Long> event) {
        ConsumerRunnerEvent runnerEvent = new ConsumerRunnerEvent(this, null, (time) -> {
            event.accept(CONTEXT_EVENT, time);
        });
        events.add(runnerEvent);
        return runnerEvent;
    }

    /**
     * Add an event listener for this task
     *
     * @param event the event
     * @return the task runner
     */
    @Override
    public TaskRunnerEvent<Runnable> onAny(final Consumer<TaskEvent> event) {
        RunnableRunnerEvent runnerEvent = new RunnableRunnerEvent(this, null, () -> event.accept(CONTEXT_EVENT));
        events.add(runnerEvent);
        return runnerEvent;
    }

    /**
     * Add an event listener for this task
     *
     * @param event the event
     * @param tick  the event consumer and the current tick
     * @return the task runner
     */
    @Override
    public TaskRunnerEvent<Consumer<Long>> on(final TaskEvent event, final Consumer<Long> tick) {
        ConsumerRunnerEvent runnerEvent = new ConsumerRunnerEvent(this, event, tick);
        events.add(runnerEvent);
        return runnerEvent;
    }

    /**
     * Add an event listener for this task
     *
     * @param event  the event
     * @param action the event consumer
     * @return the task runner
     */
    @Override
    public TaskRunnerEvent<Runnable> on(final TaskEvent event, final Runnable action) {
        RunnableRunnerEvent runnerEvent = new RunnableRunnerEvent(this, event, action);
        events.add(runnerEvent);
        return runnerEvent;
    }

    /**
     * Unhook a task event
     *
     * @param event the event
     */
    @Override
    public void off(final TaskRunnerEvent<?> event) {
        events.remove(event);
    }

    /**
     * Get a task executor by its ID
     *
     * @param id the task executor ID
     * @return the task executor
     */
    @Nullable
    public static AsyncTaskExecutor getExecutor(final long id) {
        return tasks.getOrDefault(id, null);
    }
}
