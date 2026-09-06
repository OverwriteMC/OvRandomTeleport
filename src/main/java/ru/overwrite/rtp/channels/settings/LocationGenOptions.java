package ru.overwrite.rtp.channels.settings;

import org.bukkit.configuration.ConfigurationSection;
import ru.overwrite.rtp.RtpManager;
import ru.overwrite.rtp.utils.VersionUtils;

import java.util.Locale;

public record LocationGenOptions(
        Shape shape,
        GenFormat genFormat,
        int minX,
        int maxX,
        int minZ,
        int maxZ,
        int nearRadiusMin,
        int nearRadiusMax,
        int centerX,
        int centerZ,
        int maxLocationAttempts,
        boolean playerOrientedCenter,
        boolean avoidTrees,
        int yAdd
) {

    public enum Shape {
        SQUARE,
        ROUND
    }

    public enum GenFormat {
        RECTANGULAR,
        RADIAL
    }

    private static final LocationGenOptions EMPTY_LGE = new LocationGenOptions(
            Shape.SQUARE,
            GenFormat.RECTANGULAR,
            -1000,
            1000,
            -1000,
            1000,
            30,
            60,
            0,
            0,
            50,
            false,
            false,
            0
    );

    public static LocationGenOptions create(ConfigurationSection locationGenOptions, RtpManager rtpManager) {
        if (locationGenOptions == null || locationGenOptions.getKeys(false).isEmpty()) {
            return EMPTY_LGE;
        }

        LocationGenOptions.Shape shape = LocationGenOptions.Shape.valueOf(locationGenOptions.getString("shape", "SQUARE").toUpperCase(Locale.ENGLISH));
        LocationGenOptions.GenFormat genFormat = LocationGenOptions.GenFormat.valueOf(locationGenOptions.getString("gen_format", "RECTANGULAR").toUpperCase(Locale.ENGLISH));

        int minX = locationGenOptions.getInt("min_x", -1000);
        int maxX = locationGenOptions.getInt("max_x", 1000);
        int minZ = locationGenOptions.getInt("min_z", -1000);
        int maxZ = locationGenOptions.getInt("max_z", 1000);
        boolean defaultsApplied = false;
        boolean invalidRadial = genFormat == GenFormat.RADIAL && (minX < 0 || minZ < 0 || minX >= maxX || minZ >= maxZ);
        if (minX > maxX || minZ > maxZ || minX == maxX && minZ == maxZ && minX == minZ || maxX == Integer.MAX_VALUE || maxZ == Integer.MAX_VALUE || invalidRadial) {
            minX = genFormat == GenFormat.RADIAL ? 0 : -1000;
            maxX = 1000;
            minZ = genFormat == GenFormat.RADIAL ? 0 : -1000;
            maxZ = 1000;
            defaultsApplied = true;
        }
        int nearRadiusMin = locationGenOptions.getInt("min_near_point_distance", 30);
        int nearRadiusMax = locationGenOptions.getInt("max_near_point_distance", 60);
        if (nearRadiusMin < 0 || nearRadiusMax < 0 || nearRadiusMin > nearRadiusMax || nearRadiusMax == Integer.MAX_VALUE) {
            nearRadiusMin = 30;
            nearRadiusMax = 60;
            defaultsApplied = true;
        }
        int centerX = locationGenOptions.getInt("center_x", 0);
        int centerZ = locationGenOptions.getInt("center_z", 0);
        if (centerX > maxX || centerZ > maxZ || centerX < minX || centerZ < minZ) {
            defaultsApplied |= centerX != 0 || centerZ != 0;
            centerX = 0;
            centerZ = 0;
        }
        int maxLocationAttempts = locationGenOptions.getInt("max_location_attempts", 50);
        if (maxLocationAttempts < 1) {
            maxLocationAttempts = 50;
            defaultsApplied = true;
        }
        boolean playerOrientedCenter = locationGenOptions.getBoolean("player_oriented_center", false);
        boolean avoidTrees = VersionUtils.SUB_VERSION > 19 && locationGenOptions.getBoolean("avoid_trees", false);
        int yAdd = !avoidTrees ? locationGenOptions.getInt("y_add", 0) : 0;

        if (defaultsApplied) {
            rtpManager.printDebug("Invalid values in " + locationGenOptions.getCurrentPath() + " replaced with defaults");
        }

        return new LocationGenOptions(
                shape,
                genFormat,
                minX,
                maxX,
                minZ,
                maxZ,
                nearRadiusMin,
                nearRadiusMax,
                centerX,
                centerZ,
                maxLocationAttempts,
                playerOrientedCenter,
                avoidTrees,
                yAdd
        );
    }
}