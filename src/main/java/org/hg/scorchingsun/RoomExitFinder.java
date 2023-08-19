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
import java.util.function.Function;

public class RoomExitFinder extends JavaPlugin {

//    public static final int MAX_DEPTH = 7;

    public static int findExitSteps(Location startLocation, int MAX_DEPTH, Function<Location, Boolean> isWay, Function<Location, Boolean> isExit) {
        World world = startLocation.getWorld();
        Set<Location> visited = new HashSet<>();
        Stack<Location> stack = new Stack<>();
        stack.push(startLocation);
        int steps = 0;
        while (!stack.isEmpty() && steps < MAX_DEPTH) {
            for (Location location: (Stack<Location>) stack.clone()){
                if (isExit.apply(location)){
                    return steps;
                }
                visited.add(location);
                stack.remove(location);
                addBlocksAround(location, stack, visited, isWay, isExit);
            }
            steps++;
        }

        return 9999999;
    }
    private static void addBlocksAround(Location location, Stack<Location> stack, Set<Location> visited, Function<Location, Boolean> isWay, Function<Location, Boolean> isExit){
        for (int x = -1; x <= 1; x+=2) {
            Location new_location = location.clone();
            new_location = new_location.add(x, 0, 0);
            if (!visited.contains(new_location)
                    && isAir(new_location, isWay) || isExit.apply(new_location)){
                stack.push(new_location);
            }
        }
        for (int y = -1; y <=1; y+=2) {
            Location new_location = location.clone();
            new_location = new_location.add(0, y, 0);
            if (!visited.contains(new_location)
                    && isAir(new_location, isWay) || isExit.apply(new_location)){
                stack.push(new_location);
            }
        }
        for (int z = -1; z <=1; z+=2) {
            Location new_location = location.clone();
            new_location = new_location.add(0, 0, z);
            if (!visited.contains(new_location)
                    && isAir(new_location, isWay) || isExit.apply(new_location)){
                stack.push(new_location);
            }
        }
    }
    private static boolean isAir(Location location, Function<Location, Boolean> isWay){
        Material material = location.getBlock().getType();
        if (isWay.apply(location)){
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
//    private static boolean isExit(Location location){
//        return location.getWorld().getHighestBlockYAt(location) <= location.getY();
//    }

}
