package net.coasterman10.Annihilation;

import java.util.Random;

import net.coasterman10.Annihilation.teams.Team;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundUtil {
	private static Random rand = new Random();

	public static void playSound(Location loc, Sound sound, float volume,
			float minPitch, float maxPitch) {
		loc.getWorld().playSound(loc, sound, volume,
				randomPitch(minPitch, maxPitch));
	}

	public static void playSoundForPlayer(Player p, Sound sound, float volume,
			float minPitch, float maxPitch) {
		p.playSound(p.getLocation(), sound, volume,
				randomPitch(minPitch, maxPitch));
	}

	public static void playSoundForTeam(Team t, Sound sound, float volume,
			float minPitch, float maxPitch) {
		for (Player p : t.getPlayers())
			playSoundForPlayer(p, sound, volume, minPitch, maxPitch);
	}

	private static float randomPitch(float min, float max) {
		return min + rand.nextFloat() * (max - min);
	}
}
