package com.blendycat.prison.region;

import com.blendycat.prison.QuadPrison;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Created by EvanMerz on 10/19/17.
 */
public class RegionListener implements Listener {

    @EventHandler
    public void onBuild(BlockPlaceEvent e){
        if(!e.getPlayer().isOp()) {
            for (Region region : QuadPrison.getRegions()) {
                Location loc = e.getBlock().getLocation();
                if (region.isInsideRegion(loc)) {
                    if (!region.canBuild() ||
                            (region.isOwnable() && (region.getOwner() == null) ||
                                    (region.getOwner() != null &&
                                            !region.getOwner().equals(e.getPlayer())))) {
                        e.setCancelled(true);
                        return;
                    }else{
                        e.setCancelled(false);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e){
        if(!e.getPlayer().isOp()) {
            for (Region region : QuadPrison.getRegions()) {
                Location loc = e.getBlock().getLocation();
                if (region.isInsideRegion(loc)) {
                    if (!region.canBreak() ||
                            (region.isOwnable() && (region.getOwner() == null) ||
                                    (region.getOwner() != null &&
                                            !region.getOwner().equals(e.getPlayer())))) {
                        e.setCancelled(true);
                        return;
                    }else{
                        e.setCancelled(false);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onOpenInventory(InventoryOpenEvent e) {
        if (!e.getPlayer().isOp()) {
            for (Region region : QuadPrison.getRegions()) {
                Block targetBlock = e.getPlayer().getTargetBlock(null, 100);
                Location loc = targetBlock.getLocation();
                if (region.isInsideRegion(loc)) {
                    if (!region.canOpenStorage() ||
                            (region.isOwnable() && (region.getOwner() == null) ||
                                    (region.getOwner() != null &&
                                            !region.getOwner().equals(e.getPlayer())))) {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(ChatColor.DARK_RED +
                                "You are not allowed to open that in this region!");
                        return;
                    } else{
                        e.setCancelled(false);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        if(!e.getPlayer().isOp()){
            for (Region region : QuadPrison.getRegions()) {
                Location loc = e.getTo();
                if(region.isInsideRegion(loc)) {
                    if (region instanceof CellBlock) {
                        if (region.getOwner() != null && !region.getOwner().equals(e.getPlayer())) {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage(ChatColor.DARK_RED + "You may not enter " +
                                    region.getOwner().getName() + "'s prison cell!");
                        }
                    }
                }
            }
        }
    }
}
