package com.example.minecraftplugin.enchants.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.example.minecraftplugin.enchants.CustomEnchantment;

/**
 * Density enchantment - applies extreme knockback and stunning effects
 */
public class Density extends CustomEnchantment {
    
    public Density(NamespacedKey key) {
        super(key, "DENSITY", 10);
    }
    
    @Override
    public void applyEffect(Player player, int level, Object... args) {
        if (args.length > 0 && args[0] instanceof Double) {
            double damage = (Double) args[0];
            
            // Density provides extreme knockback scaling with level
            // Level 10 provides massive knockback and brief stun
            double knockbackMultiplier = 1.0 + (level * 0.5); // Up to 6x knockback at level 10
            
            // The actual knockback and stun effects are handled in EnchantmentManager
        }
    }
    
    @Override
    public boolean conflictsWithEnchantment(Enchantment other) {
        return other.getKey().getKey().contains("knockback") && !other.equals(this);
    }
}