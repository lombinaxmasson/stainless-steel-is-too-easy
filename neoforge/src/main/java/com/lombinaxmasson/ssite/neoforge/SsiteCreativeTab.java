package com.lombinaxmasson.ssite.neoforge;

import com.lombinaxmasson.ssite.SsiteAddon;
import com.lombinaxmasson.ssite.material.PolymerMaterials;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** One creative tab holding the M4 polymer and chemical item forms. */
public final class SsiteCreativeTab {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SsiteAddon.MOD_ID);

    private SsiteCreativeTab() {}

    public static void register(IEventBus modEventBus) {
        TABS.register("materials", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.ssite.materials"))
                .withSearchBar()
                .icon(SsiteCreativeTab::icon)
                .displayItems((params, output) -> output.acceptAll(PolymerMaterials.creativeStacks()))
                .build());
        TABS.register(modEventBus);
    }

    private static ItemStack icon() {
        var stacks = PolymerMaterials.creativeStacks();
        return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0);
    }
}
