package com.example.minecraftplugin.enchants;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

/**
 * Protection V enchantment - extends vanilla protection to level 5
 */
public class ProtectionV extends CustomEnchantment {
    
    public ProtectionV(NamespacedKey key) {
        super(key, "PROTECTION_V", 5);
    }
    
    @Override
    public void applyEffect(Player player, int level, Object... args) {
        // This method would be called from the EnchantmentManager
        // when damage is taken to calculate protection
        
        if (args.length > 0 && args[0] instanceof Double) {
            double damage = (Double) args[0];
            
            // Calculate protection reduction
            // Level 5 provides 20% damage reduction (4% per level)
            double reduction = Math.min(0.20, level * 0.04);
            double reducedDamage = damage * (1.0 - reduction);
            
            // The actual damage modification would be handled in EnchantmentManager
        }
    }
    
    @Override
    public boolean conflictsWithEnchantment(Enchantment other) {
        // Conflicts with other protection enchantments
        return other.getKey().getKey().contains("protection") && !other.equals(this);
    }
}