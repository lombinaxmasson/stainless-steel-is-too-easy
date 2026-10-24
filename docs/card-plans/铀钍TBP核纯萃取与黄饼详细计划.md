# 铀钍钒湿法前段与 TBP 核纯萃取详细计划

> 计划 slug：`m6/uranium-thorium-tbp`
> 状态：待实现（2026-10-24）。本文件位于 `docs/card-plans/`。前置是 [`稀土三矿开矿与直拆捷径移除`](稀土三矿开矿与直拆捷径移除详细计划.md)（提供 `UranylSulfateSolution` 与 `ThoriumConcentrate`）与 M4 [`萃取剂与润滑油基础油`](closed/萃取剂与润滑油基础油详细计划.md)（提供 `TBP`）以及 [`苯乙烯、离子交换树脂与 PMMA`](closed/苯乙烯离子交换树脂与PMMA详细计划.md)（提供 `AnionExchangeResin`）。
> 性质：M6 第四张卡，与第 2、3 张卡并行。把沥青铀矿/晶质铀矿的氧化酸浸液用 **强碱阴离子交换树脂** 富集成黄饼，再用 **`TBP` 萃取反萃** 得到核纯硝酸铀酰与硝酸钍，最终产出 `UO2 → UF4` 与 `ThO2 → Th`，接回 GT6CE 原有的 UF6 富集与钍燃料链。

```text
lane            = planned
slug            = m6/uranium-thorium-tbp
owned_paths     = common/src/main/java/com/lombinaxmasson/ssite/material/NuclearMaterials.java
                  common/src/main/java/com/lombinaxmasson/ssite/chemistry/NuclearRecipes.java
                  common/src/main/resources/assets/gt6ce/lang/en_us.json
                  common/src/main/resources/assets/gt6ce/lang/zh_cn.json
depends_on      = m6/ore-leaching, m4/extractants-lubricants, m4/styrene-acrylic-ion-exchange
close_target    = 9 种核材料注册；黄饼、TBP 铀钍萃取、UO2/UF4 与 ThO2/Th 全线跑通，TBP 95% 循环
```

---

## 0. 它解决什么

1. **堵住铀矿的最后两条捷径**：上一张卡已经移除了 `Pitchblende` 离心直拆与 `Uraninite` / `Pitchblende` 的坩埚直熔；本卡给出唯一剩下的正路——氧化酸浸 $\rightarrow$ 树脂吸附 $\rightarrow$ 黄饼 $\rightarrow$ TBP 核纯萃取。
2. **让 M4 的强碱阴离子交换树脂与 TBP 上岗**：
   - 铀在硫酸浸出液里以 `[UO2(SO4)3]4-` 阴离子存在，正好由**强碱性阴离子交换树脂**吸附富集，氨水洗脱得到重铀酸铵（黄饼）；
   - 黄饼与独居石钍饼分别溶于硝酸，用 **`TBP`（磷酸三丁酯）** 萃取，把铀/钍与裂变产物、稀土、铁等杂质分开。
3. **接回 GT6CE 已有的核链**：本卡只做到 `UF4` 与 `ThO2/Th`；GT6CE 已有 `UF4 + F -> UF6`、`UF6` 离心富集与 `Ca + UF4 -> U-238`，本卡不重写同位素分离。
4. **关于标题里的「钒」**：总览第 4 张卡的标题沿用了「铀钍钒」，但正文只要求铀钍前段；钒已经在 M5 [`金红石/钛铁矿氯化、除钒精馏与 Kroll 海绵钛闭环`](closed/钛钒氯化与海绵钛详细计划.md) 里从钛渣闭环。本卡**不重复做钒**，标题保留但不扩链（见第 5 节）。

---

## 1. 新增材料（9 种，追加至 `NuclearMaterials`）

