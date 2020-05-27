package mc.dragons.dragons.core.events;

import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.MetadataValue;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.PlayerLoader;
import mc.dragons.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.dragons.core.gameobject.npc.NPC;
import mc.dragons.dragons.core.gameobject.player.SkillType;
import mc.dragons.dragons.core.gameobject.player.User;
import mc.dragons.dragons.core.gameobject.region.Region;

public class EntityDamageByEntityEventListener implements Listener {

	private PlayerLoader playerLoader;
	private RegionLoader regionLoader;
	
	public EntityDamageByEntityEventListener(Dragons instance) {
		playerLoader = (PlayerLoader) GameObjectType.PLAYER.getLoader();
		regionLoader = (RegionLoader) GameObjectType.REGION.getLoader();
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {

		Entity target = event.getEntity();
		
		User userDamager = null;
		NPC npcTarget = null;
		NPC npcDamager = null;
		if(event.getDamager() instanceof Player) {
			Player player = (Player) event.getDamager();
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
		
		Set<Region> regions = regionLoader.getRegionsByLocationXZ(target.getLocation());
		
		if(target instanceof Player) {
			if(userDamager == null && npcDamager != null) { // player was damaged by entity
				event.setDamage(event.getDamage() + 1.2 * npcDamager.getLevel());
			}
			else if(userDamager != null) {
				event.setDamage(event.getDamage() + Math.random() * userDamager.getSkillLevel(SkillType.MELEE));
			}
			User user = playerLoader.fromPlayer((Player)target);
			event.setDamage(Math.max(0.0, event.getDamage() - 0.5 * Math.random() * user.getSkillLevel(SkillType.DEFENSE)));
			if(event.getDamager() instanceof Player) { // PVP
				for(Region region : regions) {
					if(!Boolean.valueOf(region.getFlags().getString("pvp"))) {
						event.setCancelled(true);
						userDamager.sendActionBar(ChatColor.GRAY + "PVP is disabled in this region.");
						return;
					}
				}
			}
			else { // EVP = PVE
				for(Region region : regions) {
					if(!Boolean.valueOf(region.getFlags().getString("pve"))) {
						event.setCancelled(true);
						return;
					}
				}
			}
		}
		else if(npcTarget != null) { // PVE
			if(event.getDamager() instanceof Player) {
				for(Region region : regions) {
					if(!Boolean.valueOf(region.getFlags().getString("pve"))) {
						event.setCancelled(true);
						userDamager.sendActionBar(ChatColor.GRAY + "PVE is disabled in this region.");
						return;
					}
				}
			}
			npcTarget.updateHealthBar();
		}
	}
}
