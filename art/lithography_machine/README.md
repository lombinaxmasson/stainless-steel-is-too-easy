# 光刻机

参考 HBM 的工业配色与像素仪表制作的原创多方块美术资产，以整块厚壁机壳、平顶、隔振基座和深凹操作面构成轮廓。前面是双晶圆装卸槽、内嵌主控、主控下方的 EU 插座和琥珀色观察窗。右侧集中放置三个流体连接口。

**宽 5 × 深 4 × 高 4 方块，35 个具名部件，3,192 个三角面。** 包含离线运行状态机和实时部件动画，尚未接入游戏的多方块结构、碰撞、接口或渲染，不改变现有光刻配方和机器行为。

![光刻机全貌](preview.png)

## 文件

| 文件 | 内容 |
| --- | --- |
| [lithography_machine.obj](lithography_machine.obj) | 完整模型；三角面、平面法线、具名 `o` / `g` 分组 |
| [lithography_machine_cutaway.obj](lithography_machine_cutaway.obj) | 移除前部、左右舱壁与顶盖的剖视模型 |
| [lithography_machine.mtl](lithography_machine.mtl) | 共用材质库：不透明机壳与半透明玻璃两种材质 |
| [lithography_machine_atlas.png](lithography_machine_atlas.png) | 512×256 RGB 像素图集；最近邻过滤 |
| `preview.html`（本地生成，不入库） | 内嵌网格与贴图的离线 WebGL 2 预览，无 CDN 或网络依赖 |
| [model.json](model.json) | 尺寸、分组、接口、控制器参考位置和剖视隐藏列表 |
| [lithography_machine_animation.js](lithography_machine_animation.js) | 可复用的单片循环状态机、SCARA 机械臂变换和曝光扫描；由生成器导出 |
| [preview_animation.gif](preview_animation.gif) | 实际浏览器帧录制的完整单片周期，加速播放 |
| [preview_running.png](preview_running.png)、[preview_running_cutaway.png](preview_running_cutaway.png) | 曝光中的外观与剖视 |
| [preview_glass.png](preview_glass.png) | 完整机壳下透过观察窗查看运行内构 |
| [preview_cutaway.png](preview_cutaway.png) | 内部结构预览 |
| [preview_front.png](preview_front.png)、[preview_back.png](preview_back.png)、[preview_side.png](preview_side.png)、[preview_top.png](preview_top.png) | 正面、背面、右侧和俯视 |
| [preview_ports_front.png](preview_ports_front.png)、[preview_ports_side.png](preview_ports_side.png) | 带接口编号和用途说明的正面、右侧预览 |

导入时将 OBJ、MTL 和 PNG 放在同一目录，保持文件名不变。使用最近邻纹理过滤、平面着色和背面剔除。观察窗 `amber_window` 单独使用 `lithography_glass` 材质，MTL 的 `d=0.22` 表示不透明度 22%；其余部件使用 `d=1` 的 `lithography_atlas`。浅琥珀色与细反光保存在同一张 RGB 图集中，透明度由材质控制。

观察窗净开口宽 1.68 格、高 1.37 格。上下横框改浅，铭牌与警示标志移至窗外。默认全貌采用略偏正面的视角，运行时可透窗看到镜筒下段、晶圆台、晶圆和曝光指示光。剖视可查看镜筒上部和掩模台。

预览先绘制不透明部件，再按距离混合玻璃。绘制玻璃时保留深度测试并关闭深度写入，实心阴影贴图仅包含不透明部件。导入其他软件时需启用玻璃材质的混合透明模式，后续游戏渲染也需要独立透明绘制阶段。

预览支持拖动旋转、滚轮缩放、五个视角、剖视、方块地面网格和部件高亮。默认展开接口说明；点击场景编号或接口按钮，会切换到正面／右侧、高亮相应接口并显示用途和流向。编号跟随视角投影，背向接口隐藏；可关闭说明查看干净外观。键盘方向键旋转，`+` / `-` 缩放，`Home` 复位。动画支持开始运行、完成本片后停机、暂停／继续及复位，并显示当前工序、进度和已完成片数。

