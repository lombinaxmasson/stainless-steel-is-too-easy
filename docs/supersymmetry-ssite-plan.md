# Stainless Steel Is Too Easy (`ssite`) — 开发计划与任务清单

> **模组定位**：基于 GT6CE Addon SDK 的硬核工业/化工扩展附属。  
> **核心梗**：在原版 GT6 里，不锈钢居然只要往坩埚里丢铁、镍、铬、锰就能化出来，然后立刻解锁蒸馏塔、深冷塔和全套大型化工机！本附属第一刀先砍掉“坩埚搓不锈钢”，把真实的火法/湿法冶金、水净化、石化单体分离、高分子合成与化工唱片一步步搭起来。
>
> **执行方式**：采用 `docs/card-plans/` 计划卡模式推进（见 [`docs/card-plans/总览.md`](card-plans/总览.md)），每张卡独立验收后再合入下一张。  
> **接口参考**：`../GregTech6CommunityEdition/GregTech6/docs/ADDON_API.zh-CN.md`

---

## 总体分期路线图

为了避免“摊子铺太大写到一半弃坑”，整个工程拆成 **6 个可独立交付的里程碑（Milestone）**。每个阶段做完都能直接进游戏跑通闭环：

1. **M0 · 脚手架与基础设施**：跑通 SDK 依赖、ServiceLoader、语言合并与测试运行。
2. **M1 · 招牌主线「不锈钢没那么容易」**：禁用原版坩埚不锈钢，实装专属 **AOD 转炉 (`ssite:aod_converter`)** 与**三级不锈钢体系**（高碳不锈钢粗钢 $\rightarrow$ 304 标准不锈钢三档吹炼 $\rightarrow$ 316L 超低碳钼不锈钢与耐酸管道），并用早期空气硬吹低收率路线破除深冷塔空分死锁。
3. **M2 · 公用工程：水净化与卤水链**：絮凝 $\rightarrow$ 污泥 $\rightarrow$ 酸碱中和 $\rightarrow$ 离子交换 $\rightarrow$ RO 反渗透 $\rightarrow$ UV 紫外超纯水；配套盐水/卤水提溴碘锂镁（需 316L 耐酸管线）。
4. **M3 · 纯石化中游：单体分离与内部自循环**：承接炼油与裂解，实装 **BTX 芳烃萃取**、**二甲苯三兄弟分离与异构化**、**C4 丁烯拆分**、**C5 馏分二聚拆解（异戊二烯 + 双环戊二烯）**。
5. **M4 · 纯石化下游：四大高分子家族（只做产出，暂不改下游）**：聚烯烃、乙烯基家族（PVC/PVAc/PVA/PVB/EVA）、五大合成橡胶与硫化体系、芳烃工程塑料与芳纶/碳纤维。
6. **M5 · 彩蛋支线：化工音乐唱片工业**：用自产的 PVC/PVAc/炭黑（黑胶线）或 聚碳酸酯/酞菁染料/银箔（CD-R 激光刻录线）批量压制/刻录自定义音乐唱片。

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
- [ ] **1 级 · 絮凝澄清水 (`ClarifiedWater`)**
  - [ ] 河水/原水/污水 + 明矾/硫酸铝（`Alum` / `VitriolOfClay`） $\xrightarrow{\text{化学浴槽 } \texttt{bath}}$ 澄清水 + **污泥 (`Sludge`)**（进干燥机/离心机回收粘土、生物质与痕量稀土）。
- [ ] **2 级 · 软化/脱矿水 (`DemineralizedWater`)**
  - [ ] 澄清水经纯碱软化与石英砂床过滤（`bath` + `QuartzSand` 催化保留），沉淀方解石/菱镁矿水垢并支持快速无垢蒸馏。
- [ ] **3 级 · 去离子水 (`DeionizedWater`)**
  - [ ] 通过**沸石分子筛 (`Zeolite`)**（未来与 M4 交联聚苯乙烯阴阳离子交换树脂联动）洗脱痕量金属离子，供湿法冶金与聚合反应使用。
- [ ] **4 级 · 反渗透纯水 (`FilteredWater`)**
  - [ ] 去离子水通过**醋酸纤维素 RO 膜 (`foil CelluloseAcetate`)** 在高压釜（`autoclave`）中高压反渗透过滤（膜组件循环使用）。
