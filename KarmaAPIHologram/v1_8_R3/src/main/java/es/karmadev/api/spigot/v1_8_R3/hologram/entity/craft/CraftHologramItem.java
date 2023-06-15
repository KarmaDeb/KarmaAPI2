package es.karmadev.api.spigot.v1_8_R3.hologram.entity.craft;

import es.karmadev.api.spigot.v1_8_R3.hologram.entity.HologramItem;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftItem;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class CraftHologramItem extends CraftItem {

    public CraftHologramItem(CraftServer server, HologramItem entity) {
        super(server, entity);
    }

    @Override
    public void remove() {}

    @Override
    public void setVelocity(Vector vel) {}

    @Override
    public boolean teleport(Location loc) {
        return false;
    }

    @Override
    public boolean teleport(Entity entity) {
        return false;
    }

    @Override
    public boolean teleport(Location loc, PlayerTeleportEvent.TeleportCause cause) {
        return false;
    }

    @Override
    public boolean teleport(Entity entity, PlayerTeleportEvent.TeleportCause cause) {
        return false;
    }

    @Override
    public void setFireTicks(int ticks) {}

    @Override
    public boolean setPassenger(Entity entity) {
        return false;
    }

    @Override
    public boolean eject() {
        return false;
    }

    @Override
    public boolean leaveVehicle() {
        return false;
    }

    @Override
    public void playEffect(EntityEffect effect) {}

    @Override
    public void setCustomName(String name) {}

    @Override
    public void setCustomNameVisible(boolean flag) {}

    @Override
    public void setItemStack(ItemStack stack) {}

    @Override
    public void setPickupDelay(int delay) {}
}

