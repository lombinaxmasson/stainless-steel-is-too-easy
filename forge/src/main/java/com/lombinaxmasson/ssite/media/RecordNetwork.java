package com.lombinaxmasson.ssite.media;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Direction-specific, bounded transport for the shared record protocol. */
public final class RecordNetwork {
    private static final int MAX_PACKET_BYTES = RecordPacket.MAX_PACKET;
    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.tryBuild("ssite", "records"), () -> VERSION,
            VERSION::equals, VERSION::equals);
    private static Consumer<RecordPacket> clientReceiver;
    private static boolean registered;

    private RecordNetwork() {}

    public static void register() {
        if (registered) throw new IllegalStateException("Record network already registered");
        registered = true;
        CHANNEL.messageBuilder(ToServer.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder((message, buffer) -> encode(message.packet(), buffer))
                .decoder(buffer -> new ToServer(decode(buffer, true)))
                .consumerMainThread(RecordNetwork::receiveServer).add();
        CHANNEL.messageBuilder(ToClient.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder((message, buffer) -> encode(message.packet(), buffer))
                .decoder(buffer -> new ToClient(decode(buffer, false)))
                .consumerMainThread((message, context) ->
                        Objects.requireNonNull(clientReceiver, "Record client receiver").accept(message.packet()))
                .add();
        MinecraftForge.EVENT_BUS.addListener(RecordNetwork::tick);
        MinecraftForge.EVENT_BUS.addListener(RecordNetwork::logout);
        MinecraftForge.EVENT_BUS.addListener(RecordNetwork::stopping);
        MinecraftForge.EVENT_BUS.addListener(RecordNetwork::commands);
    }

    public static void setClientReceiver(Consumer<RecordPacket> receiver) {
        clientReceiver = Objects.requireNonNull(receiver);
    }

    public static void sendToServer(RecordPacket packet) {
        checkDirection(packet, true);
        CHANNEL.sendToServer(new ToServer(packet));
    }

    public static void sendToPlayer(ServerPlayer player, RecordPacket packet) {
        checkDirection(packet, false);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ToClient(packet));
    }

    private static void encode(RecordPacket packet, FriendlyByteBuf buffer) {
        byte[] bytes = packet.encode();
        if (bytes.length > MAX_PACKET_BYTES) throw new IllegalArgumentException("Record packet too large");
        buffer.writeByteArray(bytes);
    }

    private static RecordPacket decode(FriendlyByteBuf buffer, boolean serverbound) {
        try {
            RecordPacket packet = RecordPacket.decode(buffer.readByteArray(MAX_PACKET_BYTES));
            if (buffer.isReadable()) throw new DecoderException("Trailing record packet data");
            checkDirection(packet, serverbound);
            return packet;
        } catch (IOException e) {
            throw new DecoderException("Invalid record packet", e);
        }
    }

    private static void checkDirection(RecordPacket packet, boolean serverbound) {
        int kind = packet.kind();
        if (serverbound ? kind < RecordPacket.UPLOAD_BEGIN || kind > RecordPacket.DOWNLOAD_REQUEST
                : kind < RecordPacket.DOWNLOAD_BEGIN || kind > RecordPacket.NOTICE) {
            throw new IllegalArgumentException("Record packet direction mismatch");
        }
    }

    private static void receiveServer(ToServer message, Supplier<NetworkEvent.Context> context) {
        ServerPlayer sender = context.get().getSender();
        if (sender == null) throw new IllegalStateException("Missing record upload sender");
        RecordServer.receive(sender, message.packet());
    }

    private static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) RecordServer.tick(event.getServer());
    }

    private static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) RecordServer.disconnect(player);
    }

    private static void stopping(ServerStoppingEvent event) {
        RecordServer.stop(event.getServer());
    }

    private static void commands(RegisterCommandsEvent event) {
        RecordServer.registerCommands(event.getDispatcher());
    }

    private record ToServer(RecordPacket packet) {}
    private record ToClient(RecordPacket packet) {}
}
