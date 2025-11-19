package ru.overwrite.rtp.actions;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.overwrite.rtp.OvRandomTeleport;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ActionService {

    private static final Pattern ACTION_PATTERN = Pattern.compile("\\[(\\S+)] ?(.*)");

    private final OvRandomTeleport plugin;
    private final Map<String, ActionType> types;

    public ActionService(OvRandomTeleport plugin) {
        this.plugin = plugin;
        this.types = new HashMap<>();
    }

    public void register(@NotNull ActionType type) {
        if (types.put(type.key().toString(), type) != null) {
            plugin.getSLF4JLogger().warn("Type '{}' was overridden with '{}'", type.key(), type.getClass().getName());
        }
        types.putIfAbsent(type.key().value(), type);
    }

    @NotNull
    public List<Action> getActionList(List<String> actionStrings) {
        if (actionStrings.isEmpty()) {
            return List.of();
        }
        ImmutableList.Builder<Action> builder = ImmutableList.builder();
        for (String actionStr : actionStrings) {
            try {
                builder.add(Objects.requireNonNull(resolveAction(actionStr), "Type doesn't exist"));
            } catch (Exception ex) {
                plugin.getSLF4JLogger().warn("Couldn't create action for string '{}'", actionStr, ex);
            }
        }
        return builder.build();
    }

    public @Nullable ActionType getType(@NotNull String typeStr) {
        return types.get(typeStr.toLowerCase(Locale.ENGLISH));
    }

    public @Nullable Action resolveAction(@NotNull String actionStr) {
        Matcher matcher = ACTION_PATTERN.matcher(actionStr);
        if (!matcher.matches()) {
            return null;
        }
        ActionType type = getType(matcher.group(1));
        if (type == null) {
            return null;
        }
        return type.instance(matcher.group(2).trim(), plugin);
    }
}
