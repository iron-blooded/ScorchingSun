package org.hg.scorchingsun;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.hg.scorchingsun.enchants.HeatAccumulationEnchantment;
import org.hg.scorchingsun.enchants.HeatDissipationEnchantment;
import org.hg.scorchingsun.enchants.listenerUpdateEnchant;

import java.lang.reflect.Field;
import java.util.HashMap;

import static org.hg.scorchingsun.EditTemp.ambientTemp;
import static org.hg.scorchingsun.EditTemp.process;

public final class ScorchingSun extends JavaPlugin implements Listener {
    private static final HashMap<String, Double> temp_players = new HashMap<>();
    public static double min_temp = -125.0;
    public static double max_temp = 300.0;
    public static double crit_firing_temp = 100.0;
    public static double toshnota = 50.0;
    public static double crit_frizing_temp = 0.0;
    public static double comfort_temp = 36.6;

    public static double round(double i) {
        return ((double) ((int) (i * 10)) / 10);
    }

    public static double finalTemp(double current_temp, double ambient_temp) {
        return finalTemp(current_temp, ambient_temp, 1);
    }

    public static double finalTemp(double current_temp, double ambient_temp, double coof) {
        int i = 1;
        if (ambient_temp < current_temp) {
            i = -1;
        }
        double final_temp = Math.abs(current_temp - ambient_temp);
        final_temp = Math.sqrt(final_temp) / (20 * coof);
        final_temp *= i;
        final_temp = current_temp + final_temp;
        if (i > 0) {
            final_temp = Math.min(max_temp, final_temp);
        } else {
            final_temp = Math.max(min_temp, final_temp);
        }
        return final_temp;
    }

    public static void minusTemp(Player player, double minus) {
        double temp = getTemp(player);
        temp -= minus;
        temp = Math.max(temp, min_temp);
        temp_players.put(player.getName(), temp);
    }

    public static void setTemp(Player player, double temp) {
        temp_players.put(player.getName(), temp);
    }

    public static double getTemp(Player player) {
        return getTemp(player, false);
    }

    public static double getTemp(Player player, boolean round) {
        double temp = comfort_temp;
        if (temp_players.containsKey(player.getName())) {
            temp = temp_players.get(player.getName());
        }
        if (round) {
            temp = round(temp);
        }
        return temp;
    }

    public static void display(Player player, String text) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW + text));
    }

    @Override
    public void onEnable() {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HeatAccumulationEnchantment.registerEnchantment(this);
        HeatDissipationEnchantment.registerEnchantment(this);
        Bukkit.getPluginManager().registerEvents(new listenerUpdateEnchant(), this);
        Bukkit.getPluginManager().registerEvents(this, this);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("sunboy")) {
                        process(player);
                    }
                }
            }
        }.runTaskTimer(this, 0, 20); // 20 тиков = 1 секунда
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            Enchantment.stopAcceptingRegistrations();
        } catch (Exception e) {
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (event.getItem().getType() == Material.POTION && player.getGameMode() == GameMode.SURVIVAL) {
            double temp = crit_firing_temp / 5;
            if (getTemp(player) - temp > 0) {
                minusTemp(player, temp);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        temp_players.put(event.getEntity().getName(), comfort_temp);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (event.getRightClicked() instanceof Player clickedPlayer) {
            // Игрок нажал правой кнопкой на другого игрока
            String name_item = null;
            try {
                name_item = player.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
            } catch (Exception e) {
                return;
            }
            if (name_item != null && name_item.contains(ChatColor.COLOR_CHAR + "") && (name_item.contains("АОС") || name_item.contains("Термометр"))) {
                display(player, "Температура " + clickedPlayer.getName() + ": " + getTemp(clickedPlayer, true) + "°");
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String name_item = null;
        try {
            name_item = player.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
        } catch (Exception e) {
            return;
        }
        if (name_item != null && name_item.contains(ChatColor.COLOR_CHAR + "") && (name_item.contains("АОС") || name_item.contains("Термометр"))) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                display(player, "Ваша температура: " + getTemp(player, true) + "°");
            } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                display(player, "Температура в округе: " + round(ambientTemp(player.getLocation())) + "°");
            }
            event.setCancelled(true);
        }
    }
}
