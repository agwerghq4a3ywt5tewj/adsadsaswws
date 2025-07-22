package com.example.minecraftplugin.enchants.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.example.minecraftplugin.enchants.CustomEnchantment;

/**
 * Armor Piercing enchantment - ignores a percentage of target's armor
 */
public class ArmorPiercing extends CustomEnchantment {
    
    public ArmorPiercing(NamespacedKey key) {
        super(key, "ARMOR_PIERCING", 7);
    }
    
    @Override
    public void applyEffect(Player player, int level, Object... args) {
        if (args.length > 0 && args[0] instanceof Double) {
            double damage = (Double) args[0];
            
            // Armor piercing ignores 10% of armor per level (max 70% at level 7)
            double armorPiercingPercent = Math.min(0.70, level * 0.10);
            
            // The actual armor piercing calculation is handled in EnchantmentManager
        }
    }
    
    @Override
    public boolean conflictsWithEnchantment(Enchantment other) {
        return false; // Doesn't conflict with other enchantments
    }
}