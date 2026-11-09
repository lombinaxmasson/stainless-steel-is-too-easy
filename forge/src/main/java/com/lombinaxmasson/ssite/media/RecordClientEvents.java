package com.lombinaxmasson.ssite.media;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.sound.SoundEngineLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only lifecycle hooks keep audio classes out of dedicated-server initialization. */
@Mod.EventBusSubscriber(modid = "ssite", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class RecordClientEvents {
    private RecordClientEvents() {}

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            RecordNetwork.setClientReceiver(RecordClient::receive);
            MinecraftForge.EVENT_BUS.addListener(RecordClientEvents::tick);
            MinecraftForge.EVENT_BUS.addListener(RecordClientEvents::logout);
            MinecraftForge.EVENT_BUS.addListener(RecordClientEvents::commands);
            MinecraftForge.EVENT_BUS.addListener(RecordClientEvents::tooltip);
        });
    }

    @SubscribeEvent
    public static void soundLoaded(SoundEngineLoadEvent event) {
        Minecraft.getInstance().execute(RecordClient::disconnect);
    }

    private static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) RecordClient.tick();
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