## 运行动画

每片演示周期为 **16.25 秒**，其中曝光扫描 4.8 秒。输入托盘将晶圆沿 `+Z` 收回 0.69 格，双连杆机械臂接片并抬高，经前侧通道送至曝光台；放片后机械臂退回，XY 滑台与晶圆一起扫描；随后机械臂取走成品，送到输出托盘，托盘伸出并交付。输入和输出分别有独立托架，整个过程只有一个 `work_wafer` 网格。

- **停机**：完成当前片、交付并熄灭曝光光后停止；不会中途复位或跳到下一片。
- **暂停**：冻结预览的自动时钟，继续后从同一位置接续；供检查使用的显式 `advance(seconds)` 仍可推进。
- **复位**：返回停机姿态并清零计数，保留观察视角、剖视和接口选择。
- **交付**：成品到达输出位置后隐藏，表示送入输出库存；下一循环才出现新的输入片。预览没有真实物品库存。
- **光效**：曝光阶段显示镜筒下方的小范围工作指示光，主控屏随运行状态调亮；机壳不整体发光。可见光是工艺示意，不代表真实 DUV 光可被肉眼看到。

机械臂采用固定分支逆运动学，避免中途翻肘；扫描滑台、工件、机械臂和托架各自具名。实体与阴影使用同一部件变换。OBJ 只记录绑定姿态，不保存动画：导入时结合 `model.json.animation` 和导出的 JavaScript 状态机应用变换及可见性。停机时隐藏 `work_wafer` 和 `exposure_light`；导入原始 OBJ 时两组网格仍然存在。

## 内部构造与坐标

![光刻机剖视](preview_cutaway.png)

内部包括独立的多级投影镜筒、掩模版定位台、XY 晶圆台、曝光位晶圆、晶圆搬运机械臂和封闭光路。采用 DUV 镜筒式设备的游戏美术示意，不对应真实设备的工程尺寸或光学设计。

1 OBJ 单位 = 1 方块，`+Y` 向上，`-Z` 为正面，原点位于整体包络的最小角。所有几何限定在 `(0,0,0)` 至 `(5,4,4)` 内。以下为预留的美术接口，其中心对齐方块外表面：

| 接口 | 方块格 `(X,Y,Z)` | 外表面中心 | 朝向 | 分组 |
| --- | --- | --- | --- | --- |
| 01 晶圆输入 | `(1,1,0)` | `(1.5,1.5,0)` | `-Z` | `load_input` |
| 02 晶圆输出 | `(0,1,0)` | `(0.5,1.5,0)` | `-Z` | `load_output` |
| 03 主控面板 | `(4,1,0)` | `(4.5,1.5,0)` | `-Z` | `control_console` |
| 04 EU 输入 | `(4,0,0)` | `(4.5,0.5,0)` | `-Z` | `eu_input` |
| 05 氮气输入 | `(4,2,1)` | `(5,2.5,1.5)` | `+X` | `nitrogen_input` |
| 06 冷却液输入 | `(4,1,2)` | `(5,1.5,2.5)` | `+X` | `coolant_input` |
| 07 冷却液回流 | `(4,2,2)` | `(5,2.5,2.5)` | `+X` | `coolant_return` |

正面直视时，两个装卸槽按从左到右的输入→输出排列，分别标有 `WAFER IN`、`WAFER OUT` 和流向箭头，可独立高亮。电力口为主控正下方的黄色方形双触点插座，凹腔贯穿基座与服务柱正面；氮气口为右侧小孔金属快接；冷却液进／回口为右侧蓝色／赭色大孔快接，标有 `CW IN` / `CW OUT`。所有连接口均有真实凹腔；颜色同时配合几何和文字识别。

元数据版本为 4，新增 `transparent_groups` 记录玻璃材质、不透明度和投影策略。版本 3 已将 EU 从右侧移到正面主控下方；`load_a`、`load_b` 的元数据 ID 保留，分别指向 `load_input`、`load_output`。托架与工件分离，独立多片网格改为单个 `work_wafer`，机械臂与滑台按动画需要分组。下游若已使用旧美术坐标、分组或单材质绘制方式，需要按本表及 `model.json` 重新映射。当前没有游戏存档、NBT 或注册 ID 迁移。

