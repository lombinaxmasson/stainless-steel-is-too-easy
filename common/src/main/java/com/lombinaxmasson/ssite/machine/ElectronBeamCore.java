package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

/**
 * P-03B electron-beam direct write.
 *
 * <p>The template slot is a kept tool: the machine accepts only the finite {@link #TEMPLATE_OUTPUTS}
 * whitelist and never builds a recipe from client data. One exposed state is shared with the mask
 * path, so the following develop, etch, implant, anneal and packaging steps are identical and the
 * electron-beam route cannot skip them. The machine is still gated by the P-04 environment snapshot
 * because it replaces the same advanced pattern-transfer step.</p>
 */
public final class ElectronBeamCore {
    public static final String MACHINE_TYPE = "electron_beam_writer";
    public static final String MAP = "ssite.recipe.electron_beam";

    public static final int WAFER_SLOT = 0, TEMPLATE_SLOT = 1, OUTPUT_SLOT = 2;
    public static final long TICKS_PER_BATCH = 900, EU_PER_TICK = 1024;
    public static final int FORMAT = 1;

    public static final String INPUT_WAFER_ID = "ssite:advanced_wafer_advancedcoatedwafer";
    public static final String OUTPUT_WAFER_ID = "ssite:advanced_wafer_ebeamexposedwafer";
    public static final String TEMPLATE_ITEM_ID = "ssite:program_template_programtemplate";

    /** The complete pattern whitelist. Unknown templates never start a batch. */
    public static final Map<String, String> TEMPLATE_OUTPUTS = Map.of(
            TEMPLATE_ITEM_ID, OUTPUT_WAFER_ID);

    public static final int IDLE = 0, WORK = 1, DONE = 2;

    public static final int ST_IDLE = 0, ST_WORKING = 1, ST_ENV_INVALID = 2, ST_WAIT_INPUT = 3,
            ST_WAIT_TEMPLATE = 4, ST_WAIT_ENERGY = 5, ST_OUTPUT_BLOCKED = 6, ST_DONE = 7;

    public static final String K_FORMAT = "ebeam.format", K_STATE = "ebeam.state",
            K_PROGRESS = "ebeam.progress", K_STATUS = "ebeam.status", K_OUTPUT = "ebeam.output",
            K_ENV_VALID = "ebeam.env_valid", K_ENV_REASON = "ebeam.env_reason",
            K_ENV_REVISION = "ebeam.env_revision";

    private ElectronBeamCore() {}

    public interface Access {
        String waferId();
        String templateId();
        String output();
        void output(String value);
        boolean hasEnergy(long amount);
        boolean consumeEnergy(long amount);
        int state();
        void state(int value);
        long progress();
        void progress(long value);
        /** 0 on success, 1 for a wrong/missing wafer, 2 for a blocked output, 3 for an unknown template. */
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
            access.output("");
            return new Tick(ST_IDLE, 0);
        }
        if (!environment.acceptsAdvancedProcess()) {
            return new Tick(ST_ENV_INVALID, access.progress());
        }
        if (state == IDLE) {
            if (!TEMPLATE_OUTPUTS.containsKey(access.templateId())) return new Tick(ST_WAIT_TEMPLATE, 0);
            if (!INPUT_WAFER_ID.equals(access.waferId())) return new Tick(ST_WAIT_INPUT, 0);
            if (!access.hasEnergy(EU_PER_TICK)) return new Tick(ST_WAIT_ENERGY, 0);
            int started = access.startBatch();
            if (started == 1) return new Tick(ST_WAIT_INPUT, 0);
            if (started == 2) return new Tick(ST_OUTPUT_BLOCKED, 0);
            if (started == 3) return new Tick(ST_WAIT_TEMPLATE, 0);
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
        state.setString(K_OUTPUT, "");
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

    public static boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || !held.isEmpty()) return false;
        if (!context.level().isClientSide) {
            BehaviorState state = context.state();
            initialize(state);
            player.displayClientMessage(Component.translatable("ssite.electron_beam.report",
                    Component.translatable("ssite.electron_beam.status." + state.getLong(K_STATUS)),
                    Component.translatable("ssite.advanced_process.env." + state.getString(K_ENV_REASON)),
                    state.getLong(K_ENV_REVISION), state.getLong(K_PROGRESS), TICKS_PER_BATCH), true);
        }
        return true;
    }

    public static boolean canInsert(int slot, ItemStack stack) {
        if (slot == WAFER_SLOT) return INPUT_WAFER_ID.equals(itemId(stack));
        if (slot == TEMPLATE_SLOT) return TEMPLATE_OUTPUTS.containsKey(itemId(stack));
        return false;
    }

    static String itemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id == null ? "" : id.toString();
    }

    static ItemStack stackOf(String itemId, int count) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        Item item = id == null ? Items.AIR : BuiltInRegistries.ITEM.get(id);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, count);
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

        @Override public String waferId() { return itemId(context.inventory().snapshot(WAFER_SLOT)); }
        @Override public String templateId() { return itemId(context.inventory().snapshot(TEMPLATE_SLOT)); }
        @Override public String output() { return state.getString(K_OUTPUT); }
        @Override public void output(String value) { state.setString(K_OUTPUT, value == null ? "" : value); }
        @Override public boolean hasEnergy(long amount) { return context.energy().mainStored() >= amount; }
        @Override public boolean consumeEnergy(long amount) { return context.energy().consumeMain(amount); }
        @Override public int state() { return (int) state.getLong(K_STATE); }
        @Override public void state(int value) { state.setLong(K_STATE, value); }
        @Override public long progress() { return state.getLong(K_PROGRESS); }
        @Override public void progress(long value) { state.setLong(K_PROGRESS, Math.max(0, value)); }

        @Override public int startBatch() {
            String outputId = TEMPLATE_OUTPUTS.get(templateId());
            if (outputId == null) return 3;
            if (!INPUT_WAFER_ID.equals(waferId())) return 1;
            ItemStack product = stackOf(outputId, 1);
            if (product.isEmpty() || !grow(context.inventory().snapshot(OUTPUT_SLOT), product)) return 2;
            consume(WAFER_SLOT, 1);
            output(outputId);
            return 0;
        }

        @Override public boolean commitProduct() {
            ItemStack product = stackOf(output(), 1);
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
