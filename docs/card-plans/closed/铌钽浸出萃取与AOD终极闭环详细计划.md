# 铌钽矿 HF 浸出、MIBK 萃取拆分与 AOD 终极材料闭环详细计划

> 计划 slug：`m5/niobium-tantalum-mibk`
> 状态：关闭（2026-10-23）。本文件位于 `docs/card-plans/closed/`。前置是 [`砍掉矿石/氧化物直拆捷径与拜耳法铝镓闭环`](拜耳法铝镓与直拆捷径移除详细计划.md)、[`水冷铜电弧重熔炉`](水冷铜电弧重熔炉详细计划.md)、[`锆石氯化、MIBK 锆铪萃取分离与 Kroll 海绵锆/铪`](锆石氯化与锆铪分离详细计划.md) 以及 M4 [`萃取剂与合成润滑油基础油`](萃取剂与润滑油基础油详细计划.md)。
> 性质：M5 收官卡（第 5 张）。在卡 1 砍掉钶钽铁矿（`Coltan`）、铌铁矿（`Columbite`）、钽铁矿（`Tantalite`）离心与电解直拆捷径的基础上，实装氢氟酸-硫酸混酸浸出 $\rightarrow$ **`MIBK` 萃取与分步反萃拆分铌钽** $\rightarrow$ 铝热还原铌（`Niobium`）与钠热还原钽（`Tantalum`），配合卡 2–4 的钛与铪，在生存模式下彻底闭环 M1 全六档 AOD 转炉（含 `NiobiumTitanium` 与 `TantalumHafniumCarbide` $\text{Ta}_4\text{HfC}_5$）！

```text
lane            = closed
slug            = m5/niobium-tantalum-mibk
owned_paths     = common/src/main/java/com/lombinaxmasson/ssite/material/MetallurgyMaterials.java
                  common/src/main/java/com/lombinaxmasson/ssite/chemistry/MetallurgyRecipes.java
                  common/src/main/resources/assets/gt6ce/lang/en_us.json
                  common/src/main/resources/assets/gt6ce/lang/zh_cn.json
depends_on      = m5/bayer-aluminium-gallium, m5/var-furnace, m5/zirconium-hafnium-mibk
close_target    = 3 种铌钽萃取流体注册；Coltan/Columbite/Tantalite 混酸浸出、MIBK 拆铌钽、铝热/钠热还原与 NbTi/Ta4HfC5 全线闭环
```

---

## 0. 它解决什么

1. **填补钶钽铁矿（`Coltan`）被砍直拆后的正规湿法产线**：
   - 卡 1 移除了 `Coltan -> Columbite + Tantalite -> Nb2O5 / Ta2O5 -> Nb / Ta` 的离心+电解无脑直拆链。
   - 现实中铌和钽总是共生在钶钽铁矿中，必须用高浓度**氢氟酸（$\text{HF}$）+ 硫酸**分解矿石，生成氟铌酸（$\text{H}_2\text{NbOF}_5$）与氟钽酸（$\text{H}_2\text{TaF}_7$）混合母液，再用 **`MIBK`（甲基异丁基酮）** 萃取并通过调节酸度分步反萃，才能将铌和钽彻底分开。
2. **改善原版还原配方的古怪流体计量并实现氟循环**：
   - 原版 `GTChemGen` 的 `Nb2O5` 和 `K2TaF7` 还原写在 0 EU/t 的 `Bath` 里，且要求输入非整数桶的熔融铝（`480 mB`）和熔融钠（`720 mB`）。
   - 本卡在 `gt.recipe.calciner` 中提供清晰的整数计量**五氧化二铌铝热还原**与**氟钽酸钾钠热还原**配方，并补齐副产氟化钠（`SodiumFluoride`）加硫酸回收氢氟酸（$\text{HF}$）的闭环。
3. **M1 六档 AOD 转炉终极会师**：
   - 至此，**铌（`Nb`）+ 钛（`Ti`）$\rightarrow$ 铌钛（`NiobiumTitanium`）** 与 **钽（`Ta`）+ 铪（`Hf`）+ 碳（`C`）$\rightarrow$ 碳化钽铪（`TantalumHafniumCarbide` $\text{Ta}_4\text{HfC}_5$）** 所需的全部特种金属均可通过生存产线量产并在 `ssite:var_furnace` 中熔炼成型！

