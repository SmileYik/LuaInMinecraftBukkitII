[history]: https://github.com/SmileYik/LuaInMinecraftBukkitII/commits/gh-page/docs/GlobalVariable.md
[LuaJava]: https://github.com/SmileYik/luajava
[LuaInMinecraftBukkit II]: https://github.com/SmileYik/LuaInMinecraftBukkitII

[ILuaEnv]: https://github.com/SmileYik/LuaInMinecraftBukkitII/blob/master/src/main/java/org/eu/smileyik/luaInMinecraftBukkitII/api/lua/luaState/ILuaEnv.java
[LuaHelper]: https://github.com/SmileYik/LuaInMinecraftBukkitII/blob/master/src/main/java/org/eu/smileyik/luaInMinecraftBukkitII/api/lua/luaState/LuaHelper.java
[LuaIOHelper]: https://github.com/SmileYik/LuaInMinecraftBukkitII/blob/master/src/main/java/org/eu/smileyik/luaInMinecraftBukkitII/api/lua/luaState/LuaIOHelper.java
[LuaInMinecraftBukkit]: https://github.com/SmileYik/LuaInMinecraftBukkitII/blob/master/src/main/java/org/eu/smileyik/luaInMinecraftBukkitII/LuaInMinecraftBukkit.java
[ILuaEventListenerBuilder]: https://github.com/SmileYik/LuaInMinecraftBukkitII/blob/master/src/main/java/org/eu/smileyik/luaInMinecraftBukkitII/api/lua/luaState/event/ILuaEventListenerBuilder.java
[ILuaCommandClassBuilder]: https://github.com/SmileYik/LuaInMinecraftBukkitII/blob/master/src/main/java/org/eu/smileyik/luaInMinecraftBukkitII/api/lua/luaState/command/ILuaCommandClassBuilder.java

[Bukkit]: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Bukkit.html
[Server]: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Server.html
[Logger]: https://docs.oracle.com/en/java/javase/17/docs/api/java.logging/java/util/logging/Logger.html
[PrintStream]: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/io/PrintStream.html

[Command Section]: ./../Command.md
[EventListener Section]: ./../EventListener.md

> Last updated on October 03, 2025 | [History][History]

> The content of this page corresponds to the latest version of the LuaInMinecraftBukkit II plugin, **1.1.0**. For historical documentation, please check the historical records of this page.

> **!! The content of this file has machine translation !!** | [Origin](./../zh/GlobalVariable.md)

There are some global variables in the Lua section to make it easier for you to interact between Lua and the Bukkit server.

## luajava

[LuaInMinecraftBukkit II][LuaInMinecraftBukkit II] is dependent on the [LuaJava][LuaJava] project, and [LuaJava][LuaJava] contains a global table named `luajava` to facilitate user interaction with Java.

### luajava.bindClass - Get a Java class

**Method Description**: This method is used to get a Java class. The returned Java class cannot use the methods originally in `Class<?>`, but can only use the static methods of the target class or access static fields.  
**Return Type**: `Class<?>` instance  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `className` | String | Java fully qualified class name |

**Example**:

```lua
local BorderLayout = luajava.bindClass("java.awt.BorderLayout")
BorderLayout.NORTH -- Get the public static field `NORTH` in the BorderLayout class
```

### luajava.class2Obj - Convert a Java Class\<?\> instance to a normal Java instance

**Method Description**: This method is used to open a `Class<?>` instance as an `Object` instance. In fact, whether or not this method is used, the `Class<?>` instance in the variable is always the same; it's just that the way Lua variables handle the `Class<?>` instance is different.  
**Return Type**: `Class<?>` instance stored as a Java `Object`  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `class` | `Class<?>` | The Java class instance to be converted |

**Example**:
Iterate through all methods in `java.awt.BorderLayout`.

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

### luajava.new - Construct a Java instance based on Class\<?\>

