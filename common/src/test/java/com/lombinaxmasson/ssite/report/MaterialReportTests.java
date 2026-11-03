package com.lombinaxmasson.ssite.report;

import com.google.gson.JsonParseException;
import com.lombinaxmasson.ssite.chemistry.ChemistryRecipes;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.DistinctFluids;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.FluidRef;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.ItemKind;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.ItemRef;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.Source;
import gregtech.api.addon.material.MaterialDeclaration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Checks production declarations and report semantics without bootstrapping game registries. */
public final class MaterialReportTests {
    private MaterialReportTests() {}

    public static void main(String[] args) throws Exception {
        Path repo = (args.length == 0 ? Path.of("") : Path.of(args[0])).toAbsolutePath().normalize();
        List<RecipeDeclaration> rows = ChemistryRecipes.declarations();
        moduleOrderAndSourceLocations(repo, rows);
        rareEarthLoopExpansion(rows);
        rubberAndFormingExpansion(rows);
        keptInputsAndArrayOrdering(rows);
        displayRowsRemainSeparate(rows);
        ethanolConditions(rows);
        vanillaItems(rows);
        chancesAndFuelQuantities(rows);
        immutableDeclarations(rows);
        repeatableDeclarations(rows);
        formingAndCatalystEvidence(rows);
        recyclingAndConditionalEvidence(rows);
        fuelAndDisplayEvidence(rows);
        thoriumFuelChain(rows);
        spentFuelChain(rows);
        blanketBreedingChain(rows);
        precisionLaserChain(rows);
        powderManufacturingChain(rows);
        fiberOpticChain(rows);
        advancedSemiconductorChain(rows);
        craftingInputsAndRemovalRules();
        shapelessAlternativesAndUnsupportedTypes();
        optionalNotesAndUnknownTargets();
        malformedNotesFail();
        contributorCoverage(repo);
        renderingAndDeterministicOutput(repo);
        System.out.println("MaterialReportTests: all 25 checks passed.");
    }

    private static void moduleOrderAndSourceLocations(Path repo, List<RecipeDeclaration> rows) throws IOException {
        Set<String> modules = new LinkedHashSet<>();
        Set<String> identities = new LinkedHashSet<>();
        for (RecipeDeclaration row : rows) {
            Path source = repo.resolve(row.source().file());
            check(Files.isRegularFile(source), "declaration source exists: " + row.source());
            List<String> lines = Files.readAllLines(source, StandardCharsets.UTF_8);
            check(row.source().line() > 0 && row.source().line() <= lines.size(), "source line is in bounds");
            check(lines.get(row.source().line() - 1).contains("row("), "source points at its recipe declaration");
            modules.add(source.getFileName().toString());
            check(identities.add(row.map() + "/" + row.label()), "recipe identity is unique: " + row.label());
        }
        check(List.copyOf(modules).equals(List.of("StainlessSteelRecipes.java", "WaterRecipes.java",
                "PetrochemRecipes.java", "PolymerRecipes.java", "MetallurgyRecipes.java",
                "RareEarthRecipes.java", "NuclearRecipes.java", "BaseMetalRecipes.java",
                "CircuitRecipes.java", "WaferRecipes.java", "LithographyRecipes.java",
                "PackagingRecipes.java", "AdvancedSemiconductorRecipes.java",
                "ElectrochemistryRecipes.java", "TextileRecipes.java",
                "InvestmentCastingRecipes.java", "OpticsRecipes.java", "PrecisionRecipes.java",
                "PowderManufacturingRecipes.java", "FiberOpticRecipes.java",
                "ProcessEnvironmentRecipes.java", "EngineeringMaterialRecipes.java",
                "UtilityRecipes.java")),
                "all central recipe modules retain runtime registration order");
    }

    private static void rareEarthLoopExpansion(List<RecipeDeclaration> rows) {
        List<RecipeDeclaration> reductions = rows.stream()
                .filter(row -> row.label().startsWith("rare-earth/calciothermy-")).toList();
        Set<String> metals = reductions.stream().map(row -> row.itemOutputs().get(0).material())
                .collect(Collectors.toSet());
        check(reductions.size() == 11 && metals.equals(Set.of("Lanthanium", "Cerium", "Praseodymium",
                "Neodymium", "Samarium", "Europium", "Gadolinium", "Terbium", "Dysprosium",
                "Yttrium", "Lutetium")), "rare-earth helper expands all eleven elements and host spelling");
        for (RecipeDeclaration row : reductions) {
            check(row.itemInputs().get(0).count() == 2 && row.itemInputs().get(1).equals(material("dust", "Calcium", 3)),
                    "calciothermic charge retains fluoride and calcium ratio");
            check(row.itemOutputs().get(0).count() == 2
                    && row.itemOutputs().get(1).equals(material("dust", "Fluorite", 3)),
                    "calciothermy preserves the fluorite recycling output");
        }
        RecipeDeclaration cerium = row(rows, "rare-earth/fluoride-cerium");
        check(cerium.itemInputs().equals(List.of(material("dust", "CeriumOxide", 2)))
                && cerium.fluidOutputs().equals(List.of(new FluidRef("DistW", 3000), new FluidRef("Oxygen", 500))),
                "cerium's separate oxidation-state recipe is not replaced by the generic loop");
    }

    private static void rubberAndFormingExpansion(List<RecipeDeclaration> rows) {
        for (String material : List.of("StyreneButadieneRubber", "NitrileRubber")) {
            RecipeDeclaration cured = row(rows, "c8/vulcanize-raw" + material.toLowerCase(java.util.Locale.ROOT));
            check(cured.itemInputs().get(0).equals(material("dust", "Raw" + material, 1)), "helper keeps each raw rubber");
            check(cured.itemOutputs().equals(List.of(material("ingot", material, 1))), "helper emits the matching cured rubber");
        }
        RecipeDeclaration foil = row(rows, "forming/kapton/foil");
        check(foil.itemInputs().equals(List.of(material("plate", "Kapton", 1)))
                && foil.itemOutputs().equals(List.of(material("foil", "Kapton", 4))), "plate-to-foil quantity is preserved");
        RecipeDeclaration wire = row(rows, "forming/ultrahighmolecularweightpolyethylene/wire");
        check(wire.itemOutputs().equals(List.of(material("wireFine", "UltraHighMolecularWeightPolyethylene", 4))),
                "wire-enabled polymer expands its third forming route");
        check(rows.stream().noneMatch(r -> r.label().equals("forming/kapton/wire")), "foil-only polymer does not gain a wire route");
    }

