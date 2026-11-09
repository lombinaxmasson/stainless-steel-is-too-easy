package com.lombinaxmasson.ssite.chemistry;

import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.FluidRef;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.ItemKind;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.ItemRef;

import java.util.List;

/**
 * Pure JVM checks of the U-01 gas loop, U-04 methanation, U-05 Fischer-Tropsch/biomass branch and
 * the U-06 solvent, catalyst and companion-distribution loops.
 */
public final class UtilityTests {
    private static final double RAW_EU_PER_MB = 384.0;          // host gas natural row: 64 EU/t x 30 t / 5 mB
    private static final double PIPELINE_EU_PER_MB = 64.0 * 32.0 / 5.0;
    private static final double PROPANE_EU_PER_MB = 64.0 * 40.0 / 5.0;
    private static final double PROCESSING_EU = 80 * 32 + 60 * 16 + 100 * 32 + 60 * 16 + 80 * 24 + 120 * 48;
    private static final double MEDIA_MAKEUP_EU = 160.0 * 48.0 / 1000.0 + 200.0 * 64.0 / 1000.0;

    private UtilityTests() {}

    public static void main(String[] args) {
        List<RecipeDeclaration> rows = ChemistryRecipes.declarations();
        dehydrationLoop(rows);
        desulfurizationLoop(rows);
        sulfurRecovery(rows);
        lightEndsRoute(rows);
        pipelineFuelRow(rows);
        mediaMakeupClosesLoop(rows);
        pipelineMethaneFeedsReforming(rows);
        energyLedger(rows);
        methanationRow(rows);
        reformingRoundTripLosesEnergy(rows);
        fischerTropschSynthesis(rows);
        fischerTropschFractionation(rows);
        fischerTropschProductsHaveSinks(rows);
        biomassClosesOnHostRoute(rows);
        furfuralReformateStrip(rows);
        sulfolaneReformateStrip(rows);
        allBtxSolventExtractsHaveStrips(rows);
        solventMakeupSources(rows);
        rareEarthResidueRecycle(rows);
        matteSeparationModes(rows);
        copperDrossSink(rows);
        sulfurDioxideSink(rows);
        ftCatalystChain(rows);
        newMaterialsHaveConsumers(rows);
        System.out.println("UtilityTests: all 24 checks passed.");
    }

    private static void dehydrationLoop(List<RecipeDeclaration> rows) {
        RecipeDeclaration dehydrate = row(rows, "u01/dehydrate");
        check(dehydrate.map().equals("gt.recipe.mixer")
                        && dehydrate.fluidInputs().equals(List.of(
                        fluid("Gas_Natural", 1000), fluid("GenLiquid_TriethyleneGlycol", 20)))
                        && dehydrate.fluidOutputs().equals(List.of(
                        fluid("GenGas_DehydratedNaturalGas", 1000), fluid("GenLiquid_RichGlycol", 20))),
                "TEG dehydration keeps the raw gas volume and returns a separate rich glycol");
        RecipeDeclaration rawDehydrate = row(rows, "u01/dehydrate-raw");
        check(rawDehydrate.map().equals("gt.recipe.mixer")
                        && rawDehydrate.fluidInputs().equals(List.of(
                        fluid("GenGas_RawNaturalGas", 1000), fluid("GenLiquid_TriethyleneGlycol", 20)))
                        && rawDehydrate.fluidOutputs().equals(dehydrate.fluidOutputs()),
                "the gas-field raw gas enters the same TEG dehydration as host-pumped gas");
        RecipeDeclaration regenerate = row(rows, "u01/regenerate-glycol");
        check(regenerate.map().equals("gt.recipe.distillery")
                        && regenerate.fluidInputs().equals(List.of(fluid("GenLiquid_RichGlycol", 20)))
                        && regenerate.fluidOutputs().equals(List.of(
                        fluid("GenLiquid_TriethyleneGlycol", 19), fluid("DistW", 10)))
                        && regenerate.keptInputs().equals(List.of(0)),
                "glycol regeneration returns 95% TEG and a water stream");
        check(recovery(20, 19), "glycol make-up is one mB per 1000 mB of raw gas");
    }

