package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.MachineBehavior;
import gregtech.api.addon.platform.MachineBehaviorContext;
import gregtech.api.energy.EnergyType;
import gregtech.api.machine.MachineFace;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** Forge item comparison; the batch ledger is shared across loaders. */
public final class IrradiationReactorBehavior implements MachineBehavior {
    public static boolean sameStack(ItemStack a, ItemStack b) { return ItemStack.isSameItemSameTags(a, b); }

    @Override public void tick(MachineBehaviorContext context) { IrradiationReactorCore.tick(context); }

    @Override public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) {
        return IrradiationReactorCore.canInsert(slot, stack);
    }

    @Override public boolean canFill(MachineBehaviorContext context, int tank, FluidStack stack) { return false; }

    @Override public boolean acceptsEnergy(MachineBehaviorContext context, EnergyType type, MachineFace face,
                                           long size, long amount) {
        return EnergyType.KU.equals(type) && size > 0 && amount > 0;
    }
}
