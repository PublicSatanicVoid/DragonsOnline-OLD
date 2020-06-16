package mc.dragons.dragons.core.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.dragons.core.gameobject.player.User;

/**
 * Event handler for player quitting.
 * 
 * @author Rick
 *
 */
public class QuitEventListener implements Listener {

	private UserLoader playerLoader;
	
	public QuitEventListener() {
		playerLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		User user = playerLoader.fromPlayer(event.getPlayer());
		user.autoSave();
		event.getPlayer().getInventory().clear(); // prevent item dupes by removing the "vanilla" item
		playerLoader.removeStalePlayer(event.getPlayer());
	}
}
