package com.example.minecraftplugin.managers;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.items.FragmentItem;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manages the tutorial system for new players learning the Testament System
 */
public class TutorialManager implements Listener {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    private final PlayerDataManager playerDataManager;
    
    // Tutorial steps
    public enum TutorialStep {
        INTRODUCTION,
        TUTORIAL_FRAGMENT_COLLECTION,
        FIRST_FRAGMENT,
        FRAGMENT_COLLECTION,
        GODLEX_USAGE,
        ALTAR_BASICS,
        TESTAMENT_COMPLETION,
        DIVINE_ITEMS,
        ASCENSION_SYSTEM,
        COMPLETED
    }
    
    // Track tutorial progress for each player
    private final Map<UUID, Set<TutorialStep>> playerTutorialProgress;
    private final Map<UUID, TutorialStep> currentStep;
    
    public TutorialManager(MinecraftPlugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.playerDataManager = playerDataManager;
        this.playerTutorialProgress = new HashMap<>();
        this.currentStep = new HashMap<>();
        
        // Register as event listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        logger.info("Tutorial Manager initialized");
    }
    
    /**
     * Check if tutorials are enabled in config
     */
    private boolean isTutorialEnabled() {
        return plugin.getConfig().getBoolean("tutorial.enabled", true);
    }
    
    /**
     * Start tutorial for a new player
     */
    public void startTutorial(Player player) {
        if (!isTutorialEnabled()) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        
        // Check if player has already started tutorial
        if (playerTutorialProgress.containsKey(playerId)) {
            return;
        }
        
        // Initialize tutorial progress
        playerTutorialProgress.put(playerId, new HashSet<>());
        currentStep.put(playerId, TutorialStep.INTRODUCTION);
        
        // Start introduction
        showIntroduction(player);
    }
    
