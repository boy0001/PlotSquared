/*
 * Copyright (c) IntellectualCrafters - 2014. You are not allowed to distribute
 * and/or monetize any of our intellectual property. IntellectualCrafters is not
 * affiliated with Mojang AB. Minecraft is a trademark of Mojang AB.
 * 
 * >> File = PlayerEvents.java >> Generated by: Citymonstret at 2014-08-09 01:43
 */

package com.intellectualcrafters.plot.listeners;

import com.intellectualcrafters.plot.*;
import com.intellectualcrafters.plot.commands.Setup;
import com.intellectualcrafters.plot.events.PlayerEnterPlotEvent;
import com.intellectualcrafters.plot.events.PlayerLeavePlotEvent;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.*;

/**
 * Player Events involving plots
 *
 * @author Citymonstret
 */
@SuppressWarnings("unused")
public class PlayerEvents implements Listener {

	private static String getName(UUID uuid) {
		return UUIDHandler.getName(uuid);
    }

    private static PlotWorld getPlotWorld(World world) {
        return PlotMain.getWorldSettings(world);
    }

	public static boolean enteredPlot(Location l1, Location l2) {
		PlotId p1 = PlayerFunctions.getPlot(new Location(l1.getWorld(), l1.getBlockX(), 64, l1.getBlockZ()));
		PlotId p2 = PlayerFunctions.getPlot(new Location(l2.getWorld(), l2.getBlockX(), 64, l2.getBlockZ()));
		if (p2 == null) {
			return false;
		}
		if (p1 == null) {
			return true;
		}
		if (p1.equals(p2)) {
			return false;
		}
		return true;
	}

	public static boolean leftPlot(Location l1, Location l2) {
		PlotId p1 = PlayerFunctions.getPlot(new Location(l1.getWorld(), l1.getBlockX(), 64, l1.getBlockZ()));
		PlotId p2 = PlayerFunctions.getPlot(new Location(l2.getWorld(), l2.getBlockX(), 64, l2.getBlockZ()));
		if (p1 == null) {
			return false;
		}
		if (p2 == null) {
			return true;
		}
		if (p1.equals(p2)) {
			return false;
		}
		return true;
	}

	private static boolean isPlotWorld(Location l) {
		return PlotMain.isPlotWorld(l.getWorld());
	}

	private static boolean isPlotWorld(World w) {
		return PlotMain.isPlotWorld(w);
	}

	public static boolean isInPlot(Location loc) {
		return getCurrentPlot(loc) != null;
	}

	public static Plot getCurrentPlot(Location loc) {
		PlotId id = PlayerFunctions.getPlot(loc);
		if (id == null) {
			return null;
		}
		World world = loc.getWorld();
		if (PlotMain.getPlots(world).containsKey(id)) {
			return PlotMain.getPlots(world).get(id);
		}
		return new Plot(id, null, Biome.FOREST, new ArrayList<UUID>(), new ArrayList<UUID>(), loc.getWorld().getName());
	}

	@EventHandler
	public static void onWorldLoad(WorldLoadEvent event) {
		PlotMain.loadWorld(event.getWorld());
	}

	@EventHandler
	public static void onJoin(PlayerJoinEvent event) {
		if (!event.getPlayer().hasPlayedBefore()) {
			event.getPlayer().saveData();
		}
		textures(event.getPlayer());
        if(isInPlot(event.getPlayer().getLocation())) {
            plotEntry(event.getPlayer(), getCurrentPlot(event.getPlayer().getLocation()));
        }
	}

	private static void textures(Player p) {
		if ((Settings.PLOT_SPECIFIC_RESOURCE_PACK.length() > 1) && isPlotWorld(p.getWorld())) {
			p.setResourcePack(Settings.PLOT_SPECIFIC_RESOURCE_PACK);
		}
	}

	@EventHandler
	public static void onChangeWorld(PlayerChangedWorldEvent event) {
		if (isPlotWorld(event.getFrom()) && (Settings.PLOT_SPECIFIC_RESOURCE_PACK.length() > 1)) {
			event.getPlayer().setResourcePack("");
		}
		else {
			textures(event.getPlayer());
		}
	}

    private static WeatherType getWeatherType(String str) {
        str = str.toLowerCase();
        if(str.equals("rain")) {
            return WeatherType.DOWNFALL;
        } else {
            return WeatherType.CLEAR;
        }
    }