**Method Description**: This method is used to construct an instance of a specified Java class.  
**Return Type**: An instance of the specified Java class  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `class` | `Class<?>` | The Java class to be constructed |
| `...` | Variable arguments | The public constructor parameters of the Java class |

**Example**:
Construct an instance of the `java.awt.Frame` class.

```lua
local Frame = luajava.bindClass("java.awt.Frame")
local frame = luajava.new(Frame, "Window created by luajava.new method")
frame:show()
```

### luajava.newInstance - Construct a Java instance based on a class name

**Method Description**: This method is used to construct an instance of a specified Java class.  
**Return Type**: An instance of the specified Java class  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `className` | String | The name of the Java class to be constructed |
| `...` | Variable arguments | The public constructor parameters of the Java class |

**Example**:
Construct an instance of the `java.awt.Frame` class.

```lua
local frame = luajava.newInstance("java.awt.Frame", "Window created by luajava.newInstance method")
frame:show()
```

### luajava.createProxy - Create a Java interface proxy instance

**Method Description**: This method is used to create a proxy instance for a Java interface.  
**Return Type**: An instance of the specified Java interface.  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `interfaceName` | String | The name of the Java interface to be proxied |
| `functionTable` | `Lua Table` | A method table. If the key name is the same as the method name in the interface, it is considered a specific implementation of that interface method. |

**Example**:
Construct an instance of the `java.lang.Runnable` interface.

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

### luajava.env - Get the current JNIEnv instance

**Method Description**: This method is used to get the JNIEnv instance attached to the current LuaState. This instance is a C/C++ pointer.  
**Return Type**: JNIEnv pointer  
**Parameter List**: None

**Example**: None

## luaBukkit

The `luaBukkit` global table is a method table added in [LuaInMinecraftBukkit II][LuaInMinecraftBukkit II], intended to assist with interaction between **Lua** and the **Bukkit** server.

In the current version, the `luaBukkit` table contains the following instances:

| Key | Value Type | Description |
| :---: | :---: | :---: |
| env    | [ILuaEnv][ILuaEnv]              | The current Lua environment, which can be used to register Bukkit events or commands. |
| helper | [LuaHelper][LuaHelper]            | Some utility methods for quickly creating Bukkit threads or converting a `LuaTable` to a Java array. |
| io     | [LuaIOHelper][LuaIOHelper]          | Utility methods related to I/O streams. |
| bukkit | [Bukkit][Bukkit]               | The Bukkit class. |
| plugin | [LuaInMinecraftBukkit][LuaInMinecraftBukkit] | The `JavaPlugin` type instance of the [LuaInMinecraftBukkit II][LuaInMinecraftBukkit II] plugin. |
| server | [Server][Server]               | The CraftServer instance. |
| log    | [Logger][Logger]               | The log printer for the current [LuaInMinecraftBukkit II][LuaInMinecraftBukkit II] plugin. |
| out    | [PrintStream][PrintStream]          | The `System.out` standard output stream. |

### ILuaEnv

Source code: [ILuaEnv][ILuaEnv]  
Usage: `luaBukkit.env:methodName`

#### listenerBuilder - Listener builder

**Method Description**: Start building a listener. See the [EventListener Section][EventListener Section] for details.  
**Return Type**: [ILuaEventListenerBuilder][ILuaEventListenerBuilder]  
**Parameter List**: None
**Example**: See the [EventListener Section][EventListener Section]

#### unregisterEventListener - Unregister a listener

**Method Description**: Unregister the listener with the specified name.  
**Return Type**: None  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `name` | String | The name of the listener |

**Example**:

```lua
-- Unregister the listener named "GreetingEvent"
luaBukkit.env:unregisterEventListener("GreetingEvent")
```

#### registerRawCommand - Register Raw Bukkit Command

**Method Description**: Constructs a raw Bukkit command. See the [Command Section] for details.  
**Return Type**: None  
**Parameter List**:  

