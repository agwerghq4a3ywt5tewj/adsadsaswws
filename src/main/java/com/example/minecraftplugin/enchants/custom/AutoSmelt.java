package com.example.minecraftplugin.enchants.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.example.minecraftplugin.enchants.CustomEnchantment;

/**
 * Auto Smelt enchantment - automatically smelts mined blocks
 */
public class AutoSmelt extends CustomEnchantment {
    
    public AutoSmelt(NamespacedKey key) {
        super(key, "AUTO_SMELT", 1);
    }
    
    @Override
    public void applyEffect(Player player, int level, Object... args) {
        // Effect is handled in EnchantmentManager during BlockBreakEvent
    }
    
    @Override
    public boolean conflictsWithEnchantment(Enchantment other) {
        return other.getKey().getKey().contains("silk_touch"); // Conflicts with silk touch
    }
}