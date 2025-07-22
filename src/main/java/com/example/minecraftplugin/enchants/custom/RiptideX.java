package com.example.minecraftplugin.enchants.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.example.minecraftplugin.enchants.CustomEnchantment;

/**
 * Enhanced Riptide enchantment - extends vanilla riptide to level 6
 * Provides enhanced water propulsion and mobility
 */
public class RiptideX extends CustomEnchantment {
    
    public RiptideX(NamespacedKey key) {
        super(key, "RIPTIDE_X", 6); // Vanilla max 3 + 3 = 6
    }
    
    @Override
    public void applyEffect(Player player, int level, Object... args) {
        // Enhanced riptide effects:
        // Level 4: 1.5x vanilla speed
        // Level 5: 2.0x vanilla speed + works in shallow water
        // Level 6: 2.5x vanilla speed + works without rain/water (divine power)
        
        if (args.length > 0 && args[0] instanceof Double) {
            double baseVelocity = (Double) args[0];
            
            // Calculate enhanced velocity based on level
            double multiplier = 1.0 + (level - 3) * 0.5; // Levels 4-6 get 0.5x bonus each
            double enhancedVelocity = baseVelocity * multiplier;
            
            // The actual velocity modification is handled in EnchantmentManager
        }
    }
    
    @Override
    public boolean conflictsWithEnchantment(Enchantment other) {
        // Conflicts with loyalty and channeling (vanilla riptide conflicts)
        return other.getKey().getKey().contains("loyalty") ||
               other.getKey().getKey().contains("channeling") ||
               (other.getKey().getKey().contains("riptide") && !other.equals(this));
    }
}