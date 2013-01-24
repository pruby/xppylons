package com.untamedears.xppylons.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;

import com.untamedears.xppylons.XpPylons;
import com.untamedears.xppylons.PylonSet;
import com.untamedears.xppylons.Pylon;
import com.untamedears.xppylons.EnergyField;

public class FishReduction implements Listener {
    private XpPylons plugin;
    private double fishReductionMax;
    
    public FishReduction(XpPylons plugin) {
        this.plugin = plugin;
        fishReductionMax = plugin.getConfig().getDouble("negativeEffects.fishReduction");
    }
    
    @EventHandler(ignoreCancelled = true)
    public void fishing(PlayerFishEvent e) {
        if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            PylonSet pylonSet = plugin.getPylons(e.getPlayer().getWorld());
            if (pylonSet != null) {
                EnergyField energyField = pylonSet.getEnergyField();
                double drain = pylonSet.energyDrainAtPoint(e.getPlayer().getLocation().getX(), e.getPlayer().getLocation().getZ());
                double energy = energyField.energyAt(e.getPlayer().getLocation().getX(), e.getPlayer().getLocation().getZ());
                double chance = (1.0 - drain) * energy * fishReductionMax;
                
                if (chance < 1.0) {
                    if (plugin.getPluginRandom().nextDouble() >= chance) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}
