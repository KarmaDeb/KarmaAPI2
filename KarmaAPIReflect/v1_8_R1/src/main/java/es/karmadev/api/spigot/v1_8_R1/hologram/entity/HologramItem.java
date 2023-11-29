package es.karmadev.api.spigot.v1_8_R1.hologram.entity;

import es.karmadev.api.core.ExceptionCollector;
import es.karmadev.api.spigot.nms.common.hologram.util.ReflectionUtils;
import es.karmadev.api.spigot.reflection.hologram.line.HologramLine;
import es.karmadev.api.spigot.reflection.hologram.line.type.ItemHolderLine;
import es.karmadev.api.spigot.reflection.hologram.nms.MinecraftEntity;
import es.karmadev.api.spigot.reflection.hologram.nms.entity.MinecraftItem;
import es.karmadev.api.spigot.v1_8_R1.hologram.entity.craft.CraftHologramItem;
import net.minecraft.server.v1_8_R1.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HologramItem extends EntityItem implements MinecraftItem {
    private boolean lockTick;
    private final ItemHolderLine line;

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

    /**
     * Mount an entity
     *
     * @param base the entity to mount
     */
    @Override
    public void mount(final MinecraftEntity base) {
        if (!(base instanceof net.minecraft.server.v1_8_R1.Entity)) return;
        net.minecraft.server.v1_8_R1.Entity entity = (net.minecraft.server.v1_8_R1.Entity) base;

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

    /**
     * Set the item stack
     *
     * @param stack the stack
     */
    @Override
    public void setStack(final ItemStack stack) {
        net.minecraft.server.v1_8_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(stack);
        if (nmsItem == null)
            nmsItem = new net.minecraft.server.v1_8_R1.ItemStack(Blocks.BEDROCK);

        if (nmsItem.getTag() == null)
            nmsItem.setTag(new NBTTagCompound());

        NBTTagCompound compound = nmsItem.getTag();
        NBTTagCompound display = compound.getCompound("display");
        if (!compound.hasKey("display"))
            compound.set("display", display);

        NBTTagList list = new NBTTagList();
        list.add(new NBTTagString("§0" + Math.random()));
        display.set("Lore", list);
        nmsItem.count = 0;
        setItemStack(nmsItem);
    }

    /**
     * Set if players should be able to
     * pick up this item
     *
     * @param allow if players/entities are allowed to
     *              pickup
     */
    @Override
    public void allowPickup(final boolean allow) {
        this.pickupDelay = (allow ? 0 : Integer.MAX_VALUE);
    }

    /**
     * Get the minecraft item stack
     *
     * @return the item stack
     */
    @Override
    public Object getStack() {
        return getItemStack();
    }
}
