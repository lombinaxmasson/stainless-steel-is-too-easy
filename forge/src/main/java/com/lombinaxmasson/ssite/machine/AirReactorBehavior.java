package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.AddonTool;
import gregtech.api.addon.platform.MachineBehavior;
import gregtech.api.addon.platform.MachineBehaviorContext;
import gregtech.api.energy.EnergyType;
import gregtech.api.machine.MachineFace;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** Forge half of the chemical-looping air bed; the regeneration ledger is shared across loaders. */
public final class AirReactorBehavior implements MachineBehavior {
    @Override public void tick(MachineBehaviorContext context) { AirReactorCore.tick(context); }

    @Override public boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        return AirReactorCore.onUse(context, player, hand);
    }

    @Override public boolean onTool(MachineBehaviorContext context, AddonTool tool, Player player,
                                   InteractionHand hand, MachineFace face) {
        return tool == AddonTool.WRENCH && AirReactorCore.onWrench(context, player);
    }

    @Override public void beforeBreak(MachineBehaviorContext context) { AirReactorCore.beforeBreak(context); }

    @Override public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) {
        return slot == AirReactorCore.INPUT_SLOT && ChemicalLoopingCore.isMagnetite(stack);
    }

    @Override public boolean canFill(MachineBehaviorContext context, int tank, FluidStack stack) { return false; }

    @Override public boolean acceptsEnergy(MachineBehaviorContext context, EnergyType type, MachineFace face,
                                           long size, long amount) {
        return EnergyType.KU.equals(type) && size > 0 && amount > 0;
    }
}
