package org.hg.scorchingsun;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class Rock {
    public static ItemStack getRock(){
        ItemStack rock = new ItemStack(Material.CLAY_BALL);
        ItemMeta itemMeta = rock.getItemMeta();
        itemMeta.setDisplayName(getName());
        rock.setItemMeta(itemMeta);
        Lore.setTemp(rock, 0);
        return rock;
    }
    public static String getName(){
        return ChatColor.GRAY+"Терморегулирующий камень";
    }
    public static boolean isRock(ItemStack itemStack){
        return itemStack.getItemMeta() != null && itemStack.getItemMeta().getDisplayName() != null && itemStack.getItemMeta().getDisplayName().contains(getName());
    }
    public static boolean isAccumulated(Player player){
        return isRock(player.getInventory().getItemInOffHand());
    }
    public static boolean isGives(Player player){
        return isRock(player.getInventory().getItemInMainHand());
    }
    public static class Lore{
        public static String lore = ChatColor.WHITE+ "Температура: "+ChatColor.YELLOW;
        public static double getTemp(ItemStack itemStack){
            if (isRock(itemStack) && itemStack.getItemMeta().getLore() != null){
                try{
                    for (String str: itemStack.getItemMeta().getLore()){
                        if (str.contains(lore)){
                            return Double.parseDouble(str.split(lore)[1]);
                        }
                    }
                }catch (Exception ignored){}

            }
            return 0;
        }
        public static void setTemp(ItemStack itemStack, double temp){
            temp = ScorchingSun.round(temp);
            itemStack.getItemMeta().setLore(Collections.singletonList(lore + temp));
        }
        public static void addTemp(ItemStack itemStack, double temp){
            setTemp(itemStack, getTemp(itemStack)+temp);
        }
    }
}
