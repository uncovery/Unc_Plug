/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.uncovery.uncplug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Chunk;

/**
 * This unloads ALL chunks on the server.
 * This is tested and safe in case there are users online.
 * @author 
 */
public class CommandUnloadChunks implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        for (World currentWorld : Bukkit.getWorlds()) {
            // get the world name to a string
            String worldname = currentWorld.getName();
            System.out.println(" Processing chunks in " + worldname + "...");

            int i = 0;
            int c = 0;
            for (Chunk currentChunk : currentWorld.getLoadedChunks()) {    
                i++;
                boolean check = currentChunk.unload(true);
                if (check) {
                    c++;
                }
            }        
            System.out.println("Unloaded " + c + " of " + i + " Chunks!");
        }
        return true;
    }
}
