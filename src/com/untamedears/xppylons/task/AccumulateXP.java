package com.untamedears.xppylons.task;

import com.untamedears.xppylons.XpPylons;
import com.untamedears.xppylons.PylonSet;
import com.untamedears.xppylons.Pylon;
import org.bukkit.World;

public class AccumulateXP implements Runnable {
    private XpPylons plugin;
    
    public AccumulateXP(XpPylons plugin) {
        this.plugin = plugin;
    }
    
    public void run() {
        for (World world : plugin.pylonWorlds()) {
            PylonSet pylonSet = plugin.getPylons(world);
            for (Pylon pylon : pylonSet.getPylonSet()) {
                pylon.accumulateXp();
            }
        }
    }
}
