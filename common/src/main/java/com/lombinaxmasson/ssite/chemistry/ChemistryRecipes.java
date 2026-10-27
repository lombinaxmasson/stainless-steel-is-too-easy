package com.lombinaxmasson.ssite.chemistry;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import gregtech.api.addon.GregTechAddon;
import gregtech.api.addon.GregTechAddons;
import gregtech.api.addon.platform.AddonItems;
import gregtech.api.addon.platform.AddonRecipes;
import net.minecraft.world.item.ItemStack;

/**
 * Chemistry recipe changes. GregTech calls this after its own machine recipes exist.
 * Fluid stacks differ by loader, so {@link FluidBridge} packs them.
 */
public final class ChemistryRecipes {
    static final List<String> REJECTED = new ArrayList<>();
    static final Set<String> MISSING = new LinkedHashSet<>();

    private ChemistryRecipes() {}

    public static void add(GregTechAddon.Context context) {
        if (context.apiVersion() != GregTechAddons.apiVersion()) {
            throw new IllegalStateException("ssite expects GregTech addon API "
                    + GregTechAddons.apiVersion() + ", found " + context.apiVersion());
        }
        REJECTED.clear();
        MISSING.clear();
        StainlessSteelRecipes.add();
        WaterRecipes.add();
        PetrochemRecipes.add();
        PolymerRecipes.add();
        MetallurgyRecipes.add();
        RareEarthRecipes.add();
        NuclearRecipes.add();
        BaseMetalRecipes.add();
        if (!MISSING.isEmpty() || !REJECTED.isEmpty()) {
            throw new IllegalStateException("ssite recipes failed; missing=" + MISSING + ", rejected=" + REJECTED);
        }
    }

    static void missing(String what) {
        MISSING.add(what);
    }

    /** Builder for one machine recipe or display row. */
    static final class Row {
        private final String map;
        private final String label;
        private ItemStack[] itemInputs;
        private ItemStack[] itemOutputs;
        private final List<String> fluidInFields = new ArrayList<>(4);
        private final List<Integer> fluidInAmounts = new ArrayList<>(4);
        private final List<String> fluidOutFields = new ArrayList<>(4);
        private final List<Integer> fluidOutAmounts = new ArrayList<>(4);
        private int[] outputChances;
        private int[] keptInputs;

        Row(String map, String label) {
            this.map = map;
            this.label = label;
        }

        Row in(ItemStack... stacks) {
            this.itemInputs = stacks;
            return this;
        }

        Row out(ItemStack... stacks) {
            this.itemOutputs = stacks;
            return this;
        }

        Row fluidIn(String field, int amount) {
            fluidInFields.add(field);
            fluidInAmounts.add(amount);
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
            fluidOutFields.add(field);
            fluidOutAmounts.add(amount);
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
            this.outputChances = chances;
            return this;
        }

        Row keep(int... slots) {
            this.keptInputs = slots;
            return this;
        }

        void add(long duration, long perTick) {
            submit(duration, perTick, false);
        }

        void display(long duration, long perTick) {
            submit(duration, perTick, true);
        }

        private void submit(long duration, long perTick, boolean displayOnly) {
            AddonRecipes.Line built = FluidBridge.line(
                    itemInputs, itemOutputs,
                    toNames(fluidInFields), toInts(fluidInAmounts),
                    toNames(fluidOutFields), toInts(fluidOutAmounts),
                    duration, perTick, outputChances, keptInputs);
            if (built == null) {
                REJECTED.add(label);
                return;
            }
            AddonRecipes.Map table = AddonRecipes.map(map);
            AddonRecipes.Line added = displayOnly ? table.addDisplay(built) : table.add(built);
            if (added == null) REJECTED.add(label);
        }

        private static String[] toNames(List<String> list) {
            return list.isEmpty() ? null : list.toArray(String[]::new);
        }

        private static int[] toInts(List<Integer> list) {
            if (list.isEmpty()) return null;
            int[] array = new int[list.size()];
            for (int i = 0; i < list.size(); i++) array[i] = list.get(i);
            return array;
        }
    }

    static Row row(String map, String label) {
        return new Row(map, label);
    }

    static ItemStack item(String prefix, String material, int count) {
        ItemStack stack = AddonItems.stack(prefix, material, count);
        if (stack.isEmpty()) {
            missing(prefix + " " + material);
        }
        return stack;
    }

    static ItemStack dust(String material, int count) {
        return item("dust", material, count);
    }

    static ItemStack circuit(int n) {
        return AddonItems.selectorTag(n);
    }

    static String[] fields(String... names) {
        return names;
    }

    static int[] amounts(int... values) {
        return values;
    }
}
