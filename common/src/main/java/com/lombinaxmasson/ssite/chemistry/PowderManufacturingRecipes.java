package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

import com.lombinaxmasson.ssite.machine.PowderBedCore;
import com.lombinaxmasson.ssite.material.PowderManufacturingMaterials;

/**
 * P-05 powder, blank and cooling-block route.
 *
 * <p>Ordinary {@code dust StainlessSteel316L} is only milled into raw powder; the printer accepts
 * only classified powder. Classification, reclaim reclassification, tailings and support recovery
 * all lose material, so no recycling loop returns as much as it takes. The printed blank is
 * desupported to a second blank id before heat treatment, and the cooling assembly is the build
 * component that unlocks the P-01 cooled batch mode and the P-03 module parallel upgrade.</p>
 */
public final class PowderManufacturingRecipes {
    private static final String ALLOY = PowderManufacturingMaterials.ALLOY;

    private PowderManufacturingRecipes() {}

    static void add() {
        powder();
        recycling();
        printing();
        finishing();
        coolingAssembly();
    }

    private static void powder() {
        // Ordinary powder is the feedstock, not a qualified powder.
        row("gt.recipe.shredder", "p05/powder-mill")
                .in(dust(ALLOY, 1))
                .out(item("rawPowder", ALLOY, 1))
                .add(80, 32);
        // Half the milled charge is out of the printable size band.
        row("gt.recipe.sifter", "p05/classify")
                .in(item("rawPowder", ALLOY, 2))
                .out(item("classifiedPowder", ALLOY, 1),
                        item("screenTailings", ALLOY, 1))
                .add(120, 48);
    }

    private static void recycling() {
        row("gt.recipe.sifter", "p05/reclaim-classify")
                .in(item("reclaimPowder", ALLOY, 2))
                .out(item("classifiedPowder", ALLOY, 1),
                        item("screenTailings", ALLOY, 1))
                .add(120, 48);
        // The host shredder already accepts these metal forms; sifting avoids shadowing its rows.
        row("gt.recipe.sifter", "p05/tailings-recycle")
                .in(item("screenTailings", ALLOY, 2))
                .out(item("rawPowder", ALLOY, 1))
                .add(80, 32);
        row("gt.recipe.sifter", "p05/support-recycle")
                .in(item("printedSupport", ALLOY, 2))
                .out(item("rawPowder", ALLOY, 1))
                .add(80, 32);
    }

    private static void printing() {
        // Display-only: PowderBedCore owns reservation, gas, progress and the two settlements.
        row(PowderBedCore.MAP, "p05/print")
                .in(item("classifiedPowder", ALLOY, PowderBedCore.POWDER_PER_BATCH),
                        item("controlModule", PowderBedCore.MODULE_MATERIAL, 1),
                        item("coolingAssembly", ALLOY, 1))
                .fluidIn("GenGas_Argon", PowderBedCore.ARGON_PER_BATCH)
                .out(item("printedBlank", ALLOY, 1),
                        item("reclaimPowder", ALLOY, PowderBedCore.RECLAIM_PER_BATCH))
                .keep(1, 2)
                .display(PowderBedCore.TICKS_PER_BATCH, PowderBedCore.EU_PER_TICK);
    }

    private static void finishing() {
        row("gt.recipe.cutter", "p05/desupport")
                .in(item("printedBlank", ALLOY, 1))
                .fluidIn("DistW", 100)
                .out(item("desupportedBlank", ALLOY, 1),
                        item("printedSupport", ALLOY, 2))
                .add(200, 64);
        // The independent desupported blank id is the only input the heat treatment accepts.
        row("gt.recipe.calciner", "p05/heat-treat")
                .in(item("desupportedBlank", ALLOY, 1))
                .fluidIn("GenGas_Argon", 2000)
                .out(item("coolingBlock", ALLOY, 1))
                .add(600, 128);
    }

    private static void coolingAssembly() {
        row("gt.recipe.welder", "p05/cooling-assembly")
                .in(item("coolingBlock", ALLOY, 1),
                        item("precisionNozzle", ALLOY, 1),
                        item("wireFine", "Copper", 4))
                .fluidIn("GenGas_Argon", 500)
                .out(item("coolingAssembly", ALLOY, 1))
                .add(300, 128);
    }
}
