package com.untamedears.xppylons.task;

import com.untamedears.xppylons.XpPylons;

public class SavePylons implements Runnable {
    private XpPylons plugin;
    
    public SavePylons(XpPylons plugin) {
        this.plugin = plugin;
    }
    
    public void run() {
        plugin.info("Saving pylon states");
        plugin.savePylons();
    }
}
