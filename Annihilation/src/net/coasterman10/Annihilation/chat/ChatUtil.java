package net.coasterman10.Annihilation.chat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.coasterman10.Annihilation.teams.Team;

public class ChatUtil {
	private static final String DARK_AQUA = ChatColor.DARK_AQUA.toString();
	private static final String GRAY = ChatColor.GRAY.toString();
	private static final String RED = ChatColor.RED.toString();

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
		return colorizeName(breaker, attacker) + GRAY + " has damaged the "
				+ victim.getName() + " team's nexus!";
	}

	private static String colorizeName(Player player, Team team) {
		return team.getPrefix() + player.getName();
	}

	public static void phaseMessage(int phase) {
		broadcast(GRAY + "==========[ " + DARK_AQUA + "Progress" + GRAY + " ]==========");
		broadcast(GRAY + "Phase " + phase + " has started");
		switch (phase) {
		case 1: 
			broadcast(GRAY + "Each nexus is invincible for 10 minutes");
			break;
		case 2:
			broadcast(GRAY + "Each nexus is no longer invincible");
			break;
		case 3:
			broadcast(GRAY + "Diamonds will now spawn in the middle");
			break;
		case 4:
			break;
		case 5:
			broadcast(RED + "2x nexus damage");
		}
		broadcast(GRAY + "==============================");
	}

	public static void winMessage(Team winner) {
		broadcast(GRAY + "==========[ " + DARK_AQUA + "End Game" + GRAY + " ]==========");
		broadcast(winner.getFullName() + " wins!");
	}
}
