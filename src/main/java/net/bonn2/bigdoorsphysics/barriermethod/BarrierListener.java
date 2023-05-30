package net.bonn2.bigdoorsphysics.barriermethod;

import net.bonn2.bigdoorsphysics.util.CollisionMethod;
import net.bonn2.bigdoorsphysics.util.Config;
import nl.pim16aap2.bigDoors.BigDoors;
import nl.pim16aap2.bigDoors.events.DoorEventToggleEnd;
import nl.pim16aap2.bigDoors.events.DoorEventToggleStart;
import nl.pim16aap2.bigDoors.moveBlocks.BlockMover;
import nl.pim16aap2.bigDoors.util.MyBlockData;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarrierListener implements Listener {

    private final Map<Long, BlockMover> BLOCK_MOVERS = new HashMap<>();
    private final Map<Long, List<ColliderBlock>> COLLIDERS = new HashMap<>();

    public Map<Long, List<ColliderBlock>> getColliders() {
        return COLLIDERS;
    }

    @EventHandler
    public void onBigDoorsToggleStart(@NotNull DoorEventToggleStart startEvent) {
        if (startEvent.instantOpen()) return;
        if (!Config.getCollisionMethod().getOrDefault(startEvent.getDoor().getType(), CollisionMethod.NONE).equals(CollisionMethod.BARRIER)) return;
        BLOCK_MOVERS.put(startEvent.getDoor().getDoorUID(), BigDoors.get().getCommander().getBlockMover(startEvent.getDoor().getDoorUID()));
    }

    @EventHandler
    public void onBigDoorsToggleEnd(@NotNull DoorEventToggleEnd endEvent) {
        if (endEvent.instantOpen()) return;
        if (!Config.getCollisionMethod().getOrDefault(endEvent.getDoor().getType(), CollisionMethod.NONE).equals(CollisionMethod.BARRIER)) return;
        for (ColliderBlock block : COLLIDERS.get(endEvent.getDoor().getDoorUID())) {
            block.remove();
        }
        COLLIDERS.remove(endEvent.getDoor().getDoorUID());
        BLOCK_MOVERS.remove(endEvent.getDoor().getDoorUID());
    }

    public void updateCollisions() {
        for (long id : BLOCK_MOVERS.keySet()) {
            if (!Config.getCollisionMethod().getOrDefault(BLOCK_MOVERS.get(id).getDoor().getType(), CollisionMethod.NONE).equals(CollisionMethod.BARRIER)) return;
            // Get saved blocks
            List<MyBlockData> doorBlocks = BLOCK_MOVERS.get(id).getSavedBlocks();
            if (doorBlocks.size() == 0) continue;
            // Move barrier blocks
            if (COLLIDERS.containsKey(id)) {
                List<ColliderBlock> oldBlocks = COLLIDERS.get(id);
                List<ColliderBlock> blocks = new ArrayList<>(doorBlocks.size());
                for (MyBlockData doorBlock : doorBlocks) {
                    Location direction = new Location(doorBlock.getFBlock().getLocation().getWorld(), 0, 0, 0);
                    // Only calculate direction if players should be moved
                    if (Config.movePlayerWithBarrier()) {
                        Vector velocity = doorBlock.getFBlock().getVelocity();
                        if (velocity.getX() > 0)
                            direction = direction.add(1,0,0);
                        if (velocity.getX() < 0)
                            direction = direction.add(-1, 0, 0);
                        if (velocity.getY() > 0)
                            direction = direction.add(0, 1, 0);
                        if (velocity.getY() < 0)
                            direction = direction.add(0, -1, 0);
                        if (velocity.getZ() > 0)
                            direction = direction.add(0, 0, 1);
                        if (velocity.getZ() < 0)
                            direction = direction.add(0, 0, -1);
                    }
                    blocks.add(new ColliderBlock(
                                doorBlock.getFBlock().getLocation().add(0, 0.1, 0).toCenterLocation(),
                                direction
                            ));
                    COLLIDERS.put(id, blocks);
                }
                for (ColliderBlock oldBlock : oldBlocks)
                    oldBlock.remove();
                COLLIDERS.get(id).forEach(ColliderBlock::place);
            }
            // Place initial barrier blocks
            else {
                List<ColliderBlock> blocks = new ArrayList<>(doorBlocks.size());
                for (MyBlockData doorBlock : doorBlocks) {
                    blocks.add(new ColliderBlock(
                            doorBlock.getFBlock().getLocation().add(0, 0.1, 0).toBlockLocation(),
                            new Location(doorBlock.getFBlock().getLocation().getWorld(), 0, 0, 0)
                    ));
                    COLLIDERS.put(id, blocks);
                }
                COLLIDERS.get(id).forEach(ColliderBlock::place);
            }
        }
    }
}
