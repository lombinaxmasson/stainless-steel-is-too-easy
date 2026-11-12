package com.lombinaxmasson.ssite.client;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/** Clear rotor interpolation and reload its mesh together with client resource packs. */
@EventBusSubscriber(modid = "ssite", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class WindTurbineClientEvents {
    private WindTurbineClientEvents() {}

    @SubscribeEvent
    public static void reloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) WindTurbineRenderer::reload);
    }
}
