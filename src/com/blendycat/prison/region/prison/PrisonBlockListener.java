package com.blendycat.prison.region.prison;

import com.blendycat.prison.QuadPrison;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Created by EvanMerz on 10/23/17.
 */
public class PrisonBlockListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent e) {
        if (e.getPlayer().isOp()) return;
        for (PrisonBlock p : QuadPrison.getPrisonBlocks()) {
            if (p.isInsideRegion(e.getBlock().getLocation())) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBuild(BlockPlaceEvent e) {
        if (e.getPlayer().isOp()) return;
        for (PrisonBlock p : QuadPrison.getPrisonBlocks()) {
            if (p.isInsideRegion(e.getBlock().getLocation())) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void teleportByEnderPearl(PlayerTeleportEvent e) {
        if (e.getPlayer().isOp()) return;
        for (PrisonBlock p : QuadPrison.getPrisonBlocks()) {
            if (e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL
                    && p.isInsideRegion(e.getFrom())) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.DARK_RED + "Escape prevented. Nice try!");
                return;
            }
        }
    }
}
