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
import net.coasterman10.Annihilation.stats.StatType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerListener implements Listener {
	private final Annihilation plugin;

	public PlayerListener(Annihilation plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		PlayerMeta pmeta = PlayerMeta.getMeta(player);
		Action a = e.getAction();
		if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
			ItemStack handItem = player.getItemInHand();
			if (handItem.getType() == Material.COMPASS) {
				boolean setCompass = false;
				boolean setToNext = false;
				while (!setCompass) {
					for (AnnihilationTeam team : AnnihilationTeam.teams()) {
						if (setToNext) {
							ItemMeta meta = handItem.getItemMeta();
							meta.setDisplayName(team.color() + "Pointing to "
									+ team.toString() + " Nexus");
							handItem.setItemMeta(meta);
							player.setCompassTarget(team.getNexus()
									.getLocation());
							setCompass = true;
							break;
						}
						if (handItem.getItemMeta().getDisplayName()
								.contains(team.toString()))
							setToNext = true;
					}
				}
			}
		}

		if (e.getClickedBlock() != null) {
			Material clickedType = e.getClickedBlock().getType();
			if (clickedType == Material.SIGN_POST
					|| clickedType == Material.WALL_SIGN) {
				Sign s = (Sign) e.getClickedBlock().getState();
				if (s.getLine(0).contains(ChatColor.DARK_PURPLE + "[Team]")) {
					String teamName = ChatColor.stripColor(s.getLine(1));
					AnnihilationTeam team = AnnihilationTeam.valueOf(teamName
							.toUpperCase());
					if (team != null) {
						if (pmeta.getTeam() == AnnihilationTeam.NONE) {
							pmeta.setTeam(team);
							ScoreboardUtil.addPlayerToTeam(player, team.name());
							player.sendMessage(ChatColor.DARK_AQUA
									+ "You joined " + team.coloredName());
							if (plugin.getPhase() > 0)
								Annihilation.Util.sendPlayerToGame(player);
						} else {
							player.sendMessage(ChatColor.DARK_AQUA
									+ "You are already on "
									+ pmeta.getTeam().coloredName());
						}

						plugin.getSignHandler().updateSigns(team);
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player player = e.getPlayer();
		PlayerMeta meta = PlayerMeta.getMeta(player);
		if (meta.isAlive()) {
			e.setRespawnLocation(meta.getTeam().getRandomSpawn());
			meta.getKit().give(player, meta.getTeam());
		} else
			e.setRespawnLocation(plugin.getMapManager().getLobbySpawnPoint());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		PlayerMeta meta = PlayerMeta.getMeta(player);
		if (meta.isAlive())
			player.teleport(meta.getTeam().getRandomSpawn());
		else
			player.teleport(plugin.getMapManager().getLobbySpawnPoint());

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

		plugin.getSignHandler().updateSigns(meta.getTeam());
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();

		if (plugin.getPhase() > 0) {
			PlayerMeta meta = PlayerMeta.getMeta(p);
			if (!meta.getTeam().getNexus().isAlive())
				meta.setAlive(false);
		}

		plugin.getStatsManager().setValue(StatType.DEATHS, p,
				plugin.getStatsManager().getStat(StatType.DEATHS, p) + 1);

		if (p.getKiller() != null && !p.getKiller().equals(p)) {
			Player killer = p.getKiller();
			plugin.getStatsManager().incrementStat(StatType.KILLS, killer);
			e.setDeathMessage(ChatUtil.formatDeathMessage(p, p.getKiller(),
					e.getDeathMessage()));

			if (PlayerMeta.getMeta(killer).getKit() == Kit.BERSERKER) {
				addHeart(killer);
			}
		} else
			e.setDeathMessage(ChatUtil.formatDeathMessage(p,
					e.getDeathMessage()));
		e.setDroppedExp(p.getTotalExperience());
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getEntity().getWorld().getName().equals("lobby"))
				e.setCancelled(true);

			if (e.getCause() == DamageCause.VOID)
				e.getEntity().teleport(
						plugin.getMapManager().getLobbySpawnPoint());
		}
	}

	@EventHandler
	public void onPlayerAttack(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		if (damager instanceof Player) {
			if (damager.getWorld().getName().equals("lobby")) {
				e.setCancelled(true);
				return;
			}
			if (plugin.getPhase() < 1) {
				e.setCancelled(true);
				return;
			}

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
		if (Annihilation.Util.isEmptyColumn(e.getBlock().getLocation()))
			e.setCancelled(true);
	}

	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		if (plugin.getPhase() > 0) {
			for (AnnihilationTeam t : AnnihilationTeam.teams()) {
				if (t.getNexus().getLocation()
						.equals(e.getBlock().getLocation())) {
					e.setCancelled(true);
					if (t.getNexus().isAlive())
						breakNexus(t, e.getPlayer());
					break;
				}
			}
		}
	}

	@EventHandler
	public void onArmorEquip(InventoryClickEvent e) {
		final Player player = (Player) e.getWhoClicked();
		if (PlayerMeta.getMeta(player).getKit() == Kit.SCOUT) {
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					PlayerMeta.getMeta(player).getKit()
							.addScoutParticles(player);
				}
			}, 2L);
		}
	}

	private void addHeart(Player player) {
		double maxHealth = player.getMaxHealth();
		if (maxHealth < 30.0) {
			double newMaxHealth = maxHealth + 2.0;
			player.setMaxHealth(newMaxHealth);
			player.setHealth(player.getHealth() + 2.0);
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
					breaker, plugin.getPhase() == 5 ? 2 : 1);

			String msg = ChatUtil.nexusBreakMessage(breaker, attacker, victim);
			for (Player p : attacker.getPlayers())
				p.sendMessage(msg);

			ScoreboardUtil.setScore(victim + " Nexus", victim.getNexus()
					.getHealth());

			Bukkit.getServer()
					.getPluginManager()
					.callEvent(
							new NexusDamageEvent(breaker, victim, victim
									.getNexus().getHealth()));

			if (victim.getNexus().getHealth() == 0) {
				ScoreboardUtil.removeScore(victim.coloredName() + " Nexus");
				Bukkit.getServer().getPluginManager()
						.callEvent(new NexusDestroyEvent(breaker, victim));
				ChatUtil.nexusDestroyed(attacker, victim);
				plugin.checkWin();
				for (Player p : victim.getPlayers()) {
					plugin.getStatsManager().incrementStat(StatType.LOSSES, p);
				}
			}

			plugin.getSignHandler().updateSigns(victim);
		}
	}
}
