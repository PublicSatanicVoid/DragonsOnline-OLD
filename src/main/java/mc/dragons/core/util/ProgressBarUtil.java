package mc.dragons.core.util;

import org.bukkit.ChatColor;

/**
 * Utility class for dealing with health bars.
 * 
 * @author Rick
 *
 */
public class ProgressBarUtil {
	public static final int HEALTH_BARS = 30;
	public static final String HEALTH_BAR_PIECE = "|";
	public static final ChatColor FULL_COLOR = ChatColor.DARK_GREEN;
	public static final ChatColor HIGH_COLOR = ChatColor.GREEN;
	public static final ChatColor WARNING_COLOR = ChatColor.RED;
	public static final ChatColor CRITICAL_COLOR = ChatColor.DARK_RED;
	public static final ChatColor EMPTY_COLOR = ChatColor.GRAY;
	public static final ChatColor NEUTRAL_COLOR = ChatColor.GRAY;
	public static final int HIGH_THRESHOLD = 25;
	public static final int WARNING_THRESHOLD = 10;
	public static final int CRITICAL_THRESHOLD = 4;
	
	public static final int PROGRESS_BARS = 40;
	public static final String PROGRESS_BAR_PIECE = "|";
	
	
	public static String getHealthBar(double health, double max) {
		String healthBar = "";
		double ratio = health / max;
		int full = (int) Math.ceil(ratio * HEALTH_BARS);
		ChatColor activeColor = full > HIGH_THRESHOLD ? FULL_COLOR : full > WARNING_THRESHOLD ? HIGH_COLOR : full > CRITICAL_THRESHOLD ? WARNING_COLOR : CRITICAL_COLOR;
		for(int i = 0; i < HEALTH_BARS; i++) {
			if(i < full) healthBar += activeColor + HEALTH_BAR_PIECE;
			else healthBar += EMPTY_COLOR + HEALTH_BAR_PIECE;
		}
		return healthBar;
	}
	
	
	public static String getCountdownBar(double percent) {
		String progressBar = "";
		int full = (int) Math.ceil(percent * PROGRESS_BARS);
		for(int i = 0; i < PROGRESS_BARS; i++) {
			if(i < full) progressBar += NEUTRAL_COLOR + PROGRESS_BAR_PIECE;
			else progressBar += FULL_COLOR + PROGRESS_BAR_PIECE;
		}
		return progressBar;
	}
}
