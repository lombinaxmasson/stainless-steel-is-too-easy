package com.lombinaxmasson.ssite.media;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Bounded loader-independent wire format. Directions are checked by each receiver. */
public record RecordPacket(int kind, String id, String title, int durationTicks, int totalBytes,
                           int offset, long blockPos, long startTick, byte[] data) {
    public static final int UPLOAD_BEGIN = 0;
    public static final int UPLOAD_CHUNK = 1;
    public static final int DOWNLOAD_REQUEST = 2;
    public static final int DOWNLOAD_BEGIN = 3;
    public static final int DOWNLOAD_CHUNK = 4;
    public static final int PLAY = 5;
    public static final int STOP = 6;
    public static final int NOTICE = 7;
    public static final int MAX_CHUNK = 16 * 1024;
    public static final int MAX_BYTES = RecordAudio.MAX_BYTES;
    public static final int MAX_PACKET = MAX_CHUNK + 512;

    public RecordPacket {
        if (kind < UPLOAD_BEGIN || kind > NOTICE || id == null || title == null
                || (!id.isEmpty() && !RecordTrack.validId(id)) || title.length() > 96
                || title.chars().anyMatch(Character::isISOControl)
                || durationTicks < 0 || durationTicks > RecordAudio.MAX_TICKS
                || totalBytes < 0 || totalBytes > MAX_BYTES || offset < 0 || offset > MAX_BYTES
                || data == null || data.length > MAX_CHUNK) {
            throw new IllegalArgumentException("Invalid music packet");
        }
        data = data.clone();
    }

    public RecordTrack track() {
        return new RecordTrack(id, title, durationTicks, totalBytes);
    }

    @Override
    public byte[] data() { return data.clone(); }

    public byte[] encode() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(bytes)) {
                out.writeByte(kind);
                out.writeUTF(id);
                out.writeUTF(title);
                out.writeInt(durationTicks);
                out.writeInt(totalBytes);
                out.writeInt(offset);
                out.writeLong(blockPos);
                out.writeLong(startTick);
                out.writeInt(data.length);
                out.write(data);
            }
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot encode music packet", e);
        }
    }

    public static RecordPacket decode(byte[] bytes) throws IOException {
        if (bytes.length > MAX_PACKET) throw new IOException("Music packet too large");
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            int kind = in.readUnsignedByte();
            String id = in.readUTF();
            String title = in.readUTF();
            int duration = in.readInt();
            int total = in.readInt();
            int offset = in.readInt();
            long pos = in.readLong();
            long tick = in.readLong();
            int length = in.readInt();
            if (length < 0 || length > MAX_CHUNK || length != in.available()) {
                throw new IOException("Invalid music chunk length");
            }
            return new RecordPacket(kind, id, title, duration, total, offset, pos, tick,
                    in.readNBytes(length));
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid music packet", e);
        }
    }
}
