# 异丙苯法苯酚丙酮联产与双酚 A 详细计划

> 计划 slug：`m3/cumene-phenol-bpa`
> 状态：关闭。本文件位于 `docs/card-plans/closed/`。前置是 [`C5 馏分热二聚拆解：异戊二烯、双环戊二烯与降冰片烯`](C5二聚与双环戊二烯详细计划.md)（实际依赖第 3、5 张卡的苯与 ZSM-5）。
> 性质：M3 收官卡。苯和丙烯烷基化成异丙苯，空气氧化成过氧化氢异丙苯，酸解出苯酚和丙酮；两者按 2:1 正好缩合成双酚 A，为 M4 的聚碳酸酯、环氧树脂和 M5 的 CD-R 盘基备料。

```text
lane            = closed
slug            = m3/cumene-phenol-bpa
owned_paths     = common/src/main/java/com/lombinaxmasson/ssite/chemistry/PetrochemRecipes.java
depends_on      = m3/c5-dimerization
close_target    = 异丙苯烷基化、两条氧化、酸解、双酚 A 缩合全线配方注册，并同步勾选路线图 M3 完成项
```

## 0. 它解决什么

异丙苯法（Hock 法）是石化里「副产物刚好被下一步吃掉」的经典：一分子异丙苯酸解给出一分子苯酚和一分子丙酮，而双酚 A 正好吃两份苯酚加一份丙酮。玩家只要把剩下的一份丙酮另找出路（M4 的 MMA、溶剂），就是一条几乎零浪费的线。

双酚 A 是 M4 聚碳酸酯（`Polycarbonate` 已内置于 GT6CE，但没有化工合成路线）、普通环氧树脂和四溴双酚 A 阻燃环氧（用 M2 的溴）的共同原料，也是 M5 CD-R 盘基的源头。

## 1. 行为

在 `PetrochemRecipes` 中新增 `cumenePhenol()`：

- **第一步：苯丙烯烷基化制异丙苯**（`gt.recipe.mixer`）
  - 输入：`1 × dust ZSM5`（不消耗）+ `GenLiquid_Benzene (1000)` + `Propylene (1000)`
  - 输出：`GenLiquid_Cumene (1000 mB)`
  - 耗能与时长：32 EU/t，160 ticks
- **第二步：异丙苯氧化制过氧化氢异丙苯**（`gt.recipe.mixer`）
  1. 纯氧：`GenLiquid_Cumene (1000)` + `Oxygen (1000)` $\rightarrow$ `GenLiquid_CumeneHydroperoxide (1000)`；32 EU/t，200 ticks
  2. 空气：`GenLiquid_Cumene (1000)` + `Air (5000)` $\rightarrow$ `GenLiquid_CumeneHydroperoxide (1000)` + `Nitrogen (4000)`；32 EU/t，400 ticks（免空分但慢一倍，还白送氮气）
- **第三步：酸解出苯酚与丙酮**（`gt.recipe.mixer`）
  - 输入：`GenLiquid_CumeneHydroperoxide (1000)` + `GenLiquid_SulfuricAcid (50)`
  - 输出：`GenLiquid_Phenol (1000)` + `GenLiquid_Acetone (1000)`
  - 耗能与时长：48 EU/t，120 ticks
- **第四步：双酚 A 缩合**（`gt.recipe.mixer`，盐酸催化，阳离子交换树脂路线留到 M4）
  - 输入：`GenLiquid_Phenol (2000)` + `GenLiquid_Acetone (1000)` + `GenGas_HydrochloricAcid (100)`
  - 输出：`1 × dust BisphenolA` + `DistW (1000 mB)`
  - 耗能与时长：48 EU/t，200 ticks
- **收尾**：同步勾选 [`docs/supersymmetry-ssite-plan.md`](../supersymmetry-ssite-plan.md) M3 章节中本里程碑实际完成的条目，并在该章节注明：结晶坩埚改冷冻机、Prins 法与「基础衍生物单体」移交 M4。

## 2. 明确不做

- 不做交联聚苯乙烯磺酸树脂催化双酚 A（M4 苯乙烯线做出树脂后再加第二条路线，并可同时接 M2 去离子水的树脂柱）
- 不做 α-甲基苯乙烯、苯乙酮等副产
- 不做苯酚的其他下游（酚醛、己内酰胺，M4）
- 不做丙酮的下游（MMA 丙酮氰醇法，M4）

## 3. 验收

- [ ] 异丙苯烷基化、两条氧化、酸解、双酚 A 共 5 条配方注册成功
- [ ] 游戏内跑通：苯 + 丙烯 $\rightarrow$ 异丙苯 $\rightarrow$ 苯酚 + 丙酮 $\rightarrow$ 双酚 A 粉
- [ ] M3 全线（第 2～7 张卡）在同一存档里连成闭环：原油 $\rightarrow$ 石脑油 $\rightarrow$ 裂解/重整 $\rightarrow$ BTX/C4/C5 $\rightarrow$ 对二甲苯、丁二烯、异戊二烯、DCPD、双酚 A
- [ ] 路线图 M3 章节已勾选并注明偏差
- [ ] `.\scripts\build.ps1` 双加载器编译通过，服务端启动至 `Done` 无异常
