# HU 输热管与独立加热板

两种可单独放置的输热组件，与 [独立流化床塔体](../fluidized_bed/README.md) 共用原创 512×256 像素图集。几何由脚本生成，不使用 AI 图像或第三方模型。

| 资源 | 几何与用途 |
| --- | --- |
| `heat_duct.obj` | 804 三角面、13 组：中心骨架、六个封头、六个连接臂；运行时只显示实际连接方向 |
| `heating_plate.obj` | 180 三角面、6 组：满格耐火加热块，顶面齐平 |
| `heat_transport_atlas.png` | 与塔体相同的最近邻图集；OBJ 的 MTL 已指向本文件 |
| `preview.html` / `preview.png` | 侧面进热的 3×3 加热板与分支管道组合展示 |
| `components.html` / `components/preview.png` | 直通、弯头、三通、六向接头及单块板的近景 |
| `model.json` | 包围盒、面数、接触面积与六向 mask 位定义 |

每个模型采用方块局部坐标 `[0,1]³`，无需按机器水平朝向旋转。热管中心骨架范围为 `[0.20,0.80]³`；连接管身截面为 `[0.25,0.75]`，延伸至对应方块边界。端法兰截面为 `[0.20,0.80]`，全部几何保持在本格内。

六向位序与 Minecraft `Direction.get3DDataValue()` 一致：下 0、上 1、北 2、南 3、西 4、东 5。`core` 始终显示；某方向连接时显示 `arm_<方向>` 并隐藏 `cap_<方向>`。渲染器与选择/碰撞形状共用连接判定。

加热板只有水平方向接入热管/相邻板，顶部专用于输出。`heat_contact` 只含一个 `Y=1`、法线向上的完整四边形，导热面积为 1；顶板侧壁归入 `plate_roof_edges`。并排九块的接触面积为 9，没有进入上方炉体空间的模型突出件。

游戏运行时使用 `ssite:obj/heat_duct.obj`、`ssite:obj/heating_plate.obj` 及 `ssite:textures/machine/fluidized_bed_combustor.png`。BER 根据实际连接绘制热管。静态世界模型保留关闭的管芯和完整加热板作为兜底；存在有效方块实体后，由模型包装器隐藏静态模型。物品栏使用独立 JSON 几何。

## 生成与检查

在仓库根目录执行：

```powershell
python scripts/models/build_heat_transport.py
python scripts/models/install_fluidized_bed.py
python scripts/models/inspect_fluidized_bed.py --smoke --input art/heat_transport/preview.html --output art/heat_transport
python scripts/models/inspect_fluidized_bed.py --smoke --input art/heat_transport/components.html --output art/heat_transport/components
```

生成依赖 Python 3 与 Pillow；截图检查另需 Playwright 和 Chromium。生成器校验坐标、法线、UV、绕序、64 种连接开闭及单格加热面积。Java 渲染测试还检查真实 OBJ 的 QUADS 输出、光照传递、六向臂和九板接触面；游戏行为与最终测试状态见 [玩法文档](../../docs/fluidized-bed.md)。
