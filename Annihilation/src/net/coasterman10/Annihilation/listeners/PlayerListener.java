package net.coasterman10.Annihilation.listeners;

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.chat.ChatUtil;
import net.coasterman10.Annihilation.chat.DeathMessageFormatter;
import net.coasterman10.Annihilation.kits.KitManager;
import net.coasterman10.Annihilation.kits.KitType;
import net.coasterman10.Annihilation.maps.MapManager;
import net.coasterman10.Annihilation.stats.StatType;
import net.coasterman10.Annihilation.teams.Team;
import net.coasterman10.Annihilation.teams.TeamManager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
	private final Annihilation plugin;
	private final MapManager mapManager;
	private final TeamManager teamManager;
	private final KitManager kitManager;
	private final DeathMessageFormatter deathMessages;

	public PlayerListener(Annihilation plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
		mapManager = plugin.getMapManager();
		teamManager = plugin.getTeamManager();
		kitManager = plugin.getKitManager();
		deathMessages = new DeathMessageFormatter(teamManager);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player player = e.getPlayer();
		Team team = teamManager.getTeamWithPlayer(player);
		if (team == null)
			e.setRespawnLocation(mapManager.getLobbySpawnPoint());
		else if (!team.isAlive() || plugin.getPhase() == 0)
			e.setRespawnLocation(mapManager.getLobbySpawnPoint());
		else {
			e.setRespawnLocation(mapManager.getSpawnPoint(team.getName()));
			plugin.getKitManager()
					.getKit(player)
					.getKitClass()
					.give(player,
							teamManager.getTeamWithPlayer(player).getName());
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		Team team = teamManager.getTeamWithPlayer(player);
		if (team == null)
			player.teleport(mapManager.getLobbySpawnPoint());
		else if (!team.isAlive() || plugin.getPhase() == 0)
			player.teleport(mapManager.getLobbySpawnPoint());
		else
			player.teleport(mapManager.getSpawnPoint(team.getName()));

		if (plugin.useMysql) {
			plugin.getDatabaseHandler()
					.query("INSERT IGNORE INTO `annihilation` (`username`, `kills`, `deaths`, `wins`, `losses`, `nexus_damage`) VALUES "
							+ "('"
							+ player.getName()
							+ "', '0', '0', '0', '0', '0');");
		}

		if (plugin.getPhase() == 0)
			plugin.getVotingManager().setCurrentForPlayers(player);
		else
			plugin.getIngameScoreboardmanager().setCurrentForPlayers(player);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();

		plugin.getStatsManager().setValue(StatType.DEATHS, p,
				plugin.getStatsManager().getStat(StatType.DEATHS, p) + 1);

		if (p.getKiller() != null) {
			plugin.getStatsManager().setValue(
					StatType.KILLS,
					p.getKiller(),
					plugin.getStatsManager().getStat(StatType.DEATHS,
							p.getKiller()) + 1);
			e.setDeathMessage(deathMessages.formatDeathMessage(p,
					p.getKiller(), e.getDeathMessage()));
		} else
			e.setDeathMessage(deathMessages.formatDeathMessage(p,
					e.getDeathMessage()));
		e.setDroppedExp(p.getTotalExperience());
	}

	@EventHandler
	public void onPlayerAttack(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		if (damager instanceof Player) {
			Player attacker = (Player) damager;
			if (kitManager.getKit(attacker) == KitType.WARRIOR) {
				ItemStack hand = attacker.getItemInHand();
				if (hand != null) {
					String lowercaseName = hand.getType().toString()
							.toLowerCase();
					if (lowercaseName.contains("sword")
							|| lowercaseName.contains("axe"))
						e.setDamage(e.getDamage() + 1.0);
				}
			}
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		if (plugin.isEmptyColumn(e.getBlock().getLocation()))
			e.setCancelled(true);
	}

	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		if (plugin.isEmptyColumn(e.getBlock().getLocation()))
			e.setCancelled(true);
		for (Team t : plugin.getTeamManager().getTeams()) {
			if (t.getNexus().getLocation().equals(e.getBlock().getLocation())) {
				e.setCancelled(true);
				Player breaker = e.getPlayer();
				Team attacker = teamManager.getTeamWithPlayer(breaker);
				if (t == attacker)
					breaker.sendMessage(ChatColor.AQUA
							+ "You can't damage your own nexus");
				else if (plugin.getPhase() == 1)
					breaker.sendMessage(ChatColor.AQUA
							+ "Nexuses are invincible in phase 1");
				else {
					t.getNexus().damage();
					plugin.getStatsManager().incrementStat(
							StatType.NEXUS_DAMAGE, breaker);
					for (Player p : attacker.getPlayers())
						p.sendMessage(ChatUtil.nexusBreakMessage(breaker,
								attacker, t));
					plugin.getIngameScoreboardmanager().updateScore(t);
					if (t.getNexus().getHealth() == 0) {
						ChatUtil.nexusDestroyed(attacker, t);
					}
					for (Player p : t.getPlayers()) {
						plugin.getStatsManager().incrementStat(StatType.LOSSES,
								p);
					}
				}
			}
		}
	}
}
