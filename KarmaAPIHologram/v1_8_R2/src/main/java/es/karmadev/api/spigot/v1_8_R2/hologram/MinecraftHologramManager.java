package es.karmadev.api.spigot.v1_8_R2.hologram;

import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.spigot.core.KarmaPlugin;
import es.karmadev.api.spigot.nms.common.hologram.util.ReflectionUtils;
import es.karmadev.api.spigot.reflection.HologramManager;
import es.karmadev.api.spigot.reflection.hologram.Hologram;
import es.karmadev.api.spigot.reflection.hologram.line.type.ItemHolderLine;
import es.karmadev.api.spigot.server.SpigotServer;
import es.karmadev.api.spigot.v1_8_R2.hologram.entity.HologramArmorStand;
import es.karmadev.api.spigot.v1_8_R2.hologram.entity.HologramItem;
import es.karmadev.api.spigot.v1_8_R2.hologram.entity.HologramSlime;
import es.karmadev.api.spigot.v1_8_R2.hologram.line.MinecraftHologramLine;
import net.minecraft.server.v1_8_R2.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class MinecraftHologramManager extends HologramManager {

    static Method validateEntity;

    /**
     * Initialize the hologram manager
     *
     * @param plugin the plugin owning the hologram
     *               manager
     */
    public MinecraftHologramManager(final KarmaPlugin plugin) {
        super(plugin);
        if (!SpigotServer.isMCP()) {
            try {
                validateEntity = World.class.getDeclaredMethod("a", Entity.class);
                validateEntity.setAccessible(true);
            } catch (NoSuchMethodException ex) {
                ExceptionCollector.catchException(MinecraftHologramManager.class, ex);
            }
        }

        try {
            registerCustomEntity(HologramArmorStand.class, "ArmorStand", 30);
            registerCustomEntity(HologramItem.class, "Item", 1);
            registerCustomEntity(HologramSlime.class, "Slime", 55);
        } catch (Exception ex) {
            ExceptionCollector.catchException(MinecraftHologramManager.class, ex);
        }
    }

    /**
     * Create a hologram
     *
     * @param name     the hologram name
     * @param location the hologram location
     * @return the created hologram
     */
    @Override
    public Hologram createHologram(final String name, final Location location) {
        return new MinecraftHologram(name, location);
    }

    /**
     * Register a custom entity into the minecraft
     * server
     *
     * @param entityClass the entity to register
     * @param name the entity name
     * @param id the entity type id
     * @throws Exception if something goes wrong
     */
    private void registerCustomEntity(final Class<?> entityClass, final String name, final int id) throws Exception {
        if (SpigotServer.isMCP()) {
            Class<?> entityTypesClass = Class.forName("net.minecraft.server.v1_8_R1.EntityTypes");
            ReflectionUtils.putInPrivateStaticMap(entityTypesClass, "field_75626_c", entityClass, name);
            ReflectionUtils.putInPrivateStaticMap(entityTypesClass, "field_75624_e", entityClass, id);
        } else {
            ReflectionUtils.putInPrivateStaticMap(EntityTypes.class, "d", entityClass, name);
            ReflectionUtils.putInPrivateStaticMap(EntityTypes.class, "f", entityClass, id);
        }
    }

    /**
     * Create a new armor stand
     *
     * @param world the stand world
     * @param x the stand X position
     * @param y the stand Y position
     * @param z the stand Z position
     * @param line the line attachment
     * @return the created entity
     */
    public static HologramArmorStand spawnArmorStand(final org.bukkit.World world, final double x, final double y, final double z, final MinecraftHologramLine line) {
        WorldServer nmsWorld = ((CraftWorld)world).getHandle();
        HologramArmorStand invisibleArmorStand = new HologramArmorStand(world, line);
        invisibleArmorStand.moveTo(x, y, z);

        addEntityToWorld(nmsWorld, invisibleArmorStand);
        return invisibleArmorStand;
    }

    /**
     * Create a new slime
     *
     * @param world the slime world
     * @param x the slime X position
     * @param y the slime Y position
     * @param z the slime Z position
     * @param line the line attachment
     * @return the created entity
     */
    public static HologramSlime spawnSlime(final org.bukkit.World world, final double x, final double y, final double z, final MinecraftHologramLine line) {
        WorldServer nmsWorld = ((CraftWorld)world).getHandle();
        HologramSlime invisibleSlime = new HologramSlime(world, line);
        invisibleSlime.moveTo(x, y, z);

        addEntityToWorld(nmsWorld, invisibleSlime);
        return invisibleSlime;
    }

    /**
     * Create a new item
     *
     * @param world the item world
     * @param x the item X position
     * @param y the item Y position
     * @param z the item Z position
     * @param item the item stack
     * @param line the line attachment
     * @return the created entity
     */
    public static HologramItem spawnItem(final org.bukkit.World world, final double x, final double y, final double z, final ItemStack item, final ItemHolderLine line) {
        WorldServer nmsWorld = ((CraftWorld)world).getHandle();
        HologramItem invisibleItem = new HologramItem(world, line);
        invisibleItem.moveTo(x, y, z);
        invisibleItem.setStack(item);

        addEntityToWorld(nmsWorld, invisibleItem);
        return invisibleItem;
    }

    /**
     * Add an entity to the world, making it visible
     * for everyone
     *
     * @param nmsWorld the world to add the entity to
     * @param nmsEntity the entity to add
     */
    public static void addEntityToWorld(final WorldServer nmsWorld, final Entity nmsEntity) {
        if (validateEntity == null) {
            nmsWorld.addEntity(nmsEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
            return;
        }

        int chunkX = MathHelper.floor(nmsEntity.locX / 16.0D);
        int chunkZ = MathHelper.floor(nmsEntity.locZ / 16.0D);
        if (!nmsWorld.chunkProviderServer.isChunkLoaded(chunkX, chunkZ)) {
            nmsEntity.dead = true;
            return;
        }

        nmsWorld.getChunkAt(chunkX, chunkZ).a(nmsEntity);
        nmsWorld.entityList.add(nmsEntity);
        try {
            validateEntity.invoke(nmsWorld, nmsEntity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
