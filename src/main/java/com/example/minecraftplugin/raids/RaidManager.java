package com.example.minecraftplugin.raids;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.enums.GodType;
import com.example.minecraftplugin.managers.GuildManager;
import com.example.minecraftplugin.managers.RaidLeaderboardManager;

/**
 * Manages custom raids for both non-converged and converged players
 */
public class RaidManager implements Listener {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    private final RaidLeaderboardManager leaderboardManager;
    private final RaidScalingCalculator scalingCalculator;
    private final GuildManager guildManager;
    
    // Active raids
    private final Map<String, ActiveRaid> activeRaids;
    private final Map<UUID, String> playerRaidMap;
    
    // Raid definitions
    private final Map<String, RaidDefinition> raidDefinitions;
    
    // Weekly challenge system
    private WeeklyChallenge currentWeeklyChallenge;
    private final List<WeeklyChallenge> availableChallenges;
    
    public RaidManager(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.leaderboardManager = new RaidLeaderboardManager(plugin);
        this.guildManager = plugin.getGuildManager();
        this.scalingCalculator = new RaidScalingCalculator(plugin);
        this.activeRaids = new HashMap<>();
        this.playerRaidMap = new HashMap<>();
        this.raidDefinitions = new HashMap<>();
        this.availableChallenges = new ArrayList<>();
        
        // Register as event listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        // Initialize raid definitions
        initializeRaidDefinitions();
        
        // Initialize weekly challenges
        initializeWeeklyChallenges();
        
        // Start weekly challenge rotation
        startWeeklyChallengeRotation();
        
        logger.info("Raid Manager initialized with " + raidDefinitions.size() + " raid types");
    }
    
