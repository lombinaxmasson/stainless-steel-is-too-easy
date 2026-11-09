package com.lombinaxmasson.ssite.media;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** An ordered, bounded transfer; repeated or missing fragments cannot publish partial files. */
public final class RecordTransfer {
    private final RecordTrack track;
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    public RecordTransfer(RecordTrack track) {
        this.track = track;
    }

    public RecordTrack track() { return track; }
    public int offset() { return bytes.size(); }
    public boolean complete() { return bytes.size() == track.bytes(); }

    public void append(int offset, byte[] data) throws IOException {
        if (offset != bytes.size() || data.length == 0 || data.length > RecordPacket.MAX_CHUNK
                || (long) offset + data.length > track.bytes()) {
            throw new IOException("Out-of-order or oversized music fragment");
        }
        bytes.write(data);
    }

    public byte[] finish() throws IOException {
        byte[] result = bytes.toByteArray();
        if (!complete() || !RecordAudio.id(result).equals(track.id())
                || RecordAudio.inspect(result).durationTicks() != track.durationTicks()) {
            throw new IOException("Music hash or duration mismatch");
        }
        return result;
    }
}
