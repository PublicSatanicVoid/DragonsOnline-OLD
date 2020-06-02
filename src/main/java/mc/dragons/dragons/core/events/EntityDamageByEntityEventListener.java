package mc.dragons.dragons.core.events;

import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.gameobject.GameObjectType;
import mc.dragons.dragons.core.gameobject.ProgressBarUtil;
import mc.dragons.dragons.core.gameobject.item.Item;
import mc.dragons.dragons.core.gameobject.loader.ItemLoader;
import mc.dragons.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.dragons.core.gameobject.npc.NPC;
import mc.dragons.dragons.core.gameobject.player.SkillType;
import mc.dragons.dragons.core.gameobject.player.User;
import mc.dragons.dragons.core.gameobject.region.Region;
import mc.dragons.dragons.core.util.MathUtil;

public class EntityDamageByEntityEventListener implements Listener {

	private UserLoader playerLoader;
	private RegionLoader regionLoader;
	private ItemLoader itemLoader;
	private Dragons plugin;
	
	public EntityDamageByEntityEventListener(Dragons instance) {
		playerLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		regionLoader = (RegionLoader) GameObjectType.REGION.<Region>getLoader();
		itemLoader = (ItemLoader) GameObjectType.ITEM.<Item>getLoader();
		plugin = instance;
	}
	
	// TODO: take into account held item & armor.
	// TODO: refactor. Have one double for damageAdd and one double for damageSubtract.
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		Entity target = event.getEntity();
		User userDamager = null;
		NPC npcTarget = null;
		NPC npcDamager = null;
		if(event.getDamager() instanceof Player) {
			Player player = (Player) event.getDamager();
			userDamager = playerLoader.fromPlayer(player);
			event.setDamage(event.getDamage() + userDamager.getSkillLevel(SkillType.MELEE));
			Item heldItem = itemLoader.getItemByItemStack(player.getItemInHand());
			if(heldItem != null) {
				event.setDamage(0.0);
				if(heldItem.hasCooldownRemaining()) {
					userDamager.sendActionBar(ChatColor.RED + "- WAIT - " + MathUtil.round(heldItem.getCooldownRemaining()) + "s -");
					event.setCancelled(true);
					return;
				}
				heldItem.registerUse();
				event.setDamage(event.getDamage() + heldItem.getDamage());
				userDamager.updateSkillProgress(SkillType.MELEE, 1 / Math.max(0.5, player.getLocation().distance(target.getLocation())));
				final User fUserDamager = userDamager;
				BukkitRunnable runnable = new BukkitRunnable() {
					@Override
					public void run() {
						if(!heldItem.hasCooldownRemaining()) {
							fUserDamager.sendActionBar(heldItem.getDecoratedName() + ChatColor.GREEN + " RECHARGED");
							this.cancel();
							return;
						}
						Item currentHeldItem = itemLoader.getItemByItemStack(fUserDamager.p().getItemInHand());
						if(!currentHeldItem.equals(heldItem)) return;
						fUserDamager.sendActionBar(heldItem.getDecoratedName() + " " + ProgressBarUtil.getCountdownBar(heldItem.getCooldownRemaining() / heldItem.getCooldown()));
					}
				};
				runnable.runTaskTimer(plugin, 0, 5);
			}
		}
		else {
			List<MetadataValue> handle = event.getDamager().getMetadata("handle");
			if(handle.size() > 0) {
				npcDamager = (NPC) handle.get(0).value();
			}
		}
		
		List<MetadataValue> handle = target.getMetadata("handle");
		if(handle.size() > 0) {
			npcTarget = (NPC) handle.get(0).value();
		}
		
		Set<Region> regions = regionLoader.getRegionsByLocationXZ(target.getLocation());
		
		if(target instanceof Player) {
			if(userDamager == null && npcDamager != null) { // player was damaged by entity
				event.setDamage(event.getDamage() + 1.2 * npcDamager.getLevel());
			}
			else if(userDamager != null) {
				event.setDamage(event.getDamage() + Math.random() * userDamager.getSkillLevel(SkillType.MELEE));
			}
			User user = playerLoader.fromPlayer((Player) target);
			ItemStack targetHeldItemStack = ((Player) target).getItemInHand();
			Item targetHeldItem = itemLoader.getItemByItemStack(targetHeldItemStack);
			double armor = 0.0;
			if(targetHeldItem != null) {
				armor += targetHeldItem.getArmor();
			}
			for(ItemStack itemStack : user.p().getInventory().getArmorContents()) {
				Item item = itemLoader.getItemByItemStack(itemStack);
				if(item != null) {
					armor += item.getArmor();
				}
			};
			event.setDamage(Math.max(0.0, event.getDamage() - 0.5 * Math.random() * user.getSkillLevel(SkillType.DEFENSE)) - armor);
			if(event.getDamager() instanceof Player) { // PVP
				for(Region region : regions) {
					if(!Boolean.valueOf(region.getFlags().getString("pvp"))) {
						event.setCancelled(true);
						userDamager.sendActionBar(ChatColor.GRAY + "PVP is disabled in this region.");
						return;
					}
				}
			}
			else { // EVP = PVE
				for(Region region : regions) {
					if(!Boolean.valueOf(region.getFlags().getString("pve"))) {
						event.setCancelled(true);
						return;
					}
				}
			}
		}
		else if(npcTarget != null) { // PVE
			if(event.getDamager() instanceof Player) {
				for(Region region : regions) {
					if(!Boolean.valueOf(region.getFlags().getString("pve"))) {
						event.setCancelled(true);
						userDamager.sendActionBar(ChatColor.GRAY + "PVE is disabled in this region.");
						return;
					}
				}
			}
			npcTarget.updateHealthBar();
		}
	}
}
