# Stainless Steel is Too Easy

GregTech 6 Community Edition 的化工向附属。模组 id `ssite`，依赖 GT6CE（模组 id `gt6ce`），支持 Forge 1.20.1（Java 17）与 NeoForge 1.21.1（Java 21）。当前为 alpha 预览版 `0.1.0-alpha.1`，配方、注册 ID 与接口仍可能变动，不保证存档兼容。

## 内容

已有实现覆盖：

- **不锈钢与 AOD**：铬铁、铬、硅铁，坩埚化 AOD 转炉，以及 25 方块、六档材料的大型 AOD。
- **水净化与卤水**：五级水净化、海水淡化与卤水综合利用。
- **石化与高分子**：原油蒸馏、芳烃与 C1–C5 平台化学品、聚烯烃、工程塑料与合成橡胶，以及线缆、密封件和润滑油等工业挂载。
- **半导体与电子制造**：三氯氢硅提纯、单晶生长、切片抛光、光刻刻蚀、封装与金属化，以及按树脂分代的电路基板。
- **冶金与萃取**：VAR 电弧炉、钛钒氯化、锆铪分离、铌钽萃取、铜镍钴锍，以及稀土与铀钍的萃取、核纯和燃料处理。
- **供热与公用工程**：流化床工业供热与独立 HU 热网、化学链燃料床与再生床、天然气净化、气田、煤气化，以及小型炉与坩埚设备。
- **大型燃气涡轮外观**：GT6CE 四档大型燃气涡轮成型后显示低模机组，运行时转子和后联轴器同轴旋转。见[模型与游戏外观说明](art/large_gas_turbine/README.md)。
- **风力发电**：不锈钢机头与可装拆的玻纤风轮，受选址和天气影响，最高 8,192 RU/t，背面可接 GT6CE 发电机。
- **唱片与音乐介质**：黑胶压盘与 CD-R 分层刻录，支持导入自己的 OGG 曲目；音频保存在服务器曲库，附近玩家自动下载缓存并同步播放。

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

`gasTurbineRenderTest` 检查大型燃气涡轮的六向定位、包围盒、顶点流、转动分组、机壳材质和启停插值，也检查燃气涡轮与照射反应堆的 VBO 合批，已接入两端 `check` 与 `compileBoth`。

两端的 `windTurbineTest` 检查出力曲线、风暴刹车、风场连续性、风轮重叠和四个朝向的渲染，已接入 `check` 与 `compileBoth`。

唱片的 `recordIndustryTest`、`recordDynamicRecipeTest`、`recordMediaTest`、`recordPlaybackTest` 也接入两端 `check` 与 `compileBoth`，分别检查制造链、曲目选择、音频协议/持久化和 PCM 播放游标。它们不替代真实音频与双客户端联机验收。

`compileBoth` 和两端的 `build` 还会生成材料用途报告和配方覆盖审计，并运行 `materialReportTest`。只更新材料报告可以执行：

```powershell
.\scripts\build.ps1 materialReport
# 只生成一端：
.\scripts\build.ps1 :neoforge:materialReport
```

报告分别在 `forge/build/reports/material-uses/` 与 `neoforge/build/reports/material-uses/`，包含可搜索的 `index.html`、表格 `materials.md` 和结构化数据 `catalog.json`。材料、形态与配方关系来自当前代码，可选的本地 `docs/material-uses.json` 只补充规划用途。报告不替代游戏内验证。

配方覆盖审计复用同一批声明和合成 JSON，列出终端产物、外部供给输入、展示行、重复声明与结构异常。只更新审计可以执行：

```powershell
.\scripts\build.ps1 recipeAudit
# 只生成一端：
.\scripts\build.ps1 :neoforge:recipeAudit
```

审计分别在 `forge/build/reports/recipe-audit/` 与 `neoforge/build/reports/recipe-audit/`，包含 `recipe-audit.md` 和 `recipe-audit.json`。它不扫描宿主自动生成的配方，因此不能判断本附属配方是否与宿主配方重复；自定义机器的运行逻辑也可能超出展示行。审计不启动 Minecraft，不替代游戏内验收。

Forge 的可安装 jar 在 `forge/build/libs/`，NeoForge 的在 `neoforge/build/libs/`，安装时选和加载器对应的那一个。

## 开发运行

进游戏需要完整的 GT6CE 模组 jar，SDK 不能代替它。

Forge 发行 jar 是 SRG，开发运行会用 `fg.deobf` 转回 Mojang 名。在 GregTech6 里执行 `:minecraft:1.20.1-forge:distributionJar`；NeoForge jar 本身就是 Mojang 名，执行 `:minecraft:1.21.1-neoforge:build`。产物在对应版本的 `build/libs/`，也可以复制到 `libs/` 并保持文件名。

