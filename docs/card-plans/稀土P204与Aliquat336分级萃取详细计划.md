# 稀土 P204 / Aliquat 336 分级萃取与变价分离详细计划

> 计划 slug：`m6/rare-earth-extraction`
> 状态：待实现（2026-10-24）。本文件位于 `docs/card-plans/`。前置是 [`稀土三矿开矿与直拆捷径移除`](稀土三矿开矿与直拆捷径移除详细计划.md)（提供 `MixedRareEarthSolution`）与 M4 [`萃取剂与润滑油基础油`](closed/萃取剂与润滑油基础油详细计划.md)（提供 `P204`、`Aliquat 336`）。
> 性质：M6 第二张卡。把上一张卡的混合稀土母液用 **`P204`** 切成轻 / 中 / 重三段，再用 **$\text{Ce}^{4+}$ 氧化沉淀**、**$\text{Eu}^{2+}$ 锌还原**与 **`Aliquat 336`** 把 11 种单一稀土氧化物拆出来。全程只用 GT6CE 内置 `mixer` 与 `centrifuge`，不新增机器。

```text
lane            = planned
slug            = m6/rare-earth-extraction
owned_paths     = common/src/main/java/com/lombinaxmasson/ssite/material/RareEarthMaterials.java
                  common/src/main/java/com/lombinaxmasson/ssite/chemistry/RareEarthRecipes.java
                  common/src/main/resources/assets/gt6ce/lang/en_us.json
                  common/src/main/resources/assets/gt6ce/lang/zh_cn.json
depends_on      = m6/ore-leaching, m4/extractants-lubricants
close_target    = 24 种材料注册；P204 三段分组、Ce/Eu 变价分离、Aliquat 提镧提钇全部跑通，11 种稀土氧化物可产
```

---

## 0. 它解决什么

1. **让 M4 的 P204 与 Aliquat 336 正式上岗**：上一张卡只有一种 `MixedRareEarthSolution`，本卡把它拆成 **轻稀土（La-Ce-Pr-Nd）**、**中稀土（Sm-Eu-Gd）**、**重稀土（Tb-Dy-Lu）** 三段，再拆成单一氧化物。
2. **复刻两条经典变价分离**：
   - 轻稀土段用**氯气氧化**把 $\text{Ce}^{3+}$ 变成 $\text{Ce}^{4+}$，以 `CeO2` 优先沉淀；
   - 中稀土段用**锌粉还原**把 $\text{Eu}^{3+}$ 变成 $\text{Eu}^{2+}$，以 `Eu2O3` 优先沉淀。
   这两步是稀土分离里最有辨识度的操作，也是本卡与「一路 P204 萃取到底」的区别。
3. **为第 3 张卡备好全部氧化物**：11 种 `X2O3`（铈为 `CeO2`）直接进氟化炉；Ho / Er / Tm / Yb 不单独拆，合并成 `HeavyRareEarthResidue`，留给 M9 的激光/荧光材料。

---

## 1. 新增材料（24 种，追加至 `RareEarthMaterials`）

### 1.1 11 种稀土氧化物

