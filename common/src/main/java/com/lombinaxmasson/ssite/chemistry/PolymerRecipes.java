package com.lombinaxmasson.ssite.chemistry;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.amounts;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.circuit;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.dust;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.fields;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.item;
import static com.lombinaxmasson.ssite.chemistry.ChemistryRecipes.row;

/** M4 polymer recipes. Each M4 card adds one method; {@link #add()} wires them all. */
public final class PolymerRecipes {
    /*
     * Conventions shared by every M4 card:
     * 1. Programmed circuits go in keptInputs and are never consumed.
     * 2. When one machine hosts a recipe whose inputs are a subset of another, tell them apart by a different circuit number.
     * 3. Scale follows M3: roughly 1000 mB of monomer yields one dust of polymer.
     * 4. dustTiny catalysts and initiators stay in keptInputs; stoichiometric reagents are consumed.
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
        deferredFill();
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
        row("gt.recipe.catalyticcracking", "c1/methane-reforming")
                .in(dust("Nickel", 1))
                .fluidIn("Methane", 1000, "Steam", 1000)
                .fluidOut("GenGas_CarbonMonoxide", 1000, "Hydrogen", 3000)
                .keep(0)
                .add(200, 64);
    }

    private static void methanol() {
        row("gt.recipe.mixer", "c1/methanol")
                .in(dust("Copper", 1), dust("Zinc", 1))
                .fluidIn("GenGas_CarbonMonoxide", 1000, "Hydrogen", 2000)
                .fluidOut("GenLiquid_Methanol", 1000)
                .keep(0, 1)
                .add(200, 64);
    }

    private static void formaldehyde() {
        row("gt.recipe.mixer", "c1/formaldehyde")
                .in(dust("Silver", 1))
                .fluidIn("GenLiquid_Methanol", 1000, "Oxygen", 500)
                .fluidOut("GenGas_Formaldehyde", 1000, "DistW", 1000)
                .keep(0)
                .add(160, 48);
    }

    private static void aceticAcid() {
        row("gt.recipe.mixer", "c1/acetic-acid-monsanto")
                .in(dust("Rhodium", 1))
                .fluidIn("GenLiquid_Methanol", 1000, "GenGas_CarbonMonoxide", 1000, "GenGas_Iodine", 10)
                .fluidOut("GenLiquid_AceticAcid", 1000)
                .keep(0)
                .add(160, 64);
        row("gt.recipe.mixer", "c1/acetic-acid-acetaldehyde")
                .in(dust("Manganese", 1))
                .fluidIn("GenLiquid_Acetaldehyde", 1000, "Oxygen", 500)
                .fluidOut("GenLiquid_AceticAcid", 1000)
                .keep(0)
                .add(160, 32);
        for (String vinegar : new String[]{"Vinegar_Grape", "Vinegar_Apple", "Vinegar_Cane", "Vinegar_Rice"}) {
            row("gt.recipe.distillery", "c1/acetic-acid-" + vinegar.toLowerCase())
                    .in(circuit(3))
                    .fluidIn(vinegar, 1000)
                    .fluidOut("GenLiquid_AceticAcid", 50, "DistW", 950)
                    .keep(0)
                    .add(100, 16);
        }
    }

    private static void ethyleneOxide() {
        row("gt.recipe.mixer", "c1/ethylene-oxide")
                .in(dust("Silver", 1))
                .fluidIn("Ethylene", 1000, "Oxygen", 500)
                .fluidOut("GenGas_EthyleneOxide", 1000)
                .keep(0)
                .add(160, 48);
    }

    private static void ethyleneGlycol() {
        row("gt.recipe.mixer", "c1/ethylene-glycol")
                .fluidIn("GenGas_EthyleneOxide", 1000, "DistW", 1000)
                .fluidOut("GenLiquid_EthyleneGlycol", 1000)
                .add(120, 32);
    }

    private static void dichloroethane() {
        row("gt.recipe.mixer", "c1/dichloroethane-direct")
                .in(dust("FerricChloride", 1))
                .fluidIn("Ethylene", 1000, "GenGas_Chlorine", 1000)
                .fluidOut("GenLiquid_Dichloroethane", 1000)
                .keep(0)
                .add(120, 32);
        row("gt.recipe.mixer", "c1/dichloroethane-oxy")
                .in(dust("Copper", 1))
                .fluidIn("Ethylene", 1000, "GenGas_HydrochloricAcid", 2000, "Oxygen", 500)
                .fluidOut("GenLiquid_Dichloroethane", 1000, "DistW", 1000)
                .keep(0)
                .add(160, 48);
    }

    private static void vinylChloride() {
        row("gt.recipe.calciner", "c1/vinyl-chloride")
                .in(circuit(1))
                .fluidIn("GenLiquid_Dichloroethane", 1000)
                .fluidOut("GenGas_VinylChloride", 1000, "GenGas_HydrochloricAcid", 1000)
                .keep(0)
                .add(160, 48);
    }

    private static void vinylAcetate() {
        row("gt.recipe.mixer", "c1/vinyl-acetate")
                .in(dust("Palladium", 1))
                .fluidIn("Ethylene", 1000, "GenLiquid_AceticAcid", 1000, "Oxygen", 500)
                .fluidOut("GenLiquid_VinylAcetate", 1000, "DistW", 1000)
                .keep(0)
                .add(200, 64);
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
        row("gt.recipe.calciner", "c3/allyl-chloride")
                .fluidIn("Propylene", 1000, "GenGas_Chlorine", 1000)
                .fluidOut("GenLiquid_AllylChloride", 1000, "GenGas_HydrochloricAcid", 1000)
                .add(160, 48);
        row("gt.recipe.mixer", "c3/dichloropropanol-hypo")
                .fluidIn("GenLiquid_AllylChloride", 1000, "GenGas_Chlorine", 1000, "DistW", 1000)
                .fluidOut("GenLiquid_Dichloropropanol", 1000, "GenGas_HydrochloricAcid", 1000)
                .add(120, 32);
        row("gt.recipe.mixer", "c3/dichloropropanol-glycerol")
                .fluidIn("GenLiquid_Glycerol", 1000, "GenGas_HydrochloricAcid", 2000, "GenLiquid_AceticAcid", 50)
                .fluidOut("GenLiquid_Dichloropropanol", 1000, "DistW", 2000)
                .add(160, 32);
        row("gt.recipe.bath", "c3/epichlorohydrin")
                .in(dust("SodiumHydroxide", 1))
                .out(dust("Salt", 1))
                .fluidIn("GenLiquid_Dichloropropanol", 1000)
                .fluidOut("GenLiquid_Epichlorohydrin", 1000, "DistW", 1000)
                .add(120, 32);
    }

    private static void hydrogenCyanideAcrylonitrile() {
        row("gt.recipe.mixer", "c3/hydrogen-cyanide")
                .in(dust("Platinum", 1))
                .fluidIn("Methane", 1000, "GenGas_Ammonia", 1000, "Oxygen", 1500)
                .fluidOut("GenLiquid_HydrogenCyanide", 1000, "DistW", 3000)
                .keep(0)
                .add(160, 64);
        row("gt.recipe.mixer", "c3/acrylonitrile")
                .in(dust("Bismuth", 1), dust("Molybdenum", 1))
                .fluidIn("Propylene", 1000, "GenGas_Ammonia", 1000, "Oxygen", 1500)
                .fluidOut("GenLiquid_Acrylonitrile", 1000, "GenLiquid_HydrogenCyanide", 150)
                .keep(0, 1)
                .add(200, 64);
    }

    private static void acroleinAcrylicAcid() {
        row("gt.recipe.mixer", "c3/acrolein")
                .in(dust("Bismuth", 1), dust("Cobalt", 1))
                .fluidIn("Propylene", 1000, "Oxygen", 1000)
                .fluidOut("GenLiquid_Acrolein", 1000, "DistW", 1000)
                .keep(0, 1)
                .add(160, 48);
        row("gt.recipe.mixer", "c3/acrylic-acid")
                .in(dust("Molybdenum", 1), dust("Vanadium", 1))
                .fluidIn("GenLiquid_Acrolein", 1000, "Oxygen", 500)
                .fluidOut("GenLiquid_AcrylicAcid", 1000)
                .keep(0, 1)
                .add(160, 48);
    }

    private static void methylMethacrylate() {
        row("gt.recipe.mixer", "c3/acetone-cyanohydrin")
                .in(item("dustTiny", "SodiumHydroxide", 1))
                .fluidIn("GenLiquid_Acetone", 1000, "GenLiquid_HydrogenCyanide", 1000)
                .fluidOut("GenLiquid_AcetoneCyanohydrin", 1000)
                .keep(0)
                .add(120, 32);
        row("gt.recipe.mixer", "c3/methyl-methacrylate")
                .fluidIn("GenLiquid_AcetoneCyanohydrin", 1000, "GenLiquid_Methanol", 1000, "GenLiquid_SulfuricAcid", 1000)
                .fluidOut("GenLiquid_MethylMethacrylate", 1000, "Dirty_Water", 1000)
                .add(200, 48);
    }

    private static void oxoAlcohols() {
        row("gt.recipe.mixer", "c3/butyraldehyde")
                .in(dust("Cobalt", 1))
                .fluidIn("Propylene", 1000, "GenGas_CarbonMonoxide", 1000, "Hydrogen", 1000)
                .fluidOut("GenLiquid_Butyraldehyde", 1000)
                .keep(0)
                .add(200, 64);
        row("gt.recipe.mixer", "c3/butanol")
                .in(circuit(1), dust("Nickel", 1))
                .fluidIn("GenLiquid_Butyraldehyde", 1000, "Hydrogen", 1000)
                .fluidOut("GenLiquid_Butanol", 1000)
                .keep(0, 1)
                .add(120, 32);
        row("gt.recipe.mixer", "c3/two-ethylhexanol")
                .in(circuit(2), dust("Nickel", 1), item("dustTiny", "SodiumHydroxide", 1))
                .fluidIn("GenLiquid_Butyraldehyde", 2000, "Hydrogen", 2000)
                .fluidOut("GenLiquid_TwoEthylhexanol", 1000, "DistW", 1000)
                .keep(0, 1, 2)
                .add(200, 48);
    }

    private static void prinsIsoprene() {
        row("gt.recipe.calciner", "c3/solid-phosphoric-acid")
                .in(dust("Phosphorus", 1), dust("SiliconDioxide", 2))
                .out(dust("SolidPhosphoricAcid", 3))
                .fluidIn("Oxygen", 2500, "DistW", 1500)
                .add(200, 48);
        row("gt.recipe.mixer", "c3/dimethyldioxane")
                .fluidIn("GenGas_Isobutylene", 1000, "GenGas_Formaldehyde", 2000, "GenLiquid_SulfuricAcid", 50)
                .fluidOut("GenLiquid_Dimethyldioxane", 1000)
                .add(160, 32);
        row("gt.recipe.catalyticcracking", "c3/isoprene-prins")
                .in(dust("SolidPhosphoricAcid", 1))
                .fluidIn("GenLiquid_Dimethyldioxane", 1000)
                .fluidOut("GenLiquid_Isoprene", 1000, "GenGas_Formaldehyde", 1000, "DistW", 1000)
                .keep(0)
                .add(160, 48);
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
        row("gt.recipe.mixer", "c3/ldpe")
                .in(circuit(1))
                .out(dust("Plastic", 1))
                .fluidIn("Ethylene", 1000, "Oxygen", 5)
                .keep(0)
                .add(200, 64);
    }

    private static void uhmwpe() {
        row("gt.recipe.mixer", "c3/uhmwpe")
                .in(circuit(2), dust("Zirconium", 1), dust("Aluminium", 1))
                .out(dust("UltraHighMolecularWeightPolyethylene", 1))
                .fluidIn("Ethylene", 1000)
                .keep(0, 1, 2)
                .add(300, 96);
    }

    private static void polypropylene() {
        row("gt.recipe.mixer", "c3/polypropylene")
                .in(dust("Aluminium", 1))
                .out(dust("Polypropylene", 1))
                .fluidIn("GenLiquid_TitaniumTetrachloride", 10, "Propylene", 1000)
                .keep(0)
                .add(200, 64);
    }

    private static void pvc() {
        row("gt.recipe.mixer", "c3/pvc")
                .out(dust("PVC", 1))
                .fluidIn("GenGas_VinylChloride", 1000, "GenLiquid_DeionizedWater", 1000, "GenLiquid_CumeneHydroperoxide", 10)
                .fluidOut("DistW", 1000)
                .add(200, 48);
    }

    private static void pvac() {
        row("gt.recipe.mixer", "c3/pvac")
                .in(circuit(1))
                .out(dust("PolyvinylAcetate", 1))
                .fluidIn("GenLiquid_VinylAcetate", 1000, "GenLiquid_CumeneHydroperoxide", 10)
                .keep(0)
                .add(160, 32);
    }

    private static void eva() {
        row("gt.recipe.mixer", "c3/eva")
                .in(circuit(2))
                .out(dust("EthyleneVinylAcetate", 4))
                .fluidIn("Ethylene", 3000, "GenLiquid_VinylAcetate", 1000, "GenLiquid_CumeneHydroperoxide", 10)
                .keep(0)
                .add(300, 48);
    }

    private static void pva() {
        row("gt.recipe.bath", "c3/pva")
                .in(dust("PolyvinylAcetate", 1), item("dustTiny", "SodiumHydroxide", 1))
                .out(dust("PolyvinylAlcohol", 1))
                .fluidIn("BioEthanol", 1000)
                .fluidOut("GenLiquid_EthylAcetate", 1000)
                .add(160, 32);
        if (FluidBridge.distinct("GenLiquid_Ethanol", "BioEthanol")) {
            row("gt.recipe.bath", "c3/pva-pure")
                    .in(dust("PolyvinylAcetate", 1), item("dustTiny", "SodiumHydroxide", 1))
                    .out(dust("PolyvinylAlcohol", 1))
                    .fluidIn("GenLiquid_Ethanol", 1000)
                    .fluidOut("GenLiquid_EthylAcetate", 1000)
                    .add(160, 32);
        }
    }

    private static void pvb() {
        row("gt.recipe.bath", "c3/pvb")
                .in(dust("PolyvinylAlcohol", 2))
                .out(dust("PolyvinylButyral", 2))
                .fluidIn("GenLiquid_Butyraldehyde", 1000)
                .fluidOut("DistW", 1000)
                .add(200, 32);
    }

    /** Press/rollingmill (and wiremill for {@code wires}) shaping shared by every M4 polymer. */
    private static void polymerForming(String material, boolean wires) {
        row("gt.recipe.press", "forming/" + material.toLowerCase() + "/plate")
                .in(dust(material, 1), circuit(1))
                .out(item("plate", material, 1))
                .keep(1)
                .add(100, 16);
        row("gt.recipe.rollingmill", "forming/" + material.toLowerCase() + "/foil")
                .in(item("plate", material, 1))
                .out(item("foil", material, 4))
                .add(100, 16);
        if (wires) {
            row("gt.recipe.wiremill", "forming/" + material.toLowerCase() + "/wire")
                    .in(item("plate", material, 1))
                    .out(item("wireFine", material, 4))
                    .add(100, 16);
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
        row("gt.recipe.catalyticcracking", "c4/styrene")
                .in(dust("Hematite", 1))
                .fluidIn("GenLiquid_Ethylbenzene", 1000, "Steam", 2000)
                .fluidOut("GenLiquid_Styrene", 1000, "Hydrogen", 1000, "DistW", 2000)
                .keep(0)
                .add(200, 64);
    }

    private static void divinylbenzene() {
        row("gt.recipe.mixer", "c4/diethylbenzene")
                .in(dust("ZSM5", 1))
                .fluidIn("GenLiquid_Ethylbenzene", 1000, "Ethylene", 1000)
                .fluidOut("GenLiquid_Diethylbenzene", 1000)
                .keep(0)
                .add(160, 32);
        row("gt.recipe.catalyticcracking", "c4/divinylbenzene")
                .in(dust("Hematite", 1))
                .fluidIn("GenLiquid_Diethylbenzene", 1000, "Steam", 2000)
                .fluidOut("GenLiquid_Divinylbenzene", 1000, "Hydrogen", 2000, "DistW", 2000)
                .keep(0)
                .add(240, 64);
    }

    private static void polystyrene() {
        row("gt.recipe.mixer", "c4/polystyrene")
                .in(circuit(1))
                .out(dust("Polystyrene", 1))
                .fluidIn("GenLiquid_Styrene", 1000, "GenLiquid_CumeneHydroperoxide", 10)
                .keep(0)
                .add(160, 32);
    }

    private static void crosslinkedBeads() {
        row("gt.recipe.mixer", "c4/crosslinked-polystyrene")
                .in(circuit(2))
                .out(dust("CrosslinkedPolystyrene", 1))
                .fluidIn(fields("GenLiquid_Styrene", "GenLiquid_Divinylbenzene", "GenLiquid_DeionizedWater",
                        "GenLiquid_CumeneHydroperoxide"), amounts(900, 100, 1000, 10))
                .fluidOut("DistW", 1000)
                .keep(0)
                .add(200, 48);
    }

    private static void cationResin() {
        row("gt.recipe.bath", "c4/cation-resin")
                .in(dust("CrosslinkedPolystyrene", 1))
                .out(dust("CationExchangeResin", 1))
                .fluidIn("GenLiquid_SulfuricAcid", 1000)
                .fluidOut("DistW", 1000)
                .add(200, 32);
    }

    private static void anionResin() {
        row("gt.recipe.mixer", "c4/chloromethylated-polystyrene")
                .in(dust("CrosslinkedPolystyrene", 1))
                .out(dust("ChloromethylatedPolystyrene", 1))
                .fluidIn("GenGas_Formaldehyde", 1000, "GenGas_HydrochloricAcid", 1000, "GenLiquid_Methanol", 100)
                .fluidOut("DistW", 1000)
                .add(200, 48);
        row("gt.recipe.mixer", "c4/anion-resin")
                .in(dust("ChloromethylatedPolystyrene", 1))
                .out(dust("AnionExchangeResin", 1))
                .fluidIn("GenLiquid_Methanol", 3000, "GenGas_Ammonia", 1000)
                .fluidOut("DistW", 3000)
                .add(200, 48);
    }

    private static void resinDeionizedWater() {
        row("gt.recipe.bath", "c4/resin-deionized-water")
                .in(dust("CationExchangeResin", 1), dust("AnionExchangeResin", 1))
                .fluidIn("GenLiquid_DemineralizedWater", 4000)
                .fluidOut("GenLiquid_DeionizedWater", 3950, "Saltwater", 50)
                .keep(0, 1)
                .add(120, 30);
    }

    private static void resinBisphenolA() {
        row("gt.recipe.mixer", "c4/resin-bisphenol-a")
                .in(dust("CationExchangeResin", 1))
                .out(dust("BisphenolA", 1))
                .fluidIn("GenLiquid_Phenol", 2000, "GenLiquid_Acetone", 1000)
                .fluidOut("DistW", 1000)
                .keep(0)
                .add(200, 48);
    }

    private static void pmma() {
        row("gt.recipe.mixer", "c4/pmma")
                .out(dust("PolymethylMethacrylate", 1))
                .fluidIn("GenLiquid_MethylMethacrylate", 1000, "GenLiquid_CumeneHydroperoxide", 10)
                .add(160, 32);
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
        row("gt.recipe.mixer", "c5/phthalic-anhydride")
                .in(dust("VanadiumPentoxide", 1))
                .out(dust("PhthalicAnhydride", 1))
                .fluidIn("GenLiquid_OrthoXylene", 1000, "Oxygen", 3000)
                .fluidOut("DistW", 3000)
                .keep(0)
                .add(200, 64);
    }

    private static void terephthalicAcid() {
        row("gt.recipe.mixer", "c5/terephthalic-acid")
                .in(dust("Cobalt", 1), dust("Manganese", 1), dust("ParaXylene", 1))
                .out(dust("TerephthalicAcid", 1))
                .fluidIn("GenLiquid_AceticAcid", 100, "GenGas_Bromine", 10, "Oxygen", 3000)
                .fluidOut("DistW", 2000)
                .keep(0, 1)
                .add(300, 96);
    }

    private static void isophthalicAcid() {
        row("gt.recipe.mixer", "c5/isophthalic-acid")
                .in(dust("Cobalt", 1), dust("Manganese", 1))
                .out(dust("IsophthalicAcid", 1))
                .fluidIn(fields("GenLiquid_MetaXylene", "GenLiquid_AceticAcid", "GenGas_Bromine", "Oxygen"), amounts(1000, 100, 10, 3000))
                .fluidOut("DistW", 2000)
                .keep(0, 1)
                .add(300, 96);
    }

    private static void phosgene() {
        row("gt.recipe.mixer", "c5/phosgene")
                .in(dust("Charcoal", 1))
                .fluidIn("GenGas_CarbonMonoxide", 1000, "GenGas_Chlorine", 1000)
                .fluidOut("GenGas_Phosgene", 1000)
                .keep(0)
                .add(120, 48);
    }

    private static void pet() {
        row("gt.recipe.mixer", "c5/pet")
                .in(dust("Antimony", 1), dust("TerephthalicAcid", 1))
                .out(dust("PolyethyleneTerephthalate", 1))
                .fluidIn("GenLiquid_EthyleneGlycol", 1000)
                .fluidOut("DistW", 2000)
                .keep(0)
                .add(240, 64);
    }

    private static void polycarbonate() {
        row("gt.recipe.mixer", "c5/polycarbonate")
                .in(dust("BisphenolA", 1), dust("SodiumHydroxide", 2))
                .out(dust("Polycarbonate", 1))
                .fluidIn("GenGas_Phosgene", 1000)
                .fluidOut("Saltwater", 2000)
                .add(240, 64);
    }

    private static void metallizedPolyester() {
        row("gt.recipe.laminator", "c5/metallized-polyester")
                .in(item("foil", "PolyethyleneTerephthalate", 1), item("foil", "Aluminium", 1))
                .out(item("foil", "MetallizedPolyester", 1))
                .add(100, 32);
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
        row("gt.recipe.rollingmill", "forming/fiberreinforcedepoxy/foil")
                .in(item("plate", "FiberReinforcedEpoxy", 1))
                .out(item("foil", "FiberReinforcedEpoxy", 4))
                .add(100, 16);
        row("gt.recipe.rollingmill", "forming/flameretardantlaminate/foil")
                .in(item("plate", "FlameRetardantLaminate", 1))
                .out(item("foil", "FlameRetardantLaminate", 4))
                .add(100, 16);
    }

    private static void phenolicResin() {
        row("gt.recipe.mixer", "c6/phenolic-resin")
                .in(item("dustTiny", "SodiumHydroxide", 1))
                .fluidIn("GenLiquid_Phenol", 1000, "GenGas_Formaldehyde", 1000)
                .fluidOut("GenLiquid_PhenolicResin", 1000)
                .keep(0)
                .add(160, 32);
        row("gt.recipe.coagulator", "c6/bakelite")
                .out(dust("Bakelite", 1))
                .fluidIn("GenLiquid_PhenolicResin", 1000)
                .add(200, 16);
        row("gt.recipe.bath", "c6/bakelite-paper")
                .in(item("plateTiny", "Paper", 1))
                .out(item("plate", "Bakelite", 1))
                .fluidIn("GenLiquid_PhenolicResin", 250)
                .add(100, 16);
    }

    private static void glassFiber() {
        row("gt.recipe.calciner", "c6/glass-fiber")
                .in(dust("Glass", 4), dust("Borax", 1), dust("Quicklime", 1))
                .out(dust("GlassFiber", 6))
                .add(300, 64);
        row("gt.recipe.wiremill", "c6/glass-fiber-wire")
                .in(dust("GlassFiber", 1))
                .out(item("wireFine", "GlassFiber", 4))
                .add(100, 32);
    }

    private static void dgebaEpoxy() {
        row("gt.recipe.mixer", "c6/epoxy-resin")
                .in(dust("BisphenolA", 1), dust("SodiumHydroxide", 2))
                .fluidIn("GenLiquid_Epichlorohydrin", 2000)
                .fluidOut("GenLiquid_EpoxyResin", 1000, "Saltwater", 2000)
                .add(200, 48);
        row("gt.recipe.mixer", "c6/epoxy-cure")
                .in(dust("PhthalicAnhydride", 1))
                .out(dust("Epoxy", 1))
                .fluidIn("GenLiquid_EpoxyResin", 1000)
                .add(160, 32);
        row("gt.recipe.bath", "c6/epoxy-glass-fiber")
                .in(item("wireFine", "GlassFiber", 8), dust("PhthalicAnhydride", 1))
                .out(item("plate", "FiberReinforcedEpoxy", 2))
                .fluidIn("GenLiquid_EpoxyResin", 1000)
                .add(200, 48);
    }

    private static void brominatedEpoxy() {
        row("gt.recipe.mixer", "c6/tetrabromobisphenol-a")
                .in(dust("BisphenolA", 1))
                .out(dust("TetrabromobisphenolA", 1))
                .fluidIn("GenGas_Bromine", 2000, "GenGas_HydrogenPeroxide", 2000)
                .fluidOut("DistW", 4000)
                .add(200, 48);
        row("gt.recipe.mixer", "c6/flame-retardant-epoxy-resin")
                .in(dust("TetrabromobisphenolA", 1), dust("SodiumHydroxide", 2))
                .fluidIn("GenLiquid_Epichlorohydrin", 2000)
                .fluidOut("GenLiquid_FlameRetardantEpoxyResin", 1000, "Saltwater", 2000)
                .add(200, 48);
        row("gt.recipe.bath", "c6/flame-retardant-laminate")
                .in(item("wireFine", "GlassFiber", 8), dust("PhthalicAnhydride", 1))
                .out(item("plate", "FlameRetardantLaminate", 2))
                .fluidIn("GenLiquid_FlameRetardantEpoxyResin", 1000)
                .add(200, 64);
    }

    private static void cresolNovolac() {
        row("gt.recipe.distillery", "c6/cresol")
                .in(circuit(4))
                .fluidIn("Oil_Creosote", 1000)
                .fluidOut("GenLiquid_Cresol", 300, "GenLiquid_Phenol", 200)
                .keep(0)
                .add(120, 32);
        row("gt.recipe.mixer", "c6/epoxy-cresol-novolac")
                .in(dust("SodiumHydroxide", 2))
                .out(dust("EpoxyCresolNovolac", 1))
                .fluidIn("GenLiquid_Cresol", 1000, "GenGas_Formaldehyde", 1000, "GenLiquid_Epichlorohydrin", 2000)
                .fluidOut("Saltwater", 2000)
                .add(240, 64);
    }

    private static void cycloaliphaticEpoxy() {
        row("gt.recipe.mixer", "c6/peracetic-acid")
                .fluidIn("GenLiquid_AceticAcid", 1000, "GenGas_HydrogenPeroxide", 1000, "GenLiquid_SulfuricAcid", 10)
                .fluidOut("GenLiquid_PeraceticAcid", 1000, "DistW", 1000)
                .add(120, 32);
        row("gt.recipe.mixer", "c6/tetrahydrobenzaldehyde")
                .fluidIn("GenGas_Butadiene", 1000, "GenLiquid_Acrolein", 1000)
                .fluidOut("GenLiquid_Tetrahydrobenzaldehyde", 1000)
                .add(160, 32);
        row("gt.recipe.mixer", "c6/cycloaliphatic-epoxy")
                .in(dust("Aluminium", 1))
                .fluidIn("GenLiquid_Tetrahydrobenzaldehyde", 2000, "GenLiquid_PeraceticAcid", 2000)
                .fluidOut("GenLiquid_CycloaliphaticEpoxy", 1000, "GenLiquid_AceticAcid", 2000)
                .keep(0)
                .add(240, 64);
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
        row("gt.recipe.mixer", "c7/nitrobenzene")
                .fluidIn("GenLiquid_Benzene", 1000, "GenLiquid_NitricAcid", 1000, "GenLiquid_SulfuricAcid", 100)
                .fluidOut("GenLiquid_Nitrobenzene", 1000, "DistW", 1000)
                .add(160, 48);
        row("gt.recipe.mixer", "c7/aniline")
                .in(dust("Nickel", 1))
                .fluidIn("GenLiquid_Nitrobenzene", 1000, "Hydrogen", 3000)
                .fluidOut("GenLiquid_Aniline", 1000, "DistW", 2000)
                .keep(0)
                .add(160, 48);
    }

    private static void mdi() {
        row("gt.recipe.mixer", "c7/methylenedianiline")
                .out(dust("Methylenedianiline", 1))
                .fluidIn("GenLiquid_Aniline", 2000, "GenGas_Formaldehyde", 1000, "GenGas_HydrochloricAcid", 100)
                .fluidOut("DistW", 1000)
                .add(200, 48);
        row("gt.recipe.mixer", "c7/mdi")
                .in(dust("Methylenedianiline", 1))
                .fluidIn("GenGas_Phosgene", 2000)
                .fluidOut("GenLiquid_MethyleneDiphenylDiisocyanate", 1000, "GenGas_HydrochloricAcid", 4000)
                .add(200, 64);
    }

    private static void polyetherPolyol() {
        row("gt.recipe.mixer", "c7/polyether-polyol")
                .in(item("dustTiny", "PotassiumHydroxide", 1))
                .fluidIn("GenLiquid_Glycerol", 1000, "GenGas_EthyleneOxide", 3000)
                .fluidOut("GenLiquid_PolyetherPolyol", 1000)
                .keep(0)
                .add(200, 48);
    }

    private static void polyurethane() {
        row("gt.recipe.mixer", "c7/polyurethane-polyol")
                .in(circuit(1))
                .out(dust("Polyurethane", 2))
                .fluidIn("GenLiquid_MethyleneDiphenylDiisocyanate", 1000, "GenLiquid_PolyetherPolyol", 1000)
                .keep(0)
                .add(200, 48);
        row("gt.recipe.mixer", "c7/polyurethane-glycol")
                .in(circuit(2))
                .out(dust("Polyurethane", 1))
                .fluidIn("GenLiquid_MethyleneDiphenylDiisocyanate", 1000, "GenLiquid_EthyleneGlycol", 1000)
                .keep(0)
                .add(200, 48);
    }

    private static void sponge() {
        row("gt.recipe.mixer", "c7/sponge")
                .in(new ItemStack(Items.YELLOW_DYE, 1))
                .out(new ItemStack(Items.SPONGE, 1))
                .fluidIn("GenLiquid_MethyleneDiphenylDiisocyanate", 250, "GenLiquid_PolyetherPolyol", 250, "CarbonDioxide", 500)
                .add(100, 32);
    }

    private static void petroleumResin() {
        row("gt.recipe.calciner", "c7/petroleum-resin")
                .in(dust("Norbornene", 1))
                .out(dust("PetroleumResin", 3))
                .fluidIn("GenLiquid_Dicyclopentadiene", 1000, "GenLiquid_Benzene", 1000)
                .add(240, 48);
        row("gt.recipe.mixer", "c7/hydrogenated-petroleum-resin")
                .in(dust("Palladium", 1), dust("PetroleumResin", 1))
                .out(dust("HydrogenatedPetroleumResin", 1))
                .fluidIn("Hydrogen", 1000)
                .keep(0)
                .add(160, 48);
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
        row("gt.recipe.calciner", "c8/carbon-black-furnace")
                .out(dust("CarbonBlack", 2))
                .fluidIn("Fuel", 1000, "Oxygen", 500)
                .fluidOut("CarbonDioxide", 500)
                .add(160, 48);
        row("gt.recipe.calciner", "c8/carbon-black-thermal")
                .in(circuit(1))
                .out(dust("CarbonBlack", 1))
                .fluidIn("Methane", 1000)
                .fluidOut("Hydrogen", 2000)
                .keep(0)
                .add(160, 48);
    }

    private static void zincOxide() {
        row("gt.recipe.roaster", "c8/zinc-oxide")
                .in(dust("Zinc", 1))
                .out(dust("ZincOxide", 1))
                .fluidIn("Oxygen", 1000)
                .add(120, 32);
    }

    private static void magnesiumOxide() {
        row("gt.recipe.calciner", "c8/magnesium-oxide")
                .in(circuit(1), dust("MagnesiumHydroxide", 1))
                .out(dust("MagnesiumOxide", 1))
                .fluidOut("Steam", 1000)
                .keep(0)
                .add(120, 32);
    }

    private static void rawRubbers() {
        row("gt.recipe.mixer", "c8/raw-polyisoprene")
                .in(dust("Aluminium", 1))
                .out(dust("RawPolyisoprene", 1))
                .fluidIn("GenLiquid_TitaniumTetrachloride", 10, "GenLiquid_Isoprene", 1000)
                .keep(0)
                .add(200, 64);
        row("gt.recipe.mixer", "c8/raw-sbr")
                .out(dust("RawStyreneButadieneRubber", 1))
                .fluidIn(fields("GenGas_Butadiene", "GenLiquid_Styrene", "GenLiquid_DeionizedWater",
                        "GenLiquid_CumeneHydroperoxide"), amounts(750, 250, 1000, 10))
                .fluidOut("DistW", 1000)
                .add(200, 48);
        row("gt.recipe.mixer", "c8/raw-nbr")
                .out(dust("RawNitrileRubber", 1))
                .fluidIn(fields("GenGas_Butadiene", "GenLiquid_Acrylonitrile", "GenLiquid_DeionizedWater",
                        "GenLiquid_CumeneHydroperoxide"), amounts(700, 300, 1000, 10))
                .fluidOut("DistW", 1000)
                .add(200, 48);
        row("gt.recipe.mixer", "c8/dichlorobutene")
                .fluidIn("GenGas_Butadiene", 1000, "GenGas_Chlorine", 1000)
                .fluidOut("GenLiquid_Dichlorobutene", 1000)
                .add(120, 32);
        row("gt.recipe.bath", "c8/chloroprene")
                .in(dust("SodiumHydroxide", 1))
                .out(dust("Salt", 1))
                .fluidIn("GenLiquid_Dichlorobutene", 1000)
                .fluidOut("GenLiquid_Chloroprene", 1000, "DistW", 1000)
                .add(120, 32);
        row("gt.recipe.mixer", "c8/raw-neoprene")
                .out(dust("RawNeoprene", 1))
                .fluidIn("GenLiquid_Chloroprene", 1000, "GenLiquid_DeionizedWater", 1000, "GenLiquid_CumeneHydroperoxide", 10)
                .fluidOut("DistW", 1000)
                .add(200, 48);
    }

    private static void silicones() {
        row("gt.recipe.mixer", "c8/chloromethane")
                .in(circuit(1))
                .fluidIn("GenLiquid_Methanol", 1000, "GenGas_HydrochloricAcid", 1000)
                .fluidOut("GenGas_Chloromethane", 1000, "DistW", 1000)
                .keep(0)
                .add(120, 32);
        row("gt.recipe.mixer", "c8/dimethyldichlorosilane")
                .in(dust("Copper", 1), dust("Silicon", 1))
                .fluidIn("GenGas_Chloromethane", 2000)
                .fluidOut("GenLiquid_Dimethyldichlorosilane", 1000)
                .keep(0)
                .add(200, 64);
        row("gt.recipe.mixer", "c8/polydimethylsiloxane")
                .fluidIn("GenLiquid_Dimethyldichlorosilane", 1000, "DistW", 1000)
                .fluidOut("GenLiquid_Polydimethylsiloxane", 1000, "GenGas_HydrochloricAcid", 2000)
                .add(160, 32);
    }

    private static void vulcanize() {
        row("gt.recipe.mixer", "c8/vulcanize-polyisoprene")
                .in(dust("RawPolyisoprene", 1), item("dustSmall", "CarbonBlack", 1), item("dustTiny", "Sulfur", 1), dust("ZincOxide", 1))
                .out(item("ingot", "Rubber", 1))
                .keep(3)
                .add(200, 48);
        vulcanizeStandard("RawStyreneButadieneRubber", "StyreneButadieneRubber");
        vulcanizeStandard("RawNitrileRubber", "NitrileRubber");
        row("gt.recipe.mixer", "c8/vulcanize-neoprene")
                .in(dust("RawNeoprene", 1), item("dustSmall", "CarbonBlack", 1), dust("MagnesiumOxide", 1), dust("ZincOxide", 1))
                .out(item("ingot", "Neoprene", 1))
                .keep(2, 3)
                .add(200, 48);
        row("gt.recipe.mixer", "c8/vulcanize-silicone")
                .in(dust("SiliconDioxide", 1))
                .out(item("ingot", "SiliconeRubber", 1))
                .fluidIn("GenLiquid_Polydimethylsiloxane", 1000, "GenLiquid_CumeneHydroperoxide", 10)
                .add(200, 48);
        row("gt.recipe.mixer", "c8/ebonite")
                .in(circuit(1), dust("RawPolyisoprene", 1), dust("Sulfur", 1), dust("ZincOxide", 1))
                .out(dust("Ebonite", 1))
                .keep(0, 3)
                .add(200, 48);
    }

    private static void vulcanizeStandard(String raw, String cured) {
        row("gt.recipe.mixer", "c8/vulcanize-" + raw.toLowerCase())
                .in(dust(raw, 1), item("dustSmall", "CarbonBlack", 1), item("dustTiny", "Sulfur", 1), dust("ZincOxide", 1))
                .out(item("ingot", cured, 1))
                .keep(3)
                .add(200, 48);
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
        row("gt.recipe.mixer", "c9/cyclohexane")
                .in(dust("Nickel", 1))
                .fluidIn("GenLiquid_Benzene", 1000, "Hydrogen", 3000)
                .fluidOut("GenLiquid_Cyclohexane", 1000)
                .keep(0)
                .add(160, 48);
        row("gt.recipe.mixer", "c9/cyclohexanone-air")
                .in(dust("Cobalt", 1))
                .fluidIn("GenLiquid_Cyclohexane", 1000, "Oxygen", 1000)
                .fluidOut("GenLiquid_Cyclohexanone", 1000, "DistW", 1000)
                .keep(0)
                .add(200, 48);
        row("gt.recipe.mixer", "c9/cyclohexanone-phenol")
                .in(dust("Palladium", 1))
                .fluidIn("GenLiquid_Phenol", 1000, "Hydrogen", 2000)
                .fluidOut("GenLiquid_Cyclohexanone", 1000)
                .keep(0)
                .add(160, 48);
    }

    private static void caprolactam() {
        row("gt.recipe.mixer", "c9/hydroxylammonium-sulfate")
                .in(dust("Platinum", 1))
                .out(dust("HydroxylammoniumSulfate", 1))
                .fluidIn("GenGas_NitrogenMonoxide", 2000, "Hydrogen", 3000, "GenLiquid_SulfuricAcid", 1000)
                .keep(0)
                .add(200, 64);
        row("gt.recipe.mixer", "c9/cyclohexanone-oxime")
                .in(dust("HydroxylammoniumSulfate", 1))
                .out(dust("CyclohexanoneOxime", 2))
                .fluidIn("GenLiquid_Cyclohexanone", 2000, "GenGas_Ammonia", 2000)
                .fluidOut("Dirty_Water", 2000)
                .add(200, 48);
        row("gt.recipe.mixer", "c9/caprolactam")
                .in(dust("CyclohexanoneOxime", 1))
                .out(dust("Caprolactam", 1))
                .fluidIn("GenLiquid_DisulfuricAcid", 500, "GenGas_Ammonia", 1000)
                .fluidOut("Dirty_Water", 1000)
                .add(160, 48);
        row("gt.recipe.mixer", "c9/nylon6")
                .in(dust("Caprolactam", 1))
                .out(dust("Polycaprolactam", 1))
                .fluidIn("DistW", 10)
                .add(240, 48);
    }

    private static void carbonFiber() {
        row("gt.recipe.mixer", "c9/polyacrylonitrile")
                .in(circuit(1))
                .out(dust("Polyacrylonitrile", 1))
                .fluidIn("GenLiquid_Acrylonitrile", 1000, "GenLiquid_DeionizedWater", 1000, "GenLiquid_CumeneHydroperoxide", 10)
                .fluidOut("DistW", 1000)
                .keep(0)
                .add(200, 48);
        row("gt.recipe.calciner", "c9/oxidize-pan")
                .in(item("wireFine", "Polyacrylonitrile", 16))
                .out(item("wireFine", "OxidizedPolyacrylonitrile", 16))
                .fluidIn("Air", 1000)
                .add(400, 64);
        row("gt.recipe.calciner", "c9/carbonize")
                .in(item("wireFine", "OxidizedPolyacrylonitrile", 16))
                .out(item("wireFine", "CarbonFiber", 8))
                .fluidIn("Nitrogen", 1000)
                .fluidOut("GenLiquid_HydrogenCyanide", 50)
                .add(600, 128);
        row("gt.recipe.loom", "c9/carbon-fiber-cloth")
                .in(item("wireFine", "CarbonFiber", 8))
                .out(item("plate", "CarbonFiber", 1))
                .add(200, 32);
        row("gt.recipe.rollingmill", "c9/carbon-fiber-foil")
                .in(item("plate", "CarbonFiber", 1))
                .out(item("foil", "CarbonFiber", 4))
                .add(100, 16);
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
        row("gt.recipe.mixer", "c10/chlorobenzene")
                .in(dust("FerricChloride", 1))
                .fluidIn("GenLiquid_Benzene", 1000, "GenGas_Chlorine", 1000)
                .fluidOut("GenLiquid_Chlorobenzene", 1000, "GenGas_HydrochloricAcid", 1000)
                .keep(0)
                .add(120, 32);
        row("gt.recipe.mixer", "c10/para-nitrochlorobenzene")
                .out(dust("ParaNitrochlorobenzene", 1))
                .fluidIn("GenLiquid_Chlorobenzene", 1000, "GenLiquid_NitricAcid", 1000, "GenLiquid_SulfuricAcid", 100)
                .fluidOut("DistW", 1000)
                .add(160, 48);
        row("gt.recipe.autoclave", "c10/para-nitroaniline")
                .in(circuit(1), dust("ParaNitrochlorobenzene", 1))
                .out(dust("ParaNitroaniline", 1))
                .fluidIn("GenGas_Ammonia", 2000)
                .keep(0)
                .add(200, 48);
        row("gt.recipe.mixer", "c10/para-phenylenediamine")
                .in(dust("Nickel", 1), dust("ParaNitroaniline", 1))
                .out(dust("ParaPhenylenediamine", 1))
                .fluidIn("Hydrogen", 3000)
                .fluidOut("DistW", 2000)
                .keep(0)
                .add(160, 48);
    }

    private static void metaPhenylenediamine() {
        row("gt.recipe.mixer", "c10/meta-dinitrobenzene")
                .out(dust("MetaDinitrobenzene", 1))
                .fluidIn("GenLiquid_Nitrobenzene", 1000, "GenLiquid_NitricAcid", 1000, "GenLiquid_SulfuricAcid", 200)
                .fluidOut("DistW", 1000)
                .add(200, 48);
        row("gt.recipe.mixer", "c10/meta-phenylenediamine")
                .in(dust("Nickel", 1), dust("MetaDinitrobenzene", 1))
                .out(dust("MetaPhenylenediamine", 1))
                .fluidIn("Hydrogen", 6000)
                .fluidOut("DistW", 4000)
                .keep(0)
                .add(200, 48);
    }

    private static void diacylChlorides() {
        row("gt.recipe.mixer", "c10/terephthaloyl-chloride")
                .in(dust("TerephthalicAcid", 1))
                .out(dust("TerephthaloylChloride", 1))
                .fluidIn("GenGas_Phosgene", 2000)
                .fluidOut("CarbonDioxide", 2000, "GenGas_HydrochloricAcid", 2000)
                .add(160, 48);
        row("gt.recipe.mixer", "c10/isophthaloyl-chloride")
                .in(dust("IsophthalicAcid", 1))
                .out(dust("IsophthaloylChloride", 1))
                .fluidIn("GenGas_Phosgene", 2000)
                .fluidOut("CarbonDioxide", 2000, "GenGas_HydrochloricAcid", 2000)
                .add(160, 48);
    }

    private static void kevlar() {
        row("gt.recipe.mixer", "c10/kevlar")
                .in(dust("CalciumChloride", 1), dust("ParaPhenylenediamine", 1), dust("TerephthaloylChloride", 1))
                .out(dust("Kevlar", 1))
                .fluidOut("GenGas_HydrochloricAcid", 2000)
                .keep(0)
                .add(300, 96);
        row("gt.recipe.bath", "c10/kevlar-spin")
                .in(dust("Kevlar", 1))
                .out(item("wireFine", "Kevlar", 4))
                .fluidIn("GenLiquid_SulfuricAcid", 1000)
                .fluidOut("GenLiquid_SulfuricAcid", 950)
                .add(200, 64);
        row("gt.recipe.loom", "c10/kevlar-cloth")
                .in(item("wireFine", "Kevlar", 8))
                .out(item("plate", "Kevlar", 1))
                .add(200, 32);
        row("gt.recipe.rollingmill", "c10/kevlar-foil")
                .in(item("plate", "Kevlar", 1))
                .out(item("foil", "Kevlar", 4))
                .add(100, 16);
    }

    private static void nomex() {
        row("gt.recipe.mixer", "c10/nomex")
                .in(dust("CalciumChloride", 1), dust("MetaPhenylenediamine", 1), dust("IsophthaloylChloride", 1))
                .out(dust("Nomex", 1))
                .fluidOut("GenGas_HydrochloricAcid", 2000)
                .keep(0)
                .add(300, 96);
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
        row("gt.recipe.calciner", "c11/chloroform")
                .fluidIn("Methane", 1000, "GenGas_Chlorine", 3000)
                .fluidOut("GenLiquid_Chloroform", 1000, "GenGas_HydrochloricAcid", 3000)
                .add(160, 48);
        row("gt.recipe.mixer", "c11/chlorodifluoromethane")
                .in(dust("Antimony", 1))
                .fluidIn("GenLiquid_Chloroform", 1000, "GenGas_HydrogenFluoride", 2000)
                .fluidOut("GenGas_Chlorodifluoromethane", 1000, "GenGas_HydrochloricAcid", 2000)
                .keep(0)
                .add(200, 64);
        row("gt.recipe.calciner", "c11/tetrafluoroethylene")
                .in(circuit(1))
                .fluidIn("GenGas_Chlorodifluoromethane", 2000)
                .fluidOut("GenGas_Tetrafluoroethylene", 1000, "GenGas_HydrochloricAcid", 2000)
                .keep(0)
                .add(200, 96);
        row("gt.recipe.calciner", "c11/hexafluoropropylene")
                .in(circuit(1))
                .fluidIn("GenGas_Tetrafluoroethylene", 3000)
                .fluidOut("GenGas_Hexafluoropropylene", 2000)
                .keep(0)
                .add(200, 96);
    }

    private static void vinylideneFluoride() {
        row("gt.recipe.mixer", "c11/trichloroethane")
                .in(dust("FerricChloride", 1))
                .fluidIn("GenGas_VinylChloride", 1000, "GenGas_HydrochloricAcid", 1000, "GenGas_Chlorine", 1000)
                .fluidOut("GenLiquid_Trichloroethane", 1000, "GenGas_HydrochloricAcid", 1000)
                .keep(0)
                .add(160, 48);
        row("gt.recipe.mixer", "c11/chlorodifluoroethane")
                .in(dust("Antimony", 1))
                .fluidIn("GenLiquid_Trichloroethane", 1000, "GenGas_HydrogenFluoride", 2000)
                .fluidOut("GenGas_Chlorodifluoroethane", 1000, "GenGas_HydrochloricAcid", 2000)
                .keep(0)
                .add(200, 64);
        row("gt.recipe.calciner", "c11/vinylidene-fluoride")
                .in(circuit(1))
                .fluidIn("GenGas_Chlorodifluoroethane", 1000)
                .fluidOut("GenGas_VinylideneFluoride", 1000, "GenGas_HydrochloricAcid", 1000)
                .keep(0)
                .add(160, 64);
    }

    private static void perfluorosulfonylVinylEther() {
        row("gt.recipe.mixer", "c11/perfluorosulfonyl-vinyl-ether")
                .fluidIn("GenGas_Tetrafluoroethylene", 2000, "GenGas_Hexafluoropropylene", 1000, "GenGas_SulfurTrioxide", 1000)
                .fluidOut("GenLiquid_PerfluorosulfonylVinylEther", 1000)
                .add(300, 96);
    }

    private static void emulsionPolymers() {
        emulsionPolymer("c11/ptfe", 1, new String[]{"GenGas_Tetrafluoroethylene"}, new int[]{1000},
                "Teflon", 200, 64);
        emulsionPolymer("c11/pvdf", 2, new String[]{"GenGas_VinylideneFluoride"}, new int[]{1000},
                "PolyvinylideneFluoride", 200, 64);
        emulsionPolymer("c11/raw-viton", 3,
                new String[]{"GenGas_VinylideneFluoride", "GenGas_Hexafluoropropylene"}, new int[]{600, 400},
                "RawViton", 240, 64);
        row("gt.recipe.mixer", "c11/nafion")
                .in(circuit(4), item("dustTiny", "SodiumPersulfate", 1))
                .out(dust("Nafion", 1))
                .fluidIn("GenGas_Tetrafluoroethylene", 1000, "GenLiquid_PerfluorosulfonylVinylEther", 1000, "GenLiquid_DeionizedWater", 1000)
                .fluidOut("DistW", 1000)
                .keep(0, 1)
                .add(300, 128);
    }

    private static void emulsionPolymer(String label, int circuit, String[] monomers, int[] amounts,
                                        String product, long duration, long perTick) {
        row("gt.recipe.mixer", label)
                .in(circuit(circuit), item("dustTiny", "SodiumPersulfate", 1))
                .out(dust(product, 1))
                .fluidIn(appendMonomers(monomers, "GenLiquid_DeionizedWater"), appendAmounts(amounts, 1000))
                .fluidOut("DistW", 1000)
                .keep(0, 1)
                .add(duration, perTick);
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
        row("gt.recipe.mixer", "c11/viton")
                .in(dust("RawViton", 1), item("dustSmall", "CarbonBlack", 1), item("dustTiny", "BisphenolA", 1), dust("MagnesiumOxide", 1))
                .out(item("ingot", "Viton", 1))
                .keep(3)
                .add(240, 64);
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
        row("gt.recipe.mixer", "c12/para-dichlorobenzene")
                .in(dust("FerricChloride", 1))
                .out(dust("ParaDichlorobenzene", 1))
                .fluidIn("GenLiquid_Chlorobenzene", 1000, "GenGas_Chlorine", 1000)
                .fluidOut("GenGas_HydrochloricAcid", 1000)
                .keep(0)
                .add(120, 32);
        row("gt.recipe.mixer", "c12/pps")
                .in(dust("ParaDichlorobenzene", 1), dust("SodiumSulfide", 1))
                .out(dust("PolyphenyleneSulfide", 1))
                .fluidIn("DistW", 1000)
                .fluidOut("Saltwater", 1000)
                .add(240, 64);
    }

    private static void polysulfone() {
        row("gt.recipe.mixer", "c12/dichlorodiphenylsulfone")
                .out(dust("Dichlorodiphenylsulfone", 1))
                .fluidIn("GenLiquid_Chlorobenzene", 2000, "GenGas_SulfurTrioxide", 1000)
                .fluidOut("DistW", 1000)
                .add(200, 48);
        row("gt.recipe.mixer", "c12/polysulfone")
                .in(dust("Dichlorodiphenylsulfone", 1), dust("BisphenolA", 1), dust("SodiumHydroxide", 2))
                .out(dust("Polysulfone", 1))
                .fluidOut("Saltwater", 2000)
                .add(240, 64);
    }

    private static void peek() {
        row("gt.recipe.mixer", "c12/hydroquinone")
                .in(dust("ZSM5", 1))
                .out(dust("Hydroquinone", 1))
                .fluidIn("GenLiquid_Phenol", 1000, "GenGas_HydrogenPeroxide", 1000)
                .fluidOut("DistW", 1000)
                .keep(0)
                .add(200, 48);
        row("gt.recipe.mixer", "c12/fluorobenzene")
                .in(dust("PotassiumFluoride", 1))
                .fluidIn("GenLiquid_Chlorobenzene", 1000)
                .fluidOut("GenLiquid_Fluorobenzene", 1000)
                .add(200, 64);
        row("gt.recipe.mixer", "c12/difluorobenzophenone")
                .in(dust("FerricChloride", 1))
                .out(dust("Difluorobenzophenone", 1))
                .fluidIn("GenLiquid_Fluorobenzene", 2000, "GenGas_Phosgene", 1000)
                .fluidOut("GenGas_HydrochloricAcid", 2000)
                .keep(0)
                .add(200, 64);
        row("gt.recipe.mixer", "c12/peek")
                .in(dust("Difluorobenzophenone", 1), dust("Hydroquinone", 1), dust("PotassiumCarbonate", 1))
                .out(dust("PolyetherEtherKetone", 1))
                .fluidOut("CarbonDioxide", 1000, "DistW", 1000)
                .add(300, 128);
    }

    private static void kapton() {
        row("gt.recipe.mixer", "c12/durene")
                .in(dust("ZSM5", 1))
                .out(dust("Durene", 1))
                .fluidIn("GenLiquid_Xylene", 1000, "GenLiquid_Methanol", 2000)
                .fluidOut("DistW", 2000)
                .keep(0)
                .add(200, 48);
        row("gt.recipe.mixer", "c12/pyromellitic-dianhydride")
                .in(dust("VanadiumPentoxide", 1), dust("Durene", 1))
                .out(dust("PyromelliticDianhydride", 1))
                .fluidIn("Oxygen", 6000)
                .fluidOut("DistW", 6000)
                .keep(0)
                .add(300, 96);
        row("gt.recipe.mixer", "c12/oxydianiline")
                .in(dust("Nickel", 1), dust("ParaNitrochlorobenzene", 2), dust("SodiumHydroxide", 2))
                .out(dust("Oxydianiline", 1))
                .fluidIn("Hydrogen", 6000)
                .fluidOut("Saltwater", 2000)
                .keep(0)
                .add(300, 96);
        row("gt.recipe.mixer", "c12/kapton")
                .in(dust("PyromelliticDianhydride", 1), dust("Oxydianiline", 1))
                .out(dust("Kapton", 1))
                .fluidOut("DistW", 2000)
                .add(400, 128);
    }

    // ── Card 13: extractants and lubricant base oil ──

    private static void extractantsLubricants() {
        mibk();
        phosphorusExtractants();
        amineExtractants();
        lubricants();
    }

    private static void mibk() {
        row("gt.recipe.mixer", "c13/mibk")
                .in(dust("CationExchangeResin", 1), dust("Palladium", 1))
                .fluidIn("GenLiquid_Acetone", 2000, "Hydrogen", 1000)
                .fluidOut("GenLiquid_MethylIsobutylKetone", 1000, "DistW", 1000)
                .keep(0, 1)
                .add(200, 48);
    }

    private static void phosphorusExtractants() {
        row("gt.recipe.mixer", "c13/phosphoryl-chloride")
                .in(dust("Phosphorus", 1))
                .fluidIn("GenGas_Chlorine", 3000, "Oxygen", 500)
                .fluidOut("GenLiquid_PhosphorylChloride", 1000)
                .add(160, 48);
        row("gt.recipe.mixer", "c13/tributyl-phosphate")
                .fluidIn("GenLiquid_PhosphorylChloride", 1000, "GenLiquid_Butanol", 3000)
                .fluidOut("GenLiquid_TributylPhosphate", 1000, "GenGas_HydrochloricAcid", 3000)
                .add(200, 48);
        row("gt.recipe.mixer", "c13/p204")
                .fluidIn("GenLiquid_PhosphorylChloride", 1000, "GenLiquid_TwoEthylhexanol", 2000, "DistW", 1000)
                .fluidOut("GenLiquid_DiethylhexylPhosphoricAcid", 1000, "GenGas_HydrochloricAcid", 3000)
                .add(200, 48);
    }

    private static void amineExtractants() {
        row("gt.recipe.mixer", "c13/octanol")
                .in(dust("Aluminium", 1))
                .fluidIn("Ethylene", 4000, "Oxygen", 500, "DistW", 1000)
                .fluidOut("GenLiquid_Octanol", 1000)
                .keep(0)
                .add(240, 64);
        row("gt.recipe.mixer", "c13/trioctylamine")
                .in(dust("Nickel", 1))
                .fluidIn("GenLiquid_Octanol", 3000, "GenGas_Ammonia", 1000)
                .fluidOut("GenLiquid_Trioctylamine", 1000, "DistW", 3000)
                .keep(0)
                .add(240, 64);
        row("gt.recipe.mixer", "c13/aliquat")
                .fluidIn("GenLiquid_Trioctylamine", 1000, "GenGas_Chloromethane", 1000)
                .fluidOut("GenLiquid_Aliquat", 1000)
                .add(160, 48);
    }

    private static void lubricants() {
        row("gt.recipe.mixer", "c13/alpha-olefins")
                .in(circuit(1), dust("Aluminium", 1))
                .fluidIn("Ethylene", 4000)
                .fluidOut("GenLiquid_AlphaOlefins", 1000)
                .keep(0, 1)
                .add(240, 64);
        row("gt.recipe.distillationtower", "c13/alpha-olefins-distill")
                .fluidIn("GenLiquid_AlphaOlefins", 1000)
                .fluidOut(fields("GenGas_OneButene", "GenLiquid_OneOctene", "GenLiquid_OneDecene", "Fuel"), amounts(250, 300, 250, 200))
                .add(160, 64);
        row("gt.recipe.mixer", "c13/pao")
                .in(dust("SolidPhosphoricAcid", 1), dust("Nickel", 1))
                .fluidIn("GenLiquid_OneDecene", 3000, "Hydrogen", 1000)
                .fluidOut("GenLiquid_Polyalphaolefin", 1000)
                .keep(0, 1)
                .add(240, 64);
        row("gt.recipe.mixer", "c13/polyisobutylene")
                .in(dust("FerricChloride", 1))
                .fluidIn("GenGas_Isobutylene", 1000)
                .fluidOut("GenLiquid_Polyisobutylene", 1000)
                .keep(0)
                .add(200, 48);
    }

    // ── Card 14: deferred fill (TDI, propylene oxide, adipic acid/nylon 66, PPO, PEI, dicyandiamide) ──

    private static void deferredFill() {
        propyleneOxide();
        adipicNylon66();
        tdi();
        ppo();
        pei();
        dicyandiamide();
        polymerForming("PolyhexamethyleneAdipamide", true);
        polymerForming("PolyphenyleneOxide", false);
        polymerForming("Polyetherimide", false);
    }

    private static void propyleneOxide() {
        row("gt.recipe.mixer", "c14/propylene-oxide")
                .in(dust("ZSM5", 1))
                .fluidIn("Propylene", 1000, "GenGas_HydrogenPeroxide", 1000)
                .fluidOut("GenLiquid_PropyleneOxide", 1000, "DistW", 1000)
                .keep(0)
                .add(160, 48);
        row("gt.recipe.mixer", "c14/propylene-glycol")
                .fluidIn("GenLiquid_PropyleneOxide", 1000, "DistW", 1000)
                .fluidOut("GenLiquid_PropyleneGlycol", 1000)
                .add(100, 32);
        row("gt.recipe.mixer", "c14/polyether-polyol-po")
                .in(circuit(2), item("dustTiny", "PotassiumHydroxide", 1))
                .fluidIn("GenLiquid_Glycerol", 1000, "GenLiquid_PropyleneOxide", 3000)
                .fluidOut("GenLiquid_PolyetherPolyol", 1000)
                .keep(0, 1)
                .add(200, 48);
    }

    private static void adipicNylon66() {
        row("gt.recipe.mixer", "c14/adipic-acid")
                .in(dust("VanadiumPentoxide", 1))
                .out(dust("AdipicAcid", 1))
                .fluidIn("GenLiquid_Cyclohexanone", 1000, "GenLiquid_NitricAcid", 2000)
                .fluidOut("GenGas_NitrogenMonoxide", 1000, "DistW", 1000)
                .keep(0)
                .add(240, 96);
        row("gt.recipe.calciner", "c14/adiponitrile")
                .in(dust("AdipicAcid", 1))
                .fluidIn("GenGas_Ammonia", 2000)
                .fluidOut("GenLiquid_Adiponitrile", 1000, "DistW", 4000)
                .add(240, 96);
        row("gt.recipe.mixer", "c14/hexamethylenediamine")
                .in(dust("Nickel", 1))
                .out(dust("Hexamethylenediamine", 1))
                .fluidIn("GenLiquid_Adiponitrile", 1000, "Hydrogen", 4000)
                .keep(0)
                .add(200, 64);
        row("gt.recipe.mixer", "c14/nylon-66")
                .in(dust("AdipicAcid", 1), dust("Hexamethylenediamine", 1))
                .out(dust("PolyhexamethyleneAdipamide", 1))
                .fluidOut("DistW", 2000)
                .add(240, 96);
    }

    private static void tdi() {
        row("gt.recipe.mixer", "c14/dinitrotoluene")
                .out(dust("Dinitrotoluene", 1))
                .fluidIn("GenLiquid_Toluene", 1000, "GenLiquid_NitricAcid", 2000, "GenLiquid_SulfuricAcid", 100)
                .fluidOut("DistW", 2000)
                .add(200, 64);
        row("gt.recipe.mixer", "c14/toluenediamine")
                .in(dust("Nickel", 1), dust("Dinitrotoluene", 1))
                .out(dust("Toluenediamine", 1))
                .fluidIn("Hydrogen", 6000)
                .fluidOut("DistW", 4000)
                .keep(0)
                .add(200, 64);
        row("gt.recipe.mixer", "c14/tdi")
                .in(dust("Toluenediamine", 1))
                .fluidIn("GenGas_Phosgene", 2000)
                .fluidOut("GenLiquid_TolueneDiisocyanate", 1000, "GenGas_HydrochloricAcid", 4000)
                .add(200, 64);
        row("gt.recipe.mixer", "c14/polyurethane-tdi")
                .in(circuit(3))
                .out(dust("Polyurethane", 2))
                .fluidIn("GenLiquid_TolueneDiisocyanate", 1000, "GenLiquid_PolyetherPolyol", 1000)
                .keep(0)
                .add(200, 48);
    }

    private static void ppo() {
        row("gt.recipe.mixer", "c14/dimethylphenol")
                .in(dust("ZSM5", 1))
                .out(dust("Dimethylphenol", 1))
                .fluidIn("GenLiquid_Phenol", 1000, "GenLiquid_Methanol", 2000)
                .fluidOut("DistW", 2000)
                .keep(0)
                .add(240, 64);
        row("gt.recipe.mixer", "c14/ppo")
                .in(dust("Copper", 1), dust("Dimethylphenol", 1))
                .out(dust("PolyphenyleneOxide", 1))
                .fluidIn("Oxygen", 1000)
                .fluidOut("DistW", 1000)
                .keep(0)
                .add(240, 64);
    }

    private static void pei() {
        row("gt.recipe.mixer", "c14/bpada")
                .in(dust("BisphenolA", 1), dust("PhthalicAnhydride", 2))
                .out(dust("BisphenolADianhydride", 1))
                .fluidOut("DistW", 2000)
                .add(300, 96);
        row("gt.recipe.mixer", "c14/pei")
                .in(dust("BisphenolADianhydride", 1), dust("MetaPhenylenediamine", 1))
                .out(dust("Polyetherimide", 1))
                .fluidOut("DistW", 2000)
                .add(400, 128);
    }

    private static void dicyandiamide() {
        row("gt.recipe.calciner", "c14/calcium-cyanamide")
                .in(dust("Quicklime", 1), dust("CoalCoke", 3))
                .out(dust("CalciumCyanamide", 1))
                .fluidIn("Nitrogen", 1000)
                .fluidOut("GenGas_CarbonMonoxide", 1000)
                .add(300, 96);
        row("gt.recipe.calciner", "c14/dicyandiamide")
                .in(dust("CalciumCyanamide", 2))
                .out(dust("Dicyandiamide", 1), dust("Calcite", 2))
                .fluidIn("DistW", 2000, "CarbonDioxide", 2000)
                .add(200, 64);
        row("gt.recipe.mixer", "c14/epoxy-dicyandiamide")
                .in(dust("Dicyandiamide", 1))
                .out(dust("Epoxy", 1))
                .fluidIn("GenLiquid_EpoxyResin", 1000)
                .add(200, 120);
    }
}
