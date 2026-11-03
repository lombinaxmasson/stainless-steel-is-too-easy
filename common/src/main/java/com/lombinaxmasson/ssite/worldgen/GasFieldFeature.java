package com.lombinaxmasson.ssite.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.FluidState;

/**
 * Writes one chunk-sized slice of a deterministic underground gas field. The cave, spring grid and
 * field identity are recomputed from the world seed for every chunk, so no shared worldgen state or
 * neighbour chunk write is needed.
 */
public final class GasFieldFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation GAS = ResourceLocation.tryBuild("gt6ce", "gas_natural_gas");
    private static final ResourceLocation SPRING = ResourceLocation.tryBuild("gt6ce", "fluid_spring_gas_natural_gas");
    private static final ResourceLocation INDICATOR = ResourceLocation.tryBuild("gt6ce", "grassblock_yellow");

    public GasFieldFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ServerLevel server = level.getLevel();
        if (server.dimension() != Level.OVERWORLD) return false;

        BlockPos origin = context.origin();
        GasFieldPlan plan = GasFieldPlan.at(server, origin.getX() >> 4, origin.getZ() >> 4);
        if (plan == null) return false;

        BlockState gas = state(GAS);
        if (gas == null) return false;

        boolean placed = carve(level, plan, gas, origin);
        BlockState spring = state(SPRING);
        if (spring != null) placed |= springs(level, plan, spring, origin);
        BlockState indicator = state(INDICATOR);
        if (indicator != null) placed |= indicators(level, plan, indicator, origin);
        return placed;
    }

    private static boolean carve(WorldGenLevel level, GasFieldPlan plan, BlockState gas, BlockPos origin) {
        int chunkX = (origin.getX() >> 4) << 4, chunkZ = (origin.getZ() >> 4) << 4;
        boolean placed = false;
        for (int x = 0; x < 16; x++) {
            int worldX = chunkX + x;
            for (int z = 0; z < 16; z++) {
                int worldZ = chunkZ + z;
                if (!plan.covers(worldX, worldZ)) continue;
                for (int y = plan.minY(); y <= plan.maxY(); y++) {
                    if (!plan.contains(worldX, y, worldZ)) continue;
                    BlockPos pos = new BlockPos(worldX, y, worldZ);
                    BlockState current = level.getBlockState(pos);
                    // Carve every solid block, not just the base-stone tag: a spring cell that
                    // lands on an ore would otherwise stay solid and be silently skipped while the
                    // plan still counts it.
                    if (!current.isSolid() || current.is(Blocks.BEDROCK)) continue;
                    if (!level.setBlock(pos, gas, 2)) continue;
                    schedule(level, pos, gas);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static boolean springs(WorldGenLevel level, GasFieldPlan plan, BlockState spring, BlockPos origin) {
        int chunkX = (origin.getX() >> 4) << 4, chunkZ = (origin.getZ() >> 4) << 4;
        boolean placed = false;
        for (int x = 0; x < 16; x++) {
            int worldX = chunkX + x;
            for (int z = 0; z < 16; z++) {
                int worldZ = chunkZ + z;
                if (!plan.covers(worldX, worldZ)) continue;
                for (int layer = 0; layer < GasFieldPlan.SPRING_LAYERS; layer++) {
                    int y = plan.springY(layer);
                    if (!plan.isSpring(worldX, y, worldZ)) continue;
                    BlockPos pos = new BlockPos(worldX, y, worldZ);
                    BlockState current = level.getBlockState(pos);
                    if (!current.isAir() && current.getFluidState().isEmpty()) continue;
                    BlockPos below = pos.below();
                    // The cavity is gas-filled, so a bottom-layer spring often has gas below it.
                    // Give it a real floor and an air headspace; otherwise the host spring block
                    // entity never activates and the advertised spring count is a lie.
                    if (!level.getBlockState(below).isSolidRender(level, below)) {
                        level.setBlock(below, Blocks.DEEPSLATE.defaultBlockState(), 2);
                    }
                    level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
                    if (level.setBlock(pos, spring, 2)) placed = true;
                }
            }
        }
        return placed;
    }

    private static boolean indicators(WorldGenLevel level, GasFieldPlan plan, BlockState indicator, BlockPos origin) {
        int chunkX = origin.getX() >> 4, chunkZ = origin.getZ() >> 4;
        if (chunkX != plan.centerX() >> 4 || chunkZ != plan.centerZ() >> 4) return false;
        boolean placed = false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int x = plan.centerX() + dx, z = plan.centerZ() + dz;
                BlockPos surface = surface(level, x, z);
                if (surface == null) continue;
                if (level.setBlock(surface, indicator, 2)) placed = true;
            }
        }
        return placed;
    }

    private static BlockPos surface(WorldGenLevel level, int x, int z) {
        int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        for (int y = top - 1; y >= top - 6; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK)) {
                BlockPos above = pos.above();
                return level.getBlockState(above).isAir() ? above : null;
            }
        }
        return null;
    }

    private static void schedule(WorldGenLevel level, BlockPos pos, BlockState state) {
        FluidState fluid = state.getFluidState();
        if (!fluid.isEmpty()) level.scheduleTick(pos, fluid.getType(), fluid.getType().getTickDelay(level));
    }

    private static BlockState state(ResourceLocation id) {
        Block block = BuiltInRegistries.BLOCK.get(id);
        return block == null || block == Blocks.AIR ? null : block.defaultBlockState();
    }
}
