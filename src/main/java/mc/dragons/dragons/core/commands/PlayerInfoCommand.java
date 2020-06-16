package mc.dragons.dragons.core.commands;

import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.floor.Floor;
import mc.dragons.dragons.core.gameobject.loader.FloorLoader;
import mc.dragons.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.dragons.core.gameobject.player.PermissionLevel;
import mc.dragons.dragons.core.gameobject.player.SkillType;
import mc.dragons.dragons.core.gameobject.player.User;
import mc.dragons.dragons.core.gameobject.region.Region;
import mc.dragons.dragons.core.util.StringUtil;
import net.md_5.bungee.api.ChatColor;

public class PlayerInfoCommand implements CommandExecutor {
	private UserLoader playerLoader;
	private RegionLoader regionLoader;
	private FloorLoader floorLoader;
	
	public PlayerInfoCommand(Dragons instance) {
		playerLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		regionLoader = (RegionLoader) GameObjectType.REGION.<Region>getLoader();
		floorLoader = (FloorLoader) GameObjectType.FLOOR.<Floor>getLoader();
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = null;
		User user = null;
		if(sender instanceof Player) {
			player = (Player) sender;
			user = playerLoader.fromPlayer(player);
			if(user.getPermissionLevel().getLevel() < PermissionLevel.MONITOR.getLevel()) {
				sender.sendMessage(ChatColor.RED + "This command requires permission level MONITOR.");
				return true;
			}
		}
		
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Please specify a player! /info <player>");
			return true;
		}
		
		String username = args[0];
		Player targetPlayer = Bukkit.getPlayerExact(username);
		
		User targetUser = playerLoader.loadObject(username);
		
		if(targetUser == null) {
			sender.sendMessage(ChatColor.RED + "That player does not exist!");
			return true;
		}
		
		
		String skills = "";
		for(SkillType skill : SkillType.values()) {
			skills += skill.toString() + " (" + targetUser.getSkillLevel(skill) + "), ";
		}
		skills = skills.substring(0, skills.length() - 2);

		sender.sendMessage(ChatColor.GOLD + "Report for User " + targetUser.getName());
		if(targetPlayer == null) {
			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + "This player is offline.");
		}
		sender.sendMessage(ChatColor.YELLOW + "UUID: " + ChatColor.RESET + targetUser.getIdentifier().getUUID().toString());
		sender.sendMessage(ChatColor.YELLOW + "XP: " + ChatColor.RESET + targetUser.getXP() + " [Level " + targetUser.getLevel() + "]");
		sender.sendMessage(ChatColor.YELLOW + "Permission Level: " + ChatColor.RESET + targetUser.getPermissionLevel().ordinal() + " (" + targetUser.getPermissionLevel().toString() + ")");
		sender.sendMessage(ChatColor.YELLOW + "Rank: " + ChatColor.RESET + targetUser.getRank().getRankName());
		if(targetPlayer == null) {
			sender.sendMessage(ChatColor.YELLOW + "Cached Location: " + ChatColor.RESET + StringUtil.locToString(targetUser.getSavedLocation()) + " in " + targetUser.getSavedLocation().getWorld().getName());
			sender.sendMessage(ChatColor.YELLOW + "Cached Floor: " + ChatColor.RESET + floorLoader.fromLocation(targetUser.getSavedLocation()).getDisplayName());
			sender.sendMessage(ChatColor.YELLOW + "Cached Regions: " + ChatColor.RESET + regionLoader.getRegionsByLocationXZ(targetUser.getSavedLocation()).stream().map(r -> r.getName()).collect(Collectors.joining(", ")));
			sender.sendMessage(ChatColor.YELLOW + "Health: " + ChatColor.RESET + targetUser.getSavedHealth() + " / " + targetUser.getSavedMaxHealth());
		}
		else {
			sender.sendMessage(ChatColor.YELLOW + "Location: " + ChatColor.RESET + StringUtil.locToString(targetPlayer.getLocation()) + " in " + targetPlayer.getWorld().getName());
			sender.sendMessage(ChatColor.YELLOW + "Floor: " + ChatColor.RESET + floorLoader.fromLocation(targetPlayer.getLocation()).getDisplayName());
			sender.sendMessage(ChatColor.YELLOW + "Regions: " + ChatColor.RESET + targetUser.getRegions().stream().map(r -> r.getFlags().getString("fullname")).collect(Collectors.joining(", ")));
			sender.sendMessage(ChatColor.YELLOW + "Health: " + ChatColor.RESET + targetPlayer.getHealth() + " / " + targetPlayer.getMaxHealth());
		}
		sender.sendMessage(ChatColor.YELLOW + "Skills: " + ChatColor.RESET + skills);
		sender.sendMessage(ChatColor.YELLOW + "First Join: " + ChatColor.RESET + targetUser.getFirstJoined().toString());
		sender.sendMessage(ChatColor.YELLOW + "Last Join: " + ChatColor.RESET + targetUser.getLastJoined().toString());
		sender.sendMessage(ChatColor.YELLOW + "Last Seen: " + ChatColor.RESET + targetUser.getLastSeen().toString());
		
		if(targetPlayer == null) {
			// User was only constructed for querying purposes. Since they're not really online, remove them from local registry
			playerLoader.unregister(targetUser);
		}
		
		return true;
	}
}
