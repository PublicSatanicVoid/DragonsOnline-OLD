package mc.dragons.core.events;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.item.Item;
import mc.dragons.core.gameobject.loader.ItemLoader;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.User;

public class PlayerDropItemListener implements Listener {
	private Logger LOGGER = Dragons.getInstance().getLogger();

	//UserLoader userLoader;
	ItemLoader itemLoader;
	
	public PlayerDropItemListener() {
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		itemLoader = (ItemLoader) GameObjectType.ITEM.<Item>getLoader();
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		ItemStack drop = event.getItemDrop().getItemStack();
		Item item = ItemLoader.fromBukkit(drop);
		LOGGER.finer("Drop item event on " + event.getPlayer().getName() + " of " + (item == null ? "null" : item.getIdentifier()));
		if(item == null) return;
		User user = UserLoader.fromPlayer(event.getPlayer());
		if(item.isUndroppable()) {
			user.sendActionBar(ChatColor.DARK_RED + "You can't drop this item!");
			event.setCancelled(true);
			return;
		}
		event.getItemDrop().setItemStack(itemLoader.registerNew(item).getItemStack());
		user.takeItem(item, 1, true, false, true);
	}
	
}
