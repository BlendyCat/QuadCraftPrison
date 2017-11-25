package com.blendycat.prison.command;

import com.blendycat.prison.QuadPrison;
import com.blendycat.prison.player.Prisoner;
import com.blendycat.prison.region.CellBlock;
import com.blendycat.prison.region.Region;
import com.blendycat.prison.region.prison.PrisonBlock;
import com.blendycat.prison.sql.QueryManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * Created by EvanMerz on 10/22/17.
 */
public class CellCommand implements CommandExecutor {

    private HashMap<Player, Integer> confirm = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if(commandSender instanceof Player){
            Player player = (Player) commandSender;
            if(args.length == 0){
                Region region = null;
                for(Region r : QuadPrison.getRegions()){
                    if(r.isInsideRegion(player.getLocation())){
                        region = r;
                        break;
                    }
                }
                if(region != null && region instanceof CellBlock){
                    CellBlock cellBlock = (CellBlock) region;
                    if(cellBlock.getOwner() == null) {
                        player.sendMessage(ChatColor.AQUA +
                                "This prison cell is vacant. If you wish to purchase this prison cell " +
                                "for " + ChatColor.GREEN + "$" + cellBlock.getPrice() + ChatColor.AQUA +
                                " type in " + ChatColor.BLUE + "/cell purchase"
                        );
                    }else if(cellBlock.getOwner().getUniqueId().equals(player.getUniqueId())){
                        player.sendMessage(ChatColor.AQUA + "You own this cell!");
                    }else{
                        player.sendMessage(ChatColor.RED + "This cell block is currently owned by " +
                                cellBlock.getOwner().getName() + ". Not for sale currently.");
                    }
                }else{
                    player.sendMessage(ChatColor.DARK_RED + "You must be in a prison cell to use this command!");
                }
            }else if(args.length == 1 && args[0].equalsIgnoreCase("purchase")){
                if(QuadPrison.getPrisoner(player).ownsCell()){
                    player.sendMessage(ChatColor.DARK_RED + "You are not permitted to own more than one cell!");
                    return true;
                }
                Region region = null;
                for(Region r : QuadPrison.getRegions()){
                    if(r.isInsideRegion(player.getLocation())){
                        region = r;
                        break;
                    }
                }
                if(region != null && region instanceof CellBlock){
                    CellBlock cellBlock = (CellBlock) region;
                    if(cellBlock.getOwner() == null) {
                        if(QuadPrison.economy == null) return true;
                        if(QuadPrison.economy.getBalance(player) >= cellBlock.getPrice()){
                            // Set the owner of the cell
                            cellBlock.setOwner(player);
                            // Update the region in the database
                            QueryManager.updateRegion(cellBlock);
                            // Withdraw the money from the player's balance
                            QuadPrison.economy.withdrawPlayer(player, cellBlock.getPrice());

                            // get the prisoner object for that player
                            Prisoner prisoner = QuadPrison.getPrisoner(player);

                            // set the prisoner's cell and update it in the database
                            prisoner.setCellID(cellBlock.getID());
                            QueryManager.updatePrisoner(prisoner);
                            player.sendMessage(ChatColor.GREEN + "Successfully purchased cell!");

                            // Now we need to get all the other players out of the cell
                            for(Player p : Bukkit.getOnlinePlayers()){
                                if(p.equals(player)) continue;
                                if(cellBlock.isInsideRegion(p.getLocation())){
                                    PrisonBlock pb = QuadPrison.getPrisoner(p).getPrisonBlock();
                                    if(pb != null && pb.getSpawn() != null){
                                        p.teleport(pb.getSpawn());
                                        p.sendMessage(ChatColor.AQUA + "Teleported out of " +
                                                player.getName() +
                                                "'s cell!");
                                    }else{
                                        p.teleport(player.getWorld().getSpawnLocation());
                                    }
                                }
                            }
                        }else{
                            player.sendMessage(ChatColor.RED + "Insufficient funds! Could not purchase!");
                        }
                    }else if(cellBlock.getOwner().getUniqueId().equals(player.getUniqueId())){
                        player.sendMessage(ChatColor.AQUA + "You already own this cell!");
                    }else{
                        player.sendMessage(ChatColor.RED + "This cell block is already owned by " +
                                cellBlock.getOwner().getName() + ". You cannot purchase it!");
                    }
                }else{
                    player.sendMessage(ChatColor.DARK_RED + "You must be in a prison cell to use this command!");
                }
            }else if(args.length == 1 && args[0].equalsIgnoreCase("here")){
                Region region = null;
                for(Region r : QuadPrison.getRegions()){
                    if(r.isInsideRegion(player.getLocation())){
                        region = r;
                        break;
                    }
                }
                if(region != null && region instanceof CellBlock) {
                    CellBlock cellBlock = (CellBlock) region;
                    ChatColor[] theme = {ChatColor.GOLD, ChatColor.RED};
                    player.sendMessage(theme[0] + "ID: " + theme[1] + cellBlock.getID());
                    player.sendMessage(theme[0] + "Cell Owner: " + theme[1] +
                            (cellBlock.getOwner() == null ? "none" : cellBlock.getOwner().getName()));
                    player.sendMessage(theme[0] + "Price: " + theme[1] + cellBlock.getPrice() +
                            (cellBlock.getOwner() == null ? " (For Sale)" : " (Not for sale)"));
                    String time = "";
                    int days = cellBlock.getDecayTime()/1440;
                    time += days == 0 ? "" : days + "d";
                    int remainder = cellBlock.getDecayTime() % 1440;
                    int hours = remainder / 60;
                    time += hours == 0 ? "" : hours + "h";
                    remainder %= 60;
                    time += remainder == 0 ? "" : remainder + "m";

                    player.sendMessage(theme[0] + "Ownership Renewal: " + theme[1] + time);
                    player.sendMessage(ChatColor.YELLOW + "(If the owner does not have sufficient funding to " +
                            "renew their cell, the cell will be reset and open for sale)");
                }else{
                    player.sendMessage(ChatColor.DARK_RED + "You must be in a prison cell to use this command");
                }

            // /cell delete
            }else if(args.length == 1 && args[0].equalsIgnoreCase("delete")){
                // Cell object
                CellBlock cell = null;
                // Loops through regions and finds what cell they're in
                for(Region r : QuadPrison.getRegions()){
                    if(r instanceof CellBlock &&
                            r.isInsideRegion(player.getLocation())){
                        cell = (CellBlock) r;
                        break;
                    }
                }
                // Null Check
                if(cell != null){
                    if(cell.getOwner() != null){
                        if(cell.getOwner().equals(player)){
                            // Confirmation for deletion
                            confirm.put(player, cell.getID());
                            player.sendMessage(ChatColor.GRAY +
                                    "Type " + ChatColor.AQUA + "/cell confirm" + ChatColor.GRAY +
                                    " within " + ChatColor.AQUA + "30" + ChatColor.GRAY +
                                    " seconds to confirm the deletion of your cell" +
                                    "(You will no longer be owner and the contents will be reset)");
                            Bukkit.getScheduler().scheduleSyncDelayedTask(
                                    QuadPrison.getInstance(),
                                    ()-> {
                                        if(confirm.containsKey(player)){
                                            confirm.remove(player);
                                            player.sendMessage(ChatColor.RED + "Deletion cancelled!");
                                        }
                                    },
                                    600
                            );
                        }else{
                            player.sendMessage(ChatColor.DARK_RED + "You don't own this cell!");
                        }
                    }else{
                        player.sendMessage(ChatColor.DARK_RED + "You don't own this cell!");
                    }
                }else{
                    player.sendMessage(ChatColor.DARK_RED +
                            "You must be standing in a cell to use this command!"
                    );
                }
            // /cell confirm
            }else if(args.length == 1 && args[0].equalsIgnoreCase("confirm")){
                if(confirm.containsKey(player)){
                    CellBlock cell = null;
                    for(Region r : QuadPrison.getRegions()){
                        if(r instanceof CellBlock && r.getID() == confirm.get(player)){
                            cell = (CellBlock) r;
                        }
                    }

                    if(cell != null){
                        // Set the owner of the cell to nobody
                        cell.setOwner(null);
                        // Reset the cell
                        cell.reset();
                        // Set the decay time back to the max (14d)
                        cell.setDecayTime(cell.getMaxDecayTime());
                        // Update the cell's decay time in the database
                        QueryManager.updateDecay(cell);
                        QueryManager.updateRegion(cell);
                        // Get the prisoner
                        Prisoner prisoner = QuadPrison.getPrisoner(player);
                        // Set the cell the prisoner owns to nothing
                        prisoner.setDoesNotCell();
                        // Update the prisoner in the database
                        QueryManager.updatePrisoner(prisoner);
                        // Send a message
                        player.sendMessage(ChatColor.GREEN + "You no longer own this cell!");
                        confirm.remove(player);

                    }else{
                        player.sendMessage(ChatColor.DARK_RED + "Error in translation. Please report this error.");
                    }
                }else{
                    player.sendMessage(ChatColor.DARK_RED + "Nothing to confirm. Try running " +
                            "/cell delete to remove your ownership of the cell?");
                }
            }
        }else{
            commandSender.sendMessage("This command is for players only!");
        }
        return true;
    }
}
