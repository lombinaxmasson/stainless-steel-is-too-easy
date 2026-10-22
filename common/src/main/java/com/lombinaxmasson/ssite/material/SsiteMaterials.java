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
                        .displayName("AOD Refining Slag").color(0x6E685C).chemicalFormula("CaSiO3·Cr2O3").dust().build(),
                MaterialDeclaration.builder("ClarifiedWater")
                        .displayName("Clarified Water").color(0x5B9BD5).chemicalFormula("H2O")
                        .meltingPointKelvin(273).boilingPointKelvin(373).density(1.0F).liquid().build(),
                MaterialDeclaration.builder("DemineralizedWater")
                        .displayName("Demineralized Water").color(0x6FB1E8).chemicalFormula("H2O")
                        .meltingPointKelvin(273).boilingPointKelvin(373).density(1.0F).liquid().build(),
                MaterialDeclaration.builder("DeionizedWater")
                        .displayName("Deionized Water").color(0x88C6F5).chemicalFormula("H2O")
                        .meltingPointKelvin(273).boilingPointKelvin(373).density(1.0F).liquid().build(),
                MaterialDeclaration.builder("FilteredWater")
                        .displayName("Filtered Water").color(0xA6DAFF).chemicalFormula("H2O")
                        .meltingPointKelvin(273).boilingPointKelvin(373).density(1.0F).liquid().build(),
                MaterialDeclaration.builder("UltrapureWater")
                        .displayName("Ultrapure Water").color(0xC8ECFF).chemicalFormula("H2O")
                        .meltingPointKelvin(273).boilingPointKelvin(373).density(1.0F).liquid().build(),
                MaterialDeclaration.builder("Alum")
                        .displayName("Potassium Alum").color(0xE8EEF2).chemicalFormula("KAl(SO4)2·12H2O")
                        .meltingPointKelvin(365).boilingPointKelvin(473).density(1.72F).dust().build(),
                MaterialDeclaration.builder("CelluloseAcetate")
                        .displayName("Cellulose Acetate").color(0xF5F5EB).chemicalFormula("C6H7O2(OH)(OAc)2")
                        .meltingPointKelvin(503).boilingPointKelvin(650).density(1.30F).dust().polymer().build(),
                MaterialDeclaration.builder("Ozone")
                        .displayName("Ozone").color(0x70B8FF).chemicalFormula("O3")
                        .meltingPointKelvin(81).boilingPointKelvin(161).density(0.00214F).gas().build(),
                MaterialDeclaration.builder("CalciumHydroxide")
                        .displayName("Calcium Hydroxide").color(0xF2EFE9).chemicalFormula("Ca(OH)2")
                        .meltingPointKelvin(853).boilingPointKelvin(2800).density(2.21F).dust().build(),
                MaterialDeclaration.builder("MagnesiumHydroxide")
                        .displayName("Magnesium Hydroxide").color(0xF5F0FA).chemicalFormula("Mg(OH)2")
                        .meltingPointKelvin(623).boilingPointKelvin(2800).density(2.34F).dust().build(),
                MaterialDeclaration.builder("DecalcifiedBrine")
                        .displayName("Decalcified Brine").color(0xB8E0D2).chemicalFormula("NaCl·Br·Li")
                        .meltingPointKelvin(255).boilingPointKelvin(380).density(1.18F).liquid().build(),
                MaterialDeclaration.builder("CrudeBromine")
                        .displayName("Crude Bromine Vapor").color(0x7A2018).chemicalFormula("Br2·Cl2·H2O")
                        .meltingPointKelvin(265).boilingPointKelvin(340).density(0.0035F).gas().build(),
                MaterialDeclaration.builder("LithiumRichBrine")
                        .displayName("Lithium-Rich Brine").color(0xD4C8F0).chemicalFormula("LiCl·NaCl")
                        .meltingPointKelvin(250).boilingPointKelvin(385).density(1.22F).liquid().build(),
                MaterialDeclaration.builder("LithiumCarbonate")
                        .displayName("Lithium Carbonate").color(0xF0EDF8).chemicalFormula("Li2CO3")
                        .meltingPointKelvin(996).boilingPointKelvin(1583).density(2.11F).dust().build());
    }

    @Override
    public List<MaterialFormRequest> forms() {
        return List.of(
                new MaterialFormRequest("CelluloseAcetate", "plate"),
                new MaterialFormRequest("CelluloseAcetate", "foil"));
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
