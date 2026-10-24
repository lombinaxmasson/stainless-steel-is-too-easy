package com.lombinaxmasson.ssite.chemistry;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.gregtech.gregtech.api.addon.GregTechAddon;
import com.gregtech.gregtech.api.addon.platform.AddonItems;
import com.gregtech.gregtech.api.addon.platform.AddonRecipes;
import com.lombinaxmasson.ssite.machine.SsiteMachines;
import net.minecraft.world.item.ItemStack;

/**
 * Chemistry recipe changes. GregTech calls this after its own machine recipes exist.
 * Fluid stacks differ by loader, so {@link FluidBridge} packs them.
 */
public final class ChemistryRecipes {
    static final List<String> REJECTED = new ArrayList<>();
    static final Set<String> MISSING = new LinkedHashSet<>();

    private ChemistryRecipes() {}

    public static void add(GregTechAddon.Context context) {
        if (context.apiVersion() != 0) {
            throw new IllegalStateException("ssite expects GregTech addon API 0, found " + context.apiVersion());
        }
        REJECTED.clear();
        MISSING.clear();
        ferrochromium();
        chromium();
        ferrosilicon();
        aod();
        slag();
        waterTreatment();
        waterPurification();
        brineChain();
        PetrochemRecipes.add();
        PolymerRecipes.add();
        MetallurgyRecipes.add();
        RareEarthRecipes.add();
        NuclearRecipes.add();
        BaseMetalRecipes.add();
        if (!MISSING.isEmpty() || !REJECTED.isEmpty()) {
            throw new IllegalStateException("ssite recipes failed; missing=" + MISSING + ", rejected=" + REJECTED);
        }
    }

    static void missing(String what) {
        MISSING.add(what);
    }

    /** Builder for one machine recipe or display row. */
    static final class Row {
        private final String map;
        private final String label;
        private ItemStack[] itemInputs;
        private ItemStack[] itemOutputs;
        private final List<String> fluidInFields = new ArrayList<>(4);
        private final List<Integer> fluidInAmounts = new ArrayList<>(4);
        private final List<String> fluidOutFields = new ArrayList<>(4);
        private final List<Integer> fluidOutAmounts = new ArrayList<>(4);
        private int[] outputChances;
        private int[] keptInputs;

        Row(String map, String label) {
            this.map = map;
            this.label = label;
        }

        Row in(ItemStack... stacks) {
            this.itemInputs = stacks;
            return this;
        }

        Row out(ItemStack... stacks) {
            this.itemOutputs = stacks;
            return this;
        }

        Row fluidIn(String field, int amount) {
            fluidInFields.add(field);
            fluidInAmounts.add(amount);
            return this;
        }

        Row fluidIn(String f1, int a1, String f2, int a2) {
            return fluidIn(f1, a1).fluidIn(f2, a2);
        }

        Row fluidIn(String f1, int a1, String f2, int a2, String f3, int a3) {
            return fluidIn(f1, a1).fluidIn(f2, a2).fluidIn(f3, a3);
        }

        Row fluidIn(String[] fields, int[] amounts) {
            if (fields == null) return this;
            for (int i = 0; i < fields.length; i++) fluidIn(fields[i], amounts[i]);
            return this;
        }

        Row fluidOut(String field, int amount) {
            fluidOutFields.add(field);
            fluidOutAmounts.add(amount);
            return this;
        }

        Row fluidOut(String f1, int a1, String f2, int a2) {
            return fluidOut(f1, a1).fluidOut(f2, a2);
        }

        Row fluidOut(String f1, int a1, String f2, int a2, String f3, int a3) {
            return fluidOut(f1, a1).fluidOut(f2, a2).fluidOut(f3, a3);
        }

        Row fluidOut(String[] fields, int[] amounts) {
            if (fields == null) return this;
            for (int i = 0; i < fields.length; i++) fluidOut(fields[i], amounts[i]);
            return this;
        }

        Row chances(int... chances) {
            this.outputChances = chances;
            return this;
        }

        Row keep(int... slots) {
            this.keptInputs = slots;
            return this;
        }

        void add(long duration, long perTick) {
            submit(duration, perTick, false);
        }

        void display(long duration, long perTick) {
            submit(duration, perTick, true);
        }

