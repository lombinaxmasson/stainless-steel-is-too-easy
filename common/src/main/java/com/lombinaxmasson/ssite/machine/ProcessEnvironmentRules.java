package com.lombinaxmasson.ssite.machine;

import net.minecraft.core.BlockPos;

/**
 * P-04 band rules shared by the controller and the JVM checks.
 *
 * <p>Cleanliness is abstract: a sealed room with one running filter vent reaches the advanced
 * band, a sealed room with an installed but idle vent reaches the basic band, and a sealed room
 * without any vent stays at zero. Temperature is stable only while a thermostat actually runs.
 * Vibration comes only from the explicit source list, falls off with distance and is reduced by
 * isolation mounts; nothing scans terrain or entities.</p>
 */
public final class ProcessEnvironmentRules {
    /** Chebyshev distance past which a registered source contributes nothing. */
    public static final int VIBRATION_RANGE = 12;
    /** Most isolation mounts that can reduce the band. */
    public static final int MAX_ISOLATION_MOUNTS = 2;

    public static final int CLEAN_BASIC = 1;
    public static final int CLEAN_ADVANCED = ProcessEnvironmentSnapshot.CLEAN_ADVANCED;

    private ProcessEnvironmentRules() {}

    public static int cleanLevel(int runningVents, int installedVents) {
        if (runningVents > 0) return CLEAN_ADVANCED;
        return installedVents > 0 ? CLEAN_BASIC : 0;
    }

    public static int temperatureBand(int runningThermostats) {
        return runningThermostats > 0 ? ProcessEnvironmentSnapshot.TEMPERATURE_STABLE : 0;
    }

    public static int vibrationBand(int rawVibration, int isolationMounts) {
        int reduced = rawVibration - Math.min(MAX_ISOLATION_MOUNTS, Math.max(0, isolationMounts));
        return Math.max(0, reduced);
    }

    /** Contribution of one active source at the given Chebyshev distance from the controller. */
    public static int vibrationContribution(int amplitude, int distance) {
        if (amplitude <= 0 || distance < 0 || distance > VIBRATION_RANGE) return 0;
        return Math.max(1, amplitude - distance / 4);
    }

    public static int distance(BlockPos a, BlockPos b) {
        return Math.max(Math.abs(a.getX() - b.getX()),
                Math.max(Math.abs(a.getY() - b.getY()), Math.abs(a.getZ() - b.getZ())));
    }

    /**
     * Builds the published snapshot. Enclosure and overlap failures win over band failures; the
     * stored {@code reason} is the first failure a consumer should report.
     */
    public static ProcessEnvironmentSnapshot evaluate(String roomId, long revision,
                                                      CleanroomRoom.Scan scan,
                                                      int runningVents, int runningThermostats,
                                                      int rawVibration, boolean overlap) {
        int clean = cleanLevel(runningVents, scan.vents().size());
        int temperature = temperatureBand(runningThermostats);
        int vibration = vibrationBand(rawVibration, scan.isolation());
        if (overlap) {
            return ProcessEnvironmentSnapshot.invalid(roomId, clean, temperature, vibration,
                    revision, ProcessEnvironmentSnapshot.REASON_OVERLAP);
        }
        if (!scan.valid()) {
            return ProcessEnvironmentSnapshot.invalid(roomId, clean, temperature, vibration,
                    revision, scan.reason());
        }
        String reason = clean < CLEAN_ADVANCED ? ProcessEnvironmentSnapshot.REASON_CLEAN
                : temperature != ProcessEnvironmentSnapshot.TEMPERATURE_STABLE
                ? ProcessEnvironmentSnapshot.REASON_TEMPERATURE
                : vibration > ProcessEnvironmentSnapshot.VIBRATION_QUIET
                ? ProcessEnvironmentSnapshot.REASON_VIBRATION
                : ProcessEnvironmentSnapshot.REASON_NONE;
        return new ProcessEnvironmentSnapshot(roomId, true, clean, temperature, vibration,
                revision, reason);
    }

    /** A stable signature for revision accounting; only published values participate. */
    public static String signature(ProcessEnvironmentSnapshot snapshot, boolean overlap) {
        return snapshot.id() + "|" + snapshot.valid() + "|" + snapshot.cleanLevel() + "|"
                + snapshot.temperatureBand() + "|" + snapshot.vibrationBand() + "|" + overlap;
    }
}
