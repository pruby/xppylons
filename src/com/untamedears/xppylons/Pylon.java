package com.untamedears.xppylons;

import java.util.ArrayList;

public class Pylon {
    private PylonSet cluster;
    private double x, y, z;
    
    public Pylon(PylonSet cluster, double x, double y, double z) {
        this.cluster = cluster;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
