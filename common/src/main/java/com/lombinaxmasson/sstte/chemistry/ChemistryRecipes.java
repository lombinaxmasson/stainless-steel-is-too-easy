package com.lombinaxmasson.sstte.chemistry;

import com.gregtech.gregtech.api.addon.GregTechAddon;

/** Chemistry recipe changes. GregTech calls this once, after its own machine recipes exist. */
public final class ChemistryRecipes {
    private ChemistryRecipes() {}

    public static void add(GregTechAddon.Context context) {
        if (context.apiVersion() != 0) {
            throw new IllegalStateException("sstte expects GregTech addon API 0, found " + context.apiVersion());
        }
    }
}
