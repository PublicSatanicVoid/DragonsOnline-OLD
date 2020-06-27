package mc.dragons.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.floor.Floor;
import mc.dragons.core.gameobject.loader.FloorLoader;
import mc.dragons.core.gameobject.loader.GameObjectRegistry;
import mc.dragons.core.gameobject.loader.ItemLoader;
import mc.dragons.core.gameobject.loader.NPCLoader;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.PermissionLevel;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.util.PermissionUtil;

public class VerifyGameIntegrityCommand implements CommandExecutor {

	private UserLoader userLoader;
	private GameObjectRegistry registry;
	private FloorLoader floorLoader;
	
	public VerifyGameIntegrityCommand(Dragons instance) {
		userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		registry = instance.getGameObjectRegistry();
		floorLoader = (FloorLoader) GameObjectType.FLOOR.<Floor>getLoader();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = null;
		User user = null;
		if(sender instanceof Player) {
			player = (Player) sender;
			user = UserLoader.fromPlayer(player);
			if(!PermissionUtil.verifyActivePermissionLevel(user, PermissionLevel.ADMIN, true)) return true;
		}
		
		int errors = 0;
		int fixed = 0;
		
		boolean resolve = false;
		if(args.length > 0) {
			if(args[0].equalsIgnoreCase("-resolve")) {
				resolve = true;
			}
		}
		
		sender.sendMessage(ChatColor.GREEN + "Verifying integrity of active game environment...");
		sender.sendMessage(ChatColor.GRAY + "- Validating user data:");
		for(GameObject gameObject : registry.getRegisteredObjects(GameObjectType.USER)) {
			User testUser = (User) gameObject;
			if(testUser.p() == null) {
				sender.sendMessage(ChatColor.RED + "    - User " + testUser.getName() + " does not correspond to a valid player");
				errors++;
				if(resolve) {
					userLoader.unregister(testUser);
					sender.sendMessage(ChatColor.DARK_GREEN + "        [Fixed]");
					fixed++;
				}
			}
			if(testUser.getSystemProfile() == null) {
				if(testUser.getActivePermissionLevel() != PermissionLevel.USER) {
					sender.sendMessage(ChatColor.RED + "    - User " + testUser.getName() + " has an elevated active permission level without being logged in to a system profile");
					errors++;
					if(resolve) {
						testUser.setActivePermissionLevel(PermissionLevel.USER);
						sender.sendMessage(ChatColor.DARK_GREEN + "        [Fixed]");
						fixed++;
					}
				}
			}
			else {
				if(testUser.getActivePermissionLevel().ordinal() > testUser.getSystemProfile().getMaxPermissionLevel().ordinal()) {
					sender.sendMessage(ChatColor.RED + "    - User " + testUser.getName() + " has an active permission level exceeding the maximum of their system profile");
					errors++;
					if(resolve) {
						testUser.setActivePermissionLevel(PermissionLevel.USER);
						sender.sendMessage(ChatColor.DARK_GREEN + "        [Fixed]");
						fixed++;
					}
				}
			}
		}
		for(Player testPlayer : Bukkit.getOnlinePlayers()) {
			if(UserLoader.fromPlayer(testPlayer) == null) {
				sender.sendMessage(ChatColor.RED + "    - Player " + testPlayer.getName() + " does not correspond to a valid GameObject");
				errors++;
				if(resolve) {
					testPlayer.kickPlayer("An error was found with your account, please relog");
					sender.sendMessage(ChatColor.DARK_GREEN + "        [Fixed]");
					fixed++;
				}
			}
			for(ItemStack itemStack : testPlayer.getInventory().getContents()) {
				if(ItemLoader.fromBukkit(itemStack) == null) {
					if(itemStack == null) continue;
					if(itemStack.getType() == null) continue;
					if(itemStack.getAmount() == 0) continue; // Is this possible?
					sender.sendMessage(ChatColor.RED + "    - Player " + testPlayer.getName() + " has a vanilla item [" + itemStack.getType().toString() + "]");
					errors++;
					if(resolve) {
						testPlayer.getInventory().remove(itemStack);
						testPlayer.sendMessage(ChatColor.RED + "A vanilla item was removed from your inventory.");
						sender.sendMessage(ChatColor.DARK_GREEN + "        [Fixed]");
						fixed++;
					}
				}
			}
		}
		
		sender.sendMessage(ChatColor.GRAY + "- Validating entities:");
		for(World world : Bukkit.getWorlds()) {
			for(Entity entity : world.getEntities()) {
				if(entity instanceof Player) continue;
				if(entity instanceof Item) {
					if(ItemLoader.fromBukkit(((Item) entity).getItemStack()) == null) {
						sender.sendMessage(ChatColor.RED + "    - Entity #" + entity.getEntityId() + " [type " + entity.getType().toString() + "] in world " + world.getName() + " does not correspond to a valid GameObject");
						if(resolve) {
							entity.remove();
							sender.sendMessage(ChatColor.DARK_GREEN + "        [Fixed]");
							fixed++;
						}
						errors++;
					}
				}
				else if(NPCLoader.fromBukkit(entity) == null) {
					sender.sendMessage(ChatColor.RED + "    - Entity #" + entity.getEntityId() + " [type " + entity.getType().toString() + "] in world " + world.getName() + " does not correspond to a valid GameObject");
					errors++;
					if(resolve) {
						entity.remove();
						sender.sendMessage(ChatColor.DARK_GREEN + "        [Fixed]");
						fixed++;
					}
				}
			}
		}
		
		sender.sendMessage(ChatColor.GRAY + "- Validating floors:");
		for(World world : Bukkit.getWorlds()) {
			if(world.getName().equals("world") || world.getName().equals("world_nether") || world.getName().contentEquals("world_the_end")) continue;
			if(floorLoader.fromWorld(world) == null) {
				sender.sendMessage(ChatColor.RED + "    - World " + world.getName() + " does not correspond to a valid floor");
				errors++;
			}
		}
		
		if(errors == 0) {
			sender.sendMessage(ChatColor.GREEN + "All checks passed! Active game environment is valid.");
		}
		else {
			sender.sendMessage(ChatColor.RED + "There were " + errors + " errors found in the active game environment.");
			if(resolve) {
				sender.sendMessage(ChatColor.RED + "" + fixed + " errors were fixed.");
			}
			else {
				sender.sendMessage(ChatColor.RED + "Run " + ChatColor.GRAY + "/verifygameintegrity -resolve " + ChatColor.RED + "to attempt to fix these issues. "
						+ "You may need to run this multiple times in some cases.");
			}
		}
		
		
		return true;
		
	}

}
