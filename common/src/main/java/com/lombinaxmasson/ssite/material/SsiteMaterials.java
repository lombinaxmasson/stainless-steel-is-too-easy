package com.lombinaxmasson.ssite.material;

import gregtech.api.addon.material.AddonMaterialContributor;
import gregtech.api.addon.recipe.CrucibleReactionDeclaration;
import gregtech.api.addon.recipe.CrucibleReactionRemoval;
import gregtech.api.addon.material.MaterialDeclaration;
import gregtech.api.addon.material.MaterialFormRequest;
import gregtech.api.addon.worldgen.OreVeinDeclaration;
import gregtech.api.addon.material.SmeltingTargetRemoval;
import gregtech.api.addon.platform.AddonItems;
import com.lombinaxmasson.ssite.SsiteAddon;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Material declarations. GregTech loads this through ServiceLoader while materials are still open. */
public final class SsiteMaterials implements AddonMaterialContributor {
    private static final String[] CREATIVE_PREFIXES = {
            "dust", "dustSmall", "dustTiny", "plate", "foil", "wireFine", "ingot"};

    public static final List<MaterialDeclaration> MATERIALS = buildMaterials();

    /**
     * Item stacks for the ssite creative tab, in declaration order across every milestone.
     * Forms a material does not generate resolve to empty and are skipped.
     */
    public static List<ItemStack> creativeStacks() {
        var stacks = new ArrayList<ItemStack>();
        for (MaterialDeclaration declaration : MATERIALS) {
            for (String prefix : CREATIVE_PREFIXES) {
                ItemStack stack = AddonItems.stack(prefix, declaration.name(), 1);
                if (!stack.isEmpty()) stacks.add(stack);
            }
        }
        return List.copyOf(stacks);
    }

    @Override
    public String addonId() {
        return SsiteAddon.MOD_ID;
    }

    @Override
    public List<MaterialDeclaration> materials() {
        return MATERIALS;
    }

