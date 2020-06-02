package mc.dragons.dragons.core.commands;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObject;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.item.Item;
import mc.dragons.dragons.core.gameobject.item.ItemClass;
import mc.dragons.dragons.core.gameobject.loader.GameObjectRegistry;
import mc.dragons.dragons.core.gameobject.loader.ItemClassLoader;
import mc.dragons.dragons.core.gameobject.loader.ItemLoader;
import mc.dragons.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.dragons.core.gameobject.player.PermissionLevel;
import mc.dragons.dragons.core.gameobject.player.User;
import mc.dragons.dragons.core.storage.StorageManager;
import mc.dragons.dragons.core.util.StringUtil;

public class ItemCommand implements CommandExecutor {
	private UserLoader playerLoader;
	private ItemLoader itemLoader;
	private ItemClassLoader itemClassLoader;
	private GameObjectRegistry registry;
	private StorageManager storageManager;
	
	public ItemCommand(Dragons instance) {
		playerLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		itemLoader = (ItemLoader) GameObjectType.ITEM.<Item>getLoader();
		itemClassLoader = (ItemClassLoader) GameObjectType.ITEM_CLASS.<ItemClass>getLoader();
		registry = instance.getGameObjectRegistry();
		storageManager = instance.getStorageManager();
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = null;
		User user = null;
		if(sender instanceof Player) {
			player = (Player) sender;
			user = playerLoader.fromPlayer(player);
			if(user.getPermissionLevel().getLevel() < PermissionLevel.GM.getLevel()) {
				sender.sendMessage(ChatColor.RED + "This command requires permission level GM.");
				return true;
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "This is an ingame-only command.");
			return true;
		}
		
		if(args.length == 0) {
			sender.sendMessage(ChatColor.YELLOW + "/item class -c <ItemClass> <MaterialType> <LvMin> <Cooldown> <IsUnbreakable> <Damage> <Armor>" 
				+ ChatColor.GRAY + " create a new item class");
			sender.sendMessage(ChatColor.YELLOW + "/item class -l" + ChatColor.GRAY + " list all item classes");
			sender.sendMessage(ChatColor.YELLOW + "/item class -s <ItemClass>" + ChatColor.GRAY + " show information about an item class");
			sender.sendMessage(ChatColor.YELLOW + "/item class -s <ItemClass> name <DisplayName>" + ChatColor.GRAY + " set item class display name");
			sender.sendMessage(ChatColor.YELLOW + "/item class -s <ItemClass> namecolor <Color>" + ChatColor.GRAY + " set item class display name color");
			sender.sendMessage(ChatColor.YELLOW + "/item class -s <ItemClass> lore <add <Lore>|remove <LineNo>]>" + ChatColor.GRAY + " view/edit item class lore");
			sender.sendMessage(ChatColor.YELLOW + "/item class -s <ItemClass> type|lvmin|cooldown|unbreakable|damage|armor <Value>" + ChatColor.GRAY + " edit item class data");
			sender.sendMessage(ChatColor.YELLOW + "/item class -s <ItemClass> push" + ChatColor.GRAY + " update all items of this class with updated stats");
			sender.sendMessage(ChatColor.YELLOW + "/item class -d <ItemClass>" + ChatColor.GRAY + " delete item class");
			sender.sendMessage(ChatColor.YELLOW + "/item give <ItemClass>" + ChatColor.GRAY + " receive an item of the specified class7");
			sender.sendMessage(ChatColor.DARK_GRAY + "" +  ChatColor.BOLD + "Note:" + ChatColor.DARK_GRAY + " Class names must not contain spaces.");
			return true;
		}
		
		if(args[0].equalsIgnoreCase("class")) {
			if(args.length == 1) {
				sender.sendMessage(ChatColor.RED + "Insufficient arguments!");
				return true;
			}
			
			if(args[1].equalsIgnoreCase("-c")) {
				if(args.length < 9) {
					sender.sendMessage(ChatColor.RED + "Insufficient arguments!");
					return true;
				}
				
				Material type = Material.valueOf(args[3]);
				int lvMin = Integer.valueOf(args[4]);
				double cooldown = Double.valueOf(args[5]);
				boolean unbreakable = Boolean.valueOf(args[6]);
				double damage = Double.valueOf(args[7]);
				double armor = Double.valueOf(args[8]);
				ItemClass itemClass = itemClassLoader.registerNew(args[2], "Unnamed Item", ChatColor.YELLOW, type, lvMin, cooldown, unbreakable, damage, armor, new ArrayList<>());
				if(itemClass == null) {
					sender.sendMessage(ChatColor.RED + "An error occurred! Does a class by this name already exist?");
					return true;
				}
				sender.sendMessage(ChatColor.GREEN + "Successfully created item class " + args[2]);
				return true;
			}
			
			if(args[1].equalsIgnoreCase("-l")) {
				sender.sendMessage(ChatColor.GREEN + "Listing all item classes:");
				for(GameObject gameObject : registry.getRegisteredObjects(GameObjectType.ITEM_CLASS)) {
					ItemClass itemClass = (ItemClass) gameObject;
					sender.sendMessage(ChatColor.GRAY + "- " + itemClass.getClassName() + " [Lv Min " + itemClass.getLevelMin() + "]");
				}
				return true;
			}
			
			if(args[1].equalsIgnoreCase("-s")) {
				if(args.length == 2) {
					sender.sendMessage(ChatColor.RED + "Insufficient arguments!");
					return true;
				}

				ItemClass itemClass = itemClassLoader.getItemClassByClassName(args[2]);
				
				if(itemClass == null) {
					sender.sendMessage(ChatColor.RED + "That's not a valid item class name!");
					return true;
				}
				
				if(args.length == 3) {
					sender.sendMessage(ChatColor.GREEN + "=== Item Class: " + itemClass.getClassName() + " ===");
					sender.sendMessage(ChatColor.GRAY + "Database identifier: " + ChatColor.GREEN + itemClass.getIdentifier().toString());
					sender.sendMessage(ChatColor.GRAY + "Display name: " + ChatColor.GREEN + itemClass.getDecoratedName());
					sender.sendMessage(ChatColor.GRAY + "Level min: " + ChatColor.GREEN + itemClass.getLevelMin());
					sender.sendMessage(ChatColor.GRAY + "Material type: " + ChatColor.GREEN + itemClass.getMaterial().toString());
					sender.sendMessage(ChatColor.GRAY + "Damage: " + ChatColor.GREEN + itemClass.getDamage() + ChatColor.GRAY + " - Armor: " + ChatColor.GREEN + itemClass.getArmor());
					sender.sendMessage(ChatColor.GRAY + "Cooldown: " + ChatColor.GREEN + itemClass.getCooldown());
					sender.sendMessage(ChatColor.GRAY + "Unbreakable: " + ChatColor.GREEN + itemClass.isUnbreakable());
					sender.sendMessage(ChatColor.GRAY + "Lore:");
					for(String loreLine : itemClass.getLore()) {
						sender.sendMessage(ChatColor.GREEN + " " + loreLine);
					}
					return true;
				}
				
				if(args[3].equalsIgnoreCase("name")) {
					String name = StringUtil.concatArgs(args, 4);
					itemClass.setName(name);
					sender.sendMessage(ChatColor.GREEN + "Updated item display name successfully.");
					return true;
				}
				
				if(args[3].equalsIgnoreCase("namecolor")) {
					ChatColor nameColor = ChatColor.valueOf(args[4]);
					itemClass.setNameColor(nameColor);
					sender.sendMessage(ChatColor.GREEN + "Updated item display name color successfully.");
					return true;
				}
				
				if(args[3].equalsIgnoreCase("lore")) {
					if(args[4].equalsIgnoreCase("add")) {
						String loreLine = StringUtil.concatArgs(args, 5);
						List<String> lore = itemClass.getLore();
						lore.add(loreLine);
						itemClass.setLore(lore);
						sender.sendMessage(ChatColor.GREEN + "Updated item lore successfully.");
						return true;
					}
					if(args[4].equalsIgnoreCase("remove")) {
						List<String> lore = itemClass.getLore();
						lore.remove(Integer.valueOf(args[5]) - 1);
						itemClass.setLore(lore);
						sender.sendMessage(ChatColor.GREEN + "Updated item lore successfully.");
						return true;
					}
				}
				
				if(args[3].equalsIgnoreCase("type")) {
					Material type = Material.valueOf(args[4]);
					itemClass.setMaterial(type);
					sender.sendMessage(ChatColor.GREEN + "Updated item type successfully.");
					return true;
				}
				
				if(args[3].equalsIgnoreCase("lvmin")) {
					int lvMin = Integer.valueOf(args[4]);
					itemClass.setLevelMin(lvMin);
					sender.sendMessage(ChatColor.GREEN + "Updated level min successfully.");
					return true;
				}
				
				if(args[3].equalsIgnoreCase("cooldown")) {
					double cooldown = Double.valueOf(args[4]);
					itemClass.setCooldown(cooldown);
					sender.sendMessage(ChatColor.GREEN + "Updated cooldown successfully.");
					return true;
				}
				
				if(args[3].equalsIgnoreCase("unbreakable")) {
					boolean unbreakable = Boolean.valueOf(args[4]);
					itemClass.setUnbreakable(unbreakable);
					sender.sendMessage(ChatColor.GREEN + "Updated unbreakable status successfully.");
					return true;
				}
				
				if(args[3].equalsIgnoreCase("damage")) {
					double damage = Double.valueOf(args[4]);
					itemClass.setDamage(damage);
					sender.sendMessage(ChatColor.GREEN + "Updated damage successfully.");
					return true;
				}
				
				if(args[3].equalsIgnoreCase("armor")) {
					double armor = Double.valueOf(args[4]);
					itemClass.setArmor(armor);
					sender.sendMessage(ChatColor.GREEN + "Updated armor successfully.");
					return true;
				}
				
				if(args[3].equalsIgnoreCase("push")) {
					storageManager.push(GameObjectType.ITEM, new Document("itemClass", itemClass.getClassName()), itemClass.getData());
					sender.sendMessage(ChatColor.GREEN + "Updated all items matching class " + itemClass.getClassName() + " in database.");
					sender.sendMessage(ChatColor.GREEN + "Players must rejoin to receive the updated items.");
					return true;
				}
			}
			
			if(args[1].equalsIgnoreCase("-d")) {
				if(args.length == 2) {
					sender.sendMessage(ChatColor.RED + "Insufficient arguments!");
					return true;
				}

				ItemClass itemClass = itemClassLoader.getItemClassByClassName(args[2]);
				
				if(itemClass == null) {
					sender.sendMessage(ChatColor.RED + "That's not a valid item class name!");
					return true;
				}
				
				registry.removeFromDatabase(itemClass);
				sender.sendMessage(ChatColor.GREEN + "Deleted item class successfully.");
				return true;
			}
			
			return true;
		}
		
		if(args[0].equalsIgnoreCase("give")) {
			if(args.length == 1) {
				sender.sendMessage(ChatColor.RED + "Insufficient arguments!");
				return true;
			}

			ItemClass itemClass = itemClassLoader.getItemClassByClassName(args[1]);
			
			if(itemClass == null) {
				sender.sendMessage(ChatColor.RED + "That's not a valid item class name!");
				return true;
			}
			
			Item item = itemLoader.registerNew(itemClass);
			user.giveItem(item);		
			return true;
		}
		
		return true;
	}
	
	
}
