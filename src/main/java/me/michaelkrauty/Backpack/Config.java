package me.michaelkrauty.Backpack;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 7/30/2014.
 *
 * @author michaelkrauty
 */
public class Config {

	private Main main;

	private File configFile = new File(Main.main.getDataFolder(), "config.yml");
	private YamlConfiguration config = new YamlConfiguration();

	public Config(Main instance) {
		main = instance;
		checkFile();
		update();
		reload();
	}

	private void checkFile() {
		if (!main.getDataFolder().exists())
			main.getDataFolder().mkdir();
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

	public void update() {
		try {
			reload();
			if (config.getString("checkupdate") == null)
				config.set("checkupdate", config.getBoolean("checkupdate"));
			if (config.getString("mysql.database") == null)
				config.set("mysql.database", config.getString("mysql.database"));
			if (config.getString("mysql.host") == null)
				config.set("mysql.host", config.getString("mysql.host"));
			if (config.getString("mysql.user") == null)
				config.set("mysql.user", config.getString("mysql.user"));
			if (config.getString("mysql.pass") == null)
				config.set("mysql.pass", config.getString("mysql.pass"));
			if (config.getString("mysql.table") == null)
				config.set("mysql.table", config.getString("mysql.table"));
			if (config.getString("data") == null)
				config.set("data", config.getString("data"));
			if (config.getString("cost") == null)
				config.set("cost", config.getInt("cost"));
			config.save(configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getInt(String path) {
		return config.getInt(path);
	}

	public String getString(String path) {
		return config.getString(path);
	}

	public boolean getBoolean(String path) {
		return config.getBoolean(path);
	}
}