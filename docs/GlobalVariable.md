[历史记录]: https://github.com/SmileYik/LuaInMinecraftBukkitII/commits/gh-page/docs/GlobalVariable.md
[LuaJava]: https://github.com/SmileYik/luajava
[LuaInMinecraftBukkit II]: https://github.com/SmileYik/LuaInMinecraftBukkitII

[ILuaEnv]: https://github.com/SmileYik/LuaInMinecraftBukkitII/blob/master/src/main/java/org/eu/smileyik/luaInMinecraftBukkitII/api/lua/luaState/ILuaEnv.java
[LuaHelper]: https://github.com/SmileYik/LuaInMinecraftBukkitII/blob/master/src/main/java/org/eu/smileyik/luaInMinecraftBukkitII/api/lua/luaState/LuaHelper.java
[LuaIOHelper]: https://github.com/SmileYik/LuaInMinecraftBukkitII/blob/master/src/main/java/org/eu/smileyik/luaInMinecraftBukkitII/api/lua/luaState/LuaIOHelper.java
[LuaInMinecraftBukkit]: https://github.com/SmileYik/LuaInMinecraftBukkitII/blob/master/src/main/java/org/eu/smileyik/luaInMinecraftBukkitII/LuaInMinecraftBukkit.java
[ILuaEventListenerBuilder]: https://github.com/SmileYik/LuaInMinecraftBukkitII/blob/master/src/main/java/org/eu/smileyik/luaInMinecraftBukkitII/api/lua/luaState/event/ILuaEventListenerBuilder.java
[ILuaCommandClassBuilder]: https://github.com/SmileYik/LuaInMinecraftBukkitII/blob/master/src/main/java/org/eu/smileyik/luaInMinecraftBukkitII/api/lua/luaState/command/ILuaCommandClassBuilder.java

[Bukkit]: https://bukkit.windit.net/javadoc/org/bukkit/Bukkit.html
[Server]: https://bukkit.windit.net/javadoc/org/bukkit/Server.html
[Logger]: https://docs.oracle.com/en/java/javase/17/docs/api/java.logging/java/util/logging/Logger.html
[PrintStream]: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/io/PrintStream.html

[Command 章节]: ./../Command.md
[EventListener 章节]: ./../EventListener.md

> 最后更新于2025年08月31日 | [历史记录]

> 此页面内容对应于 LuaInMinecraftBukkit II 插件的最新版本 **1.0.9**, 历史文档可以插件此页面的历史记录

在 Lua 部分拥有一些全局变量, 以方便您让 Lua 与 Bukkit 服务器之间的交互更为简单.

## luajava

[LuaInMinecraftBukkit II] 依附于 [LuaJava] 项目, 而 [LuaJava] 中存在一个名为 `luajava` 的全局表, 皆在于方便使用者与 Java 进行交互.

### luajava.bindClass - 获取 Java 类

**方法说明**: 该方法用于获取 Java 类, 返回的 Java 类无法使用 `Class<?>` 中原本方法, 只能使用目标类的静态方法或访问静态字段.  
**返回类型**: Class<?> 实例  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `className` | 字符串类型 | Java 全类名 |

**例子**: 
```lua
local BorderLayout = luajava.bindClass("java.awt.BorderLayout")
BorderLayout.NORTH -- 获取 BorderLayout 类中的公共静态字段 `NORTH`
```

### luajava.class2Obj - 将 Java Class<?> 实例转为普通 Java 实例

**方法说明**: 该方法用于将 `Class<?>` 实例作为 `Object` 实例打开. 实际上无论是否使用该方法, 变量中的 `Class<?>` 实例始终相同, 仅仅是 Lua 变量处理 `Class<?>` 实例的方式不同.  
**返回类型**: 当作 Java Object 存储 Class<?> 实例  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `class` | `Class<?>` | 要转换的 Java 类实例 |

**例子**: 
遍历 `java.awt.BorderLayout` 中所有方法
```lua
local BorderLayout = luajava.bindClass("java.awt.BorderLayout")
local clazz = luajava.class2Obj(BorderLayout)
while clazz ~= nil do
    clazz = luajava.class2Obj(clazz)
    local methods = clazz:getDeclaredMethods()
    for i=1, #methods do
        print(methods[i] .. "")
    end
    clazz = clazz:getSuperclass()
end
```