| Parameter | Parameter Type | Description |
| :-: | :-: | :-: |
| `command` | String Type | The command name |
| `handler` | `LuaFunction` | The command handler. This handler accepts four parameters: `sender`, `command`, `label`, `args`, corresponding to the `Sender`, `Command`, `Command Label`, and `Command Arguments`. The command handler should return a value of type `bool`, i.e., `true/false`, to indicate whether the command executed successfully. |

**Example**: Please see the [Command Section]

#### commandClassBuilder - Command class builder

**Method Description**: Start building a Bukkit server command class. See the [Command Section][Command Section] for details.  
**Return Type**: [ILuaCommandClassBuilder][ILuaCommandClassBuilder]  
**Parameter List**: None
**Example**: See the [Command Section][Command Section]

#### registerCommand - Register a Bukkit command

**Method Description**: Register several command classes built by the `commandClassBuilder` method with Bukkit. See the [Command Section][Command Section] for details.  
**Return Type**: `Result<Boolean, Exception>`  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `rootCommand` | String | The root command name |
| `classes` | `LuaTable`/`Class<?>[]` | An array of command classes, an array-style `LuaTable` should be passed |

**Example**: See the [Command Section][Command Section]

#### registerCommand - Register a Bukkit command and set aliases

**Method Description**: Register several command classes built by the `commandClassBuilder` method with Bukkit. See the [Command Section][Command Section] for details.  
**Return Type**: `Result<Boolean, Exception>`  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `rootCommand` | String | The root command name |
| `aliases` | `LuaTable`/`String[]` | A string array, an array-style `LuaTable` should be passed |
| `classes` | `LuaTable`/`Class<?>[]` | An array of command classes, an array-style `LuaTable` should be passed |

**Example**: See the [Command Section][Command Section]

#### registerCleaner - Register a cleaner

**Method Description**: Register a `LuaFunction` as a cleaner to be called before the Lua environment is shut down.  
**Return Type**: None  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `cleaner` | `LuaFunction` | The cleaner |

**Example**:

```lua
-- When the Lua environment is closed, print the log "Cleaning..."
luaBukkit.env:registerCleaner(function ()
    luaBukkit.log:info("Cleaning...")
end)
```

#### registerSoftReload - Register a soft reload closure

**Method Description**: Register a `LuaFunction` as a soft reload closure to clean up data in Lua when the soft reload command is executed.  
**Return Type**: `Result<Void, String>`, returns failure message on failure.  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `luaCallable` | `LuaFunction` | The soft reload closure |

**Example**:

```lua
-- When the Lua environment is soft-restarted, reset the counter to 0 and print the log "Reloading..."
local counter = 128
luaBukkit.env:registerSoftReload(function ()
    counter = 0
    luaBukkit.log:info("Reloading...")
end)
```

#### pooledCallable - Transform a Lua closure into a closure that can be run in the Lua pool

**Method Description**: Wraps a `function() end` so it can run in the Lua pool. When it runs, it transfers the wrapped method to a **new Lua state machine**. This method should be used in a different thread from the current one. Additionally, the Lua pool must be enabled for the current Lua environment in `config.yml`.  
**Return Type**: A closure that can be run in the Lua pool.  
**Parameter List**:  
| Parameter | Parameter Type | Description |
| :-: | :-: | :-: |
| `luaCallable` | `LuaFunction` | The closure |

**Examples**:

1.  Running 2 infinite loops in parallel:

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

2.  Getting return values:

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

#### path - Get file path

**Method Description**: Get the actual file path under the Lua environment directory.  
**Return Type**: `String`, the file path.  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `path` | `String` | The file path |

**Example**:

```lua
local realPath = luaBukkit.env:path("readme.txt")
luaBukkit.log:info(realPath)
```

#### file - Get a file

**Method Description**: Get the actual file under the Lua environment directory.  
**Return Type**: `File`  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `path` | `String` | The file path |

**Example**:

```lua
local file = luaBukkit.env:file("readme.txt")
if file:exists() then
    luaBukkit.log:info("Exists!")
end
```

#### setJustUseFirstMethod - Configure Lua's Java method detection behavior

