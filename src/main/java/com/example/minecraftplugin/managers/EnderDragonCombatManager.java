package com.example.minecraftplugin.managers;

import com.example.minecraftplugin.MinecraftPlugin;
import org.bukkit.Color;
import org.bukkit.Color;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manages enhanced Ender Dragon combat with phases and unique abilities
 */
public class EnderDragonCombatManager {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    private final BroadcastManager broadcastManager;
    
    // Combat state
    private EnderDragon dragon;
    private DragonPhase currentPhase;
    private final List<Player> participants;
    private final Map<UUID, Integer> playerDamage;
    private BukkitTask combatTask;
    private long combatStartTime;
    
    // Phase tracking
    private double currentPosture;
    private double maxPosture;
    private double postureRegenRate;
    private boolean isPostureBroken;
    private boolean parryWindowActive;
    private long parryWindowEndTime;
    private String currentAttackType;
    private boolean isPerilousAttack;
    
    private int phaseTransitions;
    private boolean isEnraged;
    private final Set<Location> crystalLocations;
    private final List<Entity> summonedMinions;
    
    // Scaling parameters
    private double healthMultiplier;
    private double damageMultiplier;
    private int playerCount;
    
    public EnderDragonCombatManager(MinecraftPlugin plugin, BroadcastManager broadcastManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.broadcastManager = broadcastManager;
        this.participants = new ArrayList<>();
        this.playerDamage = new HashMap<>();
        this.crystalLocations = new HashSet<>();
        this.summonedMinions = new ArrayList<>();
        this.currentPhase = DragonPhase.CRYSTAL_PHASE;
        this.phaseTransitions = 0;
        this.isEnraged = false;
        
        logger.info("Ender Dragon Combat Manager initialized");
    }
    
    /**
     * Start enhanced dragon combat
     */
    public void startCombat(EnderDragon dragon, List<Player> players, double healthMult, double damageMult) {
        this.dragon = dragon;
        this.participants.addAll(players);
        this.healthMultiplier = healthMult;
        this.damageMultiplier = damageMult;
        this.playerCount = players.size();
        this.combatStartTime = System.currentTimeMillis();
        this.currentPosture = 0.0;
        this.maxPosture = 100.0 * healthMult; // Scale posture with health multiplier
        this.postureRegenRate = 0.5; // Posture regenerates at 0.5 per second
        this.isPostureBroken = false;
        this.parryWindowActive = false;
        
        // Apply scaling to dragon
        applyDragonScaling();
        
        // Initialize combat phases
        initializeCombatPhases();
        
        // Start combat monitoring
        startCombatMonitoring();
        
        // Broadcast combat start
        broadcastManager.broadcastBossSpawn("Enhanced Ender Dragon", dragon.getLocation());
        
        // Notify participants
        for (Player player : participants) {
            player.sendTitle("§5§l⚔ ENHANCED ENDER DRAGON ⚔", 
                           "§7Phase: " + currentPhase.getDisplayName(), 20, 60, 20);
            player.sendMessage("§5§l=== ENHANCED DRAGON COMBAT INITIATED ===");
            player.sendMessage("§7The Ender Dragon has been enhanced with divine power!");
            player.sendMessage("§7Current Phase: §f" + currentPhase.getDisplayName());
            player.sendMessage("§7Health Scaling: §f" + String.format("%.1fx", healthMultiplier));
            player.sendMessage("§7Damage Scaling: §f" + String.format("%.1fx", damageMultiplier));
        }
        
        logger.info("Started enhanced Ender Dragon combat with " + players.size() + " participants");
    }
    
    /**
     * Apply scaling to the dragon
     */
    private void applyDragonScaling() {
        if (dragon == null) return;
        
        // Scale health
        double baseHealth = dragon.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getBaseValue();
        double newMaxHealth = baseHealth * healthMultiplier;
        dragon.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(newMaxHealth);
        dragon.setHealth(newMaxHealth);
        
        // Scale damage (will be applied in attack methods)
        
        logger.info("Applied scaling to Ender Dragon: Health " + String.format("%.1fx", healthMultiplier) + 
                   ", Damage " + String.format("%.1fx", damageMultiplier));
    }
    
    /**
     * Initialize combat phases
     */
    private void initializeCombatPhases() {
        // Spawn additional end crystals for crystal phase
        spawnAdditionalCrystals();
        
        // Set initial phase
        currentPhase = DragonPhase.CRYSTAL_PHASE;
    }
    
    /**
     * Start combat monitoring task
     */
    private void startCombatMonitoring() {
        combatTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (dragon == null || dragon.isDead()) {
                    handleDragonDefeat();
                    cancel();
                    return;
                }
                
                // Update combat state
                updateCombatState();
                
                // Check for phase transitions
                checkPhaseTransitions();
                
                // Execute phase-specific behavior
                executePhaseAbilities();
                
                // Check for enrage conditions
                checkEnrageConditions();
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }
    
