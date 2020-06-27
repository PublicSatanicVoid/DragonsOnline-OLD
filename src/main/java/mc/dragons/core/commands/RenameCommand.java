package mc.dragons.core.commands;

import java.util.function.Supplier;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mc.dragons.core.gameobject.item.Item;
import mc.dragons.core.gameobject.loader.ItemLoader;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.PermissionLevel;
import mc.dragons.core.gameobject.user.SkillType;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.util.PermissionUtil;
import mc.dragons.core.util.StringUtil;

public class RenameCommand implements CommandExecutor {

	//private UserLoader userLoader;
	
	public RenameCommand() {
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This is an ingame-only command.");
			return true;
		}
		
		
		Player player = (Player) sender;
		User user = UserLoader.fromPlayer(player);
		
		boolean bypassed = PermissionUtil.verifyActivePermissionLevel(user, PermissionLevel.MOD, false);
		boolean valid = user.getSkillLevel(SkillType.BLACKSMITHING) >= 20 && user.getGold() >= 50.0 || bypassed;
		
		if(!valid) {
			sender.sendMessage(ChatColor.RED + "Requires Blacksmithing Lv. 20+ and 50 Gold.");
			return true;
		}
		
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Specify a new name for the item! /rename <NewItemName>");
			return true;
		}
		
		String renameTo = ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', StringUtil.concatArgs(args, 0));

		Supplier<ItemStack> heldItemStackSupplier = () -> user.p().getItemInHand();
		Item heldItem = ItemLoader.fromBukkit(heldItemStackSupplier.get());
		
		if(heldItem == null) {
			sender.sendMessage(ChatColor.RED + "You must hold the item you want to rename!");
			return true;
		}

		heldItem.setCustom(true);
		heldItem.safePermanentRename(heldItemStackSupplier, renameTo);
		heldItem.safePermanentRelore(heldItemStackSupplier, heldItem.getLore().toArray(new String[heldItem.getLore().size()]));
		sender.sendMessage(ChatColor.GREEN + "Renamed your held item to " + renameTo);
		
		if(!bypassed) {
			user.takeGold(50.0);
		}
		
		return true;
		
	}

}
