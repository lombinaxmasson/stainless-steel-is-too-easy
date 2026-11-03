package com.lombinaxmasson.ssite.material;

import gregtech.api.addon.material.MaterialDeclaration;
import gregtech.api.addon.material.MaterialFormRequest;
import gregtech.api.addon.material.PrefixDeclaration;
import gregtech.api.material.GTValues;

import java.util.List;
import java.util.Set;

/**
 * P-03 advanced semiconductor route.
 *
 * <p>The advanced route gets its own prefixes so the M9 wafer, die and chip ids keep their
 * existing meaning. Every wafer stage is a separate material: a raw silicon wafer cannot match a
 * later stage, and the old {@code ProcessedWafer} cannot be exchanged for an advanced wafer. The
 * frame materials declare {@code polymer()} only to unlock the generated {@code plate} form; the
 * flag has no other effect.</p>
 *
 * <p>Item textures reuse the host wafer, die and chip icons; the stage colours provide the
 * visible distinction between materials.</p>
 */
public final class AdvancedSemiconductorMaterials {
    public static final String CLEANED = "AdvancedCleanedWafer";
    public static final String OXIDIZED = "AdvancedOxidizedWafer";
    public static final String COATED = "AdvancedCoatedWafer";
    public static final String EXPOSED = "AdvancedExposedWafer";
    public static final String EBEAM_EXPOSED = "EBeamExposedWafer";
    public static final String DEVELOPED = "AdvancedDevelopedWafer";
    public static final String ETCHED = "AdvancedEtchedWafer";
    public static final String STRIPPED = "AdvancedStrippedWafer";
    public static final String IMPLANTED = "ImplantedWafer";
    public static final String ANNEALED = "AnnealedWafer";
    public static final String PROCESSED = "AdvancedProcessedWafer";
    public static final String DIE = "AdvancedChipDie";
    public static final String CHIP = "AdvancedChip";
    public static final String LEAD_FRAME = "AdvancedLeadFrame";
    public static final String DIE_ATTACHED = "AdvancedDieAttachedFrame";
    public static final String BONDED = "AdvancedBondedFrame";
    public static final String MODULE = "PrecisionControlModule";
    public static final String TEMPLATE = "ProgramTemplate";

    public static final List<MaterialDeclaration> MATERIALS = List.of(
            waferStage(CLEANED, "Advanced Cleaned", 0x8FA8D8),
            waferStage(OXIDIZED, "Advanced Oxidized", 0xD0C0A0),
            waferStage(COATED, "Advanced Resist-Coated", 0xA86A38),
            waferStage(EXPOSED, "Advanced Mask-Exposed", 0x8A6AB8),
            waferStage(EBEAM_EXPOSED, "Electron-Beam Exposed", 0x7A8AD0),
            waferStage(DEVELOPED, "Advanced Developed", 0xC87898),
            waferStage(ETCHED, "Advanced Etched", 0x70A890),
            waferStage(STRIPPED, "Advanced Stripped", 0xA8B8C8),
            waferStage(IMPLANTED, "Ion-Implanted", 0xE0B050),
            waferStage(ANNEALED, "Implant-Annealed", 0xF0D070),
            waferStage(PROCESSED, "Advanced Processed", 0x7A8A96),
            MaterialDeclaration.builder(DIE)
                    .displayName("Advanced IC").color(0xB8C8D8).chemicalFormula("Si")
                    .meltingPointKelvin(1687).density(2.33F).build(),
            MaterialDeclaration.builder(LEAD_FRAME)
                    .displayName("Advanced Lead Frame").color(0xC8A860).chemicalFormula("CuZn·Sn·Ag")
                    .meltingPointKelvin(1200).density(8.60F).polymer().build(),
            MaterialDeclaration.builder(DIE_ATTACHED)
                    .displayName("Advanced Die-Attached Frame").color(0x8A9AA6).chemicalFormula("Si·CuZn")
                    .meltingPointKelvin(1200).density(5.10F).polymer().build(),
            MaterialDeclaration.builder(BONDED)
                    .displayName("Advanced Bonded Frame").color(0xE0C878).chemicalFormula("Si·CuZn·Au")
                    .meltingPointKelvin(1200).density(5.60F).polymer().build(),
            MaterialDeclaration.builder(CHIP)
                    .displayName("Advanced IC").color(0x5A5A68).chemicalFormula("Si·(C10H12O2)n")
                    .meltingPointKelvin(450).density(1.90F).build(),
            MaterialDeclaration.builder(MODULE)
                    .displayName("Precision Control").color(0x9FB8D8).chemicalFormula("Si·Cu·Au")
                    .meltingPointKelvin(1500).boilingPointKelvin(2400).density(4.8F).build(),
            MaterialDeclaration.builder(TEMPLATE)
                    .displayName("Advanced Lithography Program").color(0xD8E8F8).chemicalFormula("SiO2·Cr")
                    .meltingPointKelvin(1983).density(2.65F).build());

    public static final List<MaterialFormRequest> FORMS = List.of(
            new MaterialFormRequest(LEAD_FRAME, "plate"),
            new MaterialFormRequest(DIE_ATTACHED, "plate"),
            new MaterialFormRequest(BONDED, "plate"));

    /** One advanced wafer is half a material unit, matching the M9 wafer prefix. */
    public static final List<PrefixDeclaration> PREFIXES = List.of(
            new PrefixDeclaration("advancedWafer", "", " Wafer", GTValues.U / 2,
                    "item/wafer", List.of(CLEANED, OXIDIZED, COATED, EXPOSED, EBEAM_EXPOSED,
                            DEVELOPED, ETCHED, STRIPPED, IMPLANTED, ANNEALED, PROCESSED), Set.of()),
            new PrefixDeclaration("advancedDie", "", " Die", GTValues.U / 2,
                    "item/die", List.of(DIE), Set.of()),
            new PrefixDeclaration("advancedChip", "", " Package", GTValues.U / 2,
                    "item/chip", List.of(CHIP), Set.of()),
            new PrefixDeclaration("controlModule", "", " Module", GTValues.U * 4,
                    "item/chip", List.of(MODULE), Set.of()),
            new PrefixDeclaration("programTemplate", "", " Template", GTValues.U,
                    "item/wafer", List.of(TEMPLATE), Set.of()));

    private static MaterialDeclaration waferStage(String name, String display, int color) {
        return MaterialDeclaration.builder(name)
                .displayName(display).color(color).chemicalFormula("Si")
                .meltingPointKelvin(1687).boilingPointKelvin(3538).density(2.33F).build();
    }

    private AdvancedSemiconductorMaterials() {}
}
