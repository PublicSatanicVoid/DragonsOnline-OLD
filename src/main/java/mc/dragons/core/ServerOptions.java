package mc.dragons.core;

import java.util.logging.Level;

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
	private int spawnLevelCap;
	
	private int deathCountdown;
	
	private int verifyIntegritySweepRate;
	
	private double defaultWalkSpeed;
	
	private Level logLevel;
	
	public ServerOptions() {
		autoSavePeriodTicks = 20 * 60 * 5;
		autoSaveEnabled = true;
		
		customSpawnRate = 20 * 5;
		customSpawningEnabled = true;
		spawnLevelCap = 10;
		
		deathCountdown = 10;
		
		verifyIntegritySweepRate = 20 * 60;
		
		defaultWalkSpeed = 0.2;
		
		logLevel = Level.INFO;
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
	
	
	
	public void setSpawnLevelCap(int cap) {
		this.spawnLevelCap = cap;
	}
	
	public int getSpawnLevelCap() {
		return spawnLevelCap;
	}
	
	
	public void setDeathCountdown(int seconds) {
		this.deathCountdown = seconds;
	}
	
	public int getDeathCountdown() {
		return deathCountdown;
	}
	
	
	/**
	 * Changes to this will not take effect until a restart.
	 * 
	 * @param rate
	 */
	public void setVerifyIntegritySweepRate(int rate) {
		this.verifyIntegritySweepRate = rate;
	}
	
	public int getVerifyIntegritySweepRate() {
		return verifyIntegritySweepRate;
	}
	
	
	public void setDefaultWalkSpeed(double speed) {
		this.defaultWalkSpeed = speed;
	}
	
	public double getDefaultWalkSpeed() {
		return defaultWalkSpeed;
	}
	
	
	public void setLogLevel(Level level) {
		this.logLevel = level;
		Dragons.getInstance().getLogger().setLevel(level);
		Dragons.getInstance().getLogger().info("Log level changed to " + level);
	}
	
	public Level getLogLevel() {
		return logLevel;
	}
}
