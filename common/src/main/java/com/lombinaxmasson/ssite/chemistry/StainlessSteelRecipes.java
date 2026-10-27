package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

import com.lombinaxmasson.ssite.machine.SsiteMachines;

/** M1 chromium, stainless-steel refining display rows and slag recovery. */
public final class StainlessSteelRecipes {
    private StainlessSteelRecipes() {}

    static void add() {
        ferrochromium();
        chromium();
        ferrosilicon();
        aod();
        slag();
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

}
