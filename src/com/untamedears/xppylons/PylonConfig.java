package com.untamedears.xppylons;

import org.bukkit.configuration.ConfigurationSection;

public class PylonConfig {
    private double maximumRadius;
    private double averageXpPerLevel;
    private double peakXpPerLevel;
    private double pylonEfficiency;
    
    public PylonConfig(double maximumRadius, double averageXpPerLevel, double pylonEfficiency) {
        this.maximumRadius = maximumRadius;
        this.averageXpPerLevel = averageXpPerLevel;
        this.peakXpPerLevel = averageXpPerLevel * 2;
        this.pylonEfficiency = pylonEfficiency;
    }
    
    public static PylonConfig loadConfig(ConfigurationSection section) {
        double maximumRadius = section.getDouble("maximumRadius");
        double averageXpPerLevel = section.getDouble("averageXpPerLevel");        
        double pylonEfficiency = section.getDouble("pylonEfficiency");
        
        return new PylonConfig(maximumRadius, averageXpPerLevel, pylonEfficiency);
    }
}
