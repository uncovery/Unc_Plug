/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.uncovery.uncplug;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


/**
 *
 * @author uncovery
 */
public class materials implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            System.out.print("Trying to list items");
            boolean check = new mySQL().openDBConnection();
            String sql1 = "TRUNCATE TABLE minecraft_srvr.items";
            new mySQL().executeSQLUpdate(sql1, false);            
            System.out.print("table is reset");
            
            ArrayList<String> itemObjects = new ArrayList<>();
            
            for (Material mat : Material.values()) {
                String matName = mat.name().toLowerCase();
                int Stacksize = mat.getMaxStackSize();
                boolean isBlock = mat.isBlock();
                int maxDurability = mat.getMaxDurability();
                int oldID = mat.getId();
                itemObjects.add("('" + matName + "'," + Stacksize + "," + isBlock + "," + maxDurability + "," + oldID + ")");
            }
            
            String sql = "INSERT INTO minecraft_srvr.items (item_name, stacksize, is_block, durability, old_id) VALUES "  + String.join(",", itemObjects);
            
            long check2 = new mySQL().executeSQLUpdate(sql, false);
            System.out.print("Done!");
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(materials.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
}
