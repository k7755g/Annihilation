package net.coasterman10.Annihilation.teams;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.StatBoard;
import net.coasterman10.Annihilation.commands.TeamCommand;

public class TeamManager {
	private final List<Team> teams = new ArrayList<Team>();
	private final StatBoard teamStats;

	public TeamManager(Annihilation plugin) {
		new TeamCommand(plugin, this);
		teamStats = new StatBoard(plugin.getServer().getScoreboardManager());
		teams.add(new Team(75, TeamName.RED));
		teams.add(new Team(75, TeamName.YELLOW));
		teams.add(new Team(75, TeamName.GREEN));
		teams.add(new Team(75, TeamName.BLUE));
		for (Team team : teams) {
			updateScore(team);
		}
	}

	private void updateScore(Team team) {
		teamStats.setScore(team.getName() + " Nexus:", team.getNexusHealth());
	}
	
	public void setScoreboardForPlayers(Player... players) {
		teamStats.showForPlayers(players);
	}

	public boolean areFriendly(String p1, String p2) {
		if (inLobby(p1) || inLobby(p2))
			return false;
		return getTeamWithPlayer(p1) == getTeamWithPlayer(p2);
	}

	public boolean inLobby(String player) {
		return getTeamWithPlayer(player) == null;
	}

	public Team getTeamWithPlayer(String player) {
		for (Team t : teams) {
			if (t.hasPlayer(player))
				return t;
		}
		return null;
	}

	public Team getTeam(TeamName name) {
		for (Team t : teams) {
			if (t.getName() == name)
				return t;
		}
		return null;
	}

	public List<Team> getTeams() {
		return teams;
	}

	public void setMapName(String name) {
		String GOLD = ChatColor.GOLD.toString();
		String BOLD = ChatColor.BOLD.toString();
		teamStats.setTitle(GOLD + BOLD + "Map: " + name);
	}

	public void showScoreboard() {
		teamStats.show();
	}
}
