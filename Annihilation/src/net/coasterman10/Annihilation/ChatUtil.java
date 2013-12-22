package net.coasterman10.Annihilation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import net.coasterman10.Annihilation.teams.Team;

public class ChatUtil {
	private static final String DARK_AQUA = ChatColor.DARK_AQUA.toString();
	private static final String GRAY = ChatColor.GRAY.toString();

	public static void nexusDestroyed(Team t, Team attacker) {
		broadcast(GRAY + "===============[ " + DARK_AQUA + "Nexus Destroyed"
				+ GRAY + " ]===============");
		broadcast(t.getFullName() + "'s" + GRAY
				+ " Nexus has been destroyed by " + attacker.getFullName());
		broadcast("===============================================");
	}

	public static void broadcast(String message) {
		Bukkit.broadcastMessage(message);
	}
}
