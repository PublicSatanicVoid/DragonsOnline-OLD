package mc.dragons.dragons.core.events;

import java.util.Set;

import org.bukkit.entity.Entity;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.dragons.core.gameobject.npc.NPC;
import mc.dragons.dragons.core.gameobject.region.Region;

public class EntityMoveListener extends PacketAdapter {
	private RegionLoader regionLoader;
	
	public EntityMoveListener(Dragons instance) {
		super(instance, PacketType.Play.Server.REL_ENTITY_MOVE, PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);
		regionLoader = (RegionLoader) GameObjectType.REGION.<Region>getLoader();
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
	}
}
