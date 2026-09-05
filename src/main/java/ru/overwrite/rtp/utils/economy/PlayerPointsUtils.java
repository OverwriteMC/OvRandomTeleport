package ru.overwrite.rtp.utils.economy;

import lombok.experimental.UtilityClass;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.entity.Player;

import java.util.UUID;

@UtilityClass
public class PlayerPointsUtils {

    private final PlayerPointsAPI API = PlayerPoints.getInstance().getAPI();

    public boolean withdraw(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        return API.take(uuid, amount);
    }

    public boolean deposit(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        return API.give(uuid, amount);
    }

    public int getBalance(Player player) {
        return API.look(player.getUniqueId());
    }
}
