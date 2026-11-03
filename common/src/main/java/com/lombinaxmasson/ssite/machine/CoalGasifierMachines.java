package com.lombinaxmasson.ssite.machine;

import com.lombinaxmasson.ssite.SsiteAddon;
import gregtech.api.addon.machine.AddonMachine;
import gregtech.api.addon.machine.AddonMachineContributor;
import gregtech.api.addon.machine.BehaviorMachine;
import gregtech.api.addon.recipe.RecipeTable;
import gregtech.api.energy.EnergyInput;
import gregtech.api.energy.EnergyType;
import gregtech.api.machine.MachineCasing;
import gregtech.api.machine.MachineFace;
import gregtech.api.machine.MachineIo;

import java.util.List;

/** Independent steam gasifier; it shares the fluidized bed's casing vocabulary, not its fuel ledger. */
public final class CoalGasifierMachines implements AddonMachineContributor {
    @Override
    public String addonId() {
        return SsiteAddon.MOD_ID;
    }

    @Override
    public List<AddonMachine> addonMachines() {
        return List.of(BehaviorMachine.builder(MachineCasing
                        .builder(CoalGasifierCore.MACHINE_TYPE, "StainlessSteel")
                        .machineType(CoalGasifierCore.MACHINE_TYPE).tier(3).build())
                .energy(EnergyInput.builder(EnergyType.EU, (int) CoalGasifierCore.EU_PER_TICK)
                        .anyPacket().capacity(CoalGasifierCore.EU_CAPACITY).build())
                .gui("ssite:textures/gui/aod_converter.png")
                .recipeMaps(CoalGasifierCore.MAP)
                .progress(80, 40)
                .item(44, 26, BehaviorMachine.SlotRole.INPUT)
                .item(116, 26, BehaviorMachine.SlotRole.OUTPUT)
                // Tanks are 18x54 and must end above the player inventory; place them side by side.
                .fluid(CoalGasifierCore.MEDIUM_CAPACITY, 152, 8, BehaviorMachine.SlotRole.INPUT)
                .fluid(CoalGasifierCore.RAW_GAS_CAPACITY, 134, 8, BehaviorMachine.SlotRole.OUTPUT)
                .clientState(CoalGasifierCore.K_STATE, CoalGasifierCore.K_PROGRESS,
                        CoalGasifierCore.K_STATUS, CoalGasifierCore.K_BATCH)
                .io(MachineIo.builder()
                        .itemInputs(MachineFace.FRONT)
                        .itemOutputs(MachineFace.BOTTOM)
                        .fluidInputs(MachineFace.FRONT)
                        .fluidOutputs(MachineFace.TOP)
                        .build())
                .build());
    }

    public static RecipeTable recipeTable() {
        return RecipeTable.machine(CoalGasifierCore.MAP)
                .title("Coal Gasification")
                .gui("ssite:textures/gui/recipes/aod.png")
                .inputItems(1).outputItems(1).minimalInputItems(1)
                .inputFluids(1).outputFluids(1).minimalInputFluids(1).minimalInputs(2)
                .needsOutputs(true)
                .build();
    }
}
