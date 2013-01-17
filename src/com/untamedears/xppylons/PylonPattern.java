package com.untamedears.xppylons;

import java.util.Map;
import java.util.HashMap;
import org.bukkit.configuration.ConfigurationSection;

public class PylonPattern {
  private int width;
  private int height;
  private Map<String, Integer> materials;
  private int[][] basePattern;
  private int[][] roofPattern;
  private int[][] levelPattern;
  
  public PylonPattern(ConfigurationSection section) {
      materials = new HashMap<String, Integer>();
      
      for (Map.Entry<String, Object> entry : section.getConfigurationSection("materials").getValues(false).entrySet()) {
          Object value = entry.getValue();
          if (value instanceof Integer) {
              materials.put(entry.getKey(), (Integer) value);
          } else if (value instanceof String) {
              materials.put(entry.getKey(), Integer.parseInt((String) value));
          }
      }
      
      String basePatternString = section.getString("patterns.basePattern");
      width = 0;
  }
}