package mc.dragons.core.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

/**
 * Hunger is not a real thing in this RPG.
 * 
 * @author Rick
 *
 */
public class HungerChangeEventListener implements Listener {

	@EventHandler
	public void onHungerChangeEvent(FoodLevelChangeEvent event) {
		event.setCancelled(true);
		Player player = (Player) event.getEntity();
		player.setFoodLevel(20);
	}
	
}
