package mc.dragons.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class JapaneseSymbolForBeginnerCommand implements CommandExecutor  {
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		sender.sendMessage("Japanese symbol for beginner");
		sender.sendMessage(ChatColor.GREEN + "🔰");
		
		return true;
	}
}
