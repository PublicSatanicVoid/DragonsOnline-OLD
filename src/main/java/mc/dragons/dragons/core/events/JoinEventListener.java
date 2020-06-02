package mc.dragons.dragons.core.events;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.dragons.core.gameobject.player.User;

/**
 * Event handler for player joins. Takes care of user registration
 * and fetching user from database.
 * 
 * @author Rick
 *
 */
public class JoinEventListener implements Listener {
	private UserLoader playerLoader;
	private Dragons plugin;
	
	public JoinEventListener(Dragons instance) {
		playerLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		plugin = instance;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		UUID uuid = p.getUniqueId();
		User user = playerLoader.loadObject(uuid);
		if(user == null) {
			plugin.getLogger().info("Player " + p.getName() + " joined for the first time");
			user = playerLoader.registerNew(p);
		}
		user.handleJoin();
	}
}
	