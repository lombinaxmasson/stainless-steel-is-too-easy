package com.lombinaxmasson.ssite.machine;

import java.util.HashMap;
import java.util.Map;

/** Pure JVM checks of the read-only fiber probe state machine. */
public final class FiberProbeTests {
    private static final String TARGET = "ssite:precision_laser_workstation";

    private FiberProbeTests() {}

    public static void main(String[] args) {
        loadedTargetRuns(); noTargetIsDeterministic(); unloadedTargetIgnoresStaleData();
        brokenAndUnlinkedTargets(); stopReasonsAreDistinct(); completionPulseIsHeld();
        completionDoesNotRetriggerWhileHeld(); pulseRearmsAfterProgressResets();
        lostTargetDropsPulse(); disabledAndStarvedTargetsDoNotSignal();
        initializationIsOneTime();
        System.out.println("FiberProbeTests: all 11 checks passed.");
    }

    private static void loadedTargetRuns() {
        FakeState state = start();
        FiberProbeCore.Reading reading = FiberProbeCore.advance(state, target(5, 10, true, false));
        check(reading.state() == FiberProbeCore.RUNNING && reading.signal() == FiberProbeCore.SIGNAL_RUNNING,
                "a supported running target drives the full signal");
        check(state.getInt(FiberProbeCore.K_STATE) == FiberProbeCore.RUNNING, "the snapshot is persisted");
    }

    private static void noTargetIsDeterministic() {
        FakeState state = start();
        FiberProbeCore.Reading reading = FiberProbeCore.advance(state, FiberProbeCore.Target.noTarget());
        check(reading.state() == FiberProbeCore.NO_TARGET && reading.signal() == FiberProbeCore.SIGNAL_OFF,
                "an empty front face reports no target and no signal");
    }

    private static void unloadedTargetIgnoresStaleData() {
        FakeState state = start();
        FiberProbeCore.Target stale = new FiberProbeCore.Target(TARGET, false, true, true, true, false, true,
                true, true, true, 9, 10, 500, 1000);
        FiberProbeCore.Reading reading = FiberProbeCore.advance(state, stale);
        check(reading.state() == FiberProbeCore.UNLOADED && reading.signal() == FiberProbeCore.SIGNAL_OFF,
                "an unloaded chunk is reported before any target field is read");
        check(state.getInt(FiberProbeCore.K_PULSE) == 0, "an unloaded target also clears the pulse");
    }

    private static void brokenAndUnlinkedTargets() {
        FiberProbeCore.Reading broken = FiberProbeCore.advance(start(), FiberProbeCore.Target.broken(TARGET));
        check(broken.state() == FiberProbeCore.BROKEN && broken.signal() == FiberProbeCore.SIGNAL_OFF,
                "a removed block entity is reported as broken");
        FiberProbeCore.Reading unlinked = FiberProbeCore.advance(start(), FiberProbeCore.Target.unlinked(TARGET));
        check(unlinked.state() == FiberProbeCore.UNLINKED && unlinked.signal() == FiberProbeCore.SIGNAL_OFF,
                "a supported block without machine controls is unlinked");
        FiberProbeCore.Reading unsupported = FiberProbeCore.advance(start(),
                FiberProbeCore.Target.unsupported("minecraft:dirt"));
        check(unsupported.state() == FiberProbeCore.UNLINKED, "an explicit adapter list rejects other blocks");
    }

    private static void stopReasonsAreDistinct() {
        check(stateFor(control(false, true, false, 0, 0, 500, 1000)) == FiberProbeCore.DISABLED,
                "the machine switch reports disabled");
        check(stateFor(control(true, false, false, 0, 0, 0, 1000)) == FiberProbeCore.NO_ENERGY,
                "an empty buffer reports no energy");
        check(stateFor(control(true, true, false, 0, 0, 500, 1000)) == FiberProbeCore.STANDBY,
                "powered without a valid recipe reports standby");
        check(stateFor(control(true, false, false, 0, 0, 0, 0)) == FiberProbeCore.IDLE,
                "an unpowered idle machine reports idle");
    }

    private static void completionPulseIsHeld() {
        FakeState state = start();
        FiberProbeCore.advance(state, target(5, 10, true, false));
        FiberProbeCore.Reading complete = FiberProbeCore.advance(state, target(10, 10, false, true));
        check(complete.started() && complete.state() == FiberProbeCore.COMPLETE
                && complete.signal() == FiberProbeCore.SIGNAL_PULSE, "a finished recipe starts a distinct pulse");
        int pulses = 1;
        for (int tick = 1; tick < FiberProbeCore.PULSE_TICKS; tick++) {
            FiberProbeCore.Reading held = FiberProbeCore.advance(state, target(0, 0, false, true));
            check(held.state() == FiberProbeCore.COMPLETE && held.signal() == FiberProbeCore.SIGNAL_PULSE,
                    "the completion pulse survives an idle target");
            pulses++;
        }
        FiberProbeCore.Reading after = FiberProbeCore.advance(state, target(0, 0, false, true));
        check(pulses == FiberProbeCore.PULSE_TICKS && after.state() == FiberProbeCore.STANDBY
                && after.signal() == FiberProbeCore.SIGNAL_OFF, "the pulse ends after one second");
    }

