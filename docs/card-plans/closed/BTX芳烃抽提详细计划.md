# BTX 芳烃糠醛抽提与溶剂循环详细计划

> 计划 slug：`m3/btx-extraction`
> 状态：关闭。本文件位于 `docs/card-plans/closed/`。前置是 [`原油常压蒸馏、石脑油蒸汽裂解与催化重整`](原油蒸馏与石脑油裂解重整详细计划.md)。
> 性质：M3 第三张卡。把裂解汽油和重整油里的芳烃用糠醛抽出来，汽提回收溶剂，再精馏出苯、甲苯、乙苯、混合二甲苯；抽余油切出 C5 馏分，余下的回裂解炉。

```text
lane            = closed
slug            = m3/btx-extraction
owned_paths     = common/src/main/java/com/lombinaxmasson/ssite/chemistry/PetrochemRecipes.java
depends_on      = m3/crude-refining-front
close_target    = 糠醛自制、萃取、汽提回收、BTEX 精馏、抽余油切 C5 全线配方注册，溶剂循环每批损耗约 5%
```

## 0. 它解决什么

SuSy 的芳烃抽提是「溶剂要单独平衡」的典型：溶剂不是催化剂，进萃取塔就跟着芳烃一起走，必须再汽提回来，回收不全就要持续补料。本卡按这个结构拆成四步，让玩家真的要管一条糠醛循环：

1. 生物质酸解造糠醛（第一桶溶剂）；
2. 萃取：原料 + 糠醛 $\rightarrow$ 富糠醛萃取液 + 抽余油；
3. 汽提：萃取液 $\rightarrow$ 糠醛（回萃取）+ 不含溶剂的 BTEX；
4. 精馏：BTEX $\rightarrow$ 苯、甲苯、乙苯、混合二甲苯；抽余油 $\rightarrow$ C5 馏分 + 石脑油（回裂解）。

环丁砜作为第二代溶剂（损耗更低、收率更高）要用丁二烯合成，放在第 4 张 C4 卡。

## 1. 行为

在 `PetrochemRecipes` 中新增 `btxExtraction()`：

- **第一步：糠醛制备**（`gt.recipe.bath`）
  - 输入：`4 × dust Wood` + `GenLiquid_SulfuricAcid (250 mB)`
  - 输出：`GenLiquid_Furfural (500 mB)` + `Dirty_Water (500 mB)`
  - 耗能与时长：24 EU/t，160 ticks
- **第二步：糠醛液液萃取**（`gt.recipe.mixer`，2 流体出）
  1. 裂解汽油：`GenLiquid_PyrolysisGasoline (2000)` + `GenLiquid_Furfural (1000)` $\rightarrow$ `GenLiquid_FurfuralExtract (2200)` + `GenLiquid_Raffinate (800)`；48 EU/t，160 ticks
  2. 重整油：`GenLiquid_Reformate (2000)` + `GenLiquid_Furfural (1000)` $\rightarrow$ `GenLiquid_FurfuralExtract (2400)` + `GenLiquid_Raffinate (600)`；48 EU/t，160 ticks
- **第三步：萃取液汽提回收糠醛**（`gt.recipe.distillery`，1 进 2 出）
  - 输入：`circuit(1)` + `GenLiquid_FurfuralExtract (2200 mB)`
  - 输出：`GenLiquid_Furfural (950 mB)` + `GenLiquid_BTEX (1200 mB)`（每批损耗 50 mB 糠醛，约 5%）
  - 耗能与时长：32 EU/t，120 ticks
- **第四步：BTEX 精馏**（`gt.recipe.distillationtower`）
  - 输入：`GenLiquid_BTEX (1000 mB)`
  - 输出：`GenLiquid_Benzene (350)` + `GenLiquid_Toluene (300)` + `GenLiquid_Ethylbenzene (80)` + `GenLiquid_Xylene (220)` + `Fuel (50)`（C9+ 重芳烃并入燃料油）
  - 耗能与时长：64 EU/t，160 ticks
- **第五步：BTEX 单方块早期蒸馏**（`gt.recipe.distillery`，低收率过渡）
  - 输入：`circuit(2)` + `GenLiquid_BTEX (1000 mB)`
  - 输出：`GenLiquid_Benzene (300)` + `GenLiquid_Toluene (250)`
  - 耗能与时长：24 EU/t，120 ticks
- **第六步：抽余油切 C5**（`gt.recipe.distillationtower`）
  - 输入：`GenLiquid_Raffinate (1000 mB)`
  - 输出：`GenLiquid_CFiveFraction (300)` + `GenLiquid_Naphtha (700)`（石脑油回蒸汽裂解炉）
  - 耗能与时长：48 EU/t，120 ticks

## 2. 明确不做

- 不做环丁砜合成与环丁砜萃取（第 4 张 C4 卡，等丁二烯有了再做）
- 不让糠醛做成 `keptFluidInputs` 不消耗（溶剂循环本身就是这张卡的玩法）
- 不区分裂解汽油与重整油抽出的 BTEX 组成（统一一种 `BTEX`，组成差异体现在萃取步的收率上）
- 不做苯/甲苯的下游（第 5、7 张卡）

## 3. 验收

- [ ] 糠醛制备、两条萃取、汽提、两条 BTEX 精馏、抽余油切 C5 共 7 条配方注册成功
- [ ] 游戏内跑一圈：1000 mB 糠醛进萃取，汽提后回收 950 mB，可直接回到萃取机继续用
- [ ] 抽余油切出的石脑油可回第 2 张卡的蒸汽裂解配方
- [ ] `.\scripts\build.ps1` 双加载器编译通过，服务端启动至 `Done` 无异常
