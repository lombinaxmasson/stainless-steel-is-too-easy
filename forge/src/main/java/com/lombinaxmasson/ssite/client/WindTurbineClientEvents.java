package com.lombinaxmasson.ssite.client;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Clear rotor interpolation and reload its mesh together with client resource packs. */
@Mod.EventBusSubscriber(modid = "ssite", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class WindTurbineClientEvents {
    private WindTurbineClientEvents() {}

    @SubscribeEvent
    public static void reloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) WindTurbineRenderer::reload);
    }
}
