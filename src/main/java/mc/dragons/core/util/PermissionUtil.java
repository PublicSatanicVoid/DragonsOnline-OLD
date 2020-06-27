package mc.dragons.core.util;

import org.bukkit.ChatColor;

import mc.dragons.core.gameobject.user.PermissionLevel;
import mc.dragons.core.gameobject.user.User;

public class PermissionUtil {
	public static boolean verifyActivePermissionLevel(User user, PermissionLevel required, boolean notify) {
		if(user.getActivePermissionLevel().ordinal() < required.ordinal()) {
			if(notify) {
				user.p().sendMessage(ChatColor.RED + "This requires permission level " + required.toString());
			}
			return false;
		}
		return true;
	}
}
