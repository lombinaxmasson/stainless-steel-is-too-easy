# Stainless Steel is Too Easy

GregTech 6 Community Edition 的化工向附属。模组 id 是 `ssite`，依赖 GT6CE（模组 id `gt6ce`），支持 Forge 1.20.1 与 NeoForge 1.21.1。仍在开发中，尚未发布。

## 内容

已有实现覆盖：

- **不锈钢与 AOD**：铬铁、铬、硅铁，坩埚化 AOD 转炉，以及 25 方块、六档材料的大型 AOD。
- **水净化与卤水**：五级水净化、海水淡化与卤水综合利用。
- **石化与高分子**：原油蒸馏、芳烃与 C1–C5 平台化学品、聚烯烃、工程塑料与合成橡胶。
- **工业挂载**：合成橡胶与 PVC/Teflon 绝缘线缆、泵与喷嘴密封件，以及 PAO/PIB 调合润滑油接入 GT6CE 的切割与锯切冷却。
- **半导体与电子制造**：三氯氢硅提纯、单晶生长、切片与抛光；光刻、刻蚀、掺杂与金属化形成加工片；按树脂分代的电路基板。
- **活泼与难熔金属**：VAR 电弧炉、钛钒氯化、锆铪分离、铌钽萃取。
- **稀土与铀钍**：P204 / Aliquat 336 分级萃取、TBP 核纯与黄饼；钍燃料压坯/芯块，以及使用后棒拆解、钍铀回收与裂变产物固封。
- **基础金属**：铜镍钴锍前段。
- **供热与设备**：流化床工业供热及其独立 HU 热运输网络、化学链燃料床/再生床、小型炉与坩埚设备。

配方、材料和进度以代码为准。本模组不包含 GT6 本体，单独安装不会生效。

美术资产：[大型燃气涡轮 LGT–01](art/large_gas_turbine/README.md) 提供适配宿主 3×3×4 结构的 OBJ、四档金属配色、独立转子染色，以及带启停旋转、温度辉光的离线剖视预览，目前尚未接入游戏渲染。

[批次照射反应堆 IR–03](art/irradiation_reactor/README.md) 提供 HBM 风格原创 OBJ、像素图集与离线动画预览，严格采用 4×4 占地、4 格高度，装料、控制、KU 和 HU 四个接口对齐方块面中心。包含滑升屏蔽门、启停联锁、转台加减速和舱内工作灯，尚未接入游戏结构或渲染。生成和检查命令见该目录说明。

[光刻机 LS–01](art/lithography_machine/README.md) 提供 HBM 配色、粗野主义厚壁外观的原创 OBJ、512×256 像素图集与离线运行动画，宽 5、深 4、高 4 格。EU 接口在正面主控下方，流体接口集中在右侧；支持单片晶圆装载、机械臂转运、曝光扫描、送出及完成本片后停机，尚未接入游戏结构或渲染。生成和检查命令见该目录说明。

[U-10 燃料床 FR–10 与再生床 AR–10](art/chemical_looping_beds/README.md) 提供 HBM 风格 OBJ、像素图集与离线预览。两台各占 1×1 格，燃料床为 1.5 格高宽筒，再生床为 3 格高细提升管。两机流体口为小圆管，KU 接口按机壳配色，HU 为烟道口。燃料床投料与点火同口；已作为 `ssite:fuel_reactor` 与 `ssite:air_reactor` 接入批次行为、HU 热网与大 OBJ 渲染，两台机器均不打开 GUI。生成和检查命令见目录说明。

[液流堆 RFB–15](art/flow_battery/README.md) 提供 HBM 风格的充放电整机 OBJ、512×256 像素图集与离线剖视预览，宽 5、深 3、高 3 格。双回路缓冲筒、循环泵、叠片电堆和双向电力柜独立分组，背面预留外接储罐与 EU 双向接口；尚未接入游戏结构或充放电行为。生成和检查命令见该目录说明。

## 环境

Gradle 8.8 要用 JDK 17 启动，NeoForge 编译再用 JDK 21。本机如果默认 `java` 是 25，直接跑 `gradlew.bat` 会起不来。Windows 用仓库里的脚本，它会自己找这两个 JDK：

```powershell
.\scripts\build.ps1
```

也可以自己设好再编译：

```powershell
$env:JAVA17_HOME = 'C:\path\to\jdk-17'
$env:JAVA21_HOME = 'C:\path\to\jdk-21'
$env:JAVA_HOME = $env:JAVA17_HOME
.\gradlew.bat compileBoth
```

## GT6CE SDK

编译期只依赖 GT6CE 的 addon SDK，不依赖它的内部实现。SDK 不放进这个仓库。默认去 `../GregTech6CommunityEdition/GregTech6` 找，别的位置用 `-Pgt6ceDir=` 指定。

在 GregTech6 根目录先打 API 与 SDK：

```powershell
.\gradlew.bat :api:assemble :minecraft:1.20.1-forge:addonSdkJar :minecraft:1.21.1-neoforge:addonSdkJar
```

