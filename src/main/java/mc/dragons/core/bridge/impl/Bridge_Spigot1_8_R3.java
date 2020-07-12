package mc.dragons.core.bridge.impl;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import mc.dragons.core.Dragons;
import mc.dragons.core.bridge.Bridge;
import mc.dragons.core.util.StringUtil;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand.EnumClientCommand;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;

public class Bridge_Spigot1_8_R3 implements Bridge {

	private Logger LOGGER = Dragons.getInstance().getLogger();
	
	@Override
	public String getAPIVersion() {
		return "1_8_R3";
	}
	
	@Override
	public void sendActionBar(Player player, String message) {
		PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message), (byte)2);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		LOGGER.finest("Sent action bar to " + player.getName());
	}

	@Override
	public void sendTitle(Player player, ChatColor titleColor, String title, ChatColor subtitleColor, String subtitle, int fadeInTime, int showTime, int fadeOutTime) {
		PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(EnumTitleAction.TITLE, ChatSerializer.a("{\"text\":\""+title+"\",\"color\":\""+titleColor.name().toLowerCase()+"\"}"), fadeInTime, showTime, fadeOutTime);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(titlePacket);
		
		PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, ChatSerializer.a("{\"text\":\""+subtitle+"\",\"color\":\""+subtitleColor.name().toLowerCase()+"\"}"), fadeInTime, showTime, fadeOutTime);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(subtitlePacket);
		
		LOGGER.finest("Sent title to " + player.getName());
	}

	@Override
	public void respawnPlayer(Player player) {
		((CraftPlayer) player).getHandle().playerConnection.a(new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN));
		LOGGER.finest("Respawned player " + player.getName());
	}

	@Override
	public void setEntityAI(Entity entity, boolean ai) {
		NBTEditor.set(entity, (byte) (!ai ? 1 : 0), "NoAI");
		LOGGER.finest("Set AI on entity " + StringUtil.entityToString(entity) + " to " + ai);
	}

	@Override
	public void setItemStackUnbreakable(ItemStack itemStack, boolean unbreakable) {
		NBTEditor.set(itemStack, unbreakable ? 1 : 0, "Unbreakable");
		LOGGER.finest("Set Unbreakability on item stack " + itemStack + " to " + unbreakable);
	}
	
	@Override
	public double[] getAABB(Entity entity) {
		net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity)entity).getHandle();
		AxisAlignedBB aabb = nmsEntity.getBoundingBox();
		LOGGER.finest("Retrieved AABB of entity " + StringUtil.entityToString(entity));
		return new double[] { aabb.a, aabb.b, aabb.c, aabb.d, aabb.e, aabb.f };
	}

	@Override
	public void setEntityInvulnerable(Entity entity, boolean immortal) {
		NBTEditor.set(entity, (byte) (immortal ? 1 : 0), "Invulnerable");
		LOGGER.finest("Set Invulnerability on entity " + StringUtil.entityToString(entity) + " to " + immortal);
	}

}
