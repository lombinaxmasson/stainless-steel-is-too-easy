package com.lombinaxmasson.ssite.media;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Pure JVM wire, transfer and persistence checks. Synthetic OGG pages test structure, not decoding. */
public final class RecordMediaTests {
    private static final int SERIAL = 0x13572468;

    private RecordMediaTests() {}

    public static void main(String[] args) throws Exception {
        trackLimitsRejectInvalidMetadata();
        structuralAudioValidationChecksLimits();
        audioValidationRejectsDamageAndDiscontinuity();
        audioContinuationIsTrackedAcrossPages();
        orderedTransferChecksOffsetsAndIntegrity();
        packetRoundTripsAndBoundsAreEnforced();
        libraryPersistsCanonicalTracksAndDeletion();
        libraryIgnoresUncommittedFiles();
        libraryRejectsCommittedDamage();
        System.out.println("RecordMediaTests: all 9 checks passed (synthetic structure; no audio decoding).");
    }

    private static void trackLimitsRejectInvalidMetadata() throws Exception {
        String id = "a".repeat(64);
        check(RecordTrack.validId(id) && !RecordTrack.validId(id.toUpperCase())
                        && !RecordTrack.validId("../music") && !RecordTrack.validId(null),
                "track ids are lower-case SHA-256 hashes");
        rejects(IllegalArgumentException.class, () -> new RecordTrack(id, "", 1, 1));
        rejects(IllegalArgumentException.class, () -> new RecordTrack(id, "a\nb", 1, 1));
        rejects(IllegalArgumentException.class, () -> new RecordTrack(id, "x".repeat(97), 1, 1));
        rejects(IllegalArgumentException.class, () -> new RecordTrack(id, "Title", 0, 1));
        rejects(IllegalArgumentException.class, () -> new RecordTrack(id, "Title", RecordAudio.MAX_TICKS + 1, 1));
        rejects(IllegalArgumentException.class, () -> new RecordTrack(id, "Title", 1, 0));
        rejects(IllegalArgumentException.class, () -> new RecordTrack(id, "Title", 1, RecordAudio.MAX_BYTES + 1));
        new RecordTrack(id, "界".repeat(96), RecordAudio.MAX_TICKS, RecordAudio.MAX_BYTES);
    }

    private static void structuralAudioValidationChecksLimits() throws Exception {
        RecordAudio.Info info = RecordAudio.inspect(audio(8000, 1, 8001));
        check(info.channels() == 1 && info.sampleRate() == 8000 && info.durationTicks() == 21,
                "duration rounds up to a full game tick");
        check(RecordAudio.inspect(audio(8000, 1, 4_800_000)).durationTicks() == RecordAudio.MAX_TICKS,
                "exactly ten minutes is within the limit");
        rejects(IOException.class, () -> RecordAudio.inspect(audio(8000, 1, 4_800_001)));
        rejects(IOException.class, () -> RecordAudio.inspect(audio(8000, 2, 8000)));
        rejects(IOException.class, () -> RecordAudio.inspect(audio(7999, 1, 8000)));
        rejects(IOException.class, () -> RecordAudio.inspect(audio(96001, 1, 8000)));
        rejects(IOException.class, () -> RecordAudio.inspect(new byte[57]));
        rejects(IOException.class, () -> RecordAudio.inspect(new byte[RecordAudio.MAX_BYTES + 1]));
    }

