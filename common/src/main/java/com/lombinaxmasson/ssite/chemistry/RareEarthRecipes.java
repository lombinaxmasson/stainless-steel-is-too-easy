package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

/** M6 rare-earth recipes. Cards 2 and 3 append their own methods here. */
public final class RareEarthRecipes {
    private RareEarthRecipes() {}

    static void add() {
        oreLeaching();
        rareEarthExtraction();
        rareEarthMetals();
    }

    /** Three ore routes converge on MixedRareEarthSolution; monazite also yields Th and U. */
    private static void oreLeaching() {
        row("gt.recipe.calciner", "bastnasite/roast")
                .in(dust("Bastnasite", 2))
                .out(dust("BastnasiteCalcine", 2))
                .fluidIn("DistW", 1000, "Oxygen", 500)
                .fluidOut("GenGas_HydrogenFluoride", 2000, "CarbonDioxide", 2000)
                .add(160, 48);

        row("gt.recipe.mixer", "bastnasite/leach")
                .in(dust("BastnasiteCalcine", 2))
                .fluidIn("GenGas_HydrochloricAcid", 6000)
                .fluidOut("GenLiquid_MixedRareEarthSolution", 2000, "Dirty_Water", 3000)
                .add(160, 48);

        row("gt.recipe.calciner", "monazite/digest")
                .in(dust("Monazite", 2))
                .out(dust("ThoriumConcentrate", 1))
                .fluidIn("GenLiquid_SulfuricAcid", 3000)
                .fluidOut("GenLiquid_MixedRareEarthSolution", 2000,
                        "GenLiquid_UranylSulfateSolution", 500, "Dirty_Water", 500)
                .add(200, 64);

        row("gt.recipe.calciner", "xenotime/caustic-fusion")
                .in(dust("Xenotime", 2), dust("SodiumHydroxide", 6))
                .fluidIn("GenGas_HydrochloricAcid", 6000)
                .fluidOut("GenLiquid_MixedRareEarthSolution", 2000, "Dirty_Water", 3000)
                .add(240, 64);

        row("gt.recipe.mixer", "rare-earth/leach")
                .in(dust("RareEarth", 4))
                .fluidIn("GenGas_HydrochloricAcid", 6000)
                .fluidOut("GenLiquid_MixedRareEarthSolution", 1500, "Dirty_Water", 1500)
                .add(160, 48);
    }

