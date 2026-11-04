package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.AddonFluids;
import gregtech.api.addon.platform.AddonItems;
import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.MachineBehaviorContext;
import gregtech.api.energy.EnergyType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Fuel half of the chemical-looping pair. One ignited batch reserves a solid fuel and one hematite
 * unit, releases magnetite, ash, carbon dioxide and water, and spreads the fuel's host heat value
 * over its own progress and the air bed's regeneration batch. Carbon dioxide and water never stop
 * the bed: a real receiver gets them, an absent one vents them.
 */
public final class FuelReactorCore {
    public static final String MACHINE_TYPE = ChemicalLoopingCore.FUEL_TYPE;

    public static final int FUEL_SLOT = 0, CARRIER_SLOT = 1, ASH_SLOT = 2, OUTPUT_SLOT = 3;
    public static final int CO2_TANK = 0, WATER_TANK = 1;
    public static final int CO2_CAPACITY = 4_000, WATER_CAPACITY = 1_000;
    public static final int CO2_PER_BATCH = 1_000, WATER_PER_BATCH = 250;
    public static final int CO2_PUSH = 500, WATER_PUSH = 250;

    public static final long BATCH_TICKS = 200, KU_PER_TICK = 4, KU_CAPACITY = 3_200;
    public static final long HEAT_CAPACITY = 128_000, EMIT_PER_TICK = 512, MAX_HEAT_OUTPUT = 512;

    public static final int FORMAT = 1;
    public static final int IDLE = 0, WORK = 1, DONE = 2;
    public static final int BATCH_NONE = 0, BATCH_CHARCOAL = 1, BATCH_COKE = 2;
    public static final int ST_IDLE = 0, ST_NOT_LIT = 1, ST_NO_FUEL = 2, ST_NO_CARRIER = 3,
            ST_ASH_FULL = 4, ST_OUTPUT_BLOCKED = 5, ST_WAIT_KU = 6, ST_HEAT_FULL = 7,
            ST_WORKING = 8, ST_DONE = 9;

    public static final String K_FORMAT = "fuel_reactor.format", K_STATE = "fuel_reactor.state",
            K_BATCH = "fuel_reactor.batch", K_PROGRESS = "fuel_reactor.progress",
            K_STATUS = "fuel_reactor.status", K_LIT = "fuel_reactor.lit",
            K_STORED = "fuel_reactor.heat.stored", K_GENERATED = "fuel_reactor.heat.generated",
            K_OUTPUT = "fuel_reactor.heat.output", K_KU = "fuel_reactor.ku",
            K_CO2 = "fuel_reactor.co2", K_WATER = "fuel_reactor.water",
            K_RECEIVER = "fuel_reactor.receiver";

    private FuelReactorCore() {}

    /** Narrow boundary so the batch engine also runs without a Minecraft world. */
    public interface Access {
        boolean lit();
        long stored();
        long add(long amount);
        long emit(long amount);
        long ku();
        boolean consumeKu(long amount);
        int state();
        void state(int value);
        int batch();
        void batch(int value);
        long progress();
        void progress(long value);
        int tankAmount(int tank);
        int tankCapacity(int tank);
        int fillTank(int tank, int amount);
        int pushTank(int tank, int amount);
        void ventTank(int tank, int amount);
        boolean hasReceiver();
        /** 0 started; 1 no fuel; 2 no hematite; 3 ash slot full; 4 output slot blocked; 5 not lit. */
        int startBatch();
        boolean commitBatch();
    }

    public record Tick(int status, long generated, long emitted) {}

    public static long heatPerTick(int batch) {
        ChemicalLoopingCore.FuelKind kind = kind(batch);
        return kind == null ? 0 : kind.heat() / BATCH_TICKS;
    }

