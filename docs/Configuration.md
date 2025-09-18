[History]: https://github.com/SmileYik/LuaInMinecraftBukkitII/commits/gh-page/docs/Configuration.md
[LatestVersion]: https://github.com/SmileYik/LuaInMinecraftBukkitII/tree/tags/1.0.9
[ResourceRepo]: https://github.com/SmileYik/LuaInMinecraftBukkitII/tree/gh-page

> 最后更新于2025年09月18日 | [历史记录][History]

> 此页面内容对应于 LuaInMinecraftBukkit II 插件的 [**1.0.9**][LatestVersion] 版本, 历史文档可以插件此页面的历史记录

这个文档将给出默认的插件配置设定及其说明.

### config.json

`config.json` 是插件的主要配置文件, 在这个配置文件中, 可以配置插件的依赖下载源, lua 环境等信息.
与此同时, 该配置文件不是一个标准的 `json` 类型的文件, 该文件允许单行`//`开头的文本作为注释.

| 配置名 | 类型 | 说明 |
| :-: | :-: | :-: |
| projectUrl        | 链接文本 | [项目资源地址][ResourceRepo], 可以去仓库中寻找. 自 **1.0.8** 版本以后, 项目资源分支都会将与插件版本相匹配的资源标记tag, tag命名格式为 `resources-[插件版本]`, 例如插件版本为 1.0.8, 则会有资源分支 `resources-1.0.8`, 此时, 链接 `https://raw.githubusercontent.com/SmileYik/LuaInMinecraftBukkitII/refs/tags/resources-1.0.8` 即可填入配置中.
| luaVersion         | `luajit` / `lua-5.2.4` / `lua-5.3.6` / `lua-5.4.8` | 设置插件所使用的 lua 版本. |
| alwaysCheckHashes  | `true` / `false` | 是否总是从资源仓库中获取最新依赖 |
| justUseFirstMethod | `true` / `false` | 是否在 lua 检测到多个可选方法时, 总是选择第一个候选项而不是抛出异常 |
| debug              | `true` / `false` | 是否启用 Debug 日志 |
| bStats             | `true` / `false` | 是否启用 bStats 统计信息 |
| luaReflection      | Lua 反射配置      | 配置 Lua 使用的反射类型 |
| luaState           | Lua 环境配置      | 配置 Lua 环境. |
| enableModules      | 文本列表          | 启用模组, 现可用模组有 `cffi` |

#### Lua Reflection - Lua 反射配置

Lua 反射配置主体配置片段如下:

```json
  "luaReflection": {
    "type": "default",
    "cacheCapacity": 1024
  },
```

各个配置项说明如下表格:

| 配置名 | 类型 | 说明 |
| :-: | :-: | :-: |
| type          | 文本值, 可选值在表格末尾列出 | Lua 中使用的 Java 反射类型 |
| cacheCapacity | 整数                       | 最多存储多少条目的反射缓存, 不同种类如方法, 字段和构造器都使用不同的缓存池 |

`type` 配置可选内容如下:  
+ `default`: 为 Lua 全局使用 Java 标准反射
+ `org.eu.smileyik.luaInMinecraftBukkitII.reflect.FastReflection`: 为 Lua 全局使用快速反射, 快速反射基于字节码生成, 速度最快, 但是对于某些使用情况下可能会出现问题.
+ `org.eu.smileyik.luaInMinecraftBukkitII.reflect.MixedFastReflection`: 该反射类型与 `FastReflection` 基本相同, 但是在方法调用失败时会调用 Java 标准反射. 一般来说不使用该类型作为 Lua 全局用反射.

#### Lua 环境配置

Lua 环境配置在 `config.json` 文件中配置, 其配置类似于以下形式, 其中 `lua-env-x` 代表环境的名称:

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

Lua 环境的配置项如下表格: 

| 配置名 | 类型 | 说明 |
| :-: | :-: | :-: |
| rootDir           | 文本               | Lua 环境的根路径, `/abc` 代表路径为服务器主机的 `.../mc_server/plugins/LuaInMinecraftBukkitII/luaState/abc`, 之后, 在 Lua 中使用 `require` 方法时, 将会从指定的路径中寻找依赖文件. |
| ignoreAccessLimit | `true` / `false`   | 是否忽略Java中的字段与方法的访问限制. 若忽略该限制, 则能够直接在 lua 中使用 Java 实例的私有方法. |
| initialization    | Lua 脚本文件配置列表 | 将在 Lua 环境初始化时加载所设定好的脚本文件. |
| autoReload        | 自动重载脚本文件配置列表 | 当 Lua 脚本发生变更时自动重载脚本所属 Lua 环境 |
| pool              | Lua 池配置          | 用于设置 Lua 池相关配置. |

##### initialization

`initialization` 列表配置类似于以下形式:

```json
{
    "initialization": [
        { ... },
        { ... },
        { ... }
    ],
}
```

该配置的配置项如下表格:

| 配置名 | 类型 | 说明 |
| :-: | :-: | :-: |
| file      | 文本(文件名) | 指定 Lua 脚本文件, 并在 Lua 环境初始化时加载该脚本, 脚本文件缩放路径应该为所设定的 `rootDir` 下 |
| asyncLoad | `true` / `false` | 是否异步初始化该脚本, 若需要异步初始化该脚本, 则将会在 Bukkit 的异步线程中初始化该脚本 |
| depends   | 文本列表 | 该脚本需要依赖哪个 `Bukkit` 插件, 可以设置依赖多个插件, 例如 `["plugin-a", "plugin-b", "plugin-c"]`, 脚本将会在所有依赖插件都启用完成时加载并初始化 |

