package com.example.minecraftplugin.shop;

import com.example.minecraftplugin.MinecraftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the raid shop GUI
 */
public class RaidShopGUI implements Listener {
    
    private final MinecraftPlugin plugin;
    private final List<ShopItem> shopItems;
    
    public RaidShopGUI(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.shopItems = new ArrayList<>();
        
        // Register as event listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        initializeShopItems();
    }
    
    /**
     * Initialize shop items
     */
    private void initializeShopItems() {
        // Divine Fragments
        shopItems.add(new ShopItem(
            "Divine Fragment Pack",
            Material.PAPER,
            "§6§lDivine Fragment Pack",
            List.of("§7Contains 3 random divine fragments", "§7Perfect for completing testaments"),
            150,
            3 // quantity
        ));
        
        // Upgrade Materials
        shopItems.add(new ShopItem(
            "Divine Essence",
            Material.NETHER_STAR,
            "§b§lDivine Essence",
            List.of("§7Used to upgrade divine items", "§7Essential crafting material"),
            200,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Cosmic Fragment",
            Material.END_CRYSTAL,
            "§d§lCosmic Fragment",
            List.of("§7Rare upgrade material", "§7Enables legendary transformations"),
            500,
            1
        ));
        
        // Valuable Materials
        shopItems.add(new ShopItem(
            "Diamond Pack",
            Material.DIAMOND,
            "§b§lDiamond Pack",
            List.of("§7High-quality diamonds", "§7Perfect for crafting"),
            100,
            5
        ));
        
        shopItems.add(new ShopItem(
            "Netherite Scrap",
            Material.NETHERITE_SCRAP,
            "§8§lNetherite Scrap",
            List.of("§7Rare netherite material", "§7Used for ultimate gear"),
            300,
            2
        ));
        
        // Custom Enchanted Books
        shopItems.add(new ShopItem(
            "Lifesteal Book",
            Material.ENCHANTED_BOOK,
            "§c§lLifesteal Enchanted Book",
            List.of("§7Heals you for damage dealt", "§7Level: III", "§7Apply to weapons"),
            400,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Vein Miner Book",
            Material.ENCHANTED_BOOK,
            "§6§lVein Miner Enchanted Book",
            List.of("§7Breaks connected ore blocks", "§7Level: I", "§7Apply to pickaxes"),
            600,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Explosive Arrows Book",
            Material.ENCHANTED_BOOK,
            "§c§lExplosive Arrows Enchanted Book",
            List.of("§7Arrows explode on impact", "§7Level: II", "§7Apply to bows"),
            500,
            1
        ));
        
        // Enhanced Armor
        shopItems.add(new ShopItem(
            "Divine Boots",
            Material.NETHERITE_BOOTS,
            "§5§lDivine Boots",
            List.of("§7Custom divine armor piece", "§7Enhanced movement abilities", "§7Feather Falling X"),
            700,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Divine Leggings",
            Material.NETHERITE_LEGGINGS,
            "§5§lDivine Leggings",
            List.of("§7Custom divine armor piece", "§7Enhanced protection", "§7Protection X"),
            900,
            1
        ));
        
        // Special Items
        shopItems.add(new ShopItem(
            "Enchanted Golden Apple",
            Material.ENCHANTED_GOLDEN_APPLE,
            "§6§lEnchanted Golden Apple",
            List.of("§7Provides powerful effects", "§7Perfect for difficult raids"),
            250,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Totem of Undying",
            Material.TOTEM_OF_UNDYING,
            "§e§lTotem of Undying",
            List.of("§7Prevents death once", "§7Essential for survival"),
            400,
            1
        ));
        
        // Custom Armor Pieces (placeholders for now)
        shopItems.add(new ShopItem(
            "Divine Helmet",
            Material.NETHERITE_HELMET,
            "§5§lDivine Helmet",
            List.of("§7Custom divine armor piece", "§7Enhanced protection and abilities"),
            800,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Divine Chestplate",
            Material.NETHERITE_CHESTPLATE,
            "§5§lDivine Chestplate",
            List.of("§7Custom divine armor piece", "§7Enhanced protection and abilities"),
            1000,
            1
        ));
        
        // Custom Enchanted Books - New Custom Enchantments
        shopItems.add(new ShopItem(
            "Vampirism Book",
            Material.ENCHANTED_BOOK,
            "§4§lVampirism V Enchanted Book",
            List.of("§7Heals for 25% of damage dealt", "§7Level: V", "§7Apply to weapons"),
            800,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Guardian Aura Book",
            Material.ENCHANTED_BOOK,
            "§a§lGuardian Aura III Enchanted Book",
            List.of("§7Grants beneficial effects to nearby allies", "§7Level: III", "§7Apply to chestplates"),
            700,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Terraforming Book",
            Material.ENCHANTED_BOOK,
            "§6§lTerraforming I Enchanted Book",
            List.of("§7Shapes terrain when breaking blocks", "§7Level: I", "§7Apply to tools"),
            600,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Soulbound Book",
            Material.ENCHANTED_BOOK,
            "§b§lSoulbound I Enchanted Book",
            List.of("§7Item remains with you on death", "§7Level: I", "§7Apply to any item"),
            1000,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Armor Piercing Book",
            Material.ENCHANTED_BOOK,
            "§6§lArmor Piercing VII Enchanted Book",
            List.of("§7Ignores 70% of target's armor", "§7Level: VII", "§7Apply to weapons"),
            1200,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Density Book",
            Material.ENCHANTED_BOOK,
            "§8§lDensity X Enchanted Book",
            List.of("§7Extreme knockback and stunning", "§7Level: X", "§7Apply to weapons"),
            1500,
            1
        ));
        
        // Enhanced Vanilla Enchantments
        shopItems.add(new ShopItem(
            "Protection X Book",
            Material.ENCHANTED_BOOK,
            "§9§lProtection X Enchanted Book",
            List.of("§7Enhanced protection beyond vanilla limits", "§7Level: X", "§7Apply to armor"),
            900,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Sharpness X Book",
            Material.ENCHANTED_BOOK,
            "§c§lSharpness X Enchanted Book",
            List.of("§7Enhanced sharpness beyond vanilla limits", "§7Level: X", "§7Apply to weapons"),
            900,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Efficiency X Book",
            Material.ENCHANTED_BOOK,
            "§e§lEfficiency X Enchanted Book",
            List.of("§7Enhanced efficiency beyond vanilla limits", "§7Level: X", "§7Apply to tools"),
            800,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Unbreaking X Book",
            Material.ENCHANTED_BOOK,
            "§7§lUnbreaking X Enchanted Book",
            List.of("§7Enhanced unbreaking beyond vanilla limits", "§7Level: X", "§7Apply to any item"),
            700,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Riptide X Book",
            Material.ENCHANTED_BOOK,
            "§b§lRiptide VI Enchanted Book",
            List.of("§7Enhanced riptide - works without water", "§7Level: VI", "§7Apply to tridents"),
            1100,
            1
        ));
        
        // Other Existing Custom Enchantments
        shopItems.add(new ShopItem(
            "Blindness Book",
            Material.ENCHANTED_BOOK,
            "§8§lBlindness III Enchanted Book",
            List.of("§7Chance to blind enemies on hit", "§7Level: III", "§7Apply to weapons"),
            600,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Magnetism Book",
            Material.ENCHANTED_BOOK,
            "§d§lMagnetism III Enchanted Book",
            List.of("§7Pulls nearby items towards you", "§7Level: III", "§7Apply to armor"),
            500,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Tree Feller Book",
            Material.ENCHANTED_BOOK,
            "§2§lTree Feller I Enchanted Book",
            List.of("§7Breaks entire trees at once", "§7Level: I", "§7Apply to axes"),
            700,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Auto Smelt Book",
            Material.ENCHANTED_BOOK,
            "§6§lAuto Smelt I Enchanted Book",
            List.of("§7Automatically smelts mined blocks", "§7Level: I", "§7Apply to tools"),
            650,
            1
        ));
        
        shopItems.add(new ShopItem(
            "Blink Dash Book",
            Material.ENCHANTED_BOOK,
            "§5§lBlink Dash I Enchanted Book",
            List.of("§7Short-range teleportation ability", "§7Level: I", "§7Apply to boots"),
            800,
            1
        ));
    }
    