    /** P204 splits the mixed liquor into light/middle/heavy groups; Aliquat, chlorine and zinc break the groups apart. */
    private static void rareEarthExtraction() {
        // P204 takes light and middle rare earths into the organic phase; heavy rare earths stay in the liquor.
        // The mixer has two fluid outputs, so this row leaves out the dirty water the plan's sketch listed third.
        row("gt.recipe.mixer", "rare-earth/p204-group")
                .fluidIn("GenLiquid_MixedRareEarthSolution", 4000,
                        "GenLiquid_DiethylhexylPhosphoricAcid", 2000, "GenGas_HydrochloricAcid", 500)
                .fluidOut("GenLiquid_LightMiddleP204Extract", 2000, "GenLiquid_HeavyRareEarthChloride", 2000)
                .add(160, 64);

        row("gt.recipe.mixer", "rare-earth/p204-light-strip")
                .fluidIn("GenLiquid_LightMiddleP204Extract", 2000, "GenGas_HydrochloricAcid", 1500)
                .fluidOut("GenLiquid_LightRareEarthChloride", 2000, "GenLiquid_MiddleRareEarthP204Extract", 2000)
                .add(160, 64);

        row("gt.recipe.mixer", "rare-earth/p204-middle-strip")
                .fluidIn("GenLiquid_MiddleRareEarthP204Extract", 2000, "GenLiquid_SulfuricAcid", 500)
                .fluidOut("GenLiquid_MiddleRareEarthChloride", 2000,
                        "GenLiquid_DiethylhexylPhosphoricAcid", 1900)
                .add(160, 64);

        // Light group: chlorine oxidises Ce3+ to Ce4+ so CeO2 drops out first.
        row("gt.recipe.mixer", "rare-earth/cerium-oxidation")
                .in(dust("SodiumHydroxide", 4))
                .fluidIn("GenLiquid_LightRareEarthChloride", 2000, "GenGas_Chlorine", 1000)
                .out(dust("CeriumOxide", 1))
                .fluidOut("GenLiquid_LightRareEarthRaffinate", 2000, "Dirty_Water", 1000)
                .add(160, 64);

        // Aliquat 336 extracts lanthanum and gives 95% of itself back.
        row("gt.recipe.mixer", "rare-earth/aliquat-lanthanum")
                .fluidIn("GenLiquid_LightRareEarthRaffinate", 2000,
                        "GenLiquid_Aliquat", 1000, "GenGas_HydrochloricAcid", 1000)
                .out(dust("LanthanumOxide", 1))
                .fluidOut("GenLiquid_PrNdRaffinate", 2000, "GenLiquid_Aliquat", 950)
                .add(160, 64);

        row("gt.recipe.mixer", "rare-earth/p204-praseodymium")
                .fluidIn("GenLiquid_PrNdRaffinate", 2000, "GenLiquid_DiethylhexylPhosphoricAcid", 1000)
                .out(dust("NeodymiumOxide", 1))
                .fluidOut("GenLiquid_PrP204Extract", 1000, "Dirty_Water", 1000)
                .add(160, 64);

        row("gt.recipe.mixer", "rare-earth/praseodymium-strip")
                .fluidIn("GenLiquid_PrP204Extract", 1000, "GenLiquid_SulfuricAcid", 500)
                .out(dust("PraseodymiumOxide", 1))
                .fluidOut("GenLiquid_DiethylhexylPhosphoricAcid", 950)
                .add(160, 48);

        // Middle group: zinc powder reduces Eu3+ to Eu2+ so Eu2O3 drops out first.
        row("gt.recipe.mixer", "rare-earth/europium-reduction")
                .in(dust("Zinc", 2))
                .fluidIn("GenLiquid_MiddleRareEarthChloride", 2000, "GenGas_HydrochloricAcid", 500)
                .out(dust("EuropiumOxide", 1))
                .fluidOut("GenLiquid_SmGdChloride", 2000, "Dirty_Water", 1000)
                .add(160, 64);

        row("gt.recipe.mixer", "rare-earth/p204-samarium")
                .fluidIn("GenLiquid_SmGdChloride", 2000, "GenLiquid_DiethylhexylPhosphoricAcid", 1000)
                .out(dust("GadoliniumOxide", 1))
                .fluidOut("GenLiquid_SmP204Extract", 1000, "Dirty_Water", 1000)
                .add(160, 64);

        row("gt.recipe.mixer", "rare-earth/samarium-strip")
                .fluidIn("GenLiquid_SmP204Extract", 1000, "GenLiquid_SulfuricAcid", 500)
                .out(dust("SamariumOxide", 1))
                .fluidOut("GenLiquid_DiethylhexylPhosphoricAcid", 950)
                .add(160, 48);

        // Heavy group: Aliquat takes yttrium, P204 takes Tb/Dy, and the centrifuge splits the loaded phase.
        row("gt.recipe.mixer", "rare-earth/aliquat-yttrium")
                .fluidIn("GenLiquid_HeavyRareEarthChloride", 2000,
                        "GenLiquid_Aliquat", 1000, "GenGas_HydrochloricAcid", 1000)
                .out(dust("YttriumOxide", 1))
                .fluidOut("GenLiquid_TbDyLuChloride", 2000, "GenLiquid_Aliquat", 950)
                .add(160, 64);

        row("gt.recipe.mixer", "rare-earth/p204-lutetium")
                .fluidIn("GenLiquid_TbDyLuChloride", 2000, "GenLiquid_DiethylhexylPhosphoricAcid", 1000)
                .out(dust("LutetiumOxide", 1))
                .fluidOut("GenLiquid_TbDyP204Extract", 1000, "Dirty_Water", 1000)
                .add(160, 64);

        row("gt.recipe.centrifuge", "rare-earth/terbium-dysprosium")
                .fluidIn("GenLiquid_TbDyP204Extract", 1000)
                .out(dust("TerbiumOxide", 1), dust("DysprosiumOxide", 1), dust("HeavyRareEarthResidue", 1))
                .fluidOut("GenLiquid_DiethylhexylPhosphoricAcid", 950, "Dirty_Water", 500)
                .add(160, 64);
    }

