package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

/** M6 rare-earth recipes. Cards 2 and 3 append their own methods here. */
public final class RareEarthRecipes {
    private RareEarthRecipes() {}

    static void add() {
        oreLeaching();
    }

    /** Three ore routes converge on MixedRareEarthSolution; monazite also yields Th and U. */
    private static void oreLeaching() {
        row("gt.recipe.calciner", "bastnasite/roast")
                .in(dust("Bastnasite", 2))
                .out(dust("BastnasiteCalcine", 2))
                .fluidIn("DistW", 1000, "Oxygen", 500)
                .fluidOut("GenGas_HydrogenFluoride", 2000, "CarbonDioxide", 2000)
                .add(160, 48);

        row("gt.recipe.mixer", "bastnasite/leach")
                .in(dust("BastnasiteCalcine", 2))
                .fluidIn("GenGas_HydrochloricAcid", 6000)
                .fluidOut("GenLiquid_MixedRareEarthSolution", 2000, "Dirty_Water", 3000)
                .add(160, 48);

        row("gt.recipe.calciner", "monazite/digest")
                .in(dust("Monazite", 2))
                .out(dust("ThoriumConcentrate", 1))
                .fluidIn("GenLiquid_SulfuricAcid", 3000)
                .fluidOut("GenLiquid_MixedRareEarthSolution", 2000,
                        "GenLiquid_UranylSulfateSolution", 500, "Dirty_Water", 500)
                .add(200, 64);

        row("gt.recipe.calciner", "xenotime/caustic-fusion")
                .in(dust("Xenotime", 2), dust("SodiumHydroxide", 6))
                .fluidIn("GenGas_HydrochloricAcid", 6000)
                .fluidOut("GenLiquid_MixedRareEarthSolution", 2000, "Dirty_Water", 3000)
                .add(240, 64);

        row("gt.recipe.mixer", "rare-earth/leach")
                .in(dust("RareEarth", 4))
                .fluidIn("GenGas_HydrochloricAcid", 6000)
                .fluidOut("GenLiquid_MixedRareEarthSolution", 1500, "Dirty_Water", 1500)
                .add(160, 48);
    }
}