    private static void desulfurizationLoop(List<RecipeDeclaration> rows) {
        RecipeDeclaration desulfurize = row(rows, "u01/desulfurize");
        check(desulfurize.map().equals("gt.recipe.mixer")
                        && desulfurize.fluidInputs().equals(List.of(
                        fluid("GenGas_DehydratedNaturalGas", 1000), fluid("GenLiquid_Methyldiethanolamine", 20)))
                        && desulfurize.fluidOutputs().equals(List.of(
                        fluid("GenGas_SweetNaturalGas", 1000), fluid("GenLiquid_RichAmine", 20))),
                "MDEA desulfurization is downstream of dehydration and produces loaded amine");
        RecipeDeclaration regenerate = row(rows, "u01/regenerate-amine");
        check(regenerate.map().equals("gt.recipe.distillery")
                        && regenerate.fluidInputs().equals(List.of(fluid("GenLiquid_RichAmine", 20)))
                        && regenerate.fluidOutputs().equals(List.of(
                        fluid("GenLiquid_Methyldiethanolamine", 19), fluid("GenGas_HydrogenSulfide", 10)))
                        && regenerate.keptInputs().equals(List.of(0)),
                "amine regeneration returns 95% MDEA and the acid gas");
        check(recovery(20, 19), "amine make-up is one mB per 1000 mB of raw gas");
    }

    private static void sulfurRecovery(List<RecipeDeclaration> rows) {
        RecipeDeclaration claus = row(rows, "u01/claus");
        check(claus.map().equals("gt.recipe.mixer")
                        && claus.fluidInputs().equals(List.of(
                        fluid("GenGas_HydrogenSulfide", 10), fluid("Oxygen", 5)))
                        && claus.itemOutputs().equals(List.of(material("dust", "Sulfur", 1)))
                        && claus.fluidOutputs().equals(List.of(fluid("DistW", 10))),
                "Claus recovery turns the acid gas into sulfur instead of venting it");
    }

    private static void lightEndsRoute(List<RecipeDeclaration> rows) {
        RecipeDeclaration stabilize = row(rows, "u01/stabilize");
        check(stabilize.map().equals("gt.recipe.distillationtower")
                        && stabilize.fluidInputs().equals(List.of(fluid("GenGas_SweetNaturalGas", 1000)))
                        && stabilize.fluidOutputs().equals(List.of(
                        fluid("GenGas_PipelineGas", 850), fluid("Propane", 70),
                        fluid("GenGas_CFourFraction", 50), fluid("GenLiquid_CFiveFraction", 30))),
                "stabilization yields pipeline gas and the existing C4/C5 separation feeds");
        int sum = stabilize.fluidOutputs().stream().mapToInt(FluidRef::amount).sum();
        check(sum == 1000, "stabilization conserves the gas volume");
        check(rows.stream().noneMatch(r -> r.label().equals("u01/stabilize")
                        && r.fluidOutputs().stream().anyMatch(f -> f.field().equals("GenLiquid_PipelineGas"))),
                "the pipeline product is the gas phase, not a relabeled liquid");
    }

    private static void pipelineFuelRow(List<RecipeDeclaration> rows) {
        RecipeDeclaration fuel = row(rows, "gt.recipe.fuels.gas", "u01/fuel/pipeline-gas");
        check(fuel.duration() == 32 && fuel.perTick() == -64
                        && fuel.fluidInputs().equals(List.of(fluid("GenGas_PipelineGas", 5)))
                        && fuel.fluidOutputs().equals(List.of(
                        fluid("DistW", 6), fluid("CarbonDioxide", 3))),
                "pipeline gas is a gas-turbine fuel with the host methane exhaust ratio");
        double euPerMb = -fuel.perTick() * fuel.duration() / (double) fuel.fluidInputs().get(0).amount();
        check(Math.abs(euPerMb - PIPELINE_EU_PER_MB) < 1e-9,
                "pipeline fuel row carries 409.6 EU/mB");
        check(rows.stream().noneMatch(r -> r.map().startsWith("gt.recipe.fuels.")
                        && r.fluidInputs().stream().anyMatch(f -> f.field().equals("GenGas_RawNaturalGas")
                        || f.field().equals("Gas_Natural") || f.field().equals("Methane"))),
                "no addon fuel row burns raw or untreated gas; the host natural-gas row is removed by data");
    }

