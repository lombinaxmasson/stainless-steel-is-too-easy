package com.lombinaxmasson.ssite.neoforge;

import com.lombinaxmasson.ssite.SsiteAddon;
import net.neoforged.fml.common.Mod;

@Mod(SsiteAddon.MOD_ID)
public final class SsiteNeoForge {
    public SsiteNeoForge() {
        SsiteAddon.register();
    }
}
