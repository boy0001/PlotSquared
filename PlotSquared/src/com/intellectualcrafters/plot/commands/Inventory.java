/*
 * Copyright (c) IntellectualCrafters - 2014. You are not allowed to distribute
 * and/or monetize any of our intellectual property. IntellectualCrafters is not
 * affiliated with Mojang AB. Minecraft is a trademark of Mojang AB.
 * 
 * >> File = Inventory.java >> Generated by: Citymonstret at 2014-08-10 13:57
 */

package com.intellectualcrafters.plot.commands;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Inventory extends SubCommand {

	public Inventory() {
		super("inventory", "plots.inventory", "Open a command inventory", "inventory", "inv", CommandCategory.INFO, true);
	}

	@Override
	public boolean execute(final Player plr, String... args) {
		ArrayList<SubCommand> cmds = new ArrayList<>();
		for (SubCommand cmd : MainCommand.subCommands) {
			if (cmd.permission.hasPermission(plr)) {
				cmds.add(cmd);
			}
		}
		int size = 9 * (int) Math.ceil(cmds.size() / 9.0);
		org.bukkit.inventory.Inventory inventory = Bukkit.createInventory(null, size, "PlotSquared Commands");
		for (SubCommand cmd : cmds) {
			inventory.addItem(getItem(cmd));
		}
		plr.openInventory(inventory);
		return true;
	}

	private ItemStack getItem(final SubCommand cmd) {
		ItemStack stack = new ItemStack(Material.COMMAND);
		ItemMeta meta = stack.getItemMeta();
		{
			meta.setDisplayName(ChatColor.GREEN + cmd.cmd + ChatColor.DARK_GRAY + " [" + ChatColor.GREEN + cmd.alias
					+ ChatColor.DARK_GRAY + "]");
			meta.setLore(new ArrayList<String>() {
				{
					add(ChatColor.RED + "Category: " + ChatColor.GOLD + cmd.category.toString());
					add(ChatColor.RED + "Description: " + ChatColor.GOLD + cmd.description);
					add(ChatColor.RED + "Usage: " + ChatColor.GOLD + "/plot " + cmd.usage);
				}
			});
		}
		stack.setItemMeta(meta);
		return stack;
	}
}
