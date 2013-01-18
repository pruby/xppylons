package com.untamedears.xppylons;

import org.bukkit.configuration.ConfigurationSection;

public class PylonConfig {
    private double maximumRadius;
    private int maxPylonHeight;
    private double targetXpPerLevelDay;
    private double pylonDepletion;
    
    public PylonConfig(ConfigurationSection section) {
        maximumRadius = section.getDouble("maximumRadius");
        maxPylonHeight = section.getInt("maxPylonHeight");
        targetXpPerLevelDay = section.getDouble("targetXpPerLevelDay");
        pylonDepletion = section.getDouble("pylonDepletion");
    }
    
    public double getMaximumRadius() {
        return maximumRadius;
    }
    
    public int getMaxPylonHeight() {
        return maxPylonHeight;
    }
    
    public double getTargetXpPerLevelDay() {
        return targetXpPerLevelDay;
    }
    
    public double getPylonDepletion() {
        return pylonDepletion;
    }
}
