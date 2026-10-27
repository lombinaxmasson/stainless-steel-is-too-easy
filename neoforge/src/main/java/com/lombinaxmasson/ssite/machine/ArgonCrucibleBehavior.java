package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.recipe.CrucibleMold;
import gregtech.api.machine.MachineFace;
import gregtech.api.addon.platform.AddonFluids;
import gregtech.api.addon.platform.AddonTool;
import gregtech.api.addon.platform.MachineBehavior;
import gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/** NeoForge half of an argon-fed crucible: the argon tank is the only loader-specific piece. */
public final class ArgonCrucibleBehavior implements MachineBehavior, MoltenPool.Holder {
    private final MoltenPool pool;
    private Fluid argon;

    public ArgonCrucibleBehavior(MoltenPool pool) {
        this.pool = pool;
    }

    @Override
    public MoltenPool pool() {
        return pool;
    }

    @Override
    public void tick(MachineBehaviorContext context) {
        pool.tick(context, new MoltenPool.ArgonTank() {
            @Override
            public int availableMillibuckets() {
                FluidStack stack = context.fluid(MoltenPool.ARGON_TANK);
                return stack.isEmpty() ? 0 : stack.getAmount();
            }

            @Override
            public boolean drainMillibuckets(int amount) {
                FluidStack drained = context.drain(MoltenPool.ARGON_TANK, amount, true);
                if (drained.isEmpty() || drained.getAmount() < amount || !isArgon(drained)) return false;
                context.drain(MoltenPool.ARGON_TANK, amount, false);
                return true;
            }
        });
    }

    @Override
    public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) {
        return pool.canInsert(context, slot, stack);
    }

    /** Pipes only fill the charge slot through the open top; the menu is not the physical lid. */
    @Override
    public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack, MachineFace face) {
        if (face != null && face != MachineFace.TOP) return false;
        if (face == MachineFace.TOP && !pool.lidOpen(context)) return false;
        return pool.canInsert(context, slot, stack);
    }

    @Override
    public boolean canFill(MachineBehaviorContext context, int tank, FluidStack stack) {
        return pool.canFill(context, tank) && isArgon(stack);
    }

    @Override
    public boolean onTool(MachineBehaviorContext context, AddonTool tool, Player player,
                          InteractionHand hand, MachineFace face) {
        if (tool != AddonTool.SPADE && tool != AddonTool.UNIVERSAL_SPADE) return false;
        return pool.skim(context, player);
    }

    @Override
    public long getCrucibleTemperature(MachineBehaviorContext context) {
        return pool.temperature(context);
    }

    @Override
    public long getCrucibleContentAmount(MachineBehaviorContext context) {
        return pool.contentAmount(context);
    }

    @Override
    public boolean fillMoldAtSide(MachineBehaviorContext context, CrucibleMold mold,
                                  Direction crucibleSide, Direction moldSide) {
        return pool.pour(context, mold, moldSide.ordinal());
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
