package com.example.minecraftplugin.listeners;

import com.example.minecraftplugin.MinecraftPlugin;
import org.bukkit.Color;
import com.example.minecraftplugin.managers.EnderDragonCombatManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles Sekiro-style parrying and deathblow mechanics for the Ender Dragon
 */
public class DragonParryListener implements Listener {
    
    private final MinecraftPlugin plugin;
    
    public DragonParryListener(MinecraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handle player attacks on the Ender Dragon for parrying
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof EnderDragon)) return;
        
        Player player = (Player) event.getDamager();
        EnderDragon dragon = (EnderDragon) event.getEntity();
        
        // Get the active EnderDragonCombatManager instance
        EnderDragonCombatManager combatManager = plugin.getEnderDragonCombatManager();
        if (combatManager == null) return;
        
        // Check if this is during a parry window
        if (combatManager.isParryWindowActive()) {
            // This is a successful parry!
            event.setCancelled(true); // Prevent direct damage from the parry hit
            
            // Deal significant posture damage
            combatManager.dealPostureDamage(25.0, player);
            
            // Perfect parry feedback
            player.sendMessage("§a§l⚔ PERFECT PARRY! ⚔");
            player.sendMessage("§7Attack: §f" + combatManager.getCurrentAttackType());
            player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.8f);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f);
            
            // Visual effect at player location
            player.getWorld().spawnParticle(org.bukkit.Particle.CRIT, 
                player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.3);
            player.getWorld().spawnParticle(org.bukkit.Particle.ENCHANTED_HIT, 
                player.getLocation().add(0, 1, 0), 15, 1, 1, 1, 0.2);
            
            // Reset parry window immediately after successful parry
            combatManager.setParryWindowActive(false);
            
            plugin.getLogger().info(player.getName() + " successfully parried dragon attack: " + 
                                   combatManager.getCurrentAttackType());
        } else {
            // Regular hit - check if dragon is posture broken for bonus damage
            if (combatManager.isPostureBroken()) {
                // Increase damage during vulnerability
                event.setDamage(event.getDamage() * 2.0);
                player.sendMessage("§e§lVulnerability Bonus! §7+100% damage");
            } else {
                // Normal damage, but also deal minor posture damage
                combatManager.dealPostureDamage(5.0, player);
            }
        }
    }
    
    /**
     * Handle right-click interactions for deathblow attempts
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR && 
            event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        
        // Only allow deathblow with empty hand or sword
        if (heldItem != null && heldItem.getType() != Material.AIR && 
            !heldItem.getType().name().contains("SWORD")) {
            return;
        }
        
        // Check if player is looking at the dragon
        Entity targetEntity = player.getTargetEntity(8); // Get entity player is looking at within 8 blocks
        if (!(targetEntity instanceof EnderDragon)) return;
        
        EnderDragon dragon = (EnderDragon) targetEntity;
        EnderDragonCombatManager combatManager = plugin.getEnderDragonCombatManager();
        
        if (combatManager != null && combatManager.isPostureBroken()) {
            // Attempt deathblow
            boolean success = combatManager.performDeathblow(player);
            if (success) {
                event.setCancelled(true); // Prevent default interaction
            }
        } else if (combatManager != null) {
            // Dragon is not vulnerable
            player.sendMessage("§c§lThe dragon's posture is not broken!");
            player.sendMessage("§7Parry its attacks to break its posture first.");
        }
    }
}