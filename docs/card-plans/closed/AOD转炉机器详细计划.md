# AOD 转炉机器详细计划

> 计划 slug：`m1/aod-converter-machine`
> 状态：关闭。本文件位于 `docs/card-plans/closed/`。前置是 [`不锈钢冶金材料与 316L 管道`](不锈钢冶金材料详细计划.md)。
> 性质：注册专属的 AOD 氩氧脱碳转炉机器与配方表，承接高碳不锈钢脱碳与 316L 冶炼。

```text
lane            = closed
slug            = m1/aod-converter-machine
owned_paths     = common/src/main/java/com/lombinaxmasson/ssite/machine/SsiteMachines.java
                  common/src/main/resources/assets/ssite/blockstates/aod_converter.json
                  common/src/main/resources/assets/ssite/models/block/machine/basic/aod_converter*.json
                  common/src/main/resources/assets/ssite/models/item/aod_converter.json
                  common/src/main/resources/assets/ssite/textures/gui/recipes/aod.png
                  common/src/main/resources/assets/ssite/lang/en_us.json
                  common/src/main/resources/assets/ssite/lang/zh_cn.json
                  forge/src/main/resources/data/ssite/recipes/aod_converter.json
                  neoforge/src/main/resources/data/ssite/recipe/aod_converter.json
depends_on      = m1/metallurgy-materials
close_target    = ssite:aod_converter 与 ssite.recipe.aod 注册完成，JEI/EMI 可查、模型贴图与合成配方齐全
```

## 0. 它解决什么

AOD 精炼需要同时投入高碳不锈钢、硅铁脱氧剂、生石灰/萤石造渣剂（或钼粉）以及氧气 + 稀释气体（空气/氮气/氩气），并产出不锈钢与精炼炉渣/尾气。专属的 `ssite:aod_converter` 和 `ssite.recipe.aod` 让玩家拥有一台标志性的冶金转炉，而不是把所有炼钢配方塞进普通搅拌机。

## 1. 行为

- 配方表 `ssite.recipe.aod`（在 `SsiteMachines.recipeTables()` 中声明）：
  - `title("AOD Refining")`
  - `gui("ssite:textures/gui/machines/aod")`（对应磁盘 `assets/ssite/textures/gui/recipes/aod.png`）
  - 槽位：`inputItems(4)`、`outputItems(2)`、`minimalInputItems(1)`、`inputFluids(2)`、`outputFluids(1)`、`minimalInputFluids(1)`、`minimalInputs(2)`、`needsOutputs(true)`。
- 机器 `ssite:aod_converter`（在 `SsiteMachines.addonMachines()` 中声明）：
  - 外壳：`MachineCasing.builder("aod_converter", "Invar").machineType("aod_converter").tier(2).build()`（用殷钢 `Invar` 外壳，MV/Tier 2 时代即可在拥有不锈钢之前造出来！）
  - 能源：`EnergyInput.builder("HU", 64).build()`（接收热能 HU，契合高温冶金转炉定位；燃烧室即可驱动）
  - 绑定配方表：`recipeMap("ssite.recipe.aod").basic().build()`
- 资源与合成表：
  - 方块状态 `assets/ssite/blockstates/aod_converter.json`、方块模型 `block/machine/basic/aod_converter.json`（及 `_running`、`_active`）与物品模型 `models/item/aod_converter.json`。
  - 中英文译名：`block.ssite.aod_converter`（英文 `AOD Converter`，中文 `AOD 精炼转炉`）。
  - 合成配方：使用殷钢机器外壳/双层板、高铝耐火砖/坩埚部件与铜/钢流体管合成，确保不依赖 `StainlessSteel` 就能造出第一台转炉。

## 2. 明确不做

- 不要求用 `StainlessSteel` 合成 `aod_converter`（否则又会死锁）
- 初版先做成单方块配方机器（`basic()`），不强制搭几十格的多方块结构，降低 M1 美工与摆结构门槛

## 3. 验收

- [ ] `ssite:aod_converter` 方块与物品在创造栏和命令中可正常获取、放置，模型无紫黑块
- [ ] 打开界面或查看 JEI/EMI 能正常显示 `ssite.recipe.aod` 的 4 物品输入 + 2 流体输入 → 2 物品输出 + 1 流体输出
- [x] 合成表只使用不锈钢之前的材料（殷钢、钢、铜等）
- [x] `.\scripts\build.ps1` 双加载器编译通过

NeoForge 1.21.1 专用服（2026-10-22）能进到 `Done`，`GT addon API 0 ready: [ssite]`，合成表 `Loaded 52360 recipes`，没有解析错误。合成用的是殷钢机器外壳、殷钢坩埚、殷钢双板、耐火砖、铜管和钢管。方块状态是 `facing` / `lit` / `running`，模型贴原版高炉侧面和正面。配方表槽位按声明是 4 物品 + 2 流体进、2 物品 + 1 流体出。没有开客户端，创造栏、放置后的模型和 JEI/EMI 分页没看过。
