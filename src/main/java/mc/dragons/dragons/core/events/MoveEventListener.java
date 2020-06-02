package mc.dragons.dragons.core.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.dragons.core.gameobject.player.User;

public class MoveEventListener implements Listener {

	private UserLoader playerLoader;
	
	public MoveEventListener() {
		playerLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		User user = playerLoader.fromPlayer(event.getPlayer());
		if(user.hasDeathCountdown()) {
			event.setTo(event.getFrom());
			return;
		}
		user.handleMove();
	}
}
