package com.untamedears.xppylons;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.material.*;
import org.bukkit.event.block.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.untamedears.xppylons.task.AccumulateXP;
import com.untamedears.xppylons.task.RecalculateXPRate;
import com.untamedears.xppylons.listener.GrowthReduction;

public class XpPylons extends JavaPlugin implements Listener {
    private static final Logger log = Logger.getLogger("XpPylons");
    private static XpPylons plugin;
    
    private PylonConfig config;
    private Map<World, PylonSet> pylonSets;
    private Map<World, EnergyField> energyFields;
    private Random pluginRandom;
    
    private PylonPattern pylonPattern;
    private int activationItemId;
    private int diviningItemId;
    private int interactionBlockId;
    
    private int accumulateXpTask;
    private int recalculateXpTask;
    
    private Set<Integer> diviningBlocks;
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        return true;
    }

    public void onEnable() {
        plugin = this;
        
        saveDefaultConfig();
        initDiviningBlocks();
        
        pylonSets = new HashMap<World, PylonSet>();
        energyFields = new HashMap<World, EnergyField>();
        pluginRandom = new Random();
        
        diviningItemId = getConfig().getInt("items.divining");
        activationItemId = getConfig().getInt("items.activation");
        interactionBlockId = getConfig().getInt("materials.interaction");
        
        pylonPattern = new PylonPattern(getConfig());
        
        this.config = new PylonConfig(getConfig().getConfigurationSection("pylons"));
        
        ConsoleCommandSender console = getServer().getConsoleSender();
        console.addAttachment(this, "xppylons.console", true);
        
        // runTest();
        
        loadPylons();
        registerEvents();
        startTasks();
        
        log.info("[XpPylons] XP Pylons enabled.");
    }

    public void onDisable() {
        savePylons();
        log.info("[XpPylons] XP Pylons disabled.");
    }
    
    public void runTest() {
        log.info("[XpPylons] Running test...");
    }
    
    public Random getPluginRandom() {
        return pluginRandom;
    }
    
    private void registerEvents(){
        try {
            PluginManager pm = getServer().getPluginManager();
            pm.registerEvents(this, this);
            pm.registerEvents(new GrowthReduction(this), this);
        }
        catch(Exception e)
        {
          severe(e.toString());
        }
    }
    
    private void startTasks() {
        long accumulateInterval = getConfig().getInt("pylons.xpCollectionInterval") * 20L;
        accumulateXpTask = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new AccumulateXP(this), accumulateInterval, accumulateInterval);
        
        long recalculateInterval = getConfig().getInt("pylons.xpCalculationInterval") * 20L;
        recalculateXpTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, new RecalculateXPRate(this), recalculateInterval, recalculateInterval);
    }
    
    private void initWorld(World world) {
        EnergyField worldEnergy = new EnergyField(world, getConfig().getConfigurationSection("energy"));
        PylonSet worldPylons = new PylonSet(this, worldEnergy, config);
        pylonSets.put(world, worldPylons);
        energyFields.put(world, worldEnergy);
    }
    
    public PylonSet getPylons(World world) {
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return null;
        }
        
        PylonSet worldPylons = pylonSets.get(world);
        if (worldPylons == null) {
            initWorld(world);
            worldPylons = pylonSets.get(world);
        }
        return worldPylons;
    }
    
    public EnergyField getEnergyField(World world) {
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return null;
        }
        
        EnergyField worldEnergy = energyFields.get(world);
        if (worldEnergy == null) {
            initWorld(world);
            worldEnergy = energyFields.get(world);
        }
        return worldEnergy;
    }
    
    public void initDiviningBlocks() {
        diviningBlocks = new HashSet<Integer>();
        diviningBlocks.add(Material.LEAVES.getId());
        diviningBlocks.add(Material.GRASS.getId());
        diviningBlocks.add(Material.LONG_GRASS.getId());
        diviningBlocks.add(Material.YELLOW_FLOWER.getId());
        diviningBlocks.add(Material.RED_ROSE.getId());
        diviningBlocks.add(Material.CACTUS.getId());
        diviningBlocks.add(Material.CROPS.getId());
        diviningBlocks.add(Material.PUMPKIN_STEM.getId());
        diviningBlocks.add(Material.MELON_STEM.getId());
        diviningBlocks.add(Material.SUGAR_CANE_BLOCK.getId());
        diviningBlocks.add(Material.WATER_LILY.getId());
        diviningBlocks.add(Material.CARROT.getId());
        diviningBlocks.add(Material.POTATO.getId());
        diviningBlocks.add(Material.SAPLING.getId());
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        try {
            Block block = null;
            int clickedBlockType = -1;
            if (e.hasBlock()) {
                block = e.getClickedBlock();
                clickedBlockType = block.getType().getId();
            }
            int materialInHand = e.getMaterial().getId() ;
            
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK && materialInHand == activationItemId && clickedBlockType == interactionBlockId) {
                // Activate/deactivate pylon
                togglePylon(block, e.getPlayer());
            } else if ((e.getAction() == Action.RIGHT_CLICK_BLOCK) && materialInHand == diviningItemId) {
                // Divining
                if (e.hasBlock() && diviningBlocks.contains(block.getTypeId())) {
                    Location diviningPoint = block.getLocation();
                    doDivining(e.getPlayer(), diviningPoint);
                }
            } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK && materialInHand == Material.GLASS_BOTTLE.getId() && clickedBlockType == interactionBlockId) {
                Pylon existingPylon = getPylons(block.getWorld()).pylonAt(block.getX(), block.getY(), block.getZ());
                if (existingPylon != null) {
                    // Fill bottles
                    e.getPlayer().sendMessage("Tower has " + Double.toString(existingPylon.getXp()) + " XP, accumulating at a rate of " + Double.toString(existingPylon.getXpRate()));
                    existingPylon.dispenseXp(e.getPlayer());
                }
            }
        } catch (RuntimeException ex) {
            severe(ex.getClass().getName());
            ex.printStackTrace();
            throw ex;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent e) {
        try {
            final World world = e.getBlock().getWorld();
            final PylonSet pylons = getPylons(world);
            final List<Pylon> possiblyDamagedPylons = pylons.pylonsAround(e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ());
            for (Pylon pylon : possiblyDamagedPylons) {
                if (pylon.getX() == e.getBlock().getX() && pylon.getY() == e.getBlock().getY() + 1 && pylon.getZ() == e.getBlock().getZ()) {
                    // Is the glow block
                    e.setCancelled(true);
                    deactivatePylon(pylon, world);
                    return;
                }
            }
            
            if (!possiblyDamagedPylons.isEmpty()) {
                scheduleStructureCheck(e.getBlock().getWorld(), possiblyDamagedPylons);
            }
        } catch (RuntimeException ex) {
            severe(ex.getClass().getName());
            ex.printStackTrace();
            throw ex;
        }
    }
    
    private void scheduleStructureCheck(final World world, final List<Pylon> possiblyDamagedPylons) {
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override 
            public void run() {
                for (Pylon pylon : possiblyDamagedPylons) {
                    if (!pylonPattern.checkStructure(world, pylon)) {
                        info("Pylon at " + Integer.toString(pylon.getX()) + ", " + Integer.toString(pylon.getY()) + ", " + Integer.toString(pylon.getZ()) + " damaged, deactivating");
                        deactivatePylon(pylon, world);
                    }
                }
            }
        }, 1L);
    }
    
    public void activatePylon(Block block, int levels) {
        World world = block.getWorld();
        Pylon newPylon = new Pylon(getPylons(world), block.getX(), block.getY(), block.getZ(), levels);
        getPylons(world).addPylon(newPylon);
        Block glowBlock = block.getRelative(0, -1, 0);
        if (glowBlock != null) {
            glowBlock.setType(Material.GLOWSTONE);
        }
    }
    
    public void deactivatePylon(Pylon pylon, World world) {
        Block glowBlock = world.getBlockAt(pylon.getX(), pylon.getY() - 1, pylon.getZ());
        if (glowBlock != null) {
            if (glowBlock.getType() == Material.GLOWSTONE) {
                glowBlock.setTypeId(pylonPattern.getOriginalGlowBlockTypeId());
            }
        }
        getPylons(world).removePylon(pylon);
    }
    
    // Drained messages used for energy drained below base level
    public static double[] drainedDiviningThresholds = {
        0.9,
        0.8,
        0.6,
        0.4,
        0.2,
        0.0
    };
    public static String[] drainedDiviningMessages = {
        "Growth seems slow here",
        "Growing things seem less green here",
        "Plantlife seems strained here",
        "There is little new growth around here",
        "Plants here are withering",
        "Plants here seem on the verge of death"
    };
    
    // Overage messages used for areas with a surplus over base level
    public static double[] overageDiviningThresholds = {
        0.9,
        0.8,
        0.6,
        0.4,
        0.2,
        0.0
    };
    
    public static String[] overageDiviningMessages = {
        "Plants here are glowing with health",
        "Plantlife here is in perfect health",
        "Plants here are sprawling vigorously",
        "Plantlife here is very healthy",
        "The plants here are green and healthy",
        "Plants here are growing normally"
    };
    
    public void doDivining(Player player, Location diviningLocation) {
        EnergyField field = getEnergyField(player.getWorld()); 
        
        double remainingEnergy;
        if (field != null) {
            double energyHere = field.energyAt(diviningLocation.getX(), diviningLocation.getZ());
            double drainHere = getPylons(player.getWorld()).energyDrainAtPoint(player.getLocation().getX(), player.getLocation().getZ());
            
            remainingEnergy = energyHere * (1.0 - drainHere);
        } else {
            // No energy here
            remainingEnergy = 0.0;
        }
        
        if (remainingEnergy < 1.0) {
            // Use drain messages
            for (int i = 0; i < drainedDiviningMessages.length; i++) {
                if (drainedDiviningThresholds[i] <= remainingEnergy) {
                    player.sendMessage(drainedDiviningMessages[i]);
                    break;
                }
            }
        } else {
            double peakEnergy = getPylons(player.getWorld()).getEnergyField().getMaxTimesBackground();
            double peakOverBackground = peakEnergy - 1.0;
            double extraProportion = (remainingEnergy - 1.0) / peakOverBackground;
            
            for (int i = 0; i < overageDiviningMessages.length; i++) {
                if (overageDiviningThresholds[i] <= extraProportion) {
                    player.sendMessage(overageDiviningMessages[i]);
                    break;
                }
            }
        }
    }
    
    public void togglePylon(Block block, Player player) {
          info("Interact");
          World world = block.getWorld();
          Pylon existingPylon = getPylons(world).pylonAt(block.getX(), block.getY(), block.getZ());
          if (existingPylon != null) {
              player.sendMessage("Deactivated pylon");
              deactivatePylon(existingPylon, block.getWorld());
          } else if (pylonPattern.testBlock(block, false)) {
              int levels = pylonPattern.countLevels(block);
              PylonSet pylons = getPylons(world);
              if (levels > pylons.getConfig().getMaxPylonHeight()) {
                  levels = pylons.getConfig().getMaxPylonHeight();
              }
              if (levels >= pylons.getConfig().getMinPylonHeight()) {
                  activatePylon(block, levels);
                  player.sendMessage("Activated pylon with " + Integer.toString(levels) + " levels");
              }
          }
    }
    
    public synchronized void loadPylons() {
        for (World world : getServer().getWorlds()) {
            File expectedSave = new File(getDataFolder(), world.getUID().toString() + ".pyl");
            if (expectedSave.exists()) {
                try {
                    FileInputStream fileInput = new FileInputStream(expectedSave);
                    ObjectInputStream objectInput = new ObjectInputStream(fileInput);
                    getPylons(world).readPylons(objectInput);
                    fileInput.close();
                } catch (FileNotFoundException e) {
                    severe("Pylon save file disappeared!");
                } catch (IOException e) {
                    severe("IO error while loading pylons");
                    e.printStackTrace();
                }
            }
        }
    }
    
    public synchronized void savePylons() {
        for (World world : pylonWorlds()) {
            File saveFile = new File(getDataFolder(), world.getUID().toString() + ".pyl");
            
            try {
                // Write to temp file, move over save file to prevent half-files
                File tempFile = File.createTempFile("pylons", ".tmp", getDataFolder());
                
                PylonSet pylonSet = getPylons(world);
                FileOutputStream fileOutput = new FileOutputStream(tempFile);
                ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput);
                
                pylonSet.savePylons(objectOutput);
                objectOutput.close();
                fileOutput.close();
                
                tempFile.renameTo(saveFile);
            } catch (FileNotFoundException e) {
                severe("File disappeared while saving pylons");
            } catch (IOException e) {
                severe("Error saving pylons");
                e.printStackTrace();
            }
        }
    }
    
    public static void info(String message){
        log.info("[XpPylons] " + message);
    }
    
    public static void severe(String message){
        log.severe("[XpPylons] " + message);
    }
    
    public static void warning(String message){
        log.warning("[XpPylons] " + message);
    }
    
    public Set<World> pylonWorlds() {
        return pylonSets.keySet();
    }
}
