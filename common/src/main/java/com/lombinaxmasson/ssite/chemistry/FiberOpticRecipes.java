package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

/**
 * P-02 optical glass, preform and fiber drawing.
 *
 * <p>One preform is a ten-unit glass batch. Drawing converts it into eight bare fibers and two
 * units of scrap; scrap recovery returns half a unit of frit per unit, so recycling can never
 * multiply germanium or glass. The drawing row keeps the P-01 micro-hole nozzle as a running
 * tool; the machine's crafting recipe does not contain the nozzle, which keeps the build and the
 * running-tool consumption separate in the material report.</p>
 */
public final class FiberOpticRecipes {
    private FiberOpticRecipes() {}

    static void add() {
        glassFrit();
        preform();
        drawing();
        coating();
        spooling();
        scrapRecovery();
    }

    private static void glassFrit() {
        row("gt.recipe.mixer", "p02/optical-glass-frit")
                .in(dust("SiliconDioxide", 8), dust("GermaniumDioxide", 2))
                .out(dust("OpticalGlassFrit", 10))
                .add(200, 64);
    }

    private static void preform() {
        row("gt.recipe.calciner", "p02/preform")
                .in(dust("OpticalGlassFrit", 10))
                .fluidIn("GenGas_Argon", 250)
                .out(item("fiberPreform", "OpticalFiber", 1))
                .add(240, 96);
    }

    private static void drawing() {
        row("ssite.recipe.fiber_drawing", "p02/draw-fiber")
                .in(item("fiberPreform", "OpticalFiber", 1),
                        item("precisionNozzle", "StainlessSteel316L", 1))
                .fluidIn("GenGas_Argon", 1000)
                .out(item("bareFiber", "OpticalFiber", 8), dust("OpticalGlassScrap", 2))
                .keep(1)
                .add(240, 128);
    }

    private static void coating() {
        row("ssite.recipe.fiber_drawing", "p02/coat-fiber")
                .in(item("bareFiber", "OpticalFiber", 8), item("ingot", "SiliconeRubber", 1))
                .out(item("coatedFiber", "OpticalFiber", 8))
                .add(160, 96);
    }

    private static void spooling() {
        row("ssite.recipe.fiber_drawing", "p02/spool-fiber")
                .in(item("coatedFiber", "OpticalFiber", 8), item("plate", "StainlessSteel316L", 1))
                .out(item("fiberSpool", "OpticalFiber", 1))
                .add(120, 64);
    }

    /** Two units of scrap return one unit of frit; the 50% loss prevents a closed recycling loop. */
    private static void scrapRecovery() {
        row("gt.recipe.calciner", "p02/scrap-recovery")
                .in(dust("OpticalGlassScrap", 2))
                .fluidIn("GenGas_Argon", 250)
                .out(dust("OpticalGlassFrit", 1))
                .add(100, 32);
    }
}
