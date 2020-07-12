package mc.dragons.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.User;

public class FastForwardDialogueCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if(sender instanceof Player) {
			User user = UserLoader.fromPlayer((Player) sender);
			while(user.hasActiveDialogue()) {
				user.nextDialogue();
			}
		}
		
		return true;
	}

}
