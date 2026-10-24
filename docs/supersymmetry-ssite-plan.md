# Stainless Steel Is Too Easy (`ssite`) — 开发计划与任务清单

> **模组定位**：基于 GT6CE Addon SDK 的硬核工业/化工扩展附属。  
> **核心梗**：在原版 GT6 里，不锈钢居然只要往坩埚里丢铁、镍、铬、锰就能化出来，然后立刻解锁蒸馏塔、深冷塔和全套大型化工机！本附属第一刀先砍掉“坩埚搓不锈钢”，把真实的火法/湿法冶金、水净化、石化单体分离、高分子合成与化工唱片一步步搭起来。
>
> **执行方式**：采用 `docs/card-plans/` 计划卡模式推进（见 [`docs/card-plans/总览.md`](card-plans/总览.md)），每张卡独立验收后再合入下一张。  
> **接口参考**：`../GregTech6CommunityEdition/GregTech6/docs/ADDON_API.zh-CN.md`

---

## 总体分期路线图

为了避免“摊子铺太大写到一半弃坑”，整个工程拆成 **可独立交付的里程碑（Milestone）**。每个阶段做完都能直接进游戏跑通闭环：

1. **M0 · 脚手架与基础设施**：跑通 SDK 依赖、ServiceLoader、语言合并与测试运行。
2. **M1 · 招牌主线「不锈钢没那么容易」**：禁用原版坩埚不锈钢，实装专属 **AOD 转炉 (`ssite:aod_converter`)** 与**三级不锈钢体系**（高碳不锈钢粗钢 $\rightarrow$ 304 标准不锈钢三档吹炼 $\rightarrow$ 316L 超低碳钼不锈钢与耐酸管道），并用早期空气硬吹低收率路线破除深冷塔空分死锁。
3. **M2 · 公用工程：水净化与卤水链**：絮凝 $\rightarrow$ 污泥 $\rightarrow$ 酸碱中和 $\rightarrow$ 离子交换 $\rightarrow$ RO 反渗透 $\rightarrow$ UV 紫外超纯水；配套盐水/卤水提溴碘锂镁（需 316L 耐酸管线）。
4. **M3 · 纯石化中游：单体分离与内部自循环**：承接炼油与裂解，实装 **BTX 芳烃萃取**、**二甲苯三兄弟分离与异构化**、**C4 丁烯拆分**、**C5 馏分二聚拆解（异戊二烯 + 双环戊二烯）**。
5. **M4 · 纯石化下游：四大高分子家族（只做产出，暂不改下游）**：聚烯烃、乙烯基家族（PVC/PVAc/PVA/PVB/EVA）、五大合成橡胶与硫化体系、芳烃工程塑料与芳纶/碳纤维、湿法冶金萃取剂。
6. **M5 · 活泼与难熔金属篇：「钛铝锆也没那么容易」**：砍掉原版矿石/氧化物离心与电解直拆捷径，实装 **拜耳法铝与伴生镓**、**镁-氯还原大循环**、**氯化除钒与 Kroll 海绵钛**、**锆石氯化与 MIBK 萃取拆分锆/铪**、**钶钽铁矿 HF 浸出与 MIBK 拆分铌/钽**，并配套专属 **水冷铜电弧重熔炉 (`ssite:var_furnace`)** 重熔海绵金属与超高温合金，彻底打通 M1 全六档 AOD 转炉（铌钛、$\text{Ta}_4\text{HfC}_5$）的生存闭环。
7. **M6 · 湿法萃取篇：稀土三矿全分离与铀钍前段**：砍掉 `RareEarth` 离心直拆与氟碳铈矿/晶质铀矿电解直熔捷径；实装氟碳铈矿、独居石、磷钇矿三矿开矿，用 M4 的 `P204` / `Aliquat 336` 分级萃取切分轻中重稀土并结合 $\text{Ce}^{4+}$/$\text{Eu}^{2+}$ 变价提纯；离子交换树脂提黄饼 + `TBP` 萃取核纯铀钍。
8. **M7 · 有色主金属篇：铜镍锍、铂族六兄弟与铅锌伴生渣网**：砍掉硫化矿常温电解/一步焙烧与铂族泥离心直拆；实装铜镍钴造锍（绿锍 $\rightarrow$ 白锍）、铜阳极泥提金银硒碲、氯络合物分离铂族六兄弟（Pt/Pd/Rh/Ir/Ru/Os）、湿法炼锌伴生镉铊铟锗、火法炼铅五步精炼回收砷锑铋银、辉钼矿烟气树脂提铼。
9. **M8 · 彩蛋支线：化工音乐唱片工业**：用自产的 PVC/PVAc/炭黑（黑胶线）或 聚碳酸酯/酞菁染料/银箔（CD-R 激光刻录线）批量压制/刻录自定义音乐唱片。
10. **M9 · 下游挂载与电子/结构材料消纳**：工业油品与线缆橡胶挂载、电路板世代升级（酚醛 $\rightarrow$ G10 $\rightarrow$ FR-4 $\rightarrow$ Kapton）、高纯硅锗/GaAs 晶圆与光刻胶、燃料电池与防弹护甲/失蜡铸造。

---

## M0 · 工程骨架与 SDK 清单

- [x] **基础模组元数据**
  - [x] 确定 `addonId` 为 `ssite`，配置 `mods.toml`（Forge 1.20.1）与 `neoforge.mods.toml`（NeoForge 1.21.1），声明强制依赖 `gt6ce`（`side="BOTH"`）。
  - [x] 在 `@Mod` 构造方法中调用 `GregTechAddons.register(new GregTechAddon() { ... })`。
- [x] **SPI 服务文件挂载 (`META-INF/services/`)**
  - [x] `com.gregtech.gregtech.api.addon.AddonMaterialContributor`（注册新材料、形态、坩埚反应与禁用规则）
  - [x] `com.gregtech.gregtech.api.addon.AddonMachineContributor`（注册新配方表、单方块/多方块机器、管道、气罐）
- [x] **资源与语言文件结构**
  - [x] `assets/gt6ce/lang/en_us.json` 与 `zh_cn.json`（存放新增材料的 `material.gt6ce.<小写材料名>` 译名）
  - [x] `assets/ssite/lang/en_us.json` 与 `zh_cn.json`（存放自定义方块、机器、唱片物品译名）
  - [x] 数据包覆盖目录：`data/ssite/recipes/`（1.20.1）或 `data/ssite/recipe/`（1.21.1），用于放 `gt6ce:machine_removal`。

---

## M1 · 招牌模块：「不锈钢没那么容易」(Stainless Steel Overhaul)

