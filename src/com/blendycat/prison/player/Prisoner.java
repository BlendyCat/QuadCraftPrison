package com.blendycat.prison.player;

import com.blendycat.prison.QuadPrison;
import com.blendycat.prison.region.prison.PrisonBlock;
import org.bukkit.entity.Player;

/**
 * Created by EvanMerz on 10/20/17.
 */
public class Prisoner {

    private Player player;
    private int clearanceLevel = 0;
    private int cellID = -1;

    public Prisoner(Player player, int clearanceLevel){
        this.player = player;
        this.clearanceLevel = clearanceLevel;
    }

    public int getClearanceLevel(){
        return clearanceLevel;
    }

    public void setCellID(int cellID){
        this.cellID = cellID;
    }

    public int getCellID(){
        return cellID;
    }

    public boolean ownsCell(){
        return cellID != -1;
    }

    public void setDoesNotCell(){
        cellID = -1;
    }

    public void setClearanceLevel(int clearanceLevel){
        this.clearanceLevel = clearanceLevel;
    }

    public Player getPlayer(){
        return player;
    }

    public PrisonBlock getPrisonBlock(){
        for(PrisonBlock p : QuadPrison.getPrisonBlocks()){
            if(p.getClearanceLevel() == clearanceLevel){
                return p;
            }
        }
        return null;
    }
}
