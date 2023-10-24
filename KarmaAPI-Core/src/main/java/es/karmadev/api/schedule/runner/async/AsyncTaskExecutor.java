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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Asynchronous task executor
 */
@SuppressWarnings("unused")
public class AsyncTaskExecutor implements TaskRunner<Long> {

    public final static ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    private final static AtomicLong globalTask = new AtomicLong(0);
    private final static Map<Long, AsyncTaskExecutor> taskInstances = new ConcurrentHashMap<>();

    private final AtomicInteger currentSecond = new AtomicInteger(0);
    private final AtomicLong timeLeft = new AtomicLong(1);
    private final AtomicLong elapsedTime = new AtomicLong(1);
    private final AtomicLong currentId = new AtomicLong(globalTask.incrementAndGet());
    private final AtomicBoolean updateTL = new AtomicBoolean(true);
    private final AtomicBoolean isRepeating = new AtomicBoolean(false);
    //private final AtomicReference<ScheduledThreadPoolExecutor> EXECUTOR = new AtomicReference<>();
    private final AtomicReference<TaskStatus> taskStatus = new AtomicReference<>(TaskStatus.STOPPED);
    private final AtomicReference<TaskStatus> lastTaskStatus = new AtomicReference<>(TaskStatus.STOPPED);
    protected final List<TaskRunnerEvent<?>> taskEvents = new CopyOnWriteArrayList<>();

    private final long interval;
    private long limit;
    private final TimeUnit workingUnit;

    private TaskEvent currentContext = TaskEvent.START;

    /**
     * Create a new asynchronous task scheduler
     *
     * @param limit the time limit
     * @param workingUnit the working unit
     */
    public AsyncTaskExecutor(final Number limit, final TimeUnit workingUnit) {
        this(1, limit, workingUnit);
    }

    /**
     * Create a new asynchronous scheduler
     *
     * @param interval the scheduler interval
     * @param limit the scheduler time limit
     * @param workingUnit the scheduler working unit
     */
    public AsyncTaskExecutor(final Number interval, final Number limit, final TimeUnit workingUnit) {
        this.interval = interval.longValue();
        this.limit = limit.longValue();
        this.workingUnit = workingUnit;
        elapsedTime.set(interval.longValue());
        timeLeft.set(limit.longValue() - interval.longValue());

        taskInstances.put(currentId.get(), this);
    }

    /**
     * Get the task ID
     *
     * @return the task ID
     */
    @Override
    public long id() {
        return currentId.get();
    }

    /**
     * Force the task runner time left
     *
     * @param newTimeLeft the new runner time left
     */
    @Override
    public void forceTimeLeft(final Long newTimeLeft) {
        elapsedTime.set(Math.max(limit - Math.min(limit, newTimeLeft), 1));
        timeLeft.set(Math.min(newTimeLeft, limit) - interval);

        updateTL.set(false);
    }

    /**
     * Force the task runner max time
     *
     * @param newMaxTime the new runner max time
     */
    @Override
    public void forceMaxTime(final Long newMaxTime) {
        long oldLimit = this.limit;
        this.limit = newMaxTime;

        if (limit > oldLimit) {
            elapsedTime.set(elapsedTime.get() + (limit - oldLimit)); //Add the difference to the elapsed time
        } else {
            elapsedTime.set(1);
            timeLeft.set(newMaxTime - 1); //Is like if we restarted the timer with a lower value
            updateTL.set(false);
        }
    }

    /**
     * Set if the task repeats infinitely
     *
     * @param status the task repeat status
     */
    @Override
    public void setRepeating(final boolean status) {
        isRepeating.compareAndSet(!status, status);
    }

