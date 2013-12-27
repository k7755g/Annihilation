package net.coasterman10.Annihilation.teams;

import net.coasterman10.Annihilation.SoundUtil;

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
		SoundUtil.playSound(location, Sound.ANVIL_LAND, 10F, 0.5F, 1F);
		SoundUtil.playSoundForTeam(owner, Sound.NOTE_PIANO, 1F, 1.6F, 1.6F);
		if (health <= 0) {
			health = 0;
			location.getBlock().setType(Material.BEDROCK);
		}
	}
}
