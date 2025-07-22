package com.example.minecraftplugin.enchants.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.example.minecraftplugin.enchants.CustomEnchantment;

/**
 * Lifesteal enchantment - heals player for percentage of damage dealt
 */
public class Lifesteal extends CustomEnchantment {
    
    public Lifesteal(NamespacedKey key) {
        super(key, "LIFESTEAL", 5);
    }
    
    @Override
    public void applyEffect(Player player, int level, Object... args) {
        if (args.length > 0 && args[0] instanceof Double) {
            double damage = (Double) args[0];
            
            // Heal for 2% per level of damage dealt
            double healAmount = damage * (level * 0.02);
            
            // The actual healing is handled in EnchantmentManager
        }
    }
    
    @Override
    public boolean conflictsWithEnchantment(Enchantment other) {
        return false; // Doesn't conflict with other enchantments
    }
}