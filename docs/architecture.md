# 架构说明

本说明对应 2026-10-25 的代码结构。工程由两个加载器子项目编译同一份 `common/` 源码，`common/` 不是单独打包的运行时模块。GT6CE addon SDK 作为编译依赖，不打进附属 jar；开发运行另需完整 GT6CE jar。

## 目录与边界

| 位置 | 职责 |
|---|---|
| `common/.../SsiteAddon.java` | 共用注册入口与 addon 标识 |
| `common/.../material/` | 材料、形态、矿脉、坩埚反应与熔炼目标移除声明 |
| `common/.../chemistry/` | 按领域组织的配方与共用配方构建工具 |
| `common/.../machine/` | 机器声明、熔池行为、AOD/VAR 工艺与炉盖结构 |
| `forge/src/main/java/`、`neoforge/src/main/java/` | 加载器入口、创造标签页，以及同包名的平台适配类 |
| `common/src/main/resources/META-INF/services/` | GT6CE 加载材料、机器、行为和结构零件的 SPI 清单 |
| `common/src/main/resources/data/ssite/recipes/` | 原版合成配方和 GT6CE 机器配方移除规则 |
| `common/src/main/resources/assets/` | 双语、本地模型、贴图和 EMI 别名 |
| `common/src/test/java/` | 两端复用的 JVM 熔池测试 |
| `gradle/gt6ce-sdk.gradle`、`scripts/` | SDK/运行时定位、JDK 选择与本机构建辅助 |

表中的 `common/.../` 均指 `common/src/main/java/com/lombinaxmasson/ssite/`。

## 注册与配方

两个加载器的 Mod 构造器调用 `SsiteAddon.register()`。GT6CE 在材料开放阶段通过 SPI 读取 `SsiteMaterials`，在自身配方就绪后调用 addon 的 `onRecipesReady`，进入 `ChemistryRecipes.add()`。

`ChemistryRecipes` 负责 API 版本检查、注册顺序、共用 `Row` 构建器与缺失/拒绝诊断。实际工艺按下面的顺序调用，保持现有顺序以降低配方碰撞与行为变化的风险：

| 配方模块 | 领域 | 材料声明 |
|---|---|---|
| `StainlessSteelRecipes` | M1 铬铁、铬、硅铁、AOD 展示行与炉渣 | `SsiteMaterials` |
| `WaterRecipes` | M2 净水耗材、五级水净化与卤水 | `SsiteMaterials` |
| `PetrochemRecipes` | M3 炼油与单体分离 | `SsiteMaterials` |
| `PolymerRecipes` | M4 高分子、平台化学品与萃取剂 | `PolymerMaterials` |
| `MetallurgyRecipes` | M5 活泼与难熔金属 | `MetallurgyMaterials` |
| `RareEarthRecipes` | M6 稀土 | `RareEarthMaterials` |
| `NuclearRecipes` | M6 铀钍前段 | `NuclearMaterials` |
| `BaseMetalRecipes` | M7 铜镍钴锍前段 | `BaseMetalMaterials` |

各材料模块由 `SsiteMaterials` 汇总。M1–M3 材料仍直接定义在汇总类，这是后续可整理项。

配方通过 `ChemistryRecipes.Row` 构建，交给对应平台的 `FluidBridge` 转换流体并提交给 SDK。缺材料与配方拒绝会集中记录，注册结束抛出异常，避免只加载一部分内容却正常启动。

## 机器行为与平台适配

`MoltenPool` 已抽出 AOD 与 VAR 共用的投料、熔化、温度、浇铸和炉盖能力。`AodConverterCore` 处理 KU 吹炼及炉体熔毁门槛，`VarFurnaceCore` 处理电弧加热和氩气下重熔；工艺差异留在各自实现里。

两端各有同包名的 `ArgonCrucibleBehavior` 与 `FluidBridge`，分别适配流体、方向和平台接口。它们由各自子项目编译，不能同时移入 `common/`。调整共用逻辑时，需检查两份适配实现。

大型 AOD 使用 `LargeAodStructure` 和 `LargeAodCore`。浇铸控制器位于 OBJ 的 (0,1,−2)，底部九个普通结构端口以 `ENERGY_INPUT` 角色转发 HU（行为机器宿主不支持用 `HEAT_INPUT` 角色转发）；两个功能端口各自储存气体。炉盖字段 `open` 与 `lid.mode` 负责手动/红石开盖，已投料后的熔炼不受盖子开关阻断。过温由共享熔池在冷却、反应之前处理：闭盖爆炸，开盖熔毁。详见 [大型 AOD 使用说明](large-aod.md)。

