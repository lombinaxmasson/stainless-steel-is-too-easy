package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.vanillaItem;

/** Bearing head and removable glass-reinforced rotor for the wind turbine. */
final class WindTurbineRecipes {
    private WindTurbineRecipes() {}

    static void add() {
        row("gt.recipe.press", "wind-turbine/bearing-head")
                .in(vanillaItem("gt6ce:casing_machine_stainlesssteel", 1), item("stickLong", "StainlessSteel", 2))
                .out(vanillaItem("ssite:wind_turbine", 1))
                .add(200, 128);
        row("gt.recipe.press", "wind-turbine/fiberglass-rotor")
                .in(item("plate", "FiberReinforcedEpoxy", 16), item("rotor", "StainlessSteel", 1))
                .out(vanillaItem("ssite:fiberglass_wind_rotor", 1))
                .add(400, 128);
    }
}
