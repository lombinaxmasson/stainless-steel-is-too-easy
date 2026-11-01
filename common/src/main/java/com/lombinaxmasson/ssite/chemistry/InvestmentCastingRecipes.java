package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.circuit;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

/**
 * M9-09 investment casting: pattern wax, ceramic shell, dewaxing and pouring.
 *
 * <p>The host has no recipe map that turns a molten material into a custom item, so the pour is a
 * calciner row fed with the same {@code GenMolten_} alloy the AOD and VAR pools pour. Only the
 * alloys named below are accepted; there is no generic "molten metal" row. The in-world faucet
 * into the host ceramic moulds is unchanged.</p>
 *
 * <p>Tooling comes from M9-08: the wax melter keeps an aramid felt filter and the pattern press
 * keeps a carbon-fibre composite die plate. Both are checked but not consumed, which is what the
 * closed M9-08 card asked this card to add.</p>
 */
public final class InvestmentCastingRecipes {
    private InvestmentCastingRecipes() {}

    static void add() {
        patterns();
        shell();
        pouring();
    }

    private static void patterns() {
        row("gt.recipe.mixer", "m9-09/pattern-wax")
                .in(dust("HydrogenatedPetroleumResin", 1), dust("Wax", 1),
                        item("plate", "AramidFelt", 1))
                .keep(2)
                .out(item("plate", "PatternWax", 2))
                .add(160, 32);
        row("gt.recipe.press", "m9-09/wax-pattern")
                .in(item("plate", "PatternWax", 2), item("plate", "CarbonFiberComposite", 1),
                        circuit(1))
                .keep(1, 2)
                .out(item("plate", "WaxPattern", 1))
                .add(80, 16);
    }

    private static void shell() {
        row("gt.recipe.mixer", "m9-09/shell-slurry")
                .in(dust("Zircon", 1), dust("Alumina", 1), dust("SiliconDioxide", 1))
                .fluidIn("DistW", 1000)
                .fluidOut("GenLiquid_CeramicShellSlurry", 1000)
                .add(200, 32);
        // One row stands for the three to five dip-and-stucco layers the card allows.
        row("gt.recipe.drying", "m9-09/shell-build")
                .in(item("plate", "WaxPattern", 4))
                .fluidIn("GenLiquid_CeramicShellSlurry", 2000)
                .out(item("plate", "CeramicShell", 1))
                .add(400, 32);
        // Dewax and burnout are one calciner pass; oxygen burns off the residual pattern carbon.
        row("gt.recipe.calciner", "m9-09/dewax-burnout")
                .in(item("plate", "CeramicShell", 1))
                .fluidIn("Oxygen", 1000)
                .out(item("plate", "FiredCeramicShell", 1), item("plate", "PatternWax", 1))
                .chances(10000, 9000)
                .add(900, 96);
    }

    private static void pouring() {
        // Stainless Steel is the bootstrap melt: the AOD exists before the VAR this casting unlocks.
        pour("StainlessSteel", "stainless-steel");
        pour("NiobiumTitanium", "niobium-titanium");
        pour("TantalumHafniumCarbide", "ta4hfc5");
        pour("Tungstensteel", "tungsten-steel");
        pour("Rhenium", "rhenium");
        row("gt.recipe.cutter", "m9-09/trim")
                .in(item("plate", "AsCastPart", 1))
                .fluidIn("DistW", 1000)
                .out(item("plate", "PrecisionCasting", 1), dust("Zircon", 1), dust("Alumina", 1))
                .chances(10000, 2500, 2500)
                .add(200, 48);
    }

    /** 216 mB is 1.5 ingots; the gating and shell loss account for the missing half. */
    private static void pour(String alloy, String slug) {
        row("gt.recipe.calciner", "m9-09/pour/" + slug)
                .in(item("plate", "FiredCeramicShell", 1))
                .fluidIn("GenMolten_" + alloy, 216)
                .out(item("plate", "AsCastPart", 1))
                .add(400, 128);
    }
}
