package net.coasterman10.Annihilation;

import org.bukkit.entity.Player;

import net.coasterman10.Annihilation.teams.Team;
import net.coasterman10.Annihilation.teams.TeamManager;

public class IngameScoreboardManager {
	private final TeamManager teamManager;
	private final StatBoard nexusBoard;

	public IngameScoreboardManager(Annihilation plugin) {
		teamManager = plugin.getTeamManager();
		nexusBoard = new StatBoard(plugin.getServer().getScoreboardManager());

		for (Team t : teamManager.getTeams()) {
			updateScore(t);
		}
		nexusBoard.show();
	}

	public void setTitle(String title) {
		nexusBoard.setTitle(title);
	}

	public void setCurrentForPlayers(Player... players) {
		nexusBoard.showForPlayers(players);
	}

	public void updateScore(Team team) {
		if (team.getNexus() != null) {
			nexusBoard.setScore(team.getName() + " Nexus", team.getNexus()
					.getHealth());
			if (team.getNexus().getHealth() == 0)
				nexusBoard.removeScore(team.getName() + " Nexus");
		}
	}
}
