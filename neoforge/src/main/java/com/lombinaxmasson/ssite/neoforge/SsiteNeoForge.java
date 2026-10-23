package com.lombinaxmasson.ssite.neoforge;

import com.lombinaxmasson.ssite.SsiteAddon;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(SsiteAddon.MOD_ID)
public final class SsiteNeoForge {
    public SsiteNeoForge(IEventBus modEventBus) {
        SsiteAddon.register();
        SsiteCreativeTab.register(modEventBus);
    }
}
