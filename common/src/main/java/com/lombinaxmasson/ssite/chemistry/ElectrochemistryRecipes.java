package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.vanillaItem;

/**
 * M9-07 electrochemical materials and battery parts.
 *
 * <p>The storage segment feeds the five host cells. The host empty cells stay hand-craftable; the
 * industrial route assembles the same items from the electrode stack in
 * {@code data/ssite/recipes/cell_*_industrial.json} and then fills the cell with
 * {@code LithiumElectrolyte} on the Canner. The host acid/water fill rows are untouched, so both
 * electrolytes remain available and the 25 battery items and their multiblock tag do not change.</p>
 *
 * <p>The card's slurry and drying stages are folded into a dry blend followed by lamination: the
 * host Laminator requires two item inputs and has no fluid output, so a slurry could not be coated
 * there. The separator's pore extraction is folded into the press row for the same reason; the
 * Rolling Mill takes a single item and no fluid.</p>
 *
 * <p>The fuel-cell and redox-flow segment only produces materials. No fuel cell, flow battery or
 * electrolyser machine is registered here, and the card is closed as an interface for those.</p>
 */
public final class ElectrochemistryRecipes {
    private ElectrochemistryRecipes() {}

    static void add() {
        activeMaterials();
        electrodeSheets();
        separator();
        electrolyte();
        cellFilling();
        flowBatteryMaterials();
    }

    private static void activeMaterials() {
        // 2 Li2CO3 + 4 Co + 3 O2 -> 4 LiCoO2 + 2 CO2, halved.
        row("gt.recipe.calciner", "m9-07/lithium-cobalt-oxide")
                .in(dust("LithiumCarbonate", 1), dust("Cobalt", 2))
                .fluidIn("Oxygen", 1500)
                .out(dust("LithiumCobaltOxide", 2))
                .fluidOut("CarbonDioxide", 1000)
                .add(600, 64);
        row("gt.recipe.mixer", "m9-07/cathode-mix")
                .in(dust("LithiumCobaltOxide", 4), dust("PolyvinylideneFluoride", 1),
                        dust("CarbonBlack", 1))
                .out(dust("CathodeMix", 6))
                .add(240, 32);
        row("gt.recipe.mixer", "m9-07/anode-mix")
                .in(dust("Graphite", 4), dust("PolyvinylideneFluoride", 1), dust("CarbonBlack", 1))
                .out(dust("AnodeMix", 6))
                .add(240, 32);
    }

    private static void electrodeSheets() {
        row("gt.recipe.laminator", "m9-07/cathode-sheet")
                .in(dust("CathodeMix", 6), item("foil", "Aluminium", 1))
                .out(item("foil", "CathodeSheet", 1))
                .add(200, 48);
        row("gt.recipe.laminator", "m9-07/anode-sheet")
                .in(dust("AnodeMix", 6), item("foil", "Copper", 1))
                .out(item("foil", "AnodeSheet", 1))
                .add(200, 48);
    }

    private static void separator() {
        row("gt.recipe.press", "m9-07/separator")
                .in(dust("UltraHighMolecularWeightPolyethylene", 2), dust("Wax", 1))
                .out(item("foil", "PolyolefinSeparator", 1))
                .add(160, 32);
    }

    private static void electrolyte() {
        // 2 LiCl + 2 P + 6 F2 -> 2 LiPF6 + Cl2, folding the LiF intermediate into one pot.
        row("gt.recipe.mixer", "m9-07/lithium-hexafluorophosphate")
                .in(dust("LithiumChloride", 2), dust("Phosphorus", 2))
                .fluidIn("GenGas_Fluorine", 6000)
                .out(dust("LithiumHexafluorophosphate", 2))
                .fluidOut("GenGas_Chlorine", 1000)
                .add(300, 64);
        row("gt.recipe.mixer", "m9-07/ethylene-carbonate")
                .fluidIn("GenGas_EthyleneOxide", 1000, "CarbonDioxide", 1000)
                .fluidOut("GenLiquid_EthyleneCarbonate", 1000)
                .add(240, 48);
        // The host has no dry-room condition on a recipe; the anhydrous requirement is implicit.
        row("gt.recipe.mixer", "m9-07/lithium-electrolyte")
                .in(dust("LithiumHexafluorophosphate", 1))
                .fluidIn("GenLiquid_EthyleneCarbonate", 1000)
                .fluidOut("GenLiquid_LithiumElectrolyte", 1000)
                .add(120, 16);
    }

    /** Fills each host empty cell with the industrial electrolyte; the host acid/water rows stay. */
    private static void cellFilling() {
        String[] cells = {
                "lead_acid", "alkaline_button", "nickel_cadmium", "lithium_cobalt", "lithium_manganese"};
        for (String cell : cells) {
            row("gt.recipe.canner", "m9-07/cell/electrolyte/" + cell)
                    .in(vanillaItem("gt6ce:" + cell + "_cell_empty", 1))
                    .fluidIn("GenLiquid_LithiumElectrolyte", 1000)
                    .out(vanillaItem("gt6ce:" + cell + "_cell_filled", 1))
                    .add(120, 16);
        }
    }

    private static void flowBatteryMaterials() {
        row("gt.recipe.calciner", "m9-07/carbon-paper")
                .in(item("wireFine", "CarbonFiber", 8))
                .fluidIn("Nitrogen", 1000)
                .out(item("foil", "CarbonPaper", 1))
                .add(600, 64);
        row("gt.recipe.press", "m9-07/membrane-electrode-assembly")
                .in(item("foil", "Nafion", 1), item("foil", "CarbonPaper", 2),
                        item("dustTiny", "Platinum", 1))
                .out(item("plate", "MembraneElectrodeAssembly", 1))
                .add(300, 96);
        row("gt.recipe.press", "m9-07/bipolar-plate")
                .in(dust("Graphite", 4), dust("PolyvinylideneFluoride", 1))
                .out(item("plate", "BipolarPlate", 1))
                .add(200, 64);
        // The V(III)/V(II) reduction is folded into the dissolution; the host has no clean reductant.
        row("gt.recipe.bath", "m9-07/vanadium-electrolyte")
                .in(dust("VanadiumPentoxide", 1))
                .fluidIn("GenLiquid_SulfuricAcid", 2000)
                .fluidOut("GenLiquid_VanadiumElectrolyte", 2000)
                .add(300, 64);
    }
}
