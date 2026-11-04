package com.lombinaxmasson.ssite.machine;

import com.lombinaxmasson.ssite.worldgen.GasFieldPlan;
import gregtech.api.addon.platform.AddonFluids;
import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.MachineBehavior;
import gregtech.api.addon.platform.MachineBehaviorContext;
import gregtech.api.energy.EnergyType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/** NeoForge half of the KU drilling rig; the field budget and plan are loader-neutral. */
public final class GasFieldExtractorBehavior implements MachineBehavior {
    private static final int OUT = 0;

    @Override
    public void tick(MachineBehaviorContext context) {
        BehaviorState state = context.state();
        if (!context.structure().formed()) {
            state.setInt(GasFieldExtractorCore.K_FORMED, 0);
            state.setInt(GasFieldExtractorCore.K_STATUS, GasFieldExtractorCore.ST_UNFORMED);
            context.panel().working(false);
            return;
        }
        state.setInt(GasFieldExtractorCore.K_FORMED, 1);

        GasFieldPlan plan = GasFieldPlan.at(context.level(), context.pos());
        if (plan == null) {
            state.setInt(GasFieldExtractorCore.K_STATUS, GasFieldExtractorCore.ST_NO_FIELD);
            context.panel().working(false);
            return;
        }

        long now = context.level().getGameTime();
        long scanned = state.getLong(GasFieldExtractorCore.K_SCAN_TICK);
        int wells = state.getInt(GasFieldExtractorCore.K_WELLS);
        if (scanned <= 0 || now - scanned >= GasFieldExtractorCore.RESCAN_TICKS) {
            wells = GasFieldExtractorCore.countWells(context.level(), context.pos());
            state.setInt(GasFieldExtractorCore.K_WELLS, wells);
            state.setLong(GasFieldExtractorCore.K_SCAN_TICK, now);
        }
        if (wells <= 0) {
            state.setInt(GasFieldExtractorCore.K_STATUS, GasFieldExtractorCore.ST_NO_WELLS);
            context.panel().working(false);
            return;
        }
        if (context.energy().mainStored() < GasFieldExtractorCore.KU_PER_TICK) {
            state.setInt(GasFieldExtractorCore.K_STATUS, GasFieldExtractorCore.ST_WAIT_KU);
            context.panel().working(false);
            return;
        }

        FluidStack gas = AddonFluids.still(GasFieldExtractorCore.RAW_GAS, 1);
        if (gas == null || gas.isEmpty()) {
            state.setInt(GasFieldExtractorCore.K_STATUS, GasFieldExtractorCore.ST_NO_FIELD);
            context.panel().working(false);
            return;
        }
        int wanted = GasFieldExtractorCore.wanted(wells, plan, Integer.MAX_VALUE);
        if (wanted <= 0) {
            state.setInt(GasFieldExtractorCore.K_STATUS, GasFieldExtractorCore.ST_QUOTA);
            context.panel().working(false);
            return;
        }
        int room = context.fluids().fill(OUT, AddonFluids.still(GasFieldExtractorCore.RAW_GAS, wanted), true);
        int amount = Math.min(wanted, room);
        if (amount <= 0) {
            state.setInt(GasFieldExtractorCore.K_STATUS, GasFieldExtractorCore.ST_OUTPUT_BLOCKED);
            context.panel().working(false);
            return;
        }
        if (!GasFieldLedger.request(context.level(), context.pos(), amount)) {
            state.setInt(GasFieldExtractorCore.K_STATUS, GasFieldExtractorCore.ST_QUOTA);
            state.setInt(GasFieldExtractorCore.K_BUDGET, GasFieldLedger.remaining(context.level(), context.pos()));
            context.panel().working(false);
            return;
        }
        if (!context.energy().consumeMain(GasFieldExtractorCore.KU_PER_TICK)) {
            state.setInt(GasFieldExtractorCore.K_STATUS, GasFieldExtractorCore.ST_WAIT_KU);
            context.panel().working(false);
            return;
        }
        context.fluids().fill(OUT, AddonFluids.still(GasFieldExtractorCore.RAW_GAS, amount), false);
        int pushed = context.world().pushCompressedGas(Direction.UP, AddonFluids.still(GasFieldExtractorCore.RAW_GAS, amount));
        if (pushed > 0) context.fluids().drain(OUT, pushed, false);
        state.setInt(GasFieldExtractorCore.K_STATUS, GasFieldExtractorCore.ST_WORKING);
        state.setInt(GasFieldExtractorCore.K_BUDGET, GasFieldLedger.remaining(context.level(), context.pos()));
        context.panel().working(true);
    }

    @Override
    public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) {
        return false;
    }

    @Override
    public boolean canFill(MachineBehaviorContext context, int tank, FluidStack stack) {
        return false;
    }

    @Override
    public boolean acceptsEnergy(MachineBehaviorContext context, EnergyType type,
                                 gregtech.api.machine.MachineFace face, long size, long amount) {
        return EnergyType.KU.equals(type) && size > 0 && amount > 0;
    }
}
