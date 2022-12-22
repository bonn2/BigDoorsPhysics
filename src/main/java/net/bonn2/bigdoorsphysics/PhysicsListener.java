package net.bonn2.bigdoorsphysics;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import nl.pim16aap2.bigDoors.BigDoors;
import nl.pim16aap2.bigDoors.events.DoorEventToggleEnd;
import nl.pim16aap2.bigDoors.events.DoorEventToggleStart;
import nl.pim16aap2.bigDoors.moveBlocks.BlockMover;
import nl.pim16aap2.bigDoors.util.MyBlockData;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.bonn2.bigdoorsphysics.BigDoorsPhysics.PLUGIN;

public class PhysicsListener implements Listener {

    private Map<Long, BlockMover> blockMovers = new HashMap<>();
    private Map<Long, List<ColliderBlock>> colliders = new HashMap<>();

    @EventHandler
    public void onBigDoorsToggleStart(@NotNull DoorEventToggleStart startEvent) {
        if (startEvent.instantOpen()) return;
        blockMovers.put(startEvent.getDoor().getDoorUID(), BigDoors.get().getCommander().getBlockMover(startEvent.getDoor().getDoorUID()));
    }

    @EventHandler
    public void onBigDoorsToggleEnd(@NotNull DoorEventToggleEnd endEvent) {
        if (endEvent.instantOpen()) return;
        for (ColliderBlock block : colliders.get(endEvent.getDoor().getDoorUID())) {
            block.remove();
        }
        colliders.remove(endEvent.getDoor().getDoorUID());
        blockMovers.remove(endEvent.getDoor().getDoorUID());
        PLUGIN.getLogger().info("b");
    }

    @EventHandler
    public void updateCollisions(ServerTickEndEvent tickEndEvent) {
        for (long id : blockMovers.keySet()) {
            List<MyBlockData> doorBlocks;
            try {
                Field SavedBlocks = BlockMover.class.getDeclaredField("savedBlocks");
                SavedBlocks.setAccessible(true);
                // Get private variable of MyBlockData List
                doorBlocks = (List<MyBlockData>) SavedBlocks.get(blockMovers.get(id));
                if (doorBlocks.size() == 0) continue;
                if (colliders.containsKey(id)) {
                    List<ColliderBlock> oldBlocks = colliders.get(id);
                    List<ColliderBlock> blocks = new ArrayList<>(doorBlocks.size());
                    for (MyBlockData doorBlock : doorBlocks) {
                        blocks.add(new ColliderBlock(doorBlock.getFBlock().getLocation().toCenterLocation()));
                        colliders.put(id, blocks);
                        PLUGIN.getLogger().info("A");
                    }
                    for (ColliderBlock oldBlock : oldBlocks)
                        oldBlock.remove();
                    colliders.get(id).forEach(ColliderBlock::place);
                }
                else {
                    List<ColliderBlock> blocks = new ArrayList<>(doorBlocks.size());
                    for (MyBlockData doorBlock : doorBlocks) {
                        blocks.add(new ColliderBlock(doorBlock.getFBlock().getLocation().toBlockLocation()));
                        colliders.put(id, blocks);
                    }
                    colliders.get(id).forEach(ColliderBlock::place);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