| 名字 | 中文 / 英文 | 化学式 | 熔点 K | 沸点 K | 密度 | 颜色 | 形态 |
|---|---|---|---|---|---|---|---|
| `LanthanumOxide` | 氧化镧 / Lanthanum Oxide | `La2O3` | 2588 | 4200 | 6.51 | `0xF0F0E0` | `dust()` |
| `CeriumOxide` | 二氧化铈 / Cerium Dioxide | `CeO2` | 2873 | 3800 | 7.22 | `0xFFF4C8` | `dust()` |
| `PraseodymiumOxide` | 氧化镨 / Praseodymium Oxide | `Pr2O3` | 2473 | 3900 | 6.77 | `0x98C060` | `dust()` |
| `NeodymiumOxide` | 氧化钕 / Neodymium Oxide | `Nd2O3` | 2503 | 3900 | 7.24 | `0xC8B8E8` | `dust()` |
| `SamariumOxide` | 氧化钐 / Samarium Oxide | `Sm2O3` | 2603 | 4100 | 7.74 | `0xE8D8A0` | `dust()` |
| `EuropiumOxide` | 氧化铕 / Europium Oxide | `Eu2O3` | 2623 | 3900 | 7.42 | `0xF0C8D8` | `dust()` |
| `GadoliniumOxide` | 氧化钆 / Gadolinium Oxide | `Gd2O3` | 2693 | 4200 | 7.41 | `0xE0E0E8` | `dust()` |
| `TerbiumOxide` | 氧化铽 / Terbium Oxide | `Tb2O3` | 2673 | 3900 | 8.27 | `0xD8C8A0` | `dust()` |
| `DysprosiumOxide` | 氧化镝 / Dysprosium Oxide | `Dy2O3` | 2673 | 3900 | 7.81 | `0xC8D0D8` | `dust()` |
| `YttriumOxide` | 氧化钇 / Yttrium Oxide | `Y2O3` | 2693 | 4300 | 5.01 | `0xE8E8E0` | `dust()` |
| `LutetiumOxide` | 氧化镥 / Lutetium Oxide | `Lu2O3` | 2763 | 4200 | 9.42 | `0xD0D8E8` | `dust()` |

### 1.2 重稀土残液与萃取相

| 名字 | 中文 / 英文 | 化学式 | 熔点 K | 沸点 K | 密度 | 颜色 | 形态 |
|---|---|---|---|---|---|---|---|
| `HeavyRareEarthResidue` | 重稀土富集物（Ho-Er-Tm-Yb）/ Heavy Rare Earth Residue | `(Ho,Er,Tm,Yb)2O3` | 2650 | 3900 | 8.60 | `0xB8B8A0` | `dust()` |
| `LightMiddleP204Extract` | 轻中稀土 P204 负载相 / Light-Middle P204 Extract | `(RE)·P204` | 210 | 400 | 0.98 | `0xC8B070` | `liquid()` |
| `MiddleRareEarthP204Extract` | 中重稀土 P204 负载相 / Middle P204 Extract | `(Sm-Gd)·P204` | 210 | 400 | 0.97 | `0xC0A868` | `liquid()` |
| `LightRareEarthChloride` | 轻稀土氯化液 / Light Rare Earth Chloride | `LaCl3·CeCl3·PrCl3·NdCl3(aq)` | 290 | 390 | 1.30 | `0xD8E8B8` | `liquid()` |
| `MiddleRareEarthChloride` | 中稀土氯化液 / Middle Rare Earth Chloride | `SmCl3·EuCl3·GdCl3(aq)` | 290 | 390 | 1.30 | `0xD0E0B0` | `liquid()` |
| `HeavyRareEarthChloride` | 重稀土氯化液 / Heavy Rare Earth Chloride | `TbCl3·DyCl3·YCl3·LuCl3(aq)` | 290 | 390 | 1.32 | `0xC8D8A8` | `liquid()` |
| `LightRareEarthRaffinate` | 除铈轻稀土液 / Cerium-Free Light Rare Earth Liquor | `LaCl3·PrCl3·NdCl3(aq)` | 290 | 390 | 1.28 | `0xD8E8C0` | `liquid()` |
| `PrNdRaffinate` | 镨钕氯化液 / Pr-Nd Chloride Liquor | `PrCl3·NdCl3(aq)` | 290 | 390 | 1.29 | `0xC8D8B0` | `liquid()` |
| `PrP204Extract` | 镨 P204 负载相 / Praseodymium P204 Extract | `Pr·P204` | 210 | 400 | 0.96 | `0xB8A060` | `liquid()` |
| `SmGdChloride` | 钐钆氯化液 / Sm-Gd Chloride Liquor | `SmCl3·GdCl3(aq)` | 290 | 390 | 1.30 | `0xD0D8A8` | `liquid()` |
| `SmP204Extract` | 钐 P204 负载相 / Samarium P204 Extract | `Sm·P204` | 210 | 400 | 0.96 | `0xB0A058` | `liquid()` |
| `TbDyLuChloride` | 铽镝镥氯化液 / Tb-Dy-Lu Chloride Liquor | `TbCl3·DyCl3·LuCl3(aq)` | 290 | 390 | 1.31 | `0xC0C8A0` | `liquid()` |
| `TbDyP204Extract` | 铽镝 P204 负载相 / Tb-Dy P204 Extract | `(Tb,Dy)·P204` | 210 | 400 | 0.97 | `0xA89858` | `liquid()` |

