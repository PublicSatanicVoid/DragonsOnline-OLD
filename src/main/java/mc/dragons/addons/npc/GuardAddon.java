package mc.dragons.addons.npc;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import mc.dragons.core.Dragons;
import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.gameobject.loader.NPCLoader;
import mc.dragons.core.gameobject.npc.NPC;
import mc.dragons.core.gameobject.npc.NPC.NPCType;
import mc.dragons.core.util.NameTagUtil;
import mc.dragons.core.util.StringUtil;

public class GuardAddon extends NPCAddon {
	
	private Set<Entity> guards;
	
	public GuardAddon() {
		guards = new HashSet<>();
		new BukkitRunnable() {
			@Override public void run() {
				for(Entity guard : guards) {
					for(Entity test : guard.getNearbyEntities(10, 10, 10)) {
						if(!(test instanceof LivingEntity)) continue;
						NPC npc = NPCLoader.fromBukkit(test);
						if(npc == null) continue;
						if(npc.getNPCType() != NPCType.HOSTILE) continue;
						LivingEntity leTest = (LivingEntity) test;
						for(Entity passenger : guard.getPassengers()) {
							if(passenger.getType() == EntityType.IRON_GOLEM) {
								IronGolem golem = (IronGolem) passenger;
								golem.setTarget((LivingEntity) leTest);
							}
						}
						if(guard instanceof Creature) {
							Creature cGuard = (Creature) guard;
							cGuard.setTarget(leTest);
						}
						LOGGER.fine("Guard " + StringUtil.entityToString(guard) + " is now targeting " + StringUtil.entityToString(test));
						break;
					}
				}
			}
		}.runTaskTimer(Dragons.getInstance(), 0L, 20L * 2);
	}
	
	@Override
	public String getName() {
		return "Guard";
	}

	@Override
	public void initialize(GameObject gameObject) {
		if(!(gameObject instanceof NPC)) return;
		NPC npc = (NPC) gameObject;
		Entity e = npc.getEntity();
		Entity guard = e.getWorld().spawnEntity(e.getLocation(), EntityType.IRON_GOLEM);
		CraftEntity obcGuard = (CraftEntity) guard;
		obcGuard.setInvulnerable(true);
		e.addPassenger(guard);
		e.addPassenger(NameTagUtil.getArmorStandNameTag(e, npc.getDecoratedName())); // Fix name tag disappearing :/
		Attributable att = (Attributable) e;
		new BukkitRunnable() {
			@Override public void run() {
				obcGuard.getHandle().setInvisible(true);
				LivingEntity leGuard = (LivingEntity) guard;
				leGuard.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 100, false, false), true);
			}
		}.runTaskLater(Dragons.getInstance(), 1L);
		LOGGER.fine("Initialized Guard addon on entity " + StringUtil.entityToString(e) + " with golem " + StringUtil.entityToString(guard));
		att.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
		guards.add(e);
	}

	@Override
	public void onMove(NPC npc, Location floc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTakeDamage(NPC on, GameObject from, double amount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDealDamage(NPC from, GameObject to, double amount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeath(NPC gameObject) {
		// TODO Auto-generated method stub
		
	}

}
