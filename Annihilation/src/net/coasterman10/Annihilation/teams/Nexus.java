package net.coasterman10.Annihilation.teams;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;

public class Nexus {
	private final Team owner;
	private final Location location;
	private int health;

	public Nexus(Team owner, Location location, int initialHealth) {
		this.owner = owner;
		this.location = location;
		location.getBlock().setType(Material.ENDER_STONE);
		health = initialHealth;
	}

	public Team getOwner() {
		return owner;
	}

	public Location getLocation() {
		return location;
	}

	public int getHealth() {
		return health;
	}

	public void damage() {
		health--;
		location.getWorld().playSound(location, Sound.ANVIL_LAND, 10F,
				0.5F + new Random().nextFloat() * 0.5F);
		if (health <= 0) {
			health = 0;
			location.getBlock().setType(Material.BEDROCK);
		}
	}
}
