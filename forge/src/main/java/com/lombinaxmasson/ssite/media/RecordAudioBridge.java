package com.lombinaxmasson.ssite.media;

import com.mojang.blaze3d.audio.OggAudioStream;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantFloat;

import java.io.IOException;
import java.io.InputStream;

/** Forge 1.20.1 audio constructor differences. Only loaded by the client playback path. */
final class RecordAudioBridge {
    private RecordAudioBridge() {}

    static AudioStream open(InputStream input) throws IOException {
        return new OggAudioStream(input);
    }

    static Sound sound(ResourceLocation id) {
        return new Sound(id.toString(), ConstantFloat.of(1.0F), ConstantFloat.of(1.0F),
                1, Sound.Type.FILE, true, false, 16);
    }
}
