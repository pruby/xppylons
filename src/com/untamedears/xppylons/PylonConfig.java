package com.untamedears.xppylons;

import org.bukkit.configuration.ConfigurationSection;

public class PylonConfig {
    private double maximumRadius;
    private int maxPylonHeight;
    private double averageXpPerLevel;
    private double peakXpPerLevel;
    private double pylonEfficiency;
    
    public PylonConfig(ConfigurationSection section) {
        maximumRadius = section.getDouble("maximumRadius");
        maxPylonHeight = section.getInt("maxPylonHeight");
        averageXpPerLevel = section.getDouble("averageXpPerLevel");
        peakXpPerLevel = averageXpPerLevel * 2;
        pylonEfficiency = section.getDouble("pylonEfficiency");
    }
    
    public double getMaximumRadius() {
        return maximumRadius;
    }
    
    public int getMaxPylonHeight() {
        return maxPylonHeight;
    }
    
    public double getAverageXpPerLevel() {
        return averageXpPerLevel;
    }
    
    public double getPylonEfficiency() {
        return pylonEfficiency;
    }
}
