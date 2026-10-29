package com.lombinaxmasson.ssite.machine;

import gregtech.api.energy.EnergyType;
import com.lombinaxmasson.ssite.SsiteAddon;
import gregtech.api.addon.machine.AddonMachine;
import gregtech.api.addon.machine.AddonMachineContributor;
import gregtech.api.addon.machine.BehaviorMachine;
import gregtech.api.addon.machine.LayoutPart;
import gregtech.api.energy.EnergyInput;
import gregtech.api.energy.EnergyOutput;
import gregtech.api.machine.MachineCasing;
import gregtech.api.machine.MachineFace;
import gregtech.api.machine.MachineIo;
import gregtech.api.multiblock.StructureLayout;
import java.util.List;

/** Declaration helper, aggregated by the addon's single SsiteMachines service provider. */
public final class FluidizedBedMachines implements AddonMachineContributor {
    @Override public String addonId() { return SsiteAddon.MOD_ID; }
    @Override public List<AddonMachine> addonMachines() {
        return List.of(BehaviorMachine.builder(MachineCasing.builder(FluidizedBedCore.MACHINE_TYPE, "Steel")
                        .machineType(FluidizedBedCore.MACHINE_TYPE).tier(3).build())
                // This buffer stores internally generated HU. The behavior refuses all external HU packets.
                .energy(EnergyInput.builder(EnergyType.HU, FluidizedBedCore.MAX_OUTPUT).anyPacket()
                        .capacity(FluidizedBedCore.HEAT_CAPACITY).faces(MachineFace.BACK).build())
                .auxiliaryEnergy(EnergyInput.builder(EnergyType.KU, 32).anyPacket().capacity(FluidizedBedCore.KU_CAPACITY)
                        .faces(MachineFace.FRONT, MachineFace.TOP).build())
                .energyOutput(EnergyOutput.builder(EnergyType.HU, 1, FluidizedBedCore.MAX_OUTPUT).remote().build())
                .gui("ssite:textures/gui/aod_converter.png")
                .item(44, 26, BehaviorMachine.SlotRole.INPUT)
                .item(116, 26, BehaviorMachine.SlotRole.OUTPUT)
                .toggle("ssite.fluidized_bed.enabled", 142, 45, false)
                .clientState(FluidizedBedCore.K_FORMED, FluidizedBedCore.K_STORED, FluidizedBedCore.K_OUTPUT,
                        FluidizedBedCore.K_GENERATED, FluidizedBedCore.K_LOSS, FluidizedBedCore.K_PENDING,
                        FluidizedBedCore.K_POWER, FluidizedBedCore.K_STATUS, FluidizedBedCore.K_ENABLED,
                        FluidizedBedCore.K_KU, FluidizedBedCore.K_RATE)
                .io(MachineIo.builder().itemInputs(MachineFace.TOP, MachineFace.FRONT)
                        .itemOutputs(MachineFace.BOTTOM).build())
                .build());
    }
    @Override public List<StructureLayout> structureLayouts() { return List.of(FluidizedBedStructure.layout()); }
    @Override public List<LayoutPart> layoutParts() {
        return List.of(part(FluidizedBedStructure.CASING), part(FluidizedBedStructure.DUCT),
                part(FluidizedBedStructure.HEAT_PLATE), part(FluidizedBedStructure.FOUNDATION));
    }
    private static LayoutPart part(String id) {
        String tile = id.equals(FluidizedBedStructure.DUCT) ? "insulation"
                : id.equals(FluidizedBedStructure.HEAT_PLATE) ? "ceramic"
                : id.equals(FluidizedBedStructure.FOUNDATION) ? "dark" : "plate";
        return LayoutPart.builder(id, "Steel").hardness(8).texture("block/machine/fluidized_bed/" + tile).build();
    }
}
