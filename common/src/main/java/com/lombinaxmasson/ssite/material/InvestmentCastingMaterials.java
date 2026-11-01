package com.lombinaxmasson.ssite.material;

import gregtech.api.addon.material.MaterialDeclaration;
import gregtech.api.addon.material.MaterialFormRequest;

import java.util.List;

/**
 * M9-09 investment casting consumables and parts.
 *
 * <p>The card names the wax, the shell and the generic casting. The chain needs three process
 * states the card folds into prose: the pressed wax pattern, the fired shell that is safe to pour
 * into, and the as-cast part that still carries its gate. Keeping them as materials stops the
 * pour from being reachable straight from a green shell.</p>
 *
 * <p>Every solid form declares {@code polymer()} only to unlock the generated {@code plate} form
 * through {@link MaterialFormRequest}; the feature has no other effect on ceramic or metal parts.</p>
 */
public final class InvestmentCastingMaterials {
    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder("PatternWax")
                    .displayName("Pattern Wax").color(0xF2E8C8).chemicalFormula("(C10H16)n·CnH2n+2")
                    .meltingPointKelvin(355).boilingPointKelvin(620).density(0.92F).polymer().build(),
            MaterialDeclaration.builder("WaxPattern")
                    .displayName("Wax Pattern").color(0xE8D8A8).chemicalFormula("(C10H16)n·CnH2n+2")
                    .meltingPointKelvin(355).boilingPointKelvin(620).density(0.90F).polymer().build(),
            MaterialDeclaration.builder("CeramicShellSlurry")
                    .displayName("Ceramic Shell Slurry").color(0xD8CFC0)
                    .chemicalFormula("ZrSiO4·Al2O3·SiO2(aq)")
                    .meltingPointKelvin(273).boilingPointKelvin(373).density(1.85F).liquid().build(),
            MaterialDeclaration.builder("CeramicShell")
                    .displayName("Ceramic Shell").color(0xC8BFA8).chemicalFormula("ZrSiO4·Al2O3·SiO2")
                    .meltingPointKelvin(2100).boilingPointKelvin(3000).density(2.60F).polymer().build(),
            MaterialDeclaration.builder("FiredCeramicShell")
                    .displayName("Fired Ceramic Shell").color(0xB8AC98).chemicalFormula("ZrO2·Al2O3·SiO2")
                    .meltingPointKelvin(2100).boilingPointKelvin(3000).density(2.55F).polymer().build(),
            MaterialDeclaration.builder("AsCastPart")
                    .displayName("As-Cast Part").color(0x7A7A80).chemicalFormula("M")
                    .meltingPointKelvin(1800).boilingPointKelvin(3000).density(7.8F).polymer().build(),
            MaterialDeclaration.builder("PrecisionCasting")
                    .displayName("Precision Casting").color(0x8C8C94).chemicalFormula("M")
                    .meltingPointKelvin(1800).boilingPointKelvin(3000).density(8.0F).polymer().build());

    public static final List<MaterialFormRequest> FORMS = List.of(
            new MaterialFormRequest("PatternWax", "plate"),
            new MaterialFormRequest("WaxPattern", "plate"),
            new MaterialFormRequest("CeramicShell", "plate"),
            new MaterialFormRequest("FiredCeramicShell", "plate"),
            new MaterialFormRequest("AsCastPart", "plate"),
            new MaterialFormRequest("PrecisionCasting", "plate"));

    private InvestmentCastingMaterials() {}
}
