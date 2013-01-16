package com.untamedears.xppylons;

import org.bukkit.configuration.ConfigurationSection;

public class PylonConfig {
    private double maximumRadius;
    private int maxPylonHeight;
    private double averageXpPerLevel;
    private double peakXpPerLevel;
    private double pylonEfficiency;
    
    public PylonConfig(double maximumRadius, int maxPylonHeight, double averageXpPerLevel, double pylonEfficiency) {
        this.maximumRadius = maximumRadius;
        this.maxPylonHeight = maxPylonHeight;
        this.averageXpPerLevel = averageXpPerLevel;
        this.peakXpPerLevel = averageXpPerLevel * 2;
        this.pylonEfficiency = pylonEfficiency;
    }
    
    public static PylonConfig loadConfig(ConfigurationSection section) {
        double maximumRadius = section.getDouble("maximumRadius");
        int maxPylonHeight = section.getInt("maxPylonHeight");
        double averageXpPerLevel = section.getDouble("averageXpPerLevel");        
        double pylonEfficiency = section.getDouble("pylonEfficiency");
        
        return new PylonConfig(maximumRadius, maxPylonHeight, averageXpPerLevel, pylonEfficiency);
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