    private static List<MaterialDeclaration> buildMaterials() {
        var materials = new ArrayList<>(List.of(
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
                        .meltingPointKelvin(996).boilingPointKelvin(1583).density(2.11F).dust().build(),
                MaterialDeclaration.builder("Naphtha")
                        .displayName("Naphtha").color(0xE8D890).chemicalFormula("C6H14")
                        .meltingPointKelvin(200).boilingPointKelvin(400).density(0.72F).liquid().build(),
                MaterialDeclaration.builder("Reformate")
                        .displayName("Reformate").color(0xE0B850).chemicalFormula("C7H8·C8H10")
                        .meltingPointKelvin(180).boilingPointKelvin(420).density(0.80F).liquid().build(),
                MaterialDeclaration.builder("PyrolysisGasoline")
                        .displayName("Pyrolysis Gasoline").color(0xC89838).chemicalFormula("C6H6·C7H8·C5H8")
                        .meltingPointKelvin(180).boilingPointKelvin(410).density(0.82F).liquid().build(),
                MaterialDeclaration.builder("CFourFraction")
                        .displayName("Crude C4 Fraction").color(0xD8E0A0).chemicalFormula("C4H6·C4H8")
                        .meltingPointKelvin(120).boilingPointKelvin(270).density(0.0024F).gas().build(),
                MaterialDeclaration.builder("Raffinate")
                        .displayName("Raffinate").color(0xF0E8B8).chemicalFormula("C5H12·C6H14")
                        .meltingPointKelvin(170).boilingPointKelvin(360).density(0.68F).liquid().build(),
                MaterialDeclaration.builder("Furfural")
                        .displayName("Furfural").color(0xA05A28).chemicalFormula("C5H4O2")
                        .meltingPointKelvin(236).boilingPointKelvin(435).density(1.16F).liquid().build(),
                MaterialDeclaration.builder("Sulfolane")
                        .displayName("Sulfolane").color(0xE8E8D8).chemicalFormula("C4H8O2S")
                        .meltingPointKelvin(301).boilingPointKelvin(558).density(1.26F).liquid().build(),
                MaterialDeclaration.builder("FurfuralExtract")
                        .displayName("Furfural Aromatic Extract").color(0x8A5A30).chemicalFormula("C5H4O2·C6H6")
                        .meltingPointKelvin(230).boilingPointKelvin(360).density(1.02F).liquid().build(),
                MaterialDeclaration.builder("SulfolaneExtract")
                        .displayName("Sulfolane Aromatic Extract").color(0xC8C0A0).chemicalFormula("C4H8O2S·C6H6")
                        .meltingPointKelvin(260).boilingPointKelvin(360).density(1.08F).liquid().build(),
                MaterialDeclaration.builder("BTEX")
                        .displayName("BTEX Aromatics").color(0xD8C8A8).chemicalFormula("C6H6·C7H8·C8H10")
                        .meltingPointKelvin(180).boilingPointKelvin(410).density(0.87F).liquid().build(),
                MaterialDeclaration.builder("Benzene")
                        .displayName("Benzene").color(0xE8E0C8).chemicalFormula("C6H6")
                        .meltingPointKelvin(279).boilingPointKelvin(353).density(0.876F).liquid().build(),
                MaterialDeclaration.builder("Toluene")
                        .displayName("Toluene").color(0xE0D8B8).chemicalFormula("C7H8")
                        .meltingPointKelvin(178).boilingPointKelvin(384).density(0.867F).liquid().build(),
                MaterialDeclaration.builder("Ethylbenzene")
                        .displayName("Ethylbenzene").color(0xD8D0B0).chemicalFormula("C8H10")
                        .meltingPointKelvin(178).boilingPointKelvin(409).density(0.867F).liquid().build(),
                MaterialDeclaration.builder("Xylene")
                        .displayName("Mixed Xylenes").color(0xD0C8A0).chemicalFormula("C8H10")
                        .meltingPointKelvin(225).boilingPointKelvin(411).density(0.86F).liquid().build(),
                MaterialDeclaration.builder("OrthoXylene")
                        .displayName("o-Xylene").color(0xC8B888).chemicalFormula("C6H4(CH3)2")
                        .meltingPointKelvin(248).boilingPointKelvin(417).density(0.88F).liquid().build(),
                MaterialDeclaration.builder("MetaParaXylene")
                        .displayName("m/p-Xylene Mix").color(0xD0C8A8).chemicalFormula("C6H4(CH3)2")
                        .meltingPointKelvin(225).boilingPointKelvin(412).density(0.86F).liquid().build(),
                MaterialDeclaration.builder("MetaXylene")
                        .displayName("m-Xylene").color(0xC0B8A0).chemicalFormula("C6H4(CH3)2")
                        .meltingPointKelvin(225).boilingPointKelvin(412).density(0.86F).liquid().build(),
                MaterialDeclaration.builder("ParaXylene")
                        .displayName("p-Xylene").color(0xF0ECE0).chemicalFormula("C6H4(CH3)2")
                        .meltingPointKelvin(286).boilingPointKelvin(411).density(0.861F).dust().liquid().build(),
                MaterialDeclaration.builder("Biphenyl")
                        .displayName("Biphenyl").color(0xF4F0E4).chemicalFormula("C12H10")
                        .meltingPointKelvin(342).boilingPointKelvin(528).density(1.04F).dust().build(),
                MaterialDeclaration.builder("Butene")
                        .displayName("Mixed Butenes").color(0xE0E8B0).chemicalFormula("C4H8")
                        .meltingPointKelvin(88).boilingPointKelvin(270).density(0.0025F).gas().build(),
                MaterialDeclaration.builder("OneButene")
                        .displayName("1-Butene").color(0xE8F0B8).chemicalFormula("CH2=CHC2H5")
                        .meltingPointKelvin(88).boilingPointKelvin(267).density(0.0025F).gas().build(),
                MaterialDeclaration.builder("TwoButene")
                        .displayName("2-Butene").color(0xD8E0A8).chemicalFormula("CH3CH=CHCH3")
                        .meltingPointKelvin(134).boilingPointKelvin(274).density(0.0025F).gas().build(),
                MaterialDeclaration.builder("Isobutylene")
                        .displayName("Isobutylene").color(0xE8E8C0).chemicalFormula("(CH3)2C=CH2")
                        .meltingPointKelvin(133).boilingPointKelvin(266).density(0.0025F).gas().build(),
                MaterialDeclaration.builder("Butadiene")
                        .displayName("1,3-Butadiene").color(0xC8D890).chemicalFormula("C4H6")
                        .meltingPointKelvin(164).boilingPointKelvin(269).density(0.0024F).gas().build(),
                MaterialDeclaration.builder("Acetaldehyde")
                        .displayName("Acetaldehyde").color(0xF0F0D8).chemicalFormula("C2H4O")
                        .meltingPointKelvin(150).boilingPointKelvin(294).density(0.784F).liquid().build(),
                MaterialDeclaration.builder("CFiveFraction")
                        .displayName("C5 Fraction").color(0xE8D8A0).chemicalFormula("C5H8·C5H6·C5H12")
                        .meltingPointKelvin(140).boilingPointKelvin(320).density(0.66F).liquid().build(),
                MaterialDeclaration.builder("DimerizedCFiveFraction")
                        .displayName("Dimerized C5 Fraction").color(0xD8C080).chemicalFormula("C5H8·C10H12·C5H12")
                        .meltingPointKelvin(140).boilingPointKelvin(440).density(0.74F).liquid().build(),
                MaterialDeclaration.builder("Isoprene")
                        .displayName("Isoprene").color(0xD0E0A0).chemicalFormula("C5H8")
                        .meltingPointKelvin(127).boilingPointKelvin(307).density(0.681F).liquid().build(),
                MaterialDeclaration.builder("Cyclopentadiene")
                        .displayName("Cyclopentadiene").color(0xE0E8C8).chemicalFormula("C5H6")
                        .meltingPointKelvin(183).boilingPointKelvin(314).density(0.80F).liquid().build(),
                MaterialDeclaration.builder("Dicyclopentadiene")
                        .displayName("Dicyclopentadiene").color(0xC8C8B0).chemicalFormula("C10H12")
                        .meltingPointKelvin(306).boilingPointKelvin(443).density(0.98F).liquid().build(),
                MaterialDeclaration.builder("Pentane")
                        .displayName("Pentane").color(0xF0F0E0).chemicalFormula("C5H12")
                        .meltingPointKelvin(143).boilingPointKelvin(309).density(0.626F).liquid().build(),
                MaterialDeclaration.builder("Norbornene")
                        .displayName("Norbornene").color(0xF8F8F0).chemicalFormula("C7H10")
                        .meltingPointKelvin(317).boilingPointKelvin(369).density(0.87F).dust().build(),
                MaterialDeclaration.builder("Cumene")
                        .displayName("Cumene").color(0xD8D0C0).chemicalFormula("C9H12")
                        .meltingPointKelvin(177).boilingPointKelvin(425).density(0.862F).liquid().build(),
                MaterialDeclaration.builder("CumeneHydroperoxide")
                        .displayName("Cumene Hydroperoxide").color(0xE8D0A0).chemicalFormula("C9H12O2")
                        .meltingPointKelvin(264).boilingPointKelvin(426).density(1.03F).liquid().build(),
                MaterialDeclaration.builder("Phenol")
                        .displayName("Phenol").color(0xF0E0E0).chemicalFormula("C6H5OH")
                        .meltingPointKelvin(314).boilingPointKelvin(455).density(1.07F).liquid().build(),
                MaterialDeclaration.builder("Acetone")
                        .displayName("Acetone").color(0xF0F4F8).chemicalFormula("C3H6O")
                        .meltingPointKelvin(178).boilingPointKelvin(329).density(0.784F).liquid().build(),
                MaterialDeclaration.builder("BisphenolA")
                        .displayName("Bisphenol A").color(0xF4F0E8).chemicalFormula("C15H16O2")
                        .meltingPointKelvin(431).boilingPointKelvin(493).density(1.20F).dust().build(),
                MaterialDeclaration.builder("ZSM5")
                        .displayName("ZSM-5 Zeolite").color(0xE8ECF0).chemicalFormula("(Na,H)AlSi23O48")
                        .meltingPointKelvin(1900).boilingPointKelvin(2500).density(1.80F).dust().build()));
        materials.addAll(PolymerMaterials.MATERIALS);
        materials.addAll(MetallurgyMaterials.MATERIALS);
        materials.addAll(RareEarthMaterials.MATERIALS);
        materials.addAll(NuclearMaterials.MATERIALS);
        materials.addAll(BaseMetalMaterials.MATERIALS);
        return List.copyOf(materials);
    }

