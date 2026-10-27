package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.circuit;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

/** M7 base-metal matte, leaching, P204 split, electrorefining and precious-metal chains. */
public final class BaseMetalRecipes {
    private BaseMetalRecipes() {}

    static void add() {
        matteSmelting();
        matteLeaching();
        nickelCobaltSplit();
        electrowinning();
        slimeRefining();
        platinumGroupSeparation();
        zincChain();
        cadmiumThallium();
        indiumGermanium();
        leadSmelting();
        leadSoftening();
        leadDesilverizing();
        leadDebismuthizing();
    }

    private static void matteSmelting() {
        row("gt.recipe.calciner", "m7/green-matte-copper")
                .in(dust("Chalcopyrite", 3))
                .out(dust("GreenMatte", 3), dust("Hematite", 1))
                .fluidIn("Oxygen", 1500)
                .fluidOut("GenGas_SulfurDioxide", 1500)
                .add(240, 64);
        row("gt.recipe.calciner", "m7/green-matte-nickel")
                .in(dust("Pentlandite", 3))
                .out(dust("GreenMatte", 3), dust("Hematite", 1))
                .fluidIn("Oxygen", 1200)
                .fluidOut("GenGas_SulfurDioxide", 1200)
                .add(240, 64);
        row("gt.recipe.calciner", "m7/white-matte")
                .in(dust("GreenMatte", 3))
                .out(dust("WhiteMatte", 2), dust("Hematite", 1))
                .fluidIn("Oxygen", 1000)
                .fluidOut("GenGas_SulfurDioxide", 1000)
                .add(240, 80);
    }

    /** Oxidative sulfate leach leaves copper as the residue; nickel and cobalt report to the liquor. */
    private static void matteLeaching() {
        row("gt.recipe.mixer", "m7/white-matte-leach")
                .in(dust("WhiteMatte", 2))
                .out(dust("CrudeCopper", 2))
                .fluidIn("GenLiquid_SulfuricAcid", 1000, "Oxygen", 500, "DistW", 1000)
                .fluidOut("GenLiquid_NickelCobaltSulfate", 1000)
                .add(200, 64);
    }

    /** P204 loads cobalt and leaves nickel in the raffinate; 96% of the organic phase comes back on stripping. */
    private static void nickelCobaltSplit() {
        row("gt.recipe.mixer", "m7/p204-nickel-cobalt")
                .fluidIn("GenLiquid_NickelCobaltSulfate", 1000,
                        "GenLiquid_DiethylhexylPhosphoricAcid", 500)
                .fluidOut("GenLiquid_NickelSulfateSolution", 1000, "GenLiquid_CobaltP204Extract", 1000)
                .add(160, 64);
        row("gt.recipe.mixer", "m7/cobalt-strip")
                .fluidIn("GenLiquid_CobaltP204Extract", 1000, "GenLiquid_SulfuricAcid", 500)
                .fluidOut("GenLiquid_CobaltSulfateSolution", 1000,
                        "GenLiquid_DiethylhexylPhosphoricAcid", 480)
                .add(160, 48);
    }

    private static void electrowinning() {
        row("gt.recipe.electrolyzer", "m7/nickel-electrowin")
                .out(dust("Nickel", 1))
                .fluidIn("GenLiquid_NickelSulfateSolution", 1000)
                .fluidOut("GenLiquid_SulfuricAcid", 950, "Oxygen", 500)
                .add(240, 96);
        row("gt.recipe.electrolyzer", "m7/cobalt-electrowin")
                .out(dust("Cobalt", 1))
                .fluidIn("GenLiquid_CobaltSulfateSolution", 1000)
                .fluidOut("GenLiquid_SulfuricAcid", 950, "Oxygen", 500)
                .add(240, 96);
        row("gt.recipe.electrolyzer", "m7/copper-electrorefine")
                .in(dust("CrudeCopper", 2))
                .out(dust("Copper", 2), dust("CopperAnodeSlime", 1), dust("PlatinumGroupConcentrate", 1))
                .chances(10000, 10000, 2500)
                .fluidIn("GenLiquid_SulfuricAcid", 1000)
                .fluidOut("GenLiquid_SulfuricAcid", 950)
                .add(300, 128);
    }

