package com.blendycat.prison.region;

import com.blendycat.prison.QuadPrison;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * Created by EvanMerz on 10/19/17.
 */
public class MineRegion extends Region {

    private HashMap<Material, Integer> blockValues;
    private Random random = new Random();


    public MineRegion(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){

        super(world, minX, minY, minZ, maxX, maxY, maxZ, Region.MINE_REGION);
        blockValues = new HashMap<>();
    }


    /**
     * Add the material to the generation list
     * @param material the material to generate
     * @param percent the percent at which the material generates
     */
    public void addRegenBlock(Material material, int percent) {
        blockValues.put(material, percent);
    }

    /**
     *
     * @param values sets the block values
     */
    public void setRegenBlocks(HashMap<Material, Integer> values){
        blockValues = values;
    }

    /**
     * Resets to the default state
     */
    @Override
    public void reset() {
        Material[] materials = sortBlockValues();
        for(int y = getMinY(); y <= getMaxY(); y++){
            for(int z = getMinZ(); z <= getMaxZ(); z++){
                for(int x = getMinX(); x <= getMaxX(); x++){
                    for(Material mat : materials){
                        int percent = blockValues.get(mat);
                        if(random.nextInt(100) + 1 <= percent){
                            final int xf = x;
                            final int yf = y;
                            final int zf = z;
                            Bukkit.getScheduler().scheduleSyncDelayedTask(QuadPrison.getInstance(), ()->
                                getWorld().getBlockAt(xf,yf,zf).setType(mat)
                            );
                        }
                    }
                }
            }
        }
        for(Player player : Bukkit.getOnlinePlayers()){
            if(isInsideRegion(player.getLocation())) {
                Location loc = player.getLocation();
                loc.setY(getMaxY() + 1);
                player.teleport(loc);
                player.sendMessage(ChatColor.AQUA + "The mine reset! You were teleported to the top!");
            }
        }
    }

    /**
     *
     * @return block values and percents
     */
    public HashMap<Material, Integer> getRegenBlocks(){
        return blockValues;
    }

    @Override
    public boolean canBuild() {
        return false;
    }

    @Override
    public boolean canBreak() {
        return true;
    }

    @Override
    public boolean canPVP() {
        return false;
    }

    @Override
    public boolean canUseItem() {
        return true;
    }

    @Override
    public boolean canOpenStorage() {
        return true;
    }

    @Override
    public boolean isOwnable() {
        return false;
    }

    /**
     * Get the data for this object
     * @return data as object
     */
    @Override
    public Object getData() {
        return blockValues.toString();
    }

    /**
     * FML why can't you check instanceof for hashmaps rip
     * @param string data string
     */
    public void setData(String string){
        blockValues = new HashMap<>();
        String list = string.replace("{", "").replace("}", "").trim();
        String[] values = list.split(Pattern.quote(","));
        for(String value : values){
            String[] set = value.trim().split("=");
            if(set.length == 2) {
                Material material = Material.getMaterial(set[0].trim().toUpperCase());
                int percent = Integer.parseInt(set[1].trim());
                blockValues.put(material, percent);
            }
        }
    }

    public Material[] sortBlockValues() {
        Material[] materials = blockValues.keySet().toArray(new Material[blockValues.keySet().size()]);
        for(int i2 = 0; i2 < materials.length; i2++) {
            for (int i = 0; i < materials.length - 1; i++) {
                if (blockValues.get(materials[i]) < blockValues.get(materials[i + 1])) {
                    Material m1 = materials[i];
                    Material m2 = materials[i + 1];
                    materials[i] = m2;
                    materials[i + 1] = m1;
                }
            }
        }
        return materials;
    }

    @Override
    public int getMaxDecayTime() {
        return 30;
    }

    @Override
    public boolean canDecay() {
        return true;
    }
}
