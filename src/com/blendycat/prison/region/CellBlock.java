package com.blendycat.prison.region;

import com.blendycat.prison.QuadPrison;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

/**
 * Created by EvanMerz on 10/20/17.
 */
public class CellBlock extends Region {

    private double price;

    public CellBlock(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
        super(world, minX, minY, minZ, maxX, maxY, maxZ, Region.CELL_BLOCK);
        price = QuadPrison.getCellPrice();
    }

    @Override
    public void reset() {
        boolean regen = false;
        if(getOwner() == null) regen = true;
        if(getOwner() != null) {
            Economy eco = QuadPrison.economy;
            if (eco.getBalance(getOwner()) < price) {
                setOwner(null);
                regen = true;
            } else {
                eco.withdrawPlayer(getOwner(), price);
            }
        }
        if(regen){
            for (int x = getMinX(); x <= getMaxX(); x++) {
                for (int y = getMinY(); y <= getMaxY(); y++) {
                    for (int z = getMinZ(); z <= getMaxZ(); z++) {
                        final int xf = x;
                        final int yf = y;
                        final int zf = z;
                        Bukkit.getScheduler().scheduleSyncDelayedTask(QuadPrison.getInstance(),
                                ()->
                        getWorld().getBlockAt(xf, yf, zf).setType(Material.AIR));
                    }
                }
            }
        }
    }

    public void setPrice(double price){
        this.price = price;
    }

    public double getPrice(){
        return price;
    }

    @Override
    public boolean isOwnable() {
        return true;
    }

    @Override
    public boolean canBuild() {
        return true;
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
    public Object getData() {
        return price;
    }

    @Override
    public int getMaxDecayTime() {
        return 20160;
    }

    @Override
    public boolean canDecay() {
        return (getOwner() != null);
    }
}