    /**
     * Initialize predefined raid definitions
     */
    private void initializeRaidDefinitions() {
        // Tier 1: Novice Raids (0-2 Testaments)
        raidDefinitions.put("sylvan_grove_blight", new RaidDefinition(
            "sylvan_grove_blight",
            "The Sylvan Grove's Blight",
            "Nature corrupted by a dark force",
            RaidTier.NOVICE,
            1, 2, // 1-2 players
            Arrays.asList(GodType.SYLVAN),
            300, // 15 minutes
            "Clear the blighted area and cleanse the Heartwood"
        ));
        
        raidDefinitions.put("fallen_crypt", new RaidDefinition(
            "fallen_crypt",
            "The Fallen Crypt",
            "Ancient tomb filled with undead guardians",
            RaidTier.NOVICE,
            1, 2,
            Arrays.asList(GodType.FALLEN),
            300,
            "Defeat the Crypt Guardian and claim the Soul Essence"
        ));
        
        // Tier 2: Adept Raids (3-6 Testaments)
        raidDefinitions.put("banishment_forge", new RaidDefinition(
            "banishment_forge",
            "The Banishment Forge's Fury",
            "An ancient forge overflowing with uncontrolled fire",
            RaidTier.ADEPT,
            2, 3, // 2-3 players
            Arrays.asList(GodType.BANISHMENT, GodType.FORGE),
            450, // 22.5 minutes
            "Navigate lava flows and defeat the Molten Golem"
        ));
        
        raidDefinitions.put("abyssal_depths", new RaidDefinition(
            "abyssal_depths",
            "The Abyssal Depths",
            "Underwater temple guarded by sea monsters",
            RaidTier.ADEPT,
            2, 3,
            Arrays.asList(GodType.ABYSSAL),
            450,
            "Dive deep and recover the Trident of Tides"
        ));
        
        // Tier 3: Master Raids (7-11 Testaments)
        raidDefinitions.put("veil_nexus", new RaidDefinition(
            "veil_nexus",
            "The Veil's Shifting Nexus",
            "A chaotic pocket dimension where reality is unstable",
            RaidTier.MASTER,
            3, 4, // 3-4 players
            Arrays.asList(GodType.VEIL, GodType.VOID),
            600, // 30 minutes
            "Navigate reality shifts and defeat the Void Weaver"
        ));
        
        raidDefinitions.put("tempest_peak", new RaidDefinition(
            "tempest_peak",
            "The Tempest Peak",
            "Storm-wracked mountain peak with lightning elementals",
            RaidTier.MASTER,
            3, 4,
            Arrays.asList(GodType.TEMPEST),
            600,
            "Ascend the peak and harness the Storm Crown"
        ));
        
        // Convergence Raids (12 Testaments)
        raidDefinitions.put("divine_convergence_trial", new RaidDefinition(
            "divine_convergence_trial",
            "The Divine Convergence Trial",
            "Ultimate test of all divine powers combined",
            RaidTier.CONVERGENCE,
            3, 6, // 3-6 players minimum
            Arrays.asList(), // All gods
            900, // 45 minutes
            "Face the Avatar of All Gods and prove your supremacy"
        ));
        
        raidDefinitions.put("reality_nexus", new RaidDefinition(
            "reality_nexus",
            "The Reality Nexus",
            "The core of existence itself, defended by cosmic entities",
            RaidTier.CONVERGENCE,
            4, 8, // 4-8 players
            Arrays.asList(),
            1200, // 60 minutes
            "Reshape reality and claim dominion over existence"
        ));
        
        raidDefinitions.put("eternal_crucible", new RaidDefinition(
            "eternal_crucible",
            "The Eternal Crucible",
            "Face manifestations of all 12 gods while protecting the Nexus Core",
            RaidTier.CONVERGENCE,
            3, 6, // 3-6 players
            Arrays.asList(), // All gods represented
            1800, // 90 minutes - longest raid
            "Survive waves of divine manifestations and protect the Nexus Core"
        ));
        
        // Enhanced Ender Dragon raid
        raidDefinitions.put("enhanced_ender_dragon", new RaidDefinition(
            "enhanced_ender_dragon",
            "Enhanced Ender Dragon",
            "Face the Ender Dragon with enhanced abilities and multiple combat phases",
            RaidTier.MASTER,
            2, 6, // 2-6 players
            Arrays.asList(), // No specific god association
            1200, // 60 minutes
            "Defeat the Enhanced Ender Dragon through all combat phases"
        ));
        
        // God-Specific Expansion Raids
        
        // Forge God Raid
        raidDefinitions.put("molten_core_descent", new RaidDefinition(
            "molten_core_descent",
            "The Molten Core Descent",
            "Navigate the ancient forge's molten depths and face automated defenses",
            RaidTier.MASTER,
            3, 4, // 3-4 players
            Arrays.asList(GodType.FORGE),
            900, // 45 minutes
            "Descend through lava flows, disable ancient machinery, and defeat the Molten Golem"
        ));
        
        // Void God Raid
        raidDefinitions.put("echoing_abyss", new RaidDefinition(
            "echoing_abyss",
            "The Echoing Abyss",
            "A maze-like void realm where reality bends and illusions reign",
            RaidTier.MASTER,
            3, 4, // 3-4 players
            Arrays.asList(GodType.VOID),
            1080, // 54 minutes
            "Navigate void traps, overcome illusions, and defeat the Shapeshifting Void Entity"
        ));
        
        // Time God Raid
        raidDefinitions.put("chronal_anomaly", new RaidDefinition(
            "chronal_anomaly",
            "The Chronal Anomaly",
            "A temporal distortion zone where time flows erratically",
            RaidTier.CONVERGENCE,
            4, 6, // 4-6 players
            Arrays.asList(GodType.TIME),
            1200, // 60 minutes
            "Solve temporal puzzles, fight time-manipulating enemies, and stabilize the anomaly"
        ));
        
        // Blood God Raid
        raidDefinitions.put("crimson_arena", new RaidDefinition(
            "crimson_arena",
            "The Crimson Arena",
            "Brutal gladiatorial combat with escalating bloodlust mechanics",
            RaidTier.ADEPT,
            2, 4, // 2-4 players
            Arrays.asList(GodType.BLOOD),
            600, // 30 minutes
            "Survive waves of combat, embrace bloodlust buffs, and defeat the Berserker Champion"
        ));
        
        // Crystal God Raid
        raidDefinitions.put("resonating_caverns", new RaidDefinition(
            "resonating_caverns",
            "The Resonating Caverns",
            "Crystal formations create harmonic puzzles and sonic challenges",
            RaidTier.ADEPT,
            2, 3, // 2-3 players
            Arrays.asList(GodType.CRYSTAL),
            450, // 22.5 minutes
            "Solve sound-based puzzles, avoid sonic attacks, and harmonize with the Crystal Core"
        ));
        
        // Shadow God Raid
        raidDefinitions.put("umbral_labyrinth", new RaidDefinition(
            "umbral_labyrinth",
            "The Umbral Labyrinth",
            "A stealth-focused maze shrouded in eternal darkness",
            RaidTier.MASTER,
            3, 4, // 3-4 players
            Arrays.asList(GodType.SHADOW),
            720, // 36 minutes
            "Navigate in darkness, avoid shadow creatures, solve light puzzles, and face the Umbral Assassin"
        ));
    }
    
