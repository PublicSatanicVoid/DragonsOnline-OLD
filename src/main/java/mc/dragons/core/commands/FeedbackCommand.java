package mc.dragons.core.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.PermissionLevel;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.storage.impl.FeedbackLoader;
import mc.dragons.core.storage.impl.FeedbackLoader.FeedbackEntry;
import mc.dragons.core.util.PermissionUtil;
import mc.dragons.core.util.StringUtil;

public class FeedbackCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = null;
		User user = null;
		
		if(sender instanceof Player) {
			player = (Player) sender;
			user = UserLoader.fromPlayer(player);
			//if(!PermissionUtil.verifyActivePermissionLevel(user, PermissionLevel.TESTER, true)) return true;
		}
		else {
			sender.sendMessage(ChatColor.RED + "This is an ingame-only command.");
			return true;
		}
		
		if(args.length == 0) {
			sender.sendMessage(ChatColor.YELLOW + "/feedback <Your feedback here>");
			if(PermissionUtil.verifyActivePermissionLevel(user, PermissionLevel.GM, false)) {
				sender.sendMessage(ChatColor.YELLOW + "/feedback -list");
				sender.sendMessage(ChatColor.YELLOW + "/feedback -unread <FeedbackUUID>");
			}
			return true;
		}
		
		if(args[0].equalsIgnoreCase("-list")) {
			if(!PermissionUtil.verifyActivePermissionLevel(user, PermissionLevel.GM, true)) return true;
			sender.sendMessage(ChatColor.GREEN + "Listing all unread feedback:");
			for(FeedbackEntry entry : FeedbackLoader.getUnreadFeedback()) {
				sender.sendMessage(ChatColor.YELLOW + "[" + entry.getUUID() + "] " + ChatColor.GREEN + "[" + entry.getFrom() + "] " + ChatColor.RESET + entry.getFeedback());
				FeedbackLoader.markRead(entry.getUUID(), true);
			}
			return true;
		}
		
		if(args[0].equalsIgnoreCase("-unread")) {
			if(!PermissionUtil.verifyActivePermissionLevel(user, PermissionLevel.GM, true)) return true;
			if(args.length < 2) {
				sender.sendMessage(ChatColor.RED + "Insufficient arguments! /feedback -unread <FeedbackUUID>");
				return true;
			}
			try {
				UUID uuid = UUID.fromString(args[1]);
				FeedbackLoader.markRead(uuid, false);
				sender.sendMessage(ChatColor.GREEN + "Marked feedback entry " + args[1] + " as unread.");
			}
			catch(IllegalArgumentException e) {
				sender.sendMessage(ChatColor.RED + "Invalid UUID!");
			}
			return true;
		}
		
		String feedback = StringUtil.concatArgs(args, 0);
		FeedbackLoader.addFeedback(user.getName(), feedback);
		sender.sendMessage(ChatColor.GREEN + "Your feedback has been recorded. Thank you for submitting it!");
		
		return true;
	}

}
