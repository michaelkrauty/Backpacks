package me.michaelkrauty.Backpack;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created on 7/6/2014.
 *
 * @author michaelkrauty
 */
public class BackpackCommand implements CommandExecutor {

	private final Main main;

	public BackpackCommand(Main main) {
		this.main = main;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can use this command.");
			return true;
		}
		final Player player = (Player) sender;
		if (sender.hasPermission("backpack.use")) {
			if (args.length == 0) {
				for (String message : main.locale.getMessage("backpack_command")) {
					sender.sendMessage(main.color(message));
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("buy")) {
				String name = "Backpack";
				if (args.length != 1) {
					name = "";
					for (int i = 1; i < args.length; i++) {
						if (i == args.length - 1)
							name = name + args[i];
						else
							name = name + args[i] + " ";
					}
					name = Main.color(name);
				}
				if (player.getInventory().firstEmpty() != -1) {
					if (main.economy != null) {
						if (!(main.economy.getBalance(player) >= main.cost)) {
							for (String message : main.locale.getMessage("insufficient_funds")) {
								player.sendMessage(main.color(message));
							}
							return true;
						}
						main.economy.withdrawPlayer(player, main.cost);
					}

					if (main.cooldowns.get(player.getUniqueId()) != null) {
						if (main.cooldowns.get(player.getUniqueId()) != 0) {
							for (String message : main.locale.getMessage("cooldown")) {
								player.sendMessage(main.color(message).replace("<cooldown>", Integer.toString(main.cooldowns.get(player.getUniqueId()))));
							}
							return true;
						}
					}

					ItemStack backpack = new ItemStack(Material.CHEST, 1);
					ItemMeta meta = backpack.getItemMeta();
					meta.setDisplayName(name);
					UUID uuid = UUID.randomUUID();
					meta.setLore(Arrays.asList(uuid.toString()));
					backpack.setItemMeta(meta);
					player.getInventory().addItem(backpack);
					main.backpacks.add(new Backpack(main, uuid.toString()));
					if (main.cost == 0) {
						for (String message : main.locale.getMessage("got_backpack_without_price")) {
							player.sendMessage(main.color(message));
						}
					} else {
						for (String message : main.locale.getMessage("bought_backpack")) {
							player.sendMessage(main.color(message.replace("<backpack_cost>", Integer.toString(main.cost))));
						}
					}
					if (!player.hasPermission("backpack.nocooldown"))
						main.cooldowns.put(player.getUniqueId(), main.cooldown);
					return true;
				}
				for (String message : main.locale.getMessage("inventory_full")) {
					player.sendMessage(main.color(message));
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("name") || args[0].equalsIgnoreCase("rename")) {
				if (player.getItemInHand() != null) {
					if (player.getItemInHand().getType() == Material.CHEST) {
						String lore;
						if ((lore = player.getItemInHand().getItemMeta().getLore().get(0)) != null) {
							if (main.getBackpack(lore) != null) {
								String name = "";
								for (int i = 1; i < args.length; i++) {
									if (i == args.length - 1)
										name = name + args[i];
									else
										name = name + args[i] + " ";
								}
								name = Main.color(name);
								ItemMeta meta = player.getItemInHand().getItemMeta();
								meta.setDisplayName(name);
								player.getItemInHand().setItemMeta(meta);
								for (String message : main.locale.getMessage("renamed_backpack")) {
									player.sendMessage(main.color(message.replace("<new_name>", name)));
								}
								return true;
							}
						}
					}
				}
				for (String message : main.locale.getMessage("backpack_not_in_hand")) {
					player.sendMessage(main.color(message));
				}
				return true;
			}
			player.sendMessage(ChatColor.RED + cmd.getUsage());
			return true;
		}
		for (String message : main.locale.getMessage("permission_denied")) {
			player.sendMessage(main.color(message));
		}
		return true;
	}
}