    /**
     * Initialize available weekly challenges
     */
    private void initializeWeeklyChallenges() {
        availableChallenges.add(new WeeklyChallenge(
            "iron_will", "Iron Will", "Mobs have 50% more health", 
            WeeklyChallenge.ChallengeType.INCREASED_MOB_HEALTH, 1.5));
        
        availableChallenges.add(new WeeklyChallenge(
            "lightning_fast", "Lightning Fast", "Mobs move 30% faster", 
            WeeklyChallenge.ChallengeType.INCREASED_MOB_SPEED, 1.3));
        
        availableChallenges.add(new WeeklyChallenge(
            "no_mercy", "No Mercy", "Player healing reduced by 50%", 
            WeeklyChallenge.ChallengeType.REDUCED_PLAYER_HEALING, 0.5));
        
        availableChallenges.add(new WeeklyChallenge(
            "heavy_hitters", "Heavy Hitters", "Mobs deal 25% more damage", 
            WeeklyChallenge.ChallengeType.INCREASED_MOB_DAMAGE, 1.25));
        
        availableChallenges.add(new WeeklyChallenge(
            "time_crunch", "Time Crunch", "20% less time to complete", 
            WeeklyChallenge.ChallengeType.TIME_PRESSURE, 0.8));
        
        availableChallenges.add(new WeeklyChallenge(
            "swarm", "Swarm", "50% more mobs spawn", 
            WeeklyChallenge.ChallengeType.SWARM_MODE, 1.5));
        
        availableChallenges.add(new WeeklyChallenge(
            "elite_forces", "Elite Forces", "Mobs have special abilities", 
            WeeklyChallenge.ChallengeType.ELITE_MOBS, 1.0));
        
        availableChallenges.add(new WeeklyChallenge(
            "scarcity", "Resource Scarcity", "Limited resources available", 
            WeeklyChallenge.ChallengeType.RESOURCE_SCARCITY, 0.7));
        
        availableChallenges.add(new WeeklyChallenge(
            "darkness", "Eternal Darkness", "Reduced visibility throughout raid", 
            WeeklyChallenge.ChallengeType.DARKNESS, 1.0));
        
        availableChallenges.add(new WeeklyChallenge(
            "chaos", "Chaos Mode", "Random effects throughout the raid", 
            WeeklyChallenge.ChallengeType.CHAOS_MODE, 1.0));
        
        // Set initial weekly challenge
        if (!availableChallenges.isEmpty()) {
            currentWeeklyChallenge = availableChallenges.get(new Random().nextInt(availableChallenges.size()));
            logger.info("Initial weekly challenge: " + currentWeeklyChallenge.getDisplayName());
        }
    }
    
