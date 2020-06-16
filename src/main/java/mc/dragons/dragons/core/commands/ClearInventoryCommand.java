package mc.dragons.dragons.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.dragons.core.gameobject.player.User;

public class ClearInventoryCommand implements CommandExecutor {
	private UserLoader playerLoader;
	
	public ClearInventoryCommand() {
		playerLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This is an ingame-only command!");
			return true;
		}
		
		Player player = (Player) sender;
		User user = playerLoader.fromPlayer(player);
		
		if(args.length == 0) {
			player.sendMessage(ChatColor.GOLD + "Warning!" + ChatColor.YELLOW + " This will clear all items from your inventory. This cannot be undone.");
			player.sendMessage(ChatColor.YELLOW + "Type" + ChatColor.GOLD + " /clear confirm" + ChatColor.YELLOW + " to proceed.");
			return true;
		}
		else {
			if(args[0].equalsIgnoreCase("confirm")) {
				user.clearInventory();
				player.sendMessage(ChatColor.GREEN + "Cleared your inventory.");
				return true;
			}
		}
		
		return true;
	}
}
