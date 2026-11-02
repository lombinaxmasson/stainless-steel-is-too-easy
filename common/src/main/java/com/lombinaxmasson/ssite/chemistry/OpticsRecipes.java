package com.lombinaxmasson.ssite.chemistry;

import java.util.Locale;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

/**
 * M9-10 Nd:YAG doping, crystal growth and optical forming.
 *
 * <p>The card expected host {@code bouleGt} and {@code rod}/{@code lens}; addon materials cannot
 * carry the BOULE, GEM or GENERATE_LENS properties those prefixes require, so the doped crystal
 * uses the existing {@code crystalBoule} prefix and the rod and lens use the addon prefixes in
 * {@code OpticsMaterials}. Cutting a boule yields rough rod and lens blanks; polishing produces
 * the polished states, and coating produces the final rod and lens ids. The stages are distinct
 * items rather than a gate on the same item.</p>
 *
 * <p>The parts have no consumer in this mod: M8 disc recording and the P laser workshop are not
 * implemented, and the host laser engraver keeps its lenses and masks as catalysts. They are
 * shipped as a downstream interface, and no solid-state emitter item is registered.</p>
 */
public final class OpticsRecipes {
    private static final String ROD = "laserRod";
    private static final String ROD_BLANK = "laserRodBlank";
    private static final String ROD_POLISHED = "laserRodPolished";
    private static final String LENS = "opticalLens";
    private static final String LENS_BLANK = "opticalLensBlank";
    private static final String LENS_POLISHED = "opticalLensPolished";

    private OpticsRecipes() {}

    static void add() {
        doping();
        crystal();
        forming();
    }

    private static void doping() {
        // One Nd2O3 supplies two Nd for 45 Y: close to the 1 at% doping the card asks for.
        row("gt.recipe.mixer", "m9-10/nd-yag-doping")
                .in(dust("YttriumAluminiumGarnet", 63), dust("NeodymiumOxide", 1))
                .out(dust("NeodymiumYAG", 64))
                .add(200, 64);
    }

    private static void crystal() {
        row("gt.recipe.crystallisationcrucible", "m9-10/boule/neodymium-yag")
                .in(dust("NeodymiumYAG", 1))
                .fluidIn("GenGas_Argon", 1000)
                .out(item("crystalBoule", "NeodymiumYAG", 1))
                .add(48000, 64);
    }

    private static void forming() {
        // The cutter has one item input, so the rod and the lens blank come from the same row.
        row("gt.recipe.cutter", "m9-10/cut-neodymium-yag")
                .in(item("crystalBoule", "NeodymiumYAG", 1))
                .fluidIn("GenLiquid_UltrapureWater", 1000)
                .out(item(ROD_BLANK, "NeodymiumYAG", 4), item(LENS_BLANK, "NeodymiumYAG", 1))
                .add(240, 64);
        for (String abrasive : new String[]{"Alumina", "CeriumOxide"}) {
            String slug = abrasive.toLowerCase(Locale.ROOT);
            polish("m9-10/polish-rod/" + slug, ROD_BLANK, ROD_POLISHED, abrasive);
            polish("m9-10/polish-lens/" + slug, LENS_BLANK, LENS_POLISHED, abrasive);
        }
        // Antireflection coating turns the polished parts into the final optical ids.
        row("gt.recipe.calciner", "m9-10/ar-coating/rod")
                .in(item(ROD_POLISHED, "NeodymiumYAG", 1), item("dustTiny", "SiliconDioxide", 1))
                .out(item(ROD, "NeodymiumYAG", 1))
                .add(240, 48);
        row("gt.recipe.calciner", "m9-10/ar-coating/lens")
                .in(item(LENS_POLISHED, "NeodymiumYAG", 1), item("dustTiny", "SiliconDioxide", 1))
                .out(item(LENS, "NeodymiumYAG", 1))
                .add(240, 48);
    }

    private static void polish(String label, String input, String output, String abrasive) {
        row("gt.recipe.bath", label)
                .in(item(input, "NeodymiumYAG", 1), dust(abrasive, 1))
                .fluidIn("GenLiquid_UltrapureWater", 1000)
                .fluidOut("Dirty_Water", 800)
                .out(item(output, "NeodymiumYAG", 1))
                .add(120, 32);
    }
}
