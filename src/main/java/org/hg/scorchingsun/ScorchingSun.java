package org.hg.scorchingsun;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Furnace;
import org.bukkit.block.Smoker;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.hg.scorchingsun.enchants.*;
import org.hg.scorchingsun.process.editTemp;

import java.lang.reflect.Field;
import java.util.HashMap;

import static org.hg.scorchingsun.EditTemp.process;

public final class ScorchingSun extends JavaPlugin implements Listener {
    private static final HashMap<String, Double> temp_players = new HashMap<>();
    public static double min_temp = -125.0;
    public static double max_temp = 300.0;
    public static double crit_firing_temp = 100.0;
    public static double toshnota = 50.0;
    public static double crit_frizing_temp = 0.0;
    public static double comfort_temp = 36.6;

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

    public static void finalTemp(Player player, double temp) {
        double current_temp = getTemp(player);
        int i = 1;
        if (temp < current_temp) {
            i = -1;
        }
        double final_temp = Math.abs(current_temp - temp);
        final_temp = Math.sqrt(final_temp) / 20;
        final_temp *= i;
        final_temp = current_temp + final_temp;
        if (i > 0) {
            final_temp = Math.min(max_temp, final_temp);
        } else {
            final_temp = Math.max(min_temp, final_temp);
        }
        temp_players.put(player.getName(), final_temp);
    }
    public static void minusTemp(Player player, double minus) {
        double temp = getTemp(player);
        temp -= minus;
        temp = Math.max(temp, min_temp);
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
            temp = ((double) ((int) (temp * 10)) / 10);
        }
        return temp;
    }


    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (event.getItem().getType() == Material.POTION && player.getGameMode() == GameMode.SURVIVAL) {
            double temp = crit_firing_temp / 5;
            if (getTemp(player)- temp > 0) {
                minusTemp(player, temp);
            }
        }
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        temp_players.put(event.getEntity().getName(), comfort_temp);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            Enchantment.stopAcceptingRegistrations();
        }catch (Exception e){}
    }
}
