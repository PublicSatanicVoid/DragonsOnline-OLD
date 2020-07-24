package mc.dragons.core.commands;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.PermissionLevel;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.tasks.LagMeter;
import mc.dragons.core.util.MathUtil;
import mc.dragons.core.util.PermissionUtil;

public class LagCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = null;
		User user = null;
		
		if(sender instanceof Player) {
			player = (Player) sender;
			user = UserLoader.fromPlayer(player);
			if(!PermissionUtil.verifyActivePermissionLevel(user, PermissionLevel.ADMIN, true)) return true;
		}
		
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("-gc")) {
				sender.sendMessage(ChatColor.GREEN + "Running GC...");
				Runtime.getRuntime().gc();
				sender.sendMessage(ChatColor.GREEN + "... Complete!");
				return true;
			}
		}
		
		sender.sendMessage(ChatColor.GREEN + "Showing lag data.");
		List<Double> tpsRecord = Dragons.getInstance().getTPSRecord();
		final int[] tps_thresholds = new int[] { 5, 10, 15, 18, 19 };
		String[] graph = new String[tps_thresholds.length];
		int time_back = 900; // 900s = 15min
		int time_step = 30;
		if (time_back > tpsRecord.size()) {
			while (time_back > tpsRecord.size()) {
				time_back -= time_step;
			}
			if (time_back == 0) {
				sender.sendMessage(ChatColor.GRAY + "Not enough data points to show TPS graph.");
			}
		}
		if (time_back != 0) {
			sender.sendMessage(ChatColor.GREEN + "TPS Graph:" + ChatColor.GRAY + " (Last "
					+ ((double) time_back / 60) + " mins, intervals of " + ((double) time_step / 60)
					+ " mins)");
		}
		int i = 0;
		for (int tps_threshold : tps_thresholds) {
			graph[i] = ChatColor.GRAY + (tps_threshold >= 10 ? "" : "0") + tps_threshold + " ";
			for (int j = time_back; j > 0; j -= time_step) {
				double sum = 0.0D;
				int count = 0;
				for (int k = j; k > j - time_step; k--) {
					count++;
					sum += tpsRecord.get(tpsRecord.size() - k);
				}
				double avg = sum / count;
				if (avg >= tps_threshold) {
					graph[i] += LagMeter.getTPSColor(avg) + "#";
				} else if (i == 0 && avg < 5.0D) {
					graph[i] += ChatColor.DARK_RED + "_";
				} else {
					if (player != null)
						graph[i] += ChatColor.DARK_GRAY + "#";
					else
						graph[i] += " ";
				}
			}
			i++;
		}

		ArrayUtils.reverse(graph);
		if (time_back != 0) {
			for (String line : graph) {
				sender.sendMessage(line);
			}
		}
		int lastSpike = -1;
		double minTPS = 20.0;
		double maxTPS = 0;
		double totalTPS = 0;
		int count = 0;
		for (i = 0; i < tpsRecord.size(); i++, count++) {
			if (tpsRecord.get(i) < 17) {
				lastSpike = i;
			}
			if (tpsRecord.get(i) < minTPS) {
				minTPS = tpsRecord.get(i);
			}
			if (tpsRecord.get(i) > maxTPS) {
				maxTPS = tpsRecord.get(i);
			}
			totalTPS += tpsRecord.get(i);
		}
		if (lastSpike != -1) {
			lastSpike = tpsRecord.size() - lastSpike;
			sender.sendMessage(ChatColor.GREEN + "Last Spike: " + ChatColor.GRAY + "" + lastSpike
					+ "s ago");
		} else {
			sender.sendMessage(ChatColor.GREEN + "Last Spike: " + ChatColor.GRAY + "> "
					+ (tpsRecord.size() / 60) + " minutes ago");
		}
		double avgTPS = MathUtil.round(totalTPS / count, 2);
		sender.sendMessage(ChatColor.GREEN + "Min TPS, Avg TPS, Max TPS: " + ChatColor.GRAY + ""
				+ minTPS + ", " + avgTPS + ", " + maxTPS);
		sender.sendMessage(ChatColor.GREEN + "Current TPS: " + ChatColor.GRAY + ""
				+ LagMeter.getRoundedTPS());
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("-verbose") || args[0].equalsIgnoreCase("-v")) {
				sender.sendMessage(ChatColor.GREEN + "Free Memory / Total Memory: " + ChatColor.GRAY
						+ MathUtil.round(Runtime.getRuntime().freeMemory() / Math.pow(10, 6)) + "MB / "
						+ MathUtil.round(Runtime.getRuntime().totalMemory() / Math.pow(10, 6)) + "MB ("
						+ MathUtil.round(100 * (double) Runtime.getRuntime().freeMemory()
								/ Runtime.getRuntime().totalMemory())
						+ "%)");
				sender.sendMessage(ChatColor.GREEN + "CPU Cores: " + ChatColor.GRAY + ""
						+ Runtime.getRuntime().availableProcessors() + ChatColor.GREEN + " - Chunks: "
						+ ChatColor.GRAY + "" + Dragons.getInstance().getLoadedChunks().size() + ChatColor.GREEN
						+ " - Entities: " + ChatColor.GRAY + ""
						+ Dragons.getInstance().getEntities().size());
			}
		}
	
		return true;
	}

}