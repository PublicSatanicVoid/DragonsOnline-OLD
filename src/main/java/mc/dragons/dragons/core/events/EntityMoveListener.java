package mc.dragons.dragons.core.events;

import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.dragons.core.gameobject.npc.NPC;
import mc.dragons.dragons.core.gameobject.player.User;
import mc.dragons.dragons.core.gameobject.region.Region;

public class EntityMoveListener extends PacketAdapter {
	private RegionLoader regionLoader;
	private UserLoader userLoader;
	
	public EntityMoveListener(Dragons instance) {
		super(instance, PacketType.Play.Server.REL_ENTITY_MOVE, PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);
		regionLoader = (RegionLoader) GameObjectType.REGION.<Region>getLoader();
		userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		Entity entity = event.getPacket().getEntityModifier(event.getPlayer().getWorld()).read(0);
		if(!entity.hasMetadata("handle")) {
			return;
		}
		NPC npc = (NPC) entity.getMetadata("handle").get(0).value();
		if(npc.isHostile()) {
			Set<Region> regions = regionLoader.getRegionsByLocationXZ(entity.getLocation());
			for(Region region : regions) {
				if(!Boolean.valueOf(region.getFlags().getString("allowhostile"))) {
					npc.remove();
				}
			}
		}
		for(Entity e : entity.getNearbyEntities(2.0, 2.0, 2.0)) {
			if(e instanceof Player) {
				User user = userLoader.fromPlayer((Player) e);
				if(user.hasDeathCountdown()) {
					entity.setVelocity(entity.getLocation().toVector().subtract(e.getLocation().toVector()).normalize().multiply(5.0));
				}
			}
		}
	}
}
