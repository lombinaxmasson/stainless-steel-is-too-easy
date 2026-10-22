package com.lombinaxmasson.sstte.forge;

import com.lombinaxmasson.sstte.SstteAddon;
import net.minecraftforge.fml.common.Mod;

@Mod(SstteAddon.MOD_ID)
public final class SstteForge {
    public SstteForge() {
        SstteAddon.register();
    }
}
