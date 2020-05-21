package mc.dragons.dragons.core.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.PlayerLoader;
import mc.dragons.dragons.core.gameobject.player.User;
import net.md_5.bungee.api.ChatColor;

/**
 * Manages chat formatting.
 * 
 * @author Rick
 *
 */
public class ChatEventListener implements Listener {

	private PlayerLoader playerLoader;
	
	public ChatEventListener() {
		playerLoader = (PlayerLoader)GameObjectType.PLAYER.getLoader();
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		User user = playerLoader.fromPlayer(event.getPlayer());
		event.setCancelled(true);
		
		String message = ChatColor.DARK_GRAY + "[" + user.getLevelColor() + user.getLevel() + ChatColor.DARK_GRAY + "] ";
		if(user.getRank().hasChatPrefix()) {
			message += ChatColor.DARK_GRAY + "[" + user.getRank().getChatPrefix() + ChatColor.DARK_GRAY + "] ";
		}
		message += user.getRank().getNameColor() + user.p().getName() + ChatColor.GRAY + " > ";
		message += user.getRank().getChatColor() + event.getMessage();
		
		Bukkit.broadcastMessage(message);
	}
}
