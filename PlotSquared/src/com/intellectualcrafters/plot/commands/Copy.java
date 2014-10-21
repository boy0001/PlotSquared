/*
 * Copyright (c) IntellectualCrafters - 2014. You are not allowed to distribute
 * and/or monetize any of our intellectual property. IntellectualCrafters is not
 * affiliated with Mojang AB. Minecraft is a trademark of Mojang AB.
 * 
 * >> File = Clear.java >> Generated by: Citymonstret at 2014-08-09 01:41
 */

package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.*;

import org.bukkit.entity.Player;

/**
 * Created by Citymonstret on 2014-08-01.
 */
public class Copy extends SubCommand {

	public Copy() {
		super(Command.COPY, "Copy a plot", "copy", CommandCategory.ACTIONS, true);
	}

	@Override
	public boolean execute(Player plr, String... args) {
		if (!PlayerFunctions.isInPlot(plr)) {
			PlayerFunctions.sendMessage(plr, "You're not in a plot.");
			return false;
		}
		Plot plot = PlayerFunctions.getCurrentPlot(plr);
		if (((plot == null) || !plot.hasOwner() || !plot.getOwner().equals(plr.getUniqueId()))
				&& !plr.hasPermission("plots.admin")) {
			PlayerFunctions.sendMessage(plr, C.NO_PLOT_PERMS);
			return false;
		}
		if (!PlayerFunctions.getTopPlot(plr.getWorld(), plot).equals(PlayerFunctions.getBottomPlot(plr.getWorld(), plot))) {
            PlayerFunctions.sendMessage(plr, C.UNLINK_REQUIRED);
            return false;
        }
        assert plot != null;
        int size = (PlotHelper.getPlotTopLocAbs(plr.getWorld(), plot.getId()).getBlockX() - PlotHelper.getPlotBottomLocAbs(plr.getWorld(), plot.getId()).getBlockX());
        PlotSelection selection = new PlotSelection(size, plr.getWorld(), plot);
        if(PlotSelection.currentSelection.containsKey(plr.getName())) {
            PlotSelection.currentSelection.remove(plr.getName());
        }
        PlotSelection.currentSelection.put(plr.getName(), selection);
        sendMessage(plr, C.CLIPBOARD_SET);
        //section.paste(plr.getWorld(), PlotHelper.getPlot(plr.getWorld()zs, new PlotId(plot.getId().x + 1, plot.getId().y)));
		return true;
	}
}