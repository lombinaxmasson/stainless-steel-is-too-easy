package com.lombinaxmasson.ssite.machine;

import gregtech.api.energy.EnergyType;
import gregtech.api.addon.platform.AddonTool;
import gregtech.api.addon.platform.MachineBehavior;
import gregtech.api.addon.platform.MachineBehaviorContext;
import gregtech.api.machine.MachineFace;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** Forge item comparison; fuel values and the conserved process are shared across loaders. */
public final class FluidizedBedBehavior implements MachineBehavior {
    private static FluidizedBedCore.Fuel resolve(ItemStack stack) {
        return FluidizedBedCore.fuel(stack);
    }
    public static boolean sameStack(ItemStack a, ItemStack b) { return ItemStack.isSameItemSameTags(a, b); }
    @Override public void tick(MachineBehaviorContext context) { FluidizedBedCore.tick(context, FluidizedBedBehavior::resolve); }
    @Override public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) {
        return slot == FluidizedBedCore.FUEL_SLOT && resolve(stack) != null;
    }
    @Override public boolean canFill(MachineBehaviorContext context, int tank, FluidStack stack) { return false; }
    @Override public boolean acceptsEnergy(MachineBehaviorContext context, EnergyType type, MachineFace face, long size, long amount) {
        return EnergyType.KU.equals(type) && size > 0 && amount > 0;
    }
    @Override public boolean onTool(MachineBehaviorContext context, AddonTool tool, Player player,
                                    InteractionHand hand, MachineFace face) {
        if (tool != AddonTool.SCREWDRIVER) return false;
        FluidizedBedCore.initialize(context.state());
        int power = FluidizedBedCore.power(context.state().getLong(FluidizedBedCore.K_POWER)) % 4 + 1;
        context.state().setLong(FluidizedBedCore.K_POWER, power);
        player.displayClientMessage(Component.translatable("ssite.fluidized_bed.power", power,
                FluidizedBedCore.outputRate(power), FluidizedBedCore.kuPerTick(power)), true);
        return true;
    }
}
