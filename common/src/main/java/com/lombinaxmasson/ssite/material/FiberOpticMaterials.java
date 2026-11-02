package com.lombinaxmasson.ssite.material;

import gregtech.api.addon.material.MaterialDeclaration;
import gregtech.api.addon.material.PrefixDeclaration;
import gregtech.api.material.GTValues;

import java.util.List;
import java.util.Set;

/**
 * P-02 optical fiber materials.
 *
 * <p>The glass batch is a game abstraction of silica and germania; the preform, bare fiber,
 * coated fiber and spooled fiber are separate prefixes on one carrier material so they keep
 * distinct item ids without generating fiber forms on every material. The coating reuses the
 * existing silicone rubber and the spool flange reuses 316L stainless steel.</p>
 */
public final class FiberOpticMaterials {
    public static final String CARRIER = "OpticalFiber";

    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder("OpticalGlassFrit")
                    .displayName("Optical Glass Frit").color(0xD8E8F0).chemicalFormula("SiO2·GeO2")
                    .meltingPointKelvin(1700).boilingPointKelvin(2200).density(2.2F).dust().build(),
            MaterialDeclaration.builder("OpticalGlassScrap")
                    .displayName("Optical Glass Scrap").color(0xA8C0C8).chemicalFormula("SiO2·GeO2")
                    .meltingPointKelvin(1700).boilingPointKelvin(2200).density(2.2F).dust().build(),
            MaterialDeclaration.builder(CARRIER)
                    .displayName("Optical Fiber").color(0xE8F8FF).chemicalFormula("SiO2·GeO2")
                    .meltingPointKelvin(1700).boilingPointKelvin(2200).density(2.2F).build());

    /**
     * The preform carries the full ten-unit glass batch. Drawing keeps 80% as bare fiber and
     * reports the remaining 20% as scrap; coated fiber and the spool keep the glass core unit,
     * while the polymer coating and the steel flange are tracked as separate consumables.
     * Item textures reuse the existing boule, rod, hose and wafer icons; no new art is introduced.
     */
    public static final List<PrefixDeclaration> PREFIXES = List.of(
            new PrefixDeclaration("fiberPreform", "", " Preform", GTValues.U * 10,
                    "item/crystal_boule", List.of(CARRIER), Set.of()),
            new PrefixDeclaration("bareFiber", "Bare ", "", GTValues.U,
                    "item/laser_rod", List.of(CARRIER), Set.of()),
            new PrefixDeclaration("coatedFiber", "Coated ", "", GTValues.U,
                    "item/hose", List.of(CARRIER), Set.of()),
            new PrefixDeclaration("fiberSpool", "Spooled ", "", GTValues.U * 8,
                    "item/wafer", List.of(CARRIER), Set.of()));

    private FiberOpticMaterials() {}
}
