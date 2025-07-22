package com.example.minecraftplugin.items;

import com.example.minecraftplugin.enums.GodType;
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
 * Shadow Mantle - Divine item for the Shadow God
 * 
 * Abilities:
 * - Passive: Umbral form (invisibility in darkness), shadow step
 * - Active: Right-click to become one with shadows temporarily
 * - Theme: Stealth, darkness, assassination
 */
public class ShadowMantle extends DivineItem {
    
    private static final int COOLDOWN_SECONDS = 30;
    private static final int SHADOW_FORM_DURATION = 200; // 10 seconds
    
    public ShadowMantle() {
        super(
            GodType.SHADOW,
            Material.LEATHER_CHESTPLATE,
            "§0§l🌑 Shadow Mantle 🌑§r",
            createLore(),
            createEnchantments(),
            true
        );
    }
    
    private static List<String> createLore() {
        return Arrays.asList(
            "§7A cloak woven from the essence of darkness,",
            "§7granting mastery over shadow and stealth.",
            "",
            "§0§lPassive Abilities:",
            "§7• Umbral Form: Enhanced invisibility in darkness",
            "§7• Shadow Step: No fall damage",
            "§7• Enhanced Night Vision in dark areas",
            "§7• Shadow Resistance: Damage reduction in darkness",
            "§7• Stealth Aura: Speed boost when invisible",
            "",
            "§0§lActive Ability:",
            "§7• Right-click for Enhanced Shadow Form",
            "§7• Complete invisibility for 15 seconds",
            "§7• Speed III and Jump Boost III",
            "§7• Absorption hearts while in shadow form",
            "§7• Cooldown: §f30 seconds",
            "",
            "§8\"In shadow, find truth. In darkness, find power.\""
        );
    }
    
    private static Map<Enchantment, Integer> createEnchantments() {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.PROTECTION, 4);
        enchants.put(Enchantment.UNBREAKING, 10);
        enchants.put(Enchantment.MENDING, 1);
        return enchants;
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        // Enter shadow form
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, SHADOW_FORM_DURATION + 100, 0)); // 15 seconds
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, SHADOW_FORM_DURATION + 100, 2)); // Speed III
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, SHADOW_FORM_DURATION + 100, 2)); // Jump Boost III
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, SHADOW_FORM_DURATION + 100, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, SHADOW_FORM_DURATION + 100, 2)); // 5 absorption hearts
        
        // Visual and audio effects
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 0.8f);
        player.playSound(player.getLocation(), Sound.BLOCK_SCULK_SENSOR_CLICKING, 0.8f, 0.5f);
        
        // Messages
        player.sendMessage("§0§l🌑 SHADOW FORM ACTIVATED! 🌑");
        player.sendMessage("§7You become one with the eternal darkness!");
        player.sendMessage("§7Enhanced speed, jumping, and protection!");
        
        return true;
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Check light level for umbral form
        int lightLevel = player.getLocation().getBlock().getLightLevel();
        
        if (lightLevel <= 3) {
            // Enhanced invisibility in darkness
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 80, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 60, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 1, false, false)); // Damage reduction
        }
        
        // Shadow step - no fall damage
        player.setFallDistance(0);
        
        // Enhanced movement in darkness
        if (lightLevel <= 7) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, false, false)); // Speed II
        }
        
        // Stealth aura - speed boost when invisible
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 2, false, false)); // Speed III when invisible
        }
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§0§l✦ DIVINE POWER AWAKENED! ✦");
        player.sendMessage("§7The Shadow Mantle wraps you in eternal darkness!");
        player.sendMessage("§7You can now move unseen through the shadows.");
        player.sendMessage("§7Right-click for shadow form, hide in darkness for invisibility.");
        
        player.playSound(player.getLocation(), Sound.BLOCK_SCULK_CATALYST_BLOOM, 0.8f, 0.8f);
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lDivine power fades... §r§cThe Shadow Mantle is no longer with you.");
        player.sendMessage("§7The shadows no longer heed your call.");
        
        // Remove shadow effects
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
    }
    
    @Override
    public int getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }
}