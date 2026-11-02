package com.lombinaxmasson.ssite.material;

import gregtech.api.addon.material.MaterialDeclaration;

import java.util.List;

/** M6 nuclear hydrometallurgy materials, the N-01 thorium fuel intermediates and the N-02 spent batches. */
public final class NuclearMaterials {
    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder("ThoriumConcentrate")
                    .displayName("Thorium Hydroxide Concentrate").color(0x606060)
                    .chemicalFormula("Th(OH)4")
                    .meltingPointKelvin(800).boilingPointKelvin(1800).density(4.20F).dust().build(),
            MaterialDeclaration.builder("UranylSulfateSolution")
                    .displayName("Crude Uranyl Sulfate Solution").color(0xC8E860)
                    .chemicalFormula("UO2SO4(aq)")
                    .meltingPointKelvin(290).boilingPointKelvin(390).density(1.60F).liquid().build(),
            MaterialDeclaration.builder("Yellowcake")
                    .displayName("Yellowcake (Ammonium Diuranate)").color(0xE8C828)
                    .chemicalFormula("(NH4)2U2O7")
                    .meltingPointKelvin(600).boilingPointKelvin(1500).density(4.80F).dust().build(),
            MaterialDeclaration.builder("UranylNitrateSolution")
                    .displayName("Crude Uranyl Nitrate Solution").color(0xD8E858)
                    .chemicalFormula("UO2(NO3)2(aq)")
                    .meltingPointKelvin(290).boilingPointKelvin(400).density(1.55F).liquid().build(),
            MaterialDeclaration.builder("PureUranylNitrate")
                    .displayName("Nuclear-Grade Uranyl Nitrate").color(0xE8F878)
                    .chemicalFormula("UO2(NO3)2(aq)")
                    .meltingPointKelvin(290).boilingPointKelvin(400).density(1.55F).liquid().build(),
            MaterialDeclaration.builder("TbpUraniumExtract")
                    .displayName("Uranium-Loaded TBP Extract").color(0xC8C070)
                    .chemicalFormula("UO2(NO3)2·TBP")
                    .meltingPointKelvin(210).boilingPointKelvin(400).density(0.99F).liquid().build(),
            MaterialDeclaration.builder("UraniumDioxide")
                    .displayName("Uranium Dioxide").color(0x303830).chemicalFormula("UO2")
                    .meltingPointKelvin(3120).boilingPointKelvin(4400).density(10.97F).dust().build(),
            MaterialDeclaration.builder("ThoriumNitrateSolution")
                    .displayName("Crude Thorium Nitrate Solution").color(0xD0D0B8)
                    .chemicalFormula("Th(NO3)4(aq)")
                    .meltingPointKelvin(290).boilingPointKelvin(400).density(1.60F).liquid().build(),
            MaterialDeclaration.builder("PureThoriumNitrate")
                    .displayName("Nuclear-Grade Thorium Nitrate").color(0xE0E0C8)
                    .chemicalFormula("Th(NO3)4(aq)")
                    .meltingPointKelvin(290).boilingPointKelvin(400).density(1.60F).liquid().build(),
            MaterialDeclaration.builder("TbpThoriumExtract")
                    .displayName("Thorium-Loaded TBP Extract").color(0xB8B088)
                    .chemicalFormula("Th(NO3)4·TBP")
                    .meltingPointKelvin(210).boilingPointKelvin(400).density(1.00F).liquid().build(),
            MaterialDeclaration.builder("ThoriumDioxide")
                    .displayName("Thorium Dioxide").color(0x585850).chemicalFormula("ThO2")
                    .meltingPointKelvin(3390).boilingPointKelvin(4400).density(10.00F).dust().build(),
            // Pressed and sintered fuel bodies stay dust-only so they cannot be cast or
            // rolled as ordinary metal; the host has no usable pellet prefix for addon forms.
            MaterialDeclaration.builder("ThoriumFuelCompact")
                    .displayName("Thorium Fuel Green Compact").color(0x5C5C50).chemicalFormula("ThO2")
                    .meltingPointKelvin(3390).boilingPointKelvin(4400).density(10.00F).dust().build(),
            MaterialDeclaration.builder("ThoriumFuelPellet")
                    .displayName("Sintered Thorium Fuel Pellet").color(0x484840).chemicalFormula("ThO2")
                    .meltingPointKelvin(3390).boilingPointKelvin(4400).density(10.00F).dust().build(),
            // Spent and residue materials stay dust-only and opt out of host decomposition:
            // without a smelting target or CENTRIFUGE/ELECTROLYZER they cannot return to fuel.
            MaterialDeclaration.builder("SpentThoriumFuel")
                    .displayName("Spent Thorium-232 Fuel Batch").color(0x4A4438).chemicalFormula("ThO2+FP")
                    .meltingPointKelvin(3390).boilingPointKelvin(4400).density(9.60F).dust().build(),
            MaterialDeclaration.builder("FissionProductResidue")
                    .displayName("Fission Product Residue").color(0x5C4530).chemicalFormula("FP")
                    .meltingPointKelvin(1200).boilingPointKelvin(2400).density(4.60F).dust().build(),
            MaterialDeclaration.builder("SolidifiedFissionResidue")
                    .displayName("Solidified Fission Product Residue").color(0x34342E).chemicalFormula("FP·SiO2")
                    .meltingPointKelvin(1200).boilingPointKelvin(2400).density(2.60F).dust().build()
    );

    private NuclearMaterials() {}
}