**Method Description**: Configures Lua's method detection behavior. When set to `true`, it will always automatically select and execute the first method in the candidate list instead of throwing an exception.  
**Return Type**: None  
**Parameter List**:  
| Parameter | Parameter Type | Description |
| :-: | :-: | :-: |
| `flag` | `Boolean` | Whether to automatically execute the first method in the candidate list |

**Example**:
```lua
luaBukkit.env:setJustUseFirstMethod(true)
```

#### ignoreMultiResultRun - Automatically Execute the First Java Method 

**Method Description**: As same as `setJustUseFirstMethod`. Configures Lua method detection behavior. When set to `true`, the first method in the candidate list is always automatically selected for execution instead of throwing an exception.  
**Return Type**: `Result<Object, LuaException>`  
**Parameter List**: 
| Parameter | Parameter Type | Description |
| :-: | :-: | :-: |
| `callable` | `LuaFunction` | Automatically selects the first method from candidates for execution when calling LuaFunction, instead of throwing an exception |

**Example**:
```lua
luaBukkit.env:ignoreMultiResultRun(function() 
    -- do something
end)
```

### LuaHelper

Source code: [LuaHelper][LuaHelper]  
Usage: `luaBukkit.helper:methodName`

#### runnable - Build a Runnable instance.

**Method Description**: Build a Runnable instance.  
**Return Type**: Runnable instance  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `callable` | `Lua Function` | A Lua method |

**Example**:

```lua
local runnable = luaBukkit.helper:runnable(
    function ()
        luaBukkit.log:info("Run!")
    end
)
runnable:run()
```

#### consumer - Build a Consumer instance.

**Method Description**: Build a Consumer instance.  
**Return Type**: Consumer instance  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `callable` | `Lua Function` | A Lua method |

**Example**:

```lua
local consumer = luaBukkit.helper:consumer(
    function (name)
        luaBukkit.log:info("Running out of " .. name .. "!")
    end
)
consumer:accept("coals")
```

#### syncCall - Run a Lua closure synchronously.

**Method Description**: Run a Lua method synchronously in Bukkit and mark it as completed upon completion.  
**Return Type**: A `CompletableFuture<Object>` instance, which can be used to check if it's completed and to get the return value.  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `callable` | `Lua Function` | A Lua method |

**Example**:

```lua
-- Note that Lua itself runs on the main thread
-- Execute in an asynchronous thread
luaBukkit.helper:asyncCall(
    function ()
        -- Synchronous execution
        local future = luaBukkit.helper:syncCall(
            function ()
                luaBukkit.log:info("sync call!")
                return "finished call!"
            end
        )
        -- Print the result when completed
        future:thenAccept(luaBukkit.helper:consumer(
            function (result)
                luaBukkit.log:info(result)
            end
        ))
    end
)
```

#### syncCall - Run a Lua closure synchronously, allowing for parameters

**Method Description**: Run a Lua method synchronously in Bukkit and mark it as completed upon completion.  
**Return Type**: A `CompletableFuture<Object>` instance, which can be used to check if it's completed and to get the return value.  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `callable` | `Lua Function` | A Lua method that accepts parameters |
| `params` | `Lua Array` | An array-style `Lua Table` used to pass parameters to the Lua method |

**Example**:

```lua
-- Note that Lua itself runs on the main thread
-- Execute in an asynchronous thread
luaBukkit.helper:asyncCall(
    function ()
        -- Synchronous execution
        local future = luaBukkit.helper:syncCall(
            function (prefix, suffix)
                luaBukkit.log:info(prefix .. "sync call!" .. suffix)
                return "finished call!"
            end,
            { "[Prefix]", " Finally!!!" }
        )
        -- Print the result when completed
        future:thenAccept(luaBukkit.helper:consumer(
            function (result)
                luaBukkit.log:info(result)
            end
        ))
    end
)
```

#### syncCallLater - Run a Lua closure synchronously after a delay.