    @Override
    public List<MaterialFormRequest> forms() {
        var forms = new ArrayList<>(List.of(
                new MaterialFormRequest("CelluloseAcetate", "plate"),
                new MaterialFormRequest("CelluloseAcetate", "foil")));
        forms.addAll(PolymerMaterials.FORMS);
        return List.copyOf(forms);
    }

    /** These ores melt into a metal in the built-in crucible table; the cards replace them with real routes. */
    @Override
    public List<SmeltingTargetRemoval> smeltingTargetRemovals() {
        return List.of(new SmeltingTargetRemoval("Bastnasite"),
                new SmeltingTargetRemoval("Uraninite"),
                new SmeltingTargetRemoval("Pitchblende"),
                new SmeltingTargetRemoval("Chalcopyrite"),
                new SmeltingTargetRemoval("Pentlandite"),
                new SmeltingTargetRemoval("Sphalerite"),
                new SmeltingTargetRemoval("Galena"),
                new SmeltingTargetRemoval("Molybdenite"),
                new SmeltingTargetRemoval("Cooperite"),
                new SmeltingTargetRemoval("Sperrylite"),
                new SmeltingTargetRemoval("Stibnite"));
    }

    @Override
    public List<OreVeinDeclaration> oreVeins() {
        return List.of(new OreVeinDeclaration("xenotime", OreVeinDeclaration.OVERWORLD,
                0, 30, 20, 3, 16, "Xenotime", "Xenotime", "Monazite", "RareEarth"));
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
