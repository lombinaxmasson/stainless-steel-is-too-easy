package com.lombinaxmasson.ssite.chemistry;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.circuit;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.vanillaItem;

import com.lombinaxmasson.ssite.machine.RecordMachines;
import com.lombinaxmasson.ssite.media.RecordCatalog;

/**
 * Record media manufacturing. Paper and copper foil make an empty, reusable track template.
 * Dynamic recipes preserve its imported track through mastering, electroforming and pressing,
 * or through recording a layered CD-R blank.
 *
 * <p>Numbers are first-pass game values. They follow the material ledgers of the existing polymer
 * and base-metal cards, not real pressing ratios.</p>
 */
public final class RecordRecipes {
    private RecordRecipes() {}

    static void add() {
        template();
        vinylFeedstock();
        vinylMastering();
        vinylPressing();
        compactDisc();
    }

    private static void template() {
        // The selector chooses the blank template recipe; the imported track is bound separately.
        row("gt.recipe.press", "m8-00/track-template")
                .in(vanillaItem("minecraft:paper", 1), item("foil", "Copper", 1), circuit(1))
                .keep(2)
                .out(item(RecordCatalog.TEMPLATE_PREFIX, RecordCatalog.TEMPLATE_MATERIAL, 1))
                .add(100, 16);
    }

    private static void vinylFeedstock() {
        row("gt.recipe.mixer", "m8-01/vinyl-compound")
                .in(dust("PVC", 4), dust("PolyvinylAcetate", 1),
                        item("dustSmall", "CarbonBlack", 1), item("dustTiny", "CalciumStearate", 1))
                .out(dust("Vinyl", 2))
                .add(200, 32);
        row("gt.recipe.press", "m8-01/blank-vinyl")
                .in(dust("Vinyl", 2), circuit(1))
                .keep(1)
                .out(item("recordBlank", "Vinyl", 1))
                .add(120, 16);
        // Minimal heat-stabiliser chain: oxidised wax gives the fatty acid, lime neutralises it.
        row("gt.recipe.mixer", "m8-01/stearic-acid")
                .in(dust("Wax", 1))
                .fluidIn("Oxygen", 2000)
                .out(dust("StearicAcid", 1))
                .fluidOut("DistW", 1000)
                .add(200, 32);
        row("gt.recipe.mixer", "m8-01/calcium-stearate")
                .in(dust("StearicAcid", 2), dust("CalciumHydroxide", 1))
                .out(dust("CalciumStearate", 1))
                .fluidOut("DistW", 1000)
                .add(160, 24);
    }

    private static void vinylMastering() {
        row("gt.recipe.mixer", "m8-01/nitrocellulose")
                .in(vanillaItem("minecraft:paper", 1))
                .fluidIn("GenLiquid_NitricAcid", 1000, "GenLiquid_SulfuricAcid", 1000)
                .out(dust("Nitrocellulose", 1))
                .fluidOut("DistW", 1000)
                .add(240, 32);
        row("gt.recipe.bath", "m8-01/lacquer-master")
                .in(item("plate", "Aluminium", 1), dust("Nitrocellulose", 1))
                .fluidIn("GenLiquid_Acetone", 500)
                .fluidOut("GenLiquid_Acetone", 400)
                .out(item("recordLacquer", "Aluminium", 1))
                .add(200, 24);
        // The executable row copies the bound track; this row documents the material ledger.
        row(RecordMachines.MAP, "m8-01/cut-master")
                .in(item("recordLacquer", "Aluminium", 1),
                        item(RecordCatalog.TEMPLATE_PREFIX, RecordCatalog.TEMPLATE_MATERIAL, 1),
                        item(RecordCatalog.STYLUS_PREFIX, RecordCatalog.STYLUS_MATERIAL, 1))
                .keep(1, 2)
                .out(item("recordCutMaster", "Aluminium", 1))
                .display(300, 128);
        // Silver sensitising and nickel electroforming share the electrolyser's two fluid tanks.
        row("gt.recipe.electrolyzer", "m8-01/nickel-stamper")
                .in(item("recordCutMaster", "Aluminium", 1), item("plate", "Nickel", 1))
                .fluidIn("GenLiquid_SilverNitrateSolution", 100,
                        "GenLiquid_NickelSulfateSolution", 1000)
                .out(item("recordStamper", "Nickel", 1))
                .display(400, 96);
    }

