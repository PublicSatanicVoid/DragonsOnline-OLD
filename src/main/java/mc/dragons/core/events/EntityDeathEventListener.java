package mc.dragons.core.events;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.item.Item;
import mc.dragons.core.gameobject.loader.GameObjectRegistry;
import mc.dragons.core.gameobject.loader.NPCLoader;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.npc.NPC;
import mc.dragons.core.gameobject.npc.NPC.NPCType;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.util.StringUtil;

public class EntityDeathEventListener implements Listener {
	private Logger LOGGER = Dragons.getInstance().getLogger();

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
		LOGGER.finer("Death event on " + StringUtil.entityToString(event.getEntity()));
		Player player = event.getEntity().getKiller();
		
		User user = UserLoader.fromPlayer(player);
		
		Entity target = event.getEntity();
		
		for(Entity passenger : new ArrayList<>(target.getPassengers())) {
			target.removePassenger(passenger);
			passenger.remove();
		}
		
		NPC npc = NPCLoader.fromBukkit(target);
		if(npc == null) return;
		
		if(npc.isImmortal()) {
			npc.setEntity(target.getLocation().getWorld().spawnEntity(target.getLocation(), npc.getEntity().getType()));
			npc.initializeEntity();
		}
		
		npc.updateHealthBar(); // Show zero health
		registry.removeFromDatabase(npc);
		
		npc.getNPCClass().handleDeath(npc);
		
		if(player == null) return;
		
		Location loc = user.getPlayer().getLocation();
		World world = loc.getWorld();
		
		for(Item item : npc.getNPCClass().getLootTable().getDrops(loc)) {
			//player.sendMessage("- entity dropped " + item.getName());
			world.dropItem(loc, item.getItemStack());
		}
		
		if(npc.getNPCType() == NPCType.HOSTILE) { 
			int xpReward = getXPReward(user.getLevel(), npc.getLevel());
			user.sendActionBar("+ " + ChatColor.GREEN + xpReward + " XP");
			user.addXP(xpReward);
		}
		
		user.updateQuests(event);
	}
	
}
