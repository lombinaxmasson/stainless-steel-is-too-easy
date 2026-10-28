package com.lombinaxmasson.ssite.compat;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/** Optional Jade integration; loaded only when Jade is present. Not a hard mod dependency. */
@WailaPlugin
public final class SsiteJadePlugin implements IWailaPlugin {
    @Override public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(AodJadeProvider.INSTANCE, BlockEntity.class);
    }

    @Override public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(AodJadeProvider.INSTANCE, Block.class);
    }
}
