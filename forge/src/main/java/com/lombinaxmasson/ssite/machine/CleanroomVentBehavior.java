package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.MachineBehavior;
import gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** Forge shell for the shared filter vent. */
public final class CleanroomVentBehavior implements MachineBehavior {
    @Override public void tick(MachineBehaviorContext context) { CleanroomVentCore.tick(context); }

    @Override public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) {
        return CleanroomVentCore.canInsert(slot, stack);
    }

    @Override public boolean canFill(MachineBehaviorContext context, int tank, FluidStack stack) {
        return false;
    }

    @Override public boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        return CleanroomVentCore.onUse(context, player, hand);
    }
}