### luajava.new - 根据 Class<?> 构造 Java 实例

**方法说明**: 该方法用于构造指定Java类的实例.  
**返回类型**: 指定Java类的实例  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `class` | `Class<?>` | 要构造的 Java 类 |
| `...` | 可变形参 | Java 类中公开构造器形参 |

**例子**: 
构建 `java.awt.Frame` 类实例.
```lua
local Frame = luajava.bindClass("java.awt.Frame")
local frame = luajava.new(Frame, "luajava.new 方法创建的窗口")
frame:show()
```

### luajava.newInstance - 根据类名构造 Java 实例

**方法说明**: 该方法用于构造指定Java类的实例.  
**返回类型**: 指定Java类的实例  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `className` | 字符串类型 | 要构造的 Java 类名 |
| `...` | 可变形参 | Java 类中公开构造器形参 |

**例子**: 
构建 `java.awt.Frame` 类实例.
```lua
local frame = luajava.newInstance("java.awt.Frame", "luajava.newInstance 方法创建的窗口")
frame:show()
```

### luajava.createProxy - 创建 Java 接口代理实例

**方法说明**: 该方法用于创建 Java 接口的代理实例  
**返回类型**: 指定 Java 接口的实例.  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `interfaceName` | 字符串类型 | 要代理的 Java 接口名 |
| `functionTable` | `Lua Table` | 方法表, 若键名与接口中的方法名相同, 则视为该接口方法的具体实现 |

**例子**: 
构建 `java.lang.Runnable` 接口实例.
```lua
local runnable = luajava.createProxy("java.lang.Runnable", {
    run = function ()
        print("run!")
    end
})
runnable:run()
local clazz = luajava.class2Obj(runnable:getClass())
print(clazz:getName())
```

### luajava.env - 获取当前 JNIEnv 实例

**方法说明**: 该方法用于获取当前 LuaState 所附着的 JNIEnv 实例. 该实例为 C/C++ 指针.  
**返回类型**: JNIEnv 指针  
**形参列表**: 无

**例子**: 无

## luaBukkit

`luaBukkit` 全局表为 [LuaInMinecraftBukkit II] 中添加的一个方法表, 意在与辅助 **Lua** 与 **Bukkit** 服务器之间的交互.

当前版本下, `luaBukkit` 表中包含以下实例:

| 键名 | 键值类型 | 说明 |
| :-: | :-: | :-: |
| env    | [ILuaEnv]              | 当前 Lua 环境, 可以用于注册 Bukkit 事件或指令等操作 |
| helper | [LuaHelper]            | 一些实用方法, 用于快速创建 Bukkit 线程, 也可以将`LuaTable`转换为Java数组 |
| io     | [LuaIOHelper]          | 与 IO 流相关的实用方法. |
| bukkit | [Bukkit]               | Bukkit 类 |
| plugin | [LuaInMinecraftBukkit] | [LuaInMinecraftBukkit II] 的 `JavaPlugin` 类型实例 |
| server | [Server]               | CraftServer 实例 |
| log    | [Logger]               | 当前 [LuaInMinecraftBukkit II] 插件的日志打印器 |
| out    | [PrintStream]          | `System.out` 标准输出流 |

### ILuaEnv

源代码: [ILuaEnv]  
使用方法: `luaBukkit.env:方法名`

#### listenerBuilder - 监听器构建器

**方法说明**: 开始构建监听器, 详细请看 [EventListener 章节]  
**返回类型**: [ILuaEventListenerBuilder]  
**形参列表**: 无
**例子**: 请看 [EventListener 章节]  

#### unregisterEventListener - 取消注册监听器

**方法说明**: 取消注册指定监听器名的监听器  
**返回类型**: 无  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `name` | 字符串类型 | 监听器名 |

**例子**: 
```lua
-- 取消注册名为 "GreetingEvent" 的监听器
luaBukkit.env:unregisterEventListener("GreetingEvent")
```

#### commandClassBuilder - 指令类构建器

**方法说明**: 开始构建 Bukkit 服务器指令类, 详细请看 [Command 章节]  
**返回类型**: [ILuaCommandClassBuilder]  
**形参列表**: 无
**例子**: 请看 [Command 章节]   

