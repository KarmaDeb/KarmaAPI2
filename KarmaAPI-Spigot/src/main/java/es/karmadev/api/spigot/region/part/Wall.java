package es.karmadev.api.spigot.region.part;

import es.karmadev.api.spigot.region.world.part.RegionWall;
import es.karmadev.api.spigot.region.world.part.WallPart;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.*;

public final class Wall implements RegionWall {

    private final Set<int[]> bounds = new HashSet<>();
    private final List<Block> blocks;
    private final WallPart part;
    private final Location start;
    private final Location end;

    public Wall(final WallPart part, final List<Block> blocks, final Location start, final Location end) {
        this.part = part;
        this.blocks = blocks;
        for (Block block : blocks) bounds.add(new int[]{block.getX(), block.getY(), block.getZ()});
        this.start = start;
        this.end = end;
    }

    /**
     * Get the wall part
     *
     * @return the wall part
     */
    @Override
    public WallPart getWall() {
        return part;
    }

    /**
     * Get all the blocks
     *
     * @return the wall blocks
     */
    @Override
    public Collection<Block> getBlocks() {
        return new ArrayList<>(blocks);
    }

    /**
     * Check if the block is within the block
     * bounds
     *
     * @param x the block x
     * @param y the block y
     * @param z the block z
     * @return if the block is inside
     */
    @Override
    public boolean isInBounds(final int x, final int y, final int z) {
        return bounds.contains(new int[]{x, y, z});
    }

    /**
     * Get the part start point
     *
     * @return the start point
     */
    @Override
    public Location getStart() {
        return start;
    }

    /**
     * Get the part end point
     *
     * @return the end point
     */
    @Override
    public Location getEnd() {
        return end;
    }
}
