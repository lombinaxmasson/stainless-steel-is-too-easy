package com.lombinaxmasson.ssite.material;

import gregtech.api.addon.material.MaterialDeclaration;
import gregtech.api.addon.material.PrefixDeclaration;
import gregtech.api.material.GTValues;

import java.util.List;
import java.util.Set;

/**
 * Record media, blank templates and manufacturing intermediates.
 *
 * <p>The playable records are custom items because the host has no record item; every other
 * intermediate is an addon prefix item so it keeps a concrete id and an automatic icon. The
 * template, cut master and stamper carry a server-library track in their item data.</p>
 */
public final class RecordMaterials {
    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder("Vinyl")
                    .displayName("Vinyl").color(0x18181C).chemicalFormula("(C2H3Cl)n·(C4H6O2)m")
                    .meltingPointKelvin(350).density(1.30F).dust().build(),
            MaterialDeclaration.builder("TrackOne")
                    .displayName("Recording").color(0xD8B050).build(),
            MaterialDeclaration.builder("PhthalocyanineDye")
                    .displayName("Phthalocyanine Dye").color(0x1E7A6E).chemicalFormula("C32H16CuN8")
                    .meltingPointKelvin(600).density(1.60F).dust().build(),
            MaterialDeclaration.builder("Urea")
                    .displayName("Urea").color(0xF2F2EA).chemicalFormula("CO(NH2)2")
                    .meltingPointKelvin(406).density(1.32F).dust().build(),
            MaterialDeclaration.builder("CopperSulfate")
                    .displayName("Copper Sulfate").color(0x2A6AC8).chemicalFormula("CuSO4")
                    .meltingPointKelvin(833).density(3.60F).dust().build(),
            MaterialDeclaration.builder("Nitrocellulose")
                    .displayName("Nitrocellulose").color(0xE8E0C8).chemicalFormula("(C6H7N3O11)n")
                    .meltingPointKelvin(450).density(1.65F).dust().build(),
            MaterialDeclaration.builder("StearicAcid")
                    .displayName("Stearic Acid").color(0xF4F0E0).chemicalFormula("C18H36O2")
                    .meltingPointKelvin(343).density(0.94F).dust().build(),
            MaterialDeclaration.builder("CalciumStearate")
                    .displayName("Calcium Stearate").color(0xF0ECE0).chemicalFormula("Ca(C18H35O2)2")
                    .meltingPointKelvin(420).density(1.03F).dust().build(),
            MaterialDeclaration.builder("DyeCoatedDisc")
                    .displayName("Dye-Coated").color(0x2E8C7A).build(),
            MaterialDeclaration.builder("ReflectiveDisc")
                    .displayName("Reflective").color(0xC8D4E4).build(),
            MaterialDeclaration.builder("CDR")
                    .displayName("CD-R").color(0xE8D8B0).build());

    /**
     * The blank vinyl and the four CD-R stages are separate ids. A bare polycarbonate dust or a
     * die-coated disc cannot be substituted for the next stage because the recipe rows name the
     * exact prefix item.
     */
    public static final List<PrefixDeclaration> PREFIXES = List.of(
            new PrefixDeclaration("recordBlank", "", " Blank Record", GTValues.U,
                    "item/wafer", List.of("Vinyl"), Set.of()),
            new PrefixDeclaration("recordLacquer", "", " Lacquer Master", GTValues.U,
                    "item/wafer", List.of("Aluminium"), Set.of()),
            new PrefixDeclaration("recordCutMaster", "Cut ", " Master", GTValues.U,
                    "item/wafer", List.of("Aluminium"), Set.of()),
            new PrefixDeclaration("recordStamper", "", " Stamper", GTValues.U,
                    "item/wafer", List.of("Nickel"), Set.of()),
            new PrefixDeclaration("recordStylus", "", " Cutting Stylus", GTValues.U,
                    "item/laser_rod", List.of("BlueSapphire"), Set.of()),
            new PrefixDeclaration("recordTemplate", "", " Template", GTValues.U,
                    "item/chip", List.of("TrackOne"), Set.of()),
            new PrefixDeclaration("discBase", "", " Disc Base", GTValues.U / 2,
                    "item/wafer", List.of("Polycarbonate"), Set.of()),
            new PrefixDeclaration("discCoated", "", " Disc", GTValues.U / 2,
                    "item/wafer", List.of("DyeCoatedDisc"), Set.of()),
            new PrefixDeclaration("discReflective", "", " Disc", GTValues.U / 2,
                    "item/wafer", List.of("ReflectiveDisc"), Set.of()),
            new PrefixDeclaration("discBlank", "Blank ", "", GTValues.U / 2,
                    "item/wafer", List.of("CDR"), Set.of()));

    private RecordMaterials() {}
}
