package mc.dragons.dragons.core.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.PlayerLoader;
import mc.dragons.dragons.core.gameobject.player.User;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand.EnumClientCommand;

/**
 * Event handler for deaths.
 * 
 * @author Rick
 *
 */
public class DeathEventListener implements Listener {
	private PlayerLoader playerLoader;
	private Dragons plugin;
	
	public DeathEventListener(Dragons instance) {
		playerLoader = (PlayerLoader)GameObjectType.PLAYER.getLoader();
		plugin = instance;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		final Player player = event.getEntity();
		final User user = playerLoader.fromPlayer(player);
		
		// Respawn the player in 10 seconds, show a customized death message
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			public void run() {
            	((CraftPlayer) player).getHandle().playerConnection.a(new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN));
            	user.sendTitle(ChatColor.DARK_RED, "You are dead.", ChatColor.RED, "Respawning in 10 seconds", 0, 20, 0);
				player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 10, 10, false, false), true);
				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 10, 10, false, false), true);
				player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 10, 10, false, false), true);
			}
		}, 1L);
	}
}
