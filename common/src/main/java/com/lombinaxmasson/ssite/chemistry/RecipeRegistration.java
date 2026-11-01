package com.lombinaxmasson.ssite.chemistry;

import java.util.List;

import gregtech.api.addon.platform.AddonItems;
import gregtech.api.addon.platform.AddonRecipes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.FluidRef;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.ItemRef;

/** Resolves symbolic declarations after the host's items, fluids and recipe maps exist. */
final class RecipeRegistration {
    private RecipeRegistration() {}

    static void register(RecipeDeclaration declaration) {
        for (var condition : declaration.distinctFluids()) {
            if (!FluidBridge.distinct(condition.first(), condition.second())) return;
        }
        AddonRecipes.Line built = FluidBridge.line(
                items(declaration.itemInputs()), items(declaration.itemOutputs()),
                fields(declaration.fluidInputs()), amounts(declaration.fluidInputs()),
                fields(declaration.fluidOutputs()), amounts(declaration.fluidOutputs()),
                declaration.duration(), declaration.perTick(),
                integers(declaration.outputChances()), integers(declaration.keptInputs()));
        if (built == null) {
            ChemistryRecipes.REJECTED.add(declaration.label());
            return;
        }
        var table = AddonRecipes.map(declaration.map());
        AddonRecipes.Line added = declaration.displayOnly() ? table.addDisplay(built) : table.add(built);
        if (added == null) ChemistryRecipes.REJECTED.add(declaration.label());
    }

    private static ItemStack[] items(List<ItemRef> references) {
        if (references.isEmpty()) return null;
        ItemStack[] stacks = new ItemStack[references.size()];
        for (int i = 0; i < stacks.length; i++) {
            ItemRef reference = references.get(i);
            stacks[i] = switch (reference.kind()) {
                case MATERIAL -> material(reference);
                case VANILLA -> vanilla(reference);
                case SELECTOR -> AddonItems.selectorTag(reference.selector());
            };
        }
        return stacks;
    }

    private static ItemStack material(ItemRef reference) {
        ItemStack stack = AddonItems.stack(reference.prefix(), reference.material(), reference.count());
        if (stack.isEmpty()) ChemistryRecipes.missing(reference.prefix() + " " + reference.material());
        return stack;
    }

    private static ItemStack vanilla(ItemRef reference) {
        ResourceLocation id = ResourceLocation.tryParse(reference.itemId());
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            ChemistryRecipes.missing("item " + reference.itemId());
            return ItemStack.EMPTY;
        }
        return new ItemStack(BuiltInRegistries.ITEM.get(id), reference.count());
    }

    private static String[] fields(List<FluidRef> references) {
        return references.isEmpty() ? null : references.stream().map(FluidRef::field).toArray(String[]::new);
    }

    private static int[] amounts(List<FluidRef> references) {
        return references.isEmpty() ? null : references.stream().mapToInt(FluidRef::amount).toArray();
    }

    private static int[] integers(List<Integer> values) {
        return values.isEmpty() ? null : values.stream().mapToInt(Integer::intValue).toArray();
    }
}
