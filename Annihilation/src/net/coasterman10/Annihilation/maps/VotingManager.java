package net.coasterman10.Annihilation.maps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.ScoreboardUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class VotingManager {
	private final Annihilation plugin;
	private final HashSet<String> maps = new HashSet<String>();
	private final HashMap<String, String> votes = new HashMap<String, String>();
	private boolean running = false;
	
	public VotingManager(Annihilation plugin) {
		this.plugin = plugin;
	}
	
	public void start() {
		maps.clear();
		votes.clear();
		
		ScoreboardUtil.removeAllScores();
		String title = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Voting";
		ScoreboardUtil.setTitle(title);

		for (String map : plugin.getMapManager().getRandomMaps()) {
			maps.add(map);
			ScoreboardUtil.setScore(map, 1);
			ScoreboardUtil.setScore(map, 0);
		}
		
		running = true;
	}

	public boolean vote(CommandSender voter, String vote) {
		for (String map : maps) {
			if (vote.equalsIgnoreCase(map)) {
				votes.put(voter.getName(), map);
				ScoreboardUtil.setScore(map, countVotes(map));
				voter.sendMessage(ChatColor.GOLD + "You voted for "
						+ ChatColor.WHITE + map);
				return true;
			}
		}
		voter.sendMessage(vote + ChatColor.RED + " is not a valid map");
		return false;
	}

	public String getWinner() {
		String winner = null;
		Integer highest = -1;
		for (String map : maps) {
			int totalVotes = countVotes(map);
			if (totalVotes > highest) {
				winner = map;
				highest = totalVotes;
			}
		}
		return winner;
	}
	
	public void end() {
		running = false;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public Set<String> getMaps() {
		return maps;
	}

	private int countVotes(String map) {
		int total = 0;
		for (String vote : votes.values())
			if (vote.equals(map))
				total++;
		return total;
	}
}
