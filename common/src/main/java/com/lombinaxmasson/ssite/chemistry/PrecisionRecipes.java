package com.lombinaxmasson.ssite.chemistry;

import com.lombinaxmasson.ssite.material.EngineeringMaterials;

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
        cooledDrilling();
        highLoadDrilling();
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

    /**
     * P-05 cooling: a distinct mode that keeps the cooling assembly instead of the drilling head.
     * The basic head row remains the single-piece mode; this row does not contain its inputs, so
     * neither recipe shadows the other. Per-nozzle material and energy are unchanged.
     */
    private static void cooledDrilling() {
        row("ssite.recipe.precision_laser", "p05/cooled-drill-nozzles")
                .in(item("plate", "StainlessSteel316L", 3),
                        item("coolingAssembly", "StainlessSteel316L", 1))
                .fluidIn("GenGas_Argon", 2000)
                .out(item("precisionNozzle", "StainlessSteel316L", 3))
                .keep(1)
                .add(720, 512);
    }

    /**
     * P-06C high-load drilling: one qualified ablative backing per six-nozzle batch. The backing is
     * the replaceable batch consumable and the mode interlock; it is consumed and returned as a
     * distinct spent item. Material and energy per nozzle are identical to the single-piece row,
     * so the batch is not a free multiplier. The host recipe machine checks both output stacks
     * before starting and keeps the batch progress across a reload.
     */
    private static void highLoadDrilling() {
        row("ssite.recipe.precision_laser", "p06/high-load-drill")
                .in(item("plate", "StainlessSteel316L", 6),
                        item("ablativeBacking", EngineeringMaterials.ABLATIVE, 1))
                .fluidIn("GenGas_Argon", 6000)
                .out(item("precisionNozzle", "StainlessSteel316L", 6),
                        item("ablativeBackingSpent", EngineeringMaterials.ABLATIVE, 1))
                .add(1440, 512);
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
