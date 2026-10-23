package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.amounts;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.circuit;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.fields;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

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

    private static void crudeRefining() {
        // GT6CE already distills every crude oil (ChemicalRecipeLoader); reuse its petrol.
        steamCrack("petrol", "Petrol",
                fields("Hydrogen", "Methane", "Ethylene", "Propylene", "GenGas_CFourFraction", "GenLiquid_PyrolysisGasoline"),
                amounts(5, 15, 30, 15, 10, 20));
        steamCrack("raffinate", "GenLiquid_Raffinate",
                fields("Hydrogen", "Methane", "Ethylene", "Propylene", "GenGas_CFourFraction", "GenLiquid_PyrolysisGasoline"),
                amounts(5, 10, 40, 20, 10, 10));
        row("gt.recipe.catalyticcracking", "reform/petrol")
                .in(dust("Platinum", 1))
                .fluidIn("Petrol", 1000)
                .fluidOut("GenLiquid_Reformate", 800, "Hydrogen", 300, "LPG", 100)
                .keep(0)
                .add(160, 48);
    }

    private static void steamCrack(String label, String feed, String[] outFields, int[] outAmounts) {
        row("gt.recipe.steamcracking", "crack/" + label)
                .fluidIn("Steam", 1000, feed, 100)
                .fluidOut(outFields, outAmounts)
                .add(64, 16);
    }

    // ── Card 3: BTX furfural extraction and solvent recycle ──

    private static void btxExtraction() {
        row("gt.recipe.bath", "furfural")
                .in(dust("Wood", 4))
                .fluidIn("GenLiquid_SulfuricAcid", 250)
                .fluidOut("GenLiquid_Furfural", 500, "Dirty_Water", 500)
                .add(160, 24);
        furfuralExtract("pygas", "GenLiquid_PyrolysisGasoline", 2200, 800);
        furfuralExtract("reformate", "GenLiquid_Reformate", 2400, 600);
        row("gt.recipe.distillery", "furfural/strip")
                .in(circuit(1))
                .fluidIn("GenLiquid_FurfuralExtract", 2200)
                .fluidOut("GenLiquid_Furfural", 950, "GenLiquid_BTEX", 1200)
                .add(120, 32);
        row("gt.recipe.distillationtower", "btex/distill")
                .fluidIn("GenLiquid_BTEX", 1000)
                .fluidOut(fields("GenLiquid_Benzene", "GenLiquid_Toluene", "GenLiquid_Ethylbenzene", "GenLiquid_Xylene", "Fuel"),
                        amounts(350, 300, 80, 220, 50))
                .add(160, 64);
        row("gt.recipe.distillery", "btex/early")
                .in(circuit(2))
                .fluidIn("GenLiquid_BTEX", 1000)
                .fluidOut("GenLiquid_Benzene", 300, "GenLiquid_Toluene", 250)
                .add(120, 24);
        row("gt.recipe.distillationtower", "raffinate/cut-c5")
                .fluidIn("GenLiquid_Raffinate", 1000)
                .fluidOut("GenLiquid_CFiveFraction", 300, "GenLiquid_Naphtha", 700)
                .add(120, 48);
    }

    private static void furfuralExtract(String label, String feed, int extractAmount, int raffinateAmount) {
        row("gt.recipe.mixer", "furfural/extract-" + label)
                .fluidIn(feed, 2000, "GenLiquid_Furfural", 1000)
                .fluidOut("GenLiquid_FurfuralExtract", extractAmount, "GenLiquid_Raffinate", raffinateAmount)
                .add(160, 48);
    }

    // ── Card 4: C4 butadiene, butene split, dehydrogenation, sulfolane ──

    private static void cFourChain() {
        row("gt.recipe.mixer", "c4/extract-butadiene")
                .fluidIn("GenGas_CFourFraction", 1000, "GenLiquid_Furfural", 50)
                .fluidOut("GenGas_Butadiene", 400, "GenGas_Butene", 600)
                .add(120, 32);
        row("gt.recipe.distillationtower", "c4/split-butenes")
                .fluidIn("GenGas_Butene", 1000)
                .fluidOut("GenGas_Isobutylene", 500, "GenGas_OneButene", 300, "GenGas_TwoButene", 200)
                .add(160, 48);
        row("gt.recipe.catalyticcracking", "c4/butane-dehydro")
                .in(dust("Chromium", 1))
                .fluidIn("Butane", 1000)
                .fluidOut("GenGas_Butene", 900, "Hydrogen", 900)
                .keep(0)
                .add(160, 48);
        oxydehydro("OneButene");
        oxydehydro("TwoButene");
        ethanolDehydro("", "BioEthanol");
        // Card 1 backfill: GT6CE also has a distinct industrial ethanol, so offer it as a second feedstock.
        if (FluidBridge.distinct("GenLiquid_Ethanol", "BioEthanol")) {
            ethanolDehydro("-pure", "GenLiquid_Ethanol");
        }
        row("gt.recipe.mixer", "c4/sulfolane")
                .in(dust("Nickel", 1))
                .fluidIn("GenGas_Butadiene", 1000, "GenGas_SulfurDioxide", 1000, "Hydrogen", 2000)
                .fluidOut("GenLiquid_Sulfolane", 1000)
                .keep(0)
                .add(200, 48);
        sulfolaneExtract("pygas", "GenLiquid_PyrolysisGasoline", 2300, 700);
        sulfolaneExtract("reformate", "GenLiquid_Reformate", 2500, 500);
        row("gt.recipe.distillery", "sulfolane/strip")
                .in(circuit(1))
                .fluidIn("GenLiquid_SulfolaneExtract", 2300)
                .fluidOut("GenLiquid_Sulfolane", 990, "GenLiquid_BTEX", 1300)
                .add(120, 32);
    }

    private static void ethanolDehydro(String suffix, String ethanol) {
        row("gt.recipe.catalyticcracking", "c4/ethanol-dehydro" + suffix)
                .in(dust("Copper", 1))
                .fluidIn(ethanol, 1000)
                .fluidOut("GenLiquid_Acetaldehyde", 900, "Hydrogen", 900)
                .keep(0)
                .add(120, 32);
        row("gt.recipe.mixer", "c4/ethanol-condense" + suffix)
                .in(dust("Tantalum", 1))
                .fluidIn(ethanol, 1000, "GenLiquid_Acetaldehyde", 1000)
                .fluidOut("GenGas_Butadiene", 800, "DistW", 1600)
                .keep(0)
                .add(200, 48);
    }

    private static void oxydehydro(String butene) {
        row("gt.recipe.mixer", "c4/oxydehydro-" + butene.toLowerCase())
                .in(dust("Molybdenum", 1))
                .fluidIn("GenGas_" + butene, 1000, "Oxygen", 500)
                .fluidOut("GenGas_Butadiene", 900, "DistW", 1000)
                .keep(0)
                .add(160, 64);
    }

    private static void sulfolaneExtract(String label, String feed, int extractAmount, int raffinateAmount) {
        row("gt.recipe.mixer", "sulfolane/extract-" + label)
                .fluidIn(feed, 2000, "GenLiquid_Sulfolane", 1000)
                .fluidOut("GenLiquid_SulfolaneExtract", extractAmount, "GenLiquid_Raffinate", raffinateAmount)
                .add(160, 48);
    }

    // ── Card 5: xylene separation, ZSM-5 isomerization, toluene, ethylbenzene ──

    private static void xyleneTolueneLoop() {
        row("gt.recipe.autoclave", "zsm5")
                .in(dust("Zeolite", 1), dust("SiliconDioxide", 2))
                .out(dust("ZSM5", 2))
                .fluidIn("DistW", 1000)
                .add(400, 64);
        row("gt.recipe.distillationtower", "xylene/ortho")
                .fluidIn("GenLiquid_Xylene", 1000)
                .fluidOut("GenLiquid_OrthoXylene", 200, "GenLiquid_MetaParaXylene", 800)
                .add(160, 64);
        row("gt.recipe.freezer", "xylene/freeze")
                .in(circuit(1))
                .out(item("dustSmall", "ParaXylene", 1))
                .fluidIn("GenLiquid_MetaParaXylene", 1000)
                .fluidOut("GenLiquid_MetaXylene", 750)
                .add(200, 32);
        row("gt.recipe.melter", "xylene/melt-paraxylene")
                .in(dust("ParaXylene", 1))
                .fluidOut("GenLiquid_ParaXylene", 1000)
                .add(40, 16);
        row("gt.recipe.mixer", "xylene/isomerize")
                .in(dust("ZSM5", 1))
                .fluidIn("GenLiquid_MetaXylene", 1000, "Hydrogen", 50)
                .fluidOut("GenLiquid_ParaXylene", 1000)
                .keep(0)
                .add(200, 48);
        row("gt.recipe.catalyticcracking", "toluene/disproportionate")
                .in(dust("ZSM5", 1))
                .fluidIn("GenLiquid_Toluene", 2000, "Hydrogen", 100)
                .fluidOut("GenLiquid_Benzene", 1000, "GenLiquid_Xylene", 1000)
                .keep(0)
                .add(200, 48);
        row("gt.recipe.calciner", "toluene/hydrodealkylate")
                .out(item("dustSmall", "Biphenyl", 1))
                .fluidIn("GenLiquid_Toluene", 1000, "Hydrogen", 1000)
                .fluidOut("GenLiquid_Benzene", 900, "Methane", 1000)
                .add(160, 64);
        row("gt.recipe.mixer", "ethylbenzene/alkylate")
                .in(dust("ZSM5", 1))
                .fluidIn("GenLiquid_Benzene", 1000, "Ethylene", 1000)
                .fluidOut("GenLiquid_Ethylbenzene", 1000)
                .keep(0)
                .add(160, 32);
    }

    // ── Card 6: C5 thermal dimerization, DCPD, norbornene, pentane recycle ──

    private static void cFiveChain() {
        row("gt.recipe.calciner", "c5/dimerize")
                .in(circuit(1))
                .fluidIn("GenLiquid_CFiveFraction", 1000)
                .fluidOut("GenLiquid_DimerizedCFiveFraction", 1000)
                .keep(0)
                .add(200, 32);
        row("gt.recipe.distillationtower", "c5/distill")
                .fluidIn("GenLiquid_DimerizedCFiveFraction", 1000)
                .fluidOut(fields("GenLiquid_Isoprene", "GenLiquid_Pentane", "GenLiquid_Dicyclopentadiene", "GenLiquid_Naphtha"),
                        amounts(360, 380, 130, 130))
                .add(160, 64);
        row("gt.recipe.calciner", "c5/dcpd-crack")
                .in(circuit(1))
                .fluidIn("GenLiquid_Dicyclopentadiene", 1000)
                .fluidOut("GenLiquid_Cyclopentadiene", 2000)
                .keep(0)
                .add(160, 48);
        row("gt.recipe.mixer", "c5/norbornene")
                .out(dust("Norbornene", 1))
                .fluidIn("GenLiquid_Cyclopentadiene", 1000, "Ethylene", 1000)
                .add(200, 32);
        row("gt.recipe.steamcracking", "c5/pentane-crack")
                .fluidIn("Steam", 1000, "GenLiquid_Pentane", 100)
                .fluidOut(fields("Hydrogen", "Methane", "Ethylene", "Propylene", "GenGas_CFourFraction"),
                        amounts(4, 18, 38, 18, 8))
                .add(64, 16);
    }

    // ── Card 7: cumene-process phenol/acetone and bisphenol A ──

    private static void cumenePhenol() {
        row("gt.recipe.mixer", "cumene/alkylate")
                .in(dust("ZSM5", 1))
                .fluidIn("GenLiquid_Benzene", 1000, "Propylene", 1000)
                .fluidOut("GenLiquid_Cumene", 1000)
                .keep(0)
                .add(160, 32);
        row("gt.recipe.mixer", "cumene/oxidize-oxygen")
                .fluidIn("GenLiquid_Cumene", 1000, "Oxygen", 1000)
                .fluidOut("GenLiquid_CumeneHydroperoxide", 1000)
                .add(200, 32);
        row("gt.recipe.mixer", "cumene/oxidize-air")
                .fluidIn("GenLiquid_Cumene", 1000, "Air", 5000)
                .fluidOut("GenLiquid_CumeneHydroperoxide", 1000, "Nitrogen", 4000)
                .add(400, 32);
        row("gt.recipe.mixer", "cumene/cleave")
                .fluidIn("GenLiquid_CumeneHydroperoxide", 1000, "GenLiquid_SulfuricAcid", 50)
                .fluidOut("GenLiquid_Phenol", 1000, "GenLiquid_Acetone", 1000)
                .add(120, 48);
        row("gt.recipe.mixer", "bisphenol-a")
                .out(dust("BisphenolA", 1))
                .fluidIn("GenLiquid_Phenol", 2000, "GenLiquid_Acetone", 1000, "GenGas_HydrochloricAcid", 100)
                .fluidOut("DistW", 1000)
                .add(200, 48);
    }
}