- [ ] **5 级 · 电子级超纯水 (`UltrapureWater`)**
  - [ ] 臭氧（`Ozone`）氧化脱除有机碳（TOC）+ 蓝宝石/钻石紫外透光灯窗（`mixer` + `lens` 催化保留）杀菌脱气，预留给未来的晶圆清洗与光刻线。

### 2.2 海水淡化与卤水（Brine）副产网
- [ ] **海水浓缩成苦卤**：海水/盐水经蒸馏塔（或蒸馏室）浓缩产出蒸馏水 + 浓缩卤水（`Brine`）+ 食盐 + 石膏（`Gypsum`）。
- [ ] **卤水除镁除钙**：生石灰消化制熟石灰（`CalciumHydroxide`），与苦卤和纯碱反应沉淀**氢氧化镁 (`MagnesiumHydroxide`)**（盐酸中和得 `MagnesiumChloride` 接通电解镁线）与方解石，得除杂卤水（`DecalcifiedBrine`）。
- [ ] **卤水吹溴与提锂**：除杂卤水酸化后通入氯气（$\text{Cl}_2$ 置换 $\text{Br}_2$）吹出**粗溴蒸汽 (`CrudeBromine`)** + **富锂母液 (`LithiumRichBrine`)**；粗溴精馏得**液溴 (`Bromine`)** 与**碘 (`Iodine`)**（做四溴双酚 A 阻燃环氧必需！）；富锂母液加纯碱沉出**碳酸锂 (`LithiumCarbonate`)**，再经盐酸转化与熔盐电解得金属锂并回收氯气。

---

## M3 · 纯石化中游：单体分离与内部自循环

> **设计目标**：接在炼油和蒸汽裂解后面，把混合馏分拆成高纯化学单体，并实现**石化内部副产物零浪费闭环**。

### 3.1 新增核心单体与中间流体清单
- [ ] **芳烃抽提系**：重整油 (`Reformate`)、裂解汽油 (`PyrolysisGasoline`)、糠醛 (`Furfural`) 或环丁砜 (`Sulfolane`) 萃取剂、BTEX 芳烃混合物 (`BTEX`)、抽余油 (`Raffinate`)。
- [ ] **BTX 与二甲苯三兄弟**：苯 (`Benzene`)、甲苯 (`Toluene`)、乙苯 (`Ethylbenzene`)、混合二甲苯 (`Xylene`)、**邻二甲苯 (`OrthoXylene`)**、**间二甲苯 (`MetaXylene`)**、**对二甲苯 (`ParaXylene`，流体+晶粉)**。
- [ ] **C4 烯烃系**：混合丁烯 (`Butene`)、**1-丁烯 (`OneButene`)**、**2-丁烯 (`TwoButene`)**、**异丁烯 (`Isobutylene`)**、**丁二烯 (`Butadiene`)**。
- [ ] **C5 馏分系**：C5 馏分 (`CFiveFraction`)、二聚 C5 馏分 (`DimerizedCFiveFraction`)、**异戊二烯 (`Isoprene`)**、**环戊二烯 (`Cyclopentadiene`)**、**双环戊二烯 (`Dicyclopentadiene` / DCPD)**、戊烷 (`Pentane`)、**降冰片烯 (`Norbornene` 粉)**。
- [ ] **基础衍生物单体**：氯乙烯 (`VinylChloride`)、醋酸乙烯 (`VinylAcetate`)、环氧乙烷 (`EthyleneOxide`)、乙二醇 (`EthyleneGlycol`)、环氧氯丙烷 (`Epichlorohydrin`)、异丙苯 (`Cumene`)、苯酚 (`Phenol`)、丙酮 (`Acetone`)、**双酚 A (`BisphenolA` 粉)**、丙烯腈 (`Acrylonitrile`)、丙烯酸 (`AcrylicAcid`)、甲基丙烯酸甲酯 (`MMA`)、**邻苯二甲酸酐 (`PhthalicAnhydride` 粉)**、**对苯二甲酸 (`TerephthalicAcid` 粉)**、**间苯二甲酸 (`IsophthalicAcid` 粉)**、己内酰胺 (`Caprolactam` 粉)、MDI。

