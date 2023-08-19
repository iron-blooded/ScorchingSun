package org.hg.scorchingsun;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Furnace;
import org.bukkit.block.Smoker;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public final class ScorchingSun extends JavaPlugin implements Listener {
    private static final HashMap<String, Double> temp_players = new HashMap<>();
    public static double min_temp = -125.0;
    public static double max_temp = 300.0;
    public static double crit_firing_temp = 100.0;
    public static double crit_frizing_temp = 0.0;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("Sunboy")) {
                        processFire(player);
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
        double temp = 36.6;
        if (temp_players.containsKey(player.getName())) {
            temp = temp_players.get(player.getName());
        }
        if (round) {
            temp = ((double) ((int) (temp * 10)) / 10);
        }
        return temp;
    }

    private void display(Player player, String text) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW + text));
//        player.sendTitle("", text, 0, 20, 0);
    }

    public void processFire(Player player) {
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        World world = player.getWorld();
        double biome_temp = world.getTemperature(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
        double need_temp = 0;
        if (biome_temp <= -1) {
            //Температура пиздецки холодных биомов
            need_temp = -40;
        } else if (biome_temp <= 0) {
            //Температура просто холодных биомов
            need_temp = -15;
        } else if (biome_temp < 1) {
            //Температура теплых биомов
            need_temp = 15;
        } else {
            //Температура пиздецки жарких биомов
            need_temp = 35;
        }
        if (world.getTime() < 12000) {
            // Температура солнца
            double coof = RoomExitFinder.findExitSteps(player.getLocation(), 7,
                    location -> location.getBlock().getType().isAir(),
                    location -> location.getWorld().getHighestBlockYAt(location) <= location.getY());
            need_temp += 15 * (1 / (coof + 1));
        }
        if (player.getLocation().getBlock().getType() == Material.WATER) {
            //Температура воды
            need_temp -= 15;
            double coof = RoomExitFinder.findExitSteps(player.getLocation(), 4,
                    location -> location.getBlock().getType() == Material.WATER,
                    location -> ((Waterlogged) player.getLocation().getBlock().getBlockData()).isWaterlogged());
            if (coof >= 0) {
                //Температура гейзера
                need_temp += 100 / (coof + 1);
            }
        }
        if (player.getLocation().getBlock().getType() == Material.POWDER_SNOW) {
            //Температура рыхлого снега
            need_temp = Math.min(-5, need_temp);
        }
        if (true) {
            //Температура костров, огня и печей
            double coof = RoomExitFinder.findExitSteps(player.getLocation(), 4,
                    location -> location.getBlock().getType().isAir(),
                    location -> {
                if (location.getBlock().getType() == Material.FIRE){return true;}
                if (location.getBlock().getType().name().contains("CAMPFIRE"))
                if (location.getBlock().getType() == Material.FURNACE || location.getBlock().getType() == Material.BLAST_FURNACE || location.getBlock().getType() == Material.SMOKER){
                    if (location.getBlock().getState() instanceof Furnace) {
                        return ((Furnace) location.getBlock().getState()).getCookTime() > 0;
                    } else if (location.getBlock().getState() instanceof Smoker) {
                        return ((Smoker) location.getBlock().getState()).getCookTime() > 0;
                    }
                }
                return false;});
            need_temp = Math.max(need_temp, 100*(1.5/(coof+1)));

        }
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
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (event.getItem().getType() == Material.POTION) {
            double temp = crit_firing_temp / 5;
            if (getTemp(player)- temp > 0) {
                minusTemp(player, temp);
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
