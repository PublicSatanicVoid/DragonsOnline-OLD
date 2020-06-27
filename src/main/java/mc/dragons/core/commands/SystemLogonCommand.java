package mc.dragons.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.PermissionLevel;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.storage.impl.SystemProfile;
import mc.dragons.core.storage.impl.SystemProfileLoader;
import mc.dragons.core.util.PermissionUtil;

public class SystemLogonCommand implements CommandExecutor {
	//private UserLoader userLoader;
	
	public SystemLogonCommand() {
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

	
		if(args.length == 0) {
			if(sender instanceof Player) {
				sender.sendMessage(ChatColor.GREEN + "System Logon Service");
				sender.sendMessage(ChatColor.YELLOW + "/syslogon <profile> <password>");
				sender.sendMessage(ChatColor.YELLOW + "/syslogon -p <new permission level>");
				sender.sendMessage(ChatColor.YELLOW + "/syslogon -c <new profile> <password> <permission level>");
				sender.sendMessage(ChatColor.YELLOW + "/syslogon -logout");
				sender.sendMessage(ChatColor.DARK_GRAY + "Note: Profiles and passwords cannot contain spaces.");
			}
			else {
				sender.sendMessage(ChatColor.YELLOW + "/syslogon -c <new profile> <password> <level>");
				sender.sendMessage(ChatColor.DARK_GRAY + "Note: Profiles and passwords cannot contain spaces.");
			}
			return true;
		}
		
		if(args[0].equalsIgnoreCase("-c")) {
			if(sender instanceof Player) {
				Player player = (Player) sender;
				User user = UserLoader.fromPlayer(player);
				if(!PermissionUtil.verifyActivePermissionLevel(user, PermissionLevel.SYSOP, true)) return true;
			}
			SystemProfileLoader.createProfile(args[1], args[2], PermissionLevel.valueOf(args[3]));
			sender.sendMessage(ChatColor.GREEN + "Created system profile successfully.");
			return true;
		}
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This is an ingame-only command!");
			return true;
		}
		
		Player player = (Player) sender;
		User user = UserLoader.fromPlayer(player);
		
		if(args[0].equalsIgnoreCase("-p")) {
			if(user.getSystemProfile() == null) {
				sender.sendMessage(ChatColor.RED + "You're not logged in to a system profile.");
				return true;
			}
			boolean result = user.setActivePermissionLevel(PermissionLevel.valueOf(args[1]));
			if(result) {
				sender.sendMessage(ChatColor.GREEN + "Changed active permission level to " + args[1]);
			}
			else {
				sender.sendMessage(ChatColor.RED + "Could not change active permission level: requested permission level exceeds maximum level for this profile.");
			}
			return true;
		}
		
		if(args[0].equalsIgnoreCase("-logout")) {
			if(user.getSystemProfile() == null) {
				sender.sendMessage(ChatColor.RED + "You're not logged in to a system profile.");
				return true;
			}
			SystemProfileLoader.logoutProfile(user.getSystemProfile().getProfileName());
			user.setActivePermissionLevel(PermissionLevel.USER);
			user.setSystemProfile(null);
			sender.sendMessage(ChatColor.GREEN + "Successfully logged out of your system profile.");
			return true;
		}
		
		if(user.getSystemProfile() != null) {
			SystemProfileLoader.logoutProfile(user.getSystemProfile().getProfileName());
			user.setActivePermissionLevel(PermissionLevel.USER);
			sender.sendMessage(ChatColor.GREEN + "Signed out of current system profile");
		}
		
		SystemProfile profile = SystemProfileLoader.loadProfile(user, args[0], args[1]);
		if(profile == null) {
			sender.sendMessage(ChatColor.RED + "Invalid credentials provided!");
			return true;
		}
		user.setSystemProfile(profile);
		
		sender.sendMessage(ChatColor.GREEN + "Logged on to system console as ~" + profile.getProfileName());
		
		
		
		return true;
	}
}
