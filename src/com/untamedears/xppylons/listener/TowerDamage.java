package com.untamedears.xppylons.listener;

import java.util.Set;
import java.util.HashSet;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.Location;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;

import com.untamedears.xppylons.XpPylons;
import com.untamedears.xppylons.PylonSet;
import com.untamedears.xppylons.Pylon;
import com.untamedears.xppylons.EnergyField;

public class TowerDamage implements Listener {
    private XpPylons plugin;
    private Set<Integer> diviningBlocks;
    
    public TowerDamage(XpPylons plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent e) {
        try {
            final World world = e.getBlock().getWorld();
            final PylonSet pylons = plugin.getPylons(world);
            final List<Pylon> possiblyDamagedPylons = pylons.pylonsAround(e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ());
            for (Pylon pylon : possiblyDamagedPylons) {
                if (pylon.getX() == e.getBlock().getX() && pylon.getY() == (e.getBlock().getY() + 1) && pylon.getZ() == e.getBlock().getZ()) {
                    // Is the glow block
                    e.setCancelled(true);
                    plugin.deactivatePylon(pylon, world);
                    return;
                }
            }
            
            if (!possiblyDamagedPylons.isEmpty()) {
                scheduleStructureCheck(e.getBlock().getWorld(), possiblyDamagedPylons);
            }
        } catch (RuntimeException ex) {
            plugin.severe(ex.getClass().getName());
            ex.printStackTrace();
            throw ex;
        }
    }
    
    private void scheduleStructureCheck(final World world, final List<Pylon> possiblyDamagedPylons) {
        final XpPylons plugin = this.plugin;
        
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override 
            public void run() {
                for (Pylon pylon : possiblyDamagedPylons) {
                    if (!plugin.getPylonPattern().checkStructure(world, pylon)) {
                        plugin.info("Pylon at " + Integer.toString(pylon.getX()) + ", " + Integer.toString(pylon.getY()) + ", " + Integer.toString(pylon.getZ()) + " damaged, deactivating");
                        plugin.deactivatePylon(pylon, world);
                    }
                }
            }
        }, 1L);
    }
    
}