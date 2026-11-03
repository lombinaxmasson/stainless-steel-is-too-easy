package com.lombinaxmasson.ssite.chemistry;

import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.FluidRef;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.ItemKind;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.ItemRef;

import java.util.List;

/** Pure JVM checks of the U-01 gas treatment loop, fuel row and energy ledger. */
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
        System.out.println("UtilityTests: all 8 checks passed.");
    }

    private static void dehydrationLoop(List<RecipeDeclaration> rows) {
        RecipeDeclaration dehydrate = row(rows, "u01/dehydrate");
        check(dehydrate.map().equals("gt.recipe.mixer")
                        && dehydrate.fluidInputs().equals(List.of(
                        fluid("Gas_Natural", 1000), fluid("GenLiquid_TriethyleneGlycol", 20)))
                        && dehydrate.fluidOutputs().equals(List.of(
                        fluid("GenGas_DehydratedNaturalGas", 1000), fluid("GenLiquid_RichGlycol", 20))),
                "TEG dehydration keeps the raw gas volume and returns a separate rich glycol");
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
                        && r.fluidInputs().stream().anyMatch(f -> f.field().equals("Gas_Natural")
                        || f.field().equals("Methane"))),
                "the addon does not replace the host natural-gas or methane fuel rows");
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
