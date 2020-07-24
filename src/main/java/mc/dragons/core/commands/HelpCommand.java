package mc.dragons.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HelpCommand implements CommandExecutor {

	public static final String INDENT = ChatColor.GRAY + "   " + ChatColor.ITALIC;
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		sender.sendMessage(ChatColor.GOLD + "Displaying help for DragonsOnline.");
		sender.sendMessage(ChatColor.YELLOW + "/channel <speak|listen> <alvador|local|guild|party|trade|help>");
		sender.sendMessage(INDENT + "Set the channels you speak and listen to.");
		sender.sendMessage("");
		sender.sendMessage(ChatColor.YELLOW + "/feedback <your feedback here>");
		sender.sendMessage(INDENT + "Send us feedback about the game.");
		sender.sendMessage("");
		sender.sendMessage(ChatColor.YELLOW + "/msg <player> <message>");
		sender.sendMessage(INDENT + "Send a private message.");
		sender.sendMessage("");
		sender.sendMessage(ChatColor.YELLOW + "/myquests");
		sender.sendMessage(INDENT + "View your quests.");
		sender.sendMessage("");
		sender.sendMessage(ChatColor.YELLOW + "/reply <message>");
		sender.sendMessage(INDENT + "Reply to the last player who messaged you.");
		
		return true;
	}

}