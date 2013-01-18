package com.untamedears.xppylons;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.noise.NoiseGenerator;
import org.bukkit.util.noise.SimplexNoiseGenerator;
import java.util.Random;

public class EnergyField {
    private long seed;
    private double timeScale;
    private double longRangeScale;
    private double midRangeScale;
    private double maxTimesBackground;
    
    private World world;
    private NoiseGenerator longRangeVariation;
    private NoiseGenerator midRangeVariation;
    
    public EnergyField(World world, ConfigurationSection config) {
        this.world = world;
        
        seed = config.getLong("seed");
        longRangeScale = config.getDouble("longRangeDistance");
        midRangeScale = config.getDouble("midRangeDistance");
        maxTimesBackground = config.getDouble("maxTimesBackground");
        
        Random initGen = new Random(seed);
        longRangeVariation = new SimplexNoiseGenerator(initGen);
        midRangeVariation = new SimplexNoiseGenerator(initGen);
    }
    
    public double energyAt(int x, int z) {
        double time = (double) world.getTime();
        double longRangeNoise = longRangeVariation.noise(x / longRangeScale, z / longRangeScale, time / timeScale);
        double midRangeNoise = midRangeVariation.noise(x / midRangeScale, z / midRangeScale, time / timeScale);
        
        double longRangeMultiplier = longRangeVariation + 1.0; // 0 to 2 times
        double midRangeExtra = (midRangeNoise + 1.0) * ((maxTimesBackground - 1.0) / 2.0);
        
        return longRangeMultiplier * midRangeExtra;
    }
}
