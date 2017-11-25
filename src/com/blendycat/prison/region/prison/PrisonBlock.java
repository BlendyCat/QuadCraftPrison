package com.blendycat.prison.region.prison;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Created by EvanMerz on 10/23/17.
 */
public class PrisonBlock {

    private int clearanceLevel;
    private String name;
    private int id;
    private World world;
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;
    private Location spawn;

    public PrisonBlock(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
        this.world = world;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        clearanceLevel = 0;
    }

    public String getName(){
        return name;
    }

    public void setSpawn(Location loc){
        this.spawn = loc;
    }

    public Location getSpawn(){
        return spawn;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setClearanceLevel(int clearanceLevel){
        this.clearanceLevel = clearanceLevel;
    }

    public int getClearanceLevel(){
        return clearanceLevel;
    }

    public boolean isInsideRegion(Location loc){
        return !(!loc.getWorld().equals(this.world) ||
                loc.getBlockX() < minX || loc.getBlockX() > maxX ||
                loc.getBlockY() < minY || loc.getBlockY() > maxY ||
                loc.getBlockZ() < minZ || loc.getBlockZ() > maxZ);
    }

    /**
     *
     * @return the min X coordinate of the region
     */
    public int getMinX() {
        return minX;
    }

    /**
     *
     * @return the min Y coordinate of the region
     */
    public int getMinY() {
        return minY;
    }

    /**
     *
     * @return the min Z coordinate of the region
     */
    public int getMinZ() {
        return minZ;
    }

    /**
     *
     * @return the max Y coordinate of the region
     */
    public int getMaxX() {
        return maxX;
    }

    /**
     *
     * @return the max Y coordinate of the region
     */
    public int getMaxY() {
        return maxY;
    }

    /**
     *
     * @return max Z coordinate of the region
     */
    public int getMaxZ() {
        return maxZ;
    }

    /**
     *
     * @return the world of the prison
     */
    public World getWorld(){
        return world;
    }

    public int getID(){
        return id;
    }

    public void setID(int id){
        this.id = id;
    }


}
