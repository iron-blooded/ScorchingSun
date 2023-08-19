package org.hg.scorchingsun;

import org.bukkit.*;
import org.bukkit.block.Furnace;
import org.bukkit.block.Smoker;
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
import org.hg.scorchingsun.process.editTemp;

import java.util.HashMap;

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

    private void finalTemp(Player player, double temp) {
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

    /*
    private void addTemp(Player player, double coof){
        double temp = getTemp(player);
        temp += Math.sqrt(temp*max_temp/((20*60*6)/(max_temp/crit_temp)))*coof;
        temp = Math.min(temp, max_temp);
        temp_players.put(player.getName(), temp);
    }
    private void minusTemp(Player player){
        double temp = getTemp(player);
        minusTemp(player, Math.sqrt(temp*min_temp/((20*60*3)/(max_temp/crit_temp))));
    }
     */
    private void minusTemp(Player player, double minus) {
        double temp = getTemp(player);
        temp -= minus;
        temp = Math.max(temp, min_temp);
        temp_players.put(player.getName(), temp);
    }

    private double getTemp(Player player) {
        return getTemp(player, false);
    }
    private double getTemp(Player player, boolean round) {
        double temp = comfort_temp;
        if (temp_players.containsKey(player.getName())) {
            temp = temp_players.get(player.getName());
        }
        if (round) {
            temp = ((double) ((int) (temp * 10)) / 10);
        }
        return temp;
    }

    private void display(Player player, String text) {
//        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW + text));
    }

    public void process(Player player) {
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        World world = player.getWorld();
        double need_temp = 0;
        need_temp += editTemp.biomeTemp(player.getLocation());
        need_temp += editTemp.sunTemp(player.getLocation());
        need_temp = editTemp.hazerTemp(player.getLocation(), need_temp);
        if (need_temp == 0){
            need_temp = editTemp.waterTemp(player.getLocation(), need_temp);
        }
        need_temp = editTemp.powderSnowTemp(player.getLocation(), need_temp);
        need_temp = editTemp.fireTemp(player.getLocation(), need_temp);
        need_temp = editTemp.armorEffectTemp(player, need_temp);
        need_temp = editTemp.lavaTemp(player.getLocation(), need_temp);
        need_temp = Math.min(max_temp, need_temp);
        need_temp = Math.max(min_temp, need_temp);
        finalTemp(player, need_temp);
        display(player, getTemp(player, true) + "°");
        if (getTemp(player) >= crit_firing_temp) {
            player.setFireTicks(20 * 3);
        } else if (getTemp(player) < crit_frizing_temp && player.getFreezeTicks() < 20*4) {
            player.setFreezeTicks(20*4);
        } else if (getTemp(player) < crit_frizing_temp-20 && player.getFreezeTicks() < 20*10) {
            player.setFreezeTicks(20*10);
        } else if (getTemp(player) >= toshnota) {
            if (Math.random() <= 0.1) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 2, 4, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 2, 4, false, false));
            }
        }
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
        temp_players.put(((Player) event.getEntity()).getName(), comfort_temp);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
