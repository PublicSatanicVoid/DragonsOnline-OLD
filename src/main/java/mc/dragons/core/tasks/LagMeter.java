package mc.dragons.core.tasks;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 
 * https://bukkit.org/threads/get-server-tps.143410/
 * 
 * @author LazyLemons
 *
 */
public class LagMeter extends BukkitRunnable {
	public static int tickCount = 0;
	public static long[] ticksArray = new long[600];
	public static long lastTick = 0L;

	public static double getTPS() {
		return getTPS(100);
	}

	public static double getLag() {
		return Math.round((1.0D - getTPS() / 20.0D) * 100.0D);
	}

	public static double getTPS(int ticks) {
		if (tickCount < ticks) {
			return 20.0D;
		}
		int target = (tickCount - 1 - ticks) % ticksArray.length;
		if (ticksArray.length > target) {
			try {
				long elapsed = System.currentTimeMillis() - ticksArray[target];
				return ticks / (elapsed / 1000.0D);
			} catch (Exception e) {
				return 20.0D;
			}
		}
		return 20.0D;
	}

	public static double getRoundedTPS() {
		return Math.round(getTPS() * 100.0) / 100.0;
	}

	public static long getElapsed(int tickID) {
		if (tickCount - tickID >= ticksArray.length) {
		}

		long time = ticksArray[(tickID % ticksArray.length)];
		return System.currentTimeMillis() - time;
	}

	@Override
	public void run() {
		ticksArray[(tickCount % ticksArray.length)] = System.currentTimeMillis();

		tickCount += 1;
	}

	public static String getTPSColor(double tps) {
		if (tps >= 18) {
			return ChatColor.DARK_GREEN + "";
		} else if (tps >= 15) {
			return ChatColor.GOLD + "";
		} else {
			return ChatColor.DARK_RED + "";
		}
	}
}