然后在本仓库：

```powershell
.\scripts\build.ps1 :forge:runClient
.\scripts\build.ps1 :neoforge:runClient
```

## 风力发电

用 GT6CE 压机制作风力机头和玻纤风轮，两条配方均消耗 128 KU/t。机头需要不锈钢机器外壳和两根长不锈钢杆。风轮需要 16 块玻纤增强环氧板和一个不锈钢转子。

在主世界露天放置机头，给正面约 10.4 格直径的风轮留出空间。手持玻纤风轮右键装入，用扳手取回。空手右键查看当前风速、预计机械出力和运行状态。背面每次送出一个 8,192 RU 包，风况决定送包频率。接普通 IV 发电机时，额定出力为 5,632 EU/t。

风况随地区与天气变化，高度加成在海平面以上 64 格达到上限。前方障碍会削弱进风，扫掠区被挡、风轮互相重叠或风暴刹车时暂停出力。支架可用普通方块搭建。

## 自定义唱片

1. 准备**单声道 OGG Vorbis** 音频（8–96 kHz、不超过 16 MiB、最长 10 分钟），放入客户端游戏目录的 `config/ssite/music/`。首版不直接读取 MP3、WAV 或网络链接。
2. 用纸、铜箔和编号 1 的编程电路，在压机制作曲目模板。主手持有模板，在进入世界后执行客户端命令：

   ```text
   /ssite-music import my_song.ogg
   ```

   文件名决定曲名。上传并通过服务器校验后，曲目会进入当前世界的曲库；此时主手仍持有模板就会自动绑定。命令只读取导入目录里的直接文件，不接收任意目录路径。
3. 已有曲目可以用 `/ssite music list [页码]` 查看，点击条目准备 `/ssite music bind <曲目ID>`，主手持有模板后执行即可重新绑定。
4. 黑胶线依次将模板的曲目传到刻纹母盘、镍压模和成品；CD-R 用模板逐张刻录。模板、刻针和压模按配方保留。空白模板不能驱动这些加工工序。
5. 将成品放入原版唱片机。64 格内的玩家自动获取缺少的音频，缓存后播放；后来进入范围会追上服务端进度。拔盘、拆机、离开范围或断线后停止。声音受“唱片机／音符盒”音量控制，首次下载需要时间；服务器重启或唱片机区块重新加载后从头播放。

曲目文件按 SHA-256 标识，改文件名不会生成另一份音频。服务器曲库位于世界目录 `ssite/music/`，容量上限 512 MiB；客户端缓存位于 `config/ssite/music-cache/`，按使用时间淘汰，最多 256 MiB 或 64 首。上传最多同时 4 个、每玩家一次，每次开始间隔至少 10 秒；下载只允许正在附近播放的曲目，按玩家分片限速。OP 可用 `/ssite music remove <曲目ID>` 删除曲目，之后的配方查询和播放会拒绝该曲目；已开工或同 tick 已缓存的加工批次可能完成，但产物无法播放或继续加工。

保留已有 `ssite:vinyl_record`、`ssite:cd_record`、`ssite:record_template_trackone` 等注册 ID；模板 ID 中的旧名称不再绑定固定曲目。旧的无曲目数据模板需要重新绑定，无数据成品需要重新制造。音频不写入物品或区块 NBT，也不随模组发行。上传共享前需确认拥有相应的音频传播授权。

双端编译和 JVM 检查不能代替联机实测；导入、实际机器加工、自动化、声音、红石与 Mixin 的实机验收范围见本地 M8 文档。

## 美术资源

[工业贴图风格参考](art/texture_style_reference/README.md)收录三套草案、UV 重排的具体画法，以及方块面和 OBJ 图集的生成入口。

## 扩展点

两端加载器共用 `common/` 里的代码，GT6CE 的类只在编译期使用，不会打进这个 jar。

- `com.lombinaxmasson.ssite.chemistry.ChemistryRecipes` 是加配方的入口。
- 新材料用 `AddonMaterialContributor`，新机器用 `AddonMachineContributor`；类要 public、带 public 无参构造，并写进对应加载器的 `META-INF/services`。

## 许可

本模组使用 GNU Lesser General Public License 第 3 版或任何更新版本，SPDX 标识为 `LGPL-3.0-or-later`。`LICENSE` 与 `COPYING` 分别是 LGPL-3.0 与 GPL-3.0 原文，也放在 jar 的 `META-INF/licenses/` 下。
