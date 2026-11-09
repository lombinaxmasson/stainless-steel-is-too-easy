package com.lombinaxmasson.ssite.chemistry;

import com.lombinaxmasson.ssite.material.EngineeringMaterials;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.circuit;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

/**
 * P-06 engineering ceramic, carbon-carbon and ablative backing routes.
 *
 * <p>The ceramic chain is batch, green body, sintered body and machined clamp. Sintering returns
 * trim alumina, so the loop loses material instead of minting it. Carbon-carbon is a finite
 * densification sequence with a separate id for every state; the ablative route uses phenolic
 * resin and curing, so it cannot be substituted for the carbonized carbon-carbon route. The spent
 * backing only yields carbon dust, never a new backing.</p>
 *
 * <p>All steps use existing host machines: mixer, press, calciner, cutter, bath, autoclave and
 * shredder. No new furnace is introduced.</p>
 */
public final class EngineeringMaterialRecipes {
    private static final String CERAMIC = EngineeringMaterials.CERAMIC;
    private static final String CARBON_CARBON = EngineeringMaterials.CARBON_CARBON;
    private static final String ABLATIVE = EngineeringMaterials.ABLATIVE;

    private EngineeringMaterialRecipes() {}

    static void add() {
        engineeringCeramic();
        carbonCarbon();
        ablativeComposite();
    }

    private static void engineeringCeramic() {
        row("gt.recipe.mixer", "p06a/ceramic-batch")
                .in(dust("Alumina", 4))
                .fluidIn("DistW", 1000)
                .out(item("ceramicBatch", CERAMIC, 4))
                .add(120, 32);
        // The press table requires two item inputs; the kept circuit is the pressing program.
        row("gt.recipe.press", "p06a/ceramic-green-body")
                .in(item("ceramicBatch", CERAMIC, 2), circuit(1))
                .keep(1)
                .out(item("ceramicGreenBody", CERAMIC, 1))
                .add(120, 48);
        // Two green bodies yield one sintered body plus trim alumina; the returned dust is smaller
        // than the original charge so the loop cannot grow.
        row("gt.recipe.calciner", "p06a/ceramic-sinter")
                .in(item("ceramicGreenBody", CERAMIC, 2))
                .fluidIn("Oxygen", 1000)
                .out(item("ceramicSintered", CERAMIC, 1), dust("Alumina", 1))
                .add(400, 96);
        row("gt.recipe.cutter", "p06a/ceramic-clamp")
                .in(item("ceramicSintered", CERAMIC, 1))
                .fluidIn("DistW", 100)
                .out(item("ceramicClamp", CERAMIC, 1))
                .add(240, 64);
    }

    private static void carbonCarbon() {
        row("gt.recipe.press", "p06b/preform")
                .in(item("wireFine", "CarbonFiber", 16), dust("Carbon", 1))
                .out(item("carbonFiberPreform", CARBON_CARBON, 1))
                .add(200, 48);
        row("gt.recipe.mixer", "p06b/impregnate")
                .in(item("carbonFiberPreform", CARBON_CARBON, 1), dust("Carbon", 2))
                .fluidIn("DistW", 1000)
                .out(item("carbonCarbonImpregnated", CARBON_CARBON, 1))
                .fluidOut("GenLiquid_DirtyWater", 1000)
                .add(200, 48);
        row("gt.recipe.calciner", "p06b/carbonize")
                .in(item("carbonCarbonImpregnated", CARBON_CARBON, 1))
                .fluidIn("Nitrogen", 1000)
                .out(item("carbonCarbonCarbonized", CARBON_CARBON, 1))
                .add(600, 128);
        // The second impregnation and carbonization are folded into one furnace pass because the
        // host calciner has two item inputs; the state id still records that this cycle happened.
        row("gt.recipe.calciner", "p06b/densify")
                .in(item("carbonCarbonCarbonized", CARBON_CARBON, 1), dust("Carbon", 2))
                .fluidIn("Nitrogen", 1000)
                .out(item("carbonCarbonDensified", CARBON_CARBON, 1))
                .add(600, 128);
        row("gt.recipe.cutter", "p06b/setter")
                .in(item("carbonCarbonDensified", CARBON_CARBON, 1))
                .fluidIn("DistW", 200)
                .out(item("carbonCarbonSetter", CARBON_CARBON, 1))
                .add(300, 64);
    }

    private static void ablativeComposite() {
        // Phenolic curing is the ablative route; it never shares an intermediate with the
        // carbonization route above.
        row("gt.recipe.bath", "p06c/billet")
                .in(item("foil", "CarbonFiber", 4))
                .fluidIn("GenLiquid_PhenolicResin", 1000)
                .out(item("ablativeBillet", ABLATIVE, 1))
                .add(200, 48);
        // The autoclave table requires two item inputs; the kept circuit is the cure program.
        row("gt.recipe.autoclave", "p06c/cure")
                .in(item("ablativeBillet", ABLATIVE, 1), circuit(2))
                .keep(1)
                .fluidIn("Nitrogen", 1000)
                .out(item("ablativeBackingBlank", ABLATIVE, 1))
                .add(400, 64);
        row("gt.recipe.cutter", "p06c/machine")
                .in(item("ablativeBackingBlank", ABLATIVE, 1))
                .fluidIn("DistW", 100)
                .out(item("ablativeBacking", ABLATIVE, 1))
                .add(300, 48);
        // A spent backing returns only part of its carbon; no row turns it back into a backing.
        row("gt.recipe.shredder", "p06c/spent-recovery")
                .in(item("ablativeBackingSpent", ABLATIVE, 1))
                .out(dust("Carbon", 1))
                .add(120, 32);
    }
}