    private static void keptInputsAndArrayOrdering(List<RecipeDeclaration> rows) {
        RecipeDeclaration neoprene = row(rows, "c8/vulcanize-neoprene");
        check(neoprene.keptInputs().equals(List.of(2, 3)), "neoprene preserves both oxide catalyst slots");
        check(neoprene.itemInputs().get(2).material().equals("MagnesiumOxide")
                && neoprene.itemInputs().get(3).material().equals("ZincOxide"), "kept indices still address the intended items");
        RecipeDeclaration viton = row(rows, "c11/raw-viton");
        check(viton.keptInputs().equals(List.of(0, 1)), "emulsion circuit and initiator are retained");
        check(viton.itemInputs().get(0).kind() == ItemKind.SELECTOR && viton.itemInputs().get(0).selector() == 3,
                "emulsion selector retains its circuit number");
        check(viton.itemInputs().get(1).equals(material("dustTiny", "SodiumPersulfate", 1)), "initiator retains its small prefix");
        check(viton.fluidInputs().equals(List.of(new FluidRef("GenGas_VinylideneFluoride", 600),
                new FluidRef("GenGas_Hexafluoropropylene", 400), new FluidRef("GenLiquid_DeionizedWater", 1000))),
                "helper array appends water without changing monomer order or amounts");
        check(viton.duration() == 240 && viton.perTick() == 64, "helper retains duration and energy");
    }

    private static void blanketBreedingChain(List<RecipeDeclaration> rows) {
        RecipeDeclaration irradiate = row(rows, "ssite.recipe.blanket", "nuclear/blanket-irradiate");
        check(irradiate.displayOnly()
                        && irradiate.itemInputs().equals(List.of(material("dust", "ThoriumFuelPellet", 4)))
                        && irradiate.itemOutputs().equals(List.of(material("dust", "IrradiatedThoriumBlanket", 4))),
                "the vessel's display row documents one four-pellet batch");
        RecipeDeclaration separate = row(rows, "nuclear/blanket-separate");
        check(separate.map().equals("gt.recipe.centrifuge")
                        && separate.itemInputs().equals(List.of(material("dust", "IrradiatedThoriumBlanket", 4))),
                "blanket separation runs in the centrifuge on the whole batch");
        long units = separate.itemOutputs().stream().mapToLong(i -> i.count() * weight(i.prefix())).sum();
        check(units == 4L * weight("dust"), "blanket separation balances the 288-unit batch");
        check(separate.itemOutputs().stream().anyMatch(i -> i.material().equals("Uranium233")),
                "blanket separation returns the bred uranium");
        RecipeDeclaration encapsulate = row(rows, "nuclear/fuel-u233-encapsulate");
        check(encapsulate.map().equals("gt.recipe.canner")
                        && encapsulate.itemInputs().equals(List.of(material("dustTiny", "Uranium233", 8),
                        new ItemRef(ItemKind.VANILLA, "", "", "gt6ce:reactor_rod_9201", 1, -1)))
                        && encapsulate.itemOutputs().equals(List.of(
                        new ItemRef(ItemKind.VANILLA, "", "", "gt6ce:reactor_rod_9222", 1, -1))),
                "the bred charge is encapsulated into the host U-233 fuel rod");
        check(rows.stream().noneMatch(r -> r.itemOutputs().stream().anyMatch(i ->
                        i.material().equals("Uranium233") && !i.prefix().equals("dustTiny"))),
                "no addon row manufactures a higher U-233 form than the bred tiny charge");
    }

    private static void precisionLaserChain(List<RecipeDeclaration> rows) {
        RecipeDeclaration module = row(rows, "gt.recipe.welder", "p01/laser-module");
        check(module.itemInputs().stream().anyMatch(i -> i.prefix().equals("laserRod")
                        && i.material().equals("NeodymiumYAG"))
                        && module.itemInputs().stream().anyMatch(i -> i.prefix().equals("opticalLens")
                        && i.material().equals("NeodymiumYAG"))
                        && module.itemInputs().stream().noneMatch(i -> i.prefix().contains("Blank")
                        || i.prefix().contains("Polished")),
                "the laser module accepts only the final coated rod and lens");
        check(module.itemOutputs().equals(List.of(material("laserModule", "LaserModule", 1))),
                "the welded optical assembly yields the solid-state module");
        List<RecipeDeclaration> modes = rows.stream()
                .filter(r -> r.map().equals("ssite.recipe.precision_laser")).toList();
        check(modes.size() == 5, "the workstation delivers cutting, drilling, welding, the cooled batch row and the P-06 high-load row");
        Set<String> heads = modes.stream()
                .filter(r -> !r.keptInputs().isEmpty())
                .map(r -> r.itemInputs().get(r.keptInputs().get(0)).prefix())
                .filter(prefix -> prefix.endsWith("Head"))
                .collect(Collectors.toSet());
        check(heads.equals(Set.of("laserCuttingHead", "laserDrillingHead", "laserWeldingHead")),
                "each basic mode keeps its own head and cannot match another mode");
        List<RecipeDeclaration> headRows = rows.stream().filter(r -> r.map().equals("gt.recipe.welder")
                && r.label().startsWith("p01/head/")).toList();
        check(headRows.size() == 3 && headRows.stream().allMatch(r -> r.itemOutputs().size() == 1
                        && r.itemOutputs().get(0).prefix().endsWith("Head")
                        && r.itemInputs().stream().anyMatch(i -> i.kind() == ItemKind.SELECTOR))
                        && headRows.stream().map(r -> r.itemInputs().stream()
                        .filter(i -> i.kind() == ItemKind.SELECTOR).findFirst().orElseThrow().selector())
                        .distinct().count() == 3,
                "every head has a welding source and a distinct selector keeps the rows apart");
        check(modes.stream().allMatch(r -> r.fluidInputs().size() == 1
                        && r.fluidInputs().get(0).field().equals("GenGas_Argon")),
                "every mode uses an argon medium");
        check(modes.stream().filter(r -> !r.label().equals("p06/high-load-drill"))
                        .allMatch(r -> r.keptInputs().size() == 1),
                "every basic mode keeps one tool");
        check(modes.stream().filter(r -> r.itemOutputs().get(0).count() == 3).allMatch(r ->
                        r.itemInputs().get(r.keptInputs().get(0)).prefix().equals("coolingAssembly")),
                "the cooled row batches three nozzles with a kept cooling assembly");
        check(modes.stream().allMatch(r -> r.itemInputs().stream()
                        .filter(i -> !i.prefix().endsWith("Head") && !i.prefix().equals("coolingAssembly")
                                && !i.prefix().equals("ablativeBacking"))
                        .allMatch(i -> i.prefix().equals("plate") && i.material().equals("StainlessSteel316L"))),
                "the first batch uses one alloy already supplied by AOD");
        RecipeDeclaration highLoad = row(rows, "ssite.recipe.precision_laser", "p06/high-load-drill");
        check(highLoad.itemInputs().equals(List.of(
                        material("plate", "StainlessSteel316L", 6),
                        material("ablativeBacking", "AblativeComposite", 1)))
                        && highLoad.keptInputs().isEmpty()
                        && highLoad.itemOutputs().equals(List.of(
                        material("precisionNozzle", "StainlessSteel316L", 6),
                        material("ablativeBackingSpent", "AblativeComposite", 1)))
                        && highLoad.fluidInputs().equals(List.of(new FluidRef("GenGas_Argon", 6000))),
                "the P-06 high-load row consumes one backing and returns one spent backing");
        check(highLoad.duration() == 6 * row(rows, "p01/drill-nozzle").duration()
                        && highLoad.perTick() == row(rows, "p01/drill-nozzle").perTick(),
                "the high-load batch keeps the single-piece material and energy rate");
    }

