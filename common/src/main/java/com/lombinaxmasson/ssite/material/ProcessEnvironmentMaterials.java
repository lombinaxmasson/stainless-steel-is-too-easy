package com.lombinaxmasson.ssite.material;

import gregtech.api.addon.material.MaterialDeclaration;
import gregtech.api.addon.material.PrefixDeclaration;
import gregtech.api.material.GTValues;

import java.util.List;
import java.util.Set;

/**
 * P-04 replaceable filter cartridge.
 *
 * <p>The carrier has no forms of its own; the cartridge is a dedicated prefix so the vent slot
 * accepts exactly one item id. The icon reuses the existing hose texture and no new art is added.</p>
 */
public final class ProcessEnvironmentMaterials {
    public static final String CARRIER = "CleanroomFilter";

    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder(CARRIER)
                    .displayName("Cleanroom").color(0xE8F0F4)
                    .meltingPointKelvin(600).density(0.4F).build());

    public static final List<PrefixDeclaration> PREFIXES = List.of(
            new PrefixDeclaration("filterCartridge", "", " Filter Cartridge", GTValues.U,
                    "item/hose", List.of(CARRIER), Set.of()));

    private ProcessEnvironmentMaterials() {}
}
