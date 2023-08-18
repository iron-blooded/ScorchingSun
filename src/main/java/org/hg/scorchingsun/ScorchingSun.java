package org.hg.scorchingsun;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;

public final class ScorchingSun extends JavaPlugin implements Listener {
    private static HashMap<String, Double> temp_players = new HashMap<>();
    public static Double min_temp = 25.0;
    public static Double max_temp = 300.0;
    public static Double crit_temp = 100.0;
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()){
                    if (player.hasPermission("Sunboy")){
                        processFire(player);
                    }
                }
            }
        }.runTaskTimer(this, 0, 20); // 20 тиков = 1 секунда
    }
    private void addTemp(Player player, Double coof){
        Double temp = getTemp(player);
        temp += Math.sqrt(temp*max_temp/((20*60*6)/(max_temp/crit_temp)))*coof;
        temp = Math.min(temp, max_temp);
        temp_players.put(player.getName(), temp);
    }
    private void minusTemp(Player player){
        Double temp = getTemp(player);
        minusTemp(player, Math.sqrt(temp*min_temp/((20*60*3)/(max_temp/crit_temp))));
    }
    private void minusTemp(Player player, double minus){
        Double temp = getTemp(player);
        temp -= minus;
        temp = Math.max(temp, min_temp);
        temp_players.put(player.getName(), temp);
    }
    private Double getTemp(Player player){
        Double temp = min_temp;
        if (temp_players.containsKey(player.getName())){
            temp = temp_players.get(player.getName());
        }
        return ((double) ((int) (temp*10))/10);
    }
    private void display(Player player, String text){
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW +text));
//        player.sendTitle("", text, 0, 20, 0);
    }

    public void processFire(Player player){
        if (player.getGameMode() != GameMode.SURVIVAL){
            return;
        }
        World world = player.getWorld();
        if (getTemp(player) >= crit_temp){
            player.setFireTicks(20*3);
        }
        int steps_exit = RoomExitFinder.findExitSteps(player.getLocation());
        Biome biome = world.getBiome(player.getLocation());
        if (steps_exit>=0
                && world.getTime() < 12000
                && player.getLocation().getBlock().getType() != Material.WATER
                && player.getLocation().getBlock().getType() != Material.POWDER_SNOW
                && !(biome.name().contains("FROZEN_") || biome.name().contains("ICE_") || biome.name().contains("COLD_") || biome.name().contains("SNOWY_"))) {
            // игрок находится на солнце
            addTemp(player, 1.0/(steps_exit+1));
            display(player, ""+getTemp(player)+"°");
        }
        else {
            if (getTemp(player) <= min_temp){
                return;
            }
            minusTemp(player);
            display(player, ""+getTemp(player)+"°");
        }
    }
    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (event.getItem().getType() == Material.POTION) {
            minusTemp(player, crit_temp/5);
        }
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
