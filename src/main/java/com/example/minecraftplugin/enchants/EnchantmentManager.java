package com.example.minecraftplugin.enchants;

import com.example.minecraftplugin.MinecraftPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Arrow;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import com.example.minecraftplugin.enchants.custom.*;
import com.example.minecraftplugin.enchants.custom.ArmorPiercing;
import com.example.minecraftplugin.enchants.custom.AutoSmelt;
import com.example.minecraftplugin.enchants.custom.BlinkDash;
import com.example.minecraftplugin.enchants.custom.Density;
import com.example.minecraftplugin.enchants.custom.EfficiencyX;
import com.example.minecraftplugin.enchants.custom.GuardianAura;
import com.example.minecraftplugin.enchants.custom.Magnetism;
import com.example.minecraftplugin.enchants.custom.Soulbound;
import com.example.minecraftplugin.enchants.custom.Terraforming;
import com.example.minecraftplugin.enchants.custom.TreeFeller;
import com.example.minecraftplugin.enchants.custom.UnbreakingX;
import com.example.minecraftplugin.enchants.custom.Vampirism;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.bukkit.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * Manages custom enchantments and their effects
 * Note: Modern Paper API makes custom enchantments complex
 * This implementation focuses on effect handling rather than registration
 */
public class EnchantmentManager implements Listener {
    
    private final MinecraftPlugin plugin;
    private final Logger logger;
    private final Map<String, CustomEnchantment> customEnchantments;
    
    // Cooldown tracking for abilities
    private final Map<UUID, Map<String, Long>> abilityCooldowns;
    
    public EnchantmentManager(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.customEnchantments = new HashMap<>();
        this.abilityCooldowns = new ConcurrentHashMap<>();
        
        // Register as event listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        // Initialize custom enchantments (without registering them as actual enchantments)
        initializeCustomEnchantments();
        
        // Start periodic tasks for certain enchantments
        startPeriodicTasks();
        
        logger.info("Enchantment Manager initialized with " + customEnchantments.size() + " custom enchantment effects");
    }
    
    /**
     * Initialize custom enchantments for effect handling
     */
    private void initializeCustomEnchantments() {
        try {
            // Vanilla-like enchantments with +3 max level
            ProtectionV protectionV = new ProtectionV(new NamespacedKey(plugin, "protection_v"));
            DivineSharpness divineSharpness = new DivineSharpness(new NamespacedKey(plugin, "divine_sharpness"));
            ProtectionX protectionX = new ProtectionX(new NamespacedKey(plugin, "protection_x"));
            SharpnessX sharpnessX = new SharpnessX(new NamespacedKey(plugin, "sharpness_x"));
            
            // Non-vanilla custom enchantments
            Lifesteal lifesteal = new Lifesteal(new NamespacedKey(plugin, "lifesteal"));
            BlindnessEnchant blindness = new BlindnessEnchant(new NamespacedKey(plugin, "blindness"));
            VeinMiner veinMiner = new VeinMiner(new NamespacedKey(plugin, "vein_miner"));
            ExplosiveArrows explosiveArrows = new ExplosiveArrows(new NamespacedKey(plugin, "explosive_arrows"));
            RiptideX riptideX = new RiptideX(new NamespacedKey(plugin, "riptide_x"));
            
            // Additional custom enchantments
            ArmorPiercing armorPiercing = new ArmorPiercing(new NamespacedKey(plugin, "armor_piercing"));
            AutoSmelt autoSmelt = new AutoSmelt(new NamespacedKey(plugin, "auto_smelt"));
            BlinkDash blinkDash = new BlinkDash(new NamespacedKey(plugin, "blink_dash"));
            Density density = new Density(new NamespacedKey(plugin, "density"));
            EfficiencyX efficiencyX = new EfficiencyX(new NamespacedKey(plugin, "efficiency_x"));
            GuardianAura guardianAura = new GuardianAura(new NamespacedKey(plugin, "guardian_aura"));
            Magnetism magnetism = new Magnetism(new NamespacedKey(plugin, "magnetism"));
            Soulbound soulbound = new Soulbound(new NamespacedKey(plugin, "soulbound"));
            Terraforming terraforming = new Terraforming(new NamespacedKey(plugin, "terraforming"));
            TreeFeller treeFeller = new TreeFeller(new NamespacedKey(plugin, "tree_feller"));
            UnbreakingX unbreakingX = new UnbreakingX(new NamespacedKey(plugin, "unbreaking_x"));
            Vampirism vampirism = new Vampirism(new NamespacedKey(plugin, "vampirism"));
            
            customEnchantments.put("PROTECTION_V", protectionV);
            customEnchantments.put("DIVINE_SHARPNESS", divineSharpness);
            customEnchantments.put("PROTECTION_X", protectionX);
            customEnchantments.put("SHARPNESS_X", sharpnessX);
            customEnchantments.put("LIFESTEAL", lifesteal);
            customEnchantments.put("BLINDNESS", blindness);
            customEnchantments.put("VEIN_MINER", veinMiner);
            customEnchantments.put("EXPLOSIVE_ARROWS", explosiveArrows);
            customEnchantments.put("RIPTIDE_X", riptideX);
            customEnchantments.put("ARMOR_PIERCING", armorPiercing);
            customEnchantments.put("AUTO_SMELT", autoSmelt);
            customEnchantments.put("BLINK_DASH", blinkDash);
            customEnchantments.put("DENSITY", density);
            customEnchantments.put("EFFICIENCY_X", efficiencyX);
            customEnchantments.put("GUARDIAN_AURA", guardianAura);
            customEnchantments.put("MAGNETISM", magnetism);
            customEnchantments.put("SOULBOUND", soulbound);
            customEnchantments.put("TERRAFORMING", terraforming);
            customEnchantments.put("TREE_FELLER", treeFeller);
            customEnchantments.put("UNBREAKING_X", unbreakingX);
            customEnchantments.put("VAMPIRISM", vampirism);
            
            logger.info("Initialized " + customEnchantments.size() + " custom enchantment effects");
            
        } catch (Exception e) {
            logger.severe("Failed to initialize custom enchantments: " + e.getMessage());
        }
    }
    
