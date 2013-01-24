package com.untamedears.xppylons.listener;

import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Monster;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;

import com.untamedears.xppylons.XpPylons;
import com.untamedears.xppylons.PylonSet;
import com.untamedears.xppylons.Pylon;
import com.untamedears.xppylons.EnergyField;

public class PassiveMobReduction implements Listener {
    private XpPylons plugin;
    private double naturalSpawnReductionMax;
    private double eggSpawnReductionMax;
    private double breedingReductionMax;
    
    public PassiveMobReduction(XpPylons plugin) {
        this.plugin = plugin;
        naturalSpawnReductionMax = plugin.getConfig().getDouble("negativeEffects.naturalSpawnReduction");
        eggSpawnReductionMax = plugin.getConfig().getDouble("negativeEffects.eggSpawnReduction");
        breedingReductionMax = plugin.getConfig().getDouble("negativeEffects.breedingReduction");
    }
    
    public boolean shouldCancelSpawn(Location location, double max_reduction) {
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
    
    // No isPassiveMob() method on entities, so fabricating from their interfaces
    public boolean isPassiveMob(Entity entity) {
        return((entity instanceof Creature) && !(entity instanceof Monster));
    }
    
    @EventHandler(ignoreCancelled = true)
    public void spawnEvent(CreatureSpawnEvent e) {
        Entity entity = e.getEntity();
        if (isPassiveMob(entity)) {
            Location location = entity.getLocation();
            switch(e.getSpawnReason()) {
                case NATURAL:
                    if (shouldCancelSpawn(location, naturalSpawnReductionMax)) {
                        e.setCancelled(true);
                    }
                    break;
                case EGG:
                    if (shouldCancelSpawn(location, eggSpawnReductionMax)) {
                        e.setCancelled(true);
                    }
                    break;
                case BREEDING:
                    if (shouldCancelSpawn(location, breedingReductionMax)) {
                        e.setCancelled(true);
                    }
                    break;
            }
        }
    }
}
