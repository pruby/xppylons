package com.untamedears.xppylons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import com.untamedears.xppylons.rtree.AABB;
import com.untamedears.xppylons.rtree.BoundedObject;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Pylon implements BoundedObject {
    private PylonSet cluster;
    private int x, y, z;
    private double radius;
    private double xpRate;
    private int height;
    private double xp;
    private Pylon.EffectBounds influence;
    
    public Pylon(PylonSet cluster, int x, int y, int z, int height) {
        this(cluster, x, y, z, height, 0.0);
    }
    
    public Pylon(PylonSet cluster, int x, int y, int z, int height, double xp) {
        this.cluster = cluster;
        this.x = x;
        this.y = y;
        this.z = z;
        this.height = height;
        this.xp = xp;
        this.influence = new Pylon.EffectBounds(this);
        
        double maxHeight = cluster.getConfig().getMaxPylonHeight();
        double maxRadius = cluster.getConfig().getMaximumRadius();
        this.radius = Math.sqrt(height / maxHeight) * maxRadius;
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
    
    public PylonSet getCluster() {
        return cluster;
    }
    
    public double getRadiusOfEffect() {
        return radius;
    }
    
    public Pylon.EffectBounds getInfluence() {
        return influence;
    }
    
    public void dispenseXp(Player player) {
        int xpPerBottle = cluster.getConfig().getXpPerBottle();
        int maxBottles = (int) (xp / xpPerBottle);
        ItemStack stack = player.getItemInHand();
        
        int amount = stack.getAmount();
        if (amount > maxBottles) {
            amount = maxBottles;
        }
        
        if (amount > 0) {
            xp -= amount * xpPerBottle;
            
            /* Take bottles */
            stack.setAmount(stack.getAmount()-amount);
            player.setItemInHand(stack);
            
            /* Give XP bottles. If inventory full, drop in world. */
            PlayerInventory inventory = player.getInventory();
            HashMap<Integer, ItemStack> spillover = inventory.addItem(new ItemStack(Material.EXP_BOTTLE, amount));
            World world = player.getWorld();
            Location location = player.getLocation();
            for (ItemStack overStack : spillover.values()) {
                world.dropItem(location, overStack);
            }
        } else {
            player.sendMessage("Not enough XP to fill bottles");
        }
        
        // Know this is deprecated, but without it has major UI glitches with inventory out of sync.
        player.updateInventory();
    }
    
    public void recalculateXpRate() {
        int numSamples = height * cluster.getConfig().getSamplesPerLevel();
        double sampleScale = cluster.getConfig().getTargetXpPerLevelInterval() / cluster.getConfig().getSamplesPerLevel();
        xpRate = 0.0;
        Random random = cluster.getPlugin().getPluginRandom();
        for (int i = 0; i < numSamples; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = random.nextDouble() * radius;
            double dx = Math.cos(angle) * distance;
            double dz = Math.sin(angle) * distance;
            double share = getInfluence().getShareAt(x + dx, z + dz);
            double energy = cluster.getEnergyField().energyAt(x + dx, z + dz);
            xpRate += share * energy * sampleScale;
        }
    }
    
    public double getXpRate() {
        return xpRate;
    }
    
    public void accumulateXp() {
        xp += xpRate;
        if (xp > cluster.getConfig().getMaxStoredXp()) {
            xp = cluster.getConfig().getMaxStoredXp();
        }
    }
    
    public double getXp() {
        return xp;
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
            return getStrengthAt(x, z) > 0;
        }
        
        public double getStrengthAt(double x, double z) {
            double radius = pylon.getRadiusOfEffect();
            double radiusSq = radius * radius;
            double dx = x - pylon.getX();
            double dz = z - pylon.getZ();
            double distSq = dx * dx + dz * dz;
            if (distSq > radiusSq) {
                return 0;
            } else {
                double dist = Math.sqrt(distSq);
                double depletionScale = pylon.getCluster().getConfig().getPylonDepletion();
                double strength = depletionScale * Math.sqrt(1.0 - (dist / radius));
                return strength;
            }
        }
        
        public double getShareAt(double x, double z) {
            double totalStrength = 0.0;
            double residual = 1.0;
            
            for (Pylon other : pylon.getCluster().pylonsInfluencing(x, z)) {
                double strengthAtPoint = other.getInfluence().getStrengthAt(x, z);
                totalStrength += strengthAtPoint;
                residual = residual * (1.0 - strengthAtPoint);
            }
            
            if (totalStrength <= 0.0 || residual <= 0.0 || residual > 1.0) {
                return 0.0;
            }
            
            double draw = 1.0 - residual;
            double share = (getStrengthAt(x, z) / totalStrength) * draw;
            return share;
        }
        
        public AABB getBounds() {
            double radius = pylon.getRadiusOfEffect();
            AABB boundingBox = new AABB();
            boundingBox.setMinCorner(x - radius, 0, z - radius);
            boundingBox.setMaxCorner(x + radius, 256, z + radius);
            return boundingBox;
        }
    }
    
    // Read/write. Note: does not meet Serializable template as requires link to parent.
    
    public void writePylon(ObjectOutputStream out)
        throws IOException {
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(z);
        out.writeInt(height);
        out.writeDouble(xp);
    }
    
    public static Pylon readPylon(PylonSet cluster, ObjectInputStream in)
        throws IOException {
        int x = in.readInt();
        int y = in.readInt();
        int z = in.readInt();
        int height = in.readInt();
        double xp = in.readDouble();
        
        Pylon pylon = new Pylon(cluster, x, y, z, height, xp);
        cluster.addPylon(pylon);
        return pylon;
    }
}