        private void submit(long duration, long perTick, boolean displayOnly) {
            AddonRecipes.Line built = FluidBridge.line(
                    itemInputs, itemOutputs,
                    toNames(fluidInFields), toInts(fluidInAmounts),
                    toNames(fluidOutFields), toInts(fluidOutAmounts),
                    duration, perTick, outputChances, keptInputs);
            if (built == null) {
                REJECTED.add(label);
                return;
            }
            AddonRecipes.Map table = AddonRecipes.map(map);
            AddonRecipes.Line added = displayOnly ? table.addDisplay(built) : table.add(built);
            if (added == null) REJECTED.add(label);
        }

        private static String[] toNames(List<String> list) {
            return list.isEmpty() ? null : list.toArray(String[]::new);
        }

        private static int[] toInts(List<Integer> list) {
            if (list.isEmpty()) return null;
            int[] array = new int[list.size()];
            for (int i = 0; i < list.size(); i++) array[i] = list.get(i);
            return array;
        }
    }

    static Row row(String map, String label) {
        return new Row(map, label);
    }

    private static void ferrochromium() {
        for (String carbon : new String[]{"Carbon", "CoalCoke"}) {
            row("gt.recipe.calciner", "ferrochromium/" + carbon)
                    .in(dust("Chromite", 1), dust("Quicklime", 1), dust(carbon, 3))
                    .out(dust("Ferrochromium", 1))
                    .fluidOut("GenGas_CarbonMonoxide", 1000)
                    .add(200, 48);
        }
    }

    /**
     * The roaster has one item slot, so the soda roast uses the calciner.
     * No chromium-oxide material is registered, and the burner mixer has one item output,
     * so carbon reduction and the thermite step share one calciner row.
     */
    private static void chromium() {
        row("gt.recipe.calciner", "sodium-chromate")
                .in(dust("Chromite", 1), dust("SodiumCarbonate", 2))
                .out(dust("SodiumChromate", 2), dust("Hematite", 1))
                .fluidIn("Oxygen", 3000)
                .add(160, 32);
        row("gt.recipe.bath", "sodium-dichromate")
                .in(dust("SodiumChromate", 2))
                .out(dust("SodiumDichromate", 1))
                .fluidIn("GenLiquid_SulfuricAcid", 1000)
                .add(100, 16);
        row("gt.recipe.calciner", "chromium")
                .in(dust("SodiumDichromate", 1), dust("Carbon", 2), dust("Aluminium", 2))
                .out(dust("Chromium", 2), dust("Alumina", 1), dust("SodiumCarbonate", 1))
                .fluidOut("GenGas_CarbonMonoxide", 1000)
                .add(200, 64);
    }

    private static void ferrosilicon() {
        for (String silica : new String[]{"SiliconDioxide", "Quartzite"}) {
            row("gt.recipe.calciner", "ferrosilicon/" + silica)
                    .in(dust(silica, 1), dust("Iron", 1), dust("Carbon", 3))
                    .out(dust("Ferrosilicon", 1))
                    .fluidOut("GenGas_CarbonMonoxide", 1000)
                    .add(200, 48);
        }
    }

    /** Display rows for the AOD table; the converter itself refines through {@code AodConverterCore}. */
    private static void aod() {
        row(SsiteMachines.AOD_MAP, "air")
                .in(dust("CarburizedStainlessSteel", 9), dust("Ferrosilicon", 1), dust("Quicklime", 2))
                .out(dust("StainlessSteel", 5), dust("AodSlag", 4))
                .fluidIn("Air", 400)
                .display(400, 32);
        row(SsiteMachines.AOD_MAP, "argon")
                .in(dust("CarburizedStainlessSteel", 9), dust("Ferrosilicon", 1),
                        dust("Quicklime", 1), dust("Fluorite", 1))
                .out(dust("StainlessSteel", 9), dust("AodSlag", 1))
                .fluidIn("GenGas_Argon", 10)
                .display(120, 64);
        row(SsiteMachines.AOD_MAP, "316l")
                .in(dust("CarburizedStainlessSteel", 8), dust("Molybdenum", 1),
                        dust("Ferrosilicon", 1), dust("Fluorite", 1))
                .out(dust("StainlessSteel316L", 9), dust("AodSlag", 1))
                .fluidIn("GenGas_Argon", 10)
                .display(160, 64);
    }

    private static void slag() {
        row("gt.recipe.centrifuge", "slag")
                .in(dust("AodSlag", 4))
                .out(dust("Chromite", 1), dust("Quicklime", 2), dust("SiliconDioxide", 1))
                .add(200, 16);
    }

    private static void waterTreatment() {
        alum();
        celluloseAcetate();
        ozone();
    }

