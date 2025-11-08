[History]: https://github.com/SmileYik/LuaInMinecraftBukkitII/commits/gh-page/docs/Configuration.md
[LatestVersion]: https://github.com/SmileYik/LuaInMinecraftBukkitII/tree/tags/1.0.9
[ResourceRepo]: https://github.com/SmileYik/LuaInMinecraftBukkitII/tree/gh-page

> Last updated on October 03, 2025 | [History][History]

> This page corresponds to version [**1.1.0**][LatestVersion] of the LuaInMinecraftBukkit II plugin. Historical documentation can be found in the history of this page.

> **!! The content of this file has machine translation !!** | [Origin](./../zh/Configuration.md)

This document provides the default plugin configuration settings and their descriptions.

-----

### config.json

`config.json` is the main configuration file for the plugin. In this file, you can configure the plugin's dependency download source, Lua environment, and other information.
At the same time, this configuration file is not a standard `json` type file; it allows single-line text starting with `//` as comments.

| Configuration Name | Type | Description |
| :-: | :-: | :-: |
| projectUrl | Link Text | [Project Resource Address][ResourceRepo], which can be found in the repository. Since version **1.0.8**, project resource branches will have tags that match the plugin version. The tag naming format is `resources-[plugin version]`. For example, if the plugin version is 1.0.8, there will be a resource branch `resources-1.0.8`. In this case, the link `https://raw.githubusercontent.com/SmileYik/LuaInMinecraftBukkitII/refs/tags/resources-1.0.8` can be filled in the configuration. Starting from version 1.1.0, the variable ${version} can be used to automatically input the current version number, e.g., https://raw.githubusercontent.com/SmileYik/LuaInMinecraftBukkitII/refs/tags/resources-${version} |
| luaVersion | `luajit` / `lua-5.2.4` / `lua-5.3.6` / `lua-5.4.8` | Sets the Lua version used by the plugin. |
| alwaysCheckHashes  | `true` / `false` | Always fetch the latest dependencies from the resource repository. |
| justUseFirstMethod | `true` / `false` | When multiple optional methods are detected in Lua, does it always select the first candidate instead of throwing an exception |
| debug | `true` / `false` | Enable Debug logs. |
| bStats | `true` / `false` | Enable bStats statistics. |
| luacage | Lua Package Manager Configuration | Configures the Luacage package manager. |
| luaReflection | Lua Reflection Configuration | Configures the reflection for Lua. |
| luaState | Lua Environment Configuration | Configures the Lua environment. |
| enableModules | List of Texts | Enable modules. Currently available modules include `cffi`. |

#### Luacage - Lua Package Manager Configuration

The configuration snippet for the Lua package manager theme is as follows:

```json
  "luacage": {
    // luacage source repository configuration
    "sources": [
      {
        // Source repository name
        "name": "default",
        // Source repository URL
        "url": "https://raw.githubusercontent.com/SmileYik/luacage-demo/refs/heads/master/repo/"
      }
    ]
  },
```

Currently, the **`sources`** field can be configured, which specifies the source of the packages. The **`sources`** field accepts an array of objects, and the configuration items for its object entities are as follows:

```json
{
  "name": "source name",
  "url": "source url"
}
```

#### Lua Reflection - Lua Reflection Configuration

The main configuration snippet for Lua reflection is as follows:

```json
  "luaReflection": {
    "type": "default",
    "cacheCapacity": 1024
  },
```

The description for each configuration item is in the following table:

| Configuration Name | Type | Description |
| :-: | :-: | :-: |
| type | Text value, optional values are listed at the end of the table | The Java reflection type used in Lua |
| cacheCapacity | Integer | The maximum number of entries to store in the reflection cache. Different types, such as methods, fields, and constructors, use different cache pools. |

The optional values for the `type` configuration are as follows:

  + `default`: Use standard Java reflection globally for Lua.
  + `org.eu.smileyik.luaInMinecraftBukkitII.reflect.FastReflection`: Use fast reflection globally for Lua. Fast reflection is based on bytecode generation and is the fastest, but may have issues in certain use cases.
  + `org.eu.smileyik.luaInMinecraftBukkitII.reflect.MixedFastReflection`: This reflection type is essentially the same as `FastReflection`, but it falls back to standard Java reflection if a method call fails. Generally, this type is not used as the global reflection for Lua.

#### Lua Environment Configuration

The Lua environment is configured in the `config.json` file, similar to the following format, where `lua-env-x` represents the name of the environment:

```json
{
  "bStats": true,
  "luaState": {
    "lua-env-1": { ... },
    "lua-env-2": { ... },
    "lua-env-n": { ... }
  },
  "enableModules": ...
}
```

The configuration items for the Lua environment are in the following table:

| Configuration Name | Type | Description |
| :-: | :-: | :-: |
| rootDir | Text | The root path of the Lua environment. `/abc` represents the path `.../mc_server/plugins/LuaInMinecraftBukkitII/luaState/abc` on the server host. Subsequently, when using the `require` method in Lua, dependency files will be searched for from the specified path. |
| ignoreAccessLimit | `true` / `false` | Whether to ignore access restrictions for fields and methods in Java. If ignored, private methods of Java instances can be directly used in Lua. |
| initialization | List of Lua Script File Configurations | Will load the specified script files when the Lua environment is initialized. |
| autoReload | Auto Reload Configuration | Automatically reload the Lua environment to which the script belongs when changes occur in the Lua script. |
| pool | Lua Pool Configuration | Used to set Lua pool-related configurations. |

##### initialization

The `initialization` list configuration is similar to the following format:

```json
{
    "initialization": [
        { ... },
        { ... },
        { ... }
    ],
}
```

