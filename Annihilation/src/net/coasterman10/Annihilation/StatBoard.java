package net.coasterman10.Annihilation;

import java.util.HashMap;

import net.coasterman10.Annihilation.teams.TeamName;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class StatBoard {
	private final Scoreboard board;
	private final Objective objective;
	private final HashMap<String, Integer> stats;

	public StatBoard(ScoreboardManager manager) {
		board = manager.getNewScoreboard();
		objective = board.registerNewObjective("obj", "dummy");
		stats = new HashMap<String, Integer>();
	}

	public void setTitle(String title) {
		objective.setDisplayName(title);
	}

	public void setScore(String name, int score) {
		stats.put(name, score);
		objective.getScore(Bukkit.getOfflinePlayer(name)).setScore(score);
	}

	public int getScore(String name) {
		if (stats.containsKey(name))
			return stats.get(name);
		else
			return 0;
	}

	public void show() {
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}

	public void hide() {
		board.clearSlot(DisplaySlot.SIDEBAR);
	}

	public void showForPlayers(Player... players) {
		for (Player p : players) {
			p.setScoreboard(board);
		}
	}

	public void removeScore(String name) {
		board.resetScores(Bukkit.getOfflinePlayer(name));
	}

	public Scoreboard getScoreboard() {
		return board;
	}

	public void registerTeams(TeamName[] values) {
		for (TeamName t : values) {
			board.registerNewTeam(t.name());
			board.getTeam(t.name()).setAllowFriendlyFire(false);
			board.getTeam(t.name()).setPrefix(t.prefix());
		}
	}
}
