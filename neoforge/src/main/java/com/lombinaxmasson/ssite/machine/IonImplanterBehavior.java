package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.AddonFluids;
import gregtech.api.addon.platform.MachineBehavior;
import gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/** NeoForge shell for the shared ion-implanter ledger. */
public final class IonImplanterBehavior implements MachineBehavior {
    @Override
    public void tick(MachineBehaviorContext context) {
        IonImplanterCore.tick(context);
    }

    @Override
    public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) {
        return IonImplanterCore.canInsert(slot, stack);
    }

    @Override
    public boolean canFill(MachineBehaviorContext context, int tank, FluidStack stack) {
        return tank == IonImplanterCore.ARGON_TANK && isArgon(stack);
    }

    @Override
    public boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        return IonImplanterCore.onUse(context, player, hand);
    }

    private static boolean isArgon(FluidStack stack) {
        FluidStack expected = AddonFluids.still("GenGas_Argon", 1);
        return stack != null && !stack.isEmpty() && expected != null && !expected.isEmpty()
                && stack.getFluid() == expected.getFluid();
    }
}