    /**
     * Update combat state
     */
    private void updateCombatState() {
        // Remove disconnected players
        participants.removeIf(player -> !player.isOnline());
        
        // Update posture regeneration
        if (!isPostureBroken && currentPosture > 0) {
            currentPosture = Math.max(0, currentPosture - postureRegenRate);
        }
        
        updateParryWindow();
        
        // Check if all players are dead or gone
        boolean hasAlivePlayers = participants.stream()
            .anyMatch(player -> player.isOnline() && player.getHealth() > 0);
        
        if (!hasAlivePlayers) {
            handleCombatFailure();
            return;
        }
        
        // Update player damage tracking (simplified)
        for (Player player : participants) {
            if (player.getLocation().distance(dragon.getLocation()) <= 100) {
                // Player is in combat range
                playerDamage.putIfAbsent(player.getUniqueId(), 0);
            }
        }
    }
    
    /**
     * Update parry window state
     */
    private void updateParryWindow() {
        if (parryWindowActive && System.currentTimeMillis() > parryWindowEndTime) {
            parryWindowActive = false;
            currentAttackType = null;
        }
    }
    
    /**
     * Check for phase transitions
     */
    private void checkPhaseTransitions() {
        double healthPercent = dragon.getHealth() / dragon.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        
        switch (currentPhase) {
            case CRYSTAL_PHASE:
                // Transition when crystals are destroyed or health drops below 75%
                if (healthPercent <= 0.75 || areAllCrystalsDestroyed()) {
                    transitionToPhase(DragonPhase.AERIAL_PHASE);
                }
                break;
                
            case AERIAL_PHASE:
                // Transition when health drops below 50%
                if (healthPercent <= 0.50) {
                    transitionToPhase(DragonPhase.GROUND_PHASE);
                }
                break;
                
            case GROUND_PHASE:
                // Transition when health drops below 25%
                if (healthPercent <= 0.25) {
                    transitionToPhase(DragonPhase.ENRAGED_PHASE);
                }
                break;
                
            case ENRAGED_PHASE:
                // Final phase - fight until death
                break;
        }
    }
    
    /**
     * Transition to a new phase
     */
    private void transitionToPhase(DragonPhase newPhase) {
        if (currentPhase == newPhase) return;
        
        DragonPhase oldPhase = currentPhase;
        currentPhase = newPhase;
        phaseTransitions++;
        
        // Notify participants
        for (Player player : participants) {
            player.sendTitle("§c§l⚡ PHASE TRANSITION ⚡", 
                           "§7" + newPhase.getDisplayName(), 10, 40, 10);
            player.sendMessage("§c§l⚡ DRAGON PHASE TRANSITION! ⚡");
            player.sendMessage("§7" + oldPhase.getDisplayName() + " → " + newPhase.getDisplayName());
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.8f);
        }
        
        // Execute phase transition effects
        executePhaseTransition(oldPhase, newPhase);
        
