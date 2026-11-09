package com.lombinaxmasson.ssite.media;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** NeoForge item-data boundary for imported track metadata. Audio bytes stay in the server library. */
public final class RecordData {
    private RecordData() {}

    public static RecordTrack get(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        if (custom == null) return null;
        CompoundTag root = custom.copyTag();
        if (!root.contains(RecordCatalog.DATA_KEY, Tag.TAG_COMPOUND)) return null;
        CompoundTag data = root.getCompound(RecordCatalog.DATA_KEY);
        try {
            return new RecordTrack(data.getString("id"), data.getString("title"),
                    data.getInt("duration"), data.getInt("bytes"));
        } catch (IllegalArgumentException invalid) {
            return null;
        }
    }

    public static void set(ItemStack stack, RecordTrack track) {
        if (stack == null || stack.isEmpty()) throw new IllegalArgumentException("Empty record carrier");
        CompoundTag data = new CompoundTag();
        data.putString("id", track.id());
        data.putString("title", track.title());
        data.putInt("duration", track.durationTicks());
        data.putInt("bytes", track.bytes());
        CustomData.update(DataComponents.CUSTOM_DATA, stack,
                root -> root.put(RecordCatalog.DATA_KEY, data));
    }

    public static boolean isTemplate(ItemStack stack) {
        return hasId(stack, RecordCatalog.TEMPLATE_ITEM);
    }

    public static boolean isRecord(ItemStack stack) {
        return hasId(stack, RecordCatalog.VINYL_ITEM) || hasId(stack, RecordCatalog.CD_ITEM);
    }

    private static boolean hasId(ItemStack stack, String path) {
        return stack != null && !stack.isEmpty()
                && RecordCatalog.itemId(path).equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }
}
