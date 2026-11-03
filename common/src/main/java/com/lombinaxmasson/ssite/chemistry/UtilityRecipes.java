package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.circuit;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

/**
 * U-01 natural-gas treatment. Raw {@code Gas_Natural} from the host spring is dehydrated with
 * triethylene glycol, sweetened with MDEA and stabilized into pipeline gas; light ends keep
 * feeding the existing C4/C5 chains and the acid gas is recovered as sulfur.
 *
 * <p>Both solvents are regenerated at 95% per pass. The one-millibucket make-up per 1000 mB of raw
 * gas is covered by the synthesis rows below, so the treatment train has no consumable dead end.
 */
public final class UtilityRecipes {
    private UtilityRecipes() {}

    static void add() {
        solventMakeup();
        gasTreatment();
        lightEnds();
        pipelineFuel();
    }

    private static void solventMakeup() {
        row("gt.recipe.mixer", "u01/teg-synthesis")
                .in(dust("Alumina", 1))
                .fluidIn("GenLiquid_EthyleneGlycol", 3000)
                .fluidOut("GenLiquid_TriethyleneGlycol", 1000, "DistW", 2000)
                .keep(0)
                .add(160, 48);
        row("gt.recipe.mixer", "u01/mdea-synthesis")
                .fluidIn("GenGas_Ammonia", 1000, "GenLiquid_Methanol", 1000, "GenGas_EthyleneOxide", 2000)
                .fluidOut("GenLiquid_Methyldiethanolamine", 1000, "DistW", 2000)
                .add(200, 64);
    }

    private static void gasTreatment() {
        // TEG dehydrates the water-saturated raw gas; the rich glycol is boiled out and reused.
        row("gt.recipe.mixer", "u01/dehydrate")
                .fluidIn("Gas_Natural", 1000, "GenLiquid_TriethyleneGlycol", 20)
                .fluidOut("GenGas_DehydratedNaturalGas", 1000, "GenLiquid_RichGlycol", 20)
                .add(80, 32);
        row("gt.recipe.distillery", "u01/regenerate-glycol")
                .in(circuit(1))
                .fluidIn("GenLiquid_RichGlycol", 20)
                .fluidOut("GenLiquid_TriethyleneGlycol", 19, "DistW", 10)
                .keep(0)
                .add(60, 16);
        // MDEA removes H2S; the loaded amine is stripped and the acid gas goes to Claus recovery.
        row("gt.recipe.mixer", "u01/desulfurize")
                .fluidIn("GenGas_DehydratedNaturalGas", 1000, "GenLiquid_Methyldiethanolamine", 20)
                .fluidOut("GenGas_SweetNaturalGas", 1000, "GenLiquid_RichAmine", 20)
                .add(100, 32);
        row("gt.recipe.distillery", "u01/regenerate-amine")
                .in(circuit(2))
                .fluidIn("GenLiquid_RichAmine", 20)
                .fluidOut("GenLiquid_Methyldiethanolamine", 19, "GenGas_HydrogenSulfide", 10)
                .keep(0)
                .add(60, 16);
        row("gt.recipe.mixer", "u01/claus")
                .fluidIn("GenGas_HydrogenSulfide", 10, "Oxygen", 5)
                .out(dust("Sulfur", 1))
                .fluidOut("DistW", 10)
                .add(80, 24);
    }

    private static void lightEnds() {
        // C3 leaves as fuel; C4 and C5 rejoin the existing petrochemical separation instead of
        // becoming a second, competing light-ends chain.
        row("gt.recipe.distillationtower", "u01/stabilize")
                .fluidIn("GenGas_SweetNaturalGas", 1000)
                .fluidOut("GenGas_PipelineGas", 850, "Propane", 70, "GenGas_CFourFraction", 50)
                .fluidOut("GenLiquid_CFiveFraction", 30)
                .add(120, 48);
        // Methane-quality feed for the existing steam-reforming route and future U-14 hydrogen.
        row("gt.recipe.distillery", "u01/pipeline-methane")
                .in(circuit(3))
                .fluidIn("GenGas_PipelineGas", 1000)
                .fluidOut("Methane", 1000)
                .keep(0)
                .add(60, 24);
    }

    private static void pipelineFuel() {
        // 32 ticks for 5 mB is 409.6 EU/mB: the 15% of raw volume removed as water, acid gas and
        // light ends no longer has to be carried through the turbine. Exhaust per mB stays at the
        // host methane/natural-gas ratio, so the batch ledger shows lower exhaust per delivered EU.
        row("gt.recipe.fuels.gas", "u01/fuel/pipeline-gas")
                .fluidIn("GenGas_PipelineGas", 5)
                .fluidOut("DistW", 6, "CarbonDioxide", 3)
                .add(32, -64);
    }
}
