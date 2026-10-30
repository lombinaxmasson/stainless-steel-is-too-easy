# Stainless Steel is Too Easy

GregTech 6 Community Edition 的化工向附属。模组 id 是 `ssite`，依赖 GT6CE（模组 id `gt6ce`），支持 Forge 1.20.1 与 NeoForge 1.21.1。仍在开发中，尚未发布。

## 内容

已有实现覆盖：

- **不锈钢与 AOD**：铬铁、铬、硅铁，坩埚化 AOD 转炉，以及 25 方块、六档材料的大型 AOD。
- **水净化与卤水**：五级水净化、海水淡化与卤水综合利用。
- **石化与高分子**：原油蒸馏、芳烃与 C1–C5 平台化学品、聚烯烃、工程塑料与合成橡胶。
- **活泼与难熔金属**：VAR 电弧炉、钛钒氯化、锆铪分离、铌钽萃取。
- **稀土与铀钍**：P204 / Aliquat 336 分级萃取、TBP 核纯与黄饼。
- **基础金属**：铜镍钴锍前段。
- **供热与设备**：流化床工业供热及其独立 HU 热运输网络、小型炉与坩埚设备。

配方、材料和进度以代码为准。本模组不包含 GT6 本体，单独安装不会生效。

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

- `api/common/build/libs/gt6ce-api-1.0.0.jar`
- `minecraft/versions/1.20.1-forge/build/addon-sdk/gt6ce-addon-sdk-1.20.1-forge-1.0.0.jar`
- `minecraft/versions/1.21.1-neoforge/build/addon-sdk/gt6ce-addon-sdk-1.21.1-neoforge-1.0.0.jar`

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

`compileBoth` 会编译两端并运行共用的 JVM 检查：熔池反应与坩埚行为（`unitTest`）、流化床燃料与热账（`fluidizedBedTest`）、HU 运输拓扑（`heatTransportTest`），以及不启动图形窗口的 OBJ 渲染顶点检查（`furnaceRenderTest`、`fluidizedBedRenderTest`）。

Forge 的可安装 jar 在 `forge/build/libs/`，NeoForge 的在 `neoforge/build/libs/`，安装时选和加载器对应的那一个。

## 开发运行

进游戏需要完整的 GT6CE 模组 jar，SDK 不能代替它。

Forge 发行 jar 是 SRG，开发运行会用 `fg.deobf` 转回 Mojang 名。在 GregTech6 里执行：

```powershell
.\gradlew.bat :minecraft:1.20.1-forge:distributionJar
```

jar 在 `minecraft/versions/1.20.1-forge/build/libs/gt6ce-1.20.1-forge-1.0.2.jar`。

NeoForge jar 本身就是 Mojang 名：

```powershell
.\gradlew.bat :minecraft:1.21.1-neoforge:build
```

jar 在 `minecraft/versions/1.21.1-neoforge/build/libs/gt6ce-neoforge-1.21.1-1.0.2.jar`。

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
