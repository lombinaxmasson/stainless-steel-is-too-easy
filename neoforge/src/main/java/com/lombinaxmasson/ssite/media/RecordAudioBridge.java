package com.lombinaxmasson.ssite.media;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantFloat;

import java.io.IOException;
import java.io.InputStream;

/** NeoForge 1.21.1 audio constructor differences. Only loaded by the client playback path. */
final class RecordAudioBridge {
    private RecordAudioBridge() {}

    static AudioStream open(InputStream input) throws IOException {
        return new JOrbisAudioStream(input);
    }

    static Sound sound(ResourceLocation id) {
        return new Sound(id, ConstantFloat.of(1.0F), ConstantFloat.of(1.0F),
                1, Sound.Type.FILE, true, false, 16);
    }
}
