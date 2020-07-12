package mc.dragons.core.events;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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
import mc.dragons.core.gameobject.user.User.PunishmentData;
import mc.dragons.core.gameobject.user.User.PunishmentType;

/**
 * Event handler for player joins. Takes care of user registration
 * and fetching user from database.
 * 
 * @author Rick
 *
 */
public class JoinEventListener implements Listener {
	private Logger LOGGER = Dragons.getInstance().getLogger();
	
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
		LOGGER.info("Join event on " + event.getPlayer().getName());
		
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		User user = userLoader.loadObject(uuid);
		event.setJoinMessage(null);
		if(user == null) {
			plugin.getLogger().info("Player " + player.getName() + " joined for the first time");
			user = userLoader.registerNew(player);
			user.sendToFloor("UndeadForest");
			for(ItemClass itemClass : defaultInventory) {
				user.giveItem(itemLoader.registerNew(itemClass), true, false, true);
			}
		}
		PunishmentData banData = user.getActivePunishmentData(PunishmentType.BAN);
		if(banData != null) {
			player.kickPlayer(ChatColor.DARK_RED + "" + ChatColor.BOLD + "You are banned.\n\n"
					+ (banData.getReason().equals("") ? "" : ChatColor.GRAY + "Reason: " + ChatColor.WHITE + banData.getReason() + "\n")
					+ ChatColor.GRAY + "Expires: " + ChatColor.WHITE + (banData.isPermanent() ? "Never" : banData.getExpiry().toString()));
			return;
		}
		
		user.handleJoin();
		player.setGameMode(GameMode.ADVENTURE);
	}
}
	