package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

/**
 * P-04 consumables for the cleanroom.
 *
 * <p>Only the replaceable filter is a machine row: one polyethylene binder, one alumina filter aid
 * and a small ultrapure water charge yield two cartridges. The controller, vent, thermostat and
 * structural parts are normal crafting-table machines and live in the recipe JSON files, matching
 * the existing P-01/P-03 equipment convention.</p>
 */
public final class ProcessEnvironmentRecipes {
    private ProcessEnvironmentRecipes() {}

    static void add() {
        row("gt.recipe.mixer", "p04/filter-cartridge")
                .in(dust("Plastic", 1), dust("Alumina", 1))
                .fluidIn("GenLiquid_UltrapureWater", 100)
                .out(item("filterCartridge", "CleanroomFilter", 2))
                .add(200, 16);
    }
}
