package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.amounts;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.circuit;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.fields;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

/** M2 water-treatment supplies, five purification stages and brine recovery. */
public final class WaterRecipes {
    private WaterRecipes() {}

    static void add() {
        waterTreatment();
        waterPurification();
        brineChain();
    }

    private static void waterTreatment() {
        alum();
        celluloseAcetate();
        ozone();
    }

    private static void alum() {
        // Alunite leaching; the hydroxide + sulphuric-acid route collides with a built-in GT6CE row.
        row("gt.recipe.bath", "alum/alunite")
                .in(dust("Alunite", 2))
                .out(dust("Alum", 2), dust("SiliconDioxide", 1))
                .fluidIn("GenLiquid_SulfuricAcid", 1000)
                .add(100, 16);
    }

    private static void celluloseAcetate() {
        for (String pulp : new String[]{"Wood", "Paper"}) {
            for (String vinegar : new String[]{"Vinegar_Grape", "Vinegar_Apple"}) {
                boolean grape = vinegar.equals("Vinegar_Grape");
                var recipe = row("gt.recipe.mixer", "cellulose-acetate/" + (grape ? "" : "apple/") + pulp)
                        .in(dust(pulp, 2))
                        .out(dust("CelluloseAcetate", 2))
                        .fluidIn(vinegar, 2000, "GenLiquid_SulfuricAcid", 250)
                        .fluidOut("GenLiquid_DirtyWater", 1000);
                if (grape) recipe.requiresDistinctFluids(vinegar, "Vinegar_Apple");
                recipe.add(160, 30);
            }
        }
        row("gt.recipe.press", "cellulose-acetate/plate")
                .in(dust("CelluloseAcetate", 1), circuit(1))
                .out(item("plate", "CelluloseAcetate", 1))
                .keep(1)
                .add(40, 16);
        row("gt.recipe.rollingmill", "cellulose-acetate/foil")
                .in(item("plate", "CelluloseAcetate", 1))
                .out(item("foil", "CelluloseAcetate", 4))
                .add(40, 16);
    }

    private static void ozone() {
        row("gt.recipe.electrolyzer", "ozone")
                .in(circuit(3))
                .fluidIn("Oxygen", 3000)
                .fluidOut("GenGas_Ozone", 2000)
                .add(100, 64);
    }

    private static void waterPurification() {
        clarifiedWater();
        sludgeRecovery();
        demineralizedWater();
        deionizedWater();
        filteredWater();
        ultrapureWater();
    }

    private static void clarifiedWater() {
        for (String source : new String[]{"Water", "GenLiquid_DirtyWater"}) {
            String label = source.equals("GenLiquid_DirtyWater") ? "DirtyWater" : source;
            for (String flocculant : new String[]{"Alum", "VitriolOfClay"}) {
                row("gt.recipe.bath", "clarified/" + label + "/" + flocculant)
                        .in(dust(flocculant, 1))
                        .fluidIn(source, 4000)
                        .fluidOut("GenLiquid_ClarifiedWater", 3600, "GenLiquid_Sludge", 400)
                        .add(100, 16);
            }
        }
    }

    private static void sludgeRecovery() {
        row("gt.recipe.drying", "sludge/dry")
                .out(dust("Clay", 1))
                .fluidIn("GenLiquid_Sludge", 1000)
                .fluidOut("Biomass", 250, "GenLiquid_DirtyWater", 500)
                .add(120, 16);
        row("gt.recipe.centrifuge", "sludge/centrifuge")
                .out(dust("Clay", 2), dust("Sand", 1), item("dustTiny", "RareEarth", 1))
                .fluidIn("GenLiquid_Sludge", 2000)
                .fluidOut("Biomass", 500, "GenLiquid_DirtyWater", 1000)
                .chances(10000, 10000, 2500)
                .add(160, 24);
    }

    private static void demineralizedWater() {
        row("gt.recipe.bath", "demineralized/soften")
                .in(dust("QuartzSand", 1), item("dustTiny", "SodiumCarbonate", 1))
                .out(item("dustSmall", "Calcite", 1), item("dustTiny", "MagnesiumCarbonate", 1))
                .fluidIn("GenLiquid_ClarifiedWater", 4000)
                .fluidOut("GenLiquid_DemineralizedWater", 3800)
                .keep(0)
                .add(120, 16);
        row("gt.recipe.distillery", "demineralized/distill")
                .in(circuit(1))
                .fluidIn("GenLiquid_DemineralizedWater", 1000)
                .fluidOut("DistW", 1000)
                .add(40, 16);
    }

    private static void deionizedWater() {
        row("gt.recipe.bath", "deionized")
                .in(dust("Zeolite", 1), item("dustTiny", "SodiumHydroxide", 1))
                .fluidIn("GenLiquid_DemineralizedWater", 4000)
                .fluidOut("GenLiquid_DeionizedWater", 3800, "Saltwater", 200)
                .keep(0)
                .add(120, 30);
    }

