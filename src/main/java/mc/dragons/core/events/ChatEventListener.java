package mc.dragons.core.events;

import java.util.logging.Logger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.User;

/**
 * Manages chat formatting.
 * 
 * @author Rick
 *
 */
public class ChatEventListener implements Listener {

	private Logger LOGGER = Dragons.getInstance().getLogger();
	//private UserLoader userLoader;
	
	public ChatEventListener() {
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		LOGGER.finer("Chat event from player " + event.getPlayer().getName());
		User user = UserLoader.fromPlayer(event.getPlayer());
		event.setCancelled(true);
		user.chat(event.getMessage());
	}
}
