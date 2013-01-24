package com.untamedears.xppylons.listener;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.Location;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.untamedears.xppylons.XpPylons;
import com.untamedears.xppylons.PylonSet;
import com.untamedears.xppylons.Pylon;
import com.untamedears.xppylons.EnergyField;

public class Divining implements Listener {
    public final static Set<Integer> diviningBlocks;

    static {
        Set<Integer> tempBlocks = new HashSet<Integer>();
        tempBlocks.add(Material.LEAVES.getId());
        tempBlocks.add(Material.GRASS.getId());
        tempBlocks.add(Material.LONG_GRASS.getId());
        tempBlocks.add(Material.YELLOW_FLOWER.getId());
        tempBlocks.add(Material.RED_ROSE.getId());
        tempBlocks.add(Material.CACTUS.getId());
        tempBlocks.add(Material.CROPS.getId());
        tempBlocks.add(Material.PUMPKIN_STEM.getId());
        tempBlocks.add(Material.MELON_STEM.getId());
        tempBlocks.add(Material.SUGAR_CANE_BLOCK.getId());
        tempBlocks.add(Material.WATER_LILY.getId());
        tempBlocks.add(Material.CARROT.getId());
        tempBlocks.add(Material.POTATO.getId());
        tempBlocks.add(Material.SAPLING.getId());
        diviningBlocks = Collections.unmodifiableSet(tempBlocks);
    }

    private XpPylons plugin;
    private int diviningItemId;
    
    public Divining(XpPylons plugin) {
        this.plugin = plugin;
        diviningItemId = plugin.getConfig().getInt("items.divining");
    }
    
    // Drained messages used for energy drained below base level
    public static final double[] drainedDiviningThresholds = {
        0.9,
        0.8,
        0.6,
        0.4,
        0.2,
        0.0
    };
    public static final String[] drainedDiviningMessages = {
        "Growth seems slow here",
        "Growing things seem less green here",
        "Plantlife seems strained here",
        "There is little new growth around here",
        "Plants here are withering",
        "Plants here seem on the verge of death"
    };
    
    // Overage messages used for areas with a surplus over base level
    public static final double[] overageDiviningThresholds = {
        0.9,
        0.8,
        0.6,
        0.4,
        0.2,
        0.0
    };
    
    public static final String[] overageDiviningMessages = {
        "Plants here are glowing with health",
        "Plantlife here is in perfect health",
        "Plants here are sprawling vigorously",
        "Plantlife here is very healthy",
        "The plants here are green and healthy",
        "Plants here are growing normally"
    };
    
    public void doDivining(Player player, Location diviningLocation) {
        EnergyField field = plugin.getEnergyField(player.getWorld()); 
        
        double remainingEnergy;
        if (field != null) {
            double energyHere = field.energyAt(diviningLocation.getX(), diviningLocation.getZ());
            double drainHere = plugin.getPylons(player.getWorld()).energyDrainAtPoint(player.getLocation().getX(), player.getLocation().getZ());
            
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
            double peakEnergy = plugin.getPylons(player.getWorld()).getEnergyField().getMaxTimesBackground();
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
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        try {
            int materialInHand = e.getMaterial().getId() ;
            
            if ((e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.hasBlock() && materialInHand == diviningItemId) {
                // Divining
                if (Divining.diviningBlocks.contains(e.getClickedBlock().getTypeId())) {
                    Location diviningPoint = e.getClickedBlock().getLocation();
                    doDivining(e.getPlayer(), diviningPoint);
                }
            }
        } catch (RuntimeException ex) {
            plugin.severe("Error handling divining");
            ex.printStackTrace();
        }
    }
}
