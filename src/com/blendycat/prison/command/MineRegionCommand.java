package com.blendycat.prison.command;

import com.blendycat.prison.QuadPrison;
import com.blendycat.prison.region.MineRegion;
import com.blendycat.prison.region.Region;
import com.blendycat.prison.sql.QueryManager;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by EvanMerz on 10/20/17.
 */
public class MineRegionCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;
            // Add a mine region to the regions /mineregion add
            // User must have a region selected with world edit first
            if(player.hasPermission("quadprison.mineregion")) {
                if (args.length >= 1 && args[0].equalsIgnoreCase("add")) {
                    if (QuadPrison.worldEdit.getSelection(player) != null) {
                        Selection sel = QuadPrison.worldEdit.getSelection(player);
                        Location min = sel.getMinimumPoint();
                        Location max = sel.getMaximumPoint();
                        int minX = min.getBlockX();
                        int minY = min.getBlockY();
                        int minZ = min.getBlockZ();
                        int maxX = max.getBlockX();
                        int maxY = max.getBlockY();
                        int maxZ = max.getBlockZ();

                        MineRegion region = new MineRegion(min.getWorld(), minX, minY, minZ, maxX, maxY, maxZ);
                        QueryManager.addRegion(region);
                        QuadPrison.addRegion(region);
                        player.sendMessage(ChatColor.AQUA + "Success! The ID of the mine region is " + region.getID() +
                                "\nTo add regeneration blocks do /mineregion addblock <material> <percent> <region ID> ");
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + "You must select a region first!");
                    }


                    // Lists all of the mine regions
                } else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                    for (Region region : QuadPrison.getRegions()) {
                        if (region.getType() == Region.MINE_REGION) {
                            player.sendMessage(ChatColor.AQUA + "ID: " + region.getID() + " X1: " + region.getMinX() +
                                    " Y1: " + region.getMinY() + " Z1: " + region.getMinZ() +
                                    " X2: " + region.getMaxX() + " Y2: " + region.getMaxY() + " Z2: " + region.getMaxZ()
                            );
                        }
                    }


                    // /mineregion addblock <material> <percent> <id>
                } else if (args.length == 4 && args[0].equalsIgnoreCase("addblock")) {
                    // the id of the mine region
                    int id = Integer.parseInt(args[3]);
                    // the percent of the new block
                    int percent = Integer.parseInt(args[2].replace("%", ""));
                    Region region = null;
                    // Loop through all the regions
                    for (Region r : QuadPrison.getRegions()) {
                        // ID check
                        if (r.getID() == id) {
                            region = r;
                            break;
                        }
                    }
                    // If no region matches
                    if (region == null) {
                        player.sendMessage(ChatColor.DARK_RED + "No region was found with that ID");
                        return true;
                    }
                    // Make sure it's a MineRegion
                    if (region instanceof MineRegion) {
                        MineRegion mr = (MineRegion) region;
                        Material mat = Material.getMaterial(args[1].toUpperCase());
                        // Check whether the material is valid
                        if (mat == null) {
                            player.sendMessage(ChatColor.DARK_RED + "That is not a valid material!");
                            return true;
                        }
                        // Finally add it
                        mr.addRegenBlock(mat, percent);
                        QueryManager.updateRegion(region);
                        // If the regions is not a mining region!
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + "That region is not a mining region!");
                    }


                    // /mineregion listblocks <id>
                } else if (args.length == 2 && args[0].equalsIgnoreCase("listblocks")) {
                    int id = Integer.parseInt(args[1]);
                    Region region = null;
                    for (Region r : QuadPrison.getRegions()) {
                        if (r.getID() == id) {
                            region = r;
                        }
                    }
                    if (region == null) {
                        player.sendMessage(ChatColor.DARK_RED + "No region exists with that ID!");
                        return true;
                    }
                    if (region instanceof MineRegion) {
                        MineRegion mr = (MineRegion) region;
                        for (Material mat : mr.sortBlockValues()) {
                            player.sendMessage(ChatColor.AQUA + mat.toString() + ": " +
                                    mr.getRegenBlocks().get(mat) + "%"
                            );
                        }
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + "That region is not a mining region!");
                    }

                } else if (args.length == 2 && args[0].equalsIgnoreCase("regenerate")) {
                    int id = Integer.parseInt(args[1]);
                    Region region = null;
                    for (Region r : QuadPrison.getRegions()) {
                        if (r.getID() == id) {
                            region = r;
                        }
                    }
                    if (region == null) {
                        player.sendMessage(ChatColor.DARK_RED + "No region exists with that ID!");
                        return true;
                    }
                    if (region instanceof MineRegion) {
                        MineRegion mr = (MineRegion) region;
                        Bukkit.getScheduler().runTaskAsynchronously(QuadPrison.getInstance(), mr::reset);
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + "That region is not a mining region!");
                    }
                } else {
                    player.sendMessage(ChatColor.DARK_AQUA + "Help for /mineregion:");
                    player.sendMessage(ChatColor.AQUA +
                            "/mineregion list\n" +
                            "/mineregion add\n" +
                            "/mineregion listblocks <id>\n" +
                            "/mineregion regenerate <id>\n" +
                            "/mineregion addblock <material> <percent> <id>");
                }
            }else{
                player.sendMessage(ChatColor.DARK_RED + "You do not have permission!");
            }
            return true;
        }else{
            commandSender.sendMessage("You must be a player to send a command");
            return false;
        }
    }
}