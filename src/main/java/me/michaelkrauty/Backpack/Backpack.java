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

	private Inventory inventory;
	private File file;
	private String group;
	private String uuid;
	private Main main;

	public Backpack(Main main, String group, String uuid) {
		this.main = main;
		this.group = group;
		this.uuid = uuid;
		file = new File(main.getDataFolder() + "/backpacks/" + group + "/" + uuid + ".yml");
		loadItems();
	}

	public void loadItems() {
		YamlConfiguration yaml = new YamlConfiguration();
		try {
			yaml.load(file);
			inventory = InventoryStringDeSerializer.StringToInventory(yaml.getString("backpack"));
		} catch (Exception e) {
			inventory = main.getServer().createInventory(null, 54, "Backpack");
		}
	}

	public void saveItems() {
		YamlConfiguration yaml = new YamlConfiguration();
		try {
			yaml.set("backpack", InventoryStringDeSerializer.InventoryToString(inventory));
			yaml.save(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	public String getGroup() {
		return group;
	}

	public String getUUID() {
		return uuid;
	}
}
