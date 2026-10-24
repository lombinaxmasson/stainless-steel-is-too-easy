package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

/** M7 base-metal matte, leaching, P204 split and electrorefining chain. */
public final class BaseMetalRecipes {
    private BaseMetalRecipes() {}

    static void add() {
        matteSmelting();
        matteLeaching();
        nickelCobaltSplit();
        electrowinning();
    }

    private static void matteSmelting() {
        row("gt.recipe.calciner", "m7/green-matte-copper")
                .in(dust("Chalcopyrite", 3))
                .out(dust("GreenMatte", 3), dust("Hematite", 1))
                .fluidIn("Oxygen", 1500)
                .fluidOut("GenGas_SulfurDioxide", 1500)
                .add(240, 64);
        row("gt.recipe.calciner", "m7/green-matte-nickel")
                .in(dust("Pentlandite", 3))
                .out(dust("GreenMatte", 3), dust("Hematite", 1))
                .fluidIn("Oxygen", 1200)
                .fluidOut("GenGas_SulfurDioxide", 1200)
                .add(240, 64);
        row("gt.recipe.calciner", "m7/white-matte")
                .in(dust("GreenMatte", 3))
                .out(dust("WhiteMatte", 2), dust("Hematite", 1))
                .fluidIn("Oxygen", 1000)
                .fluidOut("GenGas_SulfurDioxide", 1000)
                .add(240, 80);
    }

    /** Oxidative sulfate leach leaves copper as the residue; nickel and cobalt report to the liquor. */
    private static void matteLeaching() {
        row("gt.recipe.mixer", "m7/white-matte-leach")
                .in(dust("WhiteMatte", 2))
                .out(dust("CrudeCopper", 2))
                .fluidIn("GenLiquid_SulfuricAcid", 1000, "Oxygen", 500, "DistW", 1000)
                .fluidOut("GenLiquid_NickelCobaltSulfate", 1000)
                .add(200, 64);
    }

    /** P204 loads cobalt and leaves nickel in the raffinate; 96% of the organic phase comes back on stripping. */
    private static void nickelCobaltSplit() {
        row("gt.recipe.mixer", "m7/p204-nickel-cobalt")
                .fluidIn("GenLiquid_NickelCobaltSulfate", 1000,
                        "GenLiquid_DiethylhexylPhosphoricAcid", 500)
                .fluidOut("GenLiquid_NickelSulfateSolution", 1000, "GenLiquid_CobaltP204Extract", 1000)
                .add(160, 64);
        row("gt.recipe.mixer", "m7/cobalt-strip")
                .fluidIn("GenLiquid_CobaltP204Extract", 1000, "GenLiquid_SulfuricAcid", 500)
                .fluidOut("GenLiquid_CobaltSulfateSolution", 1000,
                        "GenLiquid_DiethylhexylPhosphoricAcid", 480)
                .add(160, 48);
    }

    private static void electrowinning() {
        row("gt.recipe.electrolyzer", "m7/nickel-electrowin")
                .out(dust("Nickel", 1))
                .fluidIn("GenLiquid_NickelSulfateSolution", 1000)
                .fluidOut("GenLiquid_SulfuricAcid", 950, "Oxygen", 500)
                .add(240, 96);
        row("gt.recipe.electrolyzer", "m7/cobalt-electrowin")
                .out(dust("Cobalt", 1))
                .fluidIn("GenLiquid_CobaltSulfateSolution", 1000)
                .fluidOut("GenLiquid_SulfuricAcid", 950, "Oxygen", 500)
                .add(240, 96);
        row("gt.recipe.electrolyzer", "m7/copper-electrorefine")
                .in(dust("CrudeCopper", 2))
                .out(dust("Copper", 2), dust("CopperAnodeSlime", 1), dust("PlatinumGroupConcentrate", 1))
                .chances(10000, 10000, 2500)
                .fluidIn("GenLiquid_SulfuricAcid", 1000)
                .fluidOut("GenLiquid_SulfuricAcid", 950)
                .add(300, 128);
    }
}
