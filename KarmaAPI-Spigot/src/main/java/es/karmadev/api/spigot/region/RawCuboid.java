package es.karmadev.api.spigot.region;

import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.file.util.PathUtilities;
import es.karmadev.api.kson.JsonNative;
import es.karmadev.api.kson.JsonObject;
import es.karmadev.api.kson.KsonException;
import es.karmadev.api.kson.io.JsonReader;
import es.karmadev.api.logger.log.console.ConsoleColor;
import es.karmadev.api.object.ObjectUtils;
import es.karmadev.api.spigot.region.exception.DimensionException;
import es.karmadev.api.spigot.region.part.Ceiling;
import es.karmadev.api.spigot.region.part.Floor;
import es.karmadev.api.spigot.region.part.Wall;
import es.karmadev.api.spigot.region.world.CuboidRegion;
import es.karmadev.api.spigot.region.world.Region;
import es.karmadev.api.spigot.region.world.part.RegionCeiling;
import es.karmadev.api.spigot.region.world.part.RegionFloor;
import es.karmadev.api.spigot.region.world.part.RegionWall;
import es.karmadev.api.spigot.region.world.part.WallPart;
import es.karmadev.api.strings.StringUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * KarmaAPI cuboid region implementation
 */
public class RawCuboid implements CuboidRegion {

    private transient String name;
    private transient JavaPlugin plugin;
    private transient World world;

    private final UUID uuid = UUID.randomUUID();
    private final double xTop;
    private final double yTop;
    private final double zTop;
    private final double xBottom;
    private final double yBottom;
    private final double zBottom;
    private double priority = 0;

    /**
     * Initialize the cuboid
     *
     * @param plugin the plugin owning this region
     * @param pos1 the region position 1
     * @param pos2 the region position 2
     * @throws DimensionException if the position worlds are not the same
     * @throws NullPointerException if any of the arguments are null or any of the
     * position worlds are null
     */
    public RawCuboid(final JavaPlugin plugin, final Location pos1, final Location pos2) throws DimensionException, NullPointerException {
        if (plugin == null || pos1 == null || pos2 == null) throw new NullPointerException("Cannot create region with null arguments. Region constructor requires all arguments to be safe");

        this.plugin = plugin;

        World pos1world = pos1.getWorld();
        World pos2world = pos2.getWorld();

        if (pos1world == null || pos2world == null) throw new NullPointerException("Cannot create region with unloaded worlds. Regions require loaded worlds to work");
        if (!pos1world.equals(pos2world)) throw new DimensionException(pos1world, pos2world);

        world = pos1world;
        xTop = Math.max(pos1.getX(), pos2.getX());
        yTop = Math.max(pos1.getY(), pos2.getY());
        zTop = Math.max(pos1.getZ(), pos2.getZ());
        xBottom = Math.min(pos1.getX(), pos2.getX());
        yBottom = Math.max(pos1.getY(), pos2.getY());
        zBottom = Math.max(pos1.getZ(), pos2.getZ());
    }

    /**
     * Get the region top bound
     *
     * @return the top bound
     */
    @Override
    public Location boundTop() {
        return new Location(world, xTop, yTop, zTop);
    }

    /**
     * Get the region bottom bound
     *
     * @return the bottom bound
     */
    @Override
    public Location boundBottom() {
        return new Location(world, xBottom, yBottom, zBottom);
    }

    /**
     * Get the region name
     *
     * @return the region name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Get the region universal unique
     * identifier
     *
     * @return the region ID
     */
    @Override
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Get the region world
     *
     * @return the region world
     */
    @Override
    public World getWorld() {
        return world;
    }

    /**
     * Get the region priority
     *
     * @return the region priority
     */
    @Override
    public double getPriority() {
        return priority;
    }

    /**
     * Set the region priority
     *
     * @param priority the region new priority
     */
    @Override
    public void setPriority(final double priority) {
        this.priority = priority;
    }

