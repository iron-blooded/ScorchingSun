package org.hg.scorchingsun;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Rock {
    public static ItemStack getRock() {
        ItemStack rock = new ItemStack(Material.CLAY_BALL);
        ItemMeta itemMeta = rock.getItemMeta();
        itemMeta.setDisplayName(getName());
        rock.setItemMeta(itemMeta);
        Lore.setTemp(rock, ScorchingSun.comfort_temp);
        return rock;
    }

    public static String getName() {
        return ChatColor.GRAY + "Терморегулирующий камень";
    }

    public static boolean isRock(ItemStack itemStack) {
        return itemStack.getItemMeta() != null && itemStack.getItemMeta().getDisplayName() != null && itemStack.getItemMeta().getDisplayName().contains(getName());
    }

    public static boolean isAccumulated(Player player) {
        return isRock(player.getInventory().getItemInOffHand());
    }

    public static boolean isGives(Player player) {
        return isRock(player.getInventory().getItemInMainHand());
    }

    public static class Lore {
        public static String text_temp_lore = ChatColor.WHITE + "Температура: " + ChatColor.YELLOW;

        public static double getTemp(ItemStack itemStack) {
            if (isRock(itemStack) && itemStack.getItemMeta().getLore() != null) {
                try {
                    for (String str : itemStack.getItemMeta().getLore()) {
                        if (str.contains(text_temp_lore)) {
                            return Double.parseDouble(str.split(text_temp_lore)[1]);
                        }
                    }
                } catch (Exception ignored) {
                }

            }
            return 0;
        }

        public static void setTemp(ItemStack itemStack, double temp) {
            temp = ScorchingSun.round(temp);
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> itemLore = new ArrayList<>();
            itemLore.add(text_temp_lore + temp);
            if (temp < ScorchingSun.crit_frizing_damage_temp) {
                itemLore.add(ChatColor.BLUE + "Обмораживающий");
            } else if (temp < ScorchingSun.crit_frizing_temp) {
                itemLore.add(ChatColor.DARK_AQUA + "Холодный");
            } else if (temp < ScorchingSun.toshnota) {
                itemLore.add(ChatColor.YELLOW + "Горячий");
            } else if (temp < ScorchingSun.crit_firing_temp) {
                itemLore.add(ChatColor.RED + "Обжигающий");
            }
            itemMeta.setLore(itemLore);
            itemStack.setItemMeta(itemMeta);
        }

        public static void addTemp(ItemStack itemStack, double temp) {
            setTemp(itemStack, getTemp(itemStack) + temp);
        }
    }
}
