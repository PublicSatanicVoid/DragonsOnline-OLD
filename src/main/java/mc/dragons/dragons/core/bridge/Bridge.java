package mc.dragons.dragons.core.bridge;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Abstraction layer providing functionality
 * which may require implementation from low-level,
 * version-specific NMS or OBC, at least for
 * some versions.
 * 
 * @author Rick
 *
 */
public interface Bridge {
	public String getAPIVersion();
	public void sendActionBar(Player player, String message);
	public void sendTitle(Player player, ChatColor titleColor, String title, 
			ChatColor subtitleColor, String subtitle, 
			int fadeInTime, int showTime, int fadeOutTime);
	public void respawnPlayer(Player player);
}
