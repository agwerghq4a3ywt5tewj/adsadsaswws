package com.example.minecraftplugin.enchants.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.example.minecraftplugin.enchants.CustomEnchantment;

/**
 * Soulbound enchantment - item remains with player on death
 */
public class Soulbound extends CustomEnchantment {
    
    public Soulbound(NamespacedKey key) {
        super(key, "SOULBOUND", 1);
    }
    
    @Override
    public void applyEffect(Player player, int level, Object... args) {
        // Effect is handled in EnchantmentManager during PlayerDeathEvent
    }
    
    @Override
    public boolean conflictsWithEnchantment(Enchantment other) {
        return false; // Doesn't conflict with other enchantments
    }
}