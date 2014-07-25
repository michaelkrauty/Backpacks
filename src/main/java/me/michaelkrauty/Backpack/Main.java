package me.michaelkrauty.Backpack;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
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

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (open.get(event.getPlayer()) != null) {
			String uuid = open.get(event.getPlayer());
			open.remove(event.getPlayer());
			for (ItemStack item : event.getPlayer().getInventory().getContents()) {
				if (item != null) {
					if (item.getType() == Material.CHEST) {
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
			backpacks.add(new Backpack(this, file.getName().split("\\.")[0]));
			backpackCount++;
		}
		getLogger().info("Loaded " + backpackCount + " backpacks.");
	}
}
