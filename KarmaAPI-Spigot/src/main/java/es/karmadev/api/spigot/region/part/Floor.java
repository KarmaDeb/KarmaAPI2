package es.karmadev.api.spigot.region.part;

import es.karmadev.api.spigot.region.world.part.RegionFloor;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.*;

public class Floor implements RegionFloor {

    private final Set<int[]> bounds = new HashSet<>();
    private final List<Block> blocks;
    private final Location start;
    private final Location end;

    public Floor(final List<Block> blocks, final Location start, final Location end) {
        this.blocks = blocks;
        for (Block block : blocks) bounds.add(new int[]{block.getX(), block.getY(), block.getZ()});
        this.start = start;
        this.end = end;
    }

    /**
     * Get all the blocks
     *
     * @return the floor blocks
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
