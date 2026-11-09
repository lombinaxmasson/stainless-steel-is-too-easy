package com.lombinaxmasson.ssite.chemistry;

import com.lombinaxmasson.ssite.media.RecordData;
import com.lombinaxmasson.ssite.media.RecordServer;
import com.lombinaxmasson.ssite.media.RecordTrack;
import gregtech.api.addon.platform.AddonRecipes;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

/** Copies a library-backed track through the four manufacturing steps that carry audio identity. */
final class RecordDynamicRecipes {
    private RecordDynamicRecipes() {}

    static void register(RecipeDeclaration declaration, AddonRecipes.Line display) {
        int source = RecordManufacturing.sourceInput(declaration.label());
        if (source < 0) return;
        if (!declaration.displayOnly()) {
            throw new IllegalArgumentException("Track-bearing recipes require display-only declarations");
        }
        AddonRecipes.map(declaration.map()).addDynamic(provider(display, source, RecordServer::track));
    }

    static AddonRecipes.Dynamic provider(AddonRecipes.Line display, int source,
                                         Function<String, RecordTrack> library) {
        ItemStack required = display.itemInputs()[source];
        return (items, fluids) -> {
            var selected = RecordManufacturing.select(items,
                    candidate -> !candidate.isEmpty() && candidate.is(required.getItem())
                            && candidate.getCount() >= required.getCount(), RecordData::get, library);
            if (selected == null) return null;
            ItemStack[] inputs = display.itemInputs();
            // Retain all source data in the requirement so the host cannot consume another track.
            inputs[source] = selected.source().copyWithCount(required.getCount());
            ItemStack[] outputs = display.itemOutputs();
            for (ItemStack output : outputs) RecordData.set(output, selected.track());
            return AddonRecipes.Line.builder()
                    .itemInputs(inputs).itemOutputs(outputs)
                    .fluidInputs(display.fluidInputs()).fluidOutputs(display.fluidOutputs())
                    .duration(display.duration()).energyPerTick(display.energyPerTick())
                    .special(display.special()).outputChances(display.outputChances())
                    .keptInputs(display.keptInputs()).build();
        };
    }
}
