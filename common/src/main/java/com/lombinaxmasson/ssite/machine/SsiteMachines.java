package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.machine.AddonMachine;
import gregtech.api.addon.machine.AddonMachineContributor;
import gregtech.api.addon.machine.BehaviorMachine;
import gregtech.api.multiblock.CellMatch;
import gregtech.api.energy.EnergyInput;
import gregtech.api.addon.pipe.FluidPipeDeclaration;
import gregtech.api.addon.machine.LayoutPart;
import gregtech.api.machine.MachineCasing;
import gregtech.api.machine.MachineFace;
import gregtech.api.machine.MachineIo;
import gregtech.api.addon.machine.PartSlots;
import gregtech.api.addon.machine.PartTanks;
import gregtech.api.addon.recipe.RecipeTable;
import gregtech.api.multiblock.StructureLayout;
import gregtech.api.multiblock.StructureRole;
import com.lombinaxmasson.ssite.SsiteAddon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
                RecipeTable.machine(LARGE_AOD_MAP)
                        .title("Large AOD Refining")
                        .gui("ssite:textures/gui/recipes/aod")
                        .inputItems(4).outputItems(2).minimalInputItems(1)
                        .inputFluids(2).minimalInputFluids(1).minimalInputs(2).needsOutputs(true).build(),
                RecipeTable.machine(AOD_MAP)
                        .title("AOD Refining")
                        .gui("ssite:textures/gui/recipes/aod")
                        .inputItems(4)
                        .outputItems(2)
                        .minimalInputItems(1)
                        .inputFluids(1)
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
        for (AodTier tier : AodTier.values()) {
            machines.add(converter(tier));
            machines.add(largeConverter(tier));
        }
        machines.add(varFurnace());
        machines.addAll(new FluidizedBedMachines().addonMachines());
        return List.copyOf(machines);
    }

    /** Machine items first (small AOD, large AOD, VAR), then every other item in the addon namespace. */
    public static List<ItemStack> creativeStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        Set<Item> added = new LinkedHashSet<>();
        List<String> machines = new ArrayList<>();
        for (AodTier tier : AodTier.values()) {
            machines.add(tier.machineType());
            machines.add(tier.largeMachineType());
        }
        machines.add(VarFurnaceCore.MACHINE_TYPE);
        machines.add(FluidizedBedCore.MACHINE_TYPE);
        for (String path : machines) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryBuild(SsiteAddon.MOD_ID, path));
            if (item != Items.AIR && added.add(item)) stacks.add(new ItemStack(item));
        }
        for (Item item : BuiltInRegistries.ITEM) {
            if (SsiteAddon.MOD_ID.equals(BuiltInRegistries.ITEM.getKey(item).getNamespace()) && added.add(item))
                stacks.add(new ItemStack(item));
        }
        return stacks;
    }

    @Override
    public List<StructureLayout> structureLayouts() {
        List<StructureLayout> layouts = new ArrayList<>();
        for (AodTier tier : AodTier.values()) {
            layouts.add(LargeAodStructure.layout(tier));
            layouts.add(StructureLayout.builder(tier.machineType())
                    .cell(0, 1, 0, CellMatch.part(MoltenPool.LID_PART), StructureRole.CASING)
                    .build());
        }
        layouts.add(StructureLayout.builder(VarFurnaceCore.MACHINE_TYPE)
                .cell(0, 1, 0, CellMatch.part(MoltenPool.LID_PART), StructureRole.CASING)
                .build());
        layouts.addAll(new FluidizedBedMachines().structureLayouts());
        return List.copyOf(layouts);
    }

    @Override
    public List<LayoutPart> layoutParts() {
        List<LayoutPart> parts = new ArrayList<>();
        parts.add(lid(MoltenPool.LID_PART));
        parts.add(lid(LargeAodStructure.LID));
        for (AodTier tier : AodTier.values()) parts.add(LayoutPart.builder(tier.largeCasing(), tier.material())
                .hardness(8).texture("block/machine/aod_converter/" + tier.suffix() + "_side").build());
        for (String gas : List.of(LargeAodStructure.OXYGEN, LargeAodStructure.ARGON))
            parts.add(LayoutPart.builder(gas, "StainlessSteel")
                    .hardness(8).texture("block/aod_lid")
                    .tanks(PartTanks.builder(1).capacity(32000).insert(GAS_FACES).build())
                    .build());
        parts.addAll(new FluidizedBedMachines().layoutParts());
        return List.copyOf(parts);
    }

    private static LayoutPart lid(String name) {
        return LayoutPart.builder(name, "Steel")
                .hardness(6.0F)
                .texture("block/aod_lid")
                .slots(name.equals(LargeAodStructure.LID)
                        ? PartSlots.builder(2).insert(MachineFace.TOP).extract(MachineFace.TOP).build()
                        : PartSlots.builder(1).insert(MachineFace.TOP).build())
                .use()
                .tick()
                .field("open")
                .field("lid.mode")
                .build();
    }

    private static BehaviorMachine largeConverter(AodTier tier) {
        return BehaviorMachine.builder(MachineCasing.builder(tier.largeMachineType(), tier.material())
                        .machineType(tier.largeMachineType()).tier(3).build())
                // SDK requires one local face. BACK is buried against a mandatory wall; real HU enters via the sole.
                .energy(EnergyInput.builder("HU", 2304).anyPacket().capacity(9_000_000L).faces(MachineFace.BACK).build())
                .gui("ssite:textures/gui/aod_converter.png")
                .recipeMaps(LARGE_AOD_MAP)
                .clientState("structure.formed", "temp.k", "pool.amount")
                .item(44, 26, BehaviorMachine.SlotRole.INPUT)
                .item(116, 26, BehaviorMachine.SlotRole.OUTPUT)
                .io(MachineIo.builder().build())
                .build();
    }

    public static final String LARGE_AOD_MAP = "ssite.recipe.large_aod";

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
