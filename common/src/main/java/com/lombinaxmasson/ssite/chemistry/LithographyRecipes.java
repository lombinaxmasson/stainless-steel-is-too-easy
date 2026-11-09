package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.circuit;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.vanillaItem;

/**
 * M9-05 photolithography and wafer processing.
 *
 * <p>The host has no lithography step, so a polished {@code wafer Silicon} goes through cleaning,
 * oxidation, coating, exposure, development, etching, resist stripping, doping and metallization
 * on existing machines and comes out as {@code wafer ProcessedWafer}.</p>
 *
 * <p>Every stage has its own item id. The final metallisation consumes {@code DopedWafer}, so a
 * raw or polished silicon wafer cannot skip cleaning, exposure, etching or doping. The dry and wet
 * oxidation branches converge on {@code OxidizedWafer}; the two etch and the two doping branches
 * converge the same way. Programming circuits are not used as stage state.</p>
 *
 * <p>The host {@code Bath} exposes a single fluid input tank, so the wet stages name their active
 * medium and leave the ultrapure-water carrier implicit; M9-04 already consumes ultrapure water
 * for slicing and polishing. The host unifies {@code plate Glass} to the vanilla glass pane, so
 * the mask blank is entered as that pane directly.</p>
 */
public final class LithographyRecipes {
    private static final String WAFER = "Silicon";
    private static final String ALKALINE_CLEANED = "AlkalineCleanedWafer";
    private static final String CLEANED = "CleanedWafer";
    private static final String OXIDIZED = "OxidizedWafer";
    private static final String COATED = "CoatedWafer";
    private static final String EXPOSED = "ExposedWafer";
    private static final String DEVELOPED = "DevelopedWafer";
    private static final String ETCHED = "EtchedWafer";
    private static final String STRIPPED = "StrippedWafer";
    private static final String DOPED = "DopedWafer";
    private static final String PROCESSED = "ProcessedWafer";

    private LithographyRecipes() {}

    static void add() {
        chemistries();
        clean();
        oxidize();
        coat();
        expose();
        develop();
        etch();
        strip();
        dope();
        metalize();
    }

    /** Process fluids and the mask blank. These feed the wafer stages below. */
    private static void chemistries() {
        // The cresol-novolac resin and its solvent are the only inputs the host can express; the
        // photoactive compound is folded into the blend.
        row("gt.recipe.mixer", "m9-05/photoresist")
                .fluidIn("GenLiquid_PhenolicResin", 1000, "GenLiquid_Acetone", 1000)
                .fluidOut("GenLiquid_Photoresist", 1000)
                .add(240, 32);
        // Dilute alkaline developer; TMAH has no host route, so the card's fallback is used.
        row("gt.recipe.mixer", "m9-05/developer")
                .fluidIn("GenGas_Ammonia", 1000, "GenLiquid_UltrapureWater", 1000)
                .fluidOut("GenLiquid_PhotoresistDeveloper", 1000)
                .add(120, 16);
        row("gt.recipe.mixer", "m9-05/buffered-hf")
                .in(dust("SodiumFluoride", 1))
                .fluidIn("GenGas_HydrogenFluoride", 1000, "GenLiquid_UltrapureWater", 1000)
                .fluidOut("GenLiquid_BufferedHydrofluoricAcid", 1000)
                .add(120, 16);
        row("gt.recipe.mixer", "m9-05/koh-solution")
                .in(dust("PotassiumHydroxide", 1))
                .fluidIn("GenLiquid_UltrapureWater", 1000)
                .fluidOut("GenLiquid_PotassiumHydroxideSolution", 1000)
                .add(80, 16);
        row("gt.recipe.calciner", "m9-05/boron-trioxide")
                .in(dust("HydrogenBorate", 2))
                .fluidIn("Nitrogen", 1000)
                .out(dust("BoronTrioxide", 1))
                .fluidOut("Steam", 3000)
                .add(240, 32);
        // Chrome-coated glass carries the pattern; the kept circuit is the design data.
        row("gt.recipe.calciner", "m9-05/photomask")
                .in(vanillaItem("minecraft:glass_pane", 1), dust("Chromium", 1), circuit(1))
                .fluidIn("GenLiquid_Photoresist", 1000)
                .out(item("plate", "PhotomaskPlate", 1))
                .keep(2)
                .add(1200, 96);
    }

