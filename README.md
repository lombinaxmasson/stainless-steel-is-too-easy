# Stainless Steel is Too Easy

GregTech 6 Community Edition 的化工向附属。打算在 SuSy 的一些内容和自己的设计上改配方、加配方，并加一些机器。

这个仓库目前是空工程：Forge 1.20.1 和 NeoForge 1.21.1 都能单独编译，配方和机器还没开始写。

模组 id 是 `sstte`，依赖的 GT6CE 模组 id 是 `gt6ce`。两边加载器共用 `common/` 里的代码。`com.lombinaxmasson.sstte.chemistry.ChemistryRecipes` 是加配方的入口。新材料用 `AddonMaterialContributor`，新机器用 `AddonMachineContributor`，类要 public、带 public 无参构造，并写进对应加载器的 `META-INF/services`。GT6CE 的类只在编译期使用，不会打进这个 jar。

## 环境

Gradle 8.8 要用 JDK 17 启动。NeoForge 编译再用 JDK 21。本机如果默认 `java` 是 25，直接跑 `gradlew.bat` 会起不来。Windows 用仓库里的脚本，它会自己找这两个 JDK：

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

编译期只依赖 GT6CE 的 addon SDK，不依赖它的内部实现。SDK 不放进这个仓库。默认去旁边的检出找：`../GregTech6`。别的位置用 `-Pgt6ceDir=`。

在 GregTech6 根目录先打 SDK：

```powershell
.\gradlew.bat :multiversion:1.20.1-forge:addonSdkJar :multiversion:1.21.1-neoforge:addonSdkJar
```

打好之后可以复制进 `libs/`。`libs/` 优先于旁边的检出：

```powershell
.\scripts\update-gt.ps1
```

产物：

- `multiversion/versions/1.20.1-forge/build/addon-sdk/gt6ce-addon-sdk-1.20.1-forge-0.0.0.jar`
- `multiversion/versions/1.21.1-neoforge/build/addon-sdk/gt6ce-addon-sdk-1.21.1-neoforge-0.0.0.jar`

GT6CE 版本号变了就改 `gradle.properties` 里的 `gt6ce_version`，并改 `scripts/update-gt.ps1` 里的文件名。IDE 里可以把 `core/src/api/java` 和 `multiversion/src/api/java` 挂到 SDK jar 上当源码。

## 编译

```powershell
.\scripts\build.ps1
.\scripts\build.ps1 :forge:build
.\scripts\build.ps1 :neoforge:build
```

Forge 的可安装 jar 在 `forge/build/libs/`，NeoForge 的在 `neoforge/build/libs/`。装的时候选和加载器对应的那一个。

## 开发运行

进游戏需要完整的 GT6CE 模组 jar，SDK 不能代替它。

Forge 发行 jar 是 SRG，开发运行会用 `fg.deobf` 转回 Mojang 名。在 GregTech6 里执行：

```powershell
.\gradlew.bat :multiversion:1.20.1-forge:distributionJar
```

jar 在 `multiversion/versions/1.20.1-forge/build/libs/gt6ce-1.20.1-forge-0.0.0.jar`。

NeoForge jar 本身就是 Mojang 名：

```powershell
.\gradlew.bat :multiversion:1.21.1-neoforge:build
```

jar 在 `multiversion/versions/1.21.1-neoforge/build/libs/gt6ce-neoforge-1.21.1-0.0.0.jar`。

然后在本仓库：

```powershell
.\scripts\build.ps1 :forge:runClient
.\scripts\build.ps1 :neoforge:runClient
```

这两个完整 jar 也可以复制到 `libs/`，文件名保持不变。