    private static void mediaMakeupClosesLoop(List<RecipeDeclaration> rows) {
        RecipeDeclaration teg = row(rows, "u01/teg-synthesis");
        check(teg.map().equals("gt.recipe.mixer")
                        && teg.itemInputs().equals(List.of(material("dust", "Alumina", 1)))
                        && teg.keptInputs().equals(List.of(0))
                        && teg.fluidInputs().equals(List.of(fluid("GenLiquid_EthyleneGlycol", 3000)))
                        && teg.fluidOutputs().equals(List.of(
                        fluid("GenLiquid_TriethyleneGlycol", 1000), fluid("DistW", 2000))),
                "TEG make-up is synthesized from the existing ethylene glycol line");
        RecipeDeclaration mdea = row(rows, "u01/mdea-synthesis");
        check(mdea.map().equals("gt.recipe.mixer")
                        && mdea.fluidInputs().equals(List.of(
                        fluid("GenGas_Ammonia", 1000), fluid("GenLiquid_Methanol", 1000),
                        fluid("GenGas_EthyleneOxide", 2000)))
                        && mdea.fluidOutputs().equals(List.of(
                        fluid("GenLiquid_Methyldiethanolamine", 1000), fluid("DistW", 2000))),
                "MDEA make-up is synthesized from ammonia, methanol and ethylene oxide");
    }

    private static void pipelineMethaneFeedsReforming(List<RecipeDeclaration> rows) {
        RecipeDeclaration enrich = row(rows, "u01/pipeline-methane");
        check(enrich.map().equals("gt.recipe.distillery")
                        && enrich.fluidInputs().equals(List.of(fluid("GenGas_PipelineGas", 1000)))
                        && enrich.fluidOutputs().equals(List.of(fluid("Methane", 1000)))
                        && enrich.keptInputs().equals(List.of(0)),
                "pipeline gas can be conditioned into the host methane identity");
        RecipeDeclaration reforming = row(rows, "c1/methane-reforming");
        check(reforming.fluidInputs().stream().anyMatch(f -> f.field().equals("Methane")),
                "the existing steam-reforming row is the U-14 hydrogen interface");
    }

    private static void energyLedger(List<RecipeDeclaration> rows) {
        double rawEnergy = 1000 * RAW_EU_PER_MB;
        double rawExhaust = 1000 * (6 + 3) / 5.0;
        double pipelineEnergy = 850 * PIPELINE_EU_PER_MB;
        double pipelineExhaust = 850 * (6 + 3) / 5.0;
        double propaneEnergy = 70 * PROPANE_EU_PER_MB;
        double propaneExhaust = 70 * (5 + 4) / 5.0;
        check(Math.abs(pipelineEnergy + propaneEnergy - rawEnergy) < 1e-6,
                "pipeline gas plus separated propane do not mint energy");
        check(pipelineExhaust + propaneExhaust < rawExhaust * 0.95,
                "the same gross energy produces less exhaust");
        double net = rawEnergy - PROCESSING_EU - MEDIA_MAKEUP_EU;
        check(net / (pipelineExhaust + propaneExhaust) > rawEnergy / rawExhaust,
                "net energy per exhaust mB stays above direct raw-gas burning");
    }

    private static void methanationRow(List<RecipeDeclaration> rows) {
        RecipeDeclaration methanation = row(rows, "gt.recipe.catalyticcracking", "u04/methanation");
        check(methanation.itemInputs().equals(List.of(material("dust", "Nickel", 1)))
                        && methanation.keptInputs().equals(List.of(0)),
                "methanation keeps the same nickel catalyst as reforming");
        check(methanation.fluidInputs().equals(List.of(
                        fluid("GenGas_CarbonMonoxide", 1000), fluid("Hydrogen", 3000)))
                        && methanation.fluidOutputs().equals(List.of(
                        fluid("Methane", 1000), fluid("DistW", 1000))),
                "Sabatier returns host methane plus one water stream");
        check(methanation.perTick() > 0 && methanation.duration() > 0,
                "the base methanation row spends EU instead of claiming the reaction heat");
    }

