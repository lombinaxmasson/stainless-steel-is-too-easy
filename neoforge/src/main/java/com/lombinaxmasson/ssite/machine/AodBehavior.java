package com.lombinaxmasson.ssite.machine;

import com.gregtech.gregtech.api.addon.CrucibleMold;
import com.gregtech.gregtech.api.addon.MachineFace;
import com.gregtech.gregtech.api.addon.platform.AddonFluids;
import com.gregtech.gregtech.api.addon.platform.MachineBehavior;
import com.gregtech.gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/** NeoForge half of the AOD converter: the argon tank is the only loader-specific piece. */
public final class AodBehavior implements MachineBehavior {
    private final AodTier tier;
    private Fluid argon;

    public AodBehavior(AodTier tier) {
        this.tier = tier;
    }

    @Override
    public void tick(MachineBehaviorContext context) {
        AodConverterCore.tick(context, tier, new AodConverterCore.ArgonTank() {
            @Override
            public int availableMillibuckets() {
                FluidStack stack = context.fluid(AodConverterCore.ARGON_TANK);
                return stack.isEmpty() ? 0 : stack.getAmount();
            }

            @Override
            public boolean drainMillibuckets(int amount) {
                FluidStack drained = context.drain(AodConverterCore.ARGON_TANK, amount, true);
                if (drained.isEmpty() || drained.getAmount() < amount || !isArgon(drained)) return false;
                context.drain(AodConverterCore.ARGON_TANK, amount, false);
                return true;
            }
        });
    }

    @Override
    public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) {
        return AodConverterCore.canInsert(context, slot, stack);
    }

    /** Pipes only fill the charge slot through the open top; the menu is not the physical lid. */
    @Override
    public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack, MachineFace face) {
        if (face != null && face != MachineFace.TOP) return false;
        if (face == MachineFace.TOP && !AodConverterCore.lidOpen(context)) return false;
        return AodConverterCore.canInsert(context, slot, stack);
    }

    @Override
    public boolean canFill(MachineBehaviorContext context, int tank, FluidStack stack) {
        return AodConverterCore.canFill(context, tank) && isArgon(stack);
    }

    @Override
    public long getCrucibleTemperature(MachineBehaviorContext context) {
        return AodConverterCore.temperature(context);
    }

    @Override
    public long getCrucibleContentAmount(MachineBehaviorContext context) {
        return AodConverterCore.contentAmount(context);
    }

    @Override
    public boolean fillMoldAtSide(MachineBehaviorContext context, CrucibleMold mold, int crucibleSide, int moldSide) {
        return AodConverterCore.pour(context, mold, moldSide);
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
