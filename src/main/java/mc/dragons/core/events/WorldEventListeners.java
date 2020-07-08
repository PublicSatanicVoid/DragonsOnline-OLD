package mc.dragons.core.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;

public class WorldEventListeners implements Listener {

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onLeavesDecay(LeavesDecayEvent event) {
		event.getBlock().setData((byte) (event.getBlock().getData() + 6));
		event.setCancelled(true);
	}
	
}
