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
 * Tutorial Divine Item - A simple divine item for teaching players the mechanics
 * 
 * Abilities:
 * - Passive: Jump Boost I (demonstrates passive effects)
 * - Active: Right-click for Speed Boost (demonstrates active abilities)
 * - Theme: Learning, guidance, new beginnings
 */
public class TutorialDivineItem extends DivineItem {
    
    private static final int COOLDOWN_SECONDS = 10; // Short cooldown for tutorial
    
    public TutorialDivineItem() {
        super(
            GodType.TUTORIAL,
            Material.WOODEN_SWORD,
            "§a§l✦ Staff of Guidance ✦§r",
            createLore(),
            createEnchantments(),
            true
        );
    }
    
    private static List<String> createLore() {
        return Arrays.asList(
            "§7A simple staff imbued with guiding energy,",
            "§7perfect for learning divine mechanics.",
            "",
            "§a§lPassive Abilities:",
            "§7• Jump Boost I",
            "§7• Shows how passive effects work",
            "",
            "§a§lActive Ability:",
            "§7• Right-click for Speed Boost II",
            "§7• Duration: §f10 seconds",
            "§7• Cooldown: §f10 seconds",
            "",
            "§e§lTutorial Item:",
            "§7This item demonstrates how divine items work.",
            "§7Try right-clicking to use the active ability!",
            "",
            "§8\"Every master was once a beginner.\""
        );
    }
    
    private static Map<Enchantment, Integer> createEnchantments() {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.UNBREAKING, 3);
        enchants.put(Enchantment.MENDING, 1);
        return enchants;
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        // Simple speed boost for tutorial
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1)); // 10 seconds, Speed II
        
        // Visual and audio effects
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        
        // Messages
        player.sendMessage("§a§l✦ GUIDANCE ACTIVATED! ✦");
        player.sendMessage("§7You feel the guiding spirit's blessing!");
        player.sendMessage("§7Speed Boost II for 10 seconds!");
        player.sendMessage("");
        player.sendMessage("§e§lTutorial: §r§eThis is how divine item abilities work!");
        player.sendMessage("§7Each divine item has unique active and passive abilities.");
        
        return true;
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Simple jump boost for tutorial
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 0, false, false));
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§a§l✦ TUTORIAL DIVINE POWER AWAKENED! ✦");
        player.sendMessage("§7You have received the Staff of Guidance!");
        player.sendMessage("§7This tutorial item will teach you about divine powers.");
        player.sendMessage("");
        player.sendMessage("§e§lTutorial Instructions:");
        player.sendMessage("§7• Notice the Jump Boost I effect (passive ability)");
        player.sendMessage("§7• Right-click to activate Speed Boost II (active ability)");
        player.sendMessage("§7• Observe the cooldown system in action");
        player.sendMessage("§7• This item will be removed when the tutorial ends");
        
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.0f);
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§e§lTutorial Complete! §r§eThe Staff of Guidance has served its purpose.");
        player.sendMessage("§7You now understand how divine items work!");
        player.sendMessage("§7Go forth and collect real divine fragments!");
        
        // Remove tutorial effects
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        player.removePotionEffect(PotionEffectType.SPEED);
    }
    
    @Override
    public int getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }
}