[History]: https://github.com/SmileYik/LuaInMinecraftBukkitII/commits/gh-page/docs/zh/Luacage.md
[LatestVersion]: https://github.com/SmileYik/LuaInMinecraftBukkitII/tree/tags/1.1.0
[ResourceRepo]: https://github.com/SmileYik/LuaInMinecraftBukkitII/tree/gh-page
[Configuration]: ./Configuration.md

> 最后更新于2025年11月08日 | [历史记录][History]

> 此页面内容对应于 LuaInMinecraftBukkit II 插件的 [**1.1.0**][LatestVersion] 版本, 历史文档可以插件此页面的历史记录

Luacage 是一个 **LuaInMinecraftBukkit II** 插件中的包管理器实现, 能够在 Minecraft Bukkit 类服务器中轻易做到安装 Lua 软件包, 并自动安装依赖及管理删除依赖等操作.

这一章节将会解释 Luacage 的用法.

## Luacage 的使用

在服务器中使用 Luacage 非常简单, 仅需要在 Minecraft Bukkit 服务器中使用 `/luacage` 指令即可. 在输入 `/luacage` 指令后, 应该会有相应的指令提示.

不过在刚开始使用时, 需要从线上拉取软件包索引, 此时使用 `luacage update` 即可更新本地中的软件包索引, 之后就可以依照帮助去安装您想要的软件包了.

在安装完软件包后, 该软件包可能不会立即生效, 需要使用 `/lua reload` 指令或者 `/lua reloadEnv 环境ID` 指令去硬重载 Lua 环境后才能使用.

### 安装本地包

如果你拥有一个本地lua包或者是你自己创建了一个lua包, 你可以将其移动到插件配置目录中的 `/plugins/LuaInMinecraftBukkitII/package/packages` 文件夹下, 之后使用 `luacage install [env id] local/[你的包名]` 去安装你的本地包. 如果你的控制台允许使用 **TAB** 补全, 则你可以按下 **TAB** 键去选择你要安装的本地包.

## 自己制作 Lua 包

你可能想尝试自己去编写自己的 Lua 包. 在本节将会说明如何制作自己的包.

### 包的存储位置

**LuaInMinecraftBukkitII** 插件将所有包都放置在 `/plugins/LuaInMinecraftBukkitII/package/packages` 文件夹下. 这个文件夹是你实际下载安装的 Lua 包实际放置的文件夹, 在这个文件夹中, 每一个子文件夹都是一个独立的 Lua 包, 其中的 Lua 包可以是使用指令安装的, 也可以是你自己创建的.

### 包的结构和标示信息

每一个 Luacage 包都是一个独立的文件夹, 每个文件夹的名字都为你的实际的包名, 并且在文件夹中一定需要包含一个名为 `package.lua` 的文件, 用于标示当前包的一些信息.

`package.lua` 文件的格式如下:

```lua
-- do something, like check runnable ...
return {
    -- 包名, 和文件夹相同
    name = "SayHello",
    -- 包的版本号
    version = "0.0.0",
    -- 包的作者
    authors = { "SmileYik" },
    -- 包的描述
    description = "This is a package that allow player get greeting when join server",
    -- 所需要的 Lua 版本, 这里暂时不会进行检测, 传入 nil 即可
    luaVersion = nil,
    -- 依赖的其他 Luacage 包. 例如依赖 json 包, 则为: dependPackages = {"json"}
    dependPackages = {},
    -- 依赖的其他 Bukkit 插件. 例如依赖 PlaceholderAPI 插件, 则为: dependPlugins = {"PlaceholderAPI"}
    dependPlugins = {},
    -- 主要运行文件(入口文件), 如果没有主要运行文件则填入: main = nil 
    -- 若存在入口文件, 则只需要填相对于包根目录的路径(package.lua所在文件夹)就可以了.
    -- 例如有个名为 `SayHello` 的包, 其拥有三个文件: `SayHello/package.lua`, `SayHello/greeting.lua`, `SayHello/main/main.lua`
    -- 若希望 `SayHello/greeting.lua` 作为入口文件, 则仅需要填入 `greeting.lua`.
    -- 若希望 `SayHello/main/main.lua` 作为入口文件, 则仅需要填入 `main/main.lua`
    main = "greeting.lua",
    -- 是否能够运行, true 代表能够运行, false 代表不支持运行
    -- 你可以在 `return` 关键字前运行检测, 以判断是否能够运行, 再在这返回结果.
    runnable = true,
    -- 不支持运行的原因.
    reason = ""
}
```

我们以 `SayHello` 包为例, 其包结构如下:

```
tree packages/SayHello/
packages/SayHello/
├── greeting.lua
└── package.lua
```

而 `package.lua` 的内容如上述所列格式一致.

### 在自己Lua脚本中使用别人的包

在安装完 Lua 的软件包后, 若该软件包没有主要运行的文件, 则会被当作依赖包并且会立即被应用. 此时就算是不重启Lua环境, 也可以直接在你的脚本中调用该依赖包中写好的Lua脚本.

为了在自己的Lua脚本中使用其他人制作的软件包中的Lua脚本, 可以使用 `require` 语句去导入, 具体 `require` 导入格式为 `require("@包名/要导入的文件")`. 其中 `@包名` 符号会在加载过程中被替换成包名的实际路径.

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

### 在自己的包中导入自己的其他Lua脚本

由于 Luacage 实际上加载脚本方式是以引用方式加载的, 即最后会被重定向到 `/plugins/LuaInMinecraftBukkitII/package/packages` 文件夹下加载 Luacage 包, 这会导致相对位置 `./` 查询失效. 

对于 Luacage 包本身来讲, `require` 语句导入自己包内的脚本文件时, 需要和 **在自己Lua脚本中使用别人的包** 这一节中导入其他人的包中脚本文件方式一样, 也就是使用 `require("@包名/要导入的文件")` 导入自己包内的脚本文件.

### 怎么在Lua环境中安装自己制作的包

你只需要确保自己的包放置在 `/plugins/LuaInMinecraftBukkitII/package/packages` 文件夹下, 之后按照 **安装本地包** 的方法, 直接使用 `luacage install [env id] local/[包名]` 即可安装, 例如: `luacage install default local/SayHello` 去安装本地的 `SayHello` 包.

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