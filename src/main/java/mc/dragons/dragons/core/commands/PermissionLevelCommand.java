package mc.dragons.dragons.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.dragons.core.gameobject.player.PermissionLevel;
import mc.dragons.dragons.core.gameobject.player.User;

public class PermissionLevelCommand implements CommandExecutor {

	private UserLoader playerLoader;
	
	public PermissionLevelCommand() {
		playerLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = null;
		User user = null;
		if(sender instanceof Player) {
			player = (Player) sender;
			user = playerLoader.fromPlayer(player);
			if(user.getPermissionLevel().getLevel() < PermissionLevel.SYSOP.getLevel()) {
				sender.sendMessage(ChatColor.RED + "This command requires permission level SYSOP.");
				return true;
			}
		}
		
		if(args.length < 2) {
			sender.sendMessage(ChatColor.RED + "Insufficient arguments! /permissionlevel <player> <level>");
			return true;
		}
		
		String username = args[0];
		Player targetPlayer = Bukkit.getPlayerExact(username);
		User targetUser = playerLoader.loadObject(username);
		
		PermissionLevel permissionLevel = null;
		try {
			permissionLevel = PermissionLevel.valueOf(args[1]);
		}
		catch(Exception e) {
			sender.sendMessage(ChatColor.RED + "An error occurred! Did you specify a valid permission level?");
		}
		if(permissionLevel == null) return true;
		
		targetUser.setPermissionLevel(permissionLevel);
	
		if(targetPlayer == null) {
			sender.sendMessage(ChatColor.YELLOW + "Player is not online on this server! They will have to rejoin for the permission update to be fully applied.");
		}
		else {
			targetUser.sendTitle(ChatColor.DARK_GRAY, "Permission Update", ChatColor.GRAY, permissionLevel.toString());
			targetPlayer.sendMessage(ChatColor.GRAY + "Your permission level was updated to " + permissionLevel.toString());
		}
		
		sender.sendMessage(ChatColor.GREEN + "Permission level updated successfully.");
		
		return true;
	}
}
