package com.lombinaxmasson.ssite.chemistry;

import com.lombinaxmasson.ssite.machine.ElectronBeamCore;
import com.lombinaxmasson.ssite.machine.IonImplanterCore;
import com.lombinaxmasson.ssite.material.AdvancedSemiconductorMaterials;
import com.lombinaxmasson.ssite.material.EngineeringMaterials;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.vanillaItem;

/**
 * P-03 advanced semiconductor route.
 *
 * <p>The route starts from the existing polished {@code wafer Silicon} and gives every stage its
 * own advanced item. The old {@code ProcessedWafer} only feeds the Elite packaging chain and has no
 * row into these ids. The mask exposure and the electron-beam exposure converge on the same
 * developed state, so neither path can skip develop, etch, implant, anneal or packaging.</p>
 *
 * <p>The first wet row uses ozone rather than the M9-05 ammonia clean so the two Bath rows do not
 * share an input set. The ion implanter and electron-beam writer own real behavior cores; their
 * rows here are display-only and mirror the cores' material, energy and time values.</p>
 */
public final class AdvancedSemiconductorRecipes {
    private static final String WAFER = "advancedWafer";

    private AdvancedSemiconductorRecipes() {}

    static void add() {
        lithography();
        implant();
        anneal();
        metalize();
        dice();
        packaging();
        module();
        templates();
        electronBeam();
    }

    private static void lithography() {
        row("gt.recipe.bath", "p03/clean")
                .in(item("wafer", "Silicon", 1))
                .fluidIn("GenGas_Ozone", 1000)
                .out(item(WAFER, AdvancedSemiconductorMaterials.CLEANED, 1))
                .fluidOut("Dirty_Water", 1000)
                .add(200, 32);
        row("gt.recipe.calciner", "p03/oxidize")
                .in(item(WAFER, AdvancedSemiconductorMaterials.CLEANED, 1))
                .fluidIn("Oxygen", 1000, "Nitrogen", 1000)
                .out(item(WAFER, AdvancedSemiconductorMaterials.OXIDIZED, 1))
                .add(600, 64);
        row("gt.recipe.bath", "p03/coat")
                .in(item(WAFER, AdvancedSemiconductorMaterials.OXIDIZED, 1))
                .fluidIn("GenLiquid_Photoresist", 1000)
                .out(item(WAFER, AdvancedSemiconductorMaterials.COATED, 1))
                .add(200, 32);
        // The physical mask is a kept tool, matching the M9-05 exposure row.
        row("gt.recipe.laserengraver", "p03/expose-mask")
                .in(item(WAFER, AdvancedSemiconductorMaterials.COATED, 1),
                        item("plate", "PhotomaskPlate", 1))
                .out(item(WAFER, AdvancedSemiconductorMaterials.EXPOSED, 1))
                .keep(1)
                .add(600, 256);
        row("gt.recipe.bath", "p03/develop-mask")
                .in(item(WAFER, AdvancedSemiconductorMaterials.EXPOSED, 1))
                .fluidIn("GenLiquid_PhotoresistDeveloper", 1000)
                .out(item(WAFER, AdvancedSemiconductorMaterials.DEVELOPED, 1))
                .fluidOut("Dirty_Water", 1000)
                .add(200, 32);
        row("gt.recipe.bath", "p03/develop-ebeam")
                .in(item(WAFER, AdvancedSemiconductorMaterials.EBEAM_EXPOSED, 1))
                .fluidIn("GenLiquid_PhotoresistDeveloper", 1000)
                .out(item(WAFER, AdvancedSemiconductorMaterials.DEVELOPED, 1))
                .fluidOut("Dirty_Water", 1000)
                .add(200, 32);
        row("gt.recipe.bath", "p03/etch")
                .in(item(WAFER, AdvancedSemiconductorMaterials.DEVELOPED, 1))
                .fluidIn("GenLiquid_BufferedHydrofluoricAcid", 1000)
                .out(item(WAFER, AdvancedSemiconductorMaterials.ETCHED, 1))
                .fluidOut("Dirty_Water", 1000)
                .add(300, 48);
        row("gt.recipe.bath", "p03/strip")
                .in(item(WAFER, AdvancedSemiconductorMaterials.ETCHED, 1))
                .fluidIn("GenLiquid_SulfuricAcid", 1000)
                .out(item(WAFER, AdvancedSemiconductorMaterials.STRIPPED, 1))
                .fluidOut("Dirty_Water", 1000)
                .add(300, 48);
    }

    private static void implant() {
        // Display-only: IonImplanterCore performs this step in the behavior machine.
        row(IonImplanterCore.MAP, "p03/implant")
                .in(item(WAFER, AdvancedSemiconductorMaterials.STRIPPED, 1), dust("Phosphorus", 1))
                .fluidIn("GenGas_Argon", IonImplanterCore.ARGON_PER_BATCH)
                .out(item(WAFER, AdvancedSemiconductorMaterials.IMPLANTED, 1))
                .display(IonImplanterCore.TICKS_PER_BATCH, IonImplanterCore.EU_PER_TICK);
    }

