package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.circuit;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.vanillaItem;

/**
 * P-01 solid-state laser module and the three precision laser modes.
 *
 * <p>The module consumes the final coated {@code laserRod} and {@code opticalLens} that M9-V-03
 * froze; the rough and polished intermediates cannot substitute for them. The workstation rows
 * live on {@code ssite.recipe.precision_laser} and each carries a distinct non-consumed head plus
 * argon, so a wrong head or medium does not match. One recipe machine runs one row at a time,
 * which is the mode interlock; no separate behaviour core is involved.</p>
 */
public final class PrecisionRecipes {
    private PrecisionRecipes() {}

    static void add() {
        heads();
        laserModule();
        cutting();
        drilling();
        welding();
    }

    private static void heads() {
        String[] modes = {"cutting", "drilling", "welding"};
        for (int i = 0; i < modes.length; i++) {
            String mode = modes[i];
            String prefix = "laser" + Character.toUpperCase(mode.charAt(0)) + mode.substring(1) + "Head";
            // The three heads share one tool set; the non-consumed circuit keeps the rows distinct.
            row("gt.recipe.welder", "p01/head/" + mode)
                    .in(item("opticalLens", "NeodymiumYAG", 1),
                            item("plate", "PrecisionCasting", 1),
                            item("wireFine", "Gold", 4),
                            circuit(i + 1))
                    .fluidIn("GenGas_Argon", 500)
                    .out(item(prefix, "LaserHead", 1))
                    .add(180, 256);
        }
    }

    private static void laserModule() {
        row("gt.recipe.welder", "p01/laser-module")
                .in(item("laserRod", "NeodymiumYAG", 1),
                        item("opticalLens", "NeodymiumYAG", 2),
                        item("plate", "PrecisionCasting", 1),
                        item("chip", "ChipElite", 1),
                        vanillaItem("gt6ce:cable_01_copper", 4))
                .fluidIn("GenGas_Argon", 1000)
                .out(item("laserModule", "LaserModule", 1))
                .add(300, 512);
    }

    private static void cutting() {
        row("ssite.recipe.precision_laser", "p01/cut-fixture")
                .in(item("plate", "StainlessSteel316L", 1),
                        item("laserCuttingHead", "LaserHead", 1))
                .fluidIn("GenGas_Argon", 500)
                .out(item("precisionFixture", "StainlessSteel316L", 2))
                .keep(1)
                .add(200, 384);
    }

    private static void drilling() {
        row("ssite.recipe.precision_laser", "p01/drill-nozzle")
                .in(item("plate", "StainlessSteel316L", 1),
                        item("laserDrillingHead", "LaserHead", 1))
                .fluidIn("GenGas_Argon", 1000)
                .out(item("precisionNozzle", "StainlessSteel316L", 1))
                .keep(1)
                .add(240, 512);
    }

    private static void welding() {
        row("ssite.recipe.precision_laser", "p01/weld-shell")
                .in(item("plate", "StainlessSteel316L", 2),
                        item("laserWeldingHead", "LaserHead", 1))
                .fluidIn("GenGas_Argon", 1000)
                .out(item("instrumentShell", "StainlessSteel316L", 1))
                .keep(1)
                .add(300, 512);
    }
}
