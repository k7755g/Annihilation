package net.coasterman10.Annihilation.chat;

import java.util.HashMap;

import net.coasterman10.Annihilation.teams.Team;
import net.coasterman10.Annihilation.teams.TeamManager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DeathMessageFormatter {
	private TeamManager teamManager;

	private static final HashMap<String, String> table = new HashMap<String, String>();

	static {
		table.put("slain", "killed");
		table.put("out of the world", "off the map");
	}

	public DeathMessageFormatter(TeamManager teamManager) {
		this.teamManager = teamManager;
	}

	public String formatDeathMessage(Player victim, Player killer,
			String original) {
		Team killerTeam = teamManager.getTeamWithPlayer((Player) killer);
		String killerColor = killerTeam != null ? killerTeam.getPrefix()
				: ChatColor.DARK_PURPLE.toString();
		String killerName = killerColor + killer.getName() + ChatColor.GRAY;

		String message = ChatColor.GRAY + formatDeathMessage(victim, original);
		message = message.replace(killer.getName(), killerName);

		return message;
	}

	public String formatDeathMessage(Player victim, String original) {
		Team victimTeam = teamManager.getTeamWithPlayer(victim);
		String victimColor = victimTeam != null ? victimTeam.getPrefix()
				: ChatColor.DARK_PURPLE.toString();
		String victimName = victimColor + victim.getName() + ChatColor.GRAY;

		String message = ChatColor.GRAY + original;
		message = message.replace(victim.getName(), victimName);

		return message;
	}
}
