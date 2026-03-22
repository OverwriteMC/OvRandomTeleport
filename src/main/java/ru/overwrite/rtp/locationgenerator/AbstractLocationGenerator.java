package ru.overwrite.rtp.locationgenerator;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import ru.overwrite.rtp.RtpManager;
import ru.overwrite.rtp.channels.Settings;
import ru.overwrite.rtp.channels.settings.Avoidance;
import ru.overwrite.rtp.utils.Utils;
import ru.overwrite.rtp.utils.VersionUtils;
import ru.overwrite.rtp.utils.regions.TownyUtils;
import ru.overwrite.rtp.utils.regions.WGUtils;

public abstract class AbstractLocationGenerator implements LocationGenerator {

    protected final RtpManager rtpManager;

    @Getter
    protected final XoRoShiRo128PlusRandom random;

    protected AbstractLocationGenerator(RtpManager rtpManager) {
        this.rtpManager = rtpManager;
        this.random = new XoRoShiRo128PlusRandom();
    }

    protected int findSafeYPoint(World world, int x, int z, boolean avoidTrees) {
        return world.getEnvironment() != World.Environment.NETHER ? findSafeOverworldYPoint(world, x, z, avoidTrees) : findSafeNetherYPoint(world, x, z);
    }

    private int findSafeOverworldYPoint(World world, int x, int z, boolean avoidTrees) {
        int highest = world.getHighestBlockYAt(x, z);
        if (!avoidTrees) {
            return highest;
        }
        for (int y = highest; y > VersionUtils.VOID_LEVEL; y--) {
            Block block = world.getBlockAt(x, y, z);
            Material type = block.getType();

            if (!block.isSolid() || Tag.LEAVES.isTagged(type) || Tag.LOGS.isTagged(type)) {
                continue;
            }

            boolean hasSolidAbove = false;
            for (int yy = y + 1; yy <= highest; yy++) {
                Block above = world.getBlockAt(x, yy, z);
                Material aboveType = above.getType();
                if (above.isSolid() && !Tag.LEAVES.isTagged(aboveType) && !Tag.LOGS.isTagged(aboveType)) {
                    hasSolidAbove = true;
                    break;
                }
            }
            if (hasSolidAbove) {
                continue;
            }
            if (!isInsideBlocks(world, x, y, z, false)) {
                return y;
            }
        }
        return -1;
    }

    private int findSafeNetherYPoint(World world, int x, int z) {
        for (int y = 32; y < 90; y++) {
            if (world.getBlockAt(x, y, z).isSolid() && !isInsideBlocks(world, x, y, z, false)) {
                return y;
            }
        }
        return -1;
    }

    protected boolean isInsideRadiusSquare(int x, int z, int minX, int minZ, int maxX, int maxZ, int centerX, int centerZ) {
        int realMinX = centerX + minX;
        int realMinZ = centerZ + minZ;
        int realMaxX = centerX + maxX;
        int realMaxZ = centerZ + maxZ;

        return (x >= realMinX && x <= realMaxX && z >= realMinZ && z <= realMaxZ);
    }

    protected boolean isInsideRadiusCircle(int x, int z, int minX, int minZ, int maxX, int maxZ, int centerX, int centerZ) {
        int deltaX = x - centerX;
        int deltaZ = z - centerZ;

        double maxDistanceRatioX = (double) deltaX / maxX;
        double maxDistanceRatioZ = (double) deltaZ / maxZ;
        double maxDistance = maxDistanceRatioX * maxDistanceRatioX + maxDistanceRatioZ * maxDistanceRatioZ;

        double minDistanceRatioX = (double) deltaX / minX;
        double minDistanceRatioZ = (double) deltaZ / minZ;
        double minDistance = minDistanceRatioX * minDistanceRatioX + minDistanceRatioZ * minDistanceRatioZ;

        return maxDistance <= 1 && minDistance >= 2;
    }

    protected boolean isLocationRestricted(Location location, Avoidance avoidance) {
        Block block = location.getBlock();
        if (block.getWorld().getEnvironment() != World.Environment.NETHER &&
                isInsideBlocks(block.getWorld(), block.getX(), block.getY(), block.getZ(), true)) {
            rtpManager.printDebug(() -> "Location " + Utils.locationToString(location) + " is inside blocks.");
            return true;
        }
        if (isDisallowedBlock(block, avoidance)) {
            rtpManager.printDebug(() -> "Location " + Utils.locationToString(location) + " contains a disallowed block.");
            return true;
        }
        if (isDisallowedBiome(block, avoidance)) {
            rtpManager.printDebug(() -> "Location " + Utils.locationToString(location) + " is in a disallowed biome.");
            return true;
        }
        if (isInsideRegion(location, avoidance)) {
            rtpManager.printDebug(() -> "Location " + Utils.locationToString(location) + " is inside a disallowed region.");
            return true;
        }
        if (isInsideTown(location, avoidance)) {
            rtpManager.printDebug(() -> "Location " + Utils.locationToString(location) + " is inside a disallowed town.");
            return true;
        }
        return false;
    }

    protected boolean isOutsideWorldBorder(World world, Location location) {
        if (world.getWorldBorder().isInside(location)) {
            return false;
        }
        rtpManager.printDebug(() -> "Location " + Utils.locationToString(location) + " is outside the world border.");
        return true;
    }

    private boolean isInsideBlocks(World world, int x, int y, int z, boolean onlyCheckOneBlockUp) {
        if (!world.getBlockAt(x, y + 2, z).isEmpty()) {
            return true;
        }
        return !onlyCheckOneBlockUp && !world.getBlockAt(x, y + 1, z).isEmpty();
    }

    private boolean isDisallowedBlock(Block block, Avoidance avoidance) {
        if (avoidance.avoidBlocks().isEmpty()) {
            return false;
        }
        return avoidance.avoidBlocksBlacklist() == avoidance.avoidBlocks().contains(block.getType());
    }

    private boolean isDisallowedBiome(Block block, Avoidance avoidance) {
        if (avoidance.avoidBiomes().isEmpty()) {
            return false;
        }
        return avoidance.avoidBiomesBlacklist() == avoidance.avoidBiomes().contains(block.getBiome());
    }

    private boolean isInsideRegion(Location loc, Avoidance avoidance) {
        if (!avoidance.avoidRegions()) {
            return false;
        }
        ApplicableRegionSet regionSet = WGUtils.getApplicableRegions(loc);
        return regionSet != null && !regionSet.getRegions().isEmpty();
    }

    private boolean isInsideTown(Location loc, Avoidance avoidance) {
        return avoidance.avoidTowns() && TownyUtils.getTownByLocation(loc) != null;
    }

    protected Location finalizeLocation(Player player, Settings settings, World world, int x, int z, boolean avoidTrees) {
        Location location = new Location(world, x + 0.5D, 0, z + 0.5D);

        if (isOutsideWorldBorder(world, location)) {
            return null;
        }

        int y = findSafeYPoint(world, x, z, avoidTrees);
        if (y < 0) {
            return null;
        }

        location.setY(y);
        if (isLocationRestricted(location, settings.avoidance())) {
            return null;
        }

        Location playerLocation = player.getLocation();
        location.setYaw(playerLocation.getYaw());
        location.setPitch(playerLocation.getPitch());
        location.setY(y + 1D);
        return location;
    }
}