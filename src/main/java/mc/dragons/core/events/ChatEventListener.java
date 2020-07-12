package mc.dragons.core.events;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.loader.FloorLoader;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.gameobject.user.User.PunishmentData;
import mc.dragons.core.gameobject.user.User.PunishmentType;
import mc.dragons.core.util.StringUtil;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Manages chat formatting.
 * 
 * @author Rick
 *
 */
public class ChatEventListener implements Listener {

	private Logger LOGGER = Dragons.getInstance().getLogger();
	//private UserLoader userLoader;
	
	public ChatEventListener() {
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		LOGGER.finer("Chat event from player " + event.getPlayer().getName());
		User user = UserLoader.fromPlayer(event.getPlayer());
		event.setCancelled(true);
		
		PunishmentData muteData = user.getActivePunishmentData(PunishmentType.MUTE);
		if(muteData != null) {
			user.getPlayer().sendMessage(ChatColor.RED + "You are muted!" + (muteData.getReason().equals("") ? "" : " (" + muteData.getReason() + ")"));
			user.getPlayer().sendMessage(ChatColor.RED + "Expires " + muteData.getExpiry().toString());
			return;
		}
		
		String messageSenderInfo = user.getLevelColor() + "" + user.getLevel() + " ";
		
		if(user.getRank().hasChatPrefix()) {
			messageSenderInfo += user.getRank().getChatPrefix() + " ";
		}
		
		messageSenderInfo += user.getRank().getNameColor() + user.getName();
		
		TextComponent message = new TextComponent(messageSenderInfo);
		message.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(ChatColor.YELLOW + "" + ChatColor.BOLD + user.getName() + "\n")
				.append(ChatColor.GRAY + "Rank: " + ChatColor.RESET + user.getRank().getNameColor() + user.getRank().getRankName() + "\n")
				.append(ChatColor.GRAY + "Level: " + user.getLevelColor() + user.getLevel() + "\n")
				.append(ChatColor.GRAY + "XP: " + ChatColor.RESET + user.getXP() + "\n")
				.append(ChatColor.GRAY + "Gold: " + ChatColor.RESET + user.getGold() + "\n")
				.append(ChatColor.GRAY + "Location: " + ChatColor.RESET + StringUtil.locToString(user.getPlayer().getLocation())
					+ ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + " (when message sent)\n") 
				.append(ChatColor.GRAY + "Floor: " + ChatColor.RESET + FloorLoader.fromWorld(user.getPlayer().getWorld()).getDisplayName()
					+ ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + " (when message sent)\n")
				.append(ChatColor.GRAY + "First Joined: " + ChatColor.RESET + user.getFirstJoined().toString())
				.create()));
		
		message.addExtra(ChatColor.GRAY + " » " + user.getRank().getChatColor() + event.getMessage());
		
		Bukkit.spigot().broadcast(message);
	}
}
