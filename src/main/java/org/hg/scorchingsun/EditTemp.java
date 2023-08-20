package org.hg.scorchingsun;
import org.hg.scorchingsun.process.editTemp;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static org.hg.scorchingsun.ScorchingSun.*;

public class EditTemp {
    public static double ambientTemp(Player player){
        Location location = player.getLocation();
        double need_temp = 0;
        need_temp += editTemp.biomeTemp(location);
        need_temp = editTemp.soulCampfireTemp(location, need_temp);
        need_temp = editTemp.iceSnowTemp(location, need_temp);
        need_temp = editTemp.lavaTemp(location, need_temp);
        need_temp += editTemp.sunTemp(location);
        need_temp = editTemp.waterTemp(location, need_temp);
        need_temp = editTemp.hazerTemp(location, need_temp);
        need_temp = editTemp.fireTemp(location, need_temp);
        need_temp = editTemp.torchTemp(location, need_temp);
        need_temp = editTemp.armorEffectTemp(player, need_temp);
        need_temp = editTemp.tagsPlayerTemp(player, need_temp);
        need_temp = editTemp.permPlayerTemp(player, need_temp);
        need_temp = Math.min(max_temp, need_temp);
        need_temp = Math.max(min_temp, need_temp);
        return need_temp;
    }

    public static void process(Player player) {
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        finalTemp(player, ambientTemp(player));
//        display(player, need_temp + "°");
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
