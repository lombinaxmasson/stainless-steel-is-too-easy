package com.lombinaxmasson.ssite.material;

import gregtech.api.addon.material.MaterialDeclaration;
import gregtech.api.addon.material.PrefixDeclaration;
import gregtech.api.material.GTValues;

import java.util.List;
import java.util.Set;

/**
 * M9-10 optical media.
 *
 * <p>Nd:YAG keeps about 1 at% neodymium on the yttrium site; the mixer row in
 * {@code OpticsRecipes} carries that ratio. The host can only grow {@code bouleGt} for
 * materials with the BOULE property, and addon declarations cannot set properties, so the
 * doped crystal reuses the {@code crystalBoule} prefix that M9-04 declared for compounds.</p>
 *
 * <p>The host has no rod or lens prefix an addon material can be valid for: {@code stick}
 * needs the metal flags and {@code lens} needs GEM plus GENERATE_LENS. Both optical forms
 * therefore get their own prefix. Their grayscale sprites are the existing boule and wafer
 * textures, so this card adds no art; dedicated icons are left to M9-V.</p>
 */
public final class OpticsMaterials {
    public static final List<MaterialDeclaration> MATERIALS = List.of(
            MaterialDeclaration.builder("NeodymiumYAG")
                    .displayName("Neodymium-Doped YAG (Nd:YAG)").color(0xC8B8E8)
                    .chemicalFormula("Nd0.03Y2.97Al5O12")
                    .meltingPointKelvin(2240).boilingPointKelvin(3400).density(4.56F).dust().build());

    /** One rod is half an ingot like the host rod; one lens weighs three quarters. */
    public static final List<PrefixDeclaration> PREFIXES = List.of(
            new PrefixDeclaration("laserRod", "", " Laser Rod", GTValues.U2,
                    "item/crystal_boule", List.of("NeodymiumYAG"), Set.of()),
            new PrefixDeclaration("opticalLens", "", " Lens", GTValues.U * 3 / 4,
                    "item/wafer", List.of("NeodymiumYAG"), Set.of()));

    private OpticsMaterials() {}
}
