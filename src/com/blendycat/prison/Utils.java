package com.blendycat.prison;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Created by EvanMerz on 10/23/17.
 */
public class Utils {

    public static boolean hasEnoughSpace(ItemStack item, PlayerInventory inv){
        int freeSpace = 0;
        for(ItemStack i : inv.getStorageContents()){
            if(i == null) {
                freeSpace += item.getMaxStackSize();
            }else if(i.getType() == item.getType()){
                freeSpace += item.getMaxStackSize() - i.getAmount();
            }
        }
        return freeSpace >= item.getAmount();
    }
}