    /**
     * Start the task
     */
    @Override
    public void start() {
        if (!status().equals(TaskStatus.STOPPED)) return;
        setStatus(TaskStatus.RUNNING, false);

        AtomicReference<ScheduledFuture<?>> future = new AtomicReference<>();
        future.set(EXECUTOR.scheduleAtFixedRate(() -> EXECUTOR.submit(() -> {
            if (status().equals(TaskStatus.STOPPED)) return;

            if (status().equals(TaskStatus.RUNNING)) {
                long timeLeft = this.timeLeft.get();

                executeEvents(TaskEvent.TICK, true);

                if (timeLeft == 0) {
                    if (!isRepeating.get()) {
                        future.get().cancel(true);
                        setStatus(TaskStatus.STOPPED, false);
                        executeEvents(TaskEvent.END, false);
                    } else {
                        this.timeLeft.set(this.limit - this.interval);
                        executeEvents(TaskEvent.RESTART, false);
                    }

                    elapsedTime.set(this.interval);
                }

                if (updateTL.get()) {
                    this.timeLeft.set(timeLeft - this.interval);
                }
                updateTL.set(true);

                return;
            }

            if (status().equals(TaskStatus.RESUMING)) {
                executeEvents(TaskEvent.RESUME, true);
                setStatus(TaskStatus.RUNNING, true);
            }

            if (lastTaskStatus.get().equals(TaskStatus.RUNNING) && status().equals(TaskStatus.PAUSED)) {
                lastTaskStatus.set(TaskStatus.PAUSED);
                executeEvents(TaskEvent.PAUSE, false);
            }
        }), 0, interval, workingUnit));
        executeEvents(TaskEvent.START, false);
    }

    /**
     * Stops the task completely.
     */
    @Override
    public void stop() {
        if (taskStatus.get().equals(TaskStatus.STOPPED)) return;

        taskStatus.set(TaskStatus.STOPPED);
        lastTaskStatus.set(TaskStatus.STOPPED);

        executeEvents(TaskEvent.STOP, false);
    }

    /**
     * Pauses the task and tries to
     * store its current state
     */
    @Override
    public void pause() {
        if (status().equals(TaskStatus.RUNNING))
            setStatus(TaskStatus.PAUSED, true);;
    }

    /**
     * Resume the task
     */
    @Override
    public void resume() {
        if (taskStatus.get().equals(TaskStatus.PAUSED))
            setStatus(TaskStatus.RESUMING, true);
    }

    /**
     * Get if this task is a repeating
     * task
     *
     * @return if the task repeats constantly
     */
    @Override
    public boolean repeating() {
        return isRepeating.get();
    }

    /**
     * Get the task status
     *
     * @return the task status
     */
    @Override
    public TaskStatus status() {
        return taskStatus.get();
    }

    /**
     * Get the task interval
     *
     * @param unit the unit to get the
     *             interval as
     * @return the task interval
     */
    @Override
    public Long interval(final TimeUnit unit) {
        return unit.convert((long) Math.ceil(this.interval - 0.1), this.workingUnit);
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
    public Long timeLeft(final TimeUnit unit) {
        return unit.convert((long) Math.ceil(timeLeft.get()), workingUnit);
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
        ConsumerRunnerEvent<Long> runnerEvent = ConsumerRunnerEvent.forType(this, null, (time) -> {
            event.accept(currentContext, time);
        });
        taskEvents.add(runnerEvent);

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
        RunnableRunnerEvent runnerEvent = new RunnableRunnerEvent(this, null, () -> event.accept(currentContext));
        taskEvents.add(runnerEvent);
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
        ConsumerRunnerEvent<Long> runnerEvent = ConsumerRunnerEvent.forType(this, event, tick);
        taskEvents.add(runnerEvent);
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
        taskEvents.add(runnerEvent);
        return runnerEvent;
    }

    /**
     * Unhook a task event
     *
     * @param event the event
     */
    @Override
    public void off(final TaskRunnerEvent<?> event) {
        taskEvents.remove(event);
    }

    /**
     * Get a task executor by its ID
     *
     * @param id the task executor ID
     * @return the task executor
     */
    @Nullable
    public static AsyncTaskExecutor getExecutor(final long id) {
        return taskInstances.get(id);
    }

    /**
     * Update the task status
     *
     * @param newStatus the new task status
     * @param updatePrevious update the previous task
     */
    private void setStatus(final TaskStatus newStatus, final boolean updatePrevious) {
        TaskStatus previous = lastTaskStatus.get();
        taskStatus.set(newStatus);
        if (updatePrevious)
            lastTaskStatus.set(previous);
    }

    @SuppressWarnings("unchecked")
    private void executeEvents(final TaskEvent taskEvent, final boolean add) {
        long elapsed = (add ? elapsedTime.getAndAdd(interval) : elapsedTime.get());

        currentContext = taskEvent;
        taskEvents.stream().filter((e) -> e.trigger() == null || e.trigger().equals(taskEvent)).forEachOrdered((event) -> {
            Object runner = event.get();
            if (runner instanceof Consumer) {
                ((Consumer<Number>) runner).accept(elapsed);
                return;
            }

            if (runner instanceof Runnable) {
                ((Runnable) runner).run();
            }
        });
    }
}
