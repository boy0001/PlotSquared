/*
 * Copyright (c) IntellectualCrafters - 2014. You are not allowed to distribute
 * and/or monetize any of our intellectual property. IntellectualCrafters is not
 * affiliated with Mojang AB. Minecraft is a trademark of Mojang AB.
 * 
 * >> File = PlayerClaimPlotEvent.java >> Generated by: Citymonstret at
 * 2014-08-09 15:21
 */

package com.intellectualcrafters.plot.events;

import java.util.ArrayList;

import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.intellectualcrafters.plot.PlotId;

/**
 * Created by Citymonstret on 2014-08-09.
 */
public class PlotUnlinkEvent extends Event implements Cancellable {
	private static HandlerList handlers = new HandlerList();
	private boolean cancelled;

	private ArrayList<PlotId> plots;
	private World world;

	/**
	 * Called when a mega-plot is unlinked.
	 * 
	 * @param world
	 * @param plots
	 */
	public PlotUnlinkEvent(World world, ArrayList<PlotId> plots) {
		this.plots = plots;
		this.world = world;
	}

	/**
	 * Get the plots involved
	 * 
	 * @return PlotId
	 */
	public ArrayList<PlotId> getPlots() {
		return this.plots;
	}

	public World getWorld() {
		return this.world;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean b) {
		this.cancelled = b;
	}
}