    /** Anode slime roast drives off selenium and tellurium; the silver-gold residue is parted with nitric acid. */
    private static void slimeRefining() {
        row("gt.recipe.calciner", "m7/slime-roast")
                .in(dust("CopperAnodeSlime", 2))
                .out(dust("SilverGoldResidue", 1), dust("TelluriumDioxide", 1))
                .fluidIn("Oxygen", 2500)
                .fluidOut("GenGas_SeleniumDioxide", 2000, "GenGas_SulfurDioxide", 1000)
                .add(240, 64);
        row("gt.recipe.mixer", "m7/selenium-reduction")
                .out(dust("Selenium", 1))
                .fluidIn("GenGas_SeleniumDioxide", 1000,
                        "GenGas_SulfurDioxide", 2000, "DistW", 2000)
                .fluidOut("GenLiquid_SulfuricAcid", 2000)
                .add(200, 48);
        row("gt.recipe.mixer", "m7/tellurium-reduction")
                .in(dust("TelluriumDioxide", 1))
                .out(dust("Tellurium", 1))
                .fluidIn("GenGas_SulfurDioxide", 2000, "DistW", 2000)
                .fluidOut("GenLiquid_SulfuricAcid", 2000)
                .add(200, 48);
        row("gt.recipe.mixer", "m7/silver-parting")
                .in(dust("SilverGoldResidue", 2))
                .out(dust("CrudeGold", 1))
                .fluidIn("GenLiquid_NitricAcid", 3000)
                .fluidOut("GenLiquid_SilverNitrateSolution", 2000, "GenGas_NitrogenMonoxide", 1000)
                .add(200, 64);
        row("gt.recipe.mixer", "m7/silver-cementation")
                .in(dust("Copper", 1))
                .out(dust("Silver", 2))
                .fluidIn("GenLiquid_SilverNitrateSolution", 2000)
                .fluidOut("Dirty_Water", 1000)
                .add(160, 48);
        row("gt.recipe.mixer", "m7/gold-dissolve")
                .in(dust("CrudeGold", 2))
                .out(dust("ChloroauricAcid", 2))
                .fluidIn("GenLiquid_NitricAcid", 2000, "GenGas_HydrochloricAcid", 6000)
                .fluidOut("GenGas_NitrogenMonoxide", 1000, "DistW", 2000)
                .add(240, 64);
        row("gt.recipe.mixer", "m7/gold-reduction")
                .in(dust("ChloroauricAcid", 2))
                .out(dust("Gold", 2))
                .fluidIn("GenGas_SulfurDioxide", 3000, "DistW", 6000)
                .fluidOut("GenLiquid_SulfuricAcid", 3000, "GenGas_HydrochloricAcid", 8000)
                .add(240, 64);
    }

