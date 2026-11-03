package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.AddonItems;
import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * P-03A ion implantation batch engine.
 *
 * <p>One batch consumes one advanced stripped wafer, one phosphorus dopant and one argon charge,
 * then advances for {@link #TICKS_PER_BATCH} ticks at {@link #EU_PER_TICK}. Materials are charged
 * once at a successful start; a lost environment, EU shortage or output blockage keeps the stored
 * progress and never re-charges. The snapshot is re-read every tick, so a revision change or an
 * invalid room pauses the same batch instead of dropping it.</p>
 *
 * <p>The engine is free of Minecraft and GregTech types so the ledger can run on the JVM. The
 * platform behavior supplies item, fluid and energy access; the display-only recipe row lives in
 * {@code AdvancedSemiconductorRecipes}, so EMI and the actual behavior stay separate.</p>
 */
public final class IonImplanterCore {
    public static final String MACHINE_TYPE = "ion_implanter";
    public static final String MAP = "ssite.recipe.ion_implanter";

    public static final int WAFER_SLOT = 0, DOPANT_SLOT = 1, OUTPUT_SLOT = 2, ARGON_TANK = 0;
    public static final int ARGON_PER_BATCH = 1000;
    public static final long TICKS_PER_BATCH = 800, EU_PER_TICK = 512;
    public static final int FORMAT = 1;

    public static final int IDLE = 0, WORK = 1, DONE = 2;

    public static final int ST_IDLE = 0, ST_WORKING = 1, ST_ENV_INVALID = 2, ST_WAIT_INPUT = 3,
            ST_WAIT_FLUID = 4, ST_WAIT_ENERGY = 5, ST_OUTPUT_BLOCKED = 6, ST_DONE = 7;

    public static final String K_FORMAT = "implanter.format", K_STATE = "implanter.state",
            K_PROGRESS = "implanter.progress", K_STATUS = "implanter.status",
            K_ENV_VALID = "implanter.env_valid", K_ENV_REASON = "implanter.env_reason",
            K_ENV_REVISION = "implanter.env_revision";

    private IonImplanterCore() {}

    /** Narrow boundary so the batch ledger also runs without a Minecraft world. */
    public interface Access {
        boolean hasWafer();
        boolean hasDopant();
        int argon();
        boolean drainArgon(int amount);
        boolean hasEnergy(long amount);
        boolean consumeEnergy(long amount);
        int state();
        void state(int value);
        long progress();
        void progress(long value);
        /** 0 on success, 1 for missing wafer/dopant, 2 for a blocked output, 3 for missing argon. */
        int startBatch();
        boolean commitProduct();
    }

    public record Tick(int status, long progress) {}

    public static Tick advance(Access access, ProcessEnvironmentSnapshot environment) {
        int state = access.state();
        if (state == DONE) {
            if (!access.commitProduct()) return new Tick(ST_OUTPUT_BLOCKED, access.progress());
            access.state(IDLE);
            access.progress(0);
            return new Tick(ST_IDLE, 0);
        }
        if (!environment.acceptsAdvancedProcess()) {
            return new Tick(ST_ENV_INVALID, access.progress());
        }
        if (state == IDLE) {
            if (!access.hasEnergy(EU_PER_TICK)) return new Tick(ST_WAIT_ENERGY, 0);
            int started = access.startBatch();
            if (started == 1) return new Tick(ST_WAIT_INPUT, 0);
            if (started == 2) return new Tick(ST_OUTPUT_BLOCKED, 0);
            if (started == 3) return new Tick(ST_WAIT_FLUID, 0);
            if (!access.consumeEnergy(EU_PER_TICK)) return new Tick(ST_WAIT_ENERGY, 0);
            access.state(WORK);
            long progress = access.progress() + 1;
            access.progress(progress);
            if (progress >= TICKS_PER_BATCH) {
                access.state(DONE);
                return new Tick(ST_DONE, progress);
            }
            return new Tick(ST_WORKING, progress);
        }
        if (!access.hasEnergy(EU_PER_TICK)) return new Tick(ST_WAIT_ENERGY, access.progress());
        if (!access.consumeEnergy(EU_PER_TICK)) return new Tick(ST_WAIT_ENERGY, access.progress());
        long progress = access.progress() + 1;
        access.progress(progress);
        if (progress >= TICKS_PER_BATCH) {
            access.state(DONE);
            return new Tick(ST_DONE, progress);
        }
        return new Tick(ST_WORKING, progress);
    }

    public static void initialize(BehaviorState state) {
        if (state.getLong(K_FORMAT) != 0) return;
        state.setLong(K_FORMAT, FORMAT);
        state.setLong(K_STATE, IDLE);
        state.setLong(K_PROGRESS, 0);
        state.setLong(K_STATUS, ST_IDLE);
        state.setBoolean(K_ENV_VALID, false);
        state.setString(K_ENV_REASON, ProcessEnvironmentSnapshot.REASON_NO_SERVICE);
        state.setLong(K_ENV_REVISION, 0);
    }

    public static void tick(MachineBehaviorContext context) {
        BehaviorState state = context.state();
        initialize(state);
        ProcessEnvironmentSnapshot environment =
                ProcessEnvironmentService.snapshot(context.level(), context.pos());
        Tick result = advance(new ContextAccess(context), environment);
        state.setLong(K_STATUS, result.status());
        state.setBoolean(K_ENV_VALID, environment.valid());
        state.setString(K_ENV_REASON, environment.acceptsAdvancedProcess()
                ? ProcessEnvironmentSnapshot.REASON_NONE : environment.failureReason());
        state.setLong(K_ENV_REVISION, environment.revision());
        context.panel().setProgress(Math.min(1F, result.progress() / (float) TICKS_PER_BATCH));
        context.panel().working(result.status() == ST_WORKING);
    }

    /** Sneak-use with an empty hand prints the same stop reason the Jade tooltip shows. */
    public static boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || !held.isEmpty()) return false;
        if (!context.level().isClientSide) {
            BehaviorState state = context.state();
            initialize(state);
            player.displayClientMessage(Component.translatable("ssite.ion_implanter.report",
                    Component.translatable("ssite.ion_implanter.status." + state.getLong(K_STATUS)),
                    Component.translatable("ssite.advanced_process.env." + state.getString(K_ENV_REASON)),
                    state.getLong(K_ENV_REVISION), state.getLong(K_PROGRESS), TICKS_PER_BATCH), true);
        }
        return true;
    }

    public static ItemStack product() {
        return AddonItems.stack("advancedWafer", "ImplantedWafer", 1);
    }

    public static boolean canInsert(int slot, ItemStack stack) {
        if (slot == WAFER_SLOT) return matches(stack, "advancedWafer", "AdvancedStrippedWafer");
        if (slot == DOPANT_SLOT) return matches(stack, "dust", "Phosphorus");
        return false;
    }

    static boolean matches(ItemStack stack, String prefix, String material) {
        if (stack == null || stack.isEmpty()) return false;
        ItemStack expected = AddonItems.stack(prefix, material, 1);
        return !expected.isEmpty() && ItemStack.isSameItem(stack, expected);
    }

    static boolean grow(ItemStack existing, ItemStack addition) {
        return existing.isEmpty() || ItemStack.isSameItem(existing, addition)
                && existing.getCount() + addition.getCount() <= existing.getMaxStackSize();
    }

    private static final class ContextAccess implements Access {
        private final MachineBehaviorContext context;
        private final BehaviorState state;

        ContextAccess(MachineBehaviorContext context) {
            this.context = context;
            this.state = context.state();
        }

        @Override public boolean hasWafer() { return matches(context.inventory().snapshot(WAFER_SLOT),
                "advancedWafer", "AdvancedStrippedWafer"); }
        @Override public boolean hasDopant() { return matches(context.inventory().snapshot(DOPANT_SLOT),
                "dust", "Phosphorus"); }

        @Override public int argon() {
            var stack = context.fluids().snapshot(ARGON_TANK);
            return stack == null || stack.isEmpty() ? 0 : stack.getAmount();
        }

        @Override public boolean drainArgon(int amount) {
            var drained = context.fluids().drain(ARGON_TANK, amount, false);
            return drained != null && !drained.isEmpty() && drained.getAmount() == amount;
        }

        @Override public boolean hasEnergy(long amount) { return context.energy().mainStored() >= amount; }
        @Override public boolean consumeEnergy(long amount) { return context.energy().consumeMain(amount); }
        @Override public int state() { return (int) state.getLong(K_STATE); }
        @Override public void state(int value) { state.setLong(K_STATE, value); }
        @Override public long progress() { return state.getLong(K_PROGRESS); }
        @Override public void progress(long value) { state.setLong(K_PROGRESS, Math.max(0, value)); }

        @Override public int startBatch() {
            if (!hasWafer() || !hasDopant()) return 1;
            if (argon() < ARGON_PER_BATCH) return 3;
            ItemStack product = product();
            if (product.isEmpty() || !grow(context.inventory().snapshot(OUTPUT_SLOT), product)) return 2;
            consume(WAFER_SLOT, 1);
            consume(DOPANT_SLOT, 1);
            return drainArgon(ARGON_PER_BATCH) ? 0 : 3;
        }

        @Override public boolean commitProduct() {
            ItemStack product = product();
            ItemStack out = context.inventory().snapshot(OUTPUT_SLOT);
            if (product.isEmpty() || !grow(out, product)) return false;
            context.inventory().set(OUTPUT_SLOT, merged(out, product));
            return true;
        }

        private void consume(int slot, int count) {
            ItemStack stack = context.inventory().snapshot(slot).copy();
            stack.shrink(count);
            context.inventory().set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
        }

        private static ItemStack merged(ItemStack existing, ItemStack addition) {
            if (existing.isEmpty()) return addition.copy();
            ItemStack merged = existing.copy();
            merged.grow(addition.getCount());
            return merged;
        }
    }
}
