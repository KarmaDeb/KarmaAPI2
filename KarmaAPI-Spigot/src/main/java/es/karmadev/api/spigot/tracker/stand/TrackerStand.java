package es.karmadev.api.spigot.tracker.stand;

import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.spigot.entity.trace.ray.PointRayTrace;
import es.karmadev.api.spigot.entity.trace.RayDirection;
import es.karmadev.api.spigot.entity.trace.ray.RayTrace;
import es.karmadev.api.spigot.entity.trace.TraceOption;
import es.karmadev.api.spigot.entity.trace.result.HitPosition;
import es.karmadev.api.spigot.entity.trace.result.RayTraceResult;
import es.karmadev.api.spigot.tracker.AutoTracker;
import es.karmadev.api.spigot.tracker.TrackerEntity;
import es.karmadev.api.spigot.tracker.event.TrackerHitTrackEvent;
import es.karmadev.api.spigot.tracker.event.TrackerStartTrackEvent;
import es.karmadev.api.spigot.tracker.event.TrackerStopTrackEvent;
import es.karmadev.api.spigot.tracker.event.TrackerSwitchTrackEvent;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tracker armor stand
 */
public class TrackerStand extends TrackerEntity {

    private final World world;
    private final double x;
    private final double y;
    private final double z;

    private final AtomicReference<ArmorStand> stand = new AtomicReference<>();
    private final AtomicReference<BukkitTask> rotTask = new AtomicReference<>();
    private final AtomicReference<BukkitTask> trackTask = new AtomicReference<>();
    private final AtomicReference<LivingEntity> target = new AtomicReference<>();
    private final AtomicReference<LivingEntity> lastTarget = new AtomicReference<>();
    private final AtomicReference<AutoTracker> tracker = new AtomicReference<>();
    private final Map<String, Object> properties = new ConcurrentHashMap<>();

    public boolean PROPERTY_SMALL = false;
    public boolean PROPERTY_BASE_PLATE = false;
    public boolean PROPERTY_INVINCIBLE = true;
    public boolean PROPERTY_INVISIBLE = false;
    public boolean PROPERTY_SHOW_NAME = false;
    public boolean PROPERTY_ARMS = false;
    public boolean PROPERTY_TAKEOFF_ITEMS = false;
    public boolean PROPERTY_TRACK_ALWAYS = false;
    public boolean PROPERTY_TRACK_LOCK = true;
    public double PROPERTY_TRACK_DISTANCE = 8d;
    public double PROPERTY_TRACE_PRECISION = RayTrace.HIGH_PRECISION;
    public String PROPERTY_NAME = "";

    /**
     * Create the tracker stand
     *
     * @param owner the tracker owner
     * @param world the tracker world
     * @param x the tracker x position
     * @param y the tracker y position
     * @param z the tracker z position
     */
    public TrackerStand(final Plugin owner, final World world, final double x, final double y, final double z) {
        super(owner);
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
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
     * Set the tracker property
     *
     * @param key   the key
     * @param value the value
     */
    @Override
    public void setProperty(final String key, final Object value) {
        if (properties.containsKey(key)) {
            Object storedValue = properties.get(key);
            if (storedValue.getClass().equals(value.getClass())) {
                properties.put(key, value);
                return;
            }
        }

        properties.put(key, value);
    }

    /**
     * Get a property
     *
     * @param key      the property key
     * @param deffault the property default value
     * @return the property value
     */
    @Override @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, T deffault) {
        Object value = properties.getOrDefault(key, deffault);
        try {
            if (value == null) return deffault;
            return (T) value;
        } catch (ClassCastException ex) {
            return deffault;
        }
    }

    /**
     * Get track target
     *
     * @return the target
     */
    @Override
    public Optional<LivingEntity> getTarget() {
        return Optional.ofNullable(target.get());
    }

    /**
     * Get the tracker auto tracker
     *
     * @return the auto tracker
     */
    @Override
    public Optional<AutoTracker> getTracker() {
        return Optional.ofNullable(tracker.get());
    }

    /**
     * Get the entity that is the
     * tracker
     *
     * @return the entity tracker
     */
    @Override
    public Entity getEntity() {
        return stand.get();
    }

