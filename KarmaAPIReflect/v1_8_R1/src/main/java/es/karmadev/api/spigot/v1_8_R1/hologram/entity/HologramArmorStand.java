package es.karmadev.api.spigot.v1_8_R1.hologram.entity;

import es.karmadev.api.spigot.nms.common.hologram.util.ReflectionUtils;
import es.karmadev.api.spigot.reflection.hologram.line.HologramLine;
import es.karmadev.api.spigot.reflection.hologram.nms.entity.MinecraftArmorStand;
import es.karmadev.api.spigot.v1_8_R1.hologram.entity.craft.CraftHologramStand;
import es.karmadev.api.spigot.v1_8_R1.hologram.entity.util.NullBoundingBox;
import net.minecraft.server.v1_8_R1.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

public final class HologramArmorStand extends EntityArmorStand implements MinecraftArmorStand {

    private boolean lockTick;
    private final HologramLine line;

    /**
     * Create a new hologram armor stand
     *
     * @param world the world to create at
     */

    public HologramArmorStand(final World world, final HologramLine  line) {
        super(((CraftWorld) world).getHandle(), 0d, 0d, 0d);
        setInvisible(true);
        setSmall(true);
        setArms(false);
        setGravity(true);
        setBasePlate(true);
        this.line = line;
        try {
            ReflectionUtils.setPrivateField(EntityArmorStand.class, this, "bg", 2147483647);
        } catch (Exception ignored) {}
        super.a(NullBoundingBox.INSTANCE);
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
    public void setCustomName(String customName) {}

    @Override
    public void setCustomNameVisible(boolean visible) {}

    @Override
    public boolean a(EntityHuman human, Vec3D vec3d) {
        return true;
    }

    @Override
    public boolean d(int i, ItemStack item) {
        return false;
    }

    @Override
    public void setEquipment(int i, ItemStack item) {}

    @Override
    public void a(AxisAlignedBB boundingBox) {}

    @Override
    public int getId() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        StackTraceElement element = null;
        if (elements.length >= 2) element = elements[2];

        if (element != null && element.getFileName() != null && element.getFileName().equals("EntityTrackerEntry.java") && element.getLineNumber() > 137 && element.getLineNumber() < 147)
            return -1;

        return super.getId();
    }

    @Override
    public void s_() {
        if (!this.lockTick)
            super.s_();
    }

    @Override
    public void makeSound(String sound, float f1, float f2) {}

    /**
     * Get the line attached to this
     * entity
     *
     * @return the entity line
     */
    @Override
    public HologramLine getLine() {
        return this.line;
    }

    /**
     * Set if the entity is locked in its
     * current tick
     *
     * @param lockTick the entity lockTick status
     */
    @Override
    public void setLockTick(final boolean lockTick) {
        this.lockTick = lockTick;
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
        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(
                getId(),
                MathHelper.floor(this.locX * 32.0D),
                MathHelper.floor(this.locY * 32.0D),
                MathHelper.floor(this.locZ * 32.0D),
                (byte)(int)(this.yaw * 256.0F / 360.0F),
                (byte)(int)(this.pitch * 256.0F / 360.0F),
                this.onGround);

        for (Object obj : this.world.players) {
            if (obj instanceof EntityPlayer) {
                EntityPlayer nmsPlayer = (EntityPlayer)obj;
                double pX = nmsPlayer.locX - this.locX;
                double pZ = nmsPlayer.locZ - this.locZ;

                double distanceSquared = (pX * pX) + (pZ * pZ);
                if (distanceSquared < 8192.0D && nmsPlayer.playerConnection != null)
                    nmsPlayer.playerConnection.sendPacket(teleportPacket);
            }
        }
    }

    /**
     * Kill the minecraft entity
     */
    @Override
    public void killEntity() {
        super.die();
    }

    /**
     * Get the minecraft entity ID
     *
     * @return the minecraft entity ID
     */
    @Override
    public int getEntityId() {
        return super.getId();
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
            this.bukkitEntity = new CraftHologramStand(this.world.getServer(), this);

        return this.bukkitEntity;
    }

    /**
     * Rename the entity
     *
     * @param name the entity new name
     */
    @Override
    public void renameEntity(String name) {
        if (name != null && name.length() > 300)
            name = name.substring(0, 300);
        super.setCustomName(name);
        super.setCustomNameVisible((name != null && !name.isEmpty()));
    }

    /**
     * Get the entity rename
     *
     * @return the new entity name
     */
    @Override
    public String entityRename() {
        return getCustomName();
    }
}