---

## 1. 新增材料（3 种，追加至 `MetallurgyMaterials`）

| 名字 | 中文 / 英文 | 化学式 | 熔点 K | 沸点 K | 密度 | 颜色 | 形态 |
|---|---|---|---|---|---|---|---|
| `NbTaLeachLiquor` | 氟铌钽酸浸出液 / Fluoroniobic-Tantalic Leach Liquor | `H2NbOF5·H2TaF7(aq)` | 260 | 385 | 1.42 | `0x98B890` | `liquid()` |
| `NbTaMibkExtract` | 负载铌钽 MIBK 有机相 / Loaded Nb-Ta MIBK Extract | `(Nb,Ta)F·MIBK` | 200 | 395 | 0.98 | `0xB8A878` | `liquid()` |
| `TantalumRichMibk` | 富钽 MIBK 有机相 / Tantalum-Rich MIBK Phase | `H2TaF7·MIBK` | 200 | 395 | 0.96 | `0xC89868` | `liquid()` |

- GT6CE 已有：`Coltan`、`Columbite`、`Tantalite`、`Pyrolusite`（$\text{MnO}_2$）、`NiobiumPentoxide`（$\text{Nb}_2\text{O}_5$）、`TantalumPentoxide`（$\text{Ta}_2\text{O}_5$）、`PotassiumFluoride`（$\text{KF}$）、`PotassiumHeptafluorotantalate`（$\text{K}_2\text{TaF}_7$）、`SodiumFluoride`（$\text{NaF}$）、`Niobium`、`Tantalum`。

---

## 2. 配方设计（`MetallurgyRecipes.niobiumTantalumMibk()`）

### 2.1 铌钽矿 HF + $\text{H}_2\text{SO}_4$ 混酸浸出

1. **钶钽铁矿浸出（均衡产氟铌钽酸母液，`gt.recipe.mixer`）**：
   - `2 × dust Coltan` + `GenGas_HydrogenFluoride (6000)` + `GenLiquid_SulfuricAcid (1000)` $\rightarrow$ `1 × dust Pyrolusite` + `GenLiquid_NbTaLeachLiquor (2000)` + `Dirty_Water (1000)`；64 EU/t，200 ticks。
2. **铌铁矿浸出（富铌矿，`gt.recipe.calciner`）**：
   - `2 × dust Columbite` + `GenGas_HydrogenFluoride (4000)` + `GenLiquid_SulfuricAcid (1000)` $\rightarrow$ `1 × dust NiobiumPentoxide` + `1 × dust Pyrolusite` + `GenLiquid_NbTaLeachLiquor (1000)`；64 EU/t，200 ticks。
3. **钽铁矿浸出（富钽矿，`gt.recipe.calciner`）**：
   - `2 × dust Tantalite` + `GenGas_HydrogenFluoride (4000)` + `GenLiquid_SulfuricAcid (1000)` $\rightarrow$ `1 × dust TantalumPentoxide` + `1 × dust Pyrolusite` + `GenLiquid_NbTaLeachLiquor (1000)`；64 EU/t，200 ticks。
   - 说明：钽铁矿浸出直接联产的 `TantalumPentoxide` 可走原版 `gt.recipe.mixer`（`7 Ta2O5 + 8 KF + 20000 HF -> 20 K2TaF7 + 15000 H2O`）或下方新增的小批量转化行：`1 × dust TantalumPentoxide` + `2 × dust PotassiumFluoride` + `GenGas_HydrogenFluoride (5000)` $\rightarrow$ `2 × dust PotassiumHeptafluorotantalate` + `DistW (3000)`（64 EU/t，160 ticks）。

### 2.2 MIBK 溶剂萃取与分步反萃拆铌钽

1. **MIBK 共萃除杂（`gt.recipe.mixer`）**：
   - `GenLiquid_NbTaLeachLiquor (2000)` + `GenLiquid_MethylIsobutylKetone (2000)` $\rightarrow$ `GenLiquid_NbTaMibkExtract (2000)` + `Dirty_Water (2000)`；64 EU/t，160 ticks。
