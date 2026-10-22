package com.lombinaxmasson.ssite.chemistry;

import com.gregtech.gregtech.api.addon.platform.AddonFluids;
import com.gregtech.gregtech.api.addon.platform.AddonRecipes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/** NeoForge half of the fluid stacks used by {@link ChemistryRecipes}. */
public final class FluidBridge {
    private FluidBridge() {}

    public static AddonRecipes.Line line(ItemStack[] itemInputs, ItemStack[] itemOutputs,
                                         String[] inFields, int[] inAmounts,
                                         String[] outFields, int[] outAmounts,
                                         long duration, long perTick) {
        return new AddonRecipes.Line(itemInputs, itemOutputs,
                fluids(inFields, inAmounts), fluids(outFields, outAmounts), duration, perTick, 0);
    }

    private static FluidStack[] fluids(String[] fields, int[] amounts) {
        if (fields == null || fields.length == 0) {
            return null;
        }
        if (amounts == null || amounts.length != fields.length) {
            throw new IllegalStateException("ssite fluid amounts do not match fields");
        }
        var stacks = new FluidStack[fields.length];
        for (int i = 0; i < fields.length; i++) {
            FluidStack stack = AddonFluids.still(fields[i], amounts[i]);
            if (stack == null || stack.isEmpty()) {
                throw new IllegalStateException("ssite missing fluid " + fields[i]);
            }
            stacks[i] = stack;
        }
        return stacks;
    }
}
