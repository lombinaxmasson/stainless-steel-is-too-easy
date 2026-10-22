package com.lombinaxmasson.ssite.machine;

import com.gregtech.gregtech.api.addon.GregTechMaterials;

/** One AOD converter hull material. Melting point and density are read when a machine is placed. */
public enum AodTier {
    STEEL("steel", "Steel"),
    STAINLESS_STEEL("stainless_steel", "StainlessSteel"),
    NIOBIUM_TITANIUM("niobium_titanium", "NiobiumTitanium"),
    TUNGSTEN_STEEL("tungsten_steel", "TungstenSteel"),
    TUNGSTEN("tungsten", "Tungsten"),
    TA4HFC5("ta4hfc5", "TantalumHafniumCarbide");

    private static final double HULL_UNITS = 7.0D;

    private final String suffix;
    private final String material;

    AodTier(String suffix, String material) {
        this.suffix = suffix;
        this.material = material;
    }

    public String suffix() {
        return suffix;
    }

    public String machineType() {
        return "aod_converter_" + suffix;
    }

    public String material() {
        return material;
    }

    private GregTechMaterials.Material resolved() {
        GregTechMaterials.Material found = GregTechMaterials.find(material);
        if (found == null) return GregTechMaterials.invalid();
        GregTechMaterials.Material canonical = found.resolve();
        return canonical == null ? GregTechMaterials.invalid() : canonical;
    }

    public long meltingPointK() {
        GregTechMaterials.Material resolved = resolved();
        return resolved.valid() ? resolved.meltingPoint() : 0;
    }

    /** The hull melts down above 1.25 times its own melting point. */
    public long meltDownK() {
        long meltingPoint = meltingPointK();
        return meltingPoint <= 0 ? 0 : (long) (meltingPoint * 1.25D);
    }

    /** GregTech's crucible rule: one HU per kelvin for each whole 100 kg of a 7 U hull. */
    public long heatPerKelvin() {
        GregTechMaterials.Material resolved = resolved();
        double density = resolved.valid() ? resolved.density() : 1.0D;
        double kilograms = density * 111.111111D * HULL_UNITS;
        return 1L + (long) (kilograms / 100.0D);
    }

    public static AodTier byMachineType(String machineType) {
        for (AodTier tier : values()) {
            if (tier.machineType().equals(machineType)) return tier;
        }
        return null;
    }
}
