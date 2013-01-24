package com.untamedears.xppylons.listener;

import org.bukkit.Location;
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
    
    private boolean shouldCancelGrowth(Location location, double max_reduction) {
        PylonSet pylonSet = plugin.getPylons(location.getWorld());
        if (pylonSet != null) {        
            EnergyField energyField = pylonSet.getEnergyField();
            
            double drain = pylonSet.energyDrainAtPoint(location.getX(), location.getZ());
            double energy = energyField.energyAt(location.getX(), location.getZ());
            double chance = (1.0 - drain) * energy / max_reduction;
            
            if (chance < 1.0) {
                if (plugin.getPluginRandom().nextDouble() >= chance) {
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void growBlock(BlockGrowEvent e) {
        if (shouldCancelGrowth(e.getBlock().getLocation(), growthReductionMax)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void growStructure(StructureGrowEvent e) {
        if (e.isFromBonemeal()) {
            return;
        }
        if (shouldCancelGrowth(e.getLocation(), treeReductionMax)) {
            e.setCancelled(true);
        }
    }
}
