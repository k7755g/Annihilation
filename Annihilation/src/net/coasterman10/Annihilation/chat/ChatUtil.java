package net.coasterman10.Annihilation.chat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.coasterman10.Annihilation.teams.Team;

public class ChatUtil {
	private static final String DARK_AQUA = ChatColor.DARK_AQUA.toString();
	private static final String GRAY = ChatColor.GRAY.toString();

	public static void broadcast(String message) {
		Bukkit.broadcastMessage(message);
	}

	public static void nexusDestroyed(Team attacker, Team victim) {
		broadcast(GRAY + "===============[ " + DARK_AQUA + "Nexus Destroyed"
				+ GRAY + " ]===============");
		broadcast(victim.getFullName() + "'s" + GRAY
				+ " nexus has been destroyed by " + attacker.getFullName());
		broadcast(GRAY + "===============================================");
	}

	public static String nexusBreakMessage(Player breaker, Team attacker,
			Team victim) {
		return colorizeName(breaker, attacker) + GRAY + " has damaged the " + victim + " team's nexus!";
	}

	private static String colorizeName(Player player, Team team) {
		return team.getPrefix() + player.getName();
	}
}
