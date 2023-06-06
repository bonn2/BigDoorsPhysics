package net.bonn2.bigdoorsphysics.shulkermethod;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Shulker;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.bonn2.bigdoorsphysics.BigDoorsPhysics.PLUGIN;

public class CommandListener implements CommandExecutor {
    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("bigdoorsphysics.admin.killentities") && !sender.isOp()) {
            sender.sendMessage("You do not have permission to use this command!");
            return true;
        }
        long count = 0;
        for (World world : PLUGIN.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Shulker && Objects.equals(entity.getCustomName(), "BigDoorsPhysicsS")) {
                    entity.remove();
                    count++;
                }
                if (entity instanceof ArmorStand && Objects.equals(entity.getCustomName(), "BigDoorsPhysicsAS")) {
                    entity.remove();
                    count++;
                }
            }
        }
        sender.sendMessage("Removed " + count + " entities!");
        return true;
    }
}