    private static void powderManufacturingChain(List<RecipeDeclaration> rows) {
        RecipeDeclaration mill = row(rows, "p05/powder-mill");
        check(mill.itemInputs().equals(List.of(material("dust", "StainlessSteel316L", 1)))
                        && mill.itemOutputs().equals(List.of(material("rawPowder", "StainlessSteel316L", 1))),
                "ordinary powder is only milled into raw powder");
        RecipeDeclaration classify = row(rows, "p05/classify");
        check(classify.itemInputs().equals(List.of(material("rawPowder", "StainlessSteel316L", 2)))
                        && classify.itemOutputs().equals(List.of(
                        material("classifiedPowder", "StainlessSteel316L", 1),
                        material("screenTailings", "StainlessSteel316L", 1))),
                "classification splits the milled charge into qualified powder and tailings");
        RecipeDeclaration print = row(rows, "ssite.recipe.powder_bed", "p05/print");
        check(print.displayOnly()
                        && print.itemInputs().equals(List.of(
                        material("classifiedPowder", "StainlessSteel316L", 4),
                        material("controlModule", "PrecisionControlModule", 1),
                        material("coolingAssembly", "StainlessSteel316L", 1)))
                        && print.keptInputs().equals(List.of(1, 2))
                        && print.fluidInputs().equals(List.of(new FluidRef("GenGas_Argon", 4000)))
                        && print.itemOutputs().equals(List.of(
                        material("printedBlank", "StainlessSteel316L", 1),
                        material("reclaimPowder", "StainlessSteel316L", 2))),
                "the powder-bed display fixes powder, module, cooling, gas and both outputs");
        RecipeDeclaration desupport = row(rows, "p05/desupport");
        check(desupport.itemInputs().equals(List.of(material("printedBlank", "StainlessSteel316L", 1)))
                        && desupport.itemOutputs().equals(List.of(
                        material("desupportedBlank", "StainlessSteel316L", 1),
                        material("printedSupport", "StainlessSteel316L", 2))),
                "support removal forces the independent blank id");
        RecipeDeclaration heat = row(rows, "p05/heat-treat");
        check(heat.itemInputs().equals(List.of(
                        material("desupportedBlank", "StainlessSteel316L", 1),
                        material("carbonCarbonSetter", "CarbonCarbon", 1)))
                        && heat.keptInputs().equals(List.of(1))
                        && heat.itemOutputs().equals(List.of(
                        material("coolingBlock", "StainlessSteel316L", 1))),
                "heat treatment accepts only the desupported blank and retains the P-06 kiln setter");
        check(rows.stream().noneMatch(r -> r.itemInputs().stream()
                        .anyMatch(i -> i.prefix().equals("printedBlank"))
                        && r.itemOutputs().stream().anyMatch(i -> i.prefix().equals("coolingBlock"))),
                "a printed blank cannot skip desupport and heat treatment");
        for (String label : List.of("p05/reclaim-classify", "p05/tailings-recycle", "p05/support-recycle")) {
            RecipeDeclaration recycle = row(rows, label);
            long in = recycle.itemInputs().stream().mapToLong(ItemRef::count).sum();
            long out = recycle.itemOutputs().stream().mapToLong(ItemRef::count).sum();
            check(out <= in, "recycling row " + label + " cannot create material");
        }
        check(row(rows, "p05/reclaim-classify").itemOutputs().get(0).count()
                        < row(rows, "p05/reclaim-classify").itemInputs().get(0).count(),
                "reclaim reclassification returns less qualified powder than it consumes");
        List<String> unexpectedPowderForms = rows.stream().filter(r -> r.label().startsWith("p05/")
                        && !r.map().equals("ssite.recipe.precision_laser"))
                .flatMap(r -> r.itemInputs().stream())
                .filter(i -> i.material().equals("StainlessSteel316L"))
                .filter(i -> !Set.of("dust", "rawPowder", "classifiedPowder", "reclaimPowder",
                        "screenTailings", "printedBlank", "desupportedBlank", "printedSupport",
                        "coolingBlock", "coolingAssembly", "precisionNozzle").contains(i.prefix()))
                .map(i -> i.prefix() + ":" + i.material())
                .toList();
        check(unexpectedPowderForms.isEmpty(),
                "the powder route only uses the whitelisted alloy and its own forms; unexpected "
                        + unexpectedPowderForms);
    }