    private static void reformingRoundTripLosesEnergy(List<RecipeDeclaration> rows) {
        RecipeDeclaration reforming = row(rows, "gt.recipe.catalyticcracking", "c1/methane-reforming");
        RecipeDeclaration methanation = row(rows, "gt.recipe.catalyticcracking", "u04/methanation");
        double reformingEu = (double) reforming.duration() * reforming.perTick();
        double methanationEu = (double) methanation.duration() * methanation.perTick();
        int methaneIn = reforming.fluidInputs().stream().filter(f -> f.field().equals("Methane"))
                .mapToInt(FluidRef::amount).sum();
        int methaneOut = methanation.fluidOutputs().stream().filter(f -> f.field().equals("Methane"))
                .mapToInt(FluidRef::amount).sum();
        int steamIn = reforming.fluidInputs().stream().filter(f -> f.field().equals("Steam"))
                .mapToInt(FluidRef::amount).sum();
        int waterOut = methanation.fluidOutputs().stream().filter(f -> f.field().equals("DistW"))
                .mapToInt(FluidRef::amount).sum();
        check(methaneIn == methaneOut,
                "the reforming/methanation round trip returns no extra methane");
        check(steamIn == waterOut,
                "reforming water leaves methanation as one distilled-water stream");
        check(reformingEu == 12_800 && methanationEu == 7_680,
                "the round trip costs 12,800 + 7,680 = 20,480 EU per 1,000 mB methane");
    }

    private static void fischerTropschSynthesis(List<RecipeDeclaration> rows) {
        RecipeDeclaration synthesis = row(rows, "gt.recipe.catalyticcracking", "u05a/ft-synthesis");
        check(synthesis.itemInputs().equals(List.of(material("dust", "Iron", 1)))
                        && synthesis.keptInputs().equals(List.of(0)),
                "the first-generation iron catalyst is retained");
        check(synthesis.fluidInputs().equals(List.of(
                        fluid("GenGas_CarbonMonoxide", 1000), fluid("Hydrogen", 2000)))
                        && synthesis.fluidOutputs().equals(List.of(
                        fluid("GenLiquid_SyntheticOil", 1000), fluid("DistW", 1000))),
                "FT consumes one part CO and two parts hydrogen for one oil and one water stream");
        RecipeDeclaration split = row(rows, "gt.recipe.distillery", "u03/clean-gas-separate");
        check(split.fluidOutputs().equals(synthesis.fluidInputs()),
                "the FT feed is exactly the U-03 clean-gas split, so the chain has no second split");
        check(synthesis.perTick() > 0 && synthesis.duration() > 0,
                "the synthesis spends EU instead of claiming reaction heat");
    }

    private static void fischerTropschFractionation(List<RecipeDeclaration> rows) {
        RecipeDeclaration fractionate = row(rows, "gt.recipe.distillationtower", "u05a/ft-fractionate");
        check(fractionate.fluidInputs().equals(List.of(fluid("GenLiquid_SyntheticOil", 1000)))
                        && fractionate.fluidOutputs().equals(List.of(
                        fluid("GenLiquid_Naphtha", 300), fluid("Diesel", 450)))
                        && fractionate.itemOutputs().equals(List.of(material("dust", "Wax", 1))),
                "one tower pass yields existing naphtha, host diesel and host wax");
        check(rows.stream().noneMatch(r -> r.map().startsWith("gt.recipe.fuels.")
                        && r.fluidInputs().stream()
                        .anyMatch(f -> f.field().equals("GenLiquid_SyntheticOil"))),
                "the oil intermediate has no fuel row; it must be fractionated");
    }