#### registerCommand - 注册 Bukkit 指令

**方法说明**: 向 Bukkit 注册由 `commandClassBuilder` 方法构建的若干指令类, 详细请看 [Command 章节]  
**返回类型**: `Result<Boolean, Exception>`  
**形参列表**: 
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `rootCommand` | 字符串类型    | 根指令名 |
| `classes`     | `LuaTable`/`Class<?>[]` | 指令类数组, 应该传入数组风格的 `LuaTable` |

**例子**: 请看 [Command 章节]   

#### registerCommand - 注册 Bukkit 指令, 并设置别名

**方法说明**: 向 Bukkit 注册由 `commandClassBuilder` 方法构建的若干指令类, 详细请看 [Command 章节]  
**返回类型**: `Result<Boolean, Exception>`  
**形参列表**: 
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `rootCommand` | 字符串类型    | 根指令名 |
| `aliases`     | `LuaTable`/`String[]`    | 字符串数组, 应该传入数组风格的 `LuaTable` |
| `classes`     | `LuaTable`/`Class<?>[]`  | 指令类数组, 应该传入数组风格的 `LuaTable` |

**例子**: 请看 [Command 章节]   

#### registerCleaner - 注册清理器

**方法说明**: 注册一个 `LuaFunction` 作为清理器, 用于当关闭 Lua 环境前调用.  
**返回类型**: 无  
**形参列表**:  
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `cleaner` | `LuaFunction` | 清理器 |

**例子**:   
```lua
-- 当 Lua 环境关闭时, 打印日志 "Cleaning..."
luaBukkit.env:registerCleaner(function () 
    luaBukkit.log:info("Cleaning...")
end)
```

#### registerSoftReload - 注册软重载闭包

**方法说明**: 注册一个 `LuaFunction` 作为软重载闭包, 用于在执行软重载指令时, 自己清理 Lua 中的数据.  
**返回类型**: `Result<Void, String>`, 失败时返回失败信息.  
**形参列表**: 
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `luaCallable` | `LuaFunction` | 软重载闭包 |

**例子**:   
```lua
-- 当 Lua 环境软重启时, 重置计数器为 0, 并打印日志 "Reloading..."
local counter = 128
luaBukkit.env:registerSoftReload(function () 
    counter = 0
    luaBukkit.log:info("Reloading...")
end)
```

#### pooledCallable - 将 Lua 闭包变为可在 Lua 池中运行的闭包

**方法说明**: 包裹 `function() end` 使其能够在 Lua 池中运行. 运行时, 会将包裹着的方法转移至一个 **新的Lua状态机** 中, 使用该方法时应当在非当前线程使用. 此外, 需要在 `config.yml` 中, 为当前 Lua 环境启用 Lua 池.  
**返回类型**: 能够在 Lua 池中运行的闭包.  
**形参列表**: 
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `luaCallable` | `LuaFunction` | 闭包 |

**例子**:   

1. 并行运行2个死循环:
```lua
local import = require "import"
local Thread = import "java.lang.Thread"

for i = 1, 2 do
    luaBukkit.helper:asyncCall(luaBukkit.env:pooledCallable(
        function ()
            while true do
                luaBukkit.log:info(Thread:currentThread():getName() .. " async call " .. i .. "!")
                Thread:sleep(1000)
            end
        end
    ))
end
```

2. 获取返回值:

```lua
local import = require "import"
import "java.lang.Thread"

for i = 1, 10 do
    local future = luaBukkit.helper:asyncCall(luaBukkit.env:pooledCallable(
        function ()
            while true do
                luaBukkit.log:info(Thread:currentThread():getName() .. " async call " .. i .. "!")
                break
            end
            return {
                abc = "abc" .. i
            }
        end
    ))
    
    -- print result
    future:thenAccept(luaBukkit.helper:consumer(
        function(result)
            print(result)
            for k, v in pairs(result) do
                luaBukkit.log:info(k .. ": " .. v)
            end
        end
    ))
end
```

#### path - 获取文件路径

**方法说明**: 获取Lua环境目录下的实际文件路径.  
**返回类型**: `String`, 文件路径.  
**形参列表**: 
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `path` | `String` | 文件路径 |

