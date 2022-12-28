package net.bonn2.bigdoorsphysics.shulkermethod;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.bonn2.bigdoorsphysics.BigDoorsPhysics.PLUGIN;

public class ColliderShulker {
    private static final double SHULKER_OFFSET = 0.74063;

    private final Location spawnLocation;
    private Shulker shulker;
    private ArmorStand armorStand;

    private final List<UUID> movedPlayers = new ArrayList<>();

    public ColliderShulker(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
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

        for (Player player : PLUGIN.getServer().getOnlinePlayers()) {
            if (player.getBoundingBox().overlaps(shulker.getBoundingBox())) {
                if (!movedPlayers.contains(player.getUniqueId()) && player.getLocation().getY() > newLocation.getY() + 0.5) {
                    player.teleport(player.getLocation().add(0, 0.5, 0));
                    movedPlayers.add(player.getUniqueId());
                } else {
                    player.setVelocity(velocity.multiply(1.5));
                    if (!movedPlayers.contains(player.getUniqueId())) movedPlayers.add(player.getUniqueId());
                }
            }
        }
    }

    public void remove() {
        shulker.remove();
        armorStand.remove();
    }
}