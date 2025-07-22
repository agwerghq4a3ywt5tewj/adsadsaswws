package com.example.minecraftplugin.enchants.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.example.minecraftplugin.enchants.CustomEnchantment;

/**
 * Vein Miner enchantment - breaks all connected blocks of same type
 */
public class VeinMiner extends CustomEnchantment {
    
    public VeinMiner(NamespacedKey key) {
        super(key, "VEIN_MINER", 1);
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