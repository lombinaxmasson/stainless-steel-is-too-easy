package com.lombinaxmasson.ssite.machine;

import com.lombinaxmasson.ssite.SsiteAddon;
import gregtech.api.addon.machine.AddonMachine;
import gregtech.api.addon.machine.AddonMachineContributor;
import gregtech.api.addon.machine.BehaviorMachine;
import gregtech.api.energy.EnergyInput;
import gregtech.api.energy.EnergyOutput;
import gregtech.api.energy.EnergyType;
import gregtech.api.machine.MachineCasing;
import gregtech.api.machine.MachineFace;
import gregtech.api.machine.MachineIo;

import java.util.List;

/**
 * Two independent single-block beds. They deliberately share no inventory or heat budget: the
 * carrier travels as an ordinary item, and each bed keeps its own HU outlet. The host requires a
 * GUI resource even though {@code onUse} always consumes the click and never opens the menu.
 */
public final class ChemicalLoopingMachines implements AddonMachineContributor {
    @Override public String addonId() { return SsiteAddon.MOD_ID; }

    @Override public List<AddonMachine> addonMachines() {
        return List.of(fuelReactor(), airReactor());
    }

    private static BehaviorMachine fuelReactor() {
        return BehaviorMachine.builder(MachineCasing.builder(FuelReactorCore.MACHINE_TYPE, "Steel")
                        .machineType(FuelReactorCore.MACHINE_TYPE).tier(2).build())
                // The main HU buffer is generated internally; acceptsEnergy refuses every HU packet.
                .energy(EnergyInput.builder(EnergyType.HU, 512).anyPacket()
                        .capacity(FuelReactorCore.HEAT_CAPACITY).faces(MachineFace.TOP).build())
                .auxiliaryEnergy(EnergyInput.builder(EnergyType.KU, 16).anyPacket()
                        .capacity(FuelReactorCore.KU_CAPACITY).faces(MachineFace.LEFT).build())
                .energyOutput(EnergyOutput.builder(EnergyType.HU, 1, FuelReactorCore.MAX_HEAT_OUTPUT)
                        .faces(MachineFace.BACK).build())
                .gui("ssite:textures/gui/aod_converter.png")
                .item(44, 26, BehaviorMachine.SlotRole.INPUT)
                .item(62, 26, BehaviorMachine.SlotRole.INPUT)
                .item(80, 26, BehaviorMachine.SlotRole.OUTPUT)
                .item(98, 26, BehaviorMachine.SlotRole.OUTPUT)
                .fluid(FuelReactorCore.CO2_CAPACITY, 134, 8, BehaviorMachine.SlotRole.OUTPUT)
                .fluid(FuelReactorCore.WATER_CAPACITY, 152, 8, BehaviorMachine.SlotRole.OUTPUT)
                .progress(8, 62)
                .clientState(FuelReactorCore.K_STATE, FuelReactorCore.K_BATCH,
                        FuelReactorCore.K_PROGRESS, FuelReactorCore.K_STATUS, FuelReactorCore.K_LIT,
                        FuelReactorCore.K_STORED, FuelReactorCore.K_GENERATED, FuelReactorCore.K_OUTPUT,
                        FuelReactorCore.K_KU, FuelReactorCore.K_CO2, FuelReactorCore.K_WATER,
                        FuelReactorCore.K_RECEIVER)
                .io(MachineIo.builder()
                        .itemInputs(MachineFace.FRONT)
                        .itemOutputs(MachineFace.BOTTOM)
                        .fluidOutputs(MachineFace.RIGHT)
                        .build())
                .build();
    }

    private static BehaviorMachine airReactor() {
        return BehaviorMachine.builder(MachineCasing.builder(AirReactorCore.MACHINE_TYPE, "Bronze")
                        .machineType(AirReactorCore.MACHINE_TYPE).tier(1).build())
                .energy(EnergyInput.builder(EnergyType.HU, 512).anyPacket()
                        .capacity(AirReactorCore.HEAT_CAPACITY).faces(MachineFace.TOP).build())
                .auxiliaryEnergy(EnergyInput.builder(EnergyType.KU, 16).anyPacket()
                        .capacity(AirReactorCore.KU_CAPACITY).faces(MachineFace.LEFT).build())
                .energyOutput(EnergyOutput.builder(EnergyType.HU, 1, AirReactorCore.MAX_HEAT_OUTPUT)
                        .faces(MachineFace.BACK).build())
                .gui("ssite:textures/gui/aod_converter.png")
                .item(44, 26, BehaviorMachine.SlotRole.INPUT)
                .item(62, 26, BehaviorMachine.SlotRole.OUTPUT)
                .progress(8, 62)
                .clientState(AirReactorCore.K_STATE, AirReactorCore.K_PROGRESS,
                        AirReactorCore.K_STATUS, AirReactorCore.K_VENT, AirReactorCore.K_BATCHES,
                        AirReactorCore.K_WEAR, AirReactorCore.K_STORED, AirReactorCore.K_GENERATED,
                        AirReactorCore.K_OUTPUT, AirReactorCore.K_KU)
                .io(MachineIo.builder()
                        .itemInputs(MachineFace.FRONT)
                        .itemOutputs(MachineFace.BOTTOM)
                        .build())
                .build();
    }
}
