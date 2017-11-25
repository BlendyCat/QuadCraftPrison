package com.blendycat.prison.portal;

import com.blendycat.prison.QuadPrison;
import com.blendycat.prison.sql.QueryManager;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Created by EvanMerz on 11/4/17.
 *
 * NEW: Integrated Query Management
 * No need to call portal.setCommand() and
 * QueryManager.setPortalCommand() when you can
 * just do portal.setCommand()!
 *
 * Object form of portals; simply awesome.
 */
public class Portal {

    private int x1;
    private int y1;
    private int z1;

    private int x2;
    private int y2;
    private int z2;

    private String name;
    private String command;
    private World world;

    public Portal(String name, World world, int x1, int y1, int z1, int x2, int y2, int z2){
        this.name = name;

        this.world = world;

        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;

        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    /**
     *
     * @param command the command to be set
     */
    public void setCommand(String command){
        this.command = command;
        QueryManager.setPortalCommand(name, command);
    }

    /**
     *
     * @return the command as a string
     */
    public String getCommand(){
        return command;
    }

    /**
     *
     * @return the ID STRING of the portal
     */
    public String getName(){
        return name;
    }

    /**
     *
     * @param loc the location to test
     * @return if the location is inside the portal
     */
    public boolean isInside(Location loc){
        return !(!loc.getWorld().equals(this.world) ||
                loc.getBlockX() < x1 || loc.getBlockX() > x2 ||
                loc.getBlockY() < y1 || loc.getBlockY() > y2 ||
                loc.getBlockZ() < z1 || loc.getBlockZ() > z2);
    }

    public void remove() {
        QueryManager.removePortal(name);
        QuadPrison.removePortal(name);
    }
}