### 3.2 核心工艺与内部副产自循环清单
- [ ] **BTX 溶剂抽提循环**：裂解汽油 / 重整油 + 糠醛 $\xrightarrow{\text{筛板塔/浴槽}}$ 芳烃萃取液 + **抽余油**（回裂解炉）+ **C5 馏分**；萃取液汽提回收糠醛，精馏得 **苯 + 甲苯 + 乙苯 + 混合二甲苯**。
- [ ] **二甲苯分离与异构化闭环（经典设计！）**
  - [ ] 混合二甲苯 $\xrightarrow{\text{蒸馏塔}}$ **邻二甲苯 (20%)** + 间/对二甲苯混液 (80%)。
  - [ ] 间/对二甲苯混液 $\xrightarrow{\text{结晶坩埚 } \texttt{crystallisationcrucible}}$ **对二甲苯晶体 (25%)** + **间二甲苯母液 (75%)**。
  - [ ] **间二甲苯** $\xrightarrow{\text{ZSM-5 分子筛催化异构化}}$ **100% 转回对二甲苯**（解决间二甲苯爆仓！）。
- [ ] **甲苯消纳闭环**
  - [ ] 路线 A（催化歧化）：甲苯 $\xrightarrow{\text{ZSM-5}}$ 苯 + 二甲苯混液。
  - [ ] 路线 B（加氢脱烷基）：甲苯 + 氢气 $\rightarrow$ 苯 + 甲烷 + 少量**联苯 (`Biphenyl`)**（联苯氯化做多氯联苯变压器油）。
- [ ] **C4 丁烯精馏与丁二烯**
  - [ ] 混合丁烯 $\xrightarrow{\text{蒸馏塔}}$ **异丁烯 (50%) + 1-丁烯 (30%) + 2-丁烯 (20%)**。
  - [ ] *非石油彩蛋路线*：乙醇 + 乙醛 $\xrightarrow{\text{钽催化剂}}$ **丁二烯 + 水**（Lebedev 法）。
- [ ] **C5 馏分热二聚巧妙分离（免深冷提纯异戊二烯）**
  - [ ] C5 馏分 $\xrightarrow{\text{加热器}}$ 二聚 C5（里面的环戊二烯自己聚合成高沸点的双环戊二烯 DCPD）。
  - [ ] 二聚 C5 $\xrightarrow{\text{蒸馏塔}}$ **异戊二烯 (36%)** + **戊烷 (38%)** + **双环戊二烯 DCPD (13%)**。
  - [ ] DCPD $\xrightarrow{\text{高温热解}}$ 2 份 **环戊二烯**（再与乙烯加成得**降冰片烯**）。
  - [ ] *合成异戊二烯路线*：异丁烯 + 甲醛 $\rightarrow$ 二甲基二噁烷 $\xrightarrow{\text{固体磷酸催化裂解}}$ **异戊二烯** + 甲醛循环。
- [ ] **异丙苯法制苯酚与双酚 A**
  - [ ] 苯 + 丙烯 $\rightarrow$ 异丙苯 $\xrightarrow{+\text{O}_2}$ 过氧化氢异丙苯 $\xrightarrow{\text{酸解}}$ **苯酚 + 丙酮**（两样刚好在阳离子交换树脂催化下缩合成 **双酚 A**！）。

---

## M4 · 纯石化下游：四大高分子与精细化学品（第一阶段只做产出）

> **策略**：按 `docs/supersymmetry-petrochem-uses.md` 表格注册最终材料与形态（粉、板、箔、环、纤维、管），把合成线修通；下游消耗配方留到 M6 再挂。

### 4.1 通用热塑性塑料
- [ ] **聚烯烃系**：PE（高压自由基聚合 vs 低压 Ziegler-Natta 催化）、**UHMWPE（超高分子量聚乙烯，Kaminsky 茂金属催化，含板与防弹纤维）**、PP（聚丙烯）。
- [ ] **乙烯基家族**：
  - [ ] 氯乙烯 $\rightarrow$ **PVC**（粉、板、箔、物品管）
  - [ ] 醋酸乙烯 $\rightarrow$ **PVAc（聚醋酸乙烯酯）** $\xrightarrow{+\text{乙醇醇解}}$ **PVA（聚乙烯醇）**（副产乙酸乙酯溶剂） $\xrightarrow{+\text{正丁醛}}$ **PVB（聚乙烯醇缩丁醛夹层玻璃膜）**
  - [ ] 乙烯 + 醋酸乙烯 $\rightarrow$ **EVA**（粉、微型/小型流体软管）
