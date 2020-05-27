package mc.dragons.dragons.core.events;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.MetadataValue;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.GameObjectRegistry;
import mc.dragons.dragons.core.gameobject.loader.PlayerLoader;
import mc.dragons.dragons.core.gameobject.npc.NPC;
import mc.dragons.dragons.core.gameobject.player.User;
import net.md_5.bungee.api.ChatColor;

public class EntityDeathEventListener implements Listener {

	private GameObjectRegistry registry;
	private PlayerLoader playerLoader;
	
	public EntityDeathEventListener(Dragons instance) {
		registry = instance.getGameObjectRegistry();
		playerLoader = (PlayerLoader)GameObjectType.PLAYER.getLoader();
	}
	
	public static int getXPReward(int levelOfKiller, int levelOfVictim) {
		return Math.max(10, 10 * (1 + levelOfVictim - 2 * levelOfKiller / 3));
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		Player player = event.getEntity().getKiller();
		
		User user = playerLoader.fromPlayer(player);
		
		Entity target = event.getEntity();
		List<MetadataValue> handle = target.getMetadata("handle");
		if(handle.size() == 0) {
			return; // Unauthorized entity
		}
		NPC npc = (NPC)handle.get(0).value();
		npc.updateHealthBar(); // Show zero health

		registry.removeFromDatabase(npc);
		
		if(player == null) return;
		
		int xpReward = getXPReward(user.getLevel(), npc.getLevel());
		user.sendActionBar("+ " + ChatColor.GREEN + xpReward + " XP");
		user.addXP(xpReward);
	}
	
}
