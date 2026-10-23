package com.lombinaxmasson.ssite.forge;

import com.lombinaxmasson.ssite.SsiteAddon;
import com.lombinaxmasson.ssite.material.SsiteMaterials;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

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
                    output.acceptAll(machineStacks());
                    output.acceptAll(SsiteMaterials.creativeStacks());
                })
                .build());
        TABS.register(modEventBus);
    }

    /** Block items GregTech registers under the addon id. */
    private static List<ItemStack> machineStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (var item : BuiltInRegistries.ITEM) {
            var id = BuiltInRegistries.ITEM.getKey(item);
            if (SsiteAddon.MOD_ID.equals(id.getNamespace())) stacks.add(new ItemStack(item));
        }
        return stacks;
    }

    private static ItemStack icon() {
        var stacks = SsiteMaterials.creativeStacks();
        return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0);
    }
}
