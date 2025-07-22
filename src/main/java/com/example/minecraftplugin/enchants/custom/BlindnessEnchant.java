package com.example.minecraftplugin.enchants.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.example.minecraftplugin.enchants.CustomEnchantment;

/**
 * Blindness enchantment - chance to apply blindness on hit
 */
public class BlindnessEnchant extends CustomEnchantment {
    
    public BlindnessEnchant(NamespacedKey key) {
        super(key, "BLINDNESS", 3);
    }
    
    @Override
    public void applyEffect(Player player, int level, Object... args) {
        // 10% chance per level to apply blindness
        double chance = level * 0.10;
        
        if (Math.random() < chance) {
            // Apply blindness effect - handled in EnchantmentManager
        }
    }
    
    @Override
    public boolean conflictsWithEnchantment(Enchantment other) {
        return false; // Doesn't conflict with other enchantments
    }
}