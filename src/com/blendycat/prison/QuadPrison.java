package com.blendycat.prison;

import com.blendycat.prison.command.*;
import com.blendycat.prison.player.Prisoner;
import com.blendycat.prison.player.PrisonerListener;
import com.blendycat.prison.portal.Portal;
import com.blendycat.prison.portal.PortalListener;
import com.blendycat.prison.region.Region;
import com.blendycat.prison.region.RegionListener;
import com.blendycat.prison.region.prison.PrisonBlock;
import com.blendycat.prison.region.prison.PrisonBlockListener;
import com.blendycat.prison.sign.SignListener;
import com.blendycat.prison.sql.QueryManager;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by EvanMerz on 10/19/17.
 */
public class QuadPrison extends JavaPlugin {
    public static Economy economy = null;

    private static ArrayList<Region> regions;
    private static ArrayList<PrisonBlock> prisonBlocks;
    private static ArrayList<Portal> portals;
    private static HashMap<Player, Prisoner> prisoners;

    private static FileConfiguration config;
    private static QuadPrison instance;

    public static WorldEditPlugin worldEdit;


    @Override
    public void onEnable(){
        getLogger().info("[ QuadPrison 0.0.2 by BlendyCat ]");
        // Make a config object for convenience
        config = getConfig();
        // Save the default config
        saveDefaultConfig();
        // Set up the tables if not set up
        QueryManager.setUpTables();
        // Register the caches
        long startRam = Runtime.getRuntime().freeMemory();
        registerCaches();
        long endRam = Runtime.getRuntime().freeMemory();
        // Make an instance object for static calls
        instance = this;
        // Register the event listener
        registerEvents();
        // Get world edit object
        worldEdit = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        // Register the commands
        registerCommands();
        // Set up the economy
        setupEconomy();
        // Finally add a scheduled repeating task to decay regions
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, ()-> {
            for(Region region: regions){
                region.update();
            }
        }, 0, 1200);

        getLogger().info("Cache ram usage of QuadPrison " + (startRam - endRam)/1000000 + " MB");
    }

    /**
     * Registers all the event listeners
     */
    private void registerEvents(){
        Bukkit.getPluginManager().registerEvents(new RegionListener(), this);
        Bukkit.getPluginManager().registerEvents(new SignListener(), this);
        Bukkit.getPluginManager().registerEvents(new PrisonerListener(), this);
        Bukkit.getPluginManager().registerEvents(new PrisonBlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new MonitorListener(), this);
        Bukkit.getPluginManager().registerEvents(new PortalListener(), this);
    }

    /**
     * Register all the commands
     */
    private void registerCommands(){
        getCommand("mineregion").setExecutor(new MineRegionCommand());
        getCommand("cellblock").setExecutor(new CellBlockCommand());
        getCommand("cell").setExecutor(new CellCommand());
        getCommand("prisonblock").setExecutor(new PrisonBlockCommand());
        getCommand("rankup").setExecutor(new RankUpCommand());
        getCommand("portal").setExecutor(new PortalCommand());
    }

    /**
     * register the lists of objects typically stored
     * in the database for efficiency and reduction of
     * database calls
     */
    private void registerCaches(){
        // Initialize region arraylist
        prisoners = new HashMap<>();
        regions = QueryManager.getRegions();
        prisonBlocks = QueryManager.getPrisonBlocks();
        portals = QueryManager.getPortals();
    }

    @Override
    public void onDisable(){
        saveConfig();
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider =
                getServer().getServicesManager()
                        .getRegistration(net.milkbowl
                                .vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
    }

    public static String getDatabaseHost(){
        String host = null;
        if(config.contains("db_host")) {
            host = config.getString("db_host");
        }else{
            instance.setUpConfig();
            Bukkit.getPluginManager().disablePlugin(instance);
        }
        return host;
    }

    public static String getDatabaseName(){
        String name = null;
        if(config.contains("db_name")) {
            name = config.getString("db_name");
        }else{
            instance.setUpConfig();
            Bukkit.getPluginManager().disablePlugin(instance);
        }
        return name;
    }

    public static String getDatabaseUser(){
        String user = null;
        if(config.contains("db_user")) {
            user = config.getString("db_user");
        }else{
            instance.setUpConfig();
            Bukkit.getPluginManager().disablePlugin(instance);
        }
        return user;
    }

    public static String getDatabasePassword(){
        String password = null;
        if(config.contains("db_password")) {
            password = config.getString("db_password");
        }else{
            instance.setUpConfig();
            Bukkit.getPluginManager().disablePlugin(instance);
        }
        return password;
    }

    public static double getCellPrice(){
        if(config.contains("cell_price")){
            return config.getDouble("cell_price");
        }else{
            return 100.0;
        }
    }

    private void setUpConfig(){
        config.addDefault("db_host", "localhost");
        config.addDefault("db_name", "name");
        config.addDefault("db_user", "username");
        config.addDefault("db_password", "password");
        config.addDefault("cell_price", 100.0);
        config.options().copyDefaults(true);
        instance.saveConfig();
    }

    public static Region[] getRegions() {
        return regions.toArray(new Region[regions.size()]);
    }

    public static PrisonBlock[] getPrisonBlocks() {
        return prisonBlocks.toArray(new PrisonBlock[prisonBlocks.size()]);
    }

    public static void addRegion(Region region){
        regions.add(region);
    }

    public static void addPrisonBlock(PrisonBlock prisonBlock){
        prisonBlocks.add(prisonBlock);
    }

    public static Prisoner getPrisoner(Player player){
        return prisoners.get(player);
    }

    public static void addPrisoner(Prisoner prisoner){
        prisoners.put(prisoner.getPlayer(), prisoner);
    }

    public static void removePrisoner(Player player){
        prisoners.remove(player);
    }

    public static Portal[] getPortals(){
        return portals.toArray(new Portal[portals.size()]);
    }

    public static void addPortal(Portal portal){
        portals.add(portal);
    }

    public static void removePortal(String name){
        for(int i = 0; i < portals.size(); i++){
            if(portals.get(i).getName().equalsIgnoreCase(name)){
                portals.remove(i);
                break;
            }
        }
    }

    public static QuadPrison getInstance(){
        return instance;
    }
}
