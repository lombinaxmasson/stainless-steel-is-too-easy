# GT6CE 附属物品按 addon 命名空间注册 —— 需求与改动总结

> 目标仓库：`../GregTech6CommunityEdition/GregTech6`（GT6CE，0.x 阶段）
> 本文档是改动前的现状梳理与工作量评估，供研究用，非最终实现方案。
> 已对照 GT6CE 源码逐条核对过（行号以核对时为准）。

## 一、需求

让 GT6CE 的 **addon 材质物品与流体** 注册在 addon 自己的命名空间下（例如 `ssite:dust_polypropylene`），
而不是统一塞在 `gt6ce:` 下。

动机：EMI/JEI 的 `@ssite` 是按 registry 命名空间过滤的，现在所有 addon 物品都在 `gt6ce:` 下，
玩家无法用 `@ssite` 过滤、也分不清哪些东西是 ssite 加的。流体在 EMI 里按流体 id 归属，同样需要迁移。

关键前提认知：**材质身份（逻辑对象、tag/矿辞、配方解析）独立于物品 registry id。**
把物品 id 从 `gt6ce:` 改成 `ssite:` 不会破坏材质的全局共享，只要 tag/矿辞仍按材质生成。

## 二、现状：已就绪的基础设施

| 路径 | 现状 | 证据 |
|------|------|------|
| 物品注册 | 已按命名空间建 `DeferredRegister`，两个平台都走 `definition.namespace()` | `multiversion/.../registry/GTItems.java:57-75`、Neo `:104`、Forge `ItemBootstrap.java:34` |
| 管道（block） | 已有 per-namespace 模式，可照抄 | `.../registry/AddonPipeRegistration.java` |
| 机器 | **已自动按 addon 命名空间注册** | `AddonMachineRuntime.java:74` `.namespace(accepted.addonId())`；ssite 配方里的 `ssite:aod_converter_steel` 印证 |
| 自定义 prefix 物品 | 已挂 addon 命名空间 | `MaterialItemDefinitions.java:168-196` 用 `prefix.itemNamespace()` |
| 材质归属 | 已在跟踪 | `AddonMaterials.java:302-317`（`owns()/owner()/owners()`）、`363-369`（`setSourceMod`）、`732-735`（link 期创建的材质 `addOwner`） |

### 不受影响、会自动跟随新 id 的部分

- **翻译**：`MaterialItem.getDescriptionId()`（`MaterialItem.java:131-138`）按 prefix 走 `item.gt6ce.<prefix>`，与物品命名空间无关，lang 不用动。
- **Tag / 矿辞**：`MaterialTagPack` 写入的是 `id.toString()`，自动跟随。
- **物品着色**：`MaterialClientModels.registerItemColors` 遍历 `GTItems.allEntries()`，自动覆盖。
- **烘焙模型别名**：`MaterialClientModels`（`:100-130`）按真实 item id 替换模型，所以即使 JSON 缺失，游戏内大概率也能正常显示（但见下文第 2 点）。
- **ssite 配方 JSON**：里面的 `gt6ce:` 引用的都是 GT 本体物品（钢、铜、机器外壳等），不用改。

## 三、要改的点

### 1. 材质标准 form 的物品 —— 小

问题在 `core/.../api/material/MaterialItemDefinitions.java`：

- `Definition` record 已带 `namespace` 字段（`:22-25`），但 4 参构造器默认 `GregTechIdentity.MOD_ID`。
- `claimPair` 的默认重载写死 gt6ce（`:231-234`）。
- **只改 `claimPair` 不够**：`claimed` 表只存 `itemId` 不存 namespace，真正交给注册的 `Definition`
  在 `all()` 里用的是 4 参构造器（`:180` 标准 form、`:188` accepted forms），仍然会落到 gt6ce。

改法：新增 `namespaceOf(material)`（有 owner 用 owner，否则 `MOD_ID`），claim 阶段和构造 `Definition` 时都使用。
同步修改 `all()` 的 javadoc（原"core 先占 id、防止 addon 抢 id"的语义在分命名空间后改变）。
约 20-30 行，外加测试。

### 2. 物品模型资源包 —— 小到中

文档早先认为"`SharedInventoryModelPack` 已按 `item.getNamespace()` 走，自动跟随"，**核对后不成立**：

- `SharedInventoryModelPack.java:109-110`：非 `gt6ce` 命名空间的物品直接 `continue`，不生成 JSON。
- `:139`：结果用 `location(...)` 写死写入 `gt6ce` 命名空间。
- `getNamespaces()`（`:321-335`）只报 gt6ce、addon prefix、多方块部件、气罐、管道的命名空间，不含材质 owner。

后果：游戏内靠烘焙阶段别名大概率能显示，但每个物品都会刷 missing model 警告，资源包也无法覆盖这些模型。

改法：放开 `MaterialFormItem` 的命名空间过滤，按物品自身命名空间 `named(id.getNamespace(), ...)` 写入，
`getNamespaces()` 补上所有材质 owner。约 15-20 行。

### 3. 流体 —— 中（唯一真正的大头）

`GTFluids` 用单例 `DeferredRegister`（Neo `:64-65` 写死 `"gt6ce"`，Forge `:72-75` 用 `GregTech.NAMESPACE`），没有 per-namespace。
需要改动的位置：

- **两个平台的注册**：Forge `bootstrap/content/FluidBootstrap.java`、Neo `platform/neoforge/fluid/FluidRegistries.java`，
  `FLUID_TYPES` / `FLUIDS` 改为按命名空间分组；`bindFluid(..., GregTech.id(path), ...)`（Forge `:119`）写死 gt6ce。
