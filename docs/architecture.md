# 架构说明

本说明对应 2026-10-29 的代码结构。工程由两个加载器子项目编译同一份 `common/` 源码，`common/` 不是单独打包的运行时模块。当前使用 **GT6CE 1.0.2 运行时与 API / addon SDK 1.0.0**；SDK 作为编译依赖，不打进附属 jar，开发运行另需对应平台的完整 GT6CE jar。

## 目录与边界

| 位置 | 职责 |
|---|---|
| `common/.../SsiteAddon.java` | 共用注册入口与 addon 标识 |
| `common/.../material/` | 材料、形态、矿脉、坩埚反应与熔炼目标移除声明 |
| `common/.../chemistry/` | 按领域组织的配方与共用配方构建工具 |
| `common/.../machine/` | 机器声明、熔池行为、AOD/VAR 工艺、流化床热源与独立 HU 网络 |
| `forge/src/main/java/`、`neoforge/src/main/java/` | 加载器入口、创造标签页，以及同包名的平台适配类 |
| `common/src/main/resources/META-INF/services/` | GT6CE 加载材料、机器、行为和结构零件的 SPI 清单 |
| `common/src/main/resources/data/ssite/recipes/` | 原版合成配方和 GT6CE 机器配方移除规则 |
| `common/src/main/resources/assets/` | 双语、本地模型、贴图和 EMI 别名 |
| `common/src/test/java/` | 两端复用的 JVM 熔池、燃料守恒、HU 拓扑与渲染检查 |
| `gradle/gt6ce-sdk.gradle`、`scripts/` | SDK/运行时定位、JDK 选择与本机构建辅助 |

表中的 `common/.../` 均指 `common/src/main/java/com/lombinaxmasson/ssite/`。

## 注册与配方

两个加载器的 Mod 构造器调用 `SsiteAddon.register()`。GT6CE 在材料开放阶段通过 SPI 读取 `SsiteMaterials`，在自身配方就绪后调用 addon 的 `onRecipesReady`，进入 `ChemistryRecipes.add()`。

GT6CE 1.0.2 的材料安装流程会为同一附属的多个材料重复创建 `ModData`，与其禁止重复模组 ID 的校验冲突。`compat/AddonMaterialOwnerMixin` 仅在 `AddonMaterials.installDefinitions` 和 `linkAddons` 两处构造调用复用已经注册的归属对象，保留全局重复 ID 校验与材料声明。升级宿主时需复核这两处调用，并在上游修复后移除此兼容补丁。

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

GT6CE 1.0.2 的坩埚温度查询读取 `crucibleContent().temperature`。两端 `ArgonCrucibleBehavior` 在空炉时返回空材料、零数量与真实炉温的 `CrucibleContent`，其 `present()` 仍为假；这使原生温度接口能够看到空炉预热，不增加材料或改变取料判断。

大型 AOD 使用 `LargeAodStructure` 和 `LargeAodCore`。浇铸控制器位于 OBJ 的 (0,1,−2)，底部九个普通结构端口以 `ENERGY_INPUT` 角色转发 HU（行为机器宿主不支持用 `HEAT_INPUT` 角色转发）；两个功能端口各自储存气体。炉盖字段 `open` 与 `lid.mode` 负责手动/红石开盖，已投料后的熔炼不受盖子开关阻断。过温由共享熔池在冷却、反应之前处理：闭盖爆炸，开盖熔毁。详见 [大型 AOD 使用说明](large-aod.md)。

`compat/CrucibleFaucetSignalMixin` 修正 GT6CE 0.1.0 龙头读取自身红石输出的问题：SSITE 对连接本附属炉子的龙头改读邻接信号，保留原有自动取料开关。此注入不引用 GT 实现类型，方法注入允许宿主后续移除；升级 GT 时需复测并在上游修复后移除此补丁。

流化床工业供热使用 `FluidizedBedCore`、`FluidizedBedStructure` 和两平台 `FluidizedBedBehavior`。高塔、分离器与后部出口构成 71 块本体；外置烟道和加热板不参与本体成型。核心通过后部出口的真实相邻块发送 HU，宿主只扣实际接收量。燃料按原版流化床材料热值的 150% 计量，不依赖普通熔炉 `burnTime` hooks；一次预留的灰、待燃料热量和 HU 缓存分开核算。KU 单独驱动鼓风，预热与余热由蓄热量约束输出。声明由 `FluidizedBedMachines` 提供，合入既有 `SsiteMachines` / `SsiteBehaviorMachines` 单一 SPI 入口，符合每附属只能提交一次的约束。

独立 `heat_duct` 与 `heating_plate` 由 `HeatTransportMachines` 声明，两平台 `HeatTransportBehavior` 调用共用 `HeatTransportCore`。每网最多 12 管和一组同层 3×3 范围内的 1～9 板；拓扑扫描不加载区块，板仅通过水平边相连，管可六面相连。网络将板组收缩为一个终点检查管环。每 tick 由稳定排序的首节点处理全网，共用 73,728 HU 总扣款预算；每板顶部输出最多 4096 HU，管路径每节线性损失 1%，板内共享无损。各源缓存按对方实际接受量与累计取整损耗扣款，处理标记在调用接收方之前写入，避免回调重入或同 tick 重组复制预算。每次外部回调前后复核路径，回调拆管或卸载时保留已完成账目并停止后续输出。

