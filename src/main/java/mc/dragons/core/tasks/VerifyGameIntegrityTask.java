package mc.dragons.core.tasks;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import mc.dragons.core.Dragons;

public class VerifyGameIntegrityTask extends BukkitRunnable {

	private Dragons plugin;
	
	public VerifyGameIntegrityTask(Dragons instance) {
		plugin = instance;
	}
	
	@Override
	public void run() {
		if(plugin.getServerOptions().isVerifyIntegrityEnabled()) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "verifygameintegrity -resolve -silent");	
		}
	}
}
