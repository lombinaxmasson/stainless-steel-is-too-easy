package com.lombinaxmasson.ssite.neoforge;

import com.lombinaxmasson.ssite.SsiteAddon;
import com.lombinaxmasson.ssite.material.PolymerMaterials;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/** The AOD machines and the M4 polymer and chemical item forms. */
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
                    output.acceptAll(machineStacks());
                    output.acceptAll(PolymerMaterials.creativeStacks());
                })
                .build());
        TABS.register(modEventBus);
    }

    /** The AOD converter block items are registered by GregTech under the addon id. */
    private static List<ItemStack> machineStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (var item : BuiltInRegistries.ITEM) {
            var id = BuiltInRegistries.ITEM.getKey(item);
            if (!SsiteAddon.MOD_ID.equals(id.getNamespace()) || !id.getPath().startsWith("aod_")) continue;
            stacks.add(new ItemStack(item));
        }
        return stacks;
    }

    private static ItemStack icon() {
        var stacks = PolymerMaterials.creativeStacks();
        return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0);
    }
}
