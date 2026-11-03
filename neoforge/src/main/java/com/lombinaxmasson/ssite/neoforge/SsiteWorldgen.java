package com.lombinaxmasson.ssite.neoforge;

import com.lombinaxmasson.ssite.SsiteAddon;
import com.lombinaxmasson.ssite.worldgen.GasFieldFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** NeoForge registration for the gas-field feature; placement is data-driven. */
public final class SsiteWorldgen {
    private static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, SsiteAddon.MOD_ID);

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> GAS_FIELD =
            FEATURES.register("gas_field", GasFieldFeature::new);

    private SsiteWorldgen() {}

    public static void register(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
    }
}
