# C3/C4 平台化学品：环氧氯丙烷、丙烯腈、丙烯酸、MMA、丁醇辛醇与 Prins 法异戊二烯详细计划

> 计划 slug：`m4/c3-platform`
> 状态：关闭。本文件位于 `docs/card-plans/closed/`。前置是 [`C1/C2 平台化学品`](C1C2平台化学品详细计划.md)。
> 性质：M4 第二张卡。丙烯下游的单体与醇类，外加 M3 因缺甲醛而移交过来的 Prins 法合成异戊二烯。

```text
lane            = closed
slug            = m4/c3-platform
owned_paths     = PolymerMaterials.java、PolymerRecipes.java、gt6ce 中英文语言文件
depends_on      = m4/c1-c2-platform
close_target    = 14 种材料注册；环氧氯丙烷两条路线、丙烯腈/氢氰酸、丙烯醛/丙烯酸、丙酮氰醇法 MMA、正丁醛/正丁醇/2-乙基己醇、Prins 异戊二烯全部可产
```

## 0. 它解决什么

| 产物 | 下游（M4 后续卡） |
|---|---|
| 环氧氯丙烷 | 环氧树脂四兄弟 |
| 丙烯腈 | 丁腈橡胶、聚丙烯腈 / 碳纤维 |
| 丙烯醛、丙烯酸 | 脂环族环氧；丙烯酸留给 M6 |
| 甲基丙烯酸甲酯（MMA） | 有机玻璃 PMMA |
| 正丁醛 | PVB 夹层膜 |
| 正丁醇、2-乙基己醇 | 萃取剂 TBP、P204 |
| Prins 法异戊二烯 | 不靠 C5 馏分也能做聚异戊二烯 |

两处跨里程碑联动：

- 环氧氯丙烷的**甘油法**直接吃 GT6 自带的甘油（`Glycerol`，生物柴油副产），和丙烯高温氯化法汇合在二氯丙醇；
- 丙酮氰醇法 MMA 吃 M3 异丙苯法联产的丙酮，正好消化掉做双酚 A 剩下的那一份。

## 1. 新增材料（14 种）

| 名字 | 中文 / 英文 | 化学式 | 熔点 K | 沸点 K | 密度 | 颜色 | 形态 |
|---|---|---|---|---|---|---|---|
| `AllylChloride` | 烯丙基氯 / Allyl Chloride | `C3H5Cl` | 138 | 318 | 0.94 | `0xE0E0B0` | `liquid()` |
| `Dichloropropanol` | 二氯丙醇 / Dichloropropanol | `C3H6Cl2O` | 269 | 447 | 1.36 | `0xD8D8A8` | `liquid()` |
| `Epichlorohydrin` | 环氧氯丙烷 / Epichlorohydrin | `C3H5ClO` | 247 | 391 | 1.18 | `0xE8E8C0` | `liquid()` |
| `HydrogenCyanide` | 氢氰酸 / Hydrogen Cyanide | `HCN` | 260 | 299 | 0.69 | `0xD0E8E8` | `liquid()` |
| `Acrylonitrile` | 丙烯腈 / Acrylonitrile | `C3H3N` | 189 | 350 | 0.81 | `0xE8D8C0` | `liquid()` |
| `Acrolein` | 丙烯醛 / Acrolein | `C3H4O` | 185 | 326 | 0.84 | `0xF0E0B0` | `liquid()` |
| `AcrylicAcid` | 丙烯酸 / Acrylic Acid | `C3H4O2` | 287 | 414 | 1.05 | `0xF0F0D8` | `liquid()` |
| `AcetoneCyanohydrin` | 丙酮氰醇 / Acetone Cyanohydrin | `C4H7NO` | 253 | 368 | 0.93 | `0xE0D8C8` | `liquid()` |
| `MethylMethacrylate` | 甲基丙烯酸甲酯 / Methyl Methacrylate | `C5H8O2` | 225 | 374 | 0.94 | `0xE8F0F8` | `liquid()` |
| `Butyraldehyde` | 正丁醛 / Butyraldehyde | `C4H8O` | 174 | 348 | 0.80 | `0xF0E8C8` | `liquid()` |
| `Butanol` | 正丁醇 / n-Butanol | `C4H9OH` | 183 | 391 | 0.81 | `0xE8E8E0` | `liquid()` |
| `TwoEthylhexanol` | 2-乙基己醇 / 2-Ethylhexanol | `C8H17OH` | 197 | 457 | 0.833 | `0xE0E0D8` | `liquid()` |
| `Dimethyldioxane` | 4,4-二甲基-1,3-二噁烷 / 4,4-Dimethyl-1,3-dioxane | `C6H12O2` | 185 | 406 | 0.95 | `0xE0E8D0` | `liquid()` |
| `SolidPhosphoricAcid` | 固体磷酸催化剂 / Solid Phosphoric Acid | `H3PO4·SiO2` | 1000 | 2000 | 2.0 | `0xD8D0B8` | `dust()` |

