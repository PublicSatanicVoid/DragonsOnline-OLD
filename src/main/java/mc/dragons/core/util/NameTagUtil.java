package mc.dragons.core.util;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class NameTagUtil {
	public static ArmorStand getArmorStandNameTag(Entity entity, String nameTag) {
		Entity nameTagFix = entity.getWorld().spawnEntity(entity.getLocation(), EntityType.ARMOR_STAND);
		nameTagFix.setCustomName(nameTag);
		nameTagFix.setCustomNameVisible(true);
		nameTagFix.setGravity(false);
		ArmorStand armorStand = (ArmorStand) nameTagFix;
		armorStand.setVisible(false);
		armorStand.setAI(false);
		armorStand.setCollidable(false);
		armorStand.setInvulnerable(true);
		armorStand.setSmall(true);
		return armorStand;
	}
}
