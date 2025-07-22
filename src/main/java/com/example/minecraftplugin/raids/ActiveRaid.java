package com.example.minecraftplugin.raids;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.raids.RaidScalingCalculator;
import com.example.minecraftplugin.raids.WeeklyChallenge;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an active raid instance
 */
public class ActiveRaid {
    
    private final String instanceId;
    private final RaidDefinition definition;
    private final List<Player> players;
    private final Location startLocation;
    private final long startTime;
    private final RaidScalingCalculator.RaidScaling scaling;
    private final WeeklyChallenge weeklyChallenge;
    
    // Raid location
    private final Location raidLocation;
    
    // Store original player locations for teleporting back
    private final Map<UUID, Location> originalLocations;
    
    // Store original blocks for obsidian encasing
    private final Map<Location, Material> originalBlocks;
    
    private RaidState state;
    private BukkitTask timeoutTask;
    private int currentObjective;
    private final List<String> completedObjectives;
    
    // Eternal Crucible specific tracking
    private int nexusCoreHealth;
    private int currentWave;
    private int godsDefeated;
    private boolean nexusCoreActive;
    
    // Divine Council integration
    private boolean councilBonusActive;
    private double rewardMultiplier;
    
    // Performance tracking
    private int objectivesCompleted;
    private int mobsDefeated;
    private int playersRevived;
    private int raidPointsAwarded;
    
    // Mob spawning
    private BukkitTask mobSpawningTask;
    private final List<LivingEntity> spawnedMobs;
    
    public ActiveRaid(String instanceId, RaidDefinition definition, List<Player> players, 
                     Location startLocation, Location raidLocation, RaidScalingCalculator.RaidScaling scaling, 
                     WeeklyChallenge weeklyChallenge) {
        this.instanceId = instanceId;
        this.definition = definition;
        this.players = new ArrayList<>(players);
        this.startLocation = startLocation;
        this.raidLocation = raidLocation;
        this.startTime = System.currentTimeMillis();
        this.scaling = scaling;
        this.weeklyChallenge = weeklyChallenge;
        this.originalLocations = new HashMap<>();
        this.originalBlocks = new HashMap<>();
        this.state = RaidState.PREPARING;
        this.currentObjective = 0;
        this.completedObjectives = new ArrayList<>();
        this.councilBonusActive = false;
        this.rewardMultiplier = 1.0;
        this.objectivesCompleted = 0;
        this.mobsDefeated = 0;
        this.playersRevived = 0;
        this.raidPointsAwarded = 0;
        this.spawnedMobs = new ArrayList<>();
        
        // Initialize Eternal Crucible specific data
        if ("eternal_crucible".equals(definition.getId())) {
            this.nexusCoreHealth = 1000;
            this.currentWave = 0;
            this.godsDefeated = 0;
            this.nexusCoreActive = true;
        }
        
        // Store original locations for all players
        for (Player player : players) {
            originalLocations.put(player.getUniqueId(), player.getLocation().clone());
        }
    }
    
    /**
     * Start the raid
     */
    public void start(MinecraftPlugin plugin) {
        state = RaidState.ACTIVE;
        
        // Notify all players
        for (Player player : players) {
            player.sendTitle("§6§l⚔ RAID STARTED! ⚔", "§7" + definition.getDisplayName(), 20, 60, 20);
            player.sendMessage("§6§l=== RAID STARTED ===§r");
            player.sendMessage("§7Raid: §f" + definition.getDisplayName());
            player.sendMessage("§7Objective: §f" + definition.getObjective());
            
            // Calculate actual time limit with scaling
            int actualTimeLimit = (int) (definition.getTimeLimit() * scaling.getTimeMultiplier());
            player.sendMessage("§7Time Limit: §f" + (actualTimeLimit / 60) + " minutes");
            player.sendMessage("§7Players: §f" + players.size());
            
            // Show scaling information
            player.sendMessage("§7Scaling: §f" + scaling.getFormattedDisplay());
            
            // Show weekly challenge if active
            if (weeklyChallenge != null && weeklyChallenge.isActive()) {
                player.sendMessage("§e§l⭐ Weekly Challenge: §r§e" + weeklyChallenge.getDisplayName());
                player.sendMessage("§7" + weeklyChallenge.getDescription());
            }
            
            player.sendMessage("");
            player.sendMessage("§a§lGood luck! Work together to succeed!");
            
            // Play start sound
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 1.0f);
        }
        
