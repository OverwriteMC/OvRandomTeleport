package ru.overwrite.rtp.color.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.Nullable;
import ru.overwrite.rtp.color.Colorizer;

public class MiniMessageColorizer implements Colorizer {

    @Override
    public String colorize(@Nullable String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        Component component = MiniMessage.miniMessage().deserialize(message);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
}
