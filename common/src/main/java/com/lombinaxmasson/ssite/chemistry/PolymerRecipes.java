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
        polyolefinVinyl();
        styreneIonExchange();
        aromaticOxidationPolyester();
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

    // ── Card 3: polyolefins and vinyl polymers (LDPE, UHMWPE, PP, PVC, PVAc/PVA/PVB, EVA) ──

    private static void polyolefinVinyl() {
        ldpe();
        uhmwpe();
        polypropylene();
        pvc();
        pvac();
        eva();
        pva();
        pvb();
        polymerForming("Polypropylene", false);
        polymerForming("UltraHighMolecularWeightPolyethylene", true);
        polymerForming("PolyvinylAlcohol", false);
        polymerForming("PolyvinylButyral", false);
        polymerForming("EthyleneVinylAcetate", false);
    }

    private static void ldpe() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c3/ldpe",
                ChemistryRecipes.items(ChemistryRecipes.circuit(1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Plastic", 1)),
                ChemistryRecipes.fields("Ethylene", "Oxygen"), ChemistryRecipes.amounts(1000, 5),
                null, null,
                200, 64, null, new int[]{0});
    }

    private static void uhmwpe() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c3/uhmwpe",
                ChemistryRecipes.items(ChemistryRecipes.circuit(2), ChemistryRecipes.dust("Zirconium", 1),
                        ChemistryRecipes.dust("Aluminium", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("UltraHighMolecularWeightPolyethylene", 1)),
                ChemistryRecipes.fields("Ethylene"), ChemistryRecipes.amounts(1000),
                null, null,
                300, 96, null, new int[]{0, 1, 2});
    }

    private static void polypropylene() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c3/polypropylene",
                ChemistryRecipes.items(ChemistryRecipes.dust("MagnesiumChloride", 1),
                        ChemistryRecipes.dust("Aluminium", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Polypropylene", 1)),
                ChemistryRecipes.fields("GenMolten_TitaniumTetrachloride", "Propylene"),
                ChemistryRecipes.amounts(10, 1000),
                null, null,
                200, 64, null, new int[]{0, 1});
    }

    private static void pvc() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c3/pvc",
                ChemistryRecipes.items(), ChemistryRecipes.items(ChemistryRecipes.dust("PVC", 1)),
                ChemistryRecipes.fields("GenGas_VinylChloride", "GenLiquid_DeionizedWater", "GenLiquid_CumeneHydroperoxide"),
                ChemistryRecipes.amounts(1000, 1000, 10),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                200, 48);
    }

    private static void pvac() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c3/pvac",
                ChemistryRecipes.items(ChemistryRecipes.circuit(1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("PolyvinylAcetate", 1)),
                ChemistryRecipes.fields("GenLiquid_VinylAcetate", "GenLiquid_CumeneHydroperoxide"),
                ChemistryRecipes.amounts(1000, 10),
                null, null,
                160, 32, null, new int[]{0});
    }

    private static void eva() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c3/eva",
                ChemistryRecipes.items(ChemistryRecipes.circuit(2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("EthyleneVinylAcetate", 4)),
                ChemistryRecipes.fields("Ethylene", "GenLiquid_VinylAcetate", "GenLiquid_CumeneHydroperoxide"),
                ChemistryRecipes.amounts(3000, 1000, 10),
                null, null,
                300, 48, null, new int[]{0});
    }

    private static void pva() {
        ChemistryRecipes.recipe("gt.recipe.bath", "c3/pva",
                ChemistryRecipes.items(ChemistryRecipes.dust("PolyvinylAcetate", 1),
                        ChemistryRecipes.item("dustTiny", "SodiumHydroxide", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("PolyvinylAlcohol", 1)),
                ChemistryRecipes.fields("BioEthanol"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_EthylAcetate"), ChemistryRecipes.amounts(1000),
                160, 32);
        if (FluidBridge.distinct("GenLiquid_Ethanol", "BioEthanol")) {
            ChemistryRecipes.recipe("gt.recipe.bath", "c3/pva-pure",
                    ChemistryRecipes.items(ChemistryRecipes.dust("PolyvinylAcetate", 1),
                            ChemistryRecipes.item("dustTiny", "SodiumHydroxide", 1)),
                    ChemistryRecipes.items(ChemistryRecipes.dust("PolyvinylAlcohol", 1)),
                    ChemistryRecipes.fields("GenLiquid_Ethanol"), ChemistryRecipes.amounts(1000),
                    ChemistryRecipes.fields("GenLiquid_EthylAcetate"), ChemistryRecipes.amounts(1000),
                    160, 32);
        }
    }

    private static void pvb() {
        ChemistryRecipes.recipe("gt.recipe.bath", "c3/pvb",
                ChemistryRecipes.items(ChemistryRecipes.dust("PolyvinylAlcohol", 2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("PolyvinylButyral", 2)),
                ChemistryRecipes.fields("GenLiquid_Butyraldehyde"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                200, 32);
    }

    /** Press/rollingmill (and wiremill for {@code wires}) shaping shared by every M4 polymer. */
    private static void polymerForming(String material, boolean wires) {
        ChemistryRecipes.recipe("gt.recipe.press", "forming/" + material.toLowerCase() + "/plate",
                ChemistryRecipes.items(ChemistryRecipes.dust(material, 1), ChemistryRecipes.circuit(1)),
                ChemistryRecipes.items(ChemistryRecipes.item("plate", material, 1)),
                null, null, null, null,
                100, 16, null, new int[]{1});
        ChemistryRecipes.recipe("gt.recipe.rollingmill", "forming/" + material.toLowerCase() + "/foil",
                ChemistryRecipes.items(ChemistryRecipes.item("plate", material, 1)),
                ChemistryRecipes.items(ChemistryRecipes.item("foil", material, 4)),
                null, null, null, null,
                100, 16);
        if (wires) {
            ChemistryRecipes.recipe("gt.recipe.wiremill", "forming/" + material.toLowerCase() + "/wire",
                    ChemistryRecipes.items(ChemistryRecipes.item("plate", material, 1)),
                    ChemistryRecipes.items(ChemistryRecipes.item("wireFine", material, 4)),
                    null, null, null, null,
                    100, 16);
        }
    }

    // ── Card 4: styrene, ion-exchange resins, and PMMA ──

    private static void styreneIonExchange() {
        styrene();
        divinylbenzene();
        polystyrene();
        crosslinkedBeads();
        cationResin();
        anionResin();
        resinDeionizedWater();
        resinBisphenolA();
        pmma();
        polymerForming("Polystyrene", false);
        polymerForming("PolymethylMethacrylate", false);
    }

    private static void styrene() {
        ChemistryRecipes.recipe("gt.recipe.catalyticcracking", "c4/styrene",
                ChemistryRecipes.items(ChemistryRecipes.dust("Hematite", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Ethylbenzene", "Steam"), ChemistryRecipes.amounts(1000, 2000),
                ChemistryRecipes.fields("GenLiquid_Styrene", "Hydrogen", "DistW"),
                ChemistryRecipes.amounts(1000, 1000, 2000),
                200, 64, null, new int[]{0});
    }

    private static void divinylbenzene() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c4/diethylbenzene",
                ChemistryRecipes.items(ChemistryRecipes.dust("ZSM5", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Ethylbenzene", "Ethylene"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenLiquid_Diethylbenzene"), ChemistryRecipes.amounts(1000),
                160, 32, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.catalyticcracking", "c4/divinylbenzene",
                ChemistryRecipes.items(ChemistryRecipes.dust("Hematite", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Diethylbenzene", "Steam"), ChemistryRecipes.amounts(1000, 2000),
                ChemistryRecipes.fields("GenLiquid_Divinylbenzene", "Hydrogen", "DistW"),
                ChemistryRecipes.amounts(1000, 2000, 2000),
                240, 64, null, new int[]{0});
    }

    private static void polystyrene() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c4/polystyrene",
                ChemistryRecipes.items(ChemistryRecipes.circuit(1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Polystyrene", 1)),
                ChemistryRecipes.fields("GenLiquid_Styrene", "GenLiquid_CumeneHydroperoxide"),
                ChemistryRecipes.amounts(1000, 10),
                null, null,
                160, 32, null, new int[]{0});
    }

    private static void crosslinkedBeads() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c4/crosslinked-polystyrene",
                ChemistryRecipes.items(ChemistryRecipes.circuit(2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("CrosslinkedPolystyrene", 1)),
                ChemistryRecipes.fields("GenLiquid_Styrene", "GenLiquid_Divinylbenzene", "GenLiquid_DeionizedWater",
                        "GenLiquid_CumeneHydroperoxide"),
                ChemistryRecipes.amounts(900, 100, 1000, 10),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                200, 48, null, new int[]{0});
    }

    private static void cationResin() {
        ChemistryRecipes.recipe("gt.recipe.bath", "c4/cation-resin",
                ChemistryRecipes.items(ChemistryRecipes.dust("CrosslinkedPolystyrene", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("CationExchangeResin", 1)),
                ChemistryRecipes.fields("GenLiquid_SulfuricAcid"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                200, 32);
    }

    private static void anionResin() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c4/chloromethylated-polystyrene",
                ChemistryRecipes.items(ChemistryRecipes.dust("CrosslinkedPolystyrene", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("ChloromethylatedPolystyrene", 1)),
                ChemistryRecipes.fields("GenGas_Formaldehyde", "GenGas_HydrochloricAcid", "GenLiquid_Methanol"),
                ChemistryRecipes.amounts(1000, 1000, 100),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                200, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c4/anion-resin",
                ChemistryRecipes.items(ChemistryRecipes.dust("ChloromethylatedPolystyrene", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("AnionExchangeResin", 1)),
                ChemistryRecipes.fields("GenLiquid_Methanol", "GenGas_Ammonia"), ChemistryRecipes.amounts(3000, 1000),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(3000),
                200, 48);
    }

    private static void resinDeionizedWater() {
        ChemistryRecipes.recipe("gt.recipe.bath", "c4/resin-deionized-water",
                ChemistryRecipes.items(ChemistryRecipes.dust("CationExchangeResin", 1),
                        ChemistryRecipes.dust("AnionExchangeResin", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_DemineralizedWater"), ChemistryRecipes.amounts(4000),
                ChemistryRecipes.fields("GenLiquid_DeionizedWater", "Saltwater"), ChemistryRecipes.amounts(3950, 50),
                120, 30, null, new int[]{0, 1});
    }

    private static void resinBisphenolA() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c4/resin-bisphenol-a",
                ChemistryRecipes.items(ChemistryRecipes.dust("CationExchangeResin", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("BisphenolA", 1)),
                ChemistryRecipes.fields("GenLiquid_Phenol", "GenLiquid_Acetone"), ChemistryRecipes.amounts(2000, 1000),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                200, 48, null, new int[]{0});
    }

    private static void pmma() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c4/pmma",
                ChemistryRecipes.items(), ChemistryRecipes.items(ChemistryRecipes.dust("PolymethylMethacrylate", 1)),
                ChemistryRecipes.fields("GenLiquid_MethylMethacrylate", "GenLiquid_CumeneHydroperoxide"),
                ChemistryRecipes.amounts(1000, 10),
                null, null,
                160, 32);
    }

    // ── Card 5: aromatic oxidation, phosgene, PET and polycarbonate ──

    private static void aromaticOxidationPolyester() {
        phthalicAnhydride();
        terephthalicAcid();
        isophthalicAcid();
        phosgene();
        pet();
        polycarbonate();
        metallizedPolyester();
        polymerForming("PolyethyleneTerephthalate", true);
    }

    private static void phthalicAnhydride() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c5/phthalic-anhydride",
                ChemistryRecipes.items(ChemistryRecipes.dust("VanadiumPentoxide", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("PhthalicAnhydride", 1)),
                ChemistryRecipes.fields("GenLiquid_OrthoXylene", "Oxygen"), ChemistryRecipes.amounts(1000, 3000),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(3000),
                200, 64, null, new int[]{0});
    }

    private static void terephthalicAcid() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c5/terephthalic-acid",
                ChemistryRecipes.items(ChemistryRecipes.dust("Cobalt", 1), ChemistryRecipes.dust("Manganese", 1),
                        ChemistryRecipes.dust("ParaXylene", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("TerephthalicAcid", 1)),
                ChemistryRecipes.fields("GenLiquid_AceticAcid", "GenGas_Bromine", "Oxygen"),
                ChemistryRecipes.amounts(100, 10, 3000),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(2000),
                300, 96, null, new int[]{0, 1});
    }

    private static void isophthalicAcid() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c5/isophthalic-acid",
                ChemistryRecipes.items(ChemistryRecipes.dust("Cobalt", 1), ChemistryRecipes.dust("Manganese", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("IsophthalicAcid", 1)),
                ChemistryRecipes.fields("GenLiquid_MetaXylene", "GenLiquid_AceticAcid", "GenGas_Bromine", "Oxygen"),
                ChemistryRecipes.amounts(1000, 100, 10, 3000),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(2000),
                300, 96, null, new int[]{0, 1});
    }

    private static void phosgene() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c5/phosgene",
                ChemistryRecipes.items(ChemistryRecipes.dust("Charcoal", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_CarbonMonoxide", "GenGas_Chlorine"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenGas_Phosgene"), ChemistryRecipes.amounts(1000),
                120, 48, null, new int[]{0});
    }

    private static void pet() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c5/pet",
                ChemistryRecipes.items(ChemistryRecipes.dust("Antimony", 1), ChemistryRecipes.dust("TerephthalicAcid", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("PolyethyleneTerephthalate", 1)),
                ChemistryRecipes.fields("GenLiquid_EthyleneGlycol"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(2000),
                240, 64, null, new int[]{0});
    }

    private static void polycarbonate() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c5/polycarbonate",
                ChemistryRecipes.items(ChemistryRecipes.dust("BisphenolA", 1), ChemistryRecipes.dust("SodiumHydroxide", 2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Polycarbonate", 1)),
                ChemistryRecipes.fields("GenGas_Phosgene"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("Saltwater"), ChemistryRecipes.amounts(2000),
                240, 64);
    }

    private static void metallizedPolyester() {
        ChemistryRecipes.recipe("gt.recipe.laminator", "c5/metallized-polyester",
                ChemistryRecipes.items(ChemistryRecipes.item("foil", "PolyethyleneTerephthalate", 1),
                        ChemistryRecipes.item("foil", "Aluminium", 1)),
                ChemistryRecipes.items(ChemistryRecipes.item("foil", "MetallizedPolyester", 1)),
                null, null, null, null,
                100, 32);
    }
}
