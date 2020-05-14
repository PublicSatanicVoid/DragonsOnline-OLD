package mc.dragons.dragons.core.events;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.GameObjectRegistry;
import mc.dragons.dragons.core.gameobject.loader.PlayerLoader;
import mc.dragons.dragons.core.gameobject.player.User;
import mc.dragons.dragons.core.storage.StorageAccess;
import mc.dragons.dragons.core.storage.StorageManager;

/**
 * Event handler for player joins. Takes care of user registration
 * and fetching user from database.
 * 
 * @author Rick
 *
 */
public class JoinEventListener implements Listener {
	
	private StorageManager storageManager;
	private GameObjectRegistry gameObjectLoader;
	private Dragons plugin;
	
	public JoinEventListener(Dragons instance) {
		storageManager = instance.getStorageManager();
		gameObjectLoader = instance.getGameObjectRegistry();
		plugin = instance;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		UUID uuid = p.getUniqueId();
		StorageAccess storageAccess = storageManager.getStorageAccess(
				GameObjectType.PLAYER, 
				uuid);
		GameObject gameObject;
		if(storageAccess == null) {
			plugin.getLogger().info("Player " + p.getName() + " joined for the first time");
			gameObject = ((PlayerLoader)GameObjectType.PLAYER.getLoader()).registerNew(p);
		}
		else {	
			gameObject = gameObjectLoader.loadObject(storageAccess);
		}
		User user = (User)gameObject;
		user.handleJoin();
	}
}
	