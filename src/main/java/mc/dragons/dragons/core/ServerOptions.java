package mc.dragons.dragons.core;

/**
 * Settings for the local server.
 * 
 * @author Rick
 *
 */
public class ServerOptions {
	private int autoSavePeriodTicks;
	private boolean autoSaveEnabled;
	
	private int customSpawnRate;
	private boolean customSpawningEnabled;
	
	public ServerOptions() {
		autoSavePeriodTicks = 20 * 60 * 5;
		autoSaveEnabled = true;
		
		customSpawnRate = 20 * 10;
		customSpawningEnabled = true;
	}
	
	
	/**
	 * Changes to this will not take effect until a restart.
	 * 
	 * @param period
	 */
	public void setAutoSavePeriodTicks(int period) {
		this.autoSavePeriodTicks = period;
	}
	
	public int getAutoSavePeriodTicks() {
		return autoSavePeriodTicks;
	}
	
	
	
	public void setAutoSaveEnabled(boolean enabled) {
		this.autoSaveEnabled = enabled;
	}
	
	public boolean isAutoSaveEnabled() {
		return autoSaveEnabled;
	}
	
	
	
	/**
	 * Changes to this will not take effect until a restart.
	 * 
	 * @param enabled
	 */
	public void setCustomSpawnRate(int rate) {
		this.customSpawnRate = rate;
	}
	
	public int getCustomSpawnRate() {
		return customSpawnRate;
	}
	
	
	
	public void setCustomSpawningEnabled(boolean enabled) {
		this.customSpawningEnabled = true;
	}
	
	public boolean isCustomSpawningEnabled() {
		return customSpawningEnabled;
	}
}
