package mc.dragons.core.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.gameobject.user.User.PunishmentData;
import mc.dragons.core.gameobject.user.User.PunishmentType;

/**
 * Manages chat formatting.
 * 
 * @author Rick
 *
 */
public class ChatEventListener implements Listener {

	//private UserLoader userLoader;
	
	public ChatEventListener() {
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		User user = UserLoader.fromPlayer(event.getPlayer());
		event.setCancelled(true);
		
		PunishmentData muteData = user.getActivePunishmentData(PunishmentType.MUTE);
		if(muteData != null) {
			user.getPlayer().sendMessage(ChatColor.RED + "You are muted!" + (muteData.getReason().equals("") ? "" : " (" + muteData.getReason() + ")"));
			user.getPlayer().sendMessage(ChatColor.RED + "Expires " + muteData.getExpiry().toString());
			return;
		}
		
		String message = user.getLevelColor() + "" + user.getLevel() + " ";
		
		if(user.getRank().hasChatPrefix()) {
			message += user.getRank().getChatPrefix() + " ";
		}
		
		message += user.getRank().getNameColor() + user.getPlayer().getName() + ChatColor.GRAY + " » ";
		message += user.getRank().getChatColor() + event.getMessage();
		
		Bukkit.broadcastMessage(message);
	}
}
