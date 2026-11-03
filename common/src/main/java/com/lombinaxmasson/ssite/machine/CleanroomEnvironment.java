package com.lombinaxmasson.ssite.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * P-04A server-side room registry and the registered {@link ProcessEnvironmentService} provider.
 *
 * <p>Only a ticking controller publishes a room. The registry keeps the interior set, the current
 * snapshot and one claim per room. Devices bind to a room when their own cell or one of the six
 * neighbours is an interior cell, so a machine standing inside the room resolves even though the
 * scan treats the machine block itself as a boundary. Chunks are checked with {@code hasChunkAt}
 * and are never loaded by this class.</p>
 *
 * <p>Refresh periods are explicit: a controller rescans every
 * {@link CleanroomControllerCore#SCAN_INTERVAL} ticks and touches its record every tick, a vent
 * report expires after {@link #VENT_REPORT_TICKS}, and a room whose controller has not touched it
 * for {@link #STALE_TICKS} stops serving devices.</p>
 */
public final class CleanroomEnvironment implements ProcessEnvironmentService.Provider {
    /** Ticks after the last controller touch before its room stops serving devices. */
    public static final long STALE_TICKS = 200;
    /** Ticks a vent report stays valid. */
    public static final long VENT_REPORT_TICKS = 40;
    /** Inactive or orphaned room records are forgotten after this many ticks. */
    public static final long FORGET_TICKS = 2400;

    private static final CleanroomEnvironment INSTANCE = new CleanroomEnvironment();
    private final Map<ResourceKey<Level>, LevelState> levels = new ConcurrentHashMap<>();

    private CleanroomEnvironment() {}

    public static void install() {
        ProcessEnvironmentService.register(INSTANCE);
    }

    public record VentReport(boolean running, int remainingPermille, long tick) {}

    private record RoomRecord(BlockPos controller, String roomId, Set<BlockPos> interior,
                              ProcessEnvironmentSnapshot snapshot, long tick, boolean active) {
        RoomRecord touch(long now) {
            return new RoomRecord(controller, roomId, interior, snapshot, now, active);
        }

        RoomRecord expire() {
            return new RoomRecord(controller, roomId, interior, snapshot, tick, false);
        }
    }

    @Override
    public ProcessEnvironmentSnapshot snapshot(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) {
            return ProcessEnvironmentSnapshot.unavailable(ProcessEnvironmentSnapshot.REASON_INVALID);
        }
        return state(level).snapshot(level, pos);
    }

    /** Refreshes the room claim and returns whether another live controller owns the same room. */
    public static boolean claim(Level level, BlockPos controller, String roomId) {
        return state(level).claim(controller, roomId, level.getGameTime());
    }

    public static boolean publish(Level level, BlockPos controller, CleanroomRoom.Scan scan,
                                  ProcessEnvironmentSnapshot snapshot) {
        return state(level).publish(controller, scan, snapshot, level.getGameTime());
    }

    /** Keeps the room fresh between scans and refreshes the claim. */
    public static void touch(Level level, BlockPos controller) {
        state(level).touch(controller, level.getGameTime());
    }

    /** The controller is gone: keep a tombstone so bound devices fail at once, not after a scan. */
    public static void withdraw(Level level, BlockPos controller) {
        state(level).withdraw(controller);
    }

    public static void reportVent(Level level, BlockPos pos, boolean running, int remainingPermille) {
        state(level).reportVent(pos, running, remainingPermille, level.getGameTime());
    }

    public static VentReport ventReport(Level level, BlockPos pos) {
        return state(level).ventReport(pos, level.getGameTime());
    }

    public static CleanroomRoom.Cells cells(Level level) {
        return pos -> {
            if (!level.hasChunkAt(pos)) return CleanroomRoom.Kind.UNLOADED;
            BlockState blockState = level.getBlockState(pos);
            if (blockState.isAir()) return CleanroomRoom.Kind.AIR;
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
            String blockId = key == null ? "" : key.toString();
            if (ProcessEnvironmentMachines.isController(blockId)) return CleanroomRoom.Kind.CONTROLLER;
            if (ProcessEnvironmentMachines.isVent(blockId)) return CleanroomRoom.Kind.VENT;
            if (ProcessEnvironmentMachines.isThermostat(blockId)) return CleanroomRoom.Kind.THERMOSTAT;
            if (ProcessEnvironmentMachines.isIsolationMount(blockId)) return CleanroomRoom.Kind.ISOLATION;
            if (ProcessEnvironmentMachines.isVibrationSource(blockId)) return CleanroomRoom.Kind.VIBRATION;
            if (isPassage(blockState)) return CleanroomRoom.Kind.PASSAGE;
            return CleanroomRoom.Kind.SEALED;
        };
    }

    /** Reads the host {@code lit} property, falling back to {@code running}; never guesses work. */
    public static boolean working(Level level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return false;
        BlockState blockState = level.getBlockState(pos);
        Boolean lit = booleanProperty(blockState, "lit");
        if (lit != null) return lit;
        Boolean running = booleanProperty(blockState, "running");
        return running != null && running;
    }

    public static boolean isControllerBlock(Level level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return false;
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock());
        return key != null && ProcessEnvironmentMachines.isController(key.toString());
    }

    private static boolean isPassage(BlockState state) {
        if (!state.getFluidState().isEmpty()) return true;
        Block block = state.getBlock();
        if (block instanceof DoorBlock && state.getValue(DoorBlock.OPEN)) return true;
        if (block instanceof TrapDoorBlock && state.getValue(TrapDoorBlock.OPEN)) return true;
        return block instanceof FenceGateBlock && state.getValue(FenceGateBlock.OPEN);
    }

    private static Boolean booleanProperty(BlockState state, String name) {
        for (Property<?> property : state.getProperties()) {
            if (!property.getName().equals(name) || property.getValueClass() != Boolean.class) continue;
            @SuppressWarnings("unchecked") Property<Boolean> value = (Property<Boolean>) property;
            return state.getValue(value);
        }
        return null;
    }

    private static LevelState state(Level level) {
        return INSTANCE.levels.computeIfAbsent(level.dimension(), key -> new LevelState());
    }

    private static final class LevelState {
        private final Map<String, RoomRecord> rooms = new HashMap<>();
        private final Map<BlockPos, String> controllerRooms = new HashMap<>();
        private final Map<BlockPos, Set<String>> cells = new HashMap<>();
        private final Map<BlockPos, VentReport> vents = new HashMap<>();
        private final RoomClaims claims = new RoomClaims();

        synchronized ProcessEnvironmentSnapshot snapshot(Level level, BlockPos pos) {
            long now = level.getGameTime();
            Set<String> ids = new LinkedHashSet<>();
            collect(pos.immutable(), ids);
            for (Direction direction : Direction.values()) collect(pos.relative(direction).immutable(), ids);
            ids.removeIf(id -> !rooms.containsKey(id));
            if (ids.isEmpty()) return ProcessEnvironmentSnapshot.unavailable(ProcessEnvironmentSnapshot.REASON_NO_ROOM);
            if (ids.size() > 1) {
                return ProcessEnvironmentSnapshot.invalid("", 0, 0, 0, 0,
                        ProcessEnvironmentSnapshot.REASON_OVERLAP);
            }
            String id = ids.iterator().next();
            RoomRecord record = rooms.get(id);
            if (!record.active()) {
                return ProcessEnvironmentSnapshot.unavailable(ProcessEnvironmentSnapshot.REASON_NO_CONTROLLER);
            }
            if (now - record.tick() > STALE_TICKS) {
                boolean gone = !level.hasChunkAt(record.controller())
                        || !isControllerBlock(level, record.controller());
                return ProcessEnvironmentSnapshot.unavailable(gone
                        ? ProcessEnvironmentSnapshot.REASON_NO_CONTROLLER
                        : ProcessEnvironmentSnapshot.REASON_UNLOADED);
            }
            if (claims.liveOwners(id, now, STALE_TICKS) > 1) {
                return ProcessEnvironmentSnapshot.invalid(id, record.snapshot().cleanLevel(),
                        record.snapshot().temperatureBand(), record.snapshot().vibrationBand(),
                        record.snapshot().revision(), ProcessEnvironmentSnapshot.REASON_OVERLAP);
            }
            return record.snapshot();
        }

        synchronized boolean claim(BlockPos controller, String roomId, long now) {
            if (roomId == null || roomId.isEmpty()) return false;
            return claims.claim(roomId, controller.immutable(), now, STALE_TICKS);
        }

        synchronized boolean publish(BlockPos controller, CleanroomRoom.Scan scan,
                                     ProcessEnvironmentSnapshot snapshot, long now) {
            BlockPos owner = controller.immutable();
            String roomId = scan.roomId();
            String oldId = controllerRooms.get(owner);
            if (oldId != null && !oldId.equals(roomId)) {
                release(owner, oldId);
                controllerRooms.remove(owner);
            }
            if (roomId.isEmpty() || scan.interior().isEmpty()) {
                prune(now);
                return false;
            }
            RoomRecord previous = rooms.get(roomId);
            if (previous == null || !previous.interior().equals(scan.interior())) {
                removeIndex(roomId);
                for (BlockPos cell : scan.interior()) {
                    cells.computeIfAbsent(cell.immutable(), key -> new LinkedHashSet<>()).add(roomId);
                }
            }
            rooms.put(roomId, new RoomRecord(owner, roomId, Set.copyOf(scan.interior()), snapshot, now, true));
            controllerRooms.put(owner, roomId);
            boolean overlap = claims.claim(roomId, owner, now, STALE_TICKS);
            prune(now);
            return overlap;
        }

        synchronized void touch(BlockPos controller, long now) {
            String id = controllerRooms.get(controller);
            if (id == null) return;
            RoomRecord record = rooms.get(id);
            if (record == null || !record.controller().equals(controller)) return;
            rooms.put(id, record.touch(now));
            claims.claim(id, controller, now, STALE_TICKS);
        }

        synchronized void withdraw(BlockPos controller) {
            String id = controllerRooms.get(controller);
            if (id == null) return;
            claims.release(id, controller);
            RoomRecord record = rooms.get(id);
            if (record != null && record.controller().equals(controller)) rooms.put(id, record.expire());
            controllerRooms.remove(controller);
        }

        synchronized void reportVent(BlockPos pos, boolean running, int remainingPermille, long now) {
            vents.put(pos.immutable(), new VentReport(running, Math.max(0, Math.min(1000, remainingPermille)), now));
        }

        synchronized VentReport ventReport(BlockPos pos, long now) {
            VentReport report = vents.get(pos.immutable());
            return report == null || now - report.tick() > VENT_REPORT_TICKS ? null : report;
        }

        private void collect(BlockPos pos, Set<String> ids) {
            Set<String> found = cells.get(pos);
            if (found != null) ids.addAll(found);
        }

        private void release(BlockPos owner, String roomId) {
            claims.release(roomId, owner);
            RoomRecord record = rooms.get(roomId);
            if (record != null && record.controller().equals(owner)) rooms.put(roomId, record.expire());
        }

        private void removeIndex(String roomId) {
            for (Set<String> ids : cells.values()) ids.remove(roomId);
            cells.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }

        private void prune(long now) {
            rooms.entrySet().removeIf(entry -> {
                RoomRecord record = entry.getValue();
                if (now - record.tick() <= FORGET_TICKS) return false;
                removeIndex(entry.getKey());
                controllerRooms.remove(record.controller(), entry.getKey());
                claims.release(entry.getKey(), record.controller());
                return true;
            });
            vents.entrySet().removeIf(entry -> now - entry.getValue().tick() > VENT_REPORT_TICKS);
        }
    }
}
