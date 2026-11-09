package com.lombinaxmasson.ssite.media;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Structural validation before untrusted uploads enter the library or audio decoder. */
public final class RecordAudio {
    public static final int MAX_BYTES = 16 * 1024 * 1024;
    public static final int MAX_TICKS = 10 * 60 * 20;
    public static final int CHUNK = 16 * 1024;
    private static final int[] CRC_TABLE = crcTable();
    public record Info(int durationTicks, int sampleRate, int channels) {}

    private RecordAudio() {}

    public static String id(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public static Info inspect(byte[] bytes) throws IOException {
        if (bytes.length < 58 || bytes.length > MAX_BYTES) throw new IOException("Invalid OGG size");
        int offset = 0, serial = 0, sequence = 0, packets = 0, sampleRate = 0, channels = 0;
        long lastGranule = -1;
        boolean ended = false;
        ByteArrayOutputStream packet = new ByteArrayOutputStream();
        while (offset < bytes.length) {
            if (ended || offset + 27 > bytes.length || bytes[offset] != 'O' || bytes[offset + 1] != 'g'
                    || bytes[offset + 2] != 'g' || bytes[offset + 3] != 'S' || bytes[offset + 4] != 0) {
                throw new IOException("Invalid OGG page");
            }
            int flags = bytes[offset + 5] & 255;
            int count = bytes[offset + 26] & 255;
            if ((flags & ~7) != 0 || offset + 27 + count > bytes.length) throw new IOException("Invalid OGG flags");
            int body = offset + 27 + count, end = body;
            for (int i = 0; i < count; i++) end += bytes[offset + 27 + i] & 255;
            if (end > bytes.length || end == offset) throw new IOException("Truncated OGG page");
            if (sequence == 0) {
                if ((flags & 2) == 0 || (flags & 1) != 0) throw new IOException("Missing OGG beginning");
                serial = littleInt(bytes, offset + 14);
            } else if ((flags & 2) != 0) {
                throw new IOException("Chained OGG streams are unsupported");
            }
            if (littleInt(bytes, offset + 14) != serial || littleInt(bytes, offset + 18) != sequence++
                    || ((flags & 1) != 0) != (packet.size() > 0)) {
                throw new IOException("Discontinuous OGG stream");
            }
            if (crc(bytes, offset, end) != littleInt(bytes, offset + 22)) throw new IOException("OGG checksum mismatch");
            for (int i = 0; i < count; i++) {
                int length = bytes[offset + 27 + i] & 255;
                if (packets < 3) {
                    if (packet.size() + length > 256 * 1024) throw new IOException("Vorbis header too large");
                    packet.write(bytes, body, length);
                } else if (length > 0 && packet.size() == 0) {
                    // Keep continuation state without retaining compressed audio packets.
                    packet.write(0);
                }
                body += length;
                if (length < 255) {
                    if (packets < 3) {
                        byte[] header = packet.toByteArray();
                        int type = packets == 0 ? 1 : packets == 1 ? 3 : 5;
                        if (header.length < 7 || header[0] != type || header[1] != 'v'
                                || header[2] != 'o' || header[3] != 'r' || header[4] != 'b'
                                || header[5] != 'i' || header[6] != 's') throw new IOException("Expected Vorbis headers");
                        if (packets == 0) {
                            if (header.length != 30 || littleInt(header, 7) != 0 || header[29] != 1) {
                                throw new IOException("Invalid Vorbis identification");
                            }
                            channels = header[11] & 255;
                            sampleRate = littleInt(header, 12);
                            int small = header[28] & 15, large = (header[28] & 255) >>> 4;
                            if (channels != 1 || sampleRate < 8000 || sampleRate > 96000
                                    || small < 6 || large < small || large > 13) {
                                throw new IOException("Use mono Vorbis, 8–96 kHz");
                            }
                        }
                    }
                    packets++;
                    packet.reset();
                }
            }
            long granule = littleLong(bytes, offset + 6);
            if ((flags & 4) != 0 && granule < 0) throw new IOException("Missing final OGG sample position");
            if (granule != -1) {
                if (granule < 0 || granule < lastGranule) throw new IOException("Invalid OGG sample position");
                lastGranule = granule;
                if (sampleRate > 0 && granule > (long) sampleRate * MAX_TICKS / 20) {
                    throw new IOException("Music exceeds ten minutes");
                }
            }
            ended = (flags & 4) != 0;
            offset = end;
        }
        if (!ended || packet.size() != 0 || packets < 4 || sampleRate == 0 || lastGranule <= 0) {
            throw new IOException("Incomplete Vorbis stream");
        }
        int duration = (int) ((lastGranule * 20 + sampleRate - 1) / sampleRate);
        if (duration > MAX_TICKS) throw new IOException("Music exceeds ten minutes");
        return new Info(duration, sampleRate, channels);
    }

    private static int littleInt(byte[] bytes, int offset) {
        return (bytes[offset] & 255) | (bytes[offset + 1] & 255) << 8
                | (bytes[offset + 2] & 255) << 16 | (bytes[offset + 3] & 255) << 24;
    }

    private static long littleLong(byte[] bytes, int offset) {
        return (littleInt(bytes, offset) & 0xffffffffL) | (long) littleInt(bytes, offset + 4) << 32;
    }

    private static int crc(byte[] bytes, int start, int end) {
        int value = 0;
        for (int i = start; i < end; i++) {
            int next = i >= start + 22 && i < start + 26 ? 0 : bytes[i] & 255;
            value = (value << 8) ^ CRC_TABLE[((value >>> 24) ^ next) & 255];
        }
        return value;
    }

    private static int[] crcTable() {
        int[] table = new int[256];
        for (int i = 0; i < table.length; i++) {
            int value = i << 24;
            for (int bit = 0; bit < 8; bit++) value = (value << 1) ^ (value < 0 ? 0x04c11db7 : 0);
            table[i] = value;
        }
        return table;
    }
}
