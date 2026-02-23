package ru.overwrite.rtp;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.projectiles.ProjectileSource;
import ru.overwrite.rtp.channels.Channel;
import ru.overwrite.rtp.channels.Specifications;
import ru.overwrite.rtp.channels.settings.Restrictions;
import ru.overwrite.rtp.utils.Utils;
import ru.overwrite.rtp.utils.VersionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class RtpListener implements Listener {

    private final RtpManager rtpManager;

    public RtpListener(OvRandomTeleport plugin) {
        this.rtpManager = plugin.getRtpManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (!e.hasChangedBlock()) {
            return;
        }
        Player player = e.getPlayer();
        if (processVoid(e, player)) {
            return;
        }
        String playerName = player.getName();
        RtpTask rtpTask = rtpManager.getActiveTasks(playerName);
        if (rtpTask != null) {
            Channel activeChannel = rtpTask.getActiveChannel();
            if (activeChannel.settings().restrictions().restrictMove()) {
                Utils.sendMessage(activeChannel.messages().movedOnTeleport(), player);
                this.cancelTeleportation(playerName, rtpTask);
            }
        }
    }

    private boolean processVoid(PlayerMoveEvent e, Player player) {
        Specifications specifications = rtpManager.getSpecifications();
        Map<String, List<String>> voidChannels = specifications.voidChannels();
        if (voidChannels.isEmpty() || e.getFrom().getBlockY() <= e.getTo().getBlockY()) {
            return false;
        }
        for (Map.Entry<String, List<String>> entry : voidChannels.entrySet()) {
            String channelId = entry.getKey();
            Object2IntMap<String> voidLevels = specifications.voidLevels();
            if (e.getTo().getBlockY() >
                    (voidLevels.isEmpty()
                            ? VersionUtils.VOID_LEVEL
                            : voidLevels.getOrDefault(channelId, VersionUtils.VOID_LEVEL))) {
                continue;
            }
            List<String> worlds = entry.getValue();
            if (!worlds.contains(player.getWorld().getName())) {
                continue;
            }
            if (!player.hasPermission("rtp.channel." + channelId)) {
                continue;
            }
            this.processTeleport(player, rtpManager.getChannelById(channelId), true);
            return true;
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            return;
        }
        Player player = e.getPlayer();
        String playerName = player.getName();
        RtpTask rtpTask = rtpManager.getActiveTasks(playerName);
        if (rtpTask != null) {
            Channel activeChannel = rtpTask.getActiveChannel();
            if (activeChannel.settings().restrictions().restrictTeleport()) {
                Utils.sendMessage(activeChannel.messages().teleportedOnTeleport(), player);
                this.cancelTeleportation(playerName, rtpTask);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (processProxy(player)) {
            return;
        }
        if (player.hasPlayedBefore()) {
            return;
        }
        Set<String> joinChannels = rtpManager.getSpecifications().joinChannels();
        if (joinChannels.isEmpty()) {
            return;
        }
        for (String channelId : joinChannels) {
            if (!player.hasPermission("rtp.channel." + channelId)) {
                continue;
            }
            this.processTeleport(player, rtpManager.getChannelById(channelId), false);
            return;
        }
    }

    private boolean processProxy(Player player) {
        if (rtpManager.getProxyCalls() != null && !rtpManager.getProxyCalls().isEmpty()) {
            String data = rtpManager.getProxyCalls().get(player.getName());
            if (data == null) {
                return false;
            }
            int separatorIndex = data.indexOf(';');
            Channel channel = rtpManager.getChannelById(data.substring(0, separatorIndex));
            World world = Bukkit.getWorld(data.substring(separatorIndex + 1));
            rtpManager.preTeleport(player, channel, world, false);
            rtpManager.getProxyCalls().remove(player.getName());
            return true;
        }
        return false;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Map<String, List<String>> respawnChannels = rtpManager.getSpecifications().respawnChannels();
        if (respawnChannels.isEmpty()) {
            return;
        }
        Player player = e.getPlayer();
        for (Map.Entry<String, List<String>> entry : respawnChannels.entrySet()) {
            List<String> worlds = entry.getValue();
            if (!worlds.contains(player.getWorld().getName())) {
                continue;
            }
            String channelId = entry.getKey();
            if (!player.hasPermission("rtp.channel." + channelId)) {
                continue;
            }
            this.processTeleport(player, rtpManager.getChannelById(channelId), false);
            return;
        }
    }

    private void processTeleport(Player player, Channel channel, boolean force) {
        List<World> activeWorlds = Utils.getWorldList(channel.activeWorlds());
        if (activeWorlds.isEmpty()) {
            rtpManager.printDebug("Unable to find any active world for channel " + channel.id());
            return;
        }
        if (!activeWorlds.contains(player.getWorld())) {
            if (channel.teleportToFirstAllowedWorld()) {
                rtpManager.preTeleport(player, channel, activeWorlds.get(0), force);
            }
            return;
        }
        rtpManager.preTeleport(player, channel, player.getWorld(), force);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }
        String playerName = player.getName();
        RtpTask rtpTask = rtpManager.getActiveTasks(playerName);
        if (rtpTask != null) {
            Channel activeChannel = rtpTask.getActiveChannel();
            Restrictions restrictions = activeChannel.settings().restrictions();
            if (restrictions.restrictDamage() && !restrictions.damageCheckOnlyPlayers()) {
                Utils.sendMessage(activeChannel.messages().damagedOnTeleport(), player);
                this.cancelTeleportation(playerName, rtpTask);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        Entity damagerEntity = e.getDamager();
        Entity damagedEntity = e.getEntity();

        if (damagerEntity instanceof Player damager) {
            this.handleDamagerPlayer(damager, damagedEntity);
        }
        if (damagedEntity instanceof Player damaged) {
            this.handleDamagedPlayer(damagerEntity, damaged);
        }
    }

    private void handleDamagerPlayer(Player damager, Entity damagedEntity) {
        String damagerName = damager.getName();
        RtpTask rtpTask = rtpManager.getActiveTasks(damagerName);
        if (rtpTask != null) {
            Channel activeChannel = rtpTask.getActiveChannel();
            Restrictions restrictions = activeChannel.settings().restrictions();
            if (restrictions.restrictDamageOthers()) {
                if (restrictions.damageCheckOnlyPlayers() && !(damagedEntity instanceof Player)) {
                    return;
                }
                Utils.sendMessage(activeChannel.messages().damagedOtherOnTeleport(), damager);
                this.cancelTeleportation(damagerName, rtpTask);
            }
        }
    }

    private void handleDamagedPlayer(Entity damagerEntity, Player damaged) {
        String damagedName = damaged.getName();
        RtpTask rtpTask = rtpManager.getActiveTasks(damagedName);
        if (rtpTask != null) {
            Channel activeChannel = rtpTask.getActiveChannel();
            Restrictions restrictions = activeChannel.settings().restrictions();
            if (restrictions.restrictDamage()) {
                Player damager = getDamager(damagerEntity);
                if (damager == null && restrictions.damageCheckOnlyPlayers()) {
                    return;
                }
                Utils.sendMessage(activeChannel.messages().damagedOnTeleport(), damaged);
                this.cancelTeleportation(damagedName, rtpTask);
            }
        }
    }

    private Player getDamager(Entity damagerEntity) {
        return switch (damagerEntity) {
            case Player p -> p;
            case Projectile p when p.getShooter() instanceof Player shooter -> shooter;
            case AreaEffectCloud c when c.getSource() instanceof Player shooter -> shooter;
            case TNTPrimed t when t.getSource() instanceof Player shooter -> shooter;
            default -> null;
        };
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }
        this.handlePlayerLeave(player);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        this.handlePlayerLeave(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(PlayerKickEvent e) {
        Player player = e.getPlayer();
        this.handlePlayerLeave(player);
    }

    private void handlePlayerLeave(Player player) {
        String playerName = player.getName();
        RtpTask rtpTask = rtpManager.getActiveTasks(playerName);
        if (rtpTask != null) {
            this.cancelTeleportation(playerName, rtpTask);
        }
    }

    private void cancelTeleportation(String playerName, RtpTask rtpTask) {
        rtpManager.printDebug("Teleportation for player " + playerName + " was cancelled because of restrictions");
        rtpTask.cancel(true);
        rtpManager.getLocationGenerator().getIterationsPerPlayer().removeInt(playerName);
    }
}