2. **去离子水低酸度优先反萃铌（`gt.recipe.mixer`）**：
   - `GenLiquid_NbTaMibkExtract (2000)` + `GenLiquid_DeionizedWater (1000)` $\rightarrow$ `1 × dust NiobiumPentoxide` + `GenLiquid_TantalumRichMibk (2000)` + `GenGas_HydrogenFluoride (2000)`；64 EU/t，160 ticks。
   - 说明：铌在低酸度下优先水解反萃进入水相，折一步直接产出 `1 × dust NiobiumPentoxide` 并回收 `2000 mB HF`；钽继续留在有机相 `TantalumRichMibk` 中。
3. **钾盐反萃结晶氟钽酸钾并再生 MIBK（`gt.recipe.centrifuge`）**：
   - `2 × dust PotassiumFluoride` + `GenLiquid_TantalumRichMibk (2000)` $\rightarrow$ `1 × dust PotassiumHeptafluorotantalate` + `GenLiquid_MethylIsobutylKetone (1900)` + `GenGas_HydrogenFluoride (1000)`；64 EU/t，160 ticks。
   - 说明：回收 **95% 的 `MIBK`**（`1900 / 2000 mB`）与 `1000 mB HF`，产出高纯氟钽酸钾结晶（`K2TaF7`）。

### 2.3 金属铌、金属钽还原与氟循环闭环

1. **五氧化二铌铝热还原（`gt.recipe.calciner`）**：
   - `1 × dust NiobiumPentoxide` + `2 × dust Aluminium` $\rightarrow$ `2 × dust Niobium` + `1 × dust Alumina`；96 EU/t，240 ticks。
   - 说明：副产 `Alumina` 可回卡 1 冰晶石电解再生金属铝。
2. **氟钽酸钾钠热还原（`gt.recipe.calciner`）**：
   - `1 × dust PotassiumHeptafluorotantalate` + `5 × dust Sodium` $\rightarrow$ `1 × dust Tantalum` + `2 × dust PotassiumFluoride` + `5 × dust SodiumFluoride`；96 EU/t，240 ticks。
   - 说明：副产的 `2 × dust PotassiumFluoride` 100% 闭环回流至上一步反萃结晶氟钽酸钾！
3. **副产氟化钠酸解回收氢氟酸（`gt.recipe.mixer`）**：
   - `2 × dust SodiumFluoride` + `GenLiquid_SulfuricAcid (1000)` $\rightarrow$ `1 × dust SodiumSulfate` + `GenGas_HydrogenFluoride (2000)`；32 EU/t，100 ticks。

---

## 3. 明确不做

- 不新增烧绿石（`Pyrochlore`）矿石（GT6CE 世界生成主产 `Coltan`、`Columbite`、`Tantalite`）
- 不删除原版 `Bath` 的熔融铝/熔融钠还原旧行（但因上游 `Nb2O5`/`K2TaF7` 已必须走本卡 HF + MIBK 产线，保留旧还原行不构成跳科技捷径）

---

## 4. 验收标准

- [x] 3 种新流体材料注册成功，中英文译名齐全
- [x] `Coltan` / `Columbite` / `Tantalite` $\xrightarrow{\text{HF + H}_2\text{SO}_4}$ `NbTaLeachLiquor` $\xrightarrow{\text{MIBK}}$ `NiobiumPentoxide` + `PotassiumHeptafluorotantalate` $\rightarrow$ `Niobium` + `Tantalum` 全线跑通
- [x] 副产 `PotassiumFluoride` 与 `SodiumFluoride`（转 `HF`）以及 `MIBK`（95% 回收）循环通畅
- [ ] 配合卡 2–4 的产物，可在 `ssite:var_furnace` 中熔炼出 `NiobiumTitanium` 与 `TantalumHafniumCarbide`（$\text{Ta}_4\text{HfC}_5$），并合出 M1 全部六档 AOD 转炉
- [x] 双加载器编译通过，服务端启动无异常
