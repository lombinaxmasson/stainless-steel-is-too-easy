package com.lombinaxmasson.ssite.machine;

import com.gregtech.gregtech.api.addon.CrucibleMold;
import com.gregtech.gregtech.api.addon.MachineFace;
import com.gregtech.gregtech.api.addon.platform.AddonFluids;
import com.gregtech.gregtech.api.addon.platform.MachineBehavior;
import com.gregtech.gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

/** Forge half of the VAR arc furnace: the argon tank is the only loader-specific piece. */
public final class VarBehavior implements MachineBehavior {
    private Fluid argon;

    @Override
    public void tick(MachineBehaviorContext context) {
        VarFurnaceCore.tick(context, new VarFurnaceCore.ArgonTank() {
            @Override
            public int availableMillibuckets() {
                FluidStack stack = context.fluid(VarFurnaceCore.ARGON_TANK);
                return stack.isEmpty() ? 0 : stack.getAmount();
            }

            @Override
            public boolean drainMillibuckets(int amount) {
                FluidStack drained = context.drain(VarFurnaceCore.ARGON_TANK, amount, true);
                if (drained.isEmpty() || drained.getAmount() < amount || !isArgon(drained)) return false;
                context.drain(VarFurnaceCore.ARGON_TANK, amount, false);
                return true;
            }
        });
    }

    @Override
    public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) {
        return VarFurnaceCore.canInsert(context, slot, stack);
    }

    /** Pipes only fill the charge slot through the open top; the menu is not the physical lid. */
    @Override
    public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack, MachineFace face) {
        if (face != null && face != MachineFace.TOP) return false;
        if (face == MachineFace.TOP && !VarFurnaceCore.lidOpen(context)) return false;
        return VarFurnaceCore.canInsert(context, slot, stack);
    }

    @Override
    public boolean canFill(MachineBehaviorContext context, int tank, FluidStack stack) {
        return VarFurnaceCore.canFill(context, tank) && isArgon(stack);
    }

    @Override
    public long getCrucibleTemperature(MachineBehaviorContext context) {
        return VarFurnaceCore.temperature(context);
    }

    @Override
    public long getCrucibleContentAmount(MachineBehaviorContext context) {
        return VarFurnaceCore.contentAmount(context);
    }

    @Override
    public boolean fillMoldAtSide(MachineBehaviorContext context, CrucibleMold mold, int crucibleSide, int moldSide) {
        return VarFurnaceCore.pour(context, mold, moldSide);
    }

    private boolean isArgon(FluidStack stack) {
        Fluid expected = argon();
        return stack != null && !stack.isEmpty() && expected != null && stack.getFluid() == expected;
    }

    private Fluid argon() {
        if (argon == null) {
            FluidStack stack = AddonFluids.still("GenGas_Argon", 1);
            if (stack != null && !stack.isEmpty()) argon = stack.getFluid();
        }
        return argon;
    }
}