**Method Description**: Run a Lua method synchronously in Bukkit after a delay and mark it as completed upon completion.  
**Return Type**: A `CompletableFuture<Object>` instance, which can be used to check if it's completed and to get the return value.  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `callable` | `Lua Function` | A Lua method |
| `tick` | `Number` | The delay time, in **ticks** |

**Example**:

```lua
-- Note that Lua itself runs on the main thread
-- Execute in an asynchronous thread
luaBukkit.helper:asyncCall(
    function ()
        -- Synchronous execution after a 20-tick (1s) delay
        local future = luaBukkit.helper:syncCallLater(
            function ()
                luaBukkit.log:info("sync call!")
                return "finished call!"
            end,
            20    -- ticks
        )
        -- Print the result when completed
        future:thenAccept(luaBukkit.helper:consumer(
            function (result)
                luaBukkit.log:info(result)
            end
        ))
    end
)
```

#### syncCallLater - Run a Lua closure synchronously after a delay, allowing for parameters

**Method Description**: Run a Lua method synchronously in Bukkit after a delay and mark it as completed upon completion.  
**Return Type**: A `CompletableFuture<Object>` instance, which can be used to check if it's completed and to get the return value.  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `callable` | `Lua Function` | A Lua method that accepts parameters |
| `tick` | `Number` | The delay time, in **ticks** |
| `params` | `Lua Array` | An array-style `Lua Table` used to pass parameters to the Lua method |

**Example**:

```lua
-- Note that Lua itself runs on the main thread
-- Execute in an asynchronous thread
luaBukkit.helper:asyncCall(
    function ()
        -- Synchronous execution after a 20-tick (1s) delay
        local future = luaBukkit.helper:syncCallLater(
            function (prefix, suffix)
                luaBukkit.log:info(prefix .. "sync call!" .. suffix)
                return "finished call!"
            end,
            20,    -- ticks
            { "[Prefix]", " Finally!!!" }
        )
        -- Print the result when completed
        future:thenAccept(luaBukkit.helper:consumer(
            function (result)
                luaBukkit.log:info(result)
            end
        ))
    end
)
```

#### syncTimer - Synchronous timer

**Important**: **When calling this method, you are responsible for managing the timer. Please release it when it is no longer needed.**

**Method Description**: Run a timer synchronously in Bukkit.  
**Return Type**: A `BukkitTask` instance.  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `callable` | `Lua Function` | A Lua method |
| `delay` | `Number` | The delay time before the timer starts running, in **ticks** |
| `period` | `Number` | The interval time for the timer to fire, in **ticks** |

**Example**:

```lua
-- Synchronous execution after a 20-tick (1s) delay, with each run interval also being 20 ticks (1s)
local timer = luaBukkit.helper:syncTimer(
    function ()
        luaBukkit.log:info("sync timer call!")
    end,
    20,   -- delay
    20    -- period
)
```

#### syncTimer - Synchronous timer, allowing for parameters

**Important**: **When calling this method, you are responsible for managing the timer. Please release it when it is no longer needed.**

**Method Description**: Run a timer synchronously in Bukkit, allowing for parameters to be passed to the `Lua Function`.  
**Return Type**: A `BukkitTask` instance.  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `callable` | `Lua Function` | A Lua method |
| `delay` | `Number` | The delay time before the timer starts running, in **ticks** |
| `period` | `Number` | The interval time for the timer to fire, in **ticks** |
| `params` | `Lua Array` | An array-style `Lua Table` used to pass parameters to the Lua method |

**Example**:

```lua
-- Synchronous execution after a 20-tick (1s) delay, with each run interval also being 20 ticks (1s)
local timer = luaBukkit.helper:syncTimer(
    function (prefix, suffix)
        luaBukkit.log:info(prefix .. "sync timer call!" .. suffix)
    end,
    20,    -- delay
    20,    -- period
    { "[Timer] ", " See you next time!" }
)
```

#### asyncCall - Run a Lua closure asynchronously.

