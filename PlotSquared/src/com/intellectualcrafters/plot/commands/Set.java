/*
 * Copyright (c) IntellectualCrafters - 2014. You are not allowed to distribute
 * and/or monetize any of our intellectual property. IntellectualCrafters is not
 * affiliated with Mojang AB. Minecraft is a trademark of Mojang AB.
 * 
 * >> File = Set.java >> Generated by: Citymonstret at 2014-08-09 01:42
 */

package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.*;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.events.PlotFlagAddEvent;
import com.intellectualcrafters.plot.events.PlotFlagRemoveEvent;
import com.intellectualcrafters.plot.listeners.PlayerEvents;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Citymonstret
 */
public class Set extends SubCommand {

	public Set() {
		super(Command.SET, "Set a plot value", "set {arg} {value...}", CommandCategory.ACTIONS, true);
	}

	public static String[] values = new String[] { "biome", "wall", "wall_filling", "floor", "alias", "home",
			"flag" };
	public static String[] aliases = new String[] { "b", "w", "wf", "f", "a", "h", "fl" };

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(Player plr, String... args) {
		if (!PlayerFunctions.isInPlot(plr)) {
			PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT);
			return false;
		}
		Plot plot = PlayerFunctions.getCurrentPlot(plr);
        if(!plot.hasOwner()) {
            sendMessage(plr, C.PLOT_NOT_CLAIMED);
            return false;
        }
		if (!plot.hasRights(plr) && !plr.hasPermission("plots.admin")) {
			PlayerFunctions.sendMessage(plr, C.NO_PLOT_PERMS);
			return false;
		}
		if (args.length < 1) {
			StringBuilder builder = new StringBuilder();
			builder.append(C.SUBCOMMAND_SET_OPTIONS_HEADER.s());
			builder.append(getArgumentList(values));
			PlayerFunctions.sendMessage(plr, builder.toString());
			return false;
		}
		for (int i = 0; i < aliases.length; i++) {
			if (aliases[i].equalsIgnoreCase(args[0])) {
				args[0] = values[i];
				break;
			}
		}
		/* TODO: Implement option */
		boolean advanced_permissions = true;
		if (advanced_permissions) {
			if (!plr.hasPermission("plots.set." + args[0].toLowerCase())) {
				PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.set."+args[0].toLowerCase());
				return false;
			}
		}

		if (args[0].equalsIgnoreCase("flag")) {
			if (args.length < 2) {
				String message = StringUtils.join(FlagManager.getFlags(), "&c, &6");
				if (PlotMain.worldGuardListener != null) {
					if (message.equals("")) {
						message = StringUtils.join(PlotMain.worldGuardListener.str_flags, "&c, &6");
					}
					else {
						message += "," + StringUtils.join(PlotMain.worldGuardListener.str_flags, "&c, &6");
					}
				}
				PlayerFunctions.sendMessage(plr, C.NEED_KEY.s().replaceAll("%values%", message));
				return false;
			}
			
			AbstractFlag af = new AbstractFlag("");
			
			try {
			    af = new AbstractFlag(args[1].toLowerCase());
			}
			catch (Exception e) {
			    
			}
			
			if (!FlagManager.getFlags().contains(af) && ((PlotMain.worldGuardListener == null) || !PlotMain.worldGuardListener.str_flags.contains(args[1].toLowerCase()))) {
				PlayerFunctions.sendMessage(plr, C.NOT_VALID_FLAG);
				return false;
			}
			if (!PlotMain.hasPermission(plr, "plots.set.flag." + args[1].toLowerCase())) {
				PlayerFunctions.sendMessage(plr, C.NO_PERMISSION);
				return false;
			}
			if (args.length == 2) {
				if (plot.settings.getFlag(args[1].toLowerCase()) == null) {
					if (PlotMain.worldGuardListener != null) {
						if (PlotMain.worldGuardListener.str_flags.contains(args[1].toLowerCase())) {
							PlotMain.worldGuardListener.removeFlag(plr, plr.getWorld(), plot, args[1]);
							return false;
						}
					}
					PlayerFunctions.sendMessage(plr, C.FLAG_NOT_IN_PLOT);
					return false;
				}
				Flag flag = plot.settings.getFlag(args[1].toLowerCase());
				PlotFlagRemoveEvent event = new PlotFlagRemoveEvent(flag, plot);
				Bukkit.getServer().getPluginManager().callEvent(event);
				if (event.isCancelled()) {
					PlayerFunctions.sendMessage(plr, C.FLAG_NOT_REMOVED);
					event.setCancelled(true);
					return false;
				}
				java.util.Set<Flag> newflags = plot.settings.getFlags();
				Flag oldFlag = plot.settings.getFlag(args[1].toLowerCase());
				if (oldFlag != null) {
					newflags.remove(oldFlag);
				}
				plot.settings.setFlags(newflags.toArray(new Flag[0]));
				DBFunc.setFlags(plr.getWorld().getName(), plot, newflags.toArray(new Flag[0]));
				PlayerFunctions.sendMessage(plr, C.FLAG_REMOVED);
	            PlayerEvents.plotEntry(plr, plot);
				return true;
			}
			try {
				String value = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
				value = af.parseValue(value);
				
				if (value==null) {
				    PlayerFunctions.sendMessage(plr, af.getValueDesc());
				    return false;
				}
				
				if ((FlagManager.getFlag(args[1].toLowerCase()) == null) && (PlotMain.worldGuardListener != null)) {
					PlotMain.worldGuardListener.addFlag(plr, plr.getWorld(), plot, args[1], value);
					return false;
				}
				
				Flag flag = new Flag(FlagManager.getFlag(args[1].toLowerCase(), true), value);
				PlotFlagAddEvent event = new PlotFlagAddEvent(flag, plot);
				Bukkit.getServer().getPluginManager().callEvent(event);
				if (event.isCancelled()) {
					PlayerFunctions.sendMessage(plr, C.FLAG_NOT_ADDED);
					event.setCancelled(true);
					return false;
				}
				plot.settings.addFlag(flag);
				DBFunc.setFlags(plr.getWorld().getName(), plot, plot.settings.getFlags().toArray(new Flag[0]));
				PlayerFunctions.sendMessage(plr, C.FLAG_ADDED);
				PlayerEvents.plotEntry(plr, plot);
				return true;
			}
			catch (Exception e) {
				PlayerFunctions.sendMessage(plr, "&c" + e.getMessage());
				return false;
			}
		}


