package com.lombinaxmasson.ssite.machine;

import com.gregtech.gregtech.api.addon.AddonMachine;
import com.gregtech.gregtech.api.addon.AddonMachineContributor;
import com.gregtech.gregtech.api.addon.EnergyInput;
import com.gregtech.gregtech.api.addon.FluidPipeDeclaration;
import com.gregtech.gregtech.api.addon.MachineCasing;
import com.gregtech.gregtech.api.addon.RecipeMachine;
import com.gregtech.gregtech.api.addon.RecipeTable;
import com.lombinaxmasson.ssite.SsiteAddon;

import java.util.List;

/** Machine, pipe and recipe-table declarations. */
public final class SsiteMachines implements AddonMachineContributor {
    public static final String AOD_MAP = "ssite.recipe.aod";

    @Override
    public String addonId() {
        return SsiteAddon.MOD_ID;
    }

    @Override
    public List<RecipeTable> recipeTables() {
        return List.of(RecipeTable.machine(AOD_MAP)
                .title("AOD Refining")
                .gui("ssite:textures/gui/machines/aod")
                .inputItems(4)
                .outputItems(2)
                .minimalInputItems(1)
                .inputFluids(2)
                .outputFluids(1)
                .minimalInputFluids(1)
                .minimalInputs(2)
                .needsOutputs(true)
                .build());
    }

    @Override
    public List<AddonMachine> addonMachines() {
        return List.of(RecipeMachine.builder(
                        MachineCasing.builder("aod_converter", "Invar")
                                .machineType("aod_converter")
                                .tier(2)
                                .build())
                .energy(EnergyInput.builder("HU", 64).build())
                .recipeMap(AOD_MAP)
                .basic()
                .build());
    }

    /** Acid-proof 316L line. Temperature 0 uses the material melting point times 1.25. */
    @Override
    public List<FluidPipeDeclaration> fluidPipes() {
        return List.of(new FluidPipeDeclaration(
                "stainless_steel_316l", "StainlessSteel316L", 300, true, true, false, false, 0, 0));
    }
}
