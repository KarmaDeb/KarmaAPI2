package es.karmadev.api.spigot.tracker.stand;

import com.google.common.util.concurrent.AtomicDouble;
import es.karmadev.api.spigot.entity.trace.RayDirection;
import es.karmadev.api.spigot.entity.trace.TraceOption;
import es.karmadev.api.spigot.entity.trace.ray.impl.AsyncRayTrace;
import es.karmadev.api.spigot.entity.trace.ray.impl.SyncRayTrace;
import es.karmadev.api.spigot.entity.trace.result.RayTraceResult;
import es.karmadev.api.spigot.tracker.AutoTracker;
import es.karmadev.api.spigot.tracker.ConstantProperty;
import es.karmadev.api.spigot.tracker.TrackerEntity;
import es.karmadev.api.spigot.tracker.event.*;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static es.karmadev.api.spigot.tracker.ConstantProperty.*;

/**
 * Tracker armor stand
 */
public class AsyncTrackerStand extends TrackerEntity {

    private final World world;
    private final double x;
    private final double y;
    private final double z;

    private ArmorStand stand;
    private BukkitTask rotTask;
    private BukkitTask trackTask;
    private LivingEntity target;
    private LivingEntity lastTarget;
    private AutoTracker tracker;

    @Getter
    private EulerAngle initialAngle;

    private EulerAngle idleRotation;
    private Consumer<Void> idleRotationEnd;

    public final static ConstantProperty<Boolean> PROPERTY_SMALL = ConstantProperty.customProperty("tracker_small", false, Boolean.class);
    public final static ConstantProperty<Boolean> PROPERTY_BASE_PLATE = ConstantProperty.customProperty("tracker_bsp", false, Boolean.class);
    public final static ConstantProperty<Boolean> PROPERTY_INVINCIBLE = ConstantProperty.customProperty("tracker_invincible", true, Boolean.class);
    public final static ConstantProperty<Boolean> PROPERTY_INVISIBLE = ConstantProperty.customProperty("tracker_invisible", false, Boolean.class);
    public final static ConstantProperty<Boolean> PROPERTY_SHOW_NAME = ConstantProperty.customProperty("tracker_displayname", false, Boolean.class);
    public final static ConstantProperty<Boolean> PROPERTY_SHOW_ARMS = ConstantProperty.customProperty("tracker_arms", false, Boolean.class);
    public final static ConstantProperty<Boolean> PROPERTY_ITERABLE = ConstantProperty.customProperty("tracker_iterable", false, Boolean.class);
    public final static ConstantProperty<String> PROPERTY_NAME = ConstantProperty.customProperty("tracker_name", "", String.class);
    public final static ConstantProperty<ItemStack> PROPERTY_HELMET = ConstantProperty.customProperty("tracker_helmet", new ItemStack(Material.AIR), ItemStack.class);
    public final static ConstantProperty<ItemStack> PROPERTY_CHESTPLATE = ConstantProperty.customProperty("tracker_chestplate", new ItemStack(Material.AIR), ItemStack.class);
    public final static ConstantProperty<ItemStack> PROPERTY_LEGGINGS = ConstantProperty.customProperty("tracker_leggings", new ItemStack(Material.AIR), ItemStack.class);
    public final static ConstantProperty<ItemStack> PROPERTY_BOOTS = ConstantProperty.customProperty("tracker_boots", new ItemStack(Material.AIR), ItemStack.class);

