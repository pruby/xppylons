package com.untamedears.xppylons.task;

import java.util.Collection;

import com.untamedears.xppylons.XpPylons;
import com.untamedears.xppylons.PylonSet;
import com.untamedears.xppylons.Pylon;
import org.bukkit.World;

public class RecalculateXPRate implements Runnable {
    private XpPylons plugin;
    private Collection<Pylon> pylons;
    
    public RecalculateXPRate(XpPylons plugin) {
        this.plugin = plugin;
    }
    
    public RecalculateXPRate(XpPylons plugin, Collection<Pylon> pylons) {
        this.plugin = plugin;
        this.pylons = pylons;
    }
    
    public void run() {
        if (pylons == null) {
            recalculateAllPylons();
        } else {
            recalculatePylonList(pylons);
        }
    }
    
    private void recalculateAllPylons() {
        for (World world : plugin.pylonWorlds()) {
            PylonSet pylonSet = plugin.getPylons(world);
            pylons = pylonSet.getPylonSet();
            
            recalculatePylonList(pylons);
        }
    }
    
    private void recalculatePylonList(Collection<Pylon> pylons) {
        for (Pylon pylon : pylons) {
            pylon.recalculateXpRate();
        }
    }
}
