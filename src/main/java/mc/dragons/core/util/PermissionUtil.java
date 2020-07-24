package mc.dragons.core.util;

import org.bukkit.ChatColor;

import mc.dragons.core.gameobject.user.PermissionLevel;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.storage.impl.SystemProfile.SystemProfileFlags.SystemProfileFlag;

public class PermissionUtil {
	public static boolean verifyActivePermissionLevel(User user, PermissionLevel required, boolean notify) {
		if(user.getActivePermissionLevel().ordinal() < required.ordinal()) {
			if(notify) {
				user.getPlayer().sendMessage(ChatColor.RED + "This requires permission level " + required);
			}
			return false;
		}
		return true;
	}
	
	public static boolean verifyActivePermissionFlag(User user, SystemProfileFlag flag, boolean notify) {
		boolean hasFlag = false;
		if(user.getSystemProfile() != null) {
			hasFlag = user.getSystemProfile().getFlags().hasFlag(flag);
		}
		if(!hasFlag) {
			user.getPlayer().sendMessage(ChatColor.RED + "This requires permission flag " + flag);
		}
		return hasFlag;
	}
}
