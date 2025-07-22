package com.example.minecraftplugin.enchants.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.example.minecraftplugin.enchants.CustomEnchantment;

/**
 * Enhanced Efficiency enchantment - extends vanilla efficiency to level 8
 */
public class EfficiencyX extends CustomEnchantment {
    
    public EfficiencyX(NamespacedKey key) {
        super(key, "EFFICIENCY_X", 8); // Vanilla max 5 + 3 = 8
    }
    
    @Override
    public void applyEffect(Player player, int level, Object... args) {
        // Enhanced mining speed - handled in block break events
    }
    
    @Override
    public boolean conflictsWithEnchantment(Enchantment other) {
        return other.getKey().getKey().contains("efficiency") && !other.equals(this);
    }
}