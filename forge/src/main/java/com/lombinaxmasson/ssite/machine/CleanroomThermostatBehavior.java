package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.AddonFluids;
import gregtech.api.addon.platform.MachineBehavior;
import gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** Forge shell for the shared thermostat; only ultrapure water is accepted as coolant. */
public final class CleanroomThermostatBehavior implements MachineBehavior {
    @Override public void tick(MachineBehaviorContext context) { CleanroomThermostatCore.tick(context); }

    @Override public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) {
        return false;
    }

    @Override public boolean canFill(MachineBehaviorContext context, int tank, FluidStack stack) {
        if (tank != CleanroomThermostatCore.TANK) return false;
        FluidStack expected = AddonFluids.still(CleanroomThermostatCore.COOLANT_FIELD, 1);
        return stack != null && !stack.isEmpty() && expected != null && !expected.isEmpty()
                && stack.getFluid() == expected.getFluid();
    }

    @Override public boolean onUse(MachineBehaviorContext context, Player player, InteractionHand hand) {
        return CleanroomThermostatCore.onUse(context, player, hand);
    }
}
