package net.coasterman10.Annihilation.teams;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.commands.TeamCommand;

public class TeamManager {
	private final Annihilation plugin;
	private final List<Team> teams = new ArrayList<Team>();

	public TeamManager(Annihilation plugin) {
		new TeamCommand(plugin, this);
		this.plugin = plugin;
		reset();
	}

	public boolean areFriendly(Player p1, Player p2) {
		if (inLobby(p1) || inLobby(p2))
			return false;
		return getTeamWithPlayer(p1) == getTeamWithPlayer(p2);
	}

	public boolean inLobby(Player player) {
		return getTeamWithPlayer(player) == null;
	}

	public Team getTeamWithPlayer(Player breaker) {
		for (Team t : teams) {
			if (t.hasPlayer(breaker))
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
	
	public void checkWin() {
		int alive = 0;
		Team winner = null;
		for (Team t : teams) {
			if (t.isAlive()) {
				alive++;
				winner = t;
			}
		}
		if (alive == 1) {
			plugin.endGame(winner);
		}
	}

	public void reset() {
		teams.clear();
		teams.add(new Team(TeamName.RED));
		teams.add(new Team(TeamName.YELLOW));
		teams.add(new Team(TeamName.GREEN));
		teams.add(new Team(TeamName.BLUE));
	}
}
