package com.untamedears.xppylons;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.Random;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class XpPylons extends JavaPlugin {
    private static final Logger log = Logger.getLogger("XpPylons");
    private static XpPylons plugin;
    
    private PylonSet pylons;
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        return true;
    }

    public void onEnable() {
        plugin = this;
        
        pylons = new PylonSet(this);
        
        ConsoleCommandSender console = getServer().getConsoleSender();
        console.addAttachment(this, "xppylons.console", true);
        log.info("[XpPylons] XP Pylons enabled.");
    }

    public void onDisable() {
        log.info("[XpPylons] XP Pylons disabled.");
    }
    
    public void registerEvents(){
        try {
            PluginManager pm = getServer().getPluginManager();
            // pm.registerEvents(new BlockListener(), this);
        }
        catch(Exception e)
        {
          severe(e.toString());
        }
    }
    
    public static void info(String message){
        log.info("[XpPylons] " + message);
    }
    
    public static void severe(String message){
        log.severe("[XpPylons] " + message);
    }
    
    public static void warning(String message){
        log.warning("[XpPylons] " + message);
    }
}
