package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.MachineBehaviorContext;
import gregtech.api.energy.EnergyType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Batch irradiation: a shielded vessel that converts sintered thorium pellets into a bred blanket batch. */
public final class IrradiationReactorCore {
    public static final String MACHINE_TYPE = "irradiation_reactor";
    public static final int TARGET_SLOT = 0, OUTPUT_SLOT = 1;
    /** One batch is four sintered pellets; the neutron source is a sealed part of the vessel. */
    public static final int BATCH_TARGETS = 4;
    public static final long TICKS_PER_TARGET = 300, BATCH_TICKS = BATCH_TARGETS * TICKS_PER_TARGET;
    public static final long KU_PER_TICK = 16, KU_CAPACITY = 6_400;
    public static final long HEAT_PER_TICK = 32, HEAT_CAPACITY = 8_000, EMIT_PER_TICK = 128;
    public static final long AMBIENT_K = 293, OVERHEAT_K = 1200, HEAT_RAISE_K = 4, COOL_K = 4;
    public static final int FORMAT = 1;

    public static final String K_FORMED = "structure.formed", K_STATE = "reactor.state",
            K_PROGRESS = "reactor.progress", K_TEMPERATURE = "reactor.temperature",
            K_STATUS = "reactor.status", K_DOWN = "reactor.down", K_STORED = "reactor.stored",
            K_GENERATED = "reactor.generated", K_EMITTED = "reactor.emitted",
            K_INITIALIZED = "reactor.initialized", K_FORMAT = "reactor.format";

    /** Machine states; the transient loading check always resolves inside one server tick. */
    public static final int IDLE = 0, WORK = 1, DONE = 2, FAULT = 3;
    /** Reported status, including why an idle or working machine is not progressing. */
    public static final int ST_IDLE = 0, ST_WORKING = 1, ST_WAIT_KU = 2, ST_WAIT_HEAT = 3,
            ST_DONE = 4, ST_FAULT = 5, ST_UNFORMED = 6, ST_WAIT_INPUT = 7, ST_OUTPUT_BLOCKED = 8;

    private static final ResourceLocation TARGET_ID = ResourceLocation.tryBuild("ssite", "dust_thoriumfuelpellet");
    private static final ResourceLocation BLANKET_ID = ResourceLocation.tryBuild("ssite", "dust_irradiatedthoriumblanket");

    private IrradiationReactorCore() {}

    /** Narrow boundary so the transition engine also runs without a Minecraft world. */
    public interface Access {
        boolean formed();
        long stored();
        long capacity();
        /** Stores generated heat and returns the accepted amount. */
        long add(long amount);
        /** Sends stored heat past the outlet and returns the amount actually taken. */
        long emit(long amount);
        long ku();
        boolean consumeKu(long amount);
        int state();
        void state(int value);
        long progress();
        void progress(long value);
        long temperature();
        void temperature(long kelvin);
        boolean down();
        void down(boolean value);
        /** 0 when the batch started, 1 for missing inputs, 2 when the output cannot take the product. */
        int startBatch();
        boolean commitProduct();
        boolean damageTooling();
    }

    public record Tick(int status, long generated, long emitted, long stored, long temperature) {}

    public static Tick advance(Access access) {
        long generated = 0, emitted = 0;
        if (!access.formed()) {
            // The destroyed shield must be observed missing before the fault can clear.
            if (access.state() == FAULT) access.down(true);
            long idle = cooled(access.temperature());
            access.temperature(idle);
            return new Tick(ST_UNFORMED, 0, 0, access.stored(), idle);
        }
        if (access.state() == FAULT) {
            if (!access.down()) return new Tick(ST_FAULT, 0, 0, access.stored(), access.temperature());
            access.state(IDLE);
            access.progress(0);
            access.temperature(AMBIENT_K);
            access.down(false);
        }
        // Heat leaves before new heat is added, so a connected sink keeps the buffer from filling.
        long stored = access.stored();
        if (stored > 0) emitted = access.emit(Math.min(EMIT_PER_TICK, stored));
        int state = access.state();
        if (state == DONE) {
            if (!access.commitProduct()) return idle(ST_OUTPUT_BLOCKED, emitted, access);
            access.state(IDLE);
            state = IDLE;
        }
        if (state == IDLE) {
            int started = access.startBatch();
            if (started != 0) return idle(started == 2 ? ST_OUTPUT_BLOCKED : ST_WAIT_INPUT, emitted, access);
            access.progress(0);
            access.state(WORK);
        }
        long temperature = access.temperature();
        if (access.ku() < KU_PER_TICK) {
            long cooled = cooled(temperature);
            access.temperature(cooled);
            return new Tick(ST_WAIT_KU, 0, emitted, access.stored(), cooled);
        }
        if (access.stored() >= access.capacity()) {
            long hot = temperature + HEAT_RAISE_K;
            access.temperature(hot);
            if (hot >= OVERHEAT_K) return overheat(access, emitted, hot);
            return new Tick(ST_WAIT_HEAT, 0, emitted, access.stored(), hot);
        }
        if (!access.consumeKu(KU_PER_TICK)) return new Tick(ST_WAIT_KU, 0, emitted, access.stored(), temperature);
        generated = access.add(HEAT_PER_TICK);
        temperature = generated < HEAT_PER_TICK ? temperature + HEAT_RAISE_K : cooled(temperature);
        access.temperature(temperature);
        if (temperature >= OVERHEAT_K) return overheat(access, emitted, temperature);
        long progress = access.progress() + 1;
        access.progress(progress);
        if (progress >= BATCH_TICKS) {
            access.state(DONE);
            return new Tick(ST_DONE, generated, emitted, access.stored(), temperature);
        }
        return new Tick(ST_WORKING, generated, emitted, access.stored(), temperature);
    }