    private static GameMode getGameMode(String str) {
        if (str.equals("creative")) {
            return GameMode.CREATIVE;
        } else if (str.equals("survival")) {
            return GameMode.SURVIVAL;
        } else if (str.equals("adventure")) {
            return GameMode.ADVENTURE;
        } else {
            return Bukkit.getDefaultGameMode();
        }
    }

	public static void plotEntry(Player player, Plot plot) {
		if (plot.hasOwner()) {
            if(plot.settings.getFlag("gamemode") != null) {
                player.setGameMode(getGameMode(plot.settings.getFlag("gamemode").getValue()));
            }
            if(plot.settings.getFlag("time") != null) {
                try {
                    Long time = Long.parseLong(plot.settings.getFlag("time").getValue());
                    player.setPlayerTime(time, false);
                } catch(Exception e) {
                    plot.settings.setFlags(FlagManager.removeFlag(plot.settings.getFlags(), "time"));
                }
            }
            if(plot.settings.getFlag("weather") != null) {
                player.setPlayerWeather(getWeatherType(plot.settings.getFlag("weather").getValue()));
            }
			if (C.TITLE_ENTERED_PLOT.s().length() > 2) {
				String sTitleMain = C.TITLE_ENTERED_PLOT.s().replaceFirst("%s", plot.getDisplayName());
				String sTitleSub = C.TITLE_ENTERED_PLOT_SUB.s().replaceFirst("%s", getName(plot.owner));
				ChatColor sTitleMainColor = ChatColor.valueOf(C.TITLE_ENTERED_PLOT_COLOR.s());
				ChatColor sTitleSubColor = ChatColor.valueOf(C.TITLE_ENTERED_PLOT_SUB_COLOR.s());
				Title title = new Title(sTitleMain, sTitleSub, 10, 20, 10);
				title.setTitleColor(sTitleMainColor);
				title.setSubtitleColor(sTitleSubColor);
				title.setTimingsToTicks();
				title.send(player);
			}
			{
				PlayerEnterPlotEvent callEvent = new PlayerEnterPlotEvent(player, plot);
				Bukkit.getPluginManager().callEvent(callEvent);
			}
			PlayerFunctions.sendMessage(player, plot.settings.getJoinMessage());
		}
	}

	public static void plotExit(Player player, Plot plot) {
		{
			PlayerLeavePlotEvent callEvent = new PlayerLeavePlotEvent(player, plot);
			Bukkit.getPluginManager().callEvent(callEvent);
		}
        player.setGameMode(Bukkit.getDefaultGameMode());
		player.resetPlayerTime();
		player.resetPlayerWeather();
		PlayerFunctions.sendMessage(player, plot.settings.getLeaveMessage());
	}

	@EventHandler(
			priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public static void PlayerMove(PlayerMoveEvent event) {
		try {
			Player player = event.getPlayer();
			Location from = event.getFrom();
			Location to = event.getTo();
			if ((from.getBlockX() != to.getBlockX()) || (from.getBlockZ() != to.getBlockZ())) {
				if (!isPlotWorld(player.getWorld())) {
					return;
				}
				if (enteredPlot(from, to)) {
					Plot plot = getCurrentPlot(event.getTo());
					boolean admin = player.hasPermission("plots.admin");
					if (plot.deny_entry(player) && !admin) {
						event.setCancelled(true);
						return;
					}
					plotEntry(player, plot);
				}
				else
					if (leftPlot(event.getFrom(), event.getTo())) {
						Plot plot = getCurrentPlot(event.getFrom());
						plotExit(player, plot);
					}
			}
		}
		catch (Exception e) {
			// Gotta catch 'em all.
		}
	}

	@EventHandler(
			priority = EventPriority.HIGHEST)
	public static void onChat(AsyncPlayerChatEvent event) {
		World world = event.getPlayer().getWorld();
		if (!isPlotWorld(world)) {
			return;
		}
		PlotWorld plotworld = PlotMain.getWorldSettings(world);
		if (!plotworld.PLOT_CHAT) {
			return;
		}
		if (getCurrentPlot(event.getPlayer().getLocation()) == null) {
			return;
		}
		String message = event.getMessage();
		String format = C.PLOT_CHAT_FORMAT.s();
		String sender = event.getPlayer().getDisplayName();
		Plot plot = getCurrentPlot(event.getPlayer().getLocation());
		PlotId id = plot.id;
		Set<Player> recipients = event.getRecipients();
		recipients.clear();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (getCurrentPlot(p.getLocation()) == plot) {
				recipients.add(p);
			}
		}
		format =
				format.replaceAll("%plot_id%", id.x + ";" + id.y).replaceAll("%sender%", sender).replaceAll("%msg%", message);
		format = ChatColor.translateAlternateColorCodes('&', format);
		event.setFormat(format);
	}

