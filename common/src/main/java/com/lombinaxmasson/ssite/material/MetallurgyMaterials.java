package com.lombinaxmasson.ssite.material;

import com.gregtech.gregtech.api.addon.MaterialDeclaration;

import java.util.List;

/** M5 metallurgy materials. Each M5 card appends its own entries here. */
public final class MetallurgyMaterials {
    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder("RedMud")
                    .displayName("Red Mud").color(0x8B3A2B).chemicalFormula("Fe2O3·TiO2·SiO2")
                    .meltingPointKelvin(1800).boilingPointKelvin(3000).density(2.50F).dust().build(),
            MaterialDeclaration.builder("BayerLiquor")
                    .displayName("Bayer Liquor").color(0xC8D8B0).chemicalFormula("NaAlO2·NaGaO2(aq)")
                    .meltingPointKelvin(265).boilingPointKelvin(385).density(1.25F).liquid().build(),
            MaterialDeclaration.builder("GalliumHydroxide")
                    .displayName("Gallium Hydroxide").color(0xE8ECF5).chemicalFormula("Ga(OH)3")
                    .meltingPointKelvin(713).boilingPointKelvin(1500).density(3.67F).dust().build(),
            MaterialDeclaration.builder("TitaniumSponge")
                    .displayName("Titanium Sponge").color(0xB898C8).chemicalFormula("Ti")
                    .meltingPointKelvin(1941).boilingPointKelvin(3560).density(4.54F).dust().build(),
            MaterialDeclaration.builder("ZirconiumSponge")
                    .displayName("Zirconium Sponge").color(0xB0D8D8).chemicalFormula("Zr")
                    .meltingPointKelvin(2128).boilingPointKelvin(4682).density(6.51F).dust().build(),
            MaterialDeclaration.builder("HafniumSponge")
                    .displayName("Hafnium Sponge").color(0x7C8088).chemicalFormula("Hf")
                    .meltingPointKelvin(2506).boilingPointKelvin(4876).density(13.31F).dust().build(),
            MaterialDeclaration.builder("CrudeTitaniumTetrachloride")
                    .displayName("Crude Titanium Tetrachloride").color(0xC8A858).chemicalFormula("TiCl4·VOCl3")
                    .meltingPointKelvin(249).boilingPointKelvin(409).density(1.73F).liquid().build(),
            MaterialDeclaration.builder("VanadiumOxytrichloride")
                    .displayName("Vanadium Oxytrichloride").color(0xE0A828).chemicalFormula("VOCl3")
                    .meltingPointKelvin(194).boilingPointKelvin(400).density(1.83F).liquid().build(),
            MaterialDeclaration.builder("CrudeZirconiumTetrachloride")
                    .displayName("Crude Zirconium Tetrachloride").color(0xE8E4D0).chemicalFormula("(Zr,Hf)Cl4")
                    .meltingPointKelvin(604).boilingPointKelvin(604).density(2.80F).dust().build(),
            MaterialDeclaration.builder("SiliconTetrachloride")
                    .displayName("Silicon Tetrachloride").color(0xDCE8E8).chemicalFormula("SiCl4")
                    .meltingPointKelvin(204).boilingPointKelvin(330).density(1.48F).liquid().build(),
            MaterialDeclaration.builder("ZirconylChlorideSolution")
                    .displayName("Zirconyl Chloride Solution").color(0xD0E8F0).chemicalFormula("(Zr,Hf)OCl2(aq)")
                    .meltingPointKelvin(265).boilingPointKelvin(380).density(1.35F).liquid().build(),
            MaterialDeclaration.builder("HafniumExtract")
                    .displayName("Hafnium-Rich MIBK Extract").color(0xD8C898).chemicalFormula("HfOCl2·MIBK")
                    .meltingPointKelvin(200).boilingPointKelvin(395).density(0.95F).liquid().build(),
            MaterialDeclaration.builder("Zirconia")
                    .displayName("Zirconium Dioxide").color(0xF4F6F8).chemicalFormula("ZrO2")
                    .meltingPointKelvin(2988).boilingPointKelvin(4573).density(5.68F).dust().build(),
            MaterialDeclaration.builder("Hafnia")
                    .displayName("Hafnium Dioxide").color(0xD8DCE0).chemicalFormula("HfO2")
                    .meltingPointKelvin(3031).boilingPointKelvin(5400).density(9.68F).dust().build(),
            MaterialDeclaration.builder("ZirconiumTetrachloride")
                    .displayName("Pure Zirconium Tetrachloride").color(0xF0F4F8).chemicalFormula("ZrCl4")
                    .meltingPointKelvin(604).boilingPointKelvin(604).density(2.80F).dust().build(),
            MaterialDeclaration.builder("HafniumTetrachloride")
                    .displayName("Hafnium Tetrachloride").color(0xD0D8E0).chemicalFormula("HfCl4")
                    .meltingPointKelvin(705).boilingPointKelvin(705).density(3.89F).dust().build(),
            MaterialDeclaration.builder("NbTaLeachLiquor")
                    .displayName("Fluoroniobic-Tantalic Leach Liquor").color(0x98B890)
                    .chemicalFormula("H2NbOF5·H2TaF7(aq)")
                    .meltingPointKelvin(260).boilingPointKelvin(385).density(1.42F).liquid().build(),
            MaterialDeclaration.builder("NbTaMibkExtract")
                    .displayName("Loaded Nb-Ta MIBK Extract").color(0xB8A878).chemicalFormula("(Nb,Ta)F·MIBK")
                    .meltingPointKelvin(200).boilingPointKelvin(395).density(0.98F).liquid().build(),
            MaterialDeclaration.builder("TantalumRichMibk")
                    .displayName("Tantalum-Rich MIBK Phase").color(0xC89868).chemicalFormula("H2TaF7·MIBK")
                    .meltingPointKelvin(200).boilingPointKelvin(395).density(0.96F).liquid().build()
    );

    private MetallurgyMaterials() {}
}