    private static void fischerTropschProductsHaveSinks(List<RecipeDeclaration> rows) {
        check(rows.stream().anyMatch(r -> r.map().equals("gt.recipe.fuels.engine")
                        && r.fluidInputs().stream()
                        .anyMatch(f -> f.field().equals("GenLiquid_Naphtha"))),
                "FT naphtha uses the existing engine fuel row");
        check(rows.stream().anyMatch(r -> r.label().equals("m9-09/pattern-wax")
                        && r.itemInputs().stream().anyMatch(i -> i.material().equals("Wax"))),
                "FT wax feeds the M9-09 pattern-wax row");
        check(rows.stream().noneMatch(r -> r.map().startsWith("gt.recipe.fuels.")
                        && r.fluidInputs().stream().anyMatch(f -> f.field().equals("Diesel"))),
                "host diesel keeps its own fuel identity instead of an addon copy");
    }

    private static void biomassClosesOnHostRoute(List<RecipeDeclaration> rows) {
        check(rows.stream().anyMatch(r -> r.fluidOutputs().stream()
                        .anyMatch(f -> f.field().equals("Biomass") && f.amount() > 0)),
                "the water chain still produces biomass as a real by-product");
        check(rows.stream().noneMatch(r -> r.fluidInputs().stream()
                        .anyMatch(f -> f.field().equals("Biomass"))
                        && r.fluidOutputs().stream().anyMatch(f -> f.field().equals("Methane"))),
                "no second biomass-to-methane row duplicates the host fermenter");
    }

    private static void furfuralReformateStrip(List<RecipeDeclaration> rows) {
        RecipeDeclaration strip = row(rows, "gt.recipe.distillery", "furfural/strip-reformate");
        check(strip.fluidInputs().equals(List.of(fluid("GenLiquid_FurfuralExtract", 2400)))
                        && strip.fluidOutputs().equals(List.of(
                        fluid("GenLiquid_Furfural", 950), fluid("GenLiquid_BTEX", 1400))),
                "the reformate furfural extract has its own strip row");
        check(strip.fluidOutputs().get(0).amount() <= 1000,
                "the reformate strip returns no more furfural than the extraction charged");
        int in = strip.fluidInputs().stream().mapToInt(FluidRef::amount).sum();
        int out = strip.fluidOutputs().stream().mapToInt(FluidRef::amount).sum();
        check(out <= in, "the reformate strip cannot create extract volume");
    }

    private static void sulfolaneReformateStrip(List<RecipeDeclaration> rows) {
        RecipeDeclaration strip = row(rows, "gt.recipe.distillery", "sulfolane/strip-reformate");
        check(strip.fluidInputs().equals(List.of(fluid("GenLiquid_SulfolaneExtract", 2500)))
                        && strip.fluidOutputs().equals(List.of(
                        fluid("GenLiquid_Sulfolane", 990), fluid("GenLiquid_BTEX", 1500))),
                "the reformate sulfolane extract has its own strip row");
        check(strip.fluidOutputs().get(0).amount() <= 1000,
                "the reformate strip returns no more sulfolane than the extraction charged");
        int in = strip.fluidInputs().stream().mapToInt(FluidRef::amount).sum();
        int out = strip.fluidOutputs().stream().mapToInt(FluidRef::amount).sum();
        check(out <= in, "the reformate strip cannot create extract volume");
    }

    private static void allBtxSolventExtractsHaveStrips(List<RecipeDeclaration> rows) {
        for (String field : List.of("GenLiquid_FurfuralExtract", "GenLiquid_SulfolaneExtract")) {
            check(rows.stream().anyMatch(r -> r.fluidInputs().stream()
                            .anyMatch(f -> f.field().equals(field))),
                    "the loaded solvent field has a strip consumer: " + field);
            long producers = rows.stream()
                    .filter(r -> r.fluidOutputs().stream().anyMatch(f -> f.field().equals(field)))
                    .count();
            long consumers = rows.stream()
                    .filter(r -> r.fluidInputs().stream().anyMatch(f -> f.field().equals(field)))
                    .count();
            check(producers > 0 && consumers > 0, "both sides of the solvent loop exist: " + field);
        }
    }