    private static void filteredWater() {
        row("gt.recipe.autoclave", "filtered/ro")
                .in(item("foil", "CelluloseAcetate", 1), circuit(1))
                .out(item("dustTiny", "Salt", 1))
                .fluidIn("GenLiquid_DeionizedWater", 4000)
                .fluidOut("GenLiquid_FilteredWater", 3600)
                .chances(1000)
                .keep(0, 1)
                .add(160, 64);
    }

    private static void ultrapureWater() {
        for (String lens : new String[]{"Sapphire", "Diamond"}) {
            row("gt.recipe.mixer", "ultrapure/" + lens)
                    .in(item("lens", lens, 1))
                    .fluidIn("GenLiquid_FilteredWater", 4000, "GenGas_Ozone", 250)
                    .fluidOut("GenLiquid_UltrapureWater", 4000, "Oxygen", 375)
                    .keep(0)
                    .add(200, 120);
        }
    }

    private static void brineChain() {
        concentrateBrine();
        removeMagnesiumCalcium();
        blowBromine();
        precipitateLithium();
    }

    private static void concentrateBrine() {
        // 1.0.6 retires the host Ocean fluid; its Saltwater field carries the same route.
        row("gt.recipe.distillationtower", "brine/saltwater")
                .out(dust("Salt", 2), dust("Gypsum", 1))
                .fluidIn("Saltwater", 10000)
                .fluidOut("DistW", 7500, "GenLiquid_Brine", 2000)
                .add(200, 48);
        row("gt.recipe.distillery", "brine/saltwater-early")
                .in(circuit(2))
                .out(item("dustSmall", "Gypsum", 1))
                .fluidIn("Saltwater", 2000)
                .fluidOut("DistW", 1400, "GenLiquid_Brine", 400)
                .add(100, 16);
    }

    private static void removeMagnesiumCalcium() {
        row("gt.recipe.bath", "brine/slake-lime")
                .in(dust("Quicklime", 1))
                .out(dust("CalciumHydroxide", 1))
                .fluidIn("Water", 1000)
                .add(40, 8);
        row("gt.recipe.bath", "brine/remove-mg-ca")
                .in(dust("CalciumHydroxide", 1), dust("SodiumCarbonate", 1))
                .out(dust("MagnesiumHydroxide", 1), dust("Calcite", 1))
                .fluidIn("GenLiquid_Brine", 4000)
                .fluidOut("GenLiquid_DecalcifiedBrine", 3600)
                .add(120, 24);
        row("gt.recipe.bath", "brine/magnesium-chloride")
                .in(dust("MagnesiumHydroxide", 1))
                .out(dust("MagnesiumChloride", 1))
                .fluidIn("GenGas_HydrochloricAcid", 2000)
                .fluidOut("DistW", 2000)
                .add(80, 16);
    }

    private static void blowBromine() {
        row("gt.recipe.mixer", "brine/blow-bromine")
                .fluidIn("GenLiquid_DecalcifiedBrine", 4000, "GenGas_Chlorine", 1000, "GenLiquid_SulfuricAcid", 250)
                .fluidOut("GenGas_CrudeBromine", 1000, "GenLiquid_LithiumRichBrine", 3000)
                .add(160, 48);
        row("gt.recipe.distillationtower", "brine/bromine")
                .fluidIn("GenGas_CrudeBromine", 2000)
                .fluidOut(fields("GenGas_Bromine", "GenGas_Iodine", "GenGas_Chlorine", "GenGas_HydrochloricAcid"),
                        amounts(1500, 200, 200, 100))
                .add(160, 64);
        row("gt.recipe.distillery", "brine/bromine-early")
                .in(circuit(1))
                .fluidIn("GenGas_CrudeBromine", 1000)
                .fluidOut("GenGas_Bromine", 700, "GenGas_HydrochloricAcid", 200)
                .add(120, 30);
    }

    private static void precipitateLithium() {
        row("gt.recipe.bath", "brine/lithium-carbonate")
                .in(dust("SodiumCarbonate", 2))
                .out(dust("LithiumCarbonate", 1), dust("Salt", 2))
                .fluidIn("GenLiquid_LithiumRichBrine", 3000)
                .fluidOut("DistW", 2000)
                .add(140, 30);
        row("gt.recipe.bath", "brine/lithium-chloride")
                .in(dust("LithiumCarbonate", 1))
                .out(dust("LithiumChloride", 2))
                .fluidIn("GenGas_HydrochloricAcid", 2000)
                .fluidOut("CarbonDioxide", 1000, "DistW", 1000)
                .add(100, 24);
        // LiCl molten-salt electrolysis already exists in GT6CE; no separate row.
    }

}