    /**
     * Create a raytrace from this tracker
     * to an entity
     *
     * @param target the entity
     * @return the raytrace
     */
    @Override
    public Optional<PointRayTrace> createRayTrace(final LivingEntity target) {
        PointRayTrace rayTrace = null;
        ArmorStand stand = this.stand.get();
        if (stand != null) {
            Location location  = stand.getLocation();
            rayTrace = new RayTrace(location.clone(), target.getLocation().clone());
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
        ArmorStand stand = this.stand.get();
        LivingEntity target = this.target.get();
        if (stand != null && target != null) {
            Location start = stand.getLocation().clone();
            Location end = target.getEyeLocation().clone();

            boolean small = stand.isSmall();
            start.add(0, (small ? 0.5 : 1.5), 0);
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
        this.tracker.set(tracker); //Thread-safe
    }

    /**
     * Set the tracker target
     *
     * @param entity the target
     */
    @Override
    public void setTrackTarget(LivingEntity entity) {
        LivingEntity old = this.target.get();
        if (old != null) {
            if (entity != null) {
                TrackerSwitchTrackEvent switchEvent = new TrackerSwitchTrackEvent(this, old, entity, TrackerSwitchTrackEvent.SwitchReason.FORCED);
                Bukkit.getServer().getPluginManager().callEvent(switchEvent);

                entity = switchEvent.getNewEntity();
            }

            TrackerStopTrackEvent event = new TrackerStopTrackEvent(this, old, TrackerStopTrackEvent.StopReason.FORCED);
            Bukkit.getServer().getPluginManager().callEvent(event);
        }

        this.target.set(entity); //Thread-safe
    }

    /**
     * Start the track task
     *
     * @param period the track period in ticks
     */
    @Override
    public void start(final long period) {
        BukkitTask rotateTask = this.rotTask.get();
        BukkitTask task = this.trackTask.get();

        if (task == null || !Bukkit.getServer().getScheduler().isCurrentlyRunning(task.getTaskId()) || rotateTask == null || !Bukkit.getServer().getScheduler().isCurrentlyRunning(rotateTask.getTaskId())) {
            stop();

            ArmorStand tmpStand = this.stand.get();
            if (tmpStand != null && tmpStand.isValid()) tmpStand.remove();

            Location unknownPosition = new Location(world, 0, -64, 0);
            tmpStand = (ArmorStand) world.spawnEntity(unknownPosition, EntityType.ARMOR_STAND);

            tmpStand.setSmall(PROPERTY_SMALL);
            tmpStand.setBasePlate(PROPERTY_BASE_PLATE);
            if (PROPERTY_INVINCIBLE) {
                tmpStand.setNoDamageTicks(Integer.MAX_VALUE);
            }
            tmpStand.setVisible(!PROPERTY_INVISIBLE);
            tmpStand.setCustomNameVisible(PROPERTY_SHOW_NAME && !ObjectUtils.isNullOrEmpty(PROPERTY_NAME));
            tmpStand.setCustomName(ChatColor.translateAlternateColorCodes('&', PROPERTY_NAME));
            tmpStand.setArms(PROPERTY_ARMS);
            tmpStand.setCanPickupItems(PROPERTY_TAKEOFF_ITEMS);

            Location realLocation = new Location(world, x, y, z);
            tmpStand.teleport(realLocation);

            this.stand.set(tmpStand);
            task = Bukkit.getServer().getScheduler().runTaskTimer(plugin, () -> {
                AutoTracker auto = tracker.get();
                if (auto != null) {
                    LivingEntity[] entities = auto.track(this, PROPERTY_TRACK_DISTANCE);
                    if (entities != null && entities.length > 0) {
                        LivingEntity tracking = null;
                        LivingEntity lastTarget = this.lastTarget.get();

                        if (PROPERTY_TRACK_LOCK && lastTarget != null && lastTarget.isValid() && !lastTarget.isDead()) {
                            PointRayTrace rt = createRayTrace(lastTarget).orElse(null);
                            if (rt != null) {
                                RayTraceResult rts = rt.trace(PROPERTY_TRACK_DISTANCE, TraceOption.STOP_ON_SOLID_HIT);

                                for (Entity e : rts.entities()) {
                                    if (e.getUniqueId().equals(lastTarget.getUniqueId())) {
                                        tracking = lastTarget;
                                        break;
                                    }
                                }
                            }
                        }
                        if (tracking == null) {
                            for (LivingEntity entity : entities) {
                                if (entity != null && entity.isValid()) {
                                    PointRayTrace rt = createRayTrace(entity).orElse(null);
                                    if (rt != null) {
                                        RayTraceResult rts = rt.trace(PROPERTY_TRACK_DISTANCE, TraceOption.STOP_ON_SOLID_HIT);

                                        boolean hit = false;
                                        for (Entity e : rts.entities()) {
                                            if (e.getUniqueId().equals(entity.getUniqueId())) {
                                                hit = true;
                                                break;
                                            }
                                        }

                                        if (hit || PROPERTY_TRACK_ALWAYS) {
                                            tracking = entity;
                                            if (lastTarget != null && !lastTarget.getUniqueId().equals(tracking.getUniqueId())) {
                                                TrackerSwitchTrackEvent.SwitchReason reason = TrackerSwitchTrackEvent.SwitchReason.AUTO_TRACKER;
                                                if (!lastTarget.isValid() || lastTarget.isDead()) {
                                                    reason = TrackerSwitchTrackEvent.SwitchReason.NO_LONGER_ALIVE;
                                                } else if (!PROPERTY_TRACK_ALWAYS) {
                                                    reason = TrackerSwitchTrackEvent.SwitchReason.NO_LONGER_VISIBLE;
                                                }

                                                TrackerSwitchTrackEvent event = new TrackerSwitchTrackEvent(this, tracking, lastTarget, reason);
                                                Bukkit.getServer().getPluginManager().callEvent(event);
                                                tracking = event.getNewEntity();
                                            } else {
                                                if (lastTarget == null) {
                                                    TrackerStartTrackEvent start = new TrackerStartTrackEvent(this, tracking);
                                                    Bukkit.getServer().getPluginManager().callEvent(start);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            lastTarget = tracking;
                            this.target.set(tracking);
                            this.lastTarget.set(lastTarget);
                        }
                    }
                }
            }, 0, period);
            rotateTask = Bukkit.getServer().getScheduler().runTaskTimer(plugin, () -> {
                LivingEntity tracking = this.target.get();
                LivingEntity lastTarget = this.lastTarget.get();
                ArmorStand stand = this.stand.get();

                if (tracking != null) {
                    if (PROPERTY_TRACK_LOCK && lastTarget != null && lastTarget.isValid() && !lastTarget.isDead()) {
                        tracking = lastTarget;
                        //this.target.set(lastTarget);
                    }

                    lastTarget = tracking;

                    if (tracking.isDead() || !tracking.isValid()) {
                        TrackerStopTrackEvent event = new TrackerStopTrackEvent(this, tracking, TrackerStopTrackEvent.StopReason.NO_LONGER_ALIVE);
                        Bukkit.getServer().getPluginManager().callEvent(event);

                        tracking = null;
                    }
                    if (stand != null) {
                        if (stand.isDead() || !stand.isValid()) tracking = null;
                    }

                    if (stand != null && tracking != null) {
                        Location trackLocation = tracking.getEyeLocation().clone();
                        Location standLocation = stand.getLocation().clone();
                        standLocation.setY(standLocation.getY() + Math.max(1.5, tracking.getEyeHeight()));

                        RayTrace prt = new RayTrace(standLocation, tracking);
                        prt.setPrecision(PROPERTY_TRACE_PRECISION);
                        prt.setDirection(RayDirection.UP_TO_DOWN);
                        prt.filterEntity(stand);

                        RayTraceResult rts = prt.trace(64, (PROPERTY_TRACK_ALWAYS ? new TraceOption[0]: new TraceOption[]{TraceOption.STOP_ON_SOLID_HIT}));
                        boolean hit = false;
                        for (Entity entity : rts.entities()) {
                            if (entity.getUniqueId().equals(tracking.getUniqueId())) {
                                hit = true;
                                break;
                            }
                        }

                        if (hit || PROPERTY_TRACK_ALWAYS) {
                            HitPosition position = rts.getHitPosition(tracking).orElse(null);
                            if (position != null || PROPERTY_TRACK_ALWAYS) {
                                Vector vector = trackLocation.toVector().subtract(standLocation.toVector()).normalize();

                                double angle_x = vector.getY() * (-1);
                                double x = vector.getX();
                                double z = vector.getZ();
                                double angle_y = 360F - Math.toDegrees(Math.atan2(x, z));

                                EulerAngle angle = new EulerAngle(angle_x, Math.toRadians(angle_y), 0);
                                stand.setHeadPose(angle);

                                TrackerHitTrackEvent event = new TrackerHitTrackEvent(this, tracking, rts);
                                Bukkit.getServer().getPluginManager().callEvent(event);
                            }
                        } else {
                            TrackerStopTrackEvent event = new TrackerStopTrackEvent(this, tracking, TrackerStopTrackEvent.StopReason.NO_LONGER_VISIBLE);
                            Bukkit.getServer().getPluginManager().callEvent(event);
                        }
                    }

                    this.lastTarget.set(lastTarget);
                    //this.target.set(target);
                }
            }, 0, 0);

            LivingEntity target = this.target.get();
            if (target != null) {
                this.lastTarget.set(target);
                TrackerStartTrackEvent start = new TrackerStartTrackEvent(this, target);
                Bukkit.getServer().getPluginManager().callEvent(start);
            }

            this.rotTask.set(rotateTask);
            this.trackTask.set(task);
        }
    }

    /**
     * Stop the track task
     */
    @Override
    public void stop() {
        BukkitTask rotateTask = this.rotTask.get();
        if (rotateTask != null && !Bukkit.getServer().getScheduler().isCurrentlyRunning(rotateTask.getTaskId())) {
            rotateTask.cancel();
            this.rotTask.set(null);
        }

        BukkitTask task = this.trackTask.get();
        if (task != null && !Bukkit.getServer().getScheduler().isCurrentlyRunning(task.getTaskId())) {
            task.cancel();
            this.trackTask.set(null);
        }
    }

    /**
     * Destroy the tracker
     */
    @Override
    public void destroy() {
        stop();
        ArmorStand stand = this.stand.get();
        if (stand != null && stand.isValid()) {
            stand.remove();
            this.stand.set(null);
        }
    }

    /**
     * Get if the track task is stopped
     *
     * @return if the track task is stopped
     */
    @Override
    public boolean isStopped() {
        return false;
    }

    /**
     * Get if the tracker entity is valid
     *
     * @return if the tracker entity is valid
     */
    @Override
    public boolean isValid() {
        return false;
    }
}
