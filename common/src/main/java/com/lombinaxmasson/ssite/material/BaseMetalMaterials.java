package com.lombinaxmasson.ssite.material;

import gregtech.api.addon.material.MaterialDeclaration;

import java.util.List;

/** M7 base-metal materials. Later M7 cards append their own intermediates here. */
public final class BaseMetalMaterials {
    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder("GreenMatte")
                    .displayName("Green Matte").color(0x3C5A3C).chemicalFormula("Cu2S·FeS")
                    .meltingPointKelvin(1400).boilingPointKelvin(1900).density(5.00F).dust().build(),
            MaterialDeclaration.builder("WhiteMatte")
                    .displayName("White Matte").color(0xC8C8D0).chemicalFormula("Cu2S·Ni3S2")
                    .meltingPointKelvin(1400).boilingPointKelvin(2100).density(5.60F).dust().build(),
            MaterialDeclaration.builder("CrudeCopper")
                    .displayName("Crude Copper").color(0xB4643C).chemicalFormula("Cu (impure)")
                    .meltingPointKelvin(1358).boilingPointKelvin(2835).density(8.50F).dust().build(),
            MaterialDeclaration.builder("CopperAnodeSlime")
                    .displayName("Copper Anode Slime").color(0x4A4038).chemicalFormula("Cu·Se·Te·Ag·Au")
                    .meltingPointKelvin(600).boilingPointKelvin(1400).density(4.20F).dust().build(),
            MaterialDeclaration.builder("PlatinumGroupConcentrate")
                    .displayName("Platinum-Group Concentrate").color(0x8A8A9A)
                    .chemicalFormula("Pt·Pd·Rh·Ir·Ru·Os")
                    .meltingPointKelvin(1800).boilingPointKelvin(3200).density(11.00F).dust().build(),
            MaterialDeclaration.builder("NickelCobaltSulfate")
                    .displayName("Nickel-Cobalt Sulfate Solution").color(0x5FA85F)
                    .chemicalFormula("NiSO4·CoSO4(aq)")
                    .meltingPointKelvin(280).boilingPointKelvin(390).density(1.40F).liquid().build(),
            MaterialDeclaration.builder("NickelSulfateSolution")
                    .displayName("Nickel Sulfate Solution").color(0x6FDEDE).chemicalFormula("NiSO4(aq)")
                    .meltingPointKelvin(280).boilingPointKelvin(390).density(1.35F).liquid().build(),
            MaterialDeclaration.builder("CobaltSulfateSolution")
                    .displayName("Cobalt Sulfate Solution").color(0xDE4242).chemicalFormula("CoSO4(aq)")
                    .meltingPointKelvin(280).boilingPointKelvin(390).density(1.35F).liquid().build(),
            MaterialDeclaration.builder("CobaltP204Extract")
                    .displayName("Cobalt-Loaded P204 Organic").color(0xC07060)
                    .chemicalFormula("CoSO4·P204")
                    .meltingPointKelvin(250).boilingPointKelvin(400).density(0.97F).liquid().build(),
            MaterialDeclaration.builder("SeleniumDioxide")
                    .displayName("Selenium Dioxide").color(0xC8B890).chemicalFormula("SeO2")
                    .meltingPointKelvin(588).boilingPointKelvin(700).density(0.0036F).gas().build(),
            MaterialDeclaration.builder("TelluriumDioxide")
                    .displayName("Tellurium Dioxide").color(0x8A7A40).chemicalFormula("TeO2")
                    .meltingPointKelvin(1006).boilingPointKelvin(1512).density(5.67F).dust().build(),
            MaterialDeclaration.builder("SilverGoldResidue")
                    .displayName("Silver-Gold Residue").color(0xC8B860).chemicalFormula("Ag·Au")
                    .meltingPointKelvin(1200).boilingPointKelvin(2500).density(12.00F).dust().build(),
            MaterialDeclaration.builder("SilverNitrateSolution")
                    .displayName("Silver Nitrate Solution").color(0xD8D8E8).chemicalFormula("AgNO3(aq)")
                    .meltingPointKelvin(250).boilingPointKelvin(400).density(1.59F).liquid().build(),
            MaterialDeclaration.builder("CrudeGold")
                    .displayName("Crude Gold").color(0xD8B840).chemicalFormula("Au (impure)")
                    .meltingPointKelvin(1337).boilingPointKelvin(3129).density(18.00F).dust().build(),
            MaterialDeclaration.builder("AmmoniumChlorideSolution")
                    .displayName("Ammonium Chloride Solution").color(0xE8E8F0).chemicalFormula("NH4Cl(aq)")
                    .meltingPointKelvin(250).boilingPointKelvin(380).density(1.15F).liquid().build(),
            MaterialDeclaration.builder("PGMChlorideLiquor")
                    .displayName("Platinum-Group Chloride Liquor").color(0x9A8A6A)
                    .chemicalFormula("Pt·Pd·Rh·Ir·Ru·Os-Cl")
                    .meltingPointKelvin(280).boilingPointKelvin(400).density(1.50F).liquid().build(),
            MaterialDeclaration.builder("OsmiumTetroxide")
                    .displayName("Osmium Tetroxide").color(0xC8E8F0).chemicalFormula("OsO4")
                    .meltingPointKelvin(313).boilingPointKelvin(403).density(4.90F).liquid().build(),
            MaterialDeclaration.builder("RutheniumTetroxide")
                    .displayName("Ruthenium Tetroxide").color(0xE8D070).chemicalFormula("RuO4")
                    .meltingPointKelvin(298).boilingPointKelvin(340).density(3.29F).liquid().build(),
            MaterialDeclaration.builder("OsmiumFreePGMLiquor")
                    .displayName("Osmium-Free Platinum-Group Liquor").color(0xA8987A)
                    .chemicalFormula("Pt·Pd·Rh·Ir·Ru-Cl")
                    .meltingPointKelvin(280).boilingPointKelvin(400).density(1.48F).liquid().build(),
            MaterialDeclaration.builder("PGMPreciousLiquor")
                    .displayName("Ruthenium-Free Platinum-Group Liquor").color(0xB8A888)
                    .chemicalFormula("Pt·Pd·Rh·Ir-Cl")
                    .meltingPointKelvin(280).boilingPointKelvin(400).density(1.46F).liquid().build(),
            MaterialDeclaration.builder("PalladiumLiquor")
                    .displayName("Palladium-Rich Liquor").color(0xC8B898)
                    .chemicalFormula("Pd·Rh·Ir-Cl")
                    .meltingPointKelvin(280).boilingPointKelvin(400).density(1.44F).liquid().build(),
            MaterialDeclaration.builder("RhodiumIridiumLiquor")
                    .displayName("Rhodium-Iridium Liquor").color(0xD0C0A8)
                    .chemicalFormula("Rh·Ir-Cl")
                    .meltingPointKelvin(280).boilingPointKelvin(400).density(1.42F).liquid().build(),
            MaterialDeclaration.builder("AmmoniumChloroplatinate")
                    .displayName("Ammonium Hexachloroplatinate").color(0xE8C860)
                    .chemicalFormula("(NH4)2PtCl6")
                    .meltingPointKelvin(500).boilingPointKelvin(800).density(3.06F).dust().build(),
            MaterialDeclaration.builder("DiamminePalladiumChloride")
                    .displayName("Diammine Palladium(II) Chloride").color(0xE0D0A0)
                    .chemicalFormula("Pd(NH3)2Cl2")
                    .meltingPointKelvin(450).boilingPointKelvin(700).density(2.50F).dust().build(),
            MaterialDeclaration.builder("AmmoniumHexachloroiridate")
                    .displayName("Ammonium Hexachloroiridate").color(0xC8B0D8)
                    .chemicalFormula("(NH4)2IrCl6")
                    .meltingPointKelvin(550).boilingPointKelvin(900).density(3.10F).dust().build(),
            MaterialDeclaration.builder("RhodiumChlorideSolution")
                    .displayName("Rhodium Chloride Solution").color(0xC86060).chemicalFormula("RhCl3(aq)")
                    .meltingPointKelvin(280).boilingPointKelvin(400).density(1.40F).liquid().build()
    );

    private BaseMetalMaterials() {}
}
