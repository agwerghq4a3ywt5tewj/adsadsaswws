package com.example.minecraftplugin.enchants.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.example.minecraftplugin.enchants.CustomEnchantment;

/**
 * Enhanced Sharpness enchantment - extends vanilla sharpness to level 8
 */
public class SharpnessX extends CustomEnchantment {
    
    public SharpnessX(NamespacedKey key) {
        super(key, "SHARPNESS_X", 8); // Vanilla max 5 + 3 = 8
    }
    
    @Override
    public void applyEffect(Player player, int level, Object... args) {
        if (args.length > 0 && args[0] instanceof Double) {
            double damage = (Double) args[0];
            
            // Enhanced sharpness - 0.5 damage per level up to 4.0 at level 8
            double bonusDamage = level * 0.5;
            
            // The actual damage modification is handled in EnchantmentManager
        }
    }
    
    @Override
    public boolean conflictsWithEnchantment(Enchantment other) {
        return other.getKey().getKey().contains("sharpness") && !other.equals(this) ||
               other.getKey().getKey().contains("smite") ||
               other.getKey().getKey().contains("bane_of_arthropods");
    }
}