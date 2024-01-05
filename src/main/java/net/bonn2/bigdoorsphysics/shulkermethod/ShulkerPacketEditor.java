package net.bonn2.bigdoorsphysics.shulkermethod;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.bonn2.bigdoorsphysics.BigDoorsPhysics;
import net.bonn2.bigdoorsphysics.util.CollisionMethod;
import net.bonn2.bigdoorsphysics.util.Config;
import org.bukkit.entity.Entity;

import java.util.Objects;
import java.util.UUID;

import static net.bonn2.bigdoorsphysics.BigDoorsPhysics.PLUGIN;

public class ShulkerPacketEditor {

    public static void register() {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        // Hide shulker spawn packet
        if (Config.spawnShulkersOnDoor() && Config.getCollisionMethod().containsValue(CollisionMethod.SHULKER)) {
            manager.addPacketListener(
                    new PacketAdapter(PLUGIN, ListenerPriority.HIGHEST,
                            PacketType.Play.Server.SPAWN_ENTITY) {
                        @Override
                        public void onPacketSending(PacketEvent event) {
                            // Check if any doors are moving
                            if (BigDoorsPhysics.SHULKER_LISTENER.getColliders().keySet().size() == 0) return;
                            // Get uuid of the spawned entity
                            UUID uuid = event.getPacket().getUUIDs().read(0);
                            if (ColliderShulker.getOwnedUUIDs().contains(uuid)) {
                                PacketContainer packetContainer = event.getPacket().deepClone();
                                packetContainer.getModifier().write(4, event.getPlayer().getWorld().getMaxHeight() + 10000);
                                event.setPacket(packetContainer);
                            }
                        }
                    }
            );
        }

        // Hide shulker move packets
        if (Config.cullDistantShulkers() && Config.getCollisionMethod().containsValue(CollisionMethod.SHULKER)) {
            manager.addPacketListener(
                    new PacketAdapter(PLUGIN, ListenerPriority.HIGHEST,
                            PacketType.Play.Server.ENTITY_TELEPORT) {
                        @Override
                        public void onPacketSending(PacketEvent event) {
                            // Check if any doors are moving
                            if (BigDoorsPhysics.SHULKER_LISTENER.getColliders().keySet().size() == 0) return;
                            // Get entity the packet is acting on
                            Entity entity = event.getPacket().getEntityModifier(event).getValues().get(0);
                            if (Objects.equals(entity, null)) return;
                            // Only hide BigDoorsPhysics entities
                            if (Objects.equals(entity.getCustomName(), "BigDoorsPhysicsAS")) {
                                PacketContainer packetContainer = event.getPacket().deepClone();
                                // Calculate the distance
                                double distance = event.getPlayer().getLocation().distance(entity.getLocation().add(0, BigDoorsPhysics.VERSION_UTIL.getShulkerOffset(), 0));
                                // Only cull shulker if they are too far away to interact with
                                if (distance > Config.shulkerCullDistance()) {
                                    packetContainer.getModifier().write(2, event.getPlayer().getWorld().getMaxHeight() + 10000);
                                    event.setPacket(packetContainer);
                                }
                            }
                        }
                    }
            );

            manager.addPacketListener(
                    new PacketAdapter(PLUGIN, ListenerPriority.HIGHEST,
                            PacketType.Play.Server.REL_ENTITY_MOVE) {
                        @Override
                        public void onPacketSending(PacketEvent event) {
                            // Check if any doors are moving
                            if (BigDoorsPhysics.SHULKER_LISTENER.getColliders().keySet().size() == 0) return;
                            // Get entity the packet is acting on
                            Entity entity = event.getPacket().getEntityModifier(event).getValues().get(0);
                            if (Objects.equals(entity, null)) return;
                            // Only hide BigDoorsPhysics entities
                            if (Objects.equals(entity.getCustomName(), "BigDoorsPhysicsAS")) {
                                // Cancel original packet
                                event.setCancelled(true);

                                // Calculate distance
                                double distance = event.getPlayer().getLocation().distance(entity.getLocation().add(0, BigDoorsPhysics.VERSION_UTIL.getShulkerOffset(), 0));

                                // Create replacement teleportation packet
                                PacketContainer newPacket;
                                newPacket = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
                                newPacket.getIntegers().write(0, entity.getEntityId());
                                newPacket.getDoubles().write(0, entity.getLocation().getX());
                                newPacket.getDoubles().write(2, entity.getLocation().getZ());
                                newPacket.getBytes().write(0, (byte) 0);
                                newPacket.getBytes().write(1, (byte) 0);
                                newPacket.getBooleans().write(0, false);

                                // Set Y value to show or hide shulker
                                if (distance > Config.shulkerCullDistance()) {
                                    // Hide Shulker
                                    newPacket.getDoubles().write(1, entity.getLocation().getY() + 10000);
                                } else {
                                    // Show Shulker
                                    newPacket.getDoubles().write(1, entity.getLocation().getY());
                                }

                                // Send new packet
                                manager.sendServerPacket(event.getPlayer(), newPacket);
                            }
                        }
                    }
            );
        }
    }
}
