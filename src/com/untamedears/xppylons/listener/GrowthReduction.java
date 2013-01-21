package com.untamedears.xppylons.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.world.StructureGrowEvent;

import com.untamedears.xppylons.XpPylons;
import com.untamedears.xppylons.PylonSet;
import com.untamedears.xppylons.Pylon;
import com.untamedears.xppylons.EnergyField;

public class GrowthReduction implements Listener {
    private XpPylons plugin;
    private double growthReductionMax;
    private double treeReductionMax;
    
    public GrowthReduction(XpPylons plugin) {
        this.plugin = plugin;
        growthReductionMax = plugin.getConfig().getDouble("negativeEffects.cropGrowthReduction");
        treeReductionMax = plugin.getConfig().getDouble("negativeEffects.treeGrowthReduction");
    }
    
    @EventHandler(ignoreCancelled = true)
    public void growBlock(BlockGrowEvent e) {
        PylonSet pylonSet = plugin.getPylons(e.getBlock().getWorld());
        EnergyField energyField = pylonSet.getEnergyField();
        
        double drain = pylonSet.energyDrainAtPoint(e.getBlock().getX(), e.getBlock().getZ());
        double energy = energyField.energyAt(e.getBlock().getX(), e.getBlock().getZ());
        double chance = (1.0 - drain) * energy * growthReductionMax;
        
        if (chance < 1.0) {
            if (plugin.getPluginRandom().nextDouble() >= chance) {
                plugin.info("Growth event cancelled, chance was " + Double.toString(chance));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void growStructure(StructureGrowEvent e) {
        if (e.isFromBonemeal()) {
            return;
        }
        
        PylonSet pylonSet = plugin.getPylons(e.getWorld());
        EnergyField energyField = pylonSet.getEnergyField();
        
        double drain = pylonSet.energyDrainAtPoint(e.getLocation().getX(), e.getLocation().getZ());
        double energy = energyField.energyAt(e.getLocation().getX(), e.getLocation().getZ());
        double chance = (1.0 - drain) * energy * treeReductionMax;
        
        if (chance < 1.0) {
            if (plugin.getPluginRandom().nextDouble() >= chance) {
                plugin.info("Growth event cancelled, chance was " + Double.toString(chance));
                e.setCancelled(true);
            }
        }
    }
}
