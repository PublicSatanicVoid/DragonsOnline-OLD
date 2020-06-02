package mc.dragons.dragons.core;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;

import mc.dragons.dragons.core.bridge.Bridge;
import mc.dragons.dragons.core.bridge.impl.Bridge_Spigot1_8_R3;
import mc.dragons.dragons.core.commands.ClearInventoryCommand;
import mc.dragons.dragons.core.commands.ItemCommand;
import mc.dragons.dragons.core.commands.NPCCommand;
import mc.dragons.dragons.core.commands.PermissionLevelCommand;
import mc.dragons.dragons.core.commands.PlayerInfoCommand;
import mc.dragons.dragons.core.commands.RankCommand;
import mc.dragons.dragons.core.commands.RegionCommand;
import mc.dragons.dragons.core.events.ChatEventListener;
import mc.dragons.dragons.core.events.DeathEventListener;
import mc.dragons.dragons.core.events.EntityDamageByEntityEventListener;
import mc.dragons.dragons.core.events.EntityDeathEventListener;
import mc.dragons.dragons.core.events.EntityMoveListener;
import mc.dragons.dragons.core.events.JoinEventListener;
import mc.dragons.dragons.core.events.MoveEventListener;
import mc.dragons.dragons.core.events.PlayerDropItemListener;
import mc.dragons.dragons.core.events.PlayerPickupItemListener;
import mc.dragons.dragons.core.events.QuitEventListener;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.item.ItemClass;
import mc.dragons.dragons.core.gameobject.loader.GameObjectRegistry;
import mc.dragons.dragons.core.gameobject.loader.ItemClassLoader;
import mc.dragons.dragons.core.gameobject.loader.NPCClassLoader;
import mc.dragons.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.dragons.core.gameobject.npc.NPCClass;
import mc.dragons.dragons.core.gameobject.region.Region;
import mc.dragons.dragons.core.storage.StorageManager;
import mc.dragons.dragons.core.storage.impl.MongoConfig;
import mc.dragons.dragons.core.storage.impl.MongoStorageManager;
import mc.dragons.dragons.core.tasks.AutoSaveTask;
import mc.dragons.dragons.core.tasks.SpawnEntityTask;

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
		
		storageManager = new MongoStorageManager(this, MongoConfig.HOST, MongoConfig.PORT, MongoConfig.USER, MongoConfig.PASSWORD, MongoConfig.AUTH_DB);
		gameObjectRegistry = new GameObjectRegistry(this, storageManager);
		
		autoSaveTask = AutoSaveTask.getInstance(this);
		spawnEntityTask = SpawnEntityTask.getInstance(this);
		
		serverOptions = new ServerOptions();
		
		
		getServer().getPluginManager().registerEvents(new JoinEventListener(this), this);
		getServer().getPluginManager().registerEvents(new QuitEventListener(), this);
		getServer().getPluginManager().registerEvents(new DeathEventListener(this), this);
		getServer().getPluginManager().registerEvents(new ChatEventListener(), this);
		getServer().getPluginManager().registerEvents(new EntityDeathEventListener(this), this);
		getServer().getPluginManager().registerEvents(new EntityDamageByEntityEventListener(this), this);
		getServer().getPluginManager().registerEvents(new MoveEventListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerDropItemListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerPickupItemListener(), this);
		
		getCommand("info").setExecutor(new PlayerInfoCommand(this));
		getCommand("region").setExecutor(new RegionCommand(this));
		getCommand("npc").setExecutor(new NPCCommand(this));
		getCommand("item").setExecutor(new ItemCommand(this));
		getCommand("clear").setExecutor(new ClearInventoryCommand());
		getCommand("rank").setExecutor(new RankCommand());
		getCommand("permissionlevel").setExecutor(new PermissionLevelCommand());
		
		((RegionLoader) GameObjectType.REGION.<Region>getLoader()).lazyLoadAll();
		((NPCClassLoader) GameObjectType.NPC_CLASS.<NPCClass>getLoader()).lazyLoadAll();
		((ItemClassLoader) GameObjectType.ITEM_CLASS.<ItemClass>getLoader()).lazyLoadAll();
		
		Bukkit.getScheduler().runTaskTimer(this, () -> autoSaveTask.run(false), 0, serverOptions.getAutoSavePeriodTicks());
		Bukkit.getScheduler().runTaskTimer(this, () -> spawnEntityTask.run(), 0, serverOptions.getCustomSpawnRate());
		
		ProtocolLibrary.getProtocolManager().addPacketListener(new EntityMoveListener(this));
		
		//((RegionLoader)GameObjectType.REGION.getLoader()).loadAll();
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
