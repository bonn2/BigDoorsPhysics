package net.bonn2.bigdoorsphysics.shulkermethod;

import net.bonn2.bigdoorsphysics.util.Config;
import net.bonn2.bigdoorsphysics.util.VersionUtils;
import net.kyori.adventure.text.Component;
import nl.pim16aap2.bigDoors.reflection.BukkitReflectionUtil;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ColliderShulker {
    public static final double SHULKER_OFFSET = 0.74063;

    private final Location spawnLocation;
    private Shulker shulker;
    private ArmorStand armorStand;

    private final List<UUID> movedEntities = new ArrayList<>();

    private static final List<UUID> ownedUUIDs = new ArrayList<>();

    public ColliderShulker(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public BoundingBox getBoundingBox() {
        return shulker.getBoundingBox();
    }

    public Location getLocation() {
        return shulker.getLocation();
    }

    /**
     * Get the uuid of the shulker and armor stand owned by this instance
     */
    public List<UUID> getUUIDs() {
        return List.of(armorStand.getUniqueId(), shulker.getUniqueId());
    }

    /**
     * Get a list of all entities that any instance of collider shulker owns
     */
    public static List<UUID> getOwnedUUIDs() {
        return ownedUUIDs;
    }

    public void place() {
        Location adjustedSpwanLocation = Config.spawnShulkersOnDoor() ?
                spawnLocation.clone().subtract(0, SHULKER_OFFSET,0) :
                spawnLocation.clone().add(0, 100000 + spawnLocation.getWorld().getMaxHeight(),0);

        armorStand = spawnLocation.getWorld().spawn(adjustedSpwanLocation, ArmorStand.class, entity -> {
            ownedUUIDs.add(entity.getUniqueId());
            entity.setPersistent(true);
            entity.customName(Component.text("BigDoorsPhysicsAS"));
            entity.setCustomNameVisible(false);
            entity.setGravity(false);
            entity.setBasePlate(false);
            entity.setInvisible(true);
            entity.setInvulnerable(true);
            entity.setAI(false);
            entity.setSilent(true);
            entity.setCanPickupItems(false);
            entity.setSmall(true);
        });
        shulker = spawnLocation.getWorld().spawn(adjustedSpwanLocation, Shulker.class, entity -> {
            ownedUUIDs.add(entity.getUniqueId());
            entity.setPersistent(true);
            entity.customName(Component.text("BigDoorsPhysicsS"));
            entity.setPeek(0);
            entity.setAware(false);
            entity.setCustomNameVisible(false);
            entity.setGravity(false);
            entity.setInvisible(true);
            entity.setInvulnerable(true);
            entity.setAI(false);
            entity.setSilent(true);
            entity.setCanPickupItems(false);
            armorStand.addPassenger(entity);
            armorStand.teleport(spawnLocation.subtract(0, SHULKER_OFFSET, 0));
        });
    }

    public void move(@NotNull Location location, Vector velocity) {
        Location newLocation = location.clone().subtract(0, SHULKER_OFFSET, 0);
        if (newLocation == location) return;

        VersionUtils.teleportWithPassenger(armorStand, shulker, location.clone().subtract(0, SHULKER_OFFSET, 0));

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
                        || Objects.equals(entity.getCustomName(), "BigDoorsPhysicsS")
                        || Objects.equals(entity.getCustomName(), "BigDoorsPhysicsAS")) continue;
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
        ownedUUIDs.remove(shulker.getUniqueId());
        shulker.remove();
        ownedUUIDs.remove(armorStand.getUniqueId());
        armorStand.remove();
    }
}
