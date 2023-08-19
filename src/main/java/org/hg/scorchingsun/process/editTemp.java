package org.hg.scorchingsun.process;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Furnace;
import org.bukkit.block.Smoker;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.hg.scorchingsun.RoomExitFinder;

public class editTemp {
    public final static double biomeTemp(Location location){
        World world = location.getWorld();
        double biome_temp = world.getTemperature(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if (biome_temp <= -1) {
            //Температура пиздецки холодных биомов
            return -40;
        } else if (biome_temp <= 0) {
            //Температура просто холодных биомов
            return  -15;
        } else if (biome_temp < 1) {
            //Температура теплых биомов
            return  15;
        } else {
            //Температура пиздецки жарких биомов
            return  35;
        }
    }
    public final static double sunTemp(Location location){
        World world = location.getWorld();
        if (world.getTime() < 12000 && !world.hasStorm() && !world.isThundering()) {
            // Температура солнца
            double coof = RoomExitFinder.findExitSteps(location, 7,
                    l -> l.getBlock().getType().isAir(),
                    l -> l.getWorld().getHighestBlockYAt(location) <= location.getY());
            return 15 * (1 / (coof + 1));
        }
        return 0;
    }
    public final static double hazerTemp(Location location, double temp){
        if (location.getBlock().getType() == Material.WATER || location.getBlock().getType() == Material.BUBBLE_COLUMN) {
            double coof = RoomExitFinder.findExitSteps(location, 4,
                    l -> (l.getBlock().getType() == Material.WATER || l.getBlock().getType() == Material.BUBBLE_COLUMN),
                    l -> l.getBlock().getType() == Material.MAGMA_BLOCK|| l.getBlock().getType() == Material.BUBBLE_COLUMN);
            if (coof >= 0 && coof <= 1000) {
                //Температура гейзера
                return Math.max(temp,(100 * (1/ (coof + 1))));
            }
        }
        return temp;
    }
    public final static double waterTemp(Location location, double temp){
        if (location.getBlock().getType() == Material.WATER){
            return temp-15;
        }
        return temp;
    }
    public final static double powderSnowTemp(Location location, double temp){
        if (location.getBlock().getType() == Material.POWDER_SNOW) {
            return Math.min(-5, temp);
        }
        return temp;
    }
    public final static double fireTemp(Location location, double temp){
        double coof = RoomExitFinder.findExitSteps(location, 4,
                l -> l.getBlock().getType().isAir(),
                l -> {
            if (l.getBlock().getType() == Material.FIRE){return true;}
            if (l.getBlock().getType().name().contains("CAMPFIRE")){return true;}
            if (l.getBlock().getType() == Material.FURNACE || l.getBlock().getType() == Material.BLAST_FURNACE || l.getBlock().getType() == Material.SMOKER){
                if (l.getBlock().getState() instanceof Furnace) {
                    return ((Furnace) l.getBlock().getState()).getCookTime() > 0;
                } else if (l.getBlock().getState() instanceof Smoker) {
                    return ((Smoker) l.getBlock().getState()).getCookTime() > 0;
                }
            }
            return false;});
        temp = Math.max(temp, 100*(1.5/(coof+1)));
        return temp;
    }
    public final static double armorEffectTemp(Player player, double temp){
        int i = 0;
        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            if (armorPiece != null && armorPiece.getType() != null && (armorPiece.getType() == Material.LEATHER_HELMET ||
                    armorPiece.getType() == Material.LEATHER_CHESTPLATE ||
                    armorPiece.getType() == Material.LEATHER_LEGGINGS ||
                    armorPiece.getType() == Material.LEATHER_BOOTS)) {
                i++;
            }
        }
        temp += 5*i;
        return temp;
    }
    public final static double lavaTemp(Location location, double temp){
        double coof = RoomExitFinder.findExitSteps(location, 5,
                    l -> l.getBlock().getType().isAir(),
                    l -> l.getBlock().getType() == Material.LAVA);
            temp = Math.max(temp, 200 * (1/(coof+1)));
        return temp;
    }
}
