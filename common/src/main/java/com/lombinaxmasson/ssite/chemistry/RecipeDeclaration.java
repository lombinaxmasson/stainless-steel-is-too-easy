package com.lombinaxmasson.ssite.chemistry;

import java.util.List;
import java.util.Objects;

/** Symbolic recipe data shared by runtime registration and build-time content reports. */
public record RecipeDeclaration(
        String map,
        String label,
        List<ItemRef> itemInputs,
        List<ItemRef> itemOutputs,
        List<FluidRef> fluidInputs,
        List<FluidRef> fluidOutputs,
        long duration,
        long perTick,
        List<Integer> outputChances,
        List<Integer> keptInputs,
        boolean displayOnly,
        List<DistinctFluids> distinctFluids,
        Source source
) {
    public RecipeDeclaration {
        Objects.requireNonNull(map);
        Objects.requireNonNull(label);
        itemInputs = List.copyOf(itemInputs);
        itemOutputs = List.copyOf(itemOutputs);
        fluidInputs = List.copyOf(fluidInputs);
        fluidOutputs = List.copyOf(fluidOutputs);
        outputChances = List.copyOf(outputChances);
        keptInputs = List.copyOf(keptInputs);
        distinctFluids = List.copyOf(distinctFluids);
        Objects.requireNonNull(source);
    }

    public enum ItemKind { MATERIAL, VANILLA, SELECTOR }

    /** Unused names are empty; non-selector references use a selector value of -1. */
    public record ItemRef(ItemKind kind, String prefix, String material, String itemId, int count, int selector) {
        public ItemRef {
            Objects.requireNonNull(kind);
            Objects.requireNonNull(prefix);
            Objects.requireNonNull(material);
            Objects.requireNonNull(itemId);
        }
    }

    public record FluidRef(String field, int amount) {
        public FluidRef {
            Objects.requireNonNull(field);
        }
    }

    /** Both fields must resolve to registered fluids with distinct identities at runtime. */
    public record DistinctFluids(String first, String second) {
        public DistinctFluids {
            Objects.requireNonNull(first);
            Objects.requireNonNull(second);
        }
    }

    public record Source(String file, int line) {
        public Source {
            Objects.requireNonNull(file);
        }
    }
}