- [ ] **苯乙烯与丙烯酸系**：**PS（聚苯乙烯）**、**交联 PS 圆珠**（苯乙烯 + 二乙烯基苯）、**PMMA（有机玻璃）**。
- [ ] **双酚 A 与聚酯系**：**PC（聚碳酸酯）**、**PET（聚对苯二甲酸乙二醇酯，含镀铝 Mylar 膜）**。

### 4.2 热固性树脂
- [ ] **酚醛树脂 (`Phenolic`)**：苯酚 + 甲醛。
- [ ] **环氧树脂四兄弟**：
  - [ ] 双酚 A + 环氧氯丙烷 + 固化剂（双氰胺 / 邻苯二甲酸酐） $\rightarrow$ **普通环氧树脂 (`Epoxy`)** + 浸玻纤出 **强化环氧板（玻璃钢）**。
  - [ ] 四溴双酚 A（用上 M2 的溴！）+ 环氧氯丙烷 $\rightarrow$ **阻燃环氧 (`FlameRetardantEpoxy`)**。
  - [ ] 甲酚 + 甲醛 + 环氧氯丙烷 $\rightarrow$ **甲酚醛环氧 (`EpoxyCresolNovolacs`)**。
  - [ ] 丁二烯 + 丙烯醛 $\rightarrow$ **脂环族环氧液**。
- [ ] **聚氨酯 (`Polyurethane`)**：MDI + 乙二醇/多元醇 + 超临界 $\text{CO}_2$ 发泡（顺手加一条：PU 块泡黄染料 = 原版海绵 `minecraft:sponge`！）。
- [ ] **氢化石油树脂 (`HydrogenatedPetroleumResin`)**：DCPD + 苯 + 降冰片烯聚合后加氢（消化 C5 副产！）。

### 4.3 合成橡胶与硫化体系
- [ ] **五大生胶合成**：
  - [ ] 异戊二烯 $\rightarrow$ **聚异戊二烯 (`Polyisoprene`)** + 炭黑混炼
  - [ ] 丁二烯 + 苯乙烯 $\rightarrow$ **丁苯橡胶 (`SBR`)** + 炭黑混炼
  - [ ] 丁二烯 + 丙烯腈 $\rightarrow$ **丁腈橡胶 (`NBR`)**
  - [ ] 丁二烯氯化 $\rightarrow$ 氯丁二烯 $\rightarrow$ **氯丁橡胶 (`Neoprene`)**
  - [ ] 偏氟乙烯 + 六氟丙烯 $\rightarrow$ **氟橡胶 (`Viton`)**
- [ ] **统一硫化成型配方**：生胶粉 + 硫磺 + 催化剂（氧化锌 / 氧化镁，不消耗）+ 模具 $\xrightarrow{\text{热压机/挤压机}}$ 对应橡胶的锭、板、环（`ring`）、箔（`foil`）。

### 4.4 合成纤维与特种工程塑料（按喜好挑选实装）
- [ ] **合成纤维四强**：
  - [ ] 己内酰胺 $\rightarrow$ **尼龙 6 (`Polycaprolactam`)**
  - [ ] 对苯二甲酰氯 + 对苯二胺 $\rightarrow$ **凯夫拉 (`Kevlar`)**
  - [ ] 间苯二甲酰氯 + 间苯二胺 $\rightarrow$ **诺梅克斯 (`Nomex`)**
  - [ ] 丙烯腈 $\rightarrow$ **聚丙烯腈 (PAN) 纤维** $\xrightarrow{\text{预氧化 + 惰性气氛高温碳化}}$ **碳纤维 (`CarbonFiber`)**
