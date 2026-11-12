package com.lombinaxmasson.ssite.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lombinaxmasson.ssite.chemistry.ChemistryRecipes;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.ItemKind;
import com.lombinaxmasson.ssite.chemistry.RecipeDeclaration.ItemRef;
import gregtech.api.addon.machine.AddonMachineContributor;
import gregtech.api.addon.material.AddonMaterialContributor;
import gregtech.api.addon.material.MaterialDeclaration;
import gregtech.api.addon.material.MaterialFormRequest;
import gregtech.api.addon.material.PrefixDeclaration;
import gregtech.api.addon.pipe.FluidPipeDeclaration;
import gregtech.api.addon.recipe.CrucibleReactionDeclaration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** Offline facts come from the same declarations the game registers; intentions remain separate. */
public final class MaterialReport {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String RESOURCE_ROOT = "common/src/main/resources/";
    private static final String SCOPE = "本表执行 SSITE 的材料与配方声明，展开循环和辅助方法，并读取本仓库合成 JSON、坩埚合金与管道声明。"
            + "宿主自动生成配方、其他模组及机器 tick 行为未全量扫描；声明形态不代表宿主已经生成成功。"
            + "展示行只是行为工艺说明，条件配方仍需宿主注册表判定。编译报告不代替游戏内注册和生存验收。";

    private MaterialReport() {}

