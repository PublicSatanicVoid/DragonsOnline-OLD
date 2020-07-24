package mc.dragons.addons.npc;

import org.bukkit.Location;

import mc.dragons.core.addon.Addon;
import mc.dragons.core.addon.AddonType;
import mc.dragons.core.gameobject.GameObject;
import mc.dragons.core.gameobject.npc.NPC;

public abstract class NPCAddon implements Addon {

	public final AddonType getType() {
		return AddonType.NPC;
	}

	public abstract void onMove(NPC npc, Location loc);
	public abstract void onTakeDamage(NPC on, GameObject from, double amount);
	public abstract void onDealDamage(NPC from, GameObject to, double amount);
	public abstract void onDeath(NPC gameObject);
	
}
