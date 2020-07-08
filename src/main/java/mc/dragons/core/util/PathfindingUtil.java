package mc.dragons.core.util;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mc.dragons.core.Dragons;

/**
 * 
 * @author funkemunky
 *
 */
public class PathfindingUtil {
	public static void walkToLocation(Entity entity, Location location, double speed, Consumer<Entity> callback) {
        double[] entityBB = Dragons.getInstance().getBridge().getAABB(entity);
        double entityHeight = entityBB[4] - entityBB[1];
		new BukkitRunnable() {
        	@Override
            public void run() {
            	Location curr = entity.getLocation();
                if(entity.isValid() && entity.getLocation().distanceSquared(location) > 1.0) {
                	
                	Location feet = curr.clone().subtract(0, entityHeight, 0);
                	Location ground = getClosestGroundXZ(feet).add(0, 1, 0);
                	
                    float yaw = getRotations(entity.getLocation(), location)[0];
                    //double up = Math.ceil(feet.getY()) - feet.getY();
                    double up = ground.getY() - feet.getY();
                    /*if(!feet.clone().add(0, up, 0).getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
                    	up *= -1;
                    }*/
                    Bukkit.getLogger().info("y="+curr.getY() + ", fy=" + feet.getY() + ", gy=" + ground.getY() + ", at="+feet.getBlock().getType().toString() + "[solid=" + feet.getBlock().getType().isSolid() + "]; "
                    		+ "below=" + feet.getBlock().getRelative(BlockFace.DOWN).getType() + "[solid=" + feet.getBlock().getRelative(BlockFace.DOWN).getType().isSolid() + "], "
                    		+ "up=" + up + ", entHeight=" + entityHeight);
                    
                    Vector move = location.clone().subtract(curr).toVector().normalize().multiply(speed).setY(0);
                    //Vector direction = new Vector(-Math.sin(yaw * Math.PI / 180.0F) * (float) 1 * 0.5F, 0, Math.cos(yaw * Math.PI / 180.0F) * (float) 1 * 0.5F).multiply(speed);
                    move.add(new Vector(0, up, 0));
                    
                    Location to = curr.add(move);
                    Location feetTo = feet.add(move);
                    to.setYaw(yaw);
                    
                    if(!feetTo.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
                    	to.add(0, -1, 0);
                    	Bukkit.getLogger().info("pathfind step down");
                    }
                    
                    if(feetTo.clone().add(move).getBlock().getType().isSolid()) {
                    	to.add(0, 1, 0);
                    	Bukkit.getLogger().info("pathfind step up");
                    }
                    
                    entity.teleport(to.add(0, 0, 0));
                    entity.setVelocity(move);
                } else {
                    this.cancel();
                    entity.setVelocity(new Vector(0, 0, 0));
                    callback.accept(entity);
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
		int n = 0;;
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
