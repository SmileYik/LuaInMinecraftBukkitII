[History]: https://github.com/SmileYik/LuaInMinecraftBukkitII/commits/gh-page/docs/zh/CodeCompletion.md
[LatestVersion]: https://github.com/SmileYik/LuaInMinecraftBukkitII/tree/tags/1.1.0

> 最后更新于2025年11月08日 | [历史记录][History]

> 此页面内容对应于 LuaInMinecraftBukkit II 插件的 [**1.1.0**][LatestVersion] 版本, 历史文档可以插件此页面的历史记录

本章内容将讲述如何实现 Lua 脚本语法补全, 包括但不限于 **LuaInMinecraftBukkit II API**, **Bukkit API**, **Java 标准API**, **Kyori Adventure API** 的语法补全.

![P1]

## LuaLS

目前语法补全依赖于 [LuaLS], 你可以在 **VSCode**, **NeoVim** 或者 **JetBrains IDE** 中安装它, 你可以在上述三个编辑器中任意选择一个你喜欢的编辑器去安装 [LuaLS] 插件, 具体安装方式可以前往它的官网查看.

## LuaInMinecraftBukkitII-LLS-Addon

[LuaInMinecraftBukkitII-LLS-Addon][LII-A] 是 [LuaLS] 的一个插件, 这个插件用于为你的 Lua 项目提供API的定义, 并分析你所编写的Lua代码, 在需要标记类型注解的地方为你自动标记类型注解.

### LuaInMinecraftBukkitII-LLS-Addon 结构

```shell
.
├── README.md        # 说明文件
├── config.json      # LuaLS 插件配置文件
├── library          # 用于存放 API 定义的文件夹
│   ├── LII          # LII 插件全局变量定义
│   ├── LII-api      # LII 插件 Lua API 定义
│   ├── luajava      # luajava 定义
│   └── luajava-api  # luajava api 定义
└── plugin.lua       # LuaLS 插件
```

`library` 文件夹中仅带有基础的API定义, 若需要拓展API定义, 则可以使用生成器生成 API 定义文件, 然后再放入 `library` 文件夹中, 最后重启 LSP 服务器即可.

### LuaInMinecraftBukkitII-LLS-Addon 自动类型标记

自动类型标记功能奠定了自动补全的基础, 因为只有正确的识别到了变量的类型, 才能找到实际的 API 定义文件, 最后实现代码补全.

自动类型标记功能将会追踪:

+ `luajava.bindClass` / `luajava.createProxy` / `luajava.newInstance` 的返回类型
+ `luajava.new` 传入的变量类型
+ Bukkit 事件处理方法中的形参的类型
+ Bukkit 指令处理器中的形参的类型

### 在 VSCode 中使用 LuaInMinecraftBukkitII-LLS-Addon

#### 在全局中使用

编辑 [LuaLS] 插件配置, 仅需要设定两个配置即可全局使用:

+ `Lua.runtime.plugin`: `/path/to/LuaInMinecraftBukkitII-LLS-Addon/plugin.lua`
+ `Lua.workspace.library`: `["/path/to/LuaInMinecraftBukkitII-LLS-Addon/"]`

将 `/path/to/LuaInMinecraftBukkitII-LLS-Addon/` 换为 LuaInMinecraftBukkitII-LLS-Addon 在你电脑中真实的文件路径即可.

#### 仅在项目中使用

若仅仅想在当前项目目录中使用 [LuaLS] 插件, 则需要在项目根目录下创建一个名为 `.vscode` 目录, 再在其中创建一个 `setting.json` 文件, 在该文件中写入如下内容(你需要将 `/path/to/LuaInMinecraftBukkitII-LLS-Addon/` 换为 LuaInMinecraftBukkitII-LLS-Addon 在你电脑中真实的文件路径):

```json
{
    "Lua.runtime.plugin": "/path/to/LuaInMinecraftBukkitII-LLS-Addon/plugin.lua",
    "Lua.workspace.library": [
        "/path/to/LuaInMinecraftBukkitII-LLS-Addon/"
    ]
}
```

## LuaInMinecraftBukkitII-LLS-Generator

[LuaInMinecraftBukkitII-LLS-Generator][LII-G] 是一个 [LuaLS] 可读的 Lua API 生成器, 皆在自动读取 `Java` 源代码文件以生成 Lua API 文件.

具体使用方法可以见仓库页面.


[LuaLS]: https://luals.github.io/
[LII-G]: https://github.com/SmileYik/LuaInMinecraftBukkitII-LLS-Generator
[LII-A]: https://github.com/SmileYik/LuaInMinecraftBukkitII-LLS-Addon

[P1]: https://github.com/SmileYik/LuaInMinecraftBukkitII-LLS-Addon/blob/master/docs/example.png?raw=true