package com.example.minecraftplugin.items;

import com.example.minecraftplugin.enums.GodType;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mace of Annihilation - Extremely busted PvP weapon
 * 
 * Abilities:
 * - Passive: Speed, Jump Boost, Resistance, chance to slow attackers
 * - Active: Massive AoE damage with extreme debuffs and self-buffs
 * - Theme: Pure destruction, chaos, annihilation
 */
public class MaceOfAnnihilation extends DivineItem {
    
    private static final int AOE_RADIUS = 15;
    private static final int COOLDOWN_SECONDS = 75;
    private static final double AOE_DAMAGE = 50.0; // Massive true damage
    
    public MaceOfAnnihilation() {
        super(
            null, // Not tied to any specific god - standalone PvP artifact
            Material.MACE,
            "§4§l⚔ Mace of Annihilation ⚔§r",
            createLore(),
            createEnchantments(),
            true
        );
    }
    
    private static List<String> createLore() {
        return Arrays.asList(
            "§7A weapon forged from pure chaos and destruction,",
            "§7designed to obliterate all who stand before it.",
            "§7This mace knows no mercy, no restraint, no limits.",
            "",
            "§4§lPassive Abilities:",
            "§7• Enhanced mobility and defense",
            "§7• Chance to slow attackers",
            "§7• Menacing aura of destruction",
            "",
            "§4§lActive Ability:",
            "§7• Right-click for Annihilation Wave",
            "§7• Massive AoE true damage (15 block radius)",
            "§7• Extreme debuffs to all enemies",
            "§7• Powerful self-buffs",
            "§7• Cooldown: §f75 seconds",
            "",
            "§c§lWARNING: §r§cThis weapon is extremely overpowered!",
            "§c§lUse responsibly in PvP scenarios.",
            "",
            "§8\"I am become death, destroyer of worlds.\""
        );
    }
    
    private static Map<Enchantment, Integer> createEnchantments() {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        
        // Extreme vanilla enchantments
        enchants.put(Enchantment.SHARPNESS, 10);
        enchants.put(Enchantment.KNOCKBACK, 8);
        enchants.put(Enchantment.UNBREAKING, 25);
        enchants.put(Enchantment.MENDING, 10);
        enchants.put(Enchantment.SWEEPING_EDGE, 5);
        
        return enchants;
    }
    
