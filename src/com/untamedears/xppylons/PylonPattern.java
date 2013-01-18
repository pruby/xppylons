package com.untamedears.xppylons;

import java.util.Map;
import java.util.HashMap;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

public class PylonPattern {
  private int width;
  private int height;
  private Map<String, Integer> materials;
  private int[][] basePattern;
  private int[][] roofPattern;
  private int[][] levelPattern;
  private int offsetWidth;
  private int offsetHeight;
  
  public PylonPattern(ConfigurationSection section) {
      materials = new HashMap<String, Integer>();
      materials.put("*", -1); // Wildcard
      
      for (Map.Entry<String, Object> entry : section.getConfigurationSection("materials").getValues(false).entrySet()) {
          Object value = entry.getValue();
          if (value instanceof Integer) {
              materials.put(entry.getKey(), (Integer) value);
          } else if (value instanceof String) {
              materials.put(entry.getKey(), Integer.parseInt((String) value));
          }
      }
      
      String basePatternString = section.getString("patterns.base");
      basePattern = loadTemplate(basePatternString);
      
      String roofPatternString = section.getString("patterns.roof");
      roofPattern = loadTemplate(roofPatternString, height, width);
      
      String levelPatternString = section.getString("patterns.level");
      levelPattern = loadTemplate(levelPatternString, height, width);
  }
  
  public int[][] loadTemplate(String template) {
      String lines[] = template.split("\\r?\\n");
      height = lines.length;
      if (height < 3 || height / 2 * 2 == height) {
          throw new RuntimeException("XP Pylon template height must be an odd number of blocks and at least 3");
      }
      offsetHeight = height / 2;
      width = lines[0].length();
      if (width < 3 || width / 2 * 2 == width) {
          throw new RuntimeException("XP Pylon template width must be an odd number of blocks and at least 3");
      }
      offsetWidth = width / 2;
      
      return loadTemplate(template, height, width);
  }
  
  public int[][] loadTemplate(String template, int height, int width) {
      String lines[] = template.split("\\r?\\n");
      
      if (lines.length != height) {
          throw new RuntimeException("Pylon templates must all have the same hight");
      }
      
      int[][] pattern = new int[height][];
      int i, j;
      for (i = 0; i < height; i += 1) {
          if (lines[i].length() != width) {
              throw new RuntimeException("Pylon template lines must all have the same width");
          }
          pattern[i] = new int[width];
          for (j = 0; j < width; j += 1) {
              String mat = lines[i].substring(j, j+1);
              Integer materialId = materials.get(mat);
              if (materialId == null) {
                  throw new RuntimeException("Invalid material " + mat + " in pylon base template");
              } else {
                  pattern[i][j] = materialId.intValue();
              }
          }
      }
      return pattern;
  }
  
  public int getOriginalGlowBlockTypeId() {
      return basePattern[offsetHeight + 1][offsetWidth + 1];
  }
  
  public boolean testBlock(Block block) {
      World world = block.getWorld();
      if (world.getEnvironment() != World.Environment.NORMAL) {
          System.out.println("Wrong environment for pylon");
          return false;
      }
      
      if (block.getTypeId() != materials.get("interaction")) {
          System.out.println("Wrong interaction block");
          return false;
      }
      
      if (block.getY() < 60 || block.getY() > 70) {
          System.out.println("Wrong Y level for pylon");
          return false;
      }
      
      if (!testTemplate(basePattern, block.getWorld(), block.getX(), block.getY() - 1, block.getZ())) {
          System.out.println("Base template mismatch");
          return false;
      }
      
      if (!testTemplate(roofPattern, block.getWorld(), block.getX(), block.getY() + 2, block.getZ())) {
          System.out.println("Roof template mismatch");
          return false;
      }
      
      return true;
  }
  
  public int countLevels(Block block) {
      int towerBase = block.getY() + 3;
      int levels;
      for (levels = 0; levels + towerBase < block.getWorld().getMaxHeight(); levels++) {
          if (!testTemplate(levelPattern, block.getWorld(), block.getX(), levels + towerBase, block.getZ())) {
              return levels;
          }
      }
      return levels;
  }
  
  public boolean checkStructure(World world, Pylon pylon) {
      int x = pylon.getX();
      int y = pylon.getY();
      int z = pylon.getZ();
      
      Block block = world.getBlockAt(x, y, z);
      if (!testBlock(block)) {
          // Damage to base
          return false;
      }
      
      if (countLevels(block) < pylon.getHeight()) {
          // Damage to tower
          return false;
      }
      
      return true;
  }
  
  private boolean testTemplate(int[][] template, World world, int x, int y, int z) {
      int dx, dz;
      for (dz = 0; dz < height; dz++) {
          int tz = z + dz - offsetHeight;
          for (dx = 0; dx < width; dx++) {
              int tx = x + dx - offsetWidth;
              int templateId = template[dz][dx];
              int blockTypeId = world.getBlockTypeIdAt(tx, y, tz);
              if (templateId > 0 && templateId != blockTypeId) {
                  return false;
              }
          }
      }
      return true;
  }
}