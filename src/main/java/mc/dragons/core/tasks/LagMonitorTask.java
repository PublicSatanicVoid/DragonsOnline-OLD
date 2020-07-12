package mc.dragons.core.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.scheduler.BukkitRunnable;

import mc.dragons.core.Dragons;

public class LagMonitorTask extends BukkitRunnable {
	
	public static final double TPS_RECORD_LENGTH = 3000;
	public static final double TPS_WARN_THRESHOLD = 16;
	public static Logger LOGGER = Dragons.getInstance().getLogger();
	
	private List<Double> tpsRecord = new ArrayList<>();
	
	public List<Double> getTPSRecord() {
		return tpsRecord;
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				Thread.sleep(1000);
				double tps = LagMeter.getRoundedTPS();
				if (tpsRecord.size() < TPS_RECORD_LENGTH) { // Save TPS for last 30 minutes
				} else {
					tpsRecord.remove(0);
				}
				tpsRecord.add(tps);
				if(tps <= TPS_WARN_THRESHOLD) {
					LOGGER.warning("TPS is unusually low! (" + tps + ")");
				}
			}
		} catch (Exception ignored) {}
	}
}
