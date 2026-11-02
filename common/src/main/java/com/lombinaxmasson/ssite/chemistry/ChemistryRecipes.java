package com.lombinaxmasson.ssite.chemistry;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import gregtech.api.addon.GregTechAddon;
import gregtech.api.addon.GregTechAddons;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.DistinctFluids;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.FluidRef;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.ItemKind;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.ItemRef;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.Source;

/**
 * Chemistry recipe declarations. GregTech registers them after its own machine recipes exist.
 * {@link RecipeRegistration} resolves symbolic items and fluids only during game startup.
 */
public final class ChemistryRecipes {
    static final List<String> REJECTED = new ArrayList<>();
    static final Set<String> MISSING = new LinkedHashSet<>();
    private static final ThreadLocal<List<RecipeDeclaration>> COLLECTOR = new ThreadLocal<>();
    private static final StackWalker SOURCE_WALKER = StackWalker.getInstance();

    private ChemistryRecipes() {}

    public static void add(GregTechAddon.Context context) {
        if (context.apiVersion() != GregTechAddons.apiVersion()) {
            throw new IllegalStateException("ssite expects GregTech addon API "
                    + GregTechAddons.apiVersion() + ", found " + context.apiVersion());
        }
        REJECTED.clear();
        MISSING.clear();
        for (RecipeDeclaration declaration : declarations()) {
            RecipeRegistration.register(declaration);
        }
        if (!MISSING.isEmpty() || !REJECTED.isEmpty()) {
            throw new IllegalStateException("ssite recipes failed; missing=" + MISSING + ", rejected=" + REJECTED);
        }
    }

    /** Executes the production recipe definitions without resolving Minecraft or host registries. */
    public static List<RecipeDeclaration> declarations() {
        List<RecipeDeclaration> previous = COLLECTOR.get();
        List<RecipeDeclaration> declarations = new ArrayList<>();
        COLLECTOR.set(declarations);
        try {
            StainlessSteelRecipes.add();
            WaterRecipes.add();
            PetrochemRecipes.add();
            PolymerRecipes.add();
            MetallurgyRecipes.add();
            RareEarthRecipes.add();
            NuclearRecipes.add();
            BaseMetalRecipes.add();
            CircuitRecipes.add();
            WaferRecipes.add();
            LithographyRecipes.add();
            PackagingRecipes.add();
            ElectrochemistryRecipes.add();
            TextileRecipes.add();
            InvestmentCastingRecipes.add();
            OpticsRecipes.add();
            PrecisionRecipes.add();
            FiberOpticRecipes.add();
            return List.copyOf(declarations);
        } finally {
            if (previous == null) COLLECTOR.remove();
            else COLLECTOR.set(previous);
        }
    }

    static void missing(String what) {
        MISSING.add(what);
    }

    /** Builder for one machine recipe or display row. */
    static final class Row {
        private final String map;
        private final String label;
        private final Source source;
        private List<ItemRef> itemInputs = List.of();
        private List<ItemRef> itemOutputs = List.of();
        private final List<FluidRef> fluidInputs = new ArrayList<>(4);
        private final List<FluidRef> fluidOutputs = new ArrayList<>(4);
        private List<Integer> outputChances = List.of();
        private List<Integer> keptInputs = List.of();
        private final List<DistinctFluids> distinctFluids = new ArrayList<>();

        Row(String map, String label, Source source) {
            this.map = map;
            this.label = label;
            this.source = source;
        }

        Row in(ItemRef... stacks) {
            this.itemInputs = List.of(stacks);
            return this;
        }

        Row out(ItemRef... stacks) {
            this.itemOutputs = List.of(stacks);
            return this;
        }

        Row fluidIn(String field, int amount) {
            fluidInputs.add(new FluidRef(field, amount));
            return this;
        }

        Row fluidIn(String f1, int a1, String f2, int a2) {
            return fluidIn(f1, a1).fluidIn(f2, a2);
        }

        Row fluidIn(String f1, int a1, String f2, int a2, String f3, int a3) {
            return fluidIn(f1, a1).fluidIn(f2, a2).fluidIn(f3, a3);
        }

        Row fluidIn(String[] fields, int[] amounts) {
            if (fields == null) return this;
            for (int i = 0; i < fields.length; i++) fluidIn(fields[i], amounts[i]);
            return this;
        }

        Row fluidOut(String field, int amount) {
            fluidOutputs.add(new FluidRef(field, amount));
            return this;
        }

        Row fluidOut(String f1, int a1, String f2, int a2) {
            return fluidOut(f1, a1).fluidOut(f2, a2);
        }

        Row fluidOut(String f1, int a1, String f2, int a2, String f3, int a3) {
            return fluidOut(f1, a1).fluidOut(f2, a2).fluidOut(f3, a3);
        }

        Row fluidOut(String[] fields, int[] amounts) {
            if (fields == null) return this;
            for (int i = 0; i < fields.length; i++) fluidOut(fields[i], amounts[i]);
            return this;
        }

        Row chances(int... chances) {
            this.outputChances = integers(chances);
            return this;
        }

        Row keep(int... slots) {
            this.keptInputs = integers(slots);
            return this;
        }

        Row requiresDistinctFluids(String first, String second) {
            distinctFluids.add(new DistinctFluids(first, second));
            return this;
        }

        void add(long duration, long perTick) {
            submit(duration, perTick, false);
        }

        void display(long duration, long perTick) {
            submit(duration, perTick, true);
        }

        private void submit(long duration, long perTick, boolean displayOnly) {
            List<RecipeDeclaration> collector = COLLECTOR.get();
            if (collector == null) {
                throw new IllegalStateException("Recipe declarations require a collection scope");
            }
            collector.add(new RecipeDeclaration(map, label, itemInputs, itemOutputs,
                    fluidInputs, fluidOutputs, duration, perTick, outputChances, keptInputs,
                    displayOnly, distinctFluids, source));
        }

        private static List<Integer> integers(int[] values) {
            List<Integer> result = new ArrayList<>(values.length);
            for (int value : values) result.add(value);
            return List.copyOf(result);
        }
    }

    static Row row(String map, String label) {
        Source source = SOURCE_WALKER.walk(frames -> frames
                .filter(frame -> !frame.getClassName().equals(ChemistryRecipes.class.getName()))
                .findFirst()
                .map(frame -> new Source("common/src/main/java/"
                        + frame.getClassName().replace('.', '/') + ".java", frame.getLineNumber()))
                .orElseGet(() -> new Source("", -1)));
        return new Row(map, label, source);
    }

    static ItemRef item(String prefix, String material, int count) {
        return new ItemRef(ItemKind.MATERIAL, prefix, material, "", count, -1);
    }

    static ItemRef dust(String material, int count) {
        return item("dust", material, count);
    }

    static ItemRef vanillaItem(String id, int count) {
        return new ItemRef(ItemKind.VANILLA, "", "", id, count, -1);
    }

    static ItemRef circuit(int n) {
        return new ItemRef(ItemKind.SELECTOR, "", "", "", 1, n);
    }

    static String[] fields(String... names) {
        return names;
    }

    static int[] amounts(int... values) {
        return values;
    }
}
