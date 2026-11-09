package com.lombinaxmasson.ssite.media;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

/** Shows the selected track on reusable tooling and workpieces supplied by the host. */
public final class RecordTooltips {
    private RecordTooltips() {}

    public static void append(ItemStack stack, List<Component> tooltip) {
        if (RecordData.isRecord(stack)) return;
        RecordTrack track = RecordData.get(stack);
        if (track == null) {
            if (RecordData.isTemplate(stack))
                tooltip.add(Component.translatable("tooltip.ssite.record.template_blank"));
            return;
        }
        tooltip.add(Component.literal(track.title()));
        int seconds = track.durationTicks() / 20;
        tooltip.add(Component.translatable("tooltip.ssite.record.duration", seconds / 60,
                String.format(Locale.ROOT, "%02d", seconds % 60)));
    }
}
