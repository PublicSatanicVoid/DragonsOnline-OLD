package mc.dragons.core.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import mc.dragons.core.gameobject.item.Item;
import mc.dragons.core.gameobject.loader.ItemLoader;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.User;

public class PlayerDropItemListener implements Listener {

	//UserLoader userLoader;
	//ItemLoader itemLoader;
	
	public PlayerDropItemListener() {
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		//itemLoader = (ItemLoader) GameObjectType.ITEM.<Item>getLoader();
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		ItemStack drop = event.getItemDrop().getItemStack();
		Item item = ItemLoader.fromBukkit(drop);
		if(item == null) return;
		User user = UserLoader.fromPlayer(event.getPlayer());
		user.takeItem(item);
	}
	
}
