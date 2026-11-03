package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.circuit;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

import com.lombinaxmasson.ssite.machine.CoalGasifierCore;

/**
 * U-01 natural-gas treatment, U-03 gasification and U-04 methanation. Raw {@code Gas_Natural} from
 * the host spring is dehydrated with triethylene glycol, sweetened with MDEA and stabilized into
 * pipeline gas; light ends keep feeding the existing C4/C5 chains and the acid gas is recovered as
 * sulfur.
 *
 * <p>Both solvents are regenerated at 95% per pass. The one-millibucket make-up per 1000 mB of raw
 * gas is covered by the synthesis rows below, so the treatment train has no consumable dead end.
 *
 * <p>U-03 adds the gasifier display rows plus raw-gas cleaning, clean-gas separation and the two
 * by-product sinks. Neither raw nor clean gas receives a fuel row; their energy is carried only by
 * the methanol and aromatic products.
 *
 * <p>U-04 reverses the existing steam-reforming row into the host methane identity. The nickel
 * catalyst is retained and the row still spends EU, so reforming plus methanation loses energy and
 * cannot mint fuel.
 *
 * <p>U-05a synthesizes one Fischer-Tropsch oil from the U-03 carbon monoxide/hydrogen split and
 * fractionates it into existing naphtha, host diesel and host wax. U-05b adds no recipe: the host
 * fermenter already turns the biomass from the water chain into methane, so a second biogas or
 * methane identity would only duplicate that route.
 */
public final class UtilityRecipes {
    private UtilityRecipes() {}

    static void add() {
        solventMakeup();
        gasTreatment();
        lightEnds();
        pipelineFuel();
        syngas();
        methanation();
        fischerTropsch();
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

    /**
     * U-03 steam gasification. Both charges release the same raw-gas volume, so the cleaning and
     * separation rows below are shared; coke only saves steam and time. Raw and clean gas stay
     * separate fluids and no fuel row is registered for either, so their chemical energy never
     * turns into a second, unaccounted heat source.
     */
    private static void syngas() {
        // Display-only: CoalGasifierCore owns the real reservation and batch ledger.
        row(CoalGasifierCore.MAP, "u03/gasify-coal")
                .in(dust("Coal", 1))
                .fluidIn("Steam", (int) CoalGasifierCore.COAL_STEAM)
                .out(item("dustSmall", "CoalSlag", 1))
                .fluidOut("GenGas_CrudeSyngas", (int) CoalGasifierCore.RAW_GAS)
                .display(CoalGasifierCore.COAL_TICKS, CoalGasifierCore.EU_PER_TICK);
        row(CoalGasifierCore.MAP, "u03/gasify-coke")
                .in(dust("CoalCoke", 1))
                .fluidIn("Steam", (int) CoalGasifierCore.COKE_STEAM)
                .out(item("dustSmall", "CoalSlag", 1))
                .fluidOut("GenGas_CrudeSyngas", (int) CoalGasifierCore.RAW_GAS)
                .display(CoalGasifierCore.COKE_TICKS, CoalGasifierCore.EU_PER_TICK);

        // Cooling and fines removal: 4000 raw gas becomes 3000 clean gas plus tar, condensate and
        // fly ash. The tar and slag rows below are the by-products' only sink.
        row("gt.recipe.centrifuge", "u03/crude-gas-purify")
                .out(item("dustTiny", "DarkAshes", 1))
                .fluidIn("GenGas_CrudeSyngas", 4_000)
                .fluidOut("GenGas_CleanSyngas", 3_000, "GenLiquid_CoalTar", 100, "DistW", 900)
                .add(80, 32);

        // Exactly the CO/H2 ratio the existing methanol row consumes. The distillery map requires
        // one item input, so the circuit is kept and distinguishes this row from the U-01 rows.
        row("gt.recipe.distillery", "u03/clean-gas-separate")
                .in(circuit(4))
                .fluidIn("GenGas_CleanSyngas", 3_000)
                .fluidOut("GenGas_CarbonMonoxide", 1_000, "Hydrogen", 2_000)
                .keep(0)
                .add(60, 32);

        // Coal tar returns to the existing aromatic chain instead of becoming a second fuel.
        row("gt.recipe.distillationtower", "u03/tar-distill")
                .fluidIn("GenLiquid_CoalTar", 1_000)
                .fluidOut("GenLiquid_Naphtha", 600, "GenLiquid_Phenol", 300, "GenLiquid_Benzene", 100)
                .add(120, 48);

        // Slag is a real solid residue, not a display item: four batches (four dustSmall) recover a
        // fraction of their oxides and iron, with the remainder left as unrecovered residue.
        row("gt.recipe.centrifuge", "u03/slag-recover")
                .in(item("dustSmall", "CoalSlag", 4))
                .out(item("dustSmall", "SiliconDioxide", 1), item("dustTiny", "Alumina", 1),
                        item("dustTiny", "Iron", 1))
                .add(200, 16);
    }

    /**
     * U-04 Sabatier methanation, the reverse of {@code c1/methane-reforming}: one part carbon
     * monoxide and three parts hydrogen return the host methane identity plus water. The nickel
     * catalyst is retained, and the row still spends EU, so the reforming round trip is strictly
     * lossy. Water leaves as distilled water after cooling; no second methane fluid and no fuel or
     * HU row are created.
     */
    private static void methanation() {
        row("gt.recipe.catalyticcracking", "u04/methanation")
                .in(dust("Nickel", 1))
                .fluidIn("GenGas_CarbonMonoxide", 1_000, "Hydrogen", 3_000)
                .fluidOut("Methane", 1_000, "DistW", 1_000)
                .keep(0)
                .add(160, 48);
    }

    /**
     * U-05a Fischer-Tropsch. The synthesis consumes exactly the carbon monoxide and hydrogen that
     * U-03 separates from clean syngas, so the two cards close into one chain without a second
     * syngas split. Iron is the first-generation catalyst and is retained. The oil is not a fuel
     * itself: it must pass the fractionation below, which keeps its energy in the finished streams.
     */
    private static void fischerTropsch() {
        row("gt.recipe.catalyticcracking", "u05a/ft-synthesis")
                .in(dust("Iron", 1))
                .fluidIn("GenGas_CarbonMonoxide", 1_000, "Hydrogen", 2_000)
                .fluidOut("GenLiquid_SyntheticOil", 1_000, "DistW", 1_000)
                .keep(0)
                .add(200, 64);

        // One tower pass yields the existing naphtha and host diesel plus paraffin wax for M9-09.
        // Naphtha and diesel are not re-registered: their fuel identity stays the host's.
        row("gt.recipe.distillationtower", "u05a/ft-fractionate")
                .fluidIn("GenLiquid_SyntheticOil", 1_000)
                .fluidOut("GenLiquid_Naphtha", 300, "Diesel", 450)
                .out(item("dust", "Wax", 1))
                .add(160, 48);
    }
}
