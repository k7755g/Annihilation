package net.coasterman10.Annihilation.stats;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.commands.StatsCommand;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class StatsManager {
	private Annihilation plugin;
	private YamlConfiguration yml;
	public static final int UNDEF_STAT = -42;

	public StatsManager(Annihilation instance) {
		this.plugin = instance;
		new StatsCommand(instance, this);
		yml = plugin.getConfigManager().getConfig("stats.yml");
	}

	public int getStat(StatType s, Player p) {
		if (!plugin.useMysql) {
			return yml.getInt(p.getName() + "." + s.name());
		} else {
			try {
				int stat = UNDEF_STAT;
				ResultSet rs = plugin
						.getDatabaseHandler()
						.query("SELECT * FROM `annihilation` WHERE `username`='"
								+ p.getName() + "'").getResultSet();

				while (rs.next())
					stat = rs.getInt(s.name().toLowerCase());

				return stat;
			} catch (SQLException ex) {
				ex.printStackTrace();
				return UNDEF_STAT;
			}
		}
	}

	public void setValue(StatType s, Player p, int value) throws IOException {
		if (!plugin.useMysql) {
			yml.set(p.getName() + "." + s.name(), value);
			plugin.getConfigManager().save("stats.yml");
		} else {
			plugin.getDatabaseHandler().query(
					"UPDATE `annihilation` SET `" + s.name().toLowerCase()
							+ "`='" + value + "' WHERE `username`='"
							+ p.getName() + "';");
		}
	}

	public void incrementStat(StatType s, Player p) {
		try {
			setValue(s, p, getStat(s, p) + 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
