package mc.dragons.core.events;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import mc.dragons.core.Dragons;

/**
 * Hunger is not a real thing in this RPG.
 * 
 * @author Rick
 *
 */
public class HungerChangeEventListener implements Listener {
	private Logger LOGGER = Dragons.getInstance().getLogger();

	@EventHandler
	public void onHungerChangeEvent(FoodLevelChangeEvent event) {
		LOGGER.finer("Hunger change event on " + event.getEntity().getName());
		event.setCancelled(true);
		Player player = (Player) event.getEntity();
		player.setFoodLevel(20);
	}
	
}
