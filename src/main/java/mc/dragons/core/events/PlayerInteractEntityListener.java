package mc.dragons.core.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import mc.dragons.core.gameobject.loader.NPCLoader;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.npc.NPC;
import mc.dragons.core.gameobject.npc.NPCConditionalActions.NPCTrigger;
import mc.dragons.core.gameobject.user.User;

public class PlayerInteractEntityListener implements Listener {
	
	//private UserLoader userLoader;
	//private ItemLoader itemLoader;
	
	public PlayerInteractEntityListener() {
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		//itemLoader = (ItemLoader) GameObjectType.ITEM.<Item>getLoader();
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEntityEvent event) {
		User user = UserLoader.fromPlayer(event.getPlayer());
		user.debug("Right-click");
		NPC npc = NPCLoader.fromBukkit(event.getRightClicked());
		if(npc != null) {
			npc.getNPCClass().executeConditionals(NPCTrigger.CLICK, user);
		}
		user.updateQuests(event);
	}
}
