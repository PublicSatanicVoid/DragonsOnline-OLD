package mc.dragons.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.craftbukkit.v1_8_R3.util.ForwardLogHandler;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.ProtocolLibrary;

import mc.dragons.core.bridge.Bridge;
import mc.dragons.core.bridge.impl.Bridge_Spigot1_8_R3;
import mc.dragons.core.commands.BypassDeathCountdownCommand;
import mc.dragons.core.commands.ClearInventoryCommand;
import mc.dragons.core.commands.DebugCommand;
import mc.dragons.core.commands.ExperimentCommand;
import mc.dragons.core.commands.FastForwardDialogueCommand;
import mc.dragons.core.commands.FeedbackCommand;
import mc.dragons.core.commands.FloorCommand;
import mc.dragons.core.commands.GamemodeCommand;
import mc.dragons.core.commands.GoToFloorCommand;
import mc.dragons.core.commands.GodModeCommand;
import mc.dragons.core.commands.ILostTheLousyStickCommand;
import mc.dragons.core.commands.InfoCommand;
import mc.dragons.core.commands.ItemCommand;
import mc.dragons.core.commands.KickCommand;
import mc.dragons.core.commands.LagCommand;
import mc.dragons.core.commands.LogLevelCommand;
import mc.dragons.core.commands.MyQuestsCommand;
import mc.dragons.core.commands.NPCCommand;
import mc.dragons.core.commands.PunishCommands;
import mc.dragons.core.commands.QuestCommand;
import mc.dragons.core.commands.RankCommand;
import mc.dragons.core.commands.RegionCommand;
import mc.dragons.core.commands.ReloadObjectsCommands;
import mc.dragons.core.commands.ReloreCommand;
import mc.dragons.core.commands.RemovePunishmentCommand;
import mc.dragons.core.commands.RenameCommand;
import mc.dragons.core.commands.ServerOptionsCommand;
import mc.dragons.core.commands.SystemLogonCommand;
import mc.dragons.core.commands.TestQuestCommand;
import mc.dragons.core.commands.UnPunishCommands;
import mc.dragons.core.commands.UpdateStatsCommand;
import mc.dragons.core.commands.VanishCommands;
import mc.dragons.core.commands.VerifyGameIntegrityCommand;
import mc.dragons.core.commands.ViewPunishmentsCommand;
import mc.dragons.core.events.ChatEventListener;
import mc.dragons.core.events.DeathEventListener;
import mc.dragons.core.events.EntityDamageByEntityEventListener;
import mc.dragons.core.events.EntityDeathEventListener;
import mc.dragons.core.events.EntityMoveListener;
import mc.dragons.core.events.EntityTargetEventListener;
import mc.dragons.core.events.HungerChangeEventListener;
import mc.dragons.core.events.InventoryEventListeners;
import mc.dragons.core.events.JoinEventListener;
import mc.dragons.core.events.MoveEventListener;
import mc.dragons.core.events.PlayerDropItemListener;
import mc.dragons.core.events.PlayerInteractEntityListener;
import mc.dragons.core.events.PlayerPickupItemListener;
import mc.dragons.core.events.QuitEventListener;
import mc.dragons.core.events.WorldEventListeners;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.floor.Floor;
import mc.dragons.core.gameobject.item.ItemClass;
import mc.dragons.core.gameobject.loader.FloorLoader;
import mc.dragons.core.gameobject.loader.GameObjectRegistry;
import mc.dragons.core.gameobject.loader.ItemClassLoader;
import mc.dragons.core.gameobject.loader.NPCClassLoader;
import mc.dragons.core.gameobject.loader.NPCLoader;
import mc.dragons.core.gameobject.loader.QuestLoader;
import mc.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.core.gameobject.npc.NPC;
import mc.dragons.core.gameobject.npc.NPCClass;
import mc.dragons.core.gameobject.quest.Quest;
import mc.dragons.core.gameobject.region.Region;
import mc.dragons.core.storage.StorageManager;
import mc.dragons.core.storage.impl.MongoConfig;
import mc.dragons.core.storage.impl.MongoStorageManager;
import mc.dragons.core.tasks.AutoSaveTask;
import mc.dragons.core.tasks.LagMeter;
import mc.dragons.core.tasks.LagMonitorTask;
import mc.dragons.core.tasks.SpawnEntityTask;
import mc.dragons.core.tasks.VerifyGameIntegrityTask;

