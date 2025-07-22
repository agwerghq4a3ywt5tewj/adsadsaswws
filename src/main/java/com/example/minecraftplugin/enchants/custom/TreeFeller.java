package com.example.minecraftplugin.enchants.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.example.minecraftplugin.enchants.CustomEnchantment;

/**
 * Tree Feller enchantment - breaks entire trees
 */
public class TreeFeller extends CustomEnchantment {
    
    public TreeFeller(NamespacedKey key) {
        super(key, "TREE_FELLER", 1);
    }
    
    @Override
    public void applyEffect(Player player, int level, Object... args) {
        // Effect is handled in EnchantmentManager during BlockBreakEvent
    }
    
    @Override
    public boolean conflictsWithEnchantment(Enchantment other) {
        return false; // Doesn't conflict with other enchantments
    }
}