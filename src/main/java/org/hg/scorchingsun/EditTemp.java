package org.hg.scorchingsun;
import org.hg.scorchingsun.enchants.*;
import org.hg.scorchingsun.process.editTemp;
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

import static org.hg.scorchingsun.ScorchingSun.*;

public class EditTemp {
    private static void display(Player player, String text) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW + text));
    }

    public static void process(Player player) {
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        World world = player.getWorld();
        Location location = player.getLocation();
        double need_temp = 0;
        need_temp += editTemp.biomeTemp(location);
        need_temp += editTemp.sunTemp(location);
        need_temp = editTemp.hazerTemp(location, need_temp);
        if (need_temp == 0){
            need_temp = editTemp.waterTemp(location, need_temp);
        }
        need_temp = editTemp.powderSnowTemp(location, need_temp);
        need_temp = editTemp.fireTemp(location, need_temp);
        need_temp = editTemp.armorEffectTemp(player, need_temp);
        need_temp = editTemp.lavaTemp(location, need_temp);
        need_temp = editTemp.torchTemp(location, need_temp);
        need_temp = editTemp.tagsPlayerTemp(player, need_temp);
        need_temp = editTemp.permPlayerTemp(player, need_temp);
        need_temp = Math.min(max_temp, need_temp);
        need_temp = Math.max(min_temp, need_temp);
        finalTemp(player, need_temp);
        display(player, need_temp + "°");
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
}
