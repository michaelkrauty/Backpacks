package me.michaelkrauty.Backpack;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

/**
 * Created on 8/11/2014.
 *
 * @author michaelkrauty
 */
public class Locale {

	private Main main;

	private File localeFile = new File(Main.main.getDataFolder(), "locale.yml");
	private YamlConfiguration locale = new YamlConfiguration();

	public Locale(Main instance) {
		main = instance;
		checkFile();
		reload();
	}

	private void checkFile() {
		if (!main.getDataFolder().exists())
			main.getDataFolder().mkdir();
		if (!localeFile.exists()) {
			try {
				localeFile.createNewFile();
				InputStream input = main.getResource("locale.yml");
				OutputStream output = new FileOutputStream(localeFile);
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
			locale.load(localeFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String[] getMessage(String path) {
		return (String[]) locale.getStringList(path).toArray();
	}

	public int getInt(String path) {
		return locale.getInt(path);
	}

	public String getString(String path) {
		return locale.getString(path);
	}

	public boolean getBoolean(String path) {
		return locale.getBoolean(path);
	}
}