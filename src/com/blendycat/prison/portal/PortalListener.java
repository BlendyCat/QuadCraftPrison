package com.blendycat.prison.portal;

import com.blendycat.prison.QuadPrison;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Created by EvanMerz on 11/5/17.
 */
public class PortalListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        for(Portal portal : QuadPrison.getPortals()){
            if(portal.isInside(e.getTo())){
                if(portal.getCommand() != null) {
                    e.getPlayer().performCommand(portal.getCommand());
                    return;
                }
                e.setCancelled(true);
                break;
            }
        }
    }
}