- **流体显示物品** `fluid_item_*`（`GTFluidItems` / Neo `FluidRegistries.ITEMS`）：待决定是否一起迁移（建议一起迁）。
- `api/fluid/FluidDisplayBinding.java:45`：按写死的 `gt6ce` 拼显示物品 id。
- **`emi/EmiCatalogRepair.java:20/40`**：只处理 `gt6ce` 条目。不改的话，ssite 流体在 EMI 里会显示成物品而不是流体，属于可见的功能回退。

简化点：不必把 namespace 塞进 `FluidCatalog`。注册时通过 `entry.materialKey()` 或
`RegisteredFluids.boundMaterial(registryName)` 找到材质，再查 `AddonMaterials.owner(...)`。
`FluidCatalog` 的路径查重（`:159-167`）保持全局即可（更严格，不会出问题）。

风险：尚未逐一排查其它代码是否默认流体 id 一定在 `gt6ce:` 下，实施时需要全局搜一遍。
约 80-150 行，两个平台都要验证。

### 4. 零碎硬编码 —— 很小

- `content/plant/MaterialBerryBushCatalog.java:40` 写死 `"gt6ce:" + itemId`；`SharedInventoryModelPack.java:145` 同理。
  只有 addon 材质带 `plantGtBerry` 形态时才会出错，顺手修。
- `mixin/ReliableEmiNamespaceMixin.java:22` 只保护 `gt6ce` 条目不被并进 `minecraft`/`c` 分组。
  ssite 物品迁走后可能被 Reliable EMI 并进 `c:dusts` 等分组，视需要决定是否扩展到 addon 命名空间。
- `EmiCatalogRepair` 的 GT 目录排序只作用于 `gt6ce`，ssite 物品迁走后不再参与 GT 排序（可接受，甚至可能更符合预期）。
- 一批 gametest / productionSmoke 按 `gt6ce` 过滤（如 `LanguageDeliveryChecks`、`CommonBlockDeliveryChecks`），
  不会挂，但会悄悄漏掉 addon 物品的覆盖，按需补。

### 5. 方块/矿石 —— 可后置

`multiversion/.../registry/GTBlocks.java` 也写死 `GregTech.NAMESPACE`。
ssite 的材质目前没有开 `ores()`，用不到，可后置。

## 四、ssite 侧连带改动

1. **EMI alias**：`common/src/main/resources/assets/emi/aliases/ssite_materials.json`（约 275 行 `item:gt6ce:...`）
   批量替换成 `ssite:`；迁移后也可以评估这个文件是否还有必要保留。
2. 其它 `gt6ce:` 引用（`data/ssite/recipe(s)/*.json`）都指向 GT 本体物品，不用改。

## 五、设计决策点（待拍板）

- **namespace 取谁的**：`owner()` 实际返回的是 addonId **字母序最小**的声明者（`QUEUE` 是 `TreeMap`），
  不是"第一个声明者"。link 期创建的材质按 link 顺序追加 owner。
  风险：以后出现一个字母序排在 `ssite` 之前、且声明了同名材质的 addon 时，该材质的物品会整体迁到对方命名空间，老存档丢物品。可选方案：
  - 继续用 `owner()`，接受风险并写进 addon 文档；
  - 多 owner 共享的材质退回 `gt6ce`（但单 owner 材质后来变共享时，id 同样会变）。
  两者都不完全稳定，需要拍板。
- **自动 vs opt-in**：是否给 `AddonMaterialContributor` 留开关。0.x 阶段建议**全自动**，不留 opt-in 冗余。
- **存档兼容**：老存档里的 `gt6ce:dust_polypropylene` 等改名后会消失，仓库目前没有任何 remap 逻辑。二选一：
  - 加别名：1.20.1 用 Forge `MissingMappingsEvent`，1.21.1 用 Neo registry alias，每个平台约 30 行；
  - 0.x 直接声明为破坏性变更，不做兼容。
- **流体显示物品**是否随流体一起迁移（建议迁）。

## 六、工作量评估

| 块 | 改动量 | 估时 |
|---|---|---|
| 物品（第 1 点） | 20-30 行 | 1-2 小时（含测试） |
| 模型资源包（第 2 点） | 15-20 行 | 约 1 小时 + 进游戏验证 |
| 零碎硬编码（第 4 点） | 个位数行 | 几分钟到半小时 |
| ssite EMI alias | 批量替换 | 几分钟 |
| 流体（第 3 点） | 80-150 行 | 半天到 1 天（两个平台） |
| 存档别名（可选） | 每平台约 30 行 | 0.x 可不做 |

不做存档兼容时合计约 **1-1.5 天**；加上存档别名和两个平台的完整实测，偏保守估计 2-3 天。

可以分两步：

1. **物品 + 模型 + 零碎**（约半天）：做完 `@ssite` 即可过滤 ssite 物品。
2. **流体**单独做：代价是流体 id 晚一步变化，老存档里罐中的流体会再破坏一次，0.x 阶段一般可接受。

时间窗口：现在是最低成本期（GT6CE 还在 0.x、真实 addon 目前就 ssite 一个、外部生态还没硬编码引用）。
每多一个 addon、多一条外部引用，成本就涨一截。

## 七、仍待验证

- 其它代码是否默认流体 id 在 `gt6ce:` 下（流体改造前需全局排查）。
- 模型资源包修复后，两个平台进游戏确认无 missing model 警告、资源包可覆盖。
- 方块/矿石注册的完整路径（后置项，暂未深挖）。
