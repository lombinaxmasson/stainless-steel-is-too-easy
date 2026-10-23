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
                    .meltingPointKelvin(1000).boilingPointKelvin(2000).density(2.0F).dust().build());

    public static final List<MaterialFormRequest> FORMS = List.of();

    private PolymerMaterials() {}
}