| 名字 | 中文 / 英文 | 化学式 | 熔点 K | 沸点 K | 密度 | 颜色 | 形态 |
|---|---|---|---|---|---|---|---|
| `Yellowcake` | 黄饼（重铀酸铵）/ Yellowcake (Ammonium Diuranate) | `(NH4)2U2O7` | 600 | 1500 | 4.80 | `0xE8C828` | `dust()` |
| `UranylNitrateSolution` | 硝酸铀酰粗液 / Crude Uranyl Nitrate Solution | `UO2(NO3)2(aq)` | 290 | 400 | 1.55 | `0xD8E858` | `liquid()` |
| `PureUranylNitrate` | 核纯硝酸铀酰 / Nuclear-Grade Uranyl Nitrate | `UO2(NO3)2(aq)` | 290 | 400 | 1.55 | `0xE8F878` | `liquid()` |
| `TbpUraniumExtract` | 负载铀 TBP 有机相 / Uranium-Loaded TBP Extract | `UO2(NO3)2·TBP` | 210 | 400 | 0.99 | `0xC8C070` | `liquid()` |
| `UraniumDioxide` | 二氧化铀 / Uranium Dioxide | `UO2` | 3120 | 4400 | 10.97 | `0x303830` | `dust()` |
| `ThoriumNitrateSolution` | 硝酸钍粗液 / Crude Thorium Nitrate Solution | `Th(NO3)4(aq)` | 290 | 400 | 1.60 | `0xD0D0B8` | `liquid()` |
| `PureThoriumNitrate` | 核纯硝酸钍 / Nuclear-Grade Thorium Nitrate | `Th(NO3)4(aq)` | 290 | 400 | 1.60 | `0xE0E0C8` | `liquid()` |
| `TbpThoriumExtract` | 负载钍 TBP 有机相 / Thorium-Loaded TBP Extract | `Th(NO3)4·TBP` | 210 | 400 | 1.00 | `0xB8B088` | `liquid()` |
| `ThoriumDioxide` | 二氧化钍 / Thorium Dioxide | `ThO2` | 3390 | 4400 | 10.00 | `0x585850` | `dust()` |

- GT6CE 已有：`Uraninite`、`Pitchblende`、`UraniumTetrafluoride`（`UF4`）、`UraniumHexafluoride`（`UF6`）、`U_238`、`Thorium`、`NitricAcid`、`SulfuricAcid`、`HydrogenPeroxide`、`Ammonia`、`TributylPhosphate`（`TBP`）、`AnionExchangeResin`、`Calcium`、`Quicklime`。
- 上一张卡已注册：`UranylSulfateSolution`、`ThoriumConcentrate`。
- `Yellowcake` 用 `dust()`；铀/钍的硝酸盐与 TBP 负载相用 `liquid()`；`UO2` / `ThO2` 用 `dust()`，不声明 `metal()`（核材料以粉/芯块形式使用，不直接压锭）。

---

## 2. 配方设计（`NuclearRecipes.uraniumThoriumTbp()`）

### 2.1 铀矿氧化酸浸与树脂提黄饼

1. **沥青铀矿氧化酸浸（`gt.recipe.autoclave`）**：
   `2 × dust Pitchblende + GenLiquid_SulfuricAcid (2000) + GenGas_HydrogenPeroxide (1000)`
   $\rightarrow$ `GenLiquid_UranylSulfateSolution (2000) + 1 × dust ThoriumConcentrate + 1 × dust Lead`；240 ticks，64 EU/t。
   - 说明：四价铀被过氧化氢氧化成 `UO22+` 进入硫酸液；铅以硫酸铅渣形式留下（折成 `Lead` 粉），钍并入 `ThoriumConcentrate` 走钍线。
2. **晶质铀矿氧化酸浸（`gt.recipe.autoclave`）**：
   `2 × dust Uraninite + GenLiquid_SulfuricAcid (2000) + GenGas_HydrogenPeroxide (1000)`
   $\rightarrow$ `GenLiquid_UranylSulfateSolution (2500)`；200 ticks，64 EU/t。
   - 说明：晶质铀矿不含钍铅，单独一条，产出略高。
3. **阴离子交换树脂吸附 + 氨水洗脱（`gt.recipe.mixer`）**：
   `GenLiquid_UranylSulfateSolution (4000) + 1 × dust AnionExchangeResin（不消耗）+ GenGas_Ammonia (2000)`
   $\rightarrow$ `1 × dust Yellowcake + Dirty_Water (3000)`；240 ticks，48 EU/t。
   - 说明：树脂 `keep(0)` 不消耗，代表吸附—洗脱—再生循环；氨水把铀酰络阴离子洗脱并以重铀酸铵沉淀，硫酸根进废水。

### 2.2 TBP 核纯萃取：铀

