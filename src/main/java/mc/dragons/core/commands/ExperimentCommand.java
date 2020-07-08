package mc.dragons.core.commands;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import mc.dragons.core.Dragons;
import mc.dragons.core.TestLogLevels;
import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.user.PermissionLevel;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.util.PathfindingUtil;
import mc.dragons.core.util.PermissionUtil;

public class ExperimentCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		Player player = null;
		User user = null;
		
		if(sender instanceof Player) {
			player = (Player) sender;
			user = UserLoader.fromPlayer(player);
			if(!PermissionUtil.verifyActivePermissionLevel(user, PermissionLevel.ADMIN, true)) return true;
		}
		
		if(args.length == 0) {
			sender.sendMessage(ChatColor.GRAY + "Permission-related experiments");
			sender.sendMessage(ChatColor.YELLOW + "/experiment addperm <node>");
			sender.sendMessage(ChatColor.YELLOW + "/experiment delperm <node>");
			sender.sendMessage(ChatColor.YELLOW + "/experiment testperm <node>");
			sender.sendMessage(ChatColor.GRAY + "User-related experiments");
			sender.sendMessage(ChatColor.YELLOW + "/experiment listusers");
			sender.sendMessage(ChatColor.YELLOW + "/experiment debugme");
			sender.sendMessage(ChatColor.GRAY + "Entity-related experiments");
			sender.sendMessage(ChatColor.YELLOW + "/experiment pathfind");
			sender.sendMessage(ChatColor.GRAY + "Logging-related experiments");
			sender.sendMessage(ChatColor.YELLOW + "/experiment testrawlog");
			sender.sendMessage(ChatColor.YELLOW + "/experiment testdebuglog");
			sender.sendMessage(ChatColor.YELLOW + "/experiment testinfolog");
			sender.sendMessage(ChatColor.YELLOW + "/experiment testalllog");
			
			return true;
		}
		
		if(args[0].equalsIgnoreCase("addperm")) {
			player.addAttachment(Dragons.getInstance(), args[1], true);
		}
		
		if(args[0].equalsIgnoreCase("delperm")) {
			player.addAttachment(Dragons.getInstance(), args[1], false);
		}
		
		if(args[0].equalsIgnoreCase("testperm")) {
			if(player.hasPermission(args[1])) {
				sender.sendMessage("Yes you have it");
			}
			else {
				sender.sendMessage("No you don't have it");
			}
		}
		
		if(args[0].equalsIgnoreCase("listusers")) {
			for(GameObject gameObject : Dragons.getInstance().getGameObjectRegistry().getRegisteredObjects(GameObjectType.USER)) {
				User u = (User) gameObject;
				sender.sendMessage("- Here's a user: " + u);
				sender.sendMessage("    - name=" + u.getName());
				sender.sendMessage("    - player=" + u.getPlayer());
			}
		}
		
		if(args[0].equalsIgnoreCase("debugme")) {
			user.setDebugging(!user.isDebugging());
			sender.sendMessage("Debugging toggled " + (user.isDebugging() ? "ON" : "OFF"));
		}
		
		if(args[0].equalsIgnoreCase("pathfind")) {
			Location spawnLoc = player.getLocation().add(player.getLocation().getDirection().clone().setY(0).normalize().multiply(10.0));
			LivingEntity e = (LivingEntity) Bukkit.getWorld("undead_forest").spawnEntity(spawnLoc,
					EntityType.VILLAGER);
			Dragons.getInstance().getBridge().setEntityAI(e, false);
			PathfindingUtil.walkToLocation(e, player.getLocation(), 0.2, unused -> {});
		}
		
		if(args[0].equalsIgnoreCase("testrawlog")) {
			SimpleDateFormat sdf = new SimpleDateFormat("k:m:s");
			System.out.println("[" + sdf.format(new Date()) + " INFO]: The quick brown fox jumped over the lazy dog");
		}
		
		if(args[0].equalsIgnoreCase("testdebuglog")) {
			Dragons.getInstance().getLogger().fine("The quick brown fox jumped over the lazy dog");
		}
		
		if(args[0].equalsIgnoreCase("testinfolog")) {
			Dragons.getInstance().getLogger().info("The quick brown fox jumped over the lazy dog");
		}
		
		if(args[0].equalsIgnoreCase("testalllog")) {
			Dragons.getInstance().getLogger().setLevel(Level.ALL);
			TestLogLevels.onEnable(Dragons.getInstance());
		}
		
		return true;
	}

	
	
}