/**
 * The main plugin class for Dragons RPG.
 * 
 * @author Rick
 *
 */
public class Dragons extends JavaPlugin {
	
	private static Dragons INSTANCE;
	private Bridge bridge;
	
	private StorageManager storageManager;
	private GameObjectRegistry gameObjectRegistry;
	
	private BukkitRunnable autoSaveRunnable;
	private BukkitRunnable spawnEntityRunnable;
	private BukkitRunnable verifyGameIntegrityRunnable;
	private LagMeter lagMeter;
	private LagMonitorTask lagMonitorTask;
	
	private ServerOptions serverOptions;
	
	public static final String serverName = Bukkit.getServer().getClass().getPackage().getName();
	public static final String serverVersion = serverName.substring(serverName.lastIndexOf(".") + 1, serverName.length()).substring(1);
	
	
	// JTN's first comment
	
	@Override
	public void onLoad() {
		synchronized(this) {
			if(INSTANCE == null) {
				INSTANCE = this;
			}
		}
	}
	
	//@SuppressWarnings("resource")
	@Override
	public void onEnable() {
		getLogger().info("Searching for compatible version...");
		switch(serverVersion) {
		case "1_8_R3":
			bridge = new Bridge_Spigot1_8_R3();
			break;
		default:
			getLogger().severe("Incompatible server version (" + serverVersion + ")");
			getLogger().severe("Cannot run Dragons.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		getLogger().info("Initializing storage and registries...");
		storageManager = new MongoStorageManager(this, MongoConfig.HOST, MongoConfig.PORT, MongoConfig.USER, MongoConfig.PASSWORD, MongoConfig.AUTH_DB);
		gameObjectRegistry = new GameObjectRegistry(this, storageManager);
		
		autoSaveRunnable = new AutoSaveTask(this);
		spawnEntityRunnable = new SpawnEntityTask(this);
		verifyGameIntegrityRunnable = new VerifyGameIntegrityTask(this);
		lagMeter = new LagMeter();
		lagMonitorTask = new LagMonitorTask();
		
		serverOptions = new ServerOptions();
		getLogger().setLevel(serverOptions.getLogLevel());
		
		getLogger().info("Registering debug logging system...");
		Logger globalLogger = Logger.getLogger("");
		globalLogger.addHandler(new ForwardLogHandler() {
			private Map<String, org.apache.logging.log4j.Logger> cachedLoggers = new ConcurrentHashMap<String, org.apache.logging.log4j.Logger>();
			
			private org.apache.logging.log4j.Logger getLogger(String name) {
			    org.apache.logging.log4j.Logger logger = this.cachedLoggers.get(name);
			    if (logger == null) {
			    	logger = LogManager.getLogger(name);
			    	this.cachedLoggers.put(name, logger);
			    } 
			    return logger;
			}
			
			@Override
			public void publish(LogRecord record) {
				org.apache.logging.log4j.Logger logger = getLogger(String.valueOf(record.getLoggerName()));
				Throwable exception = record.getThrown();
				Level level = record.getLevel();
				String message = getFormatter().formatMessage(record);
				Level logLevel = getServerOptions().getLogLevel();
				if(level.intValue() >= Level.INFO.intValue()) return;
				if(level.intValue() < logLevel.intValue()) return;
				logger.log(org.apache.logging.log4j.Level.INFO, "[" + level + "] " + message, exception);
			}
		});
		
		
		PluginManager pluginManager = getServer().getPluginManager();
		
		getLogger().info("Registering events...");
		pluginManager.registerEvents(new JoinEventListener(this), this);
		pluginManager.registerEvents(new QuitEventListener(), this);
		pluginManager.registerEvents(new DeathEventListener(this), this);
		pluginManager.registerEvents(new ChatEventListener(), this);
		pluginManager.registerEvents(new EntityDeathEventListener(this), this);
		pluginManager.registerEvents(new EntityDamageByEntityEventListener(this), this);
		pluginManager.registerEvents(new MoveEventListener(), this);
		pluginManager.registerEvents(new PlayerDropItemListener(), this);
		pluginManager.registerEvents(new PlayerPickupItemListener(this), this);
		pluginManager.registerEvents(new HungerChangeEventListener(), this);
		pluginManager.registerEvents(new PlayerInteractEntityListener(), this);
		pluginManager.registerEvents(new WorldEventListeners(), this);
		pluginManager.registerEvents(new EntityTargetEventListener(this), this);
		pluginManager.registerEvents(new InventoryEventListeners(), this);
		
		getLogger().info("Loading game objects...");
		// Order here is important! If floors aren't loaded first, then their worlds aren't loaded first, 
		// and then constructing regions causes NPEs trying to access their worlds
		((FloorLoader) GameObjectType.FLOOR.<Floor>getLoader()).lazyLoadAll();
		((RegionLoader) GameObjectType.REGION.<Region>getLoader()).lazyLoadAll();
		((NPCClassLoader) GameObjectType.NPC_CLASS.<NPCClass>getLoader()).lazyLoadAll();
		((ItemClassLoader) GameObjectType.ITEM_CLASS.<ItemClass>getLoader()).lazyLoadAll();
		((QuestLoader) GameObjectType.QUEST.<Quest>getLoader()).lazyLoadAll();
		new BukkitRunnable() {
			@Override public void run() {
				((NPCLoader) GameObjectType.NPC.<NPC>getLoader()).lazyLoadAllPermanent();
			}
		}.runTaskLater(this, 20L);
		
		getLogger().info("Registering commands...");
		getCommand("info").setExecutor(new InfoCommand(this));
		getCommand("region").setExecutor(new RegionCommand(this));
		getCommand("npc").setExecutor(new NPCCommand(this));
		getCommand("item").setExecutor(new ItemCommand(this));
		getCommand("floor").setExecutor(new FloorCommand(this));
		getCommand("clear").setExecutor(new ClearInventoryCommand());
		getCommand("rank").setExecutor(new RankCommand());
		getCommand("syslogon").setExecutor(new SystemLogonCommand());
		getCommand("testquest").setExecutor(new TestQuestCommand(this));
		getCommand("quest").setExecutor(new QuestCommand(this));
		getCommand("godmode").setExecutor(new GodModeCommand());
		getCommand("bypassdeathcountdown").setExecutor(new BypassDeathCountdownCommand());
		getCommand("rename").setExecutor(new RenameCommand());
		getCommand("relore").setExecutor(new ReloreCommand());
		getCommand("verifygameintegrity").setExecutor(new VerifyGameIntegrityCommand(this));
		CommandExecutor gamemodeCommandExecutor = new GamemodeCommand();
		getCommand("gamemode").setExecutor(gamemodeCommandExecutor);
		getCommand("gma").setExecutor(gamemodeCommandExecutor);
		getCommand("gmc").setExecutor(gamemodeCommandExecutor);
		getCommand("gms").setExecutor(gamemodeCommandExecutor);
		getCommand("gotofloor").setExecutor(new GoToFloorCommand(this));
		getCommand("experiment").setExecutor(new ExperimentCommand());
		CommandExecutor punishCommandsExecutor = new PunishCommands();
		getCommand("ban").setExecutor(punishCommandsExecutor);
		getCommand("mute").setExecutor(punishCommandsExecutor);
		getCommand("kick").setExecutor(new KickCommand());
		CommandExecutor unPunishCommandsExecutor = new UnPunishCommands();
		getCommand("unban").setExecutor(unPunishCommandsExecutor);
		getCommand("unmute").setExecutor(unPunishCommandsExecutor);
		getCommand("viewpunishments").setExecutor(new ViewPunishmentsCommand());
		getCommand("ilostthelousystick").setExecutor(new ILostTheLousyStickCommand());
		getCommand("removepunishment").setExecutor(new RemovePunishmentCommand());
		CommandExecutor vanishCommandsExecutor = new VanishCommands();
		getCommand("vanish").setExecutor(vanishCommandsExecutor);
		getCommand("unvanish").setExecutor(vanishCommandsExecutor);
		getCommand("updatestats").setExecutor(new UpdateStatsCommand());
		getCommand("loglevel").setExecutor(new LogLevelCommand());
		getCommand("debug").setExecutor(new DebugCommand());
		getCommand("lag").setExecutor(new LagCommand());
		ReloadObjectsCommands reloadObjectsCommandsExecutor = new ReloadObjectsCommands();
		getCommand("reloadquests").setExecutor(reloadObjectsCommandsExecutor);
		getCommand("serveroptions").setExecutor(new ServerOptionsCommand());
		getCommand("feedback").setExecutor(new FeedbackCommand());
		getCommand("fastforwarddialogue").setExecutor(new FastForwardDialogueCommand());
		getCommand("myquests").setExecutor(new MyQuestsCommand());
		
		getLogger().info("Scheduling tasks...");
		autoSaveRunnable.runTaskTimer(this, 0L, serverOptions.getAutoSavePeriodTicks());
		spawnEntityRunnable.runTaskTimer(this, 0L, serverOptions.getCustomSpawnRate());
		verifyGameIntegrityRunnable.runTaskTimer(this, 0L, serverOptions.getVerifyIntegritySweepRate());
		lagMeter.runTaskTimer(this, 100L, 1L);
		lagMonitorTask.runTaskAsynchronously(this);
		
		getLogger().info("Registering packet listeners...");
		ProtocolLibrary.getProtocolManager().addPacketListener(new EntityMoveListener(this));
	}
	
	@Override
	public void onDisable() {
		((AutoSaveTask) autoSaveRunnable).run(true);
		for(Entity e : getEntities()) {
			e.remove();
		}
	}
	
	public List<Chunk> getLoadedChunks() {
		List<Chunk> chunks = new ArrayList<Chunk>();
		for (World w : Bukkit.getWorlds()) {
			for (Chunk c : w.getLoadedChunks()) {
				chunks.add(c);
			}
		}
		return chunks;
	}
	
	public List<Entity> getEntities() {
		List<Entity> entities = new ArrayList<Entity>();
		for (World w : Bukkit.getWorlds()) {
			entities.addAll(w.getEntities());
		}
		return entities;
	}
	
	public static Dragons getInstance() {
		return INSTANCE;
	}
	
	public StorageManager getStorageManager() {
		return storageManager;
	}
	
	public GameObjectRegistry getGameObjectRegistry() {
		return gameObjectRegistry;
	}
	
	public ServerOptions getServerOptions() {
		return serverOptions;
	}
	
	public Bridge getBridge() {
		return bridge;
	}
	
	public List<Double> getTPSRecord() {
		return lagMonitorTask.getTPSRecord();
	}
	
	public BukkitRunnable getAutoSaveRunnable() {
		return autoSaveRunnable;
	}
	
	public void setAutoSaveRunnable(BukkitRunnable runnable) {
		autoSaveRunnable = runnable;
	}
	
	public BukkitRunnable getSpawnEntityRunnable() {
		return spawnEntityRunnable;
	}
	
	public void setSpawnEntityRunnable(BukkitRunnable runnable) {
		spawnEntityRunnable = runnable;
	}
	
	public BukkitRunnable getVerifyGameIntegrityRunnable() {
		return verifyGameIntegrityRunnable;
	}
	
	public void setVerifyGameIntegrityRunnable(BukkitRunnable runnable) {
		verifyGameIntegrityRunnable = runnable;
	}

}