`FluidizedBedEnergyMixin` 仅取消 SSITE 流化床和 HU 运输机器的主 HU 主动抽取，保留流化床 KU 辅助通道与其他机器原有行为；宿主的 `pollEnergy` 不调用行为的 `acceptsEnergy`，否则可绕过热源外部 HU 拒收和非法管网的输入检查。`HeatTransportAccessMixin` 为运输核心提供同步宿主缓存与原生 HU 发包访问，不保留行为回调 context。`ObjMultiblocks` 让流化床与大型 AOD 复用装配预览、建筑之杖和成型零件隐藏桥接；独立管板由 `HeatTransportRenderer` 绘制，并按连接状态生成选中与碰撞轮廓。玩法与坐标见[流化床工业供热](fluidized-bed.md)。

GT6CE 1.0.2 会保留未成型结构中已匹配零件的绑定，供建筑工具路由使用。`ObjMultiblockDesignMixin` 因此按控制器的实际成型边沿切换绑定零件的外观设计，`LargeAodPartMixin` 不再仅凭绑定隐藏零件；拆坏结构时恢复普通外观和完整基础形状，同时保留工具绑定。

AOD 与 VAR 配方表中的展示行不执行熔池反应；真实反应在行为核心中执行。修改工艺数值时，要核对行为、展示行和文档是否一致。气体单位也按设备区分：AOD 每内部单位 10 mB，VAR 为 100 mB。

客户端的 `ObjMesh` 将模型解析为三角形，`SmallFurnaceRenderer` 负责朝向、炉盖和电极变换，再通过两端的 `MeshSink` 写入顶点。`RenderType.entityCutout` 使用 QUADS，因此每个 OBJ 三角形必须提交为 `A,B,C,C` 四个顶点；直接连续提交三个顶点会把相邻面和动作分组错误连接，关闭背面剔除无法修复这种错接。OBJ 的原始朝向和法向保持不变。

## 资源与构建

共享数据源采用 1.20.1 的 `recipes/` 和合成结果 `item` 格式；NeoForge 的 `processResources` 将目录转换为 `recipe/`，并转换对应合成结果字段。改资源转换时，应检查生成结果。

Gradle 8.8 使用 JDK 17 启动，Forge 目标为 JDK 17，NeoForge 目标为 JDK 21。`scripts/build.ps1` 负责定位两个 JDK。SDK 和运行时均优先从 `libs/` 解析，否则查找旁边的 GT6CE 检出。`gt6ce_version=1.0.2` 与 `gt6ce_api_version=1.0.0` 分别选择完整宿主与 SDK；更新 SDK 不代表运行时已经更新。旧 0.1.0 宿主的字符串能量接口与当前 `EnergyType` 接口不兼容，不能混用。

## 验证边界

- `scripts/build.ps1 compileBoth`：两端编译，运行两端 `MoltenPoolTests`、`FluidizedBedTests`、`HeatTransportTests` 和流化床实际顶点流检查。
- `scripts/build.ps1 build`：两端完整构建，覆盖资源处理与打包。
- `scripts/build.ps1 :neoforge:furnaceRenderTest`（或 Forge 对应任务）：捕获实际渲染器的顶点输出，检查 QUADS 拼接、朝向、UV、光照、五个接触面，以及两台机器的四个朝向和七个开盖位置；不启动图形窗口。该检查也接入各端的 `check`。
- 开发服务器启动：检查配方是否被拒绝、移除规则是否命中、运行时 API 是否匹配。
- `scripts/testing/fluidized_bed_server.py`：在既有 NeoForge 隔离测试世界验证四向本体、源到实体首管的 HU 总账、断外管保持成型、燃料/灰自动化与两种大型炉子升温；不随发行 JAR 打包。
- `scripts/testing/heat_transport_server.py`：同一隔离世界内的小网络专项，核对原生 HU 注入、单板/九板、多入口守恒、非法网络拒收、断路余热与真实未加载区块边界；不修改玩家世界或 EULA。
- 客户端人工验收：检查 EMI、贴图、交互和真实产线运行。

2026-10-29 已完成 Forge 重混淆 JAR 与 NeoForge JAR 构建，以及熔池、流化床、HU 网络和渲染回归。GT6CE 1.0.2 / NeoForge 1.21.1 隔离服务器中的流化床 14 阶段、运输 5 阶段与大型 AOD 建造专项均通过，详见[流化床验证记录](fluidized-bed.md#资源与验证入口)。Forge 尚未实机验证，NeoForge 尚未做客户端视觉验收；服务端与顶点流检查不替代这两项验收。
