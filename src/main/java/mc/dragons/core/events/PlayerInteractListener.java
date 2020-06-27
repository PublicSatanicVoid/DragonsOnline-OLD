package mc.dragons.core.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.User;

public class PlayerInteractListener implements Listener {
	
	//private UserLoader userLoader;
	//private ItemLoader itemLoader;
	
	public PlayerInteractListener() {
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		//itemLoader = (ItemLoader) GameObjectType.ITEM.<Item>getLoader();
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		User user = UserLoader.fromPlayer(event.getPlayer());
		user.updateQuests(event);
	}
}
