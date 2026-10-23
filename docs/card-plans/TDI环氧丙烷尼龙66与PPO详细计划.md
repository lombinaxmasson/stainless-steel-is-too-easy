# M4 补遗：TDI、环氧丙烷、己二酸 / 尼龙 66、PPO / PEI 与双氰胺详细计划

> 计划 slug：`m4/deferred-fill`
> 状态：实现完成，待验收（2026-10-23）。前置是 [`聚氨酯与石油树脂`](closed/聚氨酯与石油树脂详细计划.md)（MDI、聚醚多元醇）、[`芳烃氧化、光气、PET 与聚碳酸酯`](closed/芳烃氧化光气聚酯与聚碳酸酯详细计划.md)（光气）、[`尼龙 6 与碳纤维`](closed/尼龙6与碳纤维详细计划.md)（环己酮）、[`芳纶`](closed/芳纶详细计划.md)（间苯二胺）、[`酚醛与环氧树脂`](closed/酚醛与环氧树脂详细计划.md)（环氧树脂与固化配方）。
> 性质：M4 收尾卡。把路线图里「延后」的六项内容一次补齐，全部沿用 M4 已建立的 `PolymerMaterials` / `PolymerRecipes` 骨架，不新增机器。

```text
lane            = open
slug            = m4/deferred-fill
owned_paths     = PolymerMaterials.java、PolymerRecipes.java、gt6ce 中英文语言文件
depends_on      = m4/polyurethane-resin、m4/aromatic-oxidation-polyester-pc、m4/nylon-carbon-fiber、m4/aramids、m4/phenolic-epoxy
close_target    = 15 种材料注册；环氧丙烷/丙二醇、己二酸→尼龙 66、TDI、PPO、PEI、双氰胺全部可产并可成型
```

## 0. 它解决什么

| 补遗项 | 之前为什么延后 | 本卡怎么补 |
|---|---|---|
| **TDI** | 聚氨酯卡只做了 MDI，TDI 需要甲苯硝化与 TDA，另起一套硝化线 | 甲苯混酸硝化 → DNT → 加氢 TDA → 光气化 TDI；TDI + 聚醚多元醇出软泡型聚氨酯，用电路和 MDI 两条路线区分 |
| **环氧丙烷** | 聚醚多元醇只用环氧乙烷，PO 需要丙烯氧化上游 | HPPO 法：丙烯 + 过氧化氢在 ZSM-5 上直接氧化；顺带做丙二醇和 PO 型聚醚多元醇 |
| **己二酸 / 尼龙 66** | 尼龙 6 卡不需要己二酸，单独做原料链太长 | 环己酮硝酸氧化得己二酸；己二酸氨化脱水得己二腈，加氢得己二胺，和己二酸缩聚得尼龙 66 |
| **PPO（聚苯醚）** | 特种工程塑料卡明确不做，缺 2,6-二甲酚 | 苯酚 + 甲醇在 ZSM-5 上甲基化得 2,6-二甲酚，铜催化氧化偶联得 PPO |
| **PEI（聚醚酰亚胺）** | 同上，缺双酚 A 二酐 | 双酚 A + 苯酐折一步得 BPADA，再和已有的间苯二胺亚胺化得 PEI |
| **双氰胺** | 环氧固化一律用苯酐 | 石灰氮法：生石灰 + 焦炭 + 氮气得氰氨化钙，水解歧化得双氰胺；作为环氧的第二条固化路线 |

## 1. 新增材料（15 种）