    private static void fiberOpticChain(List<RecipeDeclaration> rows) {
        RecipeDeclaration frit = row(rows, "gt.recipe.mixer", "p02/optical-glass-frit");
        check(frit.itemInputs().equals(List.of(material("dust", "SiliconDioxide", 8),
                        material("dust", "GermaniumDioxide", 2)))
                        && frit.itemOutputs().equals(List.of(material("dust", "OpticalGlassFrit", 10))),
                "the glass batch conserves eight silica and two germania units");
        RecipeDeclaration preform = row(rows, "gt.recipe.calciner", "p02/preform");
        check(preform.itemInputs().equals(List.of(material("dust", "OpticalGlassFrit", 10)))
                        && preform.itemOutputs().equals(List.of(material("fiberPreform", "OpticalFiber", 1))),
                "one preform carries the full ten-unit batch");
        RecipeDeclaration draw = row(rows, "ssite.recipe.fiber_drawing", "p02/draw-fiber");
        check(draw.itemInputs().equals(List.of(material("fiberPreform", "OpticalFiber", 1),
                        material("precisionNozzle", "StainlessSteel316L", 1)))
                        && draw.keptInputs().equals(List.of(1)),
                "the drawing row keeps the P-01 nozzle instead of consuming it");
        check(draw.itemOutputs().equals(List.of(material("bareFiber", "OpticalFiber", 8),
                        material("dust", "OpticalGlassScrap", 2))),
                "drawing reports eighty percent fiber and twenty percent scrap");
        check(draw.fluidInputs().equals(List.of(new FluidRef("GenGas_Argon", 1000))),
                "drawing uses an argon medium");
        RecipeDeclaration coat = row(rows, "ssite.recipe.fiber_drawing", "p02/coat-fiber");
        check(coat.itemInputs().equals(List.of(material("bareFiber", "OpticalFiber", 8),
                        material("ingot", "SiliconeRubber", 1)))
                        && coat.itemOutputs().equals(List.of(material("coatedFiber", "OpticalFiber", 8))),
                "coating consumes the existing silicone rubber");
        RecipeDeclaration spool = row(rows, "ssite.recipe.fiber_drawing", "p02/spool-fiber");
        check(spool.itemInputs().equals(List.of(material("coatedFiber", "OpticalFiber", 8),
                        material("plate", "StainlessSteel316L", 1)))
                        && spool.itemOutputs().equals(List.of(material("fiberSpool", "OpticalFiber", 1))),
                "spooling keeps the eight-unit glass core");
        RecipeDeclaration scrap = row(rows, "gt.recipe.calciner", "p02/scrap-recovery");
        check(scrap.itemInputs().equals(List.of(material("dust", "OpticalGlassScrap", 2)))
                        && scrap.itemOutputs().equals(List.of(material("dust", "OpticalGlassFrit", 1))),
                "scrap recovery returns half a unit and cannot multiply germanium");
        List<RecipeDeclaration> drawing = rows.stream()
                .filter(r -> r.map().equals("ssite.recipe.fiber_drawing")).toList();
        check(drawing.size() == 3 && drawing.stream().allMatch(r -> r.itemInputs().size() == 2),
                "the drawing table holds the three two-input fiber steps");
    }

    private static void advancedSemiconductorChain(List<RecipeDeclaration> rows) {
        RecipeDeclaration clean = row(rows, "p03/clean");
        check(clean.itemInputs().equals(List.of(material("wafer", "Silicon", 1)))
                        && clean.fluidInputs().equals(List.of(new FluidRef("GenGas_Ozone", 1000)))
                        && clean.itemOutputs().equals(List.of(material("advancedWafer", "AdvancedCleanedWafer", 1))),
                "advanced lithography starts from raw silicon and a distinct ozone clean");
        check(rows.stream().noneMatch(r -> r.label().startsWith("p03/")
                        && r.itemInputs().stream().anyMatch(i -> i.material().equals("ProcessedWafer"))),
                "the old processed wafer has no route into the advanced chain");

        RecipeDeclaration implant = row(rows, "ssite.recipe.ion_implanter", "p03/implant");
        check(implant.displayOnly()
                        && implant.itemInputs().equals(List.of(material("advancedWafer", "AdvancedStrippedWafer", 1),
                        material("dust", "Phosphorus", 1)))
                        && implant.fluidInputs().equals(List.of(new FluidRef("GenGas_Argon", 1000)))
                        && implant.itemOutputs().equals(List.of(material("advancedWafer", "ImplantedWafer", 1))),
                "the implanter display row documents the wafer, phosphorus and argon charge");

        RecipeDeclaration anneal = row(rows, "p03/anneal");
        check(anneal.itemInputs().equals(List.of(material("advancedWafer", "ImplantedWafer", 1)))
                        && anneal.itemOutputs().equals(List.of(material("advancedWafer", "AnnealedWafer", 1))),
                "only the implanted state can be annealed");

        RecipeDeclaration dice = row(rows, "p03/dice");
        check(dice.itemInputs().equals(List.of(material("advancedWafer", "AdvancedProcessedWafer", 1)))
                        && dice.itemOutputs().equals(List.of(material("advancedDie", "AdvancedChipDie", 8))),
                "advanced dice consumes only the processed advanced wafer");

        RecipeDeclaration module = row(rows, "p03/control-module");
        check(module.itemOutputs().equals(List.of(material("controlModule", "PrecisionControlModule", 1)))
                        && module.itemInputs().stream().anyMatch(i -> i.kind() == ItemKind.VANILLA
                        && i.itemId().equals("gt6ce:circuit_elite"))
                        && module.itemInputs().stream().anyMatch(i -> i.prefix().equals("ceramicClamp")
                        && i.material().equals("EngineeringCeramic"))
                        && module.itemInputs().stream().noneMatch(i -> i.prefix().equals("precisionFixture"))
                        && module.itemInputs().stream().noneMatch(i -> i.prefix().equals("advancedWafer")
                        || i.prefix().equals("advancedDie")),
                "the control module consumes the P-06 ceramic clamp plus existing Elite and P-01 parts");

        RecipeDeclaration template = row(rows, "p03/program-template");
        check(template.itemOutputs().equals(List.of(material("programTemplate", "ProgramTemplate", 1))),
                "the program template has an explicit manufacturing row");

        RecipeDeclaration ebeam = row(rows, "ssite.recipe.electron_beam", "p03/ebeam");
        check(ebeam.displayOnly() && ebeam.keptInputs().equals(List.of(1))
                        && ebeam.itemOutputs().equals(List.of(material("advancedWafer", "EBeamExposedWafer", 1))),
                "the electron-beam display row keeps the template and shares the later route");
    }

    private static long weight(String prefix) {
        return switch (prefix) {
            case "dust" -> 72;
            case "dustTiny" -> 8;
            case "dustDiv72" -> 1;
            default -> throw new AssertionError("unexpected dust prefix: " + prefix);
        };
    }

    private static void displayRowsRemainSeparate(List<RecipeDeclaration> rows) {
        Map<String, Long> counts = rows.stream().filter(RecipeDeclaration::displayOnly)
                .collect(Collectors.groupingBy(RecipeDeclaration::map, Collectors.counting()));
        check(counts.equals(Map.of("ssite.recipe.aod", 3L, "ssite.recipe.large_aod", 3L, "ssite.recipe.var", 5L,
                        "ssite.recipe.blanket", 1L, "ssite.recipe.ion_implanter", 1L,
                        "ssite.recipe.electron_beam", 1L, "ssite.recipe.powder_bed", 1L)),
                "the two AOD sizes, VAR, the blanket vessel and the P-03 machines remain display-only groups");
        RecipeDeclaration var = row(rows, "var/ta4hfc5");
        check(var.itemOutputs().equals(List.of(material("dust", "TantalumHafniumCarbide", 10)))
                && var.fluidInputs().equals(List.of(new FluidRef("GenGas_Argon", 200))), "VAR display retains alloy and argon quantities");
        RecipeDeclaration small = row(rows, "ssite.recipe.aod", "argon");
        RecipeDeclaration large = row(rows, "ssite.recipe.large_aod", "argon");
        check(small.fluidInputs().equals(List.of(new FluidRef("GenGas_Argon", 10)))
                && large.fluidInputs().equals(List.of(new FluidRef("Oxygen", 10), new FluidRef("GenGas_Argon", 10))),
                "small and large AOD do not share the wrong gas inputs");
    }

