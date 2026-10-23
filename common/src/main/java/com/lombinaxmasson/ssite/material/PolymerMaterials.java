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
                    .meltingPointKelvin(180).boilingPointKelvin(346).density(0.934F).liquid().build());

    public static final List<MaterialFormRequest> FORMS = List.of();

    private PolymerMaterials() {}
}