    private static void audioValidationRejectsDamageAndDiscontinuity() throws Exception {
        byte[] valid = audio(8000, 1, 8000);
        byte[] corrupted = valid.clone();
        corrupted[corrupted.length - 1] ^= 1;
        rejects(IOException.class, () -> RecordAudio.inspect(corrupted));
        rejects(IOException.class, () -> RecordAudio.inspect(Arrays.copyOf(valid, valid.length - 1)));
        rejects(IOException.class, () -> RecordAudio.inspect(join(valid, valid)));
        rejects(IOException.class, () -> RecordAudio.inspect(join(
                page(2, 0, SERIAL, 0, identification(8000, 1)),
                page(0, 0, SERIAL, 2, header(3)), page(0, 0, SERIAL, 3, header(5)),
                page(4, 8000, SERIAL, 4, new byte[]{0}))));
        rejects(IOException.class, () -> RecordAudio.inspect(join(
                page(2, 0, SERIAL, 0, identification(8000, 1)),
                page(0, 0, SERIAL + 1, 1, header(3)), page(0, 0, SERIAL, 2, header(5)),
                page(4, 8000, SERIAL, 3, new byte[]{0}))));
        rejects(IOException.class, () -> RecordAudio.inspect(join(
                page(2, 0, SERIAL, 0, identification(8000, 1)),
                page(0, 9000, SERIAL, 1, header(3)), page(0, 0, SERIAL, 2, header(5)),
                page(4, 8000, SERIAL, 3, new byte[]{0}))));
        rejects(IOException.class, () -> RecordAudio.inspect(join(
                page(2, 0, SERIAL, 0, identification(8000, 1)),
                page(0, 0, SERIAL, 1, header(3)), page(0, 0, SERIAL, 2, header(5)),
                page(0, 8000, SERIAL, 3, new byte[]{0}))));
        rejects(IOException.class, () -> RecordAudio.inspect(join(
                page(2, 0, SERIAL, 0, identification(8000, 1)),
                page(0, 0, SERIAL, 1, header(3)), page(0, 0, SERIAL, 2, header(5)),
                page(0, 8000, SERIAL, 3, new byte[]{0}), page(4, -1, SERIAL, 4, new byte[]{0}))));
    }

    private static void audioContinuationIsTrackedAcrossPages() throws Exception {
        byte[] comment = Arrays.copyOf(header(3), 300);
        byte[] begin = page(2, 0, SERIAL, 0, identification(8000, 1));
        byte[] partial = page(0, -1, SERIAL, 1, new byte[]{(byte) 255}, Arrays.copyOf(comment, 255));
        byte[] continued = page(1, 0, SERIAL, 2, new byte[]{45}, Arrays.copyOfRange(comment, 255, 300));
        byte[] setup = page(0, 0, SERIAL, 3, header(5));
        byte[] end = page(4, 8000, SERIAL, 4, new byte[]{0});
        check(RecordAudio.inspect(join(begin, partial, continued, setup, end)).durationTicks() == 20,
                "continued header packets retain their complete identity");
        byte[] missingContinuationFlag = page(0, 0, SERIAL, 2,
                new byte[]{45}, Arrays.copyOfRange(comment, 255, 300));
        rejects(IOException.class, () -> RecordAudio.inspect(join(begin, partial, missingContinuationFlag, setup, end)));
    }

    private static void orderedTransferChecksOffsetsAndIntegrity() throws Exception {
        byte[] audio = audio(8000, 1, 8000);
        RecordTrack track = track("Imported", audio);
        RecordTransfer transfer = new RecordTransfer(track);
        rejects(IOException.class, transfer::finish);
        rejects(IOException.class, () -> transfer.append(1, new byte[]{0}));
        rejects(IOException.class, () -> transfer.append(0, new byte[0]));
        rejects(IOException.class, () -> transfer.append(0, new byte[RecordPacket.MAX_CHUNK + 1]));
        byte[] firstFragment = Arrays.copyOf(audio, 32);
        transfer.append(0, firstFragment);
        firstFragment[0] ^= 1;
        check(transfer.offset() == 32 && !transfer.complete(), "partial uploads remain incomplete");
        rejects(IOException.class, () -> transfer.append(0, Arrays.copyOf(audio, 32)));
        rejects(IOException.class, () -> transfer.append(32, new byte[audio.length]));
        transfer.append(32, Arrays.copyOfRange(audio, 32, audio.length));
        check(transfer.complete() && Arrays.equals(transfer.finish(), audio), "ordered fragments publish exact bytes");
        rejects(IOException.class, () -> transfer.append(audio.length, new byte[]{0}));
        RecordTransfer badHash = new RecordTransfer(new RecordTrack("b".repeat(64), "Bad hash", 20, audio.length));
        badHash.append(0, audio);
        rejects(IOException.class, badHash::finish);
        RecordTransfer badDuration = new RecordTransfer(new RecordTrack(track.id(), "Bad length", 21, audio.length));
        badDuration.append(0, audio);
        rejects(IOException.class, badDuration::finish);
    }