**Method Description**: Run a Lua method asynchronously in Bukkit and mark it as completed upon completion.  
**Return Type**: A `CompletableFuture<Object>` instance, which can be used to check if it's completed and to get the return value.  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `callable` | `Lua Function` | A Lua method |

**Example**:

```lua
-- Note that Lua itself runs on the main thread
-- Execute in an asynchronous thread
luaBukkit.helper:asyncCall(
    function ()
        -- Asynchronous execution
        local future = luaBukkit.helper:asyncCall(
            function ()
                luaBukkit.log:info("async call!")
                return "finished call!"
            end
        )
        -- Print the result when completed
        future:thenAccept(luaBukkit.helper:consumer(
            function (result)
                luaBukkit.log:info(result)
            end
        ))
    end
)
```

#### asyncCall - Run a Lua closure asynchronously, allowing for parameters

**Method Description**: Run a Lua method asynchronously in Bukkit and mark it as completed upon completion.  
**Return Type**: A `CompletableFuture<Object>` instance, which can be used to check if it's completed and to get the return value.  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `callable` | `Lua Function` | A Lua method that accepts parameters |
| `params` | `Lua Array` | An array-style `Lua Table` used to pass parameters to the Lua method |

**Example**:

```lua
-- Note that Lua itself runs on the main thread
-- Execute in an asynchronous thread
luaBukkit.helper:asyncCall(
    function ()
        -- Asynchronous execution
        local future = luaBukkit.helper:asyncCall(
            function (prefix, suffix)
                luaBukkit.log:info(prefix .. "async call!" .. suffix)
                return "finished call!"
            end,
            { "[Prefix]", " Finally!!!" }
        )
        -- Print the result when completed
        future:thenAccept(luaBukkit.helper:consumer(
            function (result)
                luaBukkit.log:info(result)
            end
        ))
    end
)
```

#### asyncCallLater - Run a Lua closure asynchronously after a delay.

**Method Description**: Run a Lua method asynchronously in Bukkit after a delay and mark it as completed upon completion.  
**Return Type**: A `CompletableFuture<Object>` instance, which can be used to check if it's completed and to get the return value.  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `callable` | `Lua Function` | A Lua method |
| `tick` | `Number` | The delay time, in **ticks** |

**Example**:

```lua
-- Note that Lua itself runs on the main thread
-- Execute in an asynchronous thread
luaBukkit.helper:asyncCall(
    function ()
        -- Asynchronous execution after a 20-tick (1s) delay
        local future = luaBukkit.helper:asyncCallLater(
            function ()
                luaBukkit.log:info("async call!")
                return "finished call!"
            end,
            20    -- ticks
        )
        -- Print the result when completed
        future:thenAccept(luaBukkit.helper:consumer(
            function (result)
                luaBukkit.log:info(result)
            end
        ))
    end
)
```

#### asyncCallLater - Run a Lua closure asynchronously after a delay, allowing for parameters

**Method Description**: Run a Lua method asynchronously in Bukkit after a delay and mark it as completed upon completion.  
**Return Type**: A `CompletableFuture<Object>` instance, which can be used to check if it's completed and to get the return value.  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `callable` | `Lua Function` | A Lua method that accepts parameters |
| `tick` | `Number` | The delay time, in **ticks** |
| `params` | `Lua Array` | An array-style `Lua Table` used to pass parameters to the Lua method |

**Example**:

```lua
-- Note that Lua itself runs on the main thread
-- Execute in an asynchronous thread
luaBukkit.helper:asyncCall(
    function ()
        -- Asynchronous execution after a 20-tick (1s) delay
        local future = luaBukkit.helper:asyncCallLater(
            function (prefix, suffix)
                luaBukkit.log:info(prefix .. "async call!" .. suffix)
                return "finished call!"
            end,
            20,    -- ticks
            { "[Prefix]", " Finally!!!" }
        )
        -- Print the result when completed
        future:thenAccept(luaBukkit.helper:consumer(
            function (result)
                luaBukkit.log:info(result)
            end
        ))
    end
)
```