1. **黄饼硝酸溶解（`gt.recipe.mixer`）**：
   `1 × dust Yellowcake + GenLiquid_NitricAcid (3000)` $\rightarrow$ `GenLiquid_UranylNitrateSolution (2000) + Dirty_Water (1000)`；160 ticks，48 EU/t。
2. **TBP 萃取铀（`gt.recipe.mixer`）**：
   `GenLiquid_UranylNitrateSolution (2000) + GenLiquid_TributylPhosphate (1000)`
   $\rightarrow$ `GenLiquid_TbpUraniumExtract (1000) + Dirty_Water (2000)`；160 ticks，64 EU/t。
   - 说明：TBP 在硝酸介质里选择性萃取铀酰，裂变产物与铁/稀土留在水相。
3. **稀硝酸反萃并再生 TBP（`gt.recipe.mixer`）**：
   `GenLiquid_TbpUraniumExtract (1000) + DistW (1000)`
   $\rightarrow$ `GenLiquid_PureUranylNitrate (2000) + GenLiquid_TributylPhosphate (950)`；160 ticks，48 EU/t。
   - 说明：回收 **95% 的 TBP**；核纯硝酸铀酰进入煅烧。
4. **还原煅烧成 `UO2`（`gt.recipe.calciner`）**：
   `GenLiquid_PureUranylNitrate (2000) + Hydrogen (1000)`
   $\rightarrow$ `1 × dust UraniumDioxide + GenGas_NitrogenDioxide (2000) + Steam (1000) + Oxygen (500)`；200 ticks，64 EU/t。
   - 说明：`2 UO2(NO3)2 + 2 H2 -> 2 UO2 + 4 NO2 + 2 H2O + O2`，按一批（1 U）折半写作 `UO2(NO3)2 + H2 -> UO2 + 2 NO2 + H2O + 0.5 O2`。
5. **氢氟酸转化 `UF4`（`gt.recipe.calciner`）**：
   `1 × dust UraniumDioxide + GenGas_HydrogenFluoride (4000)`
   $\rightarrow$ `1 × dust UraniumTetrafluoride + DistW (2000)`；160 ticks，64 EU/t。
   - 说明：`UO2 + 4 HF -> UF4 + 2 H2O`。`UF4` 进入 GT6CE 已有的 `UF4 + F2 -> UF6` 与离心富集链；本卡不重写同位素分离。

### 2.3 TBP 核纯萃取：钍

1. **钍饼硝酸溶解（`gt.recipe.mixer`）**：
   `1 × dust ThoriumConcentrate + GenLiquid_NitricAcid (3000)` $\rightarrow$ `GenLiquid_ThoriumNitrateSolution (2000) + Dirty_Water (1000)`；160 ticks，48 EU/t。
2. **TBP 萃取钍（`gt.recipe.mixer`）**：
   `GenLiquid_ThoriumNitrateSolution (2000) + GenLiquid_TributylPhosphate (1000)`
   $\rightarrow$ `GenLiquid_TbpThoriumExtract (1000) + Dirty_Water (2000)`；160 ticks，64 EU/t。
3. **稀硝酸反萃并再生 TBP（`gt.recipe.mixer`）**：
   `GenLiquid_TbpThoriumExtract (1000) + DistW (1000)`
   $\rightarrow$ `GenLiquid_PureThoriumNitrate (2000) + GenLiquid_TributylPhosphate (950)`；160 ticks，48 EU/t。
4. **煅烧成 `ThO2`（`gt.recipe.calciner`）**：
   `GenLiquid_PureThoriumNitrate (2000)`
   $\rightarrow$ `1 × dust ThoriumDioxide + GenGas_NitrogenDioxide (4000) + Oxygen (2000)`；200 ticks，64 EU/t。
   - 说明：`Th(NO3)4 -> ThO2 + 4 NO2 + O2`；`ThO2` 是钍燃料与钨钍电极的原料。
5. **钙热还原成金属钍（`gt.recipe.calciner`）**：
   `1 × dust ThoriumDioxide + 2 × dust Calcium` $\rightarrow$ `1 × dust Thorium + 2 × dust Quicklime`；240 ticks，96 EU/t。
   - 说明：`ThO2 + 2 Ca -> Th + 2 CaO`。钍金属进 GT6CE 已有的钍燃料/合金链。