- [ ] **氟塑料与特种工程塑料**：**PTFE（特氟龙）**、**PVDF**、**Nafion**、**聚砜 (`Polysulfone`)**、**PPS**、**PEEK**、**Kapton（聚酰亚胺）**。
- [ ] **湿法冶金萃取剂与功能油品（可选）**：**TBP（磷酸三丁酯）**、**P204**、**Aliquat 336**、**PAO 全合成润滑油**。

---

## M5 · 彩蛋支线：化工音乐唱片工业 (Chemical Music Discs)

> **设计目标**：把前面做出来的乙烯基塑料、工程塑料、炭黑、有机染料直接变成游戏里能放进唱片机听歌的自定义唱片！

### 5.1 复古黑胶唱片线（Vinyl Record，对接乙烯/氯碱线）
- [ ] **混炼黑胶胶料 (`VinylRecordCompound`)**
  - [ ] `PVC 粉 (4)` + `PVAc 聚醋酸乙烯粉 (1)` + `炭黑粉 (1)` + `硬脂酸钙/锌热稳定剂` $\xrightarrow{\text{搅拌机}}$ **黑胶唱片混炼料**。
- [ ] **压制空白黑胶盘 (`BlankVinylRecord`)**
  - [ ] 黑胶混炼料 + 唱片模具 + 蒸汽 $\xrightarrow{\text{挤压机/热压机}}$ **空白黑胶唱片**（若加彩色有机染料则出**彩胶唱片**）。
- [ ] **母盘刻纹与电铸镍压模 (`RecordStamper`)**
  - [ ] 铝板 + 硝化纤维素漆 $\rightarrow$ **涂漆母盘 (Lacquer Disc)**。
  - [ ] 涂漆母盘 + 蓝宝石刻针 + 编程电路 `selectorTag(n)` $\rightarrow$ 刻好第 $n$ 首歌纹路的母盘 $\xrightarrow{+\text{硝酸银敏化}+\text{镍电镀}}$ **第 $n$ 首歌的镍压模**。
- [ ] **批量压盘量产**
  - [ ] 空白黑胶唱片 + 纸标签 + **第 $n$ 首歌的镍压模（`keptInputs` 不消耗！）** $\xrightarrow{\text{压机 } \texttt{press}}$ **成品音乐唱片 #n**！

### 5.2 现代激光刻录光盘线（CD-R，对接双酚 A / 芳烃线）
- [ ] **注塑透明聚碳酸酯盘基 (`BlankPolycarbonateDisc`)**
  - [ ] 光学级**聚碳酸酯 (`Polycarbonate`)** + 模具 $\xrightarrow{\text{挤压机}}$ **透明 PC 盘基**。
- [ ] **涂覆有机记录层与贵金属反射层 (`BlankCDR`)**
  - [ ] 邻苯二甲酸酐（邻二甲苯氧化产物）+ 尿素 + 铜盐 $\rightarrow$ **酞菁染料 (`PhthalocyanineDye`)**。
  - [ ] 透明 PC 盘基 + 酞菁染料 + **银箔 / 金箔**（反射层）+ 丙烯酸保护漆 $\xrightarrow{\text{淋涂/组装机}}$ **空白可刻录光盘 (Blank CD-R)**。
- [ ] **激光一步刻录**
  - [ ] 空白 CD-R + 透镜 + 编程电路 `selectorTag(n)` $\xrightarrow{\text{激光雕刻机 } \texttt{laserengraver}}$ **成品音乐光盘 #n**！

---

## M6 · 未来下游挂载预留清单（等纯石化跑通后再勾选）

- [ ] **电路板工业升级**：酚醛纸基板（MV） $\rightarrow$ 普通环氧 G10 板（HV） $\rightarrow$ 四溴阻燃环氧 FR-4 板（EV） $\rightarrow$ Kapton 柔性电路板 & ECN 芯片塑封料。
- [ ] **稀土与核燃料溶剂萃取**：用 TBP 和 Aliquat 336 在混合澄清槽里分离镧系稀土、提纯核级铀钍。
- [ ] **防弹护甲与宇航服**：用凯夫拉、诺梅克斯、UHMWPE、EVA 微型液冷管、镀铝 PET 膜、PC 面罩合成护甲。
- [ ] **精密失蜡铸造**：用氢化石油树脂 + 交联 PS 珠 + 石蜡做精密铸造蜡模，浇铸单晶高温合金涡轮叶片。