    /**
     * Start periodic tasks for enchantments that need continuous effects
     */
    private void startPeriodicTasks() {
        // Magnetism task - runs every second
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    handleMagnetismEffect(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        // Guardian Aura task - runs every 2 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    handleGuardianAuraEffect(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 40L);
    }
    
    /**
     * Handle magnetism effect for pulling nearby items
     */
    private void handleMagnetismEffect(Player player) {
        // Check if player has magnetism on any armor piece
        int magnetismLevel = 0;
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (hasCustomEnchantment(armor, "MAGNETISM")) {
                magnetismLevel = Math.max(magnetismLevel, getCustomEnchantmentLevel(armor, "MAGNETISM"));
            }
        }
        
        if (magnetismLevel == 0) return;
        
        // Pull nearby items
        double radius = 3.0 + (magnetismLevel * 2.0); // 5-9 block radius
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Item) {
                Item item = (Item) entity;
                
                // Calculate pull force
                Vector direction = player.getLocation().toVector().subtract(item.getLocation().toVector());
                double distance = direction.length();
                
                if (distance > 0.5) { // Don't pull if too close
                    direction.normalize();
                    double pullStrength = 0.1 + (magnetismLevel * 0.05);
                    direction.multiply(pullStrength);
                    
                    item.setVelocity(direction);
                }
            }
        }
        
        // Visual effect
        if (magnetismLevel > 0 && Math.random() < 0.3) {
            player.getWorld().spawnParticle(Particle.ENCHANT, 
                player.getLocation().add(0, 1, 0), 5, 1, 1, 1, 0.1);
        }
    }
    
    /**
     * Handle guardian aura effect for buffing nearby allies
     */
    private void handleGuardianAuraEffect(Player player) {
        // Check if player has guardian aura on chestplate
        ItemStack chestplate = player.getInventory().getChestplate();
        if (!hasCustomEnchantment(chestplate, "GUARDIAN_AURA")) {
            return;
        }
        
        int auraLevel = getCustomEnchantmentLevel(chestplate, "GUARDIAN_AURA");
        double radius = 5.0 + (auraLevel * 2.0); // 7-11 block radius
        
        // Find nearby players (allies)
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player && entity != player) {
                Player ally = (Player) entity;
                
                // Apply beneficial effects based on level
                switch (auraLevel) {
                    case 3:
                        ally.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 60, 0, false, false));
                        // Fall through
                    case 2:
                        ally.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0, false, false));
                        // Fall through
                    case 1:
                        ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0, false, false));
                        break;
                }
                
                // Visual effect on ally
                if (Math.random() < 0.2) {
                    ally.getWorld().spawnParticle(Particle.HEART, 
                        ally.getLocation().add(0, 2, 0), 2, 0.5, 0.5, 0.5, 0.1);
                }
            }
        }
        
        // Visual effect on guardian
        if (Math.random() < 0.3) {
            player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, 
                player.getLocation().add(0, 1, 0), 3, 1, 1, 1, 0.1);
        }
    }
    
    /**
     * Handle damage events for protection enchantments
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        double damage = event.getDamage();
        double totalReduction = 0.0;
        
        // Check all armor pieces for protection enchantments
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack armorPiece : armor) {
            if (armorPiece == null) continue;
            
            // Check for vanilla Protection
            if (armorPiece.containsEnchantment(Enchantment.PROTECTION)) {
                int level = armorPiece.getEnchantmentLevel(Enchantment.PROTECTION);
                totalReduction += level * 0.04; // 4% per level
            }
            
            // Check for custom Protection X (enhanced vanilla protection)
            if (hasCustomEnchantment(armorPiece, "PROTECTION_X")) {
                int level = getCustomEnchantmentLevel(armorPiece, "PROTECTION_X");
                totalReduction += level * 0.04; // 4% per level
                
                // Apply custom Protection X effect
                CustomEnchantment protectionX = customEnchantments.get("PROTECTION_X");
                if (protectionX != null) {
                    protectionX.applyEffect(player, level, damage);
                }
            }
        }
        
        // Cap total protection at 80%
        totalReduction = Math.min(0.80, totalReduction);
        
        if (totalReduction > 0) {
            double newDamage = damage * (1.0 - totalReduction);
            event.setDamage(newDamage);
            
            // Visual feedback for high protection
            if (totalReduction > 0.5) {
                player.getWorld().spawnParticle(org.bukkit.Particle.ENCHANTED_HIT, 
                    player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
            }
        }
    }
    
    /**
     * Handle damage by entity events for weapon enchantments
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        
        Player attacker = (Player) event.getDamager();
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        
        if (weapon == null) return;
        
        double damage = event.getDamage();
        double bonusDamage = 0.0;
        
        // Check for vanilla Sharpness
        if (weapon.containsEnchantment(Enchantment.SHARPNESS)) {
            int level = weapon.getEnchantmentLevel(Enchantment.SHARPNESS);
            bonusDamage += level * 0.5; // 0.5 damage per level
        }
        
        // Check for custom Sharpness X
        if (hasCustomEnchantment(weapon, "SHARPNESS_X")) {
            int level = getCustomEnchantmentLevel(weapon, "SHARPNESS_X");
            bonusDamage += level * 0.5; // 0.5 damage per level
            
            // Apply custom Sharpness X effect
            CustomEnchantment sharpnessX = customEnchantments.get("SHARPNESS_X");
            if (sharpnessX != null) {
                sharpnessX.applyEffect(attacker, level, damage);
            }
            
            // Special effects at higher levels
            if (level >= 5 && Math.random() < 0.1 * (level - 4)) {
                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) event.getEntity();
                    target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0));
                    
                    attacker.sendMessage("§6§lEnhanced Strike! §r§6Your weapon glows with enhanced power!");
                    attacker.getWorld().spawnParticle(org.bukkit.Particle.CRIT, 
                        target.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.2);
                }
            }
        }
        
        // Check for Lifesteal
        if (hasCustomEnchantment(weapon, "LIFESTEAL")) {
            int level = getCustomEnchantmentLevel(weapon, "LIFESTEAL");
            double healAmount = damage * (level * 0.02); // 2% per level
            
            double currentHealth = attacker.getHealth();
            double maxHealth = attacker.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
            double newHealth = Math.min(currentHealth + healAmount, maxHealth);
            attacker.setHealth(newHealth);
            
            if (healAmount > 0.5) {
                attacker.sendMessage("§4§lLifesteal: §r§4+" + String.format("%.1f", healAmount) + " health");
                attacker.getWorld().spawnParticle(org.bukkit.Particle.HEART, 
                    attacker.getLocation().add(0, 1, 0), 5, 0.5, 0.5, 0.5, 0.1);
            }
        }
        
        // Check for Vampirism (stronger than Lifesteal)
        if (hasCustomEnchantment(weapon, "VAMPIRISM")) {
            int level = getCustomEnchantmentLevel(weapon, "VAMPIRISM");
            double healAmount = damage * (level * 0.05); // 5% per level
            
            double currentHealth = attacker.getHealth();
            double maxHealth = attacker.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
            double newHealth = Math.min(currentHealth + healAmount, maxHealth);
            attacker.setHealth(newHealth);
            
            if (healAmount > 0.5) {
                attacker.sendMessage("§4§lVampirism: §r§4+" + String.format("%.1f", healAmount) + " health");
                attacker.getWorld().spawnParticle(org.bukkit.Particle.HEART, 
                    attacker.getLocation().add(0, 1, 0), 5, 0.5, 0.5, 0.5, 0.1);
            }
        }
        
        // Check for Blindness
        if (hasCustomEnchantment(weapon, "BLINDNESS")) {
            int level = getCustomEnchantmentLevel(weapon, "BLINDNESS");
            double chance = level * 0.10; // 10% per level
            
            if (Math.random() < chance && event.getEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getEntity();
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                
                attacker.sendMessage("§8§lBlindness Strike! §r§8Your enemy is blinded!");
                target.getWorld().spawnParticle(org.bukkit.Particle.SQUID_INK, 
                    target.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
            }
        }
        
        // Check for Armor Piercing
        if (hasCustomEnchantment(weapon, "ARMOR_PIERCING")) {
            int level = getCustomEnchantmentLevel(weapon, "ARMOR_PIERCING");
            double armorPiercingPercent = Math.min(0.70, level * 0.10); // 10% per level, max 70%
            
            // Apply armor piercing by increasing damage
            double piercingBonus = damage * armorPiercingPercent;
            bonusDamage += piercingBonus;
            
            if (level >= 5) {
                attacker.sendMessage("§6§lArmor Piercing: §r§6Ignored " + 
                    String.format("%.0f%%", armorPiercingPercent * 100) + " armor!");
            }
        }
        
        // Check for Density (extreme knockback)
        if (hasCustomEnchantment(weapon, "DENSITY")) {
            int level = getCustomEnchantmentLevel(weapon, "DENSITY");
            
            if (event.getEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getEntity();
                
                // Apply massive knockback
                Vector direction = target.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize();
                direction.setY(0.5 + (level * 0.1));
                direction.multiply(1.0 + (level * 0.5)); // Up to 6x knockback at level 10
                target.setVelocity(direction);
                
                // Apply stun effect at higher levels
                if (level >= 7) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 3));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 1));
                }
                
                attacker.sendMessage("§8§lDensity Strike! §r§8Massive knockback!");
                target.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION, 
                    target.getLocation(), 5, 0.5, 0.5, 0.5, 0.1);
            }
        }
        
        if (bonusDamage > 0) {
            event.setDamage(damage + bonusDamage);
        }
    }
    
    /**
     * Handle player death events for soulbound enchantment
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        List<ItemStack> soulboundItems = new ArrayList<>();
        
        // Check for soulbound items in drops
        Iterator<ItemStack> dropIterator = event.getDrops().iterator();
        while (dropIterator.hasNext()) {
            ItemStack item = dropIterator.next();
            if (hasCustomEnchantment(item, "SOULBOUND")) {
                soulboundItems.add(item.clone());
                dropIterator.remove(); // Remove from drops
            }
        }
        
        // Return soulbound items to player on respawn
        if (!soulboundItems.isEmpty()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                for (ItemStack item : soulboundItems) {
                    player.getInventory().addItem(item);
                }
                player.sendMessage("§b§lSoulbound: §r§bYour soulbound items have returned to you!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
            }, 20L); // 1 second delay to ensure player has respawned
        }
    }
    
    /**
     * Handle block break events for mining enchantments
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        
        if (tool == null) return;
        
        // Check for enhanced Efficiency X
        if (hasCustomEnchantment(tool, "EFFICIENCY_X")) {
            int level = getCustomEnchantmentLevel(tool, "EFFICIENCY_X");
            if (level > 5) {
                // Apply enhanced mining speed
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20, level - 3, false, false));
            }
        }
        
        // Check for Vein Miner
        if (hasCustomEnchantment(tool, "VEIN_MINER")) {
            executeVeinMiner(player, event.getBlock(), tool);
        }
        
        // Check for Auto Smelt
        if (hasCustomEnchantment(tool, "AUTO_SMELT")) {
            executeAutoSmelt(player, event.getBlock(), tool);
        }
        
        // Check for Tree Feller
        if (hasCustomEnchantment(tool, "TREE_FELLER")) {
            executeTreeFeller(player, event.getBlock(), tool);
        }
        
        // Check for Terraforming
        if (hasCustomEnchantment(tool, "TERRAFORMING")) {
            executeTerraforming(player, event.getBlock(), tool);
        }
    }
    
    /**
     * Handle projectile hit events for bow enchantments
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        if (!(event.getEntity() instanceof Arrow)) return;
        
        Player shooter = (Player) event.getEntity().getShooter();
        ItemStack bow = shooter.getInventory().getItemInMainHand();
        
        if (bow == null) return;
        
        // Check for Explosive Arrows
        if (hasCustomEnchantment(bow, "EXPLOSIVE_ARROWS")) {
            int level = getCustomEnchantmentLevel(bow, "EXPLOSIVE_ARROWS");
            float explosionPower = level * 1.0f;
            
            Location hitLocation = event.getEntity().getLocation();
            hitLocation.getWorld().createExplosion(hitLocation, explosionPower, false, false);
            
            shooter.sendMessage("§c§lExplosive Arrow! §r§cBoom!");
        }
    }
    
    /**
     * Execute vein miner effect
     */
    private void executeVeinMiner(Player player, Block startBlock, ItemStack tool) {
        Material targetMaterial = startBlock.getType();
        Set<Block> blocksToBreak = new HashSet<>();
        Set<Location> visited = new HashSet<>();
        
        // Find all connected blocks of the same type
        findConnectedBlocks(startBlock, targetMaterial, blocksToBreak, visited, 64); // Max 64 blocks
        
        // Break all found blocks
        for (Block block : blocksToBreak) {
            if (block.getType() == targetMaterial) {
                block.breakNaturally(tool);
            }
        }
        
        if (blocksToBreak.size() > 1) {
            player.sendMessage("§6§lVein Miner: §r§6Broke " + blocksToBreak.size() + " connected blocks!");
        }
    }
    
    /**
     * Execute auto smelt effect
     */
    private void executeAutoSmelt(Player player, Block block, ItemStack tool) {
        Material blockType = block.getType();
        Material smeltedResult = getSmeltedResult(blockType);
        
        if (smeltedResult != null) {
            // Cancel normal drop and give smelted result
            block.setType(Material.AIR);
            ItemStack smeltedItem = new ItemStack(smeltedResult, 1);
            player.getWorld().dropItemNaturally(block.getLocation(), smeltedItem);
            
            player.sendMessage("§6§lAuto Smelt: §r§6Automatically smelted " + blockType.name());
        }
    }
    
    /**
     * Execute tree feller effect
     */
    private void executeTreeFeller(Player player, Block startBlock, ItemStack tool) {
        if (!isWoodBlock(startBlock.getType())) {
            return;
        }
        
        Set<Block> treeBlocks = new HashSet<>();
        Set<Location> visited = new HashSet<>();
        
        // Find all connected wood and leaf blocks
        findTreeBlocks(startBlock, treeBlocks, visited, 100); // Max 100 blocks
        
        // Break all found blocks
        for (Block block : treeBlocks) {
            if (isWoodBlock(block.getType()) || isLeafBlock(block.getType())) {
                block.breakNaturally(tool);
            }
        }
        
        if (treeBlocks.size() > 1) {
            player.sendMessage("§2§lTree Feller: §r§2Felled " + treeBlocks.size() + " blocks!");
        }
    }
    
    /**
     * Execute terraforming effect
     */
    private void executeTerraforming(Player player, Block block, ItemStack tool) {
        // Simple terraforming - flatten area around broken block
        Location center = block.getLocation();
        int radius = 2;
        
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Location terraformLoc = center.clone().add(x, 0, z);
                Block terraformBlock = terraformLoc.getBlock();
                
                if (terraformBlock.getType().isSolid() && terraformBlock.getType() != Material.BEDROCK) {
                    terraformBlock.setType(Material.AIR);
                }
            }
        }
        
        player.sendMessage("§6§lTerraforming: §r§6Shaped the terrain!");
    }
    
    /**
     * Get smelted result for auto smelt
     */
    private Material getSmeltedResult(Material input) {
        switch (input) {
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
                return Material.IRON_INGOT;
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
            case NETHER_GOLD_ORE:
                return Material.GOLD_INGOT;
            case COPPER_ORE:
            case DEEPSLATE_COPPER_ORE:
                return Material.COPPER_INGOT;
            case COBBLESTONE:
                return Material.STONE;
            case SAND:
                return Material.GLASS;
            case CLAY:
                return Material.TERRACOTTA;
            default:
                return null;
        }
    }
    
    /**
     * Check if material is a wood block
     */
    private boolean isWoodBlock(Material material) {
        return material.name().contains("LOG") || material.name().contains("WOOD");
    }
    
    /**
     * Check if material is a leaf block
     */
    private boolean isLeafBlock(Material material) {
        return material.name().contains("LEAVES");
    }
    
    /**
     * Find all tree blocks recursively
     */
    private void findTreeBlocks(Block block, Set<Block> found, Set<Location> visited, int maxBlocks) {
        if (found.size() >= maxBlocks) return;
        if (visited.contains(block.getLocation())) return;
        if (!isWoodBlock(block.getType()) && !isLeafBlock(block.getType())) return;
        
        visited.add(block.getLocation());
        found.add(block);
        
        // Check all 26 adjacent blocks (3x3x3 cube)
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block adjacent = block.getRelative(x, y, z);
                    findTreeBlocks(adjacent, found, visited, maxBlocks);
                }
            }
        }
    }
    
    /**
     * Recursively find connected blocks of the same type
     */
    private void findConnectedBlocks(Block block, Material targetMaterial, Set<Block> found, Set<Location> visited, int maxBlocks) {
        if (found.size() >= maxBlocks) return;
        if (visited.contains(block.getLocation())) return;
        if (block.getType() != targetMaterial) return;
        
        visited.add(block.getLocation());
        found.add(block);
        
        // Check all 6 adjacent blocks
        Block[] adjacent = {
            block.getRelative(1, 0, 0),
            block.getRelative(-1, 0, 0),
            block.getRelative(0, 1, 0),
            block.getRelative(0, -1, 0),
            block.getRelative(0, 0, 1),
            block.getRelative(0, 0, -1)
        };
        
        for (Block adj : adjacent) {
            findConnectedBlocks(adj, targetMaterial, found, visited, maxBlocks);
        }
    }
    
    /**
     * Handle riptide events for enhanced riptide
     */
    @EventHandler
    public void onPlayerRiptide(PlayerRiptideEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null) return;
        
        // Check for custom RiptideX
        if (hasCustomEnchantment(item, "RIPTIDE_X")) {
            int level = getCustomEnchantmentLevel(item, "RIPTIDE_X");
            
            if (level > 3) {
                // Enhanced riptide effects
                Vector velocity = player.getVelocity();
                double multiplier = 1.0 + (level - 3) * 0.5; // 0.5x bonus per level above 3
                
                velocity.multiply(multiplier);
                player.setVelocity(velocity);
                
                // Enhanced visual effects
                player.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, 
                    player.getLocation(), 20, 1, 1, 1, 0.3);
                player.getWorld().spawnParticle(Particle.DRIPPING_WATER, 
                    player.getLocation(), 30, 1.5, 1.5, 1.5, 0.2);
                
                // Enhanced sound
                player.playSound(player.getLocation(), Sound.ENTITY_DOLPHIN_SPLASH, 1.0f, 0.8f);
                
                player.sendMessage("§b§lEnhanced Riptide! §r§bLevel " + level + " propulsion!");
                
                // Apply custom RiptideX effect
                CustomEnchantment riptideX = customEnchantments.get("RIPTIDE_X");
                if (riptideX != null) {
                    riptideX.applyEffect(player, level, velocity.length());
                }
            }
        }
    }
    
    /**
     * Handle divine riptide (works without water/rain at max level)
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR && 
            event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // Handle Blink Dash
        if (item != null && hasCustomEnchantment(item, "BLINK_DASH")) {
            if (handleBlinkDash(player, item)) {
                event.setCancelled(true);
                return;
            }
        }
        
        if (item == null || item.getType() != Material.TRIDENT) return;
        
        // Check for divine level RiptideX (level 6)
        if (hasCustomEnchantment(item, "RIPTIDE_X")) {
            int level = getCustomEnchantmentLevel(item, "RIPTIDE_X");
            
            if (level >= 6) {
                // Divine riptide works anywhere
                boolean inWater = player.isInWater();
                boolean inRain = player.getWorld().hasStorm() && 
                    player.getLocation().getBlock().getTemperature() > 0.15;
                
                if (!inWater && !inRain) {
                    // Check cooldown to prevent spam
                    if (!player.hasCooldown(Material.TRIDENT)) {
                        executeDivineRiptide(player, item, level);
                        player.setCooldown(Material.TRIDENT, 40); // 2 second cooldown
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    /**
     * Handle blink dash ability
     */
    private boolean handleBlinkDash(Player player, ItemStack item) {
        // Check cooldown
        if (isOnCooldown(player, "blink_dash")) {
            long remaining = getRemainingCooldown(player, "blink_dash");
            player.sendMessage("§5§lBlink Dash: §r§cCooldown " + remaining + "s remaining");
            return false;
        }
        
        // Calculate teleport destination
        Location playerLoc = player.getLocation();
        Vector direction = playerLoc.getDirection().normalize();
        double distance = 5.0; // 5 block teleport
        
        Location targetLoc = playerLoc.add(direction.multiply(distance));
        
        // Find safe landing spot
        for (int y = 0; y >= -3; y--) {
            Location testLoc = targetLoc.clone().add(0, y, 0);
            if (testLoc.getBlock().getType().isSolid() && 
                testLoc.clone().add(0, 1, 0).getBlock().getType().isAir() &&
                testLoc.clone().add(0, 2, 0).getBlock().getType().isAir()) {
                targetLoc = testLoc.add(0, 1, 0);
                break;
            }
        }
        
        // Teleport player
        Location originalLoc = player.getLocation();
        player.teleport(targetLoc);
        
        // Visual effects
        originalLoc.getWorld().spawnParticle(Particle.PORTAL, originalLoc, 20, 0.5, 0.5, 0.5, 0.3);
        targetLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, targetLoc, 20, 0.5, 0.5, 0.5, 0.3);
        
        // Sound effects
        player.playSound(originalLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
        player.playSound(targetLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
        
        player.sendMessage("§5§lBlink Dash! §r§5Short-range teleportation!");
        
        // Set cooldown
        setCooldown(player, "blink_dash", 8); // 8 second cooldown
        
        return true;
    }
    
    /**
     * Check if ability is on cooldown
     */
    private boolean isOnCooldown(Player player, String ability) {
        Map<String, Long> playerCooldowns = abilityCooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return false;
        
        Long lastUsed = playerCooldowns.get(ability);
        if (lastUsed == null) return false;
        
        return System.currentTimeMillis() - lastUsed < getCooldownDuration(ability) * 1000L;
    }
    
    /**
     * Get remaining cooldown time
     */
    private long getRemainingCooldown(Player player, String ability) {
        Map<String, Long> playerCooldowns = abilityCooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return 0;
        
        Long lastUsed = playerCooldowns.get(ability);
        if (lastUsed == null) return 0;
        
        long elapsed = System.currentTimeMillis() - lastUsed;
        long cooldownMs = getCooldownDuration(ability) * 1000L;
        
        return Math.max(0, (cooldownMs - elapsed) / 1000L);
    }
    
    /**
     * Set ability cooldown
     */
    private void setCooldown(Player player, String ability, int seconds) {
        abilityCooldowns.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                       .put(ability, System.currentTimeMillis());
    }
    
    /**
     * Get cooldown duration for ability
     */
    private int getCooldownDuration(String ability) {
        switch (ability) {
            case "blink_dash": return 8;
            default: return 10;
        }
    }
    
    /**
     * Execute divine riptide (works without water/rain)
     */
    private void executeDivineRiptide(Player player, ItemStack trident, int level) {
        // Create water effect around player temporarily
        Location playerLoc = player.getLocation();
        
        // Apply riptide-like propulsion
        Vector direction = player.getLocation().getDirection();
        direction.normalize();
        direction.multiply(2.5 * (level - 2)); // Enhanced speed for divine level
        
        player.setVelocity(direction);
        
        // Divine visual effects
        player.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, playerLoc, 40, 2, 2, 2, 0.5);
        player.getWorld().spawnParticle(Particle.DRIPPING_WATER, playerLoc, 50, 2, 2, 2, 0.3);
        player.getWorld().spawnParticle(Particle.ENCHANT, playerLoc, 30, 1.5, 1.5, 1.5, 0.2);
        
        // Divine sound effects
        player.playSound(playerLoc, Sound.ENTITY_DOLPHIN_SPLASH, 1.0f, 0.6f);
        player.playSound(playerLoc, Sound.BLOCK_WATER_AMBIENT, 1.0f, 1.2f);
        
        player.sendMessage("§b§l🌊 DIVINE RIPTIDE! 🌊");
        player.sendMessage("§7The power of the abyss propels you through any realm!");
        
        logger.info(player.getName() + " used divine riptide (level " + level + ")");
    }
    
    /**
     * Check if item has custom enchantment (simulated through lore or NBT)
     */
    private boolean hasCustomEnchantment(ItemStack item, String enchantmentName) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return false;
        }
        
        // Check lore for custom enchantment indicators
        for (String line : item.getItemMeta().getLore()) {
            String cleanLine = ChatColor.stripColor(line).toLowerCase();
            String cleanEnchantName = enchantmentName.toLowerCase().replace("_", " ");
            
            if (cleanLine.contains(cleanEnchantName) || 
                line.contains("Divine") || 
                line.contains("Protection V") ||
                line.contains("Sharpness X") ||
                line.contains("Efficiency X") ||
                line.contains("Unbreaking X") ||
                line.contains("Riptide VI") ||
                line.contains("Lifesteal") ||
                line.contains("Vampirism") ||
                line.contains("Vein Miner") ||
                line.contains("Explosive Arrows") ||
                line.contains("Armor Piercing") ||
                line.contains("Density") ||
                line.contains("Auto Smelt") ||
                line.contains("Tree Feller") ||
                line.contains("Magnetism") ||
                line.contains("Soulbound") ||
                line.contains("Terraforming") ||
                line.contains("Guardian Aura") ||
                line.contains("Blink Dash") ||
                line.contains("Blindness")) {
                return true;
            }
        }
        
        // For divine items, assume they have enhanced enchantments
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName();
            if (displayName.contains("Divine") || displayName.contains("Legendary") || 
                displayName.contains("Mythic") || displayName.contains("Transcendent")) {
                return true;
            }
            
            // Special check for Trident of the Abyss and RiptideX
            if (enchantmentName.equals("RIPTIDE_X") && displayName.contains("Trident of the Abyss")) {
                return true;
            }
            
            // Special checks for Mace of Annihilation
            if (displayName.contains("Mace of Annihilation")) {
                if (enchantmentName.equals("DENSITY") || 
                    enchantmentName.equals("ARMOR_PIERCING") || 
                    enchantmentName.equals("LIFESTEAL") || 
                    enchantmentName.equals("BLINDNESS") || 
                    enchantmentName.equals("VAMPIRISM")) {
                    return true;
                }
            }
            
            // Check for enhanced vanilla enchantments on divine items
            if (displayName.contains("Divine") || displayName.contains("Enhanced") || 
                displayName.contains("Legendary") || displayName.contains("Mythic") || 
                displayName.contains("Transcendent")) {
                
                // Enhanced vanilla enchantments
                if (enchantmentName.equals("PROTECTION_X") || 
                    enchantmentName.equals("SHARPNESS_X") || 
                    enchantmentName.equals("EFFICIENCY_X") || 
                    enchantmentName.equals("UNBREAKING_X")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Get custom enchantment level (simulated)
     */
    private int getCustomEnchantmentLevel(ItemStack item, String enchantmentName) {
        if (!hasCustomEnchantment(item, enchantmentName)) {
            return 0;
        }
        
        // For divine items, return enhanced levels based on rarity
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName();
            if (displayName.contains("Transcendent")) {
                return 10;
            } else if (displayName.contains("Mythic")) {
                return 8;
            } else if (displayName.contains("Legendary")) {
                return 6;
            } else if (displayName.contains("Enhanced")) {
                return 5;
            } else if (displayName.contains("Divine")) {
                return 4;
            }
            
            // Special handling for Trident of the Abyss RiptideX
            if (enchantmentName.equals("RIPTIDE_X") && displayName.contains("Trident of the Abyss")) {
                return 6; // Divine level riptide
            }
            
            // Special handling for Mace of Annihilation
            if (displayName.contains("Mace of Annihilation")) {
                if (enchantmentName.equals("DENSITY")) {
                    return 10; // Maximum density
                } else if (enchantmentName.equals("ARMOR_PIERCING")) {
                    return 7; // Maximum armor piercing
                } else if (enchantmentName.equals("VAMPIRISM")) {
                    return 5; // Maximum vampirism
                } else if (enchantmentName.equals("LIFESTEAL")) {
                    return 3; // Moderate lifesteal
                } else if (enchantmentName.equals("BLINDNESS")) {
                    return 3; // Moderate blindness
                }
            }
        }
        
        // Check lore for custom enchantment levels
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            for (String line : item.getItemMeta().getLore()) {
                if (line.contains(enchantmentName.replace("_", " "))) {
                    // Try to extract level from lore line
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        try {
                            return Integer.parseInt(part);
                        } catch (NumberFormatException ignored) {}
                    }
                }
                
                // Check for Roman numerals in enhanced vanilla enchantments
                if (enchantmentName.equals("PROTECTION_X") && line.contains("Protection")) {
                    if (line.contains("X")) return 10;
                    if (line.contains("IX")) return 9;
                    if (line.contains("VIII")) return 8;
                    if (line.contains("VII")) return 7;
                    if (line.contains("VI")) return 6;
                    if (line.contains("V")) return 5;
                }
                
                if (enchantmentName.equals("SHARPNESS_X") && line.contains("Sharpness")) {
                    if (line.contains("X")) return 10;
                    if (line.contains("IX")) return 9;
                    if (line.contains("VIII")) return 8;
                    if (line.contains("VII")) return 7;
                    if (line.contains("VI")) return 6;
                    if (line.contains("V")) return 5;
                }
                
                if (enchantmentName.equals("EFFICIENCY_X") && line.contains("Efficiency")) {
                    if (line.contains("X")) return 10;
                    if (line.contains("IX")) return 9;
                    if (line.contains("VIII")) return 8;
                    if (line.contains("VII")) return 7;
                    if (line.contains("VI")) return 6;
                    if (line.contains("V")) return 5;
                }
                
                if (enchantmentName.equals("UNBREAKING_X") && line.contains("Unbreaking")) {
                    if (line.contains("X")) return 10;
                    if (line.contains("IX")) return 9;
                    if (line.contains("VIII")) return 8;
                    if (line.contains("VII")) return 7;
                    if (line.contains("VI")) return 6;
                    if (line.contains("V")) return 5;
                }
            }
        }
        
        return 3; // Default enhanced level
    }
    
    /**
     * Get custom enchantment by name
     */
    public CustomEnchantment getCustomEnchantment(String name) {
        return customEnchantments.get(name);
    }
    
    /**
     * Get all custom enchantments
     */
    public Map<String, CustomEnchantment> getCustomEnchantments() {
        return new HashMap<>(customEnchantments);
    }
    
    /**
     * Apply custom enchantment effects to an item (for divine forge system)
     */
    public void applyCustomEnchantmentEffects(ItemStack item, String enchantmentName, int level) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        
        // Add visual indicators through lore
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        java.util.List<String> lore = meta.hasLore() ? meta.getLore() : new java.util.ArrayList<>();
        
        // Add custom enchantment to lore
        String displayName = enchantmentName.replace("_", " ");
        if (enchantmentName.equals("RIPTIDE_X")) {
            displayName = "Enhanced Riptide";
        }
        lore.add("§5§l" + displayName + " " + level);
        meta.setLore(lore);
        
        // Add glow effect
        meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        
        item.setItemMeta(meta);
        
        logger.info("Applied custom enchantment " + enchantmentName + " level " + level + " to item");
    }
}