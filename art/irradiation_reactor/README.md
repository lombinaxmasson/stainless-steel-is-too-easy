# 批次照射反应堆 IR–03

参考 HBM 核工业设备的造型语言制作的原创低多边形模型。厚重的八角屏蔽舱、前部圆形装料锁、侧置换热柱和独立仪表柜共同组成完整设备，顶部带索引驱动、螺栓顶盖与吊耳。灰绿机壳、暖灰屏蔽层、炭黑接缝配少量黄黑警示纹；仪表、铭牌和局部磨损使用像素贴图。

**本模型独立设计，不按现有五方块结构建模。** 实际外包尺寸为宽 5.08 × 高 4.98 × 深 5.08 方块。它是美术资产，尚未接入游戏；游戏中的结构判定、碰撞、接口位置和渲染仍以现有 Java 实现为准。若后续采用此造型，需要另行设计对应的多方块结构。

![IR–03 全貌](preview.png)

## 文件

| 文件 | 内容 |
| --- | --- |
| [preview.html](preview.html) | 内嵌网格与贴图的离线 WebGL 2 预览，无 CDN 或网络依赖 |
| [irradiation_reactor.obj](irradiation_reactor.obj) | 完整模型，31 个具名部件、8,712 个三角面，平面法线 |
| [irradiation_reactor_cutaway.obj](irradiation_reactor_cutaway.obj) | 隐去前半屏蔽壳与顶盖的剖视版本 |
| [irradiation_reactor.mtl](irradiation_reactor.mtl) | 两份 OBJ 共用的材质定义 |
| [irradiation_reactor_atlas.png](irradiation_reactor_atlas.png) | 512×256 RGB 像素图集，使用最近邻过滤 |
| [model.json](model.json) | 坐标、包围盒、部件中英文对照、剖视隐藏列表和网格检查结果 |
| [preview_cutaway.png](preview_cutaway.png) | 剖视截图 |
| [preview_front.png](preview_front.png)、[preview_back.png](preview_back.png)、[preview_side.png](preview_side.png)、[preview_top.png](preview_top.png) | 正面、背面、右侧与俯视截图 |

预览支持拖动旋转、滚轮缩放、五个预设视角、方块网格、部件高亮与剖视。键盘方向键旋转，`+` / `-` 缩放，`Home` 回到全貌。剖视中的中央源柱、四支靶匣与转台是游戏美术示意。

## 坐标与部件

1 OBJ 单位 = 1 方块，`+Y` 向上、`-Z` 为正面，地面位于 `Y=0`。反应舱轴心位于 `X=-0.60, Z=0.35`。包围盒为 `(-2.60, 0, -2.65)` 至 `(2.48, 4.98, 2.43)`。这些坐标是独立模型坐标，没有绑定控制器格。

主体前后壁、带箍和加强筋分别分组；装料锁、压爪、顶盖、螺栓、顶部驱动均可独立选择。内构分为 `source_capsule`、`sample_carousel`、`target_capsules` 和 `interior_liner`。换热柱、散热片、管路、阀门、控制柜、检修踏台与梯子保持独立分组。完整列表见 `model.json`。

## 参考与生成

造型参考来自本机 HBM 检出中的 `exposure_chamber.obj`、`purex.obj`、`silex.obj` 及其对应贴图：

```text
D:\Baobao\projects\dev\java\Hbm-s-Nuclear-Tech-GIT\src\main\resources\assets\hbm\models\machines
```

参考点是厚板和粗管轮廓、低饱和工业配色、功能件分组及像素明暗边。模型和贴图均由本地脚本重新生成，生成过程不读取或复制 HBM 网格、UV 或贴图。

在仓库根目录执行，使用 Python 3、Pillow；截图与交互检查另需 Playwright 及其 Chromium：

```powershell
python scripts/models/build_irradiation_reactor.py
python scripts/models/inspect_irradiation_reactor.py
```

生成器的输入是 `scripts/models/build_irradiation_reactor.py`、`irradiation_reactor_ir03_materials.py` 与 `irradiation_reactor_ir03_viewer.html`，并复用同目录的几何原语、基础像素绘制函数和预览外壳样式。**修改这些输入再生成，不手改 OBJ、MTL、图集或预览。** `scripts/models/` 按仓库约定仅在本地维护，已被忽略；仓库中的产物可直接导入或预览，但只有包含这些脚本的工作副本可以重新生成。

生成检查覆盖有限坐标、单位法线、UV 范围、退化三角面、面序和部件集合；独立检查脚本解析导出的 OBJ/MTL，并验证预览加载、剖视、视角、网格、部件选择、旋转缩放和窄屏布局，报告写入 `build/irradiation-reactor-qa/report.json`。双端游戏内渲染、碰撞和机器行为尚未验证。
