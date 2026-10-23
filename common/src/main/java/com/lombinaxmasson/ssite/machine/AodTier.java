package com.lombinaxmasson.ssite.machine;

import com.gregtech.gregtech.api.addon.GregTechMaterials;

/** One AOD converter hull material. Melting point and density are read once, the first time they resolve. */
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
    private volatile long meltingPointK;
    private volatile long heatPerKelvin;

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
        GregTechMaterials.Material found = MoltenPool.material(material);
        if (!found.valid()) return found;
        GregTechMaterials.Material canonical = found.resolve();
        return canonical == null ? GregTechMaterials.invalid() : canonical;
    }

    public long meltingPointK() {
        if (meltingPointK > 0) return meltingPointK;
        GregTechMaterials.Material resolved = resolved();
        long value = resolved.valid() ? resolved.meltingPoint() : 0;
        if (value > 0) meltingPointK = value;
        return value;
    }

    /** The hull melts down above 1.25 times its own melting point. */
    public long meltDownK() {
        long meltingPoint = meltingPointK();
        return meltingPoint <= 0 ? 0 : (long) (meltingPoint * 1.25D);
    }

    /** GregTech's crucible rule: one HU per kelvin for each whole 100 kg of a 7 U hull. */
    public long heatPerKelvin() {
        if (heatPerKelvin > 0) return heatPerKelvin;
        GregTechMaterials.Material resolved = resolved();
        double density = resolved.valid() ? resolved.density() : 1.0D;
        long value = 1L + (long) (density * 111.111111D * HULL_UNITS / 100.0D);
        if (resolved.valid()) heatPerKelvin = value;
        return value;
    }

    public static AodTier byMachineType(String machineType) {
        for (AodTier tier : values()) {
            if (tier.machineType().equals(machineType)) return tier;
        }
        return null;
    }
}
