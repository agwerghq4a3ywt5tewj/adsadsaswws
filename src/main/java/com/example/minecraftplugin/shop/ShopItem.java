package com.example.minecraftplugin.shop;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an item in the raid shop
 */
public class ShopItem {
    
    private final String name;
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private final int cost;
    private final int quantity;
    
    public ShopItem(String name, Material material, String displayName, List<String> lore, int cost, int quantity) {
        this.name = name;
        this.material = material;
        this.displayName = displayName;
        this.lore = new ArrayList<>(lore);
        this.cost = cost;
        this.quantity = quantity;
    }
    
    /**
     * Create display item for shop GUI
     */
    public ItemStack createDisplayItem(int playerPoints) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(displayName);
            
            List<String> displayLore = new ArrayList<>(lore);
            displayLore.add("");
            displayLore.add("§e§lCost: §f" + cost + " RP");
            displayLore.add("§e§lQuantity: §f" + quantity);
            displayLore.add("");
            
            if (playerPoints >= cost) {
                displayLore.add("§a§lClick to purchase!");
            } else {
                displayLore.add("§c§lInsufficient points!");
                displayLore.add("§7Need " + (cost - playerPoints) + " more RP");
            }
            
            meta.setLore(displayLore);
            
            // Add glow effect
            meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Create the actual item to give to player
     */
    public ItemStack createActualItem() {
        ItemStack item = new ItemStack(material, quantity);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    // Getters
    public String getName() { return name; }
    public Material getMaterial() { return material; }
    public String getDisplayName() { return displayName; }
    public List<String> getLore() { return new ArrayList<>(lore); }
    public int getCost() { return cost; }
    public int getQuantity() { return quantity; }
}