    @Override
    public boolean onRightClick(Player player, ItemStack item) {
        // ANNIHILATION WAVE - Extremely overpowered AoE attack
        
        List<Entity> nearbyEntities = player.getNearbyEntities(AOE_RADIUS, AOE_RADIUS, AOE_RADIUS);
        int enemiesAffected = 0;
        int playersAffected = 0;
        
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                
                // Deal massive true damage (bypasses armor)
                target.damage(AOE_DAMAGE, player);
                
                // Apply extreme debuffs
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 400, 4)); // Wither V for 20s
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 600, 4)); // Slowness V for 30s
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 4)); // Weakness V for 30s
                target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 400, 2)); // Nausea III for 20s
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0)); // Blindness for 10s
                target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 600, 4)); // Mining Fatigue V for 30s
                
                // Extreme knockback
                Vector direction = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                direction.setY(1.0); // Strong upward component
                direction.multiply(5.0); // Massive knockback
                target.setVelocity(direction);
                
                if (entity instanceof Player) {
                    playersAffected++;
                } else {
                    enemiesAffected++;
                }
            }
        }
        
        // Grant wielder extreme temporary buffs
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 600, 9)); // Strength X for 30s
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 600, 4)); // Resistance V for 30s
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 4)); // Regeneration V for 30s
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 19)); // Absorption XX for 30s (40 extra hearts)
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 3)); // Speed IV for 30s
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 600, 3)); // Jump Boost IV for 30s
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 600, 0)); // Fire Resistance for 30s
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 600, 0)); // Water Breathing for 30s
        
        // Heal to full
        player.setHealth(player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setSaturation(20);
        
        // Over-the-top visual and audio effects
        for (int i = 0; i < 10; i++) {
            player.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_EMITTER, 
                player.getLocation().add(0, 1, 0), 5);
        }
        
        // Create expanding shockwave effect
        for (int radius = 1; radius <= AOE_RADIUS; radius++) {
            final int currentRadius = radius;
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                org.bukkit.Bukkit.getPluginManager().getPlugin("MinecraftPlugin"), 
                () -> {
                    for (int i = 0; i < 32; i++) {
                        double angle = (i / 32.0) * 360;
                        double x = player.getLocation().getX() + currentRadius * Math.cos(Math.toRadians(angle));
                        double z = player.getLocation().getZ() + currentRadius * Math.sin(Math.toRadians(angle));
                        org.bukkit.Location effectLoc = new org.bukkit.Location(player.getWorld(), x, player.getLocation().getY(), z);
                        
                        player.getWorld().spawnParticle(org.bukkit.Particle.DUST, effectLoc, 5, 0.2, 0.2, 0.2, 0,
                            new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 2.0f));
                        player.getWorld().spawnParticle(org.bukkit.Particle.LARGE_SMOKE, effectLoc, 3, 0.1, 0.1, 0.1, 0.05);
                    }
                }, radius * 2L);
        }
        
        // Lightning strikes for dramatic effect
        for (int i = 0; i < 8; i++) {
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                org.bukkit.Bukkit.getPluginManager().getPlugin("MinecraftPlugin"), 
                () -> {
                    org.bukkit.Location strikeLoc = player.getLocation().add(
                        (Math.random() - 0.5) * AOE_RADIUS * 2,
                        0,
                        (Math.random() - 0.5) * AOE_RADIUS * 2
                    );
                    player.getWorld().strikeLightningEffect(strikeLoc);
                }, i * 5L);
        }
        
        // Epic sound sequence
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.3f);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 2.0f, 0.1f);
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.8f);
        
        // Messages
        player.sendMessage("§4§l⚔ ANNIHILATION WAVE UNLEASHED! ⚔");
        player.sendMessage("§7Devastated " + (enemiesAffected + playersAffected) + " entities!");
        player.sendMessage("§7Players affected: §c" + playersAffected);
        player.sendMessage("§7Mobs destroyed: §c" + enemiesAffected);
        player.sendMessage("§7You are empowered with divine destruction!");
        
        // Server announcement
        org.bukkit.Bukkit.broadcastMessage("§4§l" + player.getName() + " §r§4has unleashed the Mace of Annihilation!");
        org.bukkit.Bukkit.broadcastMessage("§c§lWARNING: §r§cExtreme destruction detected in the area!");
        
        return true;
    }
    
    @Override
    public void applyPassiveEffects(Player player, ItemStack item) {
        // Constant mobility and defensive buffs
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, false, false)); // Speed II
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 1, false, false)); // Jump Boost II
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0, false, false)); // Resistance I
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 60, 1, false, false)); // Absorption II
        
        // Chance to slow attackers when hit
        if (Math.random() < 0.1) { // 10% chance per tick
            for (Entity entity : player.getNearbyEntities(3, 3, 3)) {
                if (entity instanceof LivingEntity && entity instanceof org.bukkit.entity.Monster) {
                    LivingEntity target = (LivingEntity) entity;
                    // Apply slowness to hostile mobs near the player (menacing aura effect)
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                }
            }
        }
        
        // Menacing aura particles
        if (Math.random() < 0.3) { // 30% chance per tick
            player.getWorld().spawnParticle(org.bukkit.Particle.DUST, 
                player.getLocation().add(0, 1, 0), 3, 0.5, 0.5, 0.5, 0.0,
                new org.bukkit.Particle.DustOptions(org.bukkit.Color.MAROON, 1.0f));
            player.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, 
                player.getLocation().add(0, 1, 0), 2, 0.3, 0.3, 0.3, 0.02);
        }
    }
    
    @Override
    public void onObtained(Player player, ItemStack item) {
        player.sendMessage("§4§l★ WEAPON OF MASS DESTRUCTION OBTAINED! ★");
        player.sendMessage("§7You have acquired the Mace of Annihilation!");
        player.sendMessage("§c§lWARNING: §r§cThis weapon is extremely overpowered!");
        player.sendMessage("§7Use with extreme caution in PvP scenarios.");
        player.sendMessage("§7Right-click to unleash annihilation upon your enemies.");
        
        // Epic acquisition effects
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 0.3f);
        player.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_EMITTER, player.getLocation(), 10);
        
        // Server warning
        org.bukkit.Bukkit.broadcastMessage("§4§l⚠ EXTREME DANGER ⚠");
        org.bukkit.Bukkit.broadcastMessage("§c§l" + player.getName() + " §r§chas obtained the Mace of Annihilation!");
        org.bukkit.Bukkit.broadcastMessage("§7All players are advised to exercise extreme caution!");
    }
    
    @Override
    public void onLost(Player player, ItemStack item) {
        player.sendMessage("§c§lDestruction fades... §r§cThe Mace of Annihilation is no longer with you.");
        player.sendMessage("§7The world breathes a sigh of relief.");
        
        // Remove passive effects
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.ABSORPTION);
        
        // Server relief announcement
        org.bukkit.Bukkit.broadcastMessage("§a§l" + player.getName() + " §r§ano longer wields the Mace of Annihilation.");
        org.bukkit.Bukkit.broadcastMessage("§7The immediate threat has passed...");
    }
    
    @Override
    public int getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }
}