package com.untamedears.xppylons.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.world.StructureGrowEvent;

import com.untamedears.xppylons.XpPylons;

public class GrowthListener implements Listener {
    private XpPylons plugin;
    
    @EventHandler(ignoreCancelled = true)
    public void growBlock(BlockGrowEvent event) {
        //event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void growStructure(StructureGrowEvent event) {
        //event.setCancelled(true);
    }
}
