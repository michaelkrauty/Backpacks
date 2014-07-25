package me.michaelkrauty.Backpack;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;

import java.io.File;

/**
 * Created on 7/24/2014.
 *
 * @author michaelkrauty
 */
public class Backpack {

	private String uuid;
	private Inventory inventory;
	private File file;

	public Backpack(Main main, String uuid) {
		this.uuid = uuid;
		file = new File(main.getDataFolder() + "/backpacks/" + uuid + ".yml");
		if (!checkFile())
			inventory = main.getServer().createInventory(null, 54, "Backpack");
		else
			load();
		main.backpacks.add(this);
	}

	public String getUUID() {
		return uuid;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	private boolean checkFile() {
		boolean exists = file.exists();
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return exists;
	}

	public void load() {
		YamlConfiguration yaml = new YamlConfiguration();
		try {
			yaml.load(file);
			inventory = InventoryStringDeSerializer.StringToInventory(yaml.getString("data"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void save() {
		YamlConfiguration yaml = new YamlConfiguration();
		try {
			yaml.set("data", InventoryStringDeSerializer.InventoryToString(inventory));
			yaml.save(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
