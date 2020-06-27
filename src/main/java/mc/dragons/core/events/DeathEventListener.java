package mc.dragons.core.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.User;

/**
 * Event handler for deaths.
 * 
 * @author Rick
 *
 */
public class DeathEventListener implements Listener {
	//private UserLoader userLoader;
	private Dragons plugin;
	
	public DeathEventListener(Dragons instance) {
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		plugin = instance;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		final Player player = event.getEntity();
		final User user = UserLoader.fromPlayer(player);
		
		// Respawn the player in 10 seconds, show a customized death message
    	int countdown = plugin.getServerOptions().getDeathCountdown();
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
            	user.respawn();
            	user.sendTitle(ChatColor.DARK_RED, "You are dead.", ChatColor.RED, "Respawning on floor 1", 0, 20, 0);
            	user.setDeathCountdown(countdown);
            	user.sendToFloor("UndeadForest");
			}
		}, 1L);
	}
}