    /**
     * Start weekly challenge rotation
     */
    private void startWeeklyChallengeRotation() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            // Check if current challenge has expired
            if (currentWeeklyChallenge == null || !currentWeeklyChallenge.isActive()) {
                rotateWeeklyChallenge();
            }
        }, 0L, 72000L); // Check every hour
    }
    
    /**
     * Rotate to a new weekly challenge
     */
    private void rotateWeeklyChallenge() {
        if (availableChallenges.isEmpty()) {
            return;
        }
        
        // Select a random challenge different from the current one
        WeeklyChallenge newChallenge;
        do {
            newChallenge = availableChallenges.get(new Random().nextInt(availableChallenges.size()));
        } while (newChallenge.getId().equals(currentWeeklyChallenge != null ? currentWeeklyChallenge.getId() : ""));

        currentWeeklyChallenge = newChallenge;

        final WeeklyChallenge finalNewChallenge = newChallenge;
        // Announce new challenge
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getServer().broadcastMessage("§e§l⭐ NEW WEEKLY RAID CHALLENGE! ⭐");
            plugin.getServer().broadcastMessage("§7Challenge: §f" + finalNewChallenge.getDisplayName());
            plugin.getServer().broadcastMessage("§7Effect: §f" + finalNewChallenge.getDescription());
            plugin.getServer().broadcastMessage("§7Duration: §f7 days");
            plugin.getServer().broadcastMessage("§7Complete raids during this challenge for bonus rewards!");
        });

        logger.info("Rotated to new weekly challenge: " + finalNewChallenge.getDisplayName());
    }
    
    /**
     * Get available raids for a player based on their progression
     */
    public List<RaidDefinition> getAvailableRaids(Player player) {
        int testamentCount = plugin.getGodManager().getTestamentCount(player);
        boolean hasConvergence = plugin.getGodManager().getConvergenceManager().hasAchievedConvergence(player);
        
        List<RaidDefinition> availableRaids = new ArrayList<>();
        
        for (RaidDefinition raid : raidDefinitions.values()) {
            if (isRaidAvailable(raid, testamentCount, hasConvergence)) {
                availableRaids.add(raid);
            }
        }
        
        return availableRaids;
    }
    
    /**
     * Check if a raid is available for the player's progression level
     */
    private boolean isRaidAvailable(RaidDefinition raid, int testamentCount, boolean hasConvergence) {
        switch (raid.getTier()) {
            case NOVICE:
                return testamentCount >= 0 && testamentCount <= 2;
            case ADEPT:
                return testamentCount >= 3 && testamentCount <= 6;
            case MASTER:
                return testamentCount >= 7 && testamentCount <= 11;
            case CONVERGENCE:
                return hasConvergence;
            default:
                return false;
        }
    }
    
    /**
     * Start a raid for a group of players
     */
    public boolean startRaid(String raidId, List<Player> players, Location startLocation) {
        RaidDefinition definition = raidDefinitions.get(raidId);
        if (definition == null) {
            logger.warning("Unknown raid ID: " + raidId);
            return false;
        }
        
        // Check for Divine Council raid bonuses
        boolean councilBonusActive = checkCouncilRaidBonus();
        
        // Calculate dynamic scaling
        RaidScalingCalculator.RaidScaling scaling = scalingCalculator.calculateScaling(players, definition, currentWeeklyChallenge);
        
        // Validate player count
        if (players.size() < definition.getMinPlayers() || players.size() > definition.getMaxPlayers()) {
            return false;
        }
        
        // Validate player eligibility
        for (Player player : players) {
            if (!isPlayerEligible(player, definition)) {
                return false;
            }
            
            // Check if player is already in a raid
            if (playerRaidMap.containsKey(player.getUniqueId())) {
                return false;
            }
        }
        
        // Generate raid area location
        Location raidLocation = generateRaidLocation(raidId, startLocation);
        
        // Create active raid
        String instanceId = UUID.randomUUID().toString();
        ActiveRaid activeRaid = new ActiveRaid(instanceId, definition, players, startLocation, raidLocation, scaling, currentWeeklyChallenge);
        
        // Teleport players to raid area
        for (Player player : players) {
            player.teleport(raidLocation);
            player.sendMessage("§6§l⚡ TELEPORTED TO RAID AREA! ⚡");
            player.sendMessage("§7You will be returned to your original location when the raid ends.");
        }
        
        // Check if this is an enhanced dragon raid
        if ("enhanced_ender_dragon".equals(raidId)) {
            // Start enhanced dragon combat
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                startEnhancedDragonCombat(players, raidLocation, scaling);
            }, 60L); // Start after 3 seconds
        }
        
        // Apply council bonuses if active
        if (councilBonusActive) {
            activeRaid.setCouncilBonusActive(true);
            for (Player player : players) {
                player.sendMessage("§d§lDivine Council Bonus: §r§dThis raid has enhanced rewards!");
            }
        }
        
        // Register raid and players
        activeRaids.put(instanceId, activeRaid);
        for (Player player : players) {
            playerRaidMap.put(player.getUniqueId(), instanceId);
        }
        
        // Start the raid
        activeRaid.start(plugin);
        
        logger.info("Started raid '" + definition.getDisplayName() + "' with " + players.size() + " players");
        return true;
    }
    
    /**
     * Generate raid location based on raid type
     */
    private Location generateRaidLocation(String raidId, Location playerLocation) {
        // Create raid areas far from spawn in flat areas
        Location raidLoc;
        
        switch (raidId) {
            case "enhanced_ender_dragon":
                // Teleport to The End for dragon raids
                org.bukkit.World endWorld = plugin.getServer().getWorld("world_the_end");
                if (endWorld != null) {
                    raidLoc = new Location(endWorld, 0, 60, 0); // End spawn area
                } else {
                    raidLoc = findFlatRaidArea(playerLocation);
                }
                break;
            case "eternal_crucible":
                // Find flat area far from spawn
                raidLoc = findFlatRaidArea(playerLocation);
                raidLoc.add(0, 50, 0); // High altitude
                break;
            default:
                // Find flat area far from spawn for other raids
                raidLoc = findFlatRaidArea(playerLocation);
                break;
        }
        
        return raidLoc;
    }
    
    /**
     * Find a flat raid area far from spawn
     */
    private Location findFlatRaidArea(Location playerLocation) {
        org.bukkit.World world = playerLocation.getWorld();
        
        // Generate coordinates far from spawn (10000+ blocks away)
        int x = 10000 + (int)(Math.random() * 5000) * (Math.random() < 0.5 ? -1 : 1);
        int z = 10000 + (int)(Math.random() * 5000) * (Math.random() < 0.5 ? -1 : 1);
        
        // Find suitable Y level
        int y = 100;
        for (int testY = 120; testY >= 60; testY--) {
            Location testLoc = new Location(world, x, testY, z);
            if (testLoc.getBlock().getType().isSolid()) {
                y = testY + 1;
                break;
            }
        }
        
        Location raidLoc = new Location(world, x, y, z);
        
        // Flatten a 20x20 area around the raid location
        flattenArea(raidLoc, 20);
        
        return raidLoc;
    }
    
    /**
     * Flatten an area around the given location
     */
    private void flattenArea(Location center, int radius) {
        org.bukkit.World world = center.getWorld();
        int centerY = center.getBlockY();
        
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Location blockLoc = center.clone().add(x, 0, z);
                
                // Clear blocks above ground level
                for (int y = centerY + 1; y <= centerY + 10; y++) {
                    blockLoc.setY(y);
                    if (blockLoc.getBlock().getType() != Material.AIR) {
                        blockLoc.getBlock().setType(Material.AIR);
                    }
                }
                
                // Set ground level
                blockLoc.setY(centerY);
                if (!blockLoc.getBlock().getType().isSolid()) {
                    blockLoc.getBlock().setType(Material.STONE);
                }
                
                // Clear below ground level
                blockLoc.setY(centerY - 1);
                if (!blockLoc.getBlock().getType().isSolid()) {
                    blockLoc.getBlock().setType(Material.STONE);
                }
            }
        }
    }
    
    /**
     * Start test raid for admins
     */
    public boolean startTestRaid(String raidId, List<Player> testPlayers) {
        RaidDefinition definition = raidDefinitions.get(raidId);
        if (definition == null) {
            if (!testPlayers.isEmpty()) {
                testPlayers.get(0).sendMessage("§cUnknown raid ID: " + raidId);
            }
            return false;
        }
        
        if (testPlayers.isEmpty()) {
            return false;
        }
        
        Player admin = testPlayers.get(0);
        
        // Generate test location far from spawn
        Location testLocation = findFlatRaidArea(admin.getLocation());
        
        // Calculate scaling for test
        RaidScalingCalculator.RaidScaling scaling = scalingCalculator.calculateScaling(testPlayers, definition, currentWeeklyChallenge);
        
        // Create active raid
        String instanceId = "test_" + UUID.randomUUID().toString();
        ActiveRaid activeRaid = new ActiveRaid(instanceId, definition, testPlayers, testLocation, testLocation, scaling, currentWeeklyChallenge);
        
        // Teleport all test players to test location
        for (Player player : testPlayers) {
            player.teleport(testLocation);
            player.sendMessage("§a§lTest Raid Started: §r§a" + definition.getDisplayName());
            player.sendMessage("§7Test Location: " + testLocation.getBlockX() + ", " + testLocation.getBlockY() + ", " + testLocation.getBlockZ());
            player.sendMessage("§7Scaling: " + scaling.getFormattedDisplay());
            player.sendMessage("§7Test Players: §f" + testPlayers.size());
        }
        
        // Register raid
        activeRaids.put(instanceId, activeRaid);
        for (Player player : testPlayers) {
            playerRaidMap.put(player.getUniqueId(), instanceId);
        }
        
        // Start the raid
        activeRaid.start(plugin);
        
        logger.info("Started test raid '" + definition.getDisplayName() + "' with " + testPlayers.size() + " test players");
        return true;
    }
    
    /**
     * Start enhanced Ender Dragon combat
     */
    private void startEnhancedDragonCombat(List<Player> players, Location location, RaidScalingCalculator.RaidScaling scaling) {
        // Find or spawn Ender Dragon
        org.bukkit.entity.EnderDragon dragon = null;
        
        // Look for existing dragon
        for (org.bukkit.entity.Entity entity : location.getWorld().getEntities()) {
            if (entity instanceof org.bukkit.entity.EnderDragon) {
                dragon = (org.bukkit.entity.EnderDragon) entity;
                break;
            }
        }
        
        // Spawn dragon if none exists
        if (dragon == null) {
            dragon = location.getWorld().spawn(location.clone().add(0, 50, 0), org.bukkit.entity.EnderDragon.class);
        }
        
        // Start enhanced combat
        if (plugin.getEnderDragonCombatManager() != null) {
            plugin.getEnderDragonCombatManager().startCombat(dragon, players, 
            scaling.getHealthMultiplier(), scaling.getDamageMultiplier());
        }
    }
    
    /**
     * Check if Divine Council raid bonus is active
     */
    private boolean checkCouncilRaidBonus() {
        if (!plugin.getConfig().getBoolean("divine_council.enabled", false)) {
            return false;
        }
        
        if (!plugin.getConfig().getBoolean("divine_council.integration.raids.council_raids_enabled", true)) {
            return false;
        }
        
        // Check if there's an active council raid proposal effect
        // In a real implementation, this would check for active council effects
        return false; // Placeholder
    }
    
    /**
     * Get current weekly challenge
     */
    public WeeklyChallenge getCurrentWeeklyChallenge() {
        return currentWeeklyChallenge;
    }
    
    /**
     * Get leaderboard manager
     */
    public RaidLeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }
    
    /**
     * Check if a player is eligible for a specific raid
     */
    private boolean isPlayerEligible(Player player, RaidDefinition definition) {
        int testamentCount = plugin.getGodManager().getTestamentCount(player);
        boolean hasConvergence = plugin.getGodManager().getConvergenceManager().hasAchievedConvergence(player);
        
        return isRaidAvailable(definition, testamentCount, hasConvergence);
    }
    
    /**
     * End a raid
     */
    public void endRaid(String instanceId, RaidResult result) {
        ActiveRaid raid = activeRaids.get(instanceId);
        if (raid == null) {
            return;
        }
        
        // Record completion for leaderboards if successful
        if (result == RaidResult.SUCCESS) {
            recordRaidCompletion(raid);
            recordGuildRaidCompletion(raid);
        }
        
        // Remove players from raid mapping
        for (Player player : raid.getPlayers()) {
            playerRaidMap.remove(player.getUniqueId());
        }
        
        // End the raid
        raid.end(result);
        
        // Remove from active raids
        activeRaids.remove(instanceId);
        
        logger.info("Ended raid '" + raid.getDefinition().getDisplayName() + "' with result: " + result);
    }
    
    /**
     * Record guild raid completion
     */
    private void recordGuildRaidCompletion(ActiveRaid raid) {
        // Check if all players are from the same guild
        GuildManager.Guild guild = null;
        boolean allSameGuild = true;
        
        for (Player player : raid.getPlayers()) {
            GuildManager.Guild playerGuild = guildManager.getPlayerGuild(player);
            if (guild == null) {
                guild = playerGuild;
            } else if (playerGuild == null || !playerGuild.getId().equals(guild.getId())) {
                allSameGuild = false;
                break;
            }
        }
        
        // Record for guild if all players are from same guild
        if (allSameGuild && guild != null) {
            int score = scalingCalculator.calculateCompletionScore(
                raid.getDefinition(), 
                System.currentTimeMillis() - raid.getStartTime(),
                raid.getPlayers(),
                raid.getScaling(),
                raid.getWeeklyChallenge()
            );
            
            guildManager.recordGuildRaidCompletion(guild, score, 
                System.currentTimeMillis() - raid.getStartTime());
            
            // Notify guild members
            for (Player player : raid.getPlayers()) {
                player.sendMessage("§6§l✦ GUILD RAID COMPLETED! ✦");
                player.sendMessage("§7Guild: §f" + guild.getName());
                player.sendMessage("§7This completion has been recorded for your guild!");
            }
        }
    }
    
    /**
     * Record raid completion for leaderboards
     */
    private void recordRaidCompletion(ActiveRaid raid) {
        List<UUID> playerIds = new ArrayList<>();
        List<String> playerNames = new ArrayList<>();
        
        for (Player player : raid.getPlayers()) {
            playerIds.add(player.getUniqueId());
            playerNames.add(player.getName());
        }
        
        // Calculate score
        int score = scalingCalculator.calculateCompletionScore(
            raid.getDefinition(), 
            System.currentTimeMillis() - raid.getStartTime(),
            raid.getPlayers(),
            raid.getScaling(),
            raid.getWeeklyChallenge()
        );
        
        // Create completion record
        RaidCompletionRecord record = new RaidCompletionRecord(
            raid.getDefinition().getId(),
            raid.getDefinition().getDisplayName(),
            playerIds,
            playerNames,
            raid.getStartTime(),
            System.currentTimeMillis(),
            score,
            raid.getDefinition().getTier(),
            raid.getWeeklyChallenge() != null && raid.getWeeklyChallenge().isActive(),
            raid.getWeeklyChallenge() != null ? raid.getWeeklyChallenge().getId() : null
        );
        
        leaderboardManager.addCompletionRecord(record);
        
        // Announce if it's a new record
        checkForNewRecord(record);
    }
    
    /**
     * Check if this is a new record and announce it
     */
    private void checkForNewRecord(RaidCompletionRecord newRecord) {
        List<RaidCompletionRecord> topRecords = leaderboardManager.getTopCompletions(newRecord.getRaidId(), 1);
        
        if (topRecords.isEmpty() || newRecord.getCompletionTime() < topRecords.get(0).getCompletionTime()) {
            // New record!
            plugin.getServer().broadcastMessage("§6§l🏆 NEW RAID RECORD! 🏆");
            plugin.getServer().broadcastMessage("§7Raid: §f" + newRecord.getRaidDisplayName());
            plugin.getServer().broadcastMessage("§7Time: §f" + newRecord.getFormattedCompletionTime());
            plugin.getServer().broadcastMessage("§7Players: §f" + String.join(", ", newRecord.getPlayerNames()));
            plugin.getServer().broadcastMessage("§7Score: §f" + newRecord.getScore());
            
            if (newRecord.isWeeklyChallengeActive()) {
                plugin.getServer().broadcastMessage("§e§l⭐ Completed during Weekly Challenge! ⭐");
            }
        }
    }
    
    /**
     * Get active raid for a player
     */
    public ActiveRaid getPlayerRaid(Player player) {
        String instanceId = playerRaidMap.get(player.getUniqueId());
        return instanceId != null ? activeRaids.get(instanceId) : null;
    }
    
    /**
     * Handle player quit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String instanceId = playerRaidMap.get(player.getUniqueId());
        
        if (instanceId != null) {
            ActiveRaid raid = activeRaids.get(instanceId);
            if (raid != null) {
                raid.removePlayer(player);
                
                // End raid if no players left
                if (raid.getPlayers().isEmpty()) {
                    endRaid(instanceId, RaidResult.ABANDONED);
                }
            }
            
            playerRaidMap.remove(player.getUniqueId());
        }
    }
    
    /**
     * Get raid statistics
     */
    public Map<String, Object> getRaidStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("active_raids", activeRaids.size());
        stats.put("players_in_raids", playerRaidMap.size());
        stats.put("available_raid_types", raidDefinitions.size());
        
        // Count raids by tier
        Map<RaidTier, Integer> tierCounts = new HashMap<>();
        for (ActiveRaid raid : activeRaids.values()) {
            RaidTier tier = raid.getDefinition().getTier();
            tierCounts.merge(tier, 1, Integer::sum);
        }
        stats.put("raids_by_tier", tierCounts);
        
        // Add leaderboard statistics
        Map<String, Object> leaderboardStats = leaderboardManager.getLeaderboardStatistics();
        stats.put("leaderboard_stats", leaderboardStats);
        
        // Add weekly challenge info
        if (currentWeeklyChallenge != null) {
            Map<String, Object> challengeInfo = new HashMap<>();
            challengeInfo.put("active", currentWeeklyChallenge.isActive());
            challengeInfo.put("name", currentWeeklyChallenge.getDisplayName());
            challengeInfo.put("hours_remaining", currentWeeklyChallenge.getHoursRemaining());
            stats.put("weekly_challenge", challengeInfo);
        }
        
        return stats;
    }
    
    /**
     * Get guild manager
     */
    public GuildManager getGuildManager() {
        return guildManager;
    }
    
    /**
     * Shutdown the raid manager
     */
    public void shutdown() {
        logger.info("Raid Manager shutting down...");
        
        // End all active raids
        for (String instanceId : new ArrayList<>(activeRaids.keySet())) {
            endRaid(instanceId, RaidResult.SERVER_SHUTDOWN);
        }
        
        // Clear all data
        activeRaids.clear();
        playerRaidMap.clear();
        
        // Shutdown leaderboard manager
        leaderboardManager.shutdown();
        
        logger.info("Raid Manager shutdown complete");
    }
    
    /**
     * Raid tiers
     */
    public enum RaidTier {
        NOVICE("Novice", "For players with 0-2 testaments"),
        ADEPT("Adept", "For players with 3-6 testaments"),
        MASTER("Master", "For players with 7-11 testaments"),
        CONVERGENCE("Convergence", "For players who have achieved Divine Convergence");
        
        private final String displayName;
        private final String description;
        
        RaidTier(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Raid results
     */
    public enum RaidResult {
        SUCCESS,
        FAILURE,
        TIMEOUT,
        ABANDONED,
        SERVER_SHUTDOWN
    }
}