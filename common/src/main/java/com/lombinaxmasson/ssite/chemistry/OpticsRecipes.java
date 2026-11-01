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
 * {@code OpticsMaterials}. Cutting a boule yields both gain rods and lens blanks; the polishing
 * and coating passes are gates on the same item, the same way M9-04 polishes wafers.</p>
 *
 * <p>The parts have no consumer in this mod: M8 disc recording and the P laser workshop are not
 * implemented, and the host laser engraver keeps its lenses and masks as catalysts. They are
 * shipped as a downstream interface, and no solid-state emitter item is registered.</p>
 */
public final class OpticsRecipes {
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
                .out(item("laserRod", "NeodymiumYAG", 4), item("opticalLens", "NeodymiumYAG", 1))
                .add(240, 64);
        for (String abrasive : new String[]{"Alumina", "CeriumOxide"}) {
            String slug = abrasive.toLowerCase(Locale.ROOT);
            polish("m9-10/polish-rod/" + slug, "laserRod", abrasive);
            polish("m9-10/polish-lens/" + slug, "opticalLens", abrasive);
        }
        // Antireflection coating on the rod end faces; silica is an existing host film material.
        row("gt.recipe.calciner", "m9-10/ar-coating")
                .in(item("laserRod", "NeodymiumYAG", 1), item("dustTiny", "SiliconDioxide", 1))
                .out(item("laserRod", "NeodymiumYAG", 1))
                .add(240, 48);
    }

    private static void polish(String label, String prefix, String abrasive) {
        row("gt.recipe.bath", label)
                .in(item(prefix, "NeodymiumYAG", 1), dust(abrasive, 1))
                .fluidIn("GenLiquid_UltrapureWater", 1000)
                .fluidOut("Dirty_Water", 800)
                .out(item(prefix, "NeodymiumYAG", 1))
                .add(120, 32);
    }
}