The configuration items for this configuration are in the following table:

| Configuration Name | Type | Description |
| :-: | :-: | :-: |
| file | Text (file name) | Specifies a Lua script file and loads it when the Lua environment is initialized. The script file's path should be under the set `rootDir`. |
| asyncLoad | `true` / `false` | Whether to asynchronously initialize this script. If enabled, the script will be initialized in a Bukkit asynchronous thread. |
| depends | List of Texts | Which `Bukkit` plugins this script depends on. You can set it to depend on multiple plugins, for example `["plugin-a", "plugin-b", "plugin-c"]`. The script will be loaded and initialized only after all dependent plugins have been enabled. |

Example, a script that depends on the `PlaceholderAPI` plugin:

```json
{
    "file": "test-placeholder.lua",
    "asyncLoad": true,
    "depends": ["PlaceholderAPI"]
}
```

##### pool

Lua pool configuration is used to configure whether to enable the Lua pool and some parameters for the Lua pool.

| Configuration Name | Type | Description |
| :-: | :-: | :-: |
| enable | `true` / `false` | Whether to enable the Lua pool |
| type | `org.eu.smileyik.luaInMinecraftBukkitII.luaState.pool.simplePool.SimpleLuaPool` | Configures the type of Lua pool
| maxSize | Integer | The maximum number of Lua state machines allowed in the Lua pool at the same time. |
| idleSize | Integer, less than `maxSize` | The maximum number of idle Lua state machines allowed in the Lua pool. |
| idleTimeout | Integer, **milliseconds** | After how long an idle state machine in the Lua pool will be removed. This only works when the number of idle state machines is greater than `idleSize`. |

##### autoReload

The `autoReload` configuration option sets whether to reload the Lua environment associated with a Lua script when modifications are detected.

| Configuration Name | Type | Description |
| :-: | :-: | :-: |
| enable       | `true` / `false`     | Enable auto-reload |
| blacklist    | Text list            | Add script files to blacklist; no reload even if modified |
| frequency    | Integer, **milliseconds**  | Detection interval |

-----

### Configuration Template

#### config.json

```json
{
  // Project resource address, used to download pre-compiled dynamic link libraries
  "projectUrl": "https://raw.githubusercontent.com/SmileYik/LuaInMinecraftBukkitII/refs/tags/resources-1.0.8",
  // Select the Lua version used by the plugin
  "luaVersion": "lua-5.4.8",
  // Always check if the dependency library hash is correct.
  // If this option is enabled, it will check if the local dependency files are the same as the online ones and get the latest dependency files every time the server starts.
  "alwaysCheckHashes": false,
  // debug flag
  "debug": false,
  // Whether to enable bStats statistics
  "bStats": true,
  // Lua reflection settings
  "luaReflection": {
    // The type of reflection used by Lua. The available reflection types are as follows:
    // org.eu.smileyik.luaInMinecraftBukkitII.reflect.FastReflection:
    //    Fast reflection based on bytecode generation. It is the fastest, but may cause issues in certain use cases.
    // org.eu.smileyik.luaInMinecraftBukkitII.reflect.MixedFastReflection:
    //    Same as FastReflection, but falls back to standard Java reflection in case of a failed call.
    // default:
    //    Standard Java reflection.
    "type": "default",
    // Reflection cache capacity, which refers to the maximum number of cache entries. The cache entries for fields, methods, and constructors are independent of each other.
    "cacheCapacity": 1024
  },
  // Lua environment settings, multiple Lua environments can be set at the same time
  "luaState": {
    // Lua environment id
    "default": {
      // The directory where this environment runs. "/" represents the luaState directory under the plugin directory.
      "rootDir": "/",
      // Whether to ignore access restrictions. When ignored, you can forcefully access private methods in Java.
      "ignoreAccessLimit": false,
      // When a method called by Lua has multiple valid results, it automatically executes the first method instead of throwing an exception.
      "justUseFirstMethod": true,
      // List of initialization scripts
      "initialization": [
        // Multiple script files can be loaded
        {
          // Script file name, must be found in the folder set by rootDir.
          "file": "test.lua",
          // Asynchronous script loading. If enabled, this script will not run on the Bukkit main thread during initialization.
          "asyncLoad": false,
          // The plugins this script depends on, is a list of plugins
          // For example: "depends": ["PlaceholderAPI"], this script will only be loaded when the PlaceholderAPI plugin is loaded.
          "depends": []
        }
      ],
      // Automatic reload settings: Automatically reload the Lua environment when changes are sent from script files included in the initialization script list.
      // Note that when reloading scripts, the reload method is a hard reload.
      "autoReload": {
        "enable": true,
        "blacklist": [
          "block-reload.lua"
        ],
        // Detection frequency, milliseconds
        "frequency": 60000
      },
      // Lua pool settings
      // The Lua pool can break the single-threaded limitation of Lua by transferring Lua closures to the state machine in the Lua pool to run, and then returning the result of the closure back to the main state machine after the run is complete.
      "pool": {
        // Whether to enable
        "enable": true,
        // Lua pool type
        "type": "org.eu.smileyik.luaInMinecraftBukkitII.luaState.pool.simplePool.SimpleLuaPool",
        // Maximum number of Lua state machines in the Lua pool
        "maxSize": 2,
        // Number of Lua state machines allowed to be idle
        "idleSize": 1,
        // After how long (in milliseconds) an idle state machine will be cleaned up. Note: This will only happen when the number of idle state machines is greater than the number set in `idleSize`.
        "idleTimeout": 6000000
      }
    }
  },
  // Modules to be loaded, currently available modules include "cffi", "jni-bridge"
  "enableModules": [
    "cffi",
    "jni-bridge"
  ]
}
```