    /** X2O3 + 6 HF -> 2 XF3 + 3 H2O, then 2 XF3 + 3 Ca -> 2 X + 3 CaF2. GT6CE spells lanthanum "Lanthanium". */
    private static void rareEarthMetals() {
        String[][] chain = {
                {"LanthanumOxide", "LanthanumFluoride", "Lanthanium"},
                {"CeriumOxide", "CeriumFluoride", "Cerium"},
                {"PraseodymiumOxide", "PraseodymiumFluoride", "Praseodymium"},
                {"NeodymiumOxide", "NeodymiumFluoride", "Neodymium"},
                {"SamariumOxide", "SamariumFluoride", "Samarium"},
                {"EuropiumOxide", "EuropiumFluoride", "Europium"},
                {"GadoliniumOxide", "GadoliniumFluoride", "Gadolinium"},
                {"TerbiumOxide", "TerbiumFluoride", "Terbium"},
                {"DysprosiumOxide", "DysprosiumFluoride", "Dysprosium"},
                {"YttriumOxide", "YttriumFluoride", "Yttrium"},
                {"LutetiumOxide", "LutetiumFluoride", "Lutetium"}
        };
        for (String[] e : chain) {
            if (e[0].equals("CeriumOxide")) continue;
            row("gt.recipe.calciner", "rare-earth/fluoride-" + e[0])
                    .in(dust(e[0], 1))
                    .out(dust(e[1], 2))
                    .fluidIn("GenGas_HydrogenFluoride", 6000)
                    .fluidOut("DistW", 3000)
                    .add(160, 64);
        }

        // CeO2 is tetravalent: 4 CeO2 + 12 HF -> 4 CeF3 + 6 H2O + O2. The basic machine input
        // tank holds 8000 mB per slot, so this row runs half a batch to keep HF at 6000 mB.
        row("gt.recipe.calciner", "rare-earth/fluoride-cerium")
                .in(dust("CeriumOxide", 2))
                .out(dust("CeriumFluoride", 2))
                .fluidIn("GenGas_HydrogenFluoride", 6000)
                .fluidOut("DistW", 3000, "Oxygen", 500)
                .add(100, 64);

        for (String[] e : chain) {
            row("gt.recipe.calciner", "rare-earth/calciothermy-" + e[1])
                    .in(dust(e[1], 2), dust("Calcium", 3))
                    .out(dust(e[2], 2), dust("Fluorite", 3))
                    .add(240, 96);
        }

        row("gt.recipe.calciner", "rare-earth/ndfeb")
                .in(dust("Neodymium", 2), dust("Iron", 14), dust("Boron", 1))
                .out(dust("NeodymiumIronBoron", 17))
                .add(240, 96);

        row("gt.recipe.calciner", "rare-earth/smco")
                .in(dust("Samarium", 1), dust("Cobalt", 5))
                .out(dust("SamariumCobalt", 6))
                .add(240, 96);

        row("gt.recipe.calciner", "rare-earth/ybco")
                .in(dust("Yttrium", 1), dust("Barium", 2), dust("Copper", 3))
                .fluidIn("Oxygen", 7000)
                .out(dust("YttriumBariumCuprate", 6))
                .add(240, 96);

        row("gt.recipe.calciner", "rare-earth/yag")
                .in(dust("YttriumOxide", 3), dust("Alumina", 5))
                .out(dust("YttriumAluminiumGarnet", 2))
                .add(240, 64);
    }
}
