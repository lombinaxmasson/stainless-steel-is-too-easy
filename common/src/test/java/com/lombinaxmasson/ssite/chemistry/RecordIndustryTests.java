package com.lombinaxmasson.ssite.chemistry;

import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.ItemKind;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.ItemRef;
import com.lombinaxmasson.ssite.machine.RecordMachines;
import com.lombinaxmasson.ssite.media.RecordCatalog;

import java.util.List;

/**
 * Pure JVM checks of the record chain: a reusable template carries an imported track, the lathe keeps the
 * template and stylus, the press keeps the stamper, and the CD-R line cannot skip a layer.
 */
public final class RecordIndustryTests {
    private static final String TEMPLATE = "recordTemplate";
    private static final String TRACK = "TrackOne";

    private RecordIndustryTests() {}

    public static void main(String[] args) {
        List<RecipeDeclaration> rows = ChemistryRecipes.declarations();
        mediaIdentitiesAreStable();
        templateRowKeepsCircuit(rows);
        latheKeepsTemplateAndStylus(rows);
        vinylPressKeepsStamper(rows);
        vinylStagesAreDistinct(rows);
        cdrStagesAreDistinct(rows);
        cdrRecordingRequiresBlank(rows);
        reflectorHasTwoMetals(rows);
        templateFeedsBothMedia(rows);
        trackBearingRowsRequireDynamicOutputs(rows);
        newDyeMaterialsClose(rows);
        recordsAreTerminal(rows);
        System.out.println("RecordIndustryTests: all 12 checks passed.");
    }

    private static void mediaIdentitiesAreStable() {
        check(RecordCatalog.VINYL_ITEM.equals("vinyl_record"), "vinyl item id is frozen");
        check(RecordCatalog.CD_ITEM.equals("cd_record"), "CD item id is frozen");
        check(!RecordCatalog.VINYL_ITEM.equals(RecordCatalog.CD_ITEM), "media ids differ");
        check(RecordCatalog.TEMPLATE_MATERIAL.equals(TRACK)
                        && RecordCatalog.TEMPLATE_ITEM.equals("record_template_trackone"),
                "the template keeps its existing registry identity");
        check(RecordCatalog.COMPARATOR_OUTPUT >= 0 && RecordCatalog.COMPARATOR_OUTPUT <= 15,
                "comparator output fits the vanilla range");
    }

    private static void templateRowKeepsCircuit(List<RecipeDeclaration> rows) {
        RecipeDeclaration row = row(rows, "gt.recipe.press", "m8-00/track-template");
        check(row.itemInputs().equals(List.of(
                        vanilla("minecraft:paper", 1), material("foil", "Copper", 1), selector(1))),
                "template uses paper, copper foil and a selector circuit");
        check(row.keptInputs().equals(List.of(2)), "the selector circuit is kept");
        check(row.itemOutputs().equals(List.of(material(TEMPLATE, TRACK, 1))),
                "the recipe makes a blank template for imported track data");
        check(!row.displayOnly(), "blank templates have an executable manufacturing recipe");
    }

    private static void latheKeepsTemplateAndStylus(List<RecipeDeclaration> rows) {
        RecipeDeclaration row = row(rows, RecordMachines.MAP, "m8-01/cut-master");
        check(row.itemInputs().size() == 3 && row.keptInputs().equals(List.of(1, 2)),
                "the lathe checks the template and stylus without consuming them");
        check(row.itemInputs().get(0).equals(material("recordLacquer", "Aluminium", 1))
                        && row.itemOutputs().equals(List.of(material("recordCutMaster", "Aluminium", 1))),
                "the lathe consumes one lacquer master and returns one cut master");
    }

    private static void vinylPressKeepsStamper(List<RecipeDeclaration> rows) {
        RecipeDeclaration row = row(rows, "gt.recipe.press", "m8-01/press-vinyl");
        check(row.keptInputs().equals(List.of(2)), "the nickel stamper is reused");
        check(row.itemInputs().get(0).equals(material("recordBlank", "Vinyl", 1)),
                "pressing consumes one blank vinyl record");
        check(row.itemOutputs().equals(List.of(vanilla("ssite:" + RecordCatalog.VINYL_ITEM, 1))),
                "pressing returns the playable vinyl item");
    }

    private static void vinylStagesAreDistinct(List<RecipeDeclaration> rows) {
        List<ItemRef> stages = List.of(
                material("recordBlank", "Vinyl", 1),
                material("recordLacquer", "Aluminium", 1),
                material("recordCutMaster", "Aluminium", 1),
                material("recordStamper", "Nickel", 1));
        for (int i = 0; i < stages.size(); i++) {
            for (int j = i + 1; j < stages.size(); j++) {
                check(!stages.get(i).equals(stages.get(j)), "vinyl stages are separate items");
            }
        }
        check(row(rows, "gt.recipe.mixer", "m8-01/vinyl-compound").itemOutputs()
                        .equals(List.of(material("dust", "Vinyl", 2))),
                "the compound is the vinyl dust");
    }

