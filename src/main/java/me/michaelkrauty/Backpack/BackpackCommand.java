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
		final Player player = (Player) sender;
		if (sender.hasPermission("backpack.use")) {
			if (args.length == 0) {
				if (player.getInventory().firstEmpty() != -1) {
					ItemStack backpack = new ItemStack(Material.CHEST, 1);
					ItemMeta meta = backpack.getItemMeta();
					meta.setDisplayName("Backpack");
					UUID uuid = UUID.randomUUID();
					meta.setLore(Arrays.asList(uuid.toString()));
					backpack.setItemMeta(meta);
					player.getInventory().addItem(backpack);
					main.backpacks.add(new Backpack(main, uuid.toString()));
					return true;
				}
				player.sendMessage(ChatColor.RED + "Your inventory is too full to give you a backpack!");
				return true;
			}
			if (args.length > 1) {
				if (args[0].equalsIgnoreCase("name") || args[0].equalsIgnoreCase("rename")) {
					if (player.getItemInHand() != null) {
						if (player.getItemInHand().getType() == Material.CHEST) {
							String lore;
							if ((lore = player.getItemInHand().getItemMeta().getLore().get(0)) != null) {
								Backpack backpack;
								if ((backpack = main.getBackpack(lore)) != null) {
									String name = "";
									for (int i = 1; i < args.length; i++) {
										if (i == args.length - 1)
											name = name + args[i];
										else
											name = name + args[i] + " ";
									}
									ItemMeta meta = player.getItemInHand().getItemMeta();
									meta.setDisplayName(name);
									player.getItemInHand().setItemMeta(meta);
									player.sendMessage(ChatColor.GRAY + "Renamed this backpack to " + name);
									return true;
								}
							}
						}
						player.sendMessage(ChatColor.RED + "Make sure you're holding a backpack in your hand.");
						return true;
					}
					player.sendMessage(ChatColor.RED + "Make sure you're holding a backpack in your hand.");
					return true;
				}
				player.sendMessage(ChatColor.RED + "Usage: /backpack rename <desired name>");
				return true;
			}
			player.sendMessage(ChatColor.RED + "Usage: /backpack rename <desired name>");
			return true;
		}
		player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
		return true;
	}
}
