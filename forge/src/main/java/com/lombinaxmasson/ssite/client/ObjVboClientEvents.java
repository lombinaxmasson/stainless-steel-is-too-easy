package com.lombinaxmasson.ssite.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = "ssite", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ObjVboClientEvents {
    private ObjVboClientEvents() {}

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        ObjVboMesh.setShader(null);
        if (usesExternalShaderPipeline()) return;
        try {
            var shader = new ShaderInstance(event.getResourceProvider(),
                    ResourceLocation.tryBuild("ssite", "obj_entity"), DefaultVertexFormat.NEW_ENTITY);
            event.registerShader(shader, ObjVboMesh::setShader);
        } catch (IOException error) {
            LogUtils.getLogger().warn("[ssite] OBJ VBO shader unavailable; using buffered rendering", error);
        }
    }

    private static boolean usesExternalShaderPipeline() {
        var mods = ModList.get();
        if (mods.isLoaded("iris") || mods.isLoaded("oculus") || mods.isLoaded("canvas")
                || mods.isLoaded("optifine")) return true;
        try {
            Class.forName("net.optifine.Config", false, ObjVboClientEvents.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException absent) {
            return false;
        } catch (LinkageError present) {
            return true;
        }
    }
}
