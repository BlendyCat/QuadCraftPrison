package com.blendycat.prison.player;

import com.blendycat.prison.QuadPrison;
import com.blendycat.prison.sql.QueryManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by EvanMerz on 10/23/17.
 */
public class PrisonerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(e.getPlayer().hasPlayedBefore()){
            Prisoner prisoner = QueryManager.getPrisoner(e.getPlayer());
            QuadPrison.addPrisoner(prisoner);
            e.setJoinMessage(ChatColor.GRAY + "Welcome back, " +
                    ChatColor.AQUA + e.getPlayer().getName() + ChatColor.GRAY + "!");
        }else{
            Prisoner prisoner = new Prisoner(e.getPlayer(), 0);
            QueryManager.addPrisoner(prisoner);
            QuadPrison.addPrisoner(prisoner);
            e.setJoinMessage(ChatColor.GRAY + "Welcome to QuadCraft, " +
                    ChatColor.AQUA + e.getPlayer().getName() + ChatColor.GRAY + "!");

        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        QuadPrison.removePrisoner(e.getPlayer());
    }

}