## 2. 配方（`PolymerRecipes.cThreePlatform()`）

- **环氧氯丙烷**
  1. 丙烯高温氯化（`gt.recipe.calciner`）：`Propylene (1000)` + `GenGas_Chlorine (1000)` $\rightarrow$ `GenLiquid_AllylChloride (1000)` + `GenGas_HydrochloricAcid (1000)`；48 EU/t，160 ticks
  2. 次氯酸化（`gt.recipe.mixer`）：`GenLiquid_AllylChloride (1000)` + `GenGas_Chlorine (1000)` + `Water (1000)` $\rightarrow$ `GenLiquid_Dichloropropanol (1000)` + `GenGas_HydrochloricAcid (1000)`；32 EU/t，120 ticks
  3. 甘油法（`gt.recipe.mixer`）：`GenLiquid_Glycerol (1000)` + `GenGas_HydrochloricAcid (2000)` + `GenLiquid_AceticAcid (50)` $\rightarrow$ `GenLiquid_Dichloropropanol (1000)` + `DistW (2000)`；32 EU/t，160 ticks
  4. 皂化闭环（`gt.recipe.bath`）：`1 × dust SodiumHydroxide` + `GenLiquid_Dichloropropanol (1000)` $\rightarrow$ `1 × dust Salt` + `GenLiquid_Epichlorohydrin (1000)` + `DistW (1000)`；32 EU/t，120 ticks
- **氢氰酸与丙烯腈**
  1. Andrussow 法氢氰酸（`gt.recipe.mixer`）：`1 × dust Platinum`（不消耗）+ `Methane (1000)` + `GenGas_Ammonia (1000)` + `Oxygen (1500)` $\rightarrow$ `GenLiquid_HydrogenCyanide (1000)` + `DistW (3000)`；64 EU/t，160 ticks
  2. 丙烯氨氧化（SOHIO，`gt.recipe.mixer`）：`1 × dust Bismuth` + `1 × dust Molybdenum`（均不消耗）+ `Propylene (1000)` + `GenGas_Ammonia (1000)` + `Oxygen (1500)` $\rightarrow$ `GenLiquid_Acrylonitrile (1000)` + `GenLiquid_HydrogenCyanide (150)`（副产水不出槽）；64 EU/t，200 ticks
- **丙烯醛与丙烯酸**
  1. 丙烯氧化（`gt.recipe.mixer`）：`1 × dust Bismuth` + `1 × dust Cobalt`（均不消耗，与氨氧化的铋钼催化剂区分）+ `Propylene (1000)` + `Oxygen (1000)` $\rightarrow$ `GenLiquid_Acrolein (1000)` + `DistW (1000)`；48 EU/t，160 ticks
  2. 丙烯醛氧化（`gt.recipe.mixer`）：`1 × dust Molybdenum` + `1 × dust Vanadium`（均不消耗）+ `GenLiquid_Acrolein (1000)` + `Oxygen (500)` $\rightarrow$ `GenLiquid_AcrylicAcid (1000)`；48 EU/t，160 ticks