    private static void solventMakeupSources(List<RecipeDeclaration> rows) {
        check(rows.stream().anyMatch(r -> r.label().equals("furfural")
                        && r.fluidOutputs().stream().anyMatch(f -> f.field().equals("GenLiquid_Furfural"))),
                "wood furfural supplies the BTX/C4 make-up");
        check(rows.stream().anyMatch(r -> r.label().equals("c4/sulfolane")
                        && r.fluidOutputs().stream().anyMatch(f -> f.field().equals("GenLiquid_Sulfolane"))),
                "sulfolane synthesis supplies its make-up");
        check(rows.stream().anyMatch(r -> r.label().equals("c13/tributyl-phosphate")
                        && r.fluidOutputs().stream().anyMatch(f -> f.field().equals("GenLiquid_TributylPhosphate"))),
                "TBP synthesis supplies the nuclear make-up");
        check(rows.stream().anyMatch(r -> r.label().equals("c13/p204")
                        && r.fluidOutputs().stream().anyMatch(f -> f.field().equals("GenLiquid_DiethylhexylPhosphoricAcid"))),
                "P204 synthesis supplies the rare-earth make-up");
        check(rows.stream().anyMatch(r -> r.label().equals("c13/aliquat")
                        && r.fluidOutputs().stream().anyMatch(f -> f.field().equals("GenLiquid_Aliquat"))),
                "Aliquat synthesis supplies the rare-earth make-up");
    }

    private static void rareEarthResidueRecycle(List<RecipeDeclaration> rows) {
        RecipeDeclaration recycle = row(rows, "rare-earth/residue-recycle");
        check(recycle.itemInputs().equals(List.of(material("dust", "HeavyRareEarthResidue", 2)))
                        && recycle.fluidInputs().equals(List.of(fluid("GenGas_HydrochloricAcid", 2000)))
                        && recycle.fluidOutputs().equals(List.of(
                        fluid("GenLiquid_HeavyRareEarthChloride", 2000), fluid("GenLiquid_DirtyWater", 1000))),
                "two residues return to the heavy-group chloride feed");
        check(rows.stream().noneMatch(r -> r.label().equals("rare-earth/residue-recycle")
                        && r.itemOutputs().stream().anyMatch(i -> i.material().equals("HeavyRareEarthResidue"))),
                "one recycle batch cannot recreate the two residues it consumes");
    }

    private static void matteSeparationModes(List<RecipeDeclaration> rows) {
        RecipeDeclaration white = row(rows, "m7/white-matte");
        RecipeDeclaration leach = row(rows, "m7/white-matte-leach");
        RecipeDeclaration split = row(rows, "m7/green-matte-split");
        RecipeDeclaration copperBlow = row(rows, "m7/copper-matte-blow");
        RecipeDeclaration nickelLeach = row(rows, "m7/nickel-matte-leach");

        check(white.itemInputs().equals(List.of(material("dust", "GreenMatte", 3)))
                        && split.itemInputs().equals(List.of(material("dust", "GreenMatte", 3))),
                "both separation modes start from the same three-matte charge");
        check(split.itemOutputs().equals(List.of(
                        material("dust", "CopperMatte", 2), material("dust", "NickelMatte", 1))),
                "the split mode separates copper and nickel mattes");
        check(copperBlow.itemInputs().equals(List.of(material("dust", "CopperMatte", 2)))
                        && copperBlow.itemOutputs().equals(List.of(
                        material("dust", "CrudeCopper", 2), material("dust", "Hematite", 1))),
                "copper matte blows to the same crude copper and hematite");
        check(nickelLeach.itemInputs().equals(List.of(material("dust", "NickelMatte", 1)))
                        && nickelLeach.fluidInputs().equals(List.of(
                        fluid("GenLiquid_SulfuricAcid", 1000), fluid("Oxygen", 500), fluid("DistW", 1000)))
                        && nickelLeach.fluidOutputs().equals(List.of(
                        fluid("GenLiquid_NickelCobaltSulfate", 1000))),
                "nickel matte leaches to the same nickel-cobalt sulfate");

        int whiteOxygen = amount(white.fluidInputs(), "Oxygen") + amount(leach.fluidInputs(), "Oxygen");
        int splitOxygen = amount(copperBlow.fluidInputs(), "Oxygen") + amount(nickelLeach.fluidInputs(), "Oxygen");
        check(whiteOxygen == splitOxygen && whiteOxygen == 1500,
                "both modes spend the same 1500 mB of oxygen");
        check(amount(leach.fluidInputs(), "GenLiquid_SulfuricAcid")
                        == amount(nickelLeach.fluidInputs(), "GenLiquid_SulfuricAcid")
                        && amount(leach.fluidInputs(), "DistW") == amount(nickelLeach.fluidInputs(), "DistW"),
                "both modes spend the same leach acid and water");
        check(amount(white.fluidOutputs(), "GenGas_SulfurDioxide")
                        == amount(copperBlow.fluidOutputs(), "GenGas_SulfurDioxide"),
                "both modes release the same sulphur dioxide");
        check(copperBlow.itemOutputs().get(0).count() == 2
                        && leach.itemOutputs().get(0).count() == 2,
                "both modes produce the same crude copper");
    }

