package com.lombinaxmasson.ssite.media;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/** Client-owned file import, bounded content cache and server-clock positional playback. */
public final class RecordClient {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_PLAYBACKS = 16;
    private static final int MAX_DOWNLOADS = 2;
    private static final int TRANSFER_TIMEOUT = 20 * 120;
    private static final long MAX_CACHE_BYTES = 256L * 1024 * 1024;
    private static final int MAX_CACHE_FILES = 64;
    private static final byte[] EMPTY = new byte[0];
    private static final Object CACHE_LOCK = new Object();
    private static final ExecutorService IO = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "ssite-record-audio");
        thread.setDaemon(true);
        return thread;
    });
    private static final Map<Long, Playback> PLAYBACKS = new HashMap<>();
    private static final Map<String, Requested> REQUESTS = new HashMap<>();
    private static final Map<String, Download> DOWNLOADS = new HashMap<>();
    private static final Map<String, Verified> READY = new HashMap<>();
    private static final Map<String, Long> RETRY_AFTER = new HashMap<>();
    private static ClientLevel world;
    private static long generation;
    private static long ticks;
    private static boolean importing;
    private static Upload upload;

    private RecordClient() {}

    /** Called on the client game thread by both loader networking adapters. */
    public static void receive(RecordPacket packet) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.getConnection() == null) return;
        attachWorld(client.level);
        try {
            switch (packet.kind()) {
                case RecordPacket.PLAY -> play(packet);
                case RecordPacket.STOP -> stop(packet.blockPos());
                case RecordPacket.DOWNLOAD_BEGIN -> beginDownload(packet);
                case RecordPacket.DOWNLOAD_CHUNK -> downloadChunk(packet);
                case RecordPacket.NOTICE -> {
                    if (packet.offset() <= RecordPacket.UPLOAD_CHUNK
                            && upload != null && upload.track.id().equals(packet.id())) upload = null;
                    if (packet.offset() == RecordPacket.DOWNLOAD_REQUEST && REQUESTS.containsKey(packet.id())) {
                        failDownload(packet.id());
                    }
                    message(Component.translatable(packet.title()));
                }
                default -> LOGGER.debug("Ignored unexpected client music packet kind {}", packet.kind());
            }
        } catch (IllegalArgumentException | IOException e) {
            failDownload(packet.id());
            LOGGER.warn("Rejected invalid music response: {}", e.getMessage());
            error("message.ssite.record.invalid_download");
        }
    }

    /** Imports only a direct child of config/ssite/music; never accepts an arbitrary path. */
    public static void importTrack(String filename) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.getConnection() == null) {
            error("message.ssite.record.not_connected");
            return;
        }
        attachWorld(client.level);
        if (importing || upload != null) {
            error("message.ssite.record.import_busy");
            return;
        }
        if (filename.isBlank() || filename.indexOf('/') >= 0 || filename.indexOf('\\') >= 0
                || filename.indexOf(':') >= 0 || filename.chars().anyMatch(Character::isISOControl)
                || !filename.toLowerCase(Locale.ROOT).endsWith(".ogg")) {
            error("message.ssite.record.filename");
            return;
        }
        importing = true;
        long token = generation;
        Path directory = client.gameDirectory.toPath().resolve("config/ssite/music");
        message(Component.translatable("message.ssite.record.importing", filename));
        IO.execute(() -> {
            try {
                Files.createDirectories(directory);
                Path base = directory.toRealPath();
                Path file = base.resolve(filename).normalize().toRealPath();
                if (!file.getParent().equals(base) || !Files.isRegularFile(file)) {
                    throw new IOException("Import must be a direct file in the music directory");
                }
                byte[] bytes = readBounded(file);
                RecordAudio.Info info = RecordAudio.inspect(bytes);
                if (info.channels() != 1) throw new IOException("Spatial record audio must be mono");
                String title = filename.substring(0, filename.length() - 4).strip();
                if (title.isEmpty()) title = "Untitled";
                if (title.length() > 96) title = title.substring(0, 96);
                RecordTrack track = new RecordTrack(RecordAudio.id(bytes), title, info.durationTicks(), bytes.length);
                client.execute(() -> {
                    if (token != generation) return;
                    importing = false;
                    upload = new Upload(track, bytes);
                });
            } catch (IOException | IllegalArgumentException e) {
                LOGGER.warn("Cannot import record audio: {}", e.getMessage());
                client.execute(() -> {
                    if (token != generation) return;
                    importing = false;
                    error("message.ssite.record.import_failed");
                });
            }
        });
    }

    /** One bounded upload chunk per client tick; downloading is paced by the server. */
    public static void tick() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.getConnection() == null) {
            if (world != null || importing || upload != null) disconnect();
            return;
        }
        attachWorld(client.level);
        ticks++;
        if (upload != null) {
            if (!upload.begun) {
                RecordNetwork.sendToServer(packet(RecordPacket.UPLOAD_BEGIN, upload.track, 0, EMPTY));
                upload.begun = true;
            } else {
                int end = Math.min(upload.bytes.length, upload.offset + RecordPacket.MAX_CHUNK);
                RecordNetwork.sendToServer(packet(RecordPacket.UPLOAD_CHUNK, upload.track, upload.offset,
                        Arrays.copyOfRange(upload.bytes, upload.offset, end)));
                upload.offset = end;
                if (end == upload.bytes.length) {
                    upload = null;
                    message(Component.translatable("message.ssite.record.sent"));
                }
            }
        }
        List<String> expired = new ArrayList<>();
        REQUESTS.forEach((id, request) -> {
            if (ticks - request.lastActivity > TRANSFER_TIMEOUT) expired.add(id);
        });
        expired.forEach(RecordClient::failDownload);
        Iterator<Map.Entry<Long, Playback>> entries = PLAYBACKS.entrySet().iterator();
        while (entries.hasNext()) {
            Playback playback = entries.next().getValue();
            long elapsed = client.level.getGameTime() - playback.startTick;
            BlockPos pos = BlockPos.of(playback.position);
            if (ticks - playback.lastSeen > 80 || elapsed >= playback.track.durationTicks()
                    || client.player == null || client.player.distanceToSqr(pos.getX() + 0.5,
                    pos.getY() + 0.5, pos.getZ() + 0.5) > 80.0 * 80.0) {
                stopSound(playback);
                entries.remove();
                continue;
            }
            if (playback.sound != null) playback.sound.updateClock(elapsed);
            else if (!playback.failed) ensureTrack(playback.track);
            startIfReady(playback);
        }
        RETRY_AFTER.entrySet().removeIf(entry -> entry.getValue() < ticks);
    }

    /** Also used on sound reload: playback state is discarded, verified files remain on disk. */
    public static void disconnect() {
        generation++;
        PLAYBACKS.values().forEach(RecordClient::stopSound);
        PLAYBACKS.clear();
        REQUESTS.clear();
        DOWNLOADS.clear();
        READY.clear();
        RETRY_AFTER.clear();
        upload = null;
        importing = false;
        world = null;
        ticks = 0;
    }

    static void playbackFailed(RecordSound sound) {
        for (Playback playback : PLAYBACKS.values()) {
            if (playback.sound == sound) {
                stopSound(playback);
                playback.failed = true;
                error("message.ssite.record.decode_failed");
            }
        }
    }

    private static void attachWorld(ClientLevel level) {
        if (world != level) {
            disconnect();
            world = level;
        }
    }

    private static void play(RecordPacket packet) {
        RecordTrack track = packet.track();
        Playback current = PLAYBACKS.get(packet.blockPos());
        if (current != null && current.track.equals(track) && current.startTick == packet.startTick()) {
            current.lastSeen = ticks;
            return;
        }
        stop(packet.blockPos());
        if (PLAYBACKS.size() >= MAX_PLAYBACKS) return;
        Playback playback = new Playback(track, packet.blockPos(), packet.startTick(), ticks);
        PLAYBACKS.put(packet.blockPos(), playback);
        ensureTrack(track);
        startIfReady(playback);
    }

    private static void stop(long position) {
        Playback previous = PLAYBACKS.remove(position);
        if (previous != null) stopSound(previous);
    }

    private static void stopSound(Playback playback) {
        if (playback.sound != null) {
            playback.sound.finish();
            Minecraft.getInstance().getSoundManager().stop(playback.sound);
            playback.sound = null;
        }
    }

    private static void startIfReady(Playback playback) {
        if (playback.sound != null || playback.failed || world == null) return;
        Verified verified = READY.get(playback.track.id());
        if (verified == null || verified.duration != playback.track.durationTicks()
                || verified.bytes != playback.track.bytes()) return;
        if (!Files.isRegularFile(verified.file)) {
            READY.remove(playback.track.id());
            ensureTrack(playback.track);
            return;
        }
        long elapsed = world.getGameTime() - playback.startTick;
        if (elapsed >= playback.track.durationTicks()) return;
        playback.sound = new RecordSound(playback.track.id(), verified.file, BlockPos.of(playback.position),
                Math.max(0, elapsed), IO);
        Minecraft.getInstance().getSoundManager().play(playback.sound);
    }

    private static void ensureTrack(RecordTrack track) {
        if (READY.containsKey(track.id()) || REQUESTS.containsKey(track.id()) || REQUESTS.size() >= MAX_DOWNLOADS
                || RETRY_AFTER.getOrDefault(track.id(), 0L) > ticks) return;
        Requested request = new Requested(track, ticks);
        REQUESTS.put(track.id(), request);
        long token = generation;
        Path file = cacheDirectory().resolve(track.id() + ".ogg");
        IO.execute(() -> {
            boolean valid = false;
            try {
                if (Files.isRegularFile(file)) {
                    verify(track, readBounded(file));
                    valid = true;
                    Files.setLastModifiedTime(file, java.nio.file.attribute.FileTime.fromMillis(System.currentTimeMillis()));
                }
            } catch (IOException | IllegalArgumentException e) {
                LOGGER.debug("Discarded damaged cached record {}: {}", track.id(), e.getMessage());
            }
            boolean cached = valid;
            Minecraft.getInstance().execute(() -> {
                if (token != generation || REQUESTS.get(track.id()) != request) return;
                if (cached) {
                    REQUESTS.remove(track.id());
                    READY.put(track.id(), new Verified(file, track.durationTicks(), track.bytes()));
                } else {
                    RecordNetwork.sendToServer(packet(RecordPacket.DOWNLOAD_REQUEST, track, 0, EMPTY));
                    message(Component.translatable("message.ssite.record.downloading", track.title()));
                }
            });
        });
    }

    private static void beginDownload(RecordPacket packet) throws IOException {
        Requested request = REQUESTS.get(packet.id());
        if (request == null || !request.track.equals(packet.track()) || DOWNLOADS.containsKey(packet.id())
                || DOWNLOADS.size() >= MAX_DOWNLOADS || packet.offset() != 0 || packet.data().length != 0) {
            throw new IOException("Unexpected music download header");
        }
        DOWNLOADS.put(packet.id(), new Download(request.track));
        request.lastActivity = ticks;
    }

    private static void downloadChunk(RecordPacket packet) throws IOException {
        Download download = DOWNLOADS.get(packet.id());
        Requested request = REQUESTS.get(packet.id());
        if (download == null || request == null || !request.track.equals(packet.track())
                || packet.offset() != download.offset || packet.data().length == 0
                || packet.data().length > download.bytes.length - download.offset) {
            throw new IOException("Discontinuous music download");
        }
        System.arraycopy(packet.data(), 0, download.bytes, download.offset, packet.data().length);
        download.offset += packet.data().length;
        request.lastActivity = ticks;
        if (download.offset != download.bytes.length) return;
        DOWNLOADS.remove(packet.id());
        long token = generation;
        Path file = cacheDirectory().resolve(packet.id() + ".ogg");
        IO.execute(() -> {
            try {
                verify(request.track, download.bytes);
                synchronized (CACHE_LOCK) {
                    Files.createDirectories(file.getParent());
                    writeCache(file, download.bytes);
                    trimCache(file.getParent());
                }
                Minecraft.getInstance().execute(() -> {
                    if (token != generation || REQUESTS.get(packet.id()) != request) return;
                    REQUESTS.remove(packet.id());
                    READY.put(packet.id(), new Verified(file, request.track.durationTicks(), request.track.bytes()));
                });
            } catch (IOException | IllegalArgumentException e) {
                LOGGER.warn("Cannot cache downloaded record {}: {}", packet.id(), e.getMessage());
                Minecraft.getInstance().execute(() -> {
                    if (token != generation) return;
                    failDownload(packet.id());
                    error("message.ssite.record.invalid_download");
                });
            }
        });
    }

    private static void verify(RecordTrack track, byte[] bytes) throws IOException {
        if (bytes.length != track.bytes() || !RecordAudio.id(bytes).equals(track.id())) {
            throw new IOException("Music content hash mismatch");
        }
        RecordAudio.Info info = RecordAudio.inspect(bytes);
        if (info.channels() != 1 || info.durationTicks() != track.durationTicks()) {
            throw new IOException("Music metadata mismatch");
        }
    }

    private static void writeCache(Path file, byte[] bytes) throws IOException {
        Path temporary = file.resolveSibling(file.getFileName() + ".part");
        try {
            Files.write(temporary, bytes);
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException cleanupFailure) {
                e.addSuppressed(cleanupFailure);
            }
            throw e;
        }
    }

    private static byte[] readBounded(Path file) throws IOException {
        long size = Files.size(file);
        if (size < 1 || size > RecordAudio.MAX_BYTES) throw new IOException("Music file exceeds 16 MiB");
        try (InputStream input = Files.newInputStream(file)) {
            byte[] bytes = input.readNBytes(RecordAudio.MAX_BYTES + 1);
            if (bytes.length != size) throw new IOException("Music file changed while reading");
            return bytes;
        }
    }

    private static Path cacheDirectory() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config/ssite/music-cache");
    }

    private static void trimCache(Path directory) throws IOException {
        List<Path> files;
        try (Stream<Path> listing = Files.list(directory)) {
            files = new ArrayList<>(listing.filter(path -> path.getFileName().toString().matches("[a-f0-9]{64}\\.ogg"))
                    .filter(Files::isRegularFile).toList());
        }
        files.sort(Comparator.comparingLong(RecordClient::modifiedTime));
        long total = 0;
        for (Path file : files) total += Files.size(file);
        int count = files.size();
        for (Path file : files) {
            if (total <= MAX_CACHE_BYTES && count <= MAX_CACHE_FILES) break;
            long size = Files.size(file);
            try {
                if (Files.deleteIfExists(file)) {
                    total -= size;
                    count--;
                }
            } catch (IOException e) {
                // Windows keeps active decoder files locked; retry eviction after another download.
                LOGGER.debug("Cannot evict cached record {}: {}", file.getFileName(), e.getMessage());
            }
        }
    }

    private static long modifiedTime(Path file) {
        try {
            return Files.getLastModifiedTime(file).toMillis();
        } catch (IOException e) {
            return 0;
        }
    }

    private static RecordPacket packet(int kind, RecordTrack track, int offset, byte[] bytes) {
        return new RecordPacket(kind, track.id(), track.title(), track.durationTicks(), track.bytes(),
                offset, 0, 0, bytes);
    }

    private static void failDownload(String id) {
        REQUESTS.remove(id);
        DOWNLOADS.remove(id);
        if (RecordTrack.validId(id)) RETRY_AFTER.put(id, ticks + 200);
    }

    private static void error(String key) {
        message(Component.translatable(key));
    }

    private static void message(Component text) {
        if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.displayClientMessage(text, false);
    }

    private record Verified(Path file, int duration, int bytes) {}

    private static final class Requested {
        private final RecordTrack track;
        private long lastActivity;

        private Requested(RecordTrack track, long tick) {
            this.track = track;
            this.lastActivity = tick;
        }
    }

    private static final class Download {
        private final byte[] bytes;
        private int offset;

        private Download(RecordTrack track) {
            bytes = new byte[track.bytes()];
        }
    }

    private static final class Upload {
        private final RecordTrack track;
        private final byte[] bytes;
        private int offset;
        private boolean begun;

        private Upload(RecordTrack track, byte[] bytes) {
            this.track = track;
            this.bytes = bytes;
        }
    }

    private static final class Playback {
        private final RecordTrack track;
        private final long position;
        private final long startTick;
        private long lastSeen;
        private RecordSound sound;
        private boolean failed;

        private Playback(RecordTrack track, long position, long startTick, long tick) {
            this.track = track;
            this.position = position;
            this.startTick = startTick;
            this.lastSeen = tick;
        }
    }
}
