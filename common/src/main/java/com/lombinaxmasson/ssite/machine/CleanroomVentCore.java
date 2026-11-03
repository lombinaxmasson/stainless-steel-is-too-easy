package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.AddonItems;
import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * P-04A filter ventilation unit.
 *
 * <p>The vent consumes EU and one replaceable filter cartridge per {@link #FILTER_TICKS} running
 * ticks. A cartridge is installed from the slot on start and wears out while running; when the
 * filter is exhausted the vent stops until another cartridge is inserted. Progress and consumed
 * material are never refunded, matching the card's "pause, do not destroy the batch" rule.</p>
 */
public final class CleanroomVentCore {
    public static final String MACHINE_TYPE = ProcessEnvironmentMachines.VENT;
    public static final String FILTER_PREFIX = "filterCartridge";
    public static final String FILTER_MATERIAL = "CleanroomFilter";

    public static final int FILTER_SLOT = 0;
    public static final int FILTER_TICKS = 24_000;
    public static final long EU_PER_TICK = 32;
    public static final int FORMAT = 1;

    public static final int ST_IDLE = 0, ST_RUNNING = 1, ST_CLOGGED = 2, ST_NO_ENERGY = 3;

    public static final String K_FORMAT = "vent.format", K_INSTALLED = "vent.installed",
            K_WEAR = "vent.wear", K_STATUS = "vent.status", K_RUNNING = "vent.running",
            K_REMAINING = "vent.remaining";

    private CleanroomVentCore() {}

    public record Step(int status, boolean running, int wear, int remainingPermille) {}

    public interface Access {
        boolean hasFilter();
        boolean consumeFilter();
        boolean installed();
        void installed(boolean value);
        int wear();
        void wear(int value);
        boolean hasEnergy(long amount);
        boolean consumeEnergy(long amount);
    }

    public static void initialize(BehaviorState state) {
        if (state.getLong(K_FORMAT) != 0) return;
        state.setLong(K_FORMAT, FORMAT);
        state.setLong(K_INSTALLED, 0);
        state.setLong(K_WEAR, 0);
        state.setLong(K_STATUS, ST_IDLE);
        state.setLong(K_RUNNING, 0);
        state.setLong(K_REMAINING, 0);
    }

    /** One server tick. A cartridge is only consumed when it is installed, never on a pause. */
    public static Step advance(Access access) {
        boolean installed = access.installed();
        int wear = Math.max(0, Math.min(FILTER_TICKS, access.wear()));
        if (!installed) {
            if (!access.hasFilter()) return new Step(ST_IDLE, false, wear, 0);
            if (!access.consumeFilter()) return new Step(ST_IDLE, false, wear, 0);
            access.installed(true);
            installed = true;
            wear = 0;
            access.wear(0);
        }
        if (wear >= FILTER_TICKS) {
            access.installed(false);
            access.wear(0);
            return new Step(ST_CLOGGED, false, 0, 0);
        }
        int remaining = Math.max(0, (FILTER_TICKS - wear) * 1000 / FILTER_TICKS);
        if (!access.hasEnergy(EU_PER_TICK) || !access.consumeEnergy(EU_PER_TICK)) {
            return new Step(ST_NO_ENERGY, false, wear, remaining);
        }
        wear++;
        access.wear(wear);
        return new Step(ST_RUNNING, true, wear,
                Math.max(0, (FILTER_TICKS - wear) * 1000 / FILTER_TICKS));
    }

    public static void tick(MachineBehaviorContext context) {
        if (context.level().isClientSide) return;
        BehaviorState state = context.state();
        initialize(state);
        Step step = advance(new ContextAccess(context, state));
        state.setLong(K_STATUS, step.status());
        state.setLong(K_RUNNING, step.running() ? 1 : 0);
        state.setLong(K_WEAR, step.wear());
        state.setLong(K_REMAINING, step.remainingPermille());
        context.panel().working(step.running());
        CleanroomEnvironment.reportVent(context.level(), context.pos(), step.running(),
                step.remainingPermille());
    }

    public static boolean canInsert(int slot, ItemStack stack) {
        return slot == FILTER_SLOT && matches(stack);
    }

    private static boolean matches(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ItemStack expected = AddonItems.stack(FILTER_PREFIX, FILTER_MATERIAL, 1);
        return !expected.isEmpty() && ItemStack.isSameItem(stack, expected);
    }

    public static boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown() || !player.getItemInHand(hand).isEmpty()) return false;
        if (!context.level().isClientSide) {
            BehaviorState state = context.state();
            initialize(state);
            player.displayClientMessage(Component.translatable("ssite.cleanroom_filter_vent.report",
                    Component.translatable("ssite.cleanroom_filter_vent.status." + state.getLong(K_STATUS)),
                    state.getLong(K_REMAINING) / 10), true);
        }
        return true;
    }

    private static final class ContextAccess implements Access {
        private final MachineBehaviorContext context;
        private final BehaviorState state;

        ContextAccess(MachineBehaviorContext context, BehaviorState state) {
            this.context = context;
            this.state = state;
        }

        @Override public boolean hasFilter() { return matches(context.inventory().snapshot(FILTER_SLOT)); }

        @Override public boolean consumeFilter() {
            ItemStack stack = context.inventory().snapshot(FILTER_SLOT).copy();
            if (!matches(stack)) return false;
            stack.shrink(1);
            context.inventory().set(FILTER_SLOT, stack.isEmpty() ? ItemStack.EMPTY : stack);
            return true;
        }

        @Override public boolean installed() { return state.getLong(K_INSTALLED) != 0; }
        @Override public void installed(boolean value) { state.setLong(K_INSTALLED, value ? 1 : 0); }
        @Override public int wear() { return (int) state.getLong(K_WEAR); }
        @Override public void wear(int value) { state.setLong(K_WEAR, Math.max(0, value)); }
        @Override public boolean hasEnergy(long amount) { return context.energy().mainStored() >= amount; }
        @Override public boolean consumeEnergy(long amount) { return context.energy().consumeMain(amount); }
    }
}
