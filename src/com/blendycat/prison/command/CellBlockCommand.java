package com.blendycat.prison.command;

import com.blendycat.prison.QuadPrison;
import com.blendycat.prison.region.CellBlock;
import com.blendycat.prison.region.Region;
import com.blendycat.prison.sql.QueryManager;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by EvanMerz on 10/21/17.
 */
public class CellBlockCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if(commandSender instanceof Player){
            Player player = (Player) commandSender;
            if(player.hasPermission("quadprison.cellblock")){
                if(args.length == 1 && args[0].equalsIgnoreCase("add")){
                    if(QuadPrison.worldEdit.getSelection(player) != null){
                        Selection selection = QuadPrison.worldEdit.getSelection(player);
                        Location min = selection.getMinimumPoint();
                        Location max = selection.getMaximumPoint();
                        CellBlock cell = new CellBlock(
                                min.getWorld(), min.getBlockX(), min.getBlockY(), min.getBlockZ(),
                                max.getBlockX(), max.getBlockY(), max.getBlockZ()
                        );
                        QueryManager.addRegion(cell);
                        QuadPrison.addRegion(cell);
                        player.sendMessage(ChatColor.AQUA + "Success! The ID of the cell block is " +
                                cell.getID() +
                                "\nTo set the price of this cell use /cellblock setprice <new price> <cell ID>"
                        );
                    }else{
                        player.sendMessage(ChatColor.DARK_RED + "You must select a region first!");
                    }
                }else if(args.length == 3 && args[0].equalsIgnoreCase("setprice")){
                    int id = Integer.parseInt(args[2]);
                    double newPrice = Double.parseDouble(args[1]);

                    Region region = null;
                    for(Region r : QuadPrison.getRegions()){
                        if(r.getID() == id){
                            region = r;
                            break;
                        }
                    }

                    if(region == null){
                        player.sendMessage(ChatColor.DARK_RED + "There is no region with that ID!");
                        return true;
                    }

                    if(region instanceof CellBlock){
                        CellBlock cell = (CellBlock) region;
                        cell.setPrice(newPrice);
                        QueryManager.updateRegion(cell);
                        player.sendMessage(ChatColor.GREEN + "Price set!");
                    }else{
                        player.sendMessage(ChatColor.DARK_RED + "That region is not a cell block!");
                    }
                }else if(args.length == 1 && args[0].equalsIgnoreCase("list")){
                    for (Region region : QuadPrison.getRegions()) {
                        if (region instanceof CellBlock) {
                            player.sendMessage(ChatColor.AQUA + "ID: " + region.getID() + " X1: " + region.getMinX() +
                                    " Y1: " + region.getMinY() + " Z1: " + region.getMinZ() +
                                    " X2: " + region.getMaxX() + " Y2: " + region.getMaxY() + " Z2: " + region.getMaxZ() +
                                    " Price: " + ((CellBlock)region).getPrice()
                            );
                        }
                    }
                }
            }else{
                player.sendMessage(ChatColor.DARK_RED + "You do not have permission!");
            }
        }else{
            commandSender.sendMessage("You must be a player to use that command!");
        }
        return true;
    }
}
