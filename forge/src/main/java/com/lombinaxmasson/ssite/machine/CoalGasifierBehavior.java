package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.AddonFluids;
import gregtech.api.addon.platform.MachineBehavior;
import gregtech.api.addon.platform.MachineBehaviorContext;
import gregtech.api.energy.EnergyType;
import gregtech.api.machine.MachineFace;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** Forge half of the steam gasifier; the batch ledger is shared across loaders. */
public final class CoalGasifierBehavior implements MachineBehavior {
    public static boolean sameStack(ItemStack a, ItemStack b) { return ItemStack.isSameItemSameTags(a, b); }

    static boolean isSteam(FluidStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        FluidStack steam = AddonFluids.still("Steam", 1);
        return steam != null && !steam.isEmpty() && stack.getFluid() == steam.getFluid();
    }

    @Override public void tick(MachineBehaviorContext context) { CoalGasifierCore.tick(context); }

    @Override public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) {
        return slot == CoalGasifierCore.INPUT_SLOT && CoalGasifierCore.batchFor(stack) != CoalGasifierCore.BATCH_NONE;
    }

    @Override public boolean canFill(MachineBehaviorContext context, int tank, FluidStack stack) {
        return tank == CoalGasifierCore.MEDIUM_TANK && isSteam(stack);
    }

    @Override public boolean acceptsEnergy(MachineBehaviorContext context, EnergyType type, MachineFace face,
                                           long size, long amount) {
        return EnergyType.EU.equals(type) && size > 0 && amount > 0;
    }
}
