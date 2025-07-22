package com.example.minecraftplugin.enchants.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.example.minecraftplugin.enchants.CustomEnchantment;

/**
 * Explosive Arrows enchantment - arrows explode on impact
 */
public class ExplosiveArrows extends CustomEnchantment {
    
    public ExplosiveArrows(NamespacedKey key) {
        super(key, "EXPLOSIVE_ARROWS", 3);
    }
    
    @Override
    public void applyEffect(Player player, int level, Object... args) {
        // Explosion power scales with level (1.0, 2.0, 3.0)
        float explosionPower = level * 1.0f;
        
        // Effect is handled in EnchantmentManager during ProjectileHitEvent
    }
    
    @Override
    public boolean conflictsWithEnchantment(Enchantment other) {
        return false; // Doesn't conflict with other enchantments
    }
}