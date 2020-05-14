package mc.dragons.dragons.core;

/**
 * Settings for the local server.
 * 
 * @author Rick
 *
 */
public class ServerOptions {
	public int autoSavePeriodTicks;
	public boolean autoSaveEnabled;
	
	public ServerOptions() {
		autoSavePeriodTicks = 20 * 60 * 5;
		autoSaveEnabled = true;
	}
	
	public void setAutoSavePeriodTicks(int period) {
		this.autoSavePeriodTicks = period;
	}
	
	public int getAutoSavePeriodTicks() {
		return autoSavePeriodTicks;
	}
	
	/**
	 * Changes to this will not take effect until a restart.
	 * 
	 * @param enabled
	 */
	public void setAutoSaveEnabled(boolean enabled) {
		this.autoSaveEnabled = enabled;
	}
	
	public boolean isAutoSaveEnabled() {
		return autoSaveEnabled;
	}
}
