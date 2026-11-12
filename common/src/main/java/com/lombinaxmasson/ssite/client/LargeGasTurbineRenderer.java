package com.lombinaxmasson.ssite.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import gregtech.content.block.machine.LargeGasTurbineControllerBlock;
import gregtech.content.block.machine.MultiblockPortBlock;
import gregtech.content.blockentity.machine.LargeGasTurbineControllerBlockEntity;
import gregtech.content.blockentity.machine.MultiblockPortBlockEntity;
import gregtech.content.multiblock.AxialStructureTransform;
import gregtech.content.multiblock.GasTurbineDefinitions;
import gregtech.content.multiblock.TurbineStructure;
import gregtech.engine.material.GTMaterialRegistry;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

/** Client visual for the four native GT6CE gas turbine grades. */
public final class LargeGasTurbineRenderer {
    public static final String MODEL = "/assets/ssite/obj/large_gas_turbine.obj";
    private static final Map<String, ResourceLocation> TEXTURES = Map.of(
            "StainlessSteel", atlas("stainless_steel"), "Titanium", atlas("titanium"),
            "TungstenSteel", atlas("tungsten_steel"), "Adamantium", atlas("adamantium"));
    private static final float PIVOT_Y = 1.5F;

    private static final class Assets {
        private static final ObjMesh MESH = ObjMesh.load(MODEL);
        private static final ObjVboMesh<Batch> VBO = new ObjVboMesh<>(Batch.class, MESH, LargeGasTurbineRenderer::batch);
    }

    private static final class NativeRenderer {
        private static final LargeGasTurbineRenderer INSTANCE =
                new LargeGasTurbineRenderer(ServiceLoader.load(MeshSink.class).findFirst().orElseThrow());
    }

    private static final class WallBlocks {
        private static final Set<Block> ALL = GasTurbineDefinitions.GRADES.stream()
                .map(GasTurbineDefinitions.Grade::wall).collect(Collectors.toUnmodifiableSet());
    }

    private final FluidizedBedRenderer meshRenderer;
    private final Map<LargeGasTurbineControllerBlockEntity, Motion> motions = new WeakHashMap<>();

    public LargeGasTurbineRenderer(MeshSink sink) {
        meshRenderer = new FluidizedBedRenderer(sink);
    }

    private static ResourceLocation atlas(String palette) {
        return ResourceLocation.tryBuild("ssite", "textures/machine/large_gas_turbine/" + palette + ".png");
    }

    static ResourceLocation texture(String casingMaterial) {
        return TEXTURES.getOrDefault(casingMaterial, TEXTURES.get("StainlessSteel"));
    }

    public static boolean isModelBlock(Block block) {
        return block instanceof LargeGasTurbineControllerBlock
                || block instanceof MultiblockPortBlock && WallBlocks.ALL.contains(block);
    }

    public static boolean usesObj(LargeGasTurbineControllerBlockEntity turbine) {
        return turbine.getLevel() != null && turbine.getLevel().isClientSide
                && !turbine.isRemoved() && turbine.isRotorFormed() && !Assets.MESH.groups().isEmpty();
    }

    public static boolean hidesBlock(BlockEntity entity) {
        if (entity instanceof LargeGasTurbineControllerBlockEntity turbine) return usesObj(turbine);
        // Native binding packets carry the owner even when a wall is shared with other machines.
        return entity instanceof MultiblockPortBlockEntity part
                && part.getLevel() != null && part.getLevel().isClientSide && !part.isRemoved()
                && part.sensorTarget() instanceof LargeGasTurbineControllerBlockEntity turbine
                && usesObj(turbine) && turbine.grade().accepts(part.getBlockState().getBlock())
                && bounds(turbine.getBlockPos(), turbine.front()).contains(part.getBlockPos().getCenter());
    }