    /**
     * Open the shop for a player
     */
    public void openShop(Player player) {
        Inventory shopInventory = Bukkit.createInventory(null, 54, "§6§lRaid Shop");
        
        // Add player info item
        int playerPoints = plugin.getPlayerDataManager().getRaidPoints(player);
        ItemStack infoItem = new ItemStack(Material.EMERALD);
        org.bukkit.inventory.meta.ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§a§lYour Raid Points");
        infoMeta.setLore(List.of(
            "§7Current Points: §f" + playerPoints + " RP",
            "§7Earn more by completing raids!"
        ));
        infoItem.setItemMeta(infoMeta);
        shopInventory.setItem(4, infoItem);
        
        // Add shop items
        int slot = 9; // Start from second row
        for (ShopItem shopItem : shopItems) {
            if (slot >= 45) break; // Don't fill last row
            
            ItemStack displayItem = shopItem.createDisplayItem(playerPoints);
            shopInventory.setItem(slot, displayItem);
            slot++;
        }
        
        // Add close button
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        org.bukkit.inventory.meta.ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName("§c§lClose Shop");
        closeItem.setItemMeta(closeMeta);
        shopInventory.setItem(49, closeItem);
        
        player.openInventory(shopInventory);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }
    
    /**
     * Handle inventory clicks
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals("§6§lRaid Shop")) return;
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        // Handle close button
        if (clickedItem.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }
        
        // Handle info item
        if (clickedItem.getType() == Material.EMERALD) {
            int points = plugin.getPlayerDataManager().getRaidPoints(player);
            player.sendMessage("§a§lRaid Points: §f" + points + " RP");
            return;
        }
        
        // Find matching shop item
        ShopItem shopItem = findShopItem(clickedItem);
        if (shopItem == null) return;
        
        // Check if player has enough points
        int playerPoints = plugin.getPlayerDataManager().getRaidPoints(player);
        if (playerPoints < shopItem.getCost()) {
            player.sendMessage("§c§lInsufficient Points! §r§cYou need " + shopItem.getCost() + " RP but only have " + playerPoints + " RP");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        
        // Process purchase
        if (plugin.getPlayerDataManager().removeRaidPoints(player, shopItem.getCost())) {
            // Give item to player
            ItemStack purchasedItem = shopItem.createActualItem();
            player.getInventory().addItem(purchasedItem);
            
            // Success feedback
            player.sendMessage("§a§lPurchase Successful!");
            player.sendMessage("§7Bought: §f" + shopItem.getDisplayName());
            player.sendMessage("§7Cost: §f" + shopItem.getCost() + " RP");
            player.sendMessage("§7Remaining Points: §f" + plugin.getPlayerDataManager().getRaidPoints(player) + " RP");
            
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            
            // Refresh shop display
            openShop(player);
        } else {
            player.sendMessage("§c§lPurchase failed! Please try again.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }
    
    /**
     * Find shop item by display item
     */
    private ShopItem findShopItem(ItemStack displayItem) {
        if (!displayItem.hasItemMeta() || !displayItem.getItemMeta().hasDisplayName()) {
            return null;
        }
        
        String displayName = displayItem.getItemMeta().getDisplayName();
        for (ShopItem shopItem : shopItems) {
            if (displayName.equals(shopItem.getDisplayName())) {
                return shopItem;
            }
        }
        
        return null;
    }
}