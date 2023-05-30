package net.bonn2.bigdoorsphysics.shulkermethod;

import net.bonn2.bigdoorsphysics.util.CollisionMethod;
import net.bonn2.bigdoorsphysics.util.Config;
import nl.pim16aap2.bigDoors.BigDoors;
import nl.pim16aap2.bigDoors.events.DoorEventToggleEnd;
import nl.pim16aap2.bigDoors.events.DoorEventToggleStart;
import nl.pim16aap2.bigDoors.moveBlocks.BlockMover;
import nl.pim16aap2.bigDoors.util.MyBlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.bonn2.bigdoorsphysics.BigDoorsPhysics.PLUGIN;

public class ShulkerListener implements Listener {

    private final Map<Long, BlockMover> BLOCK_MOVERS = new HashMap<>();
    private final Map<Long, List<ColliderShulker>> COLLIDERS = new HashMap<>();
    private final List<UUID> ENTITY_UUIDS = new ArrayList<>();

    public Map<Long, List<ColliderShulker>> getColliders() {
        return COLLIDERS;
    }

    @EventHandler
    public void onBigDoorsToggleStart(@NotNull DoorEventToggleStart startEvent) {
        if (!Config.getCollisionMethod().getOrDefault(startEvent.getDoor().getType(), CollisionMethod.NONE).equals(CollisionMethod.SHULKER)) return;
        if (startEvent.instantOpen()) return;
        BLOCK_MOVERS.put(startEvent.getDoor().getDoorUID(), BigDoors.get().getCommander().getBlockMover(startEvent.getDoor().getDoorUID()));
    }

    @EventHandler
    public void onBigDoorsToggleEnd(@NotNull DoorEventToggleEnd endEvent) {
        if (!Config.getCollisionMethod().getOrDefault(endEvent.getDoor().getType(), CollisionMethod.NONE).equals(CollisionMethod.SHULKER)) return;
        if (endEvent.instantOpen()) return;

        if (COLLIDERS.containsKey(endEvent.getDoor().getDoorUID())) {
            if (Config.movePlayerWithShulker() && Config.correctEndingClipping()) {
                for (Player player : PLUGIN.getServer().getOnlinePlayers()) {
                    for (ColliderShulker block : COLLIDERS.get(endEvent.getDoor().getDoorUID())) {
                        if (player.getBoundingBox().overlaps(block.getBoundingBox().clone().shift(new Vector(0, 0.1, 0)))
                                && player.getLocation().getY() > block.getBoundingBox().getCenterY()) {
                            player.teleport(player.getLocation().add(
                                    0,
                                    player.getLocation().getY() - block.getBoundingBox().getCenterY() + 0.05,
                                    0));
                        }
                    }
                }
            }

            if (Config.moveEntityWithShulker() && Config.correctEndingClipping()) {
                for (ColliderShulker block : COLLIDERS.get(endEvent.getDoor().getDoorUID())) {
                    for (Entity entity : block.getLocation().getNearbyEntities(2, 2, 2)) {
                        if (entity instanceof Player
                                || entity instanceof FallingBlock
                                || Objects.equals(entity.getCustomName(), "BigDoorsPhysicsS")
                                || Objects.equals(entity.getCustomName(), "BigDoorsPhysicsAS")) continue;
                        if (entity.getBoundingBox().overlaps(block.getBoundingBox().clone().shift(new Vector(0, 0.1, 0)))
                                && entity.getLocation().getY() > block.getBoundingBox().getCenterY()) {
                            entity.teleport(entity.getLocation().add(
                                    0,
                                    entity.getLocation().getY() - block.getBoundingBox().getCenterY() + 0.05,
                                    0));
                        }
                    }
                }
            }

            for (ColliderShulker block : COLLIDERS.get(endEvent.getDoor().getDoorUID())) {
                ENTITY_UUIDS.removeAll(block.getUUIDs());
                block.remove();
            }
        }

        COLLIDERS.remove(endEvent.getDoor().getDoorUID());
        BLOCK_MOVERS.remove(endEvent.getDoor().getDoorUID());
    }

    public void updateCollisions() {
        for (long id : BLOCK_MOVERS.keySet()) {
            if (!Config.getCollisionMethod().getOrDefault(BLOCK_MOVERS.get(id).getDoor().getType(), CollisionMethod.NONE).equals(CollisionMethod.SHULKER)) continue;
            // Get saved blocks
            List<MyBlockData> doorBlocks = BLOCK_MOVERS.get(id).getSavedBlocks();
            if (doorBlocks.size() == 0) continue;
            // Update shulker locations
            if (COLLIDERS.containsKey(id)) {
                for (int i = 0; i < doorBlocks.size(); i++) {
                    COLLIDERS.get(id).get(i).move(doorBlocks.get(i).getFBlock().getLocation(), doorBlocks.get(i).getFBlock().getVelocity());
                }
            }
            // Spawn shulkers
            else {
                List<ColliderShulker> shulkers = new ArrayList<>(doorBlocks.size());
                for (MyBlockData doorBlock : doorBlocks) {
                    shulkers.add(new ColliderShulker(doorBlock.getFBlock().getLocation()));
                    COLLIDERS.put(id, shulkers);
                }
                COLLIDERS.get(id).forEach(ColliderShulker::place);
                COLLIDERS.get(id).forEach((colliderShulker -> ENTITY_UUIDS.addAll(colliderShulker.getUUIDs())));
            }
        }
    }

    @EventHandler
    public void protectShulkers(@NotNull EntityDamageEvent event) {
        if (ENTITY_UUIDS.contains(event.getEntity().getUniqueId()))
            event.setCancelled(true);
    }
}
