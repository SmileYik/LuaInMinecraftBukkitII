> Last updated on November 08, 2025 | [History][History]

> The content on this page corresponds to version [**1.1.0**][LatestVersion] of the LuaInMinecraftBukkit II plugin. Historical documentation can be found in the history of this page.

> **!! The content of this file has machine translation !!** | [Origin](./../zh/CodeCompletion.md)

This chapter will discuss how to achieve Lua script syntax completion, including but not limited to syntax completion for the **LuaInMinecraftBukkit II API**, **Bukkit API**, **Java Standard API**, and **Kyori Adventure API**.

## LuaLS

Currently, syntax completion relies on [LuaLS][LuaLS]. You can install it in **VSCode**, **NeoVim**, or **JetBrains IDE**. You can choose any of these three editors to install the [LuaLS][LuaLS] plugin. For specific installation methods, please visit its official website.

## LuaInMinecraftBukkitII-LLS-Addon

[LuaInMinecraftBukkitII-LLS-Addon][LII-A] is a plugin for [LuaLS][LuaLS]. This plugin is used to provide API definitions for your Lua project and to analyze the Lua code you write, automatically marking type annotations where necessary.

### LuaInMinecraftBukkitII-LLS-Addon Structure

```shell
.
├── README.md        # Documentation file
├── config.json      # LuaLS plugin configuration file
├── library          # Folder for storing API definitions
│   ├── LII          # LII plugin global variable definitions
│   ├── LII-api      # LII plugin Lua API definitions
│   ├── luajava      # luajava definitions
│   └── luajava-api  # luajava api definitions
└── plugin.lua       # LuaLS plugin
```

The `library` folder only contains basic API definitions. If you need to extend the API definitions, you can use the generator to create API definition files, place them in the `library` folder, and then restart the LSP server.

### LuaInMinecraftBukkitII-LLS-Addon Automatic Type Annotation

The automatic type annotation feature forms the basis for auto-completion, as correct identification of variable types is necessary to find the actual API definition file and ultimately achieve code completion.

The automatic type annotation feature will track:

  + The return type of `luajava.bindClass` / `luajava.createProxy` / `luajava.newInstance`
  + The variable type passed to `luajava.new`
  + The parameter types in Bukkit event handling methods
  + The parameter types in Bukkit command handlers

### Using LuaInMinecraftBukkitII-LLS-Addon in VSCode

#### Global Usage

To use it globally, edit the [LuaLS][LuaLS] plugin configuration. You only need to set two configurations:

  + `Lua.runtime.plugin`: `/path/to/LuaInMinecraftBukkitII-LLS-Addon/plugin.lua`
  + `Lua.workspace.library`: `["/path/to/LuaInMinecraftBukkitII-LLS-Addon/"]`

Replace `/path/to/LuaInMinecraftBukkitII-LLS-Addon/` with the actual file path of LuaInMinecraftBukkitII-LLS-Addon on your computer.

#### Project-Specific Usage

If you only want to use the [LuaLS][LuaLS] plugin within the current project directory, you need to create a directory named `.vscode` in the project root, and then create a `setting.json` file inside it. Write the following content in this file (you need to replace `/path/to/LuaInMinecraftBukkitII-LLS-Addon/` with the actual file path of LuaInMinecraftBukkitII-LLS-Addon on your computer):

```json
{
    "Lua.runtime.plugin": "/path/to/LuaInMinecraftBukkitII-LLS-Addon/plugin.lua",
    "Lua.workspace.library": [
        "/path/to/LuaInMinecraftBukkitII-LLS-Addon/"
    ]
}
```

## LuaInMinecraftBukkitII-LLS-Generator

[LuaInMinecraftBukkitII-LLS-Generator][LII-G] is a [LuaLS][LuaLS] readable Lua API generator, designed to automatically read `Java` source code files to generate Lua API files.

For specific usage instructions, please see the repository page.

[History]: https://github.com/SmileYik/LuaInMinecraftBukkitII/commits/gh-page/docs/en/CodeCompletion.md
[LatestVersion]: https://github.com/SmileYik/LuaInMinecraftBukkitII/tree/tags/1.1.0

[LuaLS]: https://luals.github.io/
[LII-G]: https://github.com/SmileYik/LuaInMinecraftBukkitII-LLS-Generator
[LII-A]: https://github.com/SmileYik/LuaInMinecraftBukkitII-LLS-Addon

[P1]: https://github.com/SmileYik/LuaInMinecraftBukkitII-LLS-Addon/blob/master/docs/example.png?raw=true