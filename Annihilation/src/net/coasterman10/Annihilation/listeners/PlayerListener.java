package net.coasterman10.Annihilation.listeners;

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.AnnihilationTeam;
import net.coasterman10.Annihilation.Kit;
import net.coasterman10.Annihilation.PlayerMeta;
import net.coasterman10.Annihilation.ScoreboardUtil;
import net.coasterman10.Annihilation.api.NexusDamageEvent;
import net.coasterman10.Annihilation.api.NexusDestroyEvent;
import net.coasterman10.Annihilation.bar.BarUtil;
import net.coasterman10.Annihilation.chat.ChatUtil;
import net.coasterman10.Annihilation.maps.MapManager;
import net.coasterman10.Annihilation.stats.StatType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player player = e.getPlayer();
		PlayerMeta meta = PlayerMeta.getMeta(player);
		if (meta.isAlive()) {
			e.setRespawnLocation(meta.getTeam().getRandomSpawn());
			meta.getKit().give(player, meta.getTeam());
		} else
			e.setRespawnLocation(mapManager.getLobbySpawnPoint());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		PlayerMeta meta = PlayerMeta.getMeta(player);
		if (meta.isAlive())
			player.teleport(meta.getTeam().getRandomSpawn());
		else
			player.teleport(mapManager.getLobbySpawnPoint());

		if (plugin.useMysql) {
			plugin.getDatabaseHandler()
					.query("INSERT IGNORE INTO `annihilation` (`username`, `kills`, "
							+ "`deaths`, `wins`, `losses`, `nexus_damage`) VALUES "
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
		for (AnnihilationTeam t : AnnihilationTeam.teams()) {
			if (t.getNexus().getLocation().equals(e.getBlock().getLocation())) {
				e.setCancelled(true);
				if (t.getNexus().isAlive())
					breakNexus(t, e.getPlayer());
				break;
			}
		}
	}

	private void breakNexus(AnnihilationTeam victim, Player breaker) {
		AnnihilationTeam attacker = PlayerMeta.getMeta(breaker).getTeam();
		if (victim == attacker)
			breaker.sendMessage(ChatColor.DARK_AQUA
					+ "You can't damage your own nexus");
		else if (plugin.getPhase() == 1)
			breaker.sendMessage(ChatColor.DARK_AQUA
					+ "Nexuses are invincible in phase 1");
		else {
			victim.getNexus().damage(plugin.getPhase() == 5 ? 2 : 1);
			plugin.getStatsManager().incrementStat(StatType.NEXUS_DAMAGE,
					breaker);

			String msg = ChatUtil.nexusBreakMessage(breaker, attacker, victim);
			for (Player p : attacker.getPlayers())
				p.sendMessage(msg);

			ScoreboardUtil.setScore(victim.coloredName() + " Nexus", victim
					.getNexus().getHealth());

			Bukkit.getServer().getPluginManager().callEvent(new NexusDamageEvent(breaker, victim, victim.getNexus().getHealth()));
			
			if (victim.getNexus().getHealth() == 0) {
				ScoreboardUtil.removeScore(victim.coloredName() + " Nexus");
				Bukkit.getServer().getPluginManager().callEvent(new NexusDestroyEvent(breaker, victim));
				ChatUtil.nexusDestroyed(attacker, victim);
				plugin.checkWin();
				for (Player p : victim.getPlayers())
					plugin.getStatsManager().incrementStat(StatType.LOSSES, p);
			}
		}
	}
}
