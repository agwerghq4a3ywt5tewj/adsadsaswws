package com.example.minecraftplugin.enchants.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.example.minecraftplugin.enchants.CustomEnchantment;

/**
 * Guardian Aura enchantment - grants beneficial effects to nearby allies
 */
public class GuardianAura extends CustomEnchantment {
    
    public GuardianAura(NamespacedKey key) {
        super(key, "GUARDIAN_AURA", 3);
    }
    
    @Override
    public void applyEffect(Player player, int level, Object... args) {
        // Effect is handled in EnchantmentManager during periodic checks
        // Level determines the strength and range of the aura
    }
    
    @Override
    public boolean conflictsWithEnchantment(Enchantment other) {
        return false; // Doesn't conflict with other enchantments
    }
}