样例, 依赖 `PlaceholderAPI` 插件的脚本:

```json
{
    "file": "test-placeholder.lua",
    "asyncLoad": true,
    "depends": ["PlaceholderAPI"]
}
```

##### pool

Lua 池配置用于配置是否启用 Lua 池以及 Lua 池的一些参数.

| 配置名 | 类型 | 说明 |
| :-: | :-: | :-: |
| enable       | `true` / `false` | 是否启用 Lua 池 |
| type         | `org.eu.smileyik.luaInMinecraftBukkitII.luaState.pool.simplePool.SimpleLuaPool` | 配置 Lua 池种类
| maxSize      | 整数                  | Lua 池中最多允许同时拥有的 Lua 状态机数量. |
| idleSize     | 整数,  小于 `maxSize` | Lua 池中最多允许闲置的 Lua 状态机数量 |
| idleTimeout  | 整数, **毫秒**        | 当 Lua 池中的状态机闲置多长时间后将其移除; 仅当闲置的状态机数量大于 `idleSize` 时工作. |


##### autoReload

`autoReload` 配置项用于配置在检测到 Lua 脚本经过修改后, 重载 Lua 脚本所属的 Lua 环境相关的选项.

| 配置名 | 类型 | 说明 |
| :-: | :-: | :-: |
| enable       | `true` / `false`     | 是否启用自动重载 |
| blacklist    | 文本列表              | 将脚本文件拉入黑名单, 即使发生修改也不重载 |
| frequency    | 整数, **毫秒**        | 检测频率 |

### 配置模板

#### config.json

```json
{
  // 项目资源地址, 用于下载预编译好的动态链接库
  "projectUrl": "https://raw.githubusercontent.com/SmileYik/LuaInMinecraftBukkitII/refs/tags/resources-1.0.8",
  // 选择插件使用的lua版本
  "luaVersion": "lua-5.4.8",
  // 总是检查依赖库Hash是否正确. 
  // 若启用该选项, 则会每次启动服务器时, 校验依赖文件于线上中的依赖文件是否相同, 并获取最新的依赖文件.
  "alwaysCheckHashes": false,
  // debug 标志
  "debug": false,
  // 是否启用 bStats 统计信息
  "bStats": true,
  // lua 反射设置
  "luaReflection": {
    // lua 所使用的反射类型, 目前可用反射类型如下:
    // org.eu.smileyik.luaInMinecraftBukkitII.reflect.FastReflection:
    //     基于字节码生成的快速反射, 速度最快, 但是对于某些使用情况下可能会出现问题
    // org.eu.smileyik.luaInMinecraftBukkitII.reflect.MixedFastReflection:
    //     与 FastReflection 相同, 但在调用失败的情况下会回退到 Java 自带的标准反射
    // default:
    //     Java 自带的标准反射.
    "type": "default",
    // 反射缓存容量, 具体指最多缓存多少条缓存记录, 字段, 方法, 构造器的缓存记录都相互独立.
    "cacheCapacity": 1024
  },
  // lua 环境设置, 可以同时设置多个 Lua 环境
  "luaState": {
    // lua 环境 id
    "default": {
      // 该环境运行在哪个目录下, "/" 代表插件目录下的 luaState 目录.
      "rootDir": "/",
      // 是否忽略访问限制, 忽略访问限制时可以强制访问java中的私有方法.
      "ignoreAccessLimit": false,
      // 当 lua 调用的方法拥有多种符合要求的结果时, 自动运行首个方法而不是抛出异常
      "justUseFirstMethod": true,
      // 初始化脚本列表
      "initialization": [
        // 可以加载多个脚本文件
        {
          // 脚本文件名, 需要能在rootDir设置的文件夹中寻找的到.
          "file": "test.lua",
          // 脚本异步加载, 若启用, 则该脚本在初始化时, 将不会在 Bukkit 主线程中运行.
          "asyncLoad": false,
          // 该脚本所依赖插件, 是插件列表
          // 例如: "depends": ["PlaceholderAPI"], 则将会在 PlaceholderAPI 插件加载时, 才会加载该脚本.
          "depends": []
        }
      ],
      // 自动重载设置, 当初始化脚本列表中包含的脚本文件发送更改时自动重载 Lua 环境
      // 需要注意的是, 重载脚本时, 重载方式为硬重载.
      "autoReload": {
        "enable": true,
        "blacklist": [
          "block-reload.lua"
        ],
        // 检测频率, 毫秒
        "frequency": 60000
      },
      // Lua 池设置
      // Lua 池可以突破 Lua 的单线程限制, 将 Lua 闭包转移至 Lua 池中的状态机中运行, 并在运行完闭后, 将闭包返回的结果返回至主状态机中.
      "pool": {
        // 是否启用
        "enable": true,
        // Lua 池类型
        "type": "org.eu.smileyik.luaInMinecraftBukkitII.luaState.pool.simplePool.SimpleLuaPool",
        // Lua 池中最大 Lua 状态机数量
        "maxSize": 2,
        // 允许空闲中的 Lua 状态机数量
        "idleSize": 1,
        // 当状态机空闲多少时间(毫秒)时会将其清理. 注意: 仅在空闲状态机数量大于 `idleSize` 中设定的数量时才会进行清理.
        "idleTimeout": 6000000
      }
    }
  },
  // 需要加载的模组, 目前可用模组有 "cffi", "jni-bridge"
  "enableModules": [
    "cffi",
    "jni-bridge"
  ]
}
```