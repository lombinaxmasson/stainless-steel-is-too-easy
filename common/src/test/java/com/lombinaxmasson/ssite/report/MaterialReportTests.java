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
        craftingInputsAndRemovalRules();
        shapelessAlternativesAndUnsupportedTypes();
        optionalNotesAndUnknownTargets();
        malformedNotesFail();
        contributorCoverage(repo);
        renderingAndDeterministicOutput(repo);
        System.out.println("MaterialReportTests: all 21 checks passed.");
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
                "PackagingRecipes.java", "ElectrochemistryRecipes.java", "TextileRecipes.java",
                "InvestmentCastingRecipes.java", "OpticsRecipes.java")),
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

    private static void displayRowsRemainSeparate(List<RecipeDeclaration> rows) {
        Map<String, Long> counts = rows.stream().filter(RecipeDeclaration::displayOnly)
                .collect(Collectors.groupingBy(RecipeDeclaration::map, Collectors.counting()));
        check(counts.equals(Map.of("ssite.recipe.aod", 3L, "ssite.recipe.large_aod", 3L, "ssite.recipe.var", 5L)),
                "the two AOD sizes and VAR remain display-only groups");
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
                        && (r.itemInputs().stream().anyMatch(i -> i.material().equals("SpentThoriumFuel")
                                || i.material().equals("FissionProductResidue"))
                        || r.itemOutputs().stream().anyMatch(i -> i.material().equals("FissionProductResidue")))),
                "spent batches and residues have no route outside the spent-fuel group");
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
        MaterialReport.MaterialRow silicon = materialRow(catalog.materials(), "Silicon");
        check(silicon.forms().stream().anyMatch(f -> f.contains("wafer")),
                "addon crystal prefixes are reported next to host forms");
        check(catalog.craftingRecipes().stream().anyMatch(r -> r.output().equals("ssite:var_furnace")),
                "repository crafting recipes contribute machine-building evidence");
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
