package net.bonn2.bigdoorsphysics;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class BigDoorsPhysics extends JavaPlugin {

    public static BigDoorsPhysics PLUGIN;

    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        PLUGIN = this;
        protocolManager = ProtocolLibrary.getProtocolManager();
        getServer().getPluginManager().registerEvents(new PhysicsListener(), this);
/*
        // Start packet editor
        protocolManager.addPacketListener(
                new PacketAdapter(this, ListenerPriority.NORMAL,
                        PacketType.Play.Server.ENTITY_TELEPORT) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        // Get entity that the packet is acting on
                        StructureModifier<Entity> entityModifier = event.getPacket().getEntityModifier(event);
                        // Only hide BigDoorsPhysics entities
                        if (Objects.equals(entityModifier.getValues().get(0).customName(), Component.text("BigDoorsPhysics"))) {
                            // Send BigDoorsPhysics entities to fucking narnia on the clients end
                            PacketContainer packetContainer = event.getPacket().deepClone();
                            // Get how far away shulker is from player
                            double distance = event.getPlayer().getLocation().distance(new Location(
                                    event.getPlayer().getWorld(),
                                    ((Double) packetContainer.getModifier().getValues().get(1)), // X Location
                                    ((Double) packetContainer.getModifier().getValues().get(2)), // Y Location
                                    ((Double) packetContainer.getModifier().getValues().get(3))  // Z Location
                            ));
                            // Only cull shulker if they are too far away to interact with
                            if (distance > 2) {
                                packetContainer.getModifier().modify(2, y -> entityModifier.getValues().get(0).getWorld().getMinHeight() - 10000);
                                event.setPacket(packetContainer);
                            }
                        }
                    }
                }
        );*/
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
