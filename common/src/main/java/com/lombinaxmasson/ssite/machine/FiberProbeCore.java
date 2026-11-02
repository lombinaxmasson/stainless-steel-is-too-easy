package com.lombinaxmasson.ssite.machine;

import java.util.Set;

/**
 * P-02 read-only snapshot engine for the fiber status probe.
 *
 * <p>Kept free of Minecraft and GregTech types so the state transitions can be checked on the
 * JVM. The world adapter supplies one immutable {@link Target} per server tick; the engine owns
 * the completion edge and the redstone level. A lost, unloaded or broken target clears both the
 * success pulse and the signal, so the probe never keeps a stale running output.</p>
 */
public final class FiberProbeCore {
    public static final String MACHINE_TYPE = "fiber_status_probe";
    public static final int FORMAT = 1;
    /** One second of pulse at twenty ticks per second. */
    public static final int PULSE_TICKS = 20;

    /** First explicit target adapter. Later machines must be added here, not inferred by block class. */
    public static final Set<String> SUPPORTED_TARGETS = Set.of("ssite:precision_laser_workstation");

    public static final int NO_TARGET = 0, UNLOADED = 1, BROKEN = 2, UNLINKED = 3,
            IDLE = 4, RUNNING = 5, COMPLETE = 6, NO_ENERGY = 7, DISABLED = 8,
            STANDBY = 9, OUTPUT_BLOCKED = 10;

    public static final int SIGNAL_OFF = 0, SIGNAL_PULSE = 8, SIGNAL_RUNNING = 15;

    public static final String K_FORMAT = "probe.format", K_STATE = "probe.state",
            K_SIGNAL = "probe.signal", K_PROGRESS = "probe.progress", K_MAX = "probe.max",
            K_PULSE = "probe.pulse", K_COMPLETE = "probe.complete", K_TARGET = "probe.target";

    private FiberProbeCore() {}

    /** One server-side observation. {@code targetId} is empty when there is no block to name. */
    public record Target(String targetId, boolean chunkLoaded, boolean blockPresent, boolean supported,
                         boolean blockEntityPresent, boolean removed, boolean controlPresent,
                         boolean enabled, boolean powered, boolean active,
                         long progress, long progressMax, long energy, long energyCapacity) {
        public static Target unloaded() {
            return new Target("", false, false, false, false, false, false, false, false, false, 0, 0, 0, 0);
        }

        public static Target noTarget() {
            return new Target("", true, false, false, false, false, false, false, false, false, 0, 0, 0, 0);
        }

        public static Target unsupported(String targetId) {
            return new Target(targetId, true, true, false, false, false, false, false, false, false, 0, 0, 0, 0);
        }

        public static Target broken(String targetId) {
            return new Target(targetId, true, true, true, false, true, false, false, false, false, 0, 0, 0, 0);
        }

        public static Target unlinked(String targetId) {
            return new Target(targetId, true, true, true, true, false, false, false, false, false, 0, 0, 0, 0);
        }

        public static Target control(String targetId, boolean enabled, boolean powered, boolean active,
                                     long progress, long progressMax, long energy, long energyCapacity) {
            return new Target(targetId, true, true, true, true, false, true,
                    enabled, powered, active, progress, progressMax, energy, energyCapacity);
        }
    }

    public record Reading(int state, int signal, boolean started, long progress, long progressMax) {}

    /** The subset of behavior state the engine needs; the platform adapter delegates to it. */
    public interface State {
        int getInt(String key);
        void setInt(String key, int value);
        long getLong(String key);
        void setLong(String key, long value);
        boolean getBoolean(String key);
        void setBoolean(String key, boolean value);
        String getString(String key);
        void setString(String key, String value);
    }

    public static void initialize(State state) {
        if (state.getInt(K_FORMAT) != 0) return;
        state.setInt(K_FORMAT, FORMAT);
        state.setInt(K_STATE, NO_TARGET);
        state.setInt(K_SIGNAL, SIGNAL_OFF);
        state.setInt(K_PULSE, 0);
        state.setBoolean(K_COMPLETE, false);
        state.setLong(K_PROGRESS, 0);
        state.setLong(K_MAX, 0);
        state.setString(K_TARGET, "");
    }

    public static boolean isSupported(String targetId) {
        return targetId != null && SUPPORTED_TARGETS.contains(targetId);
    }

    public static String statusKey(int state) {
        return "ssite.fiber_probe.status." + state;
    }

    /**
     * Advances one server tick. The completion edge is the rising transition of
     * {@code progress >= max}; the pulse survives while the target reports idle, but never while
     * the target is missing or unloaded.
     */
    public static Reading advance(State state, Target target) {
        int pulse = state.getInt(K_PULSE);
        boolean complete = state.getBoolean(K_COMPLETE);
        boolean linked = target.chunkLoaded() && target.blockPresent() && target.supported()
                && target.blockEntityPresent() && !target.removed() && target.controlPresent();
        boolean completeNow = linked && target.progressMax() > 0 && target.progress() >= target.progressMax();
        boolean started = completeNow && !complete;

        int status;
        if (!target.chunkLoaded()) status = UNLOADED;
        else if (!target.blockPresent()) status = NO_TARGET;
        else if (!target.supported()) status = UNLINKED;
        else if (!target.blockEntityPresent() || target.removed()) status = BROKEN;
        else if (!target.controlPresent()) status = UNLINKED;
        else if (!target.enabled()) status = DISABLED;
        else if (target.active()) status = RUNNING;
        else if (completeNow) status = started || pulse > 0 ? COMPLETE : OUTPUT_BLOCKED;
        else if (target.energyCapacity() > 0 && target.energy() <= 0) status = NO_ENERGY;
        else if (target.powered()) status = STANDBY;
        else status = IDLE;

        if (started) pulse = PULSE_TICKS;
        if (!linked) {
            pulse = 0;
            complete = false;
        } else {
            complete = completeNow;
        }
        if (pulse > 0 && (status == IDLE || status == STANDBY || status == OUTPUT_BLOCKED)) status = COMPLETE;

        int signal = status == RUNNING ? SIGNAL_RUNNING : status == COMPLETE ? SIGNAL_PULSE : SIGNAL_OFF;
        if (pulse > 0) pulse--;

        state.setInt(K_PULSE, pulse);
        state.setBoolean(K_COMPLETE, complete);
        state.setInt(K_STATE, status);
        state.setInt(K_SIGNAL, signal);
        state.setLong(K_PROGRESS, linked ? target.progress() : 0);
        state.setLong(K_MAX, linked ? target.progressMax() : 0);
        state.setString(K_TARGET, target.targetId() == null ? "" : target.targetId());
        return new Reading(status, signal, started,
                linked ? target.progress() : 0, linked ? target.progressMax() : 0);
    }
}
