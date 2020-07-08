package mc.dragons.core.bridge;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
	public void setEntityAI(Entity entity, boolean ai);
	public void setItemStackUnbreakable(ItemStack itemStack, boolean unbreakable);
	double[] getAABB(Entity entity);
	public void setEntityInvulnerable(Entity entity, boolean immortal);
}