- GT6CE 已有：`DiethylhexylPhosphoricAcid`（`P204`）、`Aliquat`（Aliquat 336）、`Ammonia`、`HydrochloricAcid`、`SulfuricAcid`、`Chlorine`、`CarbonDioxide`、`SodiumHydroxide`、`Zinc`、`DeionizedWater`、`Dirty_Water`。
- 所有液体密度取 `0.96..1.35`，熔点取 210 K（有机相）或 290 K（水相），避免流体温差逻辑把萃取相当成气体。

---

## 2. 配方设计（`RareEarthRecipes.rareEarthExtraction()`）

### 2.1 P204 三段分组

1. **P204 粗分：轻中稀土进有机相，重稀土留在水相（`gt.recipe.mixer`）**：
   `GenLiquid_MixedRareEarthSolution (4000) + GenLiquid_DiethylhexylPhosphoricAcid (2000) + GenGas_HydrochloricAcid (500)`
   $\rightarrow$ `GenLiquid_LightMiddleP204Extract (2000) + GenLiquid_HeavyRareEarthChloride (2000) + Dirty_Water (2000)`；160 ticks，64 EU/t。
   - 说明：P204 对轻/中稀土亲和力高，重稀土留在萃余水相；`HCl` 调酸度。
2. **轻段反萃（`gt.recipe.mixer`）**：
   `GenLiquid_LightMiddleP204Extract (2000) + GenGas_HydrochloricAcid (1500)`
   $\rightarrow$ `GenLiquid_LightRareEarthChloride (2000) + GenLiquid_MiddleRareEarthP204Extract (2000)`；160 ticks，64 EU/t。
3. **中段反萃并再生 P204（`gt.recipe.mixer`）**：
   `GenLiquid_MiddleRareEarthP204Extract (2000) + GenLiquid_SulfuricAcid (500)`
   $\rightarrow$ `GenLiquid_MiddleRareEarthChloride (2000) + GenLiquid_DiethylhexylPhosphoricAcid (1900)`；160 ticks，64 EU/t。
   - 说明：回收 **95% 的 P204**（`1900 / 2000 mB`），其余 5% 由 M4 产线补。

### 2.2 轻稀土：铈氧化、Aliquat 提镧、P204 拆镨钕

1. **氯气氧化沉铈（`gt.recipe.mixer`）**：
   `GenLiquid_LightRareEarthChloride (2000) + GenGas_Chlorine (1000) + 4 × dust SodiumHydroxide`
   $\rightarrow$ `1 × dust CeriumOxide + GenLiquid_LightRareEarthRaffinate (2000) + Dirty_Water (1000)`；160 ticks，64 EU/t。
   - 说明：$\text{Ce}^{3+} \rightarrow \text{Ce}^{4+}$ 后以 $\text{Ce(OH)}_4$ 沉淀，煅烧与沉淀折成一步直接出 `CeO2`；其余三价稀土留在水相。
