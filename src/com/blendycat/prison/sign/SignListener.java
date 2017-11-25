package com.blendycat.prison.sign;

import com.blendycat.prison.QuadPrison;
import com.blendycat.prison.Utils;
import com.blendycat.prison.player.Prisoner;
import com.blendycat.prison.region.CellBlock;
import com.blendycat.prison.region.Region;
import com.blendycat.prison.region.prison.PrisonBlock;
import com.blendycat.prison.sql.QueryManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by EvanMerz on 10/22/17.
 */
public class SignListener implements Listener {

    private static final String prisoncell = ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA + "PrisonCell" +
            ChatColor.DARK_GRAY + "]";
    private static final String shop = ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA + "PrisonShop" +
            ChatColor.DARK_GRAY + "]";
    private static final String lvlup = ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA + "LevelUp" +
            ChatColor.DARK_GRAY + "]";

    @EventHandler
    public void onSignCreation(SignChangeEvent e){
        Player player = e.getPlayer();
        if(e.getLine(0).equalsIgnoreCase("[PrisonCell]")){
            if(!player.isOp()){
                e.setCancelled(true);
                player.sendMessage(ChatColor.DARK_RED + "You are not allowed to place [PrisonCell] signs!");
            }else{
                if(isIntParsable(e.getLine(1))){
                    e.setLine(0, prisoncell);
                    e.setLine(2, ChatColor.translateAlternateColorCodes('&', e.getLine(2)));
                    e.setLine(3, ChatColor.translateAlternateColorCodes('&', e.getLine(3)));
                    player.sendMessage(ChatColor.AQUA + "PrisonCell sign created successfully!");
                }else{
                    player.sendMessage(ChatColor.RED + "Line 2 must be a valid cell ID");
                }
            }
        // [PrisonShop]  // Sign TAG
        //     B:100     // Price and sell or buy
        //  COBBLESTONE  // Material name
        //      64       // Amount
        }else if(e.getLine(0).equalsIgnoreCase("[PrisonShop]")){
            if(!player.isOp()){
                e.setCancelled(true);
                player.sendMessage(ChatColor.DARK_RED + "You are not allowed to place [PrisonShop] signs!");
            }else{
                String[] line4 = e.getLine(3).split(":");
                if(!line4[0].trim().equalsIgnoreCase("B") &&
                        !line4[0].trim().equalsIgnoreCase("S")){
                    player.sendMessage(ChatColor.DARK_RED + "Invalid shop type! Must be 'B' for buy shop or 'S' " +
                            "for sell shop");
                }
                if(line4.length != 2 || !isDoubleParsable(line4[1].trim())){
                    e.setCancelled(true);
                    player.sendMessage(ChatColor.DARK_RED + "Line 4 must be a valid price! (e.g. B:22.5 or S:12)");
                    return;
                }
                // Materials can be formatted as such: WOOL:15
                String[] materialLine = e.getLine(2).split(":");
                // Get the material from the name
                Material mat = Material.matchMaterial(materialLine[0].trim().toUpperCase());
                if(mat == null){
                    e.setCancelled(true);
                    player.sendMessage(ChatColor.DARK_RED + "Line 3 must be a valid material!");
                    return;
                }

                if(!isIntParsable(e.getLine(1).trim())){
                    e.setCancelled(true);
                    player.sendMessage(ChatColor.DARK_RED + "Line 1 must be a valid whole number!");
                    return;
                }

                // REFORMATTING
                e.setLine(0, shop);
                e.setLine(3, line4[0].trim().toUpperCase() + ":" + line4[1].trim());
                if(materialLine.length == 2 && isIntParsable(materialLine[1].trim())){
                    e.setLine(2, materialLine[0].toUpperCase().trim() + ":" + materialLine[1].trim());
                }else{
                    e.setLine(2, materialLine[0].toUpperCase().trim());
                }
                e.setLine(1, e.getLine(1).trim());
                player.sendMessage(ChatColor.GREEN + "PrisonShop sign created successfully!");
            }
        }else if(e.getLine(0).equalsIgnoreCase("[LevelUp]")){
            if(!player.isOp()){
                e.setCancelled(true);
                player.sendMessage(ChatColor.DARK_RED + "No permission to create [LevelUp] signs!");
                return;
            }
            if(isDoubleParsable(e.getLine(2).trim())){
                e.setLine(0, lvlup);
                e.setLine(2, "$" + e.getLine(2).trim());
                e.setLine(3, ChatColor.translateAlternateColorCodes('&', e.getLine(3)));
                player.sendMessage(ChatColor.GREEN + "Level up sign created!");
            }else{
                player.sendMessage(ChatColor.DARK_RED + "Line 2 must be a valid price! (No Currency Symbols)");
            }
        }
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent e){
        Player player = e.getPlayer();
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
            if(e.getClickedBlock().getState() instanceof Sign){
                Sign sign = (Sign) e.getClickedBlock().getState();
                if(sign.getLine(0).equals(prisoncell)){
                    Region region = null;
                    for(Region r : QuadPrison.getRegions()){
                        if(r.getID() == Integer.parseInt(sign.getLine(1))){
                            region = r;
                            break;
                        }
                    }
                    if(region == null){
                        player.sendMessage(ChatColor.DARK_RED + "No region exists with that ID");
                        return;
                    }
                    if(region instanceof CellBlock){
                        CellBlock cellBlock = (CellBlock) region;
                        ChatColor[] theme = {ChatColor.GOLD, ChatColor.RED};
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
                        e.getPlayer().sendMessage(ChatColor.DARK_RED + "That region is not a prison cell!");
                    }
                }else if(sign.getLine(0).equals(shop)){
                    String[] bs = sign.getLine(3).split(":");
                    boolean sell = bs[0].equals("S");
                    double price = Double.parseDouble(bs[1]);
                    String[] matLn = sign.getLine(2).split(":");
                    Material mat = Material.matchMaterial(matLn[0]);
                    int dura = 0;
                    if(matLn.length == 2)
                        dura = Integer.parseInt(matLn[1]);
                    int amount = Integer.parseInt(sign.getLine(1));
                    ItemStack item = new ItemStack(mat, amount);
                    item.setDurability((short) dura);
                    ItemStack ti = item.clone();
                    ti.setAmount(1);
                    if(sell){
                        // Selling Item
                        if(player.getInventory().containsAtLeast(ti, amount)){
                            player.getInventory().removeItem(item);
                            QuadPrison.economy.depositPlayer(player, price);
                            player.sendMessage(ChatColor.GREEN + "Sold " + amount + " " +
                                    mat.toString().toLowerCase().replace('_', ' ') + " for $" + price + "!");
                        }else{
                            player.sendMessage(ChatColor.DARK_RED + "You do not have " +
                                    item.getAmount() + " " +
                                    mat.toString().toLowerCase().replace('_', ' ') + "!");
                        }
                    }else{
                        // Buying Item
                        if(QuadPrison.economy.getBalance(player) >= price){
                            if(Utils.hasEnoughSpace(item, player.getInventory())){
                                QuadPrison.economy.withdrawPlayer(player, price);
                                player.getInventory().addItem(item);
                                player.sendMessage(ChatColor.AQUA + "Bought " + amount + " " +
                                        mat.toString().toLowerCase().replace('_', ' ') + " for $" + price + "!");
                            }else{
                                player.sendMessage(ChatColor.DARK_RED + "Not enough room in inventory!");
                            }
                        }else{
                            player.sendMessage(ChatColor.DARK_RED + "Insufficient funds to purchase " +
                                    mat.toString().toLowerCase().replace('_', ' ') + "!");
                        }
                    }
                }else if(sign.getLine(0).equals(lvlup)){
                    String name = sign.getLine(1).trim();
                    double price = Double.parseDouble(sign.getLine(2).replace("$", ""));
                    PrisonBlock block = null;
                    for(PrisonBlock bl : QuadPrison.getPrisonBlocks()){
                        if(bl.getName()!=null && bl.getName().equalsIgnoreCase(name)){
                            block = bl;
                        }
                    }

                    if(block == null) return;

                    if(QuadPrison.economy.getBalance(player) >= price){
                        Prisoner prisoner = QuadPrison.getPrisoner(player);
                        if(prisoner.getClearanceLevel() >= block.getClearanceLevel()){
                            player.sendMessage(ChatColor.DARK_RED + "You've already leveled up!");
                            return;
                        }
                        QuadPrison.economy.withdrawPlayer(player, price);
                        prisoner.setClearanceLevel(block.getClearanceLevel());
                        QueryManager.updatePrisoner(prisoner);
                        player.teleport(block.getSpawn());
                        player.sendMessage(ChatColor.GREEN + "Leveled up to prison block " + block.getName() + "!");
                    }else{
                        player.sendMessage(ChatColor.DARK_RED + "Insufficient funds!");
                    }
                }
            }
        }
    }

    private boolean isIntParsable(String integer){
        try{
            int i = Integer.parseInt(integer);
            return true;
        }catch(NumberFormatException ex){
            return false;
        }
    }

    private boolean isDoubleParsable(String val){
        try{
            double i = Double.parseDouble(val);
            return true;
        }catch(NumberFormatException ex){
            return false;
        }
    }
}
