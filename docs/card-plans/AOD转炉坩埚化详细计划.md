# AOD 转炉坩埚化重做与多材质炉体详细计划

> 计划 slug：`m1/crucible-aod-converter`
> 状态：**实现完成，待进游戏验收（2026-10-22）**。前置 [`铬铁初炼与 AOD 三档精炼产线`](closed/不锈钢精炼产线详细计划.md) 与 GT6CE 侧附属坩埚接口均已就绪（`edcfde5 feat: let a behavior machine back a crucible and pour into a mold`）。
> 性质：把初版 UI 配方机形式的 `ssite:aod_converter` 重做为真正的 **\(1\times 1\times 2\) 坩埚式冶金设备**（底部坩埚 + 顶部可开关炉盖），并按 GT6 坩埚熔点体系设置 **钢、不锈钢、铌钛、钨钢、钨、碳化钽铪** 六档炉体材质，利用钼的高熔点（\(2896\text{ K}\)）由铌钛及以上炉体自然卡住 316L 冶炼门槛。

```text
lane            = open
slug            = m1/crucible-aod-converter
owned_paths     = common/src/main/java/com/lombinaxmasson/ssite/machine/SsiteMachines.java
                  common/src/main/java/com/lombinaxmasson/ssite/material/SsiteMaterials.java
                  common/src/main/java/com/lombinaxmasson/ssite/chemistry/ChemistryRecipes.java
                  common/src/main/resources/assets/ssite/blockstates/aod_converter*.json
                  common/src/main/resources/assets/ssite/models/block/machine/**
                  common/src/main/resources/assets/ssite/models/item/aod_converter*.json
                  common/src/main/resources/assets/ssite/lang/en_us.json
                  common/src/main/resources/assets/ssite/lang/zh_cn.json
                  forge/src/main/resources/data/ssite/recipes/aod_converter*.json
                  neoforge/src/main/resources/data/ssite/recipe/aod_converter*.json
depends_on      = m1/stainless-refining-recipes
close_target    = 六档材质的 1x1x2 坩埚式 AOD 转炉进游戏可开盖投料、接管+KU 吹氩、按熔毁温度自然卡住 316L 钼熔炼并支持浇铸口出钢
```

---

## 0. 它解决什么

1. **交互失真问题**：初版 `ssite:aod_converter` 做成了带 GUI 槽位的普通单方块配方机（`basic()` + `ssite.recipe.aod`），丢失了 GT6 最有特色的坩埚控温、投料、鼓风与浇铸体验。现实中的 AOD 炉本身就是上方带炉口盖、底部带风口、侧壁可倾倒/出钢的耐火内衬容器，天然适合做成 GT6 坩埚的直系拓展。
2. **合金进阶门槛问题**：原版 GT6 依靠坩埚材料的熔毁温度（\(\text{meltDownTemperatureK} = \lfloor \text{meltingPointK} \times 1.25 \rfloor\)）来自然限制高熔点合金的冶炼。316L 钼不锈钢需要熔化纯钼（熔点 \(2896\text{ K}\)），而钢坩埚（上限 \(2557\text{ K}\)）和不锈钢坩埚（上限 \(2428\text{ K}\)）在化开钼之前就会熔毁，必须升级到**铌钛**（上限 \(2931\text{ K}\)）或更高阶炉体才能冶炼。把 AOD 转炉也做成多材质坩埚序列，无需硬编码锁定配方等级，仅靠物理熔点就能完美实现合金分级。

---

## 1. 结构、热力学与交互设计

### 1.1 \(1\times 1\times 2\) 双格结构与外观

- **整体尺寸**：宽 \(1\times\) 深 \(1\times\) 高 \(2\)（占地 1 格、高 2 格的上下组合或双格结构，无需加固环）。
- **下半格（底部坩埚炉体）**：
  - 外形沿用坩埚轮廓，通过 `tintindex = 0` 按炉体材料自动染色。
  - **四面进气口贴图**：四个水平侧面均绘制进气风口贴图，**任意一个侧面连接流体管道输入氩气（`Argon`）均可生效**，无需死记特定朝向。
  - **坩埚外设兼容**：水平侧面同样支持挂载原版 GT6 的**浇铸口（`Faucet`）**与**温度计（`Thermometer`）**；底部或侧面接收热能（`HU` / `CU` / `Ignis`）与动能（`KU`）。
