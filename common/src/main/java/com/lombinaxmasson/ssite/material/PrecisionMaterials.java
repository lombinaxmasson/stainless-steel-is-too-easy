package com.lombinaxmasson.ssite.material;

import gregtech.api.addon.material.MaterialDeclaration;
import gregtech.api.addon.material.PrefixDeclaration;
import gregtech.api.material.GTValues;

import java.util.List;
import java.util.Set;

/**
 * P-01 precision laser workpieces and the solid-state laser module.
 *
 * <p>The first production batch uses a single alloy, {@code StainlessSteel316L}, that the existing
 * AOD route already supplies. The three workpieces are addon prefixes on that alloy rather than
 * new materials so their ids keep the alloy identity. The module and the interchangeable cutting,
 * drilling and welding heads are separate materials with one prefix each.</p>
 */
public final class PrecisionMaterials {
    public static final String ALLOY = "StainlessSteel316L";

    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder("LaserModule")
                    .displayName("Solid-State Laser").color(0x9FB8D8)
                    .meltingPointKelvin(1800).boilingPointKelvin(2600).density(4.2F).build(),
            MaterialDeclaration.builder("LaserHead")
                    .displayName("Laser Head").color(0x7F98B8)
                    .meltingPointKelvin(1800).boilingPointKelvin(2600).density(4.2F).build());

    /**
     * The optical module is a build component, not a consumable. The three heads are kept inputs
     * in the workstation rows, so a wrong head cannot match a mode. Item textures reuse the
     * existing optical and machined-part icons; no new art is introduced.
     */
    public static final List<PrefixDeclaration> PREFIXES = List.of(
            new PrefixDeclaration("laserModule", "", " Module", GTValues.U * 4,
                    "item/chip", List.of("LaserModule"), Set.of()),
            new PrefixDeclaration("laserCuttingHead", "Cutting ", "", GTValues.U2,
                    "item/optical_lens", List.of("LaserHead"), Set.of()),
            new PrefixDeclaration("laserDrillingHead", "Drilling ", "", GTValues.U2,
                    "item/optical_lens", List.of("LaserHead"), Set.of()),
            new PrefixDeclaration("laserWeldingHead", "Welding ", "", GTValues.U2,
                    "item/optical_lens", List.of("LaserHead"), Set.of()),
            new PrefixDeclaration("precisionNozzle", "", " Precision Nozzle", GTValues.U,
                    "item/hose", List.of(ALLOY), Set.of()),
            new PrefixDeclaration("precisionFixture", "", " Precision Fixture", GTValues.U2,
                    "item/chip", List.of(ALLOY), Set.of()),
            new PrefixDeclaration("instrumentShell", "", " Instrument Shell", GTValues.U * 2,
                    "item/wafer", List.of(ALLOY), Set.of()));

    private PrecisionMaterials() {}
}
