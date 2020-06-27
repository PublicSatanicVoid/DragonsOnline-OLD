package mc.dragons.core.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.PermissionLevel;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.storage.impl.SystemProfileLoader;

/**
 * Event handler for player quitting.
 * 
 * @author Rick
 *
 */
public class QuitEventListener implements Listener {

	private UserLoader userLoader;
	
	public QuitEventListener() {
		userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		User user = UserLoader.fromPlayer(event.getPlayer());
		user.autoSave();
		if(user.getSystemProfile() != null) {
			SystemProfileLoader.logoutProfile(user.getSystemProfile().getProfileName());
			user.setActivePermissionLevel(PermissionLevel.USER);
			user.setSystemProfile(null);
		}
		event.setQuitMessage(null);
		event.getPlayer().getInventory().clear(); // prevent item dupes by removing the "vanilla" items - refilled on login
		userLoader.removeStalePlayer(event.getPlayer());
	}
}