产物：

- `api/common/build/libs/gt6ce-api-1.3.0.jar`
- `minecraft/versions/1.20.1-forge/build/addon-sdk/gt6ce-addon-sdk-1.20.1-forge-1.3.0.jar`
- `minecraft/versions/1.21.1-neoforge/build/addon-sdk/gt6ce-addon-sdk-1.21.1-neoforge-1.3.0.jar`

也可以把它们复制进 `libs/`。`libs/` 优先于旁边的检出：

```powershell
.\scripts\update-gt.ps1
```

脚本从 `gradle.properties` 读取 `gt6ce_version` 与 `gt6ce_api_version`，升级 GT6CE 时改这两项即可。IDE 里可以把 `core/src/api/java` 和 `multiversion/src/api/java` 挂到 SDK jar 上当源码。

## 编译与检查

```powershell
.\scripts\build.ps1
.\scripts\build.ps1 :forge:build
.\scripts\build.ps1 :neoforge:build
```

`compileBoth` 会编译两端并运行共用的 JVM 检查：熔池反应与坩埚行为（`unitTest`）、流化床燃料与热账（`fluidizedBedTest`）、HU 运输拓扑（`heatTransportTest`）、批次照射堆的批次与热量账（`irradiationReactorTest`）、光纤探头的目标/失联/完成脉冲状态机（`fiberProbeTest`）、离子注入与电子束批次账（`advancedProcessTest`）、粉末床的预留/气体/双输出/并行批次账（`powderBedTest`）、超净间房间与环境规则（`processEnvironmentTest`）、工程陶瓷/碳碳/烧蚀状态链与 P-01 高负荷批次契约（`engineeringMaterialTest`）、天然气净化闭环与管输气燃料/排气账（`utilityTest`）、气田计划与共享抽取预算（`gasFieldTest`）、煤气化器的预留/暂停/完成转换（`coalGasifierTest`）、化学链燃料床/再生床的载体、CO₂/水排空与磨损账（`chemicalLoopingTest`），以及不启动图形窗口的 OBJ 渲染顶点检查（`furnaceRenderTest`、`fluidizedBedRenderTest`、`chemicalLoopingRenderTest`）。

`compileBoth` 和两端的 `build` 还会生成材料用途报告，并运行 `materialReportTest`。只更新报告可以执行：

```powershell
.\scripts\build.ps1 materialReport
# 只生成一端：
.\scripts\build.ps1 :neoforge:materialReport
```

报告分别在 `forge/build/reports/material-uses/` 与 `neoforge/build/reports/material-uses/`，包含可搜索的 `index.html`、表格 `materials.md` 和结构化数据 `catalog.json`。材料、形态、配方与实际引用关系来自当前代码；可选的本地 `docs/material-uses.json` 只补充规划用途。报告区分同材料成型、催化剂、条件配方和行为展示行，不把“本次扫描未发现用途”当成材料在整个游戏中没有用途。宿主自动生成配方、其他模组与机器 tick 行为未全量扫描，生成报告不替代游戏内验证。

Forge 的可安装 jar 在 `forge/build/libs/`，NeoForge 的在 `neoforge/build/libs/`，安装时选和加载器对应的那一个。

## 开发运行

进游戏需要完整的 GT6CE 模组 jar，SDK 不能代替它。

Forge 发行 jar 是 SRG，开发运行会用 `fg.deobf` 转回 Mojang 名。在 GregTech6 里执行：

```powershell
.\gradlew.bat :minecraft:1.20.1-forge:distributionJar
```

jar 在 `minecraft/versions/1.20.1-forge/build/libs/gt6ce-1.20.1-forge-1.0.4.jar`。

NeoForge jar 本身就是 Mojang 名：

```powershell
.\gradlew.bat :minecraft:1.21.1-neoforge:build
```

jar 在 `minecraft/versions/1.21.1-neoforge/build/libs/gt6ce-neoforge-1.21.1-1.0.4.jar`。

然后在本仓库：

```powershell
.\scripts\build.ps1 :forge:runClient
.\scripts\build.ps1 :neoforge:runClient
```

这两个完整 jar 也可以复制到 `libs/`，文件名保持不变。

## 扩展点

两端加载器共用 `common/` 里的代码，GT6CE 的类只在编译期使用，不会打进这个 jar。

- `com.lombinaxmasson.ssite.chemistry.ChemistryRecipes` 是加配方的入口。
- 新材料用 `AddonMaterialContributor`，新机器用 `AddonMachineContributor`；类要 public、带 public 无参构造，并写进对应加载器的 `META-INF/services`。

## 许可

本模组使用 GNU Lesser General Public License 第 3 版或（由你选择）任何更新版本。`LICENSE` 是 LGPL-3.0 原文，`COPYING` 是它引用的 GPL-3.0 原文。两份原文也放在 jar 的 `META-INF/licenses/` 下。模组列表里的 SPDX 标识是 `LGPL-3.0-or-later`。
