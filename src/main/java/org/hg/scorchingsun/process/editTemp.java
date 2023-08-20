package org.hg.scorchingsun.process;

import org.bukkit.*;
import org.bukkit.block.Campfire;
import org.bukkit.block.Furnace;
import org.bukkit.block.Smoker;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.hg.scorchingsun.RoomExitFinder;
import org.hg.scorchingsun.enchants.HeatAccumulationEnchantment;
import org.hg.scorchingsun.enchants.HeatDissipationEnchantment;

import java.util.Set;

public class editTemp {
    public static double biomeTemp(Location location) {
        World world = location.getWorld();
        double biome_temp = world.getTemperature(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if (biome_temp <= -1) {
            //Температура пиздецки холодных биомов
            return -40;
        } else if (biome_temp <= 0) {
            //Температура просто холодных биомов
            return -15;
        } else if (biome_temp < 1) {
            //Температура теплых биомов
            return 15;
        } else {
            //Температура пиздецки жарких биомов
            return 35;
        }
    }

    public static double sunTemp(Location location) {
        World world = location.getWorld();
        if (world.getTime() < 12000 && !world.hasStorm() && !world.isThundering()) {
            // Температура солнца
            double coof = RoomExitFinder.findExitSteps(location, 5,
                    l -> l.getBlock().getType().isAir(),
                    l -> l.getWorld().getHighestBlockYAt(l) <= l.getY());
            return 15 * (1 / (coof + 1));
        }
        return 0;
    }

    public static double hazerTemp(Location location, double temp) {
        if (location.getBlock().getType() == Material.WATER || location.getBlock().getType() == Material.BUBBLE_COLUMN) {
            double coof = RoomExitFinder.findExitSteps(location, 4,
                    l -> (l.getBlock().getType() == Material.WATER || l.getBlock().getType() == Material.BUBBLE_COLUMN),
                    l -> l.getBlock().getType() == Material.MAGMA_BLOCK || l.getBlock().getType() == Material.BUBBLE_COLUMN);
            if (coof >= 0 && coof <= 1000) {
                //Температура гейзера
                return Math.max(temp, (100 * (1 / (coof + 1))));
            }
        }
        return temp;
    }

    public static double waterTemp(Location location, double temp) {
        if (location.getBlock().getType() == Material.WATER) {
            return temp - 15;
        }
        return temp;
    }

    public static double iceSnowTemp(Location location, double temp) {
        double coof = RoomExitFinder.findExitSteps(location, 3,
                l -> l.getBlock().getType().isAir(),
                l -> l.getBlock().getType() == Material.POWDER_SNOW || l.getBlock().getType().name().contains("ICE"));
        coof = (1 / (coof + 1));
        if (coof > 0.0001){
            temp = Math.min(temp, -5 * coof);
        }
        return temp;
    }

    public static double fireTemp(Location location, double temp) {
        double coof = RoomExitFinder.findExitSteps(location, 4,
                l -> l.getBlock().getType().isAir(),
                l -> {
                    if (l.getBlock().getType() == Material.FIRE) {
                        return true;
                    }
                    if (l.getBlock().getType().name().contains("CAMPFIRE")) {
                        return true;
                    }
                    if (l.getBlock().getType() == Material.FURNACE || l.getBlock().getType() == Material.BLAST_FURNACE || l.getBlock().getType() == Material.SMOKER) {
                        if (l.getBlock().getState() instanceof Furnace) {
                            return ((Furnace) l.getBlock().getState()).getCookTime() > 0;
                        } else if (l.getBlock().getState() instanceof Smoker) {
                            return ((Smoker) l.getBlock().getState()).getCookTime() > 0;
                        }
                    }
                    return false;
                });
        coof = (1 / (coof + 1));
        if (coof > 0.0001) {
            temp = Math.max(temp, 100 * coof);
        }
        return temp;
    }

    public static double armorEffectTemp(Player player, double temp) {
        int i = 0;
        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            if (armorPiece == null || armorPiece.getType() == null) {
                continue;
            }
            if (armorPiece.getType() == Material.LEATHER_HELMET ||
                    armorPiece.getType() == Material.LEATHER_CHESTPLATE ||
                    armorPiece.getType() == Material.LEATHER_LEGGINGS ||
                    armorPiece.getType() == Material.LEATHER_BOOTS) {
                i += 5;
            }
            if (armorPiece.getEnchantments() != null) {
                if (armorPiece.getEnchantments().containsKey(Enchantment.getByKey(NamespacedKey.minecraft(HeatAccumulationEnchantment.getEchName())))) {
                    int level = armorPiece.getEnchantmentLevel(Enchantment.getByKey(NamespacedKey.minecraft(HeatAccumulationEnchantment.getEchName())));
                    i += 5 * level;
                }
                if (armorPiece.getEnchantments().containsKey(Enchantment.getByKey(NamespacedKey.minecraft(HeatDissipationEnchantment.getEchName())))) {
                    int level = armorPiece.getEnchantmentLevel(Enchantment.getByKey(NamespacedKey.minecraft(HeatDissipationEnchantment.getEchName())));
                    i -= 5 * level;
                }
            }
        }
        temp += i;
        return temp;
    }

    public static double lavaTemp(Location location, double temp) {
        double coof = RoomExitFinder.findExitSteps(location, 5,
                l -> l.getBlock().getType().isAir(),
                l -> l.getBlock().getType() == Material.LAVA);
        coof = (1 / (coof + 1));
        if (coof > 0.0001) {
            temp = Math.max(temp, 200 *coof);
        }
        return temp;
    }

    public static double torchTemp(Location location, double temp) {
        double coof = RoomExitFinder.findExitSteps(location, 3,
                l -> l.getBlock().getType().isAir(),
                l -> l.getBlock().getType() == Material.TORCH);
        coof = (1 / (coof + 1));
        if (coof > 0.0001) {
            temp +=  5 * coof;
        }
        return temp;
    }

    public static double tagsPlayerTemp(Player player, double temp) {
        Set<String> tags = player.getScoreboardTags();
        if (tags.contains("Warm1")) {
            temp += 5;
        }
        if (tags.contains("Warm2")) {
            temp += 10;
        }
        if (tags.contains("Warm3")) {
            temp += 15;
        }
        if (tags.contains("Warm+")) {
            temp += 30;
        }
        if (tags.contains("Cold1")) {
            temp -= 5;
        }
        if (tags.contains("Cold2")) {
            temp -= 10;
        }
        if (tags.contains("Cold3")) {
            temp -= 15;
        }
        if (tags.contains("Cold+")) {
            temp -= 30;
        }
        return temp;
    }
    public static double soulCampfireTemp(Location location, double temp){
        double coof = RoomExitFinder.findExitSteps(location, 3,
                l -> l.getBlock().getType().isAir(),
                l -> l.getBlock().getType() == Material.SOUL_CAMPFIRE);
        coof = (1 / (coof + 1));
        if (coof > 0.0001) {
            temp -= 15 *coof;
        }
        return temp;
    }
    public static double permPlayerTemp(Player player, double temp) {
        return temp+getPermissionNumber("temperaturetrain.", player);
    }

    private static int getPermissionNumber(String permission, Player player) {
        for (int i = 10; i >= -10; i--) {
            String perm = permission + i;
            if (player.hasPermission(perm)) {
                return i;
            }
        }
        return 0; // если игрок не имеет никаких пермишенов с данным префиксом
    }

}
