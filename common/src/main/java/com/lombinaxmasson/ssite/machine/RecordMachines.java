package com.lombinaxmasson.ssite.machine;

import com.lombinaxmasson.ssite.SsiteAddon;
import gregtech.api.addon.machine.AddonMachine;
import gregtech.api.addon.machine.AddonMachineContributor;
import gregtech.api.addon.recipe.RecipeMachine;
import gregtech.api.addon.recipe.RecipeTable;
import gregtech.api.energy.EnergyInput;
import gregtech.api.energy.EnergyType;
import gregtech.api.machine.MachineCasing;

import java.util.List;

/**
 * M8 record lathe. A declarative recipe machine is enough: the lacquer master is consumed, and
 * the track template plus the sapphire stylus are kept inputs. A dynamic recipe copies the
 * library-backed track to the cut master. Energy is EU, matching the electric spindle.
 */
public final class RecordMachines implements AddonMachineContributor {
    public static final String MAP = "ssite.recipe.record_lathe";
    public static final String MACHINE_TYPE = "record_lathe";
    public static final int INPUT_ITEM_SLOTS = 3;
    public static final int OUTPUT_ITEM_SLOTS = 1;

    @Override
    public String addonId() {
        return SsiteAddon.MOD_ID;
    }

    @Override
    public List<AddonMachine> addonMachines() {
        return List.of(RecipeMachine.builder(MachineCasing.builder(MACHINE_TYPE, "StainlessSteel")
                        .machineType(MACHINE_TYPE).tier(2).build())
                .energy(EnergyInput.builder(EnergyType.EU, 128).build())
                .recipeMap(MAP)
                .build());
    }

    public static RecipeTable recipeTable() {
        return RecipeTable.machine(MAP)
                .title("Record Lathe")
                .gui("ssite:textures/gui/recipes/aod.png")
                .inputItems(INPUT_ITEM_SLOTS).outputItems(OUTPUT_ITEM_SLOTS)
                .minimalInputItems(INPUT_ITEM_SLOTS).minimalInputs(INPUT_ITEM_SLOTS)
                .needsOutputs(true)
                .build();
    }
}
