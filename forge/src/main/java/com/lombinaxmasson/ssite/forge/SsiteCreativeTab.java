package com.lombinaxmasson.ssite.forge;

import com.lombinaxmasson.ssite.SsiteAddon;
import com.lombinaxmasson.ssite.machine.SsiteMachines;
import com.lombinaxmasson.ssite.material.SsiteMaterials;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

/** The ssite machines, pipes and material forms. */
public final class SsiteCreativeTab {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SsiteAddon.MOD_ID);

    private SsiteCreativeTab() {}

    public static void register(IEventBus modEventBus) {
        TABS.register("materials", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.ssite.materials"))
                .withSearchBar()
                .icon(SsiteCreativeTab::icon)
                .displayItems((params, output) -> {
                    output.acceptAll(SsiteMachines.creativeStacks());
                    output.acceptAll(SsiteMaterials.creativeStacks());
                })
                .build());
        TABS.register(modEventBus);
    }

    private static ItemStack icon() {
        var stacks = SsiteMaterials.creativeStacks();
        return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0);
    }
}
