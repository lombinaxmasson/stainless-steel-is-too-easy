package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.recipe.CrucibleContent;
import gregtech.api.addon.recipe.CrucibleMold;
import gregtech.api.machine.MachineFace;
import gregtech.api.addon.platform.AddonFluids;
import gregtech.api.addon.platform.AddonTool;
import gregtech.api.addon.platform.MachineBehavior;
import gregtech.api.addon.platform.PartBehavior;
import gregtech.api.addon.platform.PartContext;
import gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

/** Forge half of an argon-fed crucible: the argon tank is the only loader-specific piece. */
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
        MoltenPool.ArgonTank argonTank = tank(context, LargeAodStructure.ARGON, "GenGas_Argon");
        MoltenPool.ArgonTank oxygenTank = pool instanceof LargeAodCore
                ? tank(context, LargeAodStructure.OXYGEN, "Oxygen") : null;
        pool.tick(context, new LargeAodCore.GasSupply() {
            public int availableMillibuckets() { return argonTank.availableMillibuckets(); }
            public boolean drainMillibuckets(int amount) { return argonTank.drainMillibuckets(amount); }
            public int oxygenMillibuckets() { return oxygenTank == null ? 0 : oxygenTank.availableMillibuckets(); }
            public boolean drainOxygen(int amount) { return oxygenTank != null && oxygenTank.drainMillibuckets(amount); }
        });
    }

    private MoltenPool.ArgonTank tank(MachineBehaviorContext context, String name, String fluidName) {
        PartContext found = null;
        if (pool instanceof LargeAodCore) {
            for (var part : context.structure().parts()) if (part.name().equals(name)) { found = part.context(); break; }
        }
        PartContext port = found;
        boolean large = pool instanceof LargeAodCore;
        return new MoltenPool.ArgonTank() {
            public int availableMillibuckets() {
                if (large && port == null) return 0;
                FluidStack stack = port == null ? context.fluids().snapshot(0) : port.fluids().snapshot(0);
                return matches(stack, fluidName) ? stack.getAmount() : 0;
            }
            public boolean drainMillibuckets(int amount) {
                if (amount <= 0 || availableMillibuckets() < amount) return false;
                FluidStack drained = port == null ? context.fluids().drain(0, amount, false) : port.fluids().drain(0, amount, false);
                return matches(drained, fluidName) && drained.getAmount() == amount;
            }
        };
    }

    public static PartBehavior gasPort(String name) {
        String gas = LargeAodStructure.ARGON.equals(name) ? "GenGas_Argon" : "Oxygen";
        return new PartBehavior() {
            @Override public boolean canFill(PartContext context, int tank, FluidStack stack) {
                return tank == 0 && matches(stack, gas);
            }
            @Override public boolean canDrain(PartContext context, int tank) { return false; }
        };
    }

    private static boolean matches(FluidStack stack, String fluidName) {
        FluidStack expected = AddonFluids.still(fluidName, 1);
        return stack != null && !stack.isEmpty() && expected != null && !expected.isEmpty()
                && stack.getFluid() == expected.getFluid();
    }

    @Override
    public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) {
        return pool.lidOpen(context) && pool.canInsert(context, slot, stack);
    }

    /** Pipes only fill the charge slot through the open top; the menu is not the physical lid. */
    @Override
    public boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack, MachineFace face) {
        if (face != null && face != MachineFace.TOP) return false;
        if (face == MachineFace.TOP && !pool.lidOpen(context)) return false;
        return pool.lidOpen(context) && pool.canInsert(context, slot, stack);
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
    public CrucibleContent crucibleContent(MachineBehaviorContext context) {
        var contents = pool.contents(context.state());
        if (contents.isEmpty()) return CrucibleContent.EMPTY;
        return new CrucibleContent(contents.get(0).material(), pool.contentAmount(context), pool.temperature(context));
    }

    @Override
    public boolean fillMoldAtSide(MachineBehaviorContext context, CrucibleMold mold,
                                  Direction crucibleSide, Direction moldSide) {
        if (pool instanceof LargeAodCore && (!context.structure().formed() || crucibleSide != context.facing())) return false;
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
