/*
 * Copyright (c) IntellectualCrafters - 2014. You are not allowed to distribute
 * and/or monetize any of our intellectual property. IntellectualCrafters is not
 * affiliated with Mojang AB. Minecraft is a trademark of Mojang AB.
 * 
 * >> File = CommandPermission.java >> Generated by: Citymonstret at 2014-08-09
 * 01:41
 */

package com.intellectualcrafters.plot.commands;

import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PlotMain;

/**
 * Created by Citymonstret on 2014-08-03.
 *
 * @author Citymonstret
 */
public class CommandPermission {

	/**
	 *
	 */
	public String permission;

	/**
	 * @param permission
	 */
	public CommandPermission(String permission) {
		this.permission = permission.toLowerCase();
	}

	/**
	 * @param player
	 * @return
	 */
	public boolean hasPermission(Player player) {
		return PlotMain.hasPermission(player, this.permission) || PlotMain.hasPermission(player, "plots.admin");
	}
}
