package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * P-04 cleanroom controller.
 *
 * <p>The controller rescans every {@link #SCAN_INTERVAL} ticks, publishes the room and touches its
 * claim every tick. A scan is rejected as a whole when a chunk is unloaded, the interior exceeds
 * the size cap or the probe budget is exhausted; leaks keep the measured interior so devices can
 * report the opening. The published revision changes only when a consumer-visible value changes,
 * which lets P-03 pause and resume on a stable tick boundary.</p>
 */
public final class CleanroomControllerCore {
    public static final String MACHINE_TYPE = ProcessEnvironmentMachines.CONTROLLER;
    public static final int SCAN_INTERVAL = 20;
    public static final int FORMAT = 1;

    public static final int ST_NO_ROOM = 0, ST_VALID = 1, ST_INVALID = 2;

    public static final String K_FORMAT = "cleanroom.format", K_LAST_SCAN = "cleanroom.last_scan",
            K_PUBLISHED = "cleanroom.published", K_SIGNATURE = "cleanroom.signature",
            K_STATUS = "cleanroom.status", K_VALID = "cleanroom.valid",
            K_REASON = "cleanroom.reason", K_REVISION = "cleanroom.revision",
            K_CLEAN = "cleanroom.clean", K_TEMPERATURE = "cleanroom.temperature",
            K_VIBRATION = "cleanroom.vibration", K_VOLUME = "cleanroom.volume",
            K_VENTS = "cleanroom.vents", K_LEAKS = "cleanroom.leaks",
            K_FILTER = "cleanroom.filter";

    private CleanroomControllerCore() {}

    public static void initialize(BehaviorState state) {
        if (state.getLong(K_FORMAT) != 0) return;
        state.setLong(K_FORMAT, FORMAT);
        state.setLong(K_LAST_SCAN, 0);
        state.setLong(K_PUBLISHED, 0);
        state.setString(K_SIGNATURE, "");
        state.setLong(K_STATUS, ST_NO_ROOM);
        state.setLong(K_VALID, 0);
        state.setString(K_REASON, ProcessEnvironmentSnapshot.REASON_NO_ROOM);
        state.setLong(K_REVISION, 0);
        state.setLong(K_CLEAN, 0);
        state.setLong(K_TEMPERATURE, 0);
        state.setLong(K_VIBRATION, 0);
        state.setLong(K_VOLUME, 0);
        state.setLong(K_VENTS, 0);
        state.setLong(K_LEAKS, 0);
        state.setLong(K_FILTER, 0);
    }

    public static void tick(MachineBehaviorContext context) {
        if (context.level().isClientSide) return;
        BehaviorState state = context.state();
        initialize(state);
        Level level = context.level();
        BlockPos pos = context.pos().immutable();
        long now = level.getGameTime();
        long lastScan = state.getLong(K_LAST_SCAN);
        boolean due = state.getLong(K_PUBLISHED) == 0 || now < lastScan
                || now - lastScan >= SCAN_INTERVAL;
        if (due) {
            rescan(context, state, level, pos);
            state.setLong(K_LAST_SCAN, now);
            state.setLong(K_PUBLISHED, 1);
        } else {
            CleanroomEnvironment.touch(level, pos);
        }
        context.panel().working(state.getLong(K_VALID) != 0);
    }

    private static void rescan(MachineBehaviorContext context, BehaviorState state,
                               Level level, BlockPos pos) {
        String scope = level.dimension().location().toString();
        CleanroomRoom.Scan scan = CleanroomRoom.scan(pos, context.facing(),
                CleanroomEnvironment.cells(level), scope);

        int runningVents = 0;
        int minFilter = scan.vents().isEmpty() ? 0 : 1000;
        for (BlockPos vent : scan.vents()) {
            CleanroomEnvironment.VentReport report = CleanroomEnvironment.ventReport(level, vent);
            boolean running = report != null
                    ? report.running() : CleanroomEnvironment.working(level, vent);
            if (running) runningVents++;
            if (report != null) {
                minFilter = Math.min(minFilter, report.remainingPermille());
            } else if (!running) {
                minFilter = 0;
            }
        }

        int runningThermostats = 0;
        for (BlockPos thermostat : scan.thermostats()) {
            if (CleanroomEnvironment.working(level, thermostat)) runningThermostats++;
        }

        int rawVibration = 0;
        for (BlockPos source : scan.vibrationSources()) {
            if (!CleanroomEnvironment.working(level, source)) continue;
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(level.getBlockState(source).getBlock());
            int amplitude = key == null ? 0
                    : ProcessEnvironmentMachines.vibrationAmplitude(key.toString());
            rawVibration = Math.max(rawVibration, ProcessEnvironmentRules.vibrationContribution(
                    amplitude, ProcessEnvironmentRules.distance(pos, source)));
        }

        boolean overlap = scan.interior().isEmpty() ? false
                : CleanroomEnvironment.claim(level, pos, scan.roomId());
        long revision = state.getLong(K_REVISION);
        ProcessEnvironmentSnapshot provisional = ProcessEnvironmentRules.evaluate(scan.roomId(),
                revision, scan, runningVents, runningThermostats, rawVibration, overlap);
        if (!scan.interior().isEmpty()) {
            String signature = ProcessEnvironmentRules.signature(provisional, overlap);
            if (!signature.equals(state.getString(K_SIGNATURE))) {
                revision++;
                state.setString(K_SIGNATURE, signature);
            }
        }

        ProcessEnvironmentSnapshot snapshot = ProcessEnvironmentRules.evaluate(scan.roomId(),
                revision, scan, runningVents, runningThermostats, rawVibration, overlap);
        // An empty scan still publishes so a previously owned room is released.
        CleanroomEnvironment.publish(level, pos, scan, snapshot);
        writeState(state, scan, snapshot, runningVents, minFilter);
    }

    private static void writeState(BehaviorState state, CleanroomRoom.Scan scan,
                                   ProcessEnvironmentSnapshot snapshot,
                                   int runningVents, int minFilter) {
        int status = scan.interior().isEmpty() ? ST_NO_ROOM
                : snapshot.acceptsAdvancedProcess() ? ST_VALID : ST_INVALID;
        state.setLong(K_STATUS, status);
        state.setLong(K_VALID, snapshot.valid() ? 1 : 0);
        state.setString(K_REASON, snapshot.valid()
                ? ProcessEnvironmentSnapshot.REASON_NONE : snapshot.failureReason());
        state.setLong(K_REVISION, snapshot.revision());
        state.setLong(K_CLEAN, snapshot.cleanLevel());
        state.setLong(K_TEMPERATURE, snapshot.temperatureBand());
        state.setLong(K_VIBRATION, snapshot.vibrationBand());
        state.setLong(K_VOLUME, scan.volume());
        state.setLong(K_VENTS, runningVents);
        state.setLong(K_LEAKS, scan.leaks().size());
        state.setLong(K_FILTER, minFilter);
    }

    /** Sneak-use with an empty hand prints the same values the Jade tooltip shows. */
    public static boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown() || !player.getItemInHand(hand).isEmpty()) return false;
        if (!context.level().isClientSide) {
            BehaviorState state = context.state();
            initialize(state);
            player.displayClientMessage(Component.translatable("ssite.cleanroom_controller.report",
                    Component.translatable("ssite.cleanroom_controller.status." + state.getLong(K_STATUS)),
                    Component.translatable("ssite.advanced_process.env." + state.getString(K_REASON)),
                    state.getLong(K_REVISION), state.getLong(K_CLEAN),
                    state.getLong(K_TEMPERATURE), state.getLong(K_VIBRATION),
                    state.getLong(K_VOLUME), state.getLong(K_VENTS),
                    state.getLong(K_FILTER) / 10), false);
        }
        return true;
    }
}
