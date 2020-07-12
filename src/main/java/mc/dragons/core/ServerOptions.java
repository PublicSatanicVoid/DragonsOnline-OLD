package mc.dragons.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import mc.dragons.core.tasks.AutoSaveTask;
import mc.dragons.core.tasks.SpawnEntityTask;
import mc.dragons.core.tasks.VerifyGameIntegrityTask;

/**
 * Settings for the local server.
 * 
 * @author Rick
 *
 */
public class ServerOptions {
	private Logger LOGGER;
	
	
	private int autoSavePeriodTicks;
	private boolean autoSaveEnabled;
	
	private int customSpawnRate;
	private boolean customSpawningEnabled;
	
	private int deathCountdown;
	
	private int verifyIntegritySweepRate;
	private boolean verifyIntegrityEnabled;
	
	private double defaultWalkSpeed;
	
	private Level logLevel;
	
	public ServerOptions() {
		LOGGER = Dragons.getInstance().getLogger();
		
		autoSavePeriodTicks = 20 * 60 * 5;
		autoSaveEnabled = true;
		
		customSpawnRate = 20 * 5;
		customSpawningEnabled = true;
		
		deathCountdown = 10;
		
		verifyIntegritySweepRate = 20 * 60;
		verifyIntegrityEnabled = true;
		
		defaultWalkSpeed = 0.2;
		
		logLevel = Level.INFO;
	}
	
	
	public void setAutoSavePeriodTicks(int period) {
		this.autoSavePeriodTicks = period;
		Dragons.getInstance().getAutoSaveRunnable().cancel();
		AutoSaveTask task = new AutoSaveTask(Dragons.getInstance());
		Dragons.getInstance().setAutoSaveRunnable(task);
		task.runTaskTimer(Dragons.getInstance(), 0L, period);
		
		LOGGER.config("Set auto-save period to " + period + " ticks");
	}
	
	public int getAutoSavePeriodTicks() {
		return autoSavePeriodTicks;
	}
	
	
	public void setAutoSaveEnabled(boolean enabled) {
		this.autoSaveEnabled = enabled;
		LOGGER.config((enabled ? "Enabled" : "Disabled") + " auto-saving");
	}
	
	public boolean isAutoSaveEnabled() {
		return autoSaveEnabled;
	}
	
	
	public void setCustomSpawnRate(int rate) {
		this.customSpawnRate = rate;
		Dragons.getInstance().getSpawnEntityRunnable().cancel();
		SpawnEntityTask task = new SpawnEntityTask(Dragons.getInstance());
		Dragons.getInstance().setSpawnEntityRunnable(task);
		task.runTaskTimer(Dragons.getInstance(), 0L, rate);
		
		LOGGER.config("Custom spawn rate set to " + rate + "s.");
	}
	
	public int getCustomSpawnRate() {
		return customSpawnRate;
	}
	
	
	
	public void setCustomSpawningEnabled(boolean enabled) {
		this.customSpawningEnabled = enabled;
		LOGGER.config((enabled ? "Enabled" : "Disabled") + " custom spawning");
	}
	
	public boolean isCustomSpawningEnabled() {
		return customSpawningEnabled;
	}
	
	
	public void setDeathCountdown(int seconds) {
		this.deathCountdown = seconds;
		LOGGER.config("Default death countdown set to " + seconds + "s");
	}
	
	public int getDeathCountdown() {
		return deathCountdown;
	}
	
	
	public void setVerifyIntegritySweepRate(int rate) {
		this.verifyIntegritySweepRate = rate;
		Dragons.getInstance().getVerifyGameIntegrityRunnable().cancel();
		VerifyGameIntegrityTask task = new VerifyGameIntegrityTask(Dragons.getInstance());
		Dragons.getInstance().setVerifyGameIntegrityRunnable(task);
		task.runTaskTimer(Dragons.getInstance(), 0L, rate);
		LOGGER.config("Game verification sweep rate set to " + rate + "s.");
	}
	
	public int getVerifyIntegritySweepRate() {
		return verifyIntegritySweepRate;
	}
	
	
	public void setVerifyIntegrityEnabled(boolean enabled) {
		this.verifyIntegrityEnabled = enabled;
		LOGGER.config((enabled ? "Enabled" : "Disabled") + " game environment verification");
	}
	
	public boolean isVerifyIntegrityEnabled() {
		return verifyIntegrityEnabled;
	}
	
	
	public void setDefaultWalkSpeed(double speed) {
		this.defaultWalkSpeed = speed;
		LOGGER.config("Default walk speed set to " + speed);
	}
	
	public double getDefaultWalkSpeed() {
		return defaultWalkSpeed;
	}
	
	
	public void setLogLevel(Level level) {
		this.logLevel = level;
		LOGGER.setLevel(level);
		LOGGER.info("Log level changed to " + level);
	}
	
	public Level getLogLevel() {
		return logLevel;
	}
}
