package mc.dragons.core.events;

import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.loader.NPCLoader;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.npc.NPC;
import mc.dragons.core.gameobject.npc.NPC.NPCType;
import mc.dragons.core.gameobject.region.Region;
import mc.dragons.core.gameobject.user.User;

public class EntityTargetEventListener implements Listener {

	Logger LOGGER;
	
	public EntityTargetEventListener(Dragons instance) {
		LOGGER = instance.getLogger();
	}
	
	@EventHandler
	public void onEntityTarget(EntityTargetEvent e) {
		if(e.getTarget() instanceof Player) {
			Player p = (Player) e.getTarget();
			LOGGER.finest("Entity target event on " + p.getName());
			User user = UserLoader.fromPlayer(p);
			NPC npc = NPCLoader.fromBukkit(e.getEntity());
			if(npc != null) {
				if(npc.getNPCType() != NPCType.HOSTILE) {
					LOGGER.finest(" - Cancelled due to mob non-hostility");
					e.setCancelled(true);
					return;
				}
			}
			if(user.isGodMode() || user.isVanished() || user.hasActiveDialogue()) {
				LOGGER.finest(" - Cancelled due to state");
				e.setCancelled(true);
				return;
			}
			Set<Region> regions = user.getRegions();
			for(Region r : regions) {
				if(!Boolean.valueOf(r.getFlags().getString("pve"))) {
					LOGGER.finest("- Cancelled due to region");
					e.setCancelled(true);
					return;
				}
			}
		}
	}
	
}
