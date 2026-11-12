package com.lombinaxmasson.ssite.neoforge;

import com.lombinaxmasson.ssite.SsiteAddon;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

final class SsiteItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, SsiteAddon.MOD_ID);

    static {
        ITEMS.register("fiberglass_wind_rotor", () -> new Item(new Item.Properties().stacksTo(1)));
    }

    private SsiteItems() {}

    static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
