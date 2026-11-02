package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.MachineBehavior;
import gregtech.api.addon.platform.MachineBehaviorContext;
import gregtech.api.signal.GtSignalCarrier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** Forge shell for the shared fiber probe snapshot engine. */
public final class FiberProbeBehavior implements MachineBehavior {
    @Override
    public void tick(MachineBehaviorContext context) {
        FiberProbeContext.tick(context);
    }

    @Override
    public long gtSignal(MachineBehaviorContext context, net.minecraft.core.Direction side) {
        return FiberProbeContext.signal(context) * GtSignalCarrier.UNIT;
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
    public boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        return FiberProbeContext.onUse(context, player, hand);
    }
}