    /** RCA-style clean: alkaline bath, then acid bath. The water carrier is implicit. */
    private static void clean() {
        row("gt.recipe.bath", "m9-05/clean/sc1")
                .in(item("wafer", WAFER, 1))
                .fluidIn("GenGas_Ammonia", 1000)
                .out(item("wafer", ALKALINE_CLEANED, 1))
                .fluidOut("GenLiquid_DirtyWater", 1000)
                .add(200, 32);
        row("gt.recipe.bath", "m9-05/clean/sc2")
                .in(item("wafer", ALKALINE_CLEANED, 1))
                .fluidIn("GenGas_HydrochloricAcid", 1000)
                .out(item("wafer", CLEANED, 1))
                .fluidOut("GenLiquid_DirtyWater", 1000)
                .add(200, 32);
    }

    private static void oxidize() {
        // Both rows start from the cleaned wafer and converge on one oxidized state.
        row("gt.recipe.calciner", "m9-05/oxidize/dry")
                .in(item("wafer", CLEANED, 1))
                .fluidIn("Oxygen", 1000, "Nitrogen", 1000)
                .out(item("wafer", OXIDIZED, 1))
                .add(600, 64);
        row("gt.recipe.calciner", "m9-05/oxidize/wet")
                .in(item("wafer", CLEANED, 1))
                .fluidIn("Oxygen", 1000, "Steam", 1000)
                .out(item("wafer", OXIDIZED, 1))
                .add(400, 64);
    }

    private static void coat() {
        row("gt.recipe.bath", "m9-05/coat")
                .in(item("wafer", OXIDIZED, 1))
                .fluidIn("GenLiquid_Photoresist", 1000)
                .out(item("wafer", COATED, 1))
                .add(200, 32);
    }

    /** The laser engraver has exactly two item slots, so the mask occupies the kept second slot. */
    private static void expose() {
        row("gt.recipe.laserengraver", "m9-05/expose")
                .in(item("wafer", COATED, 1), item("plate", "PhotomaskPlate", 1))
                .out(item("wafer", EXPOSED, 1))
                .keep(1)
                .add(600, 256);
    }

    private static void develop() {
        row("gt.recipe.bath", "m9-05/develop")
                .in(item("wafer", EXPOSED, 1))
                .fluidIn("GenLiquid_PhotoresistDeveloper", 1000)
                .out(item("wafer", DEVELOPED, 1))
                .fluidOut("GenLiquid_DirtyWater", 1000)
                .add(200, 32);
    }

    private static void etch() {
        row("gt.recipe.bath", "m9-05/etch/oxide")
                .in(item("wafer", DEVELOPED, 1))
                .fluidIn("GenLiquid_BufferedHydrofluoricAcid", 1000)
                .out(item("wafer", ETCHED, 1))
                .fluidOut("GenLiquid_DirtyWater", 1000)
                .add(300, 48);
        row("gt.recipe.bath", "m9-05/etch/silicon")
                .in(item("wafer", DEVELOPED, 1))
                .fluidIn("GenLiquid_PotassiumHydroxideSolution", 1000)
                .out(item("wafer", ETCHED, 1))
                .fluidOut("GenLiquid_DirtyWater", 1000)
                .add(600, 48);
    }

    private static void strip() {
        row("gt.recipe.bath", "m9-05/strip")
                .in(item("wafer", ETCHED, 1))
                .fluidIn("GenLiquid_SulfuricAcid", 1000)
                .out(item("wafer", STRIPPED, 1))
                .fluidOut("GenLiquid_DirtyWater", 1000)
                .add(300, 48);
    }

    private static void dope() {
        row("gt.recipe.calciner", "m9-05/dope/phosphorus")
                .in(item("wafer", STRIPPED, 1))
                .fluidIn("GenLiquid_PhosphorylChloride", 1000, "Oxygen", 1000)
                .out(item("wafer", DOPED, 1))
                .add(1200, 96);
        row("gt.recipe.calciner", "m9-05/dope/boron")
                .in(item("wafer", STRIPPED, 1), dust("BoronTrioxide", 1))
                .fluidIn("Nitrogen", 1000)
                .out(item("wafer", DOPED, 1))
                .add(1200, 96);
    }

    /** Vacuum aluminium evaporation is abstracted as one calciner row. */
    private static void metalize() {
        row("gt.recipe.calciner", "m9-05/metalize")
                .in(item("wafer", DOPED, 1), dust("Aluminium", 1))
                .fluidIn("GenGas_Argon", 1000)
                .out(item("wafer", PROCESSED, 1))
                .add(900, 96);
    }
}