2. **Aliquat 336 提镧（`gt.recipe.mixer`）**：
   `GenLiquid_LightRareEarthRaffinate (2000) + GenLiquid_Aliquat (1000) + GenGas_HydrochloricAcid (1000)`
   $\rightarrow$ `1 × dust LanthanumOxide + GenLiquid_PrNdRaffinate (2000) + GenLiquid_Aliquat (950)`；160 ticks，64 EU/t。
   - 说明：把「Aliquat 萃镧 + 盐酸反萃 + 氨沉煅烧」折成一步，直接出 `La2O3`；回收 **95% 的 Aliquat**。
3. **P204 拆镨钕（`gt.recipe.mixer`）**：
   `GenLiquid_PrNdRaffinate (2000) + GenLiquid_DiethylhexylPhosphoricAcid (1000)`
   $\rightarrow$ `1 × dust NeodymiumOxide + GenLiquid_PrP204Extract (1000) + Dirty_Water (1000)`；160 ticks，64 EU/t。
   - 说明：P204 优先萃取镨，钕留在水相并以氨沉煅烧折成 `Nd2O3`；钕给 1 U，符合「钕是磁体主量」的直觉。
4. **镨反萃（`gt.recipe.mixer`）**：
   `GenLiquid_PrP204Extract (1000) + GenLiquid_SulfuricAcid (500)`
   $\rightarrow$ `1 × dust PraseodymiumOxide + GenLiquid_DiethylhexylPhosphoricAcid (950)`；160 ticks，48 EU/t。

### 2.3 中稀土：铕锌还原、P204 拆钐钆

1. **锌粉还原沉铕（`gt.recipe.mixer`）**：
   `GenLiquid_MiddleRareEarthChloride (2000) + 2 × dust Zinc + GenGas_HydrochloricAcid (500)`
   $\rightarrow$ `1 × dust EuropiumOxide + GenLiquid_SmGdChloride (2000) + Dirty_Water (1000)`；160 ticks，64 EU/t。
   - 说明：$\text{Eu}^{3+} \rightarrow \text{Eu}^{2+}$ 后以硫酸亚铕/氢氧化物形式优先析出，折成一步直接出 `Eu2O3`；锌进废水，不回收（M7 的湿法锌会补锌的循环，本卡不抢）。
2. **P204 拆钐钆（`gt.recipe.mixer`）**：
   `GenLiquid_SmGdChloride (2000) + GenLiquid_DiethylhexylPhosphoricAcid (1000)`
   $\rightarrow$ `1 × dust GadoliniumOxide + GenLiquid_SmP204Extract (1000) + Dirty_Water (1000)`；160 ticks，64 EU/t。
3. **钐反萃（`gt.recipe.mixer`）**：
   `GenLiquid_SmP204Extract (1000) + GenLiquid_SulfuricAcid (500)`
   $\rightarrow$ `1 × dust SamariumOxide + GenLiquid_DiethylhexylPhosphoricAcid (950)`；160 ticks，48 EU/t。

### 2.4 重稀土：Aliquat 提钇、P204 拆镥与铽镝、离心分铽镝

1. **Aliquat 336 提钇（`gt.recipe.mixer`）**：
   `GenLiquid_HeavyRareEarthChloride (2000) + GenLiquid_Aliquat (1000) + GenGas_HydrochloricAcid (1000)`
   $\rightarrow$ `1 × dust YttriumOxide + GenLiquid_TbDyLuChloride (2000) + GenLiquid_Aliquat (950)`；160 ticks，64 EU/t。
   - 说明：钇在重稀土段优先被 Aliquat 萃取并反萃；回收 95% Aliquat。
2. **P204 拆镥与铽镝（`gt.recipe.mixer`）**：
   `GenLiquid_TbDyLuChloride (2000) + GenLiquid_DiethylhexylPhosphoricAcid (1000)`
   $\rightarrow$ `1 × dust LutetiumOxide + GenLiquid_TbDyP204Extract (1000) + Dirty_Water (1000)`；160 ticks，64 EU/t。
