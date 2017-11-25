package com.blendycat.prison.command;

import com.blendycat.prison.QuadPrison;
import com.blendycat.prison.player.Prisoner;
import com.blendycat.prison.region.prison.PrisonBlock;
import com.blendycat.prison.sql.QueryManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by EvanMerz on 10/25/17.
 */
public class RankUpCommand implements CommandExecutor {

    private ArrayList<Player> confirm = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;
            // Just /rankup
            if (args.length == 0) {
                // Get the prisoner object
                Prisoner prisoner = QuadPrison.getPrisoner(player);
                // Next rank level
                int rank = prisoner.getClearanceLevel() + 1;
                // If a rankup exists
                if (QueryManager.rankupExists(rank)) {
                    String name = "";
                    double price = QueryManager.getRankupPrice(rank);
                    for(PrisonBlock b : QuadPrison.getPrisonBlocks()){
                        if(b.getClearanceLevel() == rank){
                            name = b.getName();
                            break;
                        }
                    }
                    player.sendMessage(ChatColor.GRAY + "Ranking up to prison block " +
                            ChatColor.AQUA + name +
                            ChatColor.GRAY + " will cost you " + ChatColor.AQUA + "$" + price +
                            ChatColor.GRAY + "."
                    );
                    if(QuadPrison.economy.getBalance(player) >= price){
                        confirm.add(player);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(QuadPrison.getInstance(), ()->{
                                if(confirm.contains(player)){
                                    confirm.remove(player);
                                }
                        }
                                , 600
                        );

                        player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.AQUA + "/rankup confirm" +
                                ChatColor.GRAY + " within " + ChatColor.AQUA + "30s" +
                                ChatColor.GRAY + " to rank up!"
                        );
                    }else {
                        player.sendMessage(ChatColor.GRAY + "You need " +
                                ChatColor.AQUA + "$" + (price - QuadPrison.economy.getBalance(player)) +
                                ChatColor.GRAY + " to rank up!"
                        );
                    }
                } else {
                    player.sendMessage(ChatColor.DARK_RED + "No rankup exists!");
                }
            }else if(args.length == 1 && args[0].equalsIgnoreCase("confirm")){
                if(confirm.contains(player)){
                    // Horray!
                    Prisoner prisoner = QuadPrison.getPrisoner(player);
                    int rank = prisoner.getClearanceLevel() + 1;
                    double price = QueryManager.getRankupPrice(rank);
                    if(QuadPrison.economy.getBalance(player) >= price){
                        prisoner.setClearanceLevel(rank);
                        QueryManager.updatePrisoner(prisoner);
                        String name = "";
                        for(PrisonBlock pb : QuadPrison.getPrisonBlocks()){
                            if(pb.getClearanceLevel() == rank){
                                name = pb.getName();
                                break;
                            }
                        }
                        QuadPrison.economy.withdrawPlayer(player, price);
                        player.sendMessage(ChatColor.GRAY + "Ranked up! Type " +
                                ChatColor.AQUA + "/pb warp "+ name + ChatColor.GRAY +
                                " to visit your new prison block!"
                        );
                        confirm.remove(player);
                    }else{
                        player.sendMessage(ChatColor.DARK_RED + "Insufficient funds!");
                    }
                }else{
                    player.sendMessage(ChatColor.DARK_RED + "Nothing to confirm. Try /rankup again " +
                            "before doing /rankup confirm?"
                    );
                }
            // Op commands
            } else if (player.isOp()){

                // /rankup add command
                //
                // USAGE: /rankup add <price> <level>
                //
                // Subarguments:
                //   Level: Integer
                //   Price: Double
                if(args.length == 3 && args[0].equalsIgnoreCase("add")){
                    int level = Integer.parseInt(args[2]);
                    double price = Double.parseDouble(args[1]);
                    if(!QueryManager.rankupExists(level)){
                        QueryManager.addRankup(level, price);
                        player.sendMessage(ChatColor.AQUA + "Level added with price $" + price);
                    }else{
                        player.sendMessage(ChatColor.DARK_RED + "That rank up already exists!\n" +
                                "Try using /rankup setprice <newprice> <level> instead.");
                    }
                }

                // /rankup setprice command
                //
                // USAGE: /rankup setprice <price> <level>
                //
                // Subarguments:
                //   Level: Integer
                //   Price: Double
                if(args.length == 3 && args[0].equalsIgnoreCase("setprice")){
                    int level = Integer.parseInt(args[2]);
                    double price = Double.parseDouble(args[1]);
                    if(QueryManager.rankupExists(level)) {
                        QueryManager.setRankupPrice(level, price);
                        player.sendMessage(ChatColor.GREEN + "Price set!");
                    }else{
                        player.sendMessage(ChatColor.DARK_RED +
                                "You cannot set the price of a rank up that does not exist!");
                    }
                }
            }
        }else{
            commandSender.sendMessage("Command is only for players!");
        }
        return true;
    }
}