> **设计目标**：原版 GT6 的 `GTAlloyTable` 有 12 条不锈钢坩埚配方（铁/熟铁/生铁/陨铁 × 镍/殷钢/镍铬 + 铬 + 锰），搅拌机还能直接混粉。这一章把它们全砍掉，换成真实的 **初炼粗钢 + 专属 AOD 转炉精炼**，并建立 **粗钢 → 304 标准不锈钢 → 316L 钼不锈钢** 的三级体系。详细计划卡见 [`docs/card-plans/总览.md`](card-plans/总览.md)。

### 1.1 砍掉原版捷径
- [x] **禁用坩埚不锈钢**：在 `AddonMaterialContributor.crucibleRemovals()` 中返回：
  ```java
  new CrucibleReactionRemoval("StainlessSteel", List.of()) // 空原料表 = 删除 StainlessSteel 的全部内置坩埚反应
  ```
- [x] **禁用搅拌机不锈钢**：通过 `gt6ce:machine_removal` 移除 `gt.recipe.mixer` 中产出熔融 `StainlessSteel` 的 12 条合金配方。GT6CE 没有对应的混粉行。

### 1.2 新增材料与管道 (`AddonMaterialContributor` & `AddonMachineContributor`)
- [x] `Ferrochromium`（铬铁合金，$\text{FeCr}_2$，`metal()`）
- [x] `Ferrosilicon`（硅铁合金，$\text{FeSi}_3$，`metal()`）
- [x] `CarburizedStainlessSteel`（第一级：高碳不锈钢粗钢，`metal()`，初炼产物）
- [x] `StainlessSteel316L`（第三级：316L 超低碳钼不锈钢，$\text{Fe}_6\text{Cr}_2\text{NiMo}$，`metal()`，搭配原版 `StainlessSteel` 作为第二级 304 标准不锈钢）
- [x] `SodiumChromate`（铬酸钠，$\text{Na}_2\text{CrO}_4$，`dust()`）与 `SodiumDichromate`（重铬酸钠，$\text{Na}_2\text{Cr}_2\text{O}_7$，`dust()`）
- [x] `AodSlag`（AOD 精炼炉渣，`dust()`，可离心回收氧化铬/钙渣）
- [x] `stainless_steel_316l` 耐酸流体管（`FluidPipeDeclaration`，用于后续卤水吹溴与强酸石化管线）

### 1.3 专属设备：坩埚式 AOD 精炼转炉（见 [`AOD转炉坩埚化详细计划.md`](card-plans/AOD转炉坩埚化详细计划.md)）
- [x] 初版单方块配方机 `ssite:aod_converter` 与 `ssite.recipe.aod`（已在 M1 前四卡跑通验证；坩埚化重做时已删除）
- [x] **坩埚化重做（\(1\times 1\times 2\) 结构）**：下方为行为机器本体（四面进气口贴图，任意侧面可接氩气管、浇铸口 `Faucet`、温度计 `Thermometer`，底/侧接 `HU` 与 `KU`），上方为 `ssite:aod_lid` 结构零件炉盖（右键开关；开盖从顶部投料，关盖密闭锁气）。`CU`/`Ignis` 暂不接。
- [x] **六档炉体材质与熔点门槛（\(\text{熔毁温度} = \lfloor \text{熔点} \times 1.25 \rfloor\)）**：
  - [x] 钢（`Steel`，上限 \(2557\text{ K}\)）：破局炉体，炼制第一批 304 不锈钢。
  - [x] 不锈钢（`StainlessSteel`，上限 \(2428\text{ K}\)，耐酸）：常规耐酸炉体。
  - [x] 铌钛（`NiobiumTitanium`，上限 \(2931\text{ K}\)）：跨越纯钼熔点（\(2896\text{ K}\)），解锁 316L 钼不锈钢。
  - [x] 钨钢（`TungstenSteel`，上限 \(3587\text{ K}\)）、钨（`Tungsten`，上限 \(4618\text{ K}\)，耐酸）、碳化钽铪（`TantalumHafniumCarbide`，上限 \(5328\text{ K}\)）：高阶宽温区炉体。
- [ ] **远期记账**：对标 GT6 大坩埚（`MultiBlockCrucible`）的**大型多方块 AOD 转炉（Large AOD Converter）**。

### 1.4 产线配方清单
- [x] **铬线分流（冶金级 vs 高纯级）**
  - [x] **冶金级铬铁（用于炼不锈钢）**：铬铁矿（`Chromite`）+ 生石灰（`Quicklime`）+ 焦炭/碳 $\xrightarrow{\text{高炉/煅烧炉}}$ **铬铁 (`Ferrochromium`)** + 一氧化碳。
  - [x] **化工级纯铬（用于镍铬合金、坎塔尔、镀铬）**：铬铁矿 + 纯碱（`SodiumCarbonate`）+ 氧气 $\xrightarrow{\text{煅烧炉}}$ 铬酸钠 $\xrightarrow{\text{酸化}}$ 重铬酸钠 $\xrightarrow{\text{煅烧炉}}$ 纯铬（`Chromium`）+ 氧化铝。碳还原和铝热并成一条，没有单独的三氧化二铬材料。
- [x] **硅铁脱氧剂制备**
  - [x] 二氧化硅（石英粉）+ 铁粉 + 焦炭/碳 $\rightarrow$ **硅铁 (`Ferrosilicon`)** + 一氧化碳。
- [x] **第一步：初炼高碳不锈钢粗钢 (`CarburizedStainlessSteel`)**
  - [x] 铁源（铁 / 熟铁 / 生铁 / 陨铁）+ 镍（或殷钢）+ **铬铁 (`Ferrochromium`)** + 锰 $\xrightarrow{\text{坩埚合金反应}}$ **高碳不锈钢 (`CarburizedStainlessSteel`)**。
- [x] **第二步：坩埚式 AOD 吹炼标准不锈钢 (`StainlessSteel` / 304)**
  - [x] **早期破局档（仅 `KU` 空气硬吹，免深冷塔）**：熔池内高碳不锈钢 + 硅铁 (`Ferrosilicon`) + 生石灰 (`Quicklime`) + **`KU` 鼓入空气 (`Air`)** $\xrightarrow{\ge 1943\text{ K}}$ **低收率 `StainlessSteel`（5/9 收率）** + `AodSlag` 炉渣。
  - [x] **成熟氩氧档（侧面通 `Argon` + `KU` + 关盖）**：熔池内高碳不锈钢 + 硅铁 + 生石灰 + 萤石 (`Fluorite`) + **`Argon`（驻留不消失）** $\xrightarrow{\ge 1943\text{ K}}$ **100% 满收率 `StainlessSteel`**。
