package net.coasterman10.Annihilation.listeners;

import java.util.HashMap;
import java.util.Map.Entry;

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.Kit;
import net.coasterman10.Annihilation.PlayerMeta;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ClassAbilityListener implements Listener {
	private final HashMap<String, Location> blockLocations = new HashMap<String, Location>();
	private final HashMap<String, Long> cooldowns = new HashMap<String, Long>();
	private final Annihilation plugin;

	public ClassAbilityListener(Annihilation plugin) {
		this.plugin = plugin;
		Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			@Override
			public void run() {
				update();
			}
		}, 20L, 20L);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onSpecialBlockPlace(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		Player player = e.getPlayer();
		Kit kit = PlayerMeta.getMeta(player).getKit();

		if (kit == Kit.OPERATIVE) {
			final Block placed = e.getClickedBlock().getRelative(
					e.getBlockFace());
			ItemStack held = player.getItemInHand();
			if (held.hasItemMeta()) {
				if (held.getType() == Material.SOUL_SAND
						&& held.getItemMeta().getDisplayName()
								.equals(ChatColor.AQUA + "Return Point")) {
					e.setCancelled(true);
					if (blockLocations.get(player.getName()) == null) {
						player.updateInventory();
						blockLocations.put(player.getName(),
								placed.getLocation());
						cooldowns.put(player.getName(), 90L);
						player.sendMessage(ChatColor.AQUA
								+ "You will be teleported back here in 90 seconds");
						Bukkit.getScheduler().runTaskLater(plugin,
								new Runnable() {
									@Override
									public void run() {
										placed.setType(Material.SOUL_SAND);
									}
								}, 1L);
					} else {
						player.sendMessage(ChatColor.RED
								+ "You have already placed a return point; you will return in "
								+ cooldowns.get(player.getName()) + " seconds");
					}
				}
			}
		}
	}

	@EventHandler
	public void onScoutGrapple(PlayerFishEvent e) {
		Player player = e.getPlayer();
		player.getItemInHand().setDurability((short) -10);
		if (e.getState() != State.IN_GROUND)
			return;
		if (PlayerMeta.getMeta(player).getKit() != Kit.SCOUT)
			return;
		if (!player.getItemInHand().getItemMeta().getDisplayName()
				.contains("Grapple"))
			return;
		Location hookLoc = e.getHook().getLocation();
		Location playerLoc = player.getLocation();
		playerLoc.setY(playerLoc.getY() + 0.5);
		player.teleport(playerLoc);

		Vector diff = hookLoc.toVector().subtract(playerLoc.toVector());
		Vector vel = new Vector();
		double d = hookLoc.distance(playerLoc);
		vel.setX((1.0 + 0.07 * d) * diff.getX() / d);
		vel.setY((1.0 + 0.03 * d) * diff.getY() / d + 0.04 * d);
		vel.setZ((1.0 + 0.07 * d) * diff.getZ() / d);
		player.setVelocity(vel);
	}

	@EventHandler
	public void onFallDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;

		Player player = (Player) e.getEntity();
		PlayerMeta meta = PlayerMeta.getMeta(player);
		if (meta.getKit() == Kit.SCOUT && e.getCause() == DamageCause.FALL) {
			if (player.getItemInHand().getItemMeta().getDisplayName()
					.contains("Grapple"))
				e.setDamage(e.getDamage() / 2.0);
		}
	}

	private void update() {
		for (Entry<String, Long> entry : cooldowns.entrySet()) {
			long cooldown = entry.getValue();
			if (cooldown > 0L) {
				cooldown--;
				entry.setValue(cooldown);
			} else
				continue;

			String name = entry.getKey();
			Player player = Bukkit.getPlayer(name);
			if (!player.isOnline())
				continue;
			switch (PlayerMeta.getMeta(player).getKit()) {
			case OPERATIVE:
				if (cooldown == 0) {
					Location returnPoint = blockLocations.get(name);
					returnPoint.getBlock().setType(Material.AIR);
					player.teleport(returnPoint);
					player.sendMessage(ChatColor.DARK_AQUA
							+ "You have been teleported back to your return point.");
					blockLocations.remove(name);
				} else if (cooldown == 20 || cooldown == 10 || cooldown <= 5) {
					player.sendMessage(ChatColor.DARK_AQUA
							+ "Teleporting back in " + cooldown + " seconds");
				}
			default:
				continue;
			}
		}
	}
}