- **上半格（顶部可开关炉盖）**：
  - 位于下半格正上方，表现为可开合的转炉炉帽/顶盖。
  - **开盖状态（Open）**：玩家右键顶盖打开；开盖时上方漏斗、投掷器或掉落物实体可从顶部开口落入下半格熔池（`admit` 进 `MoltenMaterialStorage`）。开盖时炉膛与大气相通，不驻留保护气。
  - **关盖状态（Closed）**：玩家再次右键顶盖关闭；关盖时阻断顶部固体投料，但使炉膛处于受控密闭状态，配合底部 `KU` + 侧面氩气管道将 `Argon` 锁在熔池中参与精炼。

> **实施说明（SDK 约束）**：当前 SDK 的行为机器只占一个方块，没有「一个控制器占两格」的入口。实际结构是**下方行为机器本体 + 正上方 `ssite:aod_lid` 结构零件**，两者合起来就是 \(1\times 1\times 2\)。炉盖的开/关是零件上名为 `open` 的命名状态，右键切换；机器只在结构成形（炉盖存在）时工作。炉盖外观目前是静态方块模型，开关只改变逻辑状态，由放大镜（`describe`）显示「炉盖：打开/关闭」。

### 1.2 气路与 `KU` 耦合机制（管道 + `KU` + 足够温度 \(\rightarrow\) 炉内氩气驻留）

在原版 GT6 的 `CrucibleProcess.process` 中，施加 `KU` 会按 `CrucibleEnergyRules.airAmount`（每 \(1000\text{ KU}\) 注入 \(1\text{ U}\) `Air`）向熔池注入空气，并在当次 `CrucibleReactions.react` 结算后立刻清除 `Air` 及所有密度 \(\le 0.0012\text{ g/cm}^3\) 的气体。

坩埚式 AOD 转炉在此基础上拓展以下气体注入与驻留规则：

1. **未接氩气管（或管道无氩气）+ 施加 `KU`**：
   - 行为与原版坩埚吹风一致：正向 `KU` 冲程向熔池打入 `Air`（空气，即 \(\text{N}_2 + \text{O}_2\) 混合气）。
   - 在精炼温度（\(\ge 1943\text{ K}\)）下，`Air` 与熔池中的高碳不锈钢粗钢发生**空气硬吹反应**（铬部分氧化烧损进炉渣，仅 50% 收率产出 `StainlessSteel`），随后剩余 `Air` 在同 tick 散逸。
2. **四个侧面任一接通 `Argon` 管道 + 施加 `KU` + 炉盖关闭 + 温度达标（\(\ge 1943\text{ K}\)）**：
   - 当检测到水平任一侧面有 `Argon` 流体输入、同时收到 `KU` 鼓风动能、炉盖处于关闭状态且炉温达到熔池反应门槛时：
   - 消耗流体管内的 `Argon`，按 `KU` 冲程折算注入熔池的 `Argon` 材料栈（替代默认的 `Air` 注入）。
   - **氩气驻留特性**：由于炉盖关闭且持续有管道供气 + `KU` 维持底吹背压，熔池内的 `Argon` **不会像敞口坩埚里的空气那样无条件消失**，而是留在熔池中直到凑齐计量并完成 AOD 氩氧脱碳反应（或开盖/失温时排空）。

### 1.3 六档炉体材质与熔点门槛（对齐 GT6 `CrucibleSpec`）

每台 AOD 转炉炉体拥有与原版小坩埚相同的热容计算基准（\(7\text{ U}\) 炉体材料热质量）和熔毁系数（\(\text{meltDownTemperatureK} = \lfloor \text{meltingPointK} \times 1.25 \rfloor\)）。共注册以下 **6 档材质**：

