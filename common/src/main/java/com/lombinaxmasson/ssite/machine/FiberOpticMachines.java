package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.machine.AddonMachine;
import gregtech.api.addon.machine.AddonMachineContributor;
import gregtech.api.addon.machine.BehaviorMachine;
import gregtech.api.addon.recipe.RecipeMachine;
import gregtech.api.addon.recipe.RecipeTable;
import gregtech.api.energy.EnergyInput;
import gregtech.api.energy.EnergyType;
import gregtech.api.machine.MachineCasing;
import com.lombinaxmasson.ssite.SsiteAddon;

import java.util.List;

/**
 * P-02 fiber drawing machine and read-only status probe.
 *
 * <p>Drawing, coating and spooling are batch rows on one declarative recipe machine. The probe is
 * a behavior machine because it must remember a completion edge and expose a GT signal; the host
 * provides the block, menu and signal-wire source. The probe's small EU inlet is the minimum legal
 * behavior declaration; it does not gate the read-only snapshot.</p>
 */
public final class FiberOpticMachines implements AddonMachineContributor {
    public static final String MAP = "ssite.recipe.fiber_drawing";
    public static final String MACHINE_TYPE = "fiber_drawing_machine";
    public static final String PROBE_TYPE = FiberProbeCore.MACHINE_TYPE;

    @Override
    public String addonId() {
        return SsiteAddon.MOD_ID;
    }

    @Override
    public List<AddonMachine> addonMachines() {
        return List.of(
                RecipeMachine.builder(MachineCasing.builder(MACHINE_TYPE, "StainlessSteel")
                                .machineType(MACHINE_TYPE).tier(2).build())
                        .energy(EnergyInput.builder(EnergyType.EU, 128).build())
                        .recipeMap(MAP)
                        .build(),
                BehaviorMachine.builder(MachineCasing.builder(PROBE_TYPE, "StainlessSteel")
                                .machineType(PROBE_TYPE).tier(2).build())
                        .energy(EnergyInput.builder(EnergyType.EU, 1).build())
                        .gui("ssite:textures/gui/aod_converter.png")
                        .progress(80, 40)
                        .signalOutput(true)
                        .clientState(FiberProbeCore.K_STATE, FiberProbeCore.K_SIGNAL,
                                FiberProbeCore.K_PROGRESS, FiberProbeCore.K_MAX,
                                FiberProbeCore.K_PULSE, FiberProbeCore.K_TARGET)
                        .build());
    }

    public static RecipeTable recipeTable() {
        return RecipeTable.machine(MAP)
                .title("Optical Fiber Drawing")
                .gui("ssite:textures/gui/recipes/aod.png")
                .inputItems(2).outputItems(2).minimalInputItems(2)
                .inputFluids(1).minimalInputFluids(0).minimalInputs(2)
                .needsOutputs(true)
                .build();
    }
}