    public static ChemicalLoopingCore.FuelKind kind(int batch) {
        return switch (batch) {
            case BATCH_CHARCOAL -> ChemicalLoopingCore.FuelKind.CHARCOAL;
            case BATCH_COKE -> ChemicalLoopingCore.FuelKind.COKE;
            default -> null;
        };
    }

    public static int batchFor(ItemStack stack) {
        ChemicalLoopingCore.FuelKind kind = ChemicalLoopingCore.fuelKind(stack);
        if (kind == null) return BATCH_NONE;
        return kind == ChemicalLoopingCore.FuelKind.CHARCOAL ? BATCH_CHARCOAL : BATCH_COKE;
    }

    public static void initialize(BehaviorState state) {
        if (state.getLong(K_FORMAT) != 0) return;
        state.setLong(K_FORMAT, FORMAT);
        state.setLong(K_STATE, IDLE);
        state.setLong(K_BATCH, BATCH_NONE);
        state.setLong(K_PROGRESS, 0);
        state.setLong(K_STATUS, ST_IDLE);
    }

    public static Tick advance(Access access) {
        long emitted = emitHeat(access);
        service(access, CO2_TANK, CO2_PUSH);
        service(access, WATER_TANK, WATER_PUSH);
        if (access.state() == DONE) {
            if (!access.commitBatch()) return new Tick(ST_OUTPUT_BLOCKED, 0, emitted);
            access.state(IDLE);
            access.batch(BATCH_NONE);
            access.progress(0);
        }
        if (access.state() == IDLE) {
            int started = access.startBatch();
            if (started != 0) return new Tick(statusFor(started), 0, emitted);
            access.progress(0);
            access.state(WORK);
        }
        if (!access.lit()) return new Tick(ST_NOT_LIT, 0, emitted);
        long rate = heatPerTick(access.batch());
        if (rate <= 0) {
            access.state(IDLE);
            access.batch(BATCH_NONE);
            access.progress(0);
            return new Tick(ST_IDLE, 0, emitted);
        }
        if (access.ku() < KU_PER_TICK) return new Tick(ST_WAIT_KU, 0, emitted);
        if (HEAT_CAPACITY - access.stored() < rate) return new Tick(ST_HEAT_FULL, 0, emitted);
        if (!access.consumeKu(KU_PER_TICK)) return new Tick(ST_WAIT_KU, 0, emitted);
        long generated = access.add(rate);
        if (generated != rate) throw new IllegalStateException("Heat store accepted a partial batch tick");
        long progress = access.progress() + 1;
        access.progress(progress);
        if (progress >= BATCH_TICKS) {
            access.state(DONE);
            return new Tick(ST_DONE, generated, emitted);
        }
        return new Tick(ST_WORKING, generated, emitted);
    }

    private static int statusFor(int started) {
        return switch (started) {
            case 1 -> ST_NO_FUEL;
            case 2 -> ST_NO_CARRIER;
            case 3 -> ST_ASH_FULL;
            case 4 -> ST_OUTPUT_BLOCKED;
            default -> ST_NOT_LIT;
        };
    }

    private static long emitHeat(Access access) {
        long stored = Math.max(0, access.stored());
        if (stored <= 0) return 0;
        return access.emit(Math.min(stored, EMIT_PER_TICK));
    }

    /** A receiver drains continuously; without one the bed vents instead of building backpressure. */
    private static void service(Access access, int tank, int rate) {
        int amount = access.tankAmount(tank);
        if (amount <= 0) return;
        if (!access.hasReceiver()) access.ventTank(tank, amount);
        else access.pushTank(tank, Math.min(rate, amount));
    }

    /** Fills a bounded tank by venting its oldest contents first, so a full tank never stops a batch. */
    public static void fillWithoutBackpressure(Access access, int tank, int amount) {
        int room = access.tankCapacity(tank) - access.tankAmount(tank);
        int excess = amount - room;
        if (excess > 0) access.ventTank(tank, Math.min(excess, access.tankAmount(tank)));
        access.fillTank(tank, amount);
    }

