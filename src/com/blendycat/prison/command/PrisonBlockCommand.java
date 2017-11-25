package com.blendycat.prison.command;

import com.blendycat.prison.QuadPrison;
import com.blendycat.prison.region.prison.PrisonBlock;
import com.blendycat.prison.sql.QueryManager;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by EvanMerz on 10/23/17.
 */
public class PrisonBlockCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (player.isOp()) {
                if (args.length == 1 && args[0].equalsIgnoreCase("add")) {
                    Selection sel = QuadPrison.worldEdit.getSelection(player);
                    if (sel != null) {
                        Location min = sel.getMinimumPoint();
                        Location max = sel.getMaximumPoint();
                        int minX = min.getBlockX();
                        int minY = min.getBlockY();
                        int minZ = min.getBlockZ();
                        int maxX = max.getBlockX();
                        int maxY = max.getBlockY();
                        int maxZ = max.getBlockZ();
                        PrisonBlock prisonBlock = new PrisonBlock(
                                min.getWorld(), minX, minY, minZ, maxX, maxY, maxZ);
                        QueryManager.addPrisonBlock(prisonBlock);
                        player.sendMessage(ChatColor.AQUA + "Success! The ID of this prison block is " +
                                prisonBlock.getID());
                        QuadPrison.addPrisonBlock(prisonBlock);
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + "You must select a region first!");
                    }
                } else if (args.length == 2 && args[0].equalsIgnoreCase("setspawn")) {
                    int id = Integer.parseInt(args[1]);
                    Location loc = player.getLocation();
                    PrisonBlock block = null;
                    for (PrisonBlock b : QuadPrison.getPrisonBlocks()) {
                        if (b.getID() == id) {
                            block = b;
                            break;
                        }
                    }
                    if (block != null) {
                        block.setSpawn(loc);
                        QueryManager.updatePrisonBlock(block);
                        player.sendMessage(ChatColor.AQUA + "Spawn set!");
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + "No prison block with that ID!");
                    }
                } else if (args.length == 3 && args[0].equalsIgnoreCase("setname")) {
                    int id = Integer.parseInt(args[2]);
                    String name = args[1].trim();
                    PrisonBlock block = null;
                    for (PrisonBlock b : QuadPrison.getPrisonBlocks()) {
                        if (b.getID() == id) {
                            block = b;
                            break;
                        }
                    }
                    if (block != null) {
                        block.setName(name);
                        QueryManager.updatePrisonBlock(block);
                        player.sendMessage(ChatColor.AQUA + "Name set!");
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + "Invalid prison block ID!");
                    }
                } else if (args.length == 3 && args[0].equalsIgnoreCase("setlevel")) {
                    int id = Integer.parseInt(args[2]);
                    int lvl = Integer.parseInt(args[1]);

                    PrisonBlock block = null;
                    for (PrisonBlock b : QuadPrison.getPrisonBlocks()) {
                        if (b.getID() == id) {
                            block = b;
                        }
                    }
                    if (block != null) {
                        block.setClearanceLevel(lvl);
                        QueryManager.updatePrisonBlock(block);
                        player.sendMessage(ChatColor.AQUA + "Clearance level set!");
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + "No prison block with that ID!");
                    }
                }
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("warp")) {
                String name = args[1].trim();
                PrisonBlock block = null;
                for (PrisonBlock pb : QuadPrison.getPrisonBlocks()) {
                    if (pb.getName() != null && name.equalsIgnoreCase(pb.getName())) {
                        block = pb;
                        break;
                    }
                }
                if (block != null) {
                    if (block.getSpawn() != null) {
                        if (QuadPrison.getPrisoner(player).getClearanceLevel() >= block.getClearanceLevel()) {
                            player.teleport(block.getSpawn());
                            player.sendMessage(
                                    ChatColor.AQUA + "Welcome to prison block " + block.getName() + "!"
                            );
                        } else {
                            player.sendMessage(ChatColor.DARK_RED +
                                    "You must level up before you may go to prison block " + block.getName() + "!"
                            );
                        }
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + "Prison block " +
                                block.getName() + " has no spawn set!");
                    }
                } else {
                    player.sendMessage(ChatColor.DARK_RED + "No prison block exists with that name.");
                }

            }
        }
        return true;
    }
}
