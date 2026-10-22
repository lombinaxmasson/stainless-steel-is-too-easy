package com.lombinaxmasson.ssite.forge;

import com.lombinaxmasson.ssite.SsiteAddon;
import net.minecraftforge.fml.common.Mod;

@Mod(SsiteAddon.MOD_ID)
public final class SsiteForge {
    public SsiteForge() {
        SsiteAddon.register();
    }
}