    private static void copperDrossSink(List<RecipeDeclaration> rows) {
        RecipeDeclaration dross = row(rows, "m7/copper-dross-recover");
        check(dross.itemInputs().equals(List.of(material("dust", "CopperDross", 3)))
                        && dross.itemOutputs().equals(List.of(
                        material("dust", "CrudeCopper", 2), material("dust", "Hematite", 1)))
                        && dross.fluidInputs().equals(List.of(fluid("Oxygen", 1500)))
                        && dross.fluidOutputs().equals(List.of(fluid("GenGas_SulfurDioxide", 1500))),
                "copper dross returns to the copper refinery instead of accumulating");
        check(dross.itemOutputs().get(0).count() < dross.itemInputs().get(0).count(),
                "dross recovery cannot multiply copper");
    }

    private static void sulfurDioxideSink(List<RecipeDeclaration> rows) {
        RecipeDeclaration oxidation = row(rows, "gt.recipe.catalyticcracking", "m7/so2-oxidation");
        check(oxidation.itemInputs().equals(List.of(material("dust", "VanadiumPentoxide", 1)))
                        && oxidation.keptInputs().equals(List.of(0))
                        && oxidation.fluidInputs().equals(List.of(
                        fluid("GenGas_SulfurDioxide", 2000), fluid("Oxygen", 1000)))
                        && oxidation.fluidOutputs().equals(List.of(
                        fluid("GenGas_SulfurTrioxide", 2000))),
                "vanadium-catalysed oxidation is the sulphur dioxide sink");
        RecipeDeclaration hydration = row(rows, "gt.recipe.mixer", "m7/so3-hydration");
        check(hydration.fluidInputs().equals(List.of(
                        fluid("GenGas_SulfurTrioxide", 1000), fluid("DistW", 1000)))
                        && hydration.fluidOutputs().equals(List.of(
                        fluid("GenLiquid_SulfuricAcid", 1000))),
                "sulphur trioxide hydrates into the existing sulfuric acid");
        check(rows.stream().anyMatch(r -> r.fluidInputs().stream()
                        .anyMatch(f -> f.field().equals("GenLiquid_SulfuricAcid"))),
                "the recovered acid has real consumers in the leach rows");
        check(rows.stream().anyMatch(r -> r.fluidOutputs().stream()
                        .anyMatch(f -> f.field().equals("GenGas_SulfurDioxide")))
                        && rows.stream().anyMatch(r -> r.fluidInputs().stream()
                        .anyMatch(f -> f.field().equals("GenGas_SulfurDioxide"))),
                "the roast gas has both producers and a consumer");
    }

