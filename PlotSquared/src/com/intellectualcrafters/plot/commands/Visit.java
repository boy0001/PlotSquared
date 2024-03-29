/*
 * Copyright (c) IntellectualCrafters - 2014. You are not allowed to distribute
 * and/or monetize any of our intellectual property. IntellectualCrafters is not
 * affiliated with Mojang AB. Minecraft is a trademark of Mojang AB.
 * 
 * >> File = Visit.java >> Generated by: Citymonstret at 2014-08-09 01:42
 */

package com.intellectualcrafters.plot.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.PlotMain;

public class Visit extends SubCommand {
	public Visit() {
		super("visit", "plots.visit", "Visit someones plot", "visit {player} [#]", "v", CommandCategory.TELEPORT, true);
	}

	public List<Plot> getPlots(UUID uuid) {
		List<Plot> plots = new ArrayList<>();
		for (Plot p : PlotMain.getPlots()) {
			if (p.owner.equals(uuid)) {
				plots.add(p);
			}
		}
		return plots;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(Player plr, String... args) {
		if (args.length < 1) {
			PlayerFunctions.sendMessage(plr, C.NEED_USER);
			return true;
		}
		String username = args[0];
		List<Plot> plots = getPlots(Bukkit.getOfflinePlayer(username).getUniqueId());
		if (plots.isEmpty()) {
			PlayerFunctions.sendMessage(plr, C.FOUND_NO_PLOTS);
			return true;
		}
		if (args.length < 2) {
			Plot plot = plots.get(0);
			PlotMain.teleportPlayer(plr, plr.getLocation(), plot);
			return true;
		}
		int i;
		try {
			i = Integer.parseInt(args[1]);
		}
		catch (Exception e) {
			PlayerFunctions.sendMessage(plr, C.NOT_VALID_NUMBER);
			return true;
		}
		if ((i < 0) || (i > plots.size())) {
			PlayerFunctions.sendMessage(plr, C.NOT_VALID_NUMBER);
			return true;
		}
		Plot plot = plots.get(i);
		PlotMain.teleportPlayer(plr, plr.getLocation(), plot);
		return true;
	}
}
