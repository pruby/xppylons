package com.untamedears.xppylons;

import java.util.ArrayList;
import rtree.AABB;
import rtree.BoundedObject;

public class Pylon implements BoundedObject {
    private PylonSet cluster;
    private int x, y, z;
    private int height;
    private Pylon.EffectBounds influence;
    
    public Pylon(PylonSet cluster, int x, int y, int z, int height) {
        this.cluster = cluster;
        this.x = x;
        this.y = y;
        this.z = z;
        this.height = height;
        this.influence = new Pylon.EffectBounds(this);
    }
    
    public AABB getBounds() {
        AABB boundingBox = new AABB();
        boundingBox.setMinCorner((double) x - 2, (double) y - 1, (double) z - 2);
        boundingBox.setMaxCorner((double) x + 2, (double) y + 2 + height, (double) z + 2);
        return boundingBox;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getZ() {
        return z;
    }
    
    public int getHeight() {
        return height;
    }
    
    public double getRadiusOfEffect() {
        double maxHeight = cluster.getConfig().getMaxPylonHeight();
        double maxRadius = cluster.getConfig().getMaximumRadius();
        return Math.sqrt(height / maxHeight) * maxRadius;
    }
    
    public Pylon.EffectBounds getInfluence() {
        return influence;
    }
    
    public class EffectBounds implements BoundedObject {
        private Pylon pylon;
        
        public EffectBounds(Pylon pylon) {
            this.pylon = pylon;
        }
        
        public Pylon getPylon() {
            return pylon;
        }
        
        public boolean affects(double x, double z) {
            double radius = pylon.getRadiusOfEffect();
            return x * x + z * z <= radius * radius;
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
