package com.lombinaxmasson.ssite.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.FluidRef;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.ItemKind;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.ItemRef;
import gregtech.api.addon.recipe.CrucibleReactionDeclaration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** Offline coverage, duplicate and consistency audit over the symbolic recipe declarations. */
public final class RecipeAudit {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String SCOPE = "本审计只读取 SSITE 的符号配方声明、本仓库合成 JSON 与坩埚反应，不启动 Minecraft，"
            + "也不扫描宿主自动生成的配方，因此不能判断本附属配方是否与宿主配方重复。展示行是机器行为说明，"
            + "不等于 GT 配方已经注册；保留槽不视为消费；选择器与合成标签不展开为具体物品；"
            + "合成中的 ssite 材料物品 ID 按前缀/材料命名约定映射到声明形态，命名不一致时会漏配；"
            + "自定义机器的实际 tick 行为可能超出声明。编译期审计不代替游戏内注册和生存验收。";
    private static final String CRUCIBLE_SOURCE = "common/src/main/resources/META-INF/services/"
            + "gregtech.api.addon.material.AddonMaterialContributor";
    private static final Comparator<Evidence> EVIDENCE_ORDER = Comparator.comparing(Evidence::category)
            .thenComparing(Evidence::label).thenComparing(Evidence::map)
            .thenComparing(Evidence::source).thenComparing(Evidence::conditional);

    private RecipeAudit() {}

