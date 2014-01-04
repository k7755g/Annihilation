package net.coasterman10.Annihilation.commands;

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.AnnihilationTeam;
import net.coasterman10.Annihilation.PlayerMeta;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamCommand implements CommandExecutor {
	private final Annihilation plugin;

	public TeamCommand(Annihilation plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (args.length == 0)
			listTeams(sender);
		else {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Only players can join teams");
			} else {
				joinTeam((Player) sender, args[0]);
			}
		}
		return true;
	}

	private void joinTeam(Player player, String team) {
		PlayerMeta meta = PlayerMeta.getMeta(player);
		if (meta.getTeam() != AnnihilationTeam.NONE) {
			AnnihilationTeam currentTeam = meta.getTeam();
			player.sendMessage(ChatColor.DARK_AQUA + "You are already on "
					+ currentTeam.coloredName());
			return;
		}

		AnnihilationTeam target;
		try {
			target = AnnihilationTeam.valueOf(team.toUpperCase());
		} catch (IllegalArgumentException e) {
			player.sendMessage(ChatColor.RED + "\"" + team
					+ "\" is not a valid team name!");
			listTeams(player);
			return;
		}

		player.sendMessage(ChatColor.DARK_AQUA + "You joined "
				+ target.coloredName());
		meta.setTeam(target);

		if (plugin.getPhase() > 0) {
			Annihilation.Util.sendPlayerToGame(player);
		}
	}

	private void listTeams(CommandSender sender) {
		sender.sendMessage(ChatColor.GRAY + "============[ "
				+ ChatColor.DARK_AQUA + "Teams" + ChatColor.GRAY
				+ " ]============");
		for (AnnihilationTeam t : AnnihilationTeam.teams()) {
			int size = 0;

			for (Player p : Bukkit.getOnlinePlayers()) {
				PlayerMeta meta = PlayerMeta.getMeta(p);
				if (meta.getTeam() == t)
					size++;
			}

			if (size != 1) {
				sender.sendMessage(t.coloredName() + " - " + size + " players");
			} else {
				sender.sendMessage(t.coloredName() + " - " + size + " player");
			}
		}
		sender.sendMessage(ChatColor.GRAY + "===============================");
	}
}
