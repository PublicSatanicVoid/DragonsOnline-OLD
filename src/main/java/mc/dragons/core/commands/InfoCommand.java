package mc.dragons.core.commands;

import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.floor.Floor;
import mc.dragons.core.gameobject.loader.FloorLoader;
import mc.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.region.Region;
import mc.dragons.core.gameobject.user.PermissionLevel;
import mc.dragons.core.gameobject.user.SkillType;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.util.PermissionUtil;
import mc.dragons.core.util.StringUtil;
import net.md_5.bungee.api.ChatColor;

public class InfoCommand implements CommandExecutor {
	private UserLoader userLoader;
	private RegionLoader regionLoader;
	private FloorLoader floorLoader;
	
	public InfoCommand(Dragons instance) {
		userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		regionLoader = (RegionLoader) GameObjectType.REGION.<Region>getLoader();
		floorLoader = (FloorLoader) GameObjectType.FLOOR.<Floor>getLoader();
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = null;
		User user = null;
		if(sender instanceof Player) {
			player = (Player) sender;
			user = UserLoader.fromPlayer(player);
			if(!PermissionUtil.verifyActivePermissionLevel(user, PermissionLevel.MOD, true)) return true;
		}
		
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Please specify a player! /info <player>");
			return true;
		}
		
		String username = args[0];
		Player targetPlayer = Bukkit.getPlayerExact(username);
		
		User targetUser = userLoader.loadObject(username);
		
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
			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + "This player is offline. Showing cached data.");
		}
		sender.sendMessage(ChatColor.YELLOW + "UUID: " + ChatColor.RESET + targetUser.getIdentifier().getUUID().toString());
		sender.sendMessage(ChatColor.YELLOW + "XP: " + ChatColor.RESET + targetUser.getXP() + " [Level " + targetUser.getLevel() + "]");
		sender.sendMessage(ChatColor.YELLOW + "Rank: " + ChatColor.RESET + targetUser.getRank().getRankName());
		sender.sendMessage(ChatColor.YELLOW + "Gold Balance: " + ChatColor.RESET + targetUser.getGold());
		if(targetPlayer == null) {
			sender.sendMessage(ChatColor.YELLOW + "Cached Location: " + ChatColor.RESET + StringUtil.locToString(targetUser.getSavedLocation()) + " in " + targetUser.getSavedLocation().getWorld().getName());
			sender.sendMessage(ChatColor.YELLOW + "Cached Floor: " + ChatColor.RESET + floorLoader.fromLocation(targetUser.getSavedLocation()).getDisplayName());
			sender.sendMessage(ChatColor.YELLOW + "Cached Regions: " + ChatColor.RESET + regionLoader.getRegionsByLocationXZ(targetUser.getSavedLocation()).stream().map(r -> r.getName()).collect(Collectors.joining(", ")));
			sender.sendMessage(ChatColor.YELLOW + "Health: " + ChatColor.RESET + targetUser.getSavedHealth() + " / " + targetUser.getSavedMaxHealth());
		}
		else {
			if(targetUser.getSystemProfile() != null) {
				sender.sendMessage(ChatColor.YELLOW + "System Profile: " + ChatColor.RESET + targetUser.getSystemProfile().getProfileName());
			}
			sender.sendMessage(ChatColor.YELLOW + "Active Permission Level: " + ChatColor.RESET + targetUser.getActivePermissionLevel().toString());
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
			userLoader.unregister(targetUser);
		}
		
		return true;
	}
}
