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
import mc.dragons.dragons.core.gameobject.player.Rank;
import mc.dragons.dragons.core.gameobject.player.User;

public class RankCommand implements CommandExecutor {
	private UserLoader playerLoader;
	
	public RankCommand() {
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
			sender.sendMessage(ChatColor.RED + "Insufficient arguments! /rank <player> <rank>");
			return true;
		}
		
		String username = args[0];
		Player targetPlayer = Bukkit.getPlayerExact(username);
		User targetUser = playerLoader.loadObject(username);
		
		Rank rank = null;
		try {
			rank = Rank.valueOf(args[1]);
		}
		catch(Exception e) {
			sender.sendMessage(ChatColor.RED + "An error occurred! Did you specify a valid rank?");
		}
		if(rank == null) return true;
		
		targetUser.setRank(rank);
	
		if(targetPlayer == null) {
			sender.sendMessage(ChatColor.YELLOW + "Player is not online on this server! They will have to rejoin for the rank update to be fully applied.");
		}
		else {
			targetUser.sendTitle(ChatColor.DARK_GRAY, "Rank Update", ChatColor.GRAY, rank.toString());
			targetPlayer.sendMessage(ChatColor.GRAY + "Your rank was updated to " + rank.toString());
		}
		
		sender.sendMessage(ChatColor.GREEN + "Rank updated successfully.");
		
		return true;
	}

}
