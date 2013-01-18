package com.untamedears.xppylons;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.Random;

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
    
    private PylonSet pylons;
    
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
        
        diviningItemId = getConfig().getInt("items.divining");
        activationItemId = getConfig().getInt("items.activation");
        interactionBlockId = getConfig().getInt("materials.interaction");
        
        pylonPattern = new PylonPattern(getConfig());
        
        PylonConfig config = new PylonConfig(getConfig().getConfigurationSection("pylons"));
        pylons = new PylonSet(this, config);
        
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
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!e.isCancelled()) {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getMaterial().getId() == activationItemId && e.hasBlock() && e.getClickedBlock().getType().getId() == interactionBlockId) {
                togglePylon(e.getClickedBlock(), e.getPlayer());
            } else if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getMaterial().getId() == diviningItemId) {
                doDivining(e.getPlayer());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent e) {
        final List<Pylon> possiblyDamagedPylons = pylons.pylonsAround(e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ());
        for (Pylon pylon : possiblyDamagedPylons) {
            if (pylon.getX() == e.getBlock().getX() && pylon.getY() == e.getBlock().getY() + 1 && pylon.getZ() == e.getBlock().getZ()) {
                // Is the glow block
                e.setCancelled(true);
            }
        }
        
        if (!possiblyDamagedPylons.isEmpty()) {
            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                @Override 
                public void run() {
                    for (Pylon pylon : possiblyDamagedPylons) {
                        if (!pylonPattern.checkStructure(e.getBlock().getWorld(), pylon)) {
                            info("Pylon at " + Integer.toString(pylon.getX()) + ", " + Integer.toString(pylon.getY()) + ", " + Integer.toString(pylon.getZ()) + " damaged, deactivating");
                            deactivatePylon(pylon, e.getBlock().getWorld());
                        }
                    }
                }
            }, 1L);
        }
    }
    
    public void activatePylon(Block block, int levels) {
        Pylon newPylon = new Pylon(pylons, block.getX(), block.getY(), block.getZ(), levels);
        pylons.addPylon(newPylon);
        Block glowBlock = block.getRelative(0, -1, 0);
        if (glowBlock != null) {
            glowBlock.setType(Material.GLOWSTONE);
        }
    }
    
    public void deactivatePylon(Pylon pylon, World world) {
        info("Removing pylon");
        Block glowBlock = world.getBlockAt(pylon.getX(), pylon.getY() - 1, pylon.getZ());
        if (glowBlock != null) {
            info("Got block");
            if (glowBlock.getType() == Material.GLOWSTONE) {
                info("Is glowstone");
                info("Changing glow block back to " + Integer.toString(pylonPattern.getOriginalGlowBlockTypeId()));
                glowBlock.setTypeId(pylonPattern.getOriginalGlowBlockTypeId());
            }
        }
        pylons.removePylon(pylon);
    }
    
    public void doDivining(Player player) {
    }
    
    public void togglePylon(Block block, Player player) {
          info("Interact");
          Pylon existingPylon = pylons.pylonAt(block.getX(), block.getY(), block.getZ());
          if (existingPylon != null) {
              player.sendMessage("Deactivated pylon");
              deactivatePylon(existingPylon, block.getWorld());
          } else if (pylonPattern.testBlock(block)) {
              int levels = pylonPattern.countLevels(block);
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