| 序号 | 注册 ID | 炉体材料 (`GTMaterial`) | 材料熔点 (\(\text{K}\)) | 熔毁上限 (\(\text{K}\)) | 耐酸 (`acidProof`) | 核心定位与解锁内容 |
|---|---|---|---|---|---|---|
| 1 | `ssite:aod_converter_steel` | `Steel`（钢） | \(2046\) | \(2557\) | 否 | **破局初代炉**：无需不锈钢即可制造；足以熔化高碳粗钢（\(1750\text{ K}\)）、硅铁（\(1683\text{ K}\)）、铬铁（\(2160\text{ K}\)）并在 \(1943\sim 2200\text{ K}\) 吹炼出 304 `StainlessSteel`。 |
| 2 | `ssite:aod_converter_stainless_steel` | `StainlessSteel`（不锈钢） | \(1943\) | \(2428\) | **是** | 耐酸常规炉体，适合 304 量产线。 |
| 3 | `ssite:aod_converter_niobium_titanium` | `NiobiumTitanium`（铌钛） | \(2345\) | \(2931\) | 否 | **316L 门槛炉**：纯钼（`Molybdenum`）熔点高达 \(2896\text{ K}\)，前两档炉体加热到此温度会直接熔毁；必须造出铌钛 AOD（上限 \(2931\text{ K} > 2896\text{ K}\)，留有 \(35\text{ K}\) 控温窗口）才能化开钼并精炼出 `StainlessSteel316L`。 |
| 4 | `ssite:aod_converter_tungsten_steel` | `TungstenSteel`（钨钢） | \(2870\) | \(3587\) | 否 | 宽温区进阶炉：把 316L 冶炼的安全控温余量从 \(35\text{ K}\) 拓宽到近 \(700\text{ K}\)，告别精细控温焦虑。 |
| 5 | `ssite:aod_converter_tungsten` | `Tungsten`（钨） | \(3695\) | \(4618\) | **是** | 超高温耐酸炉体。 |
| 6 | `ssite:aod_converter_ta4hfc5` | `TantalumHafniumCarbide`（碳化钽铪） | \(4263\) | \(5328\) | 否 | 终极耐温炉体，对齐 GT6 顶级坩埚。 |

---

## 2. 熔池精炼配方重构（移除 `ssite.recipe.aod`）

移除原 `SsiteMachines.AOD_MAP`（`ssite.recipe.aod`）及机器 GUI 贴图 `assets/ssite/textures/gui/recipes/aod.png`，将精炼逻辑改为 AOD 熔池内的坩埚反应：

1. **早期破局档 · 空气硬吹炼 304（仅需 `KU` 鼓入 `Air`）**
   - **反应条件**：熔池温度 \(\ge 1943\text{ K}\)，无需接氩气管。
   - **熔池配比**：
     \[
     9\text{ U }\mathtt{CarburizedStainlessSteel} + 1\text{ U }\mathtt{Ferrosilicon} + 2\text{ U }\mathtt{Quicklime} + \mathtt{Air}\text{ (由 KU 注入)} \xrightarrow{\ge 1943\text{ K}} 5\text{ U }\mathtt{StainlessSteel} + 4\text{ U }\mathtt{AodSlag}
     \]
   - **特点**：铬被空气大量氧化进炉渣，收率约 55%，但能在没有深冷空分塔的阶段炼出第一批 304 不锈钢；`AodSlag` 浇铸冷却成块/粉后仍可进离心机回收 `Chromite`。
2. **成熟量产档 · 氩气底吹炼 304（侧面通 `Argon` + `KU` + 关盖）**
   - **反应条件**：熔池温度 \(\ge 1943\text{ K}\)，炉盖关闭，侧面流体管通入 `Argon` 且施加 `KU`。
   - **熔池配比**：
     \[
     9\text{ U }\mathtt{CarburizedStainlessSteel} + 1\text{ U }\mathtt{Ferrosilicon} + 1\text{ U }\mathtt{Quicklime} + 1\text{ U }\mathtt{Fluorite} + \mathtt{Argon} \xrightarrow{\ge 1943\text{ K}} 9\text{ U }\mathtt{StainlessSteel} + 1\text{ U }\mathtt{AodSlag}
     \]
   - **特点**：100% 满收率产出 `StainlessSteel`，不再需要单独的氮氧过渡档。
