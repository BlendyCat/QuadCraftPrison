package com.blendycat.prison.sql;

import com.blendycat.prison.player.Prisoner;
import com.blendycat.prison.portal.Portal;
import com.blendycat.prison.region.CellBlock;
import com.blendycat.prison.region.MineRegion;
import com.blendycat.prison.region.Region;
import com.blendycat.prison.region.prison.PrisonBlock;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by EvanMerz on 10/20/17.
 */
public class QueryManager {

    public static void setUpTables(){
        Connection conn = DatabaseManager.getConnection();
        if(conn == null) return;
        try{
            CallableStatement stmt = conn.prepareCall(
                    "CREATE TABLE IF NOT EXISTS `regions`(" +
                            "`id` INT(11) NOT NULL AUTO_INCREMENT," +
                            "`world` VARCHAR(255)," +
                            "`minX` INT," +
                            "`minY` INT," +
                            "`minZ` INT," +
                            "`maxX` INT," +
                            "`maxY` INT," +
                            "`maxZ` INT," +
                            "`type` INT," +
                            "`data` BLOB," +
                            "`owner` VARCHAR(255)," +
                            "`decay` INT NOT NULL DEFAULT 0," +
                            "PRIMARY KEY(`id`));"
            );
            stmt.execute();

            stmt = conn.prepareCall(
                    "CREATE TABLE IF NOT EXISTS `prisoners`(" +
                            "`uuid` VARCHAR(255) NOT NULL," +
                            "`clearance_level` INT NOT NULL," +
                            "`cell_ID` INT NOT NULL," +
                            "PRIMARY KEY(`uuid`));"
            );
            stmt.execute();

            stmt = conn.prepareCall(
                    "CREATE TABLE IF NOT EXISTS `prison_blocks`(" +
                            "`id` INT(11) NOT NULL AUTO_INCREMENT," +
                            "`world` VARCHAR(255)," +
                            "`minX` INT," +
                            "`minY` INT," +
                            "`minZ` INT," +
                            "`maxX` INT," +
                            "`maxY` INT," +
                            "`maxZ` INT," +
                            "`name` VARCHAR(255)," +
                            "`clearance_level` INT NOT NULL," +
                            "`spawnX` INT," +
                            "`spawnY` INT," +
                            "`spawnZ` INT," +
                            "`yaw` INT, " +
                            "`pitch` INT, " +
                            "PRIMARY KEY(`id`));"
            );
            stmt.execute();

            stmt = conn.prepareCall(
                    "CREATE TABLE IF NOT EXISTS `rankups`(" +
                            "`level` INT(11) NOT NULL," +
                            "`price` DOUBLE," +
                            "PRIMARY KEY(`level`));");
            stmt.execute();

            stmt = conn.prepareCall(
                    "CREATE TABLE IF NOT EXISTS `portals`(" +
                            "`name` VARCHAR(255) NOT NULL," +
                            "`world` VARCHAR(255), " +
                            "`x1` INT," +
                            "`y1` INT," +
                            "`z1` INT," +
                            "`x2` INT," +
                            "`y2` INT," +
                            "`z2` INT," +
                            "`command` VARCHAR(255)," +
                            "PRIMARY KEY(`name`));");

            stmt.execute();

            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }
    /**
     * adds the mine region
     * @param region region to add to database
     */
    public static void addRegion(Region region){
        Connection conn = DatabaseManager.getConnection();
        if(conn == null) return;
        try{
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO `regions`(" +
                            "`world`, `minX`, `minY`, `minZ`, `maxX`, " +
                            "`maxY`, `maxZ`, `type`, `data`, `owner`)" +
                            "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
            stmt.setString(1, region.getWorld().getName());
            stmt.setInt(2, region.getMinX());
            stmt.setInt(3, region.getMinY());
            stmt.setInt(4, region.getMinZ());
            stmt.setInt(5, region.getMaxX());
            stmt.setInt(6, region.getMaxY());
            stmt.setInt(7, region.getMaxZ());
            stmt.setObject(8, region.getType());
            stmt.setObject(9, region.getData());
            stmt.setString(10, region.getOwner() == null ?
                    null : region.getOwner().getUniqueId().toString());
            stmt.execute();
            stmt = conn.prepareCall("SELECT `id` FROM `regions`;");
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            if(rs.last()){
                region.setID(rs.getInt(1));
            }
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public static void updateRegion(Region region){
        Connection conn = DatabaseManager.getConnection();
        if(conn == null) return;
        try{
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE `regions` SET `data`=?,  `owner`=? WHERE `id`=?;");
            stmt.setObject(1, region.getData());
            stmt.setString(2, region.getOwner() == null ?
                    null : region.getOwner().getUniqueId().toString());
            stmt.setInt(3, region.getID());
            stmt.execute();
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public static void updateDecay(Region region){
        Connection conn = DatabaseManager.getConnection();
        if(conn == null) return;
        try{
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE `regions` SET `decay`=? WHERE `id`=?;");
            stmt.setObject(1, region.getDecayTime());
            stmt.setInt(2, region.getID());
            stmt.execute();
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    /**
     * Get all the regions stored in the database
     * @return all regions
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<Region> getRegions(){
        ArrayList<Region> regions = new ArrayList<>();
        Connection conn = DatabaseManager.getConnection();
        if(conn == null) return null;
        try{
            CallableStatement stmt = conn.prepareCall("SELECT * FROM `regions`;");
            stmt.execute();
            ResultSet set = stmt.getResultSet();
            while(set.next()){
                // Set it up as a mine region
                int id = set.getInt("id");
                World world = Bukkit.getWorld(set.getString("world"));
                int minX = set.getInt("minX");
                int minY = set.getInt("minY");
                int minZ = set.getInt("minZ");
                int maxX = set.getInt("maxX");
                int maxY = set.getInt("maxY");
                int maxZ = set.getInt("maxZ");
                int type = set.getInt("type");
                String data = set.getString("data");
                String str = set.getString("owner");
                int decay = set.getInt("decay");
                OfflinePlayer owner = null;
                if(str != null){
                    owner = Bukkit.getOfflinePlayer(UUID.fromString(str));
                }
                if(type == Region.MINE_REGION){
                    MineRegion region = new MineRegion(world, minX, minY, minZ, maxX, maxY, maxZ);
                    region.setOwner(owner);
                    region.setID(id);
                    region.setData(data);
                    region.setDecayTime(decay);
                    regions.add(region);
                }else if(type == Region.CELL_BLOCK){
                    CellBlock region = new CellBlock(world, minX, minY, minZ, maxX, maxY, maxZ);
                    region.setOwner(owner);
                    region.setID(id);
                    region.setPrice(Double.parseDouble(data));
                    region.setDecayTime(decay);
                    regions.add(region);
                }
            }
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
        return regions;
    }

    public static void addPrisoner(Prisoner prisoner){
        Connection conn = DatabaseManager.getConnection();
        try{
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO `prisoners`(`uuid`, `clearance_level`, `cell_id`) VALUES(?, ?, ?);");
            stmt.setString(1, prisoner.getPlayer().getUniqueId().toString());
            stmt.setInt(2, prisoner.getClearanceLevel());
            stmt.setInt(3, prisoner.getCellID());
            stmt.execute();
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public static Prisoner getPrisoner(Player player){
        Prisoner prisoner = null;
        Connection conn = DatabaseManager.getConnection();
        try{
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT `clearance_level`, `cell_id` FROM `prisoners` WHERE `uuid`=?");
            stmt.setString(1, player.getUniqueId().toString());
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            if(rs.next()){
                prisoner = new Prisoner(player, rs.getInt(1));
                prisoner.setCellID(rs.getInt(2));
            }
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
        return prisoner;
    }

    public static void addPrisonBlock(PrisonBlock prisonBlock){
        Connection conn = DatabaseManager.getConnection();
        try{
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO `prison_blocks`(" +
                            "`world`, " +
                            "`minX`, " +
                            "`minY`, " +
                            "`minZ`, " +
                            "`maxX`, " +
                            "`maxY`, " +
                            "`maxZ`, " +
                            "`name`, " +
                            "`clearance_level`) " +
                            "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);"
            );

            stmt.setString(1, prisonBlock.getWorld().getName());
            stmt.setInt(2, prisonBlock.getMinX());
            stmt.setInt(3, prisonBlock.getMinY());
            stmt.setInt(4, prisonBlock.getMinZ());
            stmt.setInt(5, prisonBlock.getMaxX());
            stmt.setInt(6, prisonBlock.getMaxY());
            stmt.setInt(7, prisonBlock.getMaxZ());
            stmt.setString(8, prisonBlock.getName());
            stmt.setInt(9, prisonBlock.getClearanceLevel());
            stmt.execute();

            stmt = conn.prepareCall("SELECT `id` FROM `prison_blocks`;");
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            if(rs.last()){
                prisonBlock.setID(rs.getInt(1));
            }
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public static ArrayList<PrisonBlock> getPrisonBlocks(){
        ArrayList<PrisonBlock> prisonBlocks = new ArrayList<>();
        Connection conn = DatabaseManager.getConnection();
        try{
            CallableStatement stmt = conn.prepareCall(
                    "SELECT * FROM `prison_blocks`;"
            );
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            while(rs.next()){
                int id = rs.getInt(1);
                World world = Bukkit.getWorld(rs.getString(2));
                int minX = rs.getInt(3);
                int minY = rs.getInt(4);
                int minZ = rs.getInt(5);
                int maxX = rs.getInt(6);
                int maxY = rs.getInt(7);
                int maxZ = rs.getInt(8);
                String name = rs.getString(9);
                int clearanceLevel = rs.getInt(10);
                int spX = rs.getInt(11);
                int spY = rs.getInt(12);
                int spZ = rs.getInt(13);
                Location loc = new Location(world, spX, spY, spZ);
                PrisonBlock block = new PrisonBlock(world, minX, minY, minZ, maxX, maxY, maxZ);
                block.setID(id);
                block.setName(name);
                block.setClearanceLevel(clearanceLevel);
                block.setSpawn(loc);
                prisonBlocks.add(block);
            }
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
        return prisonBlocks;
    }

    public static void updatePrisonBlock(PrisonBlock prisonBlock){
        Connection conn = DatabaseManager.getConnection();
        try{
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE `prison_blocks` SET `name`=?, `clearance_level`=?, " +
                            "`spawnX`=?, `spawnY`=?, `spawnZ`=? WHERE `id`=?;"
            );
            stmt.setString(1, prisonBlock.getName());
            stmt.setInt(2, prisonBlock.getClearanceLevel());
            if(prisonBlock.getSpawn() == null) {
                stmt.setInt(3, 0);
                stmt.setInt(4, 0);
                stmt.setInt(5, 0);
            }else{
                stmt.setInt(3, prisonBlock.getSpawn().getBlockX());
                stmt.setInt(4, prisonBlock.getSpawn().getBlockY());
                stmt.setInt(5, prisonBlock.getSpawn().getBlockZ());
            }
            stmt.setInt(6, prisonBlock.getID());
            stmt.execute();
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public static void updatePrisoner(Prisoner prisoner){
        Connection conn = DatabaseManager.getConnection();
        try{
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE `prisoners` SET `clearance_level`=?, `cell_ID`=? WHERE `uuid`=?;"
            );
            stmt.setInt(1, prisoner.getClearanceLevel());
            stmt.setInt(2, prisoner.getCellID());
            stmt.setString(3, prisoner.getPlayer().getUniqueId().toString());
            stmt.execute();

            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public static double getRankupPrice(int level){
        Connection conn = DatabaseManager.getConnection();
        double price = 0;
        try{
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT `price` FROM `rankups` WHERE `level`=?;");
            stmt.setInt(1, level);
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            if(rs.next()){
                price = rs.getDouble(1);
            }
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
        return price;
    }

    public static boolean rankupExists(int level){
        Connection conn = DatabaseManager.getConnection();
        boolean rank = false;
        try{
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `rankups` WHERE `level`=?;");
            stmt.setInt(1, level);
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            if(rs.next()){
                rank = true;
            }
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
        return rank;
    }

    public static void addRankup(int level, double price){
        Connection conn = DatabaseManager.getConnection();
        try{
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO `rankups`(`level`, `price`) " +
                            "VALUES(?, ?);"
            );
            stmt.setInt(1, level);
            stmt.setDouble(2, price);
            stmt.execute();
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public static void setRankupPrice(int level, double price){
        Connection conn = DatabaseManager.getConnection();
        try{
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE `rankups` SET " +
                            "`price`=? WHERE " +
                            "`level`=?;"
            );
            stmt.setDouble(1, price);
            stmt.setInt(2, level);
            stmt.execute();
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public static void addPortal(String name, Selection sel){
        Connection conn = DatabaseManager.getConnection();
        try{
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO `portals`(`name`, `world`, `x1`, `y1`, `z1`, `x2`, `y2`, `z2`) " +
                            "VALUES(?, ?, ?, ?, ?, ?, ?, ?);"
            );
            Location min = sel.getMinimumPoint();
            Location max = sel.getMaximumPoint();

            stmt.setString(1, name);

            stmt.setString(2, min.getWorld().getName());

            stmt.setInt(3, min.getBlockX());
            stmt.setInt(4, min.getBlockY());
            stmt.setInt(5, min.getBlockZ());

            stmt.setInt(6, max.getBlockX());
            stmt.setInt(7, max.getBlockY());
            stmt.setInt(8, max.getBlockZ());
            stmt.execute();

            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public static void removePortal(String name){
        Connection conn = DatabaseManager.getConnection();
        try{
            PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM `portals` WHERE `name`=?;"
            );

            stmt.setString(1, name);
            stmt.execute();

            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public static ArrayList<Portal> getPortals(){
        ArrayList<Portal> portals = new ArrayList<>();
        Connection conn = DatabaseManager.getConnection();
        try{
            CallableStatement stmt = conn.prepareCall(
                    "SELECT * FROM `portals`;"
            );
            stmt.execute();

            ResultSet rs = stmt.getResultSet();

            while(rs.next()){
                String name = rs.getString("name");
                String worldName = rs.getString("world");
                World world = Bukkit.getWorld(worldName);
                int x1 = rs.getInt("x1");
                int y1 = rs.getInt("y1");
                int z1 = rs.getInt("z1");

                int x2 = rs.getInt("x2");
                int y2 = rs.getInt("y2");
                int z2 = rs.getInt("z2");

                String command = rs.getString("command");

                Portal portal = new Portal(
                        name, world,
                        x1, y1, z1,
                        x2, y2, z2
                );

                portal.setCommand(command);

                portals.add(portal);
            }
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
        return portals;
    }

    public static void setPortalCommand(String name, String command){
        Connection conn = DatabaseManager.getConnection();
        try{
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE `portals` SET `command`=? WHERE `name`=?;"
            );
            stmt.setString(1, command);
            stmt.setString(2, name);

            stmt.execute();
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }
}
