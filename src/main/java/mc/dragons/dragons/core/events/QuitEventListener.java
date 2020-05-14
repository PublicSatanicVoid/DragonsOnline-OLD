package mc.dragons.dragons.core.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.GameObjectRegistry;
import mc.dragons.dragons.core.gameobject.loader.PlayerLoader;
import mc.dragons.dragons.core.gameobject.player.User;
import mc.dragons.dragons.core.storage.StorageManager;

/**
 * Event handler for player quitting.
 * 
 * @author Rick
 *
 */
public class QuitEventListener implements Listener {

	private PlayerLoader playerLoader;
	
	public QuitEventListener() {
		playerLoader = (PlayerLoader)GameObjectType.PLAYER.getLoader();
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		User user = playerLoader.fromPlayer(event.getPlayer());
		user.autoSave();
		playerLoader.removeStalePlayer(event.getPlayer());
	}
}