    public record Evidence(String label, String map, String category, String source, boolean conditional) {}
    public record Flow(String key, String label, String kind, List<Evidence> producers, List<Evidence> consumers) {}
    public record Duplicate(String map, String signature, List<String> recipes) {}
    public record Issue(String category, String recipe, String map, String detail, String source) {}
    public record MapCount(String map, int executable, int display) {
        public int total() {
            return executable + display;
        }
    }
    public record UnusedMaterial(String name, List<String> forms, List<String> intents) {}
    public record DisplayRow(String map, String label, String source) {}
    public record Audit(String platform, int declarations, int executable, int display, int crafting, int crucible,
                        List<Flow> terminals, List<Flow> externalInputs, List<Duplicate> duplicates,
                        List<Issue> issues, List<MapCount> maps, List<UnusedMaterial> unusedMaterials,
                        List<DisplayRow> displayRows, List<String> diagnostics) {}

    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("Usage: RecipeAudit <repository> <output> <platform>");
        Path repository = Path.of(args[0]).toAbsolutePath().normalize();
        Path output = Path.of(args[1]).toAbsolutePath().normalize();
        MaterialReport.Catalog catalog = MaterialReport.collect(repository, args[2]);
        Audit audit = analyze(catalog);
        write(audit, repository, output);
        System.out.println("Recipe audit: " + audit.declarations() + " declarations ("
                + audit.executable() + " executable, " + audit.display() + " display), "
                + audit.crafting() + " crafting, " + audit.crucible() + " crucible; "
                + audit.terminals().size() + " terminal outputs, "
                + audit.externalInputs().size() + " external inputs, "
                + audit.duplicates().size() + " duplicate groups, "
                + audit.issues().size() + " structural issues, "
                + audit.unusedMaterials().size() + " unreferenced materials -> " + output);
    }

    public static Audit analyze(MaterialReport.Catalog catalog) {
        List<RecipeDeclaration> recipes = catalog.recipes();
        Map<String, FlowState> flows = new TreeMap<>();
        List<Issue> issues = new ArrayList<>();
        List<String> diagnostics = new ArrayList<>();
        List<DisplayRow> displayRows = new ArrayList<>();
        Set<String> identities = new LinkedHashSet<>();
        Set<String> tags = new TreeSet<>();
        Map<String, int[]> mapCounts = new TreeMap<>();
        Map<String, List<RecipeDeclaration>> duplicateGroups = new LinkedHashMap<>();
        int selectors = 0;

        Map<String, Set<Evidence>> materialProducers = new TreeMap<>();
        Map<String, Set<Evidence>> materialConsumers = new TreeMap<>();
        int crucibleIndex = 0;
        for (CrucibleReactionDeclaration reaction : catalog.crucibleRecipes()) {
            Evidence evidence = new Evidence("crucible/" + (++crucibleIndex) + "/" + reaction.output(),
                    "坩埚合金", "crucible", CRUCIBLE_SOURCE, false);
            materialProducers.computeIfAbsent(reaction.output(), key -> new TreeSet<>(EVIDENCE_ORDER)).add(evidence);
            for (var part : reaction.parts())
                materialConsumers.computeIfAbsent(part.material(), key -> new TreeSet<>(EVIDENCE_ORDER)).add(evidence);
        }

        for (RecipeDeclaration recipe : recipes) {
            String identity = recipe.map() + "/" + recipe.label();
            if (!identities.add(identity)) issue(issues, "重复身份", recipe, "同一配方表内出现同名声明");
            mapCounts.computeIfAbsent(recipe.map(), key -> new int[2])[recipe.displayOnly() ? 1 : 0]++;
            if (recipe.displayOnly()) displayRows.add(new DisplayRow(recipe.map(), recipe.label(), source(recipe)));

            Evidence evidence = recipeEvidence(recipe);
            for (ItemRef output : recipe.itemOutputs()) {
                if (output.kind() == ItemKind.SELECTOR) {
                    selectors++;
                    issue(issues, "选择器产物", recipe, "选择器不是具体物品产物，无法参与覆盖统计");
                    continue;
                }
                FlowState flow = itemFlow(flows, output);
                if (flow != null) flow.producers.add(evidence);
            }
            for (FluidRef output : recipe.fluidOutputs()) fluidFlow(flows, output.field()).producers.add(evidence);
            for (int i = 0; i < recipe.itemInputs().size(); i++) {
                if (recipe.keptInputs().contains(i)) continue;
                ItemRef input = recipe.itemInputs().get(i);
                if (input.kind() == ItemKind.SELECTOR) {
                    selectors++;
                    continue;
                }
                FlowState flow = itemFlow(flows, input);
                if (flow != null) flow.consumers.add(evidence);
            }
            for (FluidRef input : recipe.fluidInputs())
                fluidFlow(flows, input.field()).consumers.add(evidence);

            structuralChecks(recipe, issues);
            if (!recipe.displayOnly())
                duplicateGroups.computeIfAbsent(recipe.map() + "\u0000" + signature(recipe),
                        key -> new ArrayList<>()).add(recipe);
        }

        Map<String, String> materialIds = new HashMap<>();
        for (FlowState flow : flows.values()) {
            if (flow.prefix != null && flow.material != null)
                materialIds.put("ssite:" + snake(flow.prefix) + "_"
                        + flow.material.toLowerCase(Locale.ROOT), flow.key);
        }
        Set<String> mappedIds = new TreeSet<>();
        for (MaterialReport.CraftingRecipe recipe : catalog.craftingRecipes()) {
            Evidence evidence = new Evidence(recipe.id(), recipe.type(), "crafting", recipe.source(), false);
            FlowState output = craftingFlow(flows, materialIds, mappedIds, recipe.output());
            if (output != null) output.producers.add(evidence);
            for (MaterialReport.CraftingInput input : recipe.inputs()) {
                for (String alternative : input.alternatives()) {
                    if (alternative.startsWith("#")) {
                        tags.add(alternative);
                        continue;
                    }
                    FlowState flow = craftingFlow(flows, materialIds, mappedIds, alternative);
                    if (flow != null) flow.consumers.add(evidence);
                }
            }
        }

        for (FlowState flow : flows.values()) {
            if (flow.producers.isEmpty() && flow.material != null)
                flow.producers.addAll(materialProducers.getOrDefault(flow.material, Set.of()));
            if (flow.consumers.isEmpty() && flow.material != null)
                flow.consumers.addAll(materialConsumers.getOrDefault(flow.material, Set.of()));
        }

        List<Flow> terminals = flows.values().stream()
                .filter(flow -> !flow.producers.isEmpty() && flow.consumers.isEmpty())
                .sorted(Comparator.comparing((FlowState flow) -> flow.kind).thenComparing(flow -> flow.key))
                .map(FlowState::freeze).toList();
        List<Flow> externalInputs = flows.values().stream()
                .filter(flow -> flow.producers.isEmpty() && !flow.consumers.isEmpty())
                .sorted(Comparator.comparing((FlowState flow) -> flow.kind).thenComparing(flow -> flow.key))
                .map(FlowState::freeze).toList();
        List<Duplicate> duplicates = duplicateGroups.values().stream()
                .filter(group -> group.size() > 1)
                .map(group -> new Duplicate(group.get(0).map(), signature(group.get(0)),
                        group.stream().map(RecipeDeclaration::label).sorted().toList()))
                .sorted(Comparator.comparing(Duplicate::map).thenComparing(Duplicate::signature))
                .toList();
        List<MapCount> maps = mapCounts.entrySet().stream()
                .map(entry -> new MapCount(entry.getKey(), entry.getValue()[0], entry.getValue()[1]))
                .toList();
        List<UnusedMaterial> unused = unusedMaterials(catalog);
        issues.sort(Comparator.comparing(Issue::category).thenComparing(Issue::map)
                .thenComparing(Issue::recipe).thenComparing(Issue::detail));
        if (!tags.isEmpty()) diagnostics.add("合成输入含 " + tags.size() + " 个标签，未展开为具体物品："
                + String.join(", ", tags));
        if (!mappedIds.isEmpty()) diagnostics.add("合成中的 " + mappedIds.size()
                + " 个 ssite 材料物品 ID 按命名约定映射到声明形态；宿主命名不一致时会漏配");
        if (selectors > 0) diagnostics.add("配方中的 " + selectors + " 个选择器槽未纳入具体物品覆盖统计");
        if (!catalog.diagnostics().isEmpty())
            diagnostics.add("材料报告另有 " + catalog.diagnostics().size() + " 条覆盖提示，见材料报告");

        int display = (int) recipes.stream().filter(RecipeDeclaration::displayOnly).count();
        return new Audit(catalog.platform(), recipes.size(), recipes.size() - display, display,
                catalog.craftingRecipes().size(), catalog.crucibleRecipes().size(),
                terminals, externalInputs, duplicates, issues, maps, unused, List.copyOf(displayRows),
                List.copyOf(diagnostics));
    }

    private static void structuralChecks(RecipeDeclaration recipe, List<Issue> issues) {
        if (recipe.duration() <= 0) issue(issues, "非正时长", recipe, "duration=" + recipe.duration());
        if (recipe.perTick() == 0) issue(issues, "零能耗", recipe, "perTick=0");
        if (!recipe.displayOnly() && recipe.itemOutputs().isEmpty() && recipe.fluidOutputs().isEmpty()
                && recipe.perTick() >= 0)
            issue(issues, "无产物", recipe, "既无物品/流体输出，也不是产能配方");
        for (ItemRef item : recipe.itemInputs()) {
            if (item.count() <= 0) issue(issues, "非正物品数量", recipe, itemLabel(item) + " × " + item.count());
        }
        for (ItemRef item : recipe.itemOutputs()) {
            if (item.count() <= 0) issue(issues, "非正物品数量", recipe, itemLabel(item) + " × " + item.count());
        }
        for (FluidRef fluid : recipe.fluidInputs()) {
            if (fluid.amount() <= 0) issue(issues, "非正流体数量", recipe, fluid.field() + " " + fluid.amount() + " mB");
        }
        for (FluidRef fluid : recipe.fluidOutputs()) {
            if (fluid.amount() <= 0) issue(issues, "非正流体数量", recipe, fluid.field() + " " + fluid.amount() + " mB");
        }
        Set<Integer> kept = new TreeSet<>();
        for (int slot : recipe.keptInputs()) {
            if (slot < 0 || slot >= recipe.itemInputs().size())
                issue(issues, "保留槽越界", recipe, "slot " + slot + " / " + recipe.itemInputs().size());
            else if (!kept.add(slot)) issue(issues, "重复保留槽", recipe, "slot " + slot);
        }
        if (!recipe.outputChances().isEmpty()) {
            if (recipe.outputChances().size() > recipe.itemOutputs().size())
                issue(issues, "概率数量超出产物", recipe, recipe.outputChances().size() + " / "
                        + recipe.itemOutputs().size());
            for (int chance : recipe.outputChances()) {
                if (chance < 0 || chance > 10000) issue(issues, "概率越界", recipe, chance + "/10000");
            }
        }
    }

    private static void issue(List<Issue> issues, String category, RecipeDeclaration recipe, String detail) {
        issues.add(new Issue(category, recipe.label(), recipe.map(), detail, source(recipe)));
    }

    private static Evidence recipeEvidence(RecipeDeclaration recipe) {
        return new Evidence(recipe.label(), recipe.map(), recipe.displayOnly() ? "display" : "recipe",
                source(recipe), !recipe.distinctFluids().isEmpty());
    }

    private static FlowState itemFlow(Map<String, FlowState> flows, ItemRef item) {
        return switch (item.kind()) {
            case MATERIAL -> flows.computeIfAbsent("material|" + item.prefix() + "|" + item.material(),
                    key -> new FlowState(key, item.prefix() + " " + item.material(), "item",
                            item.prefix(), item.material()));
            case VANILLA -> rawItemFlow(flows, item.itemId());
            case SELECTOR -> null;
        };
    }

    private static FlowState rawItemFlow(Map<String, FlowState> flows, String id) {
        return flows.computeIfAbsent("item|" + id, key -> new FlowState(key, id, "item", null, null));
    }

    private static FlowState fluidFlow(Map<String, FlowState> flows, String field) {
        return flows.computeIfAbsent("fluid|" + field, key -> new FlowState(key, field, "fluid", null, null));
    }

    private static FlowState craftingFlow(Map<String, FlowState> flows, Map<String, String> materialIds,
                                          Set<String> mapped, String id) {
        String key = materialIds.get(id);
        if (key == null) return rawItemFlow(flows, id);
        mapped.add(id);
        return flows.get(key);
    }

    /** Prefix registry names use snake case; item ids append the lowercased material name. */
    private static String snake(String name) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            char previous = result.length() == 0 ? '\0' : name.charAt(i - 1);
            if (Character.isUpperCase(c)) {
                if (Character.isLowerCase(previous) && result.length() > 0) result.append('_');
                result.append(Character.toLowerCase(c));
            } else if (Character.isDigit(c)) {
                if (Character.isLetter(previous) && result.length() > 0) result.append('_');
                result.append(c);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private static String signature(RecipeDeclaration recipe) {
        return "in=" + itemRefs(recipe.itemInputs()) + " keep=" + recipe.keptInputs()
                + " out=" + itemRefs(recipe.itemOutputs()) + " chances=" + recipe.outputChances()
                + " fin=" + fluidRefs(recipe.fluidInputs()) + " fout=" + fluidRefs(recipe.fluidOutputs())
                + " " + recipe.duration() + "t/" + recipe.perTick() + "eu";
    }

    private static List<String> itemRefs(List<ItemRef> items) {
        return items.stream().map(item -> itemLabel(item) + "×" + item.count()).sorted().toList();
    }

    private static List<String> fluidRefs(List<FluidRef> fluids) {
        return fluids.stream().map(fluid -> fluid.field() + "×" + fluid.amount()).sorted().toList();
    }

    private static String itemLabel(ItemRef item) {
        return switch (item.kind()) {
            case MATERIAL -> item.prefix() + " " + item.material();
            case VANILLA -> item.itemId();
            case SELECTOR -> "selector " + item.selector();
        };
    }

    private static String source(RecipeDeclaration recipe) {
        return recipe.source().file() + (recipe.source().line() > 0 ? "#L" + recipe.source().line() : "");
    }

    private static List<UnusedMaterial> unusedMaterials(MaterialReport.Catalog catalog) {
        return catalog.materials().stream()
                .filter(row -> row.origin().equals("SSITE 材料声明"))
                .filter(row -> row.producers().isEmpty() && row.uses().isEmpty() && row.capabilities().isEmpty())
                .map(row -> new UnusedMaterial(row.name(), List.copyOf(row.forms()),
                        row.intents().stream().map(intent -> intent.stage() + " · " + intent.component()
                                + " → " + intent.destination()).toList()))
                .sorted(Comparator.comparing(UnusedMaterial::name))
                .toList();
    }

    private static final class FlowState {
        final String key;
        final String label;
        final String kind;
        final String prefix;
        final String material;
        final Set<Evidence> producers = new TreeSet<>(EVIDENCE_ORDER);
        final Set<Evidence> consumers = new TreeSet<>(EVIDENCE_ORDER);

        FlowState(String key, String label, String kind, String prefix, String material) {
            this.key = key;
            this.label = label;
            this.kind = kind;
            this.prefix = prefix;
            this.material = material;
        }

        Flow freeze() {
            return new Flow(key, label, kind, List.copyOf(producers), List.copyOf(consumers));
        }
    }

    public static void write(Audit audit, Path repository, Path output) throws IOException {
        Files.createDirectories(output);
        Files.writeString(output.resolve("recipe-audit.json"), JSON.toJson(audit) + "\n", StandardCharsets.UTF_8);
        Files.writeString(output.resolve("recipe-audit.md"), markdown(audit, repository, output),
                StandardCharsets.UTF_8);
    }

    public static String markdown(Audit audit, Path repository, Path output) {
        StringBuilder text = new StringBuilder("# 配方覆盖审计\n\n> 自动生成，勿手改。平台：");
        text.append(md(audit.platform())).append("。\n\n").append(SCOPE).append("\n\n")
                .append("统计：").append(audit.declarations()).append(" 条声明（").append(audit.executable())
                .append(" 可执行 / ").append(audit.display()).append(" 展示）；").append(audit.crafting())
                .append(" 条合成；").append(audit.crucible()).append(" 条坩埚；")
                .append(audit.terminals().size()).append(" 个终端产物；")
                .append(audit.externalInputs().size()).append(" 个外部供给输入；")
                .append(audit.duplicates().size()).append(" 组重复；")
                .append(audit.issues().size()).append(" 个结构异常；")
                .append(audit.unusedMaterials().size()).append(" 个未引用材料。\n\n");
        if (!audit.diagnostics().isEmpty()) {
            text.append("## 覆盖提示\n\n");
            for (String diagnostic : audit.diagnostics()) text.append("- ").append(md(diagnostic)).append('\n');
            text.append('\n');
        }
        text.append("## 结构异常（").append(audit.issues().size()).append("）\n\n");
        if (audit.issues().isEmpty()) {
            text.append("未发现。\n\n");
        } else {
            text.append("| 类别 | 配方 | 表 | 说明 | 来源 |\n|---|---|---|---|---|\n");
            for (Issue issue : audit.issues())
                text.append("| ").append(md(issue.category())).append(" | ").append(md(issue.recipe()))
                        .append(" | ").append(md(issue.map())).append(" | ").append(md(issue.detail()))
                        .append(" | ").append(sourceLink(repository, output, issue.source())).append(" |\n");
            text.append('\n');
        }
        text.append("## 重复声明（").append(audit.duplicates().size()).append(" 组）\n\n");
        if (audit.duplicates().isEmpty()) {
            text.append("未发现完全相同的可执行声明。\n\n");
        } else {
            text.append("| 表 | 配方 | 签名 |\n|---|---|---|\n");
            for (Duplicate duplicate : audit.duplicates())
                text.append("| ").append(md(duplicate.map())).append(" | ")
                        .append(md(String.join("、", duplicate.recipes()))).append(" | ")
                        .append(md(duplicate.signature())).append(" |\n");
            text.append('\n');
        }
        text.append("## 终端产物（").append(audit.terminals().size()).append("）\n\n")
                .append("有产出声明，但扫描范围内没有任何下游消费声明。展示行也计入证据并标注；"
                        + "不等于该产物在游戏中无用途。\n\n");
        if (audit.terminals().isEmpty()) {
            text.append("未发现。\n\n");
        } else {
            text.append("| 产物 | 类型 | 来源证据 |\n|---|---|---|\n");
            for (Flow flow : audit.terminals())
                text.append("| ").append(md(flow.label())).append(" | ").append(kindName(flow.kind()))
                        .append(" | ").append(evidenceLines(flow.producers(), repository, output)).append(" |\n");
            text.append('\n');
        }
        text.append("## 外部供给输入（").append(audit.externalInputs().size()).append("）\n\n")
                .append("被本附属配方消费，但扫描范围内没有本附属产出声明；通常来自宿主、原版或自定义机器的运行逻辑。"
                        + "坩埚材料级证据也会列出。\n\n");
        if (audit.externalInputs().isEmpty()) {
            text.append("未发现。\n\n");
        } else {
            text.append("| 输入 | 类型 | 消费证据 |\n|---|---|---|\n");
            for (Flow flow : audit.externalInputs())
                text.append("| ").append(md(flow.label())).append(" | ").append(kindName(flow.kind()))
                        .append(" | ").append(evidenceLines(flow.consumers(), repository, output)).append(" |\n");
            text.append('\n');
        }
        text.append("## 展示行（").append(audit.displayRows().size()).append("）\n\n")
                .append("这些声明只描述机器行为，不是 GT 机器配方表里的可执行行。\n\n");
        if (audit.displayRows().isEmpty()) {
            text.append("未发现。\n\n");
        } else {
            text.append("| 表 | 配方 | 来源 |\n|---|---|---|\n");
            for (DisplayRow row : audit.displayRows())
                text.append("| ").append(md(row.map())).append(" | ").append(md(row.label())).append(" | ")
                        .append(sourceLink(repository, output, row.source())).append(" |\n");
            text.append('\n');
        }
        text.append("## 声明但未引用的材料（").append(audit.unusedMaterials().size()).append("）\n\n")
                .append("SSITE 声明了材料，但扫描范围内没有产物、消费或部件能力证据。规划注记单列，不视为用途。\n\n");
        if (audit.unusedMaterials().isEmpty()) {
            text.append("未发现。\n\n");
        } else {
            text.append("| 材料 | 声明形态 | 规划注记 |\n|---|---|---|\n");
            for (UnusedMaterial material : audit.unusedMaterials())
                text.append("| ").append(md(material.name())).append(" | ").append(mdLines(material.forms()))
                        .append(" | ").append(mdLines(material.intents())).append(" |\n");
            text.append('\n');
        }
        text.append("## 配方表统计（").append(audit.maps().size()).append("）\n\n")
                .append("| 表 | 可执行 | 展示 | 合计 |\n|---|---|---|---|\n");
        for (MapCount map : audit.maps())
            text.append("| ").append(md(map.map())).append(" | ").append(map.executable()).append(" | ")
                    .append(map.display()).append(" | ").append(map.total()).append(" |\n");
        return text.toString();
    }

    private static String evidenceLines(List<Evidence> evidence, Path repository, Path output) {
        return evidence.stream().map(item -> {
            String link = sourceLink(repository, output, item.source());
            return md(item.label()) + "（" + md(item.map()) + "；" + categoryName(item.category())
                    + (item.conditional() ? "；宿主条件未判定" : "") + "）" + (link.isEmpty() ? "" : " " + link);
        }).collect(Collectors.joining("<br>"));
    }

    private static String kindName(String kind) {
        return switch (kind) {
            case "item" -> "物品";
            case "fluid" -> "流体";
            default -> kind;
        };
    }

    private static String categoryName(String category) {
        return switch (category) {
            case "recipe" -> "可执行配方";
            case "display" -> "展示行";
            case "crafting" -> "合成";
            case "crucible" -> "坩埚（材料级）";
            default -> category;
        };
    }

    private static String sourceLink(Path repository, Path output, String source) {
        if (source == null || source.isBlank() || source.startsWith("fixture")) return "";
        return "[来源](" + MaterialReport.link(repository, output, source) + ")";
    }

    private static String mdLines(List<String> values) {
        if (values.isEmpty()) return "";
        return values.stream().map(RecipeAudit::md).collect(Collectors.joining("<br>"));
    }

    private static String md(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("|", "&#124;").replace("\r", "").replace("\n", "<br>");
    }
}
