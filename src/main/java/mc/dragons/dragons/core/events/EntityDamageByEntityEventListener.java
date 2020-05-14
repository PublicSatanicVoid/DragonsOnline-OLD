package mc.dragons.dragons.core.events;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.MetadataValue;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.PlayerLoader;
import mc.dragons.dragons.core.gameobject.npc.NPC;
import mc.dragons.dragons.core.gameobject.player.SkillType;
import mc.dragons.dragons.core.gameobject.player.User;

public class EntityDamageByEntityEventListener implements Listener {

	private PlayerLoader playerLoader;
	
	public EntityDamageByEntityEventListener(Dragons instance) {
		playerLoader = (PlayerLoader)GameObjectType.PLAYER.getLoader();
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {

		Entity target = event.getEntity();
		
		User userDamager = null;
		NPC npcTarget = null;
		NPC npcDamager = null;
		if(event.getDamager() instanceof Player) {
			Player player = (Player) event.getDamager();
			if(player == null) return;
			userDamager = playerLoader.fromPlayer(player);
			event.setDamage(event.getDamage() + userDamager.getSkillLevel(SkillType.MELEE));
		}
		else {
			List<MetadataValue> handle = event.getDamager().getMetadata("handle");
			if(handle.size() > 0) {
				npcDamager = (NPC) handle.get(0).value();
			}
		}
		
		List<MetadataValue> handle = target.getMetadata("handle");
		if(handle.size() > 0) {
			npcTarget = (NPC) handle.get(0).value();
		}
		
		if(target instanceof Player) {
			if(userDamager == null && npcDamager != null) { // player was damaged by entity
				event.setDamage(event.getDamage() + 1.2 * npcDamager.getLevel());
			}
			else if(userDamager != null){
				event.setDamage(event.getDamage() + userDamager.getSkillLevel(SkillType.MELEE));
			}
			User user = playerLoader.fromPlayer((Player)target);
			event.setDamage(Math.max(0.0, event.getDamage() - 0.5 * user.getSkillLevel(SkillType.DEFENSE)));
		}
		else if(npcTarget != null){
			npcTarget.updateHealthBar();
		}
	}
}