| 名字 | 中文 / 英文 | 化学式 | 熔点 K | 沸点 K | 密度 | 颜色 | 形态 |
|---|---|---|---|---|---|---|---|
| `PropyleneOxide` | 环氧丙烷 / Propylene Oxide | `C3H6O` | 161 | 307 | 0.83 | `0xE8E8D0` | `liquid()` |
| `PropyleneGlycol` | 丙二醇 / Propylene Glycol | `C3H8O2` | 213 | 461 | 1.04 | `0xE0E8E0` | `liquid()` |
| `AdipicAcid` | 己二酸 / Adipic Acid | `C6H10O4` | 425 | 610 | 1.36 | `0xF8F8F0` | `dust()` |
| `Adiponitrile` | 己二腈 / Adiponitrile | `C6H8N2` | 275 | 568 | 0.95 | `0xE8E0D0` | `liquid()` |
| `Hexamethylenediamine` | 己二胺 / Hexamethylenediamine | `C6H16N2` | 315 | 477 | 0.84 | `0xE0D8C8` | `dust()` |
| `PolyhexamethyleneAdipamide` | 尼龙 66 / Nylon 66 | `(C12H22N2O2)n` | 538 | — | 1.14 | `0xF0F0E8` | `dust().polymer().wires()` |
| `Dinitrotoluene` | 二硝基甲苯 / Dinitrotoluene | `C7H6N2O4` | 344 | 573 | 1.52 | `0xE8D878` | `dust()` |
| `Toluenediamine` | 甲苯二胺 / Toluenediamine | `C7H10N2` | 337 | 553 | 1.05 | `0xD8C8A8` | `dust()` |
| `TolueneDiisocyanate` | TDI / Toluene Diisocyanate | `C9H6N2O2` | 294 | 524 | 1.21 | `0xC8A060` | `liquid()` |
| `Dimethylphenol` | 2,6-二甲酚 / 2,6-Xylenol | `C8H10O` | 318 | 476 | 1.01 | `0xE8D8B0` | `dust()` |
| `PolyphenyleneOxide` | 聚苯醚 / PPO | `(C8H8O)n` | 540 | — | 1.06 | `0xE8E0C0` | `dust().polymer()` |
| `BisphenolADianhydride` | 双酚 A 二酐 / BPADA | `C31H24O7` | 460 | — | 1.30 | `0xF8F0E0` | `dust()` |
| `Polyetherimide` | 聚醚酰亚胺 / PEI | `(C37H24N2O6)n` | 490 | — | 1.27 | `0xD8B060` | `dust().polymer()` |
| `CalciumCyanamide` | 氰氨化钙 / Calcium Cyanamide | `CaCN2` | 1500 | — | 2.29 | `0x8C8C80` | `dust()` |
| `Dicyandiamide` | 双氰胺 / Dicyandiamide | `C2H4N4` | 482 | — | 1.40 | `0xF0F0E8` | `dust()` |

- 实现前查 GT6CE 是否已有 `AdipicAcid` / `TolueneDiisocyanate` / `Nylon` 类材料；若有就复用并从本表删掉。
- 三种新聚合物加进 `PolymerMaterials.buildForms()` 的 `polymers` 数组；`PolyhexamethyleneAdipamide` 同时加进 `wires` 数组（尼龙 66 可纺丝）。

## 2. 配方（`PolymerRecipes.deferredFill()`）

在 `PolymerRecipes.add()` 末尾调用 `deferredFill()`。

### 2.1 环氧丙烷与丙二醇

- **HPPO 直接氧化**（`gt.recipe.mixer`）：`1 × dust ZSM5`（不消耗，代表钛硅分子筛）+ `Propylene (1000)` + `GenGas_HydrogenPeroxide (1000)` $\rightarrow$ `GenLiquid_PropyleneOxide (1000)` + `DistW (1000)`；48 EU/t，160 ticks
  - 不做氯醇法和共氧化法，副产只有水。
- **水合**（`gt.recipe.mixer`）：`GenLiquid_PropyleneOxide (1000)` + `DistW (1000)` $\rightarrow$ `GenLiquid_PropyleneGlycol (1000)`；32 EU/t，100 ticks
- **PO 型聚醚多元醇**（`gt.recipe.mixer`）：`circuit(2)` + `1 × dustTiny PotassiumHydroxide` + `GenLiquid_Glycerol (1000)` + `GenLiquid_PropyleneOxide (3000)` $\rightarrow$ `GenLiquid_PolyetherPolyol (1000)`；48 EU/t，200 ticks
  - 与卡 7 的环氧乙烷路线靠电路 2 区分（卡 7 那条不带电路，碱按普通输入消耗，本行照抄）。

### 2.2 己二酸与尼龙 66

