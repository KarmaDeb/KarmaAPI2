package es.karmadev.api.spigot.region.world.part;

import org.bukkit.block.Block;

import java.util.Collection;

/**
 * KarmaAPI region ceil
 */
public interface RegionCeiling extends RegionPart {

    /**
     * Get all the blocks
     *
     * @return the ceiling blocks
     */
    Collection<Block> getBlocks();

    /**
     * Check if the block is within the block
     * bounds
     *
     * @param x the block x
     * @param y the block y
     * @param z the block z
     * @return if the block is inside
     */
    boolean isInBounds(final int x, final int y, final int z);
}
