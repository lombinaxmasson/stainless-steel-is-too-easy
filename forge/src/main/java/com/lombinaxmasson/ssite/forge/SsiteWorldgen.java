package com.lombinaxmasson.ssite.forge;

import com.lombinaxmasson.ssite.SsiteAddon;
import com.lombinaxmasson.ssite.worldgen.GasFieldFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Forge registration for the gas-field feature; placement is data-driven. */
public final class SsiteWorldgen {
    private static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, SsiteAddon.MOD_ID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> GAS_FIELD =
            FEATURES.register("gas_field", GasFieldFeature::new);

    private SsiteWorldgen() {}

    public static void register(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
    }
}
