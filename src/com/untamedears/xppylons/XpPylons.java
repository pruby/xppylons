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

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.material.*;
import org.bukkit.event.block.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class XpPylons extends JavaPlugin implements Listener {
    private static final Logger log = Logger.getLogger("XpPylons");
    private static XpPylons plugin;
    
    private PylonConfig config;
    private Map<World, PylonSet> pylonSets;
    private Map<World, EnergyField> energyFields;
    
    private PylonPattern pylonPattern;
    private int activationItemId;
    private int diviningItemId;
    private int interactionBlockId;
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        return true;
    }

    public void onEnable() {
        plugin = this;
        
        saveDefaultConfig();
        
        pylonSets = new HashMap<World, PylonSet>();
        energyFields = new HashMap<World, EnergyField>();
        
        diviningItemId = getConfig().getInt("items.divining");
        activationItemId = getConfig().getInt("items.activation");
        interactionBlockId = getConfig().getInt("materials.interaction");
        
        pylonPattern = new PylonPattern(getConfig());
        
        this.config = new PylonConfig(getConfig().getConfigurationSection("pylons"));
        
        ConsoleCommandSender console = getServer().getConsoleSender();
        console.addAttachment(this, "xppylons.console", true);
        
        // runTest();
        
        registerEvents();
        
        log.info("[XpPylons] XP Pylons enabled.");
    }

    public void onDisable() {
        log.info("[XpPylons] XP Pylons disabled.");
    }
    
    public void runTest() {
        log.info("[XpPylons] Running test...");
    }
    
    public void registerEvents(){
        try {
            PluginManager pm = getServer().getPluginManager();
            pm.registerEvents(this, this);
        }
        catch(Exception e)
        {
          severe(e.toString());
        }
    }
    
    private void initWorld(World world) {
        PylonSet worldPylons = new PylonSet(this, config);
        EnergyField worldEnergy = new EnergyField(world, getConfig().getConfigurationSection("energy"));
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
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        try {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getMaterial().getId() == activationItemId && e.hasBlock() && e.getClickedBlock().getType().getId() == interactionBlockId) {
                togglePylon(e.getClickedBlock(), e.getPlayer());
            } else if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getMaterial().getId() == diviningItemId) {
                doDivining(e.getPlayer());
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
                info("Is glowstone");
                info("Changing glow block back to " + Integer.toString(pylonPattern.getOriginalGlowBlockTypeId()));
                glowBlock.setTypeId(pylonPattern.getOriginalGlowBlockTypeId());
            }
        }
        getPylons(world).removePylon(pylon);
    }
    
    public void doDivining(Player player) {
        EnergyField field = getEnergyField(player.getWorld()); 
        double energyHere = field.energyAt(player.getLocation().getX(), player.getLocation().getZ());
        player.sendMessage("Energy here is " + Double.toString(energyHere));
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
    
    public static void info(String message){
        log.info("[XpPylons] " + message);
    }
    
    public static void severe(String message){
        log.severe("[XpPylons] " + message);
    }
    
    public static void warning(String message){
        log.warning("[XpPylons] " + message);
    }
}
