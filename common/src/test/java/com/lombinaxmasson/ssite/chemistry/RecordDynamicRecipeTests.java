package com.lombinaxmasson.ssite.chemistry;

import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.ItemRef;
import com.lombinaxmasson.ssite.media.RecordTrack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Exercises the production selection core and declared media flow without initializing game registries. */
public final class RecordDynamicRecipeTests {
    private static final RecordTrack FIRST = new RecordTrack("a".repeat(64), "First", 2400, 4096);
    private static final RecordTrack SECOND = new RecordTrack("b".repeat(64), "Second", 3600, 8192);
    private static final List<RecipeDeclaration> ROWS = ChemistryRecipes.declarations();

    private record Carrier(ItemRef item, RecordTrack track) {}

    private RecordDynamicRecipeTests() {}

    public static void main(String[] args) {
        unknownAndUnboundTracksDoNotMatch();
        sourceSelectionChecksIdentityAndCount();
        everyStageUsesCanonicalTrack();
        manufacturingChainsPreserveTrackIdentity();
        independentQueriesObserveLibraryChanges();
        System.out.println("RecordDynamicRecipeTests: all 5 checks passed (selection core; native stacks require game smoke).");
    }

    private static void unknownAndUnboundTracksDoNotMatch() {
        ItemRef template = source("m8-01/cut-master");
        Function<String, RecordTrack> library = Map.of(FIRST.id(), FIRST)::get;
        check(select(List.of(new Carrier(template, null)), template, library) == null,
                "blank templates cannot produce recorded media");
        check(select(List.of(new Carrier(template, SECOND)), template, library) == null,
                "unknown audio ids cannot produce recorded media");
        check(select(List.of(new Carrier(template, FIRST)), template, ignored -> SECOND) == null,
                "library metadata for a different hash cannot replace the bound track");
    }

    private static void sourceSelectionChecksIdentityAndCount() {
        ItemRef template = source("m8-01/cut-master");
        Carrier known = new Carrier(template, FIRST);
        Carrier empty = new Carrier(new ItemRef(template.kind(), template.prefix(), template.material(),
                template.itemId(), 0, template.selector()), FIRST);
        Carrier wrongItem = new Carrier(row("m8-01/cut-master").itemInputs().get(0), FIRST);
        var selected = select(Arrays.asList(null, wrongItem, empty, new Carrier(template, null),
                new Carrier(template, SECOND), known), template, Map.of(FIRST.id(), FIRST)::get);
        check(selected != null && selected.source() == known && selected.track().equals(FIRST),
                "wrong items, empty stacks, unbound carriers and unknown hashes do not hide a valid source");
    }

    private static void everyStageUsesCanonicalTrack() {
        RecordTrack misleading = new RecordTrack(FIRST.id(), "Incorrect title", 1, 1);
        for (String label : List.of("m8-01/cut-master", "m8-01/nickel-stamper",
                "m8-01/press-vinyl", "m8-02/record-cdr")) {
            RecipeDeclaration declaration = row(label);
            ItemRef required = source(label);
            Carrier input = new Carrier(required, misleading);
            var selected = select(List.of(input), required, Map.of(FIRST.id(), FIRST)::get);
            check(selected != null && selected.track().equals(FIRST),
                    "output uses authoritative title, duration and size: " + label);
            check(selected.source() == input && input.track().equals(misleading),
                    "selection retains the exact source and does not rewrite its metadata: " + label);
            check(declaration.displayOnly(), "the static declaration cannot erase native track data: " + label);
            check(declaration.keptInputs().equals(switch (label) {
                case "m8-01/cut-master" -> List.of(1, 2);
                case "m8-01/press-vinyl" -> List.of(2);
                case "m8-02/record-cdr" -> List.of(1);
                default -> List.of();
            }), "the native provider receives the declared reusable-tool slots: " + label);
        }
    }

    private static void manufacturingChainsPreserveTrackIdentity() {
        for (RecordTrack track : List.of(FIRST, SECOND)) {
            Function<String, RecordTrack> library = Map.of(track.id(), track)::get;
            Carrier template = new Carrier(source("m8-01/cut-master"), track);
            Carrier carried = template;
            for (String label : List.of("m8-01/cut-master", "m8-01/nickel-stamper", "m8-01/press-vinyl")) {
                var selected = select(List.of(carried), source(label), library);
                check(selected != null, "the preceding output feeds the next source carrier: " + label);
                carried = new Carrier(row(label).itemOutputs().get(0), selected.track());
            }
            check(carried.item().itemId().equals("ssite:vinyl_record") && carried.track().equals(track),
                    "the full mastering and pressing chain carries the imported track into vinyl");
            var cd = select(List.of(template), source("m8-02/record-cdr"), library);
            check(cd != null && cd.track().equals(track)
                            && row("m8-02/record-cdr").itemOutputs().get(0).itemId().equals("ssite:cd_record"),
                    "the same template feeds the CD recording route");
        }
    }

    private static void independentQueriesObserveLibraryChanges() {
        ItemRef template = source("m8-01/cut-master");
        Map<String, RecordTrack> library = new HashMap<>(Map.of(FIRST.id(), FIRST, SECOND.id(), SECOND));
        var first = select(List.of(new Carrier(template, FIRST)), template, library::get);
        var second = select(List.of(new Carrier(template, SECOND)), template, library::get);
        check(first != null && second != null && first.track().equals(FIRST) && second.track().equals(SECOND),
                "successive tracks do not share mutable selection state");
        library.remove(FIRST.id());
        check(select(List.of(new Carrier(template, FIRST)), template, library::get) == null,
                "removing a library track prevents the next manufacturing lookup");
        check(first.track().equals(FIRST), "a later library change cannot rewrite an existing result");
    }

    private static RecordManufacturing.Selection<Carrier> select(List<Carrier> inputs, ItemRef required,
                                                                Function<String, RecordTrack> library) {
        return RecordManufacturing.select(inputs, carrier -> {
            ItemRef item = carrier.item();
            return item.kind() == required.kind() && item.prefix().equals(required.prefix())
                    && item.material().equals(required.material()) && item.itemId().equals(required.itemId())
                    && item.selector() == required.selector() && item.count() >= required.count();
        }, Carrier::track, library);
    }

    private static ItemRef source(String label) {
        RecipeDeclaration declaration = row(label);
        return declaration.itemInputs().get(RecordManufacturing.sourceInput(label));
    }

    private static RecipeDeclaration row(String label) {
        return ROWS.stream().filter(r -> r.label().equals(label)).findFirst().orElseThrow();
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
