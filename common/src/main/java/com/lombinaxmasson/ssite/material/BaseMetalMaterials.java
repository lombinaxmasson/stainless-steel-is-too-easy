package com.lombinaxmasson.ssite.material;

import com.gregtech.gregtech.api.addon.MaterialDeclaration;

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
                    .meltingPointKelvin(250).boilingPointKelvin(400).density(0.97F).liquid().build()
    );

    private BaseMetalMaterials() {}
}
