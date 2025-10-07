[History]: https://github.com/SmileYik/LuaInMinecraftBukkitII/commits/gh-page/docs/zh/Luacage.md
[LatestVersion]: https://github.com/SmileYik/LuaInMinecraftBukkitII/tree/tags/1.1.0
[ResourceRepo]: https://github.com/SmileYik/LuaInMinecraftBukkitII/tree/gh-page
[Configuration]: ./Configuration.md

> 最后更新于2025年10月07日 | [历史记录][History]

> 此页面内容对应于 LuaInMinecraftBukkit II 插件的 [**1.1.0**][LatestVersion] 版本, 历史文档可以插件此页面的历史记录

Luacage 是一个 **LuaInMinecraftBukkit II** 插件中的包管理器实现, 能够在 Minecraft Bukkit 类服务器中轻易做到安装 Lua 软件包, 并自动安装依赖及管理删除依赖等操作.

这一章节将会解释 Luacage 的用法.

## 数据源

Luacage 允许通过编辑配置文件(`config.json`)来编辑所使用的数据源. 每个数据源都应该至少拥有一个**数据源名**以及**数据源URL**, 数据源的配置可以见 [配置文件章节][Configuration].

默认情况下, 若没有配置名为 `default` 数据源, **LuaInMinecraftBukkit II** 将会自动创建一个 `default` 数据源.

### 自建数据源

Luacage 所使用的数据源很简单, 您可以通过本节内容自己创建一个自己的数据源, 如果您对于自建数据源不感兴趣, 可以跳过本节内容.

Luacage 会从配置的基础URL下寻找 `packages` 目录, `luacage.json` 文件 以及 `luacage.json.hash` 文件, 这些目录及文件含义如下:

+ `packages` 目录: 这个目录用于存放各种 Lua 包,  它的第一层子文件夹都为包名(或者叫包的ID, 包的唯一标识符)
+ `luacage.json` 文件: 这个文件存放 `packages` 中所有包的元数据, 如版本, 简介, 作者和依赖包(插件)等等信息.
+ `luacage.json.hash` 文件: 这个文件为纯文本文件, 为 `luacage.json` 的 `SHA-256` 散列值, 用于保证 Luacage 在获取到正确的 `luacage.json`

此外, **LuaInMinecraftBukkit II** 提供了自动更新 `luacage.json` 的启动项, 能够自动检测 `packages` 中的包, 并以此更新 `luacage.json` 以及 `luacage.json.hash`. 可以在linux下, 简单的使用以下指令去更新 `./repo` 目录中的包数据:

```bash
git clone https://github.com/SmileYik/LuaInMinecraftBukkitII.git
cd LuaInMinecraftBukkitII
git submodule update --init --recursive
chmod +x ./gradlew
mkdir run
echo "eula=true" > run/eula.txt
luainminecraftbukkit_luacage_build="$(pwd)/../repo" ./gradlew runServer -PWorkflow=YES -PTargetJava=21
grep -q "org.eu.smileyik.luaInMinecraftBukkitII" ./run/logs/latest.log && { echo "存在报错信息，终止！" >&2; exit 1; }
cd ..
```

此外你也可以去参考官方 Luacage 数据源仓库的自动构建脚本.

## Luacage 的使用

在服务器中使用 Luacage 非常简单, 仅需要在 Minecraft Bukkit 服务器中使用 `/luacage` 指令即可. 在输入 `/luacage` 指令后, 应该会有相应的指令提示.

不过在刚开始使用时, 需要从线上拉取软件包索引, 此时使用 `luacage update` 即可更新本地中的软件包索引, 之后就可以依照帮助去安装您想要的软件包了.

在安装完软件包后, 该软件包可能不会立即生效, 需要使用 `/lua reload` 指令或者 `/lua reloadEnv 环境ID` 指令去硬重载 Lua 环境后才能使用.

## 在自己Lua脚本中使用别人的包

在安装完 Lua 的软件包后, 若该软件包没有主要运行的文件, 则会被当作依赖包并且会立即被应用. 此时就算是不重启Lua环境, 也可以直接在你的脚本中调用该依赖包中写好的Lua脚本.

为了在自己的Lua脚本中使用其他人制作的软件包中的Lua脚本, 可以使用 `require` 语句去导入, 具体 `require` 导入格式为 `require("@包名/要导入的文件")`.

这里举一个较为详细的例子加深理解:

假设有个名为 `json` 的软件包, 这个包在磁盘中的存储目录结构如下:

```bash
❯ tree json
json
├── json.lua
├── LICENSE
└── package.lua

1 directory, 3 files
```

此时我们在自己的脚本中需要调用该包中的 `json.lua` 模块, 就可以通过语句 `require("@json/json")` 导入到你自己的脚本中, 然后再进行使用.

同样的, 假设我这次想知道 `json` 包的版本, 而 `package.lua` 中会返回一个表, 这个表中`version`字段将会含有 `json` 包的版本信息. 此时就可以通过以下代码去获取 `json` 包的版本信息:

```lua
local jsonMeta = require "@json/package"
local jsonVersion = jsonMeta.version
```