    private static void anneal() {
        // Only the implanted state can be annealed; a stripped wafer cannot enter this row.
        row("gt.recipe.calciner", "p03/anneal")
                .in(item(WAFER, AdvancedSemiconductorMaterials.IMPLANTED, 1))
                .fluidIn("GenGas_Argon", 1000)
                .out(item(WAFER, AdvancedSemiconductorMaterials.ANNEALED, 1))
                .add(600, 64);
    }

    private static void metalize() {
        row("gt.recipe.calciner", "p03/metalize")
                .in(item(WAFER, AdvancedSemiconductorMaterials.ANNEALED, 1), dust("Aluminium", 1))
                .fluidIn("GenGas_Argon", 1000)
                .out(item(WAFER, AdvancedSemiconductorMaterials.PROCESSED, 1))
                .add(900, 96);
    }

    private static void dice() {
        // The old ProcessedWafer cannot reach this row; only the advanced processed state can.
        row("gt.recipe.cutter", "p03/dice")
                .in(item(WAFER, AdvancedSemiconductorMaterials.PROCESSED, 1))
                .fluidIn("GenLiquid_UltrapureWater", 1000)
                .out(item("advancedDie", AdvancedSemiconductorMaterials.DIE, 8))
                .add(240, 64);
    }

    private static void packaging() {
        // Copper and silver keep this row from being a superset of the M9-06 Brass+Tin frame.
        row("gt.recipe.press", "p03/lead-frame")
                .in(item("plate", "Copper", 1), item("dustTiny", "Tin", 1),
                        item("dustTiny", "Silver", 1))
                .out(item("plate", AdvancedSemiconductorMaterials.LEAD_FRAME, 1))
                .add(64, 16);
        row("gt.recipe.welder", "p03/die-attach")
                .in(item("advancedDie", AdvancedSemiconductorMaterials.DIE, 1),
                        item("plate", AdvancedSemiconductorMaterials.LEAD_FRAME, 1))
                .fluidIn("GenMolten_SolderingAlloy", 72)
                .out(item("plate", AdvancedSemiconductorMaterials.DIE_ATTACHED, 1))
                .add(120, 32);
        row("gt.recipe.welder", "p03/wire-bond")
                .in(item("plate", AdvancedSemiconductorMaterials.DIE_ATTACHED, 1),
                        item("wireFine", "Gold", 8))
                .out(item("plate", AdvancedSemiconductorMaterials.BONDED, 1))
                .add(180, 48);
        row("gt.recipe.injector", "p03/mold")
                .in(item("plate", AdvancedSemiconductorMaterials.BONDED, 1), dust("MoldingCompound", 1))
                .fluidIn("Nitrogen", 1000)
                .out(item("advancedChip", AdvancedSemiconductorMaterials.CHIP, 1))
                .add(240, 96);
    }

    private static void module() {
        // The first module is an add-on component; it does not replace the host circuit_master route.
        // P-06 replaces the temporary P-01 fixture with the insulating ceramic wafer clamp.
        row("gt.recipe.welder", "p03/control-module")
                .in(item("advancedChip", AdvancedSemiconductorMaterials.CHIP, 1),
                        vanillaItem("gt6ce:circuit_elite", 1),
                        item("ceramicClamp", EngineeringMaterials.CERAMIC, 1),
                        item("instrumentShell", "StainlessSteel316L", 1))
                .fluidIn("GenMolten_SolderingAlloy", 288)
                .out(item("controlModule", AdvancedSemiconductorMaterials.MODULE, 1))
                .add(300, 128);
    }

    private static void templates() {
        // The template is a written data carrier, not a physical mask; the writer keeps it.
        // The host laser engraver has no fluid input tank, so the write row uses only items.
        row("gt.recipe.laserengraver", "p03/program-template")
                .in(vanillaItem("gt6ce:circuit_elite", 1), dust("Chromium", 1))
                .out(item("programTemplate", AdvancedSemiconductorMaterials.TEMPLATE, 1))
                .add(600, 128);
    }

    private static void electronBeam() {
        // Display-only: ElectronBeamCore owns the whitelist and the real ledger.
        row(ElectronBeamCore.MAP, "p03/ebeam")
                .in(item(WAFER, AdvancedSemiconductorMaterials.COATED, 1),
                        item("programTemplate", AdvancedSemiconductorMaterials.TEMPLATE, 1))
                .out(item(WAFER, AdvancedSemiconductorMaterials.EBEAM_EXPOSED, 1))
                .keep(1)
                .display(ElectronBeamCore.TICKS_PER_BATCH, ElectronBeamCore.EU_PER_TICK);
    }
}
