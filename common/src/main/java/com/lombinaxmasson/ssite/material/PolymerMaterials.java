package com.lombinaxmasson.ssite.material;

import com.gregtech.gregtech.api.addon.MaterialDeclaration;
import com.gregtech.gregtech.api.addon.MaterialFormRequest;
import com.gregtech.gregtech.api.addon.platform.AddonItems;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
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
                    .meltingPointKelvin(450).density(1.15F).dust().polymer().build(),
            MaterialDeclaration.builder("Cyclohexane")
                    .displayName("Cyclohexane").color(0xE8E8E0).chemicalFormula("C6H12")
                    .meltingPointKelvin(280).boilingPointKelvin(354).density(0.78F).liquid().build(),
            MaterialDeclaration.builder("Cyclohexanone")
                    .displayName("Cyclohexanone").color(0xE8E0D0).chemicalFormula("C6H10O")
                    .meltingPointKelvin(242).boilingPointKelvin(429).density(0.95F).liquid().build(),
            MaterialDeclaration.builder("HydroxylammoniumSulfate")
                    .displayName("Hydroxylammonium Sulfate").color(0xF8F8F0).chemicalFormula("(NH3OH)2SO4")
                    .meltingPointKelvin(393).density(1.88F).dust().build(),
            MaterialDeclaration.builder("CyclohexanoneOxime")
                    .displayName("Cyclohexanone Oxime").color(0xF0F0E0).chemicalFormula("C6H11NO")
                    .meltingPointKelvin(363).boilingPointKelvin(483).density(1.10F).dust().build(),
            MaterialDeclaration.builder("Caprolactam")
                    .displayName("Caprolactam").color(0xFFFFF8).chemicalFormula("C6H11NO")
                    .meltingPointKelvin(342).boilingPointKelvin(543).density(1.01F).dust().build(),
            MaterialDeclaration.builder("Polycaprolactam")
                    .displayName("Nylon 6").color(0xF0F0E8).chemicalFormula("(C6H11NO)n")
                    .meltingPointKelvin(496).density(1.14F).dust().polymer().wires().build(),
            MaterialDeclaration.builder("Polyacrylonitrile")
                    .displayName("Polyacrylonitrile").color(0xF8F0E8).chemicalFormula("(C3H3N)n")
                    .meltingPointKelvin(590).density(1.18F).dust().polymer().wires().build(),
            MaterialDeclaration.builder("OxidizedPolyacrylonitrile")
                    .displayName("Oxidized PAN").color(0x5A3A20).chemicalFormula("(C3HN)n")
                    .meltingPointKelvin(900).density(1.38F).dust().wires().build(),
            MaterialDeclaration.builder("CarbonFiber")
                    .displayName("Carbon Fiber").color(0x1C1C20).chemicalFormula("C")
                    .meltingPointKelvin(3800).density(1.80F).polymer().wires().build(),
            MaterialDeclaration.builder("Chlorobenzene")
                    .displayName("Chlorobenzene").color(0xE0E8D0).chemicalFormula("C6H5Cl")
                    .meltingPointKelvin(228).boilingPointKelvin(405).density(1.11F).liquid().build(),
            MaterialDeclaration.builder("ParaNitrochlorobenzene")
                    .displayName("p-Nitrochlorobenzene").color(0xF0E890).chemicalFormula("C6H4ClNO2")
                    .meltingPointKelvin(356).boilingPointKelvin(515).density(1.52F).dust().build(),
            MaterialDeclaration.builder("ParaNitroaniline")
                    .displayName("p-Nitroaniline").color(0xF0C838).chemicalFormula("C6H6N2O2")
                    .meltingPointKelvin(420).boilingPointKelvin(605).density(1.44F).dust().build(),
            MaterialDeclaration.builder("ParaPhenylenediamine")
                    .displayName("p-Phenylenediamine").color(0xD8C8B0).chemicalFormula("C6H8N2")
                    .meltingPointKelvin(413).boilingPointKelvin(540).density(1.14F).dust().build(),
            MaterialDeclaration.builder("MetaDinitrobenzene")
                    .displayName("m-Dinitrobenzene").color(0xF0E8C0).chemicalFormula("C6H4N2O4")
                    .meltingPointKelvin(363).boilingPointKelvin(570).density(1.58F).dust().build(),
            MaterialDeclaration.builder("MetaPhenylenediamine")
                    .displayName("m-Phenylenediamine").color(0xE0D0B8).chemicalFormula("C6H8N2")
                    .meltingPointKelvin(337).boilingPointKelvin(555).density(1.14F).dust().build(),
            MaterialDeclaration.builder("TerephthaloylChloride")
                    .displayName("Terephthaloyl Chloride").color(0xF8F8F0).chemicalFormula("C8H4Cl2O2")
                    .meltingPointKelvin(356).boilingPointKelvin(538).density(1.34F).dust().build(),
            MaterialDeclaration.builder("IsophthaloylChloride")
                    .displayName("Isophthaloyl Chloride").color(0xF8F8E8).chemicalFormula("C8H4Cl2O2")
                    .meltingPointKelvin(317).boilingPointKelvin(549).density(1.39F).dust().build(),
            MaterialDeclaration.builder("Kevlar")
                    .displayName("Kevlar").color(0xE8C840).chemicalFormula("(C14H10N2O2)n")
                    .meltingPointKelvin(800).density(1.44F).dust().polymer().wires().build(),
            MaterialDeclaration.builder("Nomex")
                    .displayName("Nomex").color(0xE8E0C0).chemicalFormula("(C14H10N2O2)n")
                    .meltingPointKelvin(640).density(1.38F).dust().polymer().wires().build(),
            MaterialDeclaration.builder("Chloroform")
                    .displayName("Chloroform").color(0xE0F0E8).chemicalFormula("CHCl3")
                    .meltingPointKelvin(210).boilingPointKelvin(334).density(1.49F).liquid().build(),
            MaterialDeclaration.builder("Chlorodifluoromethane")
                    .displayName("Chlorodifluoromethane").color(0xD8F0F0).chemicalFormula("CHClF2")
                    .meltingPointKelvin(98).boilingPointKelvin(232).density(0.0036F).gas().build(),
            MaterialDeclaration.builder("Tetrafluoroethylene")
                    .displayName("Tetrafluoroethylene").color(0xE0F8F0).chemicalFormula("C2F4")
                    .meltingPointKelvin(131).boilingPointKelvin(197).density(0.0041F).gas().build(),
            MaterialDeclaration.builder("Hexafluoropropylene")
                    .displayName("Hexafluoropropylene").color(0xD8F0E8).chemicalFormula("C3F6")
                    .meltingPointKelvin(120).boilingPointKelvin(244).density(0.0061F).gas().build(),
            MaterialDeclaration.builder("Trichloroethane")
                    .displayName("1,1,1-Trichloroethane").color(0xD8E8C8).chemicalFormula("C2H3Cl3")
                    .meltingPointKelvin(240).boilingPointKelvin(347).density(1.32F).liquid().build(),
            MaterialDeclaration.builder("Chlorodifluoroethane")
                    .displayName("Chlorodifluoroethane").color(0xD0E8E0).chemicalFormula("C2H3ClF2")
                    .meltingPointKelvin(142).boilingPointKelvin(263).density(0.0042F).gas().build(),
            MaterialDeclaration.builder("VinylideneFluoride")
                    .displayName("Vinylidene Fluoride").color(0xE0F0F8).chemicalFormula("C2H2F2")
                    .meltingPointKelvin(129).boilingPointKelvin(188).density(0.0026F).gas().build(),
            MaterialDeclaration.builder("PolyvinylideneFluoride")
                    .displayName("PVDF").color(0xF0F8F8).chemicalFormula("(C2H2F2)n")
                    .meltingPointKelvin(450).density(1.78F).dust().polymer().build(),
            MaterialDeclaration.builder("RawViton")
                    .displayName("Raw Viton").color(0xC8C0B0).chemicalFormula("(C2H2F2)n(C3F6)m")
                    .meltingPointKelvin(320).density(1.80F).dust().build(),
            MaterialDeclaration.builder("Viton")
                    .displayName("Viton").color(0x3A2E28).chemicalFormula("(C2H2F2)n(C3F6)m")
                    .meltingPointKelvin(470).density(1.85F).metal().build(),
            MaterialDeclaration.builder("PerfluorosulfonylVinylEther")
                    .displayName("Perfluorosulfonyl Vinyl Ether").color(0xE8F0E0).chemicalFormula("C7F14O4S")
                    .meltingPointKelvin(200).boilingPointKelvin(408).density(1.70F).liquid().build(),
            MaterialDeclaration.builder("Nafion")
                    .displayName("Nafion").color(0xF0F0E8).chemicalFormula("(C2F4)n(C7F13O5S)m")
                    .meltingPointKelvin(470).density(1.98F).dust().polymer().build(),
            MaterialDeclaration.builder("ParaDichlorobenzene")
                    .displayName("p-Dichlorobenzene").color(0xF8F8F0).chemicalFormula("C6H4Cl2")
                    .meltingPointKelvin(326).boilingPointKelvin(447).density(1.25F).dust().build(),
            MaterialDeclaration.builder("PolyphenyleneSulfide")
                    .displayName("PPS").color(0xC8A060).chemicalFormula("(C6H4S)n")
                    .meltingPointKelvin(553).density(1.35F).dust().polymer().build(),
            MaterialDeclaration.builder("Dichlorodiphenylsulfone")
                    .displayName("Dichlorodiphenyl Sulfone").color(0xF0F0F0).chemicalFormula("C12H8Cl2O2S")
                    .meltingPointKelvin(421).boilingPointKelvin(670).density(1.50F).dust().build(),
            MaterialDeclaration.builder("Polysulfone")
                    .displayName("Polysulfone").color(0xE8D8A0).chemicalFormula("(C27H22O4S)n")
                    .meltingPointKelvin(460).density(1.24F).dust().polymer().build(),
            MaterialDeclaration.builder("Hydroquinone")
                    .displayName("Hydroquinone").color(0xF8F8F0).chemicalFormula("C6H6O2")
                    .meltingPointKelvin(445).boilingPointKelvin(560).density(1.30F).dust().build(),
            MaterialDeclaration.builder("Fluorobenzene")
                    .displayName("Fluorobenzene").color(0xE0F0F0).chemicalFormula("C6H5F")
                    .meltingPointKelvin(229).boilingPointKelvin(358).density(1.02F).liquid().build(),
            MaterialDeclaration.builder("Difluorobenzophenone")
                    .displayName("Difluorobenzophenone").color(0xF0F0E0).chemicalFormula("C13H8F2O")
                    .meltingPointKelvin(380).boilingPointKelvin(590).density(1.25F).dust().build(),
            MaterialDeclaration.builder("PolyetherEtherKetone")
                    .displayName("PEEK").color(0xC8B890).chemicalFormula("(C19H12O3)n")
                    .meltingPointKelvin(616).density(1.32F).dust().polymer().build(),
            MaterialDeclaration.builder("Durene")
                    .displayName("Durene").color(0xF0F0E8).chemicalFormula("C10H14")
                    .meltingPointKelvin(352).boilingPointKelvin(470).density(0.84F).dust().build(),
            MaterialDeclaration.builder("PyromelliticDianhydride")
                    .displayName("Pyromellitic Dianhydride").color(0xF8F8F8).chemicalFormula("C10H2O6")
                    .meltingPointKelvin(559).boilingPointKelvin(670).density(1.68F).dust().build(),
            MaterialDeclaration.builder("Oxydianiline")
                    .displayName("Oxydianiline").color(0xE8D8C8).chemicalFormula("C12H12N2O")
                    .meltingPointKelvin(461).boilingPointKelvin(663).density(1.32F).dust().build(),
            MaterialDeclaration.builder("Kapton")
                    .displayName("Kapton").color(0xD08820).chemicalFormula("(C22H10N2O5)n")
                    .meltingPointKelvin(870).density(1.42F).dust().polymer().build(),
            MaterialDeclaration.builder("MethylIsobutylKetone")
                    .displayName("MIBK").color(0xE8E8E0).chemicalFormula("C6H12O")
                    .meltingPointKelvin(189).boilingPointKelvin(390).density(0.80F).liquid().build(),
            MaterialDeclaration.builder("PhosphorylChloride")
                    .displayName("Phosphoryl Chloride").color(0xE8F0D8).chemicalFormula("POCl3")
                    .meltingPointKelvin(274).boilingPointKelvin(379).density(1.65F).liquid().build(),
            MaterialDeclaration.builder("TributylPhosphate")
                    .displayName("Tributyl Phosphate").color(0xF0F0E0).chemicalFormula("C12H27O4P")
                    .meltingPointKelvin(193).boilingPointKelvin(562).density(0.98F).liquid().build(),
            MaterialDeclaration.builder("DiethylhexylPhosphoricAcid")
                    .displayName("P204").color(0xF0E8D0).chemicalFormula("C16H35O4P")
                    .meltingPointKelvin(213).boilingPointKelvin(600).density(0.97F).liquid().build(),
            MaterialDeclaration.builder("Octanol")
                    .displayName("1-Octanol").color(0xE8E8D8).chemicalFormula("C8H17OH")
                    .meltingPointKelvin(257).boilingPointKelvin(468).density(0.83F).liquid().build(),
            MaterialDeclaration.builder("Trioctylamine")
                    .displayName("Trioctylamine").color(0xE0D8C8).chemicalFormula("C24H51N")
                    .meltingPointKelvin(239).boilingPointKelvin(640).density(0.81F).liquid().build(),
            MaterialDeclaration.builder("Aliquat")
                    .displayName("Aliquat 336").color(0xE8D8B0).chemicalFormula("C25H54NCl")
                    .meltingPointKelvin(253).boilingPointKelvin(500).density(0.88F).liquid().build(),
            MaterialDeclaration.builder("AlphaOlefins")
                    .displayName("Alpha Olefins").color(0xE0E0D0).chemicalFormula("CnH2n")
                    .meltingPointKelvin(200).boilingPointKelvin(420).density(0.72F).liquid().build(),
            MaterialDeclaration.builder("OneOctene")
                    .displayName("1-Octene").color(0xE8E8D8).chemicalFormula("C8H16")
                    .meltingPointKelvin(171).boilingPointKelvin(394).density(0.72F).liquid().build(),
            MaterialDeclaration.builder("OneDecene")
                    .displayName("1-Decene").color(0xE8E0D0).chemicalFormula("C10H20")
                    .meltingPointKelvin(207).boilingPointKelvin(444).density(0.74F).liquid().build(),
            MaterialDeclaration.builder("Polyalphaolefin")
                    .displayName("Polyalphaolefin").color(0xF0E8C0).chemicalFormula("(C10H20)n")
                    .meltingPointKelvin(220).boilingPointKelvin(650).density(0.82F).liquid().build(),
            MaterialDeclaration.builder("Polyisobutylene")
                    .displayName("Polyisobutylene").color(0xE8E0C8).chemicalFormula("(C4H8)n")
                    .meltingPointKelvin(210).boilingPointKelvin(600).density(0.92F).liquid().build());

    public static final List<MaterialFormRequest> FORMS = buildForms();

    private static final String[] CREATIVE_PREFIXES = {
            "dust", "dustSmall", "dustTiny", "plate", "foil", "wireFine", "ingot"};

    /**
     * Item stacks for the ssite creative tab, in declaration order.
     * Forms a material does not generate resolve to empty and are skipped.
     */
    public static List<ItemStack> creativeStacks() {
        var stacks = new ArrayList<ItemStack>();
        for (MaterialDeclaration declaration : MATERIALS) {
            for (String prefix : CREATIVE_PREFIXES) {
                ItemStack stack = AddonItems.stack(prefix, declaration.name(), 1);
                if (!stack.isEmpty()) stacks.add(stack);
            }
        }
        return List.copyOf(stacks);
    }

    private static List<MaterialFormRequest> buildForms() {
        String[] polymers = {
                "Polypropylene", "UltraHighMolecularWeightPolyethylene", "PolyvinylAlcohol",
                "PolyvinylButyral", "EthyleneVinylAcetate", "Polystyrene", "PolymethylMethacrylate",
                "PolyethyleneTerephthalate", "MetallizedPolyester", "Epoxy", "FiberReinforcedEpoxy",
                "FlameRetardantLaminate", "Polyurethane", "Ebonite", "Polycaprolactam",
                "Polyacrylonitrile", "CarbonFiber", "Nomex", "Kevlar", "PolyvinylideneFluoride",
                "Nafion", "PolyphenyleneSulfide", "Polysulfone", "PolyetherEtherKetone", "Kapton"};
        String[] wires = {
                "UltraHighMolecularWeightPolyethylene", "PolyethyleneTerephthalate", "Polycaprolactam",
                "Polyacrylonitrile", "CarbonFiber", "Nomex", "Kevlar", "GlassFiber",
                "OxidizedPolyacrylonitrile"};
        var forms = new ArrayList<MaterialFormRequest>();
        for (String material : polymers) {
            forms.add(new MaterialFormRequest(material, "plate"));
            forms.add(new MaterialFormRequest(material, "foil"));
        }
        for (String material : wires) {
            forms.add(new MaterialFormRequest(material, "wireFine"));
        }
        return List.copyOf(forms);
    }

    private PolymerMaterials() {}
}
