package org.hg.scorchingsun;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class RoomExitFinder extends JavaPlugin {

    public static final int MAX_DEPTH = 7;

    public static int findExitSteps(Location startLocation) {
        World world = startLocation.getWorld();
        Set<Location> visited = new HashSet<>();
        Stack<Location> stack = new Stack<>();
        stack.push(startLocation);
        int steps = 0;
        while (!stack.isEmpty() && steps < MAX_DEPTH) {
            for (Location location: (Stack<Location>) stack.clone()){
                if (isExit(location)){
                    return steps;
                }
                visited.add(location);
                stack.remove(location);
                addBlocksAround(location, stack, visited);
            }
            steps++;
        }

        return MAX_DEPTH*-1;
    }
    private static void addBlocksAround(Location location, Stack<Location> stack, Set<Location> visited){
        for (int x = -1; x <= 1; x+=2) {
            Location new_location = location.clone();
            new_location = new_location.add(x, 0, 0);
            if (!visited.contains(new_location)
                    && isAir(new_location)){
                stack.push(new_location);
            }
        }
        for (int y = -1; y <=1; y+=2) {
            Location new_location = location.clone();
            new_location = new_location.add(0, y, 0);
            if (!visited.contains(new_location)
                    && isAir(new_location)){
                stack.push(new_location);
            }
        }
        for (int z = -1; z <=1; z+=2) {
            Location new_location = location.clone();
            new_location = new_location.add(0, 0, z);
            if (!visited.contains(new_location)
                    && isAir(new_location)){
                stack.push(new_location);
            }
        }
    }
    private static boolean isAir(Location location){
        Material material = location.getBlock().getType();
        if (material.isAir()){
            return true;
        }
        else if (material.name().contains("_DOOR")){
            Door door = (Door) location.getBlock().getBlockData();
            return door.isOpen();
        } else if (!material.isInteractable()
                && material.isSolid()
                && !material.name().contains("_SLAB")
                && !material.name().contains("_PLATE")
                && !material.name().equals("CHAIN")
                && !material.name().contains("_BARS")) {
            return false;
        }
        return true;
    }
    private static boolean isExit(Location location){
        return location.getWorld().getHighestBlockYAt(location) <= location.getY();
    }

}
