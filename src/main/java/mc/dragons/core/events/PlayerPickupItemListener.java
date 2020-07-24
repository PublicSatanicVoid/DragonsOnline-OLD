package mc.dragons.core.events;

import java.util.Arrays;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.item.Item;
import mc.dragons.core.gameobject.loader.ItemLoader;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.User;

public class PlayerPickupItemListener implements Listener {
	private Logger LOGGER = Dragons.getInstance().getLogger();
	//UserLoader userLoader;
	Dragons plugin;
	//ItemLoader itemLoader;
	
	public static final String GOLD_CURRENCY_DISPLAY_NAME = ChatColor.RESET + "" + ChatColor.GOLD + "Currency:Gold";
	
	public PlayerPickupItemListener(Dragons instance) {
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		plugin = instance;
		//itemLoader = (ItemLoader) GameObjectType.ITEM.<Item>getLoader();
	}
	
	@EventHandler
	public void onPlayerPickupItem(EntityPickupItemEvent event) {
		if(!(event.getEntity() instanceof Player)) return;
		Player player = (Player) event.getEntity();
		
		ItemStack pickup = event.getItem().getItemStack();	
		User user = UserLoader.fromPlayer(player);
		
		if(pickup == null) return;
		if(pickup.getItemMeta() == null) return;
		if(pickup.getItemMeta().getDisplayName() == null) return;
		
		Item item = ItemLoader.fromBukkit(pickup);
		
		LOGGER.finer("Pickup item event on " + player.getName() + " of " + (item == null ? "null" : item.getIdentifier()));
		
		if(item == null) return;
		
		if(pickup.getItemMeta().getDisplayName().equals(GOLD_CURRENCY_DISPLAY_NAME)) { // Both colors are necessary!
			int amount = pickup.getAmount();
			user.giveGold(amount * 1.0);
			new BukkitRunnable() {
				@Override
				public void run() {
					Arrays.asList(player.getInventory().getContents())
						.stream()
						.filter(i -> i != null)
						.filter(i -> i.getItemMeta() != null)
						.filter(i -> i.getItemMeta().getDisplayName() != null)
						.filter(i -> i.getItemMeta().getDisplayName().equals(GOLD_CURRENCY_DISPLAY_NAME))
						.forEach(i -> {
							player.getInventory().remove(i);
							plugin.getGameObjectRegistry().removeFromDatabase(item);
						});
				}
			}.runTaskLater(plugin, 1);
			return;
		}
		
		item.setQuantity(pickup.getAmount());
		user.giveItem(item, true, false, false);
//		for(int i = 0; i < pickup.getAmount(); i++) {
//			user.giveItem(item, true, true, false);
//		}
		event.setCancelled(true);
		event.getItem().remove();
	}
}