    /**
     * Precache the assignments
     *
     * @throws IllegalAccessException shouldn't be thrown
     */
    public static void precacheAssignments() throws IllegalAccessException {
        Field[] fields = AsyncTrackerStand.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(ConstantProperty.class)) {
                ConstantProperty<?> property = (ConstantProperty<?>) field.get(AsyncTrackerStand.class);
                property.assignTo(AsyncTrackerStand.class);
            }
        }
    }

    /**
     * Create the tracker stand
     *
     * @param owner the tracker owner
     * @param world the tracker world
     * @param x the tracker x position
     * @param y the tracker y position
     * @param z the tracker z position
     */
    public AsyncTrackerStand(final Plugin owner, final World world, final double x, final double y, final double z) {
        super(owner);
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;

        PROPERTY_SMALL.set(this, false);
        PROPERTY_BASE_PLATE.set(this, false);
        PROPERTY_INVINCIBLE.set(this, true);
        PROPERTY_INVISIBLE.set(this, false);
        PROPERTY_SHOW_NAME.set(this, false);
        PROPERTY_SHOW_ARMS.set(this, false);
        PROPERTY_ITERABLE.set(this, false);
        PROPERTY_NAME.set(this, "");
        PROPERTY_HELMET.set(this, null);
        PROPERTY_CHESTPLATE.set(this, null);
        PROPERTY_LEGGINGS.set(this, null);
        PROPERTY_BOOTS.set(this, null);

        TRACKER_RADIUS.set(this, 8d);
        ROTATION_SMOOTHNESS.set(this, 0d);
        ROTATION_FRAMES.set(this, 20d);
        ALWAYS_TRACK.set(this, false);
        TRACK_LOCK.set(this, true);
        PRECISION.set(this, SyncRayTrace.HIGH_PRECISION);
        TOLERANCE.set(this, 0.05);
    }

    /**
     * Animate the tracker to rotate until the
     * target angle
     *
     * @param targetAngle the target angle
     */
    public void setIdleAngle(final EulerAngle targetAngle, final Consumer<Void> onEnd) {
        idleRotation = targetAngle;
        idleRotationEnd = onEnd;
    }

    /**
     * Set the tracker initial angle
     *
     * @param startPos the start angle
     */
    public void setInitialAngle(final EulerAngle startPos) {
        initialAngle = startPos;
    }

    /**
     * Get the tracker world
     *
     * @return the tracker world
     */
    @Override
    public World getWorld() {
        return world;
    }

    /**
     * Get the tracker position X
     *
     * @return the tracker X position
     */
    @Override
    public double getX() {
        return x;
    }

    /**
     * Get the tracker position Y
     *
     * @return the tracker Y position
     */
    @Override
    public double getY() {
        return y;
    }

    /**
     * Get the tracker position Z
     *
     * @return the tracker Z position
     */
    @Override
    public double getZ() {
        return z;
    }

    /**
     * Get track target
     *
     * @return the target
     */
    @Override
    public Optional<LivingEntity> getTarget() {
        return Optional.ofNullable(target);
    }

    /**
     * Get the tracker auto tracker
     *
     * @return the auto tracker
     */
    @Override
    public Optional<AutoTracker> getTracker() {
        return Optional.ofNullable(tracker);
    }

    /**
     * Get the entity that is the
     * tracker
     *
     * @return the entity tracker
     */
    @Override
    public Entity getEntity() {
        return stand;
    }

    /**
     * Create a raytrace from this tracker
     * to an entity
     *
     * @param target the entity
     * @return the raytrace
     */
    @Override
    public Optional<SyncRayTrace> createRayTrace(final LivingEntity target) {
        SyncRayTrace rayTrace = null;
        if (stand != null) {
            Location location  = stand.getLocation();
            rayTrace = SyncRayTrace.createRayTrace(location, target);
            rayTrace.setPrecision(PRECISION.get(this));
            rayTrace.setTolerance(TOLERANCE.get(this));

            if (location.getY() < target.getLocation().getY()) {
                rayTrace.setDirection(RayDirection.DOWN_TO_UP); //From stand (down) bottom to entity bottom (up)
            }

            //rayTrace.setDirection(RayDirection.UP_TO_DOWN);
        }

        return Optional.ofNullable(rayTrace);
    }

    /**
     * Create an asynchronous ray trace from this tracker
     * to an entity
     *
     * @param target the entity
     * @return the raytrace
     */
    @Override
    public Optional<AsyncRayTrace> createAsyncRayTrace(final LivingEntity target) {
        AsyncRayTrace rayTrace = null;
        if (stand != null) {
            Location location  = stand.getLocation();
            rayTrace = AsyncRayTrace.createRayTrace(location, target);
            rayTrace.setPrecision(PRECISION.get(this));
            rayTrace.setTolerance(TOLERANCE.get(this));

            if (location.getY() < target.getLocation().getY()) {
                rayTrace.setDirection(RayDirection.DOWN_TO_UP); //From stand (down) bottom to entity bottom (up)
            }

            //rayTrace.setDirection(RayDirection.UP_TO_DOWN);
        }

        return Optional.ofNullable(rayTrace);
    }

    /**
     * Get the current track direction
     *
     * @return the track direction
     */
    @Override
    public Vector getDirection() {
        ArmorStand stand = this.stand;
        LivingEntity target = this.target;
        if (stand != null && target != null) {
            Location start = stand.getEyeLocation().clone();
            Location end = target.getLocation().clone();

            //boolean small = stand.isSmall();
            //start.add(0, (small ? 0.5 : 1.5), 0);
            return end.toVector().subtract(start.toVector()).normalize();
        }

        return new Vector(0, 0, 0);
    }

    /**
     * Set the tracker auto tracker
     *
     * @param tracker the auto tracker
     */
    @Override
    public void setTracker(final AutoTracker tracker) {
        this.tracker = tracker;
    }

    /**
     * Set the tracker target
     *
     * @param entity the target
     */
    @Override
    public void setTrackTarget(LivingEntity entity) {
        LivingEntity old = this.target;
        if (old != null) {
            if (entity != null) {
                TrackerSwitchTrackEvent switchEvent = new TrackerSwitchTrackEvent(this, old, entity, TrackerSwitchTrackEvent.SwitchReason.FORCED);
                Bukkit.getServer().getPluginManager().callEvent(switchEvent);

                entity = switchEvent.getNewEntity();
            }

            TrackerStopTrackEvent event = new TrackerStopTrackEvent(this, old, TrackerStopTrackEvent.StopReason.FORCED);
            Bukkit.getServer().getPluginManager().callEvent(event);
        }

        this.target = entity; //Thread-safe
    }

    /**
     * Start the track task
     *
     * @param period the track period in ticks
     */
    @Override
    public void start(final long period) {
        BukkitTask rotateTask = this.rotTask;
        BukkitTask task = this.trackTask;

        if (task == null || !Bukkit.getServer().getScheduler().isCurrentlyRunning(task.getTaskId()) || rotateTask == null || !Bukkit.getServer().getScheduler().isCurrentlyRunning(rotateTask.getTaskId())) {
            stop();

            ArmorStand tmpStand = this.stand;
            if (tmpStand != null && tmpStand.isValid()) tmpStand.remove();

            Location unknownPosition = new Location(world, 0, -64, 0);
            tmpStand = (ArmorStand) world.spawnEntity(unknownPosition, EntityType.ARMOR_STAND);

            tmpStand.setSmall(PROPERTY_SMALL.get(this));
            tmpStand.setBasePlate(PROPERTY_BASE_PLATE.get(this));
            if (PROPERTY_INVINCIBLE.get(this)) {
                tmpStand.setNoDamageTicks(Integer.MAX_VALUE);
            }
            tmpStand.setVisible(!PROPERTY_INVISIBLE.get(this));
            tmpStand.setCustomNameVisible(PROPERTY_SHOW_NAME.get(this));
            tmpStand.setCustomName(ChatColor.translateAlternateColorCodes('&', PROPERTY_NAME.get(this)));
            tmpStand.setArms(PROPERTY_SHOW_ARMS.get(this));
            tmpStand.setCanPickupItems(PROPERTY_ITERABLE.get(this));

            if (initialAngle != null) {
                tmpStand.setHeadPose(initialAngle);
            } else {
                initialAngle = tmpStand.getHeadPose();
            }

            Location realLocation = new Location(world, x, y, z);
            tmpStand.teleport(realLocation);

            EntityEquipment equipment = tmpStand.getEquipment();
            if (equipment != null) {
                ItemStack helmet = PROPERTY_HELMET.get(this);
                ItemStack chestplate = PROPERTY_CHESTPLATE.get(this);
                ItemStack leggings = PROPERTY_LEGGINGS.get(this);
                ItemStack boots = PROPERTY_BOOTS.get(this);

                equipment.setHelmet(helmet);
                equipment.setChestplate(chestplate);
                equipment.setLeggings(leggings);
                equipment.setBoots(boots);
            }

            this.stand = tmpStand;
            AtomicBoolean rotating = new AtomicBoolean();
            AtomicBoolean lock = new AtomicBoolean();

            AtomicReference<StandAnimator> animator = new AtomicReference<>();
            task = Bukkit.getServer().getScheduler().runTaskTimer(plugin, () -> {
                AutoTracker auto = tracker;
                if (auto != null) {
                    if (lastTarget != null && lastTarget.isValid() && !lastTarget.isDead()) {
                        if (ALWAYS_TRACK.get(this)) { //We will always track the last target unless is not valid
                            AsyncRayTrace rt = createAsyncRayTrace(lastTarget).orElse(null);
                            if (rt != null) {
                                LivingEntity ent = lastTarget;
                                rt.trace(TRACKER_RADIUS.get(this)).onComplete((rts) -> {
                                    TrackerTracksEvent tracksEvent = new TrackerTracksEvent(this, ent, rts.get());
                                    Bukkit.getServer().getPluginManager().callEvent(tracksEvent);

                                    if (lock.get()) {
                                        TrackerHitTrackEvent event = new TrackerHitTrackEvent(this, ent, rts.get());
                                        Bukkit.getServer().getPluginManager().callEvent(event);
                                    }
                                });
                            }
                        } else if (TRACK_LOCK.get(this)) { //Otherwise, if we just "lock" to target, we will try to track him
                            AsyncRayTrace rt = createAsyncRayTrace(lastTarget).orElse(null);
                            if (rt != null) {
                                rt.filterEntity(stand);
                                if (stand.getLocation().getY() < lastTarget.getLocation().getY()) {
                                    rt.setDirection(RayDirection.DOWN_TO_UP);
                                }

                                rt.trace(TRACKER_RADIUS.get(this)).onComplete((rts) -> {
                                    RayTraceResult result = rts.get();
                                    boolean hit = result.getHitPosition(this.lastTarget).isPresent();

                                    if (hit) {
                                        target = lastTarget;

                                        TrackerTracksEvent tracksEvent = new TrackerTracksEvent(this, this.target, result);
                                        Bukkit.getServer().getPluginManager().callEvent(tracksEvent);

                                        if (lock.get()) {
                                            TrackerHitTrackEvent event = new TrackerHitTrackEvent(this, this.target, result);
                                            Bukkit.getServer().getPluginManager().callEvent(event);
                                        }
                                    } else {
                                        TrackerStopTrackEvent event = new TrackerStopTrackEvent(this, this.target, TrackerStopTrackEvent.StopReason.NO_LONGER_VISIBLE);
                                        Bukkit.getServer().getPluginManager().callEvent(event);

                                        /*target = null;
                                        lastTarget = null;*/
                                        lock.set(false);
                                    }
                                });
                            }
                        }
                    }

                    if (target == null) {
                        lock.set(false);

                        LivingEntity[] entities = auto.track(this, TRACKER_RADIUS.get(this));

                        if (entities != null) {
                            AtomicReference<LivingEntity> foundEntity = new AtomicReference<>(null);

                            for (LivingEntity entity : entities) {
                                if (foundEntity.get() != null) break;

                                if (entity != null && entity.isValid()) {
                                    AsyncRayTrace rt = createAsyncRayTrace(entity).orElse(null);
                                    if (rt != null) {
                                        rt.filterEntity(stand);
                                        if (stand.getLocation().getY() < entity.getLocation().getY()) {
                                            rt.setDirection(RayDirection.DOWN_TO_UP);
                                        }

                                        rt.trace(TRACKER_RADIUS.get(this), TraceOption.ROLLBACK_ON_HIT).onComplete((rts) -> {
                                            if (foundEntity.get() != null) return;

                                            RayTraceResult result = rts.get();
                                            boolean hit = result.getHitPosition(entity).isPresent();

                                            if (hit || ALWAYS_TRACK.get(this)) {
                                                target = entity;

                                                if (this.lastTarget != null && !this.lastTarget.getUniqueId().equals(entity.getUniqueId())) {
                                                    TrackerSwitchTrackEvent event = getTrackerSwitchTrackEvent(this.lastTarget, this.target);
                                                    Bukkit.getServer().getPluginManager().callEvent(event);
                                                    foundEntity.set(event.getNewEntity());
                                                } else {
                                                    if (lastTarget == null) {
                                                        TrackerStartTrackEvent start = new TrackerStartTrackEvent(this, this.target);
                                                        Bukkit.getServer().getPluginManager().callEvent(start);
                                                    }
                                                }

                                                if (lock.get()) {
                                                    TrackerHitTrackEvent event = new TrackerHitTrackEvent(this, this.target, result);
                                                    Bukkit.getServer().getPluginManager().callEvent(event);
                                                }

                                                lastTarget = target;
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                }
            }, 0, period);

            AtomicDouble lastY = new AtomicDouble(Double.MIN_VALUE);
            rotateTask = Bukkit.getServer().getScheduler().runTaskTimer(plugin, () -> {
                ArmorStand stand = this.stand;

                if (stand != null && !stand.isDead() && stand.isValid()) {
                    if (equipment != null) {
                        ItemStack helmet = PROPERTY_HELMET.get(this);
                        ItemStack chestplate = PROPERTY_CHESTPLATE.get(this);
                        ItemStack leggings = PROPERTY_LEGGINGS.get(this);
                        ItemStack boots = PROPERTY_BOOTS.get(this);

                        equipment.setHelmet(helmet);
                        equipment.setChestplate(chestplate);
                        equipment.setLeggings(leggings);
                        equipment.setBoots(boots);
                    }

                    stand.setCustomNameVisible(PROPERTY_SHOW_NAME.get(this));
                    stand.setCustomName(ChatColor.translateAlternateColorCodes('&', PROPERTY_NAME.get(this)));
                }

                if (target != null) {
                    if (stand != null) {
                        if (stand.isDead() || !stand.isValid()) {
                            target = null;
                        }
                    }

                    if (stand != null && target != null) {
                        Location trackLocation = target.getEyeLocation().clone();
                        Location standLocation = stand.getEyeLocation().clone();
                        //standLocation.setY(standLocation.getY() + Math.max(1.5, tracking.getEyeHeight()));

                        SyncRayTrace prt = createRayTrace(target).orElse(null);
                        if (prt == null) return;

                        prt.filterEntity(stand);
                        if (stand.getLocation().getY() < target.getLocation().getY()) {
                            prt.setDirection(RayDirection.DOWN_TO_UP);
                        }

                        RayTraceResult rts = prt.trace(TRACKER_RADIUS.get(this), (ALWAYS_TRACK.get(this) ? new TraceOption[0] : new TraceOption[]{TraceOption.ROLLBACK_ON_HIT}));
                        boolean hit = rts.getHitPosition(target).isPresent();

                        if (hit || ALWAYS_TRACK.get(this)) {
                            Vector vector = trackLocation.toVector().subtract(standLocation.toVector()).normalize();

                            double angleX = vector.getY() * (-1);
                            double x = vector.getX();
                            double z = vector.getZ();
                            double angleY = 360F - Math.toDegrees(Math.atan2(x, z));

                            //EulerAngle currentAngle = stand.getHeadPose();
                            EulerAngle targetAngle = new EulerAngle(angleX, Math.toRadians(angleY), 0);

                            if (lock.get() || ROTATION_SMOOTHNESS.get(this) == 0) {
                                //stand.setHeadPose(targetAngle);
                                stand.setHeadPose(targetAngle);
                            } else {
                                if (angleHitsEntity(standLocation, target)) {
                                    rotating.set(false);
                                    animator.set(null);

                                    lock.set(true);
                                    return;
                                }

                                if (rotating.get()) {
                                    StandAnimator existingAnimator = animator.get();
                                    existingAnimator.setTarget(targetAngle);
                                    existingAnimator.animate();

                                    if (angleHitsEntity(standLocation, target)) {
                                        rotating.set(false);
                                        lock.set(true);

                                        animator.set(null);
                                    }

                                    if (existingAnimator.finished() && rotating.get()) { //We couldn't track entity to time
                                        rotating.set(false);
                                        animator.set(null);
                                    }

                                    return;
                                }

                                rotating.set(true);
                                animator.set(new StandAnimator(stand, targetAngle, ROTATION_SMOOTHNESS.get(this), ROTATION_FRAMES.get(this)));
                            }
                        }
                    }

                    //this.lastTarget = lastTarget;
                } else {
                    if (idleRotation != null && stand != null) {
                        StandAnimator standAnimator = animator.get();
                        if (standAnimator == null) {
                            standAnimator = new StandAnimator(stand, idleRotation, ROTATION_SMOOTHNESS.get(this), ROTATION_FRAMES.get(this));
                            animator.set(standAnimator);
                        }

                        standAnimator.animate();
                        double currentY = stand.getHeadPose().getY();

                        if (currentY == idleRotation.getY() || currentY == lastY.get()) {
                            animator.set(null);

                            if (idleRotationEnd != null) {
                                Consumer<Void> instance = idleRotationEnd;
                                idleRotationEnd = null;
                                instance.accept(null);
                            }
                        }

                        lastY.set(currentY);
                    }
                }
            }, 0, 0);

            if (target != null) {
                this.lastTarget = target;
                TrackerStartTrackEvent start = new TrackerStartTrackEvent(this, target);
                Bukkit.getServer().getPluginManager().callEvent(start);
            }

            this.rotTask = rotateTask;
            this.trackTask = task;
        }
    }

    private boolean angleHitsEntity(final Location standLocation, final LivingEntity tracking) {
        EulerAngle checkAngle = stand.getHeadPose();
        Vector dirVector = getAngleVector(checkAngle);
        SyncRayTrace rt = createRayTrace(tracking).orElse(null);
        if (rt == null) return false;

        rt.filterEntity(stand);
        rt.setDirection(dirVector);

        RayTraceResult subResult = rt.trace(target.getLocation().distance(standLocation) + 2d, TraceOption.STOP_ON_SOLID_BLOCK);
        return subResult.getHitPosition(target).isPresent();
    }

    @NotNull
    private Vector getAngleVector(EulerAngle tempAngle) {
        double yawDegrees = tempAngle.getY();
        double pitchDegrees = tempAngle.getX();

        float yaw = (float) Math.toDegrees(yawDegrees);
        float pitch = (float) Math.toDegrees(pitchDegrees);

        Vector vector = new Vector();
        vector.setY(-Math.sin(Math.toRadians(pitch)));

        double xz = Math.cos(Math.toRadians(pitch));

        vector.setX(-xz * Math.sin(Math.toRadians(yaw)));
        vector.setZ(xz * Math.cos(Math.toRadians(yaw)));

        return vector;
    }

    @NotNull
    private TrackerSwitchTrackEvent getTrackerSwitchTrackEvent(LivingEntity lastTarget, LivingEntity tracking) {
        TrackerSwitchTrackEvent.SwitchReason reason = TrackerSwitchTrackEvent.SwitchReason.AUTO_TRACKER;
        if (!lastTarget.isValid() || lastTarget.isDead()) {
            reason = TrackerSwitchTrackEvent.SwitchReason.NO_LONGER_ALIVE;
        } else if (!ALWAYS_TRACK.get(this)) {
            reason = TrackerSwitchTrackEvent.SwitchReason.NO_LONGER_VISIBLE;
        }

        return new TrackerSwitchTrackEvent(this, lastTarget, tracking, reason);
    }

    /**
     * Stop the track task
     */
    @Override
    public void stop() {
        BukkitTask rotateTask = this.rotTask;
        if (rotateTask != null && !Bukkit.getServer().getScheduler().isCurrentlyRunning(rotateTask.getTaskId())) {
            rotateTask.cancel();
            this.rotTask = null;
        }

        BukkitTask task = this.trackTask;
        if (task != null && !Bukkit.getServer().getScheduler().isCurrentlyRunning(task.getTaskId())) {
            task.cancel();
            this.trackTask = null;
        }
    }

    /**
     * Destroy the tracker
     */
    @Override
    public void destroy() {
        stop();
        if (stand != null && stand.isValid()) {
            stand.remove();
            this.stand = null;
        }
    }

    /**
     * Get if the track task is stopped
     *
     * @return if the track task is stopped
     */
    @Override
    public boolean isStopped() {
        return !isValid() || rotTask == null || rotTask.isCancelled() || trackTask == null || trackTask.isCancelled();
    }

    /**
     * Get if the tracker entity is valid
     *
     * @return if the tracker entity is valid
     */
    @Override
    public boolean isValid() {
        return stand != null && !stand.isDead() && stand.isValid();
    }
}
