package com.example.minecraftplugin.enchants.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.example.minecraftplugin.enchants.CustomEnchantment;

/**
 * Enhanced Protection enchantment - extends vanilla protection to level 7
 */
public class ProtectionX extends CustomEnchantment {
    
    public ProtectionX(NamespacedKey key) {
        super(key, "PROTECTION_X", 7); // Vanilla max 4 + 3 = 7
    }
    
    @Override
    public void applyEffect(Player player, int level, Object... args) {
        if (args.length > 0 && args[0] instanceof Double) {
            double damage = (Double) args[0];
            
            // Enhanced protection - 4% per level up to 28% at level 7
            double reduction = Math.min(0.28, level * 0.04);
            double reducedDamage = damage * (1.0 - reduction);
            
            // The actual damage modification is handled in EnchantmentManager
        }
    }
    
    @Override
    public boolean conflictsWithEnchantment(Enchantment other) {
        return other.getKey().getKey().contains("protection") && !other.equals(this);
    }
}