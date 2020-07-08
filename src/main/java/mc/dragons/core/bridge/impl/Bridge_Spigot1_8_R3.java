package mc.dragons.core.bridge.impl;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import mc.dragons.core.bridge.Bridge;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand.EnumClientCommand;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;

public class Bridge_Spigot1_8_R3 implements Bridge {

	@Override
	public String getAPIVersion() {
		return "1_8_R3";
	}
	
	@Override
	public void sendActionBar(Player player, String message) {
		PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message), (byte)2);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

	@Override
	public void sendTitle(Player player, ChatColor titleColor, String title, ChatColor subtitleColor, String subtitle, int fadeInTime, int showTime, int fadeOutTime) {
		PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(EnumTitleAction.TITLE, ChatSerializer.a("{\"text\":\""+title+"\",\"color\":\""+titleColor.name().toLowerCase()+"\"}"), fadeInTime, showTime, fadeOutTime);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(titlePacket);
		
		PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, ChatSerializer.a("{\"text\":\""+subtitle+"\",\"color\":\""+subtitleColor.name().toLowerCase()+"\"}"), fadeInTime, showTime, fadeOutTime);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(subtitlePacket);
	}

	@Override
	public void respawnPlayer(Player player) {
		((CraftPlayer) player).getHandle().playerConnection.a(new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN));
	}

	@Override
	public void setEntityAI(Entity entity, boolean ai) {
		NBTEditor.set(entity, (byte) (!ai ? 1 : 0), "NoAI");
	}

	@Override
	public void setItemStackUnbreakable(ItemStack itemStack, boolean unbreakable) {
		NBTEditor.set(itemStack, unbreakable ? 1 : 0, "Unbreakable");
	}
	
	@Override
	public double[] getAABB(Entity entity) {
		net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity)entity).getHandle();
		AxisAlignedBB aabb = nmsEntity.getBoundingBox();
		return new double[] { aabb.a, aabb.b, aabb.c, aabb.d, aabb.e, aabb.f };
	}

	@Override
	public void setEntityInvulnerable(Entity entity, boolean immortal) {
		NBTEditor.set(entity, (byte) (immortal ? 1 : 0), "Invulnerable");
	}

}
