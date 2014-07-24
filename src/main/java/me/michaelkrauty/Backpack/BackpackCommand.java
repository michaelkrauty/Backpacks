package me.michaelkrauty.Backpack;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
			String group = main.getWorldGroup(player.getWorld().getName());
			if (group != null) {
				Backpack backpack;
				if ((backpack = main.getBackpack(group, player.getUniqueId().toString())) == null) {
					backpack = new Backpack(main, group, player.getUniqueId().toString());
					main.backpacks.add(backpack);
				}
				player.openInventory(backpack.getInventory());
				main.open.put(player, backpack);
				return true;
			}
			player.sendMessage(ChatColor.GRAY + "This world isn't part of a share! Please report this to an admin.");
			return true;
		}
		player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
		return true;
	}
}
