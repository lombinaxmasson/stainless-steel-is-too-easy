package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.vanillaItem;

/**
 * M9-02 circuit board substrates.
 *
 * <p>Boards are laminated in the bath map rather than the press map on purpose: the datapack
 * removal that drops the host's press rows matches every row with the same output, so a new
 * press row producing the same board would delete itself. Bath also has slots for the resin.</p>
 */
public final class CircuitRecipes {
    private CircuitRecipes() {}

    static void add() {
        // The port has no circuit_part_elite or _ultimate recipe, so the substrate and conductor
        // carry the high tiers and the parts stay at the highest grade that can actually be made.
        boards("advanced", "advanced", "Bakelite", "GenLiquid_PhenolicResin", 250, "gold", "platinum");
        boards("elite", "master", "FiberReinforcedEpoxy", "GenLiquid_EpoxyResin", 500, "gold", "platinum");
        boards("master", "master", "FlameRetardantLaminate", "GenLiquid_CycloaliphaticEpoxy", 500, "platinum");
        boards("ultimate", "master", "Kapton", "GenLiquid_CycloaliphaticEpoxy", 1000, "platinum");
    }

    private static void boards(String tier, String parts, String substrate, String resin, int resinAmount,
                               String... conductors) {
        for (String conductor : conductors) {
            row("gt.recipe.bath", "m9-02/circuit-board/" + tier + "/" + conductor)
                    .in(vanillaItem("gt6ce:circuit_plate_" + conductor, 1),
                            vanillaItem("gt6ce:circuit_part_" + parts, 4),
                            item("plate", substrate, 1))
                    .fluidIn(resin, resinAmount)
                    .out(vanillaItem("gt6ce:circuit_board_" + tier, 1))
                    // Bath is a TU machine and only supplies its 1 TU/t baseline internally.
                    .add(64, 1);
        }
    }
}
