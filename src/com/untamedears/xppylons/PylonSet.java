package com.untamedears.xppylons;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;

import rtree.BoundedObject;
import rtree.RTree;

public class PylonSet {
    private XpPylons plugin;
    private Set<Pylon> pylons;
    private RTree pylonStructures;
    private RTree pylonInfluences;
    
    public PylonSet(XpPylons plugin) {
        this.plugin = plugin;
        pylons = new HashSet<Pylon>();
        pylonStructures = new RTree(2, 4);
        pylonInfluences = new RTree(2, 4);
    }
    
    public void addPylon(Pylon pylon) {
        pylons.add(pylon);
        pylonStructures.insert(pylon);
        pylonInfluences.insert(pylon.getInfluence());
    }
    
    public void addPylon(double x, double y, double z) {
        addPylon(new Pylon(this, x, y, z));
    }
    
    public void removePylon(Pylon pylon) {
        pylonInfluences.remove(pylon.getInfluence());
        pylonStructures.remove(pylon);
        pylons.remove(pylon);
    }
    
    public Pylon pylonAt(double x, double y, double z) {
        BoundedObject pylonFound = pylonStructures.queryOne(x, y, z);
        return (Pylon) pylonFound;
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
}
