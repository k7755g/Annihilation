package net.coasterman10.Annihilation;

import java.util.HashSet;
import java.util.Set;

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
	private static final Set<String> stats;

	private final static boolean DEBUG = false;

	static {
		board = Bukkit.getScoreboardManager().getNewScoreboard();
		objective = board.registerNewObjective("obj", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		stats = new HashSet<String>();
	}

	public static void showForPlayers(Player... players) {
		for (Player p : players) {
			p.setScoreboard(board);
			if (DEBUG)
				Bukkit.getLogger().info(
						"Showing scoreboard for player " + p.getName());
		}
	}

	public static void setTitle(String title) {
		objective.setDisplayName(title);
		if (DEBUG)
			Bukkit.getLogger()
					.info("Set scoreboard title to \"" + title + "\"");
	}

	public static void setScore(String name, int score) {
		stats.add(name);
		objective.getScore(Bukkit.getOfflinePlayer(name)).setScore(score);
		if (DEBUG)
			Bukkit.getLogger().info(
					"Set score for \"" + name + "\" to " + score + "");
	}

	public static void removeAllScores() {
		for (String s : stats)
			removeScore(s);
	}

	public static void removeScore(String name) {
		stats.remove(name);
		board.resetScores(Bukkit.getOfflinePlayer(name));
		if (DEBUG)
			Bukkit.getLogger().info("Removed \"" + name + "\" from scoreboard");
	}

	public static void registerTeam(String name, ChatColor color) {
		Team t = board.registerNewTeam(name);
		t.setAllowFriendlyFire(false);
		t.setPrefix(color.toString());
		if (DEBUG)
			Bukkit.getLogger().info(
					"Registered team " + name + " with color " + color.name());
	}

	public static void addPlayerToTeam(Player p, String team) {
		if (board.getTeam(team) != null) {
			board.getTeam(team).addPlayer(Bukkit.getOfflinePlayer(p.getName()));
			if (DEBUG)
				Bukkit.getLogger().info(
						"Added player " + p.getName() + " to team " + team);
		} else if (DEBUG)
			Bukkit.getLogger().info(
					"Tried to add player " + p.getName() + " to team " + team
							+ " but board team is null");
	}
}