`compat/CrucibleFaucetSignalMixin` 修正 GT6CE 0.1.0 龙头读取自身红石输出的问题：SSITE 对连接本附属炉子的龙头改读邻接信号，保留原有自动取料开关。此注入不引用 GT 实现类型，方法注入允许宿主后续移除；升级 GT 时需复测并在上游修复后移除此补丁。

流化床工业供热使用 `FluidizedBedCore`、`FluidizedBedStructure` 和两平台 `FluidizedBedBehavior`。主机、短烟道与九格炉台属于同一固定结构；核心通过已成型炉台的正上方邻格发送 HU，九格共享一个总预算，宿主按实际接收量扣能量。煤族燃料的原生燃烧时间、一次预留的灰产物、待燃料热量和 HU 缓存分开核算；KU 单独驱动鼓风，预热与余热由蓄热量约束输出。声明由 `FluidizedBedMachines` 辅助类提供，再合入既有 `SsiteMachines` / `SsiteBehaviorMachines` 单一 SPI 入口，适配当前宿主每附属只能提交一次的约束。

`FluidizedBedEnergyMixin` 仅取消这台机器的主 HU 主动抽取，保留 KU 辅助通道；宿主的 `pollEnergy` 不调用行为的 `acceptsEnergy`，只覆盖被动注入不足以使热库保持内部生成。`ObjMultiblocks` 让流化床与大型 AOD 复用装配预览、建筑之杖和成型零件隐藏桥接。`FluidizedBedRenderer` 保持单独的静态模型与坐标变换，Jade 集成在 `FluidizedBedJadeProvider`。玩法与坐标见[流化床工业供热](fluidized-bed.md)。

AOD 与 VAR 配方表中的展示行不执行熔池反应；真实反应在行为核心中执行。修改工艺数值时，要核对行为、展示行和文档是否一致。气体单位也按设备区分：AOD 每内部单位 10 mB，VAR 为 100 mB。

客户端的 `ObjMesh` 将模型解析为三角形，`SmallFurnaceRenderer` 负责朝向、炉盖和电极变换，再通过两端的 `MeshSink` 写入顶点。`RenderType.entityCutout` 使用 QUADS，因此每个 OBJ 三角形必须提交为 `A,B,C,C` 四个顶点；直接连续提交三个顶点会把相邻面和动作分组错误连接，关闭背面剔除无法修复这种错接。OBJ 的原始朝向和法向保持不变。

## 资源与构建

共享数据源采用 1.20.1 的 `recipes/` 和合成结果 `item` 格式；NeoForge 的 `processResources` 将目录转换为 `recipe/`，并转换对应合成结果字段。改资源转换时，应检查生成结果。

Gradle 8.8 使用 JDK 17 启动，Forge 目标为 JDK 17，NeoForge 目标为 JDK 21。`scripts/build.ps1` 负责定位两个 JDK。SDK 和运行时均优先从 `libs/` 解析，否则查找旁边的 GT6CE 检出；更新 SDK 不代表运行时已经更新。

## 验证边界

- `scripts/build.ps1 compileBoth`：两端编译，运行两端 `MoltenPoolTests`、`FluidizedBedTests` 和流化床实际顶点流检查。
- `scripts/build.ps1 build`：两端完整构建，覆盖资源处理与打包。
- `scripts/build.ps1 :neoforge:furnaceRenderTest`（或 Forge 对应任务）：捕获实际渲染器的顶点输出，检查 QUADS 拼接、朝向、UV、光照、五个接触面，以及两台机器的四个朝向和七个开盖位置；不启动图形窗口。该检查也接入各端的 `check`。
- 开发服务器启动：检查配方是否被拒绝、移除规则是否命中、运行时 API 是否匹配。
- `scripts/testing/fluidized_bed_server.py`：在既有 NeoForge 隔离测试世界验证真实 HU 接收、九格总账、燃料/灰自动化与两种大型炉子升温；不随发行 JAR 打包。
- 客户端人工验收：检查 EMI、贴图、交互和真实产线运行。

JVM 测试通过不等于配方在 GT6CE 内完整加载；服务端启动通过也不能替代客户端验收。具体执行结果记录在计划卡或整理记录中。
