# 苯乙烯、聚苯乙烯、离子交换树脂与有机玻璃详细计划

> 计划 slug：`m4/styrene-acrylic-ion-exchange`
> 状态：待实现。前置是 [`C3/C4 平台化学品`](C3平台化学品与Prins异戊二烯详细计划.md)（需要 MMA、甲醇、甲醛）。
> 性质：M4 第 4 张卡，同属第一批。乙苯脱氢出苯乙烯，做聚苯乙烯和离子交换树脂，再反哺 M2 去离子水和 M3 双酚 A，各给一条第二路线。

```text
lane            = open
slug            = m4/styrene-acrylic-ion-exchange
owned_paths     = PolymerMaterials.java、PolymerRecipes.java、gt6ce 中英文语言文件
depends_on      = m4/c3-platform
close_target    = 9 种材料注册；苯乙烯、二乙烯苯、PS、交联 PS、阳/阴离子交换树脂、PMMA 可产；树脂法去离子水与树脂法双酚 A 可用
```

## 0. 它解决什么

M3 的乙苯（BTEX 蒸馏 + ZSM-5 烷基化）到现在都没有下游。本卡：

1. 乙苯脱氢出**苯乙烯**，均聚出**聚苯乙烯**；
2. 加**二乙烯苯**做交联，悬浮聚合出**交联聚苯乙烯珠**；
3. 珠子磺化得**阳离子交换树脂**，氯甲基化再胺化得**阴离子交换树脂**；
4. 两种树脂**回头接入前面的里程碑**：
   - M2 去离子水：混床离子交换（损耗比沸石法小：4000 → 3950，沸石法是 4000 → 3800）；
   - M3 双酚 A：磺酸树脂催化，不再消耗氯化氢；
5. MMA 聚合出**有机玻璃（PMMA）**。

## 1. 新增材料（9 种）

| 名字 | 中文 / 英文 | 化学式 | 熔点 K | 沸点 K | 密度 | 颜色 | 形态 |
|---|---|---|---|---|---|---|---|
| `Styrene` | 苯乙烯 / Styrene | `C8H8` | 243 | 418 | 0.909 | `0xE8E0D0` | `liquid()` |
| `Diethylbenzene` | 二乙苯 / Diethylbenzene | `C10H14` | 190 | 457 | 0.86 | `0xE0D8C8` | `liquid()` |
| `Divinylbenzene` | 二乙烯苯 / Divinylbenzene | `C10H10` | 207 | 468 | 0.91 | `0xE8D8C0` | `liquid()` |
| `Polystyrene` | 聚苯乙烯 / Polystyrene | `(C8H8)n` | 513 | — | 1.05 | `0xF0F0F0` | `dust().polymer()` |
| `CrosslinkedPolystyrene` | 交联聚苯乙烯珠 / Crosslinked Polystyrene Beads | `(C8H8)n(C10H10)m` | 573 | — | 1.05 | `0xF0E8D8` | `dust()` |
| `ChloromethylatedPolystyrene` | 氯甲基化聚苯乙烯 / Chloromethylated Polystyrene | `(C9H9Cl)n` | 573 | — | 1.15 | `0xE8E0C0` | `dust()` |
| `CationExchangeResin` | 阳离子交换树脂 / Cation Exchange Resin | `(C8H7SO3H)n` | 573 | — | 1.25 | `0x8C4A2A` | `dust()` |
| `AnionExchangeResin` | 阴离子交换树脂 / Anion Exchange Resin | `(C12H18NCl)n` | 573 | — | 1.10 | `0xE0C890` | `dust()` |
| `PolymethylMethacrylate` | 有机玻璃 / PMMA | `(C5H8O2)n` | 433 | — | 1.18 | `0xE0F0F8` | `dust().polymer()` |

- 实现前在 GT6CE 里查一下有没有 `Polystyrene`（或 `PS`）。如果已有，就复用它，并从本表删掉。

## 2. 配方（`PolymerRecipes.styreneIonExchange()`）

- **苯乙烯**
  1. 乙苯脱氢（`gt.recipe.catalyticcracking`）：`1 × dust Hematite`（不消耗，铁系催化剂）+ `GenLiquid_Ethylbenzene (1000)` + `Steam (2000)` $\rightarrow$ `GenLiquid_Styrene (1000)` + `Hydrogen (1000)` + `DistW (2000)`；64 EU/t，200 ticks
