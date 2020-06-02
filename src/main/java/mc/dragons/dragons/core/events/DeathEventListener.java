package mc.dragons.dragons.core.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.dragons.core.gameobject.player.User;

/**
 * Event handler for deaths.
 * 
 * @author Rick
 *
 */
public class DeathEventListener implements Listener {
	private UserLoader playerLoader;
	private Dragons plugin;
	
	public DeathEventListener(Dragons instance) {
		playerLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		plugin = instance;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		final Player player = event.getEntity();
		final User user = playerLoader.fromPlayer(player);
		
		// Respawn the player in 10 seconds, show a customized death message
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			public void run() {
            	user.respawn();
            	user.sendTitle(ChatColor.DARK_RED, "You are dead.", ChatColor.RED, "Respawning in 10 seconds", 0, 20, 0);
            	int countdown = plugin.getServerOptions().getDeathCountdown();
            	user.setDeathCountdown(countdown);
			}
		}, 1L);
	}
}
