package mc.dragons.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.gameobject.user.User.PunishmentType;
import mc.dragons.core.storage.impl.SystemProfile.SystemProfileFlags.SystemProfileFlag;
import mc.dragons.core.util.PermissionUtil;
import mc.dragons.core.util.StringUtil;

public class PunishCommands implements CommandExecutor {

	private UserLoader userLoader;
	
	public PunishCommands() {
		userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	
		Player player = null; 
		User user = null;
		
		if(sender instanceof Player) {
			player = (Player) sender;
			user = UserLoader.fromPlayer(player);
			//if(!PermissionUtil.verifyActivePermissionLevel(user, PermissionLevel.MOD, true)) return true;
			if(!PermissionUtil.verifyActivePermissionFlag(user, SystemProfileFlag.MODERATION, true)) return true;
		}
		
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Specify a player! /" + label + " <player> [reason] [-d #y#w#d#h#m#s]");
			return true;
		}
		
		User targetUser = userLoader.loadObject(args[0]);
		int durationIndex = StringUtil.getFlagIndex(args, "-d", 1);
		String duration = durationIndex == -1 ? "" : StringUtil.concatArgs(args, durationIndex + 1);
		String reason = StringUtil.concatArgs(args, 1, durationIndex == -1 ? args.length : durationIndex);
		long durationSeconds = StringUtil.parseTimespanToSeconds(duration);
		
		if(targetUser == null) {
			sender.sendMessage(ChatColor.RED + "That player was not found!");
			return true;
		}
		
		targetUser.punish(label.equalsIgnoreCase("ban") ? PunishmentType.BAN : PunishmentType.MUTE, reason, durationSeconds);
		sender.sendMessage(ChatColor.GREEN + "Punishment applied successfully.");
		
		return true;
	}

}
