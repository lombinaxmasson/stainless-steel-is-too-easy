package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.vanillaItem;

/**
 * M9-06 die packaging and board-level assembly.
 *
 * <p>The elite packaged die is the first tier. Master and ultimate keep the host parts route until
 * their dies are added; the host {@code circuit_part_elite} and {@code circuit_part_ultimate} have
 * no press rows in this port, so the board step uses {@code circuit_part_master}, the highest part
 * that can actually be made.</p>
 *
 * <p>The final assembly lives in the Welder rather than the Bath. A {@code gt6ce:machine_removal}
 * matches every row with the elite board as an input, and the overlay also applies to addon rows,
 * so an addon Bath row would delete itself. The Welder is outside that map and has a soldering
 * fluid slot.</p>
 */
public final class PackagingRecipes {
    private static final String PROCESSED = "ProcessedWafer";
    private static final String DIE = "ChipDieElite";
    private static final String CHIP = "ChipElite";

    private PackagingRecipes() {}

    static void add() {
        moldingCompound();
        leadFrame();
        dice();
        dieAttach();
        wireBond();
        mold();
        boardAssembly();
    }

    private static void moldingCompound() {
        row("gt.recipe.mixer", "m9-06/molding-compound")
                .in(dust("EpoxyCresolNovolac", 1), dust("SiliconDioxide", 3),
                        item("dustTiny", "Dicyandiamide", 1))
                .out(dust("MoldingCompound", 4))
                .add(240, 32);
    }

    /** Tin plating makes the copper-alloy strip solderable; the press stamps the lead pattern. */
    private static void leadFrame() {
        row("gt.recipe.press", "m9-06/lead-frame")
                .in(item("plate", "Brass", 1), item("dustTiny", "Tin", 1))
                .out(item("plate", "LeadFrame", 1))
                .add(64, 16);
    }

    private static void dice() {
        row("gt.recipe.cutter", "m9-06/dice/processed-wafer")
                .in(item("wafer", PROCESSED, 1))
                .fluidIn("GenLiquid_UltrapureWater", 1000)
                .out(item("die", DIE, 8))
                .add(240, 64);
    }

    private static void dieAttach() {
        row("gt.recipe.welder", "m9-06/die-attach")
                .in(item("die", DIE, 1), item("plate", "LeadFrame", 1))
                .fluidIn("GenMolten_SolderingAlloy", 72)
                .out(item("plate", "DieAttachedFrame", 1))
                .add(120, 32);
    }

    private static void wireBond() {
        row("gt.recipe.welder", "m9-06/wire-bond")
                .in(item("plate", "DieAttachedFrame", 1), item("wireFine", "Gold", 8))
                .out(item("plate", "BondedFrame", 1))
                .add(180, 48);
    }

    private static void mold() {
        row("gt.recipe.injector", "m9-06/mold")
                .in(item("plate", "BondedFrame", 1), dust("MoldingCompound", 1))
                .fluidIn("Nitrogen", 1000)
                .out(item("chip", CHIP, 1))
                .add(240, 96);
    }

    private static void boardAssembly() {
        row("gt.recipe.welder", "m9-06/board-assembly/elite")
                .in(vanillaItem("gt6ce:circuit_board_elite", 1),
                        item("chip", CHIP, 4),
                        vanillaItem("gt6ce:circuit_part_master", 4))
                .fluidIn("GenMolten_SolderingAlloy", 288)
                .out(vanillaItem("gt6ce:circuit_elite", 1))
                .add(240, 64);
    }
}
