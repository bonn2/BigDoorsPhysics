package net.bonn2.bigdoorsphysics.shulkermethod;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import nl.pim16aap2.bigDoors.BigDoors;
import nl.pim16aap2.bigDoors.events.DoorEventToggleEnd;
import nl.pim16aap2.bigDoors.events.DoorEventToggleStart;
import nl.pim16aap2.bigDoors.moveBlocks.BlockMover;
import nl.pim16aap2.bigDoors.util.MyBlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;

import static net.bonn2.bigdoorsphysics.BigDoorsPhysics.CONFIG;
import static net.bonn2.bigdoorsphysics.BigDoorsPhysics.PLUGIN;

public class ShulkerListener implements Listener {

    private final Map<Long, BlockMover> BLOCK_MOVERS = new HashMap<>();
    private final Map<Long, List<ColliderShulker>> COLLIDERS = new HashMap<>();

    public Map<Long, List<ColliderShulker>> getColliders() {
        return COLLIDERS;
    }
    @EventHandler
    public void onBigDoorsToggleStart(@NotNull DoorEventToggleStart startEvent) {
        if (!Objects.requireNonNull(CONFIG.getString("method")).equalsIgnoreCase("SHULKER")) return;
        if (startEvent.instantOpen()) return;
        BLOCK_MOVERS.put(startEvent.getDoor().getDoorUID(), BigDoors.get().getCommander().getBlockMover(startEvent.getDoor().getDoorUID()));
    }

    @EventHandler
    public void onBigDoorsToggleEnd(@NotNull DoorEventToggleEnd endEvent) {
        if (!Objects.requireNonNull(CONFIG.getString("method")).equalsIgnoreCase("SHULKER")) return;
        if (endEvent.instantOpen()) return;

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

        for (ColliderShulker block : COLLIDERS.get(endEvent.getDoor().getDoorUID())) {
            block.remove();
        }
        COLLIDERS.remove(endEvent.getDoor().getDoorUID());
        BLOCK_MOVERS.remove(endEvent.getDoor().getDoorUID());
    }

    @EventHandler
    public void updateCollisions(ServerTickEndEvent tickEndEvent) {
        if (!Objects.requireNonNull(CONFIG.getString("method")).equalsIgnoreCase("SHULKER")) return;
        for (long id : BLOCK_MOVERS.keySet()) {
            List<MyBlockData> doorBlocks;
            try {
                Field SavedBlocks = BlockMover.class.getDeclaredField("savedBlocks");
                SavedBlocks.setAccessible(true);
                // Get private variable of MyBlockData List
                doorBlocks = (List<MyBlockData>) SavedBlocks.get(BLOCK_MOVERS.get(id));
                if (doorBlocks.size() == 0) continue;
                if (COLLIDERS.containsKey(id)) {
                    for (int i = 0; i < doorBlocks.size(); i++) {
                        COLLIDERS.get(id).get(i).move(doorBlocks.get(i).getFBlock().getLocation(), doorBlocks.get(i).getFBlock().getVelocity());
                    }
                }
                else {
                    List<ColliderShulker> shulkers = new ArrayList<>(doorBlocks.size());
                    for (MyBlockData doorBlock : doorBlocks) {
                        shulkers.add(new ColliderShulker(doorBlock.getFBlock().getLocation()));
                        COLLIDERS.put(id, shulkers);
                    }
                    COLLIDERS.get(id).forEach(ColliderShulker::place);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
