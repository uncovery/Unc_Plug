/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.uncovery.uncplug;

import net.minecraft.server.v1_12_R1.MinecraftServer;

/**
 *
 * @author spiesol01
 */
public class tps {
    public double getTPS() {
        double[] tps = MinecraftServer.getServer().recentTps;
        if (tps[0] > 20) {
            return 20;
        }
        return tps[0];
    }
}