---

## 3. 数量与循环核对

| 项 | 投入 | 产出 / 回收 | 说明 |
|---|---|---|---|
| 沥青铀矿 | 2 矿 + 2000 硫酸 + 1000 过氧化氢 | 2000 铀酰液 + 钍饼 + 铅 | 铀钍铅三路都接回主线 |
| 晶质铀矿 | 2 矿 + 2000 硫酸 + 1000 过氧化氢 | 2500 铀酰液 | 无钍铅副产 |
| 树脂吸附 | 4000 铀酰液 + 2000 氨 | 1 黄饼 | 树脂不消耗 |
| TBP 提铀 | 1000 TBP | 回收 950 TBP | 95% |
| TBP 提钍 | 1000 TBP | 回收 950 TBP | 95% |

- 所有 `mixer` 行最多 3 路流体输入、2 路流体输出；`autoclave` 最多 2 物品入 / 2 流体入 / 1 流体出；`calciner` 最多 3/3。
- `UF4` 之后不再新增配方，直接使用 GT6CE 的 `UF4 -> UF6 -> 离心富集` 与 `Ca + UF4 -> U_238`。
- 钍线只做到 `Th`，不新增钍燃料棒配方；GT6CE 已有钍燃料链继续消化。

---

## 4. 偏差与说明

1. **「铀钍钒」里的钒不做**：钒已在 M5 钛钒氯化卡闭环（`VOCl3 -> V2O5 -> V`）；本卡的铀浸出液不副产钒，避免与 M5 的钒线重复。总览标题保留，正文按「铀钍前段」执行。
2. **黄饼用树脂 + 氨水一步折成**：工业上是「树脂吸附 → 氨水洗脱 → 重铀酸铵沉淀」三步；本卡折成一条 `mixer` 行，树脂不消耗，氨水与硫酸根进废水。
3. **铅渣折成 `Lead` 粉**：沥青铀矿的硫酸铅渣没有对应 GT6CE 材料（没有 `LeadSulfate`），按 M5 的副产折法折成 `Lead`；若后续 M7 做铅线，可改为 `Anglesite` 或 `LeadSulfate`。
4. **`UO2` 与 `ThO2` 是新材料**：GT6CE 只有 `Uraninite`（`UO2` 矿）与 `UF4/UF6`，没有可用的 `UO2` / `ThO2` 粉；本卡新建，用于核燃料芯块与钍金属还原。
5. **不重写同位素分离**：`UF6` 离心富集、`U-235/U-238` 分离、钍燃料棒全部沿用 GT6CE 内置配方。
6. **零新增机器**：全部 `autoclave` / `mixer` / `calciner`，没有新机器贴图。

---

## 5. 明确不做

- 不做钒的二次回收（M5 已闭环）。
- 不做铀钚循环、MOX、快堆燃料。
- 不做 `UF6` 富集与燃料棒组装（GT6CE 已有）。
- 不做钍基熔盐堆或钍增殖（M9/远期）。
- 不做铅渣的精炼（M7 的铅线再做）。

---

## 6. 验收标准

- [ ] 9 种新材料注册成功，中英文译名齐全。
- [ ] `Pitchblende` / `Uraninite` 氧化酸浸都能产出 `UranylSulfateSolution`；沥青铀矿额外产 `ThoriumConcentrate` 与铅渣。
- [ ] 阴离子交换树脂吸附 + 氨水洗脱产出 `Yellowcake`，树脂不消耗、可循环。
- [ ] 黄饼经硝酸溶解 + TBP 萃取/反萃得到核纯硝酸铀酰，TBP 回收 95%。
- [ ] 核纯硝酸铀酰经还原煅烧得 `UO2`，再经 `HF` 得 `UF4`；`UF4` 可在 GT6CE 原有链里继续到 `UF6`。
- [ ] 独居石钍饼经硝酸溶解 + TBP 萃取/反萃得到核纯硝酸钍，TBP 回收 95%；煅烧得 `ThO2`，钙热还原得 `Th`。
- [ ] 所有配方注册成功，`REJECTED` 为空；双加载器 `build` 通过，NeoForge 服务端启动至 `Done` 无异常。
- [ ] 无新增机器方块与手工贴图。
