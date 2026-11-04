package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.MachineBehaviorContext;
import gregtech.api.energy.EnergyType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Air half of the chemical-looping pair. Magnetite is re-oxidised to hematite against ambient air
 * represented by the host KU blower; a host {@code gt6ce:air_vent} item must be installed on the
 * vessel before the bed runs. Carrier wear is deterministic: one magnetite in every
 * {@link #WEAR_PERIOD} batches is lost instead of returning hematite, so ore or fresh carrier
 * powder has to make up the difference.
 */
public final class AirReactorCore {
    public static final String MACHINE_TYPE = ChemicalLoopingCore.AIR_TYPE;

    public static final int INPUT_SLOT = 0, OUTPUT_SLOT = 1;
    public static final long BATCH_TICKS = 100, KU_PER_TICK = 8, KU_CAPACITY = 3_200;
    public static final long HEAT_PER_BATCH = 16_000, HEAT_PER_TICK = HEAT_PER_BATCH / BATCH_TICKS;
    public static final long HEAT_CAPACITY = 64_000, EMIT_PER_TICK = 512, MAX_HEAT_OUTPUT = 512;
    public static final int WEAR_PERIOD = 64;

    public static final int FORMAT = 1;
    public static final int IDLE = 0, WORK = 1, DONE = 2;
    public static final int ST_IDLE = 0, ST_NO_VENT = 1, ST_NO_CARRIER = 2, ST_OUTPUT_BLOCKED = 3,
            ST_WAIT_KU = 4, ST_HEAT_FULL = 5, ST_WORKING = 6, ST_DONE = 7, ST_WEAR = 8;

    public static final String K_FORMAT = "air_reactor.format", K_STATE = "air_reactor.state",
            K_PROGRESS = "air_reactor.progress", K_STATUS = "air_reactor.status",
            K_VENT = "air_reactor.vent", K_BATCHES = "air_reactor.batches", K_WEAR = "air_reactor.wear",
            K_STORED = "air_reactor.heat.stored", K_GENERATED = "air_reactor.heat.generated",
            K_OUTPUT = "air_reactor.heat.output", K_KU = "air_reactor.ku";

    private AirReactorCore() {}

    /** Narrow boundary so the batch engine also runs without a Minecraft world. */
    public interface Access {
        boolean ventInstalled();
        long stored();
        long add(long amount);
        long emit(long amount);
        long ku();
        boolean consumeKu(long amount);
        int state();
        void state(int value);
        long progress();
        void progress(long value);
        long batches();
        void batches(long value);
        boolean wear();
        void wear(boolean value);
        /** 0 started; 1 no vent; 2 no magnetite; 3 output slot blocked. */
        int startBatch();
        boolean commitBatch();
    }

    public record Tick(int status, long generated, long emitted) {}

    public static void initialize(BehaviorState state) {
        if (state.getLong(K_FORMAT) != 0) return;
        state.setLong(K_FORMAT, FORMAT);
        state.setLong(K_STATE, IDLE);
        state.setLong(K_PROGRESS, 0);
        state.setLong(K_STATUS, ST_IDLE);
        state.setLong(K_BATCHES, 0);
        state.setBoolean(K_WEAR, false);
    }

    public static Tick advance(Access access) {
        long emitted = emitHeat(access);
        if (access.state() == DONE) {
            if (!access.commitBatch()) return new Tick(ST_OUTPUT_BLOCKED, 0, emitted);
            access.state(IDLE);
            access.progress(0);
            access.wear(false);
        }
        if (access.state() == IDLE) {
            int started = access.startBatch();
            if (started != 0) return new Tick(started == 1 ? ST_NO_VENT
                    : started == 2 ? ST_NO_CARRIER : ST_OUTPUT_BLOCKED, 0, emitted);
            access.progress(0);
            access.state(WORK);
        }
        if (!access.ventInstalled()) return new Tick(ST_NO_VENT, 0, emitted);
        if (access.ku() < KU_PER_TICK) return new Tick(ST_WAIT_KU, 0, emitted);
        if (HEAT_CAPACITY - access.stored() < HEAT_PER_TICK) return new Tick(ST_HEAT_FULL, 0, emitted);
        if (!access.consumeKu(KU_PER_TICK)) return new Tick(ST_WAIT_KU, 0, emitted);
        long generated = access.add(HEAT_PER_TICK);
        if (generated != HEAT_PER_TICK) throw new IllegalStateException("Heat store accepted a partial batch tick");
        long progress = access.progress() + 1;
        access.progress(progress);
        if (progress >= BATCH_TICKS) {
            access.state(DONE);
            return new Tick(access.wear() ? ST_WEAR : ST_DONE, generated, emitted);
        }
        return new Tick(ST_WORKING, generated, emitted);
    }

    private static long emitHeat(Access access) {
        long stored = Math.max(0, access.stored());
        if (stored <= 0) return 0;
        return access.emit(Math.min(stored, EMIT_PER_TICK));
    }

    /** Always consumes the click: the machine must never open its placeholder menu. */
    public static boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        BehaviorState state = context.state();
        initialize(state);
        ItemStack held = player.getItemInHand(hand);
        boolean server = !context.level().isClientSide;
        if (ChemicalLoopingCore.isAirVent(held)) {
            if (!server) return true;
            if (ventInstalled(state)) {
                message(player, "ssite.air_reactor.vent_present");
            } else {
                held.shrink(1);
                state.setString(K_VENT, sideOf(context.pos(), player).getName());
                message(player, "ssite.air_reactor.vent_installed");
            }
            return true;
        }
        if (held.isEmpty()) {
            if (server) message(player, statusKey(state));
            return true;
        }
        if (ChemicalLoopingCore.isMagnetite(held)) {
            if (server) load(context, player, hand);
            return true;
        }
        if (server) message(player, statusKey(state));
        return true;
    }

    /** The wrench removes the vent and blocks rotation; without a vent it keeps rotating the block. */
    public static boolean onWrench(MachineBehaviorContext context, Player player) {
        BehaviorState state = context.state();
        if (!ventInstalled(state)) return false;
        if (context.level().isClientSide) return true;
        state.setString(K_VENT, "");
        ItemStack vent = ChemicalLoopingCore.airVent();
        if (!vent.isEmpty() && !player.getInventory().add(vent)) player.drop(vent, false);
        message(player, "ssite.air_reactor.vent_removed");
        return true;
    }

    public static void beforeBreak(MachineBehaviorContext context) {
        BehaviorState state = context.state();
        initialize(state);
        if (!ventInstalled(state)) return;
        state.setString(K_VENT, "");
        ItemStack vent = ChemicalLoopingCore.airVent();
        if (vent.isEmpty()) return;
        ItemStack leftover = context.inventory().insertOutput(vent);
        if (!leftover.isEmpty()) playerDrop(context, leftover);
    }

    private static void playerDrop(MachineBehaviorContext context, ItemStack stack) {
        if (context.level() instanceof net.minecraft.server.level.ServerLevel server) {
            net.minecraft.world.entity.item.ItemEntity entity = new net.minecraft.world.entity.item.ItemEntity(
                    server, context.pos().getX() + .5, context.pos().getY() + .5, context.pos().getZ() + .5, stack);
            server.addFreshEntity(entity);
        }
    }

    private static void load(MachineBehaviorContext context, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack existing = context.inventory().snapshot(INPUT_SLOT);
        if (!ChemicalLoopingCore.grow(existing, held)) {
            message(player, "ssite.air_reactor.load_blocked");
            return;
        }
        int moved = Math.min(held.getCount(), held.getMaxStackSize() - existing.getCount());
        if (moved <= 0) return;
        ItemStack one = held.copy();
        one.setCount(moved);
        context.inventory().set(INPUT_SLOT, ChemicalLoopingCore.merged(existing, one));
        held.shrink(moved);
        message(player, "ssite.air_reactor.loaded", moved);
    }

    /** onUse has no clicked face, so the image uses the player's side as the installed face. */
    public static Direction sideOf(BlockPos pos, Player player) {
        double dx = player.getX() - pos.getX() - .5;
        double dz = player.getZ() - pos.getZ() - .5;
        if (Math.abs(dx) >= Math.abs(dz)) return dx >= 0 ? Direction.EAST : Direction.WEST;
        return dz >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    public static boolean ventInstalled(BehaviorState state) {
        return !state.getString(K_VENT).isEmpty();
    }

    private static String statusKey(BehaviorState state) {
        return "ssite.air_reactor.status." + state.getLong(K_STATUS);
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

        @Override public boolean ventInstalled() { return AirReactorCore.ventInstalled(state); }
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
        @Override public long progress() { return Math.max(0, state.getLong(K_PROGRESS)); }
        @Override public void progress(long value) { state.setLong(K_PROGRESS, Math.max(0, value)); }
        @Override public long batches() { return Math.max(0, state.getLong(K_BATCHES)); }
        @Override public void batches(long value) { state.setLong(K_BATCHES, Math.max(0, value)); }
        @Override public boolean wear() { return state.getBoolean(K_WEAR); }
        @Override public void wear(boolean value) { state.setBoolean(K_WEAR, value); }

        @Override public int startBatch() {
            if (!ventInstalled()) return 1;
            ItemStack input = context.inventory().snapshot(INPUT_SLOT);
            if (!ChemicalLoopingCore.isMagnetite(input)) return 2;
            long next = batches() + 1;
            boolean wear = next % WEAR_PERIOD == 0;
            ItemStack product = ChemicalLoopingCore.hematite();
            if (!wear && (product.isEmpty()
                    || !ChemicalLoopingCore.grow(context.inventory().snapshot(OUTPUT_SLOT), product))) return 3;
            ItemStack consumed = input.copy();
            consumed.shrink(1);
            context.inventory().set(INPUT_SLOT, consumed.isEmpty() ? ItemStack.EMPTY : consumed);
            batches(next);
            wear(wear);
            return 0;
        }

        @Override public boolean commitBatch() {
            if (wear()) return true;
            ItemStack product = ChemicalLoopingCore.hematite();
            ItemStack output = context.inventory().snapshot(OUTPUT_SLOT);
            if (product.isEmpty() || !ChemicalLoopingCore.grow(output, product)) return false;
            context.inventory().set(OUTPUT_SLOT, ChemicalLoopingCore.merged(output, product));
            return true;
        }
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
        context.panel().setProgress(Math.min(1F, state.getLong(K_PROGRESS) / (float) BATCH_TICKS));
        context.panel().working(result.generated() > 0 || result.emitted() > 0);
    }
}
