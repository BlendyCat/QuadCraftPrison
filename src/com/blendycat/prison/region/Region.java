package com.blendycat.prison.region;

import com.blendycat.prison.QuadPrison;
import com.blendycat.prison.sql.QueryManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.io.Serializable;

/**
 * Created by EvanMerz on 10/19/17.
 */
public abstract class Region implements Serializable {

    public static final int MINE_REGION = 0;
    public static final int CELL_BLOCK = 1;
    public static final int FURNACE_REGION = 2;
    public static final int SHOP_REGION = 3;
    public static final int PVP_ARENA = 4;
    private int decayTime = 0;

    private World world;

    private int minX;
    private int minY;
    private int minZ;

    private int maxX;
    private int maxY;
    private int maxZ;

    private int type;
    private int id;

    private OfflinePlayer owner;


    Region(World world, int minX, int minY, int minZ,
           int maxX, int maxY, int maxZ, int type) {
        this.world = world;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;

        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;

        this.type = type;
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
     * @return the type of the region
     */
    public int getType() {
        return type;
    }

    public World getWorld(){
        return world;
    }

    public void setID(int id){
        this.id = id;
    }

    public int getID(){
        return id;
    }

    /**
     * Reset the region to the initial state
     */
    public abstract void reset();

    public abstract boolean isOwnable();
    public abstract boolean canBuild();
    public abstract boolean canBreak();
    public abstract boolean canPVP();
    public abstract boolean canUseItem();
    public abstract boolean canOpenStorage();

    /**
     *
     * @return the data associated with this region
     */
    public abstract Object getData();

    public void setOwner(OfflinePlayer player){
        this.owner = player;
    }

    public OfflinePlayer getOwner(){
        return owner;
    }

    public boolean isInsideRegion(Location loc){
        return !(!loc.getWorld().equals(this.world) ||
                loc.getBlockX() < minX || loc.getBlockX() > maxX ||
                loc.getBlockY() < minY || loc.getBlockY() > maxY ||
                loc.getBlockZ() < minZ || loc.getBlockZ() > maxZ);
    }

    public abstract int getMaxDecayTime();

    public int getDecayTime(){
        return decayTime;
    }

    public void setDecayTime(int time){
        decayTime = time;
        QueryManager.updateDecay(this);
    }

    public void update(){
        if(canDecay()) {
            decayTime--;
            if (decayTime <= 0) {
                reset();
                decayTime = getMaxDecayTime();
            }
            Bukkit.getScheduler().runTaskAsynchronously(QuadPrison.getInstance(), ()->
                QueryManager.updateDecay(this)
            );
        }
    }

    public abstract boolean canDecay();
}
