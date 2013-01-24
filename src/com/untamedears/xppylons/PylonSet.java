package com.untamedears.xppylons;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;

import com.untamedears.xppylons.rtree.BoundedObject;
import com.untamedears.xppylons.rtree.RTree;
import com.untamedears.xppylons.rtree.AABB;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public class PylonSet {
    private XpPylons plugin;
    private PylonConfig config;
    private EnergyField field;
    private Set<Pylon> pylons;
    private RTree pylonStructures;
    private RTree pylonInfluences;
    
    public PylonSet(XpPylons plugin, EnergyField field, PylonConfig config) {
        this.plugin = plugin;
        this.field = field;
        this.config = config;
        
        pylons = new HashSet<Pylon>();
        pylonStructures = new RTree(2, 4);
        pylonInfluences = new RTree(2, 4);
    }
    
    public XpPylons getPlugin() {
        return plugin;
    }
    
    public synchronized void addPylon(Pylon pylon) {
        pylons.add(pylon);
        pylonStructures.insert(pylon);
        pylonInfluences.insert(pylon.getInfluence());
        
        recalculateAffected(pylon);
    }
    
    public synchronized void addPylon(int x, int y, int z, int height) {
        addPylon(new Pylon(this, x, y, z, height));
    }
    
    public synchronized void removePylon(Pylon pylon) {
        pylonInfluences.remove(pylon.getInfluence());
        pylonStructures.remove(pylon);
        pylons.remove(pylon);
        
        recalculateAffected(pylon);
    }
    
    public void recalculateAffected(Pylon pylon) {
        List<BoundedObject> overlaps = new LinkedList<BoundedObject>();
        pylonInfluences.query(overlaps, pylon.getInfluence().getBounds());
        
        for (BoundedObject overlap : overlaps) {
            assert(overlap instanceof Pylon.EffectBounds);
            Pylon affectedPylon = ((Pylon.EffectBounds) overlap).getPylon();
            affectedPylon.recalculateXpRate();
        }
    }
    
    public EnergyField getEnergyField() {
        return field;
    }
    
    public synchronized Set<Pylon> getPylonSet() {
        return new HashSet<Pylon>(pylons);
    }
    
    public synchronized Pylon pylonAt(int x, int y, int z) {
        List<Pylon> pylonsAroundPoint = pylonsAround(x, y, z);
        for (Pylon pylon : pylonsAroundPoint) {
            if (((int) pylon.getX()) == x && ((int) pylon.getY()) == y && ((int) pylon.getZ()) == z) {
                return pylon;
            }
        }
        return null;
    }
    
    public synchronized List<Pylon> pylonsAround(double x, double y, double z) {
        LinkedList<BoundedObject> pylonsAsBoxes = new LinkedList<BoundedObject>();
        
        pylonStructures.query(pylonsAsBoxes, x, y, z);
        
        LinkedList<Pylon> pylons = new LinkedList<Pylon>();
        for (BoundedObject pylonAsBox : pylonsAsBoxes) {
            assert(pylonAsBox instanceof Pylon);
            
            pylons.add((Pylon) pylonAsBox);
        }
        
        return pylons;
    }
    
    public synchronized List<Pylon> pylonsAround(AABB box) {
        LinkedList<BoundedObject> pylonsAsBoxes = new LinkedList<BoundedObject>();
        
        pylonStructures.query(pylonsAsBoxes, box);
        
        LinkedList<Pylon> pylons = new LinkedList<Pylon>();
        for (BoundedObject pylonAsBox : pylonsAsBoxes) {
            assert(pylonAsBox instanceof Pylon);
            
            pylons.add((Pylon) pylonAsBox);
        }
        
        return pylons;
    }
    
    public synchronized List<Pylon> pylonsInfluencing(double x, double z) {
        LinkedList<BoundedObject> influences = new LinkedList<BoundedObject>();
        LinkedList<Pylon> pylonsInfluencing = new LinkedList<Pylon>();
        
        pylonInfluences.query(influences, x, 60, z);
        
        for (BoundedObject influence : influences) {
            assert(influence instanceof Pylon.EffectBounds);
            
            Pylon.EffectBounds bounds = (Pylon.EffectBounds) influence;
            if (bounds.affects(x, z)) {
              Pylon affectingPylon = bounds.getPylon();
              pylonsInfluencing.add(affectingPylon);
            }
        }
        
        return pylonsInfluencing;
    }
    
    public double energyDrainAtPoint(double x, double z) {
        double residual = 1.0;
        
        for (Pylon pylon : pylonsInfluencing(x, z)) {
            double strengthAtPoint = pylon.getInfluence().getStrengthAt(x, z);
            residual = residual * (1.0 - strengthAtPoint);
        }
        
        return 1.0 - residual;
    }
    
    public PylonConfig getConfig() {
        return config;
    }
    
    public synchronized void savePylons(ObjectOutputStream output) throws IOException {
        int numPylons = pylons.size();
        output.writeInt(numPylons);
        for (Pylon p : pylons) {
            p.writePylon(output);
        }
    }
    
    public synchronized void readPylons(ObjectInputStream input) throws IOException {
        int numPylons = input.readInt();
        boolean seenError = false;
        for (int i = 0; i < numPylons; i++) {
            try {
                Pylon.readPylon(this, input);
            } catch (IOException e) {
                if (!seenError) {
                    plugin.severe("IO failure reading pylons");
                    e.printStackTrace();
                    seenError = true;
                }
            }
        }
    }
}
