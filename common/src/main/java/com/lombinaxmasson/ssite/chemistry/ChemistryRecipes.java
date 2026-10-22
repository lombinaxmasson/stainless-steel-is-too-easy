package com.lombinaxmasson.ssite.chemistry;

import com.gregtech.gregtech.api.addon.GregTechAddon;
import com.gregtech.gregtech.api.addon.platform.AddonItems;
import com.gregtech.gregtech.api.addon.platform.AddonRecipes;
import net.minecraft.world.item.ItemStack;

/**
 * Chemistry recipe changes. GregTech calls this once, after its own machine recipes exist.
 * Fluid stacks differ by loader, so {@link FluidBridge} packs them.
 */
public final class ChemistryRecipes {
    private ChemistryRecipes() {}

    public static void add(GregTechAddon.Context context) {
        if (context.apiVersion() != 0) {
            throw new IllegalStateException("ssite expects GregTech addon API 0, found " + context.apiVersion());
        }
        ferrochromium();
        chromium();
        ferrosilicon();
        slag();
    }

    private static void ferrochromium() {
        for (String carbon : new String[]{"Carbon", "CoalCoke"}) {
            recipe("gt.recipe.calciner", "ferrochromium/" + carbon,
                    items(dust("Chromite", 1), dust("Quicklime", 1), dust(carbon, 3)),
                    items(dust("Ferrochromium", 1)),
                    null, null,
                    fields("GenGas_CarbonMonoxide"), amounts(1000),
                    200, 48);
        }
    }

    /**
     * The roaster has one item slot, so the soda roast uses the calciner.
     * No chromium-oxide material is registered, and the burner mixer has one item output,
     * so carbon reduction and the thermite step share one calciner row.
     */
    private static void chromium() {
        recipe("gt.recipe.calciner", "sodium-chromate",
                items(dust("Chromite", 1), dust("SodiumCarbonate", 2)),
                items(dust("SodiumChromate", 2), dust("Hematite", 1)),
                fields("Oxygen"), amounts(3000),
                null, null,
                160, 32);
        recipe("gt.recipe.bath", "sodium-dichromate",
                items(dust("SodiumChromate", 2)),
                items(dust("SodiumDichromate", 1)),
                fields("GenLiquid_SulfuricAcid"), amounts(1000),
                null, null,
                100, 16);
        recipe("gt.recipe.calciner", "chromium",
                items(dust("SodiumDichromate", 1), dust("Carbon", 2), dust("Aluminium", 2)),
                items(dust("Chromium", 2), dust("Alumina", 1), dust("SodiumCarbonate", 1)),
                null, null,
                fields("GenGas_CarbonMonoxide"), amounts(1000),
                200, 64);
    }

    private static void ferrosilicon() {
        for (String silica : new String[]{"SiliconDioxide", "Quartzite"}) {
            recipe("gt.recipe.calciner", "ferrosilicon/" + silica,
                    items(dust(silica, 1), dust("Iron", 1), dust("Carbon", 3)),
                    items(dust("Ferrosilicon", 1)),
                    null, null,
                    fields("GenGas_CarbonMonoxide"), amounts(1000),
                    200, 48);
        }
    }

    private static void slag() {
        recipe("gt.recipe.centrifuge", "slag",
                items(dust("AodSlag", 4)),
                items(dust("Chromite", 1), dust("Quicklime", 2), dust("SiliconDioxide", 1)),
                null, null, null, null,
                200, 16);
    }

    private static void recipe(String map, String label, ItemStack[] itemInputs, ItemStack[] itemOutputs,
                               String[] fluidInFields, int[] fluidInAmounts,
                               String[] fluidOutFields, int[] fluidOutAmounts,
                               long duration, long perTick) {
        AddonRecipes.Line line = AddonRecipes.map(map).add(FluidBridge.line(
                itemInputs, itemOutputs, fluidInFields, fluidInAmounts, fluidOutFields, fluidOutAmounts,
                duration, perTick));
        if (line == null) {
            throw new IllegalStateException("ssite recipe rejected: " + label);
        }
    }

    private static ItemStack dust(String material, int count) {
        ItemStack stack = AddonItems.stack("dust", material, count);
        if (stack.isEmpty()) {
            throw new IllegalStateException("ssite missing dust " + material);
        }
        return stack;
    }

    private static ItemStack[] items(ItemStack... stacks) {
        return stacks;
    }

    private static String[] fields(String... names) {
        return names;
    }

    private static int[] amounts(int... values) {
        return values;
    }
}
