package com.lombinaxmasson.ssite.neoforge.client;

import com.lombinaxmasson.ssite.client.MeshSink;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** 1.21.1 Blaze3D vertex calls. */
public final class NeoForgeMeshSink implements MeshSink {
    @Override
    public void vertex(VertexConsumer buffer, float x, float y, float z, float u, float v,
                       float nx, float ny, float nz, int packedLight) {
        buffer.addVertex(x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(nx, ny, nz);
    }
}