    /**
     * Show the introduction tutorial
     */
    private void showIntroduction(Player player) {
        player.sendTitle("§6§l✦ Welcome to the Testament System ✦", "§7Your divine journey begins...", 20, 100, 20);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage("§6§l=== TESTAMENT SYSTEM TUTORIAL ===§r");
                player.sendMessage("§7Welcome to an epic journey of divine power and ascension!");
                player.sendMessage("");
                player.sendMessage("§e§lWhat is the Testament System?");
                player.sendMessage("§7• Collect fragments from 12 fallen gods");
                player.sendMessage("§7• Build altars and complete testaments");
                player.sendMessage("§7• Gain divine powers and ascend to godhood");
                player.sendMessage("§7• Ultimate goal: Master all gods for Divine Convergence");
                player.sendMessage("");
                player.sendMessage("§a§lNext Step: §r§aLearn with tutorial fragments!");
                player.sendMessage("§7I'm giving you tutorial fragments to practice with:");
                
                // Give all 7 tutorial fragments
                giveTutorialFragments(player);
                player.sendMessage("");
                player.sendMessage("§7Type §f/tutorial skip§7 to skip this tutorial anytime.");
                
                // Play tutorial sound
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
                
                // Mark step as completed and advance
                completeStep(player, TutorialStep.INTRODUCTION);
                setCurrentStep(player, TutorialStep.TUTORIAL_FRAGMENT_COLLECTION);
            }
        }.runTaskLater(plugin, 60L); // 3 second delay
    }
    
    /**
     * Handle first fragment pickup
     */
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();
        
        // Check if it's a fragment
        FragmentItem.FragmentInfo fragmentInfo = FragmentItem.getFragmentInfo(item);
        if (fragmentInfo == null) {
            return;
        }
        
        // Check if player is on first fragment step
        if (getCurrentStep(player) == TutorialStep.FIRST_FRAGMENT) {
            showFirstFragmentTutorial(player, fragmentInfo.getGodType());
        } else if (getCurrentStep(player) == TutorialStep.FRAGMENT_COLLECTION) {
            showFragmentCollectionProgress(player, fragmentInfo.getGodType());
        }
    }
    
    /**
     * Show first fragment tutorial
     */
    private void showFirstFragmentTutorial(Player player, GodType god) {
        player.sendTitle("§6§l✦ First Fragment Found! ✦", "§7Fragment of the " + god.getDisplayName(), 20, 80, 20);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage("§6§l✦ CONGRATULATIONS! ✦");
                player.sendMessage("§7You found your first divine fragment!");
                player.sendMessage("");
                player.sendMessage("§e§lFragment Information:");
                player.sendMessage("§7• God: §f" + god.getDisplayName());
                player.sendMessage("§7• Theme: §f" + god.getTheme());
                player.sendMessage("§7• You need §f7 fragments§7 to complete this testament");
                player.sendMessage("");
                player.sendMessage("§a§lNext Step: §r§aLearn about fragment collection!");
                player.sendMessage("§7Use §f/testament fragments§7 to check your progress");
                player.sendMessage("§7Use §f/godlex " + god.name().toLowerCase() + "§7 to learn about this god");
                
                // Create celebration particles
                createCelebrationParticles(player);
                
                // Mark step as completed and advance
                completeStep(player, TutorialStep.FIRST_FRAGMENT);
                setCurrentStep(player, TutorialStep.FRAGMENT_COLLECTION);
            }
        }.runTaskLater(plugin, 20L); // 1 second delay
    }
    
    /**
     * Give tutorial fragments to player
     */
    private void giveTutorialFragments(Player player) {
        for (int i = 1; i <= 7; i++) {
            com.example.minecraftplugin.items.FragmentItem fragment = 
                new com.example.minecraftplugin.items.FragmentItem(GodType.TUTORIAL, i);
            ItemStack fragmentItem = fragment.createItemStack();
            player.getInventory().addItem(fragmentItem);
        }
        
        player.sendMessage("§a§l✦ TUTORIAL FRAGMENTS RECEIVED! ✦");
        player.sendMessage("§7You now have all 7 Tutorial God fragments!");
        player.sendMessage("§7Next: Build a simple altar and complete the tutorial testament.");
        player.sendMessage("");
        player.sendMessage("§e§lTutorial Altar Instructions:");
        player.sendMessage("§7• Place a §fSTONE§7 block as the center");
        player.sendMessage("§7• Right-click the stone with all 7 fragments in your inventory");
        player.sendMessage("§7• This will complete your tutorial testament!");
    }
    
    /**
     * Show fragment collection progress
     */
    private void showFragmentCollectionProgress(Player player, GodType god) {
        if (!hasCompletedStep(player, TutorialStep.GODLEX_USAGE)) {
            player.sendMessage("§e§lTip: §r§eUse §f/godlex " + god.name().toLowerCase() + "§e to learn more about this god!");
            
            // Mark godlex step as completed
            completeStep(player, TutorialStep.GODLEX_USAGE);
            
            // Check if ready for altar tutorial
            if (plugin.getGodManager().hasAllFragments(player, god)) {
                setCurrentStep(player, TutorialStep.ALTAR_BASICS);
                showAltarBasicsTutorial(player, god);
            }
        }
    }
    
    /**
     * Show altar basics tutorial
     */
    private void showAltarBasicsTutorial(Player player, GodType god) {
        player.sendTitle("§6§l⚡ Ready for Testament! ⚡", "§7Build an altar for " + god.getDisplayName(), 20, 80, 20);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage("§6§l⚡ TESTAMENT READY! ⚡");
                player.sendMessage("§7You have collected all 7 fragments for the " + god.getDisplayName() + "!");
                player.sendMessage("");
                player.sendMessage("§e§lNext Step: §r§eBuild an altar!");
                player.sendMessage("§7• Find the altar center block: §f" + god.getAltarCenterBlock().name());
                player.sendMessage("§7• Right-click it with all 7 fragments in your inventory");
                player.sendMessage("§7• Use §f/godlex " + god.name().toLowerCase() + "§7 for altar requirements");
                player.sendMessage("");
                player.sendMessage("§a§lTip: §r§aAltars can be found naturally or built manually!");
                player.sendMessage("§7Look for the correct biomes: §f" + String.join(", ", god.getBiomes()));
                
                // Mark step as completed
                completeStep(player, TutorialStep.ALTAR_BASICS);
                setCurrentStep(player, TutorialStep.TESTAMENT_COMPLETION);
            }
        }.runTaskLater(plugin, 20L);
    }
    
    /**
     * Handle altar interaction for tutorial
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Check for tutorial altar interaction
        if (getCurrentStep(player) == TutorialStep.TUTORIAL_FRAGMENT_COLLECTION) {
            if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.STONE) {
                handleTutorialAltarInteraction(player);
            }
        }
    }
    
    /**
     * Handle tutorial altar interaction
     */
    private void handleTutorialAltarInteraction(Player player) {
        // Check if player has all 7 tutorial fragments
        int tutorialFragments = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                com.example.minecraftplugin.items.FragmentItem.FragmentInfo info = 
                    com.example.minecraftplugin.items.FragmentItem.getFragmentInfo(item);
                if (info != null && info.getGodType() == GodType.TUTORIAL) {
                    tutorialFragments++;
                }
            }
        }
        
        if (tutorialFragments < 7) {
            player.sendMessage("§c§lTutorial Altar: §r§cYou need all 7 tutorial fragments!");
            player.sendMessage("§7You have: §f" + tutorialFragments + "/7 §7tutorial fragments");
            return;
        }
        
        // Remove tutorial fragments
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                com.example.minecraftplugin.items.FragmentItem.FragmentInfo info = 
                    com.example.minecraftplugin.items.FragmentItem.getFragmentInfo(item);
                if (info != null && info.getGodType() == GodType.TUTORIAL) {
                    player.getInventory().remove(item);
                }
            }
        }
        
        // Give tutorial divine item
        com.example.minecraftplugin.items.TutorialDivineItem tutorialItem = 
            new com.example.minecraftplugin.items.TutorialDivineItem();
        ItemStack tutorialItemStack = tutorialItem.createItemStack();
        player.getInventory().addItem(tutorialItemStack);
        tutorialItem.onObtained(player, tutorialItemStack);
        
        // Advance tutorial
        completeStep(player, TutorialStep.TUTORIAL_FRAGMENT_COLLECTION);
        setCurrentStep(player, TutorialStep.DIVINE_ITEMS);
        
        // Show tutorial completion
        showTutorialTestamentCompletion(player, tutorialItemStack);
    }
    
    /**
     * Show tutorial testament completion
     */
    private void showTutorialTestamentCompletion(Player player, ItemStack tutorialItemStack) {
        player.sendTitle("§a§l✦ TUTORIAL TESTAMENT COMPLETED! ✦", "§7You understand the basics!", 20, 80, 20);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage("§a§l✦ TUTORIAL TESTAMENT COMPLETED! ✦");
                player.sendMessage("§7Congratulations! You've learned the core mechanics!");
                player.sendMessage("");
                player.sendMessage("§e§lWhat You've Learned:");
                player.sendMessage("§7• How to collect divine fragments");
                player.sendMessage("§7• How to build and use altars");
                player.sendMessage("§7• How divine items work (active and passive abilities)");
                player.sendMessage("§7• The basic flow of testament completion");
                player.sendMessage("");
                player.sendMessage("§a§lNext Steps:");
                player.sendMessage("§7• Start collecting real divine fragments from chests and mobs");
                player.sendMessage("§7• Use §f/godlex§7 to learn about the 12 gods");
                player.sendMessage("§7• Use §f/testament status§7 to track your progress");
                player.sendMessage("§7• Build real altars and complete actual testaments!");
                player.sendMessage("");
                player.sendMessage("§6§lTutorial Complete! Your divine journey begins now!");
                
                // Remove tutorial divine item after a delay
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    player.getInventory().remove(tutorialItemStack);
                    player.sendMessage("§e§lTutorial item removed. §r§eGo collect real divine fragments!");
                }, 200L); // 10 seconds
                
                // Mark tutorial as completed
                completeStep(player, TutorialStep.DIVINE_ITEMS);
                completeStep(player, TutorialStep.ASCENSION_SYSTEM);
                setCurrentStep(player, TutorialStep.COMPLETED);
                
                // Create celebration particles
                createCelebrationParticles(player);
            }
        }.runTaskLater(plugin, 20L); // 1 second delay
    }
    
    /**
     * Show testament completion tutorial
     */
    public void showTestamentCompletionTutorial(Player player) {
        player.sendTitle("§5§l★ TESTAMENT COMPLETED! ★", "§7Divine power flows through you!", 20, 100, 20);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage("§5§l★ DIVINE POWER AWAKENED! ★");
                player.sendMessage("§7You have completed your first testament!");
                player.sendMessage("");
                player.sendMessage("§e§lWhat You've Gained:");
                player.sendMessage("§7• A divine item with unique powers");
                player.sendMessage("§7• Increased ascension level");
                player.sendMessage("§7• Progress toward Divine Convergence");
                player.sendMessage("");
                player.sendMessage("§a§lNext Steps:");
                player.sendMessage("§7• Use §f/testament status§7 to check your progress");
                player.sendMessage("§7• Collect fragments for other gods");
                player.sendMessage("§7• Master all 12 gods for ultimate power!");
                player.sendMessage("");
                player.sendMessage("§6§lCongratulations! You've completed the tutorial!");
                player.sendMessage("§7Continue your divine journey and ascend to godhood!");
                
                // Mark tutorial as completed
                completeStep(player, TutorialStep.TESTAMENT_COMPLETION);
                completeStep(player, TutorialStep.DIVINE_ITEMS);
                completeStep(player, TutorialStep.ASCENSION_SYSTEM);
                setCurrentStep(player, TutorialStep.COMPLETED);
                
                // Create epic completion particles
                createEpicParticles(player);
            }
        }.runTaskLater(plugin, 40L); // 2 second delay
    }
    
    /**
     * Handle player join for tutorial
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is new (no testament data)
        if (plugin.getGodManager().getTestamentCount(player) == 0 && 
            !playerTutorialProgress.containsKey(player.getUniqueId())) {
            
            // Delay tutorial start to ensure player has fully loaded
            new BukkitRunnable() {
                @Override
                public void run() {
                    startTutorial(player);
                }
            }.runTaskLater(plugin, 60L); // 3 second delay
        }
    }
    
    /**
     * Create celebration particles
     */
    private void createCelebrationParticles(Player player) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 60) { // 3 seconds
                    cancel();
                    return;
                }
                
                // Create colorful particles around player
                player.getWorld().spawnParticle(Particle.FIREWORK, 
                    player.getLocation().add(0, 1, 0), 5, 1, 1, 1, 0.1);
                player.getWorld().spawnParticle(Particle.HEART, 
                    player.getLocation().add(0, 1, 0), 3, 0.5, 0.5, 0.5, 0.1);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Create epic completion particles
     */
    private void createEpicParticles(Player player) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 100) { // 5 seconds
                    cancel();
                    return;
                }
                
                // Create epic particle spiral
                double radius = 2.0;
                double height = 0.1 * ticks;
                double angle = ticks * 0.3;
                
                double x = player.getLocation().getX() + radius * Math.cos(angle);
                double y = player.getLocation().getY() + height;
                double z = player.getLocation().getZ() + radius * Math.sin(angle);
                
                player.getWorld().spawnParticle(Particle.END_ROD, 
                    new org.bukkit.Location(player.getWorld(), x, y, z), 3, 0.1, 0.1, 0.1, 0.05);
                player.getWorld().spawnParticle(Particle.ENCHANT, 
                    new org.bukkit.Location(player.getWorld(), x, y, z), 5, 0.2, 0.2, 0.2, 0.1);
                
                if (ticks % 20 == 0) {
                    player.getWorld().spawnParticle(Particle.EXPLOSION, 
                        player.getLocation().add(0, 1, 0), 1);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Skip tutorial for a player
     */
    public void skipTutorial(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Mark all steps as completed
        Set<TutorialStep> allSteps = new HashSet<>();
        for (TutorialStep step : TutorialStep.values()) {
            allSteps.add(step);
        }
        playerTutorialProgress.put(playerId, allSteps);
        currentStep.put(playerId, TutorialStep.COMPLETED);
        
        player.sendMessage("§e§lTutorial skipped! §r§eYou can always use §f/godlex§e and §f/testament§e commands for help.");
    }
    
    /**
     * Reset tutorial for a player
     */
    public void resetTutorial(Player player) {
        UUID playerId = player.getUniqueId();
        playerTutorialProgress.remove(playerId);
        currentStep.remove(playerId);
        
        player.sendMessage("§a§lTutorial reset! §r§aRejoin the server to restart the tutorial.");
    }
    
    /**
     * Get current tutorial step for a player
     */
    public TutorialStep getCurrentStep(Player player) {
        return currentStep.getOrDefault(player.getUniqueId(), TutorialStep.COMPLETED);
    }
    
    /**
     * Set current tutorial step for a player
     */
    private void setCurrentStep(Player player, TutorialStep step) {
        currentStep.put(player.getUniqueId(), step);
    }
    
    /**
     * Mark a tutorial step as completed
     */
    private void completeStep(Player player, TutorialStep step) {
        UUID playerId = player.getUniqueId();
        playerTutorialProgress.computeIfAbsent(playerId, k -> new HashSet<>()).add(step);
    }
    
    /**
     * Check if a player has completed a tutorial step
     */
    public boolean hasCompletedStep(Player player, TutorialStep step) {
        UUID playerId = player.getUniqueId();
        return playerTutorialProgress.getOrDefault(playerId, new HashSet<>()).contains(step);
    }
    
    /**
     * Check if tutorial is completed for a player
     */
    public boolean isTutorialCompleted(Player player) {
        return getCurrentStep(player) == TutorialStep.COMPLETED;
    }
    
    /**
     * Get tutorial progress for a player
     */
    public String getTutorialProgress(Player player) {
        Set<TutorialStep> completed = playerTutorialProgress.getOrDefault(player.getUniqueId(), new HashSet<>());
        TutorialStep current = getCurrentStep(player);
        
        StringBuilder progress = new StringBuilder();
        progress.append("§6§l=== TUTORIAL PROGRESS ===§r\n");
        progress.append("§7Current Step: §f").append(current.name()).append("\n");
        progress.append("§7Completed Steps: §f").append(completed.size()).append("/").append(TutorialStep.values().length).append("\n");
        progress.append("\n");
        
        for (TutorialStep step : TutorialStep.values()) {
            String status = completed.contains(step) ? "§a✓" : (step == current ? "§e→" : "§7○");
            progress.append(status).append(" §7").append(step.name()).append("\n");
        }
        
        return progress.toString();
    }
}