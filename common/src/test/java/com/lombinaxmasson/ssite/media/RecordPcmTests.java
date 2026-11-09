package com.lombinaxmasson.ssite.media;

import net.minecraft.client.sounds.AudioStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.AudioFormat;

/** PCM-cursor regressions use fixed samples and deliberately oversized decoder blocks. */
public final class RecordPcmTests {
    private RecordPcmTests() {}

    public static void run() throws IOException {
        seekInsideBlockRetainsFollowingSamples();
        repeatedSeekDoesNotLoseBufferedAudio();
        skipAcrossBlocksAndEof();
        stereoIsMixedToMono();
        cancellationAndCloseReachDecoder();
        System.out.println("RecordPcmTests: all 5 checks passed.");
    }

    public static void main(String[] args) throws IOException {
        run();
    }

    private static void seekInsideBlockRetainsFollowingSamples() throws IOException {
        try (RecordPcmStream stream = new RecordPcmStream(new Samples(1, 8, 4))) {
            stream.skipTo(3, () -> false);
            check(stream.cursorFrames() == 3, "seek cursor reaches sample three");
            check(stream.read(2).getShort() == 3, "the next sample is not the next decoder block");
            check(stream.read(2).getShort() == 4, "following blocks remain contiguous");
        }
    }

    private static void repeatedSeekDoesNotLoseBufferedAudio() throws IOException {
        try (RecordPcmStream stream = new RecordPcmStream(new Samples(1, 8, 4))) {
            stream.skipTo(1, () -> false);
            stream.skipTo(3, () -> false);
            check(stream.read(2).getShort() == 3, "catching up again uses buffered remainder");
            check(stream.read(2).getShort() == 4, "repeated seeks do not skip a whole block");
        }
    }

    private static void skipAcrossBlocksAndEof() throws IOException {
        try (RecordPcmStream stream = new RecordPcmStream(new Samples(1, 12, 4))) {
            stream.skipTo(9, () -> false);
            check(stream.read(2).getShort() == 9, "seek crosses multiple decoder blocks");
            check(!stream.read(2).hasRemaining(), "the retained final block is delivered once");
        }
        try (RecordPcmStream stream = new RecordPcmStream(new Samples(1, 4, 4))) {
            expectFailure(() -> stream.skipTo(5, () -> false), "seek past EOF must fail");
        }
    }

    private static void stereoIsMixedToMono() throws IOException {
        try (RecordPcmStream stream = new RecordPcmStream(new Samples(2, 8, 4))) {
            stream.skipTo(2, () -> false);
            check(stream.getFormat().getChannels() == 1, "positional output has one channel");
            check(stream.read(2).getShort() == 7, "left/right channels are averaged after seeking");
        }
    }

    private static void cancellationAndCloseReachDecoder() throws IOException {
        Samples samples = new Samples(1, 8, 4);
        try (RecordPcmStream stream = new RecordPcmStream(samples)) {
            expectFailure(() -> stream.skipTo(1, () -> true), "cancelled seeks must stop");
            check(samples.cursor == 0, "cancellation occurs before decoding more data");
        }
        check(samples.closed, "close releases the decoder");
    }

    private static void expectFailure(Checked action, String message) throws IOException {
        try {
            action.run();
        } catch (IOException expected) {
            return;
        }
        throw new AssertionError(message);
    }

    private static void check(boolean success, String message) {
        if (!success) throw new AssertionError(message);
    }

    @FunctionalInterface
    private interface Checked { void run() throws IOException; }

    private static final class Samples implements AudioStream {
        private final int channels;
        private final int total;
        private final int blockFrames;
        private int cursor;
        private boolean closed;

        private Samples(int channels, int total, int blockFrames) {
            this.channels = channels;
            this.total = total;
            this.blockFrames = blockFrames;
        }

        @Override public AudioFormat getFormat() { return new AudioFormat(48000, 16, channels, true, false); }

        @Override
        public ByteBuffer read(int requested) {
            int count = Math.min(total - cursor, blockFrames);
            ByteBuffer samples = ByteBuffer.allocateDirect(count * channels * 2).order(ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < count; i++, cursor++) {
                samples.putShort((short)cursor);
                if (channels == 2) samples.putShort((short)(cursor + 10));
            }
            return samples.flip();
        }

        @Override public void close() { closed = true; }
    }
}
