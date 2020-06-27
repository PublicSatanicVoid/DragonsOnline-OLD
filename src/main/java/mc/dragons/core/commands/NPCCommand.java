package mc.dragons.core.commands;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.loader.GameObjectRegistry;
import mc.dragons.core.gameobject.loader.NPCClassLoader;
import mc.dragons.core.gameobject.loader.NPCLoader;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.npc.NPC;
import mc.dragons.core.gameobject.npc.NPCClass;
import mc.dragons.core.gameobject.user.PermissionLevel;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.util.PermissionUtil;
import mc.dragons.core.util.StringUtil;
import net.md_5.bungee.api.ChatColor;

public class NPCCommand implements CommandExecutor {
	//private UserLoader userLoader;
	private NPCLoader npcLoader;
	private NPCClassLoader npcClassLoader;
	private GameObjectRegistry gameObjectRegistry;
	
	public NPCCommand(Dragons instance) {
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		npcLoader = (NPCLoader) GameObjectType.NPC.<NPC>getLoader();
		npcClassLoader = (NPCClassLoader) GameObjectType.NPC_CLASS.<NPCClass>getLoader();
		gameObjectRegistry = instance.getGameObjectRegistry();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = null;
		User user = null;
		if(sender instanceof Player) {
			player = (Player) sender;
			user = UserLoader.fromPlayer(player);
			if(!PermissionUtil.verifyActivePermissionLevel(user, PermissionLevel.GM, true)) return true;
		}
		else {
			sender.sendMessage(ChatColor.RED + "This is an ingame-only command.");
			return true;
		}
		
		if(args.length == 0) {
			sender.sendMessage(ChatColor.YELLOW + "/npc class -c <ClassName> <EntityType> <MaxHealth> <Level> <IsHostile>" + ChatColor.GRAY + " create a new NPC class");
			sender.sendMessage(ChatColor.YELLOW + "/npc class -l" + ChatColor.GRAY + " list all NPC classes");
			sender.sendMessage(ChatColor.YELLOW + "/npc class -s <ClassName>" + ChatColor.GRAY + " view information about NPC class");
			sender.sendMessage(ChatColor.YELLOW + "/npc class -s <ClassName> type <EntityType>" + ChatColor.GRAY + " change type of NPC class");
			sender.sendMessage(ChatColor.YELLOW + "/npc class -s <ClassName> name <DisplayName>" + ChatColor.GRAY + " set NPC class display name");
			sender.sendMessage(ChatColor.YELLOW + "/npc class -s <ClassName> health <MaxHealth>" + ChatColor.GRAY + " set NPC class max health");
			sender.sendMessage(ChatColor.YELLOW + "/npc class -s <ClassName> level <Level>" + ChatColor.GRAY + " set NPC level");
			sender.sendMessage(ChatColor.YELLOW + "/npc class -s <ClassName> hostile <IsHostile>" + ChatColor.GRAY + " mark NPC as hostile or not");
			sender.sendMessage(ChatColor.DARK_GRAY + " * Does not affect NPC pathfinding behavior (must be implemented programmatically)");
			sender.sendMessage(ChatColor.YELLOW + "/npc class -s <ClassName> loot [<RegionName> <ItemClassName> <Chance%|DEL>]" + ChatColor.GRAY + " manage NPC class loot table");
			sender.sendMessage(ChatColor.YELLOW + "/npc class -d <ClassName>" + ChatColor.GRAY + " delete NPC class");
			sender.sendMessage(ChatColor.YELLOW + "/npc spawn <ClassName>" + ChatColor.GRAY + " spawn a new NPC of the given class");
			sender.sendMessage(ChatColor.DARK_GRAY + "" +  ChatColor.BOLD + "Note:" + ChatColor.DARK_GRAY + " Class names must not contain spaces.");
			return true;
		}
		
		if(args[0].equalsIgnoreCase("class")) {
			if(args.length == 1) {
				sender.sendMessage(ChatColor.RED + "Insufficient arguments!");
				return true;
			}
			if(args[1].equalsIgnoreCase("-c")) {
				if(args.length < 7) {
					sender.sendMessage(ChatColor.RED + "Insufficient arguments! /npc class -c <ClassName> <EntityType> <MaxHealth> <Level> <IsHostile>");
					return true;
				}
				String npcClassName = args[2];
				EntityType type = EntityType.valueOf(args[3].toUpperCase());
				double maxHealth = Double.valueOf(args[4]);
				int level = Integer.valueOf(args[5]);
				boolean hostile = Boolean.valueOf(args[6]);
				NPCClass npcClass = npcClassLoader.registerNew(npcClassName, "Unnamed Entity", type, maxHealth, level, hostile);
				if(npcClass == null) {
					sender.sendMessage(ChatColor.RED + "An error occurred! Does a class by this name already exist?");
					return true;
				}
				sender.sendMessage(ChatColor.GREEN + "Successfully created NPC class " + npcClassName);
				return true;
			}
			if(args[1].equalsIgnoreCase("-l")) {
				sender.sendMessage(ChatColor.GREEN + "Listing all NPC classes:");
				for(GameObject gameObject : Dragons.getInstance().getGameObjectRegistry().getRegisteredObjects(GameObjectType.NPC_CLASS)) {
					NPCClass npcClass = (NPCClass) gameObject;
					sender.sendMessage(ChatColor.GRAY + "- " + npcClass.getClassName() + " [Lv " + npcClass.getLevel() + "]");
				}
				return true;
			}
			if(args[1].equalsIgnoreCase("-s")) {
				if(args.length < 3) {
					sender.sendMessage(ChatColor.RED + "Insufficient arguments!");
					return true;
				}
				NPCClass npcClass = npcClassLoader.getNPCClassByClassName(args[2]);
				if(npcClass == null) {
					sender.sendMessage(ChatColor.RED + "That's not a valid NPC class name!");
					return true;
				}
				if(args.length == 3) {
					sender.sendMessage(ChatColor.GREEN + "=== NPC Class: " + npcClass.getClassName() + " ===");
					sender.sendMessage(ChatColor.GRAY + "Database identifier: " + ChatColor.GREEN + npcClass.getIdentifier().toString());
					sender.sendMessage(ChatColor.GRAY + "Display name: " + ChatColor.GREEN + npcClass.getName());
					sender.sendMessage(ChatColor.GRAY + "Max health: " + ChatColor.GREEN + npcClass.getMaxHealth());
					sender.sendMessage(ChatColor.GRAY + "Level: " + ChatColor.GREEN + npcClass.getLevel());
					sender.sendMessage(ChatColor.GRAY + "Hostile: " + ChatColor.GREEN + npcClass.isHostile());
					return true;
				}
				if(args[3].equalsIgnoreCase("type")) {
					EntityType type = EntityType.valueOf(args[4].toUpperCase());
					npcClass.setEntityType(type);
					sender.sendMessage(ChatColor.GREEN + "Updated entity type successfully.");
					return true;
				}
				if(args[3].equalsIgnoreCase("name")) {
					npcClass.setName(StringUtil.concatArgs(args, 4));
					sender.sendMessage(ChatColor.GREEN + "Updated entity display name successfully.");
					return true;
				}
				if(args[3].equalsIgnoreCase("health")) {
					npcClass.setMaxHealth(Double.valueOf(args[4]));
					sender.sendMessage(ChatColor.GREEN + "Updated entity max health successfully.");
					return true;
				}
				if(args[3].equalsIgnoreCase("level")) {
					npcClass.setLevel(Integer.valueOf(args[4]));
					sender.sendMessage(ChatColor.GREEN + "Updated entity level successfully.");
					return true;
				}
				if(args[3].equalsIgnoreCase("hostile")) {
					npcClass.setHostile(Boolean.valueOf(args[4]));
					sender.sendMessage(ChatColor.GREEN + "Updated entity hostility successfully.");
					return true;
				}
				if(args[3].equalsIgnoreCase("loot")) {
					if(args.length == 4) {
						sender.sendMessage(ChatColor.GREEN + "Loot Table:");
						for(Entry<String, Map<String, Double>> regionLoot : npcClass.getLootTable().asMap().entrySet()) {
							sender.sendMessage(ChatColor.GRAY + "- Region: " + regionLoot.getKey());
							for(Entry<String, Double> itemLoot : regionLoot.getValue().entrySet()) {
								sender.sendMessage(ChatColor.GRAY + "   - " + itemLoot.getKey() + ": " + itemLoot.getValue() + "%");
							}
						}
						return true;
					}
					if(args[6].equalsIgnoreCase("del")) {
						npcClass.deleteFromLootTable(args[4], args[5]);
						sender.sendMessage(ChatColor.GREEN + "Removed from entity loot table successfully.");
						return true;
					}
					npcClass.updateLootTable(args[4], args[5], Double.valueOf(args[6]));
					sender.sendMessage(ChatColor.GREEN + "Updated entity loot table successfully.");
					return true;
				}
				return true;
			}
			if(args[1].equalsIgnoreCase("-d")) {
				if(args.length == 2) {
					sender.sendMessage(ChatColor.RED + "Specify a class name to delete! /npc class -d <ClassName>");
					return true;
				}
				NPCClass npcClass = npcClassLoader.getNPCClassByClassName(args[2]);
				if(npcClass == null) {
					sender.sendMessage(ChatColor.RED + "That's not a valid NPC class name!");
					return true;
				}
				gameObjectRegistry.removeFromDatabase(npcClass);
				sender.sendMessage(ChatColor.GREEN + "Successfully deleted NPC class.");
				return true;
			}
		}
		if(args[0].equalsIgnoreCase("spawn")) {
			npcLoader.registerNew(player.getWorld(), player.getLocation(), args[1]);
			sender.sendMessage(ChatColor.GREEN + "Spawned an NPC of class " + args[1] + " at your location.");
			return true;
		}
		
		return true;
	}

}
