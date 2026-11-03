package com.lombinaxmasson.ssite.machine;

import com.lombinaxmasson.ssite.material.PowderManufacturingMaterials;
import gregtech.api.addon.platform.AddonItems;
import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * P-05 powder-bed batch ledger.
 *
 * <p>One batch consumes four classified powder, four thousand mB of argon and
 * {@link #TICKS_PER_BATCH} ticks of EU. All inputs and both output stacks are checked before the
 * powder is charged; the argon is then drained by progress, and the blank and the unsintered
 * reclaim powder settle separately when the batch reaches {@link #DONE}. A blocked blank or
 * reclaim output keeps the finished batch pending, so a reload never starts the batch again.</p>
 *
 * <p>The optional P-03 control module plus a P-05 cooling assembly in the two kept slots raise the
 * real batch count to two. The second batch is not a free multiplier: powder, argon, EU and both
 * output stacks are doubled for the same progress, and removing either part before the next batch
 * starts returns the machine to one batch.</p>
 *
 * <p>The core is free of Minecraft and GregTech types so the ledger runs on the JVM; the platform
 * behavior supplies item, fluid and energy access.</p>
 */
public final class PowderBedCore {
    public static final String MACHINE_TYPE = "powder_bed_printer";
    public static final String MAP = "ssite.recipe.powder_bed";

    public static final int POWDER_SLOT = 0, MODULE_SLOT = 1, COOLING_SLOT = 2,
            BLANK_SLOT = 3, RECLAIM_SLOT = 4, ARGON_TANK = 0;

    public static final int POWDER_PER_BATCH = 4;
    public static final int RECLAIM_PER_BATCH = 2;
    public static final int ARGON_PER_BATCH = 4000;
    public static final long TICKS_PER_BATCH = 400;
    public static final long EU_PER_TICK = 512;
    public static final int MAX_PARALLEL = 2;
    public static final int FORMAT = 1;

    public static final String POWDER_PREFIX = "classifiedPowder";
    public static final String BLANK_PREFIX = "printedBlank";
    public static final String RECLAIM_PREFIX = "reclaimPowder";
    public static final String MODULE_PREFIX = "controlModule";
    public static final String MODULE_MATERIAL = "PrecisionControlModule";
    public static final String COOLING_PREFIX = "coolingAssembly";

    public static final int IDLE = 0, WORK = 1, DONE = 2;

    public static final int ST_IDLE = 0, ST_WORKING = 1, ST_WAIT_POWDER = 2, ST_WAIT_ARGON = 3,
            ST_WAIT_ENERGY = 4, ST_OUTPUT_BLOCKED = 5, ST_DONE = 6;

    public static final String K_FORMAT = "powder_bed.format", K_STATE = "powder_bed.state",
            K_PROGRESS = "powder_bed.progress", K_STATUS = "powder_bed.status",
            K_ARGON_REMAINING = "powder_bed.argon_remaining", K_PARALLEL = "powder_bed.parallel",
            K_ARGON = "powder_bed.argon", K_RECLAIM = "powder_bed.reclaim",
            K_UPGRADE = "powder_bed.upgrade";

    private PowderBedCore() {}

    /** Narrow boundary so the batch ledger also runs without a Minecraft world. */
    public interface Access {
        int powderCount();
        int argon();
        boolean drainArgon(int amount);
        int blankSpace();
        int reclaimSpace();
        boolean hasUpgrade();
        boolean hasEnergy(long amount);
        boolean consumeEnergy(long amount);
        int state();
        void state(int value);
        long progress();
        void progress(long value);
        int argonRemaining();
        void argonRemaining(int value);
        int parallel();
        void parallel(int value);
        /** 0 on success, 1 for missing powder, 2 for a blocked output, 3 for missing argon. */
        int startBatch(int parallel);
        boolean commitProduct();
    }

    public record Tick(int status, long progress) {}

    public static Tick advance(Access access) {
        int state = access.state();
        if (state == DONE) {
            if (!access.commitProduct()) return new Tick(ST_OUTPUT_BLOCKED, access.progress());
            access.state(IDLE);
            access.progress(0);
            access.argonRemaining(0);
            access.parallel(0);
            return new Tick(ST_IDLE, 0);
        }
        if (state == WORK && access.parallel() <= 0) {
            access.state(IDLE);
            access.progress(0);
            access.argonRemaining(0);
            return new Tick(ST_IDLE, 0);
        }
        if (state == IDLE) {
            int parallel = access.hasUpgrade() ? MAX_PARALLEL : 1;
            if (access.powderCount() < POWDER_PER_BATCH * parallel)
                return new Tick(ST_WAIT_POWDER, 0);
            if (access.argon() < ARGON_PER_BATCH * parallel)
                return new Tick(ST_WAIT_ARGON, 0);
            if (!access.hasEnergy(EU_PER_TICK * parallel))
                return new Tick(ST_WAIT_ENERGY, 0);
            int started = access.startBatch(parallel);
            if (started == 1) return new Tick(ST_WAIT_POWDER, 0);
            if (started == 2) return new Tick(ST_OUTPUT_BLOCKED, 0);
            if (started == 3) return new Tick(ST_WAIT_ARGON, 0);
            access.parallel(parallel);
            access.argonRemaining(ARGON_PER_BATCH * parallel);
            access.state(WORK);
        }
        int parallel = Math.max(1, access.parallel());
        long energy = EU_PER_TICK * parallel;
        if (!access.hasEnergy(energy)) return new Tick(ST_WAIT_ENERGY, access.progress());
        int remaining = access.argonRemaining();
        if (remaining > 0) {
            int drain = Math.min(argonPerTick(parallel), remaining);
            if (!access.drainArgon(drain)) return new Tick(ST_WAIT_ARGON, access.progress());
            access.argonRemaining(remaining - drain);
        }
        if (!access.consumeEnergy(energy)) return new Tick(ST_WAIT_ENERGY, access.progress());
        long progress = access.progress() + 1;
        access.progress(progress);
        if (progress >= TICKS_PER_BATCH) {
            access.state(DONE);
            return new Tick(ST_DONE, progress);
        }
        return new Tick(ST_WORKING, progress);
    }

    static int argonPerTick(int parallel) {
        long total = (long) ARGON_PER_BATCH * parallel;
        return (int) Math.min(Integer.MAX_VALUE, (total + TICKS_PER_BATCH - 1) / TICKS_PER_BATCH);
    }

    public static boolean acceptsPowder(String prefix, String material) {
        return POWDER_PREFIX.equals(prefix) && PowderManufacturingMaterials.ALLOY.equals(material);
    }

    public static boolean acceptsModule(String prefix, String material) {
        return MODULE_PREFIX.equals(prefix) && MODULE_MATERIAL.equals(material);
    }

    public static boolean acceptsCooling(String prefix, String material) {
        return COOLING_PREFIX.equals(prefix) && PowderManufacturingMaterials.ALLOY.equals(material);
    }

    public static void initialize(BehaviorState state) {
        if (state.getLong(K_FORMAT) != 0) return;
        state.setLong(K_FORMAT, FORMAT);
        state.setLong(K_STATE, IDLE);
        state.setLong(K_PROGRESS, 0);
        state.setLong(K_STATUS, ST_IDLE);
        state.setLong(K_ARGON_REMAINING, 0);
        state.setLong(K_PARALLEL, 0);
        state.setLong(K_ARGON, 0);
        state.setLong(K_RECLAIM, 0);
        state.setBoolean(K_UPGRADE, false);
    }

    public static void tick(MachineBehaviorContext context) {
        BehaviorState state = context.state();
        initialize(state);
        Tick result = advance(new ContextAccess(context));
        state.setLong(K_STATUS, result.status());
        state.setLong(K_PROGRESS, result.progress());
        var argon = context.fluids().snapshot(ARGON_TANK);
        state.setLong(K_ARGON, argon == null || argon.isEmpty() ? 0 : argon.getAmount());
        state.setLong(K_RECLAIM, context.inventory().snapshot(RECLAIM_SLOT).getCount());
        state.setBoolean(K_UPGRADE, hasUpgrade(context));
        context.panel().setProgress(Math.min(1F, result.progress() / (float) TICKS_PER_BATCH));
        context.panel().working(result.status() == ST_WORKING);
    }

    /** Sneak-use with an empty hand prints the same pause reason the Jade tooltip shows. */
    public static boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || !held.isEmpty()) return false;
        if (!context.level().isClientSide) {
            BehaviorState state = context.state();
            initialize(state);
            player.displayClientMessage(Component.translatable("ssite.powder_bed.report",
                    Component.translatable("ssite.powder_bed.status." + state.getLong(K_STATUS)),
                    state.getLong(K_ARGON), state.getLong(K_RECLAIM),
                    state.getLong(K_PARALLEL)), true);
        }
        return true;
    }

    public static boolean matches(ItemStack stack, String prefix, String material) {
        if (stack == null || stack.isEmpty()) return false;
        ItemStack expected = AddonItems.stack(prefix, material, 1);
        return !expected.isEmpty() && ItemStack.isSameItem(stack, expected);
    }

    static boolean hasUpgrade(MachineBehaviorContext context) {
        return matches(context.inventory().snapshot(MODULE_SLOT), MODULE_PREFIX, MODULE_MATERIAL)
                && matches(context.inventory().snapshot(COOLING_SLOT), COOLING_PREFIX,
                PowderManufacturingMaterials.ALLOY);
    }

    private static final class ContextAccess implements Access {
        private final MachineBehaviorContext context;
        private final BehaviorState state;

        ContextAccess(MachineBehaviorContext context) {
            this.context = context;
            this.state = context.state();
        }

        @Override public int powderCount() {
            return context.inventory().snapshot(POWDER_SLOT).getCount();
        }

        @Override public int argon() {
            var stack = context.fluids().snapshot(ARGON_TANK);
            return stack == null || stack.isEmpty() ? 0 : stack.getAmount();
        }

        @Override public boolean drainArgon(int amount) {
            var drained = context.fluids().drain(ARGON_TANK, amount, false);
            return drained != null && !drained.isEmpty() && drained.getAmount() == amount;
        }

        @Override public int blankSpace() {
            return spaceFor(context.inventory().snapshot(BLANK_SLOT),
                    AddonItems.stack(BLANK_PREFIX, PowderManufacturingMaterials.ALLOY, 1));
        }

        @Override public int reclaimSpace() {
            return spaceFor(context.inventory().snapshot(RECLAIM_SLOT),
                    AddonItems.stack(RECLAIM_PREFIX, PowderManufacturingMaterials.ALLOY, 1));
        }

        @Override public boolean hasUpgrade() {
            return PowderBedCore.hasUpgrade(context);
        }

        @Override public boolean hasEnergy(long amount) { return context.energy().mainStored() >= amount; }
        @Override public boolean consumeEnergy(long amount) { return context.energy().consumeMain(amount); }
        @Override public int state() { return (int) state.getLong(K_STATE); }
        @Override public void state(int value) { state.setLong(K_STATE, value); }
        @Override public long progress() { return state.getLong(K_PROGRESS); }
        @Override public void progress(long value) { state.setLong(K_PROGRESS, Math.max(0, value)); }
        @Override public int argonRemaining() { return (int) state.getLong(K_ARGON_REMAINING); }
        @Override public void argonRemaining(int value) { state.setLong(K_ARGON_REMAINING, Math.max(0, value)); }
        @Override public int parallel() { return (int) state.getLong(K_PARALLEL); }
        @Override public void parallel(int value) { state.setLong(K_PARALLEL, Math.max(0, value)); }

        @Override public int startBatch(int parallel) {
            int needed = POWDER_PER_BATCH * parallel;
            if (powderCount() < needed) return 1;
            if (argon() < ARGON_PER_BATCH * parallel) return 3;
            if (blankSpace() < parallel || reclaimSpace() < RECLAIM_PER_BATCH * parallel) return 2;
            ItemStack powder = context.inventory().snapshot(POWDER_SLOT).copy();
            powder.shrink(needed);
            context.inventory().set(POWDER_SLOT, powder.isEmpty() ? ItemStack.EMPTY : powder);
            return 0;
        }

        @Override public boolean commitProduct() {
            int parallel = parallel();
            if (parallel <= 0) return false;
            ItemStack blank = AddonItems.stack(BLANK_PREFIX, PowderManufacturingMaterials.ALLOY, parallel);
            ItemStack reclaim = AddonItems.stack(RECLAIM_PREFIX, PowderManufacturingMaterials.ALLOY,
                    RECLAIM_PER_BATCH * parallel);
            ItemStack blankOut = context.inventory().snapshot(BLANK_SLOT);
            ItemStack reclaimOut = context.inventory().snapshot(RECLAIM_SLOT);
            if (blank.isEmpty() || reclaim.isEmpty() || !grow(blankOut, blank) || !grow(reclaimOut, reclaim))
                return false;
            context.inventory().set(BLANK_SLOT, merged(blankOut, blank));
            context.inventory().set(RECLAIM_SLOT, merged(reclaimOut, reclaim));
            return true;
        }

        private static int spaceFor(ItemStack existing, ItemStack addition) {
            if (addition.isEmpty()) return 0;
            if (existing.isEmpty()) return addition.getMaxStackSize();
            if (!ItemStack.isSameItem(existing, addition)) return 0;
            return Math.max(0, existing.getMaxStackSize() - existing.getCount());
        }

        private static boolean grow(ItemStack existing, ItemStack addition) {
            return existing.isEmpty() || ItemStack.isSameItem(existing, addition)
                    && existing.getCount() + addition.getCount() <= existing.getMaxStackSize();
        }

        private static ItemStack merged(ItemStack existing, ItemStack addition) {
            if (existing.isEmpty()) return addition.copy();
            ItemStack merged = existing.copy();
            merged.grow(addition.getCount());
            return merged;
        }
    }
}