    private static void alum() {
        // Alunite leaching; the hydroxide + sulphuric-acid route collides with a built-in GT6CE row.
        row("gt.recipe.bath", "alum/alunite")
                .in(dust("Alunite", 2))
                .out(dust("Alum", 2), dust("SiliconDioxide", 1))
                .fluidIn("GenLiquid_SulfuricAcid", 1000)
                .add(100, 16);
    }

    private static void celluloseAcetate() {
        for (String pulp : new String[]{"Wood", "Paper"}) {
            row("gt.recipe.mixer", "cellulose-acetate/" + pulp)
                    .in(dust(pulp, 2))
                    .out(dust("CelluloseAcetate", 2))
                    .fluidIn("Vinegar_Grape", 2000, "GenLiquid_SulfuricAcid", 250)
                    .fluidOut("Dirty_Water", 1000)
                    .add(160, 30);
        }
        row("gt.recipe.press", "cellulose-acetate/plate")
                .in(dust("CelluloseAcetate", 1), circuit(1))
                .out(item("plate", "CelluloseAcetate", 1))
                .keep(1)
                .add(40, 16);
        row("gt.recipe.rollingmill", "cellulose-acetate/foil")
                .in(item("plate", "CelluloseAcetate", 1))
                .out(item("foil", "CelluloseAcetate", 4))
                .add(40, 16);
    }

