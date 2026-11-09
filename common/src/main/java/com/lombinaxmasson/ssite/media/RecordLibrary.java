package com.lombinaxmasson.ssite.media;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/** World-local content-addressed library. A metadata file is the commit marker for each track. */
public final class RecordLibrary {
    public static final long MAX_LIBRARY_BYTES = 512L * 1024 * 1024;
    private final Path directory;
    private final Map<String, RecordTrack> tracks = new ConcurrentHashMap<>();
    private long totalBytes;

    public RecordLibrary(Path directory) throws IOException {
        this.directory = directory.toAbsolutePath().normalize();
        Files.createDirectories(this.directory);
        try (var files = Files.list(this.directory)) {
            for (Path file : files.filter(p -> p.getFileName().toString().endsWith(".properties")).sorted().toList()) {
                String name = file.getFileName().toString();
                String id = name.substring(0, name.length() - ".properties".length());
                if (!RecordTrack.validId(id)) continue;
                Properties metadata = new Properties();
                try (InputStream input = Files.newInputStream(file)) {
                    metadata.load(input);
                } catch (IllegalArgumentException e) {
                    throw new IOException("Invalid music metadata: " + id, e);
                }
                try {
                    RecordTrack track = new RecordTrack(id, metadata.getProperty("title"),
                            Integer.parseInt(metadata.getProperty("durationTicks")),
                            Integer.parseInt(metadata.getProperty("bytes")));
                    Path audio = audioPath(id);
                    if (!Files.isRegularFile(audio) || Files.size(audio) != track.bytes()
                            || totalBytes + track.bytes() > MAX_LIBRARY_BYTES) {
                        throw new IOException("Missing or oversized audio in music library: " + id);
                    }
                    byte[] data = Files.readAllBytes(audio);
                    if (!RecordAudio.id(data).equals(id)
                            || RecordAudio.inspect(data).durationTicks() != track.durationTicks()) {
                        throw new IOException("Damaged audio in music library: " + id);
                    }
                    tracks.put(id, track);
                    totalBytes += track.bytes();
                } catch (IllegalArgumentException e) {
                    throw new IOException("Invalid music metadata: " + id, e);
                }
            }
        }
    }

    public RecordTrack get(String id) { return tracks.get(id); }
    public List<RecordTrack> tracks() { return new ArrayList<>(tracks.values()); }

    public synchronized RecordTrack add(RecordTrack track, byte[] bytes) throws IOException {
        RecordTrack existing = tracks.get(track.id());
        if (existing != null) return existing;
        if (bytes.length != track.bytes() || !RecordAudio.id(bytes).equals(track.id())
                || RecordAudio.inspect(bytes).durationTicks() != track.durationTicks()) {
            throw new IOException("Invalid music upload");
        }
        if (totalBytes + bytes.length > MAX_LIBRARY_BYTES) throw new IOException("Music library is full");
        Path audioTemp = directory.resolve(track.id() + ".ogg.part");
        Path metadataTemp = directory.resolve(track.id() + ".properties.part");
        try {
            Files.write(audioTemp, bytes);
            Properties metadata = new Properties();
            metadata.setProperty("title", track.title());
            metadata.setProperty("durationTicks", Integer.toString(track.durationTicks()));
            metadata.setProperty("bytes", Integer.toString(track.bytes()));
            try (OutputStream output = Files.newOutputStream(metadataTemp)) {
                metadata.store(output, "SSITE music metadata");
            }
            Files.move(audioTemp, audioPath(track.id()), StandardCopyOption.REPLACE_EXISTING);
            Files.move(metadataTemp, directory.resolve(track.id() + ".properties"), StandardCopyOption.REPLACE_EXISTING);
            tracks.put(track.id(), track);
            totalBytes += track.bytes();
            return track;
        } finally {
            Files.deleteIfExists(audioTemp);
            Files.deleteIfExists(metadataTemp);
        }
    }

    public InputStream open(String id) throws IOException {
        if (!tracks.containsKey(id)) throw new IOException("Unknown track");
        return Files.newInputStream(audioPath(id));
    }

    public synchronized boolean remove(String id) throws IOException {
        RecordTrack track = tracks.get(id);
        if (track == null) return false;
        // Remove the commit marker first, so interrupted deletion never republishes a track.
        Files.deleteIfExists(directory.resolve(id + ".properties"));
        tracks.remove(id);
        totalBytes -= track.bytes();
        Files.deleteIfExists(audioPath(id));
        return true;
    }

    private Path audioPath(String id) {
        if (!RecordTrack.validId(id)) throw new IllegalArgumentException("Invalid track hash");
        return directory.resolve(id + ".ogg");
    }
}