    private static Tick idle(int status, long emitted, Access access) {
        long temperature = cooled(access.temperature());
        access.temperature(temperature);
        return new Tick(status, 0, emitted, access.stored(), temperature);
    }

    private static Tick overheat(Access access, long emitted, long temperature) {
        access.damageTooling();
        access.state(FAULT);
        access.progress(0);
        access.down(false);
        return new Tick(ST_FAULT, 0, emitted, access.stored(), temperature);
    }

    private static long cooled(long kelvin) {
        return Math.max(AMBIENT_K, kelvin - COOL_K);
    }

    public static void initialize(BehaviorState state) {
        if (state.getLong(K_FORMAT) != 0) return;
        state.setLong(K_FORMAT, FORMAT);
        state.setLong(K_STATE, IDLE);
        state.setLong(K_TEMPERATURE, AMBIENT_K);
        state.setLong(K_STATUS, ST_IDLE);
    }

    public static void tick(MachineBehaviorContext context) {
        BehaviorState state = context.state();
        initialize(state);
        Tick result = advance(new ContextAccess(context));
        state.setBoolean(K_FORMED, context.structure().formed());
        state.setLong(K_STATUS, result.status());
        state.setLong(K_STORED, result.stored());
        state.setLong(K_GENERATED, result.generated());
        state.setLong(K_EMITTED, result.emitted());
        state.setLong(K_TEMPERATURE, result.temperature());
        context.panel().setProgress(Math.min(1F, state.getLong(K_PROGRESS) / (float) BATCH_TICKS));
        context.panel().working(result.generated() > 0);
    }

    public static ItemStack product() {
        return addonStack(BLANKET_ID, BATCH_TARGETS);
    }

    /** Only the documented charge enters the machine; the output slot is machine-written. */
    public static boolean canInsert(int slot, ItemStack stack) {
        return slot == TARGET_SLOT && matches(stack, TARGET_ID);
    }

    public static boolean isTarget(ItemStack stack) {
        return matches(stack, TARGET_ID);
    }

    static boolean sameStack(ItemStack first, ItemStack second) {
        return IrradiationReactorBehavior.sameStack(first, second);
    }

    private static boolean matches(ItemStack stack, ResourceLocation id) {
        return stack != null && !stack.isEmpty() && id != null
                && id.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private static ItemStack addonStack(ResourceLocation id, int count) {
        Item item = id == null ? Items.AIR : BuiltInRegistries.ITEM.get(id);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, count);
    }

    static boolean grow(ItemStack existing, ItemStack addition) {
        return existing.isEmpty() || sameStack(existing, addition)
                && existing.getCount() + addition.getCount() <= existing.getMaxStackSize();
    }

    private static final class ContextAccess implements Access {
        private final MachineBehaviorContext context;
        private final BehaviorState state;

        ContextAccess(MachineBehaviorContext context) {
            this.context = context;
            this.state = context.state();
        }

        @Override public boolean formed() { return context.structure().formed(); }
        @Override public long stored() { return context.energy().mainStored(); }
        @Override public long capacity() { return HEAT_CAPACITY; }
        @Override public long add(long amount) { return context.energy().generateMain(amount); }

        @Override public long emit(long amount) {
            BlockPos target = IrradiationReactorStructure.heatPortTarget(context.pos(), context.facing());
            if (!context.level().hasChunkAt(target)) return 0;
            if (!HeatTransportConnections.isType(context.level().getBlockState(target).getBlock(),
                    HeatTransportMachines.DUCT)) return 0;
            return context.energy().emitTo(target, Direction.UP, 1, amount).transferredUnits();
        }

        @Override public long ku() { return context.energy().auxiliaryStored(EnergyType.KU); }
        @Override public boolean consumeKu(long amount) { return context.energy().consumeAuxiliary(EnergyType.KU, amount); }
        @Override public int state() { return (int) state.getLong(K_STATE); }
        @Override public void state(int value) { state.setLong(K_STATE, value); }
        @Override public long progress() { return state.getLong(K_PROGRESS); }
        @Override public void progress(long value) { state.setLong(K_PROGRESS, Math.max(0, value)); }
        @Override public long temperature() {
            long kelvin = state.getLong(K_TEMPERATURE);
            return kelvin <= 0 ? AMBIENT_K : kelvin;
        }
        @Override public void temperature(long kelvin) { state.setLong(K_TEMPERATURE, Math.max(AMBIENT_K, kelvin)); }
        @Override public boolean down() { return state.getBoolean(K_DOWN); }
        @Override public void down(boolean value) { state.setBoolean(K_DOWN, value); }

        @Override public int startBatch() {
            ItemStack targets = context.inventory().snapshot(TARGET_SLOT);
            if (!isTarget(targets) || targets.getCount() < BATCH_TARGETS) return 1;
            ItemStack product = product();
            if (product.isEmpty()) return 1;
            if (!grow(context.inventory().snapshot(OUTPUT_SLOT), product)) return 2;
            consume(TARGET_SLOT, BATCH_TARGETS);
            return 0;
        }

        @Override public boolean commitProduct() {
            ItemStack product = product();
            ItemStack out = context.inventory().snapshot(OUTPUT_SLOT);
            if (product.isEmpty() || !grow(out, product)) return false;
            context.inventory().set(OUTPUT_SLOT, merged(out, product));
            return true;
        }

        @Override public boolean damageTooling() {
            BlockPos shield = IrradiationReactorStructure.shield(context.pos(), context.facing());
            if (!context.level().hasChunkAt(shield)) return false;
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(context.level().getBlockState(shield).getBlock());
            if (!id.getNamespace().equals("ssite")
                    || !id.getPath().equals(IrradiationReactorStructure.CASING)) return false;
            context.level().destroyBlock(shield, false);
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
