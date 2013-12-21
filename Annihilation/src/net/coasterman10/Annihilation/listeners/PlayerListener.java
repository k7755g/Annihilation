package net.coasterman10.Annihilation.listeners;

import java.io.IOException;

import net.coasterman10.Annihilation.Annihilation;
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

	public PlayerListener(Annihilation plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
		mapManager = plugin.getMapManager();
		teamManager = plugin.getTeamManager();
		kitManager = plugin.getKitManager();
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player player = e.getPlayer();
		Team team = teamManager.getTeamWithPlayer(player.getName());
		if (team == null)
			e.setRespawnLocation(mapManager.getLobbySpawnPoint());
		else if (!team.isAlive() || plugin.getPhase() == 0)
			e.setRespawnLocation(mapManager.getLobbySpawnPoint());
		else {
			e.setRespawnLocation(mapManager.getSpawnPoint(team.getName()));
			plugin.getKitManager().getKit(player).getKitClass().give(player);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		Team team = teamManager.getTeamWithPlayer(player.getName());
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

		plugin.getVotingManager().setCurrentForPlayers(player);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();

		try {
			plugin.getStatsManager().setValue(StatType.DEATHS, p,
					plugin.getStatsManager().getStat(StatType.DEATHS, p) + 1);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if (p.getKiller() != null) {
			try {
				plugin.getStatsManager().setValue(
						StatType.KILLS,
						p.getKiller(),
						plugin.getStatsManager().getStat(StatType.DEATHS,
								p.getKiller()) + 1);
				e.setDeathMessage(createDeathMessage(p, p.getKiller(),
						e.getDeathMessage()));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else
			e.setDeathMessage(createDeathMessage(p, e.getDeathMessage()));
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

	private String createDeathMessage(Player victim, Player killer,
			String message) {
		Team victimTeam = teamManager.getTeamWithPlayer(victim.getName());
		Team killerTeam = teamManager.getTeamWithPlayer(killer.getName());
		String victimColor = victimTeam != null ? victimTeam.getPrefix()
				: ChatColor.DARK_PURPLE.toString();
		String killerColor = killerTeam != null ? killerTeam.getPrefix()
				: ChatColor.DARK_PURPLE.toString();

		String victimName = victimColor + victim.getName() + ChatColor.GRAY;
		String killerName = killerColor + killer.getName();

		String deathMessage = message.replace(victim.getName(), victimName);
		deathMessage = deathMessage.replace(killer.getName(), killerName);
		deathMessage.replace("slain", "killed");

		return deathMessage;
	}

	private String createDeathMessage(Player victim, String message) {
		Team victimTeam = teamManager.getTeamWithPlayer(victim.getName());
		String victimColor = victimTeam != null ? victimTeam.getPrefix()
				: ChatColor.DARK_PURPLE.toString();

		String victimName = victimColor + victim.getName() + ChatColor.GRAY;

		String deathMessage = message.replace(victim.getName(), victimName);
		deathMessage.replace("slain", "killed");

		return deathMessage;
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		if (plugin.isEmptyColumn(event.getBlock().getLocation()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if (plugin.isEmptyColumn(event.getBlock().getLocation()))
			event.setCancelled(true);
	}
}
