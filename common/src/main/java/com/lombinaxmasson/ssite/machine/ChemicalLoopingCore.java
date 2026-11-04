package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.material.GregTechMaterials;
import gregtech.api.addon.platform.AddonItems;
import gregtech.api.material.GTValues;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Shared carrier and fuel vocabulary for the two chemical-looping beds. The host already owns
 * both iron oxides, so no new carrier material is introduced; heat is split between the beds so
 * that the two batches together match the host fuel value instead of multiplying it.
 */
public final class ChemicalLoopingCore {
    public static final String FUEL_TYPE = "fuel_reactor", AIR_TYPE = "air_reactor";
    private static final ResourceLocation AIR_VENT_ID = ResourceLocation.tryBuild("gt6ce", "air_vent");

    /** Charcoal and coke batches differ only in heat and ash; the stoichiometry is identical. */
    public enum FuelKind {
        CHARCOAL(24_000, "Ashes"), COKE(64_000, "DarkAshes");
        private final long heat;
        private final String ash;
        FuelKind(long heat, String ash) { this.heat = heat; this.ash = ash; }
        public long heat() { return heat; }
        public String ash() { return ash; }
    }

    private ChemicalLoopingCore() {}

    public static boolean isMachine(String type) {
        return FUEL_TYPE.equals(type) || AIR_TYPE.equals(type);
    }

    public static boolean isHematite(ItemStack stack) {
        return isDust(stack, "Hematite");
    }

    public static boolean isMagnetite(ItemStack stack) {
        return isDust(stack, "Magnetite");
    }

    private static boolean isDust(ItemStack stack, String material) {
        GregTechMaterials.StackComposition composition = AddonItems.composition(stack);
        return composition.present() && "dust".equals(composition.prefix())
                && composition.units().size() == 1
                && material.equals(composition.units().get(0).material())
                && composition.units().get(0).amount() == GTValues.U;
    }

    /**
     * Only single-unit processed carbon enters the beds. Vanilla charcoal is one material unit;
     * coal, blocks and multi-unit gems are refused so a batch always consumes exactly one unit.
     */
    public static FuelKind fuelKind(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        if (stack.is(Items.CHARCOAL)) return FuelKind.CHARCOAL;
        GregTechMaterials.StackComposition composition = AddonItems.composition(stack);
        if (!composition.present() || composition.units().size() != 1) return null;
        if (!"dust".equals(composition.prefix()) && !"gem".equals(composition.prefix())) return null;
        if (composition.units().get(0).amount() != GTValues.U) return null;
        return switch (composition.units().get(0).material()) {
            case "Charcoal" -> FuelKind.CHARCOAL;
            case "Coke", "CoalCoke" -> FuelKind.COKE;
            default -> null;
        };
    }

    public static ItemStack hematite() {
        return AddonItems.stack("dust", "Hematite", 1);
    }

    public static ItemStack magnetite() {
        return AddonItems.stack("dust", "Magnetite", 1);
    }

    public static ItemStack ash(FuelKind kind) {
        return kind == null ? ItemStack.EMPTY : AddonItems.stack("dustTiny", kind.ash(), 1);
    }

    public static boolean isAirVent(ItemStack stack) {
        return stack != null && !stack.isEmpty() && AIR_VENT_ID != null
                && AIR_VENT_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static ItemStack airVent() {
        Item item = AIR_VENT_ID == null ? Items.AIR : BuiltInRegistries.ITEM.get(AIR_VENT_ID);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    static boolean sameStack(ItemStack first, ItemStack second) {
        return ChemicalLoopingBehavior.sameStack(first, second);
    }

    static boolean grow(ItemStack existing, ItemStack addition) {
        return existing.isEmpty() || sameStack(existing, addition)
                && existing.getCount() + addition.getCount() <= existing.getMaxStackSize();
    }

    static ItemStack merged(ItemStack existing, ItemStack addition) {
        if (existing.isEmpty()) return addition.copy();
        ItemStack merged = existing.copy();
        merged.grow(addition.getCount());
        return merged;
    }
}
