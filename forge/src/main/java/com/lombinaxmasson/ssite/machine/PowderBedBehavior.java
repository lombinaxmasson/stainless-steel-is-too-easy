package com.lombinaxmasson.ssite.machine;

import com.lombinaxmasson.ssite.material.PowderManufacturingMaterials;
import gregtech.api.addon.platform.AddonFluids;
import gregtech.api.addon.platform.MachineBehavior;
import gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** Forge shell for the shared powder-bed ledger. */
public final class PowderBedBehavior implements MachineBehavior {
    @Override
    public void tick(MachineBehaviorContext context) {
        PowderBedCore.tick(context);
    }

    @Override
    public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) {
        if (slot == PowderBedCore.POWDER_SLOT) {
            return PowderBedCore.matches(stack, PowderBedCore.POWDER_PREFIX,
                    PowderManufacturingMaterials.ALLOY);
        }
        if (slot == PowderBedCore.MODULE_SLOT) {
            return PowderBedCore.matches(stack, PowderBedCore.MODULE_PREFIX,
                    PowderBedCore.MODULE_MATERIAL);
        }
        if (slot == PowderBedCore.COOLING_SLOT) {
            return PowderBedCore.matches(stack, PowderBedCore.COOLING_PREFIX,
                    PowderManufacturingMaterials.ALLOY);
        }
        return false;
    }

    @Override
    public boolean canFill(MachineBehaviorContext context, int tank, FluidStack stack) {
        return tank == PowderBedCore.ARGON_TANK && isArgon(stack);
    }

    @Override
    public boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        return PowderBedCore.onUse(context, player, hand);
    }

    private static boolean isArgon(FluidStack stack) {
        FluidStack expected = AddonFluids.still("GenGas_Argon", 1);
        return stack != null && !stack.isEmpty() && expected != null && !expected.isEmpty()
                && stack.getFluid() == expected.getFluid();
    }
}
