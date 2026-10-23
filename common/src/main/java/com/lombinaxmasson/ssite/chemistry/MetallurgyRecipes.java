package com.lombinaxmasson.ssite.chemistry;

import com.lombinaxmasson.ssite.machine.SsiteMachines;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.circuit;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

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
            row("gt.recipe.calciner", "titanium/rutile-" + carbon)
                    .in(dust("Rutile", 1), dust(carbon, 2))
                    .fluidIn("GenGas_Chlorine", 4000)
                    .fluidOut("GenLiquid_CrudeTitaniumTetrachloride", 1000, "GenGas_CarbonMonoxide", 2000)
                    .add(160, 64);
            row("gt.recipe.calciner", "titanium/ilmenite-" + carbon)
                    .in(dust("Ilmenite", 1), dust(carbon, 3))
                    .out(dust("FerricChloride", 1))
                    .fluidIn("GenGas_Chlorine", 7000)
                    .fluidOut("GenLiquid_CrudeTitaniumTetrachloride", 1000, "GenGas_CarbonMonoxide", 3000)
                    .add(200, 64);
        }
        row("gt.recipe.calciner", "titanium/ferric-chloride")
                .in(dust("FerricChloride", 2))
                .out(dust("Hematite", 1))
                .fluidIn("Oxygen", 1500)
                .fluidOut("GenGas_Chlorine", 3000)
                .add(120, 48);
    }

    private static void vanadiumRecovery() {
        row("gt.recipe.distillationtower", "titanium/vanadium-tower")
                .fluidIn("GenLiquid_CrudeTitaniumTetrachloride", 10000)
                .fluidOut("GenLiquid_TitaniumTetrachloride", 9500, "GenLiquid_VanadiumOxytrichloride", 500)
                .add(200, 64);
        row("gt.recipe.distillery", "titanium/vanadium-still")
                .in(circuit(1))
                .fluidIn("GenLiquid_CrudeTitaniumTetrachloride", 1000)
                .fluidOut("GenLiquid_TitaniumTetrachloride", 900, "GenLiquid_VanadiumOxytrichloride", 100)
                .keep(0)
                .add(100, 48);
        row("gt.recipe.mixer", "vanadium/oxide")
                .out(dust("VanadiumPentoxide", 1))
                .fluidIn("GenLiquid_VanadiumOxytrichloride", 2000, "DistW", 3000)
                .fluidOut("GenGas_HydrochloricAcid", 6000)
                .add(160, 48);
        row("gt.recipe.calciner", "vanadium/magnetite")
                .in(dust("VanadiumMagnetite", 1), dust("SodiumCarbonate", 1))
                .out(dust("Hematite", 1), dust("VanadiumPentoxide", 1))
                .fluidIn("Oxygen", 1000)
                .fluidOut("CarbonDioxide", 1000)
                .add(200, 64);
        row("gt.recipe.calciner", "vanadium/calcium")
                .in(dust("VanadiumPentoxide", 1), dust("Calcium", 5))
                .out(dust("Vanadium", 2), dust("Quicklime", 5))
                .add(200, 96);
        row("gt.recipe.calciner", "vanadium/aluminium")
                .in(dust("VanadiumPentoxide", 3), dust("Aluminium", 10))
                .out(dust("Vanadium", 6), dust("Alumina", 5))
                .add(240, 96);
    }

    private static void kroll() {
        row("gt.recipe.calciner", "titanium/kroll")
                .in(dust("Magnesium", 2))
                .out(dust("TitaniumSponge", 1), dust("MagnesiumChloride", 2))
                .fluidIn("GenLiquid_TitaniumTetrachloride", 1000, "GenGas_Argon", 100)
                .fluidOut("GenGas_Argon", 100)
                .add(240, 96);
    }

    private static void zirconiumHafniumMibk() {
        zirconChlorination();
        zirconHafniumSeparation();
        zirconiumHafniumKroll();
    }

    private static void zirconChlorination() {
        for (String carbon : new String[]{"Carbon", "CoalCoke"}) {
            row("gt.recipe.calciner", "zircon/chloride-" + carbon)
                    .in(dust("Zircon", 1), dust(carbon, 4))
                    .out(dust("CrudeZirconiumTetrachloride", 1))
                    .fluidIn("GenGas_Chlorine", 4000)
                    .fluidOut("GenLiquid_SiliconTetrachloride", 1000, "GenGas_CarbonMonoxide", 4000)
                    .add(200, 96);
        }
        row("gt.recipe.mixer", "zircon/sicl4-hydrolysis")
                .out(dust("SiliconDioxide", 1))
                .fluidIn("GenLiquid_SiliconTetrachloride", 1000, "DistW", 2000)
                .fluidOut("GenGas_HydrochloricAcid", 4000)
                .add(100, 32);
    }

    private static void zirconHafniumSeparation() {
        row("gt.recipe.mixer", "zircon/hydrolysis")
                .in(dust("CrudeZirconiumTetrachloride", 1))
                .fluidIn("GenLiquid_DeionizedWater", 2000)
                .fluidOut("GenLiquid_ZirconylChlorideSolution", 1000, "GenGas_HydrochloricAcid", 2000)
                .add(120, 48);
        row("gt.recipe.mixer", "zircon/mibk-extract")
                .out(dust("Zirconia", 4))
                .fluidIn("GenLiquid_ZirconylChlorideSolution", 5000,
                        "GenLiquid_MethylIsobutylKetone", 1000, "GenGas_Ammonia", 1000)
                .fluidOut("GenLiquid_HafniumExtract", 1000, "Dirty_Water", 4000)
                .add(200, 64);
        // The centrifuge has one fluid input; the mixer fits the acid strip and MIBK return.
        row("gt.recipe.mixer", "zircon/hafnium-strip")
                .out(dust("Hafnia", 1))
                .fluidIn("GenLiquid_HafniumExtract", 1000, "GenLiquid_SulfuricAcid", 500)
                .fluidOut("GenLiquid_MethylIsobutylKetone", 950, "Dirty_Water", 500)
                .add(160, 64);
    }

    private static void zirconiumHafniumKroll() {
        for (String carbon : new String[]{"Carbon", "CoalCoke"}) {
            row("gt.recipe.calciner", "zirconia/chloride-" + carbon)
                    .in(dust("Zirconia", 1), dust(carbon, 2))
                    .out(dust("ZirconiumTetrachloride", 1))
                    .fluidIn("GenGas_Chlorine", 4000)
                    .fluidOut("GenGas_CarbonMonoxide", 2000)
                    .add(160, 64);
            row("gt.recipe.calciner", "hafnia/chloride-" + carbon)
                    .in(dust("Hafnia", 1), dust(carbon, 2))
                    .out(dust("HafniumTetrachloride", 1))
                    .fluidIn("GenGas_Chlorine", 4000)
                    .fluidOut("GenGas_CarbonMonoxide", 2000)
                    .add(160, 64);
        }
        row("gt.recipe.calciner", "zirconium/kroll")
                .in(dust("ZirconiumTetrachloride", 1), dust("Magnesium", 2))
                .out(dust("ZirconiumSponge", 1), dust("MagnesiumChloride", 2))
                .fluidIn("GenGas_Argon", 100)
                .fluidOut("GenGas_Argon", 100)
                .add(240, 96);
        row("gt.recipe.calciner", "hafnium/kroll")
                .in(dust("HafniumTetrachloride", 1), dust("Magnesium", 2))
                .out(dust("HafniumSponge", 1), dust("MagnesiumChloride", 2))
                .fluidIn("GenGas_Argon", 100)
                .fluidOut("GenGas_Argon", 100)
                .add(240, 120);
    }

    private static void niobiumTantalumMibk() {
        niobiumTantalumLeaching();
        niobiumTantalumSeparation();
        niobiumTantalumReduction();
    }

    private static void niobiumTantalumLeaching() {
        row("gt.recipe.mixer", "coltan/leach")
                .in(dust("Coltan", 2))
                .out(dust("Pyrolusite", 1))
                .fluidIn("GenGas_HydrogenFluoride", 6000, "GenLiquid_SulfuricAcid", 1000)
                .fluidOut("GenLiquid_NbTaLeachLiquor", 2000, "Dirty_Water", 1000)
                .add(200, 64);
        row("gt.recipe.calciner", "columbite/leach")
                .in(dust("Columbite", 2))
                .out(dust("NiobiumPentoxide", 1), dust("Pyrolusite", 1))
                .fluidIn("GenGas_HydrogenFluoride", 4000, "GenLiquid_SulfuricAcid", 1000)
                .fluidOut("GenLiquid_NbTaLeachLiquor", 1000)
                .add(200, 64);
        row("gt.recipe.calciner", "tantalite/leach")
                .in(dust("Tantalite", 2))
                .out(dust("TantalumPentoxide", 1), dust("Pyrolusite", 1))
                .fluidIn("GenGas_HydrogenFluoride", 4000, "GenLiquid_SulfuricAcid", 1000)
                .fluidOut("GenLiquid_NbTaLeachLiquor", 1000)
                .add(200, 64);
        row("gt.recipe.mixer", "tantalum/k2taf7")
                .in(dust("TantalumPentoxide", 1), dust("PotassiumFluoride", 2))
                .out(dust("PotassiumHeptafluorotantalate", 2))
                .fluidIn("GenGas_HydrogenFluoride", 5000)
                .fluidOut("DistW", 3000)
                .add(160, 64);
    }

    private static void niobiumTantalumSeparation() {
        row("gt.recipe.mixer", "niobium-tantalum/extract")
                .fluidIn("GenLiquid_NbTaLeachLiquor", 2000, "GenLiquid_MethylIsobutylKetone", 2000)
                .fluidOut("GenLiquid_NbTaMibkExtract", 2000, "Dirty_Water", 2000)
                .add(160, 64);
        row("gt.recipe.mixer", "niobium/strip")
                .out(dust("NiobiumPentoxide", 1))
                .fluidIn("GenLiquid_NbTaMibkExtract", 2000, "GenLiquid_DeionizedWater", 1000)
                .fluidOut("GenLiquid_TantalumRichMibk", 2000, "GenGas_HydrogenFluoride", 2000)
                .add(160, 64);
        row("gt.recipe.centrifuge", "tantalum/strip")
                .in(dust("PotassiumFluoride", 2))
                .out(dust("PotassiumHeptafluorotantalate", 1))
                .fluidIn("GenLiquid_TantalumRichMibk", 2000)
                .fluidOut("GenLiquid_MethylIsobutylKetone", 1900, "GenGas_HydrogenFluoride", 1000)
                .add(160, 64);
    }

    private static void niobiumTantalumReduction() {
        row("gt.recipe.calciner", "niobium/aluminothermy")
                .in(dust("NiobiumPentoxide", 1), dust("Aluminium", 2))
                .out(dust("Niobium", 2), dust("Alumina", 1))
                .add(240, 96);
        row("gt.recipe.calciner", "tantalum/sodium")
                .in(dust("PotassiumHeptafluorotantalate", 1), dust("Sodium", 5))
                .out(dust("Tantalum", 1), dust("PotassiumFluoride", 2), dust("SodiumFluoride", 5))
                .add(240, 96);
        row("gt.recipe.mixer", "sodium-fluoride/hf")
                .in(dust("SodiumFluoride", 2))
                .out(dust("SodiumSulfate", 1))
                .fluidIn("GenLiquid_SulfuricAcid", 1000)
                .fluidOut("GenGas_HydrogenFluoride", 2000)
                .add(100, 32);
    }

    /** The furnace runs these in its behavior; the rows only document the ratios and gas use. */
    private static void varDisplay() {
        row(SsiteMachines.VAR_MAP, "var/titanium")
                .in(dust("TitaniumSponge", 1)).out(dust("Titanium", 1))
                .fluidIn("GenGas_Argon", 100).display(200, 128);
        row(SsiteMachines.VAR_MAP, "var/zirconium")
                .in(dust("ZirconiumSponge", 1)).out(dust("Zirconium", 1))
                .fluidIn("GenGas_Argon", 100).display(200, 128);
        row(SsiteMachines.VAR_MAP, "var/hafnium")
                .in(dust("HafniumSponge", 1)).out(dust("Hafnium", 1))
                .fluidIn("GenGas_Argon", 100).display(200, 128);
        row(SsiteMachines.VAR_MAP, "var/niobium-titanium")
                .in(dust("Niobium", 1), dust("Titanium", 1)).out(dust("NiobiumTitanium", 2))
                .fluidIn("GenGas_Argon", 100).display(200, 128);
        row(SsiteMachines.VAR_MAP, "var/ta4hfc5")
                .in(dust("Tantalum", 4), dust("Hafnium", 1), dust("Carbon", 5))
                .out(dust("TantalumHafniumCarbide", 10))
                .fluidIn("GenGas_Argon", 200).display(200, 128);
    }

    private static void bayerAluminiumGallium() {
        bayerDigestion();
        redMudRecovery();
        gallium();
        aluminiumFluoride();
        hallHeroult();
    }

    private static void bayerDigestion() {
        row("gt.recipe.autoclave", "bayer/digest")
                .in(dust("Bauxite", 2), dust("SodiumHydroxide", 2))
                .out(dust("SodiumAluminate", 2), dust("RedMud", 1))
                .fluidIn("Steam", 4000)
                .fluidOut("GenLiquid_BayerLiquor", 1000)
                .add(200, 64);
        row("gt.recipe.calciner", "bayer/calcine")
                .in(dust("AluminiumHydroxide", 2), circuit(1))
                .out(dust("Alumina", 1))
                .fluidOut("DistW", 3000)
                .keep(1)
                .add(120, 48);
    }

    private static void redMudRecovery() {
        row("gt.recipe.bath", "bayer/red-mud")
                .in(dust("RedMud", 3))
                .out(dust("Hematite", 1), dust("Rutile", 1), dust("SiliconDioxide", 1))
                .fluidIn("GenLiquid_SulfuricAcid", 2000)
                .fluidOut("Dirty_Water", 1000)
                .add(160, 32);
    }

    private static void gallium() {
        row("gt.recipe.mixer", "gallium/carbonate")
                .out(dust("GalliumHydroxide", 1))
                .fluidIn("GenLiquid_BayerLiquor", 4000, "CarbonDioxide", 1000)
                .fluidOut("Saltwater", 1000)
                .add(160, 48);
        row("gt.recipe.electrolyzer", "gallium/electrolysis")
                .in(dust("GalliumHydroxide", 2), circuit(1))
                .out(dust("Gallium", 2))
                .fluidOut("Oxygen", 1500, "DistW", 3000)
                .keep(1)
                .add(200, 64);
    }

    private static void aluminiumFluoride() {
        row("gt.recipe.mixer", "alf3/hf")
                .in(circuit(1), dust("Alumina", 1))
                .fluidIn("GenGas_HydrogenFluoride", 6000)
                .fluidOut("GenLiquid_AluminiumFluoride", 2000, "DistW", 3000)
                .keep(0)
                .add(160, 48);
        row("gt.recipe.mixer", "alf3/h2sif6")
                .in(dust("Alumina", 1))
                .out(dust("SiliconDioxide", 1))
                .fluidIn("GenLiquid_HexafluorosilicicAcid", 3000)
                .fluidOut("GenLiquid_AluminiumFluoride", 2000, "DistW", 1000)
                .add(160, 48);
    }

    private static void hallHeroult() {
        // CoalCoke has a built-in electrolytic decomposition, which collides with this row's inputs.
        row("gt.recipe.electrolyzer", "aluminium/Carbon")
                .in(dust("Alumina", 2), dust("Carbon", 3))
                .out(dust("Aluminium", 4))
                .fluidIn("GenLiquid_Cryolite", 50, "GenLiquid_AluminiumFluoride", 50)
                .fluidOut("CarbonDioxide", 3000)
                .add(300, 96);
    }
}
