package com.lombinaxmasson.ssite.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/**
 * Deterministic layout of one underground gas field. A field owns a square block region and is
 * generated slice by slice, so every chunk in the region can compute the same cave and spring
 * grid without writing outside its own chunk.
 */
public record GasFieldPlan(long seed, int anchorChunkX, int anchorChunkZ,
                           int centerX, int centerZ, int centerY,
                           int radiusX, int radiusY, int radiusZ,
                           int springCount, int quotaPerTick) {

    public static final int REGION_CHUNKS = 4;
    public static final int REGION_SHIFT = 2;
    public static final int REGION_BLOCKS = REGION_CHUNKS * 16;
    public static final int RARITY = 12;
    public static final int MIN_RADIUS = 18, MAX_RADIUS = 22;
    public static final int MIN_RADIUS_Y = 8, MAX_RADIUS_Y = 11;
    public static final int SPRING_STEP = 3;
    public static final int SPRING_LAYERS = 2;
    public static final int FLUID_PER_BLOCK = 1000;

    /** Host {@code FluidSpringRules} natural-gas roll bound; amount is a roll interval, not a tank. */
    public static final int HOST_SPRING_ROLL = 3000;

    /** Bump when the hidden layout or quota formula changes; old worlds keep generating deterministically. */
    public static final int FORMAT = 1;

    public static GasFieldPlan at(long worldSeed, int chunkX, int chunkZ) {
        int anchorX = anchorChunk(chunkX), anchorZ = anchorChunk(chunkZ);
        RandomSource random = RandomSource.create(mix(worldSeed, anchorX, anchorZ));
        if (random.nextInt(RARITY) != 0) return null;

        int radiusX = MIN_RADIUS + random.nextInt(MAX_RADIUS - MIN_RADIUS + 1);
        int radiusZ = MIN_RADIUS + random.nextInt(MAX_RADIUS - MIN_RADIUS + 1);
        int radiusY = MIN_RADIUS_Y + random.nextInt(MAX_RADIUS_Y - MIN_RADIUS_Y + 1);
        int marginX = radiusX + 1, marginZ = radiusZ + 1;
        int anchorXBlock = anchorX << 4, anchorZBlock = anchorZ << 4;
        int centerX = anchorXBlock + marginX + random.nextInt(REGION_BLOCKS - 2 * marginX);
        int centerZ = anchorZBlock + marginZ + random.nextInt(REGION_BLOCKS - 2 * marginZ);
        // Keep the whole ellipsoid below the surface and above the world floor.
        int centerY = -18 - random.nextInt(14);

        int springs = countSprings(radiusX, radiusY, radiusZ);
        int quota = Math.max(1, springs * FLUID_PER_BLOCK / HOST_SPRING_ROLL);
        return new GasFieldPlan(worldSeed, anchorX, anchorZ, centerX, centerZ, centerY,
                radiusX, radiusY, radiusZ, springs, quota);
    }

    public static GasFieldPlan at(ServerLevel level, int chunkX, int chunkZ) {
        return level == null ? null : at(level.getSeed(), chunkX, chunkZ);
    }

    public static GasFieldPlan at(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) return null;
        return at(server, pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static int anchorChunk(int chunk) {
        return chunk & ~(REGION_CHUNKS - 1);
    }

    public boolean ownsChunk(int chunkX, int chunkZ) {
        return chunkX >= anchorChunkX && chunkX < anchorChunkX + REGION_CHUNKS
                && chunkZ >= anchorChunkZ && chunkZ < anchorChunkZ + REGION_CHUNKS;
    }

    public int minY() {
        return centerY - radiusY;
    }

    public int maxY() {
        return centerY + radiusY;
    }

    public int minX() {
        return centerX - radiusX;
    }

    public int maxX() {
        return centerX + radiusX;
    }

    public int minZ() {
        return centerZ - radiusZ;
    }

    public int maxZ() {
        return centerZ + radiusZ;
    }

    public boolean covers(int x, int z) {
        long dx = x - centerX, dz = z - centerZ;
        return dx * dx * radiusZ * radiusZ + dz * dz * radiusX * radiusX
                <= (long) radiusX * radiusX * radiusZ * radiusZ;
    }

    public boolean contains(int x, int y, int z) {
        long dx = x - centerX, dy = y - centerY, dz = z - centerZ;
        long rx = radiusX, ry = radiusY, rz = radiusZ;
        return dx * dx * ry * ry * rz * rz + dy * dy * rx * rx * rz * rz + dz * dz * rx * rx * ry * ry
                <= rx * rx * ry * ry * rz * rz;
    }

    public int springY(int layer) {
        return minY() + 1 + layer * SPRING_STEP;
    }

    public boolean isSpring(int x, int y, int z) {
        int offsetX = x - centerX, offsetZ = z - centerZ;
        if (offsetX < -radiusX + 1 || offsetX > radiusX - 1) return false;
        if (offsetZ < -radiusZ + 1 || offsetZ > radiusZ - 1) return false;
        if (Math.floorMod(offsetX + radiusX - 1, SPRING_STEP) != 0) return false;
        if (Math.floorMod(offsetZ + radiusZ - 1, SPRING_STEP) != 0) return false;
        for (int layer = 0; layer < SPRING_LAYERS; layer++) {
            // The host spring needs replaceable headspace above it; require the same in the plan so
            // the advertised spring count matches the springs that can actually emit.
            if (y == springY(layer) && contains(x, y, z) && contains(x, y + 1, z)) return true;
        }
        return false;
    }

    private static int countSprings(int radiusX, int radiusY, int radiusZ) {
        int count = 0;
        for (int x = -radiusX + 1; x <= radiusX - 1; x += SPRING_STEP) {
            for (int z = -radiusZ + 1; z <= radiusZ - 1; z += SPRING_STEP) {
                for (int layer = 0; layer < SPRING_LAYERS; layer++) {
                    int y = -radiusY + 1 + layer * SPRING_STEP;
                    if (inside(x, y, z, radiusX, radiusY, radiusZ)
                            && inside(x, y + 1, z, radiusX, radiusY, radiusZ)) count++;
                }
            }
        }
        return count;
    }

    private static boolean inside(int x, int y, int z, int rx, int ry, int rz) {
        long dx = x, dy = y, dz = z;
        return dx * dx * ry * ry * rz * rz + dy * dy * rx * rx * rz * rz + dz * dz * rx * rx * ry * ry
                <= (long) rx * rx * ry * ry * rz * rz;
    }

    private static long mix(long seed, int anchorX, int anchorZ) {
        long value = seed;
        value ^= (long) anchorX * 0x9E3779B97F4A7C15L;
        value ^= (long) anchorZ * 0xC2B2AE3D27D4EB4FL;
        value ^= 0x5DEECE66DL;
        value ^= value >>> 33;
        value *= 0xFF51AFD7ED558CCDL;
        value ^= value >>> 33;
        value *= 0xC4CEB9FE1A85EC53L;
        value ^= value >>> 33;
        return value;
    }
}
