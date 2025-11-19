package ru.overwrite.rtp.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Action {

    void perform(@NotNull Player player, @Nullable String[] searchList, @Nullable String[] replacementList);

}
