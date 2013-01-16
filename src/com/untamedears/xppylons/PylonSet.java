package com.untamedears.xppylons;

import java.util.ArrayList;

public class PylonSet {
    private XpPylons plugin;
    private ArrayList<Pylon> pylons;
    
    public PylonSet(XpPylons plugin) {
        this.plugin = plugin;
    }
    
    public void addPylon(Pylon pylon) {
        pylons.add(pylon);
    }
}
