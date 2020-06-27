package mc.dragons.core.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.User;

public class MoveEventListener implements Listener {

	//private UserLoader userLoader;
	
	public MoveEventListener() {
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		User user = UserLoader.fromPlayer(event.getPlayer());
		if(user.hasDeathCountdown()) {
			event.setTo(event.getFrom());
			return;
		}
		user.handleMove();
	}
}
