package mc.dragons.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.loader.QuestLoader;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.quest.Quest;
import mc.dragons.core.gameobject.user.PermissionLevel;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.util.PermissionUtil;

public class ReloadQuestsCommand implements CommandExecutor  {
	
	private QuestLoader questLoader;
	
	public ReloadQuestsCommand() {
		questLoader = (QuestLoader) GameObjectType.QUEST.<Quest>getLoader();
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
		
		sender.sendMessage(ChatColor.GREEN + "Reloading quests...");
		
		new BukkitRunnable() {
			@Override
			public void run() {
				questLoader.loadAll(true);
				sender.sendMessage(ChatColor.GREEN + "All quests have been reloaded!");
			}
		}.runTaskLater(Dragons.getInstance(), 1L);
		
		return true;
	}
}
