package com.lombinaxmasson.ssite.chemistry;

import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.FluidRef;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.ItemKind;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.ItemRef;
import com.lombinaxmasson.ssite.machine.PrecisionMachines;
import com.lombinaxmasson.ssite.material.EngineeringMaterials;

import java.util.List;
import java.util.Set;

/** Pure JVM checks of the P-06 state chains and the P-01 high-load batch contract. */
public final class EngineeringMaterialTests {
    private static final String CERAMIC = EngineeringMaterials.CERAMIC;
    private static final String CARBON_CARBON = EngineeringMaterials.CARBON_CARBON;
    private static final String ABLATIVE = EngineeringMaterials.ABLATIVE;

    private EngineeringMaterialTests() {}

    public static void main(String[] args) {
        List<RecipeDeclaration> rows = ChemistryRecipes.declarations();
        ceramicChainIsForwardOnly(rows);
        carbonCarbonDensificationIsFinite(rows);
        ablativeBackingIsConsumableBatchTooling(rows);
        highLoadBatchAccountIsFrozen(rows);
        thermalToolingIsKept(rows);
        p03ModuleConsumesCeramicClamp(rows);
        compositeRoutesCannotConvertIntoEachOther(rows);
        System.out.println("EngineeringMaterialTests: all 7 checks passed.");
    }

    private static void ceramicChainIsForwardOnly(List<RecipeDeclaration> rows) {
        RecipeDeclaration batch = row(rows, "p06a/ceramic-batch");
        check(batch.itemInputs().equals(List.of(material("dust", "Alumina", 4)))
                        && batch.fluidInputs().equals(List.of(new FluidRef("DistW", 1000)))
                        && batch.itemOutputs().equals(List.of(material("ceramicBatch", CERAMIC, 4))),
                "the ceramic batch keeps the existing alumina source and a distinct batch id");
        RecipeDeclaration green = row(rows, "p06a/ceramic-green-body");
        check(green.itemInputs().equals(List.of(
                        material("ceramicBatch", CERAMIC, 2),
                        selector(1)))
                        && green.keptInputs().equals(List.of(1))
                        && green.itemOutputs().equals(List.of(material("ceramicGreenBody", CERAMIC, 1))),
                "the green body is pressed from the batch with a kept press program");
        RecipeDeclaration sinter = row(rows, "p06a/ceramic-sinter");
        check(sinter.itemInputs().equals(List.of(material("ceramicGreenBody", CERAMIC, 2)))
                        && sinter.itemOutputs().equals(List.of(
                        material("ceramicSintered", CERAMIC, 1),
                        material("dust", "Alumina", 1))),
                "sintering returns less alumina than it started from");
        RecipeDeclaration clamp = row(rows, "p06a/ceramic-clamp");
        check(clamp.itemInputs().equals(List.of(material("ceramicSintered", CERAMIC, 1)))
                        && clamp.itemOutputs().equals(List.of(material("ceramicClamp", CERAMIC, 1))),
                "only the sintered body is machined into the insulating clamp");
        check(rows.stream().noneMatch(r -> r.label().startsWith("p06a/")
                        && r.itemInputs().stream().anyMatch(i -> i.prefix().equals("ceramicClamp"))),
                "the finished clamp never feeds an earlier ceramic state");
        check(rows.stream().noneMatch(r -> r.label().startsWith("p06a/")
                        && r.itemInputs().stream().anyMatch(i -> i.prefix().equals("ceramicGreenBody"))
                        && r.itemOutputs().stream().anyMatch(i -> i.prefix().equals("ceramicClamp"))),
                "a green body cannot skip sintering");
    }