    public record Evidence(String category, String recipe, String map, List<String> outputs,
                           String source, boolean conditional) {}
    public record Intent(String material, String stage, String component, String destination,
                         String status, String source) {}
    public record CraftingInput(List<String> alternatives, int count) {}
    public record CraftingRecipe(String id, String type, List<CraftingInput> inputs,
                                 String output, int count, String source) {}
    public record MaterialRow(String key, String kind, String name, String displayName, String origin,
                              List<String> forms, List<Evidence> producers, List<Evidence> uses,
                              List<String> capabilities, List<Intent> intents) {}
    public record Catalog(String platform, List<MaterialRow> materials, List<RecipeDeclaration> recipes,
                          List<CrucibleReactionDeclaration> crucibleRecipes,
                          List<CraftingRecipe> craftingRecipes, List<String> diagnostics) {}

    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("Usage: MaterialReport <repository> <output> <platform>");
        Path repository = Path.of(args[0]).toAbsolutePath().normalize();
        Path output = Path.of(args[1]).toAbsolutePath().normalize();
        Catalog catalog = collect(repository, args[2]);
        write(catalog, repository, output);
        System.out.println("Material report: " + catalog.materials().size() + " entries, "
                + catalog.recipes().size() + " machine/display declarations, "
                + catalog.craftingRecipes().size() + " crafting recipes, "
                + catalog.diagnostics().size() + " diagnostics -> " + output);
    }

    public static Catalog collect(Path repository, String platform) throws Exception {
        List<String> diagnostics = new ArrayList<>();
        List<MaterialDeclaration> declarations = new ArrayList<>();
        List<MaterialFormRequest> forms = new ArrayList<>();
        List<PrefixDeclaration> prefixes = new ArrayList<>();
        List<CrucibleReactionDeclaration> crucibles = new ArrayList<>();
        for (AddonMaterialContributor contributor : contributors(repository, AddonMaterialContributor.class)) {
            declarations.addAll(contributor.materials());
            forms.addAll(contributor.forms());
            prefixes.addAll(contributor.prefixes());
            crucibles.addAll(contributor.crucibleReactions());
        }
        List<FluidPipeDeclaration> pipes = new ArrayList<>();
        for (AddonMachineContributor contributor : contributors(repository, AddonMachineContributor.class)) {
            pipes.addAll(contributor.fluidPipes());
        }
        List<RecipeDeclaration> recipes = ChemistryRecipes.declarations();
        Map<String, MutableRow> rows = buildRows(declarations, recipes);
        for (MaterialFormRequest form : forms) {
            material(rows, form.materialName()).forms.add("声明形态 " + form.prefixName());
        }
        for (PrefixDeclaration prefix : prefixes) {
            for (String material : prefix.materials()) {
                material(rows, material).forms.add("声明前缀 " + prefix.name());
            }
        }
        int crucibleIndex = 0;
        for (CrucibleReactionDeclaration reaction : crucibles) {
            String id = "crucible/" + (++crucibleIndex) + "/" + reaction.output();
            List<String> outputs = List.of(reaction.output() + " × " + reaction.yield() + "（比例）");
            Evidence evidence = new Evidence("crucible", id, "坩埚合金声明", outputs,
                    RESOURCE_ROOT + "META-INF/services/" + AddonMaterialContributor.class.getName(), false);
            material(rows, reaction.output()).producers.add(evidence);
            for (var part : reaction.parts()) material(rows, part.material()).uses.add(evidence);
        }
        for (FluidPipeDeclaration pipe : pipes) {
            material(rows, pipe.material()).capabilities.add("流体管道 " + pipe.idSuffix()
                    + "；基础容量 " + pipe.baseCapacity() + "；耐酸 " + pipe.acidProof()
                    + "；气密 " + pipe.gasProof() + "（声明能力，宿主生成未展开）");
        }
        List<CraftingRecipe> crafting = readCrafting(repository.resolve(RESOURCE_ROOT + "data/ssite/recipes"), diagnostics);
        for (CraftingRecipe recipe : crafting) {
            Evidence produced = new Evidence("crafting", recipe.id(), recipe.type(),
                    List.of(recipe.output() + " × " + recipe.count()), recipe.source(), false);
            rawItem(rows, recipe.output()).producers.add(produced);
            for (CraftingInput input : recipe.inputs()) {
                for (String alternative : input.alternatives()) {
                    Evidence use = new Evidence(input.alternatives().size() > 1 ? "crafting-alternative" : "crafting",
                            recipe.id(), recipe.type(), produced.outputs(), recipe.source(), false);
                    rawItem(rows, alternative).uses.add(use);
                }
            }
        }
        Set<String> known = rows.values().stream().filter(row -> row.kind.equals("material"))
                .map(row -> row.name).collect(Collectors.toCollection(TreeSet::new));
        Set<String> missingSources = new TreeSet<>();
        for (Intent intent : readIntents(repository.resolve("docs/material-uses.json"), known, diagnostics)) {
            if (!Files.isRegularFile(repository.resolve(intent.source().split("#", 2)[0])))
                missingSources.add(intent.source());
            MutableRow row = rows.get("material:" + intent.material());
            if (row == null) {
                row = new MutableRow("planned:" + intent.material(), "planned", intent.material(), "仅规划注记；声明未找到");
                rows.putIfAbsent(row.key, row);
                row = rows.get(row.key);
            }
            row.intents.add(intent);
        }
        for (String source : missingSources) diagnostics.add("规划依据文件不存在：" + source);
        Map<String, String> translations = translations(repository);
        for (MutableRow row : rows.values()) {
            if (row.kind.equals("material")) {
                row.displayName = translations.getOrDefault("material.gt6ce." + row.name.toLowerCase(Locale.ROOT), row.displayName);
            } else if (row.kind.equals("item")) {
                String[] id = row.name.split(":", 2);
                if (id.length == 2) row.displayName = translations.getOrDefault("item." + id[0] + "." + id[1],
                        translations.getOrDefault("block." + id[0] + "." + id[1], row.name));
            }
        }
        return new Catalog(platform, freeze(rows), List.copyOf(recipes), List.copyOf(crucibles),
                List.copyOf(crafting), List.copyOf(diagnostics));
    }

    private static <T> List<T> contributors(Path repository, Class<T> contract) throws Exception {
        Path file = repository.resolve(RESOURCE_ROOT + "META-INF/services/" + contract.getName());
        if (!Files.isRegularFile(file)) throw new IOException("Missing contributor list: " + file);
        List<T> found = new ArrayList<>();
        Set<String> names = new TreeSet<>();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String name = line.split("#", 2)[0].trim();
            if (!name.isEmpty() && names.add(name))
                found.add(contract.cast(Class.forName(name).getDeclaredConstructor().newInstance()));
        }
        return found;
    }

    /** A minimal fixture entry point; production collection also adds forms, crafting, pipes and intentions. */
    public static List<MaterialRow> analyze(List<MaterialDeclaration> declarations, List<RecipeDeclaration> recipes) {
        return freeze(buildRows(declarations, recipes));
    }

    private static Map<String, MutableRow> buildRows(List<MaterialDeclaration> declarations,
                                                    List<RecipeDeclaration> recipes) {
        Map<String, MutableRow> rows = new TreeMap<>();
        Set<String> declared = new TreeSet<>();
        for (MaterialDeclaration declaration : declarations) {
            if (!declared.add(declaration.name())) throw new IllegalArgumentException("Duplicate material: " + declaration.name());
            MutableRow row = material(rows, declaration.name());
            row.origin = "SSITE 材料声明";
            row.displayName = declaration.displayName();
            declaration.features().stream().sorted().forEach(feature -> row.forms.add("声明特征 " + feature.name()));
        }
        for (RecipeDeclaration recipe : recipes) {
            List<String> outputs = outputs(recipe);
            String source = recipe.source().file() + (recipe.source().line() > 0 ? "#L" + recipe.source().line() : "");
            for (ItemRef item : recipe.itemOutputs()) {
                MutableRow row = itemRow(rows, item);
                if (row == null) continue;
                row.producers.add(new Evidence(recipe.displayOnly() ? "display" : "production", recipe.label(),
                        recipe.map(), outputs, source, !recipe.distinctFluids().isEmpty()));
                row.forms.add("配方输出 " + itemLabel(item));
            }
            for (var fluid : recipe.fluidOutputs()) {
                MutableRow row = fluidRow(rows, fluid.field(), declared);
                row.forms.add("流体字段 " + fluid.field());
                row.producers.add(new Evidence(recipe.displayOnly() ? "display" : "production", recipe.label(),
                        recipe.map(), outputs, source, !recipe.distinctFluids().isEmpty()));
            }
            for (int i = 0; i < recipe.itemInputs().size(); i++) {
                ItemRef item = recipe.itemInputs().get(i);
                MutableRow row = itemRow(rows, item);
                if (row != null) {
                    row.forms.add("配方引用 " + itemLabel(item));
                    row.uses.add(new Evidence(classify(recipe, i, false), recipe.label(), recipe.map(),
                            outputs, source, !recipe.distinctFluids().isEmpty()));
                }
            }
            for (int i = 0; i < recipe.fluidInputs().size(); i++) {
                var fluid = recipe.fluidInputs().get(i);
                MutableRow row = fluidRow(rows, fluid.field(), declared);
                row.forms.add("流体字段 " + fluid.field());
                row.uses.add(new Evidence(classify(recipe, i, true), recipe.label(), recipe.map(),
                        outputs, source, !recipe.distinctFluids().isEmpty()));
            }
        }
        return rows;
    }

    public static String classify(RecipeDeclaration recipe, int inputIndex, boolean fluid) {
        if (recipe.displayOnly()) return "display";
        if (!fluid && recipe.keptInputs().contains(inputIndex)) return "catalyst";
        if (recipe.perTick() < 0) return "fuel";
        if (fluid) {
            String field = recipe.fluidInputs().get(inputIndex).field();
            if (recipe.fluidOutputs().stream().anyMatch(output -> output.field().equals(field))) return "recycle";
        } else {
            ItemRef input = recipe.itemInputs().get(inputIndex);
            if (input.kind() == ItemKind.MATERIAL && recipe.fluidInputs().isEmpty()
                    && recipe.fluidOutputs().isEmpty() && !recipe.itemOutputs().isEmpty()
                    && recipe.itemOutputs().stream().allMatch(output -> output.kind() == ItemKind.MATERIAL
                            && output.material().equals(input.material()))) {
                boolean sameInputs = true;
                for (int i = 0; i < recipe.itemInputs().size(); i++) {
                    if (recipe.keptInputs().contains(i)) continue;
                    ItemRef other = recipe.itemInputs().get(i);
                    if (other.kind() != ItemKind.MATERIAL || !other.material().equals(input.material())) sameInputs = false;
                }
                if (sameInputs) return "forming";
            }
        }
        return "process";
    }

    private static MutableRow material(Map<String, MutableRow> rows, String material) {
        return rows.computeIfAbsent("material:" + material,
                key -> new MutableRow(key, "material", material, "宿主材料引用（未解析注册表）"));
    }

    private static MutableRow rawItem(Map<String, MutableRow> rows, String id) {
        String kind = id.startsWith("#") ? "tag" : "item";
        return rows.computeIfAbsent(kind + ":" + id,
                key -> new MutableRow(key, kind, id, "原始物品 ID / 标签；未猜测材料归属"));
    }

    private static MutableRow itemRow(Map<String, MutableRow> rows, ItemRef item) {
        return switch (item.kind()) {
            case MATERIAL -> material(rows, item.material());
            case VANILLA -> rawItem(rows, item.itemId());
            case SELECTOR -> null;
        };
    }

    private static MutableRow fluidRow(Map<String, MutableRow> rows, String field, Set<String> declared) {
        for (String prefix : List.of("GenLiquid_", "GenGas_")) {
            if (field.startsWith(prefix) && declared.contains(field.substring(prefix.length())))
                return material(rows, field.substring(prefix.length()));
        }
        return rows.computeIfAbsent("fluid:" + field,
                key -> new MutableRow(key, "fluid", field, "流体字段；宿主别名未归一化"));
    }

    private static List<String> outputs(RecipeDeclaration recipe) {
        List<String> outputs = new ArrayList<>();
        for (int i = 0; i < recipe.itemOutputs().size(); i++) {
            ItemRef item = recipe.itemOutputs().get(i);
            String chance = i < recipe.outputChances().size()
                    ? " [概率 " + recipe.outputChances().get(i) + "/10000]" : "";
            outputs.add(itemLabel(item) + " × " + item.count() + chance);
        }
        for (var fluid : recipe.fluidOutputs()) outputs.add(fluid.field() + " × " + fluid.amount() + " mB");
        return List.copyOf(outputs);
    }

    private static String itemLabel(ItemRef item) {
        return switch (item.kind()) {
            case MATERIAL -> item.prefix() + " " + item.material();
            case VANILLA -> item.itemId();
            case SELECTOR -> "selector " + item.selector();
        };
    }

    private static List<MaterialRow> freeze(Map<String, MutableRow> rows) {
        Comparator<Evidence> order = Comparator.comparing(Evidence::category).thenComparing(Evidence::recipe)
                .thenComparing(Evidence::map).thenComparing(Evidence::source);
        return rows.values().stream().map(row -> new MaterialRow(row.key, row.kind, row.name,
                row.displayName, row.origin, List.copyOf(row.forms),
                row.producers.stream().distinct().sorted(order).toList(),
                row.uses.stream().distinct().sorted(order).toList(),
                List.copyOf(row.capabilities), row.intents.stream().sorted(Comparator.comparing(Intent::stage)
                        .thenComparing(Intent::component).thenComparing(Intent::destination)).toList())).toList();
    }

    private static final class MutableRow {
        final String key;
        final String kind;
        final String name;
        String displayName;
        String origin;
        final Set<String> forms = new TreeSet<>();
        final List<Evidence> producers = new ArrayList<>();
        final List<Evidence> uses = new ArrayList<>();
        final Set<String> capabilities = new TreeSet<>();
        final List<Intent> intents = new ArrayList<>();

        MutableRow(String key, String kind, String name, String origin) {
            this.key = key;
            this.kind = kind;
            this.name = name;
            this.displayName = name;
            this.origin = origin;
        }
    }

    public static List<CraftingRecipe> readCrafting(Path directory, List<String> diagnostics) throws IOException {
        if (!Files.isDirectory(directory)) throw new IOException("Missing recipe directory: " + directory);
        List<CraftingRecipe> recipes = new ArrayList<>();
        try (var files = Files.walk(directory)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).sorted().toList()) {
                JsonObject json = readObject(file);
                String type = requiredString(json, "type");
                if (type.equals("gt6ce:machine_removal")) continue;
                String relative = directory.relativize(file).toString().replace('\\', '/');
                if (!type.equals("minecraft:crafting_shaped") && !type.equals("minecraft:crafting_shapeless")) {
                    diagnostics.add("未扫描的配方类型：" + relative + " (" + type + ")");
                    continue;
                }
                List<CraftingInput> inputs = new ArrayList<>();
                if (type.endsWith("crafting_shaped")) {
                    JsonObject keys = json.getAsJsonObject("key");
                    Map<Character, Integer> counts = new TreeMap<>();
                    for (JsonElement line : json.getAsJsonArray("pattern")) {
                        for (char symbol : line.getAsString().toCharArray())
                            if (symbol != ' ') counts.merge(symbol, 1, Integer::sum);
                    }
                    for (var count : counts.entrySet()) {
                        JsonElement ingredient = keys.get(String.valueOf(count.getKey()));
                        if (ingredient == null) throw new IllegalArgumentException("Missing pattern key " + count.getKey() + ": " + file);
                        inputs.add(new CraftingInput(alternatives(ingredient), count.getValue()));
                    }
                } else {
                    for (JsonElement ingredient : json.getAsJsonArray("ingredients"))
                        inputs.add(new CraftingInput(alternatives(ingredient), 1));
                }
                JsonElement result = json.get("result");
                String output;
                int count;
                if (result.isJsonPrimitive()) {
                    output = result.getAsString();
                    count = 1;
                } else {
                    JsonObject object = result.getAsJsonObject();
                    output = requiredString(object, object.has("item") ? "item" : "id");
                    count = object.has("count") ? object.get("count").getAsInt() : 1;
                }
                if (count < 1) throw new IllegalArgumentException("Nonpositive crafting output: " + file);
                recipes.add(new CraftingRecipe("ssite:" + relative.substring(0, relative.length() - 5),
                        type, List.copyOf(inputs), output, count, RESOURCE_ROOT + "data/ssite/recipes/" + relative));
            }
        }
        return List.copyOf(recipes);
    }

    private static List<String> alternatives(JsonElement element) {
        Set<String> values = new TreeSet<>();
        if (element.isJsonArray()) {
            for (JsonElement part : element.getAsJsonArray()) values.addAll(alternatives(part));
        } else if (element.isJsonObject()) {
            JsonObject ingredient = element.getAsJsonObject();
            if (ingredient.has("item") && ingredient.size() == 1) values.add(requiredString(ingredient, "item"));
            else if (ingredient.has("tag") && ingredient.size() == 1) values.add("#" + requiredString(ingredient, "tag"));
            else throw new IllegalArgumentException("Unsupported crafting ingredient: " + element);
        } else {
            throw new IllegalArgumentException("Unsupported crafting ingredient: " + element);
        }
        if (values.isEmpty()) throw new IllegalArgumentException("Empty crafting alternatives");
        return List.copyOf(values);
    }

    public static List<Intent> readIntents(Path file, Set<String> known, List<String> diagnostics) throws IOException {
        if (!Files.exists(file)) {
            diagnostics.add("未提供可选的 docs/material-uses.json；本次只显示代码事实。");
            return List.of();
        }
        JsonObject root = readObject(file);
        if (!root.has("version") || root.get("version").getAsInt() != 1 || !root.has("groups"))
            throw new IllegalArgumentException("Unsupported material intention schema: " + file);
        List<Intent> intents = new ArrayList<>();
        for (JsonElement entry : root.getAsJsonArray("groups")) {
            JsonObject group = entry.getAsJsonObject();
            String stage = requiredString(group, "stage");
            String component = requiredString(group, "component");
            String destination = requiredString(group, "destination");
            String status = requiredString(group, "status");
            String source = requiredString(group, "source");
            if (!Set.of("planned", "candidate", "external").contains(status))
                throw new IllegalArgumentException("Unknown intention status: " + status);
            if (!source.startsWith("docs/") || source.contains("..") || source.contains("\\"))
                throw new IllegalArgumentException("Intention source must stay under docs/: " + source);
            JsonArray names = group.getAsJsonArray("materials");
            if (names == null || names.isEmpty()) throw new IllegalArgumentException("Intention group has no materials");
            for (JsonElement name : names) {
                String material = name.getAsString();
                if (material.isBlank()) throw new IllegalArgumentException("Empty intention material");
                if (!known.contains(material)) diagnostics.add("规划目标未在当前材料声明或符号物品配方中找到：" + material);
                intents.add(new Intent(material, stage, component, destination, status, source));
            }
        }
        return List.copyOf(intents);
    }

    private static JsonObject readObject(Path file) throws IOException {
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static String requiredString(JsonObject object, String field) {
        if (!object.has(field) || !object.get(field).isJsonPrimitive()
                || !object.getAsJsonPrimitive(field).isString() || object.get(field).getAsString().isBlank())
            throw new IllegalArgumentException("Missing nonempty string '" + field + "' in " + object);
        return object.get(field).getAsString();
    }

    private static Map<String, String> translations(Path repository) throws IOException {
        Map<String, String> translations = new TreeMap<>();
        for (String namespace : List.of("gt6ce", "ssite")) {
            Path file = repository.resolve(RESOURCE_ROOT + "assets/" + namespace + "/lang/zh_cn.json");
            if (Files.isRegularFile(file)) readObject(file).entrySet().forEach(entry ->
                    translations.put(entry.getKey(), entry.getValue().getAsString()));
        }
        return translations;
    }

    public static void write(Catalog catalog, Path repository, Path output) throws IOException {
        Files.createDirectories(output);
        Files.writeString(output.resolve("catalog.json"), JSON.toJson(catalog) + "\n", StandardCharsets.UTF_8);
        Files.writeString(output.resolve("materials.md"), markdown(catalog, repository, output), StandardCharsets.UTF_8);
        Files.writeString(output.resolve("index.html"), html(catalog, repository, output), StandardCharsets.UTF_8);
    }

    public static String markdown(Catalog catalog, Path repository, Path output) {
        StringBuilder text = new StringBuilder("# 材料、部件与下游用途\n\n> 自动生成，勿手改。平台：");
        text.append(md(catalog.platform())).append("。\n\n").append(SCOPE)
                .append("\n\n规划注记独立于代码事实；它不会使材料自动变为“已完成”。原始物品 ID 与未解析流体单列，未猜测材料别名。\n\n")
                .append("条目：").append(catalog.materials().size()).append("；机器与展示声明：")
                .append(catalog.recipes().size()).append("；坩埚声明：").append(catalog.crucibleRecipes().size())
                .append("；合成配方：").append(catalog.craftingRecipes().size()).append("。\n\n")
                .append("完整数量、概率、保留槽及条件见 [catalog.json](catalog.json)，交互表见 [index.html](index.html)。\n\n");
        if (!catalog.diagnostics().isEmpty()) {
            text.append("## 覆盖提示\n\n");
            for (String diagnostic : catalog.diagnostics()) text.append("- ").append(md(diagnostic)).append('\n');
            text.append('\n');
        }
        text.append("| 材料 / 原始标识 | 来源与形态 | 扫描范围内的用途 | 产物 / 部件 | 规划用途与归属 |\n")
                .append("|---|---|---|---|---|\n");
        for (MaterialRow row : catalog.materials()) {
            text.append("| ").append(md(row.displayName())).append("<br>").append(md(row.name())).append(" | ")
                    .append(md(row.origin())).append("<br>").append(md(String.join("；", row.forms()))).append(" | ")
                    .append(md(status(row))).append("<br>").append(md(categorySummary(row))).append(" | ")
                    .append(md(targetSummary(row))).append(" | ").append(md(intentSummary(row))).append(" |\n");
        }
        text.append("\n## 逐项证据\n\n");
        for (MaterialRow row : catalog.materials()) {
            text.append("### ").append(md(row.name())).append("\n\n");
            for (Evidence use : row.uses()) {
                text.append("- ").append(categoryName(use.category())).append(use.conditional() ? "（宿主条件未判定）" : "")
                        .append("：").append(md(use.recipe())).append(" → ").append(md(String.join("；", use.outputs())))
                        .append(" — [来源](").append(link(repository, output, use.source())).append(")\n");
            }
            for (String capability : row.capabilities()) text.append("- ").append(md(capability)).append('\n');
            for (Intent intent : row.intents()) text.append("- ").append(intentStatus(intent.status())).append("：")
                    .append(md(intent.stage() + " · " + intent.component() + " → " + intent.destination()))
                    .append(" — [规划依据](").append(link(repository, output, intent.source())).append(")\n");
            if (row.uses().isEmpty() && row.capabilities().isEmpty()) text.append("- ").append(status(row)).append('\n');
            text.append('\n');
        }
        return text.toString();
    }

    public static String html(Catalog catalog, Path repository, Path output) {
        StringBuilder html = new StringBuilder("""
                <!doctype html>
                <html lang="zh-CN"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
                <title>SSITE 材料与下游用途</title><style>
                :root{color-scheme:light dark;font-family:system-ui,"Microsoft YaHei",sans-serif}
                body{margin:0 auto;max-width:1500px;padding:24px;color:CanvasText;background:Canvas}
                h1{font-size:1.6rem}p{line-height:1.6}.scope{max-width:1050px;color:GrayText}
                .tools{display:flex;flex-wrap:wrap;gap:14px;align-items:center;margin:24px 0}
                input,select{font:inherit;padding:8px;max-width:100%;box-sizing:border-box}
                table{border-collapse:collapse;width:100%;table-layout:fixed}
                th,td{text-align:left;vertical-align:top;border-bottom:1px solid GrayText;padding:12px;overflow-wrap:anywhere}
                th:nth-child(1){width:18%}th:nth-child(2){width:19%}th:nth-child(3){width:32%}th:nth-child(4){width:31%}
                small,.muted{color:GrayText}details{margin:8px 0}summary{cursor:pointer}
                ul{padding-left:20px}li{margin:7px 0;line-height:1.5}code{font-size:.9em}
                a{color:LinkText}tr[hidden]{display:none!important}.tag{font-size:.85em;border:1px solid GrayText;padding:2px 5px;display:inline-block;margin:2px}
                @media(max-width:700px){body{padding:14px}table,thead,tbody,tr,th,td{display:block}thead{display:none}
                tr{border-bottom:2px solid GrayText;margin:14px 0}td{border:0;padding:8px 0}}
                </style></head><body><h1>SSITE 材料、部件与下游用途</h1>
                """);
        html.append("<p>自动生成 · ").append(escape(catalog.platform())).append(" · ")
                .append(catalog.materials().size()).append(" 个条目 · ").append(catalog.recipes().size())
                .append(" 条机器 / 展示声明 · ").append(catalog.craftingRecipes().size()).append(" 条合成</p>")
                .append("<p class=\"scope\">").append(escape(SCOPE)).append("</p>")
                .append("<p class=\"scope\">规划用途独立于实际证据；未来用途不会自动标为完成。原始物品 ID、标签和未解析流体单列，未猜测其材料归属。</p>")
                .append("<p><a href=\"materials.md\">Markdown 表</a> · <a href=\"catalog.json\">完整声明与数量 JSON</a></p>");
        if (!catalog.diagnostics().isEmpty()) {
            html.append("<details><summary>覆盖提示（").append(catalog.diagnostics().size()).append("）</summary><ul>");
            for (String diagnostic : catalog.diagnostics()) html.append("<li>").append(escape(diagnostic)).append("</li>");
            html.append("</ul></details>");
        }
        html.append("""
                <div class="tools"><label>搜索 <input id="search" type="search" placeholder="材料、配方、产物或规划"></label>
                <label>范围 <select id="kind"><option value="">全部条目</option><option value="owned" selected>SSITE 声明材料</option>
                <option value="material">全部材料名</option><option value="fluid">未解析流体字段</option>
                <option value="item">原始物品 ID</option><option value="tag">物品标签</option><option value="planned">仅规划目标</option></select></label>
                <label>用途 <select id="use"><option value="">全部用途状态</option><option value="gap">未发现无条件工艺用途</option>
                <option value="process">工艺消费</option><option value="fuel">燃料</option><option value="forming">同材料成型</option>
                <option value="catalyst">催化剂 / 工具</option><option value="recycle">回收循环</option>
                <option value="display">行为说明</option><option value="intent">有规划注记</option></select></label>
                <output id="count" aria-live="polite"></output></div>
                <table><thead><tr><th>材料与形态</th><th>实际用途摘要</th><th>配方与产物证据</th><th>规划部件与下游</th></tr></thead><tbody>
                """);
        for (MaterialRow row : catalog.materials()) {
            String categories = row.uses().stream().map(Evidence::category).distinct().sorted().collect(Collectors.joining(" "));
            if (!hasConsumption(row)) categories += " gap";
            if (!row.intents().isEmpty()) categories += " intent";
            html.append("<tr data-kind=\"").append(escape(row.kind())).append("\" data-owned=\"")
                    .append(row.origin().equals("SSITE 材料声明")).append("\" data-use=\"").append(escape(categories))
                    .append("\"><td><strong>").append(escape(row.displayName())).append("</strong><br><code>")
                    .append(escape(row.name())).append("</code><br><small>").append(escape(row.origin())).append("</small>")
                    .append("<details><summary>形态与字段（").append(row.forms().size()).append("）</summary><ul>");
            for (String form : row.forms()) html.append("<li>").append(escape(form)).append("</li>");
            html.append("</ul></details></td><td>").append(escape(status(row))).append("<br>");
            for (String category : row.uses().stream().map(Evidence::category).distinct().sorted().toList())
                html.append("<span class=\"tag\">").append(categoryName(category)).append("</span>");
            for (String capability : row.capabilities()) html.append("<p>").append(escape(capability)).append("</p>");
            html.append("</td><td>");
            appendEvidence(html, "来源配方", row.producers(), repository, output);
            appendEvidence(html, "用途与输出", row.uses(), repository, output);
            html.append("</td><td>");
            if (row.intents().isEmpty()) html.append("<span class=\"muted\">未标注未来用途；不等于没有用途</span>");
            for (Intent intent : row.intents()) {
                html.append("<p><span class=\"tag\">").append(intentStatus(intent.status())).append("</span> ")
                        .append(escape(intent.stage())).append("<br>").append(escape(intent.component())).append("<br>→ ")
                        .append(escape(intent.destination())).append("<br><a href=\"")
                        .append(escape(link(repository, output, intent.source()))).append("\">规划依据</a></p>");
            }
            html.append("</td></tr>");
        }
        html.append("""
                </tbody></table><script>
                (function(){'use strict';
                var rows=Array.from(document.querySelectorAll('tbody tr'));
                var search=document.getElementById('search'),kind=document.getElementById('kind'),use=document.getElementById('use');
                rows.forEach(function(row){row.dataset.search=row.textContent.toLowerCase();});
                function update(){var n=0,q=search.value.toLowerCase().trim();
                rows.forEach(function(row){var shown=(!q||row.dataset.search.indexOf(q)>=0)
                &&(!kind.value||(kind.value==='owned'?row.dataset.owned==='true':row.dataset.kind===kind.value))
                &&(!use.value||row.dataset.use.split(' ').indexOf(use.value)>=0);row.hidden=!shown;if(shown)n++;});
                document.getElementById('count').textContent=n+' / '+rows.length+' 个条目';}
                search.addEventListener('input',update);kind.addEventListener('change',update);use.addEventListener('change',update);update();
                })();
                </script></body></html>
                """);
        return html.toString();
    }

    private static void appendEvidence(StringBuilder html, String title, List<Evidence> evidence, Path repository, Path output) {
        if (evidence.isEmpty()) return;
        html.append("<details><summary>").append(title).append("（").append(evidence.size()).append("）</summary><ul>");
        for (Evidence item : evidence) {
            html.append("<li>").append(categoryName(item.category()))
                    .append(item.conditional() ? " · 宿主条件未判定" : "").append("<br><code>")
                    .append(escape(item.recipe())).append("</code><br><small>").append(escape(item.map()))
                    .append(" · SSITE 声明</small><br>→ ").append(escape(String.join("；", item.outputs())))
                    .append("<br><a href=\"").append(escape(link(repository, output, item.source())))
                    .append("\">来源</a></li>");
        }
        html.append("</ul></details>");
    }

    public static boolean hasConsumption(MaterialRow row) {
        return row.uses().stream().anyMatch(evidence -> !evidence.conditional()
                && Set.of("process", "fuel", "crucible", "crafting", "crafting-alternative", "recycle").contains(evidence.category()));
    }

    public static String status(MaterialRow row) {
        if (hasConsumption(row)) return "存在 SSITE 声明的工艺用途（含回收循环）";
        if (!row.uses().isEmpty()) return "目前仅发现成型、催化、行为说明或条件用途";
        if (!row.capabilities().isEmpty()) return "已有部件能力声明；生成消费未展开";
        return "当前扫描未发现消费；宿主与外部用途未全量扫描";
    }

    private static String categorySummary(MaterialRow row) {
        return row.uses().stream().collect(Collectors.groupingBy(Evidence::category, TreeMap::new, Collectors.counting()))
                .entrySet().stream().map(entry -> categoryName(entry.getKey()) + " " + entry.getValue())
                .collect(Collectors.joining("；"));
    }

    private static String targetSummary(MaterialRow row) {
        return row.uses().stream().filter(evidence -> !evidence.category().equals("forming"))
                .flatMap(evidence -> evidence.outputs().stream()).distinct().limit(4).collect(Collectors.joining("；"));
    }

    private static String intentSummary(MaterialRow row) {
        return row.intents().stream().map(intent -> intentStatus(intent.status()) + " · " + intent.stage() + "："
                + intent.component() + " → " + intent.destination()).collect(Collectors.joining("；"));
    }

    public static String categoryName(String category) {
        return switch (category) {
            case "process" -> "工艺消耗";
            case "fuel" -> "燃料";
            case "catalyst" -> "不消耗的催化剂 / 工具";
            case "forming" -> "同材料成型";
            case "recycle" -> "同名流体回收 / 循环";
            case "display" -> "行为工艺说明";
            case "crucible" -> "坩埚合金";
            case "crafting" -> "合成";
            case "crafting-alternative" -> "合成候选输入";
            case "production" -> "生产";
            default -> throw new IllegalArgumentException("Unknown evidence category: " + category);
        };
    }

    private static String intentStatus(String status) {
        return switch (status) {
            case "planned" -> "规划";
            case "candidate" -> "候选";
            case "external" -> "外部下游";
            default -> throw new IllegalArgumentException("Unknown intention status: " + status);
        };
    }

    public static String link(Path repository, Path output, String source) {
        String[] parts = source.split("#", 2);
        String file = parts[0];
        if (!file.contains("/") && file.endsWith(".java"))
            file = "common/src/main/java/com/lombinaxmasson/ssite/chemistry/" + file;
        Path target = repository.toAbsolutePath().normalize().resolve(file);
        String relative;
        try {
            relative = output.toAbsolutePath().normalize().relativize(target)
                    .toString().replace('\\', '/').replace(" ", "%20").replace("(", "%28").replace(")", "%29");
        } catch (IllegalArgumentException differentRoot) {
            relative = target.toUri().toString();
        }
        return relative + (parts.length == 2 ? "#" + parts[1] : "");
    }

    public static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String md(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("|", "&#124;").replace("\r", "").replace("\n", "<br>");
    }
}
