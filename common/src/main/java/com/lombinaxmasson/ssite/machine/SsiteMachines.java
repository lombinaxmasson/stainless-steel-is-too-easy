package com.lombinaxmasson.ssite.machine;

import com.gregtech.gregtech.api.addon.AddonMachine;
import com.gregtech.gregtech.api.addon.AddonMachineContributor;
import com.gregtech.gregtech.api.addon.BehaviorMachine;
import com.gregtech.gregtech.api.addon.CellMatch;
import com.gregtech.gregtech.api.addon.EnergyInput;
import com.gregtech.gregtech.api.addon.FluidPipeDeclaration;
import com.gregtech.gregtech.api.addon.LayoutPart;
import com.gregtech.gregtech.api.addon.MachineCasing;
import com.gregtech.gregtech.api.addon.MachineFace;
import com.gregtech.gregtech.api.addon.MachineIo;
import com.gregtech.gregtech.api.addon.PartSlots;
import com.gregtech.gregtech.api.addon.RecipeTable;
import com.gregtech.gregtech.api.addon.StructureLayout;
import com.gregtech.gregtech.api.addon.StructureRole;
import com.lombinaxmasson.ssite.SsiteAddon;

import java.util.ArrayList;
import java.util.List;

/** AOD converter machines, their one-block lid layout and the acid-proof 316L pipe. */
public final class SsiteMachines implements AddonMachineContributor {
    /** Display-only table: the converter refines in its behavior, not by crafting these rows. */
    public static final String AOD_MAP = "ssite.recipe.aod";
    /** Display-only table for the VAR arc furnace's remelting and alloying ratios. */
    public static final String VAR_MAP = "ssite.recipe.var";

    private static final MachineFace[] HOT_FACES = {
            MachineFace.BOTTOM, MachineFace.FRONT, MachineFace.LEFT, MachineFace.RIGHT, MachineFace.BACK};
    private static final MachineFace[] GAS_FACES = {
            MachineFace.FRONT, MachineFace.LEFT, MachineFace.RIGHT, MachineFace.BACK};

    @Override
    public String addonId() {
        return SsiteAddon.MOD_ID;
    }

    @Override
    public List<RecipeTable> recipeTables() {
        return List.of(
                RecipeTable.machine(AOD_MAP)
                        .title("AOD Refining")
                        .gui("ssite:textures/gui/recipes/aod")
                        .inputItems(4)
                        .outputItems(2)
                        .minimalInputItems(1)
                        .inputFluids(2)
                        .outputFluids(1)
                        .minimalInputFluids(1)
                        .minimalInputs(2)
                        .needsOutputs(true)
                        .build(),
                RecipeTable.machine(VAR_MAP)
                        .title("VAR Arc Remelting")
                        .gui("ssite:textures/gui/recipes/aod")
                        .inputItems(3)
                        .outputItems(1)
                        .minimalInputItems(1)
                        .inputFluids(1)
                        .minimalInputFluids(1)
                        .minimalInputs(1)
                        .needsOutputs(true)
                        .build());
    }

    @Override
    public List<AddonMachine> addonMachines() {
        List<AddonMachine> machines = new ArrayList<>();
        for (AodTier tier : AodTier.values()) machines.add(converter(tier));
        machines.add(varFurnace());
        return List.copyOf(machines);
    }

    @Override
    public List<StructureLayout> structureLayouts() {
        List<StructureLayout> layouts = new ArrayList<>();
        for (AodTier tier : AodTier.values()) {
            layouts.add(StructureLayout.builder(tier.machineType())
                    .cell(0, 1, 0, CellMatch.part(AodConverterCore.LID_PART), StructureRole.CASING)
                    .build());
        }
        layouts.add(StructureLayout.builder(VarFurnaceCore.MACHINE_TYPE)
                .cell(0, 1, 0, CellMatch.part(AodConverterCore.LID_PART), StructureRole.CASING)
                .build());
        return List.copyOf(layouts);
    }

    @Override
    public List<LayoutPart> layoutParts() {
        return List.of(LayoutPart.builder(AodConverterCore.LID_PART, "Steel")
                .hardness(6.0F)
                .texture("aod_lid")
                .slots(PartSlots.builder(1).insert(MachineFace.TOP).build())
                .use()
                .field("open")
                .build());
    }

    /** Acid-proof 316L line. Temperature 0 uses the material melting point times 1.25. */
    @Override
    public List<FluidPipeDeclaration> fluidPipes() {
        return List.of(new FluidPipeDeclaration(
                "stainless_steel_316l", "StainlessSteel316L", 300, true, true, false, false, 0, 0));
    }

    private static BehaviorMachine varFurnace() {
        return BehaviorMachine.builder(MachineCasing.builder(VarFurnaceCore.MACHINE_TYPE, "Copper")
                        .machineType(VarFurnaceCore.MACHINE_TYPE)
                        .tier(3)
                        .build())
                .energy(EnergyInput.builder("EU", 512).anyPacket().capacity(1_000_000L)
                        .faces(HOT_FACES).build())
                .gui("ssite:textures/gui/aod_converter.png")
                .recipeMaps(VAR_MAP)
                .item(44, 26, BehaviorMachine.SlotRole.INPUT)
                .fluid(8000, 152, 8, BehaviorMachine.SlotRole.INPUT)
                .io(MachineIo.builder()
                        .itemInputs(MachineFace.TOP)
                        .itemOutputs(MachineFace.BOTTOM, MachineFace.LEFT, MachineFace.RIGHT, MachineFace.BACK)
                        .fluidInputs(GAS_FACES)
                        .build())
                .build();
    }

    private static BehaviorMachine converter(AodTier tier) {
        return BehaviorMachine.builder(MachineCasing.builder(tier.machineType(), tier.material())
                        .machineType(tier.machineType())
                        .tier(2)
                        .build())
                .energy(EnergyInput.builder("HU", 256).anyPacket().capacity(1_000_000L)
                        .faces(HOT_FACES).build())
                .auxiliaryEnergy(EnergyInput.builder("KU", 64).anyPacket().capacity(64_000L)
                        .faces(HOT_FACES).build())
                .gui("ssite:textures/gui/aod_converter.png")
                .recipeMaps(AOD_MAP)
                .item(44, 26, BehaviorMachine.SlotRole.INPUT)
                .item(116, 26, BehaviorMachine.SlotRole.OUTPUT)
                .fluid(8000, 152, 8, BehaviorMachine.SlotRole.INPUT)
                .io(MachineIo.builder()
                        .itemInputs(MachineFace.TOP)
                        .itemOutputs(MachineFace.BOTTOM, MachineFace.LEFT, MachineFace.RIGHT, MachineFace.BACK)
                        .fluidInputs(GAS_FACES)
                        .build())
                .build();
    }
}
