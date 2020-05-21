package mc.dragons.dragons.core.gameobject.player;

import net.md_5.bungee.api.ChatColor;

/**
 * Player ranks. Ranks are purely cosmetic;
 * actual permissions are associated with
 * {@link mc.dragons.dragons.core.gameobject.player.PermissionLevel}.
 * 
 * @author Rick
 *
 */
public enum Rank {
	DEFAULT("", ChatColor.GRAY, ChatColor.GRAY),
	PATRON(ChatColor.DARK_AQUA + "PATRON", ChatColor.AQUA, ChatColor.WHITE),
	YOUTUBE(ChatColor.RED + "YOU" + ChatColor.WHITE + "TUBE", ChatColor.RED, ChatColor.WHITE),
	
	MODERATOR(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "MOD", ChatColor.GREEN, ChatColor.WHITE),
	GAME_MASTER(ChatColor.GOLD + "" + ChatColor.BOLD + "GM", ChatColor.YELLOW, ChatColor.WHITE),
	ADMIN(ChatColor.DARK_RED + "" + ChatColor.BOLD + "ADMIN", ChatColor.RED, ChatColor.WHITE),
	DEVELOPER(ChatColor.DARK_RED + "" + ChatColor.BOLD + "DEV", ChatColor.RED, ChatColor.WHITE),
	OPERATOR(ChatColor.DARK_RED + "" + ChatColor.BOLD + "SYSOP", ChatColor.RED, ChatColor.WHITE);
	
	private String chatPrefix;
	private ChatColor nameColor;
	private ChatColor chatColor;
	
	Rank(String chatPrefix, ChatColor nameColor, ChatColor chatColor) {
		this.chatPrefix = chatPrefix;
		this.nameColor = nameColor;
		this.chatColor = chatColor;
	}
	
	public boolean hasChatPrefix() {
		return this != DEFAULT;
	}
	
	public String getChatPrefix() {
		return chatPrefix;
	}
	
	public ChatColor getNameColor() {
		return nameColor;
	}
	
	public ChatColor getChatColor() {
		return chatColor;
	}
}