- [x] **第三步：铌钛及以上 AOD 精炼 316L 钼不锈钢 (`StainlessSteel316L`)**
  - [x] 熔池内高碳不锈钢 + **纯钼 (`Molybdenum`，熔点 \(2896\text{ K}\))** + 硅铁 + 萤石 + **`Argon`（侧面管道 + `KU` + 关盖）** $\xrightarrow{\ge 2896\text{ K}}$ **316L 钼不锈钢 (`StainlessSteel316L`)**（钢/不锈钢炉体在此温度前熔毁，硬性要求铌钛及以上 AOD 炉体）。

---

## M2 · 公用工程：水净化与卤水综合利用

> **设计目标**：利用 GT6 原版已有的 `Ocean`、`Saltwater`、`Brine`、`DistW`、`Sewage`、`Sludge` 和现成机器（`bath`、`mixer`、`autoclave`、`distillery`、`distillationtower`、`drying`、`centrifuge`、`electrolyzer`），做一条层层递进的水处理链与苦卤综合利用网。详细计划卡见 [`docs/card-plans/总览.md`](card-plans/总览.md)。

### 2.1 水净化阶梯清单
- [x] **1 级 · 絮凝澄清水 (`ClarifiedWater`)**
  - [x] 河水/原水/污水 + 明矾/硫酸铝（`Alum` / `VitriolOfClay`） $\xrightarrow{\text{化学浴槽 } \texttt{bath}}$ 澄清水 + **污泥 (`Sludge`)**（进干燥机/离心机回收粘土、生物质与痕量稀土）。
- [x] **2 级 · 软化/脱矿水 (`DemineralizedWater`)**
  - [x] 澄清水经纯碱软化与石英砂床过滤（`bath` + `QuartzSand` 催化保留），沉淀方解石/菱镁矿水垢并支持快速无垢蒸馏。
- [x] **3 级 · 去离子水 (`DeionizedWater`)**
  - [x] 通过**沸石分子筛 (`Zeolite`)**（未来与 M4 交联聚苯乙烯阴阳离子交换树脂联动）洗脱痕量金属离子，供湿法冶金与聚合反应使用。
- [x] **4 级 · 反渗透纯水 (`FilteredWater`)**
  - [x] 去离子水通过**醋酸纤维素 RO 膜 (`foil CelluloseAcetate`)** 在高压釜（`autoclave`）中高压反渗透过滤（膜组件循环使用）。
- [x] **5 级 · 电子级超纯水 (`UltrapureWater`)**
  - [x] 臭氧（`Ozone`）氧化脱除有机碳（TOC）+ 蓝宝石/钻石紫外透光灯窗（`mixer` + `lens` 催化保留）杀菌脱气，预留给未来的晶圆清洗与光刻线。

### 2.2 海水淡化与卤水（Brine）副产网
- [x] **海水浓缩成苦卤**：海水/盐水经蒸馏塔（或蒸馏室）浓缩产出蒸馏水 + 浓缩卤水（`Brine`）+ 食盐 + 石膏（`Gypsum`）。
- [x] **卤水除镁除钙**：生石灰消化制熟石灰（`CalciumHydroxide`），与苦卤和纯碱反应沉淀**氢氧化镁 (`MagnesiumHydroxide`)**（盐酸中和得 `MagnesiumChloride` 接通电解镁线）与方解石，得除杂卤水（`DecalcifiedBrine`）。
- [x] **卤水吹溴与提锂**：除杂卤水酸化后通入氯气（$\text{Cl}_2$ 置换 $\text{Br}_2$）吹出**粗溴蒸汽 (`CrudeBromine`)** + **富锂母液 (`LithiumRichBrine`)**；粗溴精馏得**液溴 (`Bromine`)** 与**碘 (`Iodine`)**（做四溴双酚 A 阻燃环氧必需！）；富锂母液加纯碱沉出**碳酸锂 (`LithiumCarbonate`)**，再经盐酸转化与熔盐电解得金属锂并回收氯气。

---

## M3 · 纯石化中游：单体分离与内部自循环

> **设计目标**：接在炼油和蒸汽裂解后面，把混合馏分拆成高纯化学单体，并实现**石化内部副产物零浪费闭环**。

### 3.1 新增核心单体与中间流体清单
- [x] **芳烃抽提系**：重整油 (`Reformate`)、裂解汽油 (`PyrolysisGasoline`)、糠醛 (`Furfural`) 或环丁砜 (`Sulfolane`) 萃取剂、BTEX 芳烃混合物 (`BTEX`)、抽余油 (`Raffinate`)。
- [x] **BTX 与二甲苯三兄弟**：苯 (`Benzene`)、甲苯 (`Toluene`)、乙苯 (`Ethylbenzene`)、混合二甲苯 (`Xylene`)、**邻二甲苯 (`OrthoXylene`)**、**间二甲苯 (`MetaXylene`)**、**对二甲苯 (`ParaXylene`，流体+晶粉)**。
- [x] **C4 烯烃系**：混合丁烯 (`Butene`)、**1-丁烯 (`OneButene`)**、**2-丁烯 (`TwoButene`)**、**异丁烯 (`Isobutylene`)**、**丁二烯 (`Butadiene`)**。
- [x] **C5 馏分系**：C5 馏分 (`CFiveFraction`)、二聚 C5 馏分 (`DimerizedCFiveFraction`)、**异戊二烯 (`Isoprene`)**、**环戊二烯 (`Cyclopentadiene`)**、**双环戊二烯 (`Dicyclopentadiene` / DCPD)**、戊烷 (`Pentane`)、**降冰片烯 (`Norbornene` 粉)**。
- [x] **基础衍生物单体（部分）**：异丙苯 (`Cumene`)、苯酚 (`Phenol`)、丙酮 (`Acetone`)、**双酚 A (`BisphenolA` 粉)** 已随 M3 注册；氯乙烯、醋酸乙烯、环氧乙烷、乙二醇、环氧氯丙烷、丙烯腈、丙烯酸、MMA、苯酐、PTA、间苯二甲酸、己内酰胺、MDI 已在 M4 各卡实装。

