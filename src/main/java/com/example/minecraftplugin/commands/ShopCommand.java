package com.example.minecraftplugin.commands;

import com.example.minecraftplugin.MinecraftPlugin;
import com.example.minecraftplugin.shop.RaidShopGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopCommand implements CommandExecutor, TabCompleter {
    
    private final MinecraftPlugin plugin;
    private final RaidShopGUI shopGUI;
    
    public ShopCommand(MinecraftPlugin plugin) {
        this.plugin = plugin;
        this.shopGUI = new RaidShopGUI(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("minecraftplugin.shop")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use the shop!");
            return true;
        }
        
        if (args.length == 0) {
            // Open shop GUI
            shopGUI.openShop(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "points":
                showPlayerPoints(player);
                break;
                
            case "help":
                showShopHelp(player);
                break;
                
            case "admin":
                if (player.hasPermission("minecraftplugin.admin")) {
                    handleAdminCommand(player, args);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use admin commands!");
                }
                break;
                
            default:
                shopGUI.openShop(player);
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("points", "help", "admin");
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            if (sender.hasPermission("minecraftplugin.admin")) {
                List<String> adminCommands = Arrays.asList("givepoints");
                for (String adminCmd : adminCommands) {
                    if (adminCmd.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(adminCmd);
                    }
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("givepoints")) {
            if (sender.hasPermission("minecraftplugin.admin")) {
                // Player names for givepoints command
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    String playerName = p.getName();
                    if (playerName.toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(playerName);
                    }
                }
            }
        }
        
        return completions;
    }
    
    private void showPlayerPoints(Player player) {
        int points = plugin.getPlayerDataManager().getRaidPoints(player);
        player.sendMessage("§6§l=== RAID POINTS ===§r");
        player.sendMessage("§7Your Raid Points: §f" + points + " RP");
        player.sendMessage("§7Use §f/shop§7 to spend your points!");
    }
    
    private void showShopHelp(Player player) {
        player.sendMessage("§6§l=== RAID SHOP HELP ===§r");
        player.sendMessage("§7/shop - Open the raid shop");
        player.sendMessage("§7/shop points - Check your raid points");
        player.sendMessage("§7/shop help - Show this help");
        player.sendMessage("");
        
        if (player.hasPermission("minecraftplugin.admin")) {
            player.sendMessage("§c§lAdmin Commands:");
            player.sendMessage("§7/shop admin givepoints <player> <amount> - Give raid points");
            player.sendMessage("");
        }
        
        player.sendMessage("§e§lHow to earn Raid Points:");
        player.sendMessage("§7• Complete raids successfully");
        player.sendMessage("§7• Higher tier raids give more points");
        player.sendMessage("§7• Complete objectives for bonus points");
        player.sendMessage("§7• Weekly challenges provide extra points");
    }
    
    private void handleAdminCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c§lAdmin Shop Commands:");
            player.sendMessage("§7/shop admin givepoints <player> <amount> - Give raid points to a player");
            return;
        }
        
        String adminCommand = args[1].toLowerCase();
        
        switch (adminCommand) {
            case "givepoints":
                if (args.length != 4) {
                    player.sendMessage("§cUsage: /shop admin givepoints <player> <amount>");
                    return;
                }
                handleGivePoints(player, args[2], args[3]);
                break;
                
            default:
                player.sendMessage("§cUnknown admin command: " + adminCommand);
                break;
        }
    }
    
    private void handleGivePoints(Player admin, String targetName, String amountStr) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            admin.sendMessage("§cPlayer not found: " + targetName);
            return;
        }
        
        try {
            int amount = Integer.parseInt(amountStr);
            if (amount <= 0) {
                admin.sendMessage("§cAmount must be positive!");
                return;
            }
            
            plugin.getPlayerDataManager().addRaidPoints(target, amount);
            
            admin.sendMessage("§a§lPoints granted successfully!");
            admin.sendMessage("§7Gave " + amount + " raid points to " + target.getName());
            
            target.sendMessage("§a§lRaid Points Received!");
            target.sendMessage("§7You received " + amount + " raid points from an administrator");
            target.sendMessage("§7Total points: " + plugin.getPlayerDataManager().getRaidPoints(target));
            
        } catch (NumberFormatException e) {
            admin.sendMessage("§cInvalid amount: " + amountStr);
        }
    }
}