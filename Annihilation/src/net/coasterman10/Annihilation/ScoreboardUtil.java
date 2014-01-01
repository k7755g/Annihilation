package net.coasterman10.Annihilation;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardUtil {
	private static final Scoreboard board;
	private static final Objective objective;
	private static final HashMap<String, Integer> stats;

	static {
		board = Bukkit.getScoreboardManager().getNewScoreboard();
		objective = board.registerNewObjective("obj", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		stats = new HashMap<String, Integer>();
	}
	
	public static void showForPlayers(Player... players) {
		for (Player p : players)
			p.setScoreboard(board);
	}

	public static void setTitle(String title) {
		objective.setDisplayName(title);
	}

	public static void setScore(String name, int score) {
		stats.put(name, score);
		objective.getScore(Bukkit.getOfflinePlayer(name)).setScore(score);
	}

	public static void removeScore(String name) {
		board.resetScores(Bukkit.getOfflinePlayer(name));
	}
	
	public static void registerTeam(String name, ChatColor color) {
		Team t = board.registerNewTeam(name);
		t.setAllowFriendlyFire(false);
		t.setPrefix(color.toString());
	}
	
	public static void addPlayerToTeam(Player p, String team) {
		if (board.getTeam(team) != null)
			board.getTeam(team).addPlayer(p);
	}
}
