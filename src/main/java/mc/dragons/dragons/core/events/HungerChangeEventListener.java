package mc.dragons.dragons.core.events;

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
		event.setFoodLevel(20);
	}
	
}
