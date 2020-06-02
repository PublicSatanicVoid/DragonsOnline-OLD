package mc.dragons.dragons.core.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.dragons.core.gameobject.player.User;

/**
 * Manages chat formatting.
 * 
 * @author Rick
 *
 */
public class ChatEventListener implements Listener {

	private UserLoader playerLoader;
	
	public ChatEventListener() {
		playerLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		User user = playerLoader.fromPlayer(event.getPlayer());
		event.setCancelled(true);
		
		String message = user.getLevelColor() + "" + user.getLevel() + " ";
		if(user.getRank().hasChatPrefix()) {
			message += user.getRank().getChatPrefix() + " ";
		}
		message += user.getRank().getNameColor() + user.p().getName() + ChatColor.GRAY + " > ";
		message += user.getRank().getChatColor() + event.getMessage();
		
		Bukkit.broadcastMessage(message);
	}
}
