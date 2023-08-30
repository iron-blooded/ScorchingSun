package org.hg.scorchingsun.process;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.block.Smoker;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.hg.scorchingsun.RoomExitFinder;
import org.hg.scorchingsun.enchants.HeatAccumulationEnchantment;
import org.hg.scorchingsun.enchants.HeatDissipationEnchantment;

import java.util.Set;
import java.util.function.BinaryOperator;

public class editTemp {
    public static calculate biomeTemp(Location location) {
        World world = location.getWorld();
        double biome_temp = world.getTemperature(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        double temp = 40 * biome_temp - 15;
        return new calculate(temp, Double::sum);
    }

    public static calculate sunTemp(Location location) {
        World world = location.getWorld();
        double time = world.getTime();
        long zahod_1 = 12000; // заход
        long zahod_2 = 14000; // заход
        long voshod_1 = 22000; // восход
        long voshod_2 = 23999; // восход
        double temp = 0;
        double factor = 0;
        if (time < zahod_1) {
            factor = 1;
        } else if (time > voshod_2) {
            factor = 0;
        } else if (time > zahod_1 && time < zahod_2) {
            factor = 1 - ((time - zahod_1) / (zahod_2 - zahod_1));
        } else if (time > voshod_1 && time < voshod_2) {
            factor = (time - voshod_1) / (voshod_2 - voshod_1);
        }
        if (!world.hasStorm() && !world.isThundering()) {
            // Температура солнца
            double coof = RoomExitFinder.findExitSteps(location, 5, l -> l.getBlock().getType().isAir(), l -> l.getWorld().getHighestBlockYAt(l) <= l.getY());
            temp = 15 * (1 / (coof + 1));
        }
        temp *= factor;
        return new calculate(temp, Double::sum);
    }

    public static calculate hazerTemp(Location location) {
        if (location.getBlock().getType() == Material.WATER || location.getBlock().getType() == Material.BUBBLE_COLUMN) {
            double coof = RoomExitFinder.findExitSteps(location, 4, l -> (l.getBlock().getType() == Material.WATER || l.getBlock().getType() == Material.BUBBLE_COLUMN), l -> l.getBlock().getType() == Material.MAGMA_BLOCK || l.getBlock().getType() == Material.BUBBLE_COLUMN);
            if (coof >= 0 && coof <= 1000) {
                //Температура гейзера
                return new calculate(100 * (1 / (coof + 1)), Math::max);
            }
        }
        return new calculate(0, Double::sum);
    }

    public static calculate waterTemp(Location location) {
        double temp = 0;
        if (location.getBlock().getType() == Material.WATER) {
            temp = -15;
        }
        return new calculate(temp, Double::sum);
    }

    public static calculate iceSnowTemp(Location location) {
        double coof = RoomExitFinder.findExitSteps(location, 3, l -> l.getBlock().getType().isAir(), l -> l.getBlock().getType() == Material.POWDER_SNOW || l.getBlock().getType() == Material.SNOW || l.getBlock().getType().name().contains("ICE"));
        coof = (1 / (coof + 1));
        if (coof > 0.0001) {
            return new calculate(-15 * coof, Double::sum);
        }
        return new calculate(0, Double::sum);
    }

    public static calculate fireTemp(Location location) {
        double coof = RoomExitFinder.findExitSteps(location, 4, l -> l.getBlock().getType().isAir(), l -> {
            if (l.getBlock().getType() == Material.FIRE) {
                return true;
            }
            if (l.getBlock().getType() == Material.CAMPFIRE) {
                return isCampfireLit(l.getBlock());
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
            return new calculate(100 * coof, Double::sum);
        }
        return new calculate(0, Double::sum);
    }

    public static calculate armorEffectTemp(Player player) {
        int i = 0;
        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            if (armorPiece == null || armorPiece.getType() == null) {
                continue;
            }
            if (armorPiece.getType() == Material.LEATHER_HELMET || armorPiece.getType() == Material.LEATHER_CHESTPLATE || armorPiece.getType() == Material.LEATHER_LEGGINGS || armorPiece.getType() == Material.LEATHER_BOOTS) {
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
            if (armorPiece.getItemMeta() != null && armorPiece.getItemMeta().getLore() != null) {
                for (String str : armorPiece.getItemMeta().getLore()) {
                    if (str != null && str.contains(ChatColor.COLOR_CHAR + "")) {
                        try {
                            if (str.contains("Повышение Температуры ")) {
                                i += Integer.parseInt(str.split("Повышение Температуры ")[1]) * 5;
                            } else if (str.contains("Понижение Температуры ")) {
                                i -= Integer.parseInt(str.split("Понижение Температуры ")[1]) * 5;
                            }
                        } catch (Exception e) {
                            player.sendMessage("У тебя на броне неверный уровень модификатора температуры!");
                        }
                    }
                }
            }
        }
        return new calculate(i, Double::sum, 10);
    }

    public static calculate lavaTemp(Location location) {
        double coof = RoomExitFinder.findExitSteps(location, 5, l -> l.getBlock().getType().isAir(), l -> l.getBlock().getType() == Material.LAVA);
        coof = (1 / (coof + 1));
        if (coof > 0.0001) {
            return new calculate(200 * coof, Math::max);
        }
        return new calculate(0, Double::sum);
    }

    public static calculate torchTemp(Location location) {
        double temp = 0;
        double coof = RoomExitFinder.findExitSteps(location, 3, l -> l.getBlock().getType().isAir(), l -> l.getBlock().getType() == Material.TORCH);
        coof = (1 / (coof + 1));
        if (coof > 0.0001) {
            temp += 5 * coof;
        }
        return new calculate(temp, Double::sum);
    }

    public static calculate tagsPlayerTemp(Player player) {
        double temp = 0;
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
        return new calculate(temp, Double::sum, 10);
    }

    public static calculate soulCampfireTemp(Location location) {
        double coof = RoomExitFinder.findExitSteps(location, 3, l -> l.getBlock().getType().isAir(), l -> l.getBlock().getType() == Material.SOUL_CAMPFIRE && isCampfireLit(l.getBlock()));
        coof = (1 / (coof + 1));
        if (coof > 0.0001) {
            return new calculate(-15 * coof, Double::sum);
        }
        return new calculate(0, Double::sum);
    }

    public static calculate permPlayerTemp(Player player) {
        return new calculate(getPermissionNumber("temperaturetrain.", player), Double::sum, 10);
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

    public static calculate soulSandTemp(Location location) {
        double coof = RoomExitFinder.findExitSteps(location, 2, l -> l.getBlock().getType().isAir(), l -> l.getBlock().getType() == Material.SOUL_SAND);
        coof = (1 / (coof + 1));
        if (coof > 0.0001) {
            return new calculate(-3 * coof, Double::sum);
        }
        return new calculate(0, Double::sum);
    }

    public static calculate bed(Location location) {
        double coof = RoomExitFinder.findExitSteps(location, 1, l -> l.getBlock().getType().isAir(), l -> l.getBlock().getType().name().contains("_BED"));
        coof = (1 / (coof + 1));
        if (coof > 0.0001) {
            return new calculate(5 * coof, Math::max);
        }
        return new calculate(0, Double::sum);
    }

    public static boolean isCampfireLit(Block campfireBlock) {
        // Проверяем, является ли блок костром
        if (campfireBlock.getType() == Material.CAMPFIRE || campfireBlock.getType() == Material.SOUL_CAMPFIRE) {
            BlockState state = campfireBlock.getState();
            // Проверяем, является ли состояние костра Campfire
            if (state.getBlockData() instanceof Campfire campfire) {
                return campfire.isLit(); // Возвращает true, если костер зажжен
            }
        }

        return false; // Блок не является костром или состояние не Campfire
    }

    public static class calculate {
        public BinaryOperator<Double> math;
        public double number;
        public double priority;

        public calculate(double number, BinaryOperator<Double> math) {
            this.number = number;
            this.math = math;
            BinaryOperator<Double> sum = (a, b) -> Double.sum(a, b);
            BinaryOperator<Double> max = (a, b) -> Math.max(a, b);
            BinaryOperator<Double> min = (a, b) -> Math.min(a, b);
            if (math.equals(sum)) {
                priority = 0;
            } else if (math.equals(min)) {
                priority = 1;
            } else if (math.equals(max)) {
                priority = 2;
            }
        }

        public calculate(double number, BinaryOperator<Double> math, double priority) {
            this.number = number;
            this.math = math;
            this.priority = priority;
        }
    }
}
