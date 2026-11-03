package com.lombinaxmasson.ssite.machine;

/**
 * Server-side snapshot of the P-04 process environment.
 *
 * <p>A snapshot is read at batch start and again on every continuing tick. {@code revision} lets a
 * consumer notice a room change even while the accepted bands stay the same; {@code reason} names
 * the first failed condition so the machine can show a specific stop reason. Clients never build or
 * submit this value: only the registered server-side provider may return it.</p>
 */
public record ProcessEnvironmentSnapshot(String id, boolean valid, int cleanLevel,
                                         int temperatureBand, int vibrationBand,
                                         long revision, String reason) {
    /** The advanced route requires the second clean band, a stable temperature and a quiet floor. */
    public static final int CLEAN_ADVANCED = 2;
    public static final int TEMPERATURE_STABLE = 1;
    public static final int VIBRATION_QUIET = 1;

    public static final String REASON_NONE = "none";
    public static final String REASON_NO_SERVICE = "no_service";
    public static final String REASON_UNLOADED = "unloaded";
    public static final String REASON_INVALID = "invalid";
    public static final String REASON_CLEAN = "clean";
    public static final String REASON_TEMPERATURE = "temperature";
    public static final String REASON_VIBRATION = "vibration";

    public ProcessEnvironmentSnapshot {
        id = id == null ? "" : id;
        reason = reason == null ? REASON_INVALID : reason;
    }

    public static ProcessEnvironmentSnapshot unavailable(String reason) {
        return new ProcessEnvironmentSnapshot("", false, 0, 0, 0, 0, reason);
    }

    public boolean acceptsAdvancedProcess() {
        return valid && cleanLevel >= CLEAN_ADVANCED
                && temperatureBand == TEMPERATURE_STABLE
                && vibrationBand <= VIBRATION_QUIET;
    }

    /** The first failing condition, or {@link #REASON_NONE} when the snapshot is acceptable. */
    public String failureReason() {
        if (!valid) return reason;
        if (cleanLevel < CLEAN_ADVANCED) return REASON_CLEAN;
        if (temperatureBand != TEMPERATURE_STABLE) return REASON_TEMPERATURE;
        if (vibrationBand > VIBRATION_QUIET) return REASON_VIBRATION;
        return REASON_NONE;
    }
}