	@EventHandler(
			priority = EventPriority.HIGH)
	public static void BlockDestroy(BlockBreakEvent event) {
		World world = event.getPlayer().getWorld();
		if (!isPlotWorld(world)) {
			return;
		}
		if (event.getPlayer().hasPermission("plots.admin")) {
			return;
		}
		if (isInPlot(event.getBlock().getLocation())) {
			Plot plot = getCurrentPlot(event.getBlock().getLocation());
			if (!plot.hasRights(event.getPlayer())) {
				event.setCancelled(true);
			}
		}
		if (PlayerFunctions.getPlot(event.getBlock().getLocation()) == null) {
			event.setCancelled(true);
		}
	}

	@EventHandler(
			priority = EventPriority.HIGH)
	public static void BlockCreate(BlockPlaceEvent event) {
		World world = event.getPlayer().getWorld();
		if (!isPlotWorld(world)) {
			return;
		}
		if (event.getPlayer().hasPermission("plots.admin")) {
			return;
		}
		if (isInPlot(event.getBlock().getLocation())) {
			Plot plot = getCurrentPlot(event.getBlockPlaced().getLocation());
			if (!plot.hasRights(event.getPlayer())) {
				event.setCancelled(true);
			}
		}
		if (PlayerFunctions.getPlot(event.getBlockPlaced().getLocation()) == null) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public static void onBigBoom(EntityExplodeEvent event) {
		World world = event.getLocation().getWorld();
		if (!isPlotWorld(world)) {
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onPeskyMobsChangeTheWorldLikeWTFEvent( // LOL!
	EntityChangeBlockEvent event) {
		World world = event.getBlock().getWorld();
		if (!isPlotWorld(world)) {
			return;
		}
		Entity e = event.getEntity();
		if (!(e instanceof Player)) {
			if (!(e instanceof org.bukkit.entity.FallingBlock)) {
				event.setCancelled(true);
			}
		}
		else {
			Block b = event.getBlock();
			Player p = (Player) e;
			if (!isInPlot(b.getLocation())) {
				if (!p.hasPermission("plots.admin")) {
					event.setCancelled(true);
				}
			}
			else {
				Plot plot = getCurrentPlot(b.getLocation());
				if (plot == null) {
					if (!p.hasPermission("plots.admin")) {
						event.setCancelled(true);
					}
				}
				else
					if (!plot.hasRights(p)) {
						if (!p.hasPermission("plots.admin")) {
							event.setCancelled(true);
						}
					}
			}
		}
	}

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onEntityBlockForm(final EntityBlockFormEvent event) {
		World world = event.getBlock().getWorld();
		if (!isPlotWorld(world)) {
			return;
		}
		if ((!(event.getEntity() instanceof Player))) {
			event.setCancelled(true);
		}
	}

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onBS(final BlockSpreadEvent e) {
		Block b = e.getBlock();
		if (isPlotWorld(b.getLocation())) {
			if (!isInPlot(b.getLocation())) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onBF(final BlockFormEvent e) {
		Block b = e.getBlock();
		if (isPlotWorld(b.getLocation())) {
			if (!isInPlot(b.getLocation())) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onBD(final BlockDamageEvent e) {
		Block b = e.getBlock();
		if (isPlotWorld(b.getLocation())) {
			if (!isInPlot(b.getLocation())) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onFade(final BlockFadeEvent e) {
		Block b = e.getBlock();
		if (isPlotWorld(b.getLocation())) {
			if (!isInPlot(b.getLocation())) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onChange(final BlockFromToEvent e) {
		Block b = e.getToBlock();
		if (isPlotWorld(b.getLocation())) {
			if (!isInPlot(b.getLocation())) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onGrow(final BlockGrowEvent e) {
		Block b = e.getBlock();
		if (isPlotWorld(b.getLocation())) {
			if (!isInPlot(b.getLocation())) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onBlockPistonExtend(final BlockPistonExtendEvent e) {
		if (isInPlot(e.getBlock().getLocation())) {

			e.getDirection();
			int modifier = e.getBlocks().size();
			Location l = e.getBlock().getLocation();
			{
				if (e.getDirection() == BlockFace.EAST) {
					l = e.getBlock().getLocation().subtract(modifier, 0, 0);
				}
				else
					if (e.getDirection() == BlockFace.NORTH) {
						l = e.getBlock().getLocation().subtract(0, 0, modifier);
					}
					else
						if (e.getDirection() == BlockFace.SOUTH) {
							l = e.getBlock().getLocation().add(0, 0, modifier);
						}
						else
							if (e.getDirection() == BlockFace.WEST) {
								l = e.getBlock().getLocation().add(modifier, 0, 0);
							}

				if (!isInPlot(l)) {
					e.setCancelled(true);
					return;
				}
			}
			for (Block b : e.getBlocks()) {
				if (!isInPlot(b.getLocation())) {
					return;
				}
				{
					if (e.getDirection() == BlockFace.EAST) {
						if (!isInPlot(b.getLocation().subtract(1, 0, 0))) {
							e.setCancelled(true);
						}
					}
					else
						if (e.getDirection() == BlockFace.NORTH) {
							if (!isInPlot(b.getLocation().subtract(0, 0, 1))) {
								e.setCancelled(true);
							}
						}
						else
							if (e.getDirection() == BlockFace.SOUTH) {
								if (!isInPlot(b.getLocation().add(0, 0, 1))) {
									e.setCancelled(true);
								}
							}
							else
								if (e.getDirection() == BlockFace.WEST) {
									if (!isInPlot(b.getLocation().add(1, 0, 0))) {
										e.setCancelled(true);
									}
								}
				}
			}
		}
	}

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onBlockPistonRetract(final BlockPistonRetractEvent e) {
		Block b = e.getRetractLocation().getBlock();
		if (isPlotWorld(b.getLocation()) && (e.getBlock().getType() == Material.PISTON_STICKY_BASE)) {
			if (!isInPlot(b.getLocation())) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onStructureGrow(final StructureGrowEvent e) {
		List<BlockState> blocks = e.getBlocks();
		boolean remove = false;
		for (int i = blocks.size() - 1; i >= 0; i--) {
			if (remove || isPlotWorld(blocks.get(i).getLocation())) {
				remove = true;
				if (!isInPlot(blocks.get(i).getLocation())) {
					e.getBlocks().remove(i);
				}
			}
		}
	}

    public static boolean getFlagValue(String value) {
        return Arrays.asList("true", "on", "enabled", "yes").contains(value.toLowerCase());
    }

	@EventHandler
	public static void onInteract(PlayerInteractEvent event) {
		if (event.getClickedBlock() == null) {
			return;
		}
		World world = event.getPlayer().getWorld();
		if (!isPlotWorld(world)) {
			return;
		}
		if (event.getPlayer().hasPermission("plots.admin")) {
			return;
		}
		if (isInPlot(event.getClickedBlock().getLocation())) {
			Plot plot = getCurrentPlot(event.getClickedBlock().getLocation());

			// They shouldn't be allowed to access other people's chests

			// if (new ArrayList<>(Arrays.asList(new Material[] {
			// Material.STONE_BUTTON, Material.WOOD_BUTTON,
			// Material.LEVER, Material.STONE_PLATE, Material.WOOD_PLATE,
			// Material.CHEST, Material.TRAPPED_CHEST, Material.TRAP_DOOR,
			// Material.WOOD_DOOR, Material.WOODEN_DOOR,
			// Material.DISPENSER, Material.DROPPER
			//
			// })).contains(event.getClickedBlock().getType())) {
			// return;
			// }

            if(PlotMain.booleanFlags.containsKey(event.getClickedBlock().getType())) {
                String flag = PlotMain.booleanFlags.get(event.getClickedBlock().getType());
                if(plot.settings.getFlag(flag) != null && getFlagValue(plot.settings.getFlag(flag).getValue()))
                    return;
            }

			if (!plot.hasRights(event.getPlayer())) {
				event.setCancelled(true);
			}
		}
		if (PlayerFunctions.getPlot(event.getClickedBlock().getLocation()) == null) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public static void MobSpawn(CreatureSpawnEvent event) {
        World world = event.getLocation().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        PlotWorld pW = getPlotWorld(world);
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if (reason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG && pW.SPAWN_EGGS) {
            return;
        } else if (reason == CreatureSpawnEvent.SpawnReason.BREEDING && pW.SPAWN_BREEDING) {
            return;
        } else if (reason == CreatureSpawnEvent.SpawnReason.CUSTOM && pW.SPAWN_CUSTOM) {
            return;
        }
		if (event.getEntity() instanceof Player) {
			return;
		}
		System.out.print(event.getEntityType().getName());
		if (!isInPlot(event.getLocation())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onBlockIgnite(final BlockIgniteEvent e) {
		if (e.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING) {
			e.setCancelled(true);
			return;
		}
		Block b = e.getBlock();
		if (b != null) {
			if (e.getPlayer() != null) {
				Player p = e.getPlayer();
				if (!isInPlot(b.getLocation())) {
					if (!p.hasPermission("plots.admin")) {
						e.setCancelled(true);
					}
				}
				else {
					Plot plot = getCurrentPlot(b.getLocation());
					if (plot == null) {
						if (!p.hasPermission("plots.admin")) {
							e.setCancelled(true);
						}
					}
					else
						if (!plot.hasRights(p)) {
							if (!p.hasPermission("plots.admin")) {
								e.setCancelled(true);
							}
						}
				}
			}
			else {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public static void onTeleport(PlayerTeleportEvent event) {
		Location f = event.getFrom();
		Location t = event.getTo();
		Location q = new Location(t.getWorld(),t.getBlockX(), 64, t.getZ());
		
		
		if (isPlotWorld(q)) {
			if (isInPlot(q)) {
				Plot plot = getCurrentPlot(event.getTo());
				if (plot.deny_entry(event.getPlayer())) {
					PlayerFunctions.sendMessage(event.getPlayer(), C.YOU_BE_DENIED);
					event.setCancelled(true);
				}
				else {
					if (enteredPlot(f, t)) {
						plotEntry(event.getPlayer(), plot);
					}
				}
			}
			else {
				if (leftPlot(f, t)) {
					Plot plot = getCurrentPlot(event.getFrom());
					plotExit(event.getPlayer(), plot);
				}
			}
			if ((event.getTo().getBlockX() >= 29999999) || (event.getTo().getBlockX() <= -29999999)
					|| (event.getTo().getBlockZ() >= 29999999) || (event.getTo().getBlockZ() <= -29999999)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onBucketEmpty(PlayerBucketEmptyEvent e) {
		if (!e.getPlayer().hasPermission("plots.admin")) {
			BlockFace bf = e.getBlockFace();
			Block b = e.getBlockClicked().getLocation().add(bf.getModX(), bf.getModY(), bf.getModZ()).getBlock();
			if (isPlotWorld(b.getLocation())) {
				if (!isInPlot(b.getLocation())) {
					PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PLOT_PERMS);
					e.setCancelled(true);
				}
				else {
					Plot plot = getCurrentPlot(b.getLocation());
					if (plot == null) {
						PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PLOT_PERMS);
						e.setCancelled(true);
					}
					else
						if (!plot.hasRights(e.getPlayer())) {
							PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PLOT_PERMS);
							e.setCancelled(true);
						}
				}
			}
		}
	}

	@EventHandler(
			priority = EventPriority.HIGHEST)
	public static void onInventoryClick(InventoryClickEvent event) {
		if (event.getInventory().getName().equalsIgnoreCase("PlotSquared Commands")) {
			event.setCancelled(true);
		}
	}

    @EventHandler
    public static void onLeave(PlayerQuitEvent event) {
        if(PlotSelection.currentSelection.containsKey(event.getPlayer().getName())) {
            PlotSelection.currentSelection.remove(event.getPlayer().getName());
        }
        if(Setup.setupMap.containsKey(event.getPlayer().getName())) {
            Setup.setupMap.remove(event.getPlayer().getName());
        }
    }

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onBucketFill(PlayerBucketFillEvent e) {
		if (!e.getPlayer().hasPermission("plots.admin")) {
			Block b = e.getBlockClicked();
			if (isPlotWorld(b.getLocation())) {
				if (!isInPlot(b.getLocation())) {
					PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PLOT_PERMS);
					e.setCancelled(true);
				}
				else {
					Plot plot = getCurrentPlot(b.getLocation());
					if (plot == null) {
						PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PLOT_PERMS);
						e.setCancelled(true);
					}
					else
						if (!plot.hasRights(e.getPlayer())) {
							PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PLOT_PERMS);
							e.setCancelled(true);
						}
				}
			}
		}
	}

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onHangingPlace(final HangingPlaceEvent e) {
		Block b = e.getBlock();
		if (isPlotWorld(b.getLocation())) {
			Player p = e.getPlayer();
			if (!isInPlot(b.getLocation())) {
				if (!p.hasPermission("plots.admin")) {
					PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
					e.setCancelled(true);
				}
			}
			else {
				Plot plot = getCurrentPlot(b.getLocation());
				if (plot == null) {
					if (!p.hasPermission("plots.admin")) {
						PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
						e.setCancelled(true);
					}
				}
				else
					if (!plot.hasRights(p)) {
						if (!p.hasPermission("plots.admin")) {
							PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
							e.setCancelled(true);
						}
					}
			}
		}
	}

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onHangingBreakByEntity(final HangingBreakByEntityEvent e) {
		Entity r = e.getRemover();
		if (r instanceof Player) {
			Player p = (Player) r;
			Location l = e.getEntity().getLocation();
			if (isPlotWorld(l)) {
				if (!isInPlot(l)) {
					if (!p.hasPermission("plots.admin")) {
						PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
						e.setCancelled(true);
					}
				}
				else {
					Plot plot = getCurrentPlot(l);
					if (plot == null) {
						if (!p.hasPermission("plots.admin")) {
							PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
							e.setCancelled(true);
						}
					}
					else
						if (!plot.hasRights(p)) {
							if (!p.hasPermission("plots.admin")) {
								PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
								e.setCancelled(true);
							}
						}
				}
			}
		}
	}

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onPlayerInteractEntity(final PlayerInteractEntityEvent e) {
		Location l = e.getRightClicked().getLocation();
		if (isPlotWorld(l)) {
			Player p = e.getPlayer();
			if (!isInPlot(l)) {
				if (!p.hasPermission("plots.admin")) {
					PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
					e.setCancelled(true);
				}
			}
			else {
				Plot plot = getCurrentPlot(l);
				if (plot == null) {
					if (!p.hasPermission("plots.admin")) {
						PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
						e.setCancelled(true);
					}
				}
				else
					if (!plot.hasRights(p)) {
						if (!p.hasPermission("plots.admin")) {
							PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
							e.setCancelled(true);
						}
					}
			}
		}
	}

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onEntityDamageByEntityEvent(final EntityDamageByEntityEvent e) {
		Location l = e.getEntity().getLocation();
		Entity d = e.getDamager();
		Entity a = e.getEntity();
        if (isPlotWorld(l)) {
			if (d instanceof Player) {
				Player p = (Player) d;
                boolean aPlr = a instanceof Player;
                PlotWorld pW = getPlotWorld(l.getWorld());
                if(!aPlr && pW.PVE) {
                    return;
                } else if(aPlr && pW.PVP) {
                    return;
                }
				if (!isInPlot(l)) {
					if (!p.hasPermission("plots.admin")) {
						PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
						e.setCancelled(true);
					}
				}
				else {
					Plot plot = getCurrentPlot(l);
					if (plot == null) {
						if (!p.hasPermission("plots.admin")) {
							PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
							e.setCancelled(true);
						}
					}
					else
						if (!plot.hasRights(p)) {
							if (!p.hasPermission("plots.admin")) {
								PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
								e.setCancelled(true);
							}
						}
				}
			}
		}
	}

	@EventHandler(
			priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onPlayerEggThrow(final PlayerEggThrowEvent e) {
		Location l = e.getEgg().getLocation();
		if (isPlotWorld(l)) {
			Player p = e.getPlayer();
			if (!isInPlot(l)) {
				if (!p.hasPermission("plots.admin")) {
					PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
					e.setHatching(false);
				}
			}
			else {
				Plot plot = getCurrentPlot(l);
				if (plot == null) {
					if (!p.hasPermission("plots.admin")) {
						PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
						e.setHatching(false);
					}
				}
				else
					if (!plot.hasRights(p)) {
						if (!p.hasPermission("plots.admin")) {
							PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
							e.setHatching(false);
						}
					}
			}
		}
	}
}