    /**
     * Get the region blocks
     *
     * @return the region blocks
     */
    @Override
    public Collection<Block> getBlocks() {
        Location cornerBottom = new Location(world, xBottom, yBottom, zBottom);
        Location cornerTop = new Location(world, xTop, yTop, zTop);

        int xMin = Math.min(cornerBottom.getBlockX(), cornerTop.getBlockX());
        int xMax = Math.max(cornerBottom.getBlockX(), cornerTop.getBlockX());
        int yMin = Math.min(cornerBottom.getBlockY(), cornerTop.getBlockY());
        int yMax = Math.max(cornerBottom.getBlockY(), cornerTop.getBlockY());
        int zMin = Math.min(cornerBottom.getBlockZ(), cornerTop.getBlockZ());
        int zMax = Math.max(cornerBottom.getBlockZ(), cornerTop.getBlockZ());

        List<Block> blocks = new ArrayList<>();
        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    blocks.add(block);
                }
            }
        }

        return blocks;
    }

    /**
     * Get if the specified x, y and z coordinates
     * are out of region bounds
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return if the coordinates are out of bounds
     */
    @Override
    public boolean isOutOfBounds(final double x, final double y, final double z) {
        return x >= xBottom && y >= yBottom && z >= zBottom && x <= xTop && y <= yTop && z <= zTop;
    }

    /**
     * Get the distance of x, y and z from the region
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the distance from the region
     */
    @Override
    public double distance(final double x, final double y, final double z) {
        Location source = new Location(world, x, y, z);
        double closerDistance = getCenter().distance(source);
        for (int i = 0; i < 6; i++){
            Location partCenter = getCenter(i).clone();
            double distance = partCenter.distance(source);
            if (distance < closerDistance) {
                closerDistance = distance;
            }
        }

        return closerDistance;
    }

    /**
     * Get the region ceiling
     *
     * @return the region ceiling
     */
    @Override
    public RegionCeiling getCeiling() {
        Location cornerBottom1 = new Location(world, xBottom, yTop, zBottom);
        Location cornerBottom2 = new Location(world, xTop, yTop, zTop);

        int xMin = Math.min(cornerBottom1.getBlockX(), cornerBottom2.getBlockX());
        int xMax = Math.max(cornerBottom1.getBlockX(), cornerBottom2.getBlockX());
        int zMin = Math.min(cornerBottom1.getBlockZ(), cornerBottom2.getBlockZ());
        int zMax = Math.max(cornerBottom1.getBlockZ(), cornerBottom2.getBlockZ());

        List<Block> blocks = new ArrayList<>();
        for (int x = xMin; x <= xMax; x++) {
            for (int z = zMin; z <= zMax; z++) {
                Block block = world.getBlockAt(x, cornerBottom1.getBlockY(), z);
                blocks.add(block);
            }
        }

        return new Ceiling(blocks, cornerBottom1, cornerBottom2);
    }

    /**
     * Get the region floor
     *
     * @return the region floor
     */
    @Override
    public RegionFloor getFloor() {
        Location cornerBottom1 = new Location(world, xBottom, yBottom, zBottom);
        Location cornerBottom2 = new Location(world, xTop, yBottom, zTop);

        int xMin = Math.min(cornerBottom1.getBlockX(), cornerBottom2.getBlockX());
        int xMax = Math.max(cornerBottom1.getBlockX(), cornerBottom2.getBlockX());
        int zMin = Math.min(cornerBottom1.getBlockZ(), cornerBottom2.getBlockZ());
        int zMax = Math.max(cornerBottom1.getBlockZ(), cornerBottom2.getBlockZ());

        List<Block> blocks = new ArrayList<>();
        for (int x = xMin; x <= xMax; x++) {
            for (int z = zMin; z <= zMax; z++) {
                Block block = world.getBlockAt(x, cornerBottom1.getBlockY(), z);
                blocks.add(block);
            }
        }

        return new Floor(blocks, cornerBottom1, cornerBottom2);
    }

    /**
     * Get the region wall
     *
     * @param part the wall part
     * @return the region wall
     */
    @Override
    public RegionWall getWall(final WallPart part) {
        Location start;
        Location end;

        switch (part) {
            case BACK:
                start = new Location(world, xBottom, yBottom, zBottom);
                end = new Location(world, xTop, yTop, zBottom);
                break;
            case RIGHT:
                start = new Location(world, xTop, yBottom, zBottom);
                end = new Location(world, xTop, yTop, zTop);
                break;
            case LEFT:
                start = new Location(world, xBottom, yBottom, zBottom);
                end = new Location(world, xBottom, yTop, zTop);
                break;
            case FRONT:
            default:
                start = new Location(world, xTop, yBottom, zTop);
                end = new Location(world, xTop, yTop, zTop);
                break;
        }

        int xMin = Math.min(start.getBlockX(), end.getBlockX());
        int xMax = Math.max(start.getBlockX(), end.getBlockX());
        int yMax = Math.max(start.getBlockY(), end.getBlockY());
        int yMin = Math.min(start.getBlockY(), end.getBlockY());
        int zMin = Math.min(start.getBlockZ(), end.getBlockZ());
        int zMax = Math.max(start.getBlockZ(), end.getBlockZ());

        List<Block> blocks = new ArrayList<>();
        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    blocks.add(block);
                }
            }
        }

        return new Wall(part, blocks, start, end);
    }

    /**
     * Get the region center
     *
     * @return the center location
     */
    public Location getCenter(final int part) {
        Location start;
        Location end;
        switch (part) {
            case 0: //CEILING
                start = new Location(world, xBottom, yTop, zBottom);
                end = new Location(world, xTop, yTop, zTop);
                break;
            case 1: //FLOOR
                start = new Location(world, xBottom, yBottom, zBottom);
                end = new Location(world, xTop, yBottom, zTop);
                break;
            case 2: //BOTTOM
                start = new Location(world, xBottom, yBottom, zBottom);
                end = new Location(world, xTop, yTop, zBottom);
                break;
            case 3: //RIGHT
                start = new Location(world, xTop, yBottom, zBottom);
                end = new Location(world, xTop, yTop, zTop);
                break;
            case 4: //LEFT
                start = new Location(world, xBottom, yBottom, zBottom);
                end = new Location(world, xBottom, yTop, zTop);
                break;
            case 5: //FRONT
            default:
                start = new Location(world, xTop, yBottom, zTop);
                end = new Location(world, xTop, yTop, zTop);
                break;
        }

        double midX = (start.getX() + end.getX()) / 2;
        double midY = (start.getY() + end.getY()) / 2;
        double midZ = (start.getZ() + end.getZ()) / 2;

        return new Location(world, midX, midY, midZ);
    }

    /**
     * Get the region center
     *
     * @return the center
     */
    @Override
    public Location getCenter() {
        double centerX = (xBottom + xTop) / 2d;
        double centerY = (yBottom + yTop) / 2d;
        double centerZ = (zBottom + zTop) / 2d;

        return new Location(world, centerX, centerY, centerZ);
    }

    /**
     * Check if this region intersects with
     * another region
     *
     * @param other the other region
     * @return if the region intersects
     */
    @Override
    public boolean intersects(final Region other) {
        if (other.getWorld().equals(getWorld()) && !other.equals(this)) {
            Location rCenter = other.getCenter();
            return !isOutOfBounds(rCenter.getX(), rCenter.getY(), rCenter.getZ());
        }

        return false;
    }

    /**
     * Get the intersecting regions
     *
     * @return the regions which this
     * region intersects with
     */
    @Override
    public Collection<Region> getIntersecting() {
        Region[] regions = GameRegionManager.getRegions();

        List<Region> collected = new ArrayList<>();
        for (Region region : regions) {
            Location rCenter = region.getCenter();
            if (region.getWorld().equals(getWorld()) && !region.equals(this)) {
                if (!isOutOfBounds(rCenter.getX(), rCenter.getY(), rCenter.getZ())) {
                    collected.add(region);
                }
            }
        }

        return collected;
    }

    /**
     * Save the region if it was already saved
     *
     * @return if the region was able to be saved
     */
    public boolean save() {
        if (name != null) return saveToMemory(name);
        return false;
    }

    /**
     * Save the region into memory
     *
     * @param name the region name
     * @return if the region was able to be saved
     * @throws NullPointerException if the name is null or empty
     */
    @Override
    public boolean saveToMemory(final String name) throws NullPointerException {
        ObjectUtils.assertNullOrEmpty(name, "Region name cannot be empty!");

        UUID regionId = UUID.nameUUIDFromBytes(("CuboidRegion:" + ConsoleColor.strip(name)).getBytes());
        Path file = plugin.getDataFolder().toPath().resolve("region").resolve(ConsoleColor.strip(name)).resolve(regionId + ".json");

        JsonObject json = JsonObject.newObject("", "");
        json.put("world", world.getUID().toString());
        json.put("region", StringUtils.serialize(this));

        String rawJson = json.toString();
        return PathUtilities.write(file, rawJson);
    }

    /**
     * Load a region from memory
     *
     * @param plugin the plugin owning the region
     * @param name the region name
     * @return the region in memory
     */
    public static Optional<RawCuboid> loadFromMemory(final JavaPlugin plugin, final String name) {
        UUID regionId = UUID.nameUUIDFromBytes(("CuboidRegion:" + ConsoleColor.strip(name)).getBytes());
        Path file = plugin.getDataFolder().toPath().resolve("region").resolve(ConsoleColor.strip(name)).resolve(regionId + ".json");

        RawCuboid region = null;
        if (Files.exists(file)) {
            try {
                JsonObject object = JsonReader.read(PathUtilities.read(file)).asObject();

                if (object.hasChild("world") && object.getChild("world").isNativeType() &&
                        object.hasChild("region") && object.getChild("region").isNativeType()) {
                    JsonNative worldPrimitive = object.getChild("world").asNative();
                    JsonNative regionPrimitive = object.getChild("region").asNative();

                    if (worldPrimitive.isString() && regionPrimitive.isString()) {
                        String uidString = worldPrimitive.getAsString();
                        String regionString = regionPrimitive.getAsString();
                        try {
                            UUID worldUid = UUID.fromString(uidString);
                            World world = plugin.getServer().getWorld(worldUid);
                            region = StringUtils.loadAndCast(regionString);

                            if (world != null && region != null) {
                                region.plugin = plugin;
                                region.world = world;
                                region.name = ConsoleColor.strip(name);
                            }
                        } catch (IllegalArgumentException ex) {
                            ExceptionCollector.catchException(Region.class, ex);
                        }
                    }
                }
            } catch (KsonException ex) {
                ExceptionCollector.catchException(Region.class, ex);
            }
        }

        return Optional.ofNullable(region);
    }
}
