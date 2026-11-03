package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.AddonItems;
import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.MachineBehaviorContext;
import gregtech.api.addon.platform.AddonFluids;
import gregtech.api.addon.material.GregTechMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Set;

/**
 * Steam-blown fixed-bed gasifier. One batch reserves a solid charge, the steam it needs and room
 * for the raw gas and residue, then releases the batch after a fixed number of powered ticks.
 * Raw gas is a distinct fluid; the cleaning and separation rows in {@code UtilityRecipes} keep
 * the chemical ledger out of any fuel table, so no synthetic heating value is invented here.
 */
public final class CoalGasifierCore {
    public static final String MACHINE_TYPE = "coal_gasifier";
    /** Display-only table: the behavior owns the real batch ledger. */
    public static final String MAP = "ssite.recipe.gasifier";

    public static final int INPUT_SLOT = 0, RESIDUE_SLOT = 1;
    public static final int MEDIUM_TANK = 0, RAW_GAS_TANK = 1;
    public static final int MEDIUM_CAPACITY = 8_000, RAW_GAS_CAPACITY = 16_000;
    public static final long EU_PER_TICK = 64, EU_CAPACITY = 1_000_000;

    public static final int BATCH_NONE = 0, BATCH_COAL = 1, BATCH_COKE = 2;
    public static final long COAL_STEAM = 2_000, COKE_STEAM = 1_500;
    public static final long COAL_TICKS = 200, COKE_TICKS = 160;
    /** Raw gas is deliberately identical for both charges; the coke batch only saves steam and time. */
    public static final long RAW_GAS = 4_000;

    public static final int IDLE = 0, WORK = 1, DONE = 2;
    public static final int ST_IDLE = 0, ST_WORKING = 1, ST_WAIT_EU = 2, ST_WAIT_INPUT = 3,
            ST_WAIT_MEDIUM = 4, ST_OUTPUT_BLOCKED = 5, ST_DONE = 6;

    public static final int FORMAT = 1;
    public static final String K_STATE = "gasifier.state", K_BATCH = "gasifier.batch",
            K_PROGRESS = "gasifier.progress", K_STATUS = "gasifier.status",
            K_FORMAT = "gasifier.format";

    private static final Set<String> SOLID_FORMS = Set.of("dust", "gem");

    private CoalGasifierCore() {}

    /**
     * The narrow boundary keeps the batch engine testable without a Minecraft world. A single-block
     * behaviour machine has no structure layout, so there is no formed gate to check here.
     */
    public interface Access {
        long eu();
        boolean consumeEu(long amount);
        int state();
        void state(int value);
        int batch();
        void batch(int value);
        long progress();
        void progress(long value);
        /** 0 started; 1 no solid charge; 2 steam missing; 3 raw tank full; 4 residue slot blocked. */
        int startBatch();
        /** Writes the reserved batch; false when the outputs cannot take it. */
        boolean commitBatch();
    }

    public record Tick(int status) {}

    public static long steamFor(int batch) {
        return switch (batch) {
            case BATCH_COAL -> COAL_STEAM;
            case BATCH_COKE -> COKE_STEAM;
            default -> 0;
        };
    }

    public static long ticksFor(int batch) {
        return switch (batch) {
            case BATCH_COAL -> COAL_TICKS;
            case BATCH_COKE -> COKE_TICKS;
            default -> 0;
        };
    }

    public static Tick advance(Access access) {
        if (access.state() == DONE) {
            if (!access.commitBatch()) return new Tick(ST_OUTPUT_BLOCKED);
            access.state(IDLE);
            access.batch(BATCH_NONE);
            access.progress(0);
        }
        if (access.state() == IDLE) {
            int started = access.startBatch();
            if (started != 0) return new Tick(started == 1 ? ST_WAIT_INPUT
                    : started == 2 ? ST_WAIT_MEDIUM : ST_OUTPUT_BLOCKED);
            access.progress(0);
            access.state(WORK);
        }
        if (access.eu() < EU_PER_TICK) return new Tick(ST_WAIT_EU);
        if (!access.consumeEu(EU_PER_TICK)) return new Tick(ST_WAIT_EU);
        long progress = access.progress() + 1;
        access.progress(progress);
        if (progress >= ticksFor(access.batch())) {
            access.state(DONE);
            return new Tick(ST_DONE);
        }
        return new Tick(ST_WORKING);
    }

