package ru.overwrite.rtp.channels.settings;

import org.bukkit.configuration.ConfigurationSection;
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

    public static LocationGenOptions create(ConfigurationSection locationGenOptions) {
        if (locationGenOptions == null || locationGenOptions.getKeys(false).isEmpty()) {
            return EMPTY_LGE;
        }

        LocationGenOptions.Shape shape = LocationGenOptions.Shape.valueOf(locationGenOptions.getString("shape", "SQUARE").toUpperCase(Locale.ENGLISH));
        LocationGenOptions.GenFormat genFormat = LocationGenOptions.GenFormat.valueOf(locationGenOptions.getString("gen_format", "RECTANGULAR").toUpperCase(Locale.ENGLISH));

        int minX = locationGenOptions.getInt("min_x", -1000);
        int maxX = locationGenOptions.getInt("max_x", 1000);
        int minZ = locationGenOptions.getInt("min_z", -1000);
        int maxZ = locationGenOptions.getInt("max_z", 1000);
        if (minX > maxX || minZ > maxZ) {
            minX = -1000;
            maxX = 1000;
            minZ = -1000;
            maxZ = 1000;
        }
        int nearRadiusMin = Math.max(locationGenOptions.getInt("min_near_point_distance", 30), 0);
        int nearRadiusMax = Math.max(locationGenOptions.getInt("max_near_point_distance", 60), 0);
        if (nearRadiusMin > nearRadiusMax) {
            nearRadiusMin = 30;
            nearRadiusMax = 60;
        }
        int centerX = locationGenOptions.getInt("center_x", 0);
        int centerZ = locationGenOptions.getInt("center_z", 0);
        int maxLocationAttempts = Math.max(locationGenOptions.getInt("max_location_attempts", 50), 1);
        boolean playerOrientedCenter = locationGenOptions.getBoolean("player_oriented_center", false);
        boolean avoidTrees = VersionUtils.SUB_VERSION > 19 && locationGenOptions.getBoolean("avoid_trees", false);
        int yAdd = !avoidTrees ? locationGenOptions.getInt("y_add", 0) : 0;

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