package com.lombinaxmasson.ssite.media;

import net.minecraft.client.sounds.AudioStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.BooleanSupplier;
import javax.sound.sampled.AudioFormat;

/** Bounded PCM cursor used to catch up after download without retaining a whole decoded song. */
final class RecordPcmStream implements AudioStream {
    private final AudioStream source;
    private final int channels;
    private final AudioFormat format;
    private ByteBuffer pending;
    private long cursorFrames;

    RecordPcmStream(AudioStream source) throws IOException {
        this.source = source;
        AudioFormat input = source.getFormat();
        channels = input.getChannels();
        if (input.getSampleSizeInBits() != 16 || channels < 1 || channels > 2 || input.isBigEndian()) {
            source.close();
            throw new IOException("Unsupported record PCM format");
        }
        format = new AudioFormat(input.getSampleRate(), 16, 1, true, false);
    }

    long cursorFrames() {
        return cursorFrames;
    }

    @Override
    public AudioFormat getFormat() {
        return format;
    }

    @Override
    public ByteBuffer read(int bytes) throws IOException {
        if (pending != null) {
            ByteBuffer result = pending;
            pending = null;
            cursorFrames += result.remaining() / 2;
            return result;
        }
        ByteBuffer input = source.read(Math.max(2, bytes) * channels).order(ByteOrder.LITTLE_ENDIAN);
        if (input.remaining() % (channels * 2) != 0) throw new IOException("Incomplete record PCM frame");
        if (channels == 1) {
            cursorFrames += input.remaining() / 2;
            return input;
        }
        ByteBuffer output = ByteBuffer.allocateDirect(input.remaining() / 4 * 2).order(ByteOrder.LITTLE_ENDIAN);
        while (input.remaining() >= 4) output.putShort((short)((input.getShort() + input.getShort()) / 2));
        output.flip();
        cursorFrames += output.remaining() / 2;
        return output;
    }

    void skipTo(long frame, BooleanSupplier cancelled) throws IOException {
        while (cursorFrames < frame) {
            if (cancelled.getAsBoolean() || Thread.currentThread().isInterrupted()) {
                throw new IOException("Record playback cancelled");
            }
            long before = cursorFrames;
            ByteBuffer block = read(64 * 1024);
            if (!block.hasRemaining()) throw new IOException("Record ended while seeking");
            if (cursorFrames > frame) {
                block.position(block.position() + Math.toIntExact((frame - before) * 2L));
                pending = block;
                cursorFrames = frame;
            }
        }
    }

    @Override
    public void close() throws IOException {
        source.close();
    }
}
