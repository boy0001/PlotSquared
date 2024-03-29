/*
 * Copyright (c) IntellectualCrafters - 2014. You are not allowed to distribute
 * and/or monetize any of our intellectual property. IntellectualCrafters is not
 * affiliated with Mojang AB. Minecraft is a trademark of Mojang AB.
 * 
 * >> File = TP.java >> Generated by: Citymonstret at 2014-08-09 01:42
 */

package com.intellectualcrafters.plot.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.PlotHelper;
import com.intellectualcrafters.plot.PlotId;
import com.intellectualcrafters.plot.PlotMain;

/**
 * @author Citymonstret
 */
public class TP extends SubCommand {

	public TP() {
		super(Command.TP, "Teleport to a plot", "tp {alias|id}", CommandCategory.TELEPORT, true);
	}

	@Override
	public boolean execute(Player plr, String... args) {
		if (args.length < 1) {
			PlayerFunctions.sendMessage(plr, C.NEED_PLOT_ID);
			return false;
		}
		String id = args[0];
		PlotId plotid;
		World world = plr.getWorld();
		if (args.length == 2) {
			if (Bukkit.getWorld(args[1]) != null) {
				world = Bukkit.getWorld(args[1]);
			}
		}
		if (!PlotMain.isPlotWorld(world)) {
			PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT_WORLD);
			return false;
		}
		Plot temp;
		if ((temp = isAlias(world, id)) != null) {
			PlotMain.teleportPlayer(plr, plr.getLocation(), temp);
			return true;
		}


		try {
			plotid = new PlotId(Integer.parseInt(id.split(";")[0]), Integer.parseInt(id.split(";")[1]));
			PlotMain.teleportPlayer(plr, plr.getLocation(), PlotHelper.getPlot(world, plotid));
			return true;
		}
		catch (Exception e) {
			PlayerFunctions.sendMessage(plr, C.NOT_VALID_PLOT_ID);
		}
		return false;
	}

	private Plot isAlias(World world, String a) {
		int index = 0;
		if (a.contains(";")) {
			String[] split = a.split(";");
			if ((split[1].length() > 0) && StringUtils.isNumeric(split[1])) {
				index = Integer.parseInt(split[1]);
			}
			a = split[0];
		}
		Player player = Bukkit.getPlayer(a);
		if (player != null) {
			Plot[] plots = PlotMain.getPlots(world, player).toArray(new Plot[0]);
			if (plots.length > index) {
				return plots[index];
			}
			return null;
		}
		for (Plot p : PlotMain.getPlots(world).values()) {
			if ((p.settings.getAlias().length() > 0) && p.settings.getAlias().equalsIgnoreCase(a)) {
				return p;
			}
		}
		return null;
	}
}
