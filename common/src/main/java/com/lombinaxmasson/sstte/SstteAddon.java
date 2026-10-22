package com.lombinaxmasson.sstte;

import com.gregtech.gregtech.api.addon.GregTechAddon;
import com.gregtech.gregtech.api.addon.GregTechAddons;
import com.lombinaxmasson.sstte.chemistry.ChemistryRecipes;

/** Shared entry for both loaders. Call {@link #register()} from each mod constructor. */
public final class SstteAddon {
    public static final String MOD_ID = "sstte";

    private SstteAddon() {}

    public static void register() {
        GregTechAddons.register(new GregTechAddon() {
            @Override
            public String id() {
                return MOD_ID;
            }

            @Override
            public void onRecipesReady(Context context) {
                ChemistryRecipes.add(context);
            }
        });
    }
}
