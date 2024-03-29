/*
 * Copyright (c) IntellectualCrafters - 2014. You are not allowed to distribute
 * and/or monetize any of our intellectual property. IntellectualCrafters is not
 * affiliated with Mojang AB. Minecraft is a trademark of Mojang AB.
 * 
 * >> File = PlotClearEvent.java >> Generated by: Citymonstret at 2014-08-09
 * 15:21
 */

package com.intellectualcrafters.plot.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.intellectualcrafters.plot.PlotId;

/**
 * Called when a plot is cleared
 */
public class PlotClearEvent extends Event implements Cancellable {
	private static HandlerList handlers = new HandlerList();
	private boolean cancelled;

	private PlotId id;
	private String world;

	/**
	 * PlotDeleteEvent: Called when a plot is cleared
	 * 
	 * @param world
	 * @param id
	 */
	public PlotClearEvent(String world, PlotId id) {
		this.id = id;
		this.world = world;
	}

	/**
	 * Get the PlotId
	 * 
	 * @return PlotId
	 */
	public PlotId getPlotId() {
		return this.id;
	}

	/**
	 * Get the world name
	 * 
	 * @return String
	 */
	public String getWorld() {
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