    public static void refreshBlocks(LargeGasTurbineControllerBlockEntity turbine) {
        Level level = turbine.getLevel();
        if (level == null || !level.isClientSide) return;
        BlockPos origin = turbine.getBlockPos();
        Direction front = turbine.front();
        refresh(level, origin);
        for (var cell : TurbineStructure.LAYOUT.cells()) {
            refresh(level, AxialStructureTransform.at(origin, front, cell.right(), cell.up(), cell.back()));
        }
    }

    private static void refresh(Level level, BlockPos pos) {
        if (level.hasChunkAt(pos)) {
            var state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
    }

    public static boolean renderReplacement(LargeGasTurbineControllerBlockEntity turbine, float tick, PoseStack pose,
                                            MultiBufferSource buffers, int light) {
        LargeGasTurbineRenderer renderer = NativeRenderer.INSTANCE;
        if (!usesObj(turbine)) {
            renderer.motions.remove(turbine);
            return false;
        }
        renderer.render(turbine, tick, pose, buffers, light);
        return true;
    }

    private void render(LargeGasTurbineControllerBlockEntity turbine, float tick, PoseStack pose,
                        MultiBufferSource buffers, int light) {
        Level level = turbine.getLevel();
        var grade = turbine.grade();
        Direction front = turbine.front();
        Motion motion = motions.computeIfAbsent(turbine, ignored -> new Motion());
        motion.advance(level.getGameTime() + (double) tick, turbine.isRotorActive());
        ResourceLocation atlas = texture(grade.casingMaterial());
        int packedLight = exteriorLight(level, turbine.getBlockPos(), front, light);
        int rotor = GTMaterialRegistry.get(grade.rotor()).getColor();
        // Keep dark metal blades legible behind the intake while retaining the host hue.
        int rotorRgb = ((160 + (rotor >> 16 & 255) * 95 / 255) << 16)
                | ((160 + (rotor >> 8 & 255) * 95 / 255) << 8) | (160 + (rotor & 255) * 95 / 255);
        if (Assets.VBO.draw(buffers, atlas, sink ->
                drawVbo(sink, pose, front, motion.angle(), motion.heat(), packedLight, rotorRgb))) return;
        draw(pose, buffers.getBuffer(RenderType.entityCutout(atlas)), front,
                motion.angle(), motion.heat(), packedLight, rotorRgb);
    }

    void draw(PoseStack pose, VertexConsumer vertices, Direction front, float angle, float heat, int light, int rotorRgb) {
        pose.pushPose();
        transform(pose, front);
        for (var group : Assets.MESH.groups()) {
            Batch batch = batch(group.name());
            if (batch.rotating()) pushRotor(pose, angle);
            meshRenderer.draw(pose, vertices, group, batch.light(light, heat), batch.color(rotorRgb, heat));
            if (batch.rotating()) pose.popPose();
        }
        pose.popPose();
    }

    static void drawVbo(ObjVboMesh.Draw<Batch> sink, PoseStack pose, Direction front, float angle,
                                float heat, int light, int rotorRgb) {
        pose.pushPose();
        try {
            transform(pose, front);
            for (Batch batch : Batch.values()) {
                if (batch.rotating()) pushRotor(pose, angle);
                try {
                    sink.batch(batch, pose, batch.light(light, heat), batch.color(rotorRgb, heat));
                } finally {
                    if (batch.rotating()) pose.popPose();
                }
            }
        } finally {
            pose.popPose();
        }
    }

    private static void pushRotor(PoseStack pose, float angle) {
        pose.pushPose();
        pose.translate(0, PIVOT_Y, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(angle));
        pose.translate(0, -PIVOT_Y, 0);
    }

    static Batch batch(String group) {
        return switch (group) {
            case "combustion_core", "turbine_shell", "turbine_cover" -> Batch.HOT;
            case "turbine_bands", "turbine_cover_bands" -> Batch.HOT_BANDS;
            case "combustors", "combustor_cover" -> Batch.COMBUSTORS;
            case "exhaust_collector", "rear_exhaust" -> Batch.EXHAUST;
            case "rotor" -> Batch.ROTOR;
            case "output_coupling" -> Batch.COUPLING;
            default -> Batch.STATIC;
        };
    }

    enum Batch {
        STATIC(0F), HOT(1F), HOT_BANDS(.8F), COMBUSTORS(.6F), EXHAUST(.25F), ROTOR(0F), COUPLING(0F);

        private final float heatWeight;

        Batch(float heatWeight) { this.heatWeight = heatWeight; }

        boolean rotating() { return this == ROTOR || this == COUPLING; }

        int color(int rotorRgb, float heat) {
            float warmth = heat * heatWeight;
            return this == ROTOR ? rotorRgb : 0xFF0000 | (255 - Math.round(64 * warmth)) << 8
                    | (255 - Math.round(115 * warmth));
        }

        int light(int packedLight, float heat) {
            return LightTexture.pack(Math.max(LightTexture.block(packedLight), Math.round(12 * (heat * heatWeight))),
                    LightTexture.sky(packedLight));
        }
    }

    static void transform(PoseStack pose, Direction front) {
        pose.translate(.5, .5, .5);
        switch (front) {
            case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(90));
            case EAST -> pose.mulPose(Axis.YP.rotationDegrees(-90));
            case UP -> pose.mulPose(Axis.XP.rotationDegrees(90));
            case DOWN -> pose.mulPose(Axis.XP.rotationDegrees(-90));
            case NORTH -> { }
        }
        // Art origin sits at the front face and one and a half blocks below the axis.
        pose.translate(0, -PIVOT_Y, -.5);
    }

