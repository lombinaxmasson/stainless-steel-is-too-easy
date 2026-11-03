package com.lombinaxmasson.ssite.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * P-04A bounded cleanroom detection.
 *
 * <p>The controller floods the air volume in front of its face and stops at the first solid
 * boundary. The scan never loads a chunk, never walks past {@link #MAX_VOLUME} interior cells and
 * never spends more than {@link #MAX_PROBES} neighbor probes. It is pure logic over a
 * {@link Cells} view so the enclosure rules run on the JVM without a world.</p>
 *
 * <p>An open door, an open trapdoor, an open fence gate or a fluid cell is a passage: the room is
 * still measured so consumers can report where it leaks, but the result is invalid. Unloaded
 * chunks, a blocked controller face, an oversized room or an exhausted budget abort the scan and
 * publish no interior volume.</p>
 */
public final class CleanroomRoom {
    /** Interior cells allowed in one room. */
    public static final int MAX_VOLUME = 2048;
    /** Longest interior span on any axis. */
    public static final int MAX_SPAN = 16;
    /** Hard ceiling on neighbor probes, including duplicate boundary probes. */
    public static final int MAX_PROBES = 32768;
    /** Leak positions kept for the status display. */
    public static final int MAX_REPORTED_LEAKS = 32;

    public enum Kind {
        /** Passable and part of the room volume. */
        AIR,
        /** Solid boundary cell. */
        SEALED,
        /** Passable opening: open door, trapdoor, gate or fluid. */
        PASSAGE,
        /** Chunk not loaded; the scan never force-loads it. */
        UNLOADED,
        CONTROLLER,
        VENT,
        THERMOSTAT,
        ISOLATION,
        VIBRATION
    }

    public interface Cells {
        Kind at(BlockPos pos);
    }

    public record Scan(boolean valid, String reason, String roomId,
                       Set<BlockPos> interior, List<BlockPos> leaks,
                       Set<BlockPos> vents, Set<BlockPos> thermostats,
                       int isolation, Set<BlockPos> vibrationSources) {
        public Scan {
            interior = unmodifiable(interior);
            leaks = List.copyOf(leaks);
            vents = unmodifiable(vents);
            thermostats = unmodifiable(thermostats);
            vibrationSources = unmodifiable(vibrationSources);
        }

        public int volume() {
            return interior.size();
        }

        public static Scan invalid(String reason) {
            return new Scan(false, reason, "", Set.of(), List.of(), Set.of(), Set.of(), 0, Set.of());
        }
    }

    private CleanroomRoom() {}

    private static <T> Set<T> unmodifiable(Set<T> source) {
        return source.isEmpty() ? Set.of() : Collections.unmodifiableSet(source);
    }

    public static Scan scan(BlockPos controller, Direction facing, Cells cells, String scope) {
        BlockPos origin = controller.immutable();
        BlockPos anchor = origin.relative(facing).immutable();
        Kind start = cells.at(anchor);
        if (start == Kind.UNLOADED) return Scan.invalid(ProcessEnvironmentSnapshot.REASON_UNLOADED);
        if (start != Kind.AIR) return Scan.invalid(ProcessEnvironmentSnapshot.REASON_INVALID);

        Set<BlockPos> interior = new LinkedHashSet<>();
        Set<BlockPos> leakCells = new LinkedHashSet<>();
        Set<BlockPos> ventCells = new LinkedHashSet<>();
        Set<BlockPos> thermostatCells = new LinkedHashSet<>();
        Set<BlockPos> isolationCells = new LinkedHashSet<>();
        Set<BlockPos> sourceCells = new LinkedHashSet<>();
        int probes = 0;
        int minX = anchor.getX(), minY = anchor.getY(), minZ = anchor.getZ();
        int maxX = minX, maxY = minY, maxZ = minZ;

        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        interior.add(anchor);
        queue.add(anchor);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values()) {
                if (++probes > MAX_PROBES) return Scan.invalid(ProcessEnvironmentSnapshot.REASON_BUDGET);
                BlockPos next = current.relative(direction).immutable();
                Kind kind = cells.at(next);
                if (kind == Kind.UNLOADED) return Scan.invalid(ProcessEnvironmentSnapshot.REASON_UNLOADED);
                if (kind == Kind.AIR) {
                    if (!interior.add(next)) continue;
                    if (interior.size() > MAX_VOLUME) return Scan.invalid(ProcessEnvironmentSnapshot.REASON_TOO_LARGE);
                    minX = Math.min(minX, next.getX());
                    minY = Math.min(minY, next.getY());
                    minZ = Math.min(minZ, next.getZ());
                    maxX = Math.max(maxX, next.getX());
                    maxY = Math.max(maxY, next.getY());
                    maxZ = Math.max(maxZ, next.getZ());
                    if (maxX - minX >= MAX_SPAN || maxY - minY >= MAX_SPAN || maxZ - minZ >= MAX_SPAN) {
                        return Scan.invalid(ProcessEnvironmentSnapshot.REASON_TOO_LARGE);
                    }
                    queue.add(next);
                    continue;
                }
                switch (kind) {
                    case PASSAGE -> {
                        if (leakCells.size() < MAX_REPORTED_LEAKS) leakCells.add(next);
                    }
                    case VENT -> ventCells.add(next);
                    case THERMOSTAT -> thermostatCells.add(next);
                    case ISOLATION -> isolationCells.add(next);
                    case VIBRATION -> sourceCells.add(next);
                    default -> { }
                }
            }
        }

        String roomId = scope + "@" + minX + "," + minY + "," + minZ + "#" + interior.size();
        if (!leakCells.isEmpty()) {
            return new Scan(false, ProcessEnvironmentSnapshot.REASON_LEAK, roomId, interior,
                    List.copyOf(leakCells), ventCells, thermostatCells, isolationCells.size(), sourceCells);
        }
        return new Scan(true, ProcessEnvironmentSnapshot.REASON_NONE, roomId, interior,
                List.of(), ventCells, thermostatCells, isolationCells.size(), sourceCells);
    }
}
