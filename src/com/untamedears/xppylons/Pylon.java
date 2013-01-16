package com.untamedears.xppylons;

import java.util.ArrayList;
import rtree.AABB;
import rtree.BoundedObject;

public class Pylon implements BoundedObject {
    private PylonSet cluster;
    private double x, y, z;
    
    public Pylon(PylonSet cluster, double x, double y, double z) {
        this.cluster = cluster;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public AABB getBounds() {
        AABB boundingBox = new AABB();
        boundingBox.setMinCorner(x - 2, 0, z - 2);
        boundingBox.setMaxCorner(x + 2, 256, z + 2);
        return boundingBox;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getZ() {
        return z;
    }
    
    public double getRadiusOfEffect() {
        return 100.0;
    }
    
    public class EffectBounds implements BoundedObject {
        private Pylon pylon;
        
        public EffectBounds(Pylon pylon) {
            this.pylon = pylon;
        }
        
        public AABB getBounds() {
            double radius = pylon.getRadiusOfEffect();
            AABB boundingBox = new AABB();
            boundingBox.setMinCorner(x - radius, 0, z - radius);
            boundingBox.setMaxCorner(x + radius, 256, z + radius);
            return boundingBox;
        }
    }
}
