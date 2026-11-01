package com.lombinaxmasson.ssite.material;

import gregtech.api.addon.material.MaterialDeclaration;
import gregtech.api.addon.material.MaterialFormRequest;
import gregtech.api.addon.material.PrefixDeclaration;
import gregtech.api.material.GTValues;

import java.util.List;
import java.util.Set;

/**
 * M9-06 packaging materials and the die/chip prefixes.
 *
 * <p>The frame materials declare {@code polymer()} only to unlock the generated {@code plate} form
 * through {@link MaterialFormRequest}; {@code AddonMaterials.publishForms} accepts plate and foil
 * requests for polymer-flagged addon materials. The feature has no other effect.</p>
 *
 * <p>Elite is the first packaged tier. Master and ultimate stay as host parts routes until this
 * tier has been accepted in game.</p>
 */
public final class PackagingMaterials {
    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder("MoldingCompound")
                    .displayName("Molding Compound").color(0x2A2A30).chemicalFormula("(C10H12O2)n·SiO2")
                    .meltingPointKelvin(450).density(1.90F).dust().build(),
            MaterialDeclaration.builder("LeadFrame")
                    .displayName("Lead Frame").color(0xB08A50).chemicalFormula("CuZn·Sn")
                    .meltingPointKelvin(1200).density(8.50F).polymer().build(),
            MaterialDeclaration.builder("DieAttachedFrame")
                    .displayName("Die-Attached Frame").color(0x8A8A96).chemicalFormula("Si·CuZn")
                    .meltingPointKelvin(1200).density(5.00F).polymer().build(),
            MaterialDeclaration.builder("BondedFrame")
                    .displayName("Bonded Frame").color(0xD8C070).chemicalFormula("Si·CuZn·Au")
                    .meltingPointKelvin(1200).density(5.50F).polymer().build(),
            MaterialDeclaration.builder("ChipDieElite")
                    .displayName("Elite IC").color(0x9AA8B4).chemicalFormula("Si")
                    .meltingPointKelvin(1687).density(2.33F).build(),
            MaterialDeclaration.builder("ChipElite")
                    .displayName("Elite IC").color(0x4A4A54).chemicalFormula("Si·(C10H12O2)n")
                    .meltingPointKelvin(450).density(1.90F).build());

    public static final List<MaterialFormRequest> FORMS = List.of(
            new MaterialFormRequest("LeadFrame", "plate"),
            new MaterialFormRequest("DieAttachedFrame", "plate"),
            new MaterialFormRequest("BondedFrame", "plate"));

    /** One die is half a material unit; a package adds the molding compound around it. */
    public static final List<PrefixDeclaration> PREFIXES = List.of(
            new PrefixDeclaration("die", "", " Die", GTValues.U / 2,
                    "item/die", List.of("ChipDieElite"), Set.of()),
            new PrefixDeclaration("chip", "", " Package", GTValues.U / 2,
                    "item/chip", List.of("ChipElite"), Set.of()));

    private PackagingMaterials() {}
}
