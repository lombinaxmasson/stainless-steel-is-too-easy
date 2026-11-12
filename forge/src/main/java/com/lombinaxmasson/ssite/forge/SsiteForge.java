package com.lombinaxmasson.ssite.forge;

import com.lombinaxmasson.ssite.SsiteAddon;
import com.lombinaxmasson.ssite.forge.media.ForgeRecordItems;
import com.lombinaxmasson.ssite.media.RecordNetwork;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SsiteAddon.MOD_ID)
public final class SsiteForge {
    public SsiteForge() {
        SsiteAddon.register();
        SsiteWorldgen.register(FMLJavaModLoadingContext.get().getModEventBus());
        SsiteCreativeTab.register(FMLJavaModLoadingContext.get().getModEventBus());
        SsiteItems.register(FMLJavaModLoadingContext.get().getModEventBus());
        ForgeRecordItems.register(FMLJavaModLoadingContext.get().getModEventBus());
        RecordNetwork.register();
    }
}
