package ru.overwrite.rtp.channels;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.overwrite.rtp.locationgenerator.LocationGenerator;

public enum ChannelType {

    DEFAULT {
        @Override
        public Location generateLocation(LocationGenerator locationGenerator, Player player, Settings settings, World world) {
            return locationGenerator.generateRandomLocation(player, settings, world);
        }
    }, NEAR_PLAYER {
        @Override
        public Location generateLocation(LocationGenerator locationGenerator, Player player, Settings settings, World world) {
            return locationGenerator.generateRandomLocationNearPlayer(player, settings, world);
        }
    }, NEAR_REGION {
        @Override
        public Location generateLocation(LocationGenerator locationGenerator, Player player, Settings settings, World world) {
            return locationGenerator.generateRandomLocationNearRandomRegion(player, settings, world);
        }
    };

    public abstract Location generateLocation(LocationGenerator locationGenerator, Player player, Settings settings, World world);

}