### 3.2 核心工艺与内部副产自循环清单
- [x] **BTX 溶剂抽提循环**：裂解汽油 / 重整油 + 糠醛 $\xrightarrow{\text{筛板塔/浴槽}}$ 芳烃萃取液 + **抽余油**（回裂解炉）+ **C5 馏分**；萃取液汽提回收糠醛，精馏得 **苯 + 甲苯 + 乙苯 + 混合二甲苯**。
- [x] **二甲苯分离与异构化闭环（经典设计！）**
  - [x] 混合二甲苯 $\xrightarrow{\text{蒸馏塔}}$ **邻二甲苯 (20%)** + 间/对二甲苯混液 (80%)。
  - [x] 间/对二甲苯混液 $\xrightarrow{\text{冷冻机 } \texttt{freezer}}$ **对二甲苯晶体 (25%)** + **间二甲苯母液 (75%)**（结晶坩埚无流体输出，改用冷冻机）。
  - [x] **间二甲苯** $\xrightarrow{\text{ZSM-5 分子筛催化异构化}}$ **100% 转回对二甲苯**（解决间二甲苯爆仓！）。
- [x] **甲苯消纳闭环**
  - [x] 路线 A（催化歧化）：甲苯 $\xrightarrow{\text{ZSM-5}}$ 苯 + 二甲苯混液。
  - [x] 路线 B（加氢脱烷基）：甲苯 + 氢气 $\rightarrow$ 苯 + 甲烷 + 少量**联苯 (`Biphenyl`)**（联苯氯化做多氯联苯变压器油）。
- [x] **C4 丁烯精馏与丁二烯**
  - [x] 混合丁烯 $\xrightarrow{\text{蒸馏塔}}$ **异丁烯 (50%) + 1-丁烯 (30%) + 2-丁烯 (20%)**。
  - [x] *非石油彩蛋路线*：乙醇 + 乙醛 $\xrightarrow{\text{钽催化剂}}$ **丁二烯 + 水**（两步法 Ostromislensky，Lebedev 一步法不做）。
- [x] **C5 馏分热二聚巧妙分离（免深冷提纯异戊二烯）**
  - [x] C5 馏分 $\xrightarrow{\text{加热器}}$ 二聚 C5（里面的环戊二烯自己聚合成高沸点的双环戊二烯 DCPD）。
  - [x] 二聚 C5 $\xrightarrow{\text{蒸馏塔}}$ **异戊二烯 (36%)** + **戊烷 (38%)** + **双环戊二烯 DCPD (13%)**。
  - [x] DCPD $\xrightarrow{\text{高温热解}}$ 2 份 **环戊二烯**（再与乙烯加成得**降冰片烯**）。
  - [x] *合成异戊二烯路线*（Prins 法，M4 卡 2 实装）：异丁烯 + 甲醛 $\rightarrow$ 二甲基二噁烷 $\xrightarrow{\text{固体磷酸催化裂解}}$ **异戊二烯** + 甲醛循环。
- [x] **异丙苯法制苯酚与双酚 A**
  - [x] 苯 + 丙烯 $\rightarrow$ 异丙苯 $\xrightarrow{+\text{O}_2}$ 过氧化氢异丙苯 $\xrightarrow{\text{酸解}}$ **苯酚 + 丙酮**（缩合成 **双酚 A**，M3 用盐酸催化，阳离子交换树脂路线移交 M4）。

> **M3 偏差**：新增炼油前段卡（GT6CE 无原油蒸馏配方）；对二甲苯分离因结晶坩埚无流体输出改用冷冻机 `freezer`；Prins 法合成异戊二烯（缺甲醛/固体磷酸）与「基础衍生物单体」中氯乙烯等 13 种（各自需要整套上游）移交 M4；乙醇法丁二烯按两步法（Ostromislensky）实现。

---

## M4 · 纯石化下游：四大高分子与精细化学品（第一阶段只做产出）

> **策略**：按 `docs/supersymmetry-petrochem-uses.md` 表格注册最终材料与形态（粉、板、箔、环、纤维、管），把合成线修通；下游消耗配方留到 M9（原 M6）再挂。

### 4.1 通用热塑性塑料
- [x] **聚烯烃系**：PE（高压自由基聚合 vs 低压 Ziegler-Natta 催化）、**UHMWPE（超高分子量聚乙烯，Kaminsky 茂金属催化，含板与防弹纤维）**、PP（聚丙烯）。
- [x] **乙烯基家族**：
  - [x] 氯乙烯 $\rightarrow$ **PVC**（粉、板、箔、物品管）
  - [x] 醋酸乙烯 $\rightarrow$ **PVAc（聚醋酸乙烯酯）** $\xrightarrow{+\text{乙醇醇解}}$ **PVA（聚乙烯醇）**（副产乙酸乙酯溶剂） $\xrightarrow{+\text{正丁醛}}$ **PVB（聚乙烯醇缩丁醛夹层玻璃膜）**
  - [x] 乙烯 + 醋酸乙烯 $\rightarrow$ **EVA**（粉、微型/小型流体软管）
- [x] **苯乙烯与丙烯酸系**：**PS（聚苯乙烯）**、**交联 PS 圆珠**（苯乙烯 + 二乙烯基苯）、**PMMA（有机玻璃）**。
- [x] **双酚 A 与聚酯系**：**PC（聚碳酸酯）**、**PET（聚对苯二甲酸乙二醇酯，含镀铝 Mylar 膜）**。

### 4.2 热固性树脂
- [x] **酚醛树脂 (`Phenolic`)**：苯酚 + 甲醛。
- [x] **环氧树脂四兄弟**：
  - [x] 双酚 A + 环氧氯丙烷 + 固化剂（双氰胺 / 邻苯二甲酸酐） $\rightarrow$ **普通环氧树脂 (`Epoxy`)** + 浸玻纤出 **强化环氧板（玻璃钢）**。
  - [x] 四溴双酚 A（用上 M2 的溴！）+ 环氧氯丙烷 $\rightarrow$ **阻燃环氧 (`FlameRetardantEpoxy`)**。
  - [x] 甲酚 + 甲醛 + 环氧氯丙烷 $\rightarrow$ **甲酚醛环氧 (`EpoxyCresolNovolacs`)**。
  - [x] 丁二烯 + 丙烯醛 $\rightarrow$ **脂环族环氧液**。
- [x] **聚氨酯 (`Polyurethane`)**：MDI + 乙二醇/多元醇 + 超临界 $\text{CO}_2$ 发泡（顺手加一条：PU 块泡黄染料 = 原版海绵 `minecraft:sponge`！）。
- [x] **氢化石油树脂 (`HydrogenatedPetroleumResin`)**：DCPD + 苯 + 降冰片烯聚合后加氢（消化 C5 副产！）。

### 4.3 合成橡胶与硫化体系
- [x] **五大生胶合成**：
  - [x] 异戊二烯 $\rightarrow$ **聚异戊二烯 (`Polyisoprene`)** + 炭黑混炼
  - [x] 丁二烯 + 苯乙烯 $\rightarrow$ **丁苯橡胶 (`SBR`)** + 炭黑混炼
  - [x] 丁二烯 + 丙烯腈 $\rightarrow$ **丁腈橡胶 (`NBR`)**
  - [x] 丁二烯氯化 $\rightarrow$ 氯丁二烯 $\rightarrow$ **氯丁橡胶 (`Neoprene`)**
  - [x] 偏氟乙烯 + 六氟丙烯 $\rightarrow$ **氟橡胶 (`Viton`)**
