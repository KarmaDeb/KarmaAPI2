package es.karmadev.api.spigot.v1_8_R1.hologram.entity;

import es.karmadev.api.spigot.reflection.hologram.line.TouchableLine;
import es.karmadev.api.spigot.v1_8_R1.hologram.entity.craft.CraftHologramSlime;
import es.karmadev.api.spigot.v1_8_R1.hologram.entity.util.NullBoundingBox;
import net.minecraft.server.v1_8_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftSlime;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class HologramSlime extends EntitySlime {

    private boolean lockTick;

    public HologramSlime(final World world) {
        super(((CraftWorld) world).getHandle());
        this.persistent = true;
        a(0f, 0f);
        setSize(1);
        setInvisible(true);
        super.a(NullBoundingBox.INSTANCE);
    }

    @Override
    public void a(AxisAlignedBB boundingBox) {}

    @Override
    public void s_() {
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

    public void setLockTick(boolean lock) {
        this.lockTick = lock;
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
}
