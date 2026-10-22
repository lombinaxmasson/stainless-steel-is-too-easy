package com.lombinaxmasson.sstte.neoforge;

import com.lombinaxmasson.sstte.SstteAddon;
import net.neoforged.fml.common.Mod;

@Mod(SstteAddon.MOD_ID)
public final class SstteNeoForge {
    public SstteNeoForge() {
        SstteAddon.register();
    }
}
