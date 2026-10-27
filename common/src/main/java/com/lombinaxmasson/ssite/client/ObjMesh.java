package com.lombinaxmasson.ssite.client;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Triangle soup per named Wavefront OBJ object, already in block-local units.
 * The OBJ exporter stores texture V up from the bottom, so V is flipped for Minecraft.
 */
public final class ObjMesh {
    public record Group(String name, float[] triangles) {}

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, ObjMesh> CACHE = new LinkedHashMap<>();

    private final List<Group> groups;

    private ObjMesh(List<Group> groups) {
        this.groups = groups;
    }

    public List<Group> groups() {
        return groups;
    }

    public static ObjMesh load(String resourcePath) {
        ObjMesh cached = CACHE.get(resourcePath);
        if (cached != null) return cached;
        ObjMesh mesh = parse(resourcePath);
        CACHE.put(resourcePath, mesh);
        return mesh;
    }

    private static ObjMesh parse(String resourcePath) {
        InputStream stream = ObjMesh.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            LOGGER.warn("[ssite] Missing machine model {}", resourcePath);
            return new ObjMesh(List.of());
        }
        List<float[]> positions = new ArrayList<>();
        List<float[]> textures = new ArrayList<>();
        List<float[]> normals = new ArrayList<>();
        Map<String, Floats> groups = new LinkedHashMap<>();
        String current = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.charAt(0) == '#') continue;
                String[] parts = line.split("\\s+");
                switch (parts[0]) {
                    case "v" -> positions.add(vector(parts));
                    case "vt" -> textures.add(vector(parts));
                    case "vn" -> normals.add(vector(parts));
                    case "o", "g" -> {
                        if (parts.length > 1) current = parts[1];
                    }
                    case "f" -> {
                        if (current == null || parts.length < 4) continue;
                        Floats out = groups.computeIfAbsent(current, ignored -> new Floats());
                        int corners = parts.length - 1;
                        int[] vertex = new int[corners];
                        int[] texture = new int[corners];
                        int[] normal = new int[corners];
                        for (int i = 0; i < corners; i++) {
                            String[] corner = parts[i + 1].split("/");
                            vertex[i] = index(corner[0], positions.size());
                            texture[i] = corner.length > 1 && !corner[1].isEmpty()
                                    ? index(corner[1], textures.size()) : -1;
                            normal[i] = corner.length > 2 && !corner[2].isEmpty()
                                    ? index(corner[2], normals.size()) : -1;
                        }
                        for (int i = 1; i + 1 < corners; i++) {
                            append(out, 0, positions, textures, normals, vertex, texture, normal);
                            append(out, i, positions, textures, normals, vertex, texture, normal);
                            append(out, i + 1, positions, textures, normals, vertex, texture, normal);
                        }
                    }
                    default -> {
                        // usemtl, mtllib, s and comments are not needed: one atlas covers the model.
                    }
                }
            }
        } catch (IOException | RuntimeException error) {
            LOGGER.warn("[ssite] Could not parse machine model {}", resourcePath, error);
            return new ObjMesh(List.of());
        }
        List<Group> parsed = new ArrayList<>(groups.size());
        groups.forEach((name, floats) -> parsed.add(new Group(name, floats.toArray())));
        return new ObjMesh(List.copyOf(parsed));
    }

    private static float[] vector(String[] parts) {
        float[] value = new float[Math.max(2, parts.length - 1)];
        for (int i = 1; i < parts.length; i++) value[i - 1] = Float.parseFloat(parts[i]);
        return value;
    }

    private static int index(String text, int size) {
        int value = Integer.parseInt(text);
        return value < 0 ? size + value : value - 1;
    }

    private static void append(Floats out, int corner, List<float[]> positions, List<float[]> textures,
                               List<float[]> normals, int[] vertex, int[] texture, int[] normal) {
        float[] position = positions.get(vertex[corner]);
        out.add(position[0], position[1], position[2]);
        if (texture[corner] >= 0 && texture[corner] < textures.size()) {
            float[] uv = textures.get(texture[corner]);
            out.add(uv[0], 1.0F - uv[1]);
        } else {
            out.add(0.0F, 0.0F);
        }
        if (normal[corner] >= 0 && normal[corner] < normals.size()) {
            float[] facing = normals.get(normal[corner]);
            out.add(facing[0], facing[1], facing[2]);
        } else {
            out.add(0.0F, 1.0F, 0.0F);
        }
    }

    /** Growable float array, one triangle corner is x,y,z,u,v,nx,ny,nz. */
    private static final class Floats {
        private float[] values = new float[256];
        private int size;

        void add(float value) {
            if (size == values.length) {
                float[] grown = new float[values.length * 2];
                System.arraycopy(values, 0, grown, 0, size);
                values = grown;
            }
            values[size++] = value;
        }

        void add(float a, float b) {
            add(a);
            add(b);
        }

        void add(float a, float b, float c) {
            add(a);
            add(b);
            add(c);
        }

        float[] toArray() {
            float[] result = new float[size];
            System.arraycopy(values, 0, result, 0, size);
            return result;
        }
    }
}
