package mc.dragons.core.util;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mc.dragons.core.Dragons;

/**
 * 
 * @author funkemunky, modified by Rick
 *
 */
public class PathfindingUtil {
	public static void walkToLocation(Entity entity, Location location, double speed, Consumer<Entity> callback) {
		entity.teleport(getClosestGroundXZ(entity.getLocation()).add(0, 1, 0));
		new BukkitRunnable() {
        	@Override
            public void run() {
            	Location curr = entity.getLocation();
                if(entity.isValid() && entity.getLocation().distanceSquared(location) > 1.0) {
                	Vector direction = location.clone().subtract(curr).toVector().normalize().multiply(speed).setY(0);
                	Location to = curr.clone().add(direction);
                	double groundY = getClosestGroundXZ(to).getY();
                	to.setY(groundY + 1);
                	entity.setVelocity(direction);
                	entity.teleport(to);
                	
                	Dragons.getInstance().getLogger().finest("PATHFIND " + StringUtil.entityToString(entity) + ": currY=" + curr.getY() + ", toY=" + to.getY() + ",toBlock=" + to.getBlock().getType());
                } else {
                    this.cancel();
                    entity.setVelocity(new Vector(0, 0, 0));
                    if(callback != null) {
                    	callback.accept(entity);
                    }
                }
            }
        }.runTaskTimer(Dragons.getInstance(), 0L, 1L);
    }
	
	public static float[] getRotations(Location one, Location two) {
        double diffX = two.getX() - one.getX();
        double diffZ = two.getZ() - one.getZ();
        double diffY = two.getY() + 2.0 - 0.4 - (one.getY() + 2.0);
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-Math.atan2(diffY, dist) * 180.0 / Math.PI);
        return new float[]{yaw, pitch};
    }
	
	public static Location getClosestGroundXZ(Location start) {
		Block nBelow = start.getBlock();
		Block nAbove = start.getBlock();
		int n = 0;
		while(!nBelow.getType().isSolid() && !nAbove.getType().isSolid()) {
			n++;
			nBelow = nBelow.getRelative(BlockFace.DOWN);
			nAbove = nAbove.getRelative(BlockFace.UP);
			if(n > 10) break;
		}
		if(nBelow.getType().isSolid()) return nBelow.getLocation();
		if(nAbove.getType().isSolid()) return nAbove.getLocation();
		return start.getBlock().getLocation();
		
	}
}
