package com.lombinaxmasson.ssite.media;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

/** A positional records-channel stream whose cursor follows the server's playback clock. */
final class RecordSound extends AbstractTickableSoundInstance {
    private final Path file;
    private final ClientLevel level;
    private final Executor executor;
    private volatile long elapsedTicks;
    private volatile long clockNanos;
    private volatile boolean cancelled;

    RecordSound(String id, Path file, BlockPos pos, long elapsedTicks, Executor executor) {
        super(SoundEvent.createVariableRangeEvent(ResourceLocation.tryBuild("ssite", "imported/" + id)),
                SoundSource.RECORDS, RandomSource.create());
        this.file = file;
        this.level = Minecraft.getInstance().level;
        this.executor = executor;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
        this.volume = 4.0F;
        updateClock(elapsedTicks);
    }

    void updateClock(long ticks) {
        this.elapsedTicks = Math.max(0, ticks);
        this.clockNanos = System.nanoTime();
    }

    void finish() {
        cancelled = true;
        stop();
    }

    @Override
    public void tick() {
        if (cancelled || Minecraft.getInstance().level != level) finish();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public WeighedSoundEvents resolve(SoundManager manager) {
        sound = RecordAudioBridge.sound(location);
        WeighedSoundEvents events = new WeighedSoundEvents(location, null);
        events.addSound(sound);
        return events;
    }

    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary buffers, Sound sound, boolean looping) {
        return CompletableFuture.supplyAsync(() -> {
            RecordPcmStream stream = null;
            try {
                if (cancelled) throw new IOException("Record playback cancelled");
                InputStream input = Files.newInputStream(file);
                AudioStream decoder;
                try {
                    decoder = RecordAudioBridge.open(input);
                } catch (IOException | RuntimeException e) {
                    input.close();
                    throw e;
                }
                stream = new RecordPcmStream(decoder);
                // Decode on the I/O worker, retaining only a small PCM block while skipping.
                // Refresh the target so time spent decoding does not become permanent drift.
                for (int attempt = 0; attempt < 8; attempt++) {
                    long ticks = elapsedTicks + Math.max(0L, System.nanoTime() - clockNanos) / 50_000_000L;
                    long target = ticks * (long)stream.getFormat().getSampleRate() / 20L;
                    if (target <= stream.cursorFrames()) break;
                    stream.skipTo(target, () -> cancelled);
                }
                if (cancelled) throw new IOException("Record playback cancelled");
                return stream;
            } catch (IOException | RuntimeException e) {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException closeFailure) {
                        e.addSuppressed(closeFailure);
                    }
                }
                if (!cancelled) Minecraft.getInstance().execute(() -> RecordClient.playbackFailed(this));
                throw new CompletionException(e);
            }
        }, executor);
    }

}
