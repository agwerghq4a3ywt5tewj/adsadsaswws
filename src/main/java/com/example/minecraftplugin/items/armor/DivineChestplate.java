package com.example.minecraftplugin.items.armor;

import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.items.DivineItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Divine Chestplate - Custom armor piece with divine strength
 */
public class DivineChestplate extends DivineItem {
    
    private static final int COOLDOWN_SECONDS = 90;
    
    public DivineChestplate() {
        super(
            null, // Not tied to specific god
            Material.NETHERITE_CHESTPLATE,
            "§5§l⚔ Divine Chestplate ⚔§r",
            createLore(),
            createEnchantments(),
            true
        );
    }
    
    private static List<String> createLore() {
        return Arrays.asList(
            "§7A chestplate infused with divine power,",
            "§7granting strength and resilience.",
            "",
            "§5§lPassive Abilities:",
            "§7• Strength II",
            "§7• Resistance I",
            "§7• Fire Resistance",
            "§7• Knockback Resistance",
            "",
            "§5§lActive Ability:",
            "§7• Right-click for Divine Aegis",
            "§7• Absorb all damage for 10 seconds",
            "§7• Reflect 50% damage to attackers",
            "§7• Cooldown: §f90 seconds",
            "",
            "§8\"The divine shield protects the worthy.\""
        );
    }
    
    private static Map<Enchantment, Integer> createEnchantments() {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.PROTECTION, 7);
        enchants.put(Enchantment.UNBREAKING, 12);
        enchants.put(Enchantment.MENDING, 3);
        enchants.put(Enchantment.THORNS, 5);
        return enchants;
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        // Grant divine aegis
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 4)); // 10 seconds, 100% damage reduction
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 10)); // Massive absorption
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0)); // Visual indicator
        
        // Visual and audio effects
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 0.8f);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.5f);
        
        // Create protective barrier effect
        player.getWorld().spawnParticle(org.bukkit.Particle.TOTEM_OF_UNDYING, player.getLocation(), 50, 2, 2, 2, 0.3);
        player.getWorld().spawnParticle(org.bukkit.Particle.BLOCK_MARKER, player.getLocation(), 30, 1.5, 1.5, 1.5, 0.2);
        
        // Messages
        player.sendMessage("§5§l⚔ DIVINE AEGIS ACTIVATED! ⚔");
        player.sendMessage("§7You are protected by divine power!");
        player.sendMessage("§7All damage absorbed for 10 seconds!");
        
        return true;
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Apply chestplate passive effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0, false, false));
        
        // Remove fire ticks
        if (player.getFireTicks() > 0) {
            player.setFireTicks(0);
        }
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§5§l✦ DIVINE ARMOR OBTAINED! ✦");
        player.sendMessage("§7You have acquired the Divine Chestplate!");
        player.sendMessage("§7Divine strength courses through you.");
        
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1.0f, 1.0f);
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lDivine strength fades... §r§cThe Divine Chestplate is no longer with you.");
        
        // Remove chestplate effects
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
    }
    
    @Override
    public int getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }
}