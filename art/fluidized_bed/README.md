# 流化床燃烧室 · FBC–01

循环流化床燃烧室游戏模型：保留高主炉筒、侧置旋风分离器、外露回料管和独立鼓风机，增加固定低位保温烟道、旁侧 3×3 供热台及前侧控制面板。造型参照 HBM 机器的低多边形轮廓、棕灰钢板、分节带箍、铆钉与像素贴图语言。供热台顶面保持完整平坦，可直接承托大型坩埚或 AOD。

几何、UV、材质与像素贴图均为本项目原创。HBM 的 OBJ 和贴图仅用于理解风格；生成脚本不读取、复制或拼接 HBM 资源。本目录保留建模源资产与离线预览，安装脚本将完整 OBJ、图集及五类方块/物品资源写入游戏资源目录；机器行为由 Java 实现。

## 文件

| 文件 | 用途 |
| --- | --- |
| `fluidized_bed_combustor.obj` | 完整模型；三角面，包含 UV、显式法线和命名部件 |
| `fluidized_bed_combustor.mtl` | 单张 atlas 的标准 OBJ 材质定义 |
| `fluidized_bed_combustor_atlas.png` | 512×256 RGB 像素贴图；8×4 个 64×64 材质块 |
| `fluidized_bed_combustor_cutaway.obj` | 独立剖视版本，显露床层、布风结构和耐火内衬 |
| `preview.html` | 自包含 WebGL 2 交互预览，模型与贴图均内嵌，无 CDN 依赖 |
| `preview.png` | 完整模型预览截图 |
| `preview_cutaway.png` | 炉体内部剖视截图 |
| `preview_back.png` | 后侧回料、平台与风管截图 |
| `model.json` | 尺寸、面数、部件、剖视隐藏列表及接口位置的机器可读清单 |

将 OBJ、MTL 和 PNG 放在同一目录后导入 Blender 等支持 Wavefront OBJ 的工具。完整 OBJ 与剖视 OBJ 是两种展示版本，**不要叠放显示**，否则会产生重面闪烁。

## 尺度与坐标

- **1 模型单位 = 1 方块**；`+Y` 向上，正面朝 `-Z`。
- 底座落在 `Y=0`；主炉筒轴线位于 `X=-0.85、Z=0.15`。
- 包围盒最小值为 `(-3.45, 0, -2.5)`，最大值为 `(7.5, 7.97, 2.18771)`。
- 实际外包尺寸按 `X×Y×Z` 为 **10.95×7.97×4.68771**；在游戏控制器局部坐标中，包络为 **11×8×5 方块**。
- 游戏渲染以控制器为基准，将 OBJ 平移 **`(+0.5, 0, +2.5)`**，再按控制器朝向旋转。模型正面板 `X[-0.5,0.5] / Y[0,1] / Z=-2.5` 与控制器正面相接。
- 完整模型当前有 **5,968 三角面、50 个命名部件**；剖视模型有 **4,880 三角面**。准确统计以同目录 `model.json` 为准。

### 供热面与净空

供热台模型范围为 `X[4.5,7.5] / Y[0,2] / Z[-1.5,1.5]`。`heat_contact` 对象**只包含 9 个 `Y=2` 的朝上四边形**，共 18 个三角面、面积 9；顶板侧面与底面属于 `heat_roof_edges`。顶面无围栏、螺栓或其他突出件，整个 3×3 上方竖直空间无本机模型遮挡。

在控制器局部方块坐标中，供热板为 `right=5..7 / up=1 / back=1..3`，顶面落在 `Y=2 / X[5,8] / Z[1,4]` 的整数边界；上接设备从 `up=2` 开始。固定烟道由主塔后下方沿 `Z≈1.75` 横向经过低位支座，再转入供热台左侧。

材质采用平面明暗与像素细节。贴图采样应设为 **Nearest / 最近邻**；Blender 的 Image Texture 节点可选择 **Closest**。生成时以 32px 逻辑网格绘制每块材质，再最近邻放大为 64px，因此细节以 2px 为单位。UV 已留出图块内缘，不宜重新自动展开或对整张 atlas 做模糊滤波。

## 构成与部件命名

