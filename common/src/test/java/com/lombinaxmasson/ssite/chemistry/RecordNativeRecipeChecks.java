package com.lombinaxmasson.ssite.chemistry;

import com.lombinaxmasson.ssite.media.RecordData;
import com.lombinaxmasson.ssite.media.RecordTrack;
import gregtech.api.addon.platform.AddonRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Native stack checks called by a loader-started smoke fixture after game registries are ready. */
public final class RecordNativeRecipeChecks {
    private static final RecordTrack FIRST = new RecordTrack("a".repeat(64), "First", 2400, 4096);
    private static final RecordTrack SECOND = new RecordTrack("b".repeat(64), "Second", 3600, 8192);

    private RecordNativeRecipeChecks() {}

    public static void run() {
        nativeMetadataCopiesIndependently();
        unknownAndUnboundTracksDoNotMatch();
        everyStagePreservesCanonicalTrack();
        sourceSelectionSkipsUnboundAndUnknownCarriers();
        System.out.println("RecordNativeRecipeChecks: all 4 checks passed.");
    }

    private static void nativeMetadataCopiesIndependently() {
        ItemStack original = new ItemStack(Items.PAPER);
        check(RecordData.get(original) == null, "unbound carriers have no track");
        RecordData.set(original, FIRST);
        ItemStack copy = original.copy();
        RecordData.set(copy, SECOND);
        check(FIRST.equals(RecordData.get(original)), "writing a copy does not rewrite the original");
        check(SECOND.equals(RecordData.get(copy)), "native item data round-trips");
        check(RecordData.get(ItemStack.EMPTY) == null && RecordData.get(null) == null,
                "empty inputs have no track");
    }

    private static void unknownAndUnboundTracksDoNotMatch() {
        AddonRecipes.Line display = display(1, 1);
        AddonRecipes.Dynamic provider = RecordDynamicRecipes.provider(display, 1, Map.of(FIRST.id(), FIRST)::get);
        List<ItemStack> inputs = Arrays.asList(display.itemInputs());
        check(provider.find(inputs, List.of()) == null, "blank templates cannot produce recorded media");
        RecordData.set(inputs.get(1), SECOND);
        check(provider.find(inputs, List.of()) == null, "unknown audio ids cannot produce recorded media");
        RecordData.set(inputs.get(1), FIRST);
        check(provider.find(inputs, List.of()) != null, "known imported tracks can be manufactured");
    }

    private static void everyStagePreservesCanonicalTrack() {
        for (String label : List.of("m8-01/cut-master", "m8-01/nickel-stamper",
                "m8-01/press-vinyl", "m8-02/record-cdr")) {
            int source = RecordManufacturing.sourceInput(label);
            int[] kept = switch (label) {
                case "m8-01/cut-master" -> new int[]{1, 2};
                case "m8-01/press-vinyl" -> new int[]{2};
                case "m8-02/record-cdr" -> new int[]{1};
                default -> new int[0];
            };
            AddonRecipes.Line display = display(source, kept);
            AddonRecipes.Dynamic provider = RecordDynamicRecipes.provider(display, source,
                    Map.of(FIRST.id(), FIRST, SECOND.id(), SECOND)::get);
            List<ItemStack> inputs = Arrays.asList(display.itemInputs());
            RecordTrack misleading = new RecordTrack(FIRST.id(), "Incorrect title", 1, 1);
            RecordData.set(inputs.get(source), misleading);
            AddonRecipes.Line result = provider.find(inputs, List.of());
            check(result != null, "the known id matches: " + label);
            check(FIRST.equals(RecordData.get(result.itemOutputs()[0])),
                    "output uses authoritative library metadata: " + label);
            check(misleading.equals(RecordData.get(result.itemInputs()[source])),
                    "host requirement retains exact source item data: " + label);
            check(misleading.equals(RecordData.get(inputs.get(source))),
                    "lookup does not modify the machine input: " + label);
            check(RecordData.get(display.itemOutputs()[0]) == null,
                    "lookup does not mutate the display row: " + label);
            check(Arrays.equals(result.keptInputs(), kept), "tools keep their consumption roles: " + label);
            check(result.duration() == display.duration() && result.energyPerTick() == display.energyPerTick(),
                    "manufacturing cost is preserved: " + label);
            RecordData.set(inputs.get(source), SECOND);
            AddonRecipes.Line next = provider.find(inputs, List.of());
            check(SECOND.equals(RecordData.get(next.itemOutputs()[0]))
                            && FIRST.equals(RecordData.get(result.itemOutputs()[0])),
                    "switching tracks cannot rewrite another batch: " + label);
        }
    }

    private static void sourceSelectionSkipsUnboundAndUnknownCarriers() {
        AddonRecipes.Line display = display(1, 1);
        AddonRecipes.Dynamic provider = RecordDynamicRecipes.provider(display, 1, Map.of(FIRST.id(), FIRST)::get);
        ItemStack unknown = new ItemStack(Items.PAPER);
        RecordData.set(unknown, SECOND);
        ItemStack known = new ItemStack(Items.PAPER);
        RecordData.set(known, FIRST);
        AddonRecipes.Line result = provider.find(List.of(new ItemStack(Items.PAPER), unknown, known), List.of());
        check(result != null && FIRST.equals(RecordData.get(result.itemOutputs()[0])),
                "unbound or missing-library carriers do not hide a valid source");
    }

    private static AddonRecipes.Line display(int source, int... kept) {
        ItemStack[] inputs = {new ItemStack(Items.IRON_INGOT), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.COPPER_INGOT)};
        inputs[source] = new ItemStack(Items.PAPER);
        return AddonRecipes.Line.builder().itemInputs(inputs).itemOutputs(new ItemStack(Items.GOLD_INGOT))
                .duration(200).energyPerTick(64).keptInputs(kept).build();
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
