package org.hg.scorchingsun.enchants;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class listenerUpdateEnchant implements Listener{
    @EventHandler
    public void onItemHeld(InventoryClickEvent event) {
        if (event.getCurrentItem()!=null){
            updateEnchantmentLore(event.getCurrentItem());
        }
        for (ItemStack itemStack: event.getClickedInventory().getStorageContents()) {
            if (itemStack != null) {
                updateEnchantmentLore(itemStack);
            }
        }
    }
    public static ItemStack updateEnchantmentLore(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null){
            return itemStack;
        }
        List<Enchantment> list = new ArrayList<>();
        list.add(Enchantment.getByKey(NamespacedKey.minecraft(HeatAccumulationEnchantment.getEchName())));
        list.add(Enchantment.getByKey(NamespacedKey.minecraft(HeatDissipationEnchantment.getEchName())));
        for (Enchantment ench :list) {
            String name = ChatColor.GRAY + ench.getName();
            List<String> lore = itemMeta.getLore();
            if (lore != null && !lore.isEmpty()){
                for (String str: itemMeta.getLore()){
                    if (str.contains(name)){
                        lore.remove(str);
                    }
                }
            }
            if (itemMeta.getEnchants() != null && !itemMeta.getEnchants().isEmpty()){
                if (itemMeta.getEnchants().containsKey(ench)){
                    int level = itemMeta.getEnchants().get(ench);
                    if (level > 0){
                        if (lore == null){
                            lore = Collections.singletonList(name + " " + level);
                        } else {
                            lore.add(0, name+" "+level);
                        }
                    }
                }
            }
            itemMeta.setLore(lore);
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
