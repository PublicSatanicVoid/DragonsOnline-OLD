package mc.dragons.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import mc.dragons.core.Dragons;
import mc.dragons.core.addon.AddonRegistry;

public class AddonCommand implements CommandExecutor {

	private AddonRegistry addonRegistry;
	
	public AddonCommand(Dragons instance) {
		addonRegistry = instance.getAddonRegistry();
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + "/addon -list");
			return true;
		}
		
		if(args[0].equalsIgnoreCase("-list")) {
			sender.sendMessage(ChatColor.GREEN + "Listing all addons:");
			addonRegistry.getAllAddons().forEach(addon -> sender.sendMessage(ChatColor.GRAY + "- " + addon.getName() + " [" + addon.getType() + "]"));
			return true;
		}
		
		
		return true;
	}
}
