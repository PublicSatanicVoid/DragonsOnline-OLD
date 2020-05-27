package mc.dragons.dragons.core.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.PlayerLoader;
import mc.dragons.dragons.core.gameobject.player.User;

public class MoveEventListener implements Listener {

	private PlayerLoader playerLoader;
	
	public MoveEventListener() {
		playerLoader = (PlayerLoader)GameObjectType.PLAYER.getLoader();
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		User user = playerLoader.fromPlayer(event.getPlayer());
		user.handleMove();
	}
}
