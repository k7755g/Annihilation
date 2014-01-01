package net.coasterman10.Annihilation.listeners;

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.AnnihilationTeam;
import net.coasterman10.Annihilation.Kit;
import net.coasterman10.Annihilation.PlayerMeta;
import net.coasterman10.Annihilation.ScoreboardUtil;
import net.coasterman10.Annihilation.bar.BarUtil;
import net.coasterman10.Annihilation.chat.ChatUtil;
import net.coasterman10.Annihilation.maps.MapManager;
import net.coasterman10.Annihilation.stats.StatType;
import net.coasterman10.EnderToolsAPI.EnderToolsAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
	private final Annihilation plugin;
	private final MapManager mapManager;

	public PlayerListener(Annihilation plugin) {
		this.plugin = plugin;
		mapManager = plugin.getMapManager();
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (Bukkit.getPluginManager().getPlugin("EnderToolsAPI") == null)
			return;

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		Block b = e.getClickedBlock();

		boolean shouldCancel = true;

		if (b.getType() == Material.WORKBENCH)
			EnderToolsAPI.openWorkbench(e.getPlayer());
		else if (b.getType() == Material.FURNACE)
			EnderToolsAPI.openFurnace(e.getPlayer());
		else if (b.getType() == Material.BREWING_STAND)
			EnderToolsAPI.openBrewingStand(e.getPlayer());
		else
			shouldCancel = false;

		if (shouldCancel)
			e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player player = e.getPlayer();
		AnnihilationTeam team = PlayerMeta.getMeta(player).getTeam();
		if (team == null)
			e.setRespawnLocation(mapManager.getLobbySpawnPoint());
		else if (!team.getNexus().isAlive() || plugin.getPhase() == 0)
			e.setRespawnLocation(mapManager.getLobbySpawnPoint());
		else {
			e.setRespawnLocation(team.getRandomSpawn());
			PlayerMeta.getMeta(player).getKit().give(player, team);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		AnnihilationTeam team = PlayerMeta.getMeta(player).getTeam();
		if (team == null)
			player.teleport(mapManager.getLobbySpawnPoint());
		else if (!team.getNexus().isAlive() || plugin.getPhase() == 0)
			player.teleport(mapManager.getLobbySpawnPoint());
		else
			player.teleport(team.getRandomSpawn());

		if (plugin.useMysql) {
			plugin.getDatabaseHandler()
					.query("INSERT IGNORE INTO `annihilation` (`username`, `kills`, `deaths`, `wins`, `losses`, `nexus_damage`) VALUES "
							+ "('"
							+ player.getName()
							+ "', '0', '0', '0', '0', '0');");
		}

		if (plugin.getPhase() == 0 && plugin.getVotingManager().isRunning()) {
			BarUtil.setMessageAndPercent(player, ChatColor.DARK_AQUA
					+ "Welcome to Annihilation!", 0.01F);
		} else {
			ScoreboardUtil.showForPlayers(player);
		}
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
			e.setDeathMessage(ChatUtil.formatDeathMessage(p, p.getKiller(),
					e.getDeathMessage()));
		} else
			e.setDeathMessage(ChatUtil.formatDeathMessage(p,
					e.getDeathMessage()));
		e.setDroppedExp(p.getTotalExperience());
	}

	@EventHandler
	public void onPlayerAttack(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		if (damager instanceof Player) {
			Player attacker = (Player) damager;
			if (PlayerMeta.getMeta(attacker).getKit() == Kit.WARRIOR) {
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
		if (Annihilation.isEmptyColumn(e.getBlock().getLocation()))
			e.setCancelled(true);
	}

	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		if (Annihilation.isEmptyColumn(e.getBlock().getLocation()))
			e.setCancelled(true);
		for (AnnihilationTeam t : AnnihilationTeam.values()) {
			if (t.getNexus().getLocation().equals(e.getBlock().getLocation())
					&& e.getBlock().getType() != Material.BEDROCK) {
				e.setCancelled(true);
				Player breaker = e.getPlayer();
				AnnihilationTeam attacker = PlayerMeta.getMeta(breaker)
						.getTeam();
				if (t == attacker)
					breaker.sendMessage(ChatColor.AQUA
							+ "You can't damage your own nexus");
				else if (plugin.getPhase() == 1)
					breaker.sendMessage(ChatColor.AQUA
							+ "Nexuses are invincible in phase 1");
				else {
					t.getNexus().damage(plugin.getPhase() == 5 ? 2 : 1);
					plugin.getStatsManager().incrementStat(
							StatType.NEXUS_DAMAGE, breaker);
					for (Player p : Bukkit.getOnlinePlayers())
						if (PlayerMeta.getMeta(p).getTeam() == attacker)
							p.sendMessage(ChatUtil.nexusBreakMessage(breaker,
									attacker, t));

					if (t.getNexus().getHealth() == 0) {
						ChatUtil.nexusDestroyed(attacker, t);
						plugin.checkWin();
						for (Player p : Bukkit.getOnlinePlayers())
							if (PlayerMeta.getMeta(p).getTeam() == t)
								plugin.getStatsManager().incrementStat(
										StatType.LOSSES, p);

					}
				}
			}
		}
	}
}