    /** Aqua regia is fed as its nitric and hydrochloric components; this port has no aqua regia fluid. */
    private static void platinumGroupSeparation() {
        row("gt.recipe.mixer", "m7/pgm-dissolve-concentrate")
                .in(dust("PlatinumGroupConcentrate", 1))
                .fluidIn("GenLiquid_NitricAcid", 2000, "GenGas_HydrochloricAcid", 6000)
                .fluidOut("GenLiquid_PGMChlorideLiquor", 1000, "GenGas_NitrogenMonoxide", 1000)
                .add(240, 64);
        row("gt.recipe.mixer", "m7/pgm-dissolve-cooperite")
                .in(dust("Cooperite", 1))
                .out(dust("Sulfur", 1))
                .fluidIn("GenLiquid_NitricAcid", 2000, "GenGas_HydrochloricAcid", 6000)
                .fluidOut("GenLiquid_PGMChlorideLiquor", 1000, "GenGas_NitrogenMonoxide", 1000)
                .add(240, 64);
        row("gt.recipe.mixer", "m7/pgm-dissolve-sperrylite")
                .in(dust("Sperrylite", 1))
                .out(dust("Arsenic", 1))
                .fluidIn("GenLiquid_NitricAcid", 2000, "GenGas_HydrochloricAcid", 6000)
                .fluidOut("GenLiquid_PGMChlorideLiquor", 1000, "GenGas_NitrogenMonoxide", 1000)
                .add(240, 64);
        row("gt.recipe.distillery", "m7/osmium-distill")
                .in(circuit(1))
                .keep(0)
                .fluidIn("GenLiquid_PGMChlorideLiquor", 1000)
                .fluidOut("GenLiquid_OsmiumTetroxide", 250, "GenLiquid_OsmiumFreePGMLiquor", 750)
                .add(240, 64);
        row("gt.recipe.distillery", "m7/ruthenium-distill")
                .in(circuit(2))
                .keep(0)
                .fluidIn("GenLiquid_OsmiumFreePGMLiquor", 750)
                .fluidOut("GenLiquid_RutheniumTetroxide", 250, "GenLiquid_PGMPreciousLiquor", 500)
                .add(240, 64);
        row("gt.recipe.calciner", "m7/osmium-reduction")
                .out(dust("Osmium", 1))
                .fluidIn("GenLiquid_OsmiumTetroxide", 250, "GenGas_Hydrogen", 1000)
                .fluidOut("DistW", 1000)
                .add(200, 64);
        row("gt.recipe.calciner", "m7/ruthenium-reduction")
                .out(dust("Ruthenium", 1))
                .fluidIn("GenLiquid_RutheniumTetroxide", 250, "GenGas_Hydrogen", 1000)
                .fluidOut("DistW", 1000)
                .add(200, 64);
        row("gt.recipe.mixer", "m7/ammonium-chloride")
                .fluidIn("GenGas_Ammonia", 1000, "GenGas_HydrochloricAcid", 1000)
                .fluidOut("GenLiquid_AmmoniumChlorideSolution", 1000)
                .add(100, 32);
        row("gt.recipe.mixer", "m7/platinum-precipitate")
                .out(dust("AmmoniumChloroplatinate", 1))
                .fluidIn("GenLiquid_PGMPreciousLiquor", 500,
                        "GenLiquid_AmmoniumChlorideSolution", 500)
                .fluidOut("GenLiquid_PalladiumLiquor", 500, "GenGas_HydrochloricAcid", 500)
                .add(160, 48);
        row("gt.recipe.calciner", "m7/platinum-reduction")
                .in(dust("AmmoniumChloroplatinate", 1))
                .out(dust("Platinum", 1))
                .fluidIn("GenGas_Hydrogen", 1000)
                .fluidOut("GenLiquid_AmmoniumChlorideSolution", 500, "GenGas_HydrochloricAcid", 2000)
                .add(200, 64);
        row("gt.recipe.mixer", "m7/palladium-precipitate")
                .out(dust("DiamminePalladiumChloride", 1))
                .fluidIn("GenLiquid_PalladiumLiquor", 500, "GenGas_Ammonia", 500)
                .fluidOut("GenLiquid_RhodiumIridiumLiquor", 500, "Dirty_Water", 500)
                .add(160, 48);
        row("gt.recipe.calciner", "m7/palladium-reduction")
                .in(dust("DiamminePalladiumChloride", 1))
                .out(dust("Palladium", 1))
                .fluidIn("GenGas_Hydrogen", 1000)
                .fluidOut("GenGas_Ammonia", 1000, "GenGas_HydrochloricAcid", 1000)
                .add(200, 64);
        row("gt.recipe.mixer", "m7/iridium-precipitate")
                .out(dust("AmmoniumHexachloroiridate", 1))
                .fluidIn("GenLiquid_RhodiumIridiumLiquor", 500,
                        "GenLiquid_AmmoniumChlorideSolution", 500, "GenGas_Chlorine", 250)
                .fluidOut("GenLiquid_RhodiumChlorideSolution", 500, "GenGas_HydrochloricAcid", 500)
                .add(160, 48);
        row("gt.recipe.calciner", "m7/iridium-reduction")
                .in(dust("AmmoniumHexachloroiridate", 1))
                .out(dust("Iridium", 1))
                .fluidIn("GenGas_Hydrogen", 1000)
                .fluidOut("GenLiquid_AmmoniumChlorideSolution", 500, "GenGas_HydrochloricAcid", 2000)
                .add(200, 64);
        row("gt.recipe.calciner", "m7/rhodium-reduction")
                .out(dust("Rhodium", 1))
                .fluidIn("GenLiquid_RhodiumChlorideSolution", 500, "GenGas_Hydrogen", 750)
                .fluidOut("GenGas_HydrochloricAcid", 1500)
                .add(200, 64);
    }

