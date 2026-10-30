# Agent 工作指南

本文件适用于整个仓库。用户的明确要求优先。文档以中文维护，代码标识符沿用项目现有英文命名。

## 渐进阅读

1. [README](README.md)：公开入口，内容概览、环境、GT6CE SDK、构建与开发运行。
2. `docs/README.md`：本地文档导航。`docs/` 已忽略，只存在于工作副本。
3. `docs/architecture.md`：代码分层、注册路径、平台边界与验证方式。
4. `docs/card-plans/总览.md`：当前进度与计划卡状态，同样只在本地维护。

## 项目事实

- 本仓库是 GregTech 6 Community Edition 的化工向附属，模组 ID `ssite`，依赖 GT6CE（模组 ID `gt6ce`）。同时支持 Forge 1.20.1（Java 17）与 NeoForge 1.21.1（Java 21）。
- `common/` 是两端共用的唯一实现，不单独打包；`forge/`、`neoforge/` 只放加载器入口、创造标签页和同包名的平台适配类。
- 两个加载器各自把 `common/src/main/java`、`common/src/main/resources` 和 `common/src/test/java` 加进源码集合；`gradle/` 下的脚本由两端分别 `apply`，只在一端注册任务会漏掉另一端。
- GT6CE 的类只在编译期可见，不会打进本模组的 jar。新增对 GT6CE 的调用要同时核对两端的 API jar。
- `gt6ce_version` 是宿主运行时版本，`gt6ce_api_version` 是 addon SDK 版本，两者独立升级；更新 SDK 不代表运行时已经更新。
- `art/` 下的 OBJ、MTL、atlas 与预览由 `scripts/models/` 生成，生成器是唯一输入，不要手改产物。
- 服务端与世界行为以 Java 实现为准；`docs/` 里的描述可能落后于代码，冲突时以代码和 `git log` 为准。

## 工作方式

- 开工先检查 `git status --short`，保留已有改动、未跟踪文件和用户资产。无初始提交或文件未跟踪时，不能认定它们是本次新建。
- 先搜索同类实现、调用方、注册入口和两平台差异，再局部修改。优先 `rg`；未安装时使用 `grep`、`find`。
- 按所在文件的缩进、命名和组织方式编写，避免顺手批量格式化、搬包或重写无关实现。
- 区分当前实现和建议边界。历史命名不是行为完整或版本一致的证明。
- 不编辑 `build/`、`run*/` 内的生成副本；查找真实输入。带生成声明的已提交资源先查生成器及原始输入，缺失时说明缺口，不臆造生成流程。
- 不顺手变更注册 ID、序列化键、NBT 格式、网络协议、配方单位或初始化顺序。必要修改须说明兼容影响与迁移方式。
- 保持客户端与服务端隔离；Jade 等可选集成不能让未安装该模组的基础游戏启动失败。网络输入须验证，日志不得泄露凭据或私密配置。
- 不吞异常或关闭检查来获得“通过”，不把编译成功写成游戏行为已验证。
- 用户未要求时，不提交、推送、发布或操作真实服务器；本地构建和检查可直接完成。不要替用户接受 EULA。

## 注释

- 代码注释用英文，写意图、约束和与 GT6 的实质差异，不写来源台账，也不写“本批次”“之前是”这类历史；历史交给 `git log`。
- 文档（README、`docs/`、`art/*/README.md`）用中文。
- 本仓库代码引用用 `{@link Foo}`、`{@link #member}`（JavaDoc）或类名、方法名（`//`），不写行号。
- 引用上游 GT6、GT6CE 或 SuSy 时写 `{@code Foo.java:123}`、`{@code Foo.bar(...)}` 这类可搜索形式，并注明所依据的版本；纯数据表可保留逐行出处。
- 只写出处的单行注释应改写成含义或删除。

## 文件必须放对地方

| 内容 | 位置 |
| --- | --- |
| 生产 Java 与实际打包资源 | `common/src/main/java`、`common/src/main/resources`；平台独有的放 `forge/src/main/...`、`neoforge/src/main/...` |
| 可重复运行的自动测试 | `common/src/test/java`，任务在 `gradle/` 里注册并由两端 `apply`；先确认任务名与执行入口 |
| 可复用且随代码演进的开发脚本 | `scripts/`（`build.ps1`、`update-gt.ps1`） |
| 一次性模型生成与检查脚本 | `scripts/models/`，已忽略 |
| 一次性联调服务器与世界夹具 | `scripts/testing/`、`neoforge/src/smokeTest/`，已忽略 |
| 计划、架构、玩法与灵感笔记 | `docs/`，已忽略，只在本地维护 |
| 材料、流体、机器、语言与贴图 | `common/src/main/resources/...`，随模组分发 |

**不要往任何 `src/` 放任务笔记、agent 交接、日志、截图、临时探针或一次性迁移脚本。** 构建产物在 `build/`、`run*/`、`libs/*.jar`，都不入库。

忽略规则不是保密措施，凭据不得进入版本库或公共记录。

## 验证与交付

- 文档/忽略规则：检查相对链接、命令与配置一致性，用 `git check-ignore -v` 验证规则；无需启动游戏或全量构建。
- 编译与共用检查：`.\scripts\build.ps1 compileBoth`。脚本自己定位 JDK 17/21；Gradle 8.8 不能由 JDK 25 启动。
- 完整构建：`.\scripts\build.ps1 build`；单端用 `:forge:build`、`:neoforge:build`。
- 纯 JVM 检查由 `unitTest`、`fluidizedBedTest`、`heatTransportTest`、`furnaceRenderTest`、`fluidizedBedRenderTest` 覆盖，均已接入各端 `check`。
- 世界、机器 tick、交互、渲染、Mixin、Jade 或可选集成还需对应游戏内验证，记录未覆盖的平台与场景。
- 新增命令、参数或注册步骤同步 README 与相关文档；新增包职责同步 `docs/architecture.md`。
- 结束时报告改动、验证命令与结果、未验证事项。受环境阻塞时报告具体原因，不宣称成功。
