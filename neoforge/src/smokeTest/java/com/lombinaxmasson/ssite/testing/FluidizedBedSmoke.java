package com.lombinaxmasson.ssite.testing;

import com.lombinaxmasson.ssite.machine.AodTier;
import com.lombinaxmasson.ssite.machine.FluidizedBedCore;
import com.lombinaxmasson.ssite.machine.FluidizedBedStructure;
import com.lombinaxmasson.ssite.machine.LargeAodStructure;
import gregtech.api.machine.MachineFace;
import gregtech.api.addon.platform.AddonItems;
import gregtech.api.addon.platform.AddonTool;
import gregtech.content.blockentity.machine.BehaviorMachineBlockEntity;
import gregtech.content.blockentity.machine.LargeCrucibleControllerBlockEntity;
import gregtech.content.blockentity.energy.EnergyNodeBlockEntity;
import gregtech.content.data.GregTechTags;
import gregtech.content.material.Materials;
import gregtech.content.multiblock.SharedLargeCrucibleStructure;
import gregtech.engine.energy.EnergyNodeSpec;
import gregtech.platform.neoforge.smeltery.SmeltingCrucibleEntity;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.List;

/** Real-world HU accounting and integration fixtures; excluded from release jars. */
@EventBusSubscriber(modid = "ssite_aod_smoke")
public final class FluidizedBedSmoke {
    private static final int BASE_X = 768;
    private static final int BASE_Y = 100;
    private static final int CONTROL = 4, AUTOMATION = 5, AOD = 6, CRUCIBLE = 7;
    private static final long MAX_HU = 2304;
    private static final Direction[] FACINGS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    private static long aodTemperature, crucibleTemperature;
    private static boolean prepared;