    private static void carbonCarbonDensificationIsFinite(List<RecipeDeclaration> rows) {
        RecipeDeclaration preform = row(rows, "p06b/preform");
        check(preform.itemInputs().contains(material("wireFine", "CarbonFiber", 16))
                        && preform.itemOutputs().equals(List.of(material("carbonFiberPreform", CARBON_CARBON, 1))),
                "the preform is built from the existing carbon fiber form");
        RecipeDeclaration impregnate = row(rows, "p06b/impregnate");
        check(impregnate.itemInputs().equals(List.of(
                        material("carbonFiberPreform", CARBON_CARBON, 1),
                        material("dust", "Carbon", 2)))
                        && impregnate.itemOutputs().equals(List.of(
                        material("carbonCarbonImpregnated", CARBON_CARBON, 1))),
                "impregnation has its own state id");
        RecipeDeclaration carbonize = row(rows, "p06b/carbonize");
        check(carbonize.itemInputs().equals(List.of(
                        material("carbonCarbonImpregnated", CARBON_CARBON, 1)))
                        && carbonize.itemOutputs().equals(List.of(
                        material("carbonCarbonCarbonized", CARBON_CARBON, 1))),
                "carbonization has its own state id");
        RecipeDeclaration densify = row(rows, "p06b/densify");
        check(densify.itemInputs().equals(List.of(
                        material("carbonCarbonCarbonized", CARBON_CARBON, 1),
                        material("dust", "Carbon", 2)))
                        && densify.itemOutputs().equals(List.of(
                        material("carbonCarbonDensified", CARBON_CARBON, 1))),
                "the second impregnation cycle is an explicit densified state");
        RecipeDeclaration setter = row(rows, "p06b/setter");
        check(setter.itemInputs().equals(List.of(material("carbonCarbonDensified", CARBON_CARBON, 1)))
                        && setter.itemOutputs().equals(List.of(material("carbonCarbonSetter", CARBON_CARBON, 1))),
                "only the densified body is finished into the kiln setter");
        Set<String> early = Set.of("carbonFiberPreform", "carbonCarbonImpregnated", "carbonCarbonCarbonized");
        Set<String> late = Set.of("carbonCarbonDensified", "carbonCarbonSetter");
        check(rows.stream().noneMatch(r -> r.label().startsWith("p06b/")
                        && r.itemInputs().stream().anyMatch(i -> late.contains(i.prefix()))
                        && r.itemOutputs().stream().anyMatch(i -> early.contains(i.prefix()))),
                "a densified or finished carbon-carbon part cannot be rewritten as an earlier state");
        check(rows.stream().noneMatch(r -> r.label().startsWith("p06b/")
                        && r.itemInputs().stream().anyMatch(i -> i.prefix().equals("carbonCarbonCarbonized"))
                        && r.itemOutputs().stream().anyMatch(i -> i.prefix().equals("carbonCarbonSetter"))),
                "a carbonized body cannot skip the densification cycle");
    }

    private static void ablativeBackingIsConsumableBatchTooling(List<RecipeDeclaration> rows) {
        RecipeDeclaration billet = row(rows, "p06c/billet");
        check(billet.itemInputs().equals(List.of(material("foil", "CarbonFiber", 4)))
                        && billet.fluidInputs().equals(List.of(new FluidRef("GenLiquid_PhenolicResin", 1000)))
                        && billet.itemOutputs().equals(List.of(material("ablativeBillet", ABLATIVE, 1))),
                "the ablative billet uses carbon fiber and phenolic resin");
        RecipeDeclaration cure = row(rows, "p06c/cure");
        check(cure.itemInputs().equals(List.of(
                        material("ablativeBillet", ABLATIVE, 1),
                        selector(2)))
                        && cure.keptInputs().equals(List.of(1))
                        && cure.itemOutputs().equals(List.of(material("ablativeBackingBlank", ABLATIVE, 1))),
                "curing produces a distinct backing blank with a kept cure program");
        RecipeDeclaration machine = row(rows, "p06c/machine");
        check(machine.itemInputs().equals(List.of(material("ablativeBackingBlank", ABLATIVE, 1)))
                        && machine.itemOutputs().equals(List.of(material("ablativeBacking", ABLATIVE, 1))),
                "machining produces the qualified backing identity");
        RecipeDeclaration spent = row(rows, "p06c/spent-recovery");
        check(spent.itemInputs().equals(List.of(material("ablativeBackingSpent", ABLATIVE, 1)))
                        && spent.itemOutputs().equals(List.of(material("dust", "Carbon", 1))),
                "a spent backing has a residue row and returns only part of its carbon");
        check(!row(rows, "p06c/machine").itemInputs().stream()
                        .anyMatch(i -> i.prefix().equals("ablativeBackingSpent")),
                "a spent backing cannot become a qualified backing again");
        check(rows.stream().noneMatch(r -> r.itemInputs().stream()
                        .anyMatch(i -> i.prefix().equals("ablativeBackingSpent"))
                        && r.itemOutputs().stream().anyMatch(i -> i.prefix().equals("ablativeBacking")
                        || i.prefix().equals("ablativeBackingBlank"))),
                "no row turns the spent backing back into a new backing or blank");
    }

