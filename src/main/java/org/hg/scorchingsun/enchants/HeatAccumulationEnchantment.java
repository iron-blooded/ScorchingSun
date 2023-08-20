package org.hg.scorchingsun.enchants;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.hg.scorchingsun.ScorchingSun;

public class HeatAccumulationEnchantment extends Enchantment {

    public HeatAccumulationEnchantment() {
        super(NamespacedKey.minecraft(getEchName()));
    }

    public static String getEchName(){
        return "heat_accumulation";
    }
    public static String getOrName(){
        return "Аккумулирование тепла";
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return true; // Можно накладывать на любой предмет
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
        return false; // Не конфликтует с другими зачарованиями
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR; // Применимо ко всем предметам
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public int getMaxLevel() {
        return 3; // Максимальный уровень - 1
    }

    @Override
    public int getStartLevel() {
        return 0;
    }
    @Override
    public String getName() {
        return getOrName(); // Название зачарования
    }
    public static void registerEnchantment(ScorchingSun plugin) {
        // Проверка, зарегистрировано ли уже зачарование с таким ключом
        if (Enchantment.getByKey(NamespacedKey.minecraft(getEchName())) == null) {
            Enchantment.registerEnchantment(new HeatAccumulationEnchantment());
        }
    }
}
