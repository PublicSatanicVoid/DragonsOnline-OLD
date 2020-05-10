package mc.dragons.dragons.core;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main plugin class for Dragons RPG.
 * 
 * @author Rick
 *
 */
public class Dragons extends JavaPlugin {
	private static Dragons INSTANCE;
	
	// JTN's first comment
	
	@Override
	public void onLoad() {
		synchronized(this) {
			if(INSTANCE == null) {
				INSTANCE = this;
			}
		}
	}
	
	@Override
	public void onEnable() {
		
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public static Dragons getInstance() {
		return INSTANCE;
	}
}
