package com.lombinaxmasson.ssite.media;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/** Direction-specific, bounded transport for the shared record protocol. */
public final class RecordNetwork {
    private static final int MAX_PACKET_BYTES = RecordPacket.MAX_PACKET;
    private static Consumer<RecordPacket> clientReceiver;
    private static boolean registered;

    private RecordNetwork() {}

    public static void register(IEventBus modEventBus) {
        if (registered) throw new IllegalStateException("Record network already registered");
        registered = true;
        modEventBus.addListener(RecordNetwork::payloads);
        NeoForge.EVENT_BUS.addListener(RecordNetwork::tick);
        NeoForge.EVENT_BUS.addListener(RecordNetwork::logout);
        NeoForge.EVENT_BUS.addListener(RecordNetwork::stopping);
        NeoForge.EVENT_BUS.addListener(RecordNetwork::commands);
    }

    public static void setClientReceiver(Consumer<RecordPacket> receiver) {
        clientReceiver = Objects.requireNonNull(receiver);
    }

    public static void sendToServer(RecordPacket packet) {
        checkDirection(packet, true);
        PacketDistributor.sendToServer(new ToServer(packet));
    }

    public static void sendToPlayer(ServerPlayer player, RecordPacket packet) {
        checkDirection(packet, false);
        PacketDistributor.sendToPlayer(player, new ToClient(packet));
    }

    private static void payloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(ToServer.TYPE, ToServer.CODEC, (message, context) -> {
            if (!(context.player() instanceof ServerPlayer player))
                throw new IllegalStateException("Missing record upload sender");
            RecordServer.receive(player, message.packet());
        });
        registrar.playToClient(ToClient.TYPE, ToClient.CODEC, (message, context) ->
                Objects.requireNonNull(clientReceiver, "Record client receiver").accept(message.packet()));
    }

    private static void encode(RecordPacket packet, RegistryFriendlyByteBuf buffer) {
        byte[] bytes = packet.encode();
        if (bytes.length > MAX_PACKET_BYTES) throw new IllegalArgumentException("Record packet too large");
        buffer.writeByteArray(bytes);
    }

    private static RecordPacket decode(RegistryFriendlyByteBuf buffer, boolean serverbound) {
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

    private static void tick(ServerTickEvent.Post event) {
        RecordServer.tick(event.getServer());
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

    private record ToServer(RecordPacket packet) implements CustomPacketPayload {
        private static final Type<ToServer> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ssite", "records_up"));
        private static final StreamCodec<RegistryFriendlyByteBuf, ToServer> CODEC = new StreamCodec<>() {
            @Override public ToServer decode(RegistryFriendlyByteBuf buffer) {
                return new ToServer(RecordNetwork.decode(buffer, true));
            }
            @Override public void encode(RegistryFriendlyByteBuf buffer, ToServer message) {
                RecordNetwork.encode(message.packet(), buffer);
            }
        };
        @Override public Type<ToServer> type() { return TYPE; }
    }

    private record ToClient(RecordPacket packet) implements CustomPacketPayload {
        private static final Type<ToClient> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ssite", "records_down"));
        private static final StreamCodec<RegistryFriendlyByteBuf, ToClient> CODEC = new StreamCodec<>() {
            @Override public ToClient decode(RegistryFriendlyByteBuf buffer) {
                return new ToClient(RecordNetwork.decode(buffer, false));
            }
            @Override public void encode(RegistryFriendlyByteBuf buffer, ToClient message) {
                RecordNetwork.encode(message.packet(), buffer);
            }
        };
        @Override public Type<ToClient> type() { return TYPE; }
    }
}
