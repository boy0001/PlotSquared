/*
 * Copyright (c) IntellectualCrafters - 2014. You are not allowed to distribute
 * and/or monetize any of our intellectual property. IntellectualCrafters is not
 * affiliated with Mojang AB. Minecraft is a trademark of Mojang AB.
 * 
 * >> File = list.java >> Generated by: Citymonstret at 2014-08-09 01:41
 */

package com.intellectualcrafters.plot.commands;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.PlotId;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.UUIDHandler;

/**
 * @author Citymonstret
 */
public class list extends SubCommand {

	public list() {
		super(Command.LIST, "List all plots", "list {mine|shared|all|world}", CommandCategory.INFO, false);
	}

	@Override
	public boolean execute(Player plr, String... args) {
		if (args.length < 1) {
			StringBuilder builder = new StringBuilder();
			builder.append(C.SUBCOMMAND_SET_OPTIONS_HEADER.s());
			if (plr!=null) {
				builder.append(getArgumentList(new String[] { "mine", "shared", "world", "all" }));
			}
			else {
				builder.append(getArgumentList(new String[] { "all" }));
			}
			PlayerFunctions.sendMessage(plr, builder.toString());
			return true;
		}
		if (args[0].equalsIgnoreCase("mine") && plr!=null) {
			StringBuilder string = new StringBuilder();
			string.append(C.PLOT_LIST_HEADER.s().replaceAll("%word%", "your") + "\n");
			int idx = 0;
			for (Plot p : PlotMain.getPlots(plr)) {
				string.append(C.PLOT_LIST_ITEM.s().replaceAll("%id%", p.id.x + ";" + p.id.y + ";" + p.world).replaceAll("%owner%", getName(p.owner))
						+ "\n");
				idx++;
			}
			if (idx == 0) {
				PlayerFunctions.sendMessage(plr, C.NO_PLOTS);
				return true;
			}
			string.append(C.PLOT_LIST_FOOTER.s().replaceAll("%word%", "You have").replaceAll("%num%", idx + "").replaceAll("%plot%", idx == 1
					? "plot" : "plots"));
			PlayerFunctions.sendMessage(plr, string.toString());
			return true;
		}
		else
			if (args[0].equalsIgnoreCase("shared") && plr!=null) {
				StringBuilder string = new StringBuilder();
				string.append(C.PLOT_LIST_HEADER.s().replaceAll("%word%", "all") + "\n");
				for (Plot p : PlotMain.getPlots()) {
					if (p.helpers.contains(plr.getUniqueId())) {
						string.append(C.PLOT_LIST_ITEM.s().replaceAll("%id%", p.id.x + ";" + p.id.y + ";" + p.world).replaceAll("%owner%", getName(p.owner))
								+ "\n");
					}
				}
				string.append(C.PLOT_LIST_FOOTER.s().replaceAll("%word%", "There is").replaceAll("%num%", PlotMain.getPlots().size()
						+ "").replaceAll("%plot%", PlotMain.getPlots().size() == 1 ? "plot" : "plots"));
				PlayerFunctions.sendMessage(plr, string.toString());
				return true;
			}
			else
				if (args[0].equalsIgnoreCase("all")) {
					StringBuilder string = new StringBuilder();
					string.append(C.PLOT_LIST_HEADER.s().replaceAll("%word%", "all") + "\n");
					for (Plot p : PlotMain.getPlots()) {
						string.append(C.PLOT_LIST_ITEM.s().replaceAll("%id%", p.id.x + ";" + p.id.y + ";" + p.world).replaceAll("%owner%", getName(p.owner))
								+ "\n");
					}
					string.append(C.PLOT_LIST_FOOTER.s().replaceAll("%word%", "There is").replaceAll("%num%", PlotMain.getPlots().size()
							+ "").replaceAll("%plot%", PlotMain.getPlots().size() == 1 ? "plot" : "plots"));
					PlayerFunctions.sendMessage(plr, string.toString());
					return true;
				}
				else
					if (args[0].equalsIgnoreCase("world") && plr!=null) {
						StringBuilder string = new StringBuilder();
						string.append(C.PLOT_LIST_HEADER.s().replaceAll("%word%", "all") + "\n");
						HashMap<PlotId, Plot> plots = PlotMain.getPlots(plr.getWorld());
						for (Plot p : plots.values()) {
							string.append(C.PLOT_LIST_ITEM.s().replaceAll("%id%", p.id.x + ";" + p.id.y + ";" + p.world).replaceAll("%owner%", getName(p.owner))
									+ "\n");
						}
						string.append(C.PLOT_LIST_FOOTER.s().replaceAll("%word%", "There is").replaceAll("%num%", plots.values().size()
								+ "").replaceAll("%plot%", plots.values().size() == 1 ? "plot" : "plots"));
						PlayerFunctions.sendMessage(plr, string.toString());
						return true;
					}
					else {
						execute(plr);
						return false;
					}
	}

	private static String getName(UUID id) {
		if (id == null) {
			return "none";
		}
		/*
		 * String name = Bukkit.getOfflinePlayer(id).getName(); if (name ==
		 * null) { return "none"; } return name;
		 */
		return UUIDHandler.getName(id);
	}

	private String getArgumentList(String[] strings) {
		StringBuilder builder = new StringBuilder();
		for (String s : strings) {
			builder.append(getString(s));
		}
		return builder.toString().substring(1, builder.toString().length() - 1);
	}

	private String getString(String s) {
		return ChatColor.translateAlternateColorCodes('&', C.BLOCK_LIST_ITEM.s().replaceAll("%mat%", s));
	}

}