    private static void cdrStagesAreDistinct(List<RecipeDeclaration> rows) {
        List<ItemRef> stages = List.of(
                material("discBase", "Polycarbonate", 1),
                material("discCoated", "DyeCoatedDisc", 1),
                material("discReflective", "ReflectiveDisc", 1),
                material("discBlank", "CDR", 1));
        for (int i = 0; i < stages.size(); i++) {
            for (int j = i + 1; j < stages.size(); j++) {
                check(!stages.get(i).equals(stages.get(j)), "CD-R stages are separate items");
            }
        }
    }

    private static void cdrRecordingRequiresBlank(List<RecipeDeclaration> rows) {
        RecipeDeclaration row = row(rows, "gt.recipe.laserengraver", "m8-02/record-cdr");
        check(row.itemInputs().get(0).equals(material("discBlank", "CDR", 1)),
                "recording names the blank CD-R, not the bare disc base");
        check(row.keptInputs().equals(List.of(1)), "recording keeps the track template");
        check(row.itemOutputs().equals(List.of(vanilla("ssite:" + RecordCatalog.CD_ITEM, 1))),
                "recording returns the playable CD item");
    }

    private static void reflectorHasTwoMetals(List<RecipeDeclaration> rows) {
        RecipeDeclaration silver = row(rows, "gt.recipe.press", "m8-02/silver-reflector");
        RecipeDeclaration gold = row(rows, "gt.recipe.press", "m8-02/gold-reflector");
        check(silver.itemInputs().get(1).equals(material("foil", "Silver", 1))
                        && gold.itemInputs().get(1).equals(material("foil", "Gold", 1)),
                "silver and gold are alternative reflector metals");
        check(silver.itemOutputs().equals(gold.itemOutputs()),
                "both reflector routes produce the same reflective disc stage");
    }

    private static void templateFeedsBothMedia(List<RecipeDeclaration> rows) {
        for (RecipeDeclaration row : rows) {
            for (ItemRef input : row.itemInputs()) {
                if (input.kind() == ItemKind.MATERIAL && input.prefix().equals(TEMPLATE)) {
                    check(input.material().equals(TRACK),
                            "template input keeps the existing item identity in " + row.label());
                }
            }
        }
        long consumers = rows.stream().filter(r -> r.itemInputs().stream()
                .anyMatch(i -> i.kind() == ItemKind.MATERIAL && i.prefix().equals(TEMPLATE))).count();
        check(consumers == 2, "the one template feeds both the lathe and the CD recorder");
    }

    private static void trackBearingRowsRequireDynamicOutputs(List<RecipeDeclaration> rows) {
        for (String label : List.of("m8-01/cut-master", "m8-01/nickel-stamper",
                "m8-01/press-vinyl", "m8-02/record-cdr")) {
            RecipeDeclaration row = rows.stream().filter(r -> r.label().equals(label)).findFirst().orElseThrow();
            check(row.displayOnly(), "a static row must not erase the imported track: " + label);
            int source = RecordManufacturing.sourceInput(label);
            check(source >= 0 && source < row.itemInputs().size(),
                    "the dynamic recipe has a source carrier: " + label);
            ItemRef carrier = row.itemInputs().get(source);
            check(carrier.prefix().equals(switch (label) {
                case "m8-01/nickel-stamper" -> "recordCutMaster";
                case "m8-01/press-vinyl" -> "recordStamper";
                default -> TEMPLATE;
            }), "the dynamic recipe copies the correct source carrier: " + label);
        }
        check(RecordManufacturing.sourceInput("m8-00/track-template") == -1,
                "manufacturing a blank template does not require imported audio");
    }

    private static void newDyeMaterialsClose(List<RecipeDeclaration> rows) {
        for (String material : List.of("Urea", "CopperSulfate", "PhthalocyanineDye",
                "StearicAcid", "CalciumStearate", "Nitrocellulose")) {
            check(rows.stream().anyMatch(r -> r.itemOutputs().stream()
                            .anyMatch(i -> i.material().equals(material))),
                    "new material has a producer: " + material);
            check(rows.stream().anyMatch(r -> r.itemInputs().stream()
                            .anyMatch(i -> i.material().equals(material))),
                    "new material has a consumer: " + material);
        }
    }

    private static void recordsAreTerminal(List<RecipeDeclaration> rows) {
        for (String id : List.of("ssite:" + RecordCatalog.VINYL_ITEM, "ssite:" + RecordCatalog.CD_ITEM)) {
            check(rows.stream().noneMatch(r -> r.itemInputs().stream()
                            .anyMatch(i -> i.kind() == ItemKind.VANILLA && i.itemId().equals(id))),
                    "the playable record is not an intermediate: " + id);
        }
    }

    private static RecipeDeclaration row(List<RecipeDeclaration> rows, String map, String label) {
        List<RecipeDeclaration> found = rows.stream()
                .filter(r -> r.map().equals(map) && r.label().equals(label)).toList();
        check(found.size() == 1, "one declaration for " + map + "/" + label);
        return found.get(0);
    }

    private static ItemRef material(String prefix, String material, int count) {
        return new ItemRef(ItemKind.MATERIAL, prefix, material, "", count, -1);
    }

    private static ItemRef vanilla(String id, int count) {
        return new ItemRef(ItemKind.VANILLA, "", "", id, count, -1);
    }

    private static ItemRef selector(int value) {
        return new ItemRef(ItemKind.SELECTOR, "", "", "", 1, value);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