- [x] **统一硫化成型配方**：生胶粉 + 硫磺 + 催化剂（氧化锌 / 氧化镁，不消耗）$\xrightarrow{\text{搅拌机}}$ 对应橡胶的**硫化胶锭**（实现上省略模具，板、环等形态交给 GT6 通用金属加工）。

### 4.4 合成纤维与特种工程塑料（按喜好挑选实装）
- [x] **合成纤维四强**：
  - [x] 己内酰胺 $\rightarrow$ **尼龙 6 (`Polycaprolactam`)**
  - [x] 对苯二甲酰氯 + 对苯二胺 $\rightarrow$ **凯夫拉 (`Kevlar`)**
  - [x] 间苯二甲酰氯 + 间苯二胺 $\rightarrow$ **诺梅克斯 (`Nomex`)**
  - [x] 丙烯腈 $\rightarrow$ **聚丙烯腈 (PAN) 纤维** $\xrightarrow{\text{预氧化 + 惰性气氛高温碳化}}$ **碳纤维 (`CarbonFiber`)**
- [x] **氟塑料与特种工程塑料**：**PTFE（特氟龙）**、**PVDF**、**Nafion**、**聚砜 (`Polysulfone`)**、**PPS**、**PEEK**、**Kapton（聚酰亚胺）**。
- [x] **湿法冶金萃取剂与功能油品（可选）**：**TBP（磷酸三丁酯）**、**P204**、**Aliquat 336**、**PAO 全合成润滑油**。

### 4.5 补遗（2026-10-23 收尾卡）
- [x] **TDI**：甲苯混酸硝化 $\rightarrow$ DNT $\rightarrow$ 加氢 TDA $\rightarrow$ 光气化 TDI；与聚醚多元醇出 TDI 型聚氨酯（电路 3 与 MDI 两条路线区分）。
- [x] **环氧丙烷 / 丙二醇**：HPPO 法丙烯 + 过氧化氢在 ZSM-5 上直接氧化；水合得丙二醇，并补 PO 型聚醚多元醇。
- [x] **己二酸 / 尼龙 66**：环己酮硝酸氧化得己二酸 $\rightarrow$ 己二腈 $\rightarrow$ 己二胺 $\rightarrow$ **尼龙 66**（可压板、轧箔、拉细线）。
- [x] **PPO（聚苯醚）**：苯酚 + 甲醇甲基化得 2,6-二甲酚，铜催化氧化偶联得 PPO。
- [x] **PEI（聚醚酰亚胺）**：双酚 A + 苯酐折一步得 BPADA，与间苯二胺亚胺化得 PEI。
- [x] **双氰胺**：石灰氮法（生石灰 + 焦炭 + 氮气）；作为环氧的第二条固化路线，苯酐路线保留。

---

## M5 · 活泼与难熔金属篇：「钛铝锆也没那么容易」(Reactive & Refractory Metals)

> **设计目标**：GT6CE 的 `DecompositionRecipeLoader` 会把 `Bauxite`、`Zircon`、`Coltan`、`Columbite`、`Tantalite` 直接塞进离心机（16 EU/t 甩出氧化铝、金属锆、五氧化二铌/钽），还会把 `Alumina`、`Rutile`、`Ilmenite`、`Nb2O5`、`Ta2O5` 直接塞进电解机（64 EU/t 无耗材拆出单质铝、钛、铌、钽），并且 `Bath` 里有 0 EU/t 的 $\text{TiCl}_4 + \text{Mg/Na} \rightarrow \text{Ti}$。本章节把这些直拆捷径全部砍掉，建立 **拜耳法铝（伴生镓） + 镁氯还原大循环 + Kroll 法海绵钛/锆/铪（伴生钒） + MIBK 萃取拆分锆铪/铌钽 + 水冷铜电弧重熔炉（VAR）** 的完整冶金体系，一举完成 M1 全六档 AOD 转炉（含 `NiobiumTitanium` 与 `TantalumHafniumCarbide` $\text{Ta}_4\text{HfC}_5$）的生存闭环。

### 5.1 砍掉原版氧化物与矿石直拆捷径
- [x] **移除离心机直拆 (`gt.recipe.centrifuge`)**：通过 `gt6ce:machine_removal` 移除 `Bauxite`（铝土矿）、`Zircon`（锆石 $\rightarrow$ 直接出锆粉！）、`Coltan`（钶钽铁矿）、`Columbite`（铌铁矿）、`Tantalite`（钽铁矿）的自动离心分解配方。
- [x] **移除电解机无耗材直拆 (`gt.recipe.electrolyzer`)**：移除 `Alumina`（氧化铝无冰晶石直接出铝）、`VitriolOfClay`（硫酸铝直接电解出氧化铝）、`Rutile`（金红石直接电解出钛）、`Ilmenite`（钛铁矿直接电解出铁+钛）、`NiobiumPentoxide`（$\text{Nb}_2\text{O}_5$）、`TantalumPentoxide`（$\text{Ta}_2\text{O}_5$）的自动电解配方。
- [x] **移除化学浴 0 电耗还原钛 (`gt.recipe.bath`)**：移除原版 `TiCl4 + Mg -> Ti + MgCl2` 与 `TiCl4 + Na -> Ti + NaCl` 的 0 EU/t 直出钛粉配方。

### 5.2 铝与镓：拜耳法补完、赤泥与冰晶石熔盐电解
- [x] **拜耳法与赤泥 (`RedMud`)**：铝土矿 + 氢氧化钠高压浸出产出偏铝酸钠 (`SodiumAluminate`) + **赤泥 (`RedMud`)**（酸浸回收铁、钛残渣）。
- [x] **种分母液提镓 (`Gallium`)**：偏铝酸钠种分出氢氧化铝 (`AluminiumHydroxide`) 的同时，从富集母液中碳酸化沉淀粗镓酸钠/氢氧化镓 $\rightarrow$ 电解得半导体级 **金属镓 (`Gallium`)**。
- [x] **冰晶石合成与熔盐电解铝**：萤石 + 浓硫酸制 $\text{HF}$ $\rightarrow$ 合成氟化铝 (`AluminiumFluoride`) 与冰晶石 (`Cryolite`)；强制氧化铝 (`Alumina`) 在熔融冰晶石 + 氟化铝助熔下消耗碳粉阳极电解产出（`CoalCoke` 阳极因原版分解配方碰撞未实装） **铝 (`Aluminium`)** + $\text{CO}_2$。

