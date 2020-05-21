package mc.dragons.dragons.core.bridge.impl;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import mc.dragons.dragons.core.Dragons;
import mc.dragons.dragons.core.bridge.Bridge;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand.EnumClientCommand;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_8_R3.PlayerConnection;

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
}
