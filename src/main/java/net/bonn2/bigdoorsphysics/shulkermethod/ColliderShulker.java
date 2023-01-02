package net.bonn2.bigdoorsphysics.shulkermethod;

import net.bonn2.bigdoorsphysics.util.Config;
import net.kyori.adventure.text.Component;
import nl.pim16aap2.bigDoors.reflection.BukkitReflectionUtil;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ColliderShulker {
    public static final double SHULKER_OFFSET = 0.74063;

    private final Location spawnLocation;
    private Shulker shulker;
    private ArmorStand armorStand;

    private final List<UUID> movedEntities = new ArrayList<>();

    public ColliderShulker(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public BoundingBox getBoundingBox() {
        return shulker.getBoundingBox();
    }

    public Location getLocation() {
        return shulker.getLocation();
    }

    public void place() {
        armorStand = spawnLocation.getWorld().spawn(spawnLocation.clone().add(0, 100000, 0), ArmorStand.class);
        armorStand.setPersistent(true);
        armorStand.customName(Component.text("BigDoorsPhysicsAS"));
        armorStand.setCustomNameVisible(false);
        armorStand.setGravity(false);
        armorStand.setBasePlate(false);
        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setAI(false);
        armorStand.setSilent(true);
        armorStand.setCanPickupItems(false);
        armorStand.setSmall(true);
        shulker = spawnLocation.getWorld().spawn(spawnLocation.clone().add(0, 100000, 0), Shulker.class);
        shulker.setPersistent(true);
        shulker.customName(Component.text("BigDoorsPhysicsS"));
        shulker.setCustomNameVisible(false);
        shulker.setInvisible(true);
        shulker.setGravity(false);
        shulker.setAI(false);
        shulker.setPeek(0);
        shulker.setAware(false);
        shulker.setCanPickupItems(false);
        armorStand.addPassenger(shulker);
        armorStand.teleport(spawnLocation.subtract(0, SHULKER_OFFSET, 0));
    }

    public void move(@NotNull Location location, Vector velocity) {
        Location newLocation = location.clone().subtract(0, SHULKER_OFFSET, 0);
        if (newLocation == location) return;

        armorStand.teleport(location.clone().subtract(0, SHULKER_OFFSET, 0), true);

        if (Config.movePlayerWithShulker()) {
            for (Player player : location.getWorld().getPlayers()) {
                if (player.getBoundingBox().overlaps(shulker.getBoundingBox())) {
                    BukkitReflectionUtil.resetFlyingCounters(player);
                    if (!movedEntities.contains(player.getUniqueId()) && player.getLocation().getY() > newLocation.getY() + 0.5) {
                        player.teleport(player.getLocation().add(0, 0.5, 0));
                        movedEntities.add(player.getUniqueId());
                    } else {
                        player.setVelocity(velocity.multiply(1.5));
                        if (!movedEntities.contains(player.getUniqueId())) movedEntities.add(player.getUniqueId());
                    }
                }
            }
        }

        if (Config.moveEntityWithShulker()) {
            for (Entity entity : location.getNearbyEntities(3, 3, 3)) {
                if (entity instanceof Player
                        || entity instanceof FallingBlock
                        || entity.name().equals(Component.text("BigDoorsPhysicsS"))
                        || entity.name().equals(Component.text("BigDoorsPhysicsAS"))) continue;
                if (entity.getBoundingBox().overlaps(shulker.getBoundingBox())) {
                    if (!movedEntities.contains(entity.getUniqueId()) && entity.getLocation().getY() > newLocation.getY() + 0.5) {
                        entity.teleport(entity.getLocation().add(0, 0.5, 0));
                        movedEntities.add(entity.getUniqueId());
                    } else {
                        entity.setVelocity(velocity.multiply(1.5));
                        if (!movedEntities.contains(entity.getUniqueId())) movedEntities.add(entity.getUniqueId());
                    }
                }
            }
        }
    }

    public void remove() {
        shulker.remove();
        armorStand.remove();
    }
}
