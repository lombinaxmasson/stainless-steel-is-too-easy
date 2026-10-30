package com.lombinaxmasson.ssite.testing;

import com.lombinaxmasson.ssite.machine.HeatTransportCore;
import gregtech.api.energy.EnergyType;
import gregtech.content.blockentity.machine.BehaviorMachineBlockEntity;
import gregtech.platform.neoforge.smeltery.SmeltingCrucibleEntity;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.List;

/** Actual registered HU hosts and native receivers, loaded only by -PssiteSmokeTest. */
@EventBusSubscriber(modid = "ssite_aod_smoke")
public final class HeatTransportSmoke {
    private HeatTransportSmoke() {}

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent event) {
        var command = Commands.literal("ssite_heat_transport_smoke").requires(source -> source.hasPermission(4));
        for (String stage : List.of("native", "shared", "topology", "disconnect", "unloaded")) {
            command.then(Commands.literal(stage).executes(context -> {
                ServerLevel level = context.getSource().getLevel();
                guard(level);
                switch (stage) {
                    case "native" -> nativeInput(level);
                    case "shared" -> shared(level);
                    case "topology" -> topology(level);
                    case "disconnect" -> disconnect(level);
                    case "unloaded" -> unloaded(level);
                    default -> throw new IllegalStateException(stage);
                }
                context.getSource().sendSuccess(() -> Component.literal("PASS: heat transport " + stage), false);
                return 1;
            }));
        }
        event.getDispatcher().register(command);
    }

    private static void guard(ServerLevel level) {
        String world = level.getServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize()
                .toString().replace('\\', '/');
        check(world.endsWith("/neoforge/build/large-aod-smoke/world"), "refusing non-fixture world: " + world);
        check(level.getServer().getPlayerCount() == 0, "refusing a world with online players");
    }

    private static Block block(String id) {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));
        check(block != Blocks.AIR, "missing block " + id);
        return block;
    }

    private static void nativeInput(ServerLevel level) {
        for (String id : List.of("heat_duct", "heating_plate", "heating_plate_from_legacy"))
            check(level.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("ssite", id)).isPresent(),
                    "missing converted recipe " + id);
        Rig single = new Rig(level, 0);
        var plate = single.plate(0, 0, 0);
        single.receiver(0, 1, 0);
        check(plate instanceof HeatTransportCore.RuntimeAccess, "thermal host mixin was not applied");
        for (Direction face : Direction.values()) {
            check(plate.doEnergyInjection(EnergyType.EU, face, 1, 100, true) == 0, "plate accepted non-HU input");
            check(plate.doEnergyInjection(EnergyType.HU, face, 0, 100, true) == 0, "plate accepted zero-sized packets");
        }
        check(plate.doEnergyInjection(EnergyType.HU, Direction.WEST, 100, 100, false) == 100,
                "native HU simulation rejected a valid plate");
        check(plate.storedEnergy() == 0 && !plate.state().getBoolean(HeatTransportCore.K_PROCESSED_SET),
                "simulated input mutated storage or consumed the network tick");
        inject(plate, 10_000);
        Ledger one = single.tick();
        check(one.output == 4096 && one.loss == 0 && plate.storedEnergy() == 5904,
                "single plate rate/remainder is wrong: " + one);
        check(single.tick().output == 0, "native host duplicated a network tick");

        Rig line = new Rig(level, 1);
        var inlet = line.duct(-3, 0, 0);
        line.duct(-2, 0, 0); line.duct(-1, 0, 0); line.plate(0, 0, 0); line.receiver(0, 1, 0);
        inject(inlet, 1000);
        Ledger routed = line.tick();
        check(routed.output == 970 && routed.loss == 30 && routed.debit == 1000,
                "three physical ducts did not apply 3% linear loss: " + routed);
    }

    private static void shared(ServerLevel level) {
        Rig direct = new Rig(level, 2); direct.grid();
        inject(direct.host(0, 0, 0), 9000);
        Ledger directLedger = direct.tick();
        check(directLedger.output == 9000 && directLedger.loss == 0, "plate-only shared pool lost heat");
        direct.receivers.forEach(receiver -> check(receiver.getEnergyBuffer() == 1000,
                "a plate without its own stored heat did not receive its fair share"));

        Rig split = new Rig(level, 3); split.grid();
        inject(split.duct(-1, 0, 1), 10_000);
        Ledger splitLedger = split.tick();
        check(splitLedger.output == 9900 && splitLedger.loss == 100 && split.stored() == 0,
                "nine destinations multiplied the rounding loss: " + splitLedger);
        split.receivers.forEach(receiver -> check(receiver.getEnergyBuffer() == 1100, "unequal nine-plate output"));

        Rig multiple = new Rig(level, 4); multiple.grid();
        var first = multiple.duct(-1, 0, 1); var second = multiple.duct(3, 0, 1);
        inject(first, 5000); inject(second, 100_000);
        Ledger multi = multiple.tick();
        check(multi.output == 9 * 4096 && multi.debit <= HeatTransportCore.PIPE_THROUGHPUT,
                "multiple inlets bypassed the network or plate budget: " + multi);
        check(first.storedEnergy() < 5000 && second.storedEnergy() < 100_000, "multi-inlet test did not use both inlets");
        multiple.receivers.forEach(receiver -> check(receiver.getEnergyBuffer() == 4096, "per-plate output cap failed"));
        for (int i = 0; i < 5; i++) check(multiple.tick().output == 0, "same-time native ticks duplicated HU");
    }

    private static void topology(ServerLevel level) {
        Rig longLine = new Rig(level, 5);
        for (int x = -13; x < 0; x++) longLine.duct(x, 0, 0);
        longLine.plate(0, 0, 0); longLine.receiver(0, 1, 0);
        invalid(longLine, longLine.host(-13, 0, 0), HeatTransportCore.TOO_LONG);

        Rig ring = new Rig(level, 6);
        ring.duct(0, 0, 0); ring.duct(1, 0, 0); ring.duct(0, 0, 1); ring.duct(1, 0, 1);
        ring.plate(2, 0, 1); ring.receiver(2, 1, 1);
        invalid(ring, ring.host(0, 0, 0), HeatTransportCore.LOOP);

        Rig wide = new Rig(level, 7);
        for (int x = 0; x < 4; x++) { wide.plate(x, 0, 0); wide.receiver(x, 1, 0); }
        invalid(wide, wide.host(0, 0, 0), HeatTransportCore.INVALID_PLATES);

        Rig separate = new Rig(level, 8);
        separate.plate(0, 0, 0); separate.duct(1, 0, 0); separate.plate(2, 0, 0);
        separate.receiver(0, 1, 0); separate.receiver(2, 1, 0);
        invalid(separate, separate.host(1, 0, 0), HeatTransportCore.INVALID_PLATES);

        Rig vertical = new Rig(level, 9);
        var low = vertical.plate(0, 0, 0); var high = vertical.plate(0, 1, 0);
        vertical.receiver(0, 2, 0);
        check(HeatTransportCore.inspect(level, low.getBlockPos()).plates() == 1
                        && HeatTransportCore.inspect(level, high.getBlockPos()).plates() == 1,
                "stacked plates joined through their output faces");
        inject(low, 1000); inject(high, 500);
        check(vertical.tick().output == 500 && low.storedEnergy() == 1000,
                "lower plate recursively heated the transport plate above it");
    }

    private static void disconnect(ServerLevel level) {
        Rig rig = new Rig(level, 10);
        var upstream = rig.duct(-3, 0, 0);
        rig.duct(-2, 0, 0); rig.duct(-1, 0, 0);
        var downstream = rig.plate(0, 0, 0); rig.receiver(0, 1, 0);
        inject(upstream, 1000); inject(downstream, 900);
        rig.remove(-2, 0, 0);
        check(upstream.doEnergyInjection(EnergyType.HU, Direction.WEST, 1, 100, true) == 0,
                "broken upstream pipe accepted additional source heat");
        Ledger ledger = rig.tick();
        check(ledger.output == 900 && ledger.loss == 0 && upstream.storedEnergy() == 1000,
                "cut route teleported upstream heat or discarded downstream residual heat: " + ledger);
    }

    private static void unloaded(ServerLevel level) {
        // A separate remote chunk, outside all other fixtures. Never request its eastern neighbor.
        BlockPos origin = new BlockPos(8207, 100, 8200), neighbor = origin.east();
        check(!level.hasChunkAt(neighbor), "unloaded fixture precondition failed: eastern chunk already loaded");
        level.getChunk(origin.getX() >> 4, origin.getZ() >> 4);
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-2, 0, -1), origin.offset(0, 1, 1)))
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
        level.setBlock(origin, block("ssite:heat_duct").defaultBlockState(), 18);
        level.setBlock(origin.west(), block("ssite:heating_plate").defaultBlockState(), 18);
        level.setBlock(origin.west().above(), block("gt6ce:smelting_crucible_steel").defaultBlockState(), 18);
        check(!level.hasChunkAt(neighbor), "fixture construction loaded the neighbor; test is invalid");
        var host = (BehaviorMachineBlockEntity) level.getBlockEntity(origin);
        seed(level, host, 1000);
        check(HeatTransportCore.inspect(level, origin).status() == HeatTransportCore.UNLOADED,
                "runtime did not reject the unloaded boundary");
        check(host.doEnergyInjection(EnergyType.HU, Direction.WEST, 1, 100, true) == 0,
                "unloaded route accepted new input");
        host.setChanged();
        check(!level.hasChunkAt(neighbor), "marking the HU buffer dirty loaded the eastern neighbor before its tick");
        BehaviorMachineBlockEntity.serverTick(level, origin, host.getBlockState(), host);
        long storedAfter = host.storedEnergy();
        boolean neighborLoaded = level.hasChunkAt(neighbor);
        check(storedAfter == 1000,
                "unloaded runtime changed stored HU: before=1000, after=" + storedAfter + ", neighborLoaded=" + neighborLoaded);
        check(!neighborLoaded,
                "runtime indicator update loaded the eastern chunk: stored=" + storedAfter
                        + ", status=" + host.state().getInt(HeatTransportCore.K_STATUS));
        var receiver = (SmeltingCrucibleEntity) level.getBlockEntity(origin.west().above());
        check(receiver.getEnergyBuffer() == 0, "unloaded network still emitted HU");
    }

    private static void invalid(Rig rig, BehaviorMachineBlockEntity inlet, int status) {
        check(HeatTransportCore.inspect(rig.level, inlet.getBlockPos()).status() == status,
                "wrong topology status, expected " + status);
        check(inlet.doEnergyInjection(EnergyType.HU, Direction.WEST, 1, 100, false) == 0
                        && inlet.doEnergyInjection(EnergyType.HU, Direction.WEST, 1, 100, true) == 0,
                "invalid network accepted native HU");
        seed(rig.level, inlet, 1000); // Models energy retained from a formerly valid route.
        Ledger result = rig.tick();
        check(result.output == 0 && result.loss == 0 && rig.stored() == 1000,
                "invalid topology consumed or transferred its retained heat");
    }

    private static void inject(BehaviorMachineBlockEntity host, long amount) {
        long before = host.storedEnergy();
        check(host.doEnergyInjection(EnergyType.HU, Direction.WEST, 1, amount, true) == amount,
                "native HU API refused " + amount + " HU at " + host.getBlockPos());
        check(host.storedEnergy() == before + amount, "native HU packet count did not match actual buffer credit");
    }

    private static void seed(ServerLevel level, BehaviorMachineBlockEntity host, long heat) {
        CompoundTag data = new CompoundTag(); host.writeMachine(data, level.registryAccess());
        data.putLong("gt.energy", heat); host.readMachine(data, level.registryAccess());
        check(host.storedEnergy() == heat, "fixture NBT heat seed failed");
    }

    private static void check(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private record Ledger(long output, long loss, long debit) {}

    private static final class Rig {
        final ServerLevel level;
        final BlockPos origin;
        final List<BehaviorMachineBlockEntity> hosts = new ArrayList<>();
        final List<SmeltingCrucibleEntity> receivers = new ArrayList<>();
        Rig(ServerLevel level, int index) {
            this.level = level; origin = new BlockPos(1152 + index * 48, 100, 64);
            BlockPos min = origin.offset(-15, -1, -3), max = origin.offset(7, 3, 5);
            // Load a one-chunk margin so a normal network never fails due to an unrelated boundary.
            for (int x = (min.getX() >> 4) - 1; x <= (max.getX() >> 4) + 1; x++)
                for (int z = (min.getZ() >> 4) - 1; z <= (max.getZ() >> 4) + 1; z++) {
                    level.setChunkForced(x, z, true); level.getChunk(x, z);
                }
            for (BlockPos pos : BlockPos.betweenClosed(min, max)) level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
        BehaviorMachineBlockEntity host(int x, int y, int z) {
            return (BehaviorMachineBlockEntity) level.getBlockEntity(origin.offset(x, y, z));
        }
        BehaviorMachineBlockEntity duct(int x, int y, int z) { return add(x, y, z, "ssite:heat_duct"); }
        BehaviorMachineBlockEntity plate(int x, int y, int z) { return add(x, y, z, "ssite:heating_plate"); }
        BehaviorMachineBlockEntity add(int x, int y, int z, String id) {
            BlockPos pos = origin.offset(x, y, z); level.setBlockAndUpdate(pos, block(id).defaultBlockState());
            check(level.getBlockEntity(pos) instanceof BehaviorMachineBlockEntity, "missing real transport host " + id);
            var host = (BehaviorMachineBlockEntity) level.getBlockEntity(pos); hosts.add(host); return host;
        }
        void receiver(int x, int y, int z) {
            BlockPos pos = origin.offset(x, y, z);
            level.setBlockAndUpdate(pos, block("gt6ce:smelting_crucible_steel").defaultBlockState());
            check(level.getBlockEntity(pos) instanceof SmeltingCrucibleEntity, "missing native GT receiver");
            receivers.add((SmeltingCrucibleEntity) level.getBlockEntity(pos));
        }
        void grid() {
            for (int x = 0; x < 3; x++) for (int z = 0; z < 3; z++) { plate(x, 0, z); receiver(x, 1, z); }
        }
        void remove(int x, int y, int z) {
            var host = host(x, y, z); hosts.remove(host);
            check(host.storedEnergy() == 0, "test must not destroy stored heat to simulate a cut");
            level.setBlockAndUpdate(host.getBlockPos(), Blocks.AIR.defaultBlockState());
        }
        long stored() { return hosts.stream().mapToLong(BehaviorMachineBlockEntity::storedEnergy).sum(); }
        long received() { return receivers.stream().mapToLong(SmeltingCrucibleEntity::getEnergyBuffer).sum(); }
        Ledger tick() {
            long before = stored(), receivedBefore = received();
            for (var host : hosts)
                BehaviorMachineBlockEntity.serverTick(level, host.getBlockPos(), host.getBlockState(), host);
            long output = received() - receivedBefore, debit = before - stored();
            // The same whole-network report is published on every member. All lossy fixtures
            // have one component; the disconnect fixture's two components both have zero loss.
            long loss = 0;
            if (debit != 0) {
                for (var host : hosts) loss = Math.max(loss, host.state().getLong(HeatTransportCore.K_LOSS));
            }
            check(debit == output + loss, "native source debit != native receiver credit + route loss: "
                    + debit + " != " + output + " + " + loss);
            check(hosts.stream().allMatch(host -> host.storedEnergy() >= 0), "negative native transport buffer");
            return new Ledger(output, loss, debit);
        }
    }
}
