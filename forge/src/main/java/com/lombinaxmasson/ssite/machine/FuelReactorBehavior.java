package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.MachineBehavior;
import gregtech.api.addon.platform.MachineBehaviorContext;
import gregtech.api.energy.EnergyType;
import gregtech.api.machine.MachineFace;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** Forge half of the chemical-looping fuel bed; the batch ledger is shared across loaders. */
public final class FuelReactorBehavior implements MachineBehavior {
    static int pushTank(MachineBehaviorContext context, int tank, int amount) {
        return ChemicalLoopingBehavior.pushTank(context, tank, amount);
    }

    static boolean hasFluidReceiver(MachineBehaviorContext context) {
        return ChemicalLoopingBehavior.hasFluidReceiver(context);
    }

    @Override public void tick(MachineBehaviorContext context) { FuelReactorCore.tick(context); }

    @Override public boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        return FuelReactorCore.onUse(context, player, hand);
    }

    @Override public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) {
        if (slot == FuelReactorCore.FUEL_SLOT) return FuelReactorCore.batchFor(stack) != FuelReactorCore.BATCH_NONE;
        return slot == FuelReactorCore.CARRIER_SLOT && ChemicalLoopingCore.isHematite(stack);
    }

    @Override public boolean canFill(MachineBehaviorContext context, int tank, FluidStack stack) { return false; }

    @Override public boolean acceptsEnergy(MachineBehaviorContext context, EnergyType type, MachineFace face,
                                           long size, long amount) {
        return EnergyType.KU.equals(type) && size > 0 && amount > 0;
    }
}