    private static void ozone() {
        row("gt.recipe.electrolyzer", "ozone")
                .in(circuit(3))
                .fluidIn("Oxygen", 3000)
                .fluidOut("GenGas_Ozone", 2000)
                .add(100, 64);
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
                row("gt.recipe.bath", "clarified/" + source + "/" + flocculant)
                        .in(dust(flocculant, 1))
                        .fluidIn(source, 4000)
                        .fluidOut("GenLiquid_ClarifiedWater", 3600, "Sludge", 400)
                        .add(100, 16);
            }
        }
        row("gt.recipe.bath", "clarified/sewage")
                .in(dust("Alum", 2))
                .fluidIn("Sewage", 4000)
                .fluidOut("GenLiquid_ClarifiedWater", 3000, "Sludge", 1000)
                .add(160, 16);
    }

    private static void sludgeRecovery() {
        row("gt.recipe.drying", "sludge/dry")
                .out(dust("Clay", 1))
                .fluidIn("Sludge", 1000)
                .fluidOut("Biomass", 250, "Dirty_Water", 500)
                .add(120, 16);
        row("gt.recipe.centrifuge", "sludge/centrifuge")
                .out(dust("Clay", 2), dust("Sand", 1), item("dustTiny", "RareEarth", 1))
                .fluidIn("Sludge", 2000)
                .fluidOut("Biomass", 500, "Dirty_Water", 1000)
                .chances(10000, 10000, 2500)
                .add(160, 24);
    }

    private static void demineralizedWater() {
        row("gt.recipe.bath", "demineralized/soften")
                .in(dust("QuartzSand", 1), item("dustTiny", "SodiumCarbonate", 1))
                .out(item("dustSmall", "Calcite", 1), item("dustTiny", "MagnesiumCarbonate", 1))
                .fluidIn("GenLiquid_ClarifiedWater", 4000)
                .fluidOut("GenLiquid_DemineralizedWater", 3800)
                .keep(0)
                .add(120, 16);
        row("gt.recipe.distillery", "demineralized/distill")
                .in(circuit(1))
                .fluidIn("GenLiquid_DemineralizedWater", 1000)
                .fluidOut("DistW", 1000)
                .add(40, 16);
    }

    private static void deionizedWater() {
        row("gt.recipe.bath", "deionized")
                .in(dust("Zeolite", 1), item("dustTiny", "SodiumHydroxide", 1))
                .fluidIn("GenLiquid_DemineralizedWater", 4000)
                .fluidOut("GenLiquid_DeionizedWater", 3800, "Saltwater", 200)
                .keep(0)
                .add(120, 30);
    }

    private static void filteredWater() {
        row("gt.recipe.autoclave", "filtered/ro")
                .in(item("foil", "CelluloseAcetate", 1), circuit(1))
                .out(item("dustTiny", "Salt", 1))
                .fluidIn("GenLiquid_DeionizedWater", 4000)
                .fluidOut("GenLiquid_FilteredWater", 3600)
                .chances(1000)
                .keep(0, 1)
                .add(160, 64);
    }

    private static void ultrapureWater() {
        for (String lens : new String[]{"Sapphire", "Diamond"}) {
            row("gt.recipe.mixer", "ultrapure/" + lens)
                    .in(item("lens", lens, 1))
                    .fluidIn("GenLiquid_FilteredWater", 4000, "GenGas_Ozone", 250)
                    .fluidOut("GenLiquid_UltrapureWater", 4000, "Oxygen", 375)
                    .keep(0)
                    .add(200, 120);
        }
    }

    private static void brineChain() {
        concentrateBrine();
        removeMagnesiumCalcium();
        blowBromine();
        precipitateLithium();
    }

    private static void concentrateBrine() {
        row("gt.recipe.distillationtower", "brine/ocean")
                .out(dust("Salt", 2), dust("Gypsum", 1))
                .fluidIn("Ocean", 10000)
                .fluidOut("DistW", 7500, "Brine", 2000)
                .add(200, 48);
        row("gt.recipe.distillationtower", "brine/saltwater")
                .out(dust("Salt", 1))
                .fluidIn("Saltwater", 5000)
                .fluidOut("DistW", 3500, "Brine", 1000)
                .add(120, 32);
        row("gt.recipe.distillery", "brine/ocean-early")
                .in(circuit(2))
                .out(item("dustSmall", "Gypsum", 1))
                .fluidIn("Ocean", 2000)
                .fluidOut("DistW", 1400, "Brine", 400)
                .add(100, 16);
    }

    private static void removeMagnesiumCalcium() {
        row("gt.recipe.bath", "brine/slake-lime")
                .in(dust("Quicklime", 1))
                .out(dust("CalciumHydroxide", 1))
                .fluidIn("Water", 1000)
                .add(40, 8);
        row("gt.recipe.bath", "brine/remove-mg-ca")
                .in(dust("CalciumHydroxide", 1), dust("SodiumCarbonate", 1))
                .out(dust("MagnesiumHydroxide", 1), dust("Calcite", 1))
                .fluidIn("Brine", 4000)
                .fluidOut("GenLiquid_DecalcifiedBrine", 3600)
                .add(120, 24);
        row("gt.recipe.bath", "brine/magnesium-chloride")
                .in(dust("MagnesiumHydroxide", 1))
                .out(dust("MagnesiumChloride", 1))
                .fluidIn("GenGas_HydrochloricAcid", 2000)
                .fluidOut("DistW", 2000)
                .add(80, 16);
    }

    private static void blowBromine() {
        row("gt.recipe.mixer", "brine/blow-bromine")
                .fluidIn("GenLiquid_DecalcifiedBrine", 4000, "GenGas_Chlorine", 1000, "GenLiquid_SulfuricAcid", 250)
                .fluidOut("GenGas_CrudeBromine", 1000, "GenLiquid_LithiumRichBrine", 3000)
                .add(160, 48);
        row("gt.recipe.distillationtower", "brine/bromine")
                .fluidIn("GenGas_CrudeBromine", 2000)
                .fluidOut(fields("GenGas_Bromine", "GenGas_Iodine", "GenGas_Chlorine", "GenGas_HydrochloricAcid"),
                        amounts(1500, 200, 200, 100))
                .add(160, 64);
        row("gt.recipe.distillery", "brine/bromine-early")
                .in(circuit(1))
                .fluidIn("GenGas_CrudeBromine", 1000)
                .fluidOut("GenGas_Bromine", 700, "GenGas_HydrochloricAcid", 200)
                .add(120, 30);
    }

    private static void precipitateLithium() {
        row("gt.recipe.bath", "brine/lithium-carbonate")
                .in(dust("SodiumCarbonate", 2))
                .out(dust("LithiumCarbonate", 1), dust("Salt", 2))
                .fluidIn("GenLiquid_LithiumRichBrine", 3000)
                .fluidOut("DistW", 2000)
                .add(140, 30);
        row("gt.recipe.bath", "brine/lithium-chloride")
                .in(dust("LithiumCarbonate", 1))
                .out(dust("LithiumChloride", 2))
                .fluidIn("GenGas_HydrochloricAcid", 2000)
                .fluidOut("CarbonDioxide", 1000, "DistW", 1000)
                .add(100, 24);
        // LiCl molten-salt electrolysis already exists in GT6CE; no separate row.
    }

    static ItemStack item(String prefix, String material, int count) {
        ItemStack stack = AddonItems.stack(prefix, material, count);
        if (stack.isEmpty()) {
            missing(prefix + " " + material);
        }
        return stack;
    }

    static ItemStack dust(String material, int count) {
        return item("dust", material, count);
    }

    static ItemStack circuit(int n) {
        return AddonItems.selectorTag(n);
    }

    static String[] fields(String... names) {
        return names;
    }

    static int[] amounts(int... values) {
        return values;
    }
}
