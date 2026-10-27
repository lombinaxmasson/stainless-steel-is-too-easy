package com.lombinaxmasson.ssite.forge.client;

import com.lombinaxmasson.ssite.client.MeshSink;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** 1.20.1 Blaze3D vertex calls. */
public final class ForgeMeshSink implements MeshSink {
    @Override
    public void vertex(VertexConsumer buffer, float x, float y, float z, float u, float v,
                       float nx, float ny, float nz, int packedLight) {
        buffer.vertex(x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(nx, ny, nz)
                .endVertex();
    }
}
