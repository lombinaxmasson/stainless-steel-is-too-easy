package com.lombinaxmasson.ssite.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Server-side wind and cached exposure for one installed rotor.
 *
 * <p>Call {@link #sample} on every loaded tick while a rotor is installed, including when another
 * condition prevents generation. Call {@link #clear} when the rotor or controller is removed.
 * Terrain is sampled on staggered schedules; world and chunk objects are never held strongly.</p>
 */
public final class WindTurbineEnvironment {
    public static final int CLEAR = 0, BLOCKED = 1, UNLOADED = 2, INDOORS = 3,
            OVERLAP = 4, WRONG_DIMENSION = 5;

    public static final double ROTOR_RADIUS = 5.201;
    public static final int REGION_SIZE = 256, WIND_PERIOD = 3600;
    public static final int CLEARANCE_INTERVAL = 40, INTAKE_INTERVAL = 100;
    public static final int INTAKE_LENGTH = 16;

    private static final int WIND_INTERVAL = 20;
    private static final double HALF_DEPTH = 0.25;
    private static final int SEARCH_RADIUS = 13, MAX_NEIGHBOURS = 256;
    private static final long CLAIM_TTL = 2;
    private static final Map<MinecraftServer, Index> INDEXES = new WeakHashMap<>();

    public record Reading(double wind, int status) {}

    private Index index;
    private Owner owner;
    private BlockPos position;
    private Direction facing;
    private List<BlockPos> clearance = List.of();
    private List<List<BlockPos>> intake = List.of();
    private Set<Chunk> chunks = Set.of();
    private final Map<Chunk, WeakReference<Object>> loadedChunks = new HashMap<>();
    private long lastSample = Long.MIN_VALUE, lastClearance = Long.MIN_VALUE,
            lastIntake = Long.MIN_VALUE, lastOverlap = Long.MIN_VALUE;
    private long lastWind = Long.MIN_VALUE;
    private int clearanceStatus = BLOCKED, intakeStatus = INDOORS;
    private int phase;
    private double exposure;
    private double regional;
    private boolean overlapping;

    public Reading sample(ServerLevel level, BlockPos pos, Direction direction) {
        if (!Level.OVERWORLD.equals(level.dimension())) {
            clear();
            return new Reading(0, WRONG_DIMENSION);
        }
        if (direction.getAxis() == Direction.Axis.Y) {
            clear();
            return new Reading(0, BLOCKED);
        }
        Index current = index(level);
        if (index != current || !pos.equals(position) || direction != facing) {
            clear();
            index = current;
            position = pos.immutable();
            facing = direction;
            phase = (int) (mix(position.asLong()) & 0x7FFFFFFF);
            prepareSamples();
            owner = index.add(this, position, facing, level.getGameTime());
        }
        long now = level.getGameTime();
        if (lastSample != Long.MIN_VALUE && (now < lastSample || now - lastSample > 1)) invalidate();
        lastSample = now;
        index.touch(owner, now);
        index.reap();

        // getChunkNow is non-loading. Identity changes also invalidate a cached air result after
        // a neighbour unloads and reloads between two callbacks.
        if (!chunksReady(level)) {
            invalidate();
            return new Reading(0, UNLOADED);
        }
        if (due(now, lastClearance, CLEARANCE_INTERVAL, phase)) {
            clearanceStatus = scanClearance(level);
            lastClearance = now;
        }
        if (due(now, lastIntake, INTAKE_INTERVAL, phase)) {
            scanIntake(level);
            lastIntake = now;
        }
        if (due(now, lastOverlap, CLEARANCE_INTERVAL, phase)) {
            overlapping = index.overlaps(level, owner, now);
            lastOverlap = now;
        }
        if (clearanceStatus != CLEAR) return new Reading(0, clearanceStatus);
        if (overlapping) return new Reading(0, OVERLAP);
        if (intakeStatus != CLEAR) return new Reading(0, intakeStatus);

        double altitude = Math.max(0, Math.min(64, position.getY() - level.getSeaLevel()));
        double height = 0.92 + altitude * (0.16 / 64.0);
        double weather = level.isThundering() ? 1.20 : level.isRaining() ? 1.08 : 1.0;
        if (due(now, lastWind, WIND_INTERVAL, phase)) {
            regional = regionalWind(level.getSeed(), now, position.getX() + 0.5, position.getZ() + 0.5);
            lastWind = now;
        }
        return new Reading(regional * height * weather * exposure, CLEAR);
    }

    /** Removes the rotor claim without keeping a server or level alive. */
    public void clear() {
        if (index != null && owner != null) index.remove(owner);
        index = null;
        owner = null;
        position = null;
        facing = null;
        clearance = List.of();
        intake = List.of();
        chunks = Set.of();
        loadedChunks.clear();
        lastSample = Long.MIN_VALUE;
        invalidate();
    }

    private void invalidate() {
        lastClearance = lastIntake = lastOverlap = lastWind = Long.MIN_VALUE;
        clearanceStatus = BLOCKED;
        intakeStatus = INDOORS;
        exposure = 0;
        overlapping = false;
    }

    private void prepareSamples() {
        Direction right = facing.getClockWise();
        BlockPos center = position.relative(facing);
        List<BlockPos> disk = new ArrayList<>();
        Set<Chunk> required = new LinkedHashSet<>();
        required.add(Chunk.at(position));
        int radius = (int) Math.ceil(ROTOR_RADIUS + 0.5);
        for (int horizontal = -radius; horizontal <= radius; horizontal++) {
            for (int vertical = -radius; vertical <= radius; vertical++) {
                double dx = Math.max(0, Math.abs(horizontal) - 0.5);
                double dy = Math.max(0, Math.abs(vertical) - 0.5);
                if (dx * dx + dy * dy > ROTOR_RADIUS * ROTOR_RADIUS) continue;
                BlockPos sample = center.relative(right, horizontal).above(vertical);
                disk.add(sample);
                required.add(Chunk.at(sample));
            }
        }
        List<List<BlockPos>> lines = new ArrayList<>();
        for (int horizontal : new int[]{-3, 0, 3}) {
            for (int vertical : new int[]{-3, 0, 3}) {
                List<BlockPos> line = new ArrayList<>(INTAKE_LENGTH);
                BlockPos start = center.relative(right, horizontal).above(vertical);
                for (int distance = 1; distance <= INTAKE_LENGTH; distance++) {
                    BlockPos sample = start.relative(facing, distance);
                    line.add(sample);
                    required.add(Chunk.at(sample));
                }
                lines.add(List.copyOf(line));
            }
        }
        clearance = List.copyOf(disk);
        intake = List.copyOf(lines);
        chunks = Set.copyOf(required);
    }

    private boolean chunksReady(ServerLevel level) {
        boolean changed = false;
        for (Chunk chunk : chunks) {
            Object loaded = level.getChunkSource().getChunkNow(chunk.x(), chunk.z());
            if (loaded == null) {
                loadedChunks.remove(chunk);
                return false;
            }
            WeakReference<Object> previous = loadedChunks.get(chunk);
            if (previous == null || previous.get() != loaded) {
                loadedChunks.put(chunk, new WeakReference<>(loaded));
                changed = true;
            }
        }
        if (changed) invalidate();
        return true;
    }

    private int scanClearance(ServerLevel level) {
        for (BlockPos cell : clearance) {
            if (level.isOutsideBuildHeight(cell)) return BLOCKED;
            if (!level.hasChunkAt(cell)) return UNLOADED;
            if (obstacle(level, cell)) return BLOCKED;
        }
        return CLEAR;
    }

    private void scanIntake(ServerLevel level) {
        int open = 0;
        boolean outside = false;
        for (List<BlockPos> line : intake) {
            int length = 0;
            for (BlockPos cell : line) {
                if (level.isOutsideBuildHeight(cell)) break;
                if (!level.hasChunkAt(cell)) {
                    intakeStatus = UNLOADED;
                    exposure = 0;
                    return;
                }
                if (obstacle(level, cell)) break;
                length++;
            }
            open += length;
            if (length > 0) {
                // A nearby outdoor inlet or an unobstructed inlet reaching outside is enough.
                // The motion-blocking heightmap includes solid glass roofs unlike a light test.
                outside |= exposed(level, line.get(0));
                if (length == line.size()) outside |= exposed(level, line.get(line.size() - 1));
            }
        }
        exposure = 0.65 + 0.35 * open / (double) (intake.size() * INTAKE_LENGTH);
        intakeStatus = outside ? CLEAR : INDOORS;
    }

    private static boolean obstacle(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.getFluidState().isEmpty() || !state.getCollisionShape(level, pos).isEmpty();
    }

    private static boolean exposed(ServerLevel level, BlockPos pos) {
        return level.hasChunkAt(pos)
                && level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) <= pos.getY();
    }

    private static boolean due(long now, long last, int interval, int phase) {
        return last == Long.MIN_VALUE || now < last
                || Math.floorDiv(now + phase % interval, interval)
                != Math.floorDiv(last + phase % interval, interval);
    }

    /** Smooth deterministic field; boundaries and target changes have matching first derivatives. */
    static double regionalWind(long seed, long tick, double x, double z) {
        double rx = x / REGION_SIZE, rz = z / REGION_SIZE;
        int ix = (int) Math.floor(rx), iz = (int) Math.floor(rz);
        double sx = smooth(rx - ix), sz = smooth(rz - iz);
        long epoch = Math.floorDiv(tick, WIND_PERIOD);
        double time = smooth(Math.floorMod(tick, WIND_PERIOD) / (double) WIND_PERIOD);
        double before = region(seed, epoch, ix, iz, sx, sz);
        double after = region(seed, epoch + 1, ix, iz, sx, sz);
        return lerp(before, after, time);
    }

    private static double region(long seed, long epoch, int x, int z, double sx, double sz) {
        return lerp(lerp(target(seed, epoch, x, z), target(seed, epoch, x + 1, z), sx),
                lerp(target(seed, epoch, x, z + 1), target(seed, epoch, x + 1, z + 1), sx), sz);
    }

    private static double target(long seed, long epoch, int x, int z) {
        long hash = mix(seed ^ (epoch * 0xD1B54A32D192ED03L)
                ^ (x * 0x9E3779B97F4A7C15L) ^ (z * 0x94D049BB133111EBL));
        return 0.72 + 0.43 * (hash >>> 11) * 0x1.0p-53;
    }

    private static long mix(long value) {
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    private static double smooth(double value) { return value * value * (3 - 2 * value); }
    private static double lerp(double a, double b, double amount) { return a + (b - a) * amount; }

    private static synchronized Index index(ServerLevel level) {
        return INDEXES.computeIfAbsent(level.getServer(), ignored -> new Index());
    }

    private record Chunk(int x, int z) {
        static Chunk at(BlockPos pos) { return new Chunk(pos.getX() >> 4, pos.getZ() >> 4); }
    }

    private record Section(int x, int y, int z) {
        static Section at(BlockPos pos) {
            return new Section(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
        }
    }

    private static final class Owner extends WeakReference<WindTurbineEnvironment> {
        final BlockPos pos;
        final Direction facing;
        final Section section;
        long tick;

        Owner(WindTurbineEnvironment environment, BlockPos pos, Direction facing,
              long tick, ReferenceQueue<WindTurbineEnvironment> queue) {
            super(environment, queue);
            this.pos = pos;
            this.facing = facing;
            this.tick = tick;
            section = Section.at(pos);
        }
    }

    private static final class Index {
        final Map<Section, Map<BlockPos, Owner>> sections = new HashMap<>();
        final ReferenceQueue<WindTurbineEnvironment> expired = new ReferenceQueue<>();

        Owner add(WindTurbineEnvironment environment, BlockPos pos, Direction facing, long now) {
            Owner added = new Owner(environment, pos, facing, now, expired);
            sections.computeIfAbsent(added.section, ignored -> new HashMap<>()).put(pos, added);
            dirtyNeighbours(pos);
            return added;
        }

        void touch(Owner owner, long now) {
            owner.tick = now;
            Map<BlockPos, Owner> bucket = sections.computeIfAbsent(owner.section, ignored -> new HashMap<>());
            if (bucket.put(owner.pos, owner) != owner) dirtyNeighbours(owner.pos);
        }

        void remove(Owner owner) {
            Map<BlockPos, Owner> bucket = sections.get(owner.section);
            if (bucket == null || !bucket.remove(owner.pos, owner)) return;
            if (bucket.isEmpty()) sections.remove(owner.section);
            dirtyNeighbours(owner.pos);
        }

        void reap() {
            // Bounded cleanup also removes dead claims in distant sections, without a world walk.
            for (int n = 0; n < 16; n++) {
                Owner owner = (Owner) expired.poll();
                if (owner == null) return;
                remove(owner);
            }
        }

        void dirtyNeighbours(BlockPos pos) {
            int visited = 0;
            for (Section section : nearby(pos)) {
                Map<BlockPos, Owner> bucket = sections.get(section);
                if (bucket == null) continue;
                for (Owner owner : bucket.values()) {
                    if (++visited > MAX_NEIGHBOURS) return;
                    WindTurbineEnvironment environment = owner.get();
                    if (environment != null) environment.lastOverlap = Long.MIN_VALUE;
                }
            }
        }

        boolean overlaps(ServerLevel level, Owner current, long now) {
            int inspected = 0;
            for (Section section : nearby(current.pos)) {
                Map<BlockPos, Owner> bucket = sections.get(section);
                if (bucket == null) continue;
                Iterator<Owner> iterator = bucket.values().iterator();
                while (iterator.hasNext()) {
                    Owner other = iterator.next();
                    if (other == current) continue;
                    WindTurbineEnvironment environment = other.get();
                    if (environment == null || now < other.tick || now - other.tick > CLAIM_TTL
                            || !level.hasChunkAt(other.pos)) {
                        iterator.remove();
                        continue;
                    }
                    // Dense invalid placements must not turn rotor checks into an unbounded scan.
                    if (++inspected > MAX_NEIGHBOURS) return true;
                    if (sweepsOverlap(current.pos, current.facing, other.pos, other.facing)) return true;
                }
                if (bucket.isEmpty()) sections.remove(section);
            }
            return false;
        }
    }

    private static List<Section> nearby(BlockPos pos) {
        List<Section> result = new ArrayList<>(27);
        for (int x = (pos.getX() - SEARCH_RADIUS) >> 4; x <= (pos.getX() + SEARCH_RADIUS) >> 4; x++) {
            for (int y = (pos.getY() - SEARCH_RADIUS) >> 4; y <= (pos.getY() + SEARCH_RADIUS) >> 4; y++) {
                for (int z = (pos.getZ() - SEARCH_RADIUS) >> 4; z <= (pos.getZ() + SEARCH_RADIUS) >> 4; z++) {
                    result.add(new Section(x, y, z));
                }
            }
        }
        return result;
    }

    /** Intersection of the two vertical swept disks, including the blade's axial thickness. */
    static boolean sweepsOverlap(BlockPos a, Direction aFacing, BlockPos b, Direction bFacing) {
        BlockPos ca = a.relative(aFacing), cb = b.relative(bFacing);
        double dx = ca.getX() - cb.getX(), dy = ca.getY() - cb.getY(), dz = ca.getZ() - cb.getZ();
        if (aFacing.getAxis() == bFacing.getAxis()) {
            double normal = aFacing.getAxis() == Direction.Axis.X ? dx : dz;
            double horizontal = aFacing.getAxis() == Direction.Axis.X ? dz : dx;
            return Math.abs(normal) <= HALF_DEPTH * 2
                    && horizontal * horizontal + dy * dy <= 4 * ROTOR_RADIUS * ROTOR_RADIUS;
        }
        double acrossA = Math.max(0, Math.abs(aFacing.getAxis() == Direction.Axis.X ? dz : dx) - HALF_DEPTH);
        double acrossB = Math.max(0, Math.abs(bFacing.getAxis() == Direction.Axis.X ? dz : dx) - HALF_DEPTH);
        if (acrossA > ROTOR_RADIUS || acrossB > ROTOR_RADIUS) return false;
        double reachA = Math.sqrt(ROTOR_RADIUS * ROTOR_RADIUS - acrossA * acrossA);
        double reachB = Math.sqrt(ROTOR_RADIUS * ROTOR_RADIUS - acrossB * acrossB);
        return Math.abs(dy) <= reachA + reachB;
    }
}