		if (args[0].equalsIgnoreCase("home")) {
			if (args.length < 2) {
				PlayerFunctions.sendMessage(plr, C.MISSING_POSITION);
				return false;
			}
			PlotHomePosition position = null;
			for (PlotHomePosition p : PlotHomePosition.values()) {
				if (p.isMatching(args[1])) {
					position = p;
				}
			}
			if (position == null) {
				PlayerFunctions.sendMessage(plr, C.INVALID_POSITION);
				return false;
			}
			DBFunc.setPosition(plr.getWorld().getName(), plot, position.toString());
			PlayerFunctions.sendMessage(plr, C.POSITION_SET);
			return true;
		}

		if (args[0].equalsIgnoreCase("alias")) {
			if (args.length < 2) {
				PlayerFunctions.sendMessage(plr, C.MISSING_ALIAS);
				return false;
			}
			String alias = args[1];
			for (Plot p : PlotMain.getPlots(plr.getWorld()).values()) {
				if (p.settings.getAlias().equalsIgnoreCase(alias)) {
					PlayerFunctions.sendMessage(plr, C.ALIAS_IS_TAKEN);
					return false;
				}
				if (Bukkit.getOfflinePlayer(alias).hasPlayedBefore()) {
					PlayerFunctions.sendMessage(plr, C.ALIAS_IS_TAKEN);
					return false;
				}
			}
			DBFunc.setAlias(plr.getWorld().getName(), plot, alias);
			PlayerFunctions.sendMessage(plr, C.ALIAS_SET_TO.s().replaceAll("%alias%", alias));
			return true;
		}
		if (args[0].equalsIgnoreCase("biome")) {
			if (args.length < 2) {
				PlayerFunctions.sendMessage(plr, C.NEED_BIOME);
				return true;
			}
			Biome biome = null;
			for (Biome b : Biome.values()) {
				if (b.toString().equalsIgnoreCase(args[1])) {
					biome = b;
					break;
				}
			}
			if (biome == null) {
				PlayerFunctions.sendMessage(plr, getBiomeList(Arrays.asList(Biome.values())));
				return true;
			}
			PlotHelper.setBiome(plr.getWorld(), plot, biome);
			PlayerFunctions.sendMessage(plr, C.BIOME_SET_TO.s() + biome.toString().toLowerCase());
			return true;
		}
		if (args[0].equalsIgnoreCase("wall")) {
			PlotWorld plotworld = PlotMain.getWorldSettings(plr.getWorld());
			if (plotworld == null) {
				PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT_WORLD);
				return true;
			}
			if (args.length < 2) {
				PlayerFunctions.sendMessage(plr, C.NEED_BLOCK);
				return true;
			}
			Material material = null;
			for (Material m : PlotWorld.BLOCKS) {
				if (m.toString().equalsIgnoreCase(args[1])) {
					material = m;
					break;
				}
			}
			if (material == null) {
				PlayerFunctions.sendMessage(plr, getBlockList(PlotWorld.BLOCKS));
				return true;
			}
			byte data = 0;