3. **高阶耐蚀档 · 氩气精炼 316L 钼不锈钢（侧面通 `Argon` + `KU` + 关盖 + 铌钛及以上炉体）**
   - **反应条件**：熔池内所有组分均处于熔融态——其中 `Molybdenum` 熔点为 \(2896\text{ K}\)，因此熔池温度必须达到 \(\ge 2896\text{ K}\)（钢/不锈钢炉体在此温度下已超过 \(2557\text{ K}\) / \(2428\text{ K}\) 熔毁，自然要求至少 `NiobiumTitanium` 炉体）。
   - **熔池配比**：
     \[
     8\text{ U }\mathtt{CarburizedStainlessSteel} + 1\text{ U }\mathtt{Molybdenum} + 1\text{ U }\mathtt{Ferrosilicon} + 1\text{ U }\mathtt{Fluorite} + \mathtt{Argon} \xrightarrow{\ge 2896\text{ K}} 9\text{ U }\mathtt{StainlessSteel316L} + 1\text{ U }\mathtt{AodSlag}
     \]

---

## 3. 前置 SDK 接口现状（已就绪）

GT6CE 最终提供的是**行为机器坩埚访问**，不是自定义坩埚方块：
- 行为机器的外壳自动实现 `ITileEntityCrucible`；`MachineBehavior` 暴露 `getCrucibleTemperature`、`getCrucibleContentAmount`、`fillMoldAtSide` 三个回调。原版龙头、模具、盆与温度计可直接识别附属 AOD 转炉。
- `CrucibleMold.fillMold(material, amount, temperature, side)` 是 SDK 安全的浇铸入口；行为自己持有熔融材料并扣减已浇出的数量。
- **没有**提供自定义坩埚方块、坩埚熔炼/合金表注入，或「关盖 + 侧面流体 + `KU` → `Argon` 驻留」的现成钩子；这些逻辑由本附属在行为机器内自行实现，见第 2 节与实施记录。

---

## 4. 明确不做与远期记账

- **不保留 UI 槽位式 `ssite.recipe.aod` 单方块机器**：彻底替换为坩埚式交互，不留双轨制。
- **不保留氮气（\(\text{N}_2 + \text{O}_2\)）吹炼 304 配方**：吹空气本就是打入氮气和氧气；304 直接分为「仅 `KU` 吹空气（低收率破局）」与「通氩气 + `KU`（100% 满收率）」两档。
- **远期记账 · 大型多方块 AOD 转炉（Large AOD Converter）**：对标 GT6 的 \(3\times 3\times 5\) 大坩埚（`MultiBlockCrucible`，\(1.10\times\) 熔点上限、大容量冶炼），记入后续里程碑账本，不在本卡实现。

---

## 5. 实施记录

实现位置：

| 内容 | 文件 |
|---|---|
| 六档炉体/布局/炉盖零件声明 | `common/src/main/java/com/lombinaxmasson/ssite/machine/SsiteMachines.java` |
| 炉体档位、熔点、熔毁温度与热容 | `common/src/main/java/com/lombinaxmasson/ssite/machine/AodTier.java` |
| 熔池、升温、吹气、反应与浇铸 | `common/src/main/java/com/lombinaxmasson/ssite/machine/AodConverterCore.java` |
| 炉盖右键与状态 | `common/src/main/java/com/lombinaxmasson/ssite/machine/SsiteLidPart.java` |
| 行为工厂 | `common/src/main/java/com/lombinaxmasson/ssite/machine/SsiteBehaviorMachines.java` |
| 两端氩气罐桥接 | `forge/src/**/machine/AodBehavior.java` 与 `neoforge/src/**/machine/AodBehavior.java` |
| 模型/贴图/语言/合成表 | `common/src/main/resources/assets/ssite/**`，`forge/src/main/resources/data/ssite/**`，`neoforge/src/main/resources/data/ssite/**` |

关键实现选择：

