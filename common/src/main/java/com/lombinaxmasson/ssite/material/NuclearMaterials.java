package com.lombinaxmasson.ssite.material;

import com.gregtech.gregtech.api.addon.MaterialDeclaration;

import java.util.List;

/** M6 nuclear hydrometallurgy materials. Card 4 appends its own entries here. */
public final class NuclearMaterials {
    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder("ThoriumConcentrate")
                    .displayName("Thorium Hydroxide Concentrate").color(0x606060)
                    .chemicalFormula("Th(OH)4")
                    .meltingPointKelvin(800).boilingPointKelvin(1800).density(4.20F).dust().build(),
            MaterialDeclaration.builder("UranylSulfateSolution")
                    .displayName("Crude Uranyl Sulfate Solution").color(0xC8E860)
                    .chemicalFormula("UO2SO4(aq)")
                    .meltingPointKelvin(290).boilingPointKelvin(390).density(1.60F).liquid().build()
    );

    private NuclearMaterials() {}
}
