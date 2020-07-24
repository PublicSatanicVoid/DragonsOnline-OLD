package mc.dragons.core.events;

import java.util.logging.Logger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.util.StringUtil;

public class MoveEventListener implements Listener {
	private Logger LOGGER = Dragons.getInstance().getLogger();

	//private UserLoader userLoader;
	
	public MoveEventListener() {
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		LOGGER.finest("Move event on " + event.getPlayer().getName() + " (" + StringUtil.locToString(event.getFrom()) + " [" + event.getFrom().getWorld().getName() + "] -> "
				+ StringUtil.locToString(event.getTo()) + " [" + event.getTo().getWorld().getName() + "])");
		User user = UserLoader.fromPlayer(event.getPlayer());
		if(user.hasDeathCountdown()) {
			event.setTo(event.getFrom());
			return;
		}
		user.handleMove();
	}
}
