package com.intellectualcrafters.plot.generator;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.Configuration;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.PlotBlock;
import com.intellectualcrafters.plot.PlotHelper;
import com.intellectualcrafters.plot.PlotId;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.PlotManager;
import com.intellectualcrafters.plot.PlotWorld;

public class DefaultPlotManager extends PlotManager {

    /*
     * Default implementation of getting a plot at a given location
     * 
     * For a simplified explanation of the math involved:
     *  - Get the current coords
     *  - shift these numbers down to something relatable for a single plot 
     *  (similar to reducing trigonometric functions down to the first quadrant)
     *  - e.g. If the plot size is 20 blocks, and we are at x=25, it's equivalent to x=5 for that specific plot
     *  
     *  From this, and knowing how thick the road is, we can say whether x=5 is road, or plot.
     *  The number of shifts done, is also counted, and this number gives us the PlotId
     *  
     * 
     */
    @Override
    public PlotId getPlotIdAbs(PlotWorld plotworld, Location loc) {
        DefaultPlotWorld dpw = ((DefaultPlotWorld) plotworld);

        // get x,z loc
        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        // get plot size
        int size = dpw.PLOT_WIDTH + dpw.ROAD_WIDTH;
        
        // get size of path on bottom part, and top part of plot
        // (As 0,0 is in the middle of a road, not the very start)
        int pathWidthLower;
        if ((dpw.ROAD_WIDTH % 2) == 0) {
            pathWidthLower = (int) (Math.floor(dpw.ROAD_WIDTH / 2) - 1);
        } else {
            pathWidthLower = (int) Math.floor(dpw.ROAD_WIDTH / 2);
        }

        // calulating how many shifts need to be done
        int dx = x / size;
        int dz = z / size;
        if (x < 0) {
            dx--;
            x += ((-dx) * size);
        }
        if (z < 0) {
            dz--;
            z += ((-dz) * size);
        }

        // reducing to first plot
        int rx = (x) % size;
        int rz = (z) % size;

        // checking if road (return null if so)
        int end = pathWidthLower + dpw.PLOT_WIDTH;
        boolean northSouth = (rz <= pathWidthLower) || (rz > end);
        boolean eastWest = (rx <= pathWidthLower) || (rx > end);
        if (northSouth || eastWest) {
            return null;
        }
        
        // returning the plot id (based on the number of shifts required)
        return new PlotId(dx + 1, dz + 1);
    }

    /*
     *  Check if a location is inside a specific plot(non-Javadoc)
     *   - For this implementation, we don't need to do anything fancier than referring to getPlotIdAbs(...)
     */
    @Override
    public boolean isInPlotAbs(PlotWorld plotworld, Location loc, Plot plot) {
        PlotId result = getPlotIdAbs(plotworld, loc);
        if (result==null) {
            return false;
        }
        return result==plot.id;
    }

