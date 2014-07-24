package me.michaelkrauty.Backpack;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;

/**
 * Created on 7/6/2014.
 *
 * @author michaelkrauty
 */
public class Main extends JavaPlugin implements Listener {

	public static Main main;
	public static Config config;

	public static ArrayList<Backpack> backpacks = new ArrayList<Backpack>();

	public static List<String> groups;
	public static HashMap<String, ArrayList<String>> shares = new HashMap<String, ArrayList<String>>();

	public HashMap<Player, Backpack> open = new HashMap<Player, Backpack>();

	public void onEnable() {
		main = this;
		checkDirs();
		config = new Config(this);
		groups = config.getGroups();
		for (String group : groups) {
			shares.put(group, config.getWorlds(group));
		}
		checkGroupDirs();
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("backpack").setExecutor(new BackpackCommand(this));
		loadBackpacks();
	}

	public void onDisable() {
		try {
			for (Backpack backpack : backpacks) {
				backpack.saveItems();
				backpacks.remove(backpack);
			}
		} catch (ConcurrentModificationException ignored) {
		}
	}

	public void checkDirs() {
		if (!getDataFolder().exists())
			getDataFolder().mkdir();
		File backpacks = new File(getDataFolder() + "/backpacks");
		if (!backpacks.exists())
			backpacks.mkdir();
	}

	public void checkGroupDirs() {
		for (String group : config.getGroups()) {
			File f = new File(getDataFolder() + "/backpacks/" + group);
			if (!f.exists())
				f.mkdir();
		}
	}

	public String getWorldGroup(String world) {
		for (String group : groups) {
			if (shares.get(group).contains(world))
				return group;
		}
		return null;
	}

	public void loadBackpacks() {
		getLogger().info("Loading backpacks...");
		int backpackCount = 0;
		File backpackFile = new File(getDataFolder() + "/backpacks");
		for (File f : backpackFile.listFiles()) {
			for (File file : f.listFiles()) {


				String fileName = file.getName().split("\\.")[0];
				try {
					backpacks.add(new Backpack(this, f.getName(), fileName));
					backpackCount++;
				} catch (ArrayIndexOutOfBoundsException ignored) {
				}


			}
		}
		getLogger().info("Loaded " + backpackCount + " backpacks.");
	}

	public Backpack getBackpack(String group, String uuid) {
		for (Backpack backpack : backpacks) {
			if (backpack.getGroup().equals(group)) {
				if (backpack.getUUID().equals(uuid)) {
					return backpack;
				}
			}
		}
		return null;
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (open.get(event.getPlayer()) instanceof Backpack) {
			Backpack backpack = open.get(event.getPlayer());
			open.remove(event.getPlayer());
			backpack.setInventory(event.getInventory());
		}
	}
}
