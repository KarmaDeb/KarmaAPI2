package es.karmadev.api.spigot.v1_8_R1.hologram.entity;

import es.karmadev.api.spigot.reflection.hologram.line.type.ItemHolderLine;
import es.karmadev.api.spigot.v1_8_R1.hologram.entity.craft.CraftHologramItem;
import net.minecraft.server.v1_8_R1.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;
import org.bukkit.entity.Player;

public class HologramItem extends EntityItem {
    private boolean lockTick;
    private ItemHolderLine line;

    public HologramItem(final World world, final ItemHolderLine line) {
        super(((CraftWorld) world).getHandle());
        this.pickupDelay = Integer.MAX_VALUE;
        this.line = line;
    }

    @Override
    public void s_() {
        this.ticksLived = 0;
        if (!this.lockTick)
            super.s_();
    }

    @Override
    public void d(EntityHuman human) {
        if (human.locY < this.locY - 1.5D || human.locY > this.locY + 1.0D)
            return;

        if (line.getPickupHandler() != null && human instanceof net.minecraft.server.v1_8_R1.EntityPlayer)
            line.getPickupHandler().onPickup((Player) human);
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
    public boolean isInvulnerable(DamageSource source) {
        return true;
    }

    @Override
    public void inactiveTick() {
        if (!this.lockTick)
            super.inactiveTick();
    }

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
            this.bukkitEntity = new CraftHologramItem(this.world.getServer(), this);

        return this.bukkitEntity;
    }
}