**例子**:
```lua
local realPath = luaBukkit.env:path("readme.txt")
luaBukkit.log:info(realPath)
```

#### file - 获取文件

**方法说明**: 获取Lua环境目录下的实际文件.  
**返回类型**: `File`  
**形参列表**: 
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `path` | `String` | 文件路径 |

**例子**:
```lua
local file = luaBukkit.env:file("readme.txt")
if file:exists() then
    luaBukkit.log:info("Exists!")
end
```

#### setJustUseFirstMethod - 设置 Lua 检测 Java 方法行为

**方法说明**: 设置 Lua 检测方法的行为, 当设置为 `true` 时, 将会总是自动选择候选方法的第一个方法执行, 而非抛出异常.  
**返回类型**: 无  
**形参列表**: 
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `flag` | `Boolean` | 是否自动选择候选方法列表的第一个方法执行 |

**例子**:
```lua
luaBukkit.env:setJustUseFirstMethod(true)
```

#### ignoreMultiResultRun - 自动选择第一个 Java 方法运行 

**方法说明**: 与 `setJustUseFirstMethod` 类似. 设置 Lua 检测方法的行为, 当设置为 `true` 时, 将会总是自动选择候选方法的第一个方法执行, 而非抛出异常.  
**返回类型**: `Result<Object, LuaException>`  
**形参列表**: 
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `callable` | `LuaFunction` | 在 LuaFunction 方法执行时自动选择候选方法的第一个方法执行, 而非抛出异常 |

**例子**:
```lua
luaBukkit.env:ignoreMultiResultRun(function() 
    -- do something
end)
```

### LuaHelper

源代码: [LuaHelper]  
使用方法: `luaBukkit.helper:方法名`

#### runnable - 构建一个 Runnable 实例.

**方法说明**: 构建一个 Runnable 实例.  
**返回类型**: Runnable 实例  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `callable` | `Lua Function` | Lua方法 |

**例子**: 
```lua
local runnable = luaBukkit.helper:runnable(
    function () 
        luaBukkit.log:info("Run!")
    end
)
runnable:run()
```

#### consumer - 构建一个 Consumer 实例.

**方法说明**: 构建一个 Consumer 实例.  
**返回类型**: Consumer 实例  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `callable` | `Lua Function` | Lua方法 |

**例子**: 
```lua
local consumer = luaBukkit.helper:consumer(
    function (name) 
        luaBukkit.log:info("Running out of " .. name .. "!")
    end
)
consumer:accept("coals")
```

#### syncCall - 同步运行 Lua 闭包.

**方法说明**: 在Bukkit中同步运行一个 Lua 方法, 并且在完成是将其标记为已完成  
**返回类型**: `CompletableFuture<Object>` 实例, 用于检测其是否已完成, 并可用于获取返回值.  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `callable` | `Lua Function` | Lua方法 |

**例子**: 
```lua
-- 注意, Lua本身运行在主线程
-- 在异步线程中执行
luaBukkit.helper:asyncCall(
    function ()
        -- 同步运行
        local future = luaBukkit.helper:syncCall(
            function ()
                luaBukkit.log:info("sync call!")
                return "finished call!"
            end
        )
        -- 当完成时打印结果
        future:thenAccept(luaBukkit.helper:consumer(
            function (result)
                luaBukkit.log:info(result)
            end
        ))
    end
)
```

#### syncCall - 同步运行 Lua 闭包, 并允许传参

**方法说明**: 在Bukkit中同步运行一个 Lua 方法, 并且在完成是将其标记为已完成  
**返回类型**: `CompletableFuture<Object>` 实例, 用于检测其是否已完成, 并可用于获取返回值.  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `callable` | `Lua Function` | 接收参数的 Lua 方法 |
| `params` | `Lua Array` | 数组风格的 `Lua Table`, 用于传参给 Lua 方法 |

**例子**: 
```lua
-- 注意, Lua本身运行在主线程
-- 在异步线程中执行
luaBukkit.helper:asyncCall(
    function ()
        -- 同步运行
        local future = luaBukkit.helper:syncCall(
            function (prefix, suffix) 
                luaBukkit.log:info(prefix .. "sync call!" .. suffix)
                return "finished call!"
            end,
            { "[Prefix]", " Finally!!!" }
        )
        -- 当完成时打印结果
        future:thenAccept(luaBukkit.helper:consumer(
            function (result)
                luaBukkit.log:info(result)
            end
        ))
    end
)
```

