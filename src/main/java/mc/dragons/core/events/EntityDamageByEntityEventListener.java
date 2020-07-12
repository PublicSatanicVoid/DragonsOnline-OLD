package mc.dragons.core.events;

import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObjectType;
import mc.dragons.core.gameobject.item.Item;
import mc.dragons.core.gameobject.loader.ItemLoader;
import mc.dragons.core.gameobject.loader.NPCLoader;
import mc.dragons.core.gameobject.loader.RegionLoader;
import mc.dragons.core.gameobject.loader.UserLoader;
import mc.dragons.core.gameobject.npc.NPC;
import mc.dragons.core.gameobject.npc.NPCConditionalActions.NPCTrigger;
import mc.dragons.core.gameobject.region.Region;
import mc.dragons.core.gameobject.user.SkillType;
import mc.dragons.core.gameobject.user.User;
import mc.dragons.core.util.MathUtil;
import mc.dragons.core.util.ProgressBarUtil;
import mc.dragons.core.util.StringUtil;

public class EntityDamageByEntityEventListener implements Listener {
	private Logger LOGGER = Dragons.getInstance().getLogger();

	//private UserLoader userLoader;
	private RegionLoader regionLoader;
	//private ItemLoader itemLoader;
	private Dragons plugin;
	
	public EntityDamageByEntityEventListener(Dragons instance) {
		//userLoader = (UserLoader) GameObjectType.USER.<User>getLoader();
		regionLoader = (RegionLoader) GameObjectType.REGION.<Region>getLoader();
		//itemLoader = (ItemLoader) GameObjectType.ITEM.<Item>getLoader();
		plugin = instance;
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		
		LOGGER.finer("Damage event on " + StringUtil.entityToString(event.getEntity()) + " by " + StringUtil.entityToString(event.getDamager()));
		
		Entity damager = event.getDamager();
		User userDamager = null;
		NPC npcDamager = null;
		if(damager instanceof Player) {
			userDamager = UserLoader.fromPlayer((Player) damager);
		}
		else if(damager instanceof Arrow) {
			Arrow arrow = (Arrow) damager;
			if(arrow.getShooter() instanceof Entity) {
				npcDamager = NPCLoader.fromBukkit((Entity) arrow.getShooter());
			}
		} else {
			npcDamager = NPCLoader.fromBukkit(damager);
		}
		
		Entity target = event.getEntity();
		User userTarget = null;
		NPC npcTarget = null;
		if(target instanceof Player) {
			userTarget = UserLoader.fromPlayer((Player) target);
		}
		else {
			npcTarget = NPCLoader.fromBukkit(target);
			if(npcTarget != null) {
				if(npcTarget.isImmortal()) {
					event.setCancelled(true);
					if(userDamager != null) {
						Item item = ItemLoader.fromBukkit(userDamager.getPlayer().getItemInHand());
						if(item != null) {
							if(item.getClassName().equals("Special:ImmortalOverride")) {
								npcTarget.getEntity().remove();
								plugin.getGameObjectRegistry().removeFromDatabase(npcTarget);
								userDamager.getPlayer().sendMessage(ChatColor.GREEN + "Removed NPC successfully.");
								return;
							}
						}
					}
					npcTarget.updateHealthBar();
					return;
				}
			}
		}
		
		double distance = damager.getLocation().distance(target.getLocation());
		
		double damage = 0.0;
		
		if(userDamager == null && npcDamager == null || userTarget == null && npcTarget == null || npcDamager != null && npcTarget != null) return; // Too many nulls to care, or they're both NPCs
		
		if(userDamager != null) {
			if(userDamager.isGodMode()) {
				if(npcTarget != null) {
					npcTarget.remove();
				}
				else if(!(target instanceof Player)) {
					target.remove();
				}
				else {
					((Player) target).setHealth(0.0);
				}
				return;
			}
		}
		if(userTarget != null) {
			if(userTarget.isGodMode()) {
				event.setCancelled(true);
				return;
			}
		}
		
		
		//
		// Handle damage adders
		//

		Set<Region> regions = regionLoader.getRegionsByLocationXZ(target.getLocation());
		
		if(userDamager == null) { // NPC damager, user target
			for(Region region : regions) {
				if(!Boolean.valueOf(region.getFlags().getString("pve"))) {
					userTarget.debug("- Cancelled damage due to a region " + region.getName() + " PVE flag = false");
					event.setCancelled(true);
					return;
				}
			}
			double weightedLevelDiscrepancy = Math.max(0, npcDamager.getLevel() - 0.5 * userTarget.getLevel());
			damage += event.getDamage() * (0.25 * weightedLevelDiscrepancy + 1);
		}
		else { // User damager
			
			Item attackerHeldItem = ItemLoader.fromBukkit(userDamager.getPlayer().getInventory().getItemInHand());
			double itemDamage = 0.5;
			if(attackerHeldItem != null) {
				if(attackerHeldItem.hasCooldownRemaining()) {
					userDamager.sendActionBar(ChatColor.RED + "- WAIT - " + MathUtil.round(attackerHeldItem.getCooldownRemaining()) + "s -");
					event.setCancelled(true);
					return;
				}
				attackerHeldItem.registerUse();
				final User fUserDamager = userDamager;
				
				// Cooldown
				new BukkitRunnable() {
					@Override
					public void run() {
						Item currentHeldItem = ItemLoader.fromBukkit(fUserDamager.getPlayer().getItemInHand());
						if(currentHeldItem == null) return;
						if(!currentHeldItem.equals(attackerHeldItem)) return;
						//fUserDamager.sendActionBar(heldItem.getDecoratedName() + " " + ProgressBarUtil.getCountdownBar(heldItem.getCooldownRemaining() / heldItem.getCooldown()));
						double percentRemaining = attackerHeldItem.getCooldownRemaining() / attackerHeldItem.getCooldown();
						//ItemStack before = currentHeldItem.getItemStack();
						//ItemMeta meta = attackerHeldItem.getItemStack().getItemMeta();
						//meta.setDisplayName(attackerHeldItem.getDecoratedName() + ChatColor.DARK_GRAY + " [" + ProgressBarUtil.getCountdownBar(percentRemaining) + ChatColor.DARK_GRAY + "]");
						//attackerHeldItem.getItemStack().setItemMeta(meta);
						String cooldownName = attackerHeldItem.getDecoratedName() + ChatColor.DARK_GRAY + " [" + ProgressBarUtil.getCountdownBar(percentRemaining) + ChatColor.DARK_GRAY + "]";
						fUserDamager.getPlayer().setItemInHand(attackerHeldItem.localRename(cooldownName));
						if(!attackerHeldItem.hasCooldownRemaining()) {
							//fUserDamager.sendActionBar(heldItem.getDecoratedName() + ChatColor.GREEN + " RECHARGED");
							//attackerHeldItem.getItemStack().getItemMeta().setDisplayName(attackerHeldItem.getDecoratedName());
							fUserDamager.getPlayer().setItemInHand(attackerHeldItem.localRename(attackerHeldItem.getDecoratedName()));
							this.cancel();
						}
						//ItemStack after = fUserDamager.p().getItemInHand();
						//if(before != after) {
						//	attackerHeldItem.setItemStack(after);
						//}
					}
				}.runTaskTimer(plugin, 0, 5);
				
				
				itemDamage = attackerHeldItem.getDamage();
				damage += itemDamage;
			}
			
			
			
			if(userTarget == null) { // NPC target
				for(Region region : regions) {
					if(!Boolean.valueOf(region.getFlags().getString("pve"))) {
						event.setCancelled(true);
						userDamager.sendActionBar(ChatColor.GRAY + "PVE is disabled in this region.");
						return;
					}
				}
			} 
			else { // Player target
				for(Region region : regions) {
					if(!Boolean.valueOf(region.getFlags().getString("pvp"))) {
						event.setCancelled(true);
						userDamager.sendActionBar(ChatColor.GRAY + "PVP is disabled in this region.");
						return;
					}
				}
			}
			
			// Melee
			userDamager.incrementSkillProgress(SkillType.MELEE, Math.min(0.5, 1 / distance));
			double randomMelee = Math.random() * userDamager.getSkillLevel(SkillType.MELEE) / distance;
			damage += randomMelee;
			
		}
		
		if(userTarget != null) {
			double randomDefense = Math.random() * userTarget.getSkillLevel(SkillType.DEFENSE);
			damage -= randomDefense;
			
			Item targetHeldItem = ItemLoader.fromBukkit(userTarget.getPlayer().getInventory().getItemInHand());
			double itemDefense = 0.0;
			if(targetHeldItem != null) {
				itemDefense = targetHeldItem.getArmor();
			}
			for(ItemStack i : userTarget.getPlayer().getInventory().getArmorContents()) {
				Item armorItem = ItemLoader.fromBukkit(i);
				if(armorItem != null) {
					itemDefense += armorItem.getArmor();
				}
			}
			double damageBlocked = Math.max(damage - itemDefense, 0.0);
			damage -= itemDefense;
			
			userTarget.incrementSkillProgress(SkillType.DEFENSE, Math.random() * damageBlocked);
		}

		damage = Math.max(0.0, damage);
		event.setDamage(damage);
		
		if(npcTarget != null) {
			npcTarget.updateHealthBar(damage);
			if(userDamager != null) {
				npcTarget.getNPCClass().executeConditionals(NPCTrigger.HIT, userDamager);
			}
		}
	}
}