    /** Sphalerite roast leaves zinc calcine and a cadmium-thallium flue dust. */
    private static void zincChain() {
        row("gt.recipe.calciner", "m7/zinc-roast")
                .in(dust("Sphalerite", 2))
                .out(dust("Zincite", 2), dust("CadmiumThalliumFlueDust", 1))
                .fluidIn("Oxygen", 3000)
                .fluidOut("GenGas_SulfurDioxide", 2000)
                .add(240, 64);
        // The sulfate solution reuses GT6CE's WhiteVitriol fluid; its built-in electrolyzer row makes zinc.
        row("gt.recipe.mixer", "m7/zincite-leach")
                .in(dust("Zincite", 1))
                .out(dust("ZincLeachResidue", 1))
                .fluidIn("GenLiquid_SulfuricAcid", 2000, "DistW", 1000)
                .fluidOut("GenLiquid_WhiteVitriol", 3000)
                .add(200, 64);
    }

    /** Zinc dust cements cadmium first, then thallium; both steps return zinc to the sulfate line. */
    private static void cadmiumThallium() {
        row("gt.recipe.mixer", "m7/flue-dust-leach")
                .in(dust("CadmiumThalliumFlueDust", 2))
                .fluidIn("GenLiquid_SulfuricAcid", 2000, "DistW", 1000)
                .fluidOut("GenLiquid_CadmiumThalliumSolution", 2000, "Dirty_Water", 1000)
                .add(160, 48);
        row("gt.recipe.mixer", "m7/cadmium-cement")
                .in(dust("Zinc", 1))
                .out(dust("Cadmium", 1))
                .fluidIn("GenLiquid_CadmiumThalliumSolution", 2000)
                .fluidOut("GenLiquid_ThalliumSulfateSolution", 2000, "GenLiquid_WhiteVitriol", 1000)
                .add(200, 64);
        row("gt.recipe.mixer", "m7/thallium-cement")
                .in(dust("Zinc", 1))
                .out(dust("Thallium", 1))
                .fluidIn("GenLiquid_ThalliumSulfateSolution", 2000)
                .fluidOut("GenLiquid_WhiteVitriol", 2000)
                .add(200, 64);
    }

    /** The zinc leach residue carries indium and germanium; TBP takes indium, distillation takes germanium. */
    private static void indiumGermanium() {
        row("gt.recipe.mixer", "m7/zinc-residue-leach")
                .in(dust("ZincLeachResidue", 4))
                .out(dust("Hematite", 1))
                .fluidIn("GenGas_HydrochloricAcid", 4000)
                .fluidOut("GenLiquid_IndiumChlorideSolution", 2000,
                        "GenLiquid_CrudeGermaniumTetrachloride", 4000)
                .add(240, 64);
        row("gt.recipe.distillery", "m7/germanium-distill")
                .in(circuit(1))
                .keep(0)
                .fluidIn("GenLiquid_CrudeGermaniumTetrachloride", 4000)
                .fluidOut("GenLiquid_GermaniumTetrachloride", 3000, "Dirty_Water", 1000)
                .add(240, 64);
        row("gt.recipe.mixer", "m7/germanium-hydrolysis")
                .fluidIn("GenLiquid_GermaniumTetrachloride", 2000, "GenLiquid_UltrapureWater", 4000)
                .out(dust("GermaniumDioxide", 1))
                .fluidOut("GenGas_HydrochloricAcid", 4000)
                .add(200, 64);
        row("gt.recipe.calciner", "m7/germanium-reduction")
                .in(dust("GermaniumDioxide", 1))
                .out(dust("Germanium", 1))
                .fluidIn("GenGas_Hydrogen", 2000)
                .fluidOut("DistW", 2000)
                .add(200, 64);
        row("gt.recipe.mixer", "m7/tbp-indium")
                .fluidIn("GenLiquid_IndiumChlorideSolution", 2000,
                        "GenLiquid_TributylPhosphate", 1000)
                .fluidOut("GenLiquid_TbpIndiumExtract", 1000, "Dirty_Water", 2000)
                .add(160, 64);
        row("gt.recipe.mixer", "m7/indium-strip")
                .fluidIn("GenLiquid_TbpIndiumExtract", 1000, "GenLiquid_SulfuricAcid", 1000)
                .fluidOut("GenLiquid_IndiumSulfateSolution", 2000,
                        "GenLiquid_TributylPhosphate", 950)
                .add(160, 48);
        row("gt.recipe.mixer", "m7/indium-cement")
                .in(dust("Zinc", 1))
                .out(dust("Indium", 1))
                .fluidIn("GenLiquid_IndiumSulfateSolution", 2000)
                .fluidOut("GenLiquid_WhiteVitriol", 2000)
                .add(200, 64);
    }

