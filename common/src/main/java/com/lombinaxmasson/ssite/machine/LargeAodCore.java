package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Large AOD: nine-cell HU sole and piped oxygen/argon instead of a KU blower. */
public final class LargeAodCore extends AodConverterCore {
    public interface GasSupply extends ArgonTank {
        int oxygenMillibuckets();
        boolean drainOxygen(int amount);
    }

    public LargeAodCore(AodTier tier) { super(tier); }

    @Override public void tick(MachineBehaviorContext context, ArgonTank argon) {
        context.state().setBoolean("structure.formed", context.structureFormed());
        context.state().setLong("pool.amount", contentAmount(context));
        super.tick(context, argon);
    }

    @Override protected long heatPerKelvin() { return 9L * tier.heatPerKelvin(); }
    @Override protected float explosionPower() { return 4.0F; }

    @Override protected void flushSlag(MachineBehaviorContext context, BehaviorState state) {
        super.flushSlag(context, state);
        ItemStack output = context.item(1);
        if (output.isEmpty()) return;
        for (var part : context.parts()) {
            var lid = part.context();
            if (!part.name().equals(LargeAodStructure.LID) || lid == null || part.slots() < 2 || !lid.item(1).isEmpty()) continue;
            lid.setItem(1, output.copy());
            context.setItem(1, ItemStack.EMPTY);
            break;
        }
    }

    @Override protected void supplyGas(MachineBehaviorContext context, ArgonTank argon,
                                       boolean open, long temperature) {
        if (argon instanceof GasSupply gases) supply(context.state(), gases, open, temperature);
    }

    /** A closed dilution unit contains 10 mB O2 + 10 mB Ar; an open oxygen unit is 10 mB O2. */
    static void supply(BehaviorState state, GasSupply gases, boolean open, long temperature) {
        if (temperature < AOD_K) return;
        if (!open) {
            int units = (int) Math.min(MAX_GAS_UNITS - state.getLong(K_ARGON),
                    Math.min(gases.availableMillibuckets(), gases.oxygenMillibuckets()) / MB_PER_GAS_UNIT);
            if (units > 0 && gases.drainMillibuckets(units * MB_PER_GAS_UNIT)
                    && gases.drainOxygen(units * MB_PER_GAS_UNIT))
                state.setLong(K_ARGON, state.getLong(K_ARGON) + units);
        }
        // Oxygen alone can also drive the lower-yield refining row, including with the lid open.
        int oxygen = (int) Math.min(MAX_GAS_UNITS - state.getLong(K_AIR),
                gases.oxygenMillibuckets() / MB_PER_GAS_UNIT);
        if (oxygen > 0 && gases.drainOxygen(oxygen * MB_PER_GAS_UNIT))
            state.setLong(K_AIR, state.getLong(K_AIR) + oxygen);
    }

    @Override protected List<BlockPos> failurePositions(MachineBehaviorContext context) {
        List<BlockPos> positions = new ArrayList<>();
        // Casings and native HU relay parts are not returned by context.parts(). Snapshot all owned cells.
        for (var cell : LargeAodStructure.layout(tier).cells()) {
            if (!(cell.match() instanceof gregtech.api.multiblock.CellMatch.Part part)) continue;
            BlockPos pos = LargeAodStructure.world(context.pos(), context.facing(), cell.right(), cell.up(), cell.back());
            if (!context.level().hasChunkAt(pos)) continue;
            var id = BuiltInRegistries.BLOCK.getKey(context.level().getBlockState(pos).getBlock());
            if (id.getNamespace().equals("ssite") && id.getPath().equals(part.name())) positions.add(pos);
        }
        return positions;
    }
}
