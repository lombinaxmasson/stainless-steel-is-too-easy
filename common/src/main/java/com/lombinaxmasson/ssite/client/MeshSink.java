package com.lombinaxmasson.ssite.client;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Writes one already-transformed vertex. The Blaze3D vertex API differs between
 * 1.20.1 and 1.21.1, so each loader supplies this small piece.
 */
public interface MeshSink {
    void vertex(VertexConsumer buffer, float x, float y, float z, float u, float v,
                float nx, float ny, float nz, int packedLight);
}
