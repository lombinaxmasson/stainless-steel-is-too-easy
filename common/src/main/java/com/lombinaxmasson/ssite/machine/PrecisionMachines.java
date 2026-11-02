package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.machine.AddonMachine;
import gregtech.api.addon.machine.AddonMachineContributor;
import gregtech.api.addon.recipe.RecipeMachine;
import gregtech.api.addon.recipe.RecipeTable;
import gregtech.api.energy.EnergyInput;
import gregtech.api.energy.EnergyType;
import gregtech.api.machine.MachineCasing;
import com.lombinaxmasson.ssite.SsiteAddon;

import java.util.List;

/**
 * P-01 precision laser workstation.
 *
 * <p>A declarative recipe machine is enough: one recipe runs at a time, and the per-mode head is a
 * kept input, so the three modes interlock without a behaviour core. The recipe table is the
 * execution table, not a display-only table.</p>
 */
public final class PrecisionMachines implements AddonMachineContributor {
    public static final String MAP = "ssite.recipe.precision_laser";
    public static final String MACHINE_TYPE = "precision_laser_workstation";

    @Override
    public String addonId() {
        return SsiteAddon.MOD_ID;
    }

    @Override
    public List<AddonMachine> addonMachines() {
        return List.of(RecipeMachine.builder(MachineCasing.builder(MACHINE_TYPE, "StainlessSteel")
                        .machineType(MACHINE_TYPE).tier(3).build())
                .energy(EnergyInput.builder(EnergyType.EU, 512).build())
                .recipeMap(MAP)
                .build());
    }

    public static RecipeTable recipeTable() {
        return RecipeTable.machine(MAP)
                .title("Precision Laser Machining")
                .gui("ssite:textures/gui/recipes/aod.png")
                .inputItems(2).outputItems(1).minimalInputItems(2)
                .inputFluids(1).minimalInputFluids(1).minimalInputs(2)
                .needsOutputs(true)
                .build();
    }
}