- **环己酮硝酸氧化**（`gt.recipe.mixer`）：`1 × dust VanadiumPentoxide`（不消耗）+ `GenLiquid_Cyclohexanone (1000)` + `GenLiquid_NitricAcid (2000)` $\rightarrow$ `1 × dust AdipicAcid` + `GenGas_NitrogenMonoxide (1000)` + `DistW (1000)`
  - 实际放出一氧化二氮，统一按一氧化氮计，可回硝酸线。
  - 96 EU/t，240 ticks。
- **氨化脱水**（`gt.recipe.calciner`）：`1 × dust AdipicAcid` + `GenGas_Ammonia (2000)` $\rightarrow$ `GenLiquid_Adiponitrile (1000)` + `DistW (4000)`
  - 二酰胺中间体不注册，两步折一步。
  - 96 EU/t，240 ticks。
- **加氢**（`gt.recipe.mixer`）：`1 × dust Nickel`（不消耗）+ `GenLiquid_Adiponitrile (1000)` + `Hydrogen (4000)` $\rightarrow$ `1 × dust Hexamethylenediamine`；64 EU/t，200 ticks
- **成盐缩聚**（`gt.recipe.mixer`）：`1 × dust AdipicAcid` + `1 × dust Hexamethylenediamine` $\rightarrow$ `1 × dust PolyhexamethyleneAdipamide` + `DistW (2000)`
  - 尼龙 66 盐中间体不注册，缩聚水按蒸馏水计。
  - 96 EU/t，240 ticks。
- **成型**：走 `polymerForming("PolyhexamethyleneAdipamide", true)`。

### 2.3 TDI

- **混酸硝化**（`gt.recipe.mixer`）：`GenLiquid_Toluene (1000)` + `GenLiquid_NitricAcid (2000)` + `GenLiquid_SulfuricAcid (100)` $\rightarrow$ `1 × dust Dinitrotoluene` + `DistW (2000)`；64 EU/t，200 ticks
  - 2,4- 与 2,6- 异构体不拆分，统一当 TDI 原料。
- **加氢**（`gt.recipe.mixer`）：`1 × dust Nickel`（不消耗）+ `1 × dust Dinitrotoluene` + `Hydrogen (6000)` $\rightarrow$ `1 × dust Toluenediamine` + `DistW (4000)`；64 EU/t，200 ticks
- **光气化**（`gt.recipe.mixer`）：`1 × dust Toluenediamine` + `GenGas_Phosgene (2000)` $\rightarrow$ `GenLiquid_TolueneDiisocyanate (1000)` + `GenGas_HydrochloricAcid (4000)`；64 EU/t，200 ticks
  - 氯化氢可回卡 1 氧氯化，和 MDI 光气化一致。
- **TDI 型聚氨酯**（`gt.recipe.mixer`）：`circuit(3)` + `GenLiquid_TolueneDiisocyanate (1000)` + `GenLiquid_PolyetherPolyol (1000)` $\rightarrow$ `2 × dust Polyurethane`；48 EU/t，200 ticks
  - 和卡 7 的 MDI 两条路线（电路 1、2）同机器同产物，靠电路 3 区分。
  - TDI 软泡和 MDI 硬泡的差别不在本卡体现，泡沫形态留给 M6。

### 2.4 PPO

- **苯酚甲基化**（`gt.recipe.mixer`）：`1 × dust ZSM5`（不消耗）+ `GenLiquid_Phenol (1000)` + `GenLiquid_Methanol (2000)` $\rightarrow$ `1 × dust Dimethylphenol` + `DistW (2000)`；64 EU/t，240 ticks
  - 与卡 12 的均四甲苯（二甲苯 + 甲醇 + ZSM-5）输入不同，不串配方。
- **氧化偶联**（`gt.recipe.mixer`）：`1 × dust Copper`（不消耗，代表铜-胺催化剂）+ `1 × dust Dimethylphenol` + `Oxygen (1000)` $\rightarrow$ `1 × dust PolyphenyleneOxide` + `DistW (1000)`；64 EU/t，240 ticks
- **成型**：走 `polymerForming("PolyphenyleneOxide", false)`。

### 2.5 PEI

- **BPADA**（`gt.recipe.mixer`）：`1 × dust BisphenolA` + `2 × dust PhthalicAnhydride` $\rightarrow$ `1 × dust BisphenolADianhydride` + `DistW (2000)`
  - 工业上的硝基苯酐 / 氯苯酐路线折成一步；硝基、氯代中间体不注册。
  - 96 EU/t，300 ticks。
