package com.lombinaxmasson.ssite.material;

import gregtech.api.addon.material.MaterialDeclaration;
import gregtech.api.addon.material.PrefixDeclaration;
import gregtech.api.material.GTValues;

import java.util.List;
import java.util.Set;

/**
 * M9-04/M9-05 semiconductor materials plus the crystal forms the host cannot generate for addons.
 *
 * <p>{@code bouleGt} and {@code plateGem} require the BOULE or GEM property, and
 * {@code AddonMaterials.publishForms} rejects a form whose prefix is not valid for the material.
 * Addon declarations cannot set those properties, so crystal boules and wafers use addon prefixes.
 * Silicon keeps the host {@code bouleGt} boule because the host material already has one.</p>
 *
 * <p>{@code PhotomaskPlate} declares {@code polymer()} only to unlock the generated {@code plate}
 * form: {@code explicitPlateOrFoil} accepts plate/foil requests for polymer-flagged addon materials,
 * and the feature has no other effect. The mask is a glass blank with a chromium film.</p>
 */
public final class SemiconductorMaterials {
    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder("Trichlorosilane")
                    .displayName("Trichlorosilane").color(0xD8E8E0).chemicalFormula("SiHCl3")
                    .meltingPointKelvin(134).boilingPointKelvin(304).density(1.34F).liquid().build(),
            MaterialDeclaration.builder("ElectronicGradeSilicon")
                    .displayName("Electronic-Grade Polysilicon").color(0x6E7480).chemicalFormula("Si")
                    .meltingPointKelvin(1687).boilingPointKelvin(3538).density(2.33F).dust().build(),
            MaterialDeclaration.builder("GalliumArsenide")
                    .displayName("Gallium Arsenide").color(0x7A8A9A).chemicalFormula("GaAs")
                    .meltingPointKelvin(1511).boilingPointKelvin(2743).density(5.32F).dust().build(),
            MaterialDeclaration.builder("IndiumPhosphide")
                    .displayName("Indium Phosphide").color(0x9A9AA8).chemicalFormula("InP")
                    .meltingPointKelvin(1338).boilingPointKelvin(2533).density(4.81F).dust().build(),
            MaterialDeclaration.builder("Photoresist")
                    .displayName("Photoresist").color(0xB07A3A).chemicalFormula("(C10H8O)n·PAG")
                    .meltingPointKelvin(300).boilingPointKelvin(420).density(1.10F).liquid().build(),
            MaterialDeclaration.builder("PhotoresistDeveloper")
                    .displayName("Photoresist Developer").color(0xD8F0E8).chemicalFormula("NH4OH·H2O")
                    .meltingPointKelvin(273).boilingPointKelvin(373).density(1.00F).liquid().build(),
            MaterialDeclaration.builder("BufferedHydrofluoricAcid")
                    .displayName("Buffered Hydrofluoric Acid").color(0x9FE0D8).chemicalFormula("HF·NaF")
                    .meltingPointKelvin(250).boilingPointKelvin(380).density(1.15F).liquid().build(),
            MaterialDeclaration.builder("PotassiumHydroxideSolution")
                    .displayName("Potassium Hydroxide Solution").color(0xE0D0F0).chemicalFormula("KOH·H2O")
                    .meltingPointKelvin(273).boilingPointKelvin(390).density(1.20F).liquid().build(),
            MaterialDeclaration.builder("BoronTrioxide")
                    .displayName("Boron Trioxide").color(0xF0F0E0).chemicalFormula("B2O3")
                    .meltingPointKelvin(723).boilingPointKelvin(2100).density(2.46F).dust().build(),
            MaterialDeclaration.builder("PhotomaskPlate")
                    .displayName("Photomask").color(0x3C3C48).chemicalFormula("SiO2·Cr")
                    .meltingPointKelvin(1983).boilingPointKelvin(2500).density(2.65F).polymer().build(),
            MaterialDeclaration.builder("ProcessedWafer")
                    .displayName("Processed").color(0x7A8A96).chemicalFormula("Si")
                    .meltingPointKelvin(1687).boilingPointKelvin(3538).density(2.33F).build());

    /** One boule keeps the host boule weight (four material units); one wafer is half a unit. */
    public static final List<PrefixDeclaration> PREFIXES = List.of(
            new PrefixDeclaration("crystalBoule", "", " Boule", GTValues.U * 4,
                    "item/crystal_boule", List.of("GalliumArsenide", "IndiumPhosphide"), Set.of()),
            new PrefixDeclaration("wafer", "", " Wafer", GTValues.U / 2,
                    "item/wafer", List.of("Silicon", "GalliumArsenide", "IndiumPhosphide", "ProcessedWafer"),
                    Set.of()));

    private SemiconductorMaterials() {}
}
