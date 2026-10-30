package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.MachineBehavior;
import gregtech.api.addon.platform.MachineBehaviorContext;
import gregtech.api.energy.EnergyType;
import gregtech.api.machine.MachineFace;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** HU-only duct/plate behavior. Network accounting is common to both loaders. */
public final class HeatTransportBehavior implements MachineBehavior {
    @Override public void tick(MachineBehaviorContext context) { HeatTransportCore.tick(context); }
    @Override public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) { return false; }
    @Override public boolean canFill(MachineBehaviorContext context, int tank, FluidStack stack) { return false; }
    @Override public boolean acceptsEnergy(MachineBehaviorContext context, EnergyType type, MachineFace face,
                                           long size, long amount) {
        return EnergyType.HU.equals(type) && size > 0 && amount > 0
                && HeatTransportCore.canReceive(context.level(), context.pos());
    }
    @Override public boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        if (!context.level().isClientSide) {
            int status = HeatTransportCore.inspect(context.level(), context.pos()).status();
            player.displayClientMessage(Component.translatable("ssite.heat_transport.inspect",
                    context.energy().mainStored(), Component.translatable(HeatTransportCore.statusTranslationKey(status))), true);
        }
        return true;
    }
}
