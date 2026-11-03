package com.lombinaxmasson.ssite.machine;

import com.lombinaxmasson.ssite.SsiteAddon;
import gregtech.api.addon.machine.AddonMachine;
import gregtech.api.addon.machine.AddonMachineContributor;
import gregtech.api.addon.machine.BehaviorMachine;
import gregtech.api.addon.machine.LayoutPart;
import gregtech.api.energy.EnergyInput;
import gregtech.api.energy.EnergyType;
import gregtech.api.machine.MachineCasing;
import gregtech.api.machine.MachineFace;
import gregtech.api.machine.MachineIo;
import gregtech.api.multiblock.StructureLayout;

import java.util.List;

/** KU drilling rig over a natural-gas field; the cave itself remains directly pumpable. */
public final class GasFieldExtractorMachines implements AddonMachineContributor {
    @Override
    public String addonId() {
        return SsiteAddon.MOD_ID;
    }

    @Override
    public List<AddonMachine> addonMachines() {
        return List.of(BehaviorMachine.builder(MachineCasing
                        .builder(GasFieldExtractorCore.MACHINE_TYPE, "StainlessSteel")
                        .machineType(GasFieldExtractorCore.MACHINE_TYPE).tier(3).build())
                .energy(EnergyInput.builder(EnergyType.KU, GasFieldExtractorCore.KU_PER_TICK)
                        .anyPacket().capacity(GasFieldExtractorCore.KU_CAPACITY)
                        .faces(MachineFace.TOP, MachineFace.FRONT, MachineFace.BACK,
                                MachineFace.LEFT, MachineFace.RIGHT).build())
                .gui("ssite:textures/gui/aod_converter.png")
                .fluid(GasFieldExtractorCore.TANK_CAPACITY, 116, 26, BehaviorMachine.SlotRole.OUTPUT)
                .clientState(GasFieldExtractorCore.K_FORMED, GasFieldExtractorCore.K_STATUS,
                        GasFieldExtractorCore.K_WELLS, GasFieldExtractorCore.K_BUDGET)
                .io(MachineIo.builder().fluidOutputs(MachineFace.TOP).build())
                .build());
    }

    @Override
    public List<StructureLayout> structureLayouts() {
        return List.of(GasFieldStructure.layout());
    }

    @Override
    public List<LayoutPart> layoutParts() {
        return List.of();
    }
}