### 5.3 专属设备：水冷铜电弧重熔炉 (`ssite:var_furnace`)
- [x] **结构与复用**：\(1\times 1\times 2\) 结构，下方为单材质 **水冷铜电弧重熔炉 (`ssite:var_furnace`)**（仅 1 套方块贴图，靠水冷铜壁自生凝壳保护，不按熔点烧穿），上方直接复用 `ssite:aod_lid` 炉盖。
- [x] **能量与气氛接口**：底部/侧面接入 **`EU`**（电弧加热），四面水平接入 **`Argon`**（单流体输入槽，与 AOD 保持一致；关盖锁气形成惰性保护气氛）。
- [x] **海绵金属重熔与超高温合金熔炼**：
  - [x] **`TitaniumSponge`（海绵钛）**、**`ZirconiumSponge`（海绵锆）**、**`HafniumSponge`（海绵铪）** 仅注册为 `.dust()`（无 `.metal()`，无法在普通敞口坩埚中熔化或直接压锭）。
  - [x] 在 VAR 电弧炉中关盖通 `Argon` 加热至对应熔点（Ti $1941\text{ K}$ / Zr $2128\text{ K}$ / Hf $2506\text{ K}$），海绵金属转化为纯净液态 `Titanium` / `Zirconium` / `Hafnium`，经原版浇铸口（`Faucet`）浇入模具成锭。
  - [x] 支持在氩气保护下电弧熔炼 **`NiobiumTitanium`（铌钛，$2345\text{ K}$）** 与 **`TantalumHafniumCarbide`（碳化钽铪 $\text{Ta}_4\text{HfC}_5$，$4263\text{ K}$）**，解决普通坩埚在具备 $\text{Ta}_4\text{HfC}_5$ 之前无法承受 $4263\text{ K}$ 的鸡生蛋死锁！

### 5.4 钛与钒：氯化、精馏除钒与 Kroll 镁热还原闭环
- [x] **沸腾氯化**：钛铁矿 (`Ilmenite`) / 金红石 (`Rutile`) + 碳/焦炭 + 氯气 $\rightarrow$ **粗四氯化钛 (`CrudeTitaniumTetrachloride`)** + $\text{FeCl}_3$ + $\text{CO}$。
- [x] **粗 $\text{TiCl}_4$ 精馏提钒**：粗四氯化钛经蒸馏塔精馏得纯 **`TitaniumTetrachloride`** + **三氯氧钒 (`VanadiumOxytrichloride`，$\text{VOCl}_3$)**；$\text{VOCl}_3$ 水解煅烧得五氧化二钒 (`VanadiumPentoxide`)，铝热/钙热还原得金属钒 (`Vanadium`)。
- [x] **Kroll 还原与镁-氯大循环**：纯 $\text{TiCl}_4 + 2\text{Mg} \rightarrow$ **海绵钛 (`TitaniumSponge`)** + $2\text{MgCl}_2$；副产氯化镁（连同 M2 卤水线的氯化镁）电解再生 **金属镁 (`Magnesium`) + 氯气 (`Chlorine`)**，实现还原剂与氯气 100% 闭环。