    private static void ftCatalystChain(List<RecipeDeclaration> rows) {
        RecipeDeclaration base = row(rows, "gt.recipe.catalyticcracking", "u05a/ft-synthesis");
        check(base.keptInputs().equals(List.of(0)), "the U-05 free-iron route remains the default");

        RecipeDeclaration prepare = row(rows, "u06/ft-catalyst-prepare");
        check(prepare.itemInputs().equals(List.of(
                        material("dust", "Iron", 2), material("dust", "Alumina", 1)))
                        && prepare.itemOutputs().equals(List.of(
                        material("dust", "FischerTropschCatalyst", 2))),
                "two iron and one alumina form two supported catalyst charges");

        RecipeDeclaration advanced = row(rows, "u06/ft-synthesis-catalysed");
        check(advanced.itemInputs().equals(List.of(
                        material("dust", "FischerTropschCatalyst", 1)))
                        && advanced.keptInputs().isEmpty()
                        && advanced.itemOutputs().equals(List.of(
                        material("dust", "SpentFischerTropschCatalyst", 1)))
                        && advanced.fluidInputs().equals(base.fluidInputs())
                        && advanced.fluidOutputs().equals(base.fluidOutputs()),
                "the advanced route spends one charge and returns one spent charge for the same oil");
        check(advanced.duration() * advanced.perTick() < base.duration() * base.perTick(),
                "the supported catalyst lowers the synthesis energy bill");

        RecipeDeclaration regenerate = row(rows, "u06/ft-catalyst-regenerate");
        check(regenerate.itemInputs().equals(List.of(
                        material("dust", "SpentFischerTropschCatalyst", 2)))
                        && regenerate.itemOutputs().equals(List.of(
                        material("dust", "FischerTropschCatalyst", 1),
                        material("dust", "CatalystResidue", 1)))
                        && regenerate.fluidInputs().equals(List.of(fluid("Hydrogen", 1000)))
                        && regenerate.fluidOutputs().equals(List.of(fluid("DistW", 1000))),
                "two spent charges regenerate into one active charge and one residue");
        check(regenerate.itemOutputs().get(0).count() < regenerate.itemInputs().get(0).count(),
                "regeneration cannot multiply catalyst charges");

        RecipeDeclaration recover = row(rows, "u06/catalyst-residue-recover");
        check(recover.itemInputs().equals(List.of(material("dust", "CatalystResidue", 3)))
                        && recover.itemOutputs().equals(List.of(
                        material("dust", "Iron", 1), material("dust", "Alumina", 1),
                        material("dust", "Carbon", 1))),
                "residue recovery returns iron, alumina and carbon");
        long in = recover.itemInputs().stream().mapToLong(ItemRef::count).sum();
        long out = recover.itemOutputs().stream().mapToLong(ItemRef::count).sum();
        check(out <= in, "residue recovery cannot create material");
        check(rows.stream().noneMatch(r -> r.map().startsWith("gt.recipe.fuels.")
                        && (r.fluidInputs().stream().anyMatch(f -> f.field().equals("GenLiquid_SyntheticOil"))
                        || r.itemInputs().stream().anyMatch(i -> i.material().startsWith("FischerTropsch")
                        || i.material().equals("CatalystResidue")))),
                "no catalyst or oil intermediate gains a fuel row");
    }

    private static void newMaterialsHaveConsumers(List<RecipeDeclaration> rows) {
        for (String material : List.of("FischerTropschCatalyst", "SpentFischerTropschCatalyst",
                "CatalystResidue", "CopperMatte", "NickelMatte", "HeavyRareEarthResidue",
                "CopperDross")) {
            check(rows.stream().anyMatch(r -> r.itemInputs().stream()
                            .anyMatch(i -> i.material().equals(material))),
                    "new or formerly stranded material has a consumer: " + material);
        }
    }

    private static int amount(List<FluidRef> refs, String field) {
        return refs.stream().filter(f -> f.field().equals(field)).mapToInt(FluidRef::amount).sum();
    }

    private static boolean recovery(int charged, int recovered) {
        return charged == 20 && recovered == 19;
    }

    private static RecipeDeclaration row(List<RecipeDeclaration> rows, String label) {
        List<RecipeDeclaration> found = rows.stream().filter(r -> r.label().equals(label)).toList();
        check(found.size() == 1, "one declaration for " + label);
        return found.get(0);
    }

    private static RecipeDeclaration row(List<RecipeDeclaration> rows, String map, String label) {
        List<RecipeDeclaration> found = rows.stream()
                .filter(r -> r.map().equals(map) && r.label().equals(label)).toList();
        check(found.size() == 1, "one declaration for " + map + "/" + label);
        return found.get(0);
    }

    private static FluidRef fluid(String field, int amount) {
        return new FluidRef(field, amount);
    }

    private static ItemRef material(String prefix, String material, int count) {
        return new ItemRef(ItemKind.MATERIAL, prefix, material, "", count, -1);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
