package me.michaelkrauty.Backpack;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 7/23/2014.
 *
 * @author michaelkrauty
 */
public class Config {

	private Main main;

	private File configFile = new File(Main.main.getDataFolder() + "/config.yml");
	private YamlConfiguration config = new YamlConfiguration();

	public Config(Main instance) {
		main = instance;
		checkFile();
		reload();
	}

	private void checkFile() {
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
				InputStream input = main.getResource("config.yml");
				OutputStream output = new FileOutputStream(configFile);
				byte[] buffer = new byte[1024];
				int bytesRead;
				while ((bytesRead = input.read(buffer)) > 0) {
					output.write(buffer, 0, bytesRead);
				}
				output.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void reload() {
		try {
			config.load(configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<String> getGroups() {
		return new ArrayList<String>(config.getConfigurationSection("shares").getKeys(true));
	}

	public ArrayList<String> getWorlds(String group) {
		return new ArrayList<String>(config.getStringList("shares." + group));
	}
}
