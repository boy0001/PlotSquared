/*
 * Copyright (c) IntellectualCrafters - 2014. You are not allowed to distribute
 * and/or monetize any of our intellectual property. IntellectualCrafters is not
 * affiliated with Mojang AB. Minecraft is a trademark of Mojang AB.
 * 
 * >> File = Home.java >> Generated by: Citymonstret at 2014-08-09 01:41
 */

package com.intellectualcrafters.plot.commands;

import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.PlotMain;

/**
 * @author Citymonstret
 */
public class Home extends SubCommand {

	public Home() {
		super(Command.HOME, "Go to your plot", "home {id|alias}", CommandCategory.TELEPORT, true);
	}

	private Plot isAlias(String a) {
		for (Plot p : PlotMain.getPlots()) {
			if ((p.settings.getAlias().length() > 0) && p.settings.getAlias().equalsIgnoreCase(a)) {
				return p;
			}
		}
		return null;
	}

	@Override
	public boolean execute(Player plr, String... args) {
		Plot[] plots = PlotMain.getPlots(plr).toArray(new Plot[0]);
		if (plots.length == 1) {
			PlotMain.teleportPlayer(plr, plr.getLocation(), plots[0]);
			return true;
		}
		else
			if (plots.length > 1) {
				if (args.length < 1) {
					args = new String[] { "1" };
				}
				int id = 0;
				try {
					id = Integer.parseInt(args[0]);
				}
				catch (Exception e) {
					Plot temp;
					if ((temp = isAlias(args[0])) != null) {
						if (temp.hasOwner()) {
							if (temp.getOwner().equals(plr.getUniqueId())) {
								PlotMain.teleportPlayer(plr, plr.getLocation(), temp);
								return true;
							}
						}
						PlayerFunctions.sendMessage(plr, C.NOT_YOUR_PLOT);
						return false;
					}
					PlayerFunctions.sendMessage(plr, C.NOT_VALID_NUMBER);
					return true;
				}
				if ((id > (plots.length)) || (id < 1)) {
					PlayerFunctions.sendMessage(plr, C.NOT_VALID_NUMBER);
					return false;
				}
				PlotMain.teleportPlayer(plr, plr.getLocation(), plots[id - 1]);
				return true;
			}
			else {
				PlayerFunctions.sendMessage(plr, C.NO_PLOTS);
				return true;
			}
	}
}
