package mc.dragons.core.events;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.MetadataValue;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.item.Item;
import mc.dragons.core.gameobject.loader.GameObjectRegistry;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.npc.NPC;
import mc.dragons.core.gameobject.user.User;
import net.md_5.bungee.api.ChatColor;

public class EntityDeathEventListener implements Listener {

	private GameObjectRegistry registry;
	//private UserLoader userLoader;
	
	public EntityDeathEventListener(Dragons instance) {
		registry = instance.getGameObjectRegistry();
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
	}
	
	public static int getXPReward(int levelOfKiller, int levelOfVictim) {
		return Math.max(10, 10 * (1 + levelOfVictim - 2 * levelOfKiller / 3));
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		Player player = event.getEntity().getKiller();
		
		User user = UserLoader.fromPlayer(player);
		
		Entity target = event.getEntity();
		List<MetadataValue> handle = target.getMetadata("handle");
		if(handle.size() == 0) {
			return; // Unauthorized entity
		}
		NPC npc = (NPC) handle.get(0).value();
		npc.updateHealthBar(); // Show zero health

		registry.removeFromDatabase(npc);
		
		if(player == null) return;
		
		World world = npc.e().getWorld();
		Location loc = npc.e().getLocation();
		for(Item item : npc.getNPCClass().getLootTable().getDrops(loc)) {
			world.dropItem(loc, item.getItemStack());
		}
		
		int xpReward = getXPReward(user.getLevel(), npc.getLevel());
		user.sendActionBar("+ " + ChatColor.GREEN + xpReward + " XP");
		user.addXP(xpReward);
		
		user.updateQuests(event);
	}
	
}
