package mc.dragons.dragons.core.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.item.Item;
import mc.dragons.dragons.core.gameobject.loader.ItemLoader;
import mc.dragons.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.dragons.core.gameobject.player.User;

public class PlayerInteractListener implements Listener {
	
	private UserLoader userLoader;
	private ItemLoader itemLoader;
	
	public PlayerInteractListener() {
		userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		itemLoader = (ItemLoader) GameObjectType.ITEM.<Item>getLoader();
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		// Oops
	}
}
