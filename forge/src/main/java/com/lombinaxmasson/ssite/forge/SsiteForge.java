package com.lombinaxmasson.ssite.forge;

import com.lombinaxmasson.ssite.SsiteAddon;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

@Mod(SsiteAddon.MOD_ID)
public final class SsiteForge {
    public SsiteForge(IEventBus modEventBus) {
        SsiteAddon.register();
        SsiteCreativeTab.register(modEventBus);
    }
}
