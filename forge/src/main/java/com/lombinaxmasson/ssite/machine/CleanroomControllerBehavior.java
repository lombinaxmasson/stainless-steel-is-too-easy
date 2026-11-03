package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.MachineBehavior;
import gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** Forge shell for the shared cleanroom controller. */
public final class CleanroomControllerBehavior implements MachineBehavior {
    @Override public void tick(MachineBehaviorContext context) { CleanroomControllerCore.tick(context); }

    @Override public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) {
        return false;
    }

    @Override public boolean canFill(MachineBehaviorContext context, int tank, FluidStack stack) {
        return false;
    }

    @Override public boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        return CleanroomControllerCore.onUse(context, player, hand);
    }

    @Override public void beforeBreak(MachineBehaviorContext context) {
        if (!context.level().isClientSide) CleanroomEnvironment.withdraw(context.level(), context.pos());
    }
}