- **二乙烯苯**
  1. 乙苯再烷基化（`gt.recipe.mixer`）：`1 × dust ZSM5`（不消耗）+ `GenLiquid_Ethylbenzene (1000)` + `Ethylene (1000)` $\rightarrow$ `GenLiquid_Diethylbenzene (1000)`；32 EU/t，160 ticks
  2. 二乙苯脱氢（`gt.recipe.catalyticcracking`）：`1 × dust Hematite`（不消耗）+ `GenLiquid_Diethylbenzene (1000)` + `Steam (2000)` $\rightarrow$ `GenLiquid_Divinylbenzene (1000)` + `Hydrogen (2000)` + `DistW (2000)`；64 EU/t，240 ticks
- **聚苯乙烯**（`gt.recipe.mixer`）：`circuit(1)` + `GenLiquid_Styrene (1000)` + `GenLiquid_CumeneHydroperoxide (10)` $\rightarrow$ `1 × dust Polystyrene`；32 EU/t，160 ticks
- **交联珠悬浮聚合**（`gt.recipe.mixer`）：`circuit(2)` + `GenLiquid_Styrene (900)` + `GenLiquid_Divinylbenzene (100)` + `GenLiquid_DeionizedWater (1000)` + `GenLiquid_CumeneHydroperoxide (10)` $\rightarrow$ `1 × dust CrosslinkedPolystyrene` + `DistW (1000)`；48 EU/t，200 ticks
- **阳离子交换树脂**（`gt.recipe.bath`）：`1 × dust CrosslinkedPolystyrene` + `GenLiquid_SulfuricAcid (1000)` $\rightarrow$ `1 × dust CationExchangeResin` + `DistW (1000)`；32 EU/t，200 ticks
- **阴离子交换树脂**
  1. 氯甲基化（`gt.recipe.mixer`）：`1 × dust CrosslinkedPolystyrene` + `GenGas_Formaldehyde (1000)` + `GenGas_HydrochloricAcid (1000)` + `GenLiquid_Methanol (100)` $\rightarrow$ `1 × dust ChloromethylatedPolystyrene` + `DistW (1000)`；48 EU/t，200 ticks
  2. 胺化（`gt.recipe.mixer`）：`1 × dust ChloromethylatedPolystyrene` + `GenLiquid_Methanol (3000)` + `GenGas_Ammonia (1000)` $\rightarrow$ `1 × dust AnionExchangeResin` + `DistW (3000)`
    - 甲醇 + 氨原位生成三甲胺，不单独注册三甲胺。
    - 48 EU/t，200 ticks。
- **树脂法去离子水**（M2 第二路线，`gt.recipe.bath`）：`1 × dust CationExchangeResin` + `1 × dust AnionExchangeResin`（均不消耗）+ `GenLiquid_DemineralizedWater (4000)` $\rightarrow$ `GenLiquid_DeionizedWater (3950)` + `Saltwater (50)`；30 EU/t，120 ticks
- **树脂法双酚 A**（M3 第二路线，`gt.recipe.mixer`）：`1 × dust CationExchangeResin`（不消耗）+ `GenLiquid_Phenol (2000)` + `GenLiquid_Acetone (1000)` $\rightarrow$ `1 × dust BisphenolA` + `DistW (1000)`；48 EU/t，200 ticks
- **PMMA 本体聚合**（`gt.recipe.mixer`）：`GenLiquid_MethylMethacrylate (1000)` + `GenLiquid_CumeneHydroperoxide (10)` $\rightarrow$ `1 × dust PolymethylMethacrylate`；32 EU/t，160 ticks
- **成型**：聚苯乙烯、PMMA 走第 3 张卡的 `polymerForming`（压板 + 轧箔）。

## 3. 明确不做

- 不做 ABS、SAN、高抗冲聚苯乙烯（HIPS）、发泡聚苯乙烯（EPS）
- 不做聚丙烯酸 / 高吸水树脂（丙烯酸留到 M6）
- 不做螯合树脂、大孔树脂等特种树脂
- 不让离子交换树脂失效或需要再生，催化剂一律不消耗

## 4. 验收

- [ ] 9 种新材料可查到，中英文译名齐全；`Polystyrene` 没有和 GT6CE 自带材料撞名
- [ ] 乙苯可经苯乙烯一路做到阳 / 阴离子交换树脂
- [ ] 树脂法去离子水与沸石法并存，同一台浴槽不串配方
- [ ] 树脂法双酚 A 与 M3 的盐酸法并存，同一台搅拌机不串配方
- [ ] 聚苯乙烯（电路 1）与交联珠（电路 2）靠电路区分
- [ ] 本卡全部配方注册成功，`REJECTED` 为空
- [ ] 双加载器编译通过，服务端启动至 `Done` 无异常