    /*
     * Get the bottom plot loc
     * (some basic math)
     */
    @Override
    public Location getPlotBottomLocAbs(PlotWorld plotworld, Plot plot) {
        DefaultPlotWorld dpw = ((DefaultPlotWorld) plotworld);
        
        int px = plot.id.x;
        int pz = plot.id.y;

        int x = (px * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - dpw.PLOT_WIDTH - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        int z = (pz * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - dpw.PLOT_WIDTH - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;

        return new Location(Bukkit.getWorld(plot.world), x, 1, z);
    }
    
    /*
     * Get the top plot loc
     * (some basic math)
     */
    @Override
    public Location getPlotTopLocAbs(PlotWorld plotworld, Plot plot) {
        DefaultPlotWorld dpw = ((DefaultPlotWorld) plotworld);
        
        int px = plot.id.x;
        int pz = plot.id.y;

        int x = (px * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        int z = (pz * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;

        return new Location(Bukkit.getWorld(plot.world), x, 256, z);
    }

    /*
     * Clearing the plot needs to only consider removing the blocks
     *  - This implementation has used the SetCuboid function, as it is fast, and uses NMS code
     *  - It also makes use of the fact that deleting chunks is a lot faster than block updates
     * 
     * This code is very messy, but you don't need to do something quite as complex unless you happen to have 512x512 sized plots
     * 
     */
    @Override
    public boolean clearPlot(Player player, Plot plot, boolean mega) {
        World world = player.getWorld();
        DefaultPlotWorld dpw = ((DefaultPlotWorld) PlotMain.getWorldSettings(world));
        
        final Location pos1 = PlotHelper.getPlotBottomLoc(world, plot.id).add(1, 0, 1);
        final Location pos2 = PlotHelper.getPlotTopLoc(world, plot.id);

        PlotBlock[] plotfloor = dpw.TOP_BLOCK;
        PlotBlock[] filling = dpw.TOP_BLOCK;
        
        if ((pos2.getBlockX() - pos1.getBlockX()) < 16) {
            PlotHelper.setSimpleCuboid(world, new Location(world, pos1.getBlockX(), 0, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, 1, pos2.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, pos1.getBlockX(), dpw.PLOT_HEIGHT + 1, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, world.getMaxHeight() + 1, pos2.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, pos1.getBlockX(), 1, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, dpw.PLOT_HEIGHT, pos2.getBlockZ() + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, pos1.getBlockX(), dpw.PLOT_HEIGHT, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, pos2.getBlockZ() + 1), plotfloor);
            return true;
        }
        int startX = (pos1.getBlockX() / 16) * 16;
        int startZ = (pos1.getBlockZ() / 16) * 16;
        int chunkX = 16 + pos2.getBlockX();
        int chunkZ = 16 + pos2.getBlockZ();
        Location l1 = PlotHelper.getPlotBottomLoc(world, plot.id);
        Location l2 = PlotHelper.getPlotTopLoc(world, plot.id);
        int plotMinX = l1.getBlockX() + 1;
        int plotMinZ = l1.getBlockZ() + 1;
        int plotMaxX = l2.getBlockX();
        int plotMaxZ = l2.getBlockZ();
        Location min = null;
        Location max = null;
        for (int i = startX; i < chunkX; i += 16) {
            for (int j = startZ; j < chunkZ; j += 16) {
                Plot plot1 = PlotHelper.getCurrentPlot(new Location(world, i, 0, j));
                if ((plot1 != null) && (plot1.getId() != plot.getId()) && plot1.hasOwner()) {
                    break;
                }
                Plot plot2 = PlotHelper.getCurrentPlot(new Location(world, i + 15, 0, j));
                if ((plot2 != null) && (plot2.getId() != plot.getId()) && plot2.hasOwner()) {
                    break;
                }
                Plot plot3 = PlotHelper.getCurrentPlot(new Location(world, i + 15, 0, j + 15));
                if ((plot3 != null) && (plot3.getId() != plot.getId()) && plot3.hasOwner()) {
                    break;
                }
                Plot plot4 = PlotHelper.getCurrentPlot(new Location(world, i, 0, j + 15));
                if ((plot4 != null) && (plot4.getId() != plot.getId()) && plot4.hasOwner()) {
                    break;
                }
                Plot plot5 = PlotHelper.getCurrentPlot(new Location(world, i + 15, 0, j + 15));
                if ((plot5 != null) && (plot5.getId() != plot.getId()) && plot5.hasOwner()) {
                    break;
                }
                if (min == null) {
                    min = new Location(world, Math.max(i - 1, plotMinX), 0, Math.max(j - 1, plotMinZ));
                    max = new Location(world, Math.min(i + 16, plotMaxX), 0, Math.min(j + 16, plotMaxZ));
                } else if ((max.getBlockZ() < (j + 15)) || (max.getBlockX() < (i + 15))) {
                    max = new Location(world, Math.min(i + 16, plotMaxX), 0, Math.min(j + 16, plotMaxZ));
                }
                world.regenerateChunk(i / 16, j / 16);
            }
        }

        if (min == null) {
            PlotHelper.setSimpleCuboid(world, new Location(world, pos1.getBlockX(), 0, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, 1, pos2.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, pos1.getBlockX(), dpw.PLOT_HEIGHT + 1, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, world.getMaxHeight() + 1, pos2.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, pos1.getBlockX(), 1, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, dpw.PLOT_HEIGHT, pos2.getBlockZ() + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, pos1.getBlockX(), dpw.PLOT_HEIGHT, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, pos2.getBlockZ() + 1), plotfloor);
        } else {
            
            if (min.getBlockX() < plotMinX) {
                min.setX(plotMinX);
            }
            if (min.getBlockZ() < plotMinZ) {
                min.setZ(plotMinZ);
            }
            if (max.getBlockX() > plotMaxX) {
                max.setX(plotMaxX);
            }
            if (max.getBlockZ() > plotMaxZ) {
                max.setZ(plotMaxZ);
            }
            
            PlotHelper.setSimpleCuboid(world, new Location(world, plotMinX, 0, plotMinZ), new Location(world, min.getBlockX() + 1, 1, min.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT + 1, plotMinZ), new Location(world, min.getBlockX() + 1, world.getMaxHeight() + 1, min.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, plotMinX, 1, plotMinZ), new Location(world, min.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, min.getBlockZ() + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT, plotMinZ), new Location(world, min.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, min.getBlockZ() + 1), plotfloor);

            PlotHelper.setSimpleCuboid(world, new Location(world, min.getBlockX(), 0, plotMinZ), new Location(world, max.getBlockX() + 1, 1, min.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, min.getBlockX(), dpw.PLOT_HEIGHT + 1, plotMinZ), new Location(world, max.getBlockX() + 1, world.getMaxHeight() + 1, min.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, min.getBlockX(), 1, plotMinZ), new Location(world, max.getBlockX() + 1, dpw.PLOT_HEIGHT, min.getBlockZ() + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, min.getBlockX(), dpw.PLOT_HEIGHT, plotMinZ), new Location(world, max.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, min.getBlockZ() + 1), plotfloor);

            PlotHelper.setSimpleCuboid(world, new Location(world, max.getBlockX(), 0, plotMinZ), new Location(world, plotMaxX + 1, 1, min.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, max.getBlockX(), dpw.PLOT_HEIGHT + 1, plotMinZ), new Location(world, plotMaxX + 1, world.getMaxHeight() + 1, min.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, max.getBlockX(), 1, plotMinZ), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT, min.getBlockZ() + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, max.getBlockX(), dpw.PLOT_HEIGHT, plotMinZ), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT + 1, min.getBlockZ() + 1), plotfloor);

            PlotHelper.setSimpleCuboid(world, new Location(world, plotMinX, 0, min.getBlockZ()), new Location(world, min.getBlockX() + 1, 1, max.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT + 1, min.getBlockZ()), new Location(world, min.getBlockX() + 1, world.getMaxHeight() + 1, max.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, plotMinX, 1, min.getBlockZ()), new Location(world, min.getBlockX() + 1, dpw.PLOT_HEIGHT, max.getBlockZ() + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT, min.getBlockZ()), new Location(world, min.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, max.getBlockZ() + 1), plotfloor);

            PlotHelper.setSimpleCuboid(world, new Location(world, plotMinX, 0, max.getBlockZ()), new Location(world, min.getBlockX() + 1, 1, plotMaxZ + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT + 1, max.getBlockZ()), new Location(world, min.getBlockX() + 1, world.getMaxHeight() + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, plotMinX, 1, max.getBlockZ()), new Location(world, min.getBlockX() + 1, dpw.PLOT_HEIGHT, plotMaxZ + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT, max.getBlockZ()), new Location(world, min.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, plotMaxZ + 1), plotfloor);

            PlotHelper.setSimpleCuboid(world, new Location(world, min.getBlockX(), 0, max.getBlockZ()), new Location(world, max.getBlockX() + 1, 1, plotMaxZ + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, min.getBlockX(), dpw.PLOT_HEIGHT + 1, max.getBlockZ()), new Location(world, max.getBlockX() + 1, world.getMaxHeight() + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, min.getBlockX(), 1, max.getBlockZ()), new Location(world, max.getBlockX() + 1, dpw.PLOT_HEIGHT, plotMaxZ + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, min.getBlockX(), dpw.PLOT_HEIGHT, max.getBlockZ()), new Location(world, max.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, plotMaxZ + 1), plotfloor);

            PlotHelper.setSimpleCuboid(world, new Location(world, max.getBlockX(), 0, min.getBlockZ()), new Location(world, plotMaxX + 1, 1, max.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, max.getBlockX(), dpw.PLOT_HEIGHT + 1, max.getBlockZ()), new Location(world, plotMaxX + 1, world.getMaxHeight() + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, max.getBlockX(), 1, min.getBlockZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT, max.getBlockZ() + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, max.getBlockX(), dpw.PLOT_HEIGHT, min.getBlockZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT + 1, max.getBlockZ() + 1), plotfloor);

            PlotHelper.setSimpleCuboid(world, new Location(world, max.getBlockX(), 0, max.getBlockZ()), new Location(world, plotMaxX + 1, 1, plotMaxZ + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, max.getBlockX(), dpw.PLOT_HEIGHT + 1, max.getBlockZ()), new Location(world, plotMaxX + 1, world.getMaxHeight() + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, max.getBlockX(), 1, max.getBlockZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT, plotMaxZ + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, max.getBlockX(), dpw.PLOT_HEIGHT, max.getBlockZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT + 1, plotMaxZ + 1), plotfloor);
        }
        return true;
    }

    /*
     * Remove sign for a plot 
     */
    @Override
    public boolean clearSign(Player player, Plot plot, boolean mega) {
        World world = player.getWorld();
        DefaultPlotWorld plotworld = (DefaultPlotWorld) PlotMain.getWorldSettings(world);
        Location pl = new Location(world, PlotHelper.getPlotBottomLoc(world, plot.id).getBlockX(), plotworld.ROAD_HEIGHT + 1, PlotHelper.getPlotBottomLoc(world, plot.id).getBlockZ());
        Block bs = pl.add(0, 0, -1).getBlock();
        bs.setType(Material.AIR);
        return true;
    }

    /*
     * Remove any entities, and teleport players inside a plot being cleared
     */
    @Override
    public boolean clearEntities(Player player, Plot plot, boolean mega) {
        World world = Bukkit.getWorld(plot.world);
        Location pos1 = PlotHelper.getPlotBottomLoc(world, plot.id).add(1, 0, 1);
        Location pos2 = PlotHelper.getPlotTopLoc(world, plot.id);
        for (int i = (pos1.getBlockX() / 16) * 16; i < (16 + ((pos2.getBlockX() / 16) * 16)); i += 16) {
            for (int j = (pos1.getBlockZ() / 16) * 16; j < (16 + ((pos2.getBlockZ() / 16) * 16)); j += 16) {
                Chunk chunk = world.getChunkAt(i, j);
                for (Entity entity : chunk.getEntities()) {
                    Location eloc = entity.getLocation();
                    if (eloc.getBlockX() >= pos1.getBlockX() && eloc.getBlockZ() >= pos1.getBlockZ() && eloc.getBlockX() <= pos2.getBlockX() && eloc.getBlockZ() <= pos2.getBlockZ()) {
                        if (entity instanceof Player) {
                            PlotMain.teleportPlayer((Player) entity, entity.getLocation(), plot);
                        } else {
                            entity.remove();
                        }
                    }
                }
            }
        }
        return true;
    }
    
    @Override
    public boolean setWall(Player player, Plot plot, Block block, PlotBlock newblock) {
        
        // CURRENTLY NOT IMPLEMENTED
        
        return false;
    }

    /*
     * Set a plot biome
     */
    @Override
    public boolean setBiome(Player player, Plot plot, Biome biome) {
        
        World world = player.getWorld();
        
        int bottomX = PlotHelper.getPlotBottomLoc(world, plot.id).getBlockX() - 1;
        int topX = PlotHelper.getPlotTopLoc(world, plot.id).getBlockX() + 1;
        int bottomZ = PlotHelper.getPlotBottomLoc(world, plot.id).getBlockZ() - 1;
        int topZ = PlotHelper.getPlotTopLoc(world, plot.id).getBlockZ() + 1;

        for (int x = bottomX; x <= topX; x++) {
            for (int z = bottomZ; z <= topZ; z++) {
                world.getBlockAt(x, 0, z).setBiome(biome);
            }
        }

        plot.settings.setBiome(biome);
        PlotMain.updatePlot(plot);
        PlotHelper.refreshPlotChunks(world, plot);
        
        return true;
    }

    /*
     * PLOT MERGING
     */
    
    @Override
    public boolean createRoadEast(PlotWorld plotworld, Plot plot) {
        DefaultPlotWorld dpw = (DefaultPlotWorld) plotworld;
        World w = Bukkit.getWorld(plot.world);
        
        Location pos1 = getPlotBottomLocAbs(plotworld, plot);
        Location pos2 = getPlotTopLocAbs(plotworld, plot);

        int sx = pos2.getBlockX();
        int ex = (sx + dpw.ROAD_WIDTH);
        int sz = pos1.getBlockZ() - 1;
        int ez = pos2.getBlockZ() + 2;
        
        PlotHelper.setSimpleCuboid(w, new Location(w, sx, Math.min(dpw.WALL_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz + 1), new Location(w, ex + 1, 257 + 1, ez), new PlotBlock((short) 0, (byte) 0));

        PlotHelper.setSimpleCuboid(w, new Location(w, sx, 1, sz + 1), new Location(w, sx + 1, dpw.WALL_HEIGHT + 1, ez), dpw.WALL_FILLING);
        PlotHelper.setSimpleCuboid(w, new Location(w, sx, dpw.WALL_HEIGHT + 1, sz + 1), new Location(w, sx + 1, dpw.WALL_HEIGHT + 2, ez), dpw.WALL_BLOCK);

        PlotHelper.setSimpleCuboid(w, new Location(w, ex, 1, sz + 1), new Location(w, ex + 1, dpw.WALL_HEIGHT + 1, ez), dpw.WALL_FILLING);
        PlotHelper.setSimpleCuboid(w, new Location(w, ex, dpw.WALL_HEIGHT + 1, sz + 1), new Location(w, ex + 1, dpw.WALL_HEIGHT + 2, ez), dpw.WALL_BLOCK);

        PlotHelper.setSimpleCuboid(w, new Location(w, sx + 1, 1, sz + 1), new Location(w, ex, dpw.ROAD_HEIGHT + 1, ez), dpw.ROAD_BLOCK);
        
        return true;
    }

    @Override
    public boolean createRoadSouth(PlotWorld plotworld, Plot plot) {
        DefaultPlotWorld dpw = (DefaultPlotWorld) plotworld;
        World w = Bukkit.getWorld(plot.world);
        
        Location pos1 = getPlotBottomLocAbs(plotworld, plot);
        Location pos2 = getPlotTopLocAbs(plotworld, plot);

        int sz = pos2.getBlockZ();
        int ez = (sz + dpw.ROAD_WIDTH);
        int sx = pos1.getBlockX() - 1;
        int ex = pos2.getBlockX() + 2;
        
        PlotHelper.setSimpleCuboid(w, new Location(w, sx, Math.min(dpw.WALL_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz + 1), new Location(w, ex + 1, 257, ez), new PlotBlock((short) 0, (byte) 0));

        PlotHelper.setSimpleCuboid(w, new Location(w, sx + 1, 1, sz), new Location(w, ex, dpw.WALL_HEIGHT + 1, sz + 1), dpw.WALL_FILLING);
        PlotHelper.setSimpleCuboid(w, new Location(w, sx + 1, dpw.WALL_HEIGHT + 1, sz), new Location(w, ex, dpw.WALL_HEIGHT + 2, sz + 1), dpw.WALL_BLOCK);

        PlotHelper.setSimpleCuboid(w, new Location(w, sx + 1, 1, ez), new Location(w, ex, dpw.WALL_HEIGHT + 1, ez + 1), dpw.WALL_FILLING);
        PlotHelper.setSimpleCuboid(w, new Location(w, sx + 1, dpw.WALL_HEIGHT + 1, ez), new Location(w, ex, dpw.WALL_HEIGHT + 2, ez + 1), dpw.WALL_BLOCK);

        PlotHelper.setSimpleCuboid(w, new Location(w, sx + 1, 1, sz + 1), new Location(w, ex, dpw.ROAD_HEIGHT + 1, ez), dpw.ROAD_BLOCK);
        
        return true;
    }

    @Override
    public boolean createRoadSouthEast(PlotWorld plotworld, Plot plot) {
        DefaultPlotWorld dpw = (DefaultPlotWorld) plotworld;
        World w = Bukkit.getWorld(plot.world);
        
        Location pos2 = getPlotTopLocAbs(plotworld, plot);

        int sx = pos2.getBlockX() + 1;
        int ex = (sx + dpw.ROAD_WIDTH) - 1;
        int sz = pos2.getBlockZ() + 1;
        int ez = (sz + dpw.ROAD_WIDTH) - 1;
        
        PlotHelper.setSimpleCuboid(w, new Location(w, sx, dpw.ROAD_HEIGHT + 1, sz + 1), new Location(w, ex + 1, 257, ez), new PlotBlock((short) 0, (byte) 0));
        PlotHelper.setSimpleCuboid(w, new Location(w, sx + 1, 1, sz + 1), new Location(w, ex, dpw.ROAD_HEIGHT + 1, ez), dpw.ROAD_BLOCK);
        
        return true;
    }

    @Override
    public boolean removeRoadEast(PlotWorld plotworld, Plot plot) {
        DefaultPlotWorld dpw = (DefaultPlotWorld) plotworld;
        World w = Bukkit.getWorld(plot.world);
        
        Location pos1 = getPlotBottomLocAbs(plotworld, plot);
        Location pos2 = getPlotTopLocAbs(plotworld, plot);

        int sx = pos2.getBlockX();
        int ex = (sx + dpw.ROAD_WIDTH);
        int sz = pos1.getBlockZ() - 1;
        int ez = pos2.getBlockZ() + 2;
        
        PlotHelper.setSimpleCuboid(w, new Location(w, sx, Math.min(dpw.PLOT_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz), new Location(w, ex + 1, 257, ez + 1), new PlotBlock((short) 0, (byte) 0));
        PlotHelper.setCuboid(w, new Location(w, sx, 1, sz), new Location(w, ex + 1, dpw.PLOT_HEIGHT, ez + 1), dpw.MAIN_BLOCK);
        PlotHelper.setCuboid(w, new Location(w, sx, dpw.PLOT_HEIGHT, sz), new Location(w, ex + 1, dpw.PLOT_HEIGHT+1, ez + 1), dpw.TOP_BLOCK);
        
        return true;
    }

    @Override
    public boolean removeRoadSouth(PlotWorld plotworld, Plot plot) {
        DefaultPlotWorld dpw = (DefaultPlotWorld) plotworld;
        World w = Bukkit.getWorld(plot.world);
        
        Location pos1 = getPlotBottomLocAbs(plotworld, plot);
        Location pos2 = getPlotTopLocAbs(plotworld, plot);

        int sz = pos2.getBlockZ();
        int ez = (sz + dpw.ROAD_WIDTH);
        int sx = pos1.getBlockX() - 1;
        int ex = pos2.getBlockX() + 2;
        
        PlotHelper.setSimpleCuboid(w, new Location(w, sx, Math.min(dpw.PLOT_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz), new Location(w, ex + 1, 257, ez + 1), new PlotBlock((short) 0, (byte) 0));
        PlotHelper.setCuboid(w, new Location(w, sx, 1, sz), new Location(w, ex + 1, dpw.PLOT_HEIGHT, ez + 1), dpw.MAIN_BLOCK);
        PlotHelper.setCuboid(w, new Location(w, sx, dpw.PLOT_HEIGHT, sz), new Location(w, ex + 1, dpw.PLOT_HEIGHT+1, ez + 1), dpw.TOP_BLOCK);
        
        return true;
    }

    @Override
    public boolean removeRoadSouthEast(PlotWorld plotworld, Plot plot) {
        DefaultPlotWorld dpw = (DefaultPlotWorld) plotworld;
        World world = Bukkit.getWorld(plot.world);
        
        Location loc = getPlotTopLocAbs(dpw, plot);

        int sx = loc.getBlockX() + 1;
        int ex = (sx + dpw.ROAD_WIDTH) - 1;
        int sz = loc.getBlockZ() + 1;
        int ez = (sz + dpw.ROAD_WIDTH) - 1;

        PlotHelper.setSimpleCuboid(world, new Location(world, sx, dpw.ROAD_HEIGHT + 1, sz), new Location(world, ex + 1, 257, ez + 1), new PlotBlock((short) 0, (byte) 0));

        PlotHelper.setCuboid(world, new Location(world, sx + 1, 1, sz + 1), new Location(world, ex, dpw.ROAD_HEIGHT, ez), dpw.MAIN_BLOCK);
        PlotHelper.setCuboid(world, new Location(world, sx + 1, dpw.ROAD_HEIGHT, sz + 1), new Location(world, ex, dpw.ROAD_HEIGHT + 1, ez), dpw.TOP_BLOCK);
        return true;
    }

    /*
     * Finishing off plot merging by adding in the walls surrounding the plot
     * (OPTIONAL)(UNFINISHED)
     */
    @Override
    public boolean finishPlotMerge(World world, PlotWorld plotworld, ArrayList<PlotId> plotIds) {
        DefaultPlotWorld dpw = (DefaultPlotWorld) plotworld;
        
        PlotId pos1 = plotIds.get(0);
        PlotId pos2 = plotIds.get(plotIds.size() - 1);
        
        PlotBlock block = dpw.WALL_BLOCK;
        
        Location megaPlotBot = PlotHelper.getPlotBottomLoc(world, pos1);
        Location megaPlotTop = PlotHelper.getPlotTopLoc(world, pos2).add(1, 0, 1);
        for (int x = megaPlotBot.getBlockX(); x <= megaPlotTop.getBlockX(); x++) {
            for (int z = megaPlotBot.getBlockZ(); z <= megaPlotTop.getBlockZ(); z++) {
                if ((z == megaPlotBot.getBlockZ()) || (z == megaPlotTop.getBlockZ()) || (x == megaPlotBot.getBlockX()) || (x == megaPlotTop.getBlockX())) {
                    world.getBlockAt(x, dpw.WALL_HEIGHT + 1, z).setTypeIdAndData(block.id, block.data, false);
                }
            }
        }
        return true;
    }
    

}
