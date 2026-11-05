# Ore Control — Fabric 26.1.2 移植版

基于 masa 的 Ore Control 0.4.3（原仓库提交 `6323cb359b21406395958f257c10de4298c19cbf`）。本移植由用户委托制作，修改于 2026-11-05，属于非官方移植，沿用 LGPL-3.0。原始仓库为 https://github.com/maruohon/orecontrol 。

源码与发布地址为 https://github.com/unionofblackbean/orecontrol-fabric 。

## 安装与配置

需要 Minecraft Java 26.1.2、Java 25 和 Fabric Loader 0.18.4 或更新兼容版本。将 `orecontrol-fabric-0.4.3-port.2+26.1.2.jar` 放入实例的 `mods` 文件夹。无需额外安装 Fabric API。专用服务器只需在服务器安装，单人游戏安装在客户端。

首次启动生成 `config/orecontrol/orecontrol.cfg`。在配置中把对应的 `B:disable…=false` 改为 `true`，下次服务器启动或单人世界重新打开时生效。全部开关默认关闭。修改影响后续生成的区块。

旧版 `.cfg` 的分类、原有布尔键、默认值和按维度完整覆盖规则均保留。新增 `Minable.disableCOPPER` 与 `Minable.disableANCIENT_DEBRIS` 对应新版本矿物。矿物总开关也覆盖现代噪声阶段的大型铁、铜矿脉及其中的粗矿块。

## 各配置项

| 分类 | 开关后缀 | 控制内容 |
| --- | --- | --- |
| Minable | COAL / DIAMOND / GOLD / IRON / LAPIS / REDSTONE / QUARTZ / EMERALD | 对应矿石，包括深层变种和下界金矿 |
| Minable | DIRT / GRAVEL / DIORITE / GRANITE / ANDESITE | 岩石中的对应资源团块 |
| Minable | SILVERFISH | 蠹虫寄生石块 |
| Minable | COPPER / ANCIENT_DEBRIS | 铜矿和远古残骸 |
| Minable | disableAllVanillaGeneration | 原版资源团块与矿脉总开关，原版岩浆块由 Populate 控制 |
| Minable | CUSTOM | 模组或数据包自有命名空间的矿物地物，以及生成模组方块的矿物地物 |
| Decorate | TREE / BIG_SHROOM / SHROOM | 树木（含倒木与下界巨型菌）、巨型蘑菇、普通蘑菇 |
| Decorate | CACTUS / DEAD_BUSH / LILYPAD / FLOWERS / GRASS / PUMPKIN / REED | 仙人掌、枯灌木、睡莲、花、草和蕨、南瓜、甘蔗 |
| Decorate | CLAY / SAND / SAND_PASS2 | 河床黏土、沙盘、沙砾盘 |
| Decorate | DESERT_WELL / FOSSIL / ROCK / ICE | 沙漠水井、化石地物、苔石堆、冰刺和冰面补丁 |
| Decorate | LAKE_WATER / LAKE_LAVA | 主世界水泉与熔岩泉，延续旧版装饰事件含义 |
| Populate | DUNGEON / FIRE / GLOWSTONE | 怪物房、下界火焰与灵魂火、萤石 |
| Populate | ICE / ANIMALS | 地形生成时的冻结与积雪、区块初始动物生成 |
| Populate | LAKE / LAVA | 水湖地物、熔岩湖地物 |
| Populate | NETHER_LAVA / NETHER_LAVA2 / NETHER_MAGMA | 下界开放熔岩泉、封闭熔岩泉、岩浆块团块 |
| Custom | disableSurfaceLavaLakes | 阻止海平面以上或距地表不足 8 格的熔岩湖，允许地下熔岩湖 |

除了明确写全的键名，其余后缀前都添加 `disable`，例如 `B:disableTREE=true`。

## 按维度设置

先在主配置的 `Generic` 分类设置 `B:enablePerDimensionConfigs=true`。以下文件存在时，对应维度使用该文件的完整设置，未指定的键默认为 false。

- `orecontrol_dim0.cfg` 对应主世界。
- `orecontrol_dim-1.cfg` 对应下界。
- `orecontrol_dim1.cfg` 对应末地。
- 新格式 `dimensions/<namespace>/<dimension_path>.cfg` 支持任意现代维度，例如 `dimensions/minecraft/the_nether.cfg`。同一维度存在两种格式时，新格式优先。

所有路径都相对于 `config/orecontrol/`。

## 跨版本语义与验证边界

Minecraft 26.1.2 的地形算法与 1.12.2 已有很大变化，本移植保留配置开关的用途。原版新版本已不再自然放置旧式水湖，`Populate.disableLAKE` 保留并可控制使用固定水状态的等价 LakeFeature，例如数据包重新加入的水湖。熔岩湖开关支持固定熔岩状态的 LakeFeature。泉水与湖泊分别控制。

Fabric 没有 Forge 1.12.2 的 CUSTOM 事件。`Minable.disableCUSTOM` 使用上述现代矿物地物分类，`Decorate.disableCUSTOM` 与 `Populate.disableCUSTOM` 均阻止非 minecraft 命名空间的非矿物配置地物。这两个非矿物 CUSTOM 开关在本移植中作用相同。直接绕过配置地物系统写入世界的其他模组需要专门适配。

测试覆盖原版配置地物、主世界和下界实际区块生成、旧配置读取和维度覆盖。未对全部第三方世界生成模组或图形客户端逐一验证，因此无法承诺任意整合包中的行为完全等同于旧 Forge 版。

## Loader 兼容范围

Loader 最低版本要求为 `>=0.18.4`。开发依赖默认使用 0.18.4，可通过 `"-Ploader_version=0.20.1"` 等参数切换测试用 Loader。Minecraft 版本要求为 26.1.2，Java 最低版本为 25。

已分别在 Loader 0.18.4 与 0.20.1 上通过全部现有专用服务器集成测试，包含 41 项开关映射、动物生成拦截、主世界和下界实际区块生成。Loader 0.18.4 的旧 Mixin 子系统会输出 JAVA_25 兼容级别提示，本次测试完整通过。

## 构建与复现

在 Java 25 环境运行 `gradlew.bat build`。成品与源码 JAR 位于 `build/libs`。执行 `gradlew.bat runIntegrationServer` 启动本地专用测试服务器，测试结果写入 `build/integration-run/test-results.txt`。测试资源属于独立 integration 源码集，不会装入成品 JAR。

测试任务会为自动生成的本地测试服务器写入 `eula=true`，仅绑定本机并选择随机端口，使用独立世界。运行测试任务前请确认你已接受 Minecraft EULA。

许可证全文见 COPYING.txt 与 COPYING.LESSER.txt。再分发本移植时请同时提供对应源码与修改说明。
