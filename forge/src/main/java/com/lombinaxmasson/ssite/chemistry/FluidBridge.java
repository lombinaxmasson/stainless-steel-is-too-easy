package com.lombinaxmasson.ssite.chemistry;

import com.gregtech.gregtech.api.addon.platform.AddonFluids;
import com.gregtech.gregtech.api.addon.platform.AddonRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** Forge half of the fluid stacks used by {@link ChemistryRecipes}. */
public final class FluidBridge {
    private FluidBridge() {}

    /** Null when an item or fluid is missing; {@link ChemistryRecipes} records the name and skips the row. */
    public static AddonRecipes.Line line(ItemStack[] itemInputs, ItemStack[] itemOutputs,
                                         String[] inFields, int[] inAmounts,
                                         String[] outFields, int[] outAmounts,
                                         long duration, long perTick,
                                         int[] outputChances, int[] keptInputs) {
        FluidStack[] fluidInputs = fluids(inFields, inAmounts);
        FluidStack[] fluidOutputs = fluids(outFields, outAmounts);
        if (missingItems(itemInputs) || missingItems(itemOutputs)
                || (inFields != null && inFields.length > 0 && fluidInputs == null)
                || (outFields != null && outFields.length > 0 && fluidOutputs == null)) {
            return null;
        }
        return new AddonRecipes.Line(itemInputs, itemOutputs,
                fluidInputs, fluidOutputs, duration, perTick, 0,
                outputChances, keptInputs);
    }

    /** True when both fields resolve to distinct, registered fluids; missing or equal fluids yield false. */
    public static boolean distinct(String fieldA, String fieldB) {
        FluidStack a = AddonFluids.still(fieldA, 1);
        FluidStack b = AddonFluids.still(fieldB, 1);
        return a != null && !a.isEmpty() && b != null && !b.isEmpty() && a.getFluid() != b.getFluid();
    }

    private static boolean missingItems(ItemStack[] stacks) {
        if (stacks == null) return false;
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) return true;
        }
        return false;
    }

    private static FluidStack[] fluids(String[] fields, int[] amounts) {
        if (fields == null || fields.length == 0) return null;
        if (amounts == null || amounts.length != fields.length) {
            throw new IllegalStateException("ssite fluid amounts do not match fields");
        }
        var stacks = new FluidStack[fields.length];
        boolean missing = false;
        for (int i = 0; i < fields.length; i++) {
            FluidStack stack = AddonFluids.still(fields[i], amounts[i]);
            if (stack == null || stack.isEmpty()) {
                ChemistryRecipes.missing("fluid " + fields[i]);
                missing = true;
            }
            stacks[i] = stack;
        }
        return missing ? null : stacks;
    }
}
