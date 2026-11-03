package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

/**
 * P-04B abstraction of process temperature control.
 *
 * <p>A working thermostat spends EU and ultrapure water and publishes the stable temperature band
 * to the room scan through the host {@code lit} state. There is no real heat model: the room either
 * has a running thermostat, an installed but idle one, or none. The host HU cache is deliberately
 * not read as a room temperature.</p>
 */
public final class CleanroomThermostatCore {
    public static final String MACHINE_TYPE = ProcessEnvironmentMachines.THERMOSTAT;
    public static final String COOLANT_FIELD = "GenLiquid_UltrapureWater";
    public static final int TANK = 0;
    public static final int COOLANT_PER_TICK = 1;
    public static final long EU_PER_TICK = 16;
    public static final int FORMAT = 1;

    public static final int ST_IDLE = 0, ST_RUNNING = 1, ST_NO_COOLANT = 2, ST_NO_ENERGY = 3;

    public static final String K_FORMAT = "thermostat.format", K_STATUS = "thermostat.status",
            K_RUNNING = "thermostat.running", K_COOLANT = "thermostat.coolant";

    private CleanroomThermostatCore() {}

    public record Step(int status, boolean running, int coolant) {}

    public interface Access {
        int coolant();
        boolean drainCoolant(int amount);
        boolean hasEnergy(long amount);
        boolean consumeEnergy(long amount);
    }

    public static void initialize(BehaviorState state) {
        if (state.getLong(K_FORMAT) != 0) return;
        state.setLong(K_FORMAT, FORMAT);
        state.setLong(K_STATUS, ST_IDLE);
        state.setLong(K_RUNNING, 0);
        state.setLong(K_COOLANT, 0);
    }

    public static Step advance(Access access) {
        int coolant = Math.max(0, access.coolant());
        if (coolant < COOLANT_PER_TICK) return new Step(ST_NO_COOLANT, false, coolant);
        if (!access.hasEnergy(EU_PER_TICK)) return new Step(ST_NO_ENERGY, false, coolant);
        if (!access.drainCoolant(COOLANT_PER_TICK)) return new Step(ST_NO_COOLANT, false, coolant);
        if (!access.consumeEnergy(EU_PER_TICK)) return new Step(ST_NO_ENERGY, false, coolant);
        return new Step(ST_RUNNING, true, access.coolant());
    }

    public static void tick(MachineBehaviorContext context) {
        if (context.level().isClientSide) return;
        BehaviorState state = context.state();
        initialize(state);
        Step step = advance(new ContextAccess(context));
        state.setLong(K_STATUS, step.status());
        state.setLong(K_RUNNING, step.running() ? 1 : 0);
        state.setLong(K_COOLANT, step.coolant());
        context.panel().working(step.running());
    }

    public static boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown() || !player.getItemInHand(hand).isEmpty()) return false;
        if (!context.level().isClientSide) {
            BehaviorState state = context.state();
            initialize(state);
            player.displayClientMessage(Component.translatable("ssite.cleanroom_thermostat.report",
                    Component.translatable("ssite.cleanroom_thermostat.status." + state.getLong(K_STATUS)),
                    state.getLong(K_COOLANT)), true);
        }
        return true;
    }

    private static final class ContextAccess implements Access {
        private final MachineBehaviorContext context;

        ContextAccess(MachineBehaviorContext context) {
            this.context = context;
        }

        @Override public int coolant() {
            var stack = context.fluids().snapshot(TANK);
            return stack == null || stack.isEmpty() ? 0 : stack.getAmount();
        }

        @Override public boolean drainCoolant(int amount) {
            var drained = context.fluids().drain(TANK, amount, false);
            return drained != null && !drained.isEmpty() && drained.getAmount() == amount;
        }

        @Override public boolean hasEnergy(long amount) { return context.energy().mainStored() >= amount; }
        @Override public boolean consumeEnergy(long amount) { return context.energy().consumeMain(amount); }
    }
}