#### asyncTimer - Asynchronous timer

**Important**: **When calling this method, you are responsible for managing the timer. Please release it when it is no longer needed.**

**Method Description**: Run a timer asynchronously in Bukkit.  
**Return Type**: A `BukkitTask` instance.  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `callable` | `Lua Function` | A Lua method |
| `delay` | `Number` | The delay time before the timer starts running, in **ticks** |
| `period` | `Number` | The interval time for the timer to fire, in **ticks** |

**Example**:

```lua
-- Asynchronous execution after a 20-tick (1s) delay, with each run interval also being 20 ticks (1s)
local timer = luaBukkit.helper:asyncTimer(
    function ()
        luaBukkit.log:info("async timer call!")
    end,
    20,   -- delay
    20    -- period
)
```

#### asyncTimer - Asynchronous timer, allowing for parameters

**Important**: **When calling this method, you are responsible for managing the timer. Please release it when it is no longer needed.**

**Method Description**: Run a timer asynchronously in Bukkit, allowing for parameters to be passed to the `Lua Function`.  
**Return Type**: A `BukkitTask` instance.  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `callable` | `Lua Function` | A Lua method |
| `delay`    | `Number`       | The delay time before the timer starts running, in **ticks** |
| `period`   | `Number`       | The interval time for the timer to fire, in **ticks** |
| `params`   | `Lua Array`    | An array-style `Lua Table` used to pass parameters to the Lua method |

**Example**:

```lua
-- Asynchronous execution after a 20-tick (1s) delay, with each run interval also being 20 ticks (1s)
local timer = luaBukkit.helper:asyncTimer(
    function (prefix, suffix)
        luaBukkit.log:info(prefix .. "async timer call!" .. suffix)
    end,
    20,    -- delay
    20,    -- period
    { "[Timer] ", " See you next time!" }
)
```

#### castArray - Convert a LuaTable to a Java array

**Method Description**: Convert an array-style `LuaTable` to a Java array.  
**Return Type**: An `Optional<Object>` type, which can be checked for null to determine if the conversion was successful.  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `className` | String | The Java class name, representing the type of array to convert to |
| `array` | `Lua Table` | An array-style `Lua Table`, whose elements should be of a single type |

**Example**:

```lua
-- Class<?>[];
local array = luaBukkit.helper:castArray(
    "java.lang.Class",
    { luaBukkit.helper, luaBukkit.helper }
)
```

#### castArray - Convert a LuaTable to a Java array

**Method Description**: Convert an array-style `LuaTable` to a Java array.  
**Return Type**: An `Optional<Object>` type, which can be checked for null to determine if the conversion was successful.  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `className` | `Class<?>` | The Java class, representing the type of array to convert to |
| `array` | `Lua Table` | An array-style `Lua Table`, whose elements should be of a single type |

**Example**:

```lua
local Class = luajava.bindClass("java.lang.Class")
-- Class<?>[];
local array = luaBukkit.helper:castArray(
    Class,
    { luaBukkit.helper, luaBukkit.helper }
)
```

### LuaIOHelper

Source code: [LuaIOHelper][LuaIOHelper]  
Usage: `luaBukkit.io:methodName`

#### transferAndClose - Transfer input stream to output stream and close both

**Method Description**: Transfer an input stream to an output stream and close both streams.  
**Return Type**: None  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `inputStream`  | `InputStream`  | The input stream |
| `outputStream` | `OutputStream` | The output stream |
| `bufferSize`   | `Number`       | The buffer size |

#### transfer - Transfer input stream to output stream

**Method Description**: Transfer an input stream to an output stream. Only performs the transfer, does not close the streams.  
**Return Type**: None  
**Parameter List**:

| Parameter | Parameter Type | Description |
| :---: | :---: | :---: |
| `inputStream`  | `InputStream`  | The input stream |
| `outputStream` | `OutputStream` | The output stream |
| `bufferSize`   | `Number`       | The buffer size |
