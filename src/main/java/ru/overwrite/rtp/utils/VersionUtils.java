package ru.overwrite.rtp.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

@UtilityClass
public final class VersionUtils {

    public final int SUB_VERSION;

    static {
        String versionPart = Bukkit.getBukkitVersion().split("-")[0];
        String[] parts = versionPart.split("\\.");
        SUB_VERSION = Integer.parseInt(parts[parts[0].length() == 1 ? 1 : 0]);
    }

    public final int VOID_LEVEL = SUB_VERSION >= 18 ? -64 : 0;

}
