package ru.overwrite.rtp.channels.settings;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.bukkit.configuration.ConfigurationSection;
import ru.overwrite.rtp.actions.Action;
import ru.overwrite.rtp.actions.ActionService;
import ru.overwrite.rtp.utils.Utils;

import java.util.List;

public record Actions(
        List<Action> preTeleportActions,
        Int2ObjectMap<List<Action>> onCooldownActions,
        List<Action> afterTeleportActions
) {

    private static final Actions EMPTY_ACTIONS = new Actions(
            List.of(),
            Int2ObjectMaps.emptyMap(),
            List.of()
    );

    public static Actions create(ActionService actionService, ConfigurationSection actions) {
        if (actions == null || actions.getKeys(false).isEmpty()) {
            return EMPTY_ACTIONS;
        }

        List<Action> preTeleportActions = actionService.getActionList(actions.getStringList("pre_teleport"));

        Int2ObjectMap<List<Action>> onCooldownActions = new Int2ObjectOpenHashMap<>();
        ConfigurationSection cdSection = actions.getConfigurationSection("on_cooldown");
        if (cdSection != null) {
            for (String key : cdSection.getKeys(false)) {
                if (!Utils.isNumeric(key)) {
                    continue;
                }
                int time = Integer.parseInt(key);
                List<Action> list = actionService.getActionList(cdSection.getStringList(key));
                onCooldownActions.put(time, list);
            }
        }

        List<Action> afterTeleportActions = actionService.getActionList(actions.getStringList("after_teleport"));

        return new Actions(preTeleportActions, onCooldownActions, afterTeleportActions);
    }
}