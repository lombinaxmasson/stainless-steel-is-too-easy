package com.lombinaxmasson.ssite.material;

import gregtech.api.addon.material.MaterialDeclaration;
import gregtech.api.addon.material.PrefixDeclaration;
import gregtech.api.material.GTValues;

import java.util.List;
import java.util.Set;

/**
 * P-06 engineering ceramics and the two distinct composite routes.
 *
 * <p>The raw inputs stay on existing materials: {@code Alumina} from the Bayer line,
 * {@code CarbonFiber} from the PAN route, {@code PhenolicResin} and {@code Carbon}. The three
 * products are separate materials so the material report does not confuse carbon-carbon with the
 * resin-based {@code CarbonFiberComposite} from M9-08.</p>
 *
 * <p>Every process stage has its own prefix; a green body, a sintered body, an impregnated body and
 * its carbonized form are separate item identities. Carbon-carbon densification is therefore a
 * finite, explicit sequence rather than a same-item "already treated" loop.</p>
 */
public final class EngineeringMaterials {
    public static final String CERAMIC = "EngineeringCeramic";
    public static final String CARBON_CARBON = "CarbonCarbon";
    public static final String ABLATIVE = "AblativeComposite";

    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder(CERAMIC)
                    .displayName("Alumina Ceramic").color(0xE6E2D8).chemicalFormula("Al2O3")
                    .meltingPointKelvin(2345).boilingPointKelvin(3250).density(3.95F).build(),
            MaterialDeclaration.builder(CARBON_CARBON)
                    .displayName("Carbon-Carbon Composite").color(0x2A2A30).chemicalFormula("C")
                    .meltingPointKelvin(3800).boilingPointKelvin(4000).density(1.75F).build(),
            MaterialDeclaration.builder(ABLATIVE)
                    .displayName("Ablative Composite").color(0x3B3028).chemicalFormula("C·(C7H6O)n")
                    .meltingPointKelvin(900).density(1.50F).build());

    /**
     * Item icons reuse the existing addon greyscale textures; no new art is introduced. The unit
     * amounts match the abstract batch scale (a plate is one U) and are not a mass model.
     */
    public static final List<PrefixDeclaration> PREFIXES = List.of(
            new PrefixDeclaration("ceramicBatch", "", " Batch", GTValues.U,
                    "item/chip", List.of(CERAMIC), Set.of()),
            new PrefixDeclaration("ceramicGreenBody", "", " Green Body", GTValues.U,
                    "item/die", List.of(CERAMIC), Set.of()),
            new PrefixDeclaration("ceramicSintered", "", " Sintered Body", GTValues.U,
                    "item/die", List.of(CERAMIC), Set.of()),
            new PrefixDeclaration("ceramicClamp", "", " Insulating Clamp", GTValues.U,
                    "item/chip", List.of(CERAMIC), Set.of()),
            new PrefixDeclaration("carbonFiberPreform", "", " Preform", GTValues.U2,
                    "item/wafer", List.of(CARBON_CARBON), Set.of()),
            new PrefixDeclaration("carbonCarbonImpregnated", "", " Impregnated Body", GTValues.U2,
                    "item/die", List.of(CARBON_CARBON), Set.of()),
            new PrefixDeclaration("carbonCarbonCarbonized", "", " Carbonized Body", GTValues.U2,
                    "item/die", List.of(CARBON_CARBON), Set.of()),
            new PrefixDeclaration("carbonCarbonDensified", "", " Densified Body", GTValues.U2,
                    "item/wafer", List.of(CARBON_CARBON), Set.of()),
            new PrefixDeclaration("carbonCarbonSetter", "", " Kiln Setter", GTValues.U4,
                    "item/wafer", List.of(CARBON_CARBON), Set.of()),
            new PrefixDeclaration("ablativeBillet", "", " Billet", GTValues.U2,
                    "item/die", List.of(ABLATIVE), Set.of()),
            new PrefixDeclaration("ablativeBackingBlank", "", " Backing Blank", GTValues.U2,
                    "item/wafer", List.of(ABLATIVE), Set.of()),
            new PrefixDeclaration("ablativeBacking", "", " Backing", GTValues.U2,
                    "item/wafer", List.of(ABLATIVE), Set.of()),
            new PrefixDeclaration("ablativeBackingSpent", "", " Spent Backing", GTValues.U2,
                    "item/wafer", List.of(ABLATIVE), Set.of()));

    private EngineeringMaterials() {}
}
