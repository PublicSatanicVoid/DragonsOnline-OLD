package mc.dragons.core;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;

import mc.dragons.core.bridge.Bridge;
import mc.dragons.core.bridge.impl.Bridge_Spigot1_8_R3;
import mc.dragons.core.commands.BypassDeathCountdownCommand;
import mc.dragons.core.commands.ClearInventoryCommand;
import mc.dragons.core.commands.FloorCommand;
import mc.dragons.core.commands.GodModeCommand;
import mc.dragons.core.commands.InfoCommand;
import mc.dragons.core.commands.ItemCommand;
import mc.dragons.core.commands.NPCCommand;
import mc.dragons.core.commands.QuestCommand;
import mc.dragons.core.commands.RankCommand;
import mc.dragons.core.commands.RegionCommand;
import mc.dragons.core.commands.ReloreCommand;
import mc.dragons.core.commands.RenameCommand;
import mc.dragons.core.commands.SystemLogonCommand;
import mc.dragons.core.commands.TestQuestCommand;
import mc.dragons.core.commands.VerifyGameIntegrityCommand;
import mc.dragons.core.events.ChatEventListener;
import mc.dragons.core.events.DeathEventListener;
import mc.dragons.core.events.EntityDamageByEntityEventListener;
import mc.dragons.core.events.EntityDeathEventListener;
import mc.dragons.core.events.EntityMoveListener;
import mc.dragons.core.events.HungerChangeEventListener;
import mc.dragons.core.events.JoinEventListener;
import mc.dragons.core.events.MoveEventListener;
import mc.dragons.core.events.PlayerDropItemListener;
import mc.dragons.core.events.PlayerPickupItemListener;
import mc.dragons.core.events.QuitEventListener;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.floor.Floor;
import mc.dragons.core.gameobject.item.ItemClass;
import mc.dragons.core.gameobject.loader.FloorLoader;
import mc.dragons.core.gameobject.loader.GameObjectRegistry;
import mc.dragons.core.gameobject.loader.ItemClassLoader;
import mc.dragons.core.gameobject.loader.NPCClassLoader;
import mc.dragons.core.gameobject.loader.QuestLoader;
import mc.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.core.gameobject.npc.NPCClass;
import mc.dragons.core.gameobject.quest.Quest;
import mc.dragons.core.gameobject.region.Region;
import mc.dragons.core.storage.StorageManager;
import mc.dragons.core.storage.impl.MongoConfig;
import mc.dragons.core.storage.impl.MongoStorageManager;
import mc.dragons.core.tasks.AutoSaveTask;
import mc.dragons.core.tasks.SpawnEntityTask;

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
	
	private AutoSaveTask autoSaveTask;
	private SpawnEntityTask spawnEntityTask;
	
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
		
		autoSaveTask = AutoSaveTask.getInstance(this);
		spawnEntityTask = SpawnEntityTask.getInstance(this);
		
		serverOptions = new ServerOptions();
		
		
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
		
		getLogger().info("Loading game objects...");
		// Order here is important! If floors aren't loaded first, then their worlds aren't loaded first, 
		// and then constructing regions causes NPEs trying to access their worlds
		((FloorLoader) GameObjectType.FLOOR.<Floor>getLoader()).lazyLoadAll();
		((RegionLoader) GameObjectType.REGION.<Region>getLoader()).lazyLoadAll();
		((NPCClassLoader) GameObjectType.NPC_CLASS.<NPCClass>getLoader()).lazyLoadAll();
		((ItemClassLoader) GameObjectType.ITEM_CLASS.<ItemClass>getLoader()).lazyLoadAll();
		((QuestLoader) GameObjectType.QUEST.<Quest>getLoader()).lazyLoadAll();
		
		getLogger().info("Scheduling tasks...");
		Bukkit.getScheduler().runTaskTimer(this, () -> autoSaveTask.run(false), 0, serverOptions.getAutoSavePeriodTicks());
		Bukkit.getScheduler().runTaskTimer(this, () -> spawnEntityTask.run(), 0, serverOptions.getCustomSpawnRate());
		
		getLogger().info("Registering packet listeners...");
		ProtocolLibrary.getProtocolManager().addPacketListener(new EntityMoveListener(this));
	}
	
	@Override
	public void onDisable() {
		autoSaveTask.run(true);
		for(World w : getServer().getWorlds()) {
			for(Entity e : w.getEntities()) {
				e.remove();
			}
		}
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

}