3. **铽镝离心拆分（`gt.recipe.centrifuge`）**：
   `GenLiquid_TbDyP204Extract (1000)`
   $\rightarrow$ `1 × dust TerbiumOxide + 1 × dust DysprosiumOxide + 1 × dust HeavyRareEarthResidue + GenLiquid_DiethylhexylPhosphoricAcid (950) + Dirty_Water (500)`；160 ticks，64 EU/t。
   - 说明：离心机只有 1 个流体输入槽，正好用「单一负载相进、多种固体出」把铽/镝/重稀土残渣一次拆开；Ho / Er / Tm / Yb 全部并进 `HeavyRareEarthResidue`。

---

## 3. 数量与循环核对

| 项 | 投入 | 回收 | 说明 |
|---|---|---|---|
| P204 粗分 | 2000 mB | 步骤 2.1-3 回收 1900 mB | 大循环 95% |
| P204 镨钕 | 1000 mB | 步骤 2.2-4 回收 950 mB | 小循环 95% |
| P204 钐钆 | 1000 mB | 步骤 2.3-3 回收 950 mB | 小循环 95% |
| P204 铽镝 | 1000 mB | 步骤 2.4-3 回收 950 mB | 小循环 95% |
| Aliquat 提镧 | 1000 mB | 步骤 2.2-2 回收 950 mB | 95% |
| Aliquat 提钇 | 1000 mB | 步骤 2.4-1 回收 950 mB | 95% |

- 11 种氧化物的原子去向：轻段 4 种（La/Ce/Pr/Nd）、中段 3 种（Sm/Eu/Gd）、重段 4 种（Tb/Dy/Y/Lu）；Ho/Er/Tm/Yb 进 `HeavyRareEarthResidue`。
- 所有 `mixer` 行最多 3 路流体输入、2 路流体输出，符合 GT6CE `gt.recipe.mixer` 的 `6 物品入 / 1 物品出 / 6 流体入 / 2 流体出`。
- 唯一的多固体拆分放在 `centrifuge`（`1 流体入 / 6 物品出 / 6 流体出`），不要往 `mixer` 里塞两个物品输出。

---

## 4. 明确不做

- 不做 P507 / Cyanex / LIX 等其他萃取剂；M4 只备了 P204、TBP、Aliquat 336、三辛胺，本卡只用 P204 与 Aliquat。
- 不单独拆 Ho / Er / Tm / Yb；它们并进 `HeavyRareEarthResidue`，M9 需要时再开卡。
- 不做铈的非稀土杂质（Th、Fe、Si）深度除杂；这些在上一张卡的钍饼/废水分流里已经处理。
- 不做稀土金属化（第 3 张卡）。
- 不新增机器；全部 `mixer` + `centrifuge`，**没有新机器贴图**。

---

## 5. 验收标准

- [ ] 24 种新材料全部注册，中英文译名齐全。
- [ ] `MixedRareEarthSolution` 经 P204 粗分得到轻/中/重三段，P204 在三条反萃线上各回收 95%。
- [ ] 轻段：`CeO2` 由氯气氧化沉淀单独拆出；`La2O3` 由 Aliquat 336 拆出；`Pr2O3` / `Nd2O3` 由 P204 拆开。
- [ ] 中段：`Eu2O3` 由锌粉还原单独拆出；`Sm2O3` / `Gd2O3` 由 P204 拆开。
- [ ] 重段：`Y2O3` 由 Aliquat 336 拆出；`Lu2O3`、`Tb2O3`、`Dy2O3` 与 `HeavyRareEarthResidue` 由 P204 + 离心机拆开。
- [ ] 11 种单一氧化物全部可在 EMI 中查到来源，且都能进入第 3 张卡的氟化炉。
- [ ] 所有配方注册成功，`REJECTED` 为空；双加载器 `build` 通过，NeoForge 服务端启动至 `Done` 无异常。