    public static AABB bounds(BlockPos pos, Direction front) {
        BlockPos a = pos.relative(right(front), -1).relative(up(front), -1);
        BlockPos b = pos.relative(right(front)).relative(up(front)).relative(front.getOpposite(), 3);
        return new AABB(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()),
                Math.max(a.getX(), b.getX()) + 1, Math.max(a.getY(), b.getY()) + 1, Math.max(a.getZ(), b.getZ()) + 1);
    }

    private static int exteriorLight(Level level, BlockPos pos, Direction front, int fallback) {
        BlockPos middle = pos.relative(front.getOpposite(), 2);
        Direction right = right(front), up = up(front);
        BlockPos[] samples = { pos.relative(front), pos.relative(front.getOpposite(), 4),
                middle.relative(right, 2), middle.relative(right, -2), middle.relative(up, 2), middle.relative(up, -2) };
        int light = fallback;
        for (BlockPos sample : samples) {
            if (!level.hasChunkAt(sample)) continue;
            int outside = LevelRenderer.getLightColor(level, sample);
            light = LightTexture.pack(Math.max(LightTexture.block(light), LightTexture.block(outside)),
                    Math.max(LightTexture.sky(light), LightTexture.sky(outside)));
        }
        return light;
    }

    private static Direction right(Direction front) {
        return front.getAxis().isVertical() ? Direction.EAST : front.getClockWise();
    }

    private static Direction up(Direction front) {
        return switch (front) { case UP -> Direction.SOUTH; case DOWN -> Direction.NORTH; default -> Direction.UP; };
    }

    /** The host supplies an active bit, so speed and warmth are presentation-only ramps. */
    static final class Motion {
        private double last = Double.NaN;
        private double angle, velocity, heat;

        void advance(double now, boolean active) {
            double elapsed = now - last;
            last = now;
            if (!(elapsed > 0)) return;
            double target = active ? 10.8D : 0D;
            if (elapsed > 100D) {
                velocity = target;
                heat = active ? 1D : 0D;
                return;
            }
            double spinTime = active ? 12D : 22D;
            double blend = -Math.expm1(-elapsed / spinTime);
            angle = (angle + target * elapsed + (velocity - target) * spinTime * blend) % 360D;
            velocity += (target - velocity) * blend;
            heat += ((active ? 1D : 0D) - heat) * -Math.expm1(-elapsed / (active ? 60D : 100D));
            if (!active && velocity < .001D) velocity = 0D;
            if (!active && heat < .001D) heat = 0D;
        }

        float angle() { return (float) angle; }
        float heat() { return (float) heat; }
    }
}
