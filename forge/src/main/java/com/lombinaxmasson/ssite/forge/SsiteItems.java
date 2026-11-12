package com.lombinaxmasson.ssite.forge;

import com.lombinaxmasson.ssite.SsiteAddon;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

final class SsiteItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SsiteAddon.MOD_ID);

    static {
        ITEMS.register("fiberglass_wind_rotor", () -> new Item(new Item.Properties().stacksTo(1)));
    }

    private SsiteItems() {}

    static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
