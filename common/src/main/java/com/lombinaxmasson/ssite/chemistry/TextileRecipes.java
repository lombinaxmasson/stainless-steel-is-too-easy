package com.lombinaxmasson.ssite.chemistry;

import java.util.Locale;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.circuit;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

/**
 * M9-08 functional textiles and composite parts.
 *
 * <p>Nomex spinning copies the host Kevlar route; UHMWPE already has fine wire from
 * {@code polymerForming}, so it only needs the loom. The card's laminator row for the insulation
 * stack has three item inputs and the Laminator takes two, so that stack is pressed instead. The
 * prepreg resin is a fluid and the Laminator has no fluid slot, so the epoxy bath carries it. The
 * autoclave needs two item stacks and one fluid input, hence the programmed circuit and the argon
 * charge. No machine is added by this card.</p>
 *
 * <p>Nothing consumes the cured composite or the felt yet: M9-09's tooling is the named consumer and
 * the card is closed as a downstream interface until that card defines the rows.</p>
 */
public final class TextileRecipes {
    private TextileRecipes() {}

    static void add() {
        nomexCloth();
        ultraHighMolecularWeightPolyethyleneCloth();
        aramidFelt();
        multilayerInsulation();
        carbonFiberComposite();
        braidedHose();
    }

    private static void nomexCloth() {
        row("gt.recipe.bath", "m9-08/nomex-spin")
                .in(dust("Nomex", 1))
                .out(item("wireFine", "Nomex", 4))
                .fluidIn("GenLiquid_SulfuricAcid", 1000)
                .fluidOut("GenLiquid_SulfuricAcid", 950)
                .add(200, 64);
        row("gt.recipe.loom", "m9-08/nomex-cloth")
                .in(item("wireFine", "Nomex", 8))
                .out(item("plate", "Nomex", 1))
                .add(200, 32);
    }

    /** UHMWPE fine wire comes from the M4 forming rows; the loom only weaves it into cloth. */
    private static void ultraHighMolecularWeightPolyethyleneCloth() {
        row("gt.recipe.loom", "m9-08/uhmwpe-cloth")
                .in(item("wireFine", "UltraHighMolecularWeightPolyethylene", 8))
                .out(item("plate", "UltraHighMolecularWeightPolyethylene", 1))
                .add(200, 32);
    }

    private static void aramidFelt() {
        row("gt.recipe.press", "m9-08/aramid-felt")
                .in(item("wireFine", "Nomex", 4), dust("Epoxy", 1))
                .out(item("plate", "AramidFelt", 1))
                .add(160, 32);
    }

    private static void multilayerInsulation() {
        row("gt.recipe.press", "m9-08/multilayer-insulation")
                .in(item("foil", "MetallizedPolyester", 1), item("plate", "Nomex", 1),
                        item("foil", "Aluminium", 1))
                .out(item("plate", "MultilayerInsulation", 2))
                .add(160, 48);
    }

    private static void carbonFiberComposite() {
        row("gt.recipe.bath", "m9-08/carbon-fiber-prepreg")
                .in(item("foil", "CarbonFiber", 1))
                .fluidIn("GenLiquid_EpoxyResin", 500)
                .out(item("foil", "CarbonFiberPrepreg", 1))
                .add(160, 32);
        row("gt.recipe.autoclave", "m9-08/carbon-fiber-composite")
                .in(item("foil", "CarbonFiberPrepreg", 4), circuit(1))
                .keep(1)
                .fluidIn("GenGas_Argon", 1000)
                .out(item("plate", "CarbonFiberComposite", 1))
                .add(600, 64);
    }

    /** Tube stock is extruded as slab stock here and braided with stainless wire on the loom. */
    private static void braidedHose() {
        for (String elastomer : new String[]{"Viton", "PolyvinylideneFluoride"}) {
            row("gt.recipe.loom", "m9-08/hose/" + elastomer.toLowerCase(Locale.ROOT))
                    .in(item("plate", elastomer, 2), item("wireFine", "StainlessSteel", 2))
                    .out(item("hose", elastomer, 1))
                    .add(240, 48);
        }
    }
}