    private static void vinylPressing() {
        // One stamper presses any number of blanks; only the blank and the paper label are consumed.
        row("gt.recipe.press", "m8-01/press-vinyl")
                .in(item("recordBlank", "Vinyl", 1), vanillaItem("minecraft:paper", 1),
                        item("recordStamper", "Nickel", 1))
                .keep(2)
                .out(vanillaItem("ssite:" + RecordCatalog.VINYL_ITEM, 1))
                .display(160, 32);
    }

    private static void compactDisc() {
        row("gt.recipe.press", "m8-02/disc-base")
                .in(dust("Polycarbonate", 2), circuit(1))
                .keep(1)
                .out(item("discBase", "Polycarbonate", 1))
                .add(100, 24);
        row("gt.recipe.mixer", "m8-02/phthalocyanine-dye")
                .in(dust("PhthalicAnhydride", 4), dust("Urea", 4), dust("CopperSulfate", 1))
                .out(dust("PhthalocyanineDye", 2))
                .add(300, 48);
        row("gt.recipe.bath", "m8-02/dye-coat")
                .in(item("discBase", "Polycarbonate", 1), dust("PhthalocyanineDye", 1))
                .fluidIn("GenLiquid_Acetone", 500)
                .fluidOut("GenLiquid_Acetone", 400)
                .out(item("discCoated", "DyeCoatedDisc", 1))
                .add(200, 24);
        row("gt.recipe.press", "m8-02/silver-reflector")
                .in(item("discCoated", "DyeCoatedDisc", 1), item("foil", "Silver", 1))
                .out(item("discReflective", "ReflectiveDisc", 1))
                .add(120, 24);
        row("gt.recipe.press", "m8-02/gold-reflector")
                .in(item("discCoated", "DyeCoatedDisc", 1), item("foil", "Gold", 1))
                .out(item("discReflective", "ReflectiveDisc", 1))
                .add(120, 24);
        row("gt.recipe.bath", "m8-02/protective-lacquer")
                .in(item("discReflective", "ReflectiveDisc", 1), dust("PolymethylMethacrylate", 1))
                .fluidIn("GenLiquid_Acetone", 500)
                .fluidOut("GenLiquid_Acetone", 400)
                .out(item("discBlank", "CDR", 1))
                .add(200, 24);
        // A bare disc base cannot match here; the row names the blank CD-R explicitly.
        row("gt.recipe.laserengraver", "m8-02/record-cdr")
                .in(item("discBlank", "CDR", 1),
                        item(RecordCatalog.TEMPLATE_PREFIX, RecordCatalog.TEMPLATE_MATERIAL, 1))
                .keep(1)
                .out(vanillaItem("ssite:" + RecordCatalog.CD_ITEM, 1))
                .display(200, 64);
        // Dye precursors: urea from ammonia and CO2, copper sulfate from copper and acid.
        row("gt.recipe.mixer", "m8-02/urea")
                .fluidIn("GenGas_Ammonia", 2000, "CarbonDioxide", 1000)
                .out(dust("Urea", 1))
                .fluidOut("DistW", 1000)
                .add(200, 32);
        row("gt.recipe.mixer", "m8-02/copper-sulfate")
                .in(dust("Copper", 1))
                .fluidIn("GenLiquid_SulfuricAcid", 1000, "Oxygen", 1000)
                .out(dust("CopperSulfate", 1))
                .fluidOut("DistW", 1000)
                .add(200, 32);
    }
}