| 构成 | OBJ 对象 / 分组名称 | 表现作用 |
| --- | --- | --- |
| 共用钢制底架 | `skid` | 承托炉筒和附属模块 |
| 底部风室 | `windbox` | 表现床层下方的一次风空间 |
| 高主燃烧筒 | `riser_shell`、`front_shell`、`crown` | 主体轮廓、前侧炉壳和顶部封盖 |
| 内衬与加强结构 | `refractory_lining`、`front_lining`、`shell_bands`、`front_bands`、`shell_ribs`、`front_ribs` | 耐火内衬、分节带箍和纵向加强筋 |
| 布风与床层 | `distributor`、`bed`、`bed_front`、`bed_particles` | 栅格式布风结构、颗粒床和局部炽热颗粒 |
| 检修与仪表 | `inspection_hatch`、`front_instruments` | 带观察孔的检修门、仪表盒与 FBC /01 标牌 |
| 底灰出口 | `ash_outlet` | 正面下方带 ASH 标识的排灰构件 |
| 固体燃料给料 | `fuel_hopper`、`fuel_screw`、`hopper_support` | 开口料斗、螺旋给料外壳、驱动与支架 |
| 旋风分离 | `cyclone`、`cyclone_bands`、`crossover_flue` | 侧置分离筒、锥段、带箍及上部烟气连接管 |
| 固体回料 | `solids_return`、`loop_seal`、`aeration_line` | 下行回料腿、回料封和细曝气支管 |
| 净烟气出口 | `flue_outlet` | 分离器顶部带法兰和暗色内孔的出口 |
| 分离器支撑与检修 | `cyclone_frame`、`service_platform`、`guardrails`、`ladder` | 支架、后侧检修台、黄栏杆和爬梯 |
| 鼓风机 | `blower_base`、`blower`、`blower_motor` | 底座、风机壳、进风护罩和驱动电机 |
| 一次风与二次风 | `primary_air`、`secondary_air` | 连接下部风室的一次风管与炉身二次风支管 |
| 起吊构件 | `lifting_lugs` | 炉顶起吊耳的低多边形外形 |
| 前侧控制器 | `controller`、`controller_mount` | 贴合控制器方块正面的仪表面板与支架 |
| 固定热烟道 | `hot_gas_duct`、`duct_flanges`、`duct_supports` | 后绕方形保温管、连接法兰与落地支座 |
| 供热台 | `heat_bank_base`、`heat_bank_refractory`、`heat_bank_casing`、`heat_bank_bands`、`heat_bank_door` | 低位耐火核心、外护板、加强框与清灰门 |
| 供热顶板 | `heat_contact`、`heat_roof_edges` | 九格平坦向上接触面与顶板侧壁 |

工艺关系按“固体燃料进入床层 → 炉筒内燃烧 → 上部烟气进入旋风分离器 → 固体经回料腿返回炉体 → 净烟气向上排出”的循环路径组织。内部结构用于游戏美术的工艺示意，未定义实际压力容器的壁厚、耐温参数或运行工况。

`model.json` 的 `ports` 记录 `fuel`、`air`、`ash`、`flue`、`heat`、`controller` 的包围范围与朝向。面边界使用 OBJ 坐标，供热连接格及 `heat_owner_cells` 使用平移后的控制器局部方块坐标；这些清单用于资源检查，实际接口行为由机器代码决定。

## 查看与剖视

直接在支持 WebGL 2 的浏览器中打开 `preview.html`。拖动旋转，滚轮缩放；可切换全貌、正面、背面和俯视，启用方块网格，或从下拉菜单高亮部件。

“剖视”会隐藏 `front_shell`、`front_lining`、`front_bands`、`front_ribs`、`inspection_hatch`、`front_instruments` 和 `bed_front`。独立剖视 OBJ 使用同样的隐藏列表，仍共用完整模型的 MTL 与贴图。它是隐藏选定部件形成的展示剖视，不是可以任意移动切面的实体 CAD 模型。

## 重新生成

需要 Python 3 与 Pillow。在项目根目录执行：

```powershell
python scripts/models/build_fluidized_bed.py
```

几何与导出源为 `scripts/models/build_fluidized_bed.py`，贴图源为 `scripts/models/fluidized_bed_materials.py`，交互预览模板为 `scripts/models/fluidized_bed_viewer.html`。脚本会重新生成本目录的两份 OBJ、MTL、atlas、`model.json` 和 `preview.html`，并校验有限坐标、单位法线、UV 范围、退化面、三角形朝向、九格供热面积、整数边界及上方净空。预览 PNG 是另行截取的展示图片，几何修改后应重新截图。

同步游戏资源时运行：

```powershell
python scripts/models/install_fluidized_bed.py
```

安装脚本先重新生成模型，再写入 `assets/ssite/obj/fluidized_bed_combustor.obj` 和 `assets/ssite/textures/machine/fluidized_bed_combustor.png`，并生成 `fluidized_bed_combustor`、`fluidized_bed_casing`、`fluidized_bed_duct`、`fluidized_bed_heat_plate`、`fluidized_bed_foundation` 的 blockstate、方块/物品模型和从图集提取的像素贴图。配方、语言文本与 Java 注册不由美术安装脚本维护。

交互检查另需 Python Playwright 与其 Chromium。完整检查覆盖旋转、缩放、四视角、剖视、部件高亮、网格及 736/360 像素下的明暗主题；结果保存到 `build/fluidized-bed-qa`：

```powershell
python scripts/models/inspect_fluidized_bed.py
```

重新生成几何后，用以下命令检查加载与剖视，并更新本目录的三张展示截图：

```powershell
python scripts/models/inspect_fluidized_bed.py --smoke --output art/fluidized_bed
```
