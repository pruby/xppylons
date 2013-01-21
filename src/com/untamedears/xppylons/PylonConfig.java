package com.untamedears.xppylons;

import org.bukkit.configuration.ConfigurationSection;

public class PylonConfig {
    private double maximumRadius;
    private int maxPylonHeight;
    private int minPylonHeight;
    private int samplesPerLevel;
    private double maxStoredXp;
    private int xpCollectionInterval;
    private int xpCalculationInterval;
    private double targetXpPerLevelDay;
    private double pylonDepletion;
    private int xpPerBottle;
    
    public PylonConfig(ConfigurationSection section) {
        maximumRadius = section.getDouble("maximumRadius");
        maxPylonHeight = section.getInt("maxPylonHeight");
        minPylonHeight = section.getInt("minPylonHeight");
        samplesPerLevel = section.getInt("samplesPerLevel");
        xpCalculationInterval = section.getInt("xpCalculationInterval");
        xpCollectionInterval = section.getInt("xpCollectionInterval");
        targetXpPerLevelDay = section.getDouble("targetXpPerLevelDay");
        maxStoredXp = section.getDouble("maxStoredXp");
        pylonDepletion = section.getDouble("pylonDepletion");
        maxStoredXp = section.getDouble("maxStoredXp");
        xpPerBottle = section.getInt("xpPerBottle");
    }
    
    public double getMaximumRadius() {
        return maximumRadius;
    }
    
    public int getMaxPylonHeight() {
        return maxPylonHeight;
    }
    
    public int getMinPylonHeight() {
        return minPylonHeight;
    }
    
    public int getSamplesPerLevel() {
        return samplesPerLevel;
    }
    
    public double getTargetXpPerLevelDay() {
        return targetXpPerLevelDay;
    }
    
    public int getXpCollectionInterval() {
        return xpCollectionInterval;
    }
    
    public int getXpCalculationInterval() {
        return xpCalculationInterval;
    }
    
    public double getMaxStoredXp() {
        return maxStoredXp;
    }
    
    public int getXpPerBottle() {
        return xpPerBottle;
    }
    
    public double getTargetXpPerLevelInterval() {
        double secondsPerDay = 60 * 60 * 24;
        double intervalsPerDay = secondsPerDay / xpCollectionInterval;
        return targetXpPerLevelDay / intervalsPerDay;
    }
    
    public double getPylonDepletion() {
        return pylonDepletion;
    }
}
