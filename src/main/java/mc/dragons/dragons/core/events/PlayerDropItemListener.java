package mc.dragons.dragons.core.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.item.Item;
import mc.dragons.dragons.core.gameobject.loader.ItemLoader;
import mc.dragons.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.dragons.core.gameobject.player.User;

public class PlayerDropItemListener implements Listener {

	UserLoader userLoader;
	ItemLoader itemLoader;
	
	public PlayerDropItemListener() {
		userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		itemLoader = (ItemLoader) GameObjectType.ITEM.<Item>getLoader();
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		ItemStack drop = event.getItemDrop().getItemStack();
		Item item = itemLoader.getItemByItemStack(drop);
		if(item == null) return;
		User user = userLoader.fromPlayer(event.getPlayer());
		
		user.takeItem(item);
	}
	
}
