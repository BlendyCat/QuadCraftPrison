package com.blendycat.prison.command;

import com.blendycat.prison.QuadPrison;
import com.blendycat.prison.portal.Portal;
import com.blendycat.prison.sql.QueryManager;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by EvanMerz on 11/4/17.
 */
public class PortalCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            if(player.isOp()){
                /*
                Command is /portal add <name>
                 */
                if(args.length == 2 && args[0].equalsIgnoreCase("add")){
                    String name = args[1];
                    // Check if the player has a valid world edit selection
                    Selection sel = QuadPrison.worldEdit.getSelection(player);
                    if(sel != null) {
                        Location min = sel.getMinimumPoint();
                        Location max = sel.getMaximumPoint();

                        // Construct the portal object
                        Portal portal = new Portal(name, min.getWorld(),
                                min.getBlockX(), min.getBlockY(), min.getBlockZ(),
                                max.getBlockX(), max.getBlockY(), max.getBlockZ()
                        );

                        QueryManager.addPortal(name, sel);
                        QuadPrison.addPortal(portal);
                        player.sendMessage(ChatColor.AQUA + "Success! Use /portal setcommand <name> <command> " +
                                "to set the command that will be executed upon entry of this portal."
                        );

                    }else{
                        player.sendMessage(ChatColor.DARK_RED + "Please make a valid world edit selection first!");
                    }
                /*
                Command is /portal setcommand <name> <command> [command args]
                 */
                }else if(args.length > 2 && args[0].equalsIgnoreCase("setcommand")){
                    String name = args[1];
                    // Assemble the command string from the arguments
                    String cmd = "";
                    for(int i = 2; i < args.length; i++){
                        cmd += args[i] + " ";
                    }
                    cmd = cmd.trim();
                    Portal portal = null;
                    for(Portal p : QuadPrison.getPortals()){
                        if(p.getName().equalsIgnoreCase(name)){
                            portal = p;
                        }
                    }
                    if(portal != null){
                        portal.setCommand(cmd);
                        player.sendMessage(ChatColor.AQUA + "Command set for portal " + portal.getName() + "!");
                    }else{
                        player.sendMessage(ChatColor.DARK_RED + "No portal with that name!");
                    }
                }else if(args.length == 2 && args[0].equalsIgnoreCase("remove")){
                    String name = args[1];
                    // Whether the portal has been removed yet
                    boolean removed = false;
                    // Remove the portal with that name if it exists
                    for(Portal p : QuadPrison.getPortals()){
                        if(p.getName().equalsIgnoreCase(name)){
                            p.remove();
                            removed = true;
                            break;
                        }
                    }
                    if(removed){
                        player.sendMessage(ChatColor.AQUA + "Removed! (Psst. Use Ctrl + Shift + N next time)");
                    }else{
                        player.sendMessage(ChatColor.DARK_RED + "You can't remove something that doesn't exist!");
                    }
                }else{
                    player.sendMessage(ChatColor.DARK_RED + "Invalid syntax!");
                }
            }else{
                player.sendMessage(ChatColor.DARK_RED + "Sorry prisoner, No permission!");
            }
        }else{
            sender.sendMessage(ChatColor.DARK_RED + "ThIs CoMmAnD iS oNlY fOr PlAyErS!");
        }
        return true;
    }
}