			if (args.length > 2) {
				try {
					data = (byte) Integer.parseInt(args[2]);
				}
				catch (Exception e) {
					PlayerFunctions.sendMessage(plr, C.NOT_VALID_DATA);
					return true;
				}
			}
			PlayerFunctions.sendMessage(plr, C.GENERATING_WALL);
			PlotHelper.adjustWall(plr, plot, new PlotBlock((short) material.getId(), data));
			return true;
		}
		if (args[0].equalsIgnoreCase("floor")) {
			if (args.length < 2) {
				PlayerFunctions.sendMessage(plr, C.NEED_BLOCK);
				return true;
			}
			PlotWorld plotworld = PlotMain.getWorldSettings(plr.getWorld());
			if (plotworld == null) {
				PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT_WORLD);
				return true;
			}
			//
			@SuppressWarnings("unchecked")
			ArrayList<Material> materials = (ArrayList<Material>) PlotWorld.BLOCKS.clone();
			materials.add(Material.AIR);
			//
			String[] strings = args[1].split(",");
			//
			int index = 0;
			//
			byte b = (byte) 0;
			Material m = null;
			//
			PlotBlock[] blocks = new PlotBlock[strings.length];

			for (String s : strings) {
				s = s.replaceAll(",", "");
				String[] ss = s.split(";");
				ss[0] = ss[0].replaceAll(";", "");
				for (Material ma : materials) {
					if (ma.toString().equalsIgnoreCase(ss[0])) {
						m = ma;
					}
				}
				if (m == null) {
					PlayerFunctions.sendMessage(plr, C.NOT_VALID_BLOCK);
					return true;
				}
				if (ss.length == 1) {

					blocks[index] = new PlotBlock((short) m.getId(), (byte) 0);
				}
				else {
					try {
						b = (byte) Integer.parseInt(ss[1]);
					}
					catch (Exception e) {
						PlayerFunctions.sendMessage(plr, C.NOT_VALID_DATA);
						return true;
					}
					blocks[index] = new PlotBlock((short) m.getId(), b);
				}
				index++;
			}
			PlotHelper.setFloor(plr, plot, blocks);
			return true;
		}
		if (args[0].equalsIgnoreCase("wall_filling")) {
			if (args.length < 2) {
				PlayerFunctions.sendMessage(plr, C.NEED_BLOCK);
				return true;
			}
			PlotWorld plotworld = PlotMain.getWorldSettings(plr.getWorld());
			if (plotworld == null) {
				PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT_WORLD);
				return true;
			}
			Material material = null;
			for (Material m : PlotWorld.BLOCKS) {
				if (m.toString().equalsIgnoreCase(args[1])) {
					material = m;
					break;
				}
			}
			if (material == null) {
				PlayerFunctions.sendMessage(plr, getBlockList(PlotWorld.BLOCKS));
				return true;
			}
			byte data = 0;

			if (args.length > 2) {
				try {
					data = (byte) Integer.parseInt(args[2]);
				}
				catch (Exception e) {
					PlayerFunctions.sendMessage(plr, C.NOT_VALID_DATA);
					return true;
				}
			}
			PlotHelper.adjustWallFilling(plr, plot, new PlotBlock((short) material.getId(), data));
			return true;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(C.SUBCOMMAND_SET_OPTIONS_HEADER.s());
		builder.append(getArgumentList(values));
		PlayerFunctions.sendMessage(plr, builder.toString());
		return false;
	}

	private String getMaterial(Material m) {
		return ChatColor.translateAlternateColorCodes('&', C.BLOCK_LIST_ITEM.s().replaceAll("%mat%", m.toString().toLowerCase()));
	}

	private String getBiome(Biome b) {
		return ChatColor.translateAlternateColorCodes('&', C.BLOCK_LIST_ITEM.s().replaceAll("%mat%", b.toString().toLowerCase()));
	}

	private String getString(String s) {
		return ChatColor.translateAlternateColorCodes('&', C.BLOCK_LIST_ITEM.s().replaceAll("%mat%", s));
	}

	private String getArgumentList(String[] strings) {
		StringBuilder builder = new StringBuilder();
		for (String s : strings) {
			builder.append(getString(s));
		}
		return builder.toString().substring(1, builder.toString().length() - 1);
	}

	private String getBiomeList(List<Biome> biomes) {
		StringBuilder builder = new StringBuilder();
		builder.append(ChatColor.translateAlternateColorCodes('&', C.NOT_VALID_BLOCK_LIST_HEADER.s()));
		for (Biome b : biomes) {
			builder.append(getBiome(b));
		}
		return builder.toString().substring(1, builder.toString().length() - 1);
	}

	private String getBlockList(List<Material> blocks) {
		StringBuilder builder = new StringBuilder();
		builder.append(ChatColor.translateAlternateColorCodes('&', C.NOT_VALID_BLOCK_LIST_HEADER.s()));
		for (Material b : blocks) {
			builder.append(getMaterial(b));
		}
		return builder.toString().substring(1, builder.toString().length() - 1);
	}

}
