package es.karmadev.api.spigot.v1_8_R3.hologram.entity;

import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.spigot.nms.common.hologram.util.ReflectionUtils;
import es.karmadev.api.spigot.reflection.hologram.line.HologramLine;
import es.karmadev.api.spigot.reflection.hologram.nms.MinecraftEntity;
import es.karmadev.api.spigot.reflection.hologram.nms.entity.MinecraftSlime;
import es.karmadev.api.spigot.v1_8_R3.hologram.entity.craft.CraftHologramSlime;
import es.karmadev.api.spigot.v1_8_R3.hologram.entity.util.NullBoundingBox;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerInteractEntityEvent;


public class HologramSlime extends EntitySlime implements MinecraftSlime {

    private boolean lockTick;
    private final HologramLine line;

    public HologramSlime(final World world, final HologramLine line) {
        super(((CraftWorld) world).getHandle());
        this.persistent = true;
        a(0f, 0f);
        setSize(1);
        setInvisible(true);
        super.a(NullBoundingBox.INSTANCE);
        this.line = line;
    }

    @Override
    public void a(AxisAlignedBB boundingBox) {}

    @Override
    public void t_() {
        if (this.ticksLived % 20 == 0)
            if (this.vehicle == null)
                die();
        if (!this.lockTick)
            super.s_();
    }

    @Override
    public void b(NBTTagCompound nbttagcompound) {}

    @Override
    public boolean c(NBTTagCompound nbttagcompound) {
        return false;
    }

    @Override
    public boolean d(NBTTagCompound nbttagcompound) {
        return false;
    }

    @Override
    public void e(NBTTagCompound nbttagcompound) {}

    @Override
    public boolean damageEntity(DamageSource damageSource, float amount) {
        if (damageSource instanceof EntityDamageSource) {
            EntityDamageSource entityDamageSource = (EntityDamageSource)damageSource;
            if (entityDamageSource.getEntity() instanceof EntityPlayer)
                Bukkit.getPluginManager().callEvent(new PlayerInteractEntityEvent(((EntityPlayer) entityDamageSource.getEntity()).getBukkitEntity(), getBukkitEntity()));
        }
        return false;
    }

    @Override
    public boolean isInvulnerable(DamageSource source) {
        return true;
    }

    @Override
    public void setCustomName(String customName) {}

    @Override
    public void setCustomNameVisible(boolean visible) {}

    @Override
    public void makeSound(String sound, float volume, float pitch) {}

    /**
     * Get the line attached to this
     * entity
     *
     * @return the entity line
     */
    @Override
    public HologramLine getLine() {
        return line;
    }

    /**
     * Set if the entity is locked in its
     * current tick
     *
     * @param lock the entity lockTick status
     */
    @Override
    public void setLockTick(final boolean lock) {
        lockTick = lock;
    }

    /**
     * Move the entity
     *
     * @param x the entity new X position
     * @param y the entity new Y position
     * @param z the entity new Z position
     */
    @Override
    public void moveTo(final double x, final double y, final double z) {
        setPosition(x, y, z);
    }

    /**
     * Kill the minecraft entity
     */
    @Override
    public void killEntity() {
        die();
    }

    /**
     * Get the minecraft entity ID
     *
     * @return the minecraft entity ID
     */
    @Override
    public int getEntityId() {
        return getId();
    }

    /**
     * Get the minecraft entity bukkit instance
     *
     * @return the minecraft entity as a bukkit
     * entity
     */
    @Override
    public Entity toBukkitEntity() {
        return getBukkitEntity();
    }

    @Override
    public void die() {
        setLockTick(false);
        super.die();
    }

    @Override
    public CraftEntity getBukkitEntity() {
        if (this.bukkitEntity == null)
            this.bukkitEntity = new CraftHologramSlime(this.world.getServer(), this);

        return this.bukkitEntity;
    }

    /**
     * Mount an entity
     *
     * @param base the entity to mount
     */
    @Override
    public void mount(final MinecraftEntity base) {
        if (!(base instanceof net.minecraft.server.v1_8_R3.Entity)) return;
        net.minecraft.server.v1_8_R3.Entity entity = (net.minecraft.server.v1_8_R3.Entity) base;

        try {
            ReflectionUtils.setPrivateField(Entity.class, this, "ap", 0.0D);
            ReflectionUtils.setPrivateField(Entity.class, this, "aq", 0.0D);
        } catch (Exception ex) {
            ExceptionCollector.catchException(HologramItem.class, ex);
        }

        if (this.vehicle != null)
            this.vehicle.passenger = null;

        this.vehicle = entity;
        entity.passenger = this;
    }
}
