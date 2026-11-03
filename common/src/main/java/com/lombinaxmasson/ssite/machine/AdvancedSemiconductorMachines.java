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

/**
 * P-03 ion implanter and electron-beam writer.
 *
 * <p>Both machines are behavior machines because the advanced route needs the P-04 environment
 * snapshot at batch start and on every continuing tick. Their recipe tables are display-only: EMI
 * shows the material and energy expectations, while {@link IonImplanterCore} and
 * {@link ElectronBeamCore} own the real ledger.</p>
 */
public final class AdvancedSemiconductorMachines implements AddonMachineContributor {
    @Override
    public String addonId() {
        return SsiteAddon.MOD_ID;
    }

    @Override
    public List<AddonMachine> addonMachines() {
        return List.of(implanter(), electronBeamWriter());
    }

    public static RecipeTable implanterTable() {
        return RecipeTable.machine(IonImplanterCore.MAP)
                .title("Ion Implantation")
                .gui("ssite:textures/gui/recipes/aod.png")
                .inputItems(2).outputItems(1).minimalInputItems(2)
                .inputFluids(1).minimalInputFluids(1).minimalInputs(2)
                .needsOutputs(true)
                .build();
    }

    public static RecipeTable electronBeamTable() {
        return RecipeTable.machine(ElectronBeamCore.MAP)
                .title("Electron-Beam Lithography")
                .gui("ssite:textures/gui/recipes/aod.png")
                .inputItems(2).outputItems(1).minimalInputItems(2)
                .minimalInputs(2)
                .needsOutputs(true)
                .build();
    }

    private static BehaviorMachine implanter() {
        return BehaviorMachine.builder(MachineCasing
                        .builder(IonImplanterCore.MACHINE_TYPE, "StainlessSteel")
                        .machineType(IonImplanterCore.MACHINE_TYPE).tier(3).build())
                .energy(EnergyInput.builder(EnergyType.EU, (int) IonImplanterCore.EU_PER_TICK)
                        .anyPacket().capacity(1_000_000L).build())
                .gui("ssite:textures/gui/aod_converter.png")
                .progress(80, 40)
                .item(44, 26, BehaviorMachine.SlotRole.INPUT)
                .item(62, 26, BehaviorMachine.SlotRole.INPUT)
                .item(116, 26, BehaviorMachine.SlotRole.OUTPUT)
                .fluid(8000, 152, 8, BehaviorMachine.SlotRole.INPUT)
                .clientState(IonImplanterCore.K_STATE, IonImplanterCore.K_PROGRESS,
                        IonImplanterCore.K_STATUS, IonImplanterCore.K_ENV_VALID,
                        IonImplanterCore.K_ENV_REASON, IonImplanterCore.K_ENV_REVISION)
                .io(MachineIo.builder()
                        .itemInputs(MachineFace.FRONT)
                        .itemOutputs(MachineFace.BOTTOM)
                        .fluidInputs(MachineFace.FRONT)
                        .build())
                .build();
    }

    private static BehaviorMachine electronBeamWriter() {
        return BehaviorMachine.builder(MachineCasing
                        .builder(ElectronBeamCore.MACHINE_TYPE, "StainlessSteel")
                        .machineType(ElectronBeamCore.MACHINE_TYPE).tier(3).build())
                .energy(EnergyInput.builder(EnergyType.EU, (int) ElectronBeamCore.EU_PER_TICK)
                        .anyPacket().capacity(1_000_000L).build())
                .gui("ssite:textures/gui/aod_converter.png")
                .progress(80, 40)
                .item(44, 26, BehaviorMachine.SlotRole.INPUT)
                .item(62, 26, BehaviorMachine.SlotRole.INPUT)
                .item(116, 26, BehaviorMachine.SlotRole.OUTPUT)
                .clientState(ElectronBeamCore.K_STATE, ElectronBeamCore.K_PROGRESS,
                        ElectronBeamCore.K_STATUS, ElectronBeamCore.K_ENV_VALID,
                        ElectronBeamCore.K_ENV_REASON, ElectronBeamCore.K_ENV_REVISION)
                .io(MachineIo.builder()
                        .itemInputs(MachineFace.FRONT)
                        .itemOutputs(MachineFace.BOTTOM)
                        .build())
                .build();
    }
}