    private static void highLoadBatchAccountIsFrozen(List<RecipeDeclaration> rows) {
        RecipeDeclaration base = row(rows, "ssite.recipe.precision_laser", "p01/drill-nozzle");
        RecipeDeclaration high = row(rows, "ssite.recipe.precision_laser", "p06/high-load-drill");
        check(high.itemInputs().equals(List.of(
                        material("plate", "StainlessSteel316L", 6),
                        material("ablativeBacking", ABLATIVE, 1))),
                "the high-load row freezes six plates and one ablative backing");
        check(high.keptInputs().isEmpty(),
                "the backing is the replaceable batch consumable, not a retained tool");
        check(high.fluidInputs().equals(List.of(new FluidRef("GenGas_Argon", 6000))),
                "six nozzles use six argon charges");
        check(high.itemOutputs().equals(List.of(
                        material("precisionNozzle", "StainlessSteel316L", 6),
                        material("ablativeBackingSpent", ABLATIVE, 1))),
                "one backing is consumed per batch and exactly one spent backing is produced");
        check(high.duration() == base.duration() * 6 && high.perTick() == base.perTick(),
                "per-nozzle processing time is unchanged");
        check(high.duration() * high.perTick() == base.duration() * base.perTick() * 6,
                "the batch spends exactly six single-piece energies");
        check(PrecisionMachines.INPUT_ITEM_SLOTS == 2
                        && PrecisionMachines.OUTPUT_ITEM_SLOTS == 2
                        && PrecisionMachines.NEEDS_OUTPUTS,
                "the workstation freezes two input and two output stacks and requires output room");
    }

    private static void thermalToolingIsKept(List<RecipeDeclaration> rows) {
        RecipeDeclaration heat = row(rows, "p05/heat-treat");
        check(heat.itemInputs().equals(List.of(
                        material("desupportedBlank", "StainlessSteel316L", 1),
                        material("carbonCarbonSetter", CARBON_CARBON, 1)))
                        && heat.keptInputs().equals(List.of(1)),
                "the carbon-carbon setter is retained kiln furniture");
        check(heat.itemInputs().get(0).prefix().equals("desupportedBlank"),
                "only the desupported blank is heat treated");
    }

    private static void p03ModuleConsumesCeramicClamp(List<RecipeDeclaration> rows) {
        RecipeDeclaration module = row(rows, "p03/control-module");
        check(module.itemInputs().stream().anyMatch(i -> i.prefix().equals("ceramicClamp")
                        && i.material().equals(CERAMIC)),
                "the P-03 module consumes the insulating ceramic clamp");
        check(module.itemInputs().stream().noneMatch(i -> i.prefix().equals("precisionFixture")),
                "the temporary precision fixture input was replaced");
    }

    private static void compositeRoutesCannotConvertIntoEachOther(List<RecipeDeclaration> rows) {
        List<RecipeDeclaration> p06 = rows.stream().filter(r -> r.label().startsWith("p06")).toList();
        check(p06.stream().noneMatch(r -> r.itemInputs().stream()
                        .anyMatch(i -> i.material().equals("CarbonFiberComposite"))
                        || r.itemOutputs().stream().anyMatch(i -> i.material().equals("CarbonFiberComposite"))),
                "carbon-carbon stays separate from the resin-based carbon fiber composite");
        Set<String> carbonCarbon = Set.of("carbonFiberPreform", "carbonCarbonImpregnated",
                "carbonCarbonCarbonized", "carbonCarbonDensified", "carbonCarbonSetter");
        Set<String> ablative = Set.of("ablativeBillet", "ablativeBackingBlank", "ablativeBacking",
                "ablativeBackingSpent");
        check(p06.stream().noneMatch(r -> r.itemInputs().stream().anyMatch(i -> carbonCarbon.contains(i.prefix()))
                        && r.itemOutputs().stream().anyMatch(i -> ablative.contains(i.prefix()))),
                "no carbon-carbon state converts into an ablative state");
        check(p06.stream().noneMatch(r -> r.itemInputs().stream().anyMatch(i -> ablative.contains(i.prefix()))
                        && r.itemOutputs().stream().anyMatch(i -> carbonCarbon.contains(i.prefix()))),
                "no ablative state converts into a carbon-carbon state");
        check(p06.stream().filter(r -> !r.label().startsWith("p06/"))
                        .noneMatch(r -> r.itemOutputs().stream().anyMatch(i ->
                                i.material().equals("StainlessSteel316L") || i.material().equals("Titanium")
                                        || i.material().equals("Aluminium"))),
                "the P-06 material routes do not mint an existing alloy");
    }

    private static RecipeDeclaration row(List<RecipeDeclaration> rows, String label) {
        List<RecipeDeclaration> found = rows.stream().filter(r -> r.label().equals(label)).toList();
        check(found.size() == 1, "one declaration for " + label);
        return found.get(0);
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

    private static ItemRef selector(int number) {
        return new ItemRef(ItemKind.SELECTOR, "", "", "", 1, number);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