#### syncCallLater - 延迟同步运行 Lua 闭包.

**方法说明**: 在Bukkit中同步运行一个 Lua 方法, 并且在完成是将其标记为已完成  
**返回类型**: `CompletableFuture<Object>` 实例, 用于检测其是否已完成, 并可用于获取返回值.  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `callable` | `Lua Function` | Lua方法 |
| `tick` | `Number` | 延迟时间, 时间单位: **tick** |

**例子**: 
```lua
-- 注意, Lua本身运行在主线程
-- 在异步线程中执行
luaBukkit.helper:asyncCall(
    function ()
        -- 延迟 20 tick (1s) 后同步运行
        local future = luaBukkit.helper:syncCallLater(
            function ()
                luaBukkit.log:info("sync call!")
                return "finished call!"
            end,
            20    -- ticks
        )
        -- 当完成时打印结果
        future:thenAccept(luaBukkit.helper:consumer(
            function (result)
                luaBukkit.log:info(result)
            end
        ))
    end
)
```

#### syncCallLater - 延迟同步运行 Lua 闭包, 并允许传参

**方法说明**: 在Bukkit中同步运行一个 Lua 方法, 并且在完成是将其标记为已完成  
**返回类型**: `CompletableFuture<Object>` 实例, 用于检测其是否已完成, 并可用于获取返回值.  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `callable` | `Lua Function` | 接收参数的 Lua 方法 |
| `tick` | `Number` | 延迟时间, 时间单位: **tick** |
| `params` | `Lua Array` | 数组风格的 `Lua Table`, 用于传参给 Lua 方法 |

**例子**: 
```lua
-- 注意, Lua本身运行在主线程
-- 在异步线程中执行
luaBukkit.helper:asyncCall(
    function ()
        -- 延迟 20 tick (1s) 后同步运行
        local future = luaBukkit.helper:syncCallLater(
            function (prefix, suffix)
                luaBukkit.log:info(prefix .. "sync call!" .. suffix)
                return "finished call!"
            end,
            20,    -- ticks
            { "[Prefix]", " Finally!!!" }
        )
        -- 当完成时打印结果
        future:thenAccept(luaBukkit.helper:consumer(
            function (result)
                luaBukkit.log:info(result)
            end
        ))
    end
)
```

#### syncTimer - 同步计时器

**使用注意**: **调用该方法则有责任管理该计时器, 请在不需要的时候释放它.**  

**方法说明**: 在Bukkit中同步运行一个计时器  
**返回类型**: `BukkitTask` 实例.  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `callable` | `Lua Function` | Lua方法 |
| `delay` | `Number` | 延迟时间, 多久之后开始运行计时器, 时间单位: **tick** |
| `period` | `Number` | 间隔时间, 计时器激发间隔, 时间单位: **tick** |

**例子**: 
```lua
-- 延迟 20 tick (1s) 后同步运行, 每次运行间隔也为 20 tick (1s)
local timer = luaBukkit.helper:syncTimer(
    function () 
        luaBukkit.log:info("sync timer call!")
    end,
    20,   -- delay
    20    -- period
)
```

#### syncTimer - 同步计时器, 并允许传参

**使用注意**: **调用该方法则有责任管理该计时器, 请在不需要的时候释放它.**  

**方法说明**: 在Bukkit中同步运行一个计时器, 并允许给`Lua Function`传参  
**返回类型**: `BukkitTask` 实例.  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `callable` | `Lua Function` | Lua方法 |
| `delay` | `Number` | 延迟时间, 多久之后开始运行计时器, 时间单位: **tick** |
| `period` | `Number` | 间隔时间, 计时器激发间隔, 时间单位: **tick** |
| `params` | `Lua Array` | 数组风格的 `Lua Table`, 用于传参给 Lua 方法 |

**例子**: 
```lua
-- 延迟 20 tick (1s) 后同步运行, 每次运行间隔也为 20 tick (1s)
local timer = luaBukkit.helper:syncTimer(
    function (prefix, suffix) 
        luaBukkit.log:info(prefix .. "sync timer call!" .. suffix)
    end,
    20,    -- delay
    20,    -- period
    { "[Timer] ", " See you next time!" }
)
```

