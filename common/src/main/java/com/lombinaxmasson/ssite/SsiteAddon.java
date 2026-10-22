package com.lombinaxmasson.ssite;

import com.gregtech.gregtech.api.addon.GregTechAddon;
import com.gregtech.gregtech.api.addon.GregTechAddons;
import com.lombinaxmasson.ssite.chemistry.ChemistryRecipes;

/** Shared entry for both loaders. Call {@link #register()} from each mod constructor. */
public final class SsiteAddon {
    public static final String MOD_ID = "ssite";

    private SsiteAddon() {}

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
