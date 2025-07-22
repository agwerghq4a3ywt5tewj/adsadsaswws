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
 * Divine Helmet - Custom armor piece with divine protection
 */
public class DivineHelmet extends DivineItem {
    
    private static final int COOLDOWN_SECONDS = 60;
    
    public DivineHelmet() {
        super(
            null, // Not tied to specific god
            Material.NETHERITE_HELMET,
            "§5§l👑 Divine Helmet 👑§r",
            createLore(),
            createEnchantments(),
            true
        );
    }
    
    private static List<String> createLore() {
        return Arrays.asList(
            "§7A helmet forged from divine essence,",
            "§7providing unparalleled protection.",
            "",
            "§5§lPassive Abilities:",
            "§7• Night Vision",
            "§7• Water Breathing",
            "§7• Immunity to blindness",
            "§7• Enhanced awareness",
            "",
            "§5§lActive Ability:",
            "§7• Right-click for Divine Sight",
            "§7• See through walls for 30 seconds",
            "§7• Detect nearby enemies",
            "§7• Cooldown: §f60 seconds",
            "",
            "§8\"Divine protection for the enlightened.\""
        );
    }
    
    private static Map<Enchantment, Integer> createEnchantments() {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.PROTECTION, 6);
        enchants.put(Enchantment.UNBREAKING, 10);
        enchants.put(Enchantment.MENDING, 2);
        enchants.put(Enchantment.RESPIRATION, 5);
        enchants.put(Enchantment.AQUA_AFFINITY, 2);
        return enchants;
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        // Grant divine sight
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 600, 0)); // 30 seconds
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 0)); // See enemies
        
        // Show nearby entities
        int enemyCount = 0;
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(50, 50, 50)) {
            if (entity instanceof org.bukkit.entity.Monster && entity instanceof org.bukkit.entity.LivingEntity) {
                ((org.bukkit.entity.LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 0));
                enemyCount++;
            }
        }
        
        // Visual and audio effects
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_LAUNCH, 1.0f, 1.5f);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.2f);
        
        // Messages
        player.sendMessage("§5§l👑 DIVINE SIGHT ACTIVATED! 👑");
        player.sendMessage("§7Your divine awareness expands!");
        player.sendMessage("§7Detected §f" + enemyCount + "§7 enemies nearby");
        
        return true;
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Apply helmet passive effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 60, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 60, 0, false, false));
        
        // Remove blindness
        if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§5§l✦ DIVINE ARMOR OBTAINED! ✦");
        player.sendMessage("§7You have acquired the Divine Helmet!");
        player.sendMessage("§7Divine protection flows through you.");
        
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1.0f, 1.2f);
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lDivine protection fades... §r§cThe Divine Helmet is no longer with you.");
        
        // Remove helmet effects
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.WATER_BREATHING);
    }
    
    @Override
    public int getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }
}