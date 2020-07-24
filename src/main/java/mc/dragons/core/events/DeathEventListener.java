package mc.dragons.core.events;

import java.util.logging.Logger;

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
	private Logger LOGGER = Dragons.getInstance().getLogger();
	
	public DeathEventListener(Dragons instance) {
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		plugin = instance;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		LOGGER.finer("Death event from " + event.getEntity().getName());
		final Player player = event.getEntity();
		final User user = UserLoader.fromPlayer(player);
		
		player.sendMessage(ChatColor.DARK_RED + "You died!");
		
    	int countdown = plugin.getServerOptions().getDeathCountdown();
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
            	user.respawn();
            	user.sendTitle(ChatColor.DARK_RED, "You are dead.", ChatColor.RED, "Respawning on floor 1", 0, countdown, 0);
            	user.setDeathCountdown(countdown);
            	user.sendToFloor("BeginnerTown");
			}
		}, 1L);
	}
}