    private static void packetRoundTripsAndBoundsAreEnforced() throws Exception {
        byte[] data = new byte[RecordPacket.MAX_CHUNK];
        data[0] = 42;
        RecordPacket original = new RecordPacket(RecordPacket.UPLOAD_CHUNK, "a".repeat(64), "界".repeat(96),
                20, RecordAudio.MAX_BYTES, 1024, -1234, 4321, data);
        data[0] = 0;
        RecordPacket decoded = RecordPacket.decode(original.encode());
        decoded.data()[0] = 0;
        check(decoded.kind() == original.kind() && decoded.id().equals(original.id())
                        && decoded.title().equals(original.title()) && decoded.offset() == 1024
                        && decoded.blockPos() == -1234 && decoded.startTick() == 4321
                        && decoded.data()[0] == 42 && decoded.data().length == RecordPacket.MAX_CHUNK,
                "the bounded wire format retains metadata and copied payload bytes");
        byte[] encoded = original.encode();
        rejects(IOException.class, () -> RecordPacket.decode(Arrays.copyOf(encoded, encoded.length - 1)));
        rejects(IOException.class, () -> RecordPacket.decode(Arrays.copyOf(encoded, encoded.length + 1)));
        rejects(IOException.class, () -> RecordPacket.decode(new byte[RecordPacket.MAX_PACKET + 1]));
        byte[] invalidKind = encoded.clone();
        invalidKind[0] = (byte) 255;
        rejects(IOException.class, () -> RecordPacket.decode(invalidKind));
        ByteArrayOutputStream malformed = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(malformed)) {
            out.writeByte(RecordPacket.UPLOAD_CHUNK);
            out.writeUTF(""); out.writeUTF("");
            out.writeInt(0); out.writeInt(0); out.writeInt(0);
            out.writeLong(0); out.writeLong(0);
            out.writeInt(-1);
        }
        rejects(IOException.class, () -> RecordPacket.decode(malformed.toByteArray()));
    }

    private static void libraryPersistsCanonicalTracksAndDeletion() throws Exception {
        withDirectory(directory -> {
            byte[] audio = audio(8000, 1, 8000);
            RecordTrack track = track("Saved 音乐", audio);
            RecordLibrary library = new RecordLibrary(directory);
            check(library.add(track, audio).equals(track), "new audio is committed");
            RecordTrack renamed = new RecordTrack(track.id(), "Another title", track.durationTicks(), track.bytes());
            check(library.add(renamed, audio).equals(track), "the existing hash retains authoritative metadata");
            RecordLibrary reopened = new RecordLibrary(directory);
            check(reopened.tracks().equals(List.of(track)), "a reopened world library retains track metadata");
            try (var input = reopened.open(track.id())) {
                check(Arrays.equals(input.readAllBytes(), audio), "a reopened world library retains exact audio");
            }
            reopened.tracks().clear();
            check(reopened.get(track.id()).equals(track), "callers cannot mutate the library through its track list");
            check(reopened.remove(track.id()) && !reopened.remove(track.id()), "deletion is idempotent");
            check(!Files.exists(directory.resolve(track.id() + ".properties"))
                            && !Files.exists(directory.resolve(track.id() + ".ogg")),
                    "deletion removes both committed files");
            check(new RecordLibrary(directory).tracks().isEmpty(), "deleted tracks stay deleted after restart");
            rejects(IOException.class, () -> reopened.open(track.id()));
        });
    }

    private static void libraryIgnoresUncommittedFiles() throws Exception {
        withDirectory(directory -> {
            byte[] audio = audio(8000, 1, 8000);
            String id = RecordAudio.id(audio);
            Files.write(directory.resolve(id + ".ogg"), audio);
            Files.writeString(directory.resolve(id + ".properties.part"), "interrupted metadata");
            Files.write(directory.resolve("b".repeat(64) + ".ogg.part"), new byte[]{1, 2});
            Files.writeString(directory.resolve("unrelated.properties"), "other data");
            check(new RecordLibrary(directory).tracks().isEmpty(),
                    "orphan audio and partial writes are never published as imported tracks");
            RecordTrack track = track("Recovered", audio);
            RecordLibrary library = new RecordLibrary(directory);
            library.add(track, audio);
            check(new RecordLibrary(directory).get(id).equals(track), "a valid import can replace an orphan audio file");
        });
    }

    private static void libraryRejectsCommittedDamage() throws Exception {
        withDirectory(directory -> {
            byte[] audio = audio(8000, 1, 8000);
            RecordTrack track = track("Original", audio);
            RecordLibrary library = new RecordLibrary(directory);
            library.add(track, audio);
            Path stored = directory.resolve(track.id() + ".ogg");
            byte[] damaged = audio.clone();
            damaged[damaged.length - 1] ^= 1;
            Files.write(stored, damaged);
            rejects(IOException.class, () -> new RecordLibrary(directory));
            Files.write(stored, Arrays.copyOf(audio, audio.length - 1));
            rejects(IOException.class, () -> new RecordLibrary(directory));
            Files.delete(stored);
            rejects(IOException.class, () -> new RecordLibrary(directory));
            Files.write(stored, audio);
            Files.writeString(directory.resolve(track.id() + ".properties"),
                    "title=Wrong duration\ndurationTicks=21\nbytes=" + audio.length + "\n");
            rejects(IOException.class, () -> new RecordLibrary(directory));
            Files.writeString(directory.resolve(track.id() + ".properties"), "title=\\uZZZZ\n");
            rejects(IOException.class, () -> new RecordLibrary(directory));
        });
    }

    private static RecordTrack track(String title, byte[] bytes) throws IOException {
        return new RecordTrack(RecordAudio.id(bytes), title, RecordAudio.inspect(bytes).durationTicks(), bytes.length);
    }

    private static byte[] audio(int rate, int channels, long samples) throws IOException {
        return join(page(2, 0, SERIAL, 0, identification(rate, channels)),
                page(0, 0, SERIAL, 1, header(3)), page(0, 0, SERIAL, 2, header(5)),
                page(4, samples, SERIAL, 3, new byte[]{0}));
    }

    private static byte[] identification(int rate, int channels) {
        byte[] header = Arrays.copyOf(header(1), 30);
        header[11] = (byte) channels;
        putInt(header, 12, rate);
        header[28] = (byte) 0x86;
        header[29] = 1;
        return header;
    }

    private static byte[] header(int type) {
        byte[] header = new byte[7];
        header[0] = (byte) type;
        System.arraycopy("vorbis".getBytes(StandardCharsets.US_ASCII), 0, header, 1, 6);
        return header;
    }

    private static byte[] page(int flags, long granule, int serial, int sequence, byte[] packet) throws IOException {
        byte[] lacing = new byte[packet.length / 255 + 1];
        Arrays.fill(lacing, (byte) 255);
        lacing[lacing.length - 1] = (byte) (packet.length % 255);
        return page(flags, granule, serial, sequence, lacing, packet);
    }

    private static byte[] page(int flags, long granule, int serial, int sequence,
                               byte[] lacing, byte[] body) throws IOException {
        byte[] result = new byte[27 + lacing.length + body.length];
        System.arraycopy("OggS".getBytes(StandardCharsets.US_ASCII), 0, result, 0, 4);
        result[5] = (byte) flags;
        putInt(result, 6, (int) granule);
        putInt(result, 10, (int) (granule >>> 32));
        putInt(result, 14, serial);
        putInt(result, 18, sequence);
        result[26] = (byte) lacing.length;
        System.arraycopy(lacing, 0, result, 27, lacing.length);
        System.arraycopy(body, 0, result, 27 + lacing.length, body.length);
        int crc = 0;
        for (byte value : result) {
            crc ^= (value & 255) << 24;
            for (int bit = 0; bit < 8; bit++) crc = crc < 0 ? (crc << 1) ^ 0x04c11db7 : crc << 1;
        }
        putInt(result, 22, crc);
        return result;
    }

    private static void putInt(byte[] bytes, int offset, int value) {
        for (int i = 0; i < 4; i++) bytes[offset + i] = (byte) (value >>> (8 * i));
    }

    private static byte[] join(byte[]... pieces) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        for (byte[] piece : pieces) result.write(piece);
        return result.toByteArray();
    }

    private static void withDirectory(DirectoryCheck check) throws Exception {
        Path directory = Files.createTempDirectory("ssite-record-media-").toAbsolutePath().normalize();
        try {
            check.run(directory);
        } finally {
            try (var files = Files.walk(directory)) {
                for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
                    if (!file.toAbsolutePath().normalize().startsWith(directory)) {
                        throw new IOException("Test cleanup escaped its temporary directory");
                    }
                    Files.deleteIfExists(file);
                }
            }
        }
    }

    private static void rejects(Class<? extends Exception> expected, ThrowingAction action) throws Exception {
        try {
            action.run();
        } catch (Exception failure) {
            if (expected.isInstance(failure)) return;
            throw new AssertionError("Expected " + expected.getSimpleName() + ", got " + failure, failure);
        }
        throw new AssertionError("Expected " + expected.getSimpleName());
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    @FunctionalInterface private interface ThrowingAction { void run() throws Exception; }
    @FunctionalInterface private interface DirectoryCheck { void run(Path directory) throws Exception; }
}
