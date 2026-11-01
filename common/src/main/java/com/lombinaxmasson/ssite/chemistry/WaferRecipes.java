package com.lombinaxmasson.ssite.chemistry;

import java.util.Locale;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

/**
 * M9-04 silicon purification, crystal growth, wafer slicing and polishing.
 *
 * <p>The host's crystallisation rows grow a boule from any silicon dust; that lets metallurgical
 * silicon skip purification. {@code data/ssite/recipes/remove_crystallisation_crucible_silicon.json}
 * drops those rows, and silicon boules now start from {@code ElectronicGradeSilicon}.</p>
 *
 * <p>Compounds cannot use the host {@code bouleGt}/{@code plateGem} forms, so they use the addon
 * {@code crystalBoule} and {@code wafer} prefixes. Silicon wafers use the same {@code wafer} prefix
 * while its boule stays the host {@code bouleGt Silicon}.</p>
 */
public final class WaferRecipes {
    private WaferRecipes() {}

    static void add() {
        siliconPurification();
        siliconCrystal();
        compoundSemiconductors();
    }

    private static void siliconPurification() {
        row("gt.recipe.mixer", "m9-04/trichlorosilane-synthesis")
                .in(dust("Silicon", 1))
                .fluidIn("GenGas_HydrochloricAcid", 3000)
                .fluidOut("GenLiquid_Trichlorosilane", 1000, "GenGas_Hydrogen", 2000)
                .add(200, 64);
        // The tower cuts the electronic-grade fraction and sends the SiCl4 heavy ends to recycle.
        row("gt.recipe.distillationtower", "m9-04/trichlorosilane-purify")
                .fluidIn("GenLiquid_Trichlorosilane", 1000)
                .fluidOut("GenLiquid_Trichlorosilane", 800, "GenLiquid_SiliconTetrachloride", 200)
                .add(120, 48);
        row("gt.recipe.calciner", "m9-04/siemens-reduction")
                .fluidIn("GenLiquid_Trichlorosilane", 1000, "GenGas_Hydrogen", 2000)
                .out(dust("ElectronicGradeSilicon", 1))
                .fluidOut("GenGas_HydrochloricAcid", 3000)
                .add(400, 96);
        row("gt.recipe.calciner", "m9-04/trichlorosilane-recycle")
                .fluidIn("GenLiquid_SiliconTetrachloride", 1000, "GenGas_Hydrogen", 1000)
                .fluidOut("GenLiquid_Trichlorosilane", 1000, "GenGas_HydrochloricAcid", 1000)
                .add(240, 64);
    }

    private static void siliconCrystal() {
        row("gt.recipe.crystallisationcrucible", "m9-04/boule/silicon")
                .in(dust("ElectronicGradeSilicon", 1))
                .fluidIn("GenGas_Argon", 1000)
                .out(item("bouleGt", "Silicon", 1))
                .add(72000, 16);
        slice("Silicon", item("bouleGt", "Silicon", 1));
        polish("Silicon");
    }

    private static void compoundSemiconductors() {
        row("gt.recipe.autoclave", "m9-04/gallium-arsenide")
                .in(dust("Gallium", 1), dust("Arsenic", 1))
                .fluidIn("GenGas_Argon", 1000)
                .out(dust("GalliumArsenide", 1))
                .add(300, 64);
        row("gt.recipe.autoclave", "m9-04/indium-phosphide")
                .in(dust("Indium", 1), dust("Phosphorus", 1))
                .fluidIn("GenGas_Argon", 1000)
                .out(dust("IndiumPhosphide", 1))
                .add(300, 64);
        for (String compound : new String[]{"GalliumArsenide", "IndiumPhosphide"}) {
            row("gt.recipe.crystallisationcrucible", "m9-04/boule/" + compound.toLowerCase(Locale.ROOT))
                    .in(dust(compound, 1))
                    .fluidIn("GenGas_Argon", 1000)
                    .out(item("crystalBoule", compound, 1))
                    .add(48000, 64);
            slice(compound, item("crystalBoule", compound, 1));
            polish(compound);
        }
    }

    private static void slice(String material, RecipeDeclaration.ItemRef boule) {
        // Ultrapure water separates this row from the host cutter rows, which only bind the four source coolants.
        row("gt.recipe.cutter", "m9-04/wafer/" + material.toLowerCase(Locale.ROOT))
                .in(boule)
                .fluidIn("GenLiquid_UltrapureWater", 1000)
                .out(item("wafer", material, 8))
                .add(240, 64);
    }

    private static void polish(String material) {
        for (String abrasive : new String[]{"Alumina", "CeriumOxide"}) {
            row("gt.recipe.bath", "m9-04/polish/" + abrasive.toLowerCase(Locale.ROOT) + "/"
                    + material.toLowerCase(Locale.ROOT))
                    .in(item("wafer", material, 1), dust(abrasive, 1))
                    .fluidIn("GenLiquid_UltrapureWater", 1000)
                    .fluidOut("Dirty_Water", 800)
                    .out(item("wafer", material, 1))
                    .add(120, 32);
        }
    }
}
