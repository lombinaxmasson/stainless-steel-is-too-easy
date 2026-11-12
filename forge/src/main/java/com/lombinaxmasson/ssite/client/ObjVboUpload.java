package com.lombinaxmasson.ssite.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;

import java.util.List;

final class ObjVboUpload {
    private ObjVboUpload() {}

    static VertexBuffer upload(List<ObjMesh.Group> groups) {
        // The 1.20.1 builder has no close operation for its native allocation.
        var builder = Tesselator.getInstance().getBuilder();
        if (builder.building()) return null;

        var buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        boolean uploaded = false;
        try {
            builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.NEW_ENTITY);
            for (var group : groups) {
                float[] vertices = group.triangles();
                for (int i = 0; i < vertices.length; i += 8) {
                    builder.vertex(vertices[i], vertices[i + 1], vertices[i + 2], 1, 1, 1, 1,
                            vertices[i + 3], vertices[i + 4], OverlayTexture.NO_OVERLAY,
                            LightTexture.FULL_BRIGHT, vertices[i + 5], vertices[i + 6], vertices[i + 7]);
                }
            }
            buffer.bind();
            // VertexBuffer.upload takes ownership of the rendered batch and releases it.
            buffer.upload(builder.end());
            uploaded = true;
            return buffer;
        } finally {
            try {
                if (builder.building()) builder.end().release();
            } finally {
                VertexBuffer.unbind();
                if (!uploaded) buffer.close();
            }
        }
    }
}