    /** Galena first oxidises to lead calcine, then coke reduces it to crude lead. */
    private static void leadSmelting() {
        row("gt.recipe.calciner", "m7/galena-roast")
                .in(dust("Galena", 2))
                .out(dust("LeadCalcine", 2))
                .fluidIn("Oxygen", 3000)
                .fluidOut("GenGas_SulfurDioxide", 2000)
                .add(240, 64);
        row("gt.recipe.calciner", "m7/lead-reduce")
                .in(dust("LeadCalcine", 2), dust("CoalCoke", 2))
                .out(dust("CrudeLead", 2))
                .fluidOut("GenGas_CarbonMonoxide", 2000)
                .add(240, 80);
    }

    /** Sulfur skims copper as a dross; molten caustic then oxidises arsenic and antimony into sodium salts. */
    private static void leadSoftening() {
        row("gt.recipe.calciner", "m7/lead-copper-dross")
                .in(dust("CrudeLead", 2), dust("Sulfur", 1))
                .out(dust("DrossedLead", 2), dust("CopperDross", 1))
                .add(200, 64);
        row("gt.recipe.calciner", "m7/lead-harris")
                .in(dust("DrossedLead", 2), dust("SodiumHydroxide", 4), dust("SodiumNitrate", 2))
                .out(dust("SoftenedLead", 2), dust("SodiumArsenate", 1), dust("SodiumAntimonate", 1))
                .add(240, 96);
        row("gt.recipe.calciner", "m7/arsenate-reduce")
                .in(dust("SodiumArsenate", 2), dust("CoalCoke", 3))
                .out(dust("Arsenic", 2), dust("SodiumCarbonate", 3))
                .add(200, 64);
        row("gt.recipe.calciner", "m7/antimonate-reduce")
                .in(dust("SodiumAntimonate", 2), dust("CoalCoke", 3))
                .out(dust("Antimony", 2), dust("SodiumCarbonate", 3))
                .add(200, 64);
    }

    /** Parkes: zinc takes silver into a crust that floats on the lead; the crust splits into silver and zinc. */
    private static void leadDesilverizing() {
        row("gt.recipe.calciner", "m7/lead-parkes")
                .in(dust("SoftenedLead", 2), dust("Zinc", 2))
                .out(dust("DesilveredLead", 2), dust("SilverZincCrust", 1))
                .add(240, 80);
        row("gt.recipe.centrifuge", "m7/silver-zinc-crust")
                .in(dust("SilverZincCrust", 2))
                .out(dust("Silver", 1), dust("Zinc", 2))
                .add(200, 64);
    }

    /** Kroll-Betterton: calcium and magnesium pull bismuth into a dross that chlorine converts to metal. */
    private static void leadDebismuthizing() {
        row("gt.recipe.calciner", "m7/lead-kroll-betterton")
                .in(dust("DesilveredLead", 2), dust("Calcium", 1), dust("Magnesium", 1))
                .out(dust("Lead", 2), dust("BismuthDross", 1))
                .add(240, 96);
        row("gt.recipe.calciner", "m7/bismuth-dross")
                .in(dust("BismuthDross", 2))
                .out(dust("Bismuth", 2), dust("CalciumChloride", 1), dust("MagnesiumChloride", 1))
                .fluidIn("GenGas_Chlorine", 1000)
                .add(200, 64);
    }
}
