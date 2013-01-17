package com.untamedears.xppylons;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;

import rtree.BoundedObject;
import rtree.RTree;

public class PylonSet {
    private XpPylons plugin;
    private PylonConfig config;
    private Set<Pylon> pylons;
    private RTree pylonStructures;
    private RTree pylonInfluences;
    
    public PylonSet(XpPylons plugin, PylonConfig config) {
        this.plugin = plugin;
        this.config = config;
        
        pylons = new HashSet<Pylon>();
        pylonStructures = new RTree(2, 4);
        pylonInfluences = new RTree(2, 4);
    }
    
    public void addPylon(Pylon pylon) {
        pylons.add(pylon);
        pylonStructures.insert(pylon);
        pylonInfluences.insert(pylon.getInfluence());
    }
    
    public void addPylon(int x, int y, int z, int height) {
        addPylon(new Pylon(this, x, y, z, height));
    }
    
    public void removePylon(Pylon pylon) {
        pylonInfluences.remove(pylon.getInfluence());
        pylonStructures.remove(pylon);
        pylons.remove(pylon);
    }
    
    public Pylon pylonAt(int x, int y, int z) {
        List<Pylon> pylonsAroundPoint = pylonsAround(x, y, z);
        for (Pylon pylon : pylonsAroundPoint) {
            if (((int) pylon.getX()) == x && ((int) pylon.getY()) == y && ((int) pylon.getZ()) == z) {
                return pylon;
            }
        }
        return null;
    }
    
    public List<Pylon> pylonsAround(double x, double y, double z) {
        LinkedList<BoundedObject> pylonsAsBoxes = new LinkedList<BoundedObject>();
        
        pylonStructures.query(pylonsAsBoxes, x, y, z);
        
        LinkedList<Pylon> pylons = new LinkedList<Pylon>();
        for (BoundedObject pylonAsBox : pylonsAsBoxes) {
            pylons.add((Pylon) pylonAsBox);
        }
        
        return pylons;
    }
    
    public List<Pylon> pylonsInfluencing(double x, double z) {
        LinkedList<BoundedObject> influences = new LinkedList<BoundedObject>();
        LinkedList<Pylon> pylonsInfluencing = new LinkedList<Pylon>();
        
        pylonInfluences.query(influences, x, 60, z);
        
        for (BoundedObject influence : influences) {
            Pylon.EffectBounds bounds = (Pylon.EffectBounds) influence;
            if (bounds.affects(x, z)) {
              Pylon affectingPylon = bounds.getPylon();
              pylonsInfluencing.add(affectingPylon);
            }
        }
        
        return pylonsInfluencing;
    }
    
    public PylonConfig getConfig() {
        return config;
    }
}