        logger.info("Dragon phase transition: " + oldPhase + " → " + newPhase);
    }
    
    /**
     * Execute phase transition effects
     */
    private void executePhaseTransition(DragonPhase oldPhase, DragonPhase newPhase) {
        Location dragonLoc = dragon.getLocation();
        
        switch (newPhase) {
            case AERIAL_PHASE:
                // Clear remaining crystals and summon aerial minions
                clearRemainingCrystals();
                summonAerialMinions();
                createPhaseTransitionEffect(dragonLoc, Particle.CLOUD);
                break;
                
            case GROUND_PHASE:
                // Force dragon to ground and summon ground minions
                forceDragonToGround();
                summonGroundMinions();
                createPhaseTransitionEffect(dragonLoc, Particle.EXPLOSION);
                break;
                
            case ENRAGED_PHASE:
                // Enrage the dragon with enhanced abilities
                enrageDragon();
                summonEnragedMinions();
                createPhaseTransitionEffect(dragonLoc, Particle.DRAGON_BREATH);
                break;
        }
    }
    
    /**
     * Execute phase-specific abilities
     */
    private void executePhaseAbilities() {
        switch (currentPhase) {
            case CRYSTAL_PHASE:
                executeCrystalPhaseAbilities();
                break;
            case AERIAL_PHASE:
                executeAerialPhaseAbilities();
                break;
            case GROUND_PHASE:
                executeGroundPhaseAbilities();
                break;
            case ENRAGED_PHASE:
                executeEnragedPhaseAbilities();
                break;
        }
    }
    
    /**
     * Crystal phase abilities
     */
    private void executeCrystalPhaseAbilities() {
        if (isPostureBroken) return; // Don't attack while posture broken
        // Regenerate health near crystals
        if (Math.random() < 0.1) { // 10% chance per second
            for (Location crystalLoc : crystalLocations) {
                if (dragon.getLocation().distance(crystalLoc) <= 20) {
                    double healAmount = dragon.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue() * 0.02;
                    dragon.setHealth(Math.min(dragon.getHealth() + healAmount, 
                        dragon.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue()));
                    
                    // Visual effect
                    dragon.getWorld().spawnParticle(Particle.HEART, dragon.getLocation(), 5, 1, 1, 1, 0.1);
                    break;
                }
            }
        }
        
        // Crystal beam attack
        if (Math.random() < 0.05) { // 5% chance per second
            executeCrystalBeamAttack();
        }
    }
    
    /**
     * Aerial phase abilities
     */
    private void executeAerialPhaseAbilities() {
        if (isPostureBroken) return; // Don't attack while posture broken
        // Fireball barrage
        if (Math.random() < 0.15) { // 15% chance per second
            executeFireballBarrage();
        }
        
        // Wind gust attack
        if (Math.random() < 0.1) { // 10% chance per second
            executeWindGustAttack();
        }
    }
    
    /**
     * Ground phase abilities
     */
    private void executeGroundPhaseAbilities() {
        if (isPostureBroken) return; // Don't attack while posture broken
        // Tail sweep attack
        if (Math.random() < 0.2) { // 20% chance per second
            executeTailSweepAttack();
        }
        
        // Breath weapon enhanced
        if (Math.random() < 0.15) { // 15% chance per second
            executeEnhancedBreathWeapon();
        }
    }
    
    /**
     * Enraged phase abilities
     */
    private void executeEnragedPhaseAbilities() {
        if (isPostureBroken) return; // Don't attack while posture broken
        // All abilities with increased frequency
        if (Math.random() < 0.25) { // 25% chance per second
            int ability = (int) (Math.random() * 4);
            switch (ability) {
                case 0:
                    executeFireballBarrage();
                    break;
                case 1:
                    executeWindGustAttack();
                    break;
                case 2:
                    executeTailSweepAttack();
                    break;
                case 3:
                    executeEnhancedBreathWeapon();
                    break;
            }
        }
        
        // Enraged-specific abilities
        if (Math.random() < 0.1) { // 10% chance per second
            executeEnragedRoar();
        }
    }
    
    /**
     * Deal posture damage to the dragon
     */
    public void dealPostureDamage(double amount, Player attacker) {
        if (isPostureBroken) return; // Cannot deal posture damage if already broken
        
        currentPosture += amount;
        
        // Visual/audio feedback for posture damage
        dragon.getWorld().spawnParticle(Particle.ENCHANTED_HIT, 
            dragon.getLocation().add(0, dragon.getHeight() / 2, 0), 15, 1, 1, 1, 0.2);
        dragon.getWorld().playSound(dragon.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 2.0f);
        
        // Show posture progress to attacker
        double posturePercent = (currentPosture / maxPosture) * 100;
        attacker.sendMessage("§e§lPosture: §f" + String.format("%.1f", posturePercent) + "%");
        
        if (currentPosture >= maxPosture) {
            currentPosture = maxPosture; // Cap at max
            breakPosture();
        }
    }
    
    /**
     * Break the dragon's posture
     */
    private void breakPosture() {
        isPostureBroken = true;
        parryWindowActive = false; // Cancel any active parry windows
        
        // Apply stun/vulnerability effects to the dragon
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 10, false, false)); // Stun
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 5, false, false)); // Vulnerability
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0, false, false)); // Visual cue
        
        // Epic visual/audio cues for posture break
        Location dragonLoc = dragon.getLocation().add(0, dragon.getHeight() / 2, 0);
        dragon.getWorld().spawnParticle(Particle.EXPLOSION, dragonLoc, 3);
        dragon.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, dragonLoc, 50, 2, 2, 2, 0.3);
        dragon.getWorld().playSound(dragon.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 0.5f);
        dragon.getWorld().playSound(dragon.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.1f);
        
        for (Player player : participants) {
            player.sendTitle("§c§l⚡ POSTURE BROKEN! ⚡", "§7Dragon is vulnerable to deathblow!", 10, 60, 10);
            player.sendMessage("§c§l⚡ POSTURE BROKEN! ⚡");
            player.sendMessage("§7Right-click the dragon for a DEATHBLOW!");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.1f);
        }
        
        // Schedule posture recovery after vulnerability window
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> resetPosture(), 100L); // 5 seconds vulnerable
        
        logger.info("Dragon posture broken - vulnerable for 5 seconds");
    }
    
    /**
     * Reset the dragon's posture
     */
    private void resetPosture() {
        isPostureBroken = false;
        currentPosture = 0; // Reset posture bar
        
        // Remove any lingering stun/vulnerability effects
        dragon.removePotionEffect(PotionEffectType.SLOWNESS);
        dragon.removePotionEffect(PotionEffectType.WEAKNESS);
        dragon.removePotionEffect(PotionEffectType.GLOWING);
        
        for (Player player : participants) {
            player.sendMessage("§a§lDragon's posture recovered!");
            player.sendTitle("§a§lPOSTURE RECOVERED", "§7Dragon is no longer vulnerable", 10, 40, 10);
        }
        
        logger.info("Dragon posture reset - no longer vulnerable");
    }
    
    /**
     * Perform deathblow attack
     */
    public boolean performDeathblow(Player player) {
        if (!isPostureBroken) {
            player.sendMessage("§c§lThe dragon is not vulnerable for a deathblow!");
            return false;
        }
        
        // Check if player is close enough
        if (player.getLocation().distance(dragon.getLocation()) > 8) {
            player.sendMessage("§c§lYou must be closer to the dragon for a deathblow!");
            return false;
        }
        
        // Deal massive damage based on current phase and health
        double maxHealth = dragon.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        double deathblowDamage;
        
        // Scale deathblow damage based on current phase
        switch (currentPhase) {
            case CRYSTAL_PHASE:
                deathblowDamage = maxHealth * 0.15; // 15% of max health
                break;
            case AERIAL_PHASE:
                deathblowDamage = maxHealth * 0.20; // 20% of max health
                break;
            case GROUND_PHASE:
                deathblowDamage = maxHealth * 0.25; // 25% of max health
                break;
            case ENRAGED_PHASE:
                deathblowDamage = maxHealth * 0.30; // 30% of max health
                break;
            default:
                deathblowDamage = maxHealth * 0.20;
                break;
        }
        
        // Apply damage
        dragon.damage(deathblowDamage, player);
        
        // Reset posture and vulnerability immediately
        resetPosture();
        
        // Epic visual and audio feedback
        Location dragonLoc = dragon.getLocation().add(0, dragon.getHeight() / 2, 0);
        dragon.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, dragonLoc, 1);
        dragon.getWorld().spawnParticle(Particle.CRIT, dragonLoc, 100, 3, 3, 3, 0.5);
        dragon.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, dragonLoc, 50, 2, 2, 2, 0.3);
        dragon.getWorld().playSound(dragon.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
        dragon.getWorld().playSound(dragon.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.1f);
        
        // Lightning strike for dramatic effect
        dragon.getWorld().strikeLightningEffect(dragonLoc);
        
        for (Player p : participants) {
            p.sendTitle("§6§l⚔ DEATHBLOW! ⚔", "§7" + player.getName() + " dealt massive damage!", 10, 80, 10);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.1f);
        }
        
        player.sendMessage("§6§l⚔ DEATHBLOW EXECUTED! ⚔");
        player.sendMessage("§7Dealt " + String.format("%.1f", deathblowDamage) + " damage!");
        
        // Check if dragon is defeated
        if (dragon.getHealth() <= 0.1) {
            handleDragonDefeat();
        }
        
        logger.info(player.getName() + " performed deathblow dealing " + deathblowDamage + " damage");
        return true;
    }
    
    /**
     * Start a parry window for the specified attack
     */
    public void startParryWindow(String attackType, boolean isPerilous, int durationMs) {
        this.currentAttackType = attackType;
        this.isPerilousAttack = isPerilous;
        this.parryWindowActive = !isPerilous; // Only parryable attacks have parry windows
        this.parryWindowEndTime = System.currentTimeMillis() + durationMs;
        
        // Notify players about the attack
        for (Player player : participants) {
            if (isPerilous) {
                player.sendMessage("§c§l⚠ PERILOUS ATTACK! ⚠ §7" + attackType + " - DODGE!");
                player.sendTitle("§c§l⚠ DODGE! ⚠", "§7" + attackType, 5, 20, 5);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 0.5f);
                
                // Red warning particles
                dragon.getWorld().spawnParticle(Particle.DUST, 
                    dragon.getLocation().add(0, dragon.getHeight() / 2, 0), 
                    30, 2, 1, 2, 0, new Particle.DustOptions(Color.RED, 2.0f));
            } else {
                player.sendMessage("§e§l⚔ PARRYABLE ATTACK! ⚔ §7" + attackType + " - TIME YOUR STRIKE!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
                
                // Yellow warning particles
                dragon.getWorld().spawnParticle(Particle.DUST, 
                    dragon.getLocation().add(0, dragon.getHeight() / 2, 0), 
                    20, 1.5, 1, 1.5, 0, new Particle.DustOptions(Color.YELLOW, 1.5f));
            }
        }
    }
    
    /**
     * Check Eternal Crucible completion conditions
     */
    private boolean checkEternalCrucibleCompletion() {
        // This would check if the Eternal Crucible has been completed
        // For now, return false as a placeholder
        return false;
    }
    
    /**
     * Spawn additional end crystals
     */
    private void spawnAdditionalCrystals() {
        World world = dragon.getWorld();
        Location dragonLoc = dragon.getLocation();
        
        // Spawn 4 additional crystals in a square pattern
        int[] offsets = {-30, -15, 15, 30};
        for (int x : offsets) {
            for (int z : offsets) {
                if (x == 0 && z == 0) continue; // Skip center
                
                Location crystalLoc = dragonLoc.clone().add(x, 10, z);
                
                // Find suitable Y level
                for (int y = crystalLoc.getBlockY(); y >= crystalLoc.getBlockY() - 20; y--) {
                    crystalLoc.setY(y);
                    if (crystalLoc.getBlock().getType().isSolid()) {
                        crystalLoc.setY(y + 1);
                        break;
                    }
                }
                
                // Spawn crystal
                EnderCrystal crystal = world.spawn(crystalLoc, EnderCrystal.class);
                crystal.setShowingBottom(true);
                crystalLocations.add(crystalLoc);
                
                // Visual effect
                world.spawnParticle(Particle.EXPLOSION, crystalLoc, 3);
            }
        }
        
        logger.info("Spawned " + crystalLocations.size() + " additional end crystals");
    }
    
    /**
     * Check if all crystals are destroyed
     */
    private boolean areAllCrystalsDestroyed() {
        World world = dragon.getWorld();
        
        for (Location crystalLoc : crystalLocations) {
            // Check for crystals in a small radius around each location
            for (Entity entity : world.getNearbyEntities(crystalLoc, 2, 2, 2)) {
                if (entity instanceof EnderCrystal) {
                    return false; // Found a crystal
                }
            }
        }
        
        return true; // No crystals found
    }
    
    /**
     * Clear remaining crystals
     */
    private void clearRemainingCrystals() {
        World world = dragon.getWorld();
        
        for (Location crystalLoc : crystalLocations) {
            for (Entity entity : world.getNearbyEntities(crystalLoc, 2, 2, 2)) {
                if (entity instanceof EnderCrystal) {
                    entity.remove();
                    world.spawnParticle(Particle.EXPLOSION_EMITTER, entity.getLocation(), 5);
                }
            }
        }
    }
    
    /**
     * Summon aerial minions
     */
    private void summonAerialMinions() {
        World world = dragon.getWorld();
        Location dragonLoc = dragon.getLocation();
        
        // Spawn phantoms
        for (int i = 0; i < 3 + playerCount; i++) {
            Location spawnLoc = dragonLoc.clone().add(
                (Math.random() - 0.5) * 20, 
                Math.random() * 10, 
                (Math.random() - 0.5) * 20
            );
            
            Phantom phantom = world.spawn(spawnLoc, Phantom.class);
            phantom.setSize(2 + (int)(Math.random() * 3)); // Size 2-4
            summonedMinions.add(phantom);
        }
        
        // Notify players
        for (Player player : participants) {
            player.sendMessage("§c§l⚡ The dragon has summoned aerial phantoms! ⚡");
        }
    }
    
    /**
     * Summon ground minions
     */
    private void summonGroundMinions() {
        World world = dragon.getWorld();
        Location dragonLoc = dragon.getLocation();
        
        // Spawn endermen
        for (int i = 0; i < 2 + playerCount; i++) {
            Location spawnLoc = dragonLoc.clone().add(
                (Math.random() - 0.5) * 15, 
                0, 
                (Math.random() - 0.5) * 15
            );
            
            // Find ground level
            for (int y = spawnLoc.getBlockY(); y >= spawnLoc.getBlockY() - 20; y--) {
                spawnLoc.setY(y);
                if (spawnLoc.getBlock().getType().isSolid()) {
                    spawnLoc.setY(y + 1);
                    break;
                }
            }
            
            Enderman enderman = world.spawn(spawnLoc, Enderman.class);
            enderman.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1));
            enderman.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
            summonedMinions.add(enderman);
        }
        
        // Notify players
        for (Player player : participants) {
            player.sendMessage("§c§l⚡ Enhanced endermen have emerged to defend the dragon! ⚡");
        }
    }
    
    /**
     * Summon enraged minions
     */
    private void summonEnragedMinions() {
        World world = dragon.getWorld();
        Location dragonLoc = dragon.getLocation();
        
        // Spawn shulkers
        for (int i = 0; i < 1 + (playerCount / 2); i++) {
            Location spawnLoc = dragonLoc.clone().add(
                (Math.random() - 0.5) * 20, 
                Math.random() * 15, 
                (Math.random() - 0.5) * 20
            );
            
            Shulker shulker = world.spawn(spawnLoc, Shulker.class);
            shulker.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 2));
            summonedMinions.add(shulker);
        }
        
        // Notify players
        for (Player player : participants) {
            player.sendMessage("§c§l⚡ Void shulkers have been summoned from the depths! ⚡");
        }
    }
    
    /**
     * Force dragon to ground
     */
    private void forceDragonToGround() {
        // This is a simplified implementation
        // In a real scenario, you might need to use NMS or more complex AI manipulation
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 2));
        
        for (Player player : participants) {
            player.sendMessage("§c§l⚡ The dragon has been forced to the ground! ⚡");
        }
    }
    
    /**
     * Enrage the dragon
     */
    private void enrageDragon() {
        isEnraged = true;
        
        // Apply enrage effects
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 2));
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1));
        
        // Visual effects
        Location dragonLoc = dragon.getLocation();
        dragon.getWorld().spawnParticle(Particle.DRAGON_BREATH, dragonLoc, 50, 3, 3, 3, 0.3);
        dragon.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, dragonLoc, 10);
        
        for (Player player : participants) {
            player.sendTitle("§4§l⚡ DRAGON ENRAGED! ⚡", "§cThe final phase begins!", 10, 60, 10);
            player.sendMessage("§4§l⚡ THE ENDER DRAGON HAS ENTERED ITS FINAL ENRAGED STATE! ⚡");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
        }
    }
    
    /**
     * Execute crystal beam attack
     */
    private void executeCrystalBeamAttack() {
        // Start parry window before attack
        startParryWindow("Crystal Beam", false, 700); // 0.7 second parry window
        
        if (crystalLocations.isEmpty()) return;
        
        // Select random crystal and target
        Location crystalLoc = crystalLocations.iterator().next();
        Player target = getRandomParticipant();
        if (target == null) return;
        
        // Create beam effect
        createBeamEffect(crystalLoc, target.getLocation(), Particle.END_ROD);
        
        // Damage target
        double damage = 6.0 * damageMultiplier;
        target.damage(damage);
        target.sendMessage("§c§l⚡ Crystal beam attack! ⚡");
    }
    
    /**
     * Execute fireball barrage
     */
    private void executeFireballBarrage() {
        // Start parry window before attack
        startParryWindow("Fireball Barrage", false, 800); // 0.8 second parry window
        
        for (int i = 0; i < 3 + playerCount; i++) {
            Player target = getRandomParticipant();
            if (target == null) continue;
            
            // Spawn fireball
            Location spawnLoc = dragon.getLocation().add(0, 2, 0);
            DragonFireball fireball = dragon.getWorld().spawn(spawnLoc, DragonFireball.class);
            fireball.setDirection(target.getLocation().subtract(spawnLoc).toVector().normalize());
            fireball.setShooter(dragon);
        }
        
        for (Player player : participants) {
            player.sendMessage("§c§l⚡ Dragon fireball barrage incoming! ⚡");
        }
    }
    
    /**
     * Execute wind gust attack
     */
    private void executeWindGustAttack() {
        // Start parry window before attack
        startParryWindow("Wind Gust", false, 600); // 0.6 second parry window
        
        Location dragonLoc = dragon.getLocation();
        
        for (Player player : participants) {
            if (player.getLocation().distance(dragonLoc) <= 20) {
                // Apply knockback
                org.bukkit.util.Vector direction = player.getLocation().toVector()
                    .subtract(dragonLoc.toVector()).normalize();
                direction.setY(0.5);
                direction.multiply(2.0);
                player.setVelocity(direction);
                
                player.sendMessage("§c§l⚡ Powerful wind gust! ⚡");
            }
        }
        
        // Visual effect
        dragon.getWorld().spawnParticle(Particle.EXPLOSION, dragonLoc, 30, 5, 5, 5, 0.2);
    }
    
    /**
     * Execute tail sweep attack
     */
    private void executeTailSweepAttack() {
        // This is a perilous attack - must be dodged, not parried
        startParryWindow("Tail Sweep", true, 1000); // 1 second dodge window
        
        Location dragonLoc = dragon.getLocation();
        
        for (Player player : participants) {
            if (player.getLocation().distance(dragonLoc) <= 8) {
                // Damage and knockback
                double damage = 8.0 * damageMultiplier;
                player.damage(damage);
                
                org.bukkit.util.Vector direction = player.getLocation().toVector()
                    .subtract(dragonLoc.toVector()).normalize();
                direction.setY(0.3);
                direction.multiply(1.5);
                player.setVelocity(direction);
                
                player.sendMessage("§c§l⚡ Dragon tail sweep! ⚡");
            }
        }
        
        // Visual effect
        dragon.getWorld().spawnParticle(Particle.SWEEP_ATTACK, dragonLoc, 20, 4, 2, 4, 0.1);
    }
    
    /**
     * Execute enhanced breath weapon
     */
    private void executeEnhancedBreathWeapon() {
        // Start parry window before attack
        startParryWindow("Dragon Breath", false, 1000); // 1 second parry window
        
        Location dragonLoc = dragon.getLocation();
        
        // Create lingering breath cloud
        for (int i = 0; i < 5; i++) {
            Location cloudLoc = dragonLoc.clone().add(
                (Math.random() - 0.5) * 10,
                Math.random() * 5,
                (Math.random() - 0.5) * 10
            );
            
            AreaEffectCloud cloud = dragon.getWorld().spawn(cloudLoc, AreaEffectCloud.class);
            cloud.setDuration(200); // 10 seconds
            cloud.setRadius(3.0f);
            cloud.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 1), false);
            cloud.addCustomEffect(new PotionEffect(PotionEffectType.WITHER, 100, 0), false);
        }
        
        for (Player player : participants) {
            player.sendMessage("§c§l⚡ Enhanced dragon breath - avoid the toxic clouds! ⚡");
        }
    }
    
    /**
     * Execute enraged roar
     */
    private void executeEnragedRoar() {
        // This is a perilous attack - affects wide area
        startParryWindow("Enraged Roar", true, 1200); // 1.2 second dodge window
        
        Location dragonLoc = dragon.getLocation();
        
        for (Player player : participants) {
            if (player.getLocation().distance(dragonLoc) <= 30) {
                // Apply fear effects
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 0));
                
                player.sendMessage("§4§l⚡ The dragon's roar fills you with dread! ⚡");
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.3f);
            }
        }
        
        // Visual effect
        dragon.getWorld().spawnParticle(Particle.SONIC_BOOM, dragonLoc, 1);
        dragon.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, dragonLoc, 5);
    }
    
    /**
     * Create beam effect between two locations
     */
    private void createBeamEffect(Location start, Location end, Particle particle) {
        double distance = start.distance(end);
        int steps = (int) (distance * 2);
        
        for (int i = 0; i <= steps; i++) {
            double ratio = i / (double) steps;
            double x = start.getX() + (end.getX() - start.getX()) * ratio;
            double y = start.getY() + (end.getY() - start.getY()) * ratio;
            double z = start.getZ() + (end.getZ() - start.getZ()) * ratio;
            
            Location beamPoint = new Location(start.getWorld(), x, y, z);
            start.getWorld().spawnParticle(particle, beamPoint, 2, 0.1, 0.1, 0.1, 0);
        }
    }
    
    /**
     * Create phase transition effect
     */
    private void createPhaseTransitionEffect(Location location, Particle particle) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 60) { // 3 seconds
                    cancel();
                    return;
                }
                
                // Create expanding ring effect
                double radius = ticks * 0.5;
                for (int i = 0; i < 32; i++) {
                    double angle = (i / 32.0) * 360;
                    double x = location.getX() + radius * Math.cos(Math.toRadians(angle));
                    double z = location.getZ() + radius * Math.sin(Math.toRadians(angle));
                    Location effectLoc = new Location(location.getWorld(), x, location.getY(), z);
                    
                    location.getWorld().spawnParticle(particle, effectLoc, 3, 0.2, 0.2, 0.2, 0.1);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Get random participant
     */
    private Player getRandomParticipant() {
        List<Player> alivePlayers = new ArrayList<>();
        for (Player player : participants) {
            if (player.isOnline() && player.getHealth() > 0) {
                alivePlayers.add(player);
            }
        }
        
        if (alivePlayers.isEmpty()) {
            return null;
        }
        
        return alivePlayers.get((int) (Math.random() * alivePlayers.size()));
    }
    
    // Getters for the new posture system
    public boolean isParryWindowActive() {
        return parryWindowActive;
    }
    
    public void setParryWindowActive(boolean active) {
        this.parryWindowActive = active;
    }
    
    public boolean isPostureBroken() {
        return isPostureBroken;
    }
    
    public String getCurrentAttackType() {
        return currentAttackType;
    }
    
    public boolean isCurrentAttackPerilous() {
        return isPerilousAttack;
    }
    
    /**
     * Check enrage conditions
     */
    private void checkEnrageConditions() {
        if (isEnraged) return;
        
        // Enrage if combat takes too long
        long combatDuration = System.currentTimeMillis() - combatStartTime;
        if (combatDuration > 600000) { // 10 minutes
            enrageDragon();
        }
    }
    
    /**
     * Handle dragon defeat
     */
    private void handleDragonDefeat() {
        // Clean up minions
        for (Entity minion : summonedMinions) {
            if (minion != null && !minion.isDead()) {
                minion.remove();
            }
        }
        
        // Calculate rewards based on participation
        awardCombatRewards();
        
        // Broadcast defeat
        String topDamager = getTopDamager();
        broadcastManager.broadcastBossDefeat("Enhanced Ender Dragon", topDamager);
        
        // Cleanup
        cleanup();
        
        logger.info("Enhanced Ender Dragon defeated after " + phaseTransitions + " phase transitions");
    }
    
    /**
     * Handle combat failure
     */
    private void handleCombatFailure() {
        for (Player player : participants) {
            if (player.isOnline()) {
                player.sendMessage("§c§l✗ DRAGON COMBAT FAILED! ✗");
                player.sendMessage("§7The Enhanced Ender Dragon remains victorious...");
            }
        }
        
        cleanup();
        
        logger.info("Enhanced Ender Dragon combat failed - all participants defeated");
    }
    
    /**
     * Award combat rewards
     */
    private void awardCombatRewards() {
        for (Player player : participants) {
            if (!player.isOnline()) continue;
            
            // Base XP reward
            int xpReward = 1000 + (phaseTransitions * 200);
            player.giveExp(xpReward);
            
            // Special rewards for enhanced dragon
            awardEnhancedDragonRewards(player);
            
            player.sendMessage("§a§l✦ ENHANCED DRAGON DEFEATED! ✦");
            player.sendMessage("§7Experience gained: §f" + xpReward + " XP");
            player.sendMessage("§7Phase transitions survived: §f" + phaseTransitions);
        }
    }
    
    /**
     * Award enhanced dragon specific rewards
     */
    private void awardEnhancedDragonRewards(Player player) {
        // Dragon Scale (rare crafting material)
        org.bukkit.inventory.ItemStack dragonScale = new org.bukkit.inventory.ItemStack(Material.DRAGON_BREATH, 3 + phaseTransitions);
        player.getInventory().addItem(dragonScale);
        
        // Enhanced Elytra (if they don't have one)
        if (!player.getInventory().contains(Material.ELYTRA)) {
            org.bukkit.inventory.ItemStack enhancedElytra = new org.bukkit.inventory.ItemStack(Material.ELYTRA);
            enhancedElytra.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 5);
            enhancedElytra.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.MENDING, 1);
            player.getInventory().addItem(enhancedElytra);
            player.sendMessage("§5§l✦ Enhanced Elytra obtained! ✦");
        }
        
        // Dragon Head (rare)
        if (Math.random() < 0.3) { // 30% chance
            org.bukkit.inventory.ItemStack dragonHead = new org.bukkit.inventory.ItemStack(Material.DRAGON_HEAD);
            player.getInventory().addItem(dragonHead);
            player.sendMessage("§5§l✦ Dragon Head obtained! ✦");
        }
    }
    
    /**
     * Get top damage dealer
     */
    private String getTopDamager() {
        UUID topDamagerId = playerDamage.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        if (topDamagerId != null) {
            Player topPlayer = Bukkit.getPlayer(topDamagerId);
            if (topPlayer != null) {
                return topPlayer.getName();
            }
        }
        
        return participants.isEmpty() ? "Unknown" : participants.get(0).getName();
    }
    
    /**
     * Cleanup combat resources
     */
    private void cleanup() {
        if (combatTask != null) {
            combatTask.cancel();
        }
        
        participants.clear();
        playerDamage.clear();
        crystalLocations.clear();
        summonedMinions.clear();
        
        dragon = null;
        currentPhase = DragonPhase.CRYSTAL_PHASE;
        phaseTransitions = 0;
        isEnraged = false;
    }
    
    /**
     * Dragon combat phases
     */
    public enum DragonPhase {
        CRYSTAL_PHASE("Crystal Defense Phase"),
        AERIAL_PHASE("Aerial Assault Phase"),
        GROUND_PHASE("Ground Combat Phase"),
        ENRAGED_PHASE("Enraged Final Phase");
        
        private final String displayName;
        
        DragonPhase(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Getters for external access
    public DragonPhase getCurrentPhase() { return currentPhase; }
    public int getPhaseTransitions() { return phaseTransitions; }
    public boolean isEnraged() { return isEnraged; }
    public List<Player> getParticipants() { return new ArrayList<>(participants); }
}