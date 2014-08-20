package me.michaelkrauty.Backpack;

import net.gravitydevelopment.updater.Updater;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
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
import java.util.Map;
import java.util.logging.Level;

/**
 * Created on 7/6/2014.
 *
 * @author michaelkrauty
 */
public class Main extends JavaPlugin implements Listener {

    /**
     * The plugin
     */
	public static Main main;

    /**
     * Any backpack object created should be stored here
     */
	public static ArrayList<Backpack> backpacks = new ArrayList<Backpack>();

    /**
     * When a player opens a backpack, store the player + the backpack's UUID here
     */
	public HashMap<Player, String> open = new HashMap<Player, String>();

    /**
     * Vault economy object
     */
	public static Economy economy = null;

    /**
     * configuration and locale objects
     */
	public static Config config;
	public static Locale locale;

    /**
     * SQL object
     */
	public static SQL sql = null;

    /**
     * Backpack cost (if 0, economy will be disabled)
     */
	public static int cost = 0;

    /**
     * Load the plugin up
     */
	public void onEnable() {
        // Define main
		main = this;

        // Register event handlers
		getServer().getPluginManager().registerEvents(this, this);

        // Register backpack command
		getCommand("backpack").setExecutor(new BackpackCommand(this));

        // Create config + locale objects
		config = new Config(this);
		locale = new Locale(this);

        // If update checking is enabled, check update
		if (config.getBoolean("checkupdate"))
			checkUpdate();

        // Try to enable economy
		try {
			if (getServer().getPluginManager().getPlugin("Vault") != null) {
				if (setupEconomy())
					cost = config.getInt("cost");
			}
		} catch (NullPointerException e) {
			getLogger().info("Vault is not installed on this server. Economy features disabled.");
		}

        // Get data saving configuration from config
		if (config.getString("data").equalsIgnoreCase("mysql") || config.getString("data").equalsIgnoreCase("sql")) {

            // Create SQL class
			sql = new SQL(this);

		} else if (!config.getString("data").equalsIgnoreCase("flatfile") && !config.getString("data").equalsIgnoreCase("file"))
            // Data type not recognized
			getLogger().warning("unrecognized data format: " + config.getString("data") + ". Must either be \"flatfile\" or \"mysql\". Using flatfile...");

        // Enable metrics
		try {
			new me.michaelkrauty.Backpack.Metrics(this).start();
		} catch (IOException e) {
			getLogger().log(Level.WARNING, "Couldn't start metrics: " + e.getMessage());
		}
		if (sql == null) {
            // SQL isn't enabled, create the backpack flatfile directory
			File file = new File(getDataFolder(), "backpacks");
			if (!file.exists())
				file.mkdir();
		}
        // Load all backpack objects, store them in the arraylist backpacks
		loadBackpacks();
	}

