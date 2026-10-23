package com.lombinaxmasson.ssite.chemistry;

/** M4 polymer recipes. Each M4 card adds one method; {@link #add()} wires them all. */
public final class PolymerRecipes {
    /*
     * Conventions shared by every M4 card:
     * 1. Programmed circuits go in keptInputs and are never consumed.
     * 2. When one machine hosts a recipe whose inputs are a subset of another, tell them apart by a different circuit number.
     * 3. Scale follows M3: roughly 1000 mB of monomer yields one dust of polymer.
     */
    private PolymerRecipes() {}

    static void add() {
        cOneCTwoPlatform();
        cThreePlatform();
    }

    // ── Card 1: C1/C2 platform chemicals (methanol, formaldehyde, acetic acid, ethylene oxide/glycol, vinyl chloride/acetate) ──

    private static void cOneCTwoPlatform() {
        methaneSteamReforming();
        methanol();
        formaldehyde();
        aceticAcid();
        ethyleneOxide();
        ethyleneGlycol();
        dichloroethane();
        vinylChloride();
        vinylAcetate();
    }

    private static void methaneSteamReforming() {
        ChemistryRecipes.recipe("gt.recipe.catalyticcracking", "c1/methane-reforming",
                ChemistryRecipes.items(ChemistryRecipes.dust("Nickel", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("Methane", "Steam"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenGas_CarbonMonoxide", "Hydrogen"), ChemistryRecipes.amounts(1000, 3000),
                200, 64, null, new int[]{0});
    }

    private static void methanol() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c1/methanol",
                ChemistryRecipes.items(ChemistryRecipes.dust("Copper", 1), ChemistryRecipes.dust("Zinc", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_CarbonMonoxide", "Hydrogen"), ChemistryRecipes.amounts(1000, 2000),
                ChemistryRecipes.fields("GenLiquid_Methanol"), ChemistryRecipes.amounts(1000),
                200, 64, null, new int[]{0, 1});
    }

    private static void formaldehyde() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c1/formaldehyde",
                ChemistryRecipes.items(ChemistryRecipes.dust("Silver", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Methanol", "Oxygen"), ChemistryRecipes.amounts(1000, 500),
                ChemistryRecipes.fields("GenGas_Formaldehyde", "DistW"), ChemistryRecipes.amounts(1000, 1000),
                160, 48, null, new int[]{0});
    }

    private static void aceticAcid() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c1/acetic-acid-monsanto",
                ChemistryRecipes.items(ChemistryRecipes.dust("Rhodium", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Methanol", "GenGas_CarbonMonoxide", "GenGas_Iodine"),
                ChemistryRecipes.amounts(1000, 1000, 10),
                ChemistryRecipes.fields("GenLiquid_AceticAcid"), ChemistryRecipes.amounts(1000),
                160, 64, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c1/acetic-acid-acetaldehyde",
                ChemistryRecipes.items(ChemistryRecipes.dust("Manganese", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Acetaldehyde", "Oxygen"), ChemistryRecipes.amounts(1000, 500),
                ChemistryRecipes.fields("GenLiquid_AceticAcid"), ChemistryRecipes.amounts(1000),
                160, 32, null, new int[]{0});
        for (String vinegar : new String[]{"Vinegar_Grape", "Vinegar_Apple", "Vinegar_Cane", "Vinegar_Rice"}) {
            ChemistryRecipes.recipe("gt.recipe.distillery", "c1/acetic-acid-" + vinegar.toLowerCase(),
                    ChemistryRecipes.items(ChemistryRecipes.circuit(3)),
                    ChemistryRecipes.items(),
                    ChemistryRecipes.fields(vinegar), ChemistryRecipes.amounts(1000),
                    ChemistryRecipes.fields("GenLiquid_AceticAcid", "DistW"), ChemistryRecipes.amounts(50, 950),
                    100, 16, null, new int[]{0});
        }
    }

    private static void ethyleneOxide() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c1/ethylene-oxide",
                ChemistryRecipes.items(ChemistryRecipes.dust("Silver", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("Ethylene", "Oxygen"), ChemistryRecipes.amounts(1000, 500),
                ChemistryRecipes.fields("GenGas_EthyleneOxide"), ChemistryRecipes.amounts(1000),
                160, 48, null, new int[]{0});
    }

    private static void ethyleneGlycol() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c1/ethylene-glycol",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_EthyleneOxide", "DistW"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenLiquid_EthyleneGlycol"), ChemistryRecipes.amounts(1000),
                120, 32);
    }

    private static void dichloroethane() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c1/dichloroethane-direct",
                ChemistryRecipes.items(ChemistryRecipes.dust("FerricChloride", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("Ethylene", "GenGas_Chlorine"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenLiquid_Dichloroethane"), ChemistryRecipes.amounts(1000),
                120, 32, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c1/dichloroethane-oxy",
                ChemistryRecipes.items(ChemistryRecipes.dust("Copper", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("Ethylene", "GenGas_HydrochloricAcid", "Oxygen"),
                ChemistryRecipes.amounts(1000, 2000, 500),
                ChemistryRecipes.fields("GenLiquid_Dichloroethane", "DistW"), ChemistryRecipes.amounts(1000, 1000),
                160, 48, null, new int[]{0});
    }

    private static void vinylChloride() {
        ChemistryRecipes.recipe("gt.recipe.calciner", "c1/vinyl-chloride",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Dichloroethane"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenGas_VinylChloride", "GenGas_HydrochloricAcid"),
                ChemistryRecipes.amounts(1000, 1000),
                160, 48);
    }

    private static void vinylAcetate() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c1/vinyl-acetate",
                ChemistryRecipes.items(ChemistryRecipes.dust("Palladium", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("Ethylene", "GenLiquid_AceticAcid", "Oxygen"),
                ChemistryRecipes.amounts(1000, 1000, 500),
                ChemistryRecipes.fields("GenLiquid_VinylAcetate", "DistW"), ChemistryRecipes.amounts(1000, 1000),
                200, 64, null, new int[]{0});
    }

    // ── Card 2: C3 platform chemicals (epichlorohydrin, acrylonitrile, acrylic acid, MMA, oxo alcohols, Prins isoprene) ──

    private static void cThreePlatform() {
        epichlorohydrin();
        hydrogenCyanideAcrylonitrile();
        acroleinAcrylicAcid();
        methylMethacrylate();
        oxoAlcohols();
        prinsIsoprene();
    }

    private static void epichlorohydrin() {
        ChemistryRecipes.recipe("gt.recipe.calciner", "c3/allyl-chloride",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("Propylene", "GenGas_Chlorine"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenLiquid_AllylChloride", "GenGas_HydrochloricAcid"),
                ChemistryRecipes.amounts(1000, 1000),
                160, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c3/dichloropropanol-hypo",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_AllylChloride", "GenGas_Chlorine", "Water"),
                ChemistryRecipes.amounts(1000, 1000, 1000),
                ChemistryRecipes.fields("GenLiquid_Dichloropropanol", "GenGas_HydrochloricAcid"),
                ChemistryRecipes.amounts(1000, 1000),
                120, 32);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c3/dichloropropanol-glycerol",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Glycerol", "GenGas_HydrochloricAcid", "GenLiquid_AceticAcid"),
                ChemistryRecipes.amounts(1000, 2000, 50),
                ChemistryRecipes.fields("GenLiquid_Dichloropropanol", "DistW"), ChemistryRecipes.amounts(1000, 2000),
                160, 32);
        ChemistryRecipes.recipe("gt.recipe.bath", "c3/epichlorohydrin",
                ChemistryRecipes.items(ChemistryRecipes.dust("SodiumHydroxide", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Salt", 1)),
                ChemistryRecipes.fields("GenLiquid_Dichloropropanol"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_Epichlorohydrin", "DistW"), ChemistryRecipes.amounts(1000, 1000),
                120, 32);
    }

    private static void hydrogenCyanideAcrylonitrile() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c3/hydrogen-cyanide",
                ChemistryRecipes.items(ChemistryRecipes.dust("Platinum", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("Methane", "GenGas_Ammonia", "Oxygen"),
                ChemistryRecipes.amounts(1000, 1000, 1500),
                ChemistryRecipes.fields("GenLiquid_HydrogenCyanide", "DistW"), ChemistryRecipes.amounts(1000, 3000),
                160, 64, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c3/acrylonitrile",
                ChemistryRecipes.items(ChemistryRecipes.dust("Bismuth", 1), ChemistryRecipes.dust("Molybdenum", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("Propylene", "GenGas_Ammonia", "Oxygen"),
                ChemistryRecipes.amounts(1000, 1000, 1500),
                ChemistryRecipes.fields("GenLiquid_Acrylonitrile", "GenLiquid_HydrogenCyanide"),
                ChemistryRecipes.amounts(1000, 150),
                200, 64, null, new int[]{0, 1});
    }

    private static void acroleinAcrylicAcid() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c3/acrolein",
                ChemistryRecipes.items(ChemistryRecipes.dust("Bismuth", 1), ChemistryRecipes.dust("Cobalt", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("Propylene", "Oxygen"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenLiquid_Acrolein", "DistW"), ChemistryRecipes.amounts(1000, 1000),
                160, 48, null, new int[]{0, 1});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c3/acrylic-acid",
                ChemistryRecipes.items(ChemistryRecipes.dust("Molybdenum", 1), ChemistryRecipes.dust("Vanadium", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Acrolein", "Oxygen"), ChemistryRecipes.amounts(1000, 500),
                ChemistryRecipes.fields("GenLiquid_AcrylicAcid"), ChemistryRecipes.amounts(1000),
                160, 48, null, new int[]{0, 1});
    }

    private static void methylMethacrylate() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c3/acetone-cyanohydrin",
                ChemistryRecipes.items(ChemistryRecipes.item("dustTiny", "SodiumHydroxide", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Acetone", "GenLiquid_HydrogenCyanide"),
                ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenLiquid_AcetoneCyanohydrin"), ChemistryRecipes.amounts(1000),
                120, 32);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c3/methyl-methacrylate",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_AcetoneCyanohydrin", "GenLiquid_Methanol", "GenLiquid_SulfuricAcid"),
                ChemistryRecipes.amounts(1000, 1000, 1000),
                ChemistryRecipes.fields("GenLiquid_MethylMethacrylate", "Dirty_Water"),
                ChemistryRecipes.amounts(1000, 1000),
                200, 48);
    }

    private static void oxoAlcohols() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c3/butyraldehyde",
                ChemistryRecipes.items(ChemistryRecipes.dust("Cobalt", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("Propylene", "GenGas_CarbonMonoxide", "Hydrogen"),
                ChemistryRecipes.amounts(1000, 1000, 1000),
                ChemistryRecipes.fields("GenLiquid_Butyraldehyde"), ChemistryRecipes.amounts(1000),
                200, 64, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c3/butanol",
                ChemistryRecipes.items(ChemistryRecipes.circuit(1), ChemistryRecipes.dust("Nickel", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Butyraldehyde", "Hydrogen"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenLiquid_Butanol"), ChemistryRecipes.amounts(1000),
                120, 32, null, new int[]{0, 1});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c3/two-ethylhexanol",
                ChemistryRecipes.items(ChemistryRecipes.circuit(2), ChemistryRecipes.dust("Nickel", 1),
                        ChemistryRecipes.item("dustTiny", "SodiumHydroxide", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Butyraldehyde", "Hydrogen"), ChemistryRecipes.amounts(2000, 2000),
                ChemistryRecipes.fields("GenLiquid_TwoEthylhexanol", "DistW"), ChemistryRecipes.amounts(1000, 1000),
                200, 48, null, new int[]{0, 1});
    }

    private static void prinsIsoprene() {
        ChemistryRecipes.recipe("gt.recipe.calciner", "c3/solid-phosphoric-acid",
                ChemistryRecipes.items(ChemistryRecipes.dust("Phosphorus", 1), ChemistryRecipes.dust("SiliconDioxide", 2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("SolidPhosphoricAcid", 3)),
                ChemistryRecipes.fields("Oxygen", "DistW"), ChemistryRecipes.amounts(2500, 1500),
                null, null,
                200, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c3/dimethyldioxane",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_Isobutylene", "GenGas_Formaldehyde", "GenLiquid_SulfuricAcid"),
                ChemistryRecipes.amounts(1000, 2000, 50),
                ChemistryRecipes.fields("GenLiquid_Dimethyldioxane"), ChemistryRecipes.amounts(1000),
                160, 32);
        ChemistryRecipes.recipe("gt.recipe.catalyticcracking", "c3/isoprene-prins",
                ChemistryRecipes.items(ChemistryRecipes.dust("SolidPhosphoricAcid", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Dimethyldioxane"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_Isoprene", "GenGas_Formaldehyde", "DistW"),
                ChemistryRecipes.amounts(1000, 1000, 1000),
                160, 48, null, new int[]{0});
    }
}
