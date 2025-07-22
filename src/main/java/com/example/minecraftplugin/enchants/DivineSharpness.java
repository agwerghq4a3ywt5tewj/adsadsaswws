package com.example.minecraftplugin.enchants;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

/**
 * Divine Sharpness enchantment - enhanced sharpness with divine power
 */
public class DivineSharpness extends CustomEnchantment {
    
    public DivineSharpness(NamespacedKey key) {
        super(key, "DIVINE_SHARPNESS", 7);
    }
    
    @Override
    public void applyEffect(Player player, int level, Object... args) {
        // This would be called when dealing damage
        if (args.length > 0 && args[0] instanceof Double) {
            double damage = (Double) args[0];
            
            // Divine Sharpness provides more damage than regular sharpness
            // Level 7 provides +3.5 damage (0.5 per level)
            double bonusDamage = level * 0.5;
            
            // Additional divine effects at higher levels
            if (level >= 5) {
                // Chance to apply divine effects
                if (Math.random() < 0.1 * (level - 4)) {
                    // Apply glowing effect to target
                    // This would be handled in EnchantmentManager
                }
            }
        }
    }
    
    @Override
    public boolean conflictsWithEnchantment(Enchantment other) {
        return other.getKey().getKey().contains("sharpness") && !other.equals(this);
    }
}