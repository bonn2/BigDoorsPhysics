package net.bonn2.bigdoorsphysics.shulkermethod;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import static net.bonn2.bigdoorsphysics.BigDoorsPhysics.PLUGIN;

public class ShulkerPacketEditor {

    public static void register() {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(
                new PacketAdapter(PLUGIN, ListenerPriority.HIGHEST,
                        PacketType.Play.Server.ENTITY_TELEPORT) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        // Get entity the packet is acting on
                        Entity entity = event.getPacket().getEntityModifier(event).getValues().get(0);
                        // Only hide BigDoorsPhysics entities
                        if (Objects.equals(entity.customName(), Component.text("BigDoorsPhysicsAS"))) {
                            PacketContainer packetContainer = event.getPacket().deepClone();
                            // Calculate the distance
                            double distance = event.getPlayer().getLocation().distance(entity.getLocation().add(0, ColliderShulker.SHULKER_OFFSET, 0));
                            // Only cull shulker if they are too far away to interact with
                            if (distance > 4) {
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
                        // Get entity the packet is acting on
                        Entity entity = event.getPacket().getEntityModifier(event).getValues().get(0);
                        // Only hide BigDoorsPhysics entities
                        if (Objects.equals(entity.customName(), Component.text("BigDoorsPhysicsAS"))) {
                            // Cancel original packet
                            event.setCancelled(true);

                            // Calculate distance
                            double distance = event.getPlayer().getLocation().distance(entity.getLocation().add(0, ColliderShulker.SHULKER_OFFSET, 0));

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
                            if (distance > 4) {
                                // Hide Shulker
                                newPacket.getDoubles().write(1, entity.getLocation().getY() + 10000);
                            } else {
                                // Show Shulker
                                newPacket.getDoubles().write(1, entity.getLocation().getY());
                            }

                            // Send new packet
                            try {
                                manager.sendServerPacket(event.getPlayer(), newPacket);
                            } catch (InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
        );
    }
}
