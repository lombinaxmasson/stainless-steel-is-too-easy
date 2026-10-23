package com.lombinaxmasson.ssite.chemistry;

import java.util.ArrayList;
import java.util.List;

import com.gregtech.gregtech.api.addon.GregTechAddon;
import com.gregtech.gregtech.api.addon.platform.AddonItems;
import com.gregtech.gregtech.api.addon.platform.AddonRecipes;
import net.minecraft.world.item.ItemStack;

/**
 * Chemistry recipe changes. GregTech calls this once, after its own machine recipes exist.
 * Fluid stacks differ by loader, so {@link FluidBridge} packs them.
 */
public final class ChemistryRecipes {
    static final List<String> REJECTED = new ArrayList<>();

    private ChemistryRecipes() {}

    public static void add(GregTechAddon.Context context) {
        if (context.apiVersion() != 0) {
            throw new IllegalStateException("ssite expects GregTech addon API 0, found " + context.apiVersion());
        }
        ferrochromium();
        chromium();
        ferrosilicon();
        slag();
        waterTreatment();
        waterPurification();
        brineChain();
        PetrochemRecipes.add();
        if (!REJECTED.isEmpty()) {
            throw new IllegalStateException("ssite recipes rejected: " + REJECTED);
        }
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

    private static void waterTreatment() {
        alum();
        celluloseAcetate();
        ozone();
    }

    private static void alum() {
        // Alunite leaching; the hydroxide + sulphuric-acid route collides with a built-in GT6CE row.
        recipe("gt.recipe.bath", "alum/alunite",
                items(dust("Alunite", 2)),
                items(dust("Alum", 2), dust("SiliconDioxide", 1)),
                fields("GenLiquid_SulfuricAcid"), amounts(1000),
                null, null,
                100, 16);
    }

    private static void celluloseAcetate() {
        for (String pulp : new String[]{"Wood", "Paper"}) {
            recipe("gt.recipe.mixer", "cellulose-acetate/" + pulp,
                    items(dust(pulp, 2)),
                    items(dust("CelluloseAcetate", 2)),
                    fields("Vinegar_Grape", "GenLiquid_SulfuricAcid"), amounts(2000, 250),
                    fields("Dirty_Water"), amounts(1000),
                    160, 30);
        }
        recipe("gt.recipe.press", "cellulose-acetate/plate",
                items(dust("CelluloseAcetate", 1), circuit(1)),
                items(item("plate", "CelluloseAcetate", 1)),
                null, null, null, null,
                40, 16, null, new int[]{1});
        recipe("gt.recipe.rollingmill", "cellulose-acetate/foil",
                items(item("plate", "CelluloseAcetate", 1)),
                items(item("foil", "CelluloseAcetate", 4)),
                null, null, null, null,
                40, 16);
    }

    private static void ozone() {
        recipe("gt.recipe.electrolyzer", "ozone",
                items(circuit(3)),
                items(),
                fields("Oxygen"), amounts(3000),
                fields("GenGas_Ozone"), amounts(2000),
                100, 64);
    }

    private static void waterPurification() {
        clarifiedWater();
        sludgeRecovery();
        demineralizedWater();
        deionizedWater();
        filteredWater();
        ultrapureWater();
    }

    private static void clarifiedWater() {
        for (String source : new String[]{"Water", "River_Water", "Dirty_Water", "Stagnant_Water", "Swampwater"}) {
            for (String flocculant : new String[]{"Alum", "VitriolOfClay"}) {
                recipe("gt.recipe.bath", "clarified/" + source + "/" + flocculant,
                        items(dust(flocculant, 1)),
                        items(),
                        fields(source), amounts(4000),
                        fields("GenLiquid_ClarifiedWater", "Sludge"), amounts(3600, 400),
                        100, 16);
            }
        }
        recipe("gt.recipe.bath", "clarified/sewage",
                items(dust("Alum", 2)),
                items(),
                fields("Sewage"), amounts(4000),
                fields("GenLiquid_ClarifiedWater", "Sludge"), amounts(3000, 1000),
                160, 16);
    }

    private static void sludgeRecovery() {
        recipe("gt.recipe.drying", "sludge/dry",
                items(),
                items(dust("Clay", 1)),
                fields("Sludge"), amounts(1000),
                fields("Biomass", "Dirty_Water"), amounts(250, 500),
                120, 16);
        recipe("gt.recipe.centrifuge", "sludge/centrifuge",
                items(),
                items(dust("Clay", 2), dust("Sand", 1), item("dustTiny", "RareEarth", 1)),
                fields("Sludge"), amounts(2000),
                fields("Biomass", "Dirty_Water"), amounts(500, 1000),
                160, 24, new int[]{10000, 10000, 2500}, null);
    }

    private static void demineralizedWater() {
        recipe("gt.recipe.bath", "demineralized/soften",
                items(dust("QuartzSand", 1), item("dustTiny", "SodiumCarbonate", 1)),
                items(item("dustSmall", "Calcite", 1), item("dustTiny", "MagnesiumCarbonate", 1)),
                fields("GenLiquid_ClarifiedWater"), amounts(4000),
                fields("GenLiquid_DemineralizedWater"), amounts(3800),
                120, 16, null, new int[]{0});
        recipe("gt.recipe.distillery", "demineralized/distill",
                items(circuit(1)),
                items(),
                fields("GenLiquid_DemineralizedWater"), amounts(1000),
                fields("DistW"), amounts(1000),
                40, 16);
    }

    private static void deionizedWater() {
        recipe("gt.recipe.bath", "deionized",
                items(dust("Zeolite", 1), item("dustTiny", "SodiumHydroxide", 1)),
                items(),
                fields("GenLiquid_DemineralizedWater"), amounts(4000),
                fields("GenLiquid_DeionizedWater", "Saltwater"), amounts(3800, 200),
                120, 30, null, new int[]{0});
    }

    private static void filteredWater() {
        recipe("gt.recipe.autoclave", "filtered/ro",
                items(item("foil", "CelluloseAcetate", 1), circuit(1)),
                items(item("dustTiny", "Salt", 1)),
                fields("GenLiquid_DeionizedWater"), amounts(4000),
                fields("GenLiquid_FilteredWater"), amounts(3600),
                160, 64, new int[]{1000}, new int[]{0, 1});
    }

    private static void ultrapureWater() {
        for (String lens : new String[]{"Sapphire", "Diamond"}) {
            recipe("gt.recipe.mixer", "ultrapure/" + lens,
                    items(item("lens", lens, 1)),
                    items(),
                    fields("GenLiquid_FilteredWater", "GenGas_Ozone"), amounts(4000, 250),
                    fields("GenLiquid_UltrapureWater", "Oxygen"), amounts(4000, 375),
                    200, 120, null, new int[]{0});
        }
    }

    private static void brineChain() {
        concentrateBrine();
        removeMagnesiumCalcium();
        blowBromine();
        precipitateLithium();
    }

    private static void concentrateBrine() {
        recipe("gt.recipe.distillationtower", "brine/ocean",
                items(),
                items(dust("Salt", 2), dust("Gypsum", 1)),
                fields("Ocean"), amounts(10000),
                fields("DistW", "Brine"), amounts(7500, 2000),
                200, 48);
        recipe("gt.recipe.distillationtower", "brine/saltwater",
                items(),
                items(dust("Salt", 1)),
                fields("Saltwater"), amounts(5000),
                fields("DistW", "Brine"), amounts(3500, 1000),
                120, 32);
        recipe("gt.recipe.distillery", "brine/ocean-early",
                items(circuit(2)),
                items(item("dustSmall", "Gypsum", 1)),
                fields("Ocean"), amounts(2000),
                fields("DistW", "Brine"), amounts(1400, 400),
                100, 16);
    }

    private static void removeMagnesiumCalcium() {
        recipe("gt.recipe.bath", "brine/slake-lime",
                items(dust("Quicklime", 1)),
                items(dust("CalciumHydroxide", 1)),
                fields("Water"), amounts(1000),
                null, null,
                40, 8);
        recipe("gt.recipe.bath", "brine/remove-mg-ca",
                items(dust("CalciumHydroxide", 1), dust("SodiumCarbonate", 1)),
                items(dust("MagnesiumHydroxide", 1), dust("Calcite", 1)),
                fields("Brine"), amounts(4000),
                fields("GenLiquid_DecalcifiedBrine"), amounts(3600),
                120, 24);
        recipe("gt.recipe.bath", "brine/magnesium-chloride",
                items(dust("MagnesiumHydroxide", 1)),
                items(dust("MagnesiumChloride", 1)),
                fields("GenGas_HydrochloricAcid"), amounts(2000),
                fields("DistW"), amounts(2000),
                80, 16);
    }

    private static void blowBromine() {
        recipe("gt.recipe.mixer", "brine/blow-bromine",
                items(),
                items(),
                fields("GenLiquid_DecalcifiedBrine", "GenGas_Chlorine", "GenLiquid_SulfuricAcid"),
                amounts(4000, 1000, 250),
                fields("GenGas_CrudeBromine", "GenLiquid_LithiumRichBrine"), amounts(1000, 3000),
                160, 48);
        recipe("gt.recipe.distillationtower", "brine/bromine",
                items(),
                items(),
                fields("GenGas_CrudeBromine"), amounts(2000),
                fields("GenGas_Bromine", "GenGas_Iodine", "GenGas_Chlorine", "GenGas_HydrochloricAcid"),
                amounts(1500, 200, 200, 100),
                160, 64);
        recipe("gt.recipe.distillery", "brine/bromine-early",
                items(circuit(1)),
                items(),
                fields("GenGas_CrudeBromine"), amounts(1000),
                fields("GenGas_Bromine", "GenGas_HydrochloricAcid"), amounts(700, 200),
                120, 30);
    }

    private static void precipitateLithium() {
        recipe("gt.recipe.bath", "brine/lithium-carbonate",
                items(dust("SodiumCarbonate", 2)),
                items(dust("LithiumCarbonate", 1), dust("Salt", 2)),
                fields("GenLiquid_LithiumRichBrine"), amounts(3000),
                fields("DistW"), amounts(2000),
                140, 30);
        recipe("gt.recipe.bath", "brine/lithium-chloride",
                items(dust("LithiumCarbonate", 1)),
                items(dust("LithiumChloride", 2)),
                fields("GenGas_HydrochloricAcid"), amounts(2000),
                fields("CarbonDioxide", "DistW"), amounts(1000, 1000),
                100, 24);
        // LiCl molten-salt electrolysis already exists in GT6CE; no separate row.
    }

    static void recipe(String map, String label, ItemStack[] itemInputs, ItemStack[] itemOutputs,
                               String[] fluidInFields, int[] fluidInAmounts,
                               String[] fluidOutFields, int[] fluidOutAmounts,
                               long duration, long perTick) {
        recipe(map, label, itemInputs, itemOutputs, fluidInFields, fluidInAmounts, fluidOutFields, fluidOutAmounts,
                duration, perTick, null, null);
    }

    static void recipe(String map, String label, ItemStack[] itemInputs, ItemStack[] itemOutputs,
                               String[] fluidInFields, int[] fluidInAmounts,
                               String[] fluidOutFields, int[] fluidOutAmounts,
                               long duration, long perTick, int[] outputChances, int[] keptInputs) {
        AddonRecipes.Line line = AddonRecipes.map(map).add(FluidBridge.line(
                itemInputs, itemOutputs, fluidInFields, fluidInAmounts, fluidOutFields, fluidOutAmounts,
                duration, perTick, outputChances, keptInputs));
        if (line == null) {
            REJECTED.add(label);
        }
    }

    static ItemStack item(String prefix, String material, int count) {
        ItemStack stack = AddonItems.stack(prefix, material, count);
        if (stack.isEmpty()) {
            throw new IllegalStateException("ssite missing " + prefix + " " + material);
        }
        return stack;
    }

    static ItemStack dust(String material, int count) {
        return item("dust", material, count);
    }

    static ItemStack circuit(int n) {
        return AddonItems.selectorTag(n);
    }

    static ItemStack[] items(ItemStack... stacks) {
        return stacks;
    }

    static String[] fields(String... names) {
        return names;
    }

    static int[] amounts(int... values) {
        return values;
    }
}
