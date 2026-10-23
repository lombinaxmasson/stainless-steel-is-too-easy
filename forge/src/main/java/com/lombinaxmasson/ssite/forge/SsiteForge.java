package com.lombinaxmasson.ssite.forge;

import com.lombinaxmasson.ssite.SsiteAddon;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SsiteAddon.MOD_ID)
public final class SsiteForge {
    public SsiteForge() {
        SsiteAddon.register();
        SsiteCreativeTab.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