- **六档 = 六个行为机器类型**（`ssite:aod_converter_<suffix>`），每档独立 `machineType`、模型和合成表；熔点、熔毁上限（\(\lfloor 1.25 \times\) 熔点 \(\rfloor\)）与热容（7 U 炉体质量，GT 的 `1 + kg/100` HU/K 规则）在放置时从材料表读取。
- **加热只看 `HU`，鼓风只看 `KU`**：主能量输入 `HU`，附属能量输入 `KU`；`CU`/`Ignis` 不接（SDK 一个行为机器只有主+副两路能量）。
- **煤气计量**：每 1000 `KU` 折算 1 U 燃气；关盖且氩气罐有 `Argon` 时优先耗 100 mB/单位注氩，否则注空气；开盖或熔池低于 \(1943\text{ K}\) 时燃气排空。
- **顶部投料**：炉盖零件自带 1 格投料槽，只有关盖时拒绝（`PartSlots.insert(TOP)` + `open` 判断）；本体每 tick 从炉盖槽或 GUI 投料槽取一件可熔料进池。这样漏斗/投料器站在炉盖顶上就能喂料，不受「本体顶面被炉盖占住」影响。
- **反应**：三档产物按卡内配比；`AodSlag` 不进入熔池，而是以粉的形式进入输出槽（`AodSlag` 只有 `dust()` 形态，没有可浇铸的方块形态，浇进模具会卡住）。离心回收配方沿用 M1 的 `gt.recipe.centrifuge`。
- **外观**：六档各自一套 16×16 侧/顶/底贴图（程序生成，随档位变色），四面侧贴图都带进气口。GT6CE 目前没有给 behavior machine 注册方块/物品颜色处理器，因此没有走 `tintindex` 路线。
- 旧的 `ssite.recipe.aod` 配方表、旧单方块模型/方块状态/合成表已删除；`textures/gui/recipes/aod.png` 因工作区里有未提交改动暂留，代码已不再引用。

## 6. 验收标准

- [x] 六档 AOD 转炉（`steel`、`stainless_steel`、`niobium_titanium`、`tungsten_steel`、`tungsten`、`ta4hfc5`）全部注册完成，\(1\times 1\times 2\) 结构（行为机器本体 + 上方炉盖零件、四面进气口贴图）与分档贴图正常，中英文译名与合成表齐全
- [x] 顶部炉盖可右键切换开关：开盖时顶部漏斗可以投料，关盖时顶部管道投料被拒；放大镜显示开关状态
- [x] 不接氩气管、关盖仅施加 `KU` 时，在 \(\ge 1943\text{ K}\) 下可将高碳不锈钢粗钢按 5/9 收率吹炼为 `StainlessSteel` + `AodSlag`
- [x] 任意水平侧面接入 `Argon` 管道 + 施加 `KU` + 关盖 + \(\ge 1943\text{ K}\) 时，`Argon` 驻留并触发 9/9 收率 `StainlessSteel` 反应
- [x] 投入 `Molybdenum` 冶炼 `StainlessSteel316L` 时必须升温至 \(\ge 2896\text{ K}\)；钢/不锈钢 AOD 在此温度前熔毁，铌钛（上限 \(2931\text{ K}\)）及以上炉体可完成冶炼
- [x] 侧面挂载原版浇铸口（`Faucet`）可将熔融 `StainlessSteel`、`StainlessSteel316L` 浇铸至模具（`Mold` / `Basin`），温度计可读温度（`AodSlag` 改走出粉口，见实施记录）
- [x] `.\scripts\build.ps1 build`（Forge 1.20.1 与 NeoForge 1.21.1）双端编译与打包通过
- [x] NeoForge 专用服务器启动验证通过：`GT addon API 0 ready: [ssite]`，52366 条配方全部解析，日志无 ERROR（2026-10-22）
- [ ] **待在游戏内逐条验收**：升温曲线、吹气速率、熔毁时机与 GUI 布局需实机调参

## 7. 已知偏差

1. 炉盖是结构零件而不是控制器的一部分，且没有开合动画；开关只改逻辑状态。
2. 炉盖关闭时 GUI 里仍可手动放料（菜单不是物理顶面），只有顶部管道投料被 `open` 状态拦截。
3. `AodSlag` 走物品输出槽，不走熔池浇铸。
4. 只接 `HU` + `KU`，没有 `CU`/`Ignis`。
5. 六档靠独立贴图区分，不使用运行时材质染色。
