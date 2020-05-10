package mc.dragons.dragons.core;

import org.bukkit.plugin.java.JavaPlugin;

public class Dragons extends JavaPlugin {
	private static Dragons INSTANCE;
	
	public void onLoad() {
		synchronized(this) {
			if(INSTANCE == null) {
				INSTANCE = this;
			}
		}
	}
	
	public void onEnable() {
		
	}
	
	public void onDisable() {
		
	}
	
	public static Dragons getInstance() {
		return INSTANCE;
	}
}
