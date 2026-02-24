package ru.overwrite.rtp.utils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.rtp.OvRandomTeleport;
import ru.overwrite.rtp.RtpManager;

public final class PluginMessage implements PluginMessageListener {

    private final OvRandomTeleport plugin;
    private final RtpManager rtpManager;

    public PluginMessage(OvRandomTeleport plugin) {
        this.plugin = plugin;
        this.rtpManager = plugin.getRtpManager();
    }

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput input = ByteStreams.newDataInput(message);
        String subchannel = input.readUTF();
        if (subchannel.equalsIgnoreCase("ovrtp")) {
            rtpManager.printDebug("Received plugin message from another server.");
            String playerName = input.readUTF();
            String teleportData = input.readUTF();
            rtpManager.printDebug("Teleport data received for player " + playerName + " " + teleportData);
            rtpManager.getProxyCalls().put(playerName, teleportData);
        }
    }

    public void sendCrossProxy(Player player, String serverId, String playerName, String teleportData) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF(serverId);
        out.writeUTF("ovrtp");
        out.writeUTF(playerName);
        out.writeUTF(teleportData);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    public void connectToServer(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}