### 5.5 锆与铪：锆石氯化、MIBK 萃取拆分与海绵锆/铪
- [x] **锆石碳热氯化**：锆石 (`Zircon`) + 碳 + 氯气 $\rightarrow$ **含铪粗四氯化锆 (`CrudeZirconiumTetrachloride`)** + 四氯化硅 (`SiliconTetrachloride`，水解回二氧化硅与盐酸）。
- [x] **MIBK 溶剂萃取分锆铪**：粗四氯化锆水解为氯氧化锆溶液 (`ZirconylChlorideSolution`)，用 **M4 的 `MIBK`（甲基异丁基酮）** + 硫氰酸铵在搅拌机/离心机中萃取分相，拆出 **纯锆相** 与 **富铪相**（有机相 `MIBK` 再生循环）。
- [x] **二次氯化与 Kroll 还原**：纯氧化锆与氧化铪分别氯化得纯 $\text{ZrCl}_4$ 与 $\text{HfCl}_4$，经镁热还原得 **海绵锆 (`ZirconiumSponge`)** 与 **海绵铪 (`HafniumSponge`)**，进 VAR 电弧炉重熔出核级锆锭与铪锭。

### 5.6 铌与钽：氢氟酸浸出、MIBK 萃取拆分与 AOD 全档位闭环
- [x] **铌钽矿混酸浸出**：钶钽铁矿 (`Coltan`) / 铌铁矿 (`Columbite`) / 钽铁矿 (`Tantalite`) + 氢氟酸 (`HF`) + 硫酸 $\rightarrow$ **氟铌钽酸混合浸出液 (`FluoroniobicTantalicAcid`)** + 硫酸锰渣。
- [x] **MIBK 选择性萃取分铌钽**：用 **`MIBK`** 萃取并分步反萃，得到纯氟钽酸液（加 $\text{KF}$ 沉出氟钽酸钾 `K2TaF7` $\xrightarrow{+\text{Na}}$ **钽 `Tantalum`**）与纯氟铌酸液（氨水沉淀煅烧得 `Nb2O5` $\xrightarrow{+\text{Al}}$ **铌 `Niobium`**）。

---

## M6 · 湿法萃取篇：稀土三矿全分离与铀钍前段 (Rare Earths & Nuclear Hydrometallurgy)

> **设计目标**：堵住 GT6CE 中 `RareEarth` 离心机一步甩出钕/钇/镧/铈/镉/铯、`Bastnasite`（氟碳铈矿）电解或坩埚直接出铈、`Uraninite`（晶质铀矿）/ `Pitchblende`（沥青铀矿）坩埚直接出铀、`Pitchblende` 离心直接拆出钍和铅的捷径。全面启用 M4 储备的 **`P204`、`TBP`、`Aliquat 336`、离子交换树脂**，在零新增机器的前提下跑通稀土三矿分级萃取与核燃料湿法前段。

> **计划卡（2026-10-24 开卡）**：见 [`docs/card-plans/总览.md`](card-plans/总览.md) 的 M6 节，四张卡已拆分并链接。**第 1 张卡（三矿开矿与直拆捷径移除）已于 2026-10-24 关闭**；坩埚直熔移除因 GT6CE 分支编译失败记为已知缺口。

### 6.1 砍掉稀土与铀矿直拆捷径
- [x] **移除离心直拆 (`gt.recipe.centrifuge`)**：移除 `RareEarth`（稀土粉全套形态）与 `Pitchblende`（沥青铀矿）的内置离心分解配方。
- [ ] **移除电解与坩埚直熔**：`Monazite`（独居石）与 `Bastnasite`（氟碳铈矿）的电解分解配方已移除；`Bastnasite` / `Uraninite`（晶质铀矿）/ `Pitchblende` 的坩埚直熔移除已在 GT6CE 工作区实现，但该分支 `RecipeMap` 编译失败导致运行 JAR 无法重建，**仍为已知缺口**。

### 6.2 稀土三矿开矿、分级萃取与变价提纯
- [x] **三矿差异化开矿**：
  - [x] **氟碳铈矿 (`Bastnasite`)**：氧化焙烧脱氟放出 $\text{HF}$ 与 $\text{CO}_2$，盐酸浸出得富轻稀土母液。
  - [x] **独居石 (`Monazite`)**：浓硫酸/热碱分解，优先沉淀分离出 **氢氧化钍精矿 (`ThoriumConcentrate`)** 与微量铀，得轻中稀土母液。
  - [x] **磷钇矿 (`Xenotime` / 富钇矿源)**：酸浸得富重稀土与钇母液。
- [ ] **P204 / Aliquat 336 分级溶剂萃取**：
  - [ ] 稀土母液在 `mixer` + `centrifuge` 中经 **`P204`** 萃取切分为 **轻稀土组（La-Ce-Pr-Nd）**、**中稀土组（Sm-Eu-Gd）**、**重稀土组（Tb-Dy-Y-Lu）**。
  - [ ] **经典变价分离**：轻稀土组氧化沉淀拆出四价铈（$\text{CeO}_2$），余液经 `Aliquat 336` 拆出镧（$\text{La}_2\text{O}_3$）、镨、钕（$\text{Nd}_2\text{O}_3$）；中稀土组用锌粉还原沉淀二价铕（$\text{EuSO}_4 \rightarrow \text{Eu}_2\text{O}_3$），余液拆出钐（$\text{Sm}_2\text{O}_3$）与钆（$\text{Gd}_2\text{O}_3$）；重稀土组拆出钇（$\text{Y}_2\text{O}_3$）与镥。
- [ ] **氟化钙热还原成金属**：各单一稀土氧化物与 $\text{HF}$ 转化为无水氟化物，在炉中用金属钙（或锂）热还原产出对应稀土金属单质。

### 6.3 铀钍湿法前段与 TBP 核纯萃取
- [ ] **铀矿浸出与树脂吸附黄饼**：沥青铀矿 / 晶质铀矿经硫酸 + 氧化剂浸出得硫酸铀酰粗液 $\rightarrow$ 通过 M4 **强碱性阴离子交换树脂 (`AnionExchangeResin`)** 吸附富集、氨水洗脱沉淀得 **重铀酸铵（黄饼 `Yellowcake`）**。
- [ ] **TBP 核纯萃取**：黄饼与独居石钍精矿分别溶于硝酸，用 **`TBP`（磷酸三丁酯）** 在 `mixer` + `centrifuge` 中萃取反萃，产出核级硝酸钍（煅烧还原得钍 `Thorium`）与核级硝酸铀酰（煅烧得 $\text{UO}_2 \xrightarrow{+\text{HF}} \text{UF}_4$）。

---

## M7 · 有色主金属篇：铜镍锍、铂族六兄弟与铅锌伴生渣网 (Base Metals, PGMs & Minor Byproducts)

> **设计目标**：原版 GT6CE 中所有硫化矿（黄铜矿、镍黄铁矿、闪锌矿、方铅矿、辉钼矿、硫铂矿、砷铂矿、辉锑矿）不仅能用电解机（64 EU/t）常温直接拆出纯金属+硫粉，还能直接丢进普通坩埚化出金属单质，或者在焙烧炉里一步烧成纯金属；`PlatinumGroupSludge`（铂族泥）更是离心机一步甩出六种铂族金属。本章节把这些捷径全部砍掉，建立 **「主金属精炼顺带捞出贵金属与稀散元素」** 的真实伴生渣网。

### 7.1 砍掉硫化矿与铂族泥直拆捷径
- [ ] **移除硫化矿常温电解 (`gt.recipe.electrolyzer`)**：移除 `Chalcopyrite`、`Pentlandite`、`Sphalerite`、`Galena`、`Molybdenite`、`Cooperite`、`Sperrylite`、`Cobaltite`、`Arsenopyrite`、`Stibnite`、`Tetrahedrite` 的电解直拆配方。
- [ ] **移除一步焙烧出金属 (`gt.recipe.roasting`) 与铂族泥离心 (`gt.recipe.centrifuge`)**：移除上述硫化矿一步焙烧直接出金属单质的行，以及 `PlatinumGroupSludge` 的离心直拆行。

### 7.2 铜镍钴造锍、铜阳极泥与铂族六兄弟分离
- [ ] **造锍与镍钴分离**：黄铜矿 / 镍黄铁矿熔炼造 **绿锍 (`GreenMatte`)** $\rightarrow$ 吹炼除铁得 **白锍 (`WhiteMatte`)**；白锍氧化酸浸分离出粗铜与硫酸镍钴母液（经 `P204` 萃取拆分镍与钴）。
- [ ] **粗铜电解与铜阳极泥 (`CopperAnodeSlime`)**：粗铜电解产出高纯阴极铜 + **铜阳极泥**；阳极泥焙烧蒸出二氧化硒/二氧化碲（$\text{SO}_2$ 还原得半导体级 **硒 `Selenium`** 与 **碲 `Tellurium`**），脱铜铅后分金银得 **银 (`Silver`)**、**金 (`Gold`)** 与 **铂族精矿**。
- [ ] **铂族六兄弟氯络合分离（Pt / Pd / Rh / Ir / Ru / Os）**：铂族精矿（及焙烧后的硫铂矿 `Cooperite`、砷铂矿 `Sperrylite`）经王水溶解为氯铂族酸母液 $\rightarrow$ 氧化蒸馏优先分离四氧化锇/四氧化钌（氢还原得 **锇 `Osmium`**、**钌 `Ruthenium`**） $\rightarrow$ 氯化铵沉淀氯铂酸铵（煅烧得海绵 **铂 `Platinum`**） $\rightarrow$ 氨络合沉淀得 **钯 `Palladium`** $\rightarrow$ 拆出六氯铱酸铵与氯铑酸铵（氢还原得 **铱 `Iridium`** 与 **铑 `Rhodium`**）。

### 7.3 湿法炼锌与镉、铊、铟、锗伴生回收
- [ ] **闪锌矿焙烧与烟尘提镉铊**：闪锌矿（`Sphalerite`）氧化焙烧产出氧化锌焙砂（`Zincite`）+ $\text{SO}_2$ + **富镉铊烟尘 (`CadmiumThalliumFlueDust`)**；烟尘酸浸后用锌粉置换拆分出 **镉 (`Cadmium`)** 与 **铊 (`Thallium`)**。
- [ ] **浸出电积锌与浸出渣提铟锗**：氧化锌焙砂硫酸浸出电积得金属锌（`Zinc`）并再生硫酸；**锌浸出渣 (`ZincLeachResidue`)** 盐酸溶解后，一路经 **`TBP` 萃取**与锌置换得 **铟 (`Indium`)**，另一路经四氯化锗精馏、超纯水水解与氢还原得半导体级 **锗 (`Germanium`)**。

### 7.4 火法炼铅五步精炼与辉钼矿烟气提铼
- [ ] **方铅矿火法五步精炼（回收 As / Sb / Bi / Ag）**：方铅矿（`Galena`）焙烧还原得粗铅（`CrudeLead`） $\rightarrow$ 除铜浮渣 $\rightarrow$ **哈里斯碱熔软化**（拆出砷酸钠与锑酸钠，还原得 **砷 `Arsenic`** 与 **锑 `Antimony`**） $\rightarrow$ **帕克斯加锌脱银**（回收 **银 `Silver`**） $\rightarrow$ **克罗尔-贝特顿钙镁脱铋**（回收 **铋 `Bismuth`**）+ 高纯 **铅 (`Lead`)**。
- [ ] **辉钼矿焙烧烟气离子交换提铼 (`Rhenium`)**：辉钼矿（`Molybdenite`）氧化焙烧产出三氧化钼（`MolybdenumTrioxide`，氨溶氢还原得纯钼）+ **含铼烟气**；水洗吸收液经 M4 **阴离子交换树脂**吸附、氨水洗脱得高铼酸铵（`AmmoniumPerrhenate`），氢还原得超合金核心金属 **铼 (`Rhenium`)**。

---

## M8 · 彩蛋支线：化工音乐唱片工业 (Chemical Music Discs)

> **设计目标**：把前面做出来的乙烯基塑料、工程塑料、炭黑、有机染料与高纯贵金属直接变成游戏里能放进唱片机听歌的自定义唱片！

### 8.1 复古黑胶唱片线（Vinyl Record，对接乙烯/氯碱线）
- [ ] **混炼黑胶胶料 (`VinylRecordCompound`)**
  - [ ] `PVC 粉 (4)` + `PVAc 聚醋酸乙烯粉 (1)` + `炭黑粉 (1)` + `硬脂酸钙/锌热稳定剂` $\xrightarrow{\text{搅拌机}}$ **黑胶唱片混炼料**。
- [ ] **压制空白黑胶盘 (`BlankVinylRecord`)**
  - [ ] 黑胶混炼料 + 唱片模具 + 蒸汽 $\xrightarrow{\text{挤压机/热压机}}$ **空白黑胶唱片**（若加彩色有机染料则出**彩胶唱片**）。
- [ ] **母盘刻纹与电铸镍压模 (`RecordStamper`)**
  - [ ] 铝板 + 硝化纤维素漆 $\rightarrow$ **涂漆母盘 (Lacquer Disc)**。
  - [ ] 涂漆母盘 + 蓝宝石刻针 + 编程电路 `selectorTag(n)` $\rightarrow$ 刻好第 $n$ 首歌纹路的母盘 $\xrightarrow{+\text{硝酸银敏化}+\text{镍电镀}}$ **第 $n$ 首歌的镍压模**。
- [ ] **批量压盘量产**
  - [ ] 空白黑胶唱片 + 纸标签 + **第 $n$ 首歌的镍压模（`keptInputs` 不消耗！）** $\xrightarrow{\text{压机 } \texttt{press}}$ **成品音乐唱片 #n**！

### 8.2 现代激光刻录光盘线（CD-R，对接双酚 A / 芳烃线）
- [ ] **注塑透明聚碳酸酯盘基 (`BlankPolycarbonateDisc`)**
  - [ ] 光学级**聚碳酸酯 (`Polycarbonate`)** + 模具 $\xrightarrow{\text{挤压机}}$ **透明 PC 盘基**。
- [ ] **涂覆有机记录层与贵金属反射层 (`BlankCDR`)**
  - [ ] 邻苯二甲酸酐（邻二甲苯氧化产物）+ 尿素 + 铜盐 $\rightarrow$ **酞菁染料 (`PhthalocyanineDye`)**。
  - [ ] 透明 PC 盘基 + 酞菁染料 + **银箔 / 金箔**（反射层）+ 丙烯酸保护漆 $\xrightarrow{\text{淋涂/组装机}}$ **空白可刻录光盘 (Blank CD-R)**。
- [ ] **激光一步刻录**
  - [ ] 空白 CD-R + 透镜 + 编程电路 `selectorTag(n)` $\xrightarrow{\text{激光雕刻机 } \texttt{laserengraver}}$ **成品音乐光盘 #n**！

---

## M9 · 下游挂载与电子/结构材料消纳 (Downstream Integration & Electronics)

- [ ] **工业油品与线缆包覆（零新贴图先行）**：联苯氯化制多氯联苯（`PCB`）高压变压器油；`PAO` / 聚异丁烯调合高级润滑油；五大合成橡胶与 `PVC`/`PTFE` 挂载至各级线缆绝缘包覆。
- [ ] **电路板工业升级**：酚醛纸基板（MV） $\rightarrow$ 普通环氧 G10 板（HV） $\rightarrow$ 四溴阻燃环氧 FR-4 板（EV） $\rightarrow$ Kapton 柔性电路板 & ECN 芯片塑封料。
- [ ] **高纯晶圆与光刻胶楼梯**：西门子法多晶硅、高纯锗、砷化镓（`GaAs`）/ 磷化铟（`InP`）晶圆；重氮萘醌-酚醛 / PMMA / KrF 光刻胶与 5 级超纯水清洗刻蚀链。
- [ ] **电池与燃料电池膜电极**：`PVDF` 电极粘结剂；`Nafion` 质子交换膜 + `PTFE` 碳纤维气体扩散层 + `Viton` 密封圈组装燃料电池与全钒液流电池。
- [ ] **防弹护甲、宇航服与精密失蜡铸造**：用凯夫拉、诺梅克斯、UHMWPE、EVA 微型液冷管、镀铝 PET 膜、PC 面罩合成护甲；用氢化石油树脂 + 交联 PS 珠 + 石蜡做精密铸造蜡模，浇铸含铼单晶高温合金涡轮叶片。
