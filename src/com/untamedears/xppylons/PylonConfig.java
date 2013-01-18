package com.untamedears.xppylons;

import org.bukkit.configuration.ConfigurationSection;

public class PylonConfig {
    private double maximumRadius;
    private int maxPylonHeight;
    private int minPylonHeight;
    private double targetXpPerLevelDay;
    private double pylonDepletion;
    
    public PylonConfig(ConfigurationSection section) {
        maximumRadius = section.getDouble("maximumRadius");
        maxPylonHeight = section.getInt("maxPylonHeight");
        minPylonHeight = section.getInt("minPylonHeight");
        targetXpPerLevelDay = section.getDouble("targetXpPerLevelDay");
        pylonDepletion = section.getDouble("pylonDepletion");
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
    
    public double getTargetXpPerLevelDay() {
        return targetXpPerLevelDay;
    }
    
    public double getPylonDepletion() {
        return pylonDepletion;
    }
}
