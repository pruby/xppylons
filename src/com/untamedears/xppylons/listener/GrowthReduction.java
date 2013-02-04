package com.untamedears.xppylons.listener;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.world.StructureGrowEvent;

import com.untamedears.xppylons.XpPylons;
import com.untamedears.xppylons.PylonSet;
import com.untamedears.xppylons.Pylon;
import com.untamedears.xppylons.EnergyField;

public class GrowthReduction implements Listener {
    private XpPylons plugin;
    private double growthReductionMax;
    private double grassReductionMax;
    private double grassDiebackMax;
    private double treeReductionMax;
    private double cropGrowthBonusXp;
    private double treeGrowthBonusXp;
    
    public GrowthReduction(XpPylons plugin) {
        this.plugin = plugin;
        growthReductionMax = plugin.getConfig().getDouble("negativeEffects.cropGrowthReduction");
        cropGrowthBonusXp = plugin.getConfig().getDouble("negativeEffects.cropGrowthBonusXp");
        grassReductionMax = plugin.getConfig().getDouble("negativeEffects.grassGrowthReduction");
        grassDiebackMax = plugin.getConfig().getDouble("negativeEffects.grassDieback");
        treeReductionMax = plugin.getConfig().getDouble("negativeEffects.treeGrowthReduction");
        treeGrowthBonusXp = plugin.getConfig().getDouble("negativeEffects.treeGrowthBonusXp");
    }
    
    private boolean shouldCancelGrowth(Location location, double max_reduction, double bonusXp) {
        PylonSet pylonSet = plugin.getPylons(location.getWorld());
        if (pylonSet != null) {
            EnergyField energyField = pylonSet.getEnergyField();
            
            double x = location.getX();
            double z = location.getZ();
            
            double drain = pylonSet.energyDrainAtPoint(x, z);
            double energy = energyField.energyAt(x, z);
            double chance = (1.0 - drain) * energy * max_reduction + (1.0 - max_reduction);
            
            if (chance < 1.0) {
                if (plugin.getPluginRandom().nextDouble() >= chance) {
                    return true;
                }
            }
            
            if (location.getWorld().getHighestBlockYAt(location) == location.getY()) {
                // Sky is visible
                pylonSet.divideBonusXp(x, z, bonusXp);
            }
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void growBlock(BlockGrowEvent e) {
        if (shouldCancelGrowth(e.getBlock().getLocation(), growthReductionMax, cropGrowthBonusXp)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void blockSpread(BlockSpreadEvent e) {
        int sourceTypeId = e.getSource().getTypeId();
        if (sourceTypeId == Material.GRASS.getId() || sourceTypeId == Material.MYCEL.getId()) {
            if (shouldCancelGrowth(e.getBlock().getLocation(), grassReductionMax, 0.0)) {
                // Don't grow
                e.setCancelled(true);
                
                if (plugin.getPluginRandom().nextDouble() < grassDiebackMax) {
                    // Kill the source grass
                    e.getSource().setType(Material.DIRT);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void growStructure(StructureGrowEvent e) {
        if (e.isFromBonemeal()) {
            return;
        }
        
        if (shouldCancelGrowth(e.getLocation(), treeReductionMax, treeGrowthBonusXp)) {
            e.setCancelled(true);
        }
    }
}