        // Teleport players to raid area
        for (Player player : players) {
            player.teleport(raidLocation);
            player.sendMessage("§6§l⚡ TELEPORTED TO RAID AREA! ⚡");
            player.sendMessage("§7You will be returned to your original location when the raid ends.");
            
            // Create obsidian encasing around player (9x9x5 box)
            createObsidianEncasing(player.getLocation());
        }
        
        // Start timeout timer
        int actualTimeLimit = (int) (definition.getTimeLimit() * scaling.getTimeMultiplier());
        timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (state == RaidState.ACTIVE) {
                    // Raid timed out
                    for (Player player : players) {
                        player.sendTitle("§c§l⏰ TIME'S UP! ⏰", "§7Raid failed due to timeout", 20, 80, 20);
                        player.sendMessage("§c§lRaid Failed! §r§cTime limit exceeded.");
                        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.8f, 0.8f);
                    }
                    
                    // End raid with timeout result
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        plugin.getRaidManager().endRaid(instanceId, RaidManager.RaidResult.TIMEOUT);
                    });
                }
            }
        }.runTaskLater(plugin, actualTimeLimit * 20L); // Convert seconds to ticks with scaling
        
        // Start raid progress monitoring
        startProgressMonitoring(plugin);
        
        // Start mob spawning
        startMobSpawning(plugin);
    }
    
    /**
     * Start mob spawning task
     */
    private void startMobSpawning(MinecraftPlugin plugin) {
        mobSpawningTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (state != RaidState.ACTIVE) {
                    cancel();
                    return;
                }
                
                spawnRaidMobs();
            }
        }.runTaskTimer(plugin, 100L, 400L); // Start after 5 seconds, repeat every 20 seconds
    }
    
    /**
     * Spawn mobs based on raid tier and scaling
     */
    private void spawnRaidMobs() {
        // Clean up dead mobs
        spawnedMobs.removeIf(mob -> mob.isDead() || !mob.isValid());
        
        // Don't spawn too many mobs
        int maxMobs = (int) (10 * scaling.getMobCountMultiplier());
        if (spawnedMobs.size() >= maxMobs) {
            return;
        }
        
        // Determine mob types based on raid tier
        EntityType[] mobTypes = getMobTypesForTier(definition.getTier());
        int mobsToSpawn = (int) (2 + players.size() * scaling.getMobCountMultiplier());
        
        for (int i = 0; i < mobsToSpawn && spawnedMobs.size() < maxMobs; i++) {
            EntityType mobType = mobTypes[(int) (Math.random() * mobTypes.length)];
            spawnMob(mobType);
        }
    }
    
    /**
     * Get mob types for raid tier
     */
    private EntityType[] getMobTypesForTier(RaidManager.RaidTier tier) {
        switch (tier) {
            case NOVICE:
                return new EntityType[]{EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER};
            case ADEPT:
                return new EntityType[]{EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, 
                                      EntityType.WITCH, EntityType.PILLAGER};
            case MASTER:
                return new EntityType[]{EntityType.WITHER_SKELETON, EntityType.BLAZE, EntityType.ENDERMAN,
                                      EntityType.VINDICATOR, EntityType.EVOKER};
            case CONVERGENCE:
                return new EntityType[]{EntityType.WITHER_SKELETON, EntityType.BLAZE, EntityType.ENDERMAN,
                                      EntityType.SHULKER, EntityType.WARDEN, EntityType.ELDER_GUARDIAN};
            default:
                return new EntityType[]{EntityType.ZOMBIE, EntityType.SKELETON};
        }
    }
    
    /**
     * Spawn a specific mob type
     */
    private void spawnMob(EntityType mobType) {
        // Find spawn location around players
        Player randomPlayer = players.get((int) (Math.random() * players.size()));
        Location spawnLoc = randomPlayer.getLocation().add(
            (Math.random() - 0.5) * 20, // Random X offset
            0,
            (Math.random() - 0.5) * 20  // Random Z offset
        );
        
        // Find safe spawn location
        for (int y = spawnLoc.getBlockY() + 5; y >= spawnLoc.getBlockY() - 5; y--) {
            spawnLoc.setY(y);
            if (spawnLoc.getBlock().getType().isSolid() && 
                spawnLoc.clone().add(0, 1, 0).getBlock().getType().isAir() &&
                spawnLoc.clone().add(0, 2, 0).getBlock().getType().isAir()) {
                spawnLoc.add(0, 1, 0);
                break;
            }
        }
        
        try {
            LivingEntity mob = (LivingEntity) spawnLoc.getWorld().spawnEntity(spawnLoc, mobType);
            
            // Apply scaling
            if (mob.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH) != null) {
                double newHealth = mob.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getBaseValue() * scaling.getHealthMultiplier();
                mob.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(newHealth);
                mob.setHealth(newHealth);
            }
            
            // Add raid-specific effects
            mob.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.STRENGTH, Integer.MAX_VALUE, 
                (int) (scaling.getDamageMultiplier() - 1)));
            
            // Mark as raid mob
            mob.setCustomName("§c§lRaid " + mobType.name().replace("_", " "));
            mob.setCustomNameVisible(true);
            
            spawnedMobs.add(mob);
            
        } catch (Exception e) {
            // Some mob types might not be spawnable in certain conditions
        }
    }
    
    /**
     * Start monitoring raid progress
     */
    private void startProgressMonitoring(MinecraftPlugin plugin) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (state != RaidState.ACTIVE) {
                    cancel();
                    return;
                }
                
                // Update every 30 seconds
                if (ticks % 600 == 0) {
                    updateRaidProgress();
                }
                
                // Check for raid completion conditions
                if (checkRaidCompletion()) {
                    // Raid completed successfully
                    for (Player player : players) {
                        player.sendTitle("§a§l✓ RAID COMPLETED! ✓", "§7Victory achieved!", 20, 100, 20);
                        player.sendMessage("§a§l✓ RAID COMPLETED SUCCESSFULLY! ✓");
                        player.sendMessage("§7Congratulations! You have conquered " + definition.getDisplayName());
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    }
                    
                    // End raid with success result
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        plugin.getRaidManager().endRaid(instanceId, RaidManager.RaidResult.SUCCESS);
                    });
                    cancel();
                    return;
                }
                
                // Check for raid failure conditions
                if (checkRaidFailure()) {
                    // Raid failed
                    for (Player player : players) {
                        player.sendTitle("§c§l✗ RAID FAILED! ✗", "§7Better luck next time", 20, 80, 20);
                        player.sendMessage("§c§l✗ RAID FAILED! ✗");
                        player.sendMessage("§7The raid has ended in failure. Regroup and try again!");
                        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.8f, 0.8f);
                    }
                    
                    // End raid with failure result
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        plugin.getRaidManager().endRaid(instanceId, RaidManager.RaidResult.FAILURE);
                    });
                    cancel();
                    return;
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Update raid progress for all players
     */
    private void updateRaidProgress() {
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        long remainingTime = definition.getTimeLimit() - elapsedTime;
        
        for (Player player : players) {
            player.sendMessage("§e§lRaid Progress Update:");
            player.sendMessage("§7Time Remaining: §f" + (remainingTime / 60) + ":" + String.format("%02d", remainingTime % 60));
            player.sendMessage("§7Objectives Completed: §f" + completedObjectives.size());
            player.sendMessage("§7Current Objective: §f" + getCurrentObjectiveDescription());
        }
    }
    
    /**
     * Check if raid is completed
     */
    private boolean checkRaidCompletion() {
        // Check for Eternal Crucible specific completion
        if ("eternal_crucible".equals(definition.getId())) {
            return checkEternalCrucibleCompletion();
        }
        
        // Default completion check for other raids
        return completedObjectives.size() >= getRequiredObjectives();
    }
    
    /**
     * Check if raid has failed
     */
    private boolean checkRaidFailure() {
        // Check for Eternal Crucible specific failure
        if ("eternal_crucible".equals(definition.getId())) {
            return checkEternalCrucibleFailure();
        }
        
        // Check if all players are dead or disconnected
        int alivePlayers = 0;
        for (Player player : players) {
            if (player.isOnline() && player.getHealth() > 0) {
                alivePlayers++;
            }
        }
        
        return alivePlayers == 0;
    }
    
    /**
     * Check Eternal Crucible completion conditions
     */
    private boolean checkEternalCrucibleCompletion() {
        // Must defeat all 12 god manifestations and keep Nexus Core alive
        return godsDefeated >= 12 && nexusCoreActive && nexusCoreHealth > 0;
    }
    
    /**
     * Check Eternal Crucible failure conditions
     */
    private boolean checkEternalCrucibleFailure() {
        // Fail if Nexus Core is destroyed or all players are dead
        if (!nexusCoreActive || nexusCoreHealth <= 0) {
            for (Player player : players) {
                player.sendMessage("§c§l✗ THE NEXUS CORE HAS BEEN DESTROYED! ✗");
                player.sendMessage("§7The cosmic balance has been shattered!");
            }
            return true;
        }
        
        // Check if all players are dead
        int alivePlayers = 0;
        for (Player player : players) {
            if (player.isOnline() && player.getHealth() > 0) {
                alivePlayers++;
            }
        }
        
        return alivePlayers == 0;
    }
    
    /**
     * Create obsidian encasing around player location (9x9x5 box)
     */
    private void createObsidianEncasing(Location center) {
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                for (int y = 0; y <= 4; y++) {
                    Location blockLoc = center.clone().add(x, y, z);
                    
                    // Store original block type
                    originalBlocks.put(blockLoc.clone(), blockLoc.getBlock().getType());
                    
                    // Set to obsidian
                    blockLoc.getBlock().setType(Material.OBSIDIAN);
                }
            }
        }
    }
    
    /**
     * Restore original blocks after raid
     */
    private void restoreOriginalBlocks() {
        for (Map.Entry<Location, Material> entry : originalBlocks.entrySet()) {
            Location location = entry.getKey();
            Material originalMaterial = entry.getValue();
            
            if (location.getWorld() != null) {
                location.getBlock().setType(originalMaterial);
            }
        }
        originalBlocks.clear();
    }
    
    /**
     * Handle Eternal Crucible specific events
     */
    public void handleEternalCrucibleEvent(String eventType, Object... params) {
        switch (eventType) {
            case "god_defeated":
                godsDefeated++;
                String godName = (String) params[0];
                completeObjective("Defeated " + godName + " manifestation (" + godsDefeated + "/12)");
                break;
            case "nexus_damage":
                int damage = (Integer) params[0];
                nexusCoreHealth = Math.max(0, nexusCoreHealth - damage);
                for (Player player : players) {
                    player.sendMessage("§c§l⚠ NEXUS CORE DAMAGED! ⚠ Health: " + nexusCoreHealth + "/1000");
                }
                break;
            case "wave_complete":
                currentWave++;
                break;
        }
    }
    
    /**
     * Get current objective description
     */
    private String getCurrentObjectiveDescription() {
        if ("eternal_crucible".equals(definition.getId())) {
            return "Wave " + (currentWave + 1) + " - Defeat god manifestations (" + godsDefeated + "/12) - Protect Nexus Core (" + nexusCoreHealth + "/1000 HP)";
        }
        
        String baseObjective = definition.getObjective();
        if (weeklyChallenge != null && weeklyChallenge.isActive()) {
            return baseObjective + " §e(Weekly Challenge: " + weeklyChallenge.getDisplayName() + ")";
        }
        return baseObjective;
    }
    
    /**
     * Get required number of objectives for completion
     */
    private int getRequiredObjectives() {
        // This would be defined per raid type
        switch (definition.getTier()) {
            case NOVICE:
                return 3;
            case ADEPT:
                return 5;
            case MASTER:
                return 7;
            case CONVERGENCE:
                return 10;
            default:
                return 1;
        }
    }
    
    /**
     * Complete an objective
     */
    public void completeObjective(String objectiveDescription) {
        completedObjectives.add(objectiveDescription);
        currentObjective++;
        objectivesCompleted++;
        
        // Notify all players
        for (Player player : players) {
            player.sendMessage("§a§l✓ Objective Completed: §r§a" + objectiveDescription);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        }
    }
    
    /**
     * Record mob defeat for statistics
     */
    public void recordMobDefeat() {
        mobsDefeated++;
    }
    
    /**
     * Record player revival for statistics
     */
    public void recordPlayerRevival() {
        playersRevived++;
    }
    
    /**
     * Remove a player from the raid
     */
    public void removePlayer(Player player) {
        players.remove(player);
        
        // Notify remaining players
        for (Player remainingPlayer : players) {
            remainingPlayer.sendMessage("§e§l" + player.getName() + " §r§ehas left the raid.");
        }
    }
    
    /**
     * End the raid
     */
    public void end(RaidManager.RaidResult result) {
        state = RaidState.COMPLETED;
        
        // Cancel timeout task
        if (timeoutTask != null) {
            timeoutTask.cancel();
        }
        
        // Cancel mob spawning task
        if (mobSpawningTask != null) {
            mobSpawningTask.cancel();
        }
        
        // Clean up spawned mobs
        for (LivingEntity mob : spawnedMobs) {
            if (mob != null && !mob.isDead()) {
                mob.remove();
            }
        }
        spawnedMobs.clear();
        
        // Restore original blocks
        restoreOriginalBlocks();
        
        // Teleport players back to start location
        for (Player player : players) {
            if (player.isOnline()) {
                // Teleport back to original location if available, otherwise start location
                Location returnLocation = originalLocations.get(player.getUniqueId());
                if (returnLocation != null) {
                    player.teleport(returnLocation);
                    player.sendMessage("§a§lReturned to your original location!");
                } else {
                    player.teleport(startLocation);
                }
            }
        }
        
        // Award rewards based on result
        if (result == RaidManager.RaidResult.SUCCESS) {
            awardRewards();
        }
    }
    
    /**
     * Award rewards to players
     */
    private void awardRewards() {
        for (Player player : players) {
            if (!player.isOnline()) {
                continue;
            }
            
            // Award XP
            int xpReward = calculateXpReward();
            player.giveExp(xpReward);
            
            // Award raid points
            int raidPoints = calculateRaidPoints();
            // Note: This would need PlayerDataManager to be accessible
            // For now, we'll track it locally
            raidPointsAwarded += raidPoints;
            
            // Award items based on raid tier
            awardTierSpecificRewards(player);
            
            // Show council bonus if active
            String bonusText = councilBonusActive ? " §d(Council Bonus!)" : "";
            
            player.sendMessage("§a§lRewards Earned:");
            player.sendMessage("§7• Experience: §f" + xpReward + " XP" + bonusText);
            player.sendMessage("§7• Raid Points: §f" + raidPoints + " RP");
            player.sendMessage("§7• Tier-specific rewards based on raid difficulty");
            
            if (councilBonusActive) {
                player.sendMessage("§d§l✦ Divine Council Blessing Applied! ✦");
                player.sendMessage("§7The Divine Council has enhanced your raid rewards!");
            }
        }
    }
    
    /**
     * Calculate raid points reward
     */
    private int calculateRaidPoints() {
        int basePoints;
        switch (definition.getTier()) {
            case NOVICE:
                basePoints = 50;
                break;
            case ADEPT:
                basePoints = 100;
                break;
            case MASTER:
                basePoints = 200;
                break;
            case CONVERGENCE:
                basePoints = 500;
                break;
            default:
                basePoints = 25;
                break;
        }
        
        // Bonus for objectives completed
        int objectiveBonus = completedObjectives.size() * 25;
        
        int totalPoints = basePoints + objectiveBonus;
        
        // Apply scaling bonuses
        totalPoints = (int) (totalPoints * ((scaling.getHealthMultiplier() + scaling.getDamageMultiplier()) / 2.0));
        
        // Weekly challenge bonus
        if (weeklyChallenge != null && weeklyChallenge.isActive()) {
            totalPoints = (int) (totalPoints * 1.5);
        }
        
        return totalPoints;
    }
    
    /**
     * Calculate XP reward based on raid tier and performance
     */
    private int calculateXpReward() {
        int baseXp;
        switch (definition.getTier()) {
            case NOVICE:
                baseXp = 100;
                break;
            case ADEPT:
                baseXp = 250;
                break;
            case MASTER:
                baseXp = 500;
                break;
            case CONVERGENCE:
                baseXp = 1000;
                break;
            default:
                baseXp = 50;
                break;
        }
        
        // Bonus for completing objectives
        int objectiveBonus = completedObjectives.size() * 50;
        
        int totalXp = baseXp + objectiveBonus;
        
        // Apply Divine Council bonus if active
        if (councilBonusActive) {
            totalXp = (int) (totalXp * rewardMultiplier);
        }
        
        // Apply weekly challenge bonus
        if (weeklyChallenge != null && weeklyChallenge.isActive()) {
            totalXp = (int) (totalXp * 1.3); // 30% bonus for weekly challenge
        }
        
        return totalXp;
    }
    
    /**
     * Award tier-specific rewards
     */
    private void awardTierSpecificRewards(Player player) {
        switch (definition.getTier()) {
            case NOVICE:
                awardNoviceRewards(player);
                break;
            case ADEPT:
                awardAdeptRewards(player);
                break;
            case MASTER:
                awardMasterRewards(player);
                break;
            case CONVERGENCE:
                awardConvergenceRewards(player);
                break;
        }
    }
    
    /**
     * Award rewards for Novice tier raids
     */
    private void awardNoviceRewards(Player player) {
        player.sendMessage("§a§l✦ NOVICE RAID REWARDS ✦");
        
        // Higher chance for divine fragment
        if (Math.random() < 0.3) { // 30% chance
            player.sendMessage("§7• §6Divine Fragment §7(lucky drop!)");
            // TODO: Actually give a random divine fragment
        }
        
        // Basic upgrade materials
        player.sendMessage("§7• §fBasic Upgrade Materials:");
        
        // Iron ingots (8-15) - increased
        int ironAmount = 8 + (int)(Math.random() * 8);
        ItemStack iron = new ItemStack(org.bukkit.Material.IRON_INGOT, ironAmount);
        player.getInventory().addItem(iron);
        player.sendMessage("§7  - §fIron Ingots x" + ironAmount);
        
        // Coal (12-20) - increased
        int coalAmount = 12 + (int)(Math.random() * 9);
        ItemStack coal = new ItemStack(org.bukkit.Material.COAL, coalAmount);
        player.getInventory().addItem(coal);
        player.sendMessage("§7  - §fCoal x" + coalAmount);
        
        // Gold ingots (2-5) - new addition
        int goldAmount = 2 + (int)(Math.random() * 4);
        ItemStack gold = new ItemStack(org.bukkit.Material.GOLD_INGOT, goldAmount);
        player.getInventory().addItem(gold);
        player.sendMessage("§7  - §6Gold Ingots x" + goldAmount);
        
        // String (5-10) - increased
        int stringAmount = 5 + (int)(Math.random() * 6);
        ItemStack string = new ItemStack(org.bukkit.Material.STRING, stringAmount);
        player.getInventory().addItem(string);
        player.sendMessage("§7  - §fString x" + stringAmount);
        
        // Bones (3-8) - increased
        int boneAmount = 3 + (int)(Math.random() * 6);
        ItemStack bones = new ItemStack(org.bukkit.Material.BONE, boneAmount);
        player.getInventory().addItem(bones);
        player.sendMessage("§7  - §fBones x" + boneAmount);
        
        // Leather (2-6) - increased
        int leatherAmount = 2 + (int)(Math.random() * 5);
        ItemStack leather = new ItemStack(org.bukkit.Material.LEATHER, leatherAmount);
        player.getInventory().addItem(leather);
        player.sendMessage("§7  - §fLeather x" + leatherAmount);
    }
    
    /**
     * Award rewards for Adept tier raids
     */
    private void awardAdeptRewards(Player player) {
        player.sendMessage("§e§l✦ ADEPT RAID REWARDS ✦");
        
        // Divine fragments (higher chance)
        if (Math.random() < 0.6) { // 60% chance
            player.sendMessage("§7• §6Divine Fragment §7(good chance!)");
            // TODO: Actually give a random divine fragment
        }
        
        // Small chance for divine item
        if (Math.random() < 0.1) { // 10% chance
            player.sendMessage("§7• §5Divine Item §7(rare drop!)");
            // TODO: Give random low-tier divine item
        }
        
        // Divine item upgrade components
        player.sendMessage("§7• §bDivine Item Upgrade Components:");
        
        // Gold ingots (5-10) - increased
        int goldAmount = 5 + (int)(Math.random() * 6);
        ItemStack gold = new ItemStack(org.bukkit.Material.GOLD_INGOT, goldAmount);
        player.getInventory().addItem(gold);
        player.sendMessage("§7  - §6Gold Ingots x" + goldAmount);
        
        // Diamonds (1-3) - new addition
        int diamondAmount = 1 + (int)(Math.random() * 3);
        ItemStack diamonds = new ItemStack(org.bukkit.Material.DIAMOND, diamondAmount);
        player.getInventory().addItem(diamonds);
        player.sendMessage("§7  - §bDiamonds x" + diamondAmount);
        
        // Redstone (15-25) - increased
        int redstoneAmount = 15 + (int)(Math.random() * 11);
        ItemStack redstone = new ItemStack(org.bukkit.Material.REDSTONE, redstoneAmount);
        player.getInventory().addItem(redstone);
        player.sendMessage("§7  - §cRedstone x" + redstoneAmount);
        
        // Lapis Lazuli (8-15) - increased
        int lapisAmount = 8 + (int)(Math.random() * 8);
        ItemStack lapis = new ItemStack(org.bukkit.Material.LAPIS_LAZULI, lapisAmount);
        player.getInventory().addItem(lapis);
        player.sendMessage("§7  - §9Lapis Lazuli x" + lapisAmount);
        
        // Ender Pearls (2-5) - increased
        int pearlAmount = 2 + (int)(Math.random() * 4);
        ItemStack pearls = new ItemStack(org.bukkit.Material.ENDER_PEARL, pearlAmount);
        player.getInventory().addItem(pearls);
        player.sendMessage("§7  - §5Ender Pearls x" + pearlAmount);
        
        // Blaze Powder (3-7) - increased
        int blazeAmount = 3 + (int)(Math.random() * 5);
        ItemStack blaze = new ItemStack(org.bukkit.Material.BLAZE_POWDER, blazeAmount);
        player.getInventory().addItem(blaze);
        player.sendMessage("§7  - §6Blaze Powder x" + blazeAmount);
        
        // Ghast Tear (1-3) - rare component, higher chance
        if (Math.random() < 0.8) { // 80% chance
            int tearAmount = 1 + (int)(Math.random() * 3);
            ItemStack tears = new ItemStack(org.bukkit.Material.GHAST_TEAR, tearAmount);
            player.getInventory().addItem(tears);
            player.sendMessage("§7  - §fGhast Tears x" + tearAmount + " §7(rare!)");
        }
    }
    
    /**
     * Award rewards for Master tier raids
     */
    private void awardMasterRewards(Player player) {
        player.sendMessage("§6§l✦ MASTER RAID REWARDS ✦");
        
        // Guaranteed divine fragment
        player.sendMessage("§7• §6Divine Fragment §7(guaranteed)");
        // TODO: Actually give a random divine fragment
        
        // Good chance for divine item
        if (Math.random() < 0.4) { // 40% chance
            player.sendMessage("§7• §5Divine Item §7(good chance!)");
            // TODO: Give random divine item
        }
        
        // Small chance for upgrade material
        if (Math.random() < 0.2) { // 20% chance
            player.sendMessage("§7• §bUpgrade Material §7(rare!)");
            // TODO: Give random upgrade material
        }
        
        // Legendary materials
        player.sendMessage("§7• §dLegendary Materials:");
        
        // Diamond (4-8) - increased
        int diamondAmount = 4 + (int)(Math.random() * 5);
        ItemStack diamonds = new ItemStack(org.bukkit.Material.DIAMOND, diamondAmount);
        player.getInventory().addItem(diamonds);
        player.sendMessage("§7  - §bDiamonds x" + diamondAmount);
        
        // Netherite Scrap (2-4) - increased
        int netheriteAmount = 2 + (int)(Math.random() * 3);
        ItemStack netherite = new ItemStack(org.bukkit.Material.NETHERITE_SCRAP, netheriteAmount);
        player.getInventory().addItem(netherite);
        player.sendMessage("§7  - §8Netherite Scrap x" + netheriteAmount);
        
        // Ancient Debris (1-3) - higher chance
        if (Math.random() < 0.7) { // 70% chance
            int debrisAmount = 1 + (int)(Math.random() * 3);
            ItemStack debris = new ItemStack(org.bukkit.Material.ANCIENT_DEBRIS, debrisAmount);
            player.getInventory().addItem(debris);
            player.sendMessage("§7  - §8Ancient Debris x" + debrisAmount + " §7(legendary!)");
        }
        
        // Emerald (5-12) - increased
        int emeraldAmount = 5 + (int)(Math.random() * 8);
        ItemStack emeralds = new ItemStack(org.bukkit.Material.EMERALD, emeraldAmount);
        player.getInventory().addItem(emeralds);
        player.sendMessage("§7  - §aEmeralds x" + emeraldAmount);
        
        // Nether Star (1-2) - guaranteed legendary component
        int starAmount = 1 + (int)(Math.random() * 2);
        ItemStack netherStar = new ItemStack(org.bukkit.Material.NETHER_STAR, starAmount);
        player.getInventory().addItem(netherStar);
        player.sendMessage("§7  - §fNether Star x" + starAmount + " §7(guaranteed!)");
        
        // Advanced divine enhancements
        player.sendMessage("§7• §5Advanced Divine Enhancements:");
        player.sendMessage("§7  - Enhanced divine item properties");
        player.sendMessage("§7  - Increased divine power effectiveness");
    }
    
    /**
     * Award specific rewards for Convergence raids
     */
    private void awardConvergenceRewards(Player player) {
        if ("eternal_crucible".equals(definition.getId())) {
            // Eternal Crucible specific rewards
            player.sendMessage("§5§l✦ ETERNAL CRUCIBLE REWARDS ✦");
            player.sendMessage("§7• §5Crucible Crown §7- Legendary headpiece");
            player.sendMessage("§7• §5Divine Essence Crystals §7(x8)"); // Increased
            player.sendMessage("§7• §5Nexus Core Fragment §7- Ultimate crafting material");
            player.sendMessage("§7• §5Eternal Title §7- 'Crucible Survivor'");
            player.sendMessage("§7• §5Permanent +3 Hearts §7bonus"); // Increased
            player.sendMessage("§7• §5Legendary Armor Piece §7- Random divine armor");
            
            // Apply permanent health bonus (increased)
            org.bukkit.attribute.AttributeInstance healthAttr = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
            if (healthAttr != null) {
                double currentMax = healthAttr.getBaseValue();
                healthAttr.setBaseValue(Math.min(60.0, currentMax + 6.0)); // +3 hearts, max 30 hearts
                player.sendMessage("§a§lPermanent Health Bonus Applied! §r§aYou now have " + (int)(healthAttr.getBaseValue() / 2) + " hearts!");
            }
            
            // Guaranteed upgrade materials
            ItemStack divineEssence = new ItemStack(org.bukkit.Material.NETHER_STAR, 5);
            player.getInventory().addItem(divineEssence);
            
            // TODO: Give random divine armor piece
            
        } else {
            // Default convergence rewards
            player.sendMessage("§7• §5Guaranteed Divine Item");
            player.sendMessage("§7• §bMultiple Upgrade Materials");
            player.sendMessage("§7• §6Legendary Variant Chance");
            player.sendMessage("§7• §dCustom Armor Piece");
        }
    }
    
    // Getters
    public String getInstanceId() {
        return instanceId;
    }
    
    public RaidDefinition getDefinition() {
        return definition;
    }
    
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }
    
    public Location getStartLocation() {
        return startLocation;
    }
    
    public RaidState getState() {
        return state;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public List<String> getCompletedObjectives() {
        return new ArrayList<>(completedObjectives);
    }
    
    // Getters for Eternal Crucible specific data
    public int getNexusCoreHealth() {
        return nexusCoreHealth;
    }
    
    public int getCurrentWave() {
        return currentWave;
    }
    
    public int getGodsDefeated() {
        return godsDefeated;
    }
    
    public boolean isNexusCoreActive() {
        return nexusCoreActive;
    }
    
    // Divine Council integration getters/setters
    public boolean isCouncilBonusActive() {
        return councilBonusActive;
    }
    
    public void setCouncilBonusActive(boolean councilBonusActive) {
        this.councilBonusActive = councilBonusActive;
        this.rewardMultiplier = councilBonusActive ? 1.5 : 1.0; // 50% bonus
    }
    
    public double getRewardMultiplier() {
        return rewardMultiplier;
    }
    
    public Map<UUID, Location> getOriginalLocations() {
        return new HashMap<>(originalLocations);
    }
    
    // Getters for scaling and weekly challenge
    public RaidScalingCalculator.RaidScaling getScaling() {
        return scaling;
    }
    
    public WeeklyChallenge getWeeklyChallenge() {
        return weeklyChallenge;
    }
    
    // Getters for performance tracking
    public int getObjectivesCompleted() {
        return objectivesCompleted;
    }
    
    public int getMobsDefeated() {
        return mobsDefeated;
    }
    
    public int getPlayersRevived() {
        return playersRevived;
    }
    
    public int getRaidPointsAwarded() {
        return raidPointsAwarded;
    }
    
    /**
     * Raid states
     */
    public enum RaidState {
        PREPARING,
        ACTIVE,
        COMPLETED
    }
}