package ru.overwrite.rtp.utils.regions;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;

@UtilityClass
public final class WGUtils {

    public final BooleanFlag RTP_IGNORE_FLAG = new BooleanFlag("rtp-base-no-teleport");

    public void setupRtpFlag() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        registry.register(RTP_IGNORE_FLAG);
    }

    public ApplicableRegionSet getApplicableRegions(Location location) {
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(location.getWorld()));
        if (regionManager == null || regionManager.getRegions().isEmpty())
            return null;
        return regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
    }

}
