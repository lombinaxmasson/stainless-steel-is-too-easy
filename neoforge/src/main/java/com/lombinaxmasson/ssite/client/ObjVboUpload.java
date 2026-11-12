package com.lombinaxmasson.ssite.client;

import com.lombinaxmasson.ssite.neoforge.client.NeoForgeMeshSink;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.LightTexture;

import java.util.List;

/** Allocates a static GPU buffer for OBJ geometry on the render thread. */
final class ObjVboUpload {
    private static final int FLOATS_PER_VERTEX = 8;
    private static final NeoForgeMeshSink VERTICES = new NeoForgeMeshSink();

    private ObjVboUpload() {}

    static VertexBuffer upload(List<ObjMesh.Group> groups) {
        int vertexCount = groups.stream().mapToInt(group -> group.triangles().length / FLOATS_PER_VERTEX).sum();
        int capacity = Math.multiplyExact(vertexCount, DefaultVertexFormat.NEW_ENTITY.getVertexSize());
        VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        try (ByteBufferBuilder allocator = new ByteBufferBuilder(Math.max(1, capacity))) {
            BufferBuilder builder = new BufferBuilder(allocator, VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.NEW_ENTITY);
            for (ObjMesh.Group group : groups) {
                float[] vertices = group.triangles();
                for (int i = 0; i < vertices.length; i += FLOATS_PER_VERTEX) {
                    VERTICES.vertex(builder, vertices[i], vertices[i + 1], vertices[i + 2],
                            vertices[i + 3], vertices[i + 4], vertices[i + 5], vertices[i + 6], vertices[i + 7],
                            LightTexture.FULL_BRIGHT);
                }
            }
            buffer.bind();
            try {
                // VertexBuffer.upload consumes and closes the MeshData returned by buildOrThrow.
                buffer.upload(builder.buildOrThrow());
            } finally {
                VertexBuffer.unbind();
            }
            return buffer;
        } catch (RuntimeException | Error failure) {
            buffer.close();
            throw failure;
        }
    }
}
