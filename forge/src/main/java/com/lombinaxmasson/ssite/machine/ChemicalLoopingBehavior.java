package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/** Forge half of the shared item comparison and the fuel bed's real fluid outlet. */
public final class ChemicalLoopingBehavior {
    private ChemicalLoopingBehavior() {}

    public static boolean sameStack(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameTags(a, b);
    }

    static boolean hasFluidReceiver(MachineBehaviorContext context) {
        return receiver(context) != null;
    }

    static int pushTank(MachineBehaviorContext context, int tank, int amount) {
        if (context.level().isClientSide || amount <= 0) return 0;
        IFluidHandler target = receiver(context);
        if (target == null) return 0;
        FluidStack stack = context.fluids().drain(tank, amount, true);
        if (stack.isEmpty()) return 0;
        int accepted = target.fill(stack, IFluidHandler.FluidAction.EXECUTE);
        if (accepted <= 0) return 0;
        context.fluids().drain(tank, accepted, false);
        return accepted;
    }

    private static IFluidHandler receiver(MachineBehaviorContext context) {
        Direction side = context.facing().getClockWise();
        BlockPos target = context.pos().relative(side);
        if (!context.level().hasChunkAt(target)) return null;
        BlockEntity entity = context.level().getBlockEntity(target);
        return entity == null ? null
                : entity.getCapability(ForgeCapabilities.FLUID_HANDLER, side.getOpposite()).resolve().orElse(null);
    }
}