- **亚胺化**（`gt.recipe.mixer`）：`1 × dust BisphenolADianhydride` + `1 × dust MetaPhenylenediamine` $\rightarrow$ `1 × dust Polyetherimide` + `DistW (2000)`
  - 聚酰胺酸中间体不注册，和 Kapton 卡的处理一致。
  - 128 EU/t，400 ticks。
- **成型**：走 `polymerForming("Polyetherimide", false)`。

### 2.6 双氰胺

- **氰氨化钙**（`gt.recipe.calciner`）：`1 × dust Quicklime` + `3 × dust CoalCoke` + `Nitrogen (1000)` $\rightarrow$ `1 × dust CalciumCyanamide` + `GenGas_CarbonMonoxide (1000)`；96 EU/t，300 ticks
- **水解歧化**（`gt.recipe.calciner`）：`2 × dust CalciumCyanamide` + `DistW (2000)` + `CarbonDioxide (2000)` $\rightarrow$ `1 × dust Dicyandiamide` + `2 × dust Calcite`
  - 搅拌机只有一个物品输出槽，两个物品产出改用煅烧炉。
  - 氰胺中间体不注册，水解、二聚折一步。
  - 64 EU/t，200 ticks。
- **环氧固化第二路线**（`gt.recipe.mixer`）：`1 × dust Dicyandiamide` + `GenLiquid_EpoxyResin (1000)` $\rightarrow$ `1 × dust Epoxy`；120 EU/t，200 ticks
  - 与卡 6 的苯酐固化路线并存；玻纤板、阻燃板继续用苯酐，不改已有配方。
  - 双氰胺是潜伏型固化剂，不额外消耗促进剂。

## 3. 明确不做

- 不做环氧丙烷的氯醇法、乙苯共氧化法、异丙苯共氧化法
- 不做己二酸的其他路线（环己烷一步氧化、丁二烯羰基化）
- 不做己二胺的丙烯腈电解二聚路线
- 不做 TDI 的 2,4 / 2,6 异构体分离与牌号区分
- 不做 PPO 的苯乙烯改性（mPPO）、PPO/PS 合金、PPO/PA 合金
- 不做 PEI 的氯代苯酐路线与 Ultem 牌号体系
- 不做双氰胺的肥料、阻燃用途，也不做三聚氰胺等三嗪类
- 不做 TDI/MDI 之外的 HDI、IPDI 等脂肪族异氰酸酯
- 不做任何泡沫、胶黏剂、涂料等成品，留给 M6

## 4. 验收

- [ ] 15 种新材料可查到，中英文译名齐全；`AdipicAcid`、`TolueneDiisocyanate` 没有和 GT6CE 自带材料撞名
- [ ] 丙烯 → 环氧丙烷 → 丙二醇、PO 型聚醚多元醇全部可产
- [ ] 环己酮 → 己二酸 → 己二腈 → 己二胺 → 尼龙 66 全线可产；尼龙 66 能压板、轧箔、拉细线
- [ ] 甲苯 → DNT → TDA → TDI → TDI 型聚氨酯可产，电路 3 与 MDI 两条路线不串
- [ ] 苯酚 → 2,6-二甲酚 → PPO 可产；PPO 能压板、轧箔
- [ ] 双酚 A + 苯酐 → BPADA → PEI 可产；PEI 能压板、轧箔
- [ ] 双氰胺能固化环氧，卡 6 的苯酐固化路线仍然可用
- [ ] 本卡 ZSM-5 配方（环氧丙烷、2,6-二甲酚）与卡 12 的对苯二酚、均四甲苯不串
- [ ] 本卡硝化配方（DNT）与卡 7 硝基苯、卡 10 对硝基氯苯不串
- [ ] `GenLiquid_NitricAcid`、`GenGas_Phosgene`、`CarbonDioxide`、`Calcite`、`CoalCoke`、`PotassiumHydroxide` 的取名已在游戏内确认
- [x] 本卡全部配方注册成功，`REJECTED` 为空
- [x] 双加载器编译通过，服务端启动至 `Done` 无异常
