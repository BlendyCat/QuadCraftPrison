package com.blendycat.prison;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Created by EvanMerz on 10/24/17.
 */
public class MonitorListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent e){
        if(e.isCancelled()){
            e.getPlayer().sendMessage(ChatColor.DARK_RED + "You may not destroy here!");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent e){
        if(e.isCancelled()){
            e.getPlayer().sendMessage(ChatColor.DARK_RED + "You may not build here!");
        }
    }
}