#### asyncCall - 异步运行 Lua 闭包.

**方法说明**: 在Bukkit中异步运行一个 Lua 方法, 并且在完成是将其标记为已完成  
**返回类型**: `CompletableFuture<Object>` 实例, 用于检测其是否已完成, 并可用于获取返回值.  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `callable` | `Lua Function` | Lua方法 |

**例子**: 
```lua
-- 注意, Lua本身运行在主线程
-- 在异步线程中执行
luaBukkit.helper:asyncCall(
    function ()
        -- 异步运行
        local future = luaBukkit.helper:asyncCall(
            function ()
                luaBukkit.log:info("async call!")
                return "finished call!"
            end
        )
        -- 当完成时打印结果
        future:thenAccept(luaBukkit.helper:consumer(
            function (result)
                luaBukkit.log:info(result)
            end
        ))
    end
)
```

#### asyncCall - 异步运行 Lua 闭包, 并允许传参

**方法说明**: 在Bukkit中异步运行一个 Lua 方法, 并且在完成是将其标记为已完成  
**返回类型**: `CompletableFuture<Object>` 实例, 用于检测其是否已完成, 并可用于获取返回值.  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `callable` | `Lua Function` | 接收参数的 Lua 方法 |
| `params` | `Lua Array` | 数组风格的 `Lua Table`, 用于传参给 Lua 方法 |

**例子**: 
```lua
-- 注意, Lua本身运行在主线程
-- 在异步线程中执行
luaBukkit.helper:asyncCall(
    function ()
        -- 异步运行
        local future = luaBukkit.helper:asyncCall(
            function (prefix, suffix)
                luaBukkit.log:info(prefix .. "async call!" .. suffix)
                return "finished call!"
            end,
            { "[Prefix]", " Finally!!!" }
        )
        -- 当完成时打印结果
        future:thenAccept(luaBukkit.helper:consumer(
            function (result)
                luaBukkit.log:info(result)
            end
        ))
    end
)
```

#### asyncCallLater - 延迟异步运行 Lua 闭包.

**方法说明**: 在Bukkit中异步运行一个 Lua 方法, 并且在完成是将其标记为已完成  
**返回类型**: `CompletableFuture<Object>` 实例, 用于检测其是否已完成, 并可用于获取返回值.  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `callable` | `Lua Function` | Lua方法 |
| `tick` | `Number` | 延迟时间, 时间单位: **tick** |

**例子**: 
```lua
-- 注意, Lua本身运行在主线程
-- 在异步线程中执行
luaBukkit.helper:asyncCall(
    function ()
        -- 延迟 20 tick (1s) 后异步运行
        local future = luaBukkit.helper:asyncCallLater(
            function ()
                luaBukkit.log:info("async call!")
                return "finished call!"
            end,
            20    -- ticks
        )
        -- 当完成时打印结果
        future:thenAccept(luaBukkit.helper:consumer(
            function (result)
                luaBukkit.log:info(result)
            end
        ))
    end
)
```

#### asyncCallLater - 延迟异步运行 Lua 闭包, 并允许传参

**方法说明**: 在Bukkit中异步运行一个 Lua 方法, 并且在完成是将其标记为已完成  
**返回类型**: `CompletableFuture<Object>` 实例, 用于检测其是否已完成, 并可用于获取返回值.  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `callable` | `Lua Function` | 接收参数的 Lua 方法 |
| `tick` | `Number` | 延迟时间, 时间单位: **tick** |
| `params` | `Lua Array` | 数组风格的 `Lua Table`, 用于传参给 Lua 方法 |

**例子**: 
```lua
-- 注意, Lua本身运行在主线程
-- 在异步线程中执行
luaBukkit.helper:asyncCall(
    function ()
        -- 延迟 20 tick (1s) 后异步运行
        local future = luaBukkit.helper:asyncCallLater(
            function (prefix, suffix)
                luaBukkit.log:info(prefix .. "async call!" .. suffix)
                return "finished call!"
            end,
            20,    -- ticks
            { "[Prefix]", " Finally!!!" }
        )
        -- 当完成时打印结果
        future:thenAccept(luaBukkit.helper:consumer(
            function (result)
                luaBukkit.log:info(result)
            end
        ))
    end
)
```

