package org.hg.scorchingsun;
import org.hg.scorchingsun.process.editTemp;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.hg.scorchingsun.ScorchingSun.*;

public class EditTemp {
    public static double ambientTemp(Player player){
        Location location = player.getLocation();
        List<editTemp.calculate> list = new ArrayList<>();
        list.add(new editTemp.calculate(ambientTemp(location), Double::sum));
        list.add(editTemp.armorEffectTemp(player));
        list.add(editTemp.tagsPlayerTemp(player));
        list.add(editTemp.permPlayerTemp(player));
        return calculate(list);
    }
    public static double ambientTemp(Location location){
        List<editTemp.calculate> list = new ArrayList<>();
        list.add(editTemp.biomeTemp(location));
        list.add(editTemp.soulCampfireTemp(location));
        list.add(editTemp.iceSnowTemp(location));
        list.add(editTemp.lavaTemp(location));
        list.add(editTemp.sunTemp(location));
        list.add(editTemp.waterTemp(location));
        list.add(editTemp.hazerTemp(location));
        list.add(editTemp.fireTemp(location));
        list.add(editTemp.torchTemp(location));
        list.add(editTemp.soulSandTemp(location));
        return calculate(list);
    }
    private static double calculate(List<editTemp.calculate> list){
        double need_temp = 0;
        list.sort(Comparator.comparingDouble(obj -> obj.priority));
        for (editTemp.calculate calc: list){
            need_temp = calc.math.apply(need_temp, calc.number);
        }
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