    private static void ethanolConditions(List<RecipeDeclaration> rows) {
        List<RecipeDeclaration> conditional = rows.stream().filter(r -> !r.distinctFluids().isEmpty()).toList();
        check(conditional.stream().map(RecipeDeclaration::label).collect(Collectors.toSet()).equals(
                Set.of("c4/ethanol-dehydro-pure", "c4/ethanol-condense-pure", "c3/pva-pure")),
                "all three registry-dependent alternatives remain explicit");
        for (RecipeDeclaration row : conditional)
            check(row.distinctFluids().equals(List.of(new DistinctFluids("GenLiquid_Ethanol", "BioEthanol"))),
                    "pure ethanol alternatives retain the exact runtime identity guard");
        for (String label : List.of("c4/ethanol-dehydro", "c4/ethanol-condense", "c3/pva"))
            check(row(rows, label).distinctFluids().isEmpty(), "ordinary ethanol routes remain unconditional");
    }

    private static void vanillaItems(List<RecipeDeclaration> rows) {
        RecipeDeclaration sponge = row(rows, "c7/sponge");
        check(sponge.itemInputs().equals(List.of(new ItemRef(ItemKind.VANILLA, "", "", "minecraft:yellow_dye", 1, -1))),
                "vanilla dye remains a concrete item rather than an inferred material");
        check(sponge.itemOutputs().equals(List.of(new ItemRef(ItemKind.VANILLA, "", "", "minecraft:sponge", 1, -1))),
                "vanilla sponge output survives symbolic declaration");
    }

    private static void chancesAndFuelQuantities(List<RecipeDeclaration> rows) {
        RecipeDeclaration copper = row(rows, "m7/copper-electrorefine");
        check(copper.outputChances().equals(List.of(10000, 10000, 2500)), "electrorefining chance slots retain their order");
        check(copper.itemOutputs().equals(List.of(material("dust", "Copper", 2), material("dust", "CopperAnodeSlime", 1),
                material("dust", "PlatinumGroupConcentrate", 1))), "rare byproduct stays in the probabilistic slot");
        RecipeDeclaration pentane = row(rows, "fuel/pentane");
        check(pentane.itemOutputs().isEmpty() && pentane.perTick() == -48 && pentane.duration() == 7,
                "fuel declaration retains generation sign and duration without item products");
        check(pentane.fluidInputs().equals(List.of(new FluidRef("GenLiquid_Pentane", 1)))
                && pentane.fluidOutputs().equals(List.of(new FluidRef("CarbonDioxide", 1))), "fuel fluid units remain unchanged");
    }

    private static void immutableDeclarations(List<RecipeDeclaration> rows) throws Exception {
        expectUnsupported(() -> rows.clear(), "declaration list is immutable");
        RecipeDeclaration viton = row(rows, "c11/raw-viton");
        expectUnsupported(() -> viton.itemInputs().clear(), "item inputs are immutable");
        expectUnsupported(() -> viton.itemOutputs().clear(), "item outputs are immutable");
        expectUnsupported(() -> viton.fluidInputs().clear(), "fluid inputs are immutable");
        expectUnsupported(() -> viton.fluidOutputs().clear(), "fluid outputs are immutable");
        expectUnsupported(() -> viton.keptInputs().clear(), "kept slot metadata is immutable");
        expectUnsupported(() -> row(rows, "m7/copper-electrorefine").outputChances().clear(), "chances are immutable");
        expectUnsupported(() -> row(rows, "c3/pva-pure").distinctFluids().clear(), "conditions are immutable");
        List<ItemRef> mutable = new ArrayList<>(List.of(material("dust", "Carbon", 1)));
        RecipeDeclaration snapshot = fixture("copy", mutable, List.of(), List.of(), List.of(), List.of(), false, List.of());
        mutable.clear();
        check(snapshot.itemInputs().size() == 1, "caller-owned lists are defensively copied");
    }

    private static void repeatableDeclarations(List<RecipeDeclaration> rows) {
        check(rows.equals(ChemistryRecipes.declarations()), "repeated execution neither accumulates rows nor changes order or sources");
    }

    private static void formingAndCatalystEvidence(List<RecipeDeclaration> rows) {
        RecipeDeclaration forming = row(rows, "forming/polypropylene/plate");
        RecipeDeclaration curing = row(rows, "c8/vulcanize-rawstyrenebutadienerubber");
        List<MaterialReport.MaterialRow> analyzed = MaterialReport.analyze(List.of(
                MaterialDeclaration.builder("Polypropylene").dust().polymer().build()), List.of(forming, curing));
        MaterialReport.MaterialRow polymer = materialRow(analyzed, "Polypropylene");
        check(!polymer.uses().isEmpty() && polymer.uses().stream().allMatch(e -> e.category().equals("forming")),
                "forming does not make a polymer appear to have a process consumption route");
        check(!MaterialReport.hasConsumption(polymer), "a forming-only material remains in the review list");
        MaterialReport.MaterialRow zinc = materialRow(analyzed, "ZincOxide");
        check(!zinc.uses().isEmpty() && zinc.uses().stream().allMatch(e -> e.category().equals("catalyst")),
                "kept zinc oxide is reported as reusable tooling, not ongoing consumption");
        check(!MaterialReport.hasConsumption(zinc), "reusable catalyst is not reported as ongoing consumption");
        check(materialRow(analyzed, "RawStyreneButadieneRubber").uses().stream().anyMatch(e -> e.category().equals("process")),
                "the same curing recipe still consumes its rubber substrate");
    }

    private static void recyclingAndConditionalEvidence(List<RecipeDeclaration> rows) {
        RecipeDeclaration copper = row(rows, "m7/copper-electrorefine");
        check(MaterialReport.classify(copper, 0, true).equals("recycle"), "950 mB returned acid is classified as recycling");
        check(MaterialReport.classify(copper, 0, false).equals("process"), "acid recycling does not hide copper consumption");
        RecipeDeclaration conditional = row(rows, "c3/pva-pure");
        List<MaterialReport.MaterialRow> analyzed = MaterialReport.analyze(List.of(), List.of(conditional));
        MaterialReport.MaterialRow acetate = materialRow(analyzed, "PolyvinylAcetate");
        check(!acetate.uses().isEmpty() && acetate.uses().stream().allMatch(MaterialReport.Evidence::conditional),
                "registry-dependent consumption retains conditional evidence");
        check(!MaterialReport.hasConsumption(acetate), "conditional-only evidence cannot claim an unconditional sink");
    }

