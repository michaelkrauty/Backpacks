package me.michaelkrauty.Backpack;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created on 7/6/2014.
 *
 * @author michaelkrauty
 */
public class Main extends JavaPlugin implements Listener {

	public static Main main;

	public static ArrayList<Backpack> backpacks = new ArrayList<Backpack>();

	public HashMap<Player, String> open = new HashMap<Player, String>();

	public void onEnable() {
		main = this;
		checkDirs();
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("backpack").setExecutor(new BackpackCommand(this));
		loadBackpacks();
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
			public void run() {
				saveBackpacks();
			}
		}, 6000, 6000);
	}

	public void onDisable() {
		for (Backpack backpack : backpacks) {
			backpack.save();
		}
		backpacks.clear();
	}

	public void checkDirs() {
		if (!getDataFolder().exists())
			getDataFolder().mkdir();
		File backpacks = new File(getDataFolder() + "/backpacks");
		if (!backpacks.exists())
			backpacks.mkdir();
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
			if (event.getItem() != null) {
				if (event.getItem().getType() == Material.CHEST) {
					if (event.getItem().getItemMeta().getLore() != null) {
						if (event.getItem().getItemMeta().getLore().get(0) != null) {
							if (main.getBackpack(event.getItem().getItemMeta().getLore().get(0)) != null) {
								event.setCancelled(true);
								event.getPlayer().openInventory(getBackpack(event.getItem().getItemMeta().getLore().get(0)).getInventory());
								open.put(event.getPlayer(), event.getItem().getItemMeta().getLore().get(0));
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (open.get(event.getPlayer()) != null) {
			String uuid = open.get(event.getPlayer());
			open.remove(event.getPlayer());
			for (ItemStack item : event.getPlayer().getInventory().getContents()) {
				if (item != null) {
					if (item.getType() == Material.CHEST) {
						if (item.getItemMeta().getLore() != null) {
							if (item.getItemMeta().getLore().get(0) != null) {
								if (item.getItemMeta().getLore().get(0).equals(uuid)) {
									getBackpack(uuid).setInventory(event.getInventory());
								}
							}
						}
					}
				}
			}
		}
	}

	public Backpack getBackpack(String uuid) {
		for (Backpack backpack : backpacks) {
			if (backpack.getUUID().equals(uuid))
				return backpack;
		}
		return null;
	}

	public void loadBackpacks() {
		int backpackCount = 0;
		getLogger().info("Loading backpacks...");
		for (File file : new File(getDataFolder() + "/backpacks").listFiles()) {
			try {
				backpacks.add(new Backpack(this, file.getName().split("\\.")[0], null));
				backpackCount++;
			} catch (NullPointerException e) {
				getLogger().info("Couldn't load backpack: " + file.getName() + " (NullPointerException)");
			}
		}
		getLogger().info("Loaded " + backpackCount + " backpacks.");
	}

	public void saveBackpacks() {
		getLogger().info("Saving backpacks...");
		for (Backpack backpack : backpacks) {
			backpack.save();
		}
		getLogger().info("Backpacks saved to file.");
	}

	/**
	 * Credit for the following two methods:
	 * https://gist.github.com/graywolf336/8153678
	 */

	public static String toBase64(Inventory inventory) throws IllegalStateException {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

			// Write the size of the inventory
			dataOutput.writeInt(inventory.getSize());

			// Save every element in the list
			for (int i = 0; i < inventory.getSize(); i++) {
				dataOutput.writeObject(inventory.getItem(i));
			}

			// Serialize that array
			dataOutput.close();
			return Base64Coder.encodeLines(outputStream.toByteArray());
		} catch (Exception e) {
			throw new IllegalStateException("Unable to save item stacks.", e);
		}
	}

	public static Inventory fromBase64(String data) throws IOException {
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
			BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
			Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());

			// Read the serialized inventory
			for (int i = 0; i < inventory.getSize(); i++) {
				inventory.setItem(i, (ItemStack) dataInput.readObject());
			}

			dataInput.close();
			return inventory;
		} catch (ClassNotFoundException e) {
			throw new IOException("Unable to decode class type.", e);
		}
	}
}