    public static void tick(MachineBehaviorContext context) {
        BehaviorState state = context.state();
        initialize(state);
        Tick result = advance(new ContextAccess(context, state));
        state.setLong(K_STATUS, result.status());
        state.setLong(K_STORED, context.energy().mainStored());
        state.setLong(K_GENERATED, result.generated());
        state.setLong(K_OUTPUT, result.emitted());
        state.setLong(K_KU, context.energy().auxiliaryStored(EnergyType.KU));
        state.setLong(K_CO2, context.fluids().snapshot(CO2_TANK).getAmount());
        state.setLong(K_WATER, context.fluids().snapshot(WATER_TANK).getAmount());
        state.setBoolean(K_RECEIVER, FuelReactorBehavior.hasFluidReceiver(context));
        context.panel().setProgress(Math.min(1F, state.getLong(K_PROGRESS) / (float) BATCH_TICKS));
        context.panel().working(result.generated() > 0 || result.emitted() > 0);
    }

    /** Always consumes the click: the machine must never open its placeholder menu. */
    public static boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        BehaviorState state = context.state();
        initialize(state);
        ItemStack held = player.getItemInHand(hand);
        boolean server = !context.level().isClientSide;
        if (held.is(Items.FLINT_AND_STEEL)) {
            if (state.getBoolean(K_LIT)) {
                if (server) message(player, "ssite.fuel_reactor.already_lit");
            } else if (server) {
                state.setBoolean(K_LIT, true);
                message(player, "ssite.fuel_reactor.ignited");
            }
            return true;
        }
        if (held.isEmpty() && player.isShiftKeyDown()) {
            if (!server) return true;
            if (state.getBoolean(K_LIT)) {
                state.setBoolean(K_LIT, false);
                message(player, "ssite.fuel_reactor.extinguished");
            } else {
                message(player, "ssite.fuel_reactor.not_lit");
            }
            return true;
        }
        if (held.isEmpty()) {
            if (server) message(player, statusKey(state));
            return true;
        }
        if (ChemicalLoopingCore.fuelKind(held) != null) {
            if (server) load(context, player, hand, FUEL_SLOT);
            return true;
        }
        if (ChemicalLoopingCore.isHematite(held)) {
            if (server) load(context, player, hand, CARRIER_SLOT);
            return true;
        }
        if (server) message(player, statusKey(state));
        return true;
    }

    private static void load(MachineBehaviorContext context, Player player, InteractionHand hand, int slot) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack existing = context.inventory().snapshot(slot);
        if (!ChemicalLoopingCore.grow(existing, held)) {
            message(player, "ssite.fuel_reactor.load_blocked");
            return;
        }
        int moved = Math.min(held.getCount(), held.getMaxStackSize() - existing.getCount());
        if (moved <= 0) return;
        ItemStack one = held.copy();
        one.setCount(moved);
        context.inventory().set(slot, ChemicalLoopingCore.merged(existing, one));
        held.shrink(moved);
        message(player, "ssite.fuel_reactor.loaded", moved);
    }

    private static String statusKey(BehaviorState state) {
        return "ssite.fuel_reactor.status." + state.getLong(K_STATUS);
    }

    private static void message(Player player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args), true);
    }

    private static final class ContextAccess implements Access {
        private final MachineBehaviorContext context;
        private final BehaviorState state;

        ContextAccess(MachineBehaviorContext context, BehaviorState state) {
            this.context = context;
            this.state = state;
        }

        @Override public boolean lit() { return state.getBoolean(K_LIT); }
        @Override public long stored() { return context.energy().mainStored(); }
        @Override public long add(long amount) { return context.energy().generateMain(amount); }

        @Override public long emit(long amount) {
            BlockPos target = context.pos().relative(context.facing().getOpposite());
            if (!context.level().hasChunkAt(target)) return 0;
            var block = context.level().getBlockState(target).getBlock();
            if (!HeatTransportConnections.isType(block, HeatTransportMachines.DUCT)
                    && !HeatTransportConnections.isType(block, HeatTransportMachines.PLATE)) return 0;
            return context.energy().emitTo(target, context.facing(), 1, amount).transferredUnits();
        }

        @Override public long ku() { return context.energy().auxiliaryStored(EnergyType.KU); }
        @Override public boolean consumeKu(long amount) {
            return context.energy().consumeAuxiliary(EnergyType.KU, amount);
        }
        @Override public int state() { return (int) state.getLong(K_STATE); }
        @Override public void state(int value) { state.setLong(K_STATE, value); }
        @Override public int batch() { return (int) state.getLong(K_BATCH); }
        @Override public void batch(int value) { state.setLong(K_BATCH, value); }
        @Override public long progress() { return Math.max(0, state.getLong(K_PROGRESS)); }
        @Override public void progress(long value) { state.setLong(K_PROGRESS, Math.max(0, value)); }
        @Override public int tankAmount(int tank) { return context.fluids().snapshot(tank).getAmount(); }
        @Override public int tankCapacity(int tank) { return context.fluids().capacity(tank); }
        @Override public int fillTank(int tank, int amount) {
            return context.fluids().fill(tank, AddonFluids.still(tank == CO2_TANK ? "CarbonDioxide" : "DistW", amount), false);
        }
        @Override public int pushTank(int tank, int amount) {
            return FuelReactorBehavior.pushTank(context, tank, amount);
        }
        @Override public void ventTank(int tank, int amount) { context.fluids().drain(tank, amount, false); }
        @Override public boolean hasReceiver() { return FuelReactorBehavior.hasFluidReceiver(context); }

        @Override public int startBatch() {
            if (!lit()) return 5;
            int batch = batchFor(context.inventory().snapshot(FUEL_SLOT));
            if (batch == BATCH_NONE) return 1;
            if (!ChemicalLoopingCore.isHematite(context.inventory().snapshot(CARRIER_SLOT))) return 2;
            ItemStack ash = ChemicalLoopingCore.ash(kind(batch));
            ItemStack product = ChemicalLoopingCore.magnetite();
            if (ash.isEmpty() || product.isEmpty()) return 4;
            if (!ChemicalLoopingCore.grow(context.inventory().snapshot(ASH_SLOT), ash)) return 3;
            if (!ChemicalLoopingCore.grow(context.inventory().snapshot(OUTPUT_SLOT), product)) return 4;
            consume(FUEL_SLOT);
            consume(CARRIER_SLOT);
            state.setLong(K_BATCH, batch);
            return 0;
        }

        @Override public boolean commitBatch() {
            ChemicalLoopingCore.FuelKind kind = kind((int) state.getLong(K_BATCH));
            if (kind == null) return false;
            ItemStack ash = ChemicalLoopingCore.ash(kind);
            ItemStack product = ChemicalLoopingCore.magnetite();
            ItemStack ashSlot = context.inventory().snapshot(ASH_SLOT);
            ItemStack outSlot = context.inventory().snapshot(OUTPUT_SLOT);
            if (ash.isEmpty() || product.isEmpty()
                    || !ChemicalLoopingCore.grow(ashSlot, ash)
                    || !ChemicalLoopingCore.grow(outSlot, product)) return false;
            fillWithoutBackpressure(this, CO2_TANK, CO2_PER_BATCH);
            fillWithoutBackpressure(this, WATER_TANK, WATER_PER_BATCH);
            context.inventory().set(ASH_SLOT, ChemicalLoopingCore.merged(ashSlot, ash));
            context.inventory().set(OUTPUT_SLOT, ChemicalLoopingCore.merged(outSlot, product));
            return true;
        }

        private void consume(int slot) {
            ItemStack stack = context.inventory().snapshot(slot).copy();
            stack.shrink(1);
            context.inventory().set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
        }
    }
}