    private FluidizedBedSmoke() {}

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent event) {
        var root = Commands.literal("ssite_fluidized_bed_smoke").requires(source -> source.hasPermission(4));
        for (String stage : List.of("prepare", "break", "repair", "structure", "energy", "energy_gate", "fuels", "controls",
                "automation_start", "automation_check", "receivers_start", "receivers_check", "persistence")) {
            root.then(Commands.literal(stage).executes(context -> {
                var level = context.getSource().getLevel();
                guard(level);
                check(stage.equals("prepare") || prepared, "run prepare before other stages");
                switch (stage) {
                    case "prepare" -> prepare(level);
                    case "break" -> breakStructures(level);
                    case "repair" -> repairStructures(level);
                    case "structure" -> verifyStructures(level);
                    case "energy" -> energy(level);
                    case "energy_gate" -> energyGate(level);
                    case "fuels" -> fuels(level);
                    case "controls" -> controls(level);
                    case "automation_start" -> startAutomation(level);
                    case "automation_check" -> verifyAutomation(level);
                    case "receivers_start" -> startReceivers(level);
                    case "receivers_check" -> verifyReceivers(level);
                    case "persistence" -> persistence(level);
                    default -> throw new IllegalStateException(stage);
                }
                context.getSource().sendSuccess(() -> Component.literal("PASS: fluidized bed " + stage), false);
                return 1;
            }));
        }
        event.getDispatcher().register(root);
    }

    private static void guard(ServerLevel level) {
        String world = level.getServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize()
                .toString().replace('\\', '/');
        check(world.endsWith("/neoforge/build/large-aod-smoke/world"), "refusing a non-fixture world: " + world);
        check(level.getServer().getPlayerCount() == 0, "refusing a world with online players");
    }

    private static BlockPos origin(int index) { return new BlockPos(BASE_X + index * 32, BASE_Y, 0); }
    private static Direction facing(int index) { return index < FACINGS.length ? FACINGS[index] : Direction.NORTH; }
    private static BlockPos pos(int index, int right, int up, int back) {
        return FluidizedBedStructure.world(origin(index), facing(index), right, up, back);
    }
    private static List<BlockPos> targets(int index) {
        List<BlockPos> targets = new ArrayList<>();
        for (int x = 5; x <= 7; x++) for (int z = 1; z <= 3; z++) targets.add(pos(index, x, 2, z));
        return targets;
    }
    private static Block block(String id) {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));
        check(block != Blocks.AIR, "missing registered block " + id);
        return block;
    }
    private static BehaviorMachineBlockEntity machine(ServerLevel level, int index) {
        check(level.getBlockEntity(origin(index)) instanceof BehaviorMachineBlockEntity, "missing controller " + index);
        return (BehaviorMachineBlockEntity) level.getBlockEntity(origin(index));
    }
    private static SmeltingCrucibleEntity receiver(ServerLevel level, BlockPos pos) {
        check(level.getBlockEntity(pos) instanceof SmeltingCrucibleEntity, "missing real GT crucible at " + pos);
        return (SmeltingCrucibleEntity) level.getBlockEntity(pos);
    }

    private static void prepare(ServerLevel level) {
        for (String recipe : List.of("fluidized_bed_combustor", "fluidized_bed_casing", "fluidized_bed_duct",
                "fluidized_bed_heat_plate", "fluidized_bed_foundation")) {
            block("ssite:" + recipe);
            check(level.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("ssite", recipe)).isPresent(),
                    "crafting recipe was not loaded from the NeoForge resource conversion: " + recipe);
        }
        for (int index = 0; index < 8; index++) {
            BlockPos min = origin(index).offset(-12, -2, -12), max = origin(index).offset(12, 10, 12);
            for (int x = min.getX() >> 4; x <= max.getX() >> 4; x++)
                for (int z = min.getZ() >> 4; z <= max.getZ() >> 4; z++) level.setChunkForced(x, z, true);
            for (BlockPos p : BlockPos.betweenClosed(min, max)) level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(origin(index), block("ssite:fluidized_bed_combustor").defaultBlockState()
                    .setValue(HorizontalDirectionalBlock.FACING, facing(index)));
            for (var cell : FluidizedBedStructure.preview().entrySet()) {
                BlockPos local = cell.getKey();
                level.setBlockAndUpdate(pos(index, local.getX(), local.getY(), local.getZ()), cell.getValue().defaultBlockState());
            }
            if (index <= AUTOMATION) for (BlockPos target : targets(index))
                level.setBlockAndUpdate(target, block("gt6ce:smelting_crucible_steel").defaultBlockState());
        }
        prepared = true;
    }

    private static void breakStructures(ServerLevel level) {
        for (int index = 0; index < 4; index++) {
            var source = machine(level, index);
            check(source.structureFormed(), facing(index) + " complete structure did not form");
            verifyShapes(level, index, true);
            // Store residual heat before breaking. A broken hot source must not keep feeding the platform.
            setEnergy(level, source, 12000, 0);
            level.setBlockAndUpdate(pos(index, 5, 1, 1), Blocks.AIR.defaultBlockState());
        }
    }

    private static void repairStructures(ServerLevel level) {
        for (int index = 0; index < 4; index++) {
            var source = machine(level, index);
            check(!source.structureFormed(), facing(index) + " missing heat plate remained formed");
            verifyShapes(level, index, false);
            check(source.state().getLong("heat.output") == 0, "broken source emitted HU");
            for (BlockPos target : targets(index))
                check(receiver(level, target).getTemperature() <= 293 && receiver(level, target).getEnergyBuffer() == 0,
                        "broken structure heated a native receiver");
            level.setBlockAndUpdate(pos(index, 5, 1, 1), block("ssite:fluidized_bed_heat_plate").defaultBlockState());
        }
    }

    private static void verifyStructures(ServerLevel level) {
        for (int index = 0; index < 8; index++)
            check(machine(level, index).structureFormed(), "repaired rotated structure did not form: " + index);
        for (int index = 0; index < 4; index++) verifyShapes(level, index, true);
    }

    private static void verifyShapes(ServerLevel level, int index, boolean formed) {
        for (int x : new int[]{-1, 1}) {
            BlockPos foundation = pos(index, x, 0, 0);
            check(level.getBlockState(foundation).getBlock().hasDynamicShape(), "formed foundation shape was cached as a full cube");
            shape(level, foundation, formed ? .28 : 1);
        }
        shape(level, origin(index), 1);
        for (int x = 5; x <= 7; x++) for (int z = 1; z <= 3; z++) {
            shape(level, pos(index, x, 0, z), 1);
            BlockPos plate = pos(index, x, 1, z);
            if (!level.getBlockState(plate).isAir()) shape(level, plate, 1);
        }
    }

    private static void shape(ServerLevel level, BlockPos pos, double height) {
        var state = level.getBlockState(pos);
        for (var shape : List.of(state.getShape(level, pos), state.getCollisionShape(level, pos))) {
            check(!shape.isEmpty(), "missing outline/collision shape at " + pos);
            var bounds = shape.bounds();
            check(Math.abs(bounds.minX) < 1e-6 && Math.abs(bounds.minY) < 1e-6 && Math.abs(bounds.minZ) < 1e-6
                            && Math.abs(bounds.maxX - 1) < 1e-6 && Math.abs(bounds.maxY - height) < 1e-6
                            && Math.abs(bounds.maxZ - 1) < 1e-6,
                    "incorrect world-dispatched outline/collision shape at " + pos + ": " + bounds);
        }
    }

    private static CompoundTag snapshot(ServerLevel level, BehaviorMachineBlockEntity source) {
        CompoundTag tag = new CompoundTag();
        source.writeMachine(tag, level.registryAccess());
        return tag;
    }

    private static void setEnergy(ServerLevel level, BehaviorMachineBlockEntity source, long heat, long air) {
        CompoundTag tag = snapshot(level, source);
        tag.putLong("gt.energy", heat);
        tag.putLong("gt.aux_energy", air);
        source.readMachine(tag, level.registryAccess());
    }

    private static void enabled(BehaviorMachineBlockEntity source, boolean enabled) {
        if (source.toggle(0) != enabled) check(source.flip(0), "burner toggle unavailable");
    }

    private static void configure(ServerLevel level, int index, boolean enabled, long heat, long air) {
        var source = machine(level, index);
        source.inventory().setStackInSlot(0, new ItemStack(Items.COAL, 16));
        source.inventory().setStackInSlot(1, ItemStack.EMPTY);
        source.state().setLong("fuel.pending", 0);
        source.state().setLong("burner.power", 4);
        enabled(source, enabled);
        setEnergy(level, source, heat, air);
    }

    /** Freeze ordinary ticks, then tick only the real source: receiver HU remains directly measurable. */
    private static long tickAccounting(ServerLevel level, int index, boolean requireEveryCell) {
        var source = machine(level, index);
        long before = source.storedEnergy();
        List<SmeltingCrucibleEntity> receivers = new ArrayList<>();
        List<Long> buffers = new ArrayList<>();
        for (BlockPos target : targets(index)) if (level.getBlockEntity(target) instanceof SmeltingCrucibleEntity crucible) {
            receivers.add(crucible); buffers.add(crucible.getEnergyBuffer());
        }
        BehaviorMachineBlockEntity.serverTick(level, origin(index), source.getBlockState(), source);
        long delivered = 0;
        for (int i = 0; i < receivers.size(); i++) {
            long received = receivers.get(i).getEnergyBuffer() - buffers.get(i);
            check(received >= 0 && (!requireEveryCell || received > 0), "HU failed to reach a platform cell");
            delivered += received;
        }
        long generated = source.state().getLong("heat.generated");
        long output = source.state().getLong("heat.output");
        long loss = source.state().getLong("heat.loss");
        check(delivered == output, "reported HU differs from nine actual receiver buffers");
        check(output >= 0 && output <= MAX_HU, "nine outputs multiplied the total power budget");
        check(before + generated - output - loss == source.storedEnergy(), "source/receivers violate HU conservation");
        check(source.storedEnergy() >= 0 && source.storedEnergy() <= source.energyCapacity(), "heat buffer outside capacity");
        return delivered;
    }

    private static void energy(ServerLevel level) {
        for (int index = 0; index < 4; index++) {
            configure(level, index, true, 0, 10000);
            var source = machine(level, index);
            for (Direction side : Direction.values())
                check(source.doEnergyInjection("HU", side, 1, 100, true) == 0, "external HU may not be converted into fuel heat");
            tickAccounting(level, index, false); // New heat becomes available on the next tick.
            check(source.state().getLong("heat.generated") > 0, "fuel plus KU did not generate HU");
            check(tickAccounting(level, index, true) > 0, "all nine cells must supply real HU");
            BlockPos blocked = targets(index).get(8);
            level.setBlockAndUpdate(blocked, Blocks.AIR.defaultBlockState());
            tickAccounting(level, index, false);
            level.setBlockAndUpdate(blocked, block("gt6ce:smelting_crucible_steel").defaultBlockState());
            enabled(source, false);
        }
    }

    private static EnergyNodeBlockEntity pullSource(ServerLevel level, BlockPos pos, Direction outputFace,
                                                    GregTechTags.Tag output, long packet) {
        level.setBlockAndUpdate(pos, block("gt6ce:electric_heater_lv").defaultBlockState()
                .setValue(BlockStateProperties.FACING, outputFace));
        check(level.getBlockEntity(pos) instanceof EnergyNodeBlockEntity, "native energy node fixture unavailable");
        var node = (EnergyNodeBlockEntity) level.getBlockEntity(pos);
        // Current heaters/engines push only. Exercise native pull accounting with a test-only specification,
        // rather than falsely passing a rejection check against a source that can never offer energy.
        node.setSpec(EnergyNodeSpec.builder("ssite_smoke_pull_" + output.getShortName().toLowerCase(), Materials.Steel)
                .kind(EnergyNodeSpec.Kind.CONVERTER).input(GregTechTags.Energy.EU, packet)
                .output(output, packet).capacity(packet * 10).build());
        check(node.doEnergyInjection(GregTechTags.Energy.EU, outputFace.getOpposite(), packet, 4, true) == 4,
                "native pull fixture could not receive its input energy");
        check(node.isEnergyEmittingTo(output, outputFace, false)
                        && node.getEnergyOffered(output, outputFace, packet) > 0
                        && node.doEnergyExtraction(output, outputFace, packet, 1, false) == 1,
                "negative test is invalid: native fixture does not actually offer extractable energy");
        return node;
    }

    private static void energyGate(ServerLevel level) {
        configure(level, CONTROL, false, 0, 0);
        var source = machine(level, CONTROL);
        BlockPos back = pos(CONTROL, 0, 0, 1), top = origin(CONTROL).above();
        var backState = level.getBlockState(back);
        var topState = level.getBlockState(top);
        check(topState.isAir(), "the KU input has no accessible adjacent cell");
        try {
            var heat = pullSource(level, back, Direction.NORTH, GregTechTags.Energy.HU, MAX_HU);
            long heatBefore = heat.stored();
            BehaviorMachineBlockEntity.serverTick(level, origin(CONTROL), source.getBlockState(), source);
            check(source.storedEnergy() == 0 && heat.stored() == heatBefore,
                    "native main-channel polling bypassed the external HU rejection");
            var air = pullSource(level, top, Direction.DOWN, GregTechTags.Energy.KU, 32);
            long airBefore = air.stored();
            BehaviorMachineBlockEntity.serverTick(level, origin(CONTROL), source.getBlockState(), source);
            check(source.storedEnergy() == 0 && heat.stored() == heatBefore, "HU poll gate failed while KU was connected");
            check(snapshot(level, source).getLong("gt.aux_energy") == airBefore && air.stored() == 0,
                    "HU pull protection also disabled or duplicated the independent KU auxiliary pull");
        } finally {
            level.setBlockAndUpdate(back, backState);
            level.setBlockAndUpdate(top, topState);
        }
    }

    private static void fuels(ServerLevel level) {
        record FuelCase(ItemStack stack, long heat) {}
        List<FuelCase> candidates = new ArrayList<>(List.of(
                new FuelCase(new ItemStack(Items.COAL), 180_000),
                new FuelCase(new ItemStack(Items.CHARCOAL), 180_000),
                new FuelCase(new ItemStack(Items.COAL_BLOCK), 1_620_000)));
        for (var nativeFuel : List.of(
                new FuelCase(AddonItems.stack("dust", "Coal", 1), 180_000),
                new FuelCase(AddonItems.stack("dust", "Charcoal", 1), 180_000),
                new FuelCase(AddonItems.stack("dust", "Coke", 1), 360_000),
                new FuelCase(AddonItems.stack("dust", "CoalCoke", 1), 360_000),
                new FuelCase(AddonItems.stack("dust", "Lignite", 1), 90_000),
                new FuelCase(AddonItems.stack("dust", "LigniteCoke", 1), 180_000),
                new FuelCase(AddonItems.stack("dust", "Anthracite", 1), 540_000),
                new FuelCase(AddonItems.stack("dust", "PetCoke", 1), 720_000))) {
            if (!nativeFuel.stack().isEmpty()) candidates.add(nativeFuel);
        }
        for (var candidate : candidates) {
            ItemStack fuel = candidate.stack();
            configure(level, CONTROL, true, 0, 6400);
            var source = machine(level, CONTROL);
            check(source.items().isItemValid(0, fuel), "actual GT item/fuel API rejects " + fuel);
            source.inventory().setStackInSlot(0, fuel.copy());
            tickAccounting(level, CONTROL, false);
            check(source.inventory().getStackInSlot(0).isEmpty(), "accepted fuel was not consumed exactly once: " + fuel);
            check(source.state().getLong("heat.generated") > 0
                            && source.state().getLong("heat.generated") + source.state().getLong("fuel.pending")
                            == candidate.heat(),
                    "fuel does not credit 150 percent of its original FluidBed heat: " + fuel);
            ItemStack ash = source.inventory().getStackInSlot(1);
            check(ash.getCount() == (fuel.is(Items.COAL_BLOCK) ? 9 : 1), "incorrect whole-U ash yield for " + fuel);
            var composition = AddonItems.composition(fuel);
            boolean charcoal = fuel.is(Items.CHARCOAL) || composition.units().size() == 1
                    && composition.units().getFirst().material().equals("Charcoal");
            ItemStack expected = AddonItems.stack("dustTiny", charcoal ? "Ashes" : "DarkAshes", 1);
            check(!expected.isEmpty() && ash.is(expected.getItem()), "charcoal/coal resolved to the wrong native ash material");
        }
        configure(level, CONTROL, true, 0, 6400);
        var source = machine(level, CONTROL);
        ItemStack fragment = AddonItems.stack("dustTiny", "Coal", 1);
        check(!fragment.isEmpty(), "tiny coal fixture is unavailable");
        check(!source.items().isItemValid(0, fragment), "tiny coal bypasses whole-U fuel filtering");
        source.inventory().setStackInSlot(0, fragment.copy());
        tickAccounting(level, CONTROL, false);
        check(source.inventory().getStackInSlot(0).getCount() == 1 && source.inventory().getStackInSlot(1).isEmpty()
                        && source.state().getLong("heat.generated") == 0 && source.state().getLong("fuel.pending") == 0,
                "fragmented coal was consumed or amplified into ash/heat");
        enabled(source, false);
    }

    private static void controls(ServerLevel level) {
        configure(level, CONTROL, false, 0, 0);
        var source = machine(level, CONTROL);
        tickAccounting(level, CONTROL, false);
        check(source.inventory().getStackInSlot(0).getCount() == 16 && source.state().getLong("heat.generated") == 0,
                "disabled source consumed fuel");
        enabled(source, true);
        tickAccounting(level, CONTROL, false);
        check(source.inventory().getStackInSlot(0).getCount() == 16 && source.state().getLong("heat.generated") == 0,
                "source burns without KU air");
        check(level.getBlockState(origin(CONTROL).above()).isAir(), "KU input is buried inside required structure");
        long accepted = source.doEnergyInjection("KU", Direction.UP, 32, 100, true);
        check(accepted > 0, "native KU injection did not charge the blower");
        var player = FakePlayerFactory.getMinecraft(level);
        player.setShiftKeyDown(false);
        int initial = (int) source.state().getLong("burner.power");
        for (int i = 0; i < 4; i++) {
            check(source.onTool(AddonTool.SCREWDRIVER, player, InteractionHand.MAIN_HAND, MachineFace.FRONT), "screwdriver not handled");
            check(source.state().getLong("burner.power") == (initial + i) % 4 + 1, "screwdriver power cycle incorrect");
        }
        BlockPos signal = pos(CONTROL, 0, 0, -1);
        level.setBlockAndUpdate(signal, Blocks.REDSTONE_BLOCK.defaultBlockState());
        tickAccounting(level, CONTROL, false);
        check(source.state().getLong("heat.generated") == 0 && source.inventory().getStackInSlot(0).getCount() == 16,
                "redstone stop consumed new fuel");
        setEnergy(level, source, 12000, 3200);
        check(tickAccounting(level, CONTROL, true) > 0 && source.state().getLong("heat.generated") == 0,
                "redstone erased residual heat or continued new combustion");
        level.setBlockAndUpdate(signal, Blocks.AIR.defaultBlockState());
        configure(level, CONTROL, true, 0, 3200);
        ItemStack fullAsh = AddonItems.stack("dustTiny", "DarkAshes", 64);
        check(!fullAsh.isEmpty(), "native coal ash material unavailable");
        source.inventory().setStackInSlot(1, fullAsh);
        tickAccounting(level, CONTROL, false);
        check(source.inventory().getStackInSlot(0).getCount() == 16 && source.state().getLong("fuel.pending") == 0,
                "blocked ash output consumed fuel without reserving its ash");
        source.inventory().setStackInSlot(1, ItemStack.EMPTY);
        tickAccounting(level, CONTROL, false);
        check(source.state().getLong("heat.generated") > 0 && !source.inventory().getStackInSlot(1).isEmpty(),
                "clearing ash output did not resume atomic fuel/ash processing");
        enabled(source, false);
    }

    private static void startAutomation(ServerLevel level) {
        configure(level, AUTOMATION, true, 0, 10000);
        var source = machine(level, AUTOMATION);
        source.inventory().setStackInSlot(0, ItemStack.EMPTY);
        var input = source.itemHandler(Direction.NORTH);
        check(input != null && input.insertItem(0, new ItemStack(Items.DIRT), false).getCount() == 1,
                "fuel port accepts non-fuel");
        check(input.insertItem(1, new ItemStack(Items.COAL), false).getCount() == 1, "fuel port inserts into ash slot");
        level.setBlockAndUpdate(pos(AUTOMATION, 0, 0, -1), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.SOUTH));
        var feed = (HopperBlockEntity) level.getBlockEntity(pos(AUTOMATION, 0, 0, -1));
        check(feed != null, "input hopper missing");
        feed.setItem(0, new ItemStack(Items.COAL, 4));
        level.setBlockAndUpdate(origin(AUTOMATION).below(), Blocks.HOPPER.defaultBlockState());
    }

    private static void verifyAutomation(ServerLevel level) {
        var source = machine(level, AUTOMATION);
        var feed = (HopperBlockEntity) level.getBlockEntity(pos(AUTOMATION, 0, 0, -1));
        var ash = (HopperBlockEntity) level.getBlockEntity(origin(AUTOMATION).below());
        check(feed != null && ash != null && feed.isEmpty(), "real input hopper did not transfer coal");
        int ashCount = 0;
        for (int slot = 0; slot < ash.getContainerSize(); slot++) {
            ItemStack stack = ash.getItem(slot);
            check(!stack.is(Items.COAL), "output hopper stole unburned fuel");
            ashCount += stack.getCount();
        }
        check(ashCount == 4, "real output hopper did not extract exactly the four committed fuel ashes");
        check(source.inventory().getStackInSlot(0).isEmpty()
                        && targets(AUTOMATION).stream().anyMatch(p -> receiver(level, p).getTemperature() > 293),
                "automatically fed fuel did not heat the native receivers");
        enabled(source, false);
    }

    private static void startReceivers(ServerLevel level) {
        BlockPos aodPos = pos(AOD, 6, 3, 0);
        level.setBlockAndUpdate(aodPos, block("ssite:" + AodTier.STEEL.largeMachineType()).defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
        for (var cell : LargeAodStructure.preview(AodTier.STEEL).entrySet()) {
            BlockPos p = cell.getKey();
            level.setBlockAndUpdate(LargeAodStructure.world(aodPos, Direction.NORTH, p.getX(), p.getY(), p.getZ()), cell.getValue().defaultBlockState());
        }
        BlockPos nativePos = pos(CRUCIBLE, 6, 2, 2);
        level.setBlockAndUpdate(nativePos, block("gt6ce:large_steel_crucible").defaultBlockState());
        var nativeCrucible = (LargeCrucibleControllerBlockEntity) level.getBlockEntity(nativePos);
        check(nativeCrucible != null, "native large crucible missing");
        for (var cell : SharedLargeCrucibleStructure.CELLS)
            level.setBlockAndUpdate(nativePos.offset(cell.x(), cell.y(), cell.z()),
                    (cell.air() ? Blocks.AIR : nativeCrucible.variant().wall()).defaultBlockState());
        aodTemperature = ((BehaviorMachineBlockEntity) level.getBlockEntity(aodPos)).getCrucibleTemperature();
        crucibleTemperature = nativeCrucible.getTemperature();
        for (int index : new int[]{AOD, CRUCIBLE}) configure(level, index, true, 0, 10000);
    }

    private static void verifyReceivers(ServerLevel level) {
        var aod = (BehaviorMachineBlockEntity) level.getBlockEntity(pos(AOD, 6, 3, 0));
        var nativeCrucible = (LargeCrucibleControllerBlockEntity) level.getBlockEntity(pos(CRUCIBLE, 6, 2, 2));
        check(aod != null && aod.structureFormed(), "real large AOD did not form on the platform");
        check(nativeCrucible != null && nativeCrucible.isStructureOk(), "native large crucible did not form on the platform");
        check(aod.getCrucibleTemperature() > Math.max(293, aodTemperature), "large AOD did not gain temperature from actual HU");
        check(nativeCrucible.getTemperature() > Math.max(293, crucibleTemperature), "native large crucible did not gain temperature from actual HU");
        for (int index : new int[]{AOD, CRUCIBLE}) enabled(machine(level, index), false);
    }

    private static void persistence(ServerLevel level) {
        var source = machine(level, CONTROL);
        enabled(source, true);
        source.state().setLong("burner.power", 3);
        source.state().setLong("fuel.pending", 3456);
        setEnergy(level, source, 4567, 1234);
        CompoundTag saved = snapshot(level, source);
        source.state().setLong("burner.power", 1);
        source.state().setLong("fuel.pending", 0);
        enabled(source, false);
        setEnergy(level, source, 0, 0);
        source.readMachine(saved, level.registryAccess());
        check(source.toggle(0) && source.state().getLong("burner.power") == 3
                        && source.state().getLong("fuel.pending") == 3456 && source.storedEnergy() == 4567
                        && snapshot(level, source).getLong("gt.aux_energy") == 1234,
                "native machine NBT round-trip lost power, switches, fuel or energy");
        enabled(source, false);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
