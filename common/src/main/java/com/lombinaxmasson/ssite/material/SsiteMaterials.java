package com.lombinaxmasson.ssite.material;

import com.gregtech.gregtech.api.addon.AddonMaterialContributor;
import com.gregtech.gregtech.api.addon.CrucibleReactionDeclaration;
import com.gregtech.gregtech.api.addon.CrucibleReactionRemoval;
import com.gregtech.gregtech.api.addon.MaterialDeclaration;
import com.gregtech.gregtech.api.addon.MaterialFormRequest;
import com.lombinaxmasson.ssite.SsiteAddon;

import java.util.ArrayList;
import java.util.List;

/** Material declarations. GregTech loads this through ServiceLoader while materials are still open. */
public final class SsiteMaterials implements AddonMaterialContributor {
    @Override
    public String addonId() {
        return SsiteAddon.MOD_ID;
    }

    @Override
    public List<MaterialDeclaration> materials() {
        return List.of(
                MaterialDeclaration.builder("Ferrochromium")
                        .displayName("Ferrochromium").color(0xB4A8B8).chemicalFormula("FeCr2")
                        .meltingPointKelvin(2160).boilingPointKelvin(3000).density(7.2F).metal().build(),
                MaterialDeclaration.builder("Ferrosilicon")
                        .displayName("Ferrosilicon").color(0x7A8288).chemicalFormula("FeSi3")
                        .meltingPointKelvin(1683).boilingPointKelvin(2800).density(4.8F).metal().build(),
                MaterialDeclaration.builder("CarburizedStainlessSteel")
                        .displayName("Carburized Stainless Steel").color(0x8E929E).chemicalFormula("Fe6Cr2NiMnC2")
                        .meltingPointKelvin(1750).boilingPointKelvin(2950).density(7.7F).metal().build(),
                MaterialDeclaration.builder("StainlessSteel316L")
                        .displayName("316L Stainless Steel").color(0xD4DCE8).chemicalFormula("Fe6Cr2NiMo")
                        .meltingPointKelvin(1970).boilingPointKelvin(3100).density(8.0F).metal().build(),
                MaterialDeclaration.builder("SodiumChromate")
                        .displayName("Sodium Chromate").color(0xF0D830).chemicalFormula("Na2CrO4").dust().build(),
                MaterialDeclaration.builder("SodiumDichromate")
                        .displayName("Sodium Dichromate").color(0xE86818).chemicalFormula("Na2Cr2O7").dust().build(),
                MaterialDeclaration.builder("AodSlag")
                        .displayName("AOD Refining Slag").color(0x6E685C).chemicalFormula("CaSiO3·Cr2O3").dust().build());
    }

    @Override
    public List<MaterialFormRequest> forms() {
        return List.of();
    }

    /** An empty input list drops every built-in crucible row that outputs StainlessSteel. */
    @Override
    public List<CrucibleReactionRemoval> crucibleRemovals() {
        return List.of(new CrucibleReactionRemoval("StainlessSteel", List.of()));
    }

    /** Coarse high-carbon steel. The AOD converter, not the crucible, makes StainlessSteel. */
    @Override
    public List<CrucibleReactionDeclaration> crucibleReactions() {
        var rows = new ArrayList<CrucibleReactionDeclaration>();
        for (String iron : List.of("Iron", "WroughtIron", "PigIron", "MeteoricIron")) {
            rows.add(coarse(iron, 6, "Nickel", 1));
            rows.add(coarse(iron, 4, "Invar", 3));
        }
        return List.copyOf(rows);
    }

    private static CrucibleReactionDeclaration coarse(String iron, long ironRatio, String nickel, long nickelRatio) {
        return new CrucibleReactionDeclaration("CarburizedStainlessSteel", 9, List.of(
                new CrucibleReactionDeclaration.Part(iron, ironRatio),
                new CrucibleReactionDeclaration.Part(nickel, nickelRatio),
                new CrucibleReactionDeclaration.Part("Ferrochromium", 1),
                new CrucibleReactionDeclaration.Part("Manganese", 1)));
    }
}
