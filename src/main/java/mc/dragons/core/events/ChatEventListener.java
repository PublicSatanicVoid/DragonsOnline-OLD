package mc.dragons.core.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.User;

/**
 * Manages chat formatting.
 * 
 * @author Rick
 *
 */
public class ChatEventListener implements Listener {

	//private UserLoader userLoader;
	
	public ChatEventListener() {
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		User user = UserLoader.fromPlayer(event.getPlayer());
		event.setCancelled(true);
		
		String message = user.getLevelColor() + "" + user.getLevel() + " ";
		
		if(user.getSystemProfile() != null) {
			message += ChatColor.GREEN + "~[" + user.getSystemProfile().getProfileName() + "] ";
		}
		
		if(user.getRank().hasChatPrefix()) {
			message += user.getRank().getChatPrefix() + " ";
		}
		
		message += user.getRank().getNameColor() + user.p().getName() + ChatColor.GRAY + " » ";
		message += user.getRank().getChatColor() + event.getMessage();
		
		Bukkit.broadcastMessage(message);
	}
}
