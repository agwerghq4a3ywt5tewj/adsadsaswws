package com.example.minecraftplugin.enchants.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.example.minecraftplugin.enchants.CustomEnchantment;

/**
 * Enhanced Unbreaking enchantment - extends vanilla unbreaking to level 6
 */
public class UnbreakingX extends CustomEnchantment {
    
    public UnbreakingX(NamespacedKey key) {
        super(key, "UNBREAKING_X", 6); // Vanilla max 3 + 3 = 6
    }
    
    @Override
    public void applyEffect(Player player, int level, Object... args) {
        // Enhanced durability - handled in item damage events
    }
    
    @Override
    public boolean conflictsWithEnchantment(Enchantment other) {
        return other.getKey().getKey().contains("unbreaking") && !other.equals(this);
    }
}