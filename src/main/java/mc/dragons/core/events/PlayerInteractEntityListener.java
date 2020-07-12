package mc.dragons.core.events;

import java.util.logging.Logger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.loader.NPCLoader;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.npc.NPC;
import mc.dragons.core.gameobject.npc.NPCConditionalActions.NPCTrigger;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.util.StringUtil;

public class PlayerInteractEntityListener implements Listener {
	private Logger LOGGER = Dragons.getInstance().getLogger();
	
	//private UserLoader userLoader;
	//private ItemLoader itemLoader;
	
	public PlayerInteractEntityListener() {
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		//itemLoader = (ItemLoader) GameObjectType.ITEM.<Item>getLoader();
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEntityEvent event) {
		LOGGER.finer("Interact entity event on " + event.getPlayer().getName() + " to " + StringUtil.entityToString(event.getRightClicked()));
		User user = UserLoader.fromPlayer(event.getPlayer());
		user.debug("Right-click");
		NPC npc = NPCLoader.fromBukkit(event.getRightClicked());
		if(npc != null) {
			npc.getNPCClass().executeConditionals(NPCTrigger.CLICK, user);
		}
		user.updateQuests(event);
	}
}
