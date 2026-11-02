package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.MachineBehaviorContext;
import gregtech.api.platform.machine.MachineControl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Server-side world adapter for the fiber status probe.
 *
 * <p>The probe reads the block directly in front of its face. {@code hasChunkAt} is checked
 * before any block entity access, so a probe never loads a neighbouring chunk. The target must
 * be on the explicit {@link FiberProbeCore#SUPPORTED_TARGETS} list and expose the host
 * {@link MachineControl} surface; P-01 is the first entry.</p>
 */
public final class FiberProbeContext {
    private FiberProbeContext() {}

    public static void tick(MachineBehaviorContext context) {
        Adapter state = new Adapter(context.state());
        FiberProbeCore.initialize(state);
        FiberProbeCore.Reading reading = FiberProbeCore.advance(state, readTarget(context));
        context.panel().setProgress(reading.progressMax() <= 0 ? 0F
                : Math.min(1F, (float) reading.progress() / (float) reading.progressMax()));
        context.panel().working(reading.state() == FiberProbeCore.RUNNING);
    }

    /** Current redstone level, lazily initialized for a signal-wire read before the first tick. */
    public static int signal(MachineBehaviorContext context) {
        Adapter state = new Adapter(context.state());
        FiberProbeCore.initialize(state);
        return state.getInt(FiberProbeCore.K_SIGNAL);
    }

    /** Shift-right-click reports the last snapshot without opening the machine menu. */
    public static boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) return false;
        Adapter state = new Adapter(context.state());
        FiberProbeCore.initialize(state);
        int status = state.getInt(FiberProbeCore.K_STATE);
        player.displayClientMessage(Component.translatable("ssite.fiber_probe.report",
                Component.translatable(FiberProbeCore.statusKey(status)),
                state.getLong(FiberProbeCore.K_PROGRESS), state.getLong(FiberProbeCore.K_MAX),
                state.getInt(FiberProbeCore.K_SIGNAL)), false);
        return true;
    }

    private static FiberProbeCore.Target readTarget(MachineBehaviorContext context) {
        Level level = context.level();
        // The machine front faces the placer; the observed block is the one behind it, matching
        // the host sensor convention so a probe placed against a machine reads that machine.
        BlockPos pos = context.pos().relative(context.facing().getOpposite());
        if (!level.hasChunkAt(pos)) return FiberProbeCore.Target.unloaded();
        BlockState blockState = level.getBlockState(pos);
        if (blockState.isAir()) return FiberProbeCore.Target.noTarget();
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
        String targetId = id == null ? "" : id.toString();
        if (!FiberProbeCore.isSupported(targetId)) return FiberProbeCore.Target.unsupported(targetId);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity == null || entity.isRemoved()) return FiberProbeCore.Target.broken(targetId);
        MachineControl control = MachineControl.find(entity, context.facing());
        if (control == null) return FiberProbeCore.Target.unlinked(targetId);
        return FiberProbeCore.Target.control(targetId,
                control.enabled(), control.running(), control.active(),
                control.progress(), control.progressMax(), control.energy(), control.energyCapacity());
    }

    private record Adapter(BehaviorState state) implements FiberProbeCore.State {
        @Override public int getInt(String key) { return state.getInt(key); }
        @Override public void setInt(String key, int value) { state.setInt(key, value); }
        @Override public long getLong(String key) { return state.getLong(key); }
        @Override public void setLong(String key, long value) { state.setLong(key, value); }
        @Override public boolean getBoolean(String key) { return state.getBoolean(key); }
        @Override public void setBoolean(String key, boolean value) { state.setBoolean(key, value); }
        @Override public String getString(String key) { return state.getString(key); }
        @Override public void setString(String key, String value) { state.setString(key, value); }
    }
}
