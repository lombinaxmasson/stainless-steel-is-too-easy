# Stainless Steel is Too Easy

GregTech 6 Community Edition 的化工向附属。模组 id `ssite`，依赖 GT6CE（模组 id `gt6ce`），支持 Forge 1.20.1（Java 17）与 NeoForge 1.21.1（Java 21）。仍在开发中，尚未发布。

## 内容

已有实现覆盖：

- **不锈钢与 AOD**：铬铁、铬、硅铁，坩埚化 AOD 转炉，以及 25 方块、六档材料的大型 AOD。
- **水净化与卤水**：五级水净化、海水淡化与卤水综合利用。
- **石化与高分子**：原油蒸馏、芳烃与 C1–C5 平台化学品、聚烯烃、工程塑料与合成橡胶，以及线缆、密封件和润滑油等工业挂载。
- **半导体与电子制造**：三氯氢硅提纯、单晶生长、切片抛光、光刻刻蚀、封装与金属化，以及按树脂分代的电路基板。
- **冶金与萃取**：VAR 电弧炉、钛钒氯化、锆铪分离、铌钽萃取、铜镍钴锍，以及稀土与铀钍的萃取、核纯和燃料处理。
- **供热与公用工程**：流化床工业供热与独立 HU 热网、化学链燃料床与再生床、天然气净化、气田、煤气化，以及小型炉与坩埚设备。

## 环境

Gradle 8.8 需要用 JDK 17 启动，NeoForge 编译再用 JDK 21；默认 `java` 是 25 时直接运行 `gradlew.bat` 会失败。Windows 下用仓库脚本，它会自己定位两个 JDK：

```powershell
.\scripts\build.ps1
```

也可以手动指定：

```powershell
$env:JAVA17_HOME = 'C:\path\to\jdk-17'
$env:JAVA21_HOME = 'C:\path\to\jdk-21'
$env:JAVA_HOME = $env:JAVA17_HOME
.\gradlew.bat compileBoth
```

## GT6CE SDK

编译期只依赖 GT6CE 的 addon SDK，不依赖其内部实现；SDK 不放进本仓库。默认在 `../GregTech6CommunityEdition/GregTech6` 查找，其他位置用 `-Pgt6ceDir=` 指定。先在 GregTech6 根目录构建 API 与 SDK：

```powershell
.\gradlew.bat :api:assemble :minecraft:1.20.1-forge:addonSdkJar :minecraft:1.21.1-neoforge:addonSdkJar
```

产物在 `api/common/build/libs/` 与各版本的 `build/addon-sdk/` 下，也可以复制进 `libs/`（优先于旁边的检出）。升级 GT6CE 时改 `gradle.properties` 的 `gt6ce_version` 与 `gt6ce_api_version`，再运行：

```powershell
.\scripts\update-gt.ps1
```

IDE 里可以把 `core/src/api/java` 和 `multiversion/src/api/java` 挂到 SDK jar 上当源码。

## 编译与检查

```powershell
.\scripts\build.ps1
.\scripts\build.ps1 :forge:build
.\scripts\build.ps1 :neoforge:build
```

`compileBoth` 会编译两端并运行共用的 JVM 检查，覆盖熔池反应、流化床、HU 运输、批次照射、光纤探头、高级晶圆工艺、粉末床、超净间、工程材料、天然气、气田、煤气化与化学链双床，以及不启动图形窗口的 OBJ 渲染顶点检查。

`compileBoth` 和两端的 `build` 还会生成材料用途报告并运行 `materialReportTest`。只更新报告可以执行：

```powershell
.\scripts\build.ps1 materialReport
# 只生成一端：
.\scripts\build.ps1 :neoforge:materialReport
```

报告分别在 `forge/build/reports/material-uses/` 与 `neoforge/build/reports/material-uses/`，包含可搜索的 `index.html`、表格 `materials.md` 和结构化数据 `catalog.json`。材料、形态与配方关系来自当前代码，可选的本地 `docs/material-uses.json` 只补充规划用途。报告不替代游戏内验证。

Forge 的可安装 jar 在 `forge/build/libs/`，NeoForge 的在 `neoforge/build/libs/`，安装时选和加载器对应的那一个。

## 开发运行

进游戏需要完整的 GT6CE 模组 jar，SDK 不能代替它。

Forge 发行 jar 是 SRG，开发运行会用 `fg.deobf` 转回 Mojang 名。在 GregTech6 里执行 `:minecraft:1.20.1-forge:distributionJar`；NeoForge jar 本身就是 Mojang 名，执行 `:minecraft:1.21.1-neoforge:build`。产物在对应版本的 `build/libs/`，也可以复制到 `libs/` 并保持文件名。

然后在本仓库：

```powershell
.\scripts\build.ps1 :forge:runClient
.\scripts\build.ps1 :neoforge:runClient
```

## 扩展点

两端加载器共用 `common/` 里的代码，GT6CE 的类只在编译期使用，不会打进这个 jar。

- `com.lombinaxmasson.ssite.chemistry.ChemistryRecipes` 是加配方的入口。
- 新材料用 `AddonMaterialContributor`，新机器用 `AddonMachineContributor`；类要 public、带 public 无参构造，并写进对应加载器的 `META-INF/services`。

## 许可

本模组使用 GNU Lesser General Public License 第 3 版或（由你选择）任何更新版本，SPDX 标识为 `LGPL-3.0-or-later`。`LICENSE` 与 `COPYING` 分别是 LGPL-3.0 与 GPL-3.0 原文，也放在 jar 的 `META-INF/licenses/` 下。
