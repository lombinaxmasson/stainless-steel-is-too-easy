package com.lombinaxmasson.ssite.chemistry;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
        phenolicEpoxy();
        polyurethaneResin();
        rubberVulcanization();
        nylonCarbonFiber();
        aramids();
        fluoropolymers();
        engineeringPlastics();
        extractantsLubricants();
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
                ChemistryRecipes.fields("GenLiquid_TitaniumTetrachloride", "Propylene"),
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

    // ── Card 6: phenolic resin, glass fiber, and the four epoxy resins ──

    private static void phenolicEpoxy() {
        phenolicResin();
        glassFiber();
        dgebaEpoxy();
        brominatedEpoxy();
        cresolNovolac();
        cycloaliphaticEpoxy();
        polymerForming("Epoxy", false);
        ChemistryRecipes.recipe("gt.recipe.rollingmill", "forming/fiberreinforcedepoxy/foil",
                ChemistryRecipes.items(ChemistryRecipes.item("plate", "FiberReinforcedEpoxy", 1)),
                ChemistryRecipes.items(ChemistryRecipes.item("foil", "FiberReinforcedEpoxy", 4)),
                null, null, null, null,
                100, 16);
        ChemistryRecipes.recipe("gt.recipe.rollingmill", "forming/flameretardantlaminate/foil",
                ChemistryRecipes.items(ChemistryRecipes.item("plate", "FlameRetardantLaminate", 1)),
                ChemistryRecipes.items(ChemistryRecipes.item("foil", "FlameRetardantLaminate", 4)),
                null, null, null, null,
                100, 16);
    }

    private static void phenolicResin() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c6/phenolic-resin",
                ChemistryRecipes.items(ChemistryRecipes.item("dustTiny", "SodiumHydroxide", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Phenol", "GenGas_Formaldehyde"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenLiquid_PhenolicResin"), ChemistryRecipes.amounts(1000),
                160, 32);
        ChemistryRecipes.recipe("gt.recipe.coagulator", "c6/bakelite",
                ChemistryRecipes.items(),
                ChemistryRecipes.items(ChemistryRecipes.dust("Bakelite", 1)),
                ChemistryRecipes.fields("GenLiquid_PhenolicResin"), ChemistryRecipes.amounts(1000),
                null, null,
                200, 16);
        ChemistryRecipes.recipe("gt.recipe.bath", "c6/bakelite-paper",
                ChemistryRecipes.items(ChemistryRecipes.item("plate", "Paper", 1)),
                ChemistryRecipes.items(ChemistryRecipes.item("plate", "Bakelite", 1)),
                ChemistryRecipes.fields("GenLiquid_PhenolicResin"), ChemistryRecipes.amounts(250),
                null, null,
                100, 16);
    }

    private static void glassFiber() {
        ChemistryRecipes.recipe("gt.recipe.calciner", "c6/glass-fiber",
                ChemistryRecipes.items(ChemistryRecipes.dust("Glass", 4), ChemistryRecipes.dust("Borax", 1),
                        ChemistryRecipes.dust("Quicklime", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("GlassFiber", 6)),
                null, null, null, null,
                300, 64);
        ChemistryRecipes.recipe("gt.recipe.wiremill", "c6/glass-fiber-wire",
                ChemistryRecipes.items(ChemistryRecipes.dust("GlassFiber", 1)),
                ChemistryRecipes.items(ChemistryRecipes.item("wireFine", "GlassFiber", 4)),
                null, null, null, null,
                100, 32);
    }

    private static void dgebaEpoxy() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c6/epoxy-resin",
                ChemistryRecipes.items(ChemistryRecipes.dust("BisphenolA", 1), ChemistryRecipes.dust("SodiumHydroxide", 2)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Epichlorohydrin"), ChemistryRecipes.amounts(2000),
                ChemistryRecipes.fields("GenLiquid_EpoxyResin", "Saltwater"), ChemistryRecipes.amounts(1000, 2000),
                200, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c6/epoxy-cure",
                ChemistryRecipes.items(ChemistryRecipes.dust("PhthalicAnhydride", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Epoxy", 1)),
                ChemistryRecipes.fields("GenLiquid_EpoxyResin"), ChemistryRecipes.amounts(1000),
                null, null,
                160, 32);
        ChemistryRecipes.recipe("gt.recipe.bath", "c6/epoxy-glass-fiber",
                ChemistryRecipes.items(ChemistryRecipes.item("wireFine", "GlassFiber", 8),
                        ChemistryRecipes.dust("PhthalicAnhydride", 1)),
                ChemistryRecipes.items(ChemistryRecipes.item("plate", "FiberReinforcedEpoxy", 2)),
                ChemistryRecipes.fields("GenLiquid_EpoxyResin"), ChemistryRecipes.amounts(1000),
                null, null,
                200, 48);
    }

    private static void brominatedEpoxy() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c6/tetrabromobisphenol-a",
                ChemistryRecipes.items(ChemistryRecipes.dust("BisphenolA", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("TetrabromobisphenolA", 1)),
                ChemistryRecipes.fields("GenGas_Bromine", "GenGas_HydrogenPeroxide"),
                ChemistryRecipes.amounts(2000, 2000),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(4000),
                200, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c6/flame-retardant-epoxy-resin",
                ChemistryRecipes.items(ChemistryRecipes.dust("TetrabromobisphenolA", 1),
                        ChemistryRecipes.dust("SodiumHydroxide", 2)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Epichlorohydrin"), ChemistryRecipes.amounts(2000),
                ChemistryRecipes.fields("GenLiquid_FlameRetardantEpoxyResin", "Saltwater"),
                ChemistryRecipes.amounts(1000, 2000),
                200, 48);
        ChemistryRecipes.recipe("gt.recipe.bath", "c6/flame-retardant-laminate",
                ChemistryRecipes.items(ChemistryRecipes.item("wireFine", "GlassFiber", 8),
                        ChemistryRecipes.dust("PhthalicAnhydride", 1)),
                ChemistryRecipes.items(ChemistryRecipes.item("plate", "FlameRetardantLaminate", 2)),
                ChemistryRecipes.fields("GenLiquid_FlameRetardantEpoxyResin"), ChemistryRecipes.amounts(1000),
                null, null,
                200, 64);
    }

    private static void cresolNovolac() {
        ChemistryRecipes.recipe("gt.recipe.distillery", "c6/cresol",
                ChemistryRecipes.items(ChemistryRecipes.circuit(4)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("Oil_Creosote"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_Cresol", "GenLiquid_Phenol"), ChemistryRecipes.amounts(300, 200),
                120, 32, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c6/epoxy-cresol-novolac",
                ChemistryRecipes.items(ChemistryRecipes.dust("SodiumHydroxide", 2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("EpoxyCresolNovolac", 1)),
                ChemistryRecipes.fields("GenLiquid_Cresol", "GenGas_Formaldehyde", "GenLiquid_Epichlorohydrin"),
                ChemistryRecipes.amounts(1000, 1000, 2000),
                ChemistryRecipes.fields("Saltwater"), ChemistryRecipes.amounts(2000),
                240, 64);
    }

    private static void cycloaliphaticEpoxy() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c6/peracetic-acid",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_AceticAcid", "GenGas_HydrogenPeroxide", "GenLiquid_SulfuricAcid"),
                ChemistryRecipes.amounts(1000, 1000, 10),
                ChemistryRecipes.fields("GenLiquid_PeraceticAcid", "DistW"), ChemistryRecipes.amounts(1000, 1000),
                120, 32);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c6/tetrahydrobenzaldehyde",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_Butadiene", "GenLiquid_Acrolein"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenLiquid_Tetrahydrobenzaldehyde"), ChemistryRecipes.amounts(1000),
                160, 32);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c6/cycloaliphatic-epoxy",
                ChemistryRecipes.items(ChemistryRecipes.dust("Aluminium", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Tetrahydrobenzaldehyde", "GenLiquid_PeraceticAcid"),
                ChemistryRecipes.amounts(2000, 2000),
                ChemistryRecipes.fields("GenLiquid_CycloaliphaticEpoxy", "GenLiquid_AceticAcid"),
                ChemistryRecipes.amounts(1000, 2000),
                240, 64, null, new int[]{0});
    }

    // ── Card 7: aniline/MDI, polyurethane, and hydrogenated petroleum resin ──

    private static void polyurethaneResin() {
        aniline();
        mdi();
        polyetherPolyol();
        polyurethane();
        sponge();
        petroleumResin();
        polymerForming("Polyurethane", false);
    }

    private static void aniline() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c7/nitrobenzene",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Benzene", "GenLiquid_NitricAcid", "GenLiquid_SulfuricAcid"),
                ChemistryRecipes.amounts(1000, 1000, 100),
                ChemistryRecipes.fields("GenLiquid_Nitrobenzene", "DistW"), ChemistryRecipes.amounts(1000, 1000),
                160, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c7/aniline",
                ChemistryRecipes.items(ChemistryRecipes.dust("Nickel", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Nitrobenzene", "Hydrogen"), ChemistryRecipes.amounts(1000, 3000),
                ChemistryRecipes.fields("GenLiquid_Aniline", "DistW"), ChemistryRecipes.amounts(1000, 2000),
                160, 48, null, new int[]{0});
    }

    private static void mdi() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c7/methylenedianiline",
                ChemistryRecipes.items(),
                ChemistryRecipes.items(ChemistryRecipes.dust("Methylenedianiline", 1)),
                ChemistryRecipes.fields("GenLiquid_Aniline", "GenGas_Formaldehyde", "GenGas_HydrochloricAcid"),
                ChemistryRecipes.amounts(2000, 1000, 100),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                200, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c7/mdi",
                ChemistryRecipes.items(ChemistryRecipes.dust("Methylenedianiline", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_Phosgene"), ChemistryRecipes.amounts(2000),
                ChemistryRecipes.fields("GenLiquid_MethyleneDiphenylDiisocyanate", "GenGas_HydrochloricAcid"),
                ChemistryRecipes.amounts(1000, 4000),
                200, 64);
    }

    private static void polyetherPolyol() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c7/polyether-polyol",
                ChemistryRecipes.items(ChemistryRecipes.item("dustTiny", "PotassiumHydroxide", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Glycerol", "GenGas_EthyleneOxide"), ChemistryRecipes.amounts(1000, 3000),
                ChemistryRecipes.fields("GenLiquid_PolyetherPolyol"), ChemistryRecipes.amounts(1000),
                200, 48);
    }

    private static void polyurethane() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c7/polyurethane-polyol",
                ChemistryRecipes.items(ChemistryRecipes.circuit(1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Polyurethane", 2)),
                ChemistryRecipes.fields("GenLiquid_MethyleneDiphenylDiisocyanate", "GenLiquid_PolyetherPolyol"),
                ChemistryRecipes.amounts(1000, 1000),
                null, null,
                200, 48, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c7/polyurethane-glycol",
                ChemistryRecipes.items(ChemistryRecipes.circuit(2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Polyurethane", 1)),
                ChemistryRecipes.fields("GenLiquid_MethyleneDiphenylDiisocyanate", "GenLiquid_EthyleneGlycol"),
                ChemistryRecipes.amounts(1000, 1000),
                null, null,
                200, 48, null, new int[]{0});
    }

    private static void sponge() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c7/sponge",
                ChemistryRecipes.items(new ItemStack(Items.YELLOW_DYE, 1)),
                ChemistryRecipes.items(new ItemStack(Items.SPONGE, 1)),
                ChemistryRecipes.fields("GenLiquid_MethyleneDiphenylDiisocyanate", "GenLiquid_PolyetherPolyol",
                        "CarbonDioxide"),
                ChemistryRecipes.amounts(250, 250, 500),
                null, null,
                100, 32);
    }

    private static void petroleumResin() {
        ChemistryRecipes.recipe("gt.recipe.calciner", "c7/petroleum-resin",
                ChemistryRecipes.items(ChemistryRecipes.dust("Norbornene", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("PetroleumResin", 3)),
                ChemistryRecipes.fields("GenLiquid_Dicyclopentadiene", "GenLiquid_Benzene"),
                ChemistryRecipes.amounts(1000, 1000),
                null, null,
                240, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c7/hydrogenated-petroleum-resin",
                ChemistryRecipes.items(ChemistryRecipes.dust("Palladium", 1), ChemistryRecipes.dust("PetroleumResin", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("HydrogenatedPetroleumResin", 1)),
                ChemistryRecipes.fields("Hydrogen"), ChemistryRecipes.amounts(1000),
                null, null,
                160, 48, null, new int[]{0});
    }

    // ── Card 8: synthetic rubber, silicone rubber, and vulcanization ──

    private static void rubberVulcanization() {
        carbonBlack();
        zincOxide();
        magnesiumOxide();
        rawRubbers();
        silicones();
        vulcanize();
        polymerForming("Ebonite", false);
    }

    private static void carbonBlack() {
        ChemistryRecipes.recipe("gt.recipe.calciner", "c8/carbon-black-furnace",
                ChemistryRecipes.items(), ChemistryRecipes.items(ChemistryRecipes.dust("CarbonBlack", 2)),
                ChemistryRecipes.fields("Fuel", "Oxygen"), ChemistryRecipes.amounts(1000, 500),
                ChemistryRecipes.fields("CarbonDioxide"), ChemistryRecipes.amounts(500),
                160, 48);
        ChemistryRecipes.recipe("gt.recipe.calciner", "c8/carbon-black-thermal",
                ChemistryRecipes.items(), ChemistryRecipes.items(ChemistryRecipes.dust("CarbonBlack", 1)),
                ChemistryRecipes.fields("Methane"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("Hydrogen"), ChemistryRecipes.amounts(2000),
                160, 48);
    }

    private static void zincOxide() {
        ChemistryRecipes.recipe("gt.recipe.roaster", "c8/zinc-oxide",
                ChemistryRecipes.items(ChemistryRecipes.dust("Zinc", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("ZincOxide", 1)),
                ChemistryRecipes.fields("Oxygen"), ChemistryRecipes.amounts(1000),
                null, null,
                120, 32);
    }

    private static void magnesiumOxide() {
        ChemistryRecipes.recipe("gt.recipe.calciner", "c8/magnesium-oxide",
                ChemistryRecipes.items(ChemistryRecipes.dust("MagnesiumHydroxide", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("MagnesiumOxide", 1)),
                null, null,
                ChemistryRecipes.fields("Steam"), ChemistryRecipes.amounts(1000),
                120, 32);
    }

    private static void rawRubbers() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c8/raw-polyisoprene",
                ChemistryRecipes.items(ChemistryRecipes.dust("Aluminium", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("RawPolyisoprene", 1)),
                ChemistryRecipes.fields("GenLiquid_TitaniumTetrachloride", "GenLiquid_Isoprene"),
                ChemistryRecipes.amounts(10, 1000),
                null, null,
                200, 64, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c8/raw-sbr",
                ChemistryRecipes.items(), ChemistryRecipes.items(ChemistryRecipes.dust("RawStyreneButadieneRubber", 1)),
                ChemistryRecipes.fields("GenGas_Butadiene", "GenLiquid_Styrene", "GenLiquid_DeionizedWater",
                        "GenLiquid_CumeneHydroperoxide"),
                ChemistryRecipes.amounts(750, 250, 1000, 10),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                200, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c8/raw-nbr",
                ChemistryRecipes.items(), ChemistryRecipes.items(ChemistryRecipes.dust("RawNitrileRubber", 1)),
                ChemistryRecipes.fields("GenGas_Butadiene", "GenLiquid_Acrylonitrile", "GenLiquid_DeionizedWater",
                        "GenLiquid_CumeneHydroperoxide"),
                ChemistryRecipes.amounts(700, 300, 1000, 10),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                200, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c8/dichlorobutene",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_Butadiene", "GenGas_Chlorine"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenLiquid_Dichlorobutene"), ChemistryRecipes.amounts(1000),
                120, 32);
        ChemistryRecipes.recipe("gt.recipe.bath", "c8/chloroprene",
                ChemistryRecipes.items(ChemistryRecipes.dust("SodiumHydroxide", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Salt", 1)),
                ChemistryRecipes.fields("GenLiquid_Dichlorobutene"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_Chloroprene", "DistW"), ChemistryRecipes.amounts(1000, 1000),
                120, 32);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c8/raw-neoprene",
                ChemistryRecipes.items(), ChemistryRecipes.items(ChemistryRecipes.dust("RawNeoprene", 1)),
                ChemistryRecipes.fields("GenLiquid_Chloroprene", "GenLiquid_DeionizedWater",
                        "GenLiquid_CumeneHydroperoxide"),
                ChemistryRecipes.amounts(1000, 1000, 10),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                200, 48);
    }

    private static void silicones() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c8/chloromethane",
                ChemistryRecipes.items(ChemistryRecipes.circuit(1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Methanol", "GenGas_HydrochloricAcid"),
                ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenGas_Chloromethane", "DistW"), ChemistryRecipes.amounts(1000, 1000),
                120, 32, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c8/dimethyldichlorosilane",
                ChemistryRecipes.items(ChemistryRecipes.dust("Copper", 1), ChemistryRecipes.dust("Silicon", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_Chloromethane"), ChemistryRecipes.amounts(2000),
                ChemistryRecipes.fields("GenLiquid_Dimethyldichlorosilane"), ChemistryRecipes.amounts(1000),
                200, 64, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c8/polydimethylsiloxane",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Dimethyldichlorosilane", "DistW"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenLiquid_Polydimethylsiloxane", "GenGas_HydrochloricAcid"),
                ChemistryRecipes.amounts(1000, 2000),
                160, 32);
    }

    private static void vulcanize() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c8/vulcanize-polyisoprene",
                ChemistryRecipes.items(ChemistryRecipes.dust("RawPolyisoprene", 1),
                        ChemistryRecipes.item("dustSmall", "CarbonBlack", 1),
                        ChemistryRecipes.item("dustTiny", "Sulfur", 1),
                        ChemistryRecipes.dust("ZincOxide", 1)),
                ChemistryRecipes.items(ChemistryRecipes.item("ingot", "Rubber", 1)),
                null, null, null, null,
                200, 48, null, new int[]{3});
        vulcanizeStandard("RawStyreneButadieneRubber", "StyreneButadieneRubber");
        vulcanizeStandard("RawNitrileRubber", "NitrileRubber");
        ChemistryRecipes.recipe("gt.recipe.mixer", "c8/vulcanize-neoprene",
                ChemistryRecipes.items(ChemistryRecipes.dust("RawNeoprene", 1),
                        ChemistryRecipes.item("dustSmall", "CarbonBlack", 1),
                        ChemistryRecipes.dust("MagnesiumOxide", 1),
                        ChemistryRecipes.dust("ZincOxide", 1)),
                ChemistryRecipes.items(ChemistryRecipes.item("ingot", "Neoprene", 1)),
                null, null, null, null,
                200, 48, null, new int[]{2, 3});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c8/vulcanize-silicone",
                ChemistryRecipes.items(ChemistryRecipes.dust("SiliconDioxide", 1)),
                ChemistryRecipes.items(ChemistryRecipes.item("ingot", "SiliconeRubber", 1)),
                ChemistryRecipes.fields("GenLiquid_Polydimethylsiloxane", "GenLiquid_CumeneHydroperoxide"),
                ChemistryRecipes.amounts(1000, 10),
                null, null,
                200, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c8/ebonite",
                ChemistryRecipes.items(ChemistryRecipes.circuit(1), ChemistryRecipes.dust("RawPolyisoprene", 1),
                        ChemistryRecipes.dust("Sulfur", 1), ChemistryRecipes.dust("ZincOxide", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Ebonite", 1)),
                null, null, null, null,
                200, 48, null, new int[]{0, 3});
    }

    private static void vulcanizeStandard(String raw, String cured) {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c8/vulcanize-" + raw.toLowerCase(),
                ChemistryRecipes.items(ChemistryRecipes.dust(raw, 1),
                        ChemistryRecipes.item("dustSmall", "CarbonBlack", 1),
                        ChemistryRecipes.item("dustTiny", "Sulfur", 1),
                        ChemistryRecipes.dust("ZincOxide", 1)),
                ChemistryRecipes.items(ChemistryRecipes.item("ingot", cured, 1)),
                null, null, null, null,
                200, 48, null, new int[]{3});
    }

    // ── Card 9: caprolactam/nylon 6 and PAN/carbon fiber ──

    private static void nylonCarbonFiber() {
        cyclohexanone();
        caprolactam();
        carbonFiber();
        polymerForming("Polycaprolactam", true);
        polymerForming("Polyacrylonitrile", true);
    }

    private static void cyclohexanone() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c9/cyclohexane",
                ChemistryRecipes.items(ChemistryRecipes.dust("Nickel", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Benzene", "Hydrogen"), ChemistryRecipes.amounts(1000, 3000),
                ChemistryRecipes.fields("GenLiquid_Cyclohexane"), ChemistryRecipes.amounts(1000),
                160, 48, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c9/cyclohexanone-air",
                ChemistryRecipes.items(ChemistryRecipes.dust("Cobalt", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Cyclohexane", "Oxygen"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenLiquid_Cyclohexanone", "DistW"), ChemistryRecipes.amounts(1000, 1000),
                200, 48, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c9/cyclohexanone-phenol",
                ChemistryRecipes.items(ChemistryRecipes.dust("Palladium", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Phenol", "Hydrogen"), ChemistryRecipes.amounts(1000, 2000),
                ChemistryRecipes.fields("GenLiquid_Cyclohexanone"), ChemistryRecipes.amounts(1000),
                160, 48, null, new int[]{0});
    }

    private static void caprolactam() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c9/hydroxylammonium-sulfate",
                ChemistryRecipes.items(ChemistryRecipes.dust("Platinum", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("HydroxylammoniumSulfate", 1)),
                ChemistryRecipes.fields("GenGas_NitrogenMonoxide", "Hydrogen", "GenLiquid_SulfuricAcid"),
                ChemistryRecipes.amounts(2000, 3000, 1000),
                null, null,
                200, 64, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c9/cyclohexanone-oxime",
                ChemistryRecipes.items(ChemistryRecipes.dust("HydroxylammoniumSulfate", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("CyclohexanoneOxime", 2)),
                ChemistryRecipes.fields("GenLiquid_Cyclohexanone", "GenGas_Ammonia"), ChemistryRecipes.amounts(2000, 2000),
                ChemistryRecipes.fields("Dirty_Water"), ChemistryRecipes.amounts(2000),
                200, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c9/caprolactam",
                ChemistryRecipes.items(ChemistryRecipes.dust("CyclohexanoneOxime", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Caprolactam", 1)),
                ChemistryRecipes.fields("GenLiquid_DisulfuricAcid", "GenGas_Ammonia"), ChemistryRecipes.amounts(500, 1000),
                ChemistryRecipes.fields("Dirty_Water"), ChemistryRecipes.amounts(1000),
                160, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c9/nylon6",
                ChemistryRecipes.items(ChemistryRecipes.dust("Caprolactam", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Polycaprolactam", 1)),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(10),
                null, null,
                240, 48);
    }

    private static void carbonFiber() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c9/polyacrylonitrile",
                ChemistryRecipes.items(ChemistryRecipes.circuit(1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Polyacrylonitrile", 1)),
                ChemistryRecipes.fields("GenLiquid_Acrylonitrile", "GenLiquid_DeionizedWater",
                        "GenLiquid_CumeneHydroperoxide"),
                ChemistryRecipes.amounts(1000, 1000, 10),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                200, 48, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.calciner", "c9/oxidize-pan",
                ChemistryRecipes.items(ChemistryRecipes.item("wireFine", "Polyacrylonitrile", 16)),
                ChemistryRecipes.items(ChemistryRecipes.item("wireFine", "OxidizedPolyacrylonitrile", 16)),
                ChemistryRecipes.fields("Air"), ChemistryRecipes.amounts(1000),
                null, null,
                400, 64);
        ChemistryRecipes.recipe("gt.recipe.calciner", "c9/carbonize",
                ChemistryRecipes.items(ChemistryRecipes.item("wireFine", "OxidizedPolyacrylonitrile", 16)),
                ChemistryRecipes.items(ChemistryRecipes.item("wireFine", "CarbonFiber", 8)),
                ChemistryRecipes.fields("Nitrogen"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_HydrogenCyanide"), ChemistryRecipes.amounts(50),
                600, 128);
        ChemistryRecipes.recipe("gt.recipe.loom", "c9/carbon-fiber-cloth",
                ChemistryRecipes.items(ChemistryRecipes.item("wireFine", "CarbonFiber", 8)),
                ChemistryRecipes.items(ChemistryRecipes.item("plate", "CarbonFiber", 1)),
                null, null, null, null,
                200, 32);
        ChemistryRecipes.recipe("gt.recipe.rollingmill", "c9/carbon-fiber-foil",
                ChemistryRecipes.items(ChemistryRecipes.item("plate", "CarbonFiber", 1)),
                ChemistryRecipes.items(ChemistryRecipes.item("foil", "CarbonFiber", 4)),
                null, null, null, null,
                100, 16);
    }

    // ── Card 10: aramids (Kevlar and Nomex) ──

    private static void aramids() {
        paraPhenylenediamine();
        metaPhenylenediamine();
        diacylChlorides();
        kevlar();
        nomex();
        polymerForming("Nomex", true);
    }

    private static void paraPhenylenediamine() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c10/chlorobenzene",
                ChemistryRecipes.items(ChemistryRecipes.dust("FerricChloride", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Benzene", "GenGas_Chlorine"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenLiquid_Chlorobenzene", "GenGas_HydrochloricAcid"),
                ChemistryRecipes.amounts(1000, 1000),
                120, 32, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c10/para-nitrochlorobenzene",
                ChemistryRecipes.items(),
                ChemistryRecipes.items(ChemistryRecipes.dust("ParaNitrochlorobenzene", 1)),
                ChemistryRecipes.fields("GenLiquid_Chlorobenzene", "GenLiquid_NitricAcid", "GenLiquid_SulfuricAcid"),
                ChemistryRecipes.amounts(1000, 1000, 100),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                160, 48);
        ChemistryRecipes.recipe("gt.recipe.autoclave", "c10/para-nitroaniline",
                ChemistryRecipes.items(ChemistryRecipes.circuit(1), ChemistryRecipes.dust("ParaNitrochlorobenzene", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("ParaNitroaniline", 1)),
                ChemistryRecipes.fields("GenGas_Ammonia"), ChemistryRecipes.amounts(2000),
                null, null,
                200, 48, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c10/para-phenylenediamine",
                ChemistryRecipes.items(ChemistryRecipes.dust("Nickel", 1), ChemistryRecipes.dust("ParaNitroaniline", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("ParaPhenylenediamine", 1)),
                ChemistryRecipes.fields("Hydrogen"), ChemistryRecipes.amounts(3000),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(2000),
                160, 48, null, new int[]{0});
    }

    private static void metaPhenylenediamine() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c10/meta-dinitrobenzene",
                ChemistryRecipes.items(),
                ChemistryRecipes.items(ChemistryRecipes.dust("MetaDinitrobenzene", 1)),
                ChemistryRecipes.fields("GenLiquid_Nitrobenzene", "GenLiquid_NitricAcid", "GenLiquid_SulfuricAcid"),
                ChemistryRecipes.amounts(1000, 1000, 200),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                200, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c10/meta-phenylenediamine",
                ChemistryRecipes.items(ChemistryRecipes.dust("Nickel", 1), ChemistryRecipes.dust("MetaDinitrobenzene", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("MetaPhenylenediamine", 1)),
                ChemistryRecipes.fields("Hydrogen"), ChemistryRecipes.amounts(6000),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(4000),
                200, 48, null, new int[]{0});
    }

    private static void diacylChlorides() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c10/terephthaloyl-chloride",
                ChemistryRecipes.items(ChemistryRecipes.dust("TerephthalicAcid", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("TerephthaloylChloride", 1)),
                ChemistryRecipes.fields("GenGas_Phosgene"), ChemistryRecipes.amounts(2000),
                ChemistryRecipes.fields("CarbonDioxide", "GenGas_HydrochloricAcid"), ChemistryRecipes.amounts(2000, 2000),
                160, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c10/isophthaloyl-chloride",
                ChemistryRecipes.items(ChemistryRecipes.dust("IsophthalicAcid", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("IsophthaloylChloride", 1)),
                ChemistryRecipes.fields("GenGas_Phosgene"), ChemistryRecipes.amounts(2000),
                ChemistryRecipes.fields("CarbonDioxide", "GenGas_HydrochloricAcid"), ChemistryRecipes.amounts(2000, 2000),
                160, 48);
    }

    private static void kevlar() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c10/kevlar",
                ChemistryRecipes.items(ChemistryRecipes.dust("CalciumChloride", 1),
                        ChemistryRecipes.dust("ParaPhenylenediamine", 1),
                        ChemistryRecipes.dust("TerephthaloylChloride", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Kevlar", 1)),
                null, null,
                ChemistryRecipes.fields("GenGas_HydrochloricAcid"), ChemistryRecipes.amounts(2000),
                300, 96, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.bath", "c10/kevlar-spin",
                ChemistryRecipes.items(ChemistryRecipes.dust("Kevlar", 1)),
                ChemistryRecipes.items(ChemistryRecipes.item("wireFine", "Kevlar", 4)),
                ChemistryRecipes.fields("GenLiquid_SulfuricAcid"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_SulfuricAcid"), ChemistryRecipes.amounts(950),
                200, 64);
        ChemistryRecipes.recipe("gt.recipe.loom", "c10/kevlar-cloth",
                ChemistryRecipes.items(ChemistryRecipes.item("wireFine", "Kevlar", 8)),
                ChemistryRecipes.items(ChemistryRecipes.item("plate", "Kevlar", 1)),
                null, null, null, null,
                200, 32);
        ChemistryRecipes.recipe("gt.recipe.rollingmill", "c10/kevlar-foil",
                ChemistryRecipes.items(ChemistryRecipes.item("plate", "Kevlar", 1)),
                ChemistryRecipes.items(ChemistryRecipes.item("foil", "Kevlar", 4)),
                null, null, null, null,
                100, 16);
    }

    private static void nomex() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c10/nomex",
                ChemistryRecipes.items(ChemistryRecipes.dust("CalciumChloride", 1),
                        ChemistryRecipes.dust("MetaPhenylenediamine", 1),
                        ChemistryRecipes.dust("IsophthaloylChloride", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Nomex", 1)),
                null, null,
                ChemistryRecipes.fields("GenGas_HydrochloricAcid"), ChemistryRecipes.amounts(2000),
                300, 96, null, new int[]{0});
    }

    // ── Card 11: fluoropolymers (PTFE, PVDF, Viton, Nafion) ──

    private static void fluoropolymers() {
        tetrafluoroethylene();
        vinylideneFluoride();
        perfluorosulfonylVinylEther();
        emulsionPolymers();
        vulcanizeViton();
        polymerForming("PolyvinylideneFluoride", false);
        polymerForming("Nafion", false);
    }

    private static void tetrafluoroethylene() {
        ChemistryRecipes.recipe("gt.recipe.calciner", "c11/chloroform",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("Methane", "GenGas_Chlorine"), ChemistryRecipes.amounts(1000, 3000),
                ChemistryRecipes.fields("GenLiquid_Chloroform", "GenGas_HydrochloricAcid"),
                ChemistryRecipes.amounts(1000, 3000),
                160, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c11/chlorodifluoromethane",
                ChemistryRecipes.items(ChemistryRecipes.dust("Antimony", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Chloroform", "GenGas_HydrogenFluoride"),
                ChemistryRecipes.amounts(1000, 2000),
                ChemistryRecipes.fields("GenGas_Chlorodifluoromethane", "GenGas_HydrochloricAcid"),
                ChemistryRecipes.amounts(1000, 2000),
                200, 64, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.calciner", "c11/tetrafluoroethylene",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_Chlorodifluoromethane"), ChemistryRecipes.amounts(2000),
                ChemistryRecipes.fields("GenGas_Tetrafluoroethylene", "GenGas_HydrochloricAcid"),
                ChemistryRecipes.amounts(1000, 2000),
                200, 96);
        ChemistryRecipes.recipe("gt.recipe.calciner", "c11/hexafluoropropylene",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_Tetrafluoroethylene"), ChemistryRecipes.amounts(3000),
                ChemistryRecipes.fields("GenGas_Hexafluoropropylene"), ChemistryRecipes.amounts(2000),
                200, 96);
    }

    private static void vinylideneFluoride() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c11/trichloroethane",
                ChemistryRecipes.items(ChemistryRecipes.dust("FerricChloride", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_VinylChloride", "GenGas_HydrochloricAcid", "GenGas_Chlorine"),
                ChemistryRecipes.amounts(1000, 1000, 1000),
                ChemistryRecipes.fields("GenLiquid_Trichloroethane", "GenGas_HydrochloricAcid"),
                ChemistryRecipes.amounts(1000, 1000),
                160, 48, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c11/chlorodifluoroethane",
                ChemistryRecipes.items(ChemistryRecipes.dust("Antimony", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Trichloroethane", "GenGas_HydrogenFluoride"),
                ChemistryRecipes.amounts(1000, 2000),
                ChemistryRecipes.fields("GenGas_Chlorodifluoroethane", "GenGas_HydrochloricAcid"),
                ChemistryRecipes.amounts(1000, 2000),
                200, 64, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.calciner", "c11/vinylidene-fluoride",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_Chlorodifluoroethane"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenGas_VinylideneFluoride", "GenGas_HydrochloricAcid"),
                ChemistryRecipes.amounts(1000, 1000),
                160, 64);
    }

    private static void perfluorosulfonylVinylEther() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c11/perfluorosulfonyl-vinyl-ether",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_Tetrafluoroethylene", "GenGas_Hexafluoropropylene",
                        "GenGas_SulfurTrioxide"),
                ChemistryRecipes.amounts(2000, 1000, 1000),
                ChemistryRecipes.fields("GenLiquid_PerfluorosulfonylVinylEther"), ChemistryRecipes.amounts(1000),
                300, 96);
    }

    private static void emulsionPolymers() {
        emulsionPolymer("c11/ptfe", 1, new String[]{"GenGas_Tetrafluoroethylene"}, new int[]{1000},
                "Teflon", 200, 64);
        emulsionPolymer("c11/pvdf", 2, new String[]{"GenGas_VinylideneFluoride"}, new int[]{1000},
                "PolyvinylideneFluoride", 200, 64);
        emulsionPolymer("c11/raw-viton", 3,
                new String[]{"GenGas_VinylideneFluoride", "GenGas_Hexafluoropropylene"}, new int[]{600, 400},
                "RawViton", 240, 64);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c11/nafion",
                ChemistryRecipes.items(ChemistryRecipes.circuit(4), ChemistryRecipes.item("dustTiny", "SodiumPersulfate", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Nafion", 1)),
                ChemistryRecipes.fields("GenGas_Tetrafluoroethylene", "GenLiquid_PerfluorosulfonylVinylEther",
                        "GenLiquid_DeionizedWater"),
                ChemistryRecipes.amounts(1000, 1000, 1000),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                300, 128, null, new int[]{0});
    }

    private static void emulsionPolymer(String label, int circuit, String[] monomers, int[] amounts,
                                        String product, long duration, long perTick) {
        ChemistryRecipes.recipe("gt.recipe.mixer", label,
                ChemistryRecipes.items(ChemistryRecipes.circuit(circuit),
                        ChemistryRecipes.item("dustTiny", "SodiumPersulfate", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust(product, 1)),
                ChemistryRecipes.fields(appendMonomers(monomers, "GenLiquid_DeionizedWater")),
                ChemistryRecipes.amounts(appendAmounts(amounts, 1000)),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                duration, perTick, null, new int[]{0});
    }

    private static String[] appendMonomers(String[] monomers, String water) {
        String[] out = new String[monomers.length + 1];
        System.arraycopy(monomers, 0, out, 0, monomers.length);
        out[monomers.length] = water;
        return out;
    }

    private static int[] appendAmounts(int[] amounts, int waterAmount) {
        int[] out = new int[amounts.length + 1];
        System.arraycopy(amounts, 0, out, 0, amounts.length);
        out[amounts.length] = waterAmount;
        return out;
    }

    private static void vulcanizeViton() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c11/viton",
                ChemistryRecipes.items(ChemistryRecipes.dust("RawViton", 1),
                        ChemistryRecipes.item("dustSmall", "CarbonBlack", 1),
                        ChemistryRecipes.item("dustTiny", "BisphenolA", 1),
                        ChemistryRecipes.dust("MagnesiumOxide", 1)),
                ChemistryRecipes.items(ChemistryRecipes.item("ingot", "Viton", 1)),
                null, null, null, null,
                240, 64, null, new int[]{3});
    }

    // ── Card 12: special engineering plastics (PPS, polysulfone, PEEK, Kapton) ──

    private static void engineeringPlastics() {
        pps();
        polysulfone();
        peek();
        kapton();
        polymerForming("PolyphenyleneSulfide", false);
        polymerForming("Polysulfone", false);
        polymerForming("PolyetherEtherKetone", false);
        polymerForming("Kapton", false);
    }

    private static void pps() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c12/para-dichlorobenzene",
                ChemistryRecipes.items(ChemistryRecipes.dust("FerricChloride", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("ParaDichlorobenzene", 1)),
                ChemistryRecipes.fields("GenLiquid_Chlorobenzene", "GenGas_Chlorine"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenGas_HydrochloricAcid"), ChemistryRecipes.amounts(1000),
                120, 32, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c12/pps",
                ChemistryRecipes.items(ChemistryRecipes.dust("ParaDichlorobenzene", 1),
                        ChemistryRecipes.dust("SodiumSulfide", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("PolyphenyleneSulfide", 1)),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("Saltwater"), ChemistryRecipes.amounts(1000),
                240, 64);
    }

    private static void polysulfone() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c12/dichlorodiphenylsulfone",
                ChemistryRecipes.items(),
                ChemistryRecipes.items(ChemistryRecipes.dust("Dichlorodiphenylsulfone", 1)),
                ChemistryRecipes.fields("GenLiquid_Chlorobenzene", "GenGas_SulfurTrioxide"),
                ChemistryRecipes.amounts(2000, 1000),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                200, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c12/polysulfone",
                ChemistryRecipes.items(ChemistryRecipes.dust("Dichlorodiphenylsulfone", 1),
                        ChemistryRecipes.dust("BisphenolA", 1), ChemistryRecipes.dust("SodiumHydroxide", 2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Polysulfone", 1)),
                null, null,
                ChemistryRecipes.fields("Saltwater"), ChemistryRecipes.amounts(2000),
                240, 64);
    }

    private static void peek() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c12/hydroquinone",
                ChemistryRecipes.items(ChemistryRecipes.dust("ZSM5", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Hydroquinone", 1)),
                ChemistryRecipes.fields("GenLiquid_Phenol", "GenGas_HydrogenPeroxide"), ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(1000),
                200, 48, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c12/fluorobenzene",
                ChemistryRecipes.items(ChemistryRecipes.dust("PotassiumFluoride", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Chlorobenzene"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_Fluorobenzene"), ChemistryRecipes.amounts(1000),
                200, 64);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c12/difluorobenzophenone",
                ChemistryRecipes.items(ChemistryRecipes.dust("FerricChloride", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Difluorobenzophenone", 1)),
                ChemistryRecipes.fields("GenLiquid_Fluorobenzene", "GenGas_Phosgene"), ChemistryRecipes.amounts(2000, 1000),
                ChemistryRecipes.fields("GenGas_HydrochloricAcid"), ChemistryRecipes.amounts(2000),
                200, 64, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c12/peek",
                ChemistryRecipes.items(ChemistryRecipes.dust("Difluorobenzophenone", 1),
                        ChemistryRecipes.dust("Hydroquinone", 1), ChemistryRecipes.dust("PotassiumCarbonate", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("PolyetherEtherKetone", 1)),
                null, null,
                ChemistryRecipes.fields("CarbonDioxide", "DistW"), ChemistryRecipes.amounts(1000, 1000),
                300, 128);
    }

    private static void kapton() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c12/durene",
                ChemistryRecipes.items(ChemistryRecipes.dust("ZSM5", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Durene", 1)),
                ChemistryRecipes.fields("GenLiquid_Xylene", "GenLiquid_Methanol"), ChemistryRecipes.amounts(1000, 2000),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(2000),
                200, 48, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c12/pyromellitic-dianhydride",
                ChemistryRecipes.items(ChemistryRecipes.dust("VanadiumPentoxide", 1), ChemistryRecipes.dust("Durene", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("PyromelliticDianhydride", 1)),
                ChemistryRecipes.fields("Oxygen"), ChemistryRecipes.amounts(6000),
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(6000),
                300, 96, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c12/oxydianiline",
                ChemistryRecipes.items(ChemistryRecipes.dust("Nickel", 1),
                        ChemistryRecipes.dust("ParaNitrochlorobenzene", 2), ChemistryRecipes.dust("SodiumHydroxide", 2)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Oxydianiline", 1)),
                ChemistryRecipes.fields("Hydrogen"), ChemistryRecipes.amounts(6000),
                ChemistryRecipes.fields("Saltwater"), ChemistryRecipes.amounts(2000),
                300, 96, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c12/kapton",
                ChemistryRecipes.items(ChemistryRecipes.dust("PyromelliticDianhydride", 1),
                        ChemistryRecipes.dust("Oxydianiline", 1)),
                ChemistryRecipes.items(ChemistryRecipes.dust("Kapton", 1)),
                null, null,
                ChemistryRecipes.fields("DistW"), ChemistryRecipes.amounts(2000),
                400, 128);
    }

    // ── Card 13: extractants and lubricant base oil ──

    private static void extractantsLubricants() {
        mibk();
        phosphorusExtractants();
        amineExtractants();
        lubricants();
    }

    private static void mibk() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c13/mibk",
                ChemistryRecipes.items(ChemistryRecipes.dust("CationExchangeResin", 1),
                        ChemistryRecipes.dust("Palladium", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Acetone", "Hydrogen"), ChemistryRecipes.amounts(2000, 1000),
                ChemistryRecipes.fields("GenLiquid_MethylIsobutylKetone", "DistW"), ChemistryRecipes.amounts(1000, 1000),
                200, 48, null, new int[]{0, 1});
    }

    private static void phosphorusExtractants() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c13/phosphoryl-chloride",
                ChemistryRecipes.items(ChemistryRecipes.dust("Phosphorus", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_Chlorine", "Oxygen"), ChemistryRecipes.amounts(3000, 500),
                ChemistryRecipes.fields("GenLiquid_PhosphorylChloride"), ChemistryRecipes.amounts(1000),
                160, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c13/tributyl-phosphate",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_PhosphorylChloride", "GenLiquid_Butanol"),
                ChemistryRecipes.amounts(1000, 3000),
                ChemistryRecipes.fields("GenLiquid_TributylPhosphate", "GenGas_HydrochloricAcid"),
                ChemistryRecipes.amounts(1000, 3000),
                200, 48);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c13/p204",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_PhosphorylChloride", "GenLiquid_TwoEthylhexanol", "DistW"),
                ChemistryRecipes.amounts(1000, 2000, 1000),
                ChemistryRecipes.fields("GenLiquid_DiethylhexylPhosphoricAcid", "GenGas_HydrochloricAcid"),
                ChemistryRecipes.amounts(1000, 3000),
                200, 48);
    }

    private static void amineExtractants() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c13/octanol",
                ChemistryRecipes.items(ChemistryRecipes.dust("Aluminium", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("Ethylene", "Oxygen", "DistW"), ChemistryRecipes.amounts(4000, 500, 1000),
                ChemistryRecipes.fields("GenLiquid_Octanol"), ChemistryRecipes.amounts(1000),
                240, 64, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c13/trioctylamine",
                ChemistryRecipes.items(ChemistryRecipes.dust("Nickel", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Octanol", "GenGas_Ammonia"), ChemistryRecipes.amounts(3000, 1000),
                ChemistryRecipes.fields("GenLiquid_Trioctylamine", "DistW"), ChemistryRecipes.amounts(1000, 3000),
                240, 64, null, new int[]{0});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c13/aliquat",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_Trioctylamine", "GenGas_Chloromethane"),
                ChemistryRecipes.amounts(1000, 1000),
                ChemistryRecipes.fields("GenLiquid_Aliquat"), ChemistryRecipes.amounts(1000),
                160, 48);
    }

    private static void lubricants() {
        ChemistryRecipes.recipe("gt.recipe.mixer", "c13/alpha-olefins",
                ChemistryRecipes.items(ChemistryRecipes.circuit(1), ChemistryRecipes.dust("Aluminium", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("Ethylene"), ChemistryRecipes.amounts(4000),
                ChemistryRecipes.fields("GenLiquid_AlphaOlefins"), ChemistryRecipes.amounts(1000),
                240, 64, null, new int[]{0, 1});
        ChemistryRecipes.recipe("gt.recipe.distillationtower", "c13/alpha-olefins-distill",
                ChemistryRecipes.items(), ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_AlphaOlefins"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenGas_OneButene", "GenLiquid_OneOctene", "GenLiquid_OneDecene", "Fuel"),
                ChemistryRecipes.amounts(250, 300, 250, 200),
                160, 64);
        ChemistryRecipes.recipe("gt.recipe.mixer", "c13/pao",
                ChemistryRecipes.items(ChemistryRecipes.dust("SolidPhosphoricAcid", 1),
                        ChemistryRecipes.dust("Nickel", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenLiquid_OneDecene", "Hydrogen"), ChemistryRecipes.amounts(3000, 1000),
                ChemistryRecipes.fields("GenLiquid_Polyalphaolefin"), ChemistryRecipes.amounts(1000),
                240, 64, null, new int[]{0, 1});
        ChemistryRecipes.recipe("gt.recipe.mixer", "c13/polyisobutylene",
                ChemistryRecipes.items(ChemistryRecipes.dust("FerricChloride", 1)),
                ChemistryRecipes.items(),
                ChemistryRecipes.fields("GenGas_Isobutylene"), ChemistryRecipes.amounts(1000),
                ChemistryRecipes.fields("GenLiquid_Polyisobutylene"), ChemistryRecipes.amounts(1000),
                200, 48, null, new int[]{0});
    }
}
