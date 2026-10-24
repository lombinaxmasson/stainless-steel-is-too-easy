package com.lombinaxmasson.ssite.material;

import com.gregtech.gregtech.api.addon.MaterialDeclaration;

import java.util.List;

/** M6 rare-earth materials. Cards 2 and 3 append their own entries here. */
public final class RareEarthMaterials {
    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder("Xenotime")
                    .displayName("Xenotime").color(0xC8C878).chemicalFormula("YPO4")
                    .meltingPointKelvin(700).boilingPointKelvin(1900).density(4.50F)
                    .dust().oreByproducts("RareEarth", "Phosphorus").build(),
            MaterialDeclaration.builder("BastnasiteCalcine")
                    .displayName("Bastnasite Calcine").color(0xC87840).chemicalFormula("CeO2·RE2O3")
                    .meltingPointKelvin(1200).boilingPointKelvin(3200).density(5.20F).dust().build(),
            MaterialDeclaration.builder("MixedRareEarthSolution")
                    .displayName("Mixed Rare Earth Solution").color(0xA8B878)
                    .chemicalFormula("RE2(SO4)3·RECl3(aq)")
                    .meltingPointKelvin(290).boilingPointKelvin(380).density(1.35F).liquid().build()
    );

    private RareEarthMaterials() {}
}