    private static void completionDoesNotRetriggerWhileHeld() {
        FakeState state = start();
        FiberProbeCore.advance(state, target(10, 10, false, true));
        int starts = 1;
        for (int tick = 0; tick < FiberProbeCore.PULSE_TICKS + 5; tick++) {
            starts += FiberProbeCore.advance(state, target(10, 10, false, true)).started() ? 1 : 0;
        }
        check(starts == 1, "a stalled completion does not restart the pulse");
        FiberProbeCore.Reading blocked = FiberProbeCore.advance(state, target(10, 10, false, true));
        check(blocked.state() == FiberProbeCore.OUTPUT_BLOCKED && blocked.signal() == FiberProbeCore.SIGNAL_OFF,
                "a completion that never resets reports output blocked");
    }

    private static void pulseRearmsAfterProgressResets() {
        FakeState state = start();
        FiberProbeCore.advance(state, target(10, 10, false, true));
        FiberProbeCore.advance(state, target(0, 0, false, true));
        FiberProbeCore.advance(state, target(5, 10, true, false));
        FiberProbeCore.Reading again = FiberProbeCore.advance(state, target(10, 10, false, true));
        check(again.started(), "a later batch starts a new pulse");
    }

    private static void lostTargetDropsPulse() {
        FakeState state = start();
        FiberProbeCore.advance(state, target(10, 10, false, true));
        FiberProbeCore.Reading lost = FiberProbeCore.advance(state, FiberProbeCore.Target.unloaded());
        check(lost.state() == FiberProbeCore.UNLOADED && lost.signal() == FiberProbeCore.SIGNAL_OFF,
                "a lost target stops the signal immediately");
        FiberProbeCore.Reading idle = FiberProbeCore.advance(state, target(0, 0, false, true));
        check(idle.state() == FiberProbeCore.STANDBY, "the old pulse is not replayed after the target returns");
    }

    private static void disabledAndStarvedTargetsDoNotSignal() {
        FakeState state = start();
        FiberProbeCore.advance(state, target(10, 10, false, true));
        FiberProbeCore.Reading disabled = FiberProbeCore.advance(state,
                new FiberProbeCore.Target(TARGET, true, true, true, true, false, true,
                        false, true, false, 10, 10, 100, 1000));
        check(disabled.state() == FiberProbeCore.DISABLED && disabled.signal() == FiberProbeCore.SIGNAL_OFF,
                "a fault or switch stop suppresses a pending pulse");
    }

    private static void initializationIsOneTime() {
        FakeState state = new FakeState();
        FiberProbeCore.initialize(state);
        check(state.getInt(FiberProbeCore.K_FORMAT) == FiberProbeCore.FORMAT
                && state.getInt(FiberProbeCore.K_STATE) == FiberProbeCore.NO_TARGET, "defaults are written once");
        state.setInt(FiberProbeCore.K_STATE, FiberProbeCore.RUNNING);
        FiberProbeCore.initialize(state);
        check(state.getInt(FiberProbeCore.K_STATE) == FiberProbeCore.RUNNING, "a saved snapshot is not reset");
    }

    private static FakeState start() {
        FakeState state = new FakeState();
        FiberProbeCore.initialize(state);
        return state;
    }

    private static FiberProbeCore.Target target(long progress, long maximum, boolean active, boolean powered) {
        return FiberProbeCore.Target.control(TARGET, true, powered, active, progress, maximum,
                powered ? 500 : 0, 1000);
    }

    private static FiberProbeCore.Target control(boolean enabled, boolean powered, boolean active,
                                                 long progress, long maximum, long energy, long capacity) {
        return FiberProbeCore.Target.control(TARGET, enabled, powered, active, progress, maximum, energy, capacity);
    }

    private static int stateFor(FiberProbeCore.Target target) {
        return FiberProbeCore.advance(start(), target).state();
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class FakeState implements FiberProbeCore.State {
        private final Map<String, Object> values = new HashMap<>();

        @Override public int getInt(String key) {
            Object value = values.get(key);
            return value instanceof Integer integer ? integer : 0;
        }

        @Override public void setInt(String key, int value) { values.put(key, value); }

        @Override public long getLong(String key) {
            Object value = values.get(key);
            return value instanceof Long number ? number : 0L;
        }

        @Override public void setLong(String key, long value) { values.put(key, value); }

        @Override public boolean getBoolean(String key) { return Boolean.TRUE.equals(values.get(key)); }

        @Override public void setBoolean(String key, boolean value) { values.put(key, value); }

        @Override public String getString(String key) {
            Object value = values.get(key);
            return value instanceof String text ? text : "";
        }

        @Override public void setString(String key, String value) { values.put(key, value); }
    }
}
