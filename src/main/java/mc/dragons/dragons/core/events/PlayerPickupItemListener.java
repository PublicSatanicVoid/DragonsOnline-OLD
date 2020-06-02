package mc.dragons.dragons.core.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.item.Item;
import mc.dragons.dragons.core.gameobject.loader.ItemLoader;
import mc.dragons.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.dragons.core.gameobject.player.User;

public class PlayerPickupItemListener implements Listener {
	UserLoader userLoader;
	ItemLoader itemLoader;
	
	public PlayerPickupItemListener() {
		userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		itemLoader = (ItemLoader) GameObjectType.ITEM.<Item>getLoader();
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		ItemStack pickup = event.getItem().getItemStack();
		Item item = itemLoader.getItemByItemStack(pickup);
		if(item == null) return;
		User user = userLoader.fromPlayer(event.getPlayer());
		user.giveItem(item, true, true, false);
	}
}
