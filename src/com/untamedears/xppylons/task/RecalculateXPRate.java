package com.untamedears.xppylons.task;

import com.untamedears.xppylons.XpPylons;
import com.untamedears.xppylons.PylonSet;
import com.untamedears.xppylons.Pylon;
import org.bukkit.World;

public class RecalculateXPRate implements Runnable {
    private XpPylons plugin;
    
    public RecalculateXPRate(XpPylons plugin) {
        this.plugin = plugin;
    }
    
    public void run() {
        for (World world : plugin.pylonWorlds()) {
            PylonSet pylonSet = plugin.getPylons(world);
            for (Pylon pylon : pylonSet.getPylonSet()) {
                pylon.recalculateXpRate();
            }
        }
    }
}