朝北放置时，以控制器方块最小角为渲染原点，模型平移 `(-4,-1,0)`；其余方向绕控制器方块中心旋转。这只是后续接入所需的坐标约定，游戏尚未采用这些接口。

## 参考、生成与检查

造型参考为本机 `Hbm-s-Nuclear-Tech-GIT` 的 `silex.obj`、`assembly_machine.obj`、`exposure_chamber.obj` 与对应贴图；公开项目见 [Hbm's Nuclear Tech Mod](https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT)。模型与贴图全部由本地脚本重新生成，生成过程不加载或复制 HBM 的网格、UV 或贴图。

在仓库根目录执行，生成需要 Python 3 和 Pillow；浏览器检查另需 Playwright 及 Chromium：

```powershell
python scripts/models/build_lithography_machine.py
python scripts/models/inspect_lithography_machine.py
# 同时录制完整运行周期：
python scripts/models/inspect_lithography_machine.py --gif
# 仅检查导出的几何、材质、包络与接口：
python scripts/models/inspect_lithography_machine.py --geometry-only
```

生成输入为 `scripts/models/build_lithography_machine.py`、`lithography_machine_materials.py`、`lithography_machine_animation.js` 与 `lithography_machine_viewer.html`；复用同目录 `build_fluidized_bed.py` 的几何原语、`irradiation_reactor_materials.py` 的基础字形及 `viewer_shell.css`。**修改生成输入后重新导出，不手改产物。** `scripts/models/` 按仓库约定被忽略，仅在当前工作副本中维护；仅克隆仓库的产物不能重建模型。

独立检查解析 OBJ/MTL，核对索引、有限坐标、单位法线、UV 范围、退化面、绕序、相对资源引用、完整/剖视组一致性、尺寸与接口方块中心；浏览器检查覆盖贴图、截图、视角、剖视、网格、部件选择、鼠标键盘与窄屏布局，以及全部七个接口的点击、编号可见性、中文用途和方块坐标。动画检查覆盖整段／分步推进、单片连续性、721 个采样姿态的完整顶点包络、固定机壳、扫描同步、真实控件与渲染矩阵。报告写入 `build/lithography-machine-qa/report.json`。包络检查不等于完整的机械碰撞或工程验证。

2026-11-03 初版曾通过 25 项检查。2026-11-04 外观与接口布局重做，重新运行上述生成和完整检查命令，33 项检查全部通过，并更新八张实际浏览器截图；没有页面、控制台或 WebGL 错误，没有外部网络请求。剖视截图采用正面视角，便于查看厚侧柱后方的曝光镜筒。

2026-11-04 运行动画追加验证：执行生成命令及 `python scripts/models/inspect_lithography_machine.py --gif`，47 项检查全部通过。721 个采样姿态的顶点均在 5×4×4 包络内，阴影深度纹理在两个扫描姿态之间有 1,768 个像素变化；真实启停、暂停、复位及完整单片周期通过。更新八张静态图、两张运行图和 98 帧 GIF；运行剖视和 GIF 采用略俯视的正面角度，展示晶圆表面与托盘运动。没有页面、控制台或 WebGL 错误，没有外部网络请求。

2026-11-04 透明观察窗追加验证。重新生成并执行 `python scripts/models/inspect_lithography_machine.py --gif`，连同补充的工件交接检查共 56 项通过。独立核对 OBJ 材质分配和 MTL 不透明度。真实渲染中，透明窗与强制不透明对照的窗口像素明显不同，窗外机壳和背面像素保持一致，玻璃没有写入实心阴影。扩大窗口后重新生成并执行 `python scripts/models/inspect_lithography_machine.py`，56 项检查通过。已目视确认默认全貌及正面略俯视都能透窗看到曝光光和晶圆中心。

本次只制作美术资产，不需要 Java 构建。Forge / NeoForge 游戏内加载、实际光照、碰撞、交互与机器行为均待接入后验证。
