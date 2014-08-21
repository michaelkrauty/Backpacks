package me.michaelkrauty.Backpack;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created on 7/30/2014.
 *
 * @author michaelkrauty
 */
public class Cooldowns {

	private Main main;

	private File file = new File(Main.main.getDataFolder(), "cooldowns.yml");
	private YamlConfiguration data = new YamlConfiguration();

	public Cooldowns(Main instance) {
		main = instance;
		checkFile();
		reload();
	}

	private void checkFile() {
		if (!main.getDataFolder().exists())
			main.getDataFolder().mkdir();
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void reload() {
		try {
			data.load(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void setCooldown(UUID uuid, int cooldown) {
        data.set(uuid.toString(), cooldown);
    }

    public void deleteEntry(UUID uuid) {
        data.getConfigurationSection("").set(uuid.toString(), null);
    }

    public HashMap<UUID, Integer> getCooldowns() {
        HashMap<UUID, Integer> cooldowns = new HashMap<UUID, Integer>();
        for (Map.Entry<String, Integer> entry : ((HashMap<String, Integer>) data.getMapList("")).entrySet()) {
            cooldowns.put(UUID.fromString(entry.getKey()), entry.getValue());
        }
        return cooldowns;
    }
}