    private static void fuelAndDisplayEvidence(List<RecipeDeclaration> rows) {
        RecipeDeclaration fuel = row(rows, "fuel/pentane");
        RecipeDeclaration display = row(rows, "var/titanium");
        check(MaterialReport.classify(fuel, 0, true).equals("fuel"), "a recipe without item products can be a fuel sink");
        List<MaterialReport.MaterialRow> analyzed = MaterialReport.analyze(List.of(), List.of(display));
        MaterialReport.MaterialRow sponge = materialRow(analyzed, "TitaniumSponge");
        check(!sponge.uses().isEmpty() && sponge.uses().stream().allMatch(e -> e.category().equals("display")),
                "a behavior display remains visible without becoming an executable process recipe");
        check(!MaterialReport.hasConsumption(sponge), "display-only evidence does not pretend to be a normal recipe registration");
    }

    private static void thoriumFuelChain(List<RecipeDeclaration> rows) {
        RecipeDeclaration compact = row(rows, "nuclear/fuel-thorium-compact");
        check(compact.map().equals("gt.recipe.press")
                && compact.itemInputs().equals(List.of(material("dust", "ThoriumDioxide", 1),
                        material("dustTiny", "Graphite", 1)))
                && compact.itemOutputs().equals(List.of(material("dust", "ThoriumFuelCompact", 1))),
                "thorium fuel pressing keeps the oxide and the green compact as distinct items");
        RecipeDeclaration sinter = row(rows, "nuclear/fuel-thorium-sinter");
        check(sinter.map().equals("gt.recipe.roaster")
                && sinter.itemInputs().equals(List.of(material("dust", "ThoriumFuelCompact", 1)))
                && sinter.itemOutputs().equals(List.of(material("dust", "ThoriumFuelPellet", 1)))
                && sinter.fluidInputs().equals(List.of(new FluidRef("GenGas_Argon", 1000))),
                "thorium fuel sintering consumes the compact under argon");
        RecipeDeclaration encapsulate = row(rows, "nuclear/fuel-thorium-encapsulate");
        check(encapsulate.map().equals("gt.recipe.canner")
                && encapsulate.itemInputs().equals(List.of(
                        material("dust", "ThoriumFuelPellet", 1),
                        new ItemRef(ItemKind.VANILLA, "", "", "gt6ce:reactor_rod_9201", 1, -1)))
                && encapsulate.itemOutputs().equals(List.of(
                        new ItemRef(ItemKind.VANILLA, "", "", "gt6ce:fuel_rod_th_232", 1, -1)))
                && encapsulate.duration() == 16 && encapsulate.perTick() == 16,
                "the sintered pellet enters the host thorium rod through the canner");
    }

    private static void spentFuelChain(List<RecipeDeclaration> rows) {
        RecipeDeclaration disassemble = row(rows, "nuclear/spent-disassemble");
        check(disassemble.map().equals("gt.recipe.shredder")
                && disassemble.itemInputs().equals(List.of(
                        new ItemRef(ItemKind.VANILLA, "", "", "gt6ce:reactor_rod_9310", 1, -1)))
                && disassemble.itemOutputs().equals(List.of(
                        material("scrapGt", "Zirconium", 9), material("dust", "SpentThoriumFuel", 1)))
                && disassemble.fluidInputs().isEmpty() && disassemble.fluidOutputs().isEmpty(),
                "disassembly accepts only the independent depleted rod item and a fuel-free shredder row");
        RecipeDeclaration separate = row(rows, "nuclear/spent-separate");
        check(separate.map().equals("gt.recipe.centrifuge")
                && separate.itemInputs().equals(List.of(material("dust", "SpentThoriumFuel", 1)))
                && separate.itemOutputs().equals(List.of(
                        material("dustTiny", "Thorium", 1), material("dustDiv72", "Uranium", 6),
                        material("dustDiv72", "FissionProductResidue", 58)))
                && separate.fluidInputs().isEmpty(),
                "separation consumes one spent batch and returns the host thorium and uranium units");
        check(separate.itemOutputs().get(0).count() * 8
                + separate.itemOutputs().get(1).count()
                + separate.itemOutputs().get(2).count() == 72,
                "separation balances the 72-unit dust batch without creating nuclear material");
        RecipeDeclaration solidify = row(rows, "nuclear/spent-solidify");
        check(solidify.map().equals("gt.recipe.press")
                && solidify.itemInputs().equals(List.of(
                        material("dustDiv72", "FissionProductResidue", 58),
                        material("dust", "Glass", 3), material("dust", "Borax", 1)))
                && solidify.itemOutputs().equals(List.of(material("dust", "SolidifiedFissionResidue", 1))),
                "solidification consumes the residue with the glass matrix and emits a separate storage item");
        check(rows.stream().noneMatch(r -> !r.label().startsWith("nuclear/spent-")
                        && !r.label().startsWith("nuclear/blanket-")
                        && (r.itemInputs().stream().anyMatch(i -> i.material().equals("SpentThoriumFuel")
                                || i.material().equals("FissionProductResidue"))
                        || r.itemOutputs().stream().anyMatch(i -> i.material().equals("FissionProductResidue")))),
                "spent batches and residues have no route outside the spent-fuel and blanket groups");
        check(rows.stream().noneMatch(r -> r.itemOutputs().stream().anyMatch(i ->
                        i.kind() == ItemKind.VANILLA && i.itemId().equals("gt6ce:reactor_rod_9310"))),
                "no addon recipe manufactures a depleted rod");
    }

    private static void craftingInputsAndRemovalRules() throws Exception {
        withTemporaryDirectory(temp -> {
            Files.writeString(temp.resolve("part.json"), """
                    {"type":"minecraft:crafting_shaped","pattern":["ABA"," A "],
                     "key":{"A":{"item":"ssite:plate_fixture"},"B":{"tag":"forge:rods/fixture"}},
                     "result":{"item":"ssite:fixture_part","count":2}}
                    """, StandardCharsets.UTF_8);
            Files.writeString(temp.resolve("removed.json"), """
                    {"type":"gt6ce:machine_removal","map":"gt.recipe.mixer",
                     "item_inputs":[{"item":"ssite:plate_fixture"}],
                     "item_outputs":[{"item":"ssite:removed_output"}]}
                    """, StandardCharsets.UTF_8);
            List<String> diagnostics = new ArrayList<>();
            List<MaterialReport.CraftingRecipe> recipes = MaterialReport.readCrafting(temp, diagnostics);
            check(recipes.size() == 1, "removal predicates never become crafting consumption");
            MaterialReport.CraftingRecipe crafting = recipes.get(0);
            check(crafting.output().equals("ssite:fixture_part") && crafting.count() == 2, "crafting output quantity is retained");
            check(crafting.inputs().stream().anyMatch(i -> i.alternatives().equals(List.of("ssite:plate_fixture")) && i.count() == 3),
                    "shaped ingredient count comes from repeated pattern cells, not the key size");
            check(crafting.inputs().stream().anyMatch(i -> i.alternatives().equals(List.of("#forge:rods/fixture")) && i.count() == 1),
                    "ingredient tags stay unresolved alternatives");
        });
    }

    private static void shapelessAlternativesAndUnsupportedTypes() throws Exception {
        withTemporaryDirectory(temp -> {
            Files.writeString(temp.resolve("mixed.json"), """
                    {"type":"minecraft:crafting_shapeless",
                     "ingredients":[[{"item":"ssite:resin_a"},{"tag":"forge:resins"}],{"item":"ssite:fiber"}],
                     "result":{"id":"ssite:laminate","count":3}}
                    """, StandardCharsets.UTF_8);
            Files.writeString(temp.resolve("unknown.json"), """
                    {"type":"example:future_processing","ingredient":{"item":"ssite:fiber"},"result":"ssite:unknown"}
                    """, StandardCharsets.UTF_8);
            List<String> diagnostics = new ArrayList<>();
            List<MaterialReport.CraftingRecipe> recipes = MaterialReport.readCrafting(temp, diagnostics);
            check(recipes.size() == 1, "unsupported serializers do not masquerade as parsed crafting recipes");
            MaterialReport.CraftingRecipe recipe = recipes.get(0);
            check(recipe.output().equals("ssite:laminate") && recipe.count() == 3, "NeoForge-style result id retains its quantity");
            check(recipe.inputs().size() == 2 && recipe.inputs().get(0).count() == 1
                    && Set.copyOf(recipe.inputs().get(0).alternatives()).equals(Set.of("ssite:resin_a", "#forge:resins")),
                    "alternative ingredients remain one choice rather than two mandatory inputs");
            check(diagnostics.stream().anyMatch(d -> d.contains("example:future_processing")), "unknown recipe type has an explicit coverage diagnostic");
        });
    }

    private static void optionalNotesAndUnknownTargets() throws Exception {
        withTemporaryDirectory(temp -> {
            List<String> diagnostics = new ArrayList<>();
            Path notes = temp.resolve("notes.json");
            check(MaterialReport.readIntents(notes, Set.of("Polypropylene"), diagnostics).isEmpty(), "missing local notes do not block a report");
            Files.writeString(notes, """
                    {"version":1,"groups":[{"materials":["Polypropylene","MissingFutureMaterial"],
                     "stage":"M9","component":"fixture","destination":"external fixture",
                     "status":"external","source":"docs/inspiration.md"}]}
                    """, StandardCharsets.UTF_8);
            List<MaterialReport.Intent> intents = MaterialReport.readIntents(notes, Set.of("Polypropylene"), diagnostics);
            check(intents.size() == 2 && intents.stream().anyMatch(i -> i.material().equals("MissingFutureMaterial")),
                    "unknown planned targets stay visible rather than disappearing");
            check(diagnostics.stream().anyMatch(d -> d.contains("MissingFutureMaterial")), "unknown target has an explicit diagnostic");
            check(intents.stream().allMatch(i -> i.status().equals("external")), "external scope does not become an implemented use");
        });
    }

    private static void contributorCoverage(Path repo) throws Exception {
        MaterialReport.Catalog catalog = MaterialReport.collect(repo, "test");
        check(catalog.crucibleRecipes().size() == 4, "two iron sources and two nickel sources expand to four crucible declarations");
        check(catalog.crucibleRecipes().stream().allMatch(r -> r.output().equals("CarburizedStainlessSteel")),
                "crucible declarations retain coarse steel rather than the removed stainless output");
        MaterialReport.MaterialRow ferrochromium = materialRow(catalog.materials(), "Ferrochromium");
        check(ferrochromium.uses().stream().filter(e -> e.category().equals("crucible")).count() == 4,
                "crucible alloying contributes downstream evidence outside the chemistry DSL");
        MaterialReport.MaterialRow pipe = materialRow(catalog.materials(), "StainlessSteel316L");
        check(!pipe.capabilities().isEmpty(), "316L pipe declaration is retained as a capability");
        MaterialReport.MaterialRow fiber = materialRow(catalog.materials(), "CarbonFiber");
        check(fiber.forms().stream().anyMatch(f -> f.contains("wireFine")), "explicit material-form requests survive helper expansion");
        MaterialReport.MaterialRow ceramic = materialRow(catalog.materials(), "EngineeringCeramic");
        check(ceramic.forms().stream().anyMatch(f -> f.contains("ceramicClamp")),
                "the P-06 ceramic prefixes are reported as declared forms");
        MaterialReport.MaterialRow carbonCarbon = materialRow(catalog.materials(), "CarbonCarbon");
        check(carbonCarbon.forms().stream().anyMatch(f -> f.contains("carbonCarbonSetter")),
                "carbon-carbon is a material separate from the resin-based composite");
        MaterialReport.MaterialRow ablative = materialRow(catalog.materials(), "AblativeComposite");
        check(ablative.forms().stream().anyMatch(f -> f.contains("ablativeBacking")),
                "the qualified and spent backing identities are reported");
        MaterialReport.MaterialRow silicon = materialRow(catalog.materials(), "Silicon");
        check(silicon.forms().stream().anyMatch(f -> f.contains("wafer")),
                "addon crystal prefixes are reported next to host forms");
        check(catalog.craftingRecipes().stream().anyMatch(r -> r.output().equals("ssite:var_furnace")),
                "repository crafting recipes contribute machine-building evidence");
        check(catalog.craftingRecipes().stream().anyMatch(r -> r.output().equals("ssite:precision_laser_workstation")),
                "the P-01 workstation recipe makes the machine reachable without its own workpieces");
        check(catalog.craftingRecipes().stream().anyMatch(r -> r.output().equals("ssite:fiber_drawing_machine")),
                "the drawing machine is craftable without its own fiber output");
        MaterialReport.CraftingRecipe implanter = catalog.craftingRecipes().stream()
                .filter(r -> r.output().equals("ssite:ion_implanter")).findFirst()
                .orElseThrow(() -> new AssertionError("missing ion implanter crafting recipe"));
        Set<String> implanterInputs = implanter.inputs().stream()
                .flatMap(input -> input.alternatives().stream()).collect(Collectors.toSet());
        check(implanterInputs.containsAll(List.of("ssite:chip_chipelite",
                        "ssite:plate_precisioncasting", "gt6ce:casing_machine_stainlesssteel",
                        "ssite:pipe_small_stainless_steel_316l",
                        "ssite:ceramic_clamp_engineeringceramic")),
                "the ion implanter is craftable from M9 products and the P-06 ceramic clamp");
        check(implanterInputs.stream().noneMatch(id -> id.contains("advanced")
                        || id.contains("program_template") || id.contains("control_module")),
                "the first machine tree has no advanced chip or module prerequisite");
        MaterialReport.CraftingRecipe writer = catalog.craftingRecipes().stream()
                .filter(r -> r.output().equals("ssite:electron_beam_writer")).findFirst()
                .orElseThrow(() -> new AssertionError("missing electron-beam writer crafting recipe"));
        Set<String> writerInputs = writer.inputs().stream()
                .flatMap(input -> input.alternatives().stream()).collect(Collectors.toSet());
        check(writerInputs.containsAll(List.of("gt6ce:circuit_elite", "ssite:chip_chipelite",
                        "gt6ce:casing_machine_stainlesssteel",
                        "ssite:ceramic_clamp_engineeringceramic")),
                "the electron-beam writer uses existing Elite circuits, M9 parts and the ceramic clamp");
        check(writerInputs.stream().noneMatch(id -> id.contains("advanced")
                        || id.contains("program_template") || id.contains("control_module")),
                "the electron-beam writer has no advanced product prerequisite");
        MaterialReport.CraftingRecipe probe = catalog.craftingRecipes().stream()
                .filter(r -> r.output().equals("ssite:fiber_status_probe")).findFirst()
                .orElseThrow(() -> new AssertionError("missing probe crafting recipe"));
        Set<String> probeInputs = probe.inputs().stream()
                .flatMap(input -> input.alternatives().stream()).collect(Collectors.toSet());
        check(probeInputs.containsAll(List.of("ssite:fiber_spool_opticalfiber",
                        "ssite:instrument_shell_stainlesssteel316l", "ssite:chip_chipelite")),
                "the probe consumes the spool, the P-01 shell and the Elite circuit");
    }