    public static int batchFor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return BATCH_NONE;
        if (stack.is(Items.COAL)) return BATCH_COAL;
        GregTechMaterials.StackComposition composition = AddonItems.composition(stack);
        if (composition == null || !composition.present() || composition.units().size() != 1) return BATCH_NONE;
        if (!SOLID_FORMS.contains(composition.prefix())) return BATCH_NONE;
        return switch (composition.units().get(0).material()) {
            case "Coal" -> composition.units().get(0).amount() > 0 ? BATCH_COAL : BATCH_NONE;
            case "CoalCoke" -> composition.units().get(0).amount() > 0 ? BATCH_COKE : BATCH_NONE;
            default -> BATCH_NONE;
        };
    }

    /** A quarter-dust of slag per charge: the coarse residue the cleaning stage does not carry. */
    public static ItemStack residue() {
        return AddonItems.stack("dustSmall", "CoalSlag", 1);
    }

    public static void initialize(BehaviorState state) {
        if (state.getLong(K_FORMAT) != 0) return;
        state.setLong(K_FORMAT, FORMAT);
        state.setLong(K_STATE, IDLE);
        state.setLong(K_BATCH, BATCH_NONE);
        state.setLong(K_PROGRESS, 0);
        state.setLong(K_STATUS, ST_IDLE);
    }

    public static void tick(MachineBehaviorContext context) {
        BehaviorState state = context.state();
        initialize(state);
        Tick result = advance(new ContextAccess(context, state));
        int batch = (int) state.getLong(K_BATCH);
        long ticks = ticksFor(batch);
        state.setLong(K_STATUS, result.status());
        context.panel().setProgress(ticks <= 0 ? 0F : Math.min(1F, state.getLong(K_PROGRESS) / (float) ticks));
        context.panel().working(result.status() == ST_WORKING || result.status() == ST_DONE);
    }

    private static final class ContextAccess implements Access {
        private final MachineBehaviorContext context;
        private final BehaviorState state;

        ContextAccess(MachineBehaviorContext context, BehaviorState state) {
            this.context = context;
            this.state = state;
        }

        @Override public long eu() { return context.energy().mainStored(); }
        @Override public boolean consumeEu(long amount) { return context.energy().consumeMain(amount); }
        @Override public int state() { return (int) state.getLong(K_STATE); }
        @Override public void state(int value) { state.setLong(K_STATE, value); }
        @Override public int batch() { return (int) state.getLong(K_BATCH); }
        @Override public void batch(int value) { state.setLong(K_BATCH, value); }
        @Override public long progress() { return Math.max(0, state.getLong(K_PROGRESS)); }
        @Override public void progress(long value) { state.setLong(K_PROGRESS, Math.max(0, value)); }

        @Override public int startBatch() {
            ItemStack input = context.inventory().snapshot(INPUT_SLOT);
            int batch = batchFor(input);
            if (batch == BATCH_NONE) return 1;
            long steam = steamFor(batch);
            if (!CoalGasifierBehavior.isSteam(context.fluids().snapshot(MEDIUM_TANK))
                    || context.fluids().snapshot(MEDIUM_TANK).getAmount() < steam) return 2;
            if (rawRoom() < RAW_GAS) return 3;
            if (!acceptsResidue()) return 4;
            ItemStack consumed = input.copy();
            consumed.shrink(1);
            context.inventory().set(INPUT_SLOT, consumed.isEmpty() ? ItemStack.EMPTY : consumed);
            context.fluids().drain(MEDIUM_TANK, (int) steam, false);
            state.setLong(K_BATCH, batch);
            return 0;
        }

        @Override public boolean commitBatch() {
            ItemStack residue = residue();
            if (residue.isEmpty() || rawRoom() < RAW_GAS || !acceptsResidue()) return false;
            int filled = context.fluids().fill(RAW_GAS_TANK,
                    AddonFluids.still("GenGas_CrudeSyngas", (int) RAW_GAS), false);
            if (filled < RAW_GAS) return false;
            ItemStack existing = context.inventory().snapshot(RESIDUE_SLOT).copy();
            if (existing.isEmpty()) {
                context.inventory().set(RESIDUE_SLOT, residue.copy());
            } else {
                existing.grow(residue.getCount());
                context.inventory().set(RESIDUE_SLOT, existing);
            }
            return true;
        }

        private long rawRoom() {
            return context.fluids().capacity(RAW_GAS_TANK)
                    - context.fluids().snapshot(RAW_GAS_TANK).getAmount();
        }

        private boolean acceptsResidue() {
            ItemStack residue = residue();
            if (residue.isEmpty()) return false;
            ItemStack existing = context.inventory().snapshot(RESIDUE_SLOT);
            return existing.isEmpty()
                    || CoalGasifierBehavior.sameStack(existing, residue)
                    && existing.getCount() + residue.getCount() <= existing.getMaxStackSize();
        }
    }
}
