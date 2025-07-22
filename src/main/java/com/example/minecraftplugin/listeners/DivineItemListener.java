package com.example.minecraftplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.items.ConvergenceNexus;
import com.example.minecraftplugin.items.DivineItem;
import com.example.minecraftplugin.managers.CooldownManager;
import com.example.minecraftplugin.managers.GodManager;

/**
 * Handles divine item interactions and passive effects
 */
public class DivineItemListener implements Listener {
    
    private final MinecraftPlugin plugin;
    private final GodManager godManager;
    private final CooldownManager cooldownManager;
    
    public DivineItemListener(MinecraftPlugin plugin, GodManager godManager, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.godManager = godManager;
        this.cooldownManager = cooldownManager;
        
        // Start passive effects task
        startPassiveEffectsTask();
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-click actions
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR && 
            event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Debug logging (only when debug mode is enabled)
        if (plugin.getConfig().getBoolean("plugin.debug", false)) {
            plugin.getLogger().info("Player " + player.getName() + " right-clicked. Item in hand: " + 
                                   (item != null ? item.getType().name() : "NONE"));
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                plugin.getLogger().info("Item display name: '" + item.getItemMeta().getDisplayName() + "'");
            }
        }
        
        if (item == null || !item.hasItemMeta()) {
            if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                plugin.getLogger().info("Item is null or has no meta, returning");
            }
            return;
        }

        // Check if it's a divine item
        DivineItem divineItem = getDivineItemFromStack(item);
        if (divineItem == null) {
            if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                plugin.getLogger().info("Item not identified as a DivineItem");
            }
            return;
        } else {
            if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                plugin.getLogger().info("DivineItem identified: " + divineItem.getDisplayName() + 
                                       " (God: " + (divineItem.getGodType() != null ? divineItem.getGodType().name() : "NONE") + ")");
            }
        }

        // Check cooldown
        String abilityName = divineItem.getGodType().name() + "_active";
        if (plugin.getConfig().getBoolean("plugin.debug", false)) {
            plugin.getLogger().info("Checking cooldown for ability: " + abilityName);
        }
        
        if (cooldownManager.isOnAbilityCooldown(player, abilityName, divineItem.getCooldownSeconds())) {
            long remaining = cooldownManager.getRemainingCooldown(player, abilityName, divineItem.getCooldownSeconds());
            player.sendMessage("§c§lAbility on cooldown! §r§cWait §f" + remaining + "§c more seconds.");
            if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                plugin.getLogger().info("Ability " + abilityName + " is on cooldown for " + player.getName() + 
                                       " (" + remaining + "s remaining)");
            }
            return;
        } else {
            if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                plugin.getLogger().info("No cooldown, attempting to use ability " + abilityName + " for " + player.getName());
            }
        }

        // Try to use the ability
        boolean abilityUsed = divineItem.onRightClick(player, item);
        if (plugin.getConfig().getBoolean("plugin.debug", false)) {
            plugin.getLogger().info("Ability used result: " + abilityUsed + " for " + divineItem.getDisplayName());
        }

        if (abilityUsed) {
            // Set cooldown
            cooldownManager.setAbilityCooldown(player, abilityName);
            if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                plugin.getLogger().info("Set cooldown for " + abilityName + " (" + divineItem.getCooldownSeconds() + "s)");
            }

            // Create visual effects
            plugin.getVisualEffectsManager().createAbilityActivationEffect(player, divineItem.getGodType(), abilityName);

            // Cancel the event to prevent other interactions
            event.setCancelled(true);
            if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                plugin.getLogger().info("Event cancelled, ability successfully used");
            }
        } else {
            if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                plugin.getLogger().info("Ability was not used (returned false)");
            }
        }
    }
    
    /**
     * Start a task that applies passive effects for divine items
     */
    private void startPassiveEffectsTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    applyPassiveEffectsForPlayer(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second (20 ticks)
    }
    
    /**
     * Apply passive effects for all divine items a player has
     */
    private void applyPassiveEffectsForPlayer(Player player) {
        // Check main hand
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        DivineItem mainHandItem = getDivineItemFromStack(mainHand);
        if (mainHandItem != null) {
            mainHandItem.applyPassiveEffects(player, mainHand);
        }
        
        // Check off hand
        ItemStack offHand = player.getInventory().getItemInOffHand();
        DivineItem offHandItem = getDivineItemFromStack(offHand);
        if (offHandItem != null) {
            offHandItem.applyPassiveEffects(player, offHand);
        }
        
        // Check inventory for divine items (passive effects when carried)
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            
            DivineItem divineItem = getDivineItemFromStack(item);
            if (divineItem != null && divineItem != mainHandItem && divineItem != offHandItem) {
                // Apply weaker passive effects when in inventory (not held)
                divineItem.applyPassiveEffects(player, item);
                
                // Create god aura effects
                if (divineItem.getGodType() != null) {
                    plugin.getVisualEffectsManager().createGodAura(player, divineItem.getGodType());
                }
            }
        }
    }
    
    /**
     * Get the DivineItem instance from an ItemStack
     */
    private DivineItem getDivineItemFromStack(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                plugin.getLogger().info("getDivineItemFromStack: item is null or has no meta");
            }
            return null;
        }
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        String displayName = meta.hasDisplayName() ? meta.getDisplayName() : null;
        java.util.List<String> lore = meta.hasLore() ? meta.getLore() : null;

        if (plugin.getConfig().getBoolean("plugin.debug", false)) {
            plugin.getLogger().info("getDivineItemFromStack: Checking item " + item.getType().name() + 
                                   " with display name: '" + displayName + "'");
        }
        
        // Check all registered divine items
        for (GodType god : GodType.values()) {
            DivineItem divineItem = godManager.getDivineItem(god);
            if (divineItem != null) {
                if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                    plugin.getLogger().info("Checking against " + god.name() + " divine item: " + divineItem.getDisplayName());
                }
                // Match by display name (strip color codes for comparison)
                boolean nameMatch = displayName != null && 
                    org.bukkit.ChatColor.stripColor(displayName).equals(
                        org.bukkit.ChatColor.stripColor(divineItem.getDisplayName()));
                boolean loreMatch = true; // Simplified lore matching for now
                if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                    plugin.getLogger().info("Name match: " + nameMatch + ", Lore match: " + loreMatch);
                }
                if (nameMatch && loreMatch) {
                    if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                        plugin.getLogger().info("Found matching divine item: " + divineItem.getDisplayName());
                    }
                    return divineItem;
                }
            }
        }
        
        // Check for Convergence Nexus (not tied to a specific god)
        ConvergenceNexus nexus = new ConvergenceNexus();
        if (plugin.getConfig().getBoolean("plugin.debug", false)) {
            plugin.getLogger().info("Checking against Convergence Nexus: " + nexus.getDisplayName());
        }
        if (displayName != null && 
            org.bukkit.ChatColor.stripColor(displayName).equals(
                org.bukkit.ChatColor.stripColor(nexus.getDisplayName()))) {
            if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                plugin.getLogger().info("Found Convergence Nexus");
            }
            return nexus;
        }
        
        if (plugin.getConfig().getBoolean("plugin.debug", false)) {
            plugin.getLogger().info("No matching divine item found");
        }
        return null;
    }
}