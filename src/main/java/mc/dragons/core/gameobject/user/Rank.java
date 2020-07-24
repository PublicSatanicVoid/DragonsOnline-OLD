package mc.dragons.core.gameobject.user;

import org.bukkit.ChatColor;

/**
 * Player ranks. Ranks are purely cosmetic;
 * actual permissions are associated with
 * {@link mc.dragons.core.gameobject.user.PermissionLevel}.
 * 
 * @author Rick
 *
 */
public enum Rank {
	DEFAULT("Default", "", ChatColor.GRAY, ChatColor.GRAY),
	PATRON("Patron", ChatColor.DARK_AQUA + "[PATRON]", ChatColor.AQUA, ChatColor.WHITE),
	PATRON_PLUS("Patron+", ChatColor.DARK_AQUA + "[PATRON" + ChatColor.YELLOW + "+" + ChatColor.DARK_AQUA + "]", ChatColor.AQUA, ChatColor.WHITE),
	INVESTOR("Investor", ChatColor.GOLD + "[INVESTOR]", ChatColor.YELLOW, ChatColor.WHITE),
	YOUTUBE("YouTuber", ChatColor.RED + "[YOU" + ChatColor.WHITE + "TUBE" + ChatColor.RED + "]", ChatColor.RED, ChatColor.WHITE),
	MEDIA("Media", ChatColor.DARK_PURPLE + "[MEDIA]", ChatColor.LIGHT_PURPLE, ChatColor.WHITE),
	
	CONTENT_TEAM("Content Team", ChatColor.BLUE + "[CONTENT]", ChatColor.BLUE, ChatColor.WHITE),
	BUILDER("Builder", ChatColor.BLUE + "[BUILDER]", ChatColor.BLUE, ChatColor.WHITE),
	BUILDER_CMD("Builder + CMD", ChatColor.BLUE + "[BUILDER" + ChatColor.DARK_PURPLE + "+CMD" + ChatColor.BLUE + "]", ChatColor.BLUE, ChatColor.WHITE),
	MODERATOR("Moderator", ChatColor.DARK_GREEN + "[MOD]", ChatColor.GREEN, ChatColor.WHITE),
	GAME_MASTER("Game Master", ChatColor.DARK_RED + "[GM]", ChatColor.RED, ChatColor.WHITE),
	ADMIN("Admin", ChatColor.DARK_RED + "[ADMIN]", ChatColor.RED, ChatColor.WHITE),
	DEVELOPER("Developer", ChatColor.DARK_RED + "[DEV]", ChatColor.RED, ChatColor.WHITE),
	SYS_OPERATOR("System Operator", ChatColor.DARK_RED + "[SYS" + ChatColor.GOLD + "OP" + ChatColor.DARK_RED + "]", ChatColor.RED, ChatColor.WHITE),
	DEV_OPERATOR("Dev Operator", ChatColor.DARK_RED + "[DEV" + ChatColor.GOLD + "OP" + ChatColor.DARK_RED + "]", ChatColor.RED, ChatColor.WHITE);
	
	private String rankName;
	private String chatPrefix;
	private ChatColor nameColor;
	private ChatColor chatColor;
	
	Rank(String rankName, String chatPrefix, ChatColor nameColor, ChatColor chatColor) {
		this.rankName = rankName;
		this.chatPrefix = chatPrefix;
		this.nameColor = nameColor;
		this.chatColor = chatColor;
	}
	
	public String getRankName() {
		return rankName;
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
