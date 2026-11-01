package com.lombinaxmasson.ssite.material;

import gregtech.api.addon.material.MaterialDeclaration;
import gregtech.api.addon.material.MaterialFormRequest;
import gregtech.api.addon.material.PrefixDeclaration;
import gregtech.api.material.GTValues;

import java.util.List;
import java.util.Set;

/**
 * M9-08 functional textiles and composite parts.
 *
 * <p>Nomex and UHMWPE cloth keep their existing {@code plate} form; only the felt, insulation,
 * prepreg and cured composite are new materials. {@code polymer()} unlocks the generated {@code plate}
 * and {@code foil} forms through {@link MaterialFormRequest} and has no other effect.</p>
 *
 * <p>The host has no material tube form - its pipes are blocks and never generate standalone items - so
 * the braided hose is an addon {@code hose} prefix on the two chemical-resistant elastomers.</p>
 */
public final class FunctionalTextileMaterials {
    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder("AramidFelt")
                    .displayName("Aramid Felt").color(0xC8B878).chemicalFormula("(C14H10N2O2)n")
                    .meltingPointKelvin(640).density(0.60F).polymer().build(),
            MaterialDeclaration.builder("MultilayerInsulation")
                    .displayName("Multilayer Insulation").color(0xE0C860).chemicalFormula("Al·PET·Al")
                    .meltingPointKelvin(550).density(0.35F).polymer().build(),
            MaterialDeclaration.builder("CarbonFiberPrepreg")
                    .displayName("Carbon Fiber Prepreg").color(0x3A3630).chemicalFormula("C·(C21H24O4)n")
                    .meltingPointKelvin(450).density(1.45F).polymer().build(),
            MaterialDeclaration.builder("CarbonFiberComposite")
                    .displayName("Carbon Fiber Composite").color(0x24262A).chemicalFormula("C·(C21H24O4)n")
                    .meltingPointKelvin(600).density(1.60F).polymer().build());

    public static final List<MaterialFormRequest> FORMS = List.of(
            new MaterialFormRequest("AramidFelt", "plate"),
            new MaterialFormRequest("MultilayerInsulation", "plate"),
            new MaterialFormRequest("CarbonFiberPrepreg", "foil"),
            new MaterialFormRequest("CarbonFiberComposite", "plate"));

    /** A hose is braided tube stock, so it carries its own item form and a tinted grayscale texture. */
    public static final List<PrefixDeclaration> PREFIXES = List.of(
            new PrefixDeclaration("hose", "", " Hose", GTValues.U2,
                    "item/hose", List.of("Viton", "PolyvinylideneFluoride"), Set.of()));

    private FunctionalTextileMaterials() {}
}
