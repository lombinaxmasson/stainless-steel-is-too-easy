package com.lombinaxmasson.ssite.machine;

import com.lombinaxmasson.ssite.SsiteAddon;
import gregtech.api.addon.machine.AddonMachine;
import gregtech.api.addon.machine.AddonMachineContributor;
import gregtech.api.addon.machine.BehaviorMachine;
import gregtech.api.addon.machine.LayoutPart;
import gregtech.api.energy.EnergyInput;
import gregtech.api.energy.EnergyOutput;
import gregtech.api.energy.EnergyType;
import gregtech.api.machine.MachineCasing;
import gregtech.api.machine.MachineFace;
import gregtech.api.machine.MachineIo;
import gregtech.api.multiblock.StructureLayout;

import java.util.List;

/** Declaration helper, aggregated by the addon's single SsiteMachines service provider. */
public final class IrradiationReactorMachines implements AddonMachineContributor {
    /** Display-only table: the vessel irradiates in its behavior, not by crafting this row. */
    public static final String MAP = "ssite.recipe.blanket";

    @Override public String addonId() { return SsiteAddon.MOD_ID; }

    @Override public List<AddonMachine> addonMachines() {
        return List.of(BehaviorMachine.builder(MachineCasing
                        .builder(IrradiationReactorCore.MACHINE_TYPE, "StainlessSteel")
                        .machineType(IrradiationReactorCore.MACHINE_TYPE).tier(3).build())
                // The main HU buffer holds heat this machine generated; external HU is refused.
                .energy(EnergyInput.builder(EnergyType.HU, IrradiationReactorCore.HEAT_CAPACITY).anyPacket()
                        .capacity(IrradiationReactorCore.HEAT_CAPACITY).faces(MachineFace.BACK).build())
                .auxiliaryEnergy(EnergyInput.builder(EnergyType.KU, IrradiationReactorCore.KU_PER_TICK).anyPacket()
                        .capacity(IrradiationReactorCore.KU_CAPACITY)
                        .faces(MachineFace.FRONT, MachineFace.BACK).build())
                .energyOutput(EnergyOutput.builder(EnergyType.HU, 1, IrradiationReactorCore.EMIT_PER_TICK).remote().build())
                .progress(80, 40)
                .item(44, 26, BehaviorMachine.SlotRole.INPUT)
                .item(116, 26, BehaviorMachine.SlotRole.OUTPUT)
                .clientState(IrradiationReactorCore.K_FORMED, IrradiationReactorCore.K_STATE,
                        IrradiationReactorCore.K_PROGRESS, IrradiationReactorCore.K_TEMPERATURE,
                        IrradiationReactorCore.K_STATUS, IrradiationReactorCore.K_STORED,
                        IrradiationReactorCore.K_GENERATED, IrradiationReactorCore.K_EMITTED,
                        IrradiationReactorCore.K_DOOR_OPEN, IrradiationReactorCore.K_PAUSED)
                .io(MachineIo.builder().itemInputs(MachineFace.FRONT).itemOutputs(MachineFace.BOTTOM).build())
                .build());
    }

    @Override public List<StructureLayout> structureLayouts() {
        return List.of(IrradiationReactorStructure.layout());
    }

    @Override public List<LayoutPart> layoutParts() {
        return List.of(
                LayoutPart.builder(IrradiationReactorStructure.CASING, "StainlessSteel")
                        .hardness(10).texture("block/machine/fluidized_bed/refractory").build(),
                LayoutPart.builder(IrradiationReactorStructure.HEAT_PORT, "StainlessSteel")
                        .hardness(10).texture("block/machine/fluidized_bed/insulation").build());
    }
}
