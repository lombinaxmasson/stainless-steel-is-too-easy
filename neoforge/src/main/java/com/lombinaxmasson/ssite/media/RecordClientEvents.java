package com.lombinaxmasson.ssite.media;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Client-only lifecycle hooks keep audio classes out of dedicated-server initialization. */
@EventBusSubscriber(modid = "ssite", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class RecordClientEvents {
    private RecordClientEvents() {}

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            RecordNetwork.setClientReceiver(RecordClient::receive);
            NeoForge.EVENT_BUS.addListener(RecordClientEvents::tick);
            NeoForge.EVENT_BUS.addListener(RecordClientEvents::logout);
            NeoForge.EVENT_BUS.addListener(RecordClientEvents::commands);
            NeoForge.EVENT_BUS.addListener(RecordClientEvents::tooltip);
        });
    }

    @SubscribeEvent
    public static void soundLoaded(SoundEngineLoadEvent event) {
        Minecraft.getInstance().execute(RecordClient::disconnect);
    }

    private static void tick(ClientTickEvent.Post event) {
        RecordClient.tick();
    }

    private static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        RecordClient.disconnect();
    }

    private static void tooltip(ItemTooltipEvent event) {
        RecordTooltips.append(event.getItemStack(), event.getToolTip());
    }

    private static void commands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("ssite-music")
                .executes(context -> {
                    context.getSource().sendSuccess(() ->
                            Component.translatable("message.ssite.music.import_usage"), false);
                    return 1;
                })
                .then(Commands.literal("import")
                        .then(Commands.argument("filename", StringArgumentType.greedyString())
                                .executes(context -> {
                                    RecordClient.importTrack(StringArgumentType.getString(context, "filename"));
                                    return 1;
                                }))));
    }
}