    // Setup vault economy
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) economy = economyProvider.getProvider();
		return (economy != null);
	}

    /**
     * Shut the plugin down
     */
	public void onDisable() {

        // If anyone has a backpack open, this will close it.
		for (Map.Entry<Player, String> entry : open.entrySet()) {
			entry.getKey().closeInventory();
		}

        // Clear the backpack arraylist
		backpacks.clear();

        // Close SQL connection if SQL is enabled
		if (sql != null)
			sql.closeConnection();
	}

    /**
     * Check for a newer version of the plugin
     */
	private void checkUpdate() {

        // Create updater object
		Updater updater = new Updater(this, 83139, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, true);

        // If update is needed, print info to console
		if (updater.shouldUpdate(getDescription().getName() + " v" + getDescription().getVersion(), updater.getLatestName())) {
			getLogger().info("---[ Backpack Updater ]---");
			getLogger().info("Backpack is out of date!");
			getLogger().info("Current Version: " + getDescription().getVersion());
			getLogger().info("Latest Version: " + updater.getLatestName());
			getLogger().info("Download the latest version here: " + updater.getLatestFileLink());
			getLogger().info("--------------------------");
		}
	}

    // Called when a player left/right clicks a block or the air
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {

        // If the player right clicked
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {

            // If they're holding an item
			if (event.getItem() != null) {

                // If the item they're holding is a chest
				if (event.getItem().getType() == Material.CHEST) {

                    // If the item has lore
					if (event.getItem().getItemMeta().getLore() != null) {
						if (event.getItem().getItemMeta().getLore().get(0) != null) {

                            // Get the backpack object based on the lore, check if it exists
							if (main.getBackpack(event.getItem().getItemMeta().getLore().get(0)) != null) {

                                // Ladies and gentlemen, we have a backpack.

                                // Cancel the event to prevent placing the chest & losing the backpack
								event.setCancelled(true);

                                // Open the inventory of the backpack
								event.getPlayer().openInventory(getBackpack(event.getItem().getItemMeta().getLore().get(0)).getInventory());

                                // Add the player to the open hashmap
								open.put(event.getPlayer(), event.getItem().getItemMeta().getLore().get(0));
							}
						}
					}
				}
			}
		}
	}

    // Called when a player closes an inventory
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {

        // If the player is looking at a backpack
		if (open.get(event.getPlayer()) != null) {

            // Get the backpack's UUID
			String uuid = open.get(event.getPlayer());

            // Remove the player from the open hashmap
			open.remove(event.getPlayer());

            // Look through the player's inventory for the backpack
			for (ItemStack item : event.getPlayer().getInventory().getContents()) {
				if (item != null) {
					if (item.getType() == Material.CHEST) {
						if (item.getItemMeta().getLore() != null) {
							if (item.getItemMeta().getLore().get(0) != null) {
								if (item.getItemMeta().getLore().get(0).equals(uuid)) {

                                    // Save the backpack
									getBackpack(uuid).setInventory(event.getInventory());
								}
							}
						}
					}
				}
			}
		}
	}

    // Called when the player clicks an item in an inventory
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {

        // If the player clicked on an item
		if (event.getCurrentItem() != null) {

            // If the item has metadata
			if (event.getCurrentItem().getItemMeta() != null) {

                // If the item has lore
				if (event.getCurrentItem().getItemMeta().getLore() != null) {
					if (event.getCurrentItem().getItemMeta().getLore().get(0) != null) {

                        // Declare a backpack object
						Backpack b2;

                        // Check if the item is a backpack
						if ((b2 = getBackpack(event.getCurrentItem().getItemMeta().getLore().get(0))) != null) {

                            // Get the player who clicked
							Player player = (Player) event.getViewers().get(0);

                            // If the player right clicked the backpack
							if (event.getAction() == InventoryAction.PICKUP_HALF) {

                                // Should never happen, but just in case...
								if (event.getViewers() == null) {
									main.getLogger().log(Level.SEVERE, "List of backpack viewers is null!");
									main.getLogger().log(Level.SEVERE, "Backpack UUID: " + b2.getUUID().toString());
									return;
								}

                                // Check if another player has the backpack open already
								if (b2.getInventory().getViewers().size() != 0) {

                                    // Send the player a nice message & cancel the event
									event.getWhoClicked().getServer().getPlayer(event.getWhoClicked().getUniqueId()).sendMessage(ChatColor.RED + "Someone else already has that backpack open.");
									event.setCancelled(true);
									return;
								}

                                // If the clicked item is a backpack, open it
								if (open.get(player) != null) {
									Backpack b1 = getBackpack(open.get(player));
									b1.setInventory(player.getOpenInventory().getTopInventory());
									open.remove(player);
								}

                                // Close the current backpack
								player.getOpenInventory().close();

                                // Open the clicked backpack
								player.openInventory(b2.getInventory());

                                // Add the player to the open hashmap
								open.put(player, b2.getUUID());

                                // Cancel the event
								event.setCancelled(true);
								return;
							}

                            // If the player has a backpack open
							if (open.get(player) != null) {

                                // If the backpack they have open is the inventory they're currently in
								if (open.get(player).equals(event.getCurrentItem().getItemMeta().getLore().get(0))) {

                                    // Prevent the player from putting the backpack into itself
									player.sendMessage(new String[]{ChatColor.RED + "You can't move a backpack while it's open", ChatColor.GRAY + "(this is to prevent dividing by zero)"});

                                    // Cancel the event
									event.setCancelled(true);
									return;
								}
							}
						}
					}
				}
			}
		}
	}

    // When an item despawns
	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemDespawn(ItemDespawnEvent event) {

        // Check if the event is canceled
		if (!event.isCancelled()) {

            // Determine if the despawned item is a backpack
			if (event.getEntity() != null) {
				if (event.getEntity().getItemStack() != null) {
					if (event.getEntity().getItemStack().getItemMeta() != null) {
						if (event.getEntity().getItemStack().getItemMeta().getLore() != null) {
							if (event.getEntity().getItemStack().getItemMeta().getLore().get(0) != null) {
								if (getBackpack(event.getEntity().getItemStack().getItemMeta().getLore().get(0)) != null) {

                                    // Delete the backpack since there's no way it could ever be opened again
									deleteBackpack(event.getEntity().getItemStack().getItemMeta().getLore().get(0));

								}
							}
						}
					}
				}
			}
		}
	}

    /**
     * Get a backpack by UUID from the arraylist of backpack objects
     */
	public Backpack getBackpack(String uuid) {
		for (Backpack backpack : backpacks) {
			if (backpack.getUUID().equals(uuid))
				return backpack;
		}
		return null;
	}

    /**
     * Delete a backpack by UUID from the arraylist of backpacks + the database
     */
	public void deleteBackpack(String uuid) {
		if (getBackpack(uuid) != null) {
			Backpack backpack = getBackpack(uuid);
			backpacks.remove(backpack);
			if (backpack.getFile().exists())
				backpack.getFile().delete();
			if (sql != null) {
				final String finalUUID = uuid;
				getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
					public void run() {
						sql.removeBackpack(finalUUID);
					}
				});
			}
		}
	}

    /**
     * Load all backpacks into memory
     */
	public void loadBackpacks() {
		getLogger().info("Loading backpacks...");

        // Clear the arraylist to prevent duplicate objects
		if (!backpacks.isEmpty())
			backpacks.clear();

        // if SQL is enabled, load backpacks from the database
        // else load all backpacks from their files
		if (sql != null)
			sql.loadAllBackpacks();
		for (File file : new File(getDataFolder() + "/backpacks").listFiles()) {
			try {
				if (main.getBackpack(file.getName()) == null)
					backpacks.add(new Backpack(this, file.getName()));
			} catch (NullPointerException e) {
				getLogger().info("Couldn't load backpack: " + file.getName() + " (NullPointerException)");
			}
		}
		getLogger().info("Loaded " + backpacks.size() + " backpacks.");
	}

    /**
     * Save all backpacks. Removed, but not forgotten.
     */
    @Deprecated
	public void saveBackpacks() {
		getLogger().info("Saving backpacks...");
		for (Backpack backpack : backpacks) {
			backpack.save();
		}
		getLogger().info("Saved " + backpacks.size() + " backpacks.");
	}

    /**
     * Replace &# with the appropriate color
     * @param str
     * @return Color coded string
     */
	public static String color(String str) {
		return str.replace("&0", "§0")
				.replace("&1", "§1")
				.replace("&2", "§2")
				.replace("&3", "§3")
				.replace("&4", "§4")
				.replace("&5", "§5")
				.replace("&6", "§6")
				.replace("&7", "§7")
				.replace("&8", "§8")
				.replace("&9", "§9")
				.replace("&a", "§a")
				.replace("&b", "§b")
				.replace("&c", "§c")
				.replace("&d", "§d")
				.replace("&e", "§e")
				.replace("&f", "§f")
				.replace("&k", "§k")
				.replace("&l", "§l")
				.replace("&m", "§m")
				.replace("&n", "§n")
				.replace("&o", "§o")
				.replace("&r", "§r");
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