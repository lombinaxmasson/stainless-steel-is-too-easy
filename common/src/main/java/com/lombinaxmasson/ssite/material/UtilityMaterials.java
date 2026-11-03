package com.lombinaxmasson.ssite.material;

import gregtech.api.addon.material.MaterialDeclaration;

import java.util.List;

/** U-01 gas treatment fluids, U-03 gasification fluids plus solid residue, and the U-05a FT oil. */
public final class UtilityMaterials {
    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder("DehydratedNaturalGas")
                    .displayName("Dehydrated Natural Gas").color(0xC8D8E8).chemicalFormula("CH4·C2H6")
                    .meltingPointKelvin(90).boilingPointKelvin(130).density(0.0008F).gas().build(),
            MaterialDeclaration.builder("SweetNaturalGas")
                    .displayName("Sweet Natural Gas").color(0xD8E8D0).chemicalFormula("CH4·C2H6")
                    .meltingPointKelvin(90).boilingPointKelvin(130).density(0.0008F).gas().build(),
            MaterialDeclaration.builder("PipelineGas")
                    .displayName("Pipeline Natural Gas").color(0xE0F0D8).chemicalFormula("CH4·C2H6")
                    .meltingPointKelvin(95).boilingPointKelvin(135).density(0.00075F).gas().build(),
            MaterialDeclaration.builder("HydrogenSulfide")
                    .displayName("Hydrogen Sulfide").color(0xE8E0A0).chemicalFormula("H2S")
                    .meltingPointKelvin(191).boilingPointKelvin(213).density(0.0015F).gas().build(),
            MaterialDeclaration.builder("TriethyleneGlycol")
                    .displayName("Triethylene Glycol (TEG)").color(0xE8F0F8).chemicalFormula("C6H14O4")
                    .meltingPointKelvin(256).boilingPointKelvin(558).density(1.125F).liquid().build(),
            MaterialDeclaration.builder("RichGlycol")
                    .displayName("Water-Rich Glycol").color(0xD8E0E8).chemicalFormula("C6H14O4·H2O")
                    .meltingPointKelvin(250).boilingPointKelvin(560).density(1.14F).liquid().build(),
            MaterialDeclaration.builder("Methyldiethanolamine")
                    .displayName("Methyldiethanolamine (MDEA)").color(0xE0E8F0).chemicalFormula("C5H13NO2")
                    .meltingPointKelvin(242).boilingPointKelvin(541).density(1.04F).liquid().build(),
            MaterialDeclaration.builder("RichAmine")
                    .displayName("Hydrogen Sulfide-Loaded Amine").color(0xD8D0A8).chemicalFormula("C5H13NO2·H2S")
                    .meltingPointKelvin(240).boilingPointKelvin(540).density(1.08F).liquid().build(),
            MaterialDeclaration.builder("CrudeSyngas")
                    .displayName("Crude Syngas").color(0x6E6A5E).chemicalFormula("CO·H2·CO2·H2O")
                    .meltingPointKelvin(90).boilingPointKelvin(130).density(0.0012F).gas().build(),
            MaterialDeclaration.builder("CleanSyngas")
                    .displayName("Clean Syngas").color(0xA8C8E0).chemicalFormula("CO·H2")
                    .meltingPointKelvin(70).boilingPointKelvin(95).density(0.0009F).gas().build(),
            MaterialDeclaration.builder("CoalTar")
                    .displayName("Coal Tar").color(0x241C14).chemicalFormula("C6H6·C7H8·C6H5OH")
                    .meltingPointKelvin(250).boilingPointKelvin(480).density(1.18F).liquid().build(),
            MaterialDeclaration.builder("CoalSlag")
                    .displayName("Coal Gasification Slag").color(0x5A5650).chemicalFormula("SiO2·Al2O3·FeO")
                    .meltingPointKelvin(1500).boilingPointKelvin(2500).density(2.60F).dust().build(),
            MaterialDeclaration.builder("SyntheticOil")
                    .displayName("Fischer-Tropsch Synthetic Oil").color(0xB8863C).chemicalFormula("CnH2n+2")
                    .meltingPointKelvin(250).boilingPointKelvin(520).density(0.85F).liquid().build());

    private UtilityMaterials() {}
}
