package com.untamedears.xppylons;

import java.util.ArrayList;

public class PylonSet {
    private XpPylons plugin;
    private ArrayList<Pylon> pylons;
    
    public PylonSet(XpPylons plugin) {
        this.plugin = plugin;
        pylons = new ArrayList<Pylon>();
    }
    
    public void addPylon(Pylon pylon) {
        pylons.add(pylon);
    }
    
    public void addPylon(double x, double y, double z) {
        pylons.add(new Pylon(this, x, y, z));
    }
}
