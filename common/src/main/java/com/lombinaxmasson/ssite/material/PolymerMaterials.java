package com.lombinaxmasson.ssite.material;

import com.gregtech.gregtech.api.addon.MaterialDeclaration;
import com.gregtech.gregtech.api.addon.MaterialFormRequest;

import java.util.List;

/** M4 polymer materials, registered on top of {@link SsiteMaterials}. Each M4 card appends its own materials here. */
public final class PolymerMaterials {
    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder("Methanol")
                    .displayName("Methanol").color(0xD8E8F0).chemicalFormula("CH3OH")
                    .meltingPointKelvin(175).boilingPointKelvin(338).density(0.792F).liquid().build(),
            MaterialDeclaration.builder("Formaldehyde")
                    .displayName("Formaldehyde").color(0xE8F0E0).chemicalFormula("CH2O")
                    .meltingPointKelvin(181).boilingPointKelvin(254).density(0.00082F).gas().build(),
            MaterialDeclaration.builder("AceticAcid")
                    .displayName("Glacial Acetic Acid").color(0xE8E0C8).chemicalFormula("CH3COOH")
                    .meltingPointKelvin(290).boilingPointKelvin(391).density(1.049F).liquid().build(),
            MaterialDeclaration.builder("EthyleneOxide")
                    .displayName("Ethylene Oxide").color(0xE0E8D8).chemicalFormula("C2H4O")
                    .meltingPointKelvin(161).boilingPointKelvin(284).density(0.0018F).gas().build(),
            MaterialDeclaration.builder("EthyleneGlycol")
                    .displayName("Ethylene Glycol").color(0xD0E0F0).chemicalFormula("C2H6O2")
                    .meltingPointKelvin(260).boilingPointKelvin(470).density(1.11F).liquid().build(),
            MaterialDeclaration.builder("Dichloroethane")
                    .displayName("1,2-Dichloroethane").color(0xD8E8C0).chemicalFormula("C2H4Cl2")
                    .meltingPointKelvin(238).boilingPointKelvin(357).density(1.25F).liquid().build(),
            MaterialDeclaration.builder("VinylChloride")
                    .displayName("Vinyl Chloride").color(0xE0F0C8).chemicalFormula("C2H3Cl")
                    .meltingPointKelvin(119).boilingPointKelvin(260).density(0.0026F).gas().build(),
            MaterialDeclaration.builder("VinylAcetate")
                    .displayName("Vinyl Acetate").color(0xF0E8D0).chemicalFormula("C4H6O2")
                    .meltingPointKelvin(180).boilingPointKelvin(346).density(0.934F).liquid().build(),
            MaterialDeclaration.builder("AllylChloride")
                    .displayName("Allyl Chloride").color(0xE0E0B0).chemicalFormula("C3H5Cl")
                    .meltingPointKelvin(138).boilingPointKelvin(318).density(0.94F).liquid().build(),
            MaterialDeclaration.builder("Dichloropropanol")
                    .displayName("Dichloropropanol").color(0xD8D8A8).chemicalFormula("C3H6Cl2O")
                    .meltingPointKelvin(269).boilingPointKelvin(447).density(1.36F).liquid().build(),
            MaterialDeclaration.builder("Epichlorohydrin")
                    .displayName("Epichlorohydrin").color(0xE8E8C0).chemicalFormula("C3H5ClO")
                    .meltingPointKelvin(247).boilingPointKelvin(391).density(1.18F).liquid().build(),
            MaterialDeclaration.builder("HydrogenCyanide")
                    .displayName("Hydrogen Cyanide").color(0xD0E8E8).chemicalFormula("HCN")
                    .meltingPointKelvin(260).boilingPointKelvin(299).density(0.69F).liquid().build(),
            MaterialDeclaration.builder("Acrylonitrile")
                    .displayName("Acrylonitrile").color(0xE8D8C0).chemicalFormula("C3H3N")
                    .meltingPointKelvin(189).boilingPointKelvin(350).density(0.81F).liquid().build(),
            MaterialDeclaration.builder("Acrolein")
                    .displayName("Acrolein").color(0xF0E0B0).chemicalFormula("C3H4O")
                    .meltingPointKelvin(185).boilingPointKelvin(326).density(0.84F).liquid().build(),
            MaterialDeclaration.builder("AcrylicAcid")
                    .displayName("Acrylic Acid").color(0xF0F0D8).chemicalFormula("C3H4O2")
                    .meltingPointKelvin(287).boilingPointKelvin(414).density(1.05F).liquid().build(),
            MaterialDeclaration.builder("AcetoneCyanohydrin")
                    .displayName("Acetone Cyanohydrin").color(0xE0D8C8).chemicalFormula("C4H7NO")
                    .meltingPointKelvin(253).boilingPointKelvin(368).density(0.93F).liquid().build(),
            MaterialDeclaration.builder("MethylMethacrylate")
                    .displayName("Methyl Methacrylate").color(0xE8F0F8).chemicalFormula("C5H8O2")
                    .meltingPointKelvin(225).boilingPointKelvin(374).density(0.94F).liquid().build(),
            MaterialDeclaration.builder("Butyraldehyde")
                    .displayName("Butyraldehyde").color(0xF0E8C8).chemicalFormula("C4H8O")
                    .meltingPointKelvin(174).boilingPointKelvin(348).density(0.80F).liquid().build(),
            MaterialDeclaration.builder("Butanol")
                    .displayName("n-Butanol").color(0xE8E8E0).chemicalFormula("C4H9OH")
                    .meltingPointKelvin(183).boilingPointKelvin(391).density(0.81F).liquid().build(),
            MaterialDeclaration.builder("TwoEthylhexanol")
                    .displayName("2-Ethylhexanol").color(0xE0E0D8).chemicalFormula("C8H17OH")
                    .meltingPointKelvin(197).boilingPointKelvin(457).density(0.833F).liquid().build(),
            MaterialDeclaration.builder("Dimethyldioxane")
                    .displayName("4,4-Dimethyl-1,3-dioxane").color(0xE0E8D0).chemicalFormula("C6H12O2")
                    .meltingPointKelvin(185).boilingPointKelvin(406).density(0.95F).liquid().build(),
            MaterialDeclaration.builder("SolidPhosphoricAcid")
                    .displayName("Solid Phosphoric Acid").color(0xD8D0B8).chemicalFormula("H3PO4·SiO2")
                    .meltingPointKelvin(1000).boilingPointKelvin(2000).density(2.0F).dust().build(),
            MaterialDeclaration.builder("Polypropylene")
                    .displayName("Polypropylene").color(0xF0F0D8).chemicalFormula("(C3H6)n")
                    .meltingPointKelvin(433).density(0.90F).dust().polymer().build(),
            MaterialDeclaration.builder("UltraHighMolecularWeightPolyethylene")
                    .displayName("UHMWPE").color(0xF8F8F8).chemicalFormula("(C2H4)n")
                    .meltingPointKelvin(415).density(0.94F).dust().polymer().wires().build(),
            MaterialDeclaration.builder("PolyvinylAcetate")
                    .displayName("Polyvinyl Acetate").color(0xF0E8D8).chemicalFormula("(C4H6O2)n")
                    .meltingPointKelvin(333).density(1.19F).dust().build(),
            MaterialDeclaration.builder("PolyvinylAlcohol")
                    .displayName("Polyvinyl Alcohol").color(0xF8F0E0).chemicalFormula("(C2H4O)n")
                    .meltingPointKelvin(473).density(1.19F).dust().polymer().build(),
            MaterialDeclaration.builder("PolyvinylButyral")
                    .displayName("Polyvinyl Butyral").color(0xE8F0F0).chemicalFormula("(C8H14O2)n")
                    .meltingPointKelvin(438).density(1.08F).dust().polymer().build(),
            MaterialDeclaration.builder("EthyleneVinylAcetate")
                    .displayName("Ethylene-Vinyl Acetate").color(0xE8E8F0).chemicalFormula("(C2H4)n(C4H6O2)m")
                    .meltingPointKelvin(363).density(0.93F).dust().polymer().build(),
            MaterialDeclaration.builder("EthylAcetate")
                    .displayName("Ethyl Acetate").color(0xE8E0D8).chemicalFormula("C4H8O2")
                    .meltingPointKelvin(189).boilingPointKelvin(350).density(0.90F).liquid().build(),
            MaterialDeclaration.builder("Styrene")
                    .displayName("Styrene").color(0xE8E0D0).chemicalFormula("C8H8")
                    .meltingPointKelvin(243).boilingPointKelvin(418).density(0.909F).liquid().build(),
            MaterialDeclaration.builder("Diethylbenzene")
                    .displayName("Diethylbenzene").color(0xE0D8C8).chemicalFormula("C10H14")
                    .meltingPointKelvin(190).boilingPointKelvin(457).density(0.86F).liquid().build(),
            MaterialDeclaration.builder("Divinylbenzene")
                    .displayName("Divinylbenzene").color(0xE8D8C0).chemicalFormula("C10H10")
                    .meltingPointKelvin(207).boilingPointKelvin(468).density(0.91F).liquid().build(),
            MaterialDeclaration.builder("Polystyrene")
                    .displayName("Polystyrene").color(0xF0F0F0).chemicalFormula("(C8H8)n")
                    .meltingPointKelvin(513).density(1.05F).dust().polymer().build(),
            MaterialDeclaration.builder("CrosslinkedPolystyrene")
                    .displayName("Crosslinked Polystyrene Beads").color(0xF0E8D8).chemicalFormula("(C8H8)n(C10H10)m")
                    .meltingPointKelvin(573).density(1.05F).dust().build(),
            MaterialDeclaration.builder("ChloromethylatedPolystyrene")
                    .displayName("Chloromethylated Polystyrene").color(0xE8E0C0).chemicalFormula("(C9H9Cl)n")
                    .meltingPointKelvin(573).density(1.15F).dust().build(),
            MaterialDeclaration.builder("CationExchangeResin")
                    .displayName("Cation Exchange Resin").color(0x8C4A2A).chemicalFormula("(C8H7SO3H)n")
                    .meltingPointKelvin(573).density(1.25F).dust().build(),
            MaterialDeclaration.builder("AnionExchangeResin")
                    .displayName("Anion Exchange Resin").color(0xE0C890).chemicalFormula("(C12H18NCl)n")
                    .meltingPointKelvin(573).density(1.10F).dust().build(),
            MaterialDeclaration.builder("PolymethylMethacrylate")
                    .displayName("PMMA").color(0xE0F0F8).chemicalFormula("(C5H8O2)n")
                    .meltingPointKelvin(433).density(1.18F).dust().polymer().build(),
            MaterialDeclaration.builder("PhthalicAnhydride")
                    .displayName("Phthalic Anhydride").color(0xF8F8F0).chemicalFormula("C8H4O3")
                    .meltingPointKelvin(404).boilingPointKelvin(568).density(1.53F).dust().build(),
            MaterialDeclaration.builder("TerephthalicAcid")
                    .displayName("Purified Terephthalic Acid").color(0xFFFFFF).chemicalFormula("C8H6O4")
                    .meltingPointKelvin(700).density(1.52F).dust().build(),
            MaterialDeclaration.builder("IsophthalicAcid")
                    .displayName("Isophthalic Acid").color(0xF8F8F8).chemicalFormula("C8H6O4")
                    .meltingPointKelvin(618).density(1.53F).dust().build(),
            MaterialDeclaration.builder("Phosgene")
                    .displayName("Phosgene").color(0xE0F0D0).chemicalFormula("COCl2")
                    .meltingPointKelvin(155).boilingPointKelvin(281).density(0.0043F).gas().build(),
            MaterialDeclaration.builder("PolyethyleneTerephthalate")
                    .displayName("PET").color(0xE8F0F0).chemicalFormula("(C10H8O4)n")
                    .meltingPointKelvin(533).density(1.38F).dust().polymer().wires().build(),
            MaterialDeclaration.builder("MetallizedPolyester")
                    .displayName("Metallized Polyester Film").color(0xC8D0D8).chemicalFormula("(C10H8O4)nAl")
                    .meltingPointKelvin(533).density(1.40F).polymer().build(),
            MaterialDeclaration.builder("PhenolicResin")
                    .displayName("Phenolic Resin").color(0xA05A28).chemicalFormula("(C7H6O)n")
                    .meltingPointKelvin(300).boilingPointKelvin(450).density(1.20F).liquid().build(),
            MaterialDeclaration.builder("GlassFiber")
                    .displayName("Glass Fiber").color(0xF0F8F8).chemicalFormula("SiO2(B2O3)(CaO)")
                    .meltingPointKelvin(1400).density(2.55F).dust().wires().build(),
            MaterialDeclaration.builder("EpoxyResin")
                    .displayName("DGEBA Epoxy Resin").color(0xE8D890).chemicalFormula("C21H24O4")
                    .meltingPointKelvin(300).boilingPointKelvin(600).density(1.16F).liquid().build(),
            MaterialDeclaration.builder("Epoxy")
                    .displayName("Epoxy").color(0xC8B060).chemicalFormula("(C21H24O4)n")
                    .meltingPointKelvin(450).density(1.20F).dust().polymer().build(),
            MaterialDeclaration.builder("FiberReinforcedEpoxy")
                    .displayName("Fiber-Reinforced Epoxy").color(0xB0B078).chemicalFormula("(C21H24O4)n(SiO2)")
                    .meltingPointKelvin(450).density(1.90F).polymer().build(),
            MaterialDeclaration.builder("TetrabromobisphenolA")
                    .displayName("Tetrabromobisphenol A").color(0xF0E8E0).chemicalFormula("C15H12Br4O2")
                    .meltingPointKelvin(454).boilingPointKelvin(589).density(2.18F).dust().build(),
            MaterialDeclaration.builder("FlameRetardantEpoxyResin")
                    .displayName("Brominated Epoxy Resin").color(0xD8C080).chemicalFormula("C21H20Br4O4")
                    .meltingPointKelvin(320).boilingPointKelvin(650).density(1.70F).liquid().build(),
            MaterialDeclaration.builder("FlameRetardantLaminate")
                    .displayName("Flame-Retardant Epoxy Laminate").color(0x6E8C3A).chemicalFormula("(C21H20Br4O4)n(SiO2)")
                    .meltingPointKelvin(450).density(1.85F).polymer().build(),
            MaterialDeclaration.builder("Cresol")
                    .displayName("Cresol").color(0xC8A878).chemicalFormula("C7H8O")
                    .meltingPointKelvin(304).boilingPointKelvin(464).density(1.05F).liquid().build(),
            MaterialDeclaration.builder("EpoxyCresolNovolac")
                    .displayName("Epoxy Cresol Novolac").color(0xE0C890).chemicalFormula("(C10H12O2)n")
                    .meltingPointKelvin(350).density(1.20F).dust().build(),
            MaterialDeclaration.builder("PeraceticAcid")
                    .displayName("Peracetic Acid").color(0xE8F0E0).chemicalFormula("CH3COOOH")
                    .meltingPointKelvin(273).boilingPointKelvin(378).density(1.13F).liquid().build(),
            MaterialDeclaration.builder("Tetrahydrobenzaldehyde")
                    .displayName("Tetrahydrobenzaldehyde").color(0xF0E8C8).chemicalFormula("C7H10O")
                    .meltingPointKelvin(177).boilingPointKelvin(437).density(0.94F).liquid().build(),
            MaterialDeclaration.builder("CycloaliphaticEpoxy")
                    .displayName("Cycloaliphatic Epoxy").color(0xF0F0E0).chemicalFormula("C14H20O4")
                    .meltingPointKelvin(250).boilingPointKelvin(630).density(1.17F).liquid().build(),
            MaterialDeclaration.builder("Nitrobenzene")
                    .displayName("Nitrobenzene").color(0xE8D070).chemicalFormula("C6H5NO2")
                    .meltingPointKelvin(279).boilingPointKelvin(484).density(1.20F).liquid().build(),
            MaterialDeclaration.builder("Aniline")
                    .displayName("Aniline").color(0xD8C8A0).chemicalFormula("C6H5NH2")
                    .meltingPointKelvin(267).boilingPointKelvin(457).density(1.02F).liquid().build(),
            MaterialDeclaration.builder("Methylenedianiline")
                    .displayName("Methylenedianiline").color(0xD8C090).chemicalFormula("C13H14N2")
                    .meltingPointKelvin(362).boilingPointKelvin(671).density(1.15F).dust().build(),
            MaterialDeclaration.builder("MethyleneDiphenylDiisocyanate")
                    .displayName("MDI").color(0xC89850).chemicalFormula("C15H10N2O2")
                    .meltingPointKelvin(313).boilingPointKelvin(587).density(1.23F).liquid().build(),
            MaterialDeclaration.builder("PolyetherPolyol")
                    .displayName("Polyether Polyol").color(0xF0E8D8).chemicalFormula("(C2H4O)nC3H5(OH)3")
                    .meltingPointKelvin(270).boilingPointKelvin(600).density(1.03F).liquid().build(),
            MaterialDeclaration.builder("Polyurethane")
                    .displayName("Polyurethane").color(0xE8D8A0).chemicalFormula("(C17H16N2O4)n")
                    .meltingPointKelvin(450).density(1.20F).dust().polymer().build(),
            MaterialDeclaration.builder("PetroleumResin")
                    .displayName("Petroleum Resin").color(0xC89040).chemicalFormula("(C10H12)n(C6H6)(C7H10)")
                    .meltingPointKelvin(370).density(1.04F).dust().build(),
            MaterialDeclaration.builder("HydrogenatedPetroleumResin")
                    .displayName("Hydrogenated Petroleum Resin").color(0xF8F0D8).chemicalFormula("(C10H16)n")
                    .meltingPointKelvin(380).density(1.00F).dust().build(),
            MaterialDeclaration.builder("CarbonBlack")
                    .displayName("Carbon Black").color(0x141414).chemicalFormula("C")
                    .meltingPointKelvin(3800).density(1.8F).dust().build(),
            MaterialDeclaration.builder("ZincOxide")
                    .displayName("Zinc Oxide").color(0xF8F8F8).chemicalFormula("ZnO")
                    .meltingPointKelvin(2247).density(5.61F).dust().build(),
            MaterialDeclaration.builder("MagnesiumOxide")
                    .displayName("Magnesium Oxide").color(0xF0F0F0).chemicalFormula("MgO")
                    .meltingPointKelvin(3125).density(3.58F).dust().build(),
            MaterialDeclaration.builder("RawPolyisoprene")
                    .displayName("Raw Polyisoprene").color(0xC8B898).chemicalFormula("(C5H8)n")
                    .meltingPointKelvin(300).density(0.91F).dust().build(),
            MaterialDeclaration.builder("RawStyreneButadieneRubber")
                    .displayName("Raw SBR").color(0xB8A890).chemicalFormula("(C8H8)(C4H6)n")
                    .meltingPointKelvin(300).density(0.94F).dust().build(),
            MaterialDeclaration.builder("RawNitrileRubber")
                    .displayName("Raw NBR").color(0xC0A878).chemicalFormula("(C3H3N)(C4H6)n")
                    .meltingPointKelvin(300).density(1.00F).dust().build(),
            MaterialDeclaration.builder("Dichlorobutene")
                    .displayName("Dichlorobutene").color(0xE0E0B8).chemicalFormula("C4H6Cl2")
                    .meltingPointKelvin(225).boilingPointKelvin(428).density(1.15F).liquid().build(),
            MaterialDeclaration.builder("Chloroprene")
                    .displayName("Chloroprene").color(0xE8E8C0).chemicalFormula("C4H5Cl")
                    .meltingPointKelvin(143).boilingPointKelvin(332).density(0.96F).liquid().build(),
            MaterialDeclaration.builder("RawNeoprene")
                    .displayName("Raw Neoprene").color(0xD0C8A0).chemicalFormula("(C4H5Cl)n")
                    .meltingPointKelvin(320).density(1.23F).dust().build(),
            MaterialDeclaration.builder("Chloromethane")
                    .displayName("Chloromethane").color(0xD8F0D8).chemicalFormula("CH3Cl")
                    .meltingPointKelvin(176).boilingPointKelvin(249).density(0.0023F).gas().build(),
            MaterialDeclaration.builder("Dimethyldichlorosilane")
                    .displayName("Dimethyldichlorosilane").color(0xE0E8E8).chemicalFormula("(CH3)2SiCl2")
                    .meltingPointKelvin(197).boilingPointKelvin(343).density(1.07F).liquid().build(),
            MaterialDeclaration.builder("Polydimethylsiloxane")
                    .displayName("Polydimethylsiloxane").color(0xE8F0F8).chemicalFormula("((CH3)2SiO)n")
                    .meltingPointKelvin(233).boilingPointKelvin(500).density(0.97F).liquid().build(),
            MaterialDeclaration.builder("StyreneButadieneRubber")
                    .displayName("Styrene-Butadiene Rubber").color(0x2A2A2A).chemicalFormula("(C8H8)(C4H6)nS")
                    .meltingPointKelvin(450).density(1.10F).metal().build(),
            MaterialDeclaration.builder("NitrileRubber")
                    .displayName("Nitrile Rubber").color(0x2E2A26).chemicalFormula("(C3H3N)(C4H6)nS")
                    .meltingPointKelvin(450).density(1.15F).metal().build(),
            MaterialDeclaration.builder("Neoprene")
                    .displayName("Neoprene").color(0x262626).chemicalFormula("(C4H5Cl)n")
                    .meltingPointKelvin(450).density(1.35F).metal().build(),
            MaterialDeclaration.builder("SiliconeRubber")
                    .displayName("Silicone Rubber").color(0xD8D8D8).chemicalFormula("((CH3)2SiO)n")
                    .meltingPointKelvin(450).density(1.15F).metal().build(),
            MaterialDeclaration.builder("Ebonite")
                    .displayName("Ebonite").color(0x1A1410).chemicalFormula("(C5H8)nS")
                    .meltingPointKelvin(450).density(1.15F).dust().polymer().build());

    public static final List<MaterialFormRequest> FORMS = List.of();

    private PolymerMaterials() {}
}