    private static void malformedNotesFail() throws Exception {
        withTemporaryDirectory(temp -> {
            Path notes = temp.resolve("notes.json");
            Files.writeString(notes, "{broken", StandardCharsets.UTF_8);
            expectFailure(() -> MaterialReport.readIntents(notes, Set.of(), new ArrayList<>()), "malformed JSON cannot silently erase intentions");
            Files.writeString(notes, """
                    {"version":1,"groups":[{"materials":["Polypropylene"],"stage":"M9",
                     "component":"fixture","destination":"fixture","status":"complete","source":"docs/inspiration.md"}]}
                    """, StandardCharsets.UTF_8);
            expectFailure(() -> MaterialReport.readIntents(notes, Set.of("Polypropylene"), new ArrayList<>()),
                    "unsupported completion status is rejected rather than overriding code facts");
        });
    }

    private static void renderingAndDeterministicOutput(Path repo) throws Exception {
        withTemporaryDirectory(temp -> {
            String unsafe = "<script>alert('fixture')</script> | cell";
            MaterialReport.MaterialRow material = new MaterialReport.MaterialRow("material:Fixture", "material", "Fixture",
                    unsafe, "ssite", List.of("dust"), List.of(), List.of(), List.of(), List.of());
            MaterialReport.Catalog catalog = new MaterialReport.Catalog("test", List.of(material), List.of(), List.of(), List.of(), List.of());
            String html = MaterialReport.html(catalog, repo, temp);
            String markdown = MaterialReport.markdown(catalog, repo, temp);
            check(!html.contains(unsafe) && html.contains("&lt;script&gt;"), "HTML treats display names as text");
            check(!markdown.contains(unsafe) && (markdown.contains("\\| cell") || markdown.contains("&#124; cell")),
                    "Markdown names cannot split table columns or inject raw script markup");
            MaterialReport.write(catalog, repo, temp);
            List<String> names = List.of("index.html", "materials.md", "catalog.json");
            List<byte[]> first = new ArrayList<>();
            for (String name : names) first.add(Files.readAllBytes(temp.resolve(name)));
            MaterialReport.write(catalog, repo, temp);
            for (int i = 0; i < names.size(); i++)
                check(Arrays.equals(first.get(i), Files.readAllBytes(temp.resolve(names.get(i)))), "repeat output is deterministic: " + names.get(i));
        });
    }

    private static RecipeDeclaration row(List<RecipeDeclaration> rows, String label) {
        List<RecipeDeclaration> found = rows.stream().filter(r -> r.label().equals(label)).toList();
        check(found.size() == 1, "one declaration for " + label);
        return found.get(0);
    }

    private static RecipeDeclaration row(List<RecipeDeclaration> rows, String map, String label) {
        return row(rows.stream().filter(r -> r.map().equals(map)).toList(), label);
    }

    private static ItemRef material(String prefix, String name, int count) {
        return new ItemRef(ItemKind.MATERIAL, prefix, name, "", count, -1);
    }

    private static MaterialReport.MaterialRow materialRow(List<MaterialReport.MaterialRow> rows, String name) {
        return rows.stream().filter(r -> r.name().equals(name)).findFirst()
                .orElseThrow(() -> new AssertionError("missing material evidence: " + name));
    }

    private static RecipeDeclaration fixture(String label, List<ItemRef> inputs, List<ItemRef> outputs,
            List<FluidRef> fluidInputs, List<FluidRef> fluidOutputs, List<Integer> kept, boolean display,
            List<DistinctFluids> conditions) {
        return new RecipeDeclaration("gt.recipe.mixer", label, inputs, outputs, fluidInputs, fluidOutputs, 100, 16,
                List.of(), kept, display, conditions, new Source("fixture.java", 1));
    }

    private static void expectUnsupported(Action action, String message) throws Exception {
        try {
            action.run();
        } catch (UnsupportedOperationException expected) {
            return;
        }
        throw new AssertionError(message);
    }

    private static void expectFailure(Action action, String message) throws Exception {
        try {
            action.run();
        } catch (IllegalArgumentException | JsonParseException | IOException expected) {
            return;
        }
        throw new AssertionError(message);
    }

    private static void withTemporaryDirectory(TemporaryAction action) throws Exception {
        Path owned = Files.createTempDirectory("ssite-material-report-test-").toAbsolutePath().normalize();
        try {
            action.run(owned);
        } finally {
            try (var entries = Files.walk(owned)) {
                for (Path path : entries.sorted(Comparator.reverseOrder()).toList()) {
                    Path target = path.toAbsolutePath().normalize();
                    if (!target.startsWith(owned)) throw new IOException("Refusing to remove a path outside the test directory");
                    Files.deleteIfExists(target);
                }
            }
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    @FunctionalInterface
    private interface Action { void run() throws Exception; }

    @FunctionalInterface
    private interface TemporaryAction { void run(Path directory) throws Exception; }
}
