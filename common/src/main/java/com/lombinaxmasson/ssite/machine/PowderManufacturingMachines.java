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
 * P-05 powder-bed printer.
 *
 * <p>A behavior machine because the batch ledger owns input reservation, progress, shielding gas
 * and two separate outputs. The display-only recipe table shows the material and energy
 * expectations for EMI; {@link PowderBedCore} owns the real transaction.</p>
 */
public final class PowderManufacturingMachines implements AddonMachineContributor {
    public static final String MACHINE_TYPE = PowderBedCore.MACHINE_TYPE;
    public static final String MAP = PowderBedCore.MAP;

    @Override
    public String addonId() {
        return SsiteAddon.MOD_ID;
    }

    @Override
    public List<AddonMachine> addonMachines() {
        return List.of(printer());
    }

    public static RecipeTable recipeTable() {
        return RecipeTable.machine(MAP)
                .title("Powder Bed Fusion")
                .gui("ssite:textures/gui/recipes/aod.png")
                .inputItems(3).outputItems(2).minimalInputItems(1)
                .inputFluids(1).minimalInputFluids(1).minimalInputs(2)
                .needsOutputs(true)
                .build();
    }

    private static BehaviorMachine printer() {
        return BehaviorMachine.builder(MachineCasing.builder(MACHINE_TYPE, "StainlessSteel")
                        .machineType(MACHINE_TYPE).tier(3).build())
                .energy(EnergyInput.builder(EnergyType.EU, (int) PowderBedCore.EU_PER_TICK)
                        .anyPacket().capacity(1_000_000L).build())
                .gui("ssite:textures/gui/aod_converter.png")
                .progress(80, 40)
                .item(26, 26, BehaviorMachine.SlotRole.INPUT)
                .item(44, 26, BehaviorMachine.SlotRole.INPUT)
                .item(62, 26, BehaviorMachine.SlotRole.INPUT)
                .item(116, 26, BehaviorMachine.SlotRole.OUTPUT)
                .item(134, 26, BehaviorMachine.SlotRole.OUTPUT)
                .fluid(8000, 152, 8, BehaviorMachine.SlotRole.INPUT)
                .recipeMaps(MAP)
                .clientState(PowderBedCore.K_STATE, PowderBedCore.K_PROGRESS,
                        PowderBedCore.K_STATUS, PowderBedCore.K_ARGON,
                        PowderBedCore.K_RECLAIM, PowderBedCore.K_PARALLEL,
                        PowderBedCore.K_UPGRADE)
                .io(MachineIo.builder()
                        .itemInputs(MachineFace.FRONT)
                        .itemOutputs(MachineFace.BOTTOM)
                        .fluidInputs(MachineFace.FRONT)
                        .build())
                .build();
    }
}
