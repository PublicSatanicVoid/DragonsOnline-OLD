package mc.dragons.dragons.core.gameobject;

import net.md_5.bungee.api.ChatColor;

/**
 * Utility class for dealing with health bars.
 * 
 * @author Rick
 *
 */
public class HealthBarUtil {
	public static final int HEALTH_BARS = 10;
	public static final String HEALTH_BAR_PIECE = "/";
	public static final ChatColor FULL_COLOR = ChatColor.GREEN;
	public static final ChatColor WARNING_COLOR = ChatColor.RED;
	public static final ChatColor EMPTY_COLOR = ChatColor.GRAY;
	public static final int WARNING_THRESHOLD = 4;
	
	public static String getHealthBar(double health, double max) {
		String healthBar = "";
		double ratio = health / max;
		int full = (int) Math.floor(ratio * HEALTH_BARS);
		ChatColor activeColor = full > WARNING_THRESHOLD ? FULL_COLOR : WARNING_COLOR;
		for(int i = 0; i < HEALTH_BARS; i++) {
			if(i < full) healthBar += activeColor + HEALTH_BAR_PIECE;
			else healthBar += EMPTY_COLOR + HEALTH_BAR_PIECE;
		}
		return healthBar;
	}
}
