package com.lombinaxmasson.ssite.chemistry;

/** M3 petrochemical recipes: crude refining, BTX extraction, C4, xylene/toluene, C5, phenol/BPA. */
public final class PetrochemRecipes {
    private PetrochemRecipes() {}

    static void add() {
        crudeRefining();
        btxExtraction();
        cFourChain();
        xyleneTolueneLoop();
        cFiveChain();
        cumenePhenol();
    }

    // ── Card 2: crude atmospheric distillation, naphtha steam cracking, platinum reforming ──

    private static final String[] CRUDE_CUTS = {
            "Propane", "Butane", "GenLiquid_Naphtha", "Petrol", "Kerosine",
            "Diesel", "Fuel", "Lubricant", "Tar"};

    private static void crudeRefining() {
        distill("Oil_Light", 50, 80, 300, 200, 150, 120, 50, 30, 20);
        distill("Oil_Normal", 30, 50, 220, 150, 150, 180, 120, 50, 50);
        distill("Oil_Medium", 30, 50, 220, 150, 150, 180, 120, 50, 50);
        distill("Oil_Heavy", 10, 20, 120, 80, 120, 200, 250, 80, 120);
        distill("Oil_ExtraHeavy", 0, 10, 60, 0, 80, 150, 300, 100, 300);
        steamCrack("naphtha", "GenLiquid_Naphtha",
                ChemistryRecipes.fields("Hydrogen", "Methane", "Ethylene", "Propylene", "GenGas_CFourFraction", "GenLiquid_PyrolysisGasoline"),
                ChemistryRecipes.amounts(5, 15, 30, 15, 10, 20));
        steamCrack("raffinate", "GenLiquid_Raffinate",
                ChemistryRecipes.fields("Hydrogen", "Methane", "Ethylene", "Propylene", "GenGas_CFourFraction", "GenLiquid_PyrolysisGasoline"),
                ChemistryRecipes.amounts(5, 10, 40, 20, 10, 10));
        ChemistryRecipes.recipe("gt.recipe.catalyticcracking", "reform/naphtha",
                ChemistryRecipes.items(ChemistryRecipes.dust("Platinum", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Naphtha"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_Reformate", "Hydrogen", "LPG"), ChemistryRecipes.amounts(800, 300, 100),
                160, 48, null, new int[]{0});
    }

    private static void distill(String oil, int propane, int butane, int naphtha, int petrol,
                                int kerosine, int diesel, int fuel, int lubricant, int tar) {
        int[] cuts = {propane, butane, naphtha, petrol, kerosine, diesel, fuel, lubricant, tar};
        int nonZero = 0;
        for (int cut : cuts) {
            if (cut > 0) nonZero++;
        }
        String[] outFields = new String[nonZero];
        int[] outAmounts = new int[nonZero];
        int j = 0;
        for (int i = 0; i < cuts.length; i++) {
            if (cuts[i] > 0) {
                outFields[j] = CRUDE_CUTS[i];
                outAmounts[j] = cuts[i];
                j++;
            }
        }
        ChemistryRecipes.recipe("gt.recipe.distillationtower", "crude/" + oil,
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields(oil), ChemistryRecipes.amounts(1000),
                outFields, outAmounts,
                200, 64);
    }

    private static void steamCrack(String label, String feed, String[] outFields, int[] outAmounts) {
        ChemistryRecipes.recipe("gt.recipe.steamcracking", "crack/" + label,
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("Steam", feed), ChemistryRecipes.amounts(1000, 100),
                outFields, outAmounts,
                64, 16);
    }

    // ── Card 3: BTX furfural extraction and solvent recycle ──

    private static void btxExtraction() {
        ChemistryRecipes.recipe("gt.recipe.bath", "furfural",
                ChemistryRecipes.items(ChemistryRecipes.dust("Wood", 4)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_SulfuricAcid"), ChemistryRecipes.amounts(250),
                ChemistryRecipes.fields("GenLiquid_Furfural", "Dirty_Water"), ChemistryRecipes.amounts(500, 500),
                160, 24);
        furfuralExtract("pygas", "GenLiquid_PyrolysisGasoline", 2200, 800);
        furfuralExtract("reformate", "GenLiquid_Reformate", 2400, 600);
        ChemistryRecipes.recipe("gt.recipe.distillery", "furfural/strip",
                ChemistryRecipes.items(ChemistryRecipes.circuit(1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_FurfuralExtract"), ChemistryRecipes.amounts(2200),
                ChemistryRecipes.fields("GenLiquid_Furfural", "GenLiquid_BTEX"), ChemistryRecipes.amounts(950, 1200),
                120, 32);
        ChemistryRecipes.recipe("gt.recipe.distillationtower", "btex/distill",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_BTEX"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_Benzene", "GenLiquid_Toluene", "GenLiquid_Ethylbenzene", "GenLiquid_Xylene", "Fuel"),
                ChemistryRecipes.amounts(350, 300, 80, 220, 50),
                160, 64);
        ChemistryRecipes.recipe("gt.recipe.distillery", "btex/early",
                ChemistryRecipes.items(ChemistryRecipes.circuit(2)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_BTEX"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_Benzene", "GenLiquid_Toluene"), ChemistryRecipes.amounts(300, 250),
                120, 24);
        ChemistryRecipes.recipe("gt.recipe.distillationtower", "raffinate/cut-c5",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Raffinate"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_CFiveFraction", "GenLiquid_Naphtha"), ChemistryRecipes.amounts(300, 700),
                120, 48);
    }

    private static void furfuralExtract(String label, String feed, int extractAmount, int raffinateAmount) {
        ChemistryRecipes.recipe("gt.recipe.mixer", "furfural/extract-" + label,
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields(feed, "GenLiquid_Furfural"), ChemistryRecipes.amounts(2000, 1000),
                ChemistryRecipes.fields("GenLiquid_FurfuralExtract", "GenLiquid_Raffinate"),
                ChemistryRecipes.amounts(extractAmount, raffinateAmount),
                160, 48);
    }

    // ── Card 4: C4 butadiene, butene split, dehydrogenation, sulfolane ──

    private static void cFourChain() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c4/extract-butadiene",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_CFourFraction", "GenLiquid_Furfural"), ChemistryRecipes.amounts(1000, 50),
                ChemistryRecipes.fields("GenGas_Butadiene", "GenGas_Butene"), ChemistryRecipes.amounts(400, 600),
                120, 32);
        ChemistryRecipes.recipe("gt.recipe.distillationtower", "c4/split-butenes",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_Butene"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenGas_Isobutylene", "GenGas_OneButene", "GenGas_TwoButene"),
                ChemistryRecipes.amounts(500, 300, 200),
                160, 48);
        ChemistryRecipes.recipe("gt.recipe.catalyticcracking", "c4/butane-dehydro",
                ChemistryRecipes.items(ChemistryRecipes.dust("Chromium", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("Butane"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenGas_Butene", "Hydrogen"), ChemistryRecipes.amounts(900, 900),
                160, 48, null, new int[]{0});
        oxydehydro("OneButene");
        oxydehydro("TwoButene");
        ChemistryRecipes.recipe("gt.recipe.catalyticcracking", "c4/ethanol-dehydro",
                ChemistryRecipes.items(ChemistryRecipes.dust("Copper", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("BioEthanol"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_Acetaldehyde", "Hydrogen"), ChemistryRecipes.amounts(900, 900),
                120, 32, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c4/ethanol-condense",
                ChemistryRecipes.items(ChemistryRecipes.dust("Tantalum", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("BioEthanol", "GenLiquid_Acetaldehyde"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenGas_Butadiene", "DistW"), ChemistryRecipes.amounts(800, 1600),
                200, 48, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c4/sulfolane",
                ChemistryRecipes.items(ChemistryRecipes.dust("Nickel", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_Butadiene", "GenGas_SulfurDioxide", "Hydrogen"),
                ChemistryRecipes.amounts(1000, 1000, 2000),
                ChemistryRecipes.fields("GenLiquid_Sulfolane"), ChemistryRecipes.amounts(1000),
                200, 48, null, new int[]{0});
        sulfolaneExtract("pygas", "GenLiquid_PyrolysisGasoline", 2300, 700);
        sulfolaneExtract("reformate", "GenLiquid_Reformate", 2500, 500);
        ChemistryRecipes.recipe("gt.recipe.distillery", "sulfolane/strip",
                ChemistryRecipes.items(ChemistryRecipes.circuit(1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_SulfolaneExtract"), ChemistryRecipes.amounts(2300),
                ChemistryRecipes.fields("GenLiquid_Sulfolane", "GenLiquid_BTEX"), ChemistryRecipes.amounts(990, 1300),
                120, 32);
    }

    private static void oxydehydro(String butene) {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c4/oxydehydro-" + butene.toLowerCase(),
                ChemistryRecipes.items(ChemistryRecipes.dust("Molybdenum", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_" + butene, "Oxygen"), ChemistryRecipes.amounts(1000, 500),
                ChemistryRecipes.fields("GenGas_Butadiene", "DistW"), ChemistryRecipes.amounts(900, 1000),
                160, 64, null, new int[]{0});
    }

    private static void sulfolaneExtract(String label, String feed, int extractAmount, int raffinateAmount) {
        ChemistryRecipes.recipe("gt.recipe.mixer", "sulfolane/extract-" + label,
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields(feed, "GenLiquid_Sulfolane"), ChemistryRecipes.amounts(2000, 1000),
                ChemistryRecipes.fields("GenLiquid_SulfolaneExtract", "GenLiquid_Raffinate"),
                ChemistryRecipes.amounts(extractAmount, raffinateAmount),
                160, 48);
    }

    // ── Card 5: xylene separation, ZSM-5 isomerization, toluene, ethylbenzene ──

    private static void xyleneTolueneLoop() {
        ChemistryRecipes.recipe("gt.recipe.autoclave", "zsm5",
                ChemistryRecipes.items(ChemistryRecipes.dust("Zeolite", 1), ChemistryRecipes.dust("SiliconDioxide", 2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("ZSM5", 2)),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                null, null,
                400, 64);
        ChemistryRecipes.recipe("gt.recipe.distillationtower", "xylene/ortho",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Xylene"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_OrthoXylene", "GenLiquid_MetaParaXylene"), ChemistryRecipes.amounts(200, 800),
                160, 64);
        ChemistryRecipes.recipe("gt.recipe.freezer", "xylene/freeze",
                ChemistryRecipes.items(ChemistryRecipes.circuit(1)),
                ChemistryRecipes.items(ChemistryRecipes.item("dustSmall", "ParaXylene", 1)),
                ChemistryRecipes.fields("GenLiquid_MetaParaXylene"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_MetaXylene"), ChemistryRecipes.amounts(750),
                200, 32);
        ChemistryRecipes.recipe("gt.recipe.melter", "xylene/melt-paraxylene",
                ChemistryRecipes.items(ChemistryRecipes.dust("ParaXylene", 1)),
                ChemistryRecipes.items(),
                null, null,
                ChemistryRecipes.fields("GenLiquid_ParaXylene"), ChemistryRecipes.amounts(1000),
                40, 16);
        ChemistryRecipes.recipe("gt.recipe.mixer", "xylene/isomerize",
                ChemistryRecipes.items(ChemistryRecipes.dust("ZSM5", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_MetaXylene", "Hydrogen"), ChemistryRecipes.amounts(1000, 50),
                ChemistryRecipes.fields("GenLiquid_ParaXylene"), ChemistryRecipes.amounts(1000),
                200, 48, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.catalyticcracking", "toluene/disproportionate",
                ChemistryRecipes.items(ChemistryRecipes.dust("ZSM5", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Toluene", "Hydrogen"), ChemistryRecipes.amounts(2000, 100),
                ChemistryRecipes.fields("GenLiquid_Benzene", "GenLiquid_Xylene"), ChemistryRecipes.amounts(1000, 1000),
                200, 48, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.calciner", "toluene/hydrodealkylate",
                ChemistryRecipes.items(),
                ChemistryRecipes.items(ChemistryRecipes.item("dustSmall", "Biphenyl", 1)),
                ChemistryRecipes.fields("GenLiquid_Toluene", "Hydrogen"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenLiquid_Benzene", "Methane"), ChemistryRecipes.amounts(900, 1000),
                160, 64);
        ChemistryRecipes.recipe("gt.recipe.mixer", "ethylbenzene/alkylate",
                ChemistryRecipes.items(ChemistryRecipes.dust("ZSM5", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Benzene", "Ethylene"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenLiquid_Ethylbenzene"), ChemistryRecipes.amounts(1000),
                160, 32, null, new int[]{0});
    }

    // ── Card 6: C5 thermal dimerization, DCPD, norbornene, pentane recycle ──

    private static void cFiveChain() {
        ChemistryRecipes.recipe("gt.recipe.calciner", "c5/dimerize",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_CFiveFraction"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_DimerizedCFiveFraction"), ChemistryRecipes.amounts(1000),
                200, 32);
        ChemistryRecipes.recipe("gt.recipe.distillationtower", "c5/distill",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_DimerizedCFiveFraction"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_Isoprene", "GenLiquid_Pentane", "GenLiquid_Dicyclopentadiene", "GenLiquid_Naphtha"),
                ChemistryRecipes.amounts(360, 380, 130, 130),
                160, 64);
        ChemistryRecipes.recipe("gt.recipe.calciner", "c5/dcpd-crack",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Dicyclopentadiene"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_Cyclopentadiene"), ChemistryRecipes.amounts(2000),
                160, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c5/norbornene",
                ChemistryRecipes.items(),
                ChemistryRecipes.items(ChemistryRecipes.dust("Norbornene", 1)),
                ChemistryRecipes.fields("GenLiquid_Cyclopentadiene", "Ethylene"), ChemistryRecipes.amounts(1000, 1000),
                null, null,
                200, 32);
        ChemistryRecipes.recipe("gt.recipe.steamcracking", "c5/pentane-crack",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("Steam", "GenLiquid_Pentane"), ChemistryRecipes.amounts(1000, 100),
                ChemistryRecipes.fields("Hydrogen", "Methane", "Ethylene", "Propylene", "GenGas_CFourFraction"),
                ChemistryRecipes.amounts(4, 18, 38, 18, 8),
                64, 16);
    }

    // ── Card 7: cumene-process phenol/acetone and bisphenol A ──

    private static void cumenePhenol() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "cumene/alkylate",
                ChemistryRecipes.items(ChemistryRecipes.dust("ZSM5", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Benzene", "Propylene"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenLiquid_Cumene"), ChemistryRecipes.amounts(1000),
                160, 32, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "cumene/oxidize-oxygen",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Cumene", "Oxygen"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenLiquid_CumeneHydroperoxide"), ChemistryRecipes.amounts(1000),
                200, 32);
        ChemistryRecipes.recipe("gt.recipe.mixer", "cumene/oxidize-air",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Cumene", "Air"), ChemistryRecipes.amounts(1000, 5000),
                ChemistryRecipes.fields("GenLiquid_CumeneHydroperoxide", "Nitrogen"), ChemistryRecipes.amounts(1000, 4000),
                400, 32);
        ChemistryRecipes.recipe("gt.recipe.mixer", "cumene/cleave",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_CumeneHydroperoxide", "GenLiquid_SulfuricAcid"), ChemistryRecipes.amounts(1000, 50),
                ChemistryRecipes.fields("GenLiquid_Phenol", "GenLiquid_Acetone"), ChemistryRecipes.amounts(1000, 1000),
                120, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "bisphenol-a",
                ChemistryRecipes.items(),
                ChemistryRecipes.items(ChemistryRecipes.dust("BisphenolA", 1)),
                ChemistryRecipes.fields("GenLiquid_Phenol", "GenLiquid_Acetone", "GenGas_HydrochloricAcid"),
                ChemistryRecipes.amounts(2000, 1000, 100),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                200, 48);
    }
}