- **丙酮氰醇法 MMA**
  1. 氰醇化（`gt.recipe.mixer`）：`1 × dustTiny SodiumHydroxide` + `GenLiquid_Acetone (1000)` + `GenLiquid_HydrogenCyanide (1000)` $\rightarrow$ `GenLiquid_AcetoneCyanohydrin (1000)`；32 EU/t，120 ticks
  2. 硫酸酰胺化 + 甲醇酯化（`gt.recipe.mixer`）：`GenLiquid_AcetoneCyanohydrin (1000)` + `GenLiquid_Methanol (1000)` + `GenLiquid_SulfuricAcid (1000)` $\rightarrow$ `GenLiquid_MethylMethacrylate (1000)` + `Dirty_Water (1000)`（硫酸氢铵废液折成脏水）；48 EU/t，200 ticks
- **羰基合成与醇类**
  1. 丙烯羰基合成正丁醛（`gt.recipe.mixer`）：`1 × dust Cobalt`（不消耗）+ `Propylene (1000)` + `GenGas_CarbonMonoxide (1000)` + `Hydrogen (1000)` $\rightarrow$ `GenLiquid_Butyraldehyde (1000)`；64 EU/t，200 ticks
  2. 正丁醛加氢（`gt.recipe.mixer`）：`circuit(1)` + `1 × dust Nickel`（不消耗）+ `GenLiquid_Butyraldehyde (1000)` + `Hydrogen (1000)` $\rightarrow$ `GenLiquid_Butanol (1000)`；32 EU/t，120 ticks
  3. 羟醛缩合加氢制 2-乙基己醇（`gt.recipe.mixer`）：`circuit(2)` + `1 × dust Nickel`（不消耗）+ `1 × dustTiny SodiumHydroxide` + `GenLiquid_Butyraldehyde (2000)` + `Hydrogen (2000)` $\rightarrow$ `GenLiquid_TwoEthylhexanol (1000)` + `DistW (1000)`；48 EU/t，200 ticks
- **Prins 法异戊二烯（M3 移交）**
  1. 固体磷酸催化剂（`gt.recipe.calciner`）：`1 × dust Phosphorus` + `2 × dust SiliconDioxide` + `Oxygen (2500)` + `DistW (1500)` $\rightarrow$ `3 × dust SolidPhosphoricAcid`；48 EU/t，200 ticks
  2. 异丁烯甲醛缩合（`gt.recipe.mixer`）：`GenGas_Isobutylene (1000)` + `GenGas_Formaldehyde (2000)` + `GenLiquid_SulfuricAcid (50)` $\rightarrow$ `GenLiquid_Dimethyldioxane (1000)`；32 EU/t，160 ticks
  3. 二噁烷催化裂解（`gt.recipe.catalyticcracking`）：`1 × dust SolidPhosphoricAcid`（不消耗）+ `GenLiquid_Dimethyldioxane (1000)` $\rightarrow$ `GenLiquid_Isoprene (1000)` + `GenGas_Formaldehyde (1000)` + `DistW (1000)`；48 EU/t，160 ticks

## 3. 明确不做

- 不做异丁烯直接氧化法 MMA（C4 法）和乙烯法 MMA
- 不做氯醇法 / 共氧化法环氧丙烷
- 不做丙烯酸酯类单体（丙烯酸丁酯等），丙烯酸只停在酸
- 不做氰化钠（金银氰化浸出留给远期湿法冶金）

## 4. 验收

- [ ] 14 种新材料可查到，中英文译名齐全
- [ ] 本卡全部配方注册成功，`REJECTED` 为空
- [ ] 丙烯氨氧化（铋 + 钼）与丙烯氧化（铋 + 钴）靠催化剂组合区分，同一台搅拌机不会串配方
- [ ] 正丁醇与 2-乙基己醇靠编程电路 1 / 2 区分，不会串配方
- [ ] `GenGas_Ammonia`、`GenLiquid_Glycerol`、`Phosphorus` 字段名在实现时已确认可取到
- [ ] 双加载器编译通过，服务端启动至 `Done` 无异常
