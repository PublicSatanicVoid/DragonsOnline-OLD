package mc.dragons.core.events;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.item.Item;
import mc.dragons.core.gameobject.item.ItemClass;
import mc.dragons.core.gameobject.loader.ItemClassLoader;
import mc.dragons.core.gameobject.loader.ItemLoader;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.User;

/**
 * Event handler for player joins. Takes care of user registration
 * and fetching user from database.
 * 
 * @author Rick
 *
 */
public class JoinEventListener implements Listener {
	private UserLoader userLoader;
	private ItemClassLoader itemClassLoader;
	private ItemLoader itemLoader;
	private Dragons plugin;
	
	private ItemClass[] defaultInventory;
	
	public JoinEventListener(Dragons instance) {
		userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		itemClassLoader = (ItemClassLoader) GameObjectType.ITEM_CLASS.<ItemClass>getLoader();
		itemLoader = (ItemLoader) GameObjectType.ITEM.<Item>getLoader();
		plugin = instance;
		
		defaultInventory = new ItemClass[] { itemClassLoader.getItemClassByClassName("LousyStick") };
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		UUID uuid = p.getUniqueId();
		User user = userLoader.loadObject(uuid);
		if(user == null) {
			plugin.getLogger().info("Player " + p.getName() + " joined for the first time");
			user = userLoader.registerNew(p);
			user.sendToFloor("UndeadForest");
			for(ItemClass itemClass : defaultInventory) {
				user.giveItem(itemLoader.registerNew(itemClass), true, false, true);
			}
		}
		user.handleJoin();
		event.setJoinMessage(null);
	}
}
	