package com.lombinaxmasson.ssite.media;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** Authoritative world library, bounded transfers and jukebox playback clocks. */
public final class RecordServer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int RANGE = 64;
    private static final byte[] EMPTY = new byte[0];
    private static volatile MinecraftServer current;
    private static volatile RecordLibrary library;
    private static ThreadPoolExecutor io;
    private static boolean failed;
    private static final Map<UUID, Upload> UPLOADS = new HashMap<>();
    private static final Map<UUID, Long> UPLOAD_COOLDOWN = new HashMap<>();
    private static final Map<UUID, ArrayDeque<Download>> DOWNLOADS = new HashMap<>();
    private static final Map<Location, Playing> PLAYING = new HashMap<>();
    private static final Map<UUID, Set<Location>> LISTENERS = new HashMap<>();
    private static final Set<Location> CHANGED = new HashSet<>();

    private record Location(ServerLevel level, BlockPos pos) {}
    private record Playing(RecordTrack track, long startTick) {}
    private static final class Upload {
        final RecordTransfer transfer;
        final long started;
        boolean processing;
        volatile boolean cancelled;
        Upload(RecordTransfer transfer, long started) { this.transfer = transfer; this.started = started; }
    }
    private static final class Download implements AutoCloseable {
        final RecordTrack track;
        final InputStream input;
        int offset;
        Download(RecordTrack track, InputStream input) { this.track = track; this.input = input; }
        @Override public void close() throws IOException { input.close(); }
    }

    private RecordServer() {}

    public static RecordTrack track(String id) {
        RecordLibrary currentLibrary = library;
        return currentLibrary == null ? null : currentLibrary.get(id);
    }
    public static boolean isKnown(String id) { return track(id) != null; }

    public static boolean isPlaying(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return false;
        Playing playing = PLAYING.get(new Location(serverLevel, pos));
        return playing != null && serverLevel.getGameTime() - playing.startTick < playing.track.durationTicks();
    }

    private static boolean ensure(MinecraftServer server) {
        if (current != server) {
            if (current != null) stop(current);
            current = server;
            try {
                library = new RecordLibrary(server.getWorldPath(LevelResource.ROOT).resolve("ssite/music"));
                io = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(3), runnable -> {
                    Thread thread = new Thread(runnable, "ssite-record-library");
                    thread.setDaemon(true);
                    return thread;
                });
            } catch (IOException e) {
                failed = true;
                LOGGER.error("Cannot load SSITE music library", e);
            }
        }
        return !failed && library != null;
    }

    public static void receive(ServerPlayer player, RecordPacket packet) {
        MinecraftServer server = player.getServer();
        if (server == null || !ensure(server)) {
            player.sendSystemMessage(Component.translatable("message.ssite.record.library_error"));
            return;
        }
        try {
            switch (packet.kind()) {
                case RecordPacket.UPLOAD_BEGIN -> beginUpload(player, packet);
                case RecordPacket.UPLOAD_CHUNK -> uploadChunk(player, packet);
                case RecordPacket.DOWNLOAD_REQUEST -> requestDownload(player, packet.id());
                default -> throw new IOException("Wrong music packet direction");
            }
        } catch (IOException | IllegalArgumentException e) {
            if (packet.kind() == RecordPacket.UPLOAD_BEGIN || packet.kind() == RecordPacket.UPLOAD_CHUNK) {
                Upload upload = UPLOADS.remove(player.getUUID());
                if (upload != null) upload.cancelled = true;
            }
            notice(player, packet.id(), packet.kind());
        }
    }

    private static void beginUpload(ServerPlayer player, RecordPacket packet) throws IOException {
        long now = player.getServer().overworld().getGameTime();
        UUID uuid = player.getUUID();
        if (packet.data().length != 0 || packet.offset() != 0 || UPLOADS.containsKey(uuid)
                || UPLOADS.size() >= 4 || now < UPLOAD_COOLDOWN.getOrDefault(uuid, Long.MIN_VALUE)) {
            throw new IOException("Music upload busy");
        }
        RecordTrack track = packet.track();
        UPLOAD_COOLDOWN.put(uuid, now + 200);
        UPLOADS.put(uuid, new Upload(new RecordTransfer(track), now));
    }

    private static void uploadChunk(ServerPlayer player, RecordPacket packet) throws IOException {
        Upload upload = UPLOADS.get(player.getUUID());
        if (upload == null || upload.processing || !upload.transfer.track().id().equals(packet.id())) {
            throw new IOException("Unknown upload");
        }
        upload.transfer.append(packet.offset(), packet.data());
        if (upload.transfer.complete()) {
            upload.processing = true;
            MinecraftServer server = player.getServer();
            RecordLibrary target = library;
            try {
                io.execute(() -> {
                    try {
                        if (upload.cancelled || current != server) return;
                        byte[] data = upload.transfer.finish();
                        RecordTrack track;
                        synchronized (target) {
                            if (upload.cancelled || current != server) return;
                            track = target.add(upload.transfer.track(), data);
                        }
                        server.execute(() -> completeUpload(server, player.getUUID(), upload, track));
                    } catch (IOException | IllegalArgumentException e) {
                        LOGGER.warn("Rejected invalid or unpersistable SSITE music upload: {}", e.getMessage());
                        server.execute(() -> {
                            if (current == server && UPLOADS.remove(player.getUUID(), upload)
                                    && server.getPlayerList().getPlayer(player.getUUID()) != null) {
                                notice(player, upload.transfer.track().id(), RecordPacket.UPLOAD_CHUNK);
                            }
                        });
                    }
                });
            } catch (RejectedExecutionException e) {
                throw new IOException("Music validation queue full", e);
            }
        }
    }

    private static void completeUpload(MinecraftServer server, UUID id, Upload upload, RecordTrack track) {
        if (current != server || !UPLOADS.remove(id, upload)) return;
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if (player == null) return;
        ItemStack held = player.getMainHandItem();
        if (RecordData.isTemplate(held)) {
            RecordData.set(held, track);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }
        player.sendSystemMessage(Component.translatable("message.ssite.record.uploaded", track.title()));
        player.sendSystemMessage(trackLink(track));
    }

    private static boolean canHear(ServerPlayer player, String id) {
        Set<Location> locations = LISTENERS.get(player.getUUID());
        if (locations == null) return false;
        for (Location location : locations) {
            Playing playing = PLAYING.get(location);
            if (playing != null && playing.track.id().equals(id)
                    && location.level == player.serverLevel()
                    && location.pos.distToCenterSqr(player.getX(), player.getY(), player.getZ()) <= RANGE * RANGE
                    && isPlaying(location.level, location.pos)) return true;
        }
        return false;
    }

    private static void requestDownload(ServerPlayer player, String id) throws IOException {
        RecordTrack track = library.get(id);
        if (track == null || !canHear(player, id)) throw new IOException("Track is not playing nearby");
        ArrayDeque<Download> queue = DOWNLOADS.computeIfAbsent(player.getUUID(), ignored -> new ArrayDeque<>());
        if (queue.stream().anyMatch(d -> d.track.id().equals(id))) return;
        if (queue.size() >= 4) throw new IOException("Music download queue full");
        Download download = new Download(track, library.open(id));
        queue.add(download);
        RecordNetwork.sendToPlayer(player, packet(RecordPacket.DOWNLOAD_BEGIN, track, 0, 0, 0, EMPTY));
    }

    public static void tick(MinecraftServer server) {
        if (!ensure(server)) return;
        long now = server.overworld().getGameTime();
        UPLOADS.entrySet().removeIf(entry -> {
            Upload upload = entry.getValue();
            if (now - upload.started <= 1200) return false;
            upload.cancelled = true;
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player != null) notice(player, upload.transfer.track().id(), RecordPacket.UPLOAD_CHUNK);
            return true;
        });
        if (now % 10 == 0) discover(server);
        for (Location location : new ArrayList<>(PLAYING.keySet())) changed(location.level, location.pos);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            updateListener(player, now % 20 == 0);
            sendDownloads(player);
        }
        CHANGED.clear();
    }

    private static void discover(MinecraftServer server) {
        Set<String> visited = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerLevel level = player.serverLevel();
            int cx = player.blockPosition().getX() >> 4, cz = player.blockPosition().getZ() >> 4;
            for (int x = cx - 4; x <= cx + 4; x++) {
                for (int z = cz - 4; z <= cz + 4; z++) {
                    String key = level.dimension().location() + "/" + x + "/" + z;
                    if (!visited.add(key)) continue;
                    var chunk = level.getChunkSource().getChunkNow(x, z);
                    if (chunk == null) continue;
                    for (var entity : new ArrayList<>(chunk.getBlockEntities().values())) {
                        if (entity instanceof JukeboxBlockEntity jukebox) changed(level, jukebox.getBlockPos());
                    }
                }
            }
        }
    }

    public static void changed(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel) || !ensure(serverLevel.getServer())) return;
        Location location = new Location(serverLevel, pos.immutable());
        RecordTrack track = null;
        if (serverLevel.hasChunkAt(pos) && serverLevel.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
            ItemStack stack = jukebox.getItem(0);
            if (RecordData.isRecord(stack)) {
                RecordTrack data = RecordData.get(stack);
                if (data != null) track = library.get(data.id());
            }
        }
        Playing old = PLAYING.get(location);
        if (track == null) {
            if (PLAYING.remove(location) != null) serverLevel.updateNeighborsAt(pos, serverLevel.getBlockState(pos).getBlock());
        } else if (old == null || !old.track.id().equals(track.id())) {
            PLAYING.put(location, new Playing(track, serverLevel.getGameTime()));
            CHANGED.add(location);
            serverLevel.updateNeighborsAt(pos, serverLevel.getBlockState(pos).getBlock());
        } else if (serverLevel.getGameTime() - old.startTick == old.track.durationTicks()) {
            serverLevel.updateNeighborsAt(pos, serverLevel.getBlockState(pos).getBlock());
        }
    }

    private static void updateListener(ServerPlayer player, boolean heartbeat) {
        Set<Location> old = LISTENERS.computeIfAbsent(player.getUUID(), ignored -> new HashSet<>());
        Set<Location> next = new HashSet<>();
        for (var entry : PLAYING.entrySet()) {
            Location location = entry.getKey();
            Playing playing = entry.getValue();
            if (location.level != player.serverLevel() || !isPlaying(location.level, location.pos)
                    || location.pos.distToCenterSqr(player.getX(), player.getY(), player.getZ()) > RANGE * RANGE) continue;
            next.add(location);
        }
        for (Location location : old) {
            if (!next.contains(location)) {
                RecordNetwork.sendToPlayer(player, new RecordPacket(RecordPacket.STOP, "", "", 0, 0, 0,
                        location.pos.asLong(), 0, EMPTY));
            }
        }
        for (Location location : next) {
            if (heartbeat || !old.contains(location) || CHANGED.contains(location)) {
                Playing playing = PLAYING.get(location);
                RecordNetwork.sendToPlayer(player, packet(RecordPacket.PLAY, playing.track, location.pos.asLong(),
                        playing.startTick, 0, EMPTY));
            }
        }
        LISTENERS.put(player.getUUID(), next);
    }

    private static void sendDownloads(ServerPlayer player) {
        ArrayDeque<Download> queue = DOWNLOADS.get(player.getUUID());
        if (queue == null) return;
        for (int i = 0; i < 2 && !queue.isEmpty(); i++) {
            Download download = queue.peek();
            try {
                if (!canHear(player, download.track.id())) throw new IOException("Listener left music range");
                byte[] data = download.input.readNBytes(Math.min(RecordPacket.MAX_CHUNK, download.track.bytes() - download.offset));
                if (data.length == 0) throw new IOException("Truncated music library file");
                RecordNetwork.sendToPlayer(player, packet(RecordPacket.DOWNLOAD_CHUNK, download.track, 0, 0, download.offset, data));
                download.offset += data.length;
                if (download.offset == download.track.bytes()) {
                    queue.remove();
                    close(download);
                }
            } catch (IOException e) {
                queue.remove();
                close(download);
                notice(player, download.track.id(), RecordPacket.DOWNLOAD_REQUEST);
            }
        }
        if (queue.isEmpty()) DOWNLOADS.remove(player.getUUID());
    }

    private static void notice(ServerPlayer player, String id, int operation) {
        // Keep upload and download failures independent even when their content hashes match.
        RecordNetwork.sendToPlayer(player, new RecordPacket(RecordPacket.NOTICE,
                RecordTrack.validId(id) ? id : "", "message.ssite.record.transfer_error", 0, 0, operation, 0, 0, EMPTY));
    }

    private static RecordPacket packet(int kind, RecordTrack track, long pos, long started, int offset, byte[] data) {
        return new RecordPacket(kind, track.id(), track.title(), track.durationTicks(), track.bytes(), offset, pos, started, data);
    }

    public static void disconnect(ServerPlayer player) {
        UUID id = player.getUUID();
        Upload upload = UPLOADS.remove(id);
        if (upload != null) upload.cancelled = true;
        UPLOAD_COOLDOWN.remove(id);
        LISTENERS.remove(id);
        ArrayDeque<Download> downloads = DOWNLOADS.remove(id);
        if (downloads != null) downloads.forEach(RecordServer::close);
    }

    public static void stop(MinecraftServer server) {
        if (current != server) return;
        DOWNLOADS.values().forEach(queue -> queue.forEach(RecordServer::close));
        DOWNLOADS.clear();
        UPLOADS.values().forEach(upload -> upload.cancelled = true);
        UPLOADS.clear();
        if (io != null) io.shutdownNow();
        io = null;
        UPLOAD_COOLDOWN.clear();
        PLAYING.clear();
        LISTENERS.clear();
        CHANGED.clear();
        library = null;
        current = null;
        failed = false;
    }

    private static void close(Download download) {
        try {
            download.close();
        } catch (IOException e) {
            LOGGER.warn("Cannot close SSITE music download", e);
        }
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ssite").then(Commands.literal("music")
                .then(Commands.literal("list").executes(context -> list(context.getSource(), 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1)).executes(context ->
                                list(context.getSource(), IntegerArgumentType.getInteger(context, "page")))))
                .then(Commands.literal("bind").then(Commands.argument("id", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            if (ensure(context.getSource().getServer())) {
                                for (RecordTrack track : library.tracks()) {
                                    if (track.id().startsWith(builder.getRemaining())) {
                                        builder.suggest(track.id(), Component.literal(track.title()));
                                    }
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> bind(context.getSource(), StringArgumentType.getString(context, "id")))))
                .then(Commands.literal("remove").requires(source -> source.hasPermission(2))
                        .then(Commands.argument("id", StringArgumentType.word()).executes(context ->
                                remove(context.getSource(), StringArgumentType.getString(context, "id")))))));
    }

    private static int list(CommandSourceStack source, int page) {
        if (!ensure(source.getServer())) return error(source, "message.ssite.record.library_error");
        var tracks = library.tracks().stream().sorted(Comparator.comparing(RecordTrack::title,
                String.CASE_INSENSITIVE_ORDER).thenComparing(RecordTrack::id)).toList();
        int pages = Math.max(1, (tracks.size() + 19) / 20);
        if (page > pages) {
            source.sendFailure(Component.translatable("message.ssite.record.page_limit", pages));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("message.ssite.record.library_count", tracks.size()), false);
        source.sendSuccess(() -> Component.translatable("message.ssite.record.library_page", page, pages), false);
        for (RecordTrack track : tracks.stream().skip((long) (page - 1) * 20).limit(20).toList()) {
            source.sendSuccess(() -> trackLink(track), false);
        }
        return tracks.size();
    }

    private static Component trackLink(RecordTrack track) {
        return Component.literal(track.title() + " — " + track.id()).withStyle(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ssite music bind " + track.id())));
    }

    private static int bind(CommandSourceStack source, String id) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (!ensure(source.getServer())) return error(source, "message.ssite.record.library_error");
        ServerPlayer player = source.getPlayerOrException();
        RecordTrack track = library.get(id);
        if (track == null) return error(source, "message.ssite.record.unknown_track");
        ItemStack held = player.getMainHandItem();
        if (!RecordData.isTemplate(held)) return error(source, "message.ssite.record.hold_template");
        RecordData.set(held, track);
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        source.sendSuccess(() -> Component.translatable("message.ssite.record.bound", track.title()), false);
        return 1;
    }

    private static int remove(CommandSourceStack source, String id) {
        if (!ensure(source.getServer())) return error(source, "message.ssite.record.library_error");
        try {
            UPLOADS.entrySet().removeIf(entry -> {
                Upload upload = entry.getValue();
                if (!upload.transfer.track().id().equals(id)) return false;
                upload.cancelled = true;
                ServerPlayer player = source.getServer().getPlayerList().getPlayer(entry.getKey());
                if (player != null) notice(player, id, RecordPacket.UPLOAD_CHUNK);
                return true;
            });
            for (var entry : DOWNLOADS.entrySet()) {
                entry.getValue().removeIf(download -> {
                    if (!download.track.id().equals(id)) return false;
                    close(download);
                    ServerPlayer player = source.getServer().getPlayerList().getPlayer(entry.getKey());
                    if (player != null) notice(player, id, RecordPacket.DOWNLOAD_REQUEST);
                    return true;
                });
            }
            if (!library.remove(id)) return error(source, "message.ssite.record.unknown_track");
            source.sendSuccess(() -> Component.translatable("message.ssite.record.removed", id), true);
            return 1;
        } catch (IOException e) {
            LOGGER.error("Cannot remove SSITE music track", e);
            return error(source, "message.ssite.record.library_error");
        }
    }

    private static int error(CommandSourceStack source, String key) {
        source.sendFailure(Component.translatable(key));
        return 0;
    }
}