#### asyncTimer - 同步计时器

**使用注意**: **调用该方法则有责任管理该计时器, 请在不需要的时候释放它.**  

**方法说明**: 在Bukkit中同步异步一个计时器  
**返回类型**: `BukkitTask` 实例.  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `callable` | `Lua Function` | Lua方法 |
| `delay` | `Number` | 延迟时间, 多久之后开始运行计时器, 时间单位: **tick** |
| `period` | `Number` | 间隔时间, 计时器激发间隔, 时间单位: **tick** |

**例子**: 
```lua
-- 延迟 20 tick (1s) 后异步运行, 每次运行间隔也为 20 tick (1s)
local timer = luaBukkit.helper:asyncTimer(
    function () 
        luaBukkit.log:info("async timer call!")
    end,
    20,   -- delay
    20    -- period
)
```

#### asyncTimer - 异步计时器, 并允许传参

**使用注意**: **调用该方法则有责任管理该计时器, 请在不需要的时候释放它.**  

**方法说明**: 在Bukkit中异步运行一个计时器, 并允许给`Lua Function`传参  
**返回类型**: `BukkitTask` 实例.  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `callable` | `Lua Function` | Lua方法 |
| `delay`    | `Number`       | 延迟时间, 多久之后开始运行计时器, 时间单位: **tick** |
| `period`   | `Number`       | 间隔时间, 计时器激发间隔, 时间单位: **tick** |
| `params`   | `Lua Array`    | 数组风格的 `Lua Table`, 用于传参给 Lua 方法 |

**例子**: 
```lua
-- 延迟 20 tick (1s) 后异步运行, 每次运行间隔也为 20 tick (1s)
local timer = luaBukkit.helper:asyncTimer(
    function (prefix, suffix) 
        luaBukkit.log:info(prefix .. "async timer call!" .. suffix)
    end,
    20,    -- delay
    20,    -- period
    { "[Timer] ", " See you next time!" }
)
```

#### castArray - 将 LuaTable 转换为 Java 数组

**方法说明**: 将数组风格的 `LuaTable` 转换为 Java 数组  
**返回类型**: `Optional<Object>` 类型, 可以根据其是否为空来判断是否转换成功  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `className` | 字符串类型 | Java 类名, 代表要转换成该类型的数组 |
| `array` | `Lua Table` | 数组风格的 `Lua Table`, 其元素类型应该单一 |

**例子**: 
```lua
-- Class<?>[];
local array = luaBukkit.helper:castArray(
    "java.lang.Class", 
    { luaBukkit.helper, luaBukkit.helper }
)
```

#### castArray - 将 LuaTable 转换为 Java 数组

**方法说明**: 将数组风格的 `LuaTable` 转换为 Java 数组  
**返回类型**: `Optional<Object>` 类型, 可以根据其是否为空来判断是否转换成功  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `className` | `Class<?>` | Java 类, 代表要转换成该类型的数组 |
| `array` | `Lua Table` | 数组风格的 `Lua Table`, 其元素类型应该单一 |

**例子**: 
```lua
local Class = luajava.bindClass("java.lang.Class")
-- Class<?>[];
local array = luaBukkit.helper:castArray(
    Class, 
    { luaBukkit.helper, luaBukkit.helper }
)
```

### LuaIOHelper

源代码: [LuaIOHelper]  
使用方法: `luaBukkit.io:方法名`

#### transferAndClose - 将输入流传输至输出流并关闭输入流与输出流

**方法说明**: 将输入流传输至输出流并关闭输入流与输出流  
**返回类型**: 无  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `inputStream`  | `InputStream`  | 输入流 |
| `outputStream` | `OutputStream` | 输出流 |
| `bufferSize`   | `Number`       | 缓冲区大小 |

#### transfer - 将输入流传输至输出流

**方法说明**: 将输入流传输至输出流, 仅进行传输, 不关闭流.  
**返回类型**: 无  
**形参列表**:
| 形参 | 形参类型 | 说明 |
| :-: | :-: | :-: |
| `inputStream`  | `InputStream`  | 输入流 |
| `outputStream` | `OutputStream` | 输出流 |
| `bufferSize`   | `Number`       | 缓冲区大小 |

