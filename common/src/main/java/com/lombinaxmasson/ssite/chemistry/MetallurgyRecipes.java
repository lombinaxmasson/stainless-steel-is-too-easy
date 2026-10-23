package com.lombinaxmasson.ssite.chemistry;

import com.gregtech.gregtech.api.addon.platform.AddonRecipes;
import com.lombinaxmasson.ssite.machine.SsiteMachines;
import net.minecraft.world.item.ItemStack;

/** M5 metallurgy recipes. Each M5 card adds one method; {@link #add()} wires them all. */
public final class MetallurgyRecipes {
    private MetallurgyRecipes() {}

    static void add() {
        bayerAluminiumGallium();
        titaniumVanadiumKroll();
        zirconiumHafniumMibk();
        niobiumTantalumMibk();
        varDisplay();
    }

    private static void titaniumVanadiumKroll() {
        titaniumChlorination();
        vanadiumRecovery();
        kroll();
    }

    private static void titaniumChlorination() {
        for (String carbon : new String[]{"Carbon", "CoalCoke"}) {
            ChemistryRecipes.recipe("gt.recipe.calciner", "titanium/rutile-" + carbon,
                    ChemistryRecipes.items(ChemistryRecipes.dust("Rutile", 1),
                            ChemistryRecipes.dust(carbon, 2)),
                    ChemistryRecipes.items(),
                    ChemistryRecipes.fields("GenGas_Chlorine"), ChemistryRecipes.amounts(4000),
                    ChemistryRecipes.fields("GenLiquid_CrudeTitaniumTetrachloride", "GenGas_CarbonMonoxide"),
                    ChemistryRecipes.amounts(1000, 2000),
                    160, 64);
            ChemistryRecipes.recipe("gt.recipe.calciner", "titanium/ilmenite-" + carbon,
                    ChemistryRecipes.items(ChemistryRecipes.dust("Ilmenite", 1),
                            ChemistryRecipes.dust(carbon, 3)),
                    ChemistryRecipes.items(ChemistryRecipes.dust("FerricChloride", 1)),
                    ChemistryRecipes.fields("GenGas_Chlorine"), ChemistryRecipes.amounts(7000),
                    ChemistryRecipes.fields("GenLiquid_CrudeTitaniumTetrachloride", "GenGas_CarbonMonoxide"),
                    ChemistryRecipes.amounts(1000, 3000),
                    200, 64);
        }
        ChemistryRecipes.recipe("gt.recipe.calciner", "titanium/ferric-chloride",
                ChemistryRecipes.items(ChemistryRecipes.dust("FerricChloride", 2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Hematite", 1)),
                ChemistryRecipes.fields("Oxygen"), ChemistryRecipes.amounts(1500),
                ChemistryRecipes.fields("GenGas_Chlorine"), ChemistryRecipes.amounts(3000),
                120, 48);
    }

    private static void vanadiumRecovery() {
        ChemistryRecipes.recipe("gt.recipe.distillationtower", "titanium/vanadium-tower",
                ChemistryRecipes.items(),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_CrudeTitaniumTetrachloride"), ChemistryRecipes.amounts(10000),
                ChemistryRecipes.fields("GenLiquid_TitaniumTetrachloride", "GenLiquid_VanadiumOxytrichloride"),
                ChemistryRecipes.amounts(9500, 500),
                200, 64);
        ChemistryRecipes.recipe("gt.recipe.distillery", "titanium/vanadium-still",
                ChemistryRecipes.items(ChemistryRecipes.circuit(1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_CrudeTitaniumTetrachloride"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_TitaniumTetrachloride", "GenLiquid_VanadiumOxytrichloride"),
                ChemistryRecipes.amounts(900, 100),
                100, 48, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "vanadium/oxide",
                ChemistryRecipes.items(),
                ChemistryRecipes.items(ChemistryRecipes.dust("VanadiumPentoxide", 1)),
                ChemistryRecipes.fields("GenLiquid_VanadiumOxytrichloride", "DistW"),
                ChemistryRecipes.amounts(2000, 3000),
                ChemistryRecipes.fields("GenGas_HydrochloricAcid"), ChemistryRecipes.amounts(6000),
                160, 48);
        ChemistryRecipes.recipe("gt.recipe.calciner", "vanadium/magnetite",
                ChemistryRecipes.items(ChemistryRecipes.dust("VanadiumMagnetite", 1),
                        ChemistryRecipes.dust("SodiumCarbonate", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Hematite", 1),
                        ChemistryRecipes.dust("VanadiumPentoxide", 1)),
                ChemistryRecipes.fields("Oxygen"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("CarbonDioxide"), ChemistryRecipes.amounts(1000),
                200, 64);
        ChemistryRecipes.recipe("gt.recipe.calciner", "vanadium/calcium",
                ChemistryRecipes.items(ChemistryRecipes.dust("VanadiumPentoxide", 1),
                        ChemistryRecipes.dust("Calcium", 5)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Vanadium", 2),
                        ChemistryRecipes.dust("Quicklime", 5)),
                null, null, null, null,
                200, 96);
        ChemistryRecipes.recipe("gt.recipe.calciner", "vanadium/aluminium",
                ChemistryRecipes.items(ChemistryRecipes.dust("VanadiumPentoxide", 3),
                        ChemistryRecipes.dust("Aluminium", 10)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Vanadium", 6),
                        ChemistryRecipes.dust("Alumina", 5)),
                null, null, null, null,
                240, 96);
    }

    private static void kroll() {
        ChemistryRecipes.recipe("gt.recipe.calciner", "titanium/kroll",
                ChemistryRecipes.items(ChemistryRecipes.dust("Magnesium", 2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("TitaniumSponge", 1),
                        ChemistryRecipes.dust("MagnesiumChloride", 2)),
                ChemistryRecipes.fields("GenLiquid_TitaniumTetrachloride", "GenGas_Argon"),
                ChemistryRecipes.amounts(1000, 100),
                ChemistryRecipes.fields("GenGas_Argon"), ChemistryRecipes.amounts(100),
                240, 96);
    }

    private static void zirconiumHafniumMibk() {
        zirconChlorination();
        zirconHafniumSeparation();
        zirconiumHafniumKroll();
    }

    private static void zirconChlorination() {
        for (String carbon : new String[]{"Carbon", "CoalCoke"}) {
            ChemistryRecipes.recipe("gt.recipe.calciner", "zircon/chloride-" + carbon,
                    ChemistryRecipes.items(ChemistryRecipes.dust("Zircon", 1),
                            ChemistryRecipes.dust(carbon, 4)),
                    ChemistryRecipes.items(ChemistryRecipes.dust("CrudeZirconiumTetrachloride", 1)),
                    ChemistryRecipes.fields("GenGas_Chlorine"), ChemistryRecipes.amounts(4000),
                    ChemistryRecipes.fields("GenLiquid_SiliconTetrachloride", "GenGas_CarbonMonoxide"),
                    ChemistryRecipes.amounts(1000, 4000),
                    200, 96);
        }
        ChemistryRecipes.recipe("gt.recipe.mixer", "zircon/sicl4-hydrolysis",
                ChemistryRecipes.items(),
                ChemistryRecipes.items(ChemistryRecipes.dust("SiliconDioxide", 1)),
                ChemistryRecipes.fields("GenLiquid_SiliconTetrachloride", "DistW"),
                ChemistryRecipes.amounts(1000, 2000),
                ChemistryRecipes.fields("GenGas_HydrochloricAcid"), ChemistryRecipes.amounts(4000),
                100, 32);
    }

    private static void zirconHafniumSeparation() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "zircon/hydrolysis",
                ChemistryRecipes.items(ChemistryRecipes.dust("CrudeZirconiumTetrachloride", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_DeionizedWater"), ChemistryRecipes.amounts(2000),
                ChemistryRecipes.fields("GenLiquid_ZirconylChlorideSolution", "GenGas_HydrochloricAcid"),
                ChemistryRecipes.amounts(1000, 2000),
                120, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "zircon/mibk-extract",
                ChemistryRecipes.items(),
                ChemistryRecipes.items(ChemistryRecipes.dust("Zirconia", 4)),
                ChemistryRecipes.fields("GenLiquid_ZirconylChlorideSolution",
                        "GenLiquid_MethylIsobutylKetone", "GenGas_Ammonia"),
                ChemistryRecipes.amounts(5000, 1000, 1000),
                ChemistryRecipes.fields("GenLiquid_HafniumExtract", "Dirty_Water"),
                ChemistryRecipes.amounts(1000, 4000),
                200, 64);
        // The centrifuge has one fluid input; the mixer fits the acid strip and MIBK return.
        ChemistryRecipes.recipe("gt.recipe.mixer", "zircon/hafnium-strip",
                ChemistryRecipes.items(),
                ChemistryRecipes.items(ChemistryRecipes.dust("Hafnia", 1)),
                ChemistryRecipes.fields("GenLiquid_HafniumExtract", "GenLiquid_SulfuricAcid"),
                ChemistryRecipes.amounts(1000, 500),
                ChemistryRecipes.fields("GenLiquid_MethylIsobutylKetone", "Dirty_Water"),
                ChemistryRecipes.amounts(950, 500),
                160, 64);
    }

    private static void zirconiumHafniumKroll() {
        for (String carbon : new String[]{"Carbon", "CoalCoke"}) {
            ChemistryRecipes.recipe("gt.recipe.calciner", "zirconia/chloride-" + carbon,
                    ChemistryRecipes.items(ChemistryRecipes.dust("Zirconia", 1),
                            ChemistryRecipes.dust(carbon, 2)),
                    ChemistryRecipes.items(ChemistryRecipes.dust("ZirconiumTetrachloride", 1)),
                    ChemistryRecipes.fields("GenGas_Chlorine"), ChemistryRecipes.amounts(4000),
                    ChemistryRecipes.fields("GenGas_CarbonMonoxide"), ChemistryRecipes.amounts(2000),
                    160, 64);
            ChemistryRecipes.recipe("gt.recipe.calciner", "hafnia/chloride-" + carbon,
                    ChemistryRecipes.items(ChemistryRecipes.dust("Hafnia", 1),
                            ChemistryRecipes.dust(carbon, 2)),
                    ChemistryRecipes.items(ChemistryRecipes.dust("HafniumTetrachloride", 1)),
                    ChemistryRecipes.fields("GenGas_Chlorine"), ChemistryRecipes.amounts(4000),
                    ChemistryRecipes.fields("GenGas_CarbonMonoxide"), ChemistryRecipes.amounts(2000),
                    160, 64);
        }
        ChemistryRecipes.recipe("gt.recipe.calciner", "zirconium/kroll",
                ChemistryRecipes.items(ChemistryRecipes.dust("ZirconiumTetrachloride", 1),
                        ChemistryRecipes.dust("Magnesium", 2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("ZirconiumSponge", 1),
                        ChemistryRecipes.dust("MagnesiumChloride", 2)),
                ChemistryRecipes.fields("GenGas_Argon"), ChemistryRecipes.amounts(100),
                ChemistryRecipes.fields("GenGas_Argon"), ChemistryRecipes.amounts(100),
                240, 96);
        ChemistryRecipes.recipe("gt.recipe.calciner", "hafnium/kroll",
                ChemistryRecipes.items(ChemistryRecipes.dust("HafniumTetrachloride", 1),
                        ChemistryRecipes.dust("Magnesium", 2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("HafniumSponge", 1),
                        ChemistryRecipes.dust("MagnesiumChloride", 2)),
                ChemistryRecipes.fields("GenGas_Argon"), ChemistryRecipes.amounts(100),
                ChemistryRecipes.fields("GenGas_Argon"), ChemistryRecipes.amounts(100),
                240, 120);
    }

    private static void niobiumTantalumMibk() {
        niobiumTantalumLeaching();
        niobiumTantalumSeparation();
        niobiumTantalumReduction();
    }

    private static void niobiumTantalumLeaching() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "coltan/leach",
                ChemistryRecipes.items(ChemistryRecipes.dust("Coltan", 2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Pyrolusite", 1)),
                ChemistryRecipes.fields("GenGas_HydrogenFluoride", "GenLiquid_SulfuricAcid"),
                ChemistryRecipes.amounts(6000, 1000),
                ChemistryRecipes.fields("GenLiquid_NbTaLeachLiquor", "Dirty_Water"),
                ChemistryRecipes.amounts(2000, 1000),
                200, 64);
        ChemistryRecipes.recipe("gt.recipe.calciner", "columbite/leach",
                ChemistryRecipes.items(ChemistryRecipes.dust("Columbite", 2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("NiobiumPentoxide", 1),
                        ChemistryRecipes.dust("Pyrolusite", 1)),
                ChemistryRecipes.fields("GenGas_HydrogenFluoride", "GenLiquid_SulfuricAcid"),
                ChemistryRecipes.amounts(4000, 1000),
                ChemistryRecipes.fields("GenLiquid_NbTaLeachLiquor"), ChemistryRecipes.amounts(1000),
                200, 64);
        ChemistryRecipes.recipe("gt.recipe.calciner", "tantalite/leach",
                ChemistryRecipes.items(ChemistryRecipes.dust("Tantalite", 2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("TantalumPentoxide", 1),
                        ChemistryRecipes.dust("Pyrolusite", 1)),
                ChemistryRecipes.fields("GenGas_HydrogenFluoride", "GenLiquid_SulfuricAcid"),
                ChemistryRecipes.amounts(4000, 1000),
                ChemistryRecipes.fields("GenLiquid_NbTaLeachLiquor"), ChemistryRecipes.amounts(1000),
                200, 64);
        ChemistryRecipes.recipe("gt.recipe.mixer", "tantalum/k2taf7",
                ChemistryRecipes.items(ChemistryRecipes.dust("TantalumPentoxide", 1),
                        ChemistryRecipes.dust("PotassiumFluoride", 2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("PotassiumHeptafluorotantalate", 2)),
                ChemistryRecipes.fields("GenGas_HydrogenFluoride"), ChemistryRecipes.amounts(5000),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(3000),
                160, 64);
    }

    private static void niobiumTantalumSeparation() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "niobium-tantalum/extract",
                ChemistryRecipes.items(),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_NbTaLeachLiquor", "GenLiquid_MethylIsobutylKetone"),
                ChemistryRecipes.amounts(2000, 2000),
                ChemistryRecipes.fields("GenLiquid_NbTaMibkExtract", "Dirty_Water"),
                ChemistryRecipes.amounts(2000, 2000),
                160, 64);
        ChemistryRecipes.recipe("gt.recipe.mixer", "niobium/strip",
                ChemistryRecipes.items(),
                ChemistryRecipes.items(ChemistryRecipes.dust("NiobiumPentoxide", 1)),
                ChemistryRecipes.fields("GenLiquid_NbTaMibkExtract", "GenLiquid_DeionizedWater"),
                ChemistryRecipes.amounts(2000, 1000),
                ChemistryRecipes.fields("GenLiquid_TantalumRichMibk", "GenGas_HydrogenFluoride"),
                ChemistryRecipes.amounts(2000, 2000),
                160, 64);
        ChemistryRecipes.recipe("gt.recipe.centrifuge", "tantalum/strip",
                ChemistryRecipes.items(ChemistryRecipes.dust("PotassiumFluoride", 2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("PotassiumHeptafluorotantalate", 1)),
                ChemistryRecipes.fields("GenLiquid_TantalumRichMibk"), ChemistryRecipes.amounts(2000),
                ChemistryRecipes.fields("GenLiquid_MethylIsobutylKetone", "GenGas_HydrogenFluoride"),
                ChemistryRecipes.amounts(1900, 1000),
                160, 64);
    }

    private static void niobiumTantalumReduction() {
        ChemistryRecipes.recipe("gt.recipe.calciner", "niobium/aluminothermy",
                ChemistryRecipes.items(ChemistryRecipes.dust("NiobiumPentoxide", 1),
                        ChemistryRecipes.dust("Aluminium", 2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Niobium", 2),
                        ChemistryRecipes.dust("Alumina", 1)),
                null, null, null, null,
                240, 96);
        ChemistryRecipes.recipe("gt.recipe.calciner", "tantalum/sodium",
                ChemistryRecipes.items(ChemistryRecipes.dust("PotassiumHeptafluorotantalate", 1),
                        ChemistryRecipes.dust("Sodium", 5)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Tantalum", 1),
                        ChemistryRecipes.dust("PotassiumFluoride", 2),
                        ChemistryRecipes.dust("SodiumFluoride", 5)),
                null, null, null, null,
                240, 96);
        ChemistryRecipes.recipe("gt.recipe.mixer", "sodium-fluoride/hf",
                ChemistryRecipes.items(ChemistryRecipes.dust("SodiumFluoride", 2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("SodiumSulfate", 1)),
                ChemistryRecipes.fields("GenLiquid_SulfuricAcid"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenGas_HydrogenFluoride"), ChemistryRecipes.amounts(2000),
                100, 32);
    }

    /** The furnace runs these in its behavior; the rows only document the ratios and gas use. */
    private static void varDisplay() {
        display("var/titanium", ChemistryRecipes.items(ChemistryRecipes.dust("TitaniumSponge", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Titanium", 1)), 100);
        display("var/zirconium", ChemistryRecipes.items(ChemistryRecipes.dust("ZirconiumSponge", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Zirconium", 1)), 100);
        display("var/hafnium", ChemistryRecipes.items(ChemistryRecipes.dust("HafniumSponge", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Hafnium", 1)), 100);
        display("var/niobium-titanium",
                ChemistryRecipes.items(ChemistryRecipes.dust("Niobium", 1), ChemistryRecipes.dust("Titanium", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("NiobiumTitanium", 2)), 100);
        display("var/ta4hfc5",
                ChemistryRecipes.items(ChemistryRecipes.dust("Tantalum", 4),
                        ChemistryRecipes.dust("Hafnium", 1), ChemistryRecipes.dust("Carbon", 5)),
                ChemistryRecipes.items(ChemistryRecipes.dust("TantalumHafniumCarbide", 10)), 200);
    }

    private static void display(String label, ItemStack[] itemInputs, ItemStack[] itemOutputs, int argonMillibuckets) {
        AddonRecipes.Line line = AddonRecipes.map(SsiteMachines.VAR_MAP).addDisplay(
                FluidBridge.line(itemInputs, itemOutputs,
                        ChemistryRecipes.fields("GenGas_Argon"), ChemistryRecipes.amounts(argonMillibuckets),
                        null, null, 200, 128));
        if (line == null) ChemistryRecipes.REJECTED.add(label);
    }

    private static void bayerAluminiumGallium() {
        bayerDigestion();
        redMudRecovery();
        gallium();
        aluminiumFluoride();
        hallHeroult();
    }

    private static void bayerDigestion() {
        ChemistryRecipes.recipe("gt.recipe.autoclave", "bayer/digest",
                ChemistryRecipes.items(
                        ChemistryRecipes.dust("Bauxite", 2),
                        ChemistryRecipes.dust("SodiumHydroxide", 2)),
                ChemistryRecipes.items(
                        ChemistryRecipes.dust("SodiumAluminate", 2),
                        ChemistryRecipes.dust("RedMud", 1)),
                ChemistryRecipes.fields("Steam"), ChemistryRecipes.amounts(4000),
                ChemistryRecipes.fields("GenLiquid_BayerLiquor"), ChemistryRecipes.amounts(1000),
                200, 64);
        ChemistryRecipes.recipe("gt.recipe.calciner", "bayer/calcine",
                ChemistryRecipes.items(ChemistryRecipes.dust("AluminiumHydroxide", 2),
                        ChemistryRecipes.circuit(1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Alumina", 1)),
                null, null,
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(3000),
                120, 48, null, new int[]{1});
    }

    private static void redMudRecovery() {
        ChemistryRecipes.recipe("gt.recipe.bath", "bayer/red-mud",
                ChemistryRecipes.items(ChemistryRecipes.dust("RedMud", 3)),
                ChemistryRecipes.items(
                        ChemistryRecipes.dust("Hematite", 1),
                        ChemistryRecipes.dust("Rutile", 1),
                        ChemistryRecipes.dust("SiliconDioxide", 1)),
                ChemistryRecipes.fields("GenLiquid_SulfuricAcid"), ChemistryRecipes.amounts(2000),
                ChemistryRecipes.fields("Dirty_Water"), ChemistryRecipes.amounts(1000),
                160, 32);
    }

    private static void gallium() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "gallium/carbonate",
                ChemistryRecipes.items(),
                ChemistryRecipes.items(ChemistryRecipes.dust("GalliumHydroxide", 1)),
                ChemistryRecipes.fields("GenLiquid_BayerLiquor", "CarbonDioxide"),
                ChemistryRecipes.amounts(4000, 1000),
                ChemistryRecipes.fields("Saltwater"), ChemistryRecipes.amounts(1000),
                160, 48);
        ChemistryRecipes.recipe("gt.recipe.electrolyzer", "gallium/electrolysis",
                ChemistryRecipes.items(
                        ChemistryRecipes.dust("GalliumHydroxide", 2),
                        ChemistryRecipes.circuit(1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Gallium", 2)),
                null, null,
                ChemistryRecipes.fields("Oxygen", "DistW"), ChemistryRecipes.amounts(1500, 3000),
                200, 64, null, new int[]{1});
    }

    private static void aluminiumFluoride() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "alf3/hf",
                ChemistryRecipes.items(
                        ChemistryRecipes.circuit(1),
                        ChemistryRecipes.dust("Alumina", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_HydrogenFluoride"), ChemistryRecipes.amounts(6000),
                ChemistryRecipes.fields("GenLiquid_AluminiumFluoride", "DistW"),
                ChemistryRecipes.amounts(2000, 3000),
                160, 48, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "alf3/h2sif6",
                ChemistryRecipes.items(ChemistryRecipes.dust("Alumina", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("SiliconDioxide", 1)),
                ChemistryRecipes.fields("GenLiquid_HexafluorosilicicAcid"), ChemistryRecipes.amounts(3000),
                ChemistryRecipes.fields("GenLiquid_AluminiumFluoride", "DistW"),
                ChemistryRecipes.amounts(2000, 1000),
                160, 48);
    }

    private static void hallHeroult() {
        // CoalCoke has a built-in electrolytic decomposition, which collides with this row's inputs.
        ChemistryRecipes.recipe("gt.recipe.electrolyzer", "aluminium/Carbon",
                ChemistryRecipes.items(
                        ChemistryRecipes.dust("Alumina", 2),
                        ChemistryRecipes.dust("Carbon", 3)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Aluminium", 4)),
                ChemistryRecipes.fields("GenLiquid_Cryolite", "GenLiquid_AluminiumFluoride"),
                ChemistryRecipes.amounts(50, 50),
                ChemistryRecipes.fields("CarbonDioxide"), ChemistryRecipes.amounts(3000),
                300, 96);
    }
}
