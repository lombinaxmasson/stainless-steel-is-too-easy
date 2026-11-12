package com.lombinaxmasson.ssite.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.slf4j.Logger;

import java.io.IOException;

@EventBusSubscriber(modid = "ssite", value = Dist.CLIENT)
public final class ObjVboClientEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation SHADER = ResourceLocation.fromNamespaceAndPath("ssite", "obj_entity");

    private ObjVboClientEvents() {}

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        ObjVboMesh.setShader(null);
        if (usesExternalShaderPipeline()) return;
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), SHADER, DefaultVertexFormat.NEW_ENTITY),
                    ObjVboMesh::setShader);
        } catch (IOException failure) {
            LOGGER.warn("[ssite] Could not load the OBJ VBO shader; using buffered rendering", failure);
        }
    }

    private static boolean usesExternalShaderPipeline() {
        ModList mods = ModList.get();
        if (mods.isLoaded("iris") || mods.isLoaded("oculus") || mods.isLoaded("canvas")
                || mods.isLoaded("optifine")) return true;
        try {
            Class.forName("net.optifine.Config", false, ObjVboClientEvents.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        } catch (LinkageError incompatibleOptiFine) {
            return true;
        }
    }
}
