package org.hg.scorchingsun.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.hg.scorchingsun.EditTemp;
import org.hg.scorchingsun.RoomExitFinder;
import org.hg.scorchingsun.ScorchingSun;

public class disableSpawn implements Listener {
    public void on(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.BREEDING){
            return;
        }
        double temp = EditTemp.ambientTemp(event.getLocation());
        if (temp < ScorchingSun.crit_frizing_temp || temp > ScorchingSun.toshnota) {
            event.setCancelled(true);
        }
    }
}
