package com.example.minecraftplugin.enchants.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.example.minecraftplugin.enchants.CustomEnchantment;

/**
 * Vampirism enchantment - heals player for percentage of damage dealt
 */
public class Vampirism extends CustomEnchantment {
    
    public Vampirism(NamespacedKey key) {
        super(key, "VAMPIRISM", 5);
    }
    
    @Override
    public void applyEffect(Player player, int level, Object... args) {
        if (args.length > 0 && args[0] instanceof Double) {
            double damage = (Double) args[0];
            
            // Heal for 5% per level of damage dealt (max 25% at level 5)
            double healAmount = damage * (level * 0.05);
            
            // The actual healing is handled in EnchantmentManager
        }
    }
    
    @Override
    public boolean conflictsWithEnchantment(Enchantment other) {
        return false; // Can stack with Lifesteal for extreme sustain
    }
}