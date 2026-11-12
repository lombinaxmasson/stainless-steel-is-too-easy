package com.lombinaxmasson.ssite.client;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/** One immutable GPU buffer per set of groups that share a pose and draw uniforms. */
final class ObjVboMesh<B extends Enum<B>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<ObjVboMesh<?>> UPLOADS = new HashSet<>();
    private static Shader shader;

    private final Class<B> batchType;
    private final EnumMap<B, List<ObjMesh.Group>> batches;
    private EnumMap<B, VertexBuffer> buffers;
    private boolean uploadFailed;

    ObjVboMesh(Class<B> batchType, ObjMesh mesh, Function<String, B> classify) {
        this.batchType = batchType;
        batches = partition(batchType, mesh.groups(), classify);
    }

    static <B extends Enum<B>> EnumMap<B, List<ObjMesh.Group>> partition(
            Class<B> batchType, List<ObjMesh.Group> groups, Function<String, B> classify) {
        var result = new EnumMap<B, List<ObjMesh.Group>>(batchType);
        for (var group : groups)
            result.computeIfAbsent(classify.apply(group.name()), key -> new ArrayList<>()).add(group);
        return result;
    }

    /** GameRenderer owns the shader. Release our buffers when the shader set is rebuilt. */
    static void setShader(ShaderInstance instance) {
        RenderSystem.assertOnRenderThread();
        for (var mesh : UPLOADS) mesh.release();
        UPLOADS.clear();
        shader = null;
        if (instance == null) return;
        Uniform pose = instance.getUniform("ObjPose");
        Uniform normal = instance.getUniform("ObjNormal");
        Uniform light = instance.getUniform("ObjLight");
        Uniform tint = instance.getUniform("ObjTint");
        if (pose == null || normal == null || light == null || tint == null) {
            LOGGER.warn("[ssite] OBJ shader is missing draw uniforms; using buffered rendering");
            return;
        }
        shader = new Shader(instance, pose, normal, light, tint);
    }

    boolean draw(MultiBufferSource source, ResourceLocation texture, Consumer<Draw<B>> drawBatches) {
        if (!RenderSystem.isOnRenderThread() || shader == null) return false;
        Minecraft client = Minecraft.getInstance();
        // Crumbling, outlines and other render passes may wrap or replace the world buffer.
        if (client.level == null || source != client.renderBuffers().bufferSource() || !ensureUploaded()) return false;

        Shader active = shader;
        ShaderInstance previous = RenderSystem.getShader();
        int texture0 = RenderSystem.getShaderTexture(0);
        int texture1 = RenderSystem.getShaderTexture(1);
        int texture2 = RenderSystem.getShaderTexture(2);
        RenderType type = RenderType.entityCutout(texture);
        type.setupRenderState();
        try {
            drawBatches.accept((batch, pose, light, rgb) -> {
                VertexBuffer buffer = buffers.get(batch);
                if (buffer != null) active.draw(buffer, pose.last(), light, rgb);
            });
        } finally {
            VertexBuffer.unbind();
            type.clearRenderState();
            RenderSystem.setShader(() -> previous);
            RenderSystem.setShaderTexture(0, texture0);
            RenderSystem.setShaderTexture(1, texture1);
            RenderSystem.setShaderTexture(2, texture2);
        }
        return true;
    }

    private boolean ensureUploaded() {
        if (buffers != null) return true;
        if (uploadFailed || batches.isEmpty()) return false;
        UPLOADS.add(this);
        var pending = new EnumMap<B, VertexBuffer>(batchType);
        try {
            for (var entry : batches.entrySet()) {
                VertexBuffer buffer = ObjVboUpload.upload(entry.getValue());
                if (buffer == null) return false;
                pending.put(entry.getKey(), buffer);
            }
            buffers = pending;
            return true;
        } catch (RuntimeException failure) {
            uploadFailed = true;
            LOGGER.warn("[ssite] Could not upload OBJ geometry; using buffered rendering", failure);
            return false;
        } finally {
            if (buffers != pending) pending.values().forEach(VertexBuffer::close);
        }
    }

    private void release() {
        if (buffers != null) buffers.values().forEach(VertexBuffer::close);
        buffers = null;
        uploadFailed = false;
    }

    @FunctionalInterface
    interface Draw<B> {
        void batch(B batch, PoseStack pose, int light, int rgb);
    }

    private record Shader(ShaderInstance instance, Uniform pose, Uniform normal, Uniform light, Uniform tint) {
        void draw(VertexBuffer buffer, PoseStack.Pose transform, int packedLight, int rgb) {
            pose.set(transform.pose());
            normal.set(transform.normal());
            light.set(LightTexture.block(packedLight), LightTexture.sky(packedLight));
            tint.set((rgb >> 16 & 255) / 255F, (rgb >> 8 & 255) / 255F, (rgb & 255) / 255F);
            buffer.bind();
            buffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), instance);
        }
    }
}
