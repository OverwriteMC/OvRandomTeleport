package ru.overwrite.rtp.channels.settings;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import ru.overwrite.rtp.RtpManager;
import ru.overwrite.rtp.utils.Utils;

import java.util.List;
import java.util.Locale;

public record Particles(
        PreTeleportParticles preTeleport,
        AfterTeleportParticles afterTeleport
) {
    public record PreTeleportParticles(
            boolean enabled,
            AnimationType animation,
            boolean sendOnlyToPlayer,
            List<ParticleData> particles,
            int dots,
            int lines,
            int dotsPerLine,
            DoubleList circlesOffset,
            double radius,
            double particleSpeed,
            double speed,
            boolean invert,
            boolean jumping,
            boolean moveNear
    ) {
    }

    public record AfterTeleportParticles(
            boolean enabled,
            boolean sendOnlyToPlayer,
            ParticleData particle,
            int count,
            double radius,
            double particleSpeed
    ) {
    }

    public record ParticleData(Particle particle, Particle.DustOptions dustOptions) {
    }

    public enum AnimationType {
        BASIC,
        CAGE
    }

    private static final Particles EMPTY_PARTICLES = new Particles(
            new PreTeleportParticles(
                    false,
                    AnimationType.BASIC,
                    false,
                    null,
                    0,
                    8,
                    8,
                    null,
                    0D,
                    0D,
                    0D,
                    false,
                    false,
                    false
            ),
            new AfterTeleportParticles(
                    false,
                    false,
                    null,
                    0,
                    0D,
                    0D
            )
    );

    public static Particles create(ConfigurationSection particles, RtpManager rtpManager) {
        if (particles == null || particles.getKeys(false).isEmpty()) {
            return EMPTY_PARTICLES;
        }

        PreTeleportParticles preTeleport = createPreTeleport(particles.getConfigurationSection("pre_teleport"));
        AfterTeleportParticles afterTeleport = createAfterTeleport(particles.getConfigurationSection("after_teleport"));
        PreTeleportParticles validatedPreTeleport = validatePreTeleport(preTeleport);
        AfterTeleportParticles validatedAfterTeleport = validateAfterTeleport(afterTeleport);
        if (preTeleport != validatedPreTeleport || afterTeleport != validatedAfterTeleport) {
            rtpManager.printDebug("Invalid values in " + particles.getCurrentPath() + " replaced with defaults");
        }
        return new Particles(validatedPreTeleport, validatedAfterTeleport);
    }

    private static PreTeleportParticles createPreTeleport(ConfigurationSection preTeleportSection) {
        boolean preTeleportEnabled = false;
        AnimationType preTeleportAnimation = null;
        boolean preTeleportSendOnlyToPlayer = false;
        List<ParticleData> preTeleportParticles = null;
        int preTeleportDots = 0;
        int preTeleportLines = 0;
        int preTeleportDotsPerLine = 0;
        DoubleList preTeleportCirclesOffset = null;
        double preTeleportRadius = 0.0D;
        double preTeleportParticleSpeed = 0.0D;
        double preTeleportSpeed = 0.0D;
        boolean preTeleportInvert = false;
        boolean preTeleportJumping = false;
        boolean preTeleportMoveNear = false;

        if (preTeleportSection != null) {
            preTeleportEnabled = preTeleportSection.getBoolean("enabled", false);
            preTeleportAnimation = AnimationType.valueOf(preTeleportSection.getString("animation", "BASIC").toUpperCase(Locale.ENGLISH));
            preTeleportSendOnlyToPlayer = preTeleportSection.getBoolean("send_only_to_player", false);
            preTeleportDots = preTeleportSection.getInt("dots", 0);
            preTeleportLines = preTeleportSection.getInt("lines", 0);
            preTeleportDotsPerLine = preTeleportSection.getInt("dots_per_line", 0);
            preTeleportRadius = preTeleportSection.getDouble("radius", 0.0D);
            preTeleportParticleSpeed = preTeleportSection.getDouble("particle_speed", 0.0D);
            preTeleportSpeed = preTeleportSection.getDouble("speed", 0.0D);
            preTeleportInvert = preTeleportSection.getBoolean("invert", false);
            preTeleportJumping = preTeleportSection.getBoolean("jumping", false);
            preTeleportMoveNear = preTeleportSection.getBoolean("move_near", false);

            List<String> particleDataList = preTeleportSection.getStringList("id");
            if (!particleDataList.isEmpty()) {
                ImmutableList.Builder<ParticleData> builder = ImmutableList.builder();
                for (String id : particleDataList) {
                    builder.add(Utils.createParticleData(id));
                }
                preTeleportParticles = builder.build();
            }

            preTeleportCirclesOffset = new DoubleArrayList(preTeleportSection.getDoubleList("circles_offset"));
        }

        return new PreTeleportParticles(
                preTeleportEnabled,
                preTeleportAnimation,
                preTeleportSendOnlyToPlayer,
                preTeleportParticles,
                preTeleportDots,
                preTeleportLines,
                preTeleportDotsPerLine,
                preTeleportCirclesOffset,
                preTeleportRadius,
                preTeleportParticleSpeed,
                preTeleportSpeed,
                preTeleportInvert,
                preTeleportJumping,
                preTeleportMoveNear
        );
    }

    private static AfterTeleportParticles createAfterTeleport(ConfigurationSection afterTeleportSection) {
        boolean afterTeleportEnabled = false;
        boolean afterTeleportSendOnlyToPlayer = false;
        ParticleData afterTeleportParticle = null;
        int afterTeleportCount = 0;
        double afterTeleportRadius = 0.0D;
        double afterTeleportParticleSpeed = 0.0D;

        if (afterTeleportSection != null) {
            afterTeleportEnabled = afterTeleportSection.getBoolean("enabled", false);
            afterTeleportSendOnlyToPlayer = afterTeleportSection.getBoolean("send_only_to_player", false);
            afterTeleportCount = afterTeleportSection.getInt("count", afterTeleportCount);
            afterTeleportRadius = afterTeleportSection.getDouble("radius", 0.0D);
            afterTeleportParticleSpeed = afterTeleportSection.getDouble("particle_speed", 0.0D);

            String particleDataString = afterTeleportSection.getString("id");
            if (particleDataString != null) {
                afterTeleportParticle = Utils.createParticleData(particleDataString);
            }
        }

        return new AfterTeleportParticles(
                afterTeleportEnabled,
                afterTeleportSendOnlyToPlayer,
                afterTeleportParticle,
                afterTeleportCount,
                afterTeleportRadius,
                afterTeleportParticleSpeed
        );
    }

    private static PreTeleportParticles validatePreTeleport(PreTeleportParticles preTeleport) {
        if (!preTeleport.enabled()) {
            return preTeleport;
        }

        List<ParticleData> particles = preTeleport.particles();
        int dots = preTeleport.dots();
        int lines = preTeleport.lines();
        int dotsPerLine = preTeleport.dotsPerLine();
        DoubleList circlesOffset = preTeleport.circlesOffset();
        boolean defaultsApplied = false;
        if (particles == null) {
            particles = List.of(new ParticleData(Particle.FLAME, null));
            defaultsApplied = true;
        }
        if (dots <= 0) {
            dots = 16;
            defaultsApplied = true;
        }
        if (preTeleport.animation() == AnimationType.CAGE) {
            if (lines <= 0 || lines > dots || dots % lines != 0) {
                dots = 16;
                lines = 8;
                defaultsApplied = true;
            }
            if (dotsPerLine < 2) {
                dotsPerLine = 8;
                defaultsApplied = true;
            }
            if (!isValidCirclesOffset(circlesOffset)) {
                circlesOffset = new DoubleArrayList(new double[]{2.0D, 0.0D});
                defaultsApplied = true;
            }
        }
        if (!defaultsApplied) {
            return preTeleport;
        }
        return new PreTeleportParticles(
                preTeleport.enabled(),
                preTeleport.animation(),
                preTeleport.sendOnlyToPlayer(),
                particles,
                dots,
                lines,
                dotsPerLine,
                circlesOffset,
                preTeleport.radius(),
                preTeleport.particleSpeed(),
                preTeleport.speed(),
                preTeleport.invert(),
                preTeleport.jumping(),
                preTeleport.moveNear()
        );
    }

    private static AfterTeleportParticles validateAfterTeleport(AfterTeleportParticles afterTeleport) {
        if (!afterTeleport.enabled()) {
            return afterTeleport;
        }

        ParticleData particle = afterTeleport.particle();
        int count = afterTeleport.count();
        boolean defaultsApplied = false;
        if (particle == null) {
            particle = new ParticleData(Particle.CLOUD, null);
            defaultsApplied = true;
        }
        if (count < 2) {
            count = 45;
            defaultsApplied = true;
        }
        if (!defaultsApplied) {
            return afterTeleport;
        }
        return new AfterTeleportParticles(
                afterTeleport.enabled(),
                afterTeleport.sendOnlyToPlayer(),
                particle,
                count,
                afterTeleport.radius(),
                afterTeleport.particleSpeed()
        );
    }

    private static boolean isValidCirclesOffset(DoubleList circlesOffset) {
        for (int i = 0; i < circlesOffset.size(); i++) {
            double offset = circlesOffset.getDouble(i);
            if (!Double.isFinite(offset) || i > 0 && offset > circlesOffset.getDouble(i - 1)) {
                return false;
            }
        }
        return circlesOffset.size() >= 2 && circlesOffset.getDouble(0) > circlesOffset.getDouble(circlesOffset.size() - 1);
    }
}