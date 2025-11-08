> Last updated on November 08, 2025 | [History][History]

> The content on this page corresponds to version [**1.1.0**][LatestVersion] of the LuaInMinecraftBukkit II plugin. Historical documentation can be found in the history of this page.

> **!! The content of this file has machine translation !!** | [Origin](../zh/Luacage.md)

Luacage is a package manager implementation in the **LuaInMinecraftBukkit II** plugin, which can easily install Lua software packages in Minecraft Bukkit-like servers, and automatically handle dependency installation and removal.

This chapter will explain how to use Luacage.

## Usage of Luacage

Using Luacage on the server is very simple; you just need to use the `/luacage` command in your Minecraft Bukkit server. After typing the `/luacage` command, you should see corresponding command prompts.

However, when you first start using it, you need to pull the package index from online. At this time, use `luacage update` to update the local package index. After that, you can install the packages you want according to the help guide.

After installing a package, it may not take effect immediately. You need to use the `/lua reload` command or `/lua reloadEnv [Environment ID]` command to hard reload the Lua environment before you can use it.

### Installing Local Packages

If you have a local Lua package or you created one yourself, you can move it to the `/plugins/LuaInMinecraftBukkitII/package/packages` folder within the plugin configuration directory. Then, use `luacage install [env id] local/[your package name]` to install your local package. If your console allows **TAB** completion, you can press the **TAB** key to select the local package you want to install.

## Creating Your Own Lua Package

You might want to try writing your own Lua package. This section will explain how to create your own package.

### Package Storage Location

The **LuaInMinecraftBukkit II** plugin places all packages in the `/plugins/LuaInMinecraftBukkitII/package/packages` folder. This folder is where your actually downloaded and installed Lua packages are placed. In this folder, every subfolder is an independent Lua package, which can be installed using commands or created by yourself.

### Package Structure and Manifest Information

Every Luacage package is an independent folder, and the name of each folder is your actual package name. Furthermore, the folder must contain a file named `package.lua`, which is used to indicate some information about the current package.

The format of the `package.lua` file is as follows:

```lua
-- do something, like check runnable ...
return {
    -- Package name, same as the folder name
    name = "SayHello",
    -- Package version number
    version = "0.0.0",
    -- Package authors
    authors = { "SmileYik" },
    -- Package description
    description = "This is a package that allow player get greeting when join server",
    -- Required Lua version. This is not checked for now, so pass nil
    luaVersion = nil,
    -- Other Luacage packages dependencies. E.g., if depending on the json package: dependPackages = {"json"}
    dependPackages = {},
    -- Other Bukkit plugin dependencies. E.g., if depending on the PlaceholderAPI plugin: dependPlugins = {"PlaceholderAPI"}
    dependPlugins = {},
    -- Main run file (entry file). If there is no main run file, fill in: main = nil 
    -- If an entry file exists, you only need to fill in the path relative to the package root directory (the folder where package.lua is located).
    -- For example, a package named `SayHello` has three files: `SayHello/package.lua`, `SayHello/greeting.lua`, `SayHello/main/main.lua`
    -- If you want `SayHello/greeting.lua` as the entry file, you only need to fill in `greeting.lua`.
    -- If you want `SayHello/main/main.lua` as the entry file, you only need to fill in `main/main.lua`
    main = "greeting.lua",
    -- Whether it is runnable. true means runnable, false means not supported
    -- You can run checks before the `return` keyword to determine if it can run, and then return the result here.
    runnable = true,
    -- Reason for not being runnable.
    reason = ""
}
```

Taking the `SayHello` package as an example, its package structure is as follows:

```
tree packages/SayHello/
packages/SayHello/
├── greeting.lua
└── package.lua
```

And the content of `package.lua` is consistent with the format listed above.

### Using Other People's Packages in Your Own Lua Scripts

After a Lua software package is installed, if it does not have a main run file, it will be treated as a dependency package and will be applied immediately. At this point, even without restarting the Lua environment, you can directly call the Lua scripts written in that dependency package from your own scripts.

To use the Lua scripts from a package created by others within your own Lua script, you can use the `require` statement to import it. The specific `require` import format is `require("@package name/file to import")`. The `@package name` symbol will be replaced with the actual path of the package name during the loading process.

Here is a more detailed example for better understanding:

Assume there is a software package named `json`, and its storage directory structure on disk is as follows:

```bash
❯ tree json
json
├── json.lua
├── LICENSE
└── package.lua

1 directory, 3 files
```

Now, if we need to call the `json.lua` module from this package in our own script, we can import it into our script using the statement `require("@json/json")` and then use it.

Similarly, suppose this time I want to know the version of the `json` package. The `package.lua` returns a table, and the `version` field in this table will contain the version information of the `json` package. At this point, you can get the version information of the `json` package with the following code:

```lua
local jsonMeta = require "@json/package"
local jsonVersion = jsonMeta.version
```

### Importing Your Other Lua Scripts Within Your Own Package

Since Luacage actually loads scripts by reference, which means it will eventually be redirected to load the Luacage package under the `/plugins/LuaInMinecraftBukkitII/package/packages` folder, this causes relative path queries like `./` to fail.

For the Luacage package itself, when using the `require` statement to import scripts within its own package, it must use the same method as importing scripts from other people's packages in the section **Using Other People's Packages in Your Own Lua Scripts**, which is to use `require("@package name/file to import")` to import scripts within its own package.

### How to Install Your Own Created Package in the Lua Environment

You only need to ensure your package is placed in the `/plugins/LuaInMinecraftBukkitII/package/packages` folder. Then, follow the method in **Installing Local Packages** and directly use `luacage install [env id] local/[package name]` to install it. For example: `luacage install default local/SayHello` to install the local `SayHello` package.

## Data Source

Luacage allows you to edit the data source used by editing the configuration file (`config.json`). Every data source should have at least a **Data Source Name** and a **Data Source URL**. The configuration of the data source can be found in the [Configuration chapter][Configuration].

By default, if a data source named `default` is not configured, **LuaInMinecraftBukkit II** will automatically create a `default` data source.

### Self-Built Data Source

The data source used by Luacage is simple. You can create your own data source through the content of this section. If you are not interested in self-built data sources, you can skip this section.

Luacage will look for the `packages` directory, the `luacage.json` file, and the `luacage.json.hash` file under the configured base URL. The meanings of these directories and files are as follows:

  + `packages` directory: This directory is used to store various Lua packages. Its first layer of subfolders are the package names (or package IDs, the unique identifiers of the packages).
  + `luacage.json` file: This file stores the metadata for all packages in `packages`, such as version, description, author, and dependencies (plugins), etc.
  + `luacage.json.hash` file: This file is a plain text file containing the `SHA-256` hash value of `luacage.json`, used to ensure Luacage gets the correct `luacage.json`.

In addition, **LuaInMinecraftBukkit II** provides a startup option to automatically update `luacage.json`. It can automatically detect packages in `packages` and update `luacage.json` and `luacage.json.hash` accordingly. In Linux, you can simply use the following commands to update the package data in the `./repo` directory:

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

You can also refer to the automatic build script of the official Luacage data source repository.

[history]: https://github.com/SmileYik/LuaInMinecraftBukkitII/commits/gh-page/docs/en/Luacage.md
[latestversion]: https://github.com/SmileYik/LuaInMinecraftBukkitII/tree/tags/1.1.0
[configuration]: ./